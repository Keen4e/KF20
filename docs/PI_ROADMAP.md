# KF20 PI-Roadmap

Stand: 2026-08-24

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
| G1-B2 | Android-Schichten | Fachlogik, Daten, Netzwerk und UI schrittweise entkoppelt | G1-B1 | 45k | 70k | PROPOSED |
| G1-B3 | Migrations- und UI-Tests | Speicher-, Export-, Upgrade- und Kernflow-Tests | G1-B2 | 35k | 55k | PROPOSED |
| G2 | Konto und Backend | Anmeldung, Nutzertrennung, Datenbank und produktives HTTPS | G1 | 240k | 360k | DEFERRED |
| G3 | KI Ende-zu-Ende | Text, Foto, Mikrofon und Chat produktionsnah; Provider-Wechseltest | G2 | 140k | 220k | DEFERRED |
| G4 | Revisionslog und Import | sichtbare Korrekturhistorie, Backup-Restore und historischer Abgleich | G1 | 120k | 190k | DEFERRED |
| G5 | Agenten-Anhänge | Bilder, PDFs und Dokumente im Chat mit sicheren Ergebnissen | G1, G3 | 160k | 260k | DEFERRED |
| G6 | Security und Datenschutz | Kontolöschung, Aufbewahrung, Limits und Monitoring | G2 | 170k | 270k | DEFERRED |
| G7 | Release und Store | Realgerät, signiertes AAB, Listing, Data Safety und interner Play-Test | G3–G6 | 150k | 250k | DEFERRED |

Gesamtprognose PI-1 vor weiterer Kalibrierung: ungefähr 1,10 bis 1,73 Millionen Roh-Tokens.

## Gate-Ablauf

1. Paket mit Ziel, Nicht-Zielen, Abnahme und P50/P80 vorschlagen.
2. Nutzerentscheidung dokumentieren.
3. Bei GO den Status in `docs/PROJECT_STATUS.md` auf `IN PROGRESS` setzen.
4. Nur dieses Paket implementieren und die Qualitätsgates ausführen.
5. Tatsächlichen Verbrauch, Commit und Workflowstatus dokumentieren.
6. Stoppen und das nächste Gate zur Priorisierung vorlegen.