# KF20

KF20 is a private daily AI companion for Android. It provides a focused text chat, local conversation history and user-controlled long-term notes.

## Architecture

- `android/`: native Android client (Kotlin + Jetpack Compose)
- `server/`: provider-neutral KF20 API plus swappable server-side AI adapters
- `docs/`: product, security and release requirements

The Android app never contains a provider API key. It sends authenticated requests only to the KF20 server. Provider selection, model name and credentials stay behind that API boundary.

The prototype selects the OpenAI adapter with `AI_PROVIDER=openai`. `AI_MODEL` chooses the model and `OPENAI_API_KEY` is supplied only through the server's secret store. A different provider is added as a server adapter implementing `chat` and `analyzeNutrition`; the Android contracts do not change. Copy `server/.env.example` only as a local configuration template and never commit real secrets.

## Authoritative project context

Every agent handoff starts with [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md). It contains the active PI package, verified commit, current blockers and the next user decisions. Continue with:

- [`docs/PI_ROADMAP.md`](docs/PI_ROADMAP.md) – ordered features, dependencies and token forecasts
- [`SPEC.md`](SPEC.md) – product requirements and acceptance criteria
- [`docs/architecture-and-provider-contracts.md`](docs/architecture-and-provider-contracts.md) – stable API and provider boundaries
- [`docs/quality-baseline.md`](docs/quality-baseline.md) – mandatory build and test gates
- [`docs/HANDOFF.md`](docs/HANDOFF.md) – current code, verification and blocker status
- [`docs/DECISIONS.md`](docs/DECISIONS.md) – accepted product, architecture and process decisions
- [`AGENTS.md`](AGENTS.md) – mandatory maintenance rules for coding agents

## Reproducible quality check

The repository pins Gradle 8.11.1 through the checked-in wrapper and verifies its distribution checksum. With Java 21 and Android SDK 36 installed, the complete local quality gate is:

```powershell
cd android
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

On macOS or Linux, use `./gradlew` with the same tasks. The GitHub workflow runs server syntax/contract tests, Android unit tests, Android lint and the debug build as separate blocking steps.

## Current state

This is a new implementation. The prior GitHub repository only contained a compiled APK and no source code.

## Before first production deployment

1. Configure identity/authentication and a production database. The current API accepts no public users yet and must not be exposed to the internet.
2. Select the provider, add its key through a secret store and deploy `server/` to a private HTTPS endpoint.
3. Set the Android `API_BASE_URL` to that endpoint.
4. Complete the Play Store items in `docs/play-store-checklist.md`.
