# KF20 Projektstatus

Stand: 2026-08-26

Diese Datei ist die zentrale Übergabequelle für Menschen und KI-Agenten. Sie wird bei jedem begonnenen oder abgeschlossenen PI-Arbeitspaket aktualisiert. Frühere Chats sind für die Übernahme nicht erforderlich.

## Repository

- GitHub: `Keen4e/KF20`
- Arbeitsbranch: `codex/kf20-rebuild`
- Draft-PR: `#1` gegen `main`
- Letzter vollständig grüner funktionaler Commit: `2f9b8f48d7827c503f9c53323102b50d264e73e0`
- Verifizierender Workflow: `33001726047`, Ergebnis: `SUCCESS`
- Geprüfte Gates: Server-Syntax/Verträge, Android-JVM-Tests, Android-Lint, Debug-APK und Artefakt-Upload

## Aktives PI-Paket

| Feld | Wert |
|---|---|
| Paket | G1-B2a – Fachmodelle und reine Berechnungslogik |
| Entscheidung | GO |
| Status | DONE – vollständig grün abgenommen |
| P50 / P80 | 20k / 30k Roh-Tokens |
| Ist-Verbrauch | Roh-Schätzung etwa 20k; Plus-Wochenanzeige ist die verbindliche Kalibrierung |
| Produktänderung | keine |
| Abnahme | Fachmodelle und Refeed-/Ziel-/Navy-Berechnungen liegen außerhalb von `MainActivity.kt`; Verhalten bleibt durch JVM-Tests gesichert |

## Implementierter Produktstand

- Native Kotlin-/Compose-App mit vier Haupttabs: Tag, Statistik, Standards und Chat
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
3. Android-Code noch weitgehend in einer Monolithdatei; Migrationen und kritische UI-Flows unzureichend getestet
4. kein vollständiger KI-End-to-End-Test mit produktionsnahem Backend
5. Kamera und Mikrofon noch nicht auf einem realen Gerät abgenommen
6. kein signiertes Release-Bundle und keine vollständig erledigten Store-/Datenschutz-Gates

## Nächste Entscheidungs-Gates

| Reihenfolge | Paket | Status | Voraussetzung | P50 | P80 |
|---|---|---|---|---:|---:|
| 1 | G1-B2b – Daten-, Netzwerk- und UI-Grenzen | PROPOSED / AWAITING DECISION | G1-B2a grün | 30k | 50k |
| 2 | G1-B3 – Speicher-, Migration- und Compose-Tests | PROPOSED | G1-B2b | 35k | 55k |
| 3 | G2 – Konto und produktives Backend | DEFERRED | G1 abgeschlossen | 240k | 360k |
| 4 | G3 – KI Ende-zu-Ende und Provider-Wechseltest | DEFERRED | G2 | 140k | 220k |

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
4. Keine privaten Chattexte, Gesundheitswerte, Schlüssel oder Serveradressen in Git übernehmen.

