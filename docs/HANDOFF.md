# KF20 Handoff

Stand: 2026-08-17

## Repository

- GitHub: `Keen4e/KF20`
- Entwicklungsbranch: `codex/kf20-rebuild`
- Draft-PR: `#1`
- `main` ist nicht die aktuelle Entwicklungsquelle.

## Implementierter Stand

- Native Android-App in Kotlin/Compose
- vier Haupttabs im aktuellen lokalen Stand: Tag, Statistik, Standards, Chat
- Tagesdashboard für Kalorien, Protein, Fett und Carbs
- Mahlzeitenbeschreibung und Fotoanalyse über geschützte Server-API
- aktuell in Arbeit: echte Kameraaufnahme, freie Textbeschreibung, optionales Mikrofon und verpflichtende KI-Auswertung
- Sportfelder aus dem Export: Aktivität, Trainings-kcal, Tracker-Gesamtverbrauch, Notiz
- Messfelder aus dem Export: Gewicht, KF Waage, Hals, Bauch, Hunger, Energie
- Navy-KFA nur mit konfigurierter Körpergröße
- Standards/Routinen, Tagesziele, Startwerte und optionale Ziele
- verschlüsselte lokale Speicherung, Erinnerungen, Aufgaben, Projekte, private Dateiverweise und Fortschrittsfotos
- optionaler Chat-Websearch mit sichtbaren Quellen im aktuellen lokalen Stand
- Server mit Auth-Token, Rate-/Größen-/Zeitlimits und ohne Chat-Inhaltslogs

## Noch nicht produktionsbereit

- kein öffentliches Nutzerkonto-/Login-System
- kein produktiv bereitgestelltes HTTPS-Backend
- keine Datenbanksynchronisation oder Kontolöschung
- keine vollständigen API-, UI- und Migrationstests
- Kamera-/Text-/Mikrofonfluss und alle Dialoge noch nicht auf Emulator/Realgerät visuell getestet
- kein signiertes Release-Bundle und kein Play-Store-Upload

## Verifikation

- Lokaler Servercheck: `node --check server/src/server.js`
- Android wird über `.github/workflows/android.yml` mit Gradle 8.11.1, Java 21 und Android 36 gebaut.
- Der letzte vollständig grüne Build vor der Navigationsänderung war Workflow-Run `32000738644`.
- Workflow-Run `32007627594` prüft den Zwischenstand der neuen Navigation; sein Ergebnis muss vor einem Status-Claim erneut gelesen werden.
- Nach den danach begonnenen Kamera-/Sprachänderungen existiert noch kein verifizierter Android-Build.

## Nächste konkrete Schritte

1. Aktuellen lokalen Kamera-/Textstand statisch bereinigen und auf den Branch laden.
2. Android-CI bis zum grünen Build ausführen und Compilerfehler beheben.
3. Tabs und Dialoge auf Emulator oder Realgerät visuell prüfen.
4. Provideradapter im Server einführen, ohne die KF20-API-Verträge zu verändern.
5. Historischen Datenimport aus dem privaten Export entwerfen; keine privaten Werte ins Repository übernehmen.
6. Konto-/Backend-Entscheidung umsetzen und danach vollständige E2E-Tests aufbauen.

## Blocker/Hinweise

- Lokal ist derzeit kein Gradle-Wrapper/Android-Emulator eingerichtet; GitHub Actions ist die maßgebliche Buildprüfung.
- Für die KI-Funktionen ist ein serverseitiger OpenAI-API-Key mit API-Abrechnung nötig; eine ChatGPT-Subscription allein genügt nicht.
- Vor Store-Veröffentlichung gelten alle Punkte in `security-release-gates.md` und `play-store-checklist.md`.

