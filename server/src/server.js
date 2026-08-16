import http from "node:http";

const port = Number(process.env.PORT ?? 8787);
const apiKey = process.env.OPENAI_API_KEY;
const allowedOrigin = process.env.KF20_ALLOWED_ORIGIN ?? "";

if (!apiKey) {
  console.warn("OPENAI_API_KEY is not configured. Requests will fail until it is set.");
}

function sendJson(response, status, body) {
  response.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Access-Control-Allow-Origin": allowedOrigin,
    "Vary": "Origin"
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
  return messages.every((message) =>
    message &&
    (message.role === "user" || message.role === "assistant") &&
    typeof message.content === "string" &&
    message.content.length > 0 &&
    message.content.length <= 12_000
  );
}

const server = http.createServer(async (request, response) => {
  if (request.method === "OPTIONS") {
    response.writeHead(204, {
      "Access-Control-Allow-Origin": allowedOrigin,
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
      "Vary": "Origin"
    });
    response.end();
    return;
  }

  if (request.method !== "POST" || request.url !== "/v1/chat") {
    sendJson(response, 404, { error: "Not found" });
    return;
  }

  try {
    const { messages, memories = [] } = await readJson(request);
    if (!validateMessages(messages) || !Array.isArray(memories)) {
      sendJson(response, 400, { error: "Invalid chat request" });
      return;
    }

    if (!apiKey) {
      sendJson(response, 503, { error: "The AI service is not configured" });
      return;
    }

    const instructions = [
      "You are KF20, a private daily companion.",
      "Be helpful, calm and direct. Ask a brief follow-up when the user's goal is unclear.",
      memories.length ? `User-approved memories:\n${memories.slice(0, 20).join("\n")}` : ""
    ].filter(Boolean).join("\n\n");

    const upstream = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${apiKey}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        model: "gpt-5.6",
        instructions,
        input: messages.map(({ role, content }) => ({ role, content })),
        store: false
      })
    });

    const payload = await upstream.json();
    if (!upstream.ok) {
      console.error("OpenAI request failed", upstream.status, payload?.error?.type);
      sendJson(response, 502, { error: "The AI service could not complete this request" });
      return;
    }

    sendJson(response, 200, { text: payload.output_text ?? "" });
  } catch (error) {
    console.error("Request failed", error instanceof Error ? error.message : "unknown error");
    sendJson(response, 400, { error: "Invalid request" });
  }
});

server.listen(port, () => console.log(`KF20 API listening on ${port}`));

