# KF20 – Arbeitsregeln für KI-Agenten

Dieses Repository muss ohne Zugriff auf frühere Chats übernehmbar bleiben. Vor Änderungen sind diese Dateien vollständig und in dieser Reihenfolge zu lesen:

1. `docs/PROJECT_STATUS.md` – einzige Quelle für Phase, aktives Paket, Blocker und nächste Entscheidungen
2. `docs/PI_ROADMAP.md` – Featurefolge, Abhängigkeiten und P50/P80-Tokenprognosen
3. `SPEC.md` – verbindliche Produktanforderungen und Abnahmekriterien
4. `docs/architecture-and-provider-contracts.md` – technische Grenzen und stabile API-Verträge
5. `docs/quality-baseline.md` – verbindliche Build-, Test- und CI-Gates
6. `docs/HANDOFF.md` – detaillierter Implementierungs- und Verifikationsverlauf
7. `docs/DECISIONS.md` – bestätigte Produkt-, Architektur- und Prozessentscheidungen
8. `docs/chat-derived-requirements.md` – aus dem privaten Export abstrahierte Anforderungen

## Verbindliche Pflege

- Jede Funktionsänderung aktualisiert im selben Branch den Code, `SPEC.md` und `docs/HANDOFF.md`.
- Architektur- oder API-Änderungen aktualisieren zusätzlich `docs/architecture-and-provider-contracts.md`.
- Der Handoff nennt den letzten verifizierten Commit und den exakten CI-/Teststatus. Ein grüner älterer Build belegt nicht den aktuellen Stand.
- Persönliche Chattexte, Fotos, Tokens, Serveradressen und konkrete Gesundheitswerte dürfen nicht in Git, Spezifikation, Fixtures oder Logs gelangen.
- Neue Gesundheitsfelder werden nur aufgenommen, wenn sie vom Nutzer bestätigt oder im abstrahierten Anforderungskatalog belegt sind.
- Android kennt keinen konkreten KI-Anbieter. Provider-spezifischer Code bleibt hinter der Server-API.
- Vor einem Store-Upload müssen alle Gates aus `docs/security-release-gates.md` und `docs/play-store-checklist.md` nachweislich erfüllt sein.
- Es darf nur ein Arbeitspaket mit dem Status `GO` umgesetzt werden. Vorschläge, Analysen und Refactorings ohne GO bleiben unangetastet.
- Vor Beginn wird das aktive Paket in `docs/PROJECT_STATUS.md` auf `IN PROGRESS` gesetzt. Nach Abschluss werden Commit, Workflow-Run, Abnahme und nächstes Entscheidungs-Gate dort ergänzt.
- Neue dauerhafte Produkt-, Architektur- oder Prozessentscheidungen werden in `docs/DECISIONS.md` protokolliert.
- Tokenprognosen werden vor Beginn als P50/P80 in `docs/PI_ROADMAP.md` geführt; die Wochenplanung stoppt bei 70 Prozent des verfügbaren Kontingents.

## Qualitätsregeln

- Nutzerwerte lokal verschlüsseln; Schlüssel aus Android Keystore.
- KI-Schätzungen sichtbar kennzeichnen und vor dem Speichern korrigierbar machen.
- Externe Aktionen niemals still ausführen.
- Nach Änderungen Server-Syntax-/Vertragstests sowie `:app:testDebugUnitTest`, `:app:lintDebug` und `:app:assembleDebug` über den eingecheckten Gradle-Wrapper ausführen; Ergebnis im Handoff vermerken.
