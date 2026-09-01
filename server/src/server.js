import { createHash, randomUUID, timingSafeEqual } from "node:crypto";
import http from "node:http";
import { pathToFileURL } from "node:url";
import { AiProviderError, createAiProvider } from "./providers/index.js";

function corsHeaders(request, allowedOrigin) {
  const origin = request.headers.origin;
  return allowedOrigin && origin === allowedOrigin ? { "Access-Control-Allow-Origin": allowedOrigin, Vary: "Origin" } : {};
}

const MAX_IMAGE_BYTES = 1_000_000;
const MAX_NUTRITION_BODY_BYTES = 1_400_000;

class RequestError extends Error {
  constructor(code, message) {
    super(message);
    this.name = "RequestError";
    this.code = code;
  }
}

function requestIdFor(request) {
  const supplied = request.headers["x-request-id"];
  return typeof supplied === "string" && /^[A-Za-z0-9._-]{8,80}$/.test(supplied) ? supplied : randomUUID();
}

function sendJson(request, response, status, body, allowedOrigin, requestId) {
  response.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store",
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "no-referrer",
    "X-Request-Id": requestId,
    ...corsHeaders(request, allowedOrigin)
  });
  response.end(JSON.stringify(body));
}

async function readJson(request, maximumBytes = 128_000) {
  const chunks = [];
  let receivedBytes = 0;
  for await (const chunk of request) {
    receivedBytes += chunk.length;
    if (receivedBytes > maximumBytes) throw new RequestError("request_too_large", "Request too large");
    chunks.push(chunk);
  }
  try {
    return JSON.parse(Buffer.concat(chunks).toString("utf8"));
  } catch (error) {
    throw new RequestError("invalid_json", "Invalid JSON", { cause: error });
  }
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
  const hasImage = validateImageDataUrl(imageDataUrl);
  return (hasDescription || hasImage) && (!imageDataUrl || hasImage);
}

function validateImageDataUrl(imageDataUrl) {
  if (typeof imageDataUrl !== "string") return false;
  const match = /^data:image\/(?:jpeg|png|webp);base64,([A-Za-z0-9+/]+={0,2})$/.exec(imageDataUrl);
  if (!match || match[1].length % 4 !== 0) return false;
  return Buffer.from(match[1], "base64").length <= MAX_IMAGE_BYTES;
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

function providerErrorResponse(request, response, error, allowedOrigin, providerId, requestId) {
  const status = error.code === "not_configured" ? 503 : 502;
  console.error("AI provider request failed", requestId, providerId, error.code);
  sendJson(request, response, status, { error: status === 503 ? "The AI service is not configured" : "The AI service could not complete this request", code: error.code, requestId }, allowedOrigin, requestId);
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
    const requestId = requestIdFor(request);
    const execution = { provider: aiProvider.id, credentialMode: "managed", storage: "none" };
    if (request.method === "OPTIONS") {
      response.writeHead(204, { "Access-Control-Allow-Methods": "POST, OPTIONS", "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Request-Id", "X-Request-Id": requestId, ...corsHeaders(request, allowedOrigin) });
      response.end();
      return;
    }
    if (request.method === "GET" && request.url === "/healthz") {
      const configured = aiProvider.isConfigured && Boolean(apiToken);
      sendJson(request, response, configured ? 200 : 503, {
        status: configured ? "ok" : "not_configured",
        provider: aiProvider.id,
        capabilities: aiProvider.capabilities,
        mode: "stateless-ai-bridge",
        storage: "none"
      }, allowedOrigin, requestId);
      return;
    }
    if (request.method !== "POST" || !["/v1/chat", "/v1/nutrition/analyze"].includes(request.url)) {
      sendJson(request, response, 404, { error: "Not found", code: "not_found", requestId }, allowedOrigin, requestId);
      return;
    }

    const token = receivedToken(request);
    if (!sameToken(apiToken, token)) {
      sendJson(request, response, 401, { error: "Unauthorized", code: "unauthorized", requestId }, allowedOrigin, requestId);
      return;
    }
    if (!rateLimit(token)) {
      sendJson(request, response, 429, { error: "Too many requests. Try again later.", code: "rate_limited", requestId }, allowedOrigin, requestId);
      return;
    }

    try {
      if (request.url === "/v1/nutrition/analyze") {
        const { description = "", imageDataUrl = "" } = await readJson(request, MAX_NUTRITION_BODY_BYTES);
        if (!validateNutritionRequest(description, imageDataUrl)) {
          sendJson(request, response, 400, { error: "Invalid nutrition request", code: "invalid_nutrition_request", requestId }, allowedOrigin, requestId);
          return;
        }
        if (!aiProvider.isConfigured) throw new AiProviderError("not_configured", "The AI provider is not configured");
        if (imageDataUrl && !aiProvider.capabilities.imageNutrition) {
          sendJson(request, response, 422, { error: "Image nutrition analysis is not supported by the configured AI provider", code: "capability_unavailable", requestId }, allowedOrigin, requestId);
          return;
        }
        const estimate = await aiProvider.analyzeNutrition({ description: description.trim(), imageDataUrl });
        sendJson(request, response, 200, { estimate, execution }, allowedOrigin, requestId);
        return;
      }

      const { messages, memories = [], webSearch = false } = await readJson(request);
      if (!validateMessages(messages) || !validateMemories(memories) || typeof webSearch !== "boolean") {
        sendJson(request, response, 400, { error: "Invalid chat request", code: "invalid_chat_request", requestId }, allowedOrigin, requestId);
        return;
      }
      if (!aiProvider.isConfigured) throw new AiProviderError("not_configured", "The AI provider is not configured");
      if (webSearch && !aiProvider.capabilities.webSearch) {
        sendJson(request, response, 422, { error: "Web search is not supported by the configured AI provider", code: "capability_unavailable", requestId }, allowedOrigin, requestId);
        return;
      }
      const result = await aiProvider.chat({ messages, memories, webSearch });
      sendJson(request, response, 200, { ...result, execution }, allowedOrigin, requestId);
    } catch (error) {
      if (error instanceof AiProviderError) {
        providerErrorResponse(request, response, error, allowedOrigin, aiProvider.id, requestId);
        return;
      }
      const code = error instanceof RequestError ? error.code : "invalid_request";
      console.error("KF20 request failed", requestId, code);
      sendJson(request, response, code === "request_too_large" ? 413 : 400, { error: code === "request_too_large" ? "Request too large" : "Invalid request", code, requestId }, allowedOrigin, requestId);
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

