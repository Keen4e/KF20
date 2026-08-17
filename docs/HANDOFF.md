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
- vollständige KF20-Wortmarke im App-Kopf und als Launcher-Icon; echte Material-Icons in der Hauptnavigation
- vereinfachter, gestufter Tagesablauf: Zielstand sehen, Mahlzeit beschreiben/fotografieren/einsprechen, KI-Ergebnis prüfen, speichern
- Tagesdashboard für Kalorien, Protein, Fett und Carbs
- Mahlzeitenbeschreibung, Kameraaufnahme und optionales Mikrofon über geschützte Server-API; freie Einträge erfordern eine erfolgreiche KI-Auswertung
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
- KI-End-to-End-Fluss benötigt weiterhin ein produktionsnahes Backend und einen realen API-Key
- Kamera und Mikrofon müssen zusätzlich auf einem realen Android-Gerät geprüft werden
- kein signiertes Release-Bundle und kein Play-Store-Upload

## Verifikation

- Lokaler Servercheck: `node --check server/src/server.js`
- Android wird über `.github/workflows/android.yml` mit Gradle 8.11.1, Java 21 und Android 36 gebaut.
- Workflow-Run `32008685257` ist für Commit `92c03bc5478c2cd084fc02c6ada499fd25cacd43` vollständig grün: Server-Syntaxcheck, Android-Debug-Build und APK-Upload waren erfolgreich.
- Das APK dieses Laufs wurde auf dem lokalen Android-16-Emulator installiert und gestartet. Die vier Haupttabs sowie Sport- und Messwertdialog wurden visuell geprüft.
- Die aktuelle UI-Vereinfachung kompiliert lokal bis einschließlich Kotlin; der maßgebliche vollständige CI-Build folgt nach dem Branch-Update.
- Die CI startet nur bei Änderungen unter `android/`, `server/` oder an der Workflowdatei. Handoff-/Spezifikationsänderungen lösen keinen redundanten Android-Build aus.

## Nächste konkrete Schritte

1. Vereinfachten UI-Stand auf den Branch laden und Android-CI bis zum grünen Build ausführen.
2. Neues APK auf dem Android-16-Emulator installieren und Tagesansicht, Navigation sowie Sport-/Messwertdialog erneut visuell prüfen.
3. Kamera und Mikrofon auf einem realen Android-Gerät testen.
4. Provideradapter im Server einführen, ohne die KF20-API-Verträge zu verändern.
5. Historischen Datenimport aus dem privaten Export entwerfen; keine privaten Werte ins Repository übernehmen.
6. Konto-/Backend-Entscheidung umsetzen und danach vollständige E2E-Tests aufbauen.

## Blocker/Hinweise

- Ein portables Android-16-Emulator-Setup ist lokal eingerichtet. Durch sporadische Windows-Sandbox-Dateisperren bleibt GitHub Actions die maßgebliche vollständige Buildprüfung.
- Für die KI-Funktionen ist ein serverseitiger OpenAI-API-Key mit API-Abrechnung nötig; eine ChatGPT-Subscription allein genügt nicht.
- Vor Store-Veröffentlichung gelten alle Punkte in `security-release-gates.md` und `play-store-checklist.md`.

