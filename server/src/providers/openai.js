import { AiProviderError } from "./errors.js";

const nutritionSchema = {
  type: "object",
  additionalProperties: false,
  properties: {
    name: { type: "string" },
    calories: { type: "integer", minimum: 0 },
    protein: { type: "number", minimum: 0 },
    fat: { type: "number", minimum: 0 },
    carbs: { type: "number", minimum: 0 },
    confidence: { type: "string", enum: ["niedrig", "mittel", "hoch"] },
    note: { type: "string" }
  },
  required: ["name", "calories", "protein", "fat", "carbs", "confidence", "note"]
};

function extractSources(payload) {
  const sources = [];
  const seen = new Set();
  for (const output of payload?.output ?? []) {
    for (const content of output?.content ?? []) {
      for (const annotation of content?.annotations ?? []) {
        if (annotation?.type !== "url_citation") continue;
        const citation = annotation.url_citation ?? annotation;
        if (typeof citation.url !== "string" || !citation.url.startsWith("http") || seen.has(citation.url)) continue;
        seen.add(citation.url);
        sources.push({ title: typeof citation.title === "string" && citation.title ? citation.title : "Quelle", url: citation.url });
      }
    }
  }
  return sources.slice(0, 8);
}

function validateNutritionEstimate(value) {
  const confidenceValues = new Set(["niedrig", "mittel", "hoch"]);
  const nonNegative = (number) => typeof number === "number" && Number.isFinite(number) && number >= 0;
  if (!value || typeof value.name !== "string" || !value.name.trim() || !Number.isInteger(value.calories) || value.calories < 0 ||
      !nonNegative(value.protein) || !nonNegative(value.fat) || !nonNegative(value.carbs) ||
      !confidenceValues.has(value.confidence) || typeof value.note !== "string") {
    throw new AiProviderError("invalid_response", "The AI provider returned an invalid nutrition estimate");
  }
  return {
    name: value.name.trim().slice(0, 200),
    calories: value.calories,
    protein: value.protein,
    fat: value.fat,
    carbs: value.carbs,
    confidence: value.confidence,
    note: value.note.slice(0, 1_000)
  };
}

export function createOpenAiProvider({ apiKey, model = "gpt-5.6", fetchImpl = globalThis.fetch, timeoutMs = 75_000 } = {}) {
  if (typeof fetchImpl !== "function") throw new TypeError("A fetch implementation is required");

  async function createResponse(body) {
    if (!apiKey) throw new AiProviderError("not_configured", "The AI provider is not configured");
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), timeoutMs);
    try {
      const upstream = await fetchImpl("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: { Authorization: `Bearer ${apiKey}`, "Content-Type": "application/json" },
        signal: controller.signal,
        body: JSON.stringify({ model, ...body, store: false })
      });
      const payload = await upstream.json().catch(() => ({}));
      if (!upstream.ok) throw new AiProviderError("upstream_failed", "The AI provider could not complete this request");
      return payload;
    } catch (error) {
      if (error instanceof AiProviderError) throw error;
      if (error?.name === "AbortError") throw new AiProviderError("timeout", "The AI provider timed out", { cause: error });
      throw new AiProviderError("upstream_unavailable", "The AI provider is unavailable", { cause: error });
    } finally {
      clearTimeout(timeout);
    }
  }

  return Object.freeze({
    id: "openai",
    isConfigured: Boolean(apiKey),
    capabilities: Object.freeze({ webSearch: true, imageNutrition: true }),

    async chat({ messages, memories, webSearch }) {
      const instructions = [
        "You are KF20, a private daily companion.",
        "Be helpful, calm and direct. Ask a brief follow-up when the user's goal is unclear.",
        "Do not present nutrition estimates as medical advice; state uncertainty and encourage user correction.",
        memories.length ? `User-approved memories:\n${memories.join("\n")}` : ""
      ].filter(Boolean).join("\n\n");
      const payload = await createResponse({
        instructions,
        input: messages.map(({ role, content }) => ({ role, content })),
        ...(webSearch ? { tools: [{ type: "web_search" }] } : {})
      });
      return { text: typeof payload.output_text === "string" ? payload.output_text : "", sources: extractSources(payload) };
    },

    async analyzeNutrition({ description, imageDataUrl }) {
      const content = [
        { type: "input_text", text: `Estimate the nutrition for this food. Description: ${description || "No description; use the image."} Return a cautious best estimate, not medical advice.` },
        ...(imageDataUrl ? [{ type: "input_image", image_url: imageDataUrl, detail: "low" }] : [])
      ];
      const payload = await createResponse({
        instructions: "You estimate food nutrition. Give calories, protein, fat and carbohydrates in grams for the described portion. Use uncertainty honestly. Return only the required schema.",
        input: [{ role: "user", content }],
        text: { format: { type: "json_schema", name: "nutrition_estimate", strict: true, schema: nutritionSchema } }
      });
      let estimate;
      try {
        estimate = JSON.parse(payload.output_text ?? "{}");
      } catch (error) {
        throw new AiProviderError("invalid_response", "The AI provider returned invalid JSON", { cause: error });
      }
      return validateNutritionEstimate(estimate);
    }
  });
}

