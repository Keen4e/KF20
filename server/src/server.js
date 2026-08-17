import { createHash, timingSafeEqual } from "node:crypto";
import http from "node:http";
import { pathToFileURL } from "node:url";
import { AiProviderError, createAiProvider } from "./providers/index.js";

function corsHeaders(request, allowedOrigin) {
  const origin = request.headers.origin;
  return allowedOrigin && origin === allowedOrigin ? { "Access-Control-Allow-Origin": allowedOrigin, Vary: "Origin" } : {};
}

function sendJson(request, response, status, body, allowedOrigin) {
  response.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store",
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "no-referrer",
    ...corsHeaders(request, allowedOrigin)
  });
  response.end(JSON.stringify(body));
}

async function readJson(request, maximumBytes = 128_000) {
  let raw = "";
  for await (const chunk of request) {
    raw += chunk;
    if (raw.length > maximumBytes) throw new Error("Request too large");
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

function validateNutritionRequest(description, imageDataUrl) {
  const hasDescription = typeof description === "string" && description.trim().length > 0 && description.length <= 4_000;
  const hasImage = typeof imageDataUrl === "string" && /^data:image\/(jpeg|png|webp);base64,/.test(imageDataUrl) && imageDataUrl.length <= 1_500_000;
  return (hasDescription || hasImage) && (!imageDataUrl || hasImage);
}

function receivedToken(request) {
  const authorization = request.headers.authorization;
  return typeof authorization === "string" && authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
}

function sameToken(expectedToken, token) {
  if (!expectedToken || !token) return false;
  const expected = Buffer.from(expectedToken);
  const actual = Buffer.from(token);
  return expected.length === actual.length && timingSafeEqual(expected, actual);
}

function providerErrorResponse(request, response, error, allowedOrigin, providerId) {
  const status = error.code === "not_configured" ? 503 : 502;
  console.error("AI provider request failed", providerId, error.code);
  sendJson(request, response, status, { error: status === 503 ? "The AI service is not configured" : "The AI service could not complete this request", code: error.code }, allowedOrigin);
}

export function createKf20Server({ env = process.env, aiProvider = createAiProvider({ env }) } = {}) {
  const apiToken = env.KF20_API_TOKEN;
  const allowedOrigin = env.KF20_ALLOWED_ORIGIN ?? "";
  const maxRequestsPerWindow = Number(env.KF20_RATE_LIMIT ?? 30);
  const rateWindowMs = 10 * 60_000;
  const rateBuckets = new Map();

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

  const cleanupTimer = setInterval(() => {
    const cutoff = Date.now() - rateWindowMs;
    for (const [key, bucket] of rateBuckets) if (bucket.startedAt < cutoff) rateBuckets.delete(key);
  }, rateWindowMs);
  cleanupTimer.unref();

  const server = http.createServer(async (request, response) => {
    if (request.method === "OPTIONS") {
      response.writeHead(204, { "Access-Control-Allow-Methods": "POST, OPTIONS", "Access-Control-Allow-Headers": "Content-Type, Authorization", ...corsHeaders(request, allowedOrigin) });
      response.end();
      return;
    }
    if (request.method === "GET" && request.url === "/healthz") {
      const configured = aiProvider.isConfigured && Boolean(apiToken);
      sendJson(request, response, configured ? 200 : 503, {
        status: configured ? "ok" : "not_configured",
        provider: aiProvider.id,
        capabilities: aiProvider.capabilities
      }, allowedOrigin);
      return;
    }
    if (request.method !== "POST" || !["/v1/chat", "/v1/nutrition/analyze"].includes(request.url)) {
      sendJson(request, response, 404, { error: "Not found" }, allowedOrigin);
      return;
    }

    const token = receivedToken(request);
    if (!sameToken(apiToken, token)) {
      sendJson(request, response, 401, { error: "Unauthorized" }, allowedOrigin);
      return;
    }
    if (!rateLimit(token)) {
      sendJson(request, response, 429, { error: "Too many requests. Try again later." }, allowedOrigin);
      return;
    }

    try {
      if (request.url === "/v1/nutrition/analyze") {
        const { description = "", imageDataUrl = "" } = await readJson(request, 1_500_000);
        if (!validateNutritionRequest(description, imageDataUrl)) {
          sendJson(request, response, 400, { error: "Invalid nutrition request" }, allowedOrigin);
          return;
        }
        if (!aiProvider.isConfigured) throw new AiProviderError("not_configured", "The AI provider is not configured");
        if (imageDataUrl && !aiProvider.capabilities.imageNutrition) {
          sendJson(request, response, 422, { error: "Image nutrition analysis is not supported by the configured AI provider", code: "capability_unavailable" }, allowedOrigin);
          return;
        }
        const estimate = await aiProvider.analyzeNutrition({ description: description.trim(), imageDataUrl });
        sendJson(request, response, 200, { estimate }, allowedOrigin);
        return;
      }

      const { messages, memories = [], webSearch = false } = await readJson(request);
      if (!validateMessages(messages) || !validateMemories(memories) || typeof webSearch !== "boolean") {
        sendJson(request, response, 400, { error: "Invalid chat request" }, allowedOrigin);
        return;
      }
      if (!aiProvider.isConfigured) throw new AiProviderError("not_configured", "The AI provider is not configured");
      if (webSearch && !aiProvider.capabilities.webSearch) {
        sendJson(request, response, 422, { error: "Web search is not supported by the configured AI provider", code: "capability_unavailable" }, allowedOrigin);
        return;
      }
      const result = await aiProvider.chat({ messages, memories, webSearch });
      sendJson(request, response, 200, result, allowedOrigin);
    } catch (error) {
      if (error instanceof AiProviderError) {
        providerErrorResponse(request, response, error, allowedOrigin, aiProvider.id);
        return;
      }
      console.error("KF20 request failed", error instanceof Error ? error.name : "unknown");
      sendJson(request, response, 400, { error: "Invalid request" }, allowedOrigin);
    }
  });

  server.on("close", () => clearInterval(cleanupTimer));
  return server;
}

function start() {
  const env = process.env;
  const port = Number(env.PORT ?? 8787);
  let aiProvider;
  try {
    aiProvider = createAiProvider({ env });
  } catch (error) {
    console.error(error instanceof Error ? error.message : "AI provider configuration failed");
    process.exitCode = 1;
    return;
  }
  if (!aiProvider.isConfigured || !env.KF20_API_TOKEN) console.warn("AI provider credentials and KF20_API_TOKEN must be configured before chat is available.");
  createKf20Server({ env, aiProvider }).listen(port, () => console.log(`KF20 API listening on ${port} with ${aiProvider.id}`));
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) start();

