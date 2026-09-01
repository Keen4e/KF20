# KF20 PI-Roadmap

Stand: 2026-09-01

## Kapazitätsregel

- Pro Woche werden höchstens 70 Prozent des verfügbaren Codex-Kontingents geplant.
- 55 Prozent sind für Featurearbeit, 10 Prozent für QA/Nacharbeit und 5 Prozent für Planung vorgesehen.
- 30 Prozent bleiben Reserve.
- Da ChatGPT Plus kein fest dokumentiertes Roh-Token-Wochenbudget bereitstellt, wird nach jedem Paket der tatsächlich angezeigte Wochenverbrauch erfasst und die Prognose kalibriert.
- Vor jedem Paket entscheidet der Nutzer ausdrücklich: `GO`, `SPLIT`, `DEFER` oder `DROP`.

## PI-1: vom Prototyp zum internen Store-Test

| Gate | Feature / Paket | Ergebnis | Abhängigkeit | P50 | P80 | Entscheidung |
|---|---|---|---|---:|---:|---|
| G1-A | Qualitätsbaseline | belastbare Lückenliste | – | 20k | 30k | DONE |
| G1-B1 | Reproduzierbarer Build und CI-Gates | Wrapper, Tests, Lint, CI und Agentenübergabe | G1-A | 25k | 40k | DONE |
| G1-B2a | Fachmodelle und reine Berechnungslogik | Domänenmodelle sowie Refeed-, Ziel- und Navy-Logik aus der UI-Datei lösen | G1-B1 | 20k | 30k | DONE |
| G1-B2b | Daten-, Netzwerk- und UI-Grenzen | Speicherung und API von Compose entkoppeln | G1-B2a | 30k | 50k | DONE |
| G1-B3 | Migrations- und UI-Tests | Speicher-, Export-, Upgrade- und Kernflow-Tests | G1-B2b | 35k | 55k | DONE · Release `g1-b3-2026-08-31` |
| G1-C1a | Startseite und zentrale Plus-Navigation | Tagesstatistik zuerst; ein Plus für Nahrung, Morgenwerte und Tagesabschluss | G1-B2b | 20k | 35k | DONE |
| G1-C1b | Drei Erfassungs-Popups | getrennte, kompakte Eingabepopups mit Speicherung und Tests | G1-C1a | 30k | 50k | DONE |
| G1-C2 | Statistikdarstellung | Umschalter Tageswerte / rollierender 7-Tage-Durchschnitt für Zeitreihen | G1-C1a | 25k | 40k | DONE |
| G1-D0 | Zielwerte und Produktwahrheit | keine erfundenen Ziele; sauberer Erststart und widerspruchsfreie Dokumentation | G1-C2 | 12k | 20k | PROPOSED |
| G1-D1 | Bearbeiten und Revisionslog | Tageswerte korrigieren und Änderungen sichtbar nachhalten | G1-B3 | 45k | 70k | PROPOSED |
| G1-D2 | Import und Restore | versionierter JSON-Restore und nutzergeführter historischer Import | G1-B3 | 45k | 75k | PROPOSED |
| G1-D3 | Standards und Tagesroutinen | Standards bearbeiten/ordnen und kontextbezogene Erinnerungen | G1-B3 | 35k | 55k | PROPOSED |
| G1-D4 | Fortschritt und Statistikdetails | Fotovergleich, Zieltrend und untersuchbare Diagramme | G1-B3 | 45k | 75k | PROPOSED |
| G1-E1 | Präzisere Nahrungserfassung | Mengen/Einheiten, bestätigte Umrechnung, Zubereitungszustand und transparente Annahmen | G1-C1b | 50k | 80k | DONE · Release `g1-e1-2026-08-31` |
| G1-S1 | Supplements | Inhaltsstoffe/Einheiten, Einnahmezeiten, Tagesbilanz, Doppelungswarnungen und Arztauszug | G1-B3 | 55k | 90k | PROPOSED |
| G1-H1 | Health Connect Basis | explizite Verbindung und Leseberechtigung; aktive Kalorien und Trainingseinheiten vom Android-Gerät übernehmen, Quelle/Dubletten transparent halten | G1-B3 | 55k | 90k | PROPOSED |
| G2-S0 | Backend-, Sync- und KI-Strategie | Local-first-Zielbild, Identität, Datenhaltung, Managed/BYOK/Plus-Modi, Secret-Lebenszyklus und umsetzbare Folgepakete | G1-B3 | 25k | 40k | DONE · Release `g2-s0-2026-08-31` |
| G2-K1 | Zustandslose KI-Brücke | Text-, Foto- und Chatanfragen über providerneutrale API; zentraler Schlüssel, Limits, keine Konten/DB/Sync, Docker und Cloudflare Tunnel | G2-S0 | 40k | 65k | GO · IN PROGRESS |
| G2-D0 | Führende Datenquelle und Zentraldaten-Verträge | Entscheidung App-vs.-Backend, Datenklassifikation, ER-Modell sowie REST-/MCP-Verträge | G2-K1 | 25k | 40k | PROPOSED · REQUIRED BEFORE CENTRALIZATION |
| G2-D1 | Zentrale Fachdatenschicht | PostgreSQL, Migrationen, Services, Audit, Idempotenz, Versionierung und deterministische Bilanzen | G2-D0 | 90k | 145k | PROPOSED |
| G2-D2 | REST und Client-Autorisierung | REST v1, getrennte Tokens/Clients, Scopes, Rotation, Limits und Autorisierungstests | G2-D1 | 80k | 130k | PROPOSED |
| G2-M1 | MCP Streamable HTTP | 19 Tools und sieben Resources über dieselben Services, HTTPS und Scope-Prüfung | G2-D2 | 80k | 130k | PROPOSED |
| G2-T1 | Telegram und JSON-Migration | gemeinsamer Datenbestand, Outbox/Retry, Nachrichten-/Bildidempotenz und Import | G2-M1 | 90k | 150k | PROPOSED |
| G2-H2 | Health Bridge und Home Assistant | Uploadqueue/Dedupe, Schritte/Aktiv-/Gesamtkalorien sowie Gewicht/KFA ohne Schätzungen | G2-D2 | 90k | 150k | PROPOSED |
| G2-P1 | Dauerhafter Fotospeicher | privater Objektspeicher, Hash/Metadaten, Zugriff, Soft Delete und Audit | G2-D2 | 55k | 90k | PROPOSED |
| G2-E1 | Zentrales Backend E2E | gemeinsame App-/Telegram-/MCP-Sicht, Parallelität, Retry, Restore und Security-Abnahme | G2-T1, G2-H2, G2-P1 | 70k | 115k | PROPOSED |
| G2-A1 | Private Backend-Basis | portables Docker-Deployment auf Homeserver, Cloudflare Tunnel, HTTPS, Healthcheck, Identität und Backup/Restore | G2-S0 | 55k | 90k | DEFERRED – Konto/Sync geparkt |
| G2-A2 | KI-Gateway v1 | Provider-Registry, zentraler OpenAI-Projektschlüssel, Limits und Ausführungsmetadaten | G2-A1 | 55k | 90k | SUPERSEDED IN PART BY G2-K1 |
| G2-A3 | BYOK und weitere Provider | Credential-Vault, OpenAI-/Anthropic-/OpenRouter-Adapter, Test/Replace/Delete und explizites Routing | G2-A2 | 75k | 120k | PROPOSED |
| G2-B1 | Local-first Datensync | Konto, Delta-Sync, Konflikte, Export und Kontolöschung; zunächst ohne Bilder | G2-A1 | 110k | 175k | DEFERRED – Daten bleiben zunächst in der App |
| G2-B2 | Fortschrittsbild-Sync | optionaler verschlüsselter Bild-Sync mit Aufbewahrung und Löschtest | G2-B1 | 55k | 90k | PROPOSED |
| G3-A1 | KI-End-to-End-Abnahme | Text, Foto, Mikrofon, Chat, Kosten-/Fehlerfälle und Providerwechsel auf Realgerät | G2-A3 | 70k | 115k | PROPOSED |
| G1-T1 | Trainingsbibliothek | Übungskatalog, Anleitungen, Bilder und Satzprotokoll | G1-B3 | 100k | 170k | DEFERRED |
| G2a | Private KI-Testumgebung | frühere Grobplanung; ersetzt durch G2-A1/A2 | G1-B3 | 70k | 110k | SUPERSEDED |
| G3a | KI-End-to-End-Abnahme | frühere Grobplanung; ersetzt durch G3-A1 | G2a | 70k | 115k | SUPERSEDED |
| G2 | Konto und Backend | frühere Grobplanung; ersetzt durch G2-A1/A2/A3/B1/B2 | G1 | 240k | 360k | SUPERSEDED |
| G3 | KI Ende-zu-Ende | frühere Grobplanung; ersetzt durch G3-A1 | G2 | 140k | 220k | SUPERSEDED |
| G4 | Revisionslog und Import | sichtbare Korrekturhistorie, Backup-Restore und historischer Abgleich | G1 | 120k | 190k | DEFERRED |
| G5 | Agenten-Anhänge | Bilder, PDFs und Dokumente im Chat mit sicheren Ergebnissen | G1, G3 | 160k | 260k | DEFERRED |
| G6 | Security und Datenschutz | Kontolöschung, Aufbewahrung, Limits und Monitoring | G2 | 170k | 270k | DEFERRED |
| G7 | Release und Store | Realgerät, signiertes AAB, Listing, Data Safety und interner Play-Test | G3–G6 | 150k | 250k | DEFERRED |

Gesamtprognose PI-1 vor weiterer Kalibrierung: ungefähr 1,16 bis 1,82 Millionen Roh-Tokens.

Die neuen D-Pakete schneiden Teile des bisherigen Sammelpakets G4 kleiner. G2-A1 bis G2-B2 und G3-A1 ersetzen nach G2-S0 die gröberen Zeilen G2a/G2/G3a/G3 als bevorzugte Zerlegung; diese Alternativen werden nicht doppelt summiert.

## Gate-Ablauf

1. Paket mit Ziel, Nicht-Zielen, Abnahme und P50/P80 vorschlagen.
2. Nutzerentscheidung dokumentieren.
3. Bei GO den Status in `docs/PROJECT_STATUS.md` auf `IN PROGRESS` setzen.
4. Nur dieses Paket implementieren und die Qualitätsgates ausführen.
5. Tatsächlichen Verbrauch, Commit und Workflowstatus dokumentieren.
6. Eine geprüfte APK als eindeutig versioniertes GitHub-Prerelease veröffentlichen und den Link dokumentieren.
7. Stoppen und das nächste Gate zur Priorisierung vorlegen.
