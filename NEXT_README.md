# KF20

KF20 is a private daily AI companion for Android. It provides a focused text chat, local conversation history and user-controlled long-term notes.

## Architecture

- `android/`: native Android client (Kotlin + Jetpack Compose)
- `server/`: server-side API boundary for the OpenAI Responses API
- `docs/`: product, security and release requirements

The Android app never contains an OpenAI API key. It sends authenticated requests only to the KF20 server, which holds the key as a secret.

## Current state

This is a new implementation. The prior GitHub repository only contained a compiled APK and no source code.

## Before first production deployment

1. Configure identity/authentication and a production database. The current API accepts no public users yet and must not be exposed to the internet.
2. Add `OPENAI_API_KEY` and deploy `server/` to a private HTTPS endpoint.
3. Set the Android `API_BASE_URL` to that endpoint.
4. Complete the Play Store items in `docs/play-store-checklist.md`.

