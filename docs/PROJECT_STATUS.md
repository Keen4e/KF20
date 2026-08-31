# KF20 Projektstatus

Stand: 2026-08-31

Diese Datei ist die zentrale Übergabequelle für Menschen und KI-Agenten. Sie wird bei jedem begonnenen oder abgeschlossenen PI-Arbeitspaket aktualisiert. Frühere Chats sind für die Übernahme nicht erforderlich.

## Repository

- GitHub: `Keen4e/KF20`
- Arbeitsbranch: `codex/kf20-rebuild`
- Draft-PR: `#1` gegen `main`
- Letzter vollständig grüner funktionaler Commit: `5a6e385683ccceb1292835614fd7a218636cc325`
- Verifizierender Workflow: `33367205499`, Ergebnis: `SUCCESS`
- Geprüfte Gates: Server-Syntax/Verträge, Android-JVM-Tests, Android-Lint, Debug-APK und Artefakt-Upload
- Veröffentlichtes Prerelease: `g1-e1-2026-08-31` mit `KF20-g1-e1-2026-08-31.apk`

## Aktives PI-Paket

| Feld | Wert |
|---|---|
| Paket | G1-B3 – Speicher-, Migration- und Compose-Tests |
| Entscheidung | GO |
| Status | IN PROGRESS |
| P50 / P80 | 35k / 55k Roh-Tokens |
| Ist-Verbrauch | nach Abschluss zu kalibrieren; die Plus-Wochenanzeige bleibt verbindlich |
| Produktänderung | keine neue Nutzerfunktion; Sicherheitsnetz für verschlüsselte Speicherung, Export, Upgrade und kritische Compose-Flows |
| Abnahme | alte und aktuelle Datenschemata bleiben lesbar; Export 4 vollständig; Gesprächsmigration idempotent; Kernflüsse automatisiert abgesichert |
| Commit / Workflow | nach Abschluss einzutragen |
| APK-Release | nach grünen Gates zu veröffentlichen |

## Implementierter Produktstand

- Native Kotlin-/Compose-App mit vier Haupttabs: Tag, Statistik, Chat und Einstellungen
- verschlüsseltes lokales Tageslog und mehrere benannte Gespräche
- Nahrung per Text, Foto oder Mikrofon mit korrigierbarer KI-Schätzung
- Morgen-Check für Sport, Gewicht, Körperfett, Umfang, Hunger und Energie
- Tagesziele, Makro-/Kaloriengrafiken, 7-/14-/30-Tage-Statistik und Testwoche
- Standards, Erinnerungen, lokaler JSON-Export und bestätigte lokale Gesamtlöschung
- providerneutrale KF20-Server-API mit OpenAI-Adapter und Vertragstests

Details und Abnahmekriterien stehen in `SPEC.md`; der technische Verlauf steht in `docs/HANDOFF.md`.

## Offene Blocker für einen externen Test

1. keine echte Nutzeranmeldung, Nutzertrennung oder Kontolöschung
2. kein produktives HTTPS-Backend und keine Datenbanksynchronisation
3. Compose-UI noch weitgehend in einer Monolithdatei; Migrationen und kritische UI-Flows unzureichend getestet
4. kein vollständiger KI-End-to-End-Test mit produktionsnahem Backend
5. Kamera und Mikrofon noch nicht auf einem realen Gerät abgenommen
6. kein signiertes Release-Bundle und keine vollständig erledigten Store-/Datenschutz-Gates

## Nächste Entscheidungs-Gates

| Reihenfolge | Paket | Status | Voraussetzung | P50 | P80 |
|---|---|---|---|---:|---:|
| 1 | G1-D0 – Zielwerte und Produktwahrheit | PROPOSED / AWAITING DECISION | aktueller Stand | 12k | 20k |
| 2 | G1-B3 – Speicher-, Migration- und Compose-Tests | PROPOSED / AWAITING DECISION | aktueller Stand | 35k | 55k |
| 3 | G1-D1 – Bearbeiten und Revisionslog | PROPOSED / AWAITING DECISION | G1-B3 | 45k | 70k |
| 4 | G1-D2 – Import und Restore | PROPOSED / AWAITING DECISION | G1-B3 | 45k | 75k |
| 5 | G1-D3 – Standards und Tagesroutinen | PROPOSED / AWAITING DECISION | G1-B3 | 35k | 55k |
| 6 | G1-D4 – Fortschritt und Statistikdetails | PROPOSED / AWAITING DECISION | G1-B3 | 45k | 75k |
| 7 | G2a – Private KI-Testumgebung | PROPOSED / AWAITING DECISION | G1-B3 | 70k | 110k |
| 8 | G1-H1 – Health Connect Basis | PROPOSED / AWAITING DECISION | G1-B3 | 55k | 90k |
| 9 | G3a – KI-End-to-End-Abnahme | PROPOSED / AWAITING DECISION | G2a | 70k | 115k |

Kein PROPOSED- oder DEFERRED-Paket darf ohne eine neue Nutzerentscheidung `GO`, `SPLIT`, `DEFER` oder `DROP` begonnen werden.

## Übergabecheckliste

Vor Arbeitsbeginn:

1. `AGENTS.md` und alle dort genannten Quellen vollständig lesen.
2. Branch-Head und Workflowstatus gegen diese Datei prüfen.
3. Nur das ausdrücklich mit GO markierte Paket bearbeiten.

Vor Übergabe:

1. Qualitätsgates aus `docs/quality-baseline.md` ausführen.
2. Code und alle betroffenen Spezifikations-/Architekturdateien gemeinsam aktualisieren.
3. Commit, Workflow-Run, Testergebnis, Blocker und nächstes Gate hier eintragen.
4. Die geprüfte APK als GitHub-Prerelease veröffentlichen und Release-Link sowie Tag hier eintragen.
5. Keine privaten Chattexte, Gesundheitswerte, Schlüssel oder Serveradressen in Git übernehmen.
