# KF20 product scope (v1)

## Product promise

KF20 is a private space for the user's daily conversation with a personal AI agent. It should feel quick, calm and continuous rather than like a generic prompt tool.

## v1 functions

- Start, rename, search and archive text conversations.
- Stream an assistant answer while it is generated.
- Persist chat history locally on the device.
- Let the user save, review and delete personal memories.
- Clearly show when a message is sent to the AI service.
- Allow deleting a conversation or all local data.

## Deliberately deferred

- Voice conversations, calendar/task integrations, file analysis and multi-device sync.
- These require explicit product and privacy decisions and must not be silently added.

## Privacy baseline

- No model-provider API key in the Android package.
- Minimal server-side request logging; no chat-content logging in production.
- Chat history belongs to the user and must be exportable/deletable.
- A privacy policy and data-safety declaration are required before Play Store publication.

