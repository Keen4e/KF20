import assert from "node:assert/strict";
import test from "node:test";
import { createKf20Server } from "../src/server.js";

async function withServer(aiProvider, run) {
  const server = createKf20Server({ env: { KF20_API_TOKEN: "test-token", KF20_RATE_LIMIT: "100" }, aiProvider });
  await new Promise((resolve, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", resolve);
  });
  const { port } = server.address();
  try {
    await run(`http://127.0.0.1:${port}`);
  } finally {
    await new Promise((resolve) => server.close(resolve));
  }
}

function post(baseUrl, path, body, token = "test-token") {
  return fetch(`${baseUrl}${path}`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
}

test("KF20 API keeps chat and nutrition contracts provider-neutral", async () => {
  const calls = [];
  const estimate = { name: "Mahlzeit", calories: 500, protein: 35, fat: 18, carbs: 48, confidence: "mittel", note: "Annahme" };
  const provider = {
    id: "contract-test",
    isConfigured: true,
    capabilities: { webSearch: false, imageNutrition: true },
    async chat(input) {
      calls.push({ method: "chat", input });
      return { text: "Antwort", sources: [] };
    },
    async analyzeNutrition(input) {
      calls.push({ method: "nutrition", input });
      return estimate;
    }
  };

  await withServer(provider, async (baseUrl) => {
    const health = await fetch(`${baseUrl}/healthz`);
    assert.equal(health.status, 200);
    assert.deepEqual(await health.json(), { status: "ok", provider: "contract-test", capabilities: { webSearch: false, imageNutrition: true } });

    const unauthorized = await post(baseUrl, "/v1/chat", { messages: [{ role: "user", content: "Frage" }] }, "wrong-token");
    assert.equal(unauthorized.status, 401);

    const chat = await post(baseUrl, "/v1/chat", { messages: [{ role: "user", content: "Frage" }], memories: [], webSearch: false });
    assert.equal(chat.status, 200);
    assert.deepEqual(await chat.json(), { text: "Antwort", sources: [] });

    const nutrition = await post(baseUrl, "/v1/nutrition/analyze", { description: "Beschreibung", imageDataUrl: "" });
    assert.equal(nutrition.status, 200);
    assert.deepEqual(await nutrition.json(), { estimate });

    const unsupported = await post(baseUrl, "/v1/chat", { messages: [{ role: "user", content: "Recherche" }], memories: [], webSearch: true });
    assert.equal(unsupported.status, 422);
    assert.equal((await unsupported.json()).code, "capability_unavailable");

    provider.capabilities.imageNutrition = false;
    const unsupportedImage = await post(baseUrl, "/v1/nutrition/analyze", { description: "Beschreibung", imageDataUrl: "data:image/jpeg;base64,AA==" });
    assert.equal(unsupportedImage.status, 422);
    assert.equal((await unsupportedImage.json()).code, "capability_unavailable");
  });

  assert.deepEqual(calls.map(({ method }) => method), ["chat", "nutrition"]);
  assert.equal(calls[1].input.description, "Beschreibung");
});

test("KF20 API rejects malformed requests before calling the provider", async () => {
  let called = false;
  const provider = {
    id: "contract-test",
    isConfigured: true,
    capabilities: { webSearch: true, imageNutrition: true },
    async chat() { called = true; },
    async analyzeNutrition() { called = true; }
  };

  await withServer(provider, async (baseUrl) => {
    const chat = await post(baseUrl, "/v1/chat", { messages: [] });
    assert.equal(chat.status, 400);
    const nutrition = await post(baseUrl, "/v1/nutrition/analyze", { description: "   ", imageDataUrl: "" });
    assert.equal(nutrition.status, 400);
  });
  assert.equal(called, false);
});

