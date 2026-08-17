import assert from "node:assert/strict";
import test from "node:test";
import { createAiProvider } from "../src/providers/index.js";
import { createOpenAiProvider } from "../src/providers/openai.js";

test("provider selection uses server configuration only", () => {
  const provider = createAiProvider({
    env: { AI_PROVIDER: "openai", AI_MODEL: "test-model", OPENAI_API_KEY: "test-key" },
    fetchImpl: async () => ({ ok: true, json: async () => ({ output_text: "ok" }) })
  });
  assert.equal(provider.id, "openai");
  assert.equal(provider.isConfigured, true);
  assert.throws(() => createAiProvider({ env: { AI_PROVIDER: "unknown" } }), /Unsupported AI_PROVIDER/);
});

test("OpenAI chat adapter returns the stable KF20 response", async () => {
  let requestBody;
  const provider = createOpenAiProvider({
    apiKey: "test-key",
    model: "test-model",
    fetchImpl: async (_url, request) => {
      requestBody = JSON.parse(request.body);
      return {
        ok: true,
        json: async () => ({
          output_text: "Antwort",
          output: [{ content: [{ annotations: [
            { type: "url_citation", url: "https://example.com/source", title: "Beispielquelle" },
            { type: "url_citation", url: "https://example.com/source", title: "Duplikat" }
          ] }] }]
        })
      };
    }
  });

  const result = await provider.chat({
    messages: [{ role: "user", content: "Frage" }],
    memories: ["Bestätigte Erinnerung"],
    webSearch: true
  });

  assert.deepEqual(result, { text: "Antwort", sources: [{ title: "Beispielquelle", url: "https://example.com/source" }] });
  assert.equal(requestBody.model, "test-model");
  assert.equal(requestBody.store, false);
  assert.deepEqual(requestBody.tools, [{ type: "web_search" }]);
  assert.match(requestBody.instructions, /User-approved memories/);
});

test("OpenAI nutrition adapter maps text and image to the stable estimate", async () => {
  let requestBody;
  const expected = { name: "Beispielmahlzeit", calories: 420, protein: 30, fat: 14, carbs: 44, confidence: "mittel", note: "Portion geschätzt" };
  const provider = createOpenAiProvider({
    apiKey: "test-key",
    fetchImpl: async (_url, request) => {
      requestBody = JSON.parse(request.body);
      return { ok: true, json: async () => ({ output_text: JSON.stringify(expected) }) };
    }
  });

  const result = await provider.analyzeNutrition({ description: "freie Beschreibung", imageDataUrl: "data:image/jpeg;base64,AA==" });

  assert.deepEqual(result, expected);
  assert.equal(requestBody.store, false);
  assert.equal(requestBody.input[0].content[1].type, "input_image");
  assert.equal(requestBody.text.format.type, "json_schema");
  assert.equal(requestBody.text.format.strict, true);
});

