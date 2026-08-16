import { createHash, timingSafeEqual } from "node:crypto";
import http from "node:http";

const port = Number(process.env.PORT ?? 8787);
const apiKey = process.env.OPENAI_API_KEY;
const apiToken = process.env.KF20_API_TOKEN;
const model = process.env.OPENAI_MODEL ?? "gpt-5.6";
const allowedOrigin = process.env.KF20_ALLOWED_ORIGIN ?? "";
const maxRequestsPerWindow = Number(process.env.KF20_RATE_LIMIT ?? 30);
const rateWindowMs = 10 * 60_000;
const rateBuckets = new Map();

if (!apiKey || !apiToken) console.warn("OPENAI_API_KEY and KF20_API_TOKEN must be configured before chat is available.");

function corsHeaders(request) {
  const origin = request.headers.origin;
  return allowedOrigin && origin === allowedOrigin ? { "Access-Control-Allow-Origin": allowedOrigin, Vary: "Origin" } : {};
}

function sendJson(request, response, status, body) {
  response.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store",
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "no-referrer",
    ...corsHeaders(request)
  });
  response.end(JSON.stringify(body));
}

async function readJson(request) {
  let raw = "";
  for await (const chunk of request) {
    raw += chunk;
    if (raw.length > 128_000) throw new Error("Request too large");
  }
  return JSON.parse(raw);
}

function validateMessages(messages) {
  if (!Array.isArray(messages) || messages.length === 0 || messages.length > 40) return false;
  return messages.every((message) => message && (message.role === "user" || message.role === "assistant") && typeof message.content === "string" && message.content.length > 0 && message.content.length <= 12_000);
}

function validateMemories(memories) {
  return Array.isArray(memories) && memories.length <= 20 && memories.every((memory) => typeof memory === "string" && memory.length <= 1_000);
}

function receivedToken(request) {
  const authorization = request.headers.authorization;
  return typeof authorization === "string" && authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
}

function sameToken(token) {
  if (!apiToken || !token) return false;
  const expected = Buffer.from(apiToken);
  const actual = Buffer.from(token);
  return expected.length === actual.length && timingSafeEqual(expected, actual);
}

function rateLimit(token) {
  const now = Date.now();
  const key = createHash("sha256").update(token).digest("hex");
  const bucket = rateBuckets.get(key);
  if (!bucket || now - bucket.startedAt >= rateWindowMs) {
    rateBuckets.set(key, { startedAt: now, count: 1 });
    return true;
  }
  bucket.count += 1;
  return bucket.count <= maxRequestsPerWindow;
}

setInterval(() => {
  const cutoff = Date.now() - rateWindowMs;
  for (const [key, bucket] of rateBuckets) if (bucket.startedAt < cutoff) rateBuckets.delete(key);
}, rateWindowMs).unref();

const server = http.createServer(async (request, response) => {
  if (request.method === "OPTIONS") {
    response.writeHead(204, { "Access-Control-Allow-Methods": "POST, OPTIONS", "Access-Control-Allow-Headers": "Content-Type, Authorization", ...corsHeaders(request) });
    response.end();
    return;
  }
  if (request.method === "GET" && request.url === "/healthz") {
    sendJson(request, response, apiKey && apiToken ? 200 : 503, { status: apiKey && apiToken ? "ok" : "not_configured" });
    return;
  }
  if (request.method !== "POST" || request.url !== "/v1/chat") {
    sendJson(request, response, 404, { error: "Not found" });
    return;
  }

  const token = receivedToken(request);
  if (!sameToken(token)) {
    sendJson(request, response, 401, { error: "Unauthorized" });
    return;
  }
  if (!rateLimit(token)) {
    sendJson(request, response, 429, { error: "Too many requests. Try again later." });
    return;
  }

  try {
    const { messages, memories = [] } = await readJson(request);
    if (!validateMessages(messages) || !validateMemories(memories)) {
      sendJson(request, response, 400, { error: "Invalid chat request" });
      return;
    }
    if (!apiKey) {
      sendJson(request, response, 503, { error: "The AI service is not configured" });
      return;
    }

    const instructions = [
      "You are KF20, a private daily companion.",
      "Be helpful, calm and direct. Ask a brief follow-up when the user's goal is unclear.",
      "Do not present nutrition estimates as medical advice; state uncertainty and encourage user correction.",
      memories.length ? `User-approved memories:\n${memories.join("\n")}` : ""
    ].filter(Boolean).join("\n\n");
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 75_000);
    const upstream = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: { Authorization: `Bearer ${apiKey}`, "Content-Type": "application/json" },
      signal: controller.signal,
      body: JSON.stringify({ model, instructions, input: messages.map(({ role, content }) => ({ role, content })), store: false })
    });
    clearTimeout(timeout);
    const payload = await upstream.json();
    if (!upstream.ok) {
      console.error("OpenAI request failed", upstream.status, payload?.error?.type);
      sendJson(request, response, 502, { error: "The AI service could not complete this request" });
      return;
    }
    sendJson(request, response, 200, { text: payload.output_text ?? "" });
  } catch (error) {
    console.error("Chat request failed", error instanceof Error ? error.name : "unknown");
    sendJson(request, response, 400, { error: "Invalid request" });
  }
});

server.listen(port, () => console.log(`KF20 API listening on ${port}`));

