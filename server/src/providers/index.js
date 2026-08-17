import { createOpenAiProvider } from "./openai.js";

export function createAiProvider({ env = process.env, fetchImpl = globalThis.fetch } = {}) {
  const providerId = (env.AI_PROVIDER ?? "openai").trim().toLowerCase();
  if (providerId === "openai") {
    return createOpenAiProvider({
      apiKey: env.OPENAI_API_KEY,
      model: env.AI_MODEL ?? env.OPENAI_MODEL ?? "gpt-5.6",
      fetchImpl
    });
  }
  throw new Error(`Unsupported AI_PROVIDER: ${providerId}`);
}

export { AiProviderError } from "./errors.js";

