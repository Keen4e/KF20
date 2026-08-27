# KF20 Projektstatus

Stand: 2026-08-27

Diese Datei ist die zentrale Übergabequelle für Menschen und KI-Agenten. Sie wird bei jedem begonnenen oder abgeschlossenen PI-Arbeitspaket aktualisiert. Frühere Chats sind für die Übernahme nicht erforderlich.

## Repository

- GitHub: `Keen4e/KF20`
- Arbeitsbranch: `codex/kf20-rebuild`
- Draft-PR: `#1` gegen `main`
- Letzter vollständig grüner funktionaler Commit: `6effd6996f6976c37638e36365caed4390dff7d7`
- Verifizierender Workflow: `33005372164`, Ergebnis: `SUCCESS`
- Geprüfte Gates: Server-Syntax/Verträge, Android-JVM-Tests, Android-Lint, Debug-APK und Artefakt-Upload

## Aktives PI-Paket

| Feld | Wert |
|---|---|
| Paket | G1-C1a – Startseite und zentrale Plus-Navigation |
| Entscheidung | GO |
| Status | IN PROGRESS |
| P50 / P80 | 20k / 35k Roh-Tokens |
| Ist-Verbrauch | nach Abschluss zu erfassen; Plus-Wochenanzeige bleibt die verbindliche Kalibrierung |
| Produktänderung | Tagesstatistik steht am Anfang der Startseite; bisherige Erfassungskarte wird durch ein einzelnes Plus unten rechts ersetzt |
| Abnahme | Plus öffnet die Auswahl Nahrung, Morgenwerte und Tagesabschluss; bestehende Erfassungswege bleiben erreichbar; vollständige Eingabepopups folgen erst nach separatem GO für G1-C1b |

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
| 1 | G1-B3 – Speicher-, Migration- und Compose-Tests | PROPOSED / AWAITING DECISION | G1-B2b grün | 35k | 55k |
| 2 | G1-C1b – Drei Erfassungs-Popups | PROPOSED / AWAITING DECISION | G1-C1a | 30k | 50k |
| 3 | G1-C2 – Tageswerte / rollierender 7-Tage-Durchschnitt | PROPOSED / AWAITING DECISION | G1-C1a | 25k | 40k |
| 4 | Feature-Lückenanalyse und Neupriorisierung | PROPOSED / AWAITING DECISION | Gespräch mit Nutzer | 15k | 25k |
| 5 | G2 – Konto und produktives Backend | DEFERRED | G1 abgeschlossen | 240k | 360k |
| 6 | G3 – KI Ende-zu-Ende und Provider-Wechseltest | DEFERRED | G2 | 140k | 220k |

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
