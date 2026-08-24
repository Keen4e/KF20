# KF20 – Arbeitsregeln für KI-Agenten

Dieses Repository muss ohne Zugriff auf frühere Chats übernehmbar bleiben. Vor Änderungen sind diese Dateien vollständig zu lesen:

1. `SPEC.md` – verbindliche Produktanforderungen und Abnahmekriterien
2. `docs/architecture-and-provider-contracts.md` – technische Grenzen und stabile API-Verträge
3. `docs/HANDOFF.md` – aktueller Implementierungs-, Test- und Blockerstatus
4. `docs/chat-derived-requirements.md` – aus dem privaten Export abstrahierte Anforderungen
5. `docs/quality-baseline.md` – verbindliche Build-, Test- und CI-Gates

## Verbindliche Pflege

- Jede Funktionsänderung aktualisiert im selben Branch den Code, `SPEC.md` und `docs/HANDOFF.md`.
- Architektur- oder API-Änderungen aktualisieren zusätzlich `docs/architecture-and-provider-contracts.md`.
- Der Handoff nennt den letzten verifizierten Commit und den exakten CI-/Teststatus. Ein grüner älterer Build belegt nicht den aktuellen Stand.
- Persönliche Chattexte, Fotos, Tokens, Serveradressen und konkrete Gesundheitswerte dürfen nicht in Git, Spezifikation, Fixtures oder Logs gelangen.
- Neue Gesundheitsfelder werden nur aufgenommen, wenn sie vom Nutzer bestätigt oder im abstrahierten Anforderungskatalog belegt sind.
- Android kennt keinen konkreten KI-Anbieter. Provider-spezifischer Code bleibt hinter der Server-API.
- Vor einem Store-Upload müssen alle Gates aus `docs/security-release-gates.md` und `docs/play-store-checklist.md` nachweislich erfüllt sein.

## Qualitätsregeln

- Nutzerwerte lokal verschlüsseln; Schlüssel aus Android Keystore.
- KI-Schätzungen sichtbar kennzeichnen und vor dem Speichern korrigierbar machen.
- Externe Aktionen niemals still ausführen.
- Nach Änderungen Server-Syntax-/Vertragstests sowie `:app:testDebugUnitTest`, `:app:lintDebug` und `:app:assembleDebug` über den eingecheckten Gradle-Wrapper ausführen; Ergebnis im Handoff vermerken.
