export class AiProviderError extends Error {
  constructor(code, message, options = {}) {
    super(message, options);
    this.name = "AiProviderError";
    this.code = code;
  }
}

