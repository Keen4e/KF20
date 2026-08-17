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
- vollständige KF20-Wortmarke im App-Kopf als echte Bildmarke statt der gestauchten ovalen Vektorvariante; vollständiges KF20-Launcher-Icon und echte Material-Icons in der Hauptnavigation
- vereinfachter, gestufter Tagesablauf: Zielstand sehen, Mahlzeit beschreiben/fotografieren/einsprechen, KI-Ergebnis prüfen, speichern
- grafisches Tagesdashboard mit Kalorienring, Makroringen und 7-Tage-Balkenverlauf
- grafischer Statistikbereich für Kalorien, Makros, Gewicht, Körperfett, Sport sowie Hunger/Energie
- konsistente Farbcodierung und zunächst eingeklappte Mahlzeitenerfassung zur Reduktion visueller Komplexität
- Tagesdashboard für Kalorien, Protein, Fett und Carbs
- Mahlzeitenbeschreibung, Kameraaufnahme und optionales Mikrofon über geschützte Server-API; freie Einträge erfordern eine erfolgreiche KI-Auswertung
- Sportfelder aus dem Export: Aktivität, Trainings-kcal, Tracker-Gesamtverbrauch, Notiz
- Messfelder aus dem Export: Gewicht, KF Waage, Hals, Bauch, Hunger, Energie
- Navy-KFA nur mit konfigurierter Körpergröße
- Standards/Routinen, Tagesziele, Startwerte und optionale Ziele
- verschlüsselte lokale Speicherung, Erinnerungen, Aufgaben, Projekte, private Dateiverweise und Fortschrittsfotos
- optionaler Chat-Websearch mit sichtbaren Quellen im aktuellen lokalen Stand
- provider-neutrales Server-Interface mit separatem OpenAI-Adapter; Auswahl über `AI_PROVIDER`/`AI_MODEL`
- Server mit Auth-Token, Rate-/Größen-/Zeitlimits und ohne Chat-Inhaltslogs
- automatische Provider- und HTTP-Vertragstests ohne echte Provideranfragen
- Debug-Testmodus unter Standards: eine aus den belegten Chat-Aggregaten abgeleitete Woche kann mit einem Knopfdruck geladen werden; die Werte bleiben unverändert und werden nur auf die letzten sieben Tage gelegt

## Noch nicht produktionsbereit

- kein öffentliches Nutzerkonto-/Login-System
- kein produktiv bereitgestelltes HTTPS-Backend
- keine Datenbanksynchronisation oder Kontolöschung
- keine vollständigen API-, UI- und Migrationstests
- KI-End-to-End-Fluss benötigt weiterhin ein produktionsnahes Backend und einen realen API-Key
- Kamera und Mikrofon müssen zusätzlich auf einem realen Android-Gerät geprüft werden
- kein signiertes Release-Bundle und kein Play-Store-Upload

## Verifikation

- Lokaler Servercheck: `node --check` für Einstieg und Adapter sowie `node --test`; 5 von 5 Tests erfolgreich.
- Lokaler Android-Check nach dem Diagramm-Redesign: `:app:compileDebugKotlin` erfolgreich; nur bestehende Deprecation-Warnungen.
- Android wird über `.github/workflows/android.yml` mit Gradle 8.11.1, Java 21 und Android 36 gebaut.
- Workflow-Run `32008685257` ist für Commit `92c03bc5478c2cd084fc02c6ada499fd25cacd43` vollständig grün: Server-Syntaxcheck, Android-Debug-Build und APK-Upload waren erfolgreich.
- Das APK dieses Laufs wurde auf dem lokalen Android-16-Emulator installiert und gestartet. Die vier Haupttabs sowie Sport- und Messwertdialog wurden visuell geprüft.
- Workflow-Run `32016043340` ist für Commit `0ce2a65cd46f2700d61dac2698d74df2461737be` vollständig grün und enthält die vereinfachte Tagesansicht, Material-Tabicons sowie das vollständige KF20-Vektorlogo.
- Das APK dieses Laufs wurde auf Android 16 installiert und kalt gestartet. Tagesansicht und Sportdialog wurden erneut visuell geprüft; im bereinigten Logcat trat kein Absturz auf.
- Workflow-Run `32022349685` ist für Commit `54df0071530346c0ff3a6fc1679c126e396c000f` vollständig grün: Servertests und Android-Build einschließlich Diagramm-Dashboard waren erfolgreich.
- Tages- und Statistikdashboard wurden auf Android 16 visuell geprüft. Die Wortmarke im Kopf wurde anschließend auf die vorhandene KF20-Bilddatei umgestellt und lokal erneut gebaut, installiert und visuell geprüft.
- Die Chat-Testwoche wurde auf Android 16 geladen und visuell geprüft: aktueller Tag 2.374 kcal, 165 g Protein, 68 g Fett, 207 g Carbs und 540 Sport-kcal bei Zielen 2.484/180/70/290; Kalorien-, Makro-, Gewichts- und weitere Verlaufsdiagramme sind befüllt.
- Der laufende Android-16-Emulator kann über ein sichtbares, interaktives KF20-Fenster im lokalen App-Browser bedient werden.
- Die CI startet nur bei Änderungen unter `android/`, `server/` oder an der Workflowdatei. Handoff-/Spezifikationsänderungen lösen keinen redundanten Android-Build aus.

## Nächste konkrete Schritte

1. Kamera und Mikrofon auf einem realen Android-Gerät testen.
2. Einen zweiten Provideradapter als Wechseltest implementieren, sobald der Zielanbieter feststeht.
3. Historischen Datenimport aus dem privaten Export entwerfen; weiterhin keine privaten Nachrichten oder den Roh-Export ins Repository übernehmen.
4. Konto-/Backend-Entscheidung umsetzen und danach vollständige E2E-Tests aufbauen.

## Blocker/Hinweise

- Ein portables Android-16-Emulator-Setup ist lokal eingerichtet. Durch sporadische Windows-Sandbox-Dateisperren bleibt GitHub Actions die maßgebliche vollständige Buildprüfung.
- Die Debug-Testwoche enthält ausschließlich abstrahierte Tagesaggregate. Der private Chat-Export bleibt außerhalb von Git und App-Paket.
- Für die KI-Funktionen ist ein serverseitiger OpenAI-API-Key mit API-Abrechnung nötig; eine ChatGPT-Subscription allein genügt nicht.
- Vor Store-Veröffentlichung gelten alle Punkte in `security-release-gates.md` und `play-store-checklist.md`.

