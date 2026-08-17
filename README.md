# KF20

KF20 is a private daily AI companion for Android. It provides a focused text chat, local conversation history and user-controlled long-term notes.

## Architecture

- `android/`: native Android client (Kotlin + Jetpack Compose)
- `server/`: provider-neutral KF20 API plus swappable server-side AI adapters
- `docs/`: product, security and release requirements

The Android app never contains a provider API key. It sends authenticated requests only to the KF20 server. Provider selection, model name and credentials stay behind that API boundary.

The prototype selects the OpenAI adapter with `AI_PROVIDER=openai`. `AI_MODEL` chooses the model and `OPENAI_API_KEY` is supplied only through the server's secret store. A different provider is added as a server adapter implementing `chat` and `analyzeNutrition`; the Android contracts do not change. Copy `server/.env.example` only as a local configuration template and never commit real secrets.

## Authoritative project context

Every implementation handoff starts with:

- [`SPEC.md`](SPEC.md) – product requirements and acceptance criteria
- [`docs/architecture-and-provider-contracts.md`](docs/architecture-and-provider-contracts.md) – stable API and provider boundaries
- [`docs/HANDOFF.md`](docs/HANDOFF.md) – current code, verification and blocker status
- [`AGENTS.md`](AGENTS.md) – mandatory maintenance rules for coding agents

## Current state

This is a new implementation. The prior GitHub repository only contained a compiled APK and no source code.

## Before first production deployment

1. Configure identity/authentication and a production database. The current API accepts no public users yet and must not be exposed to the internet.
2. Select the provider, add its key through a secret store and deploy `server/` to a private HTTPS endpoint.
3. Set the Android `API_BASE_URL` to that endpoint.
4. Complete the Play Store items in `docs/play-store-checklist.md`.

