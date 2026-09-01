# KF20 Handoff

## G2-K1 – Private zustandslose KI-Brücke (2026-09-01, IN PROGRESS)

- Android, Server und Dokumentation verwenden weiterhin die stabilen KF20-Endpunkte. Erfolgreiche Antworten ergänzen `execution` mit Provider, `managed` und `storage: none`; Android zeigt diese Grenze bei der Nährwertschätzung an.
- `GET /healthz` weist `stateless-ai-bridge`/`storage: none` aus. Unter Einstellungen kann die App die konfigurierte Brücke testen, ohne einen Provider-Schlüssel zu kennen.
- Request-IDs, No-store-Header, tatsächliche 1-MB-Bildgrößenprüfung, generische Fehlercodes und inhaltsfreie Fehlerlogs sind implementiert. Beide OpenAI-Aufrufpfade setzen `store: false`.
- `server/Dockerfile` und `deploy/compose*.yaml` liefern den Homeserverbetrieb; der normale Stack veröffentlicht keinen Host-Port und erreicht die API nur über `cloudflared`. Reale Schlüssel, Domain und Tunnel sind nicht im Repository und wurden nicht erfunden.
- `docs/PRIVATE_AI_BRIDGE.md` ist die Betriebsanleitung. `docs/MCP_BACKEND_GAP_ANALYSIS.md` gleicht die später eingereichte zentrale Backend-/MCP-/Telegram-Anleitung vollständig ab.
- Die Zentralbackend-Anleitung widerspricht D-018 („Daten zunächst in der App“). Darum sind Datenbank, REST-Fachdaten, MCP, Telegram, Health Bridge, Home Assistant und dauerhafte Serverfotos nicht Bestandteil von G2-K1. G2-D0 ist das notwendige Richtungs-Gate.
- Lokal ausgeführt: Server-Syntax und `npm test`, 5/5 erfolgreich; Docker-Compose-Konfiguration wurde ohne Interpolation erfolgreich gerendert. Der Android-Gesamtlauf ist lokal mangels Java-Runtime nicht ausführbar und wird durch GitHub Actions geprüft.
- Commit, Workflow und APK-Prerelease werden nach dem vollständigen Gate ergänzt.

## G2-S0 – Backend-, Sync- und KI-Provider-Strategie (2026-08-31)

- Nutzer-GO: `G2-S0 GO`.
- `docs/BACKEND_STRATEGY.md` ist die verbindliche neue Zielarchitektur für Local-first-Sync, Identität, PostgreSQL/Supabase-Spike, Secret Vault und KI-Routing.
- Die drei Zugangsarten sind getrennt: KF20-verwaltet, benutzereigener Schlüssel (BYOK) und manueller ChatGPT-Plus-Begleitmodus.
- OpenAI direkt, Anthropic direkt und OpenRouter sind getrennte Providerwege. Ein stiller Wechsel von Provider, Vermittler oder Kostenträger ist ausgeschlossen.
- Vorhandener IONOS-Webspace ist für Domain/Landingpage vorgesehen. Nur falls der Vertrag ein echter VPS/Cloud Server mit Adminzugriff ist, wird IONOS in G2-A1 als Container-Alternative zu Cloud Run geprüft.
- Für die private Alpha ist der vorhandene Homeserver mit Docker und ausschließlich ausgehendem Cloudflare Tunnel nun die bevorzugte Laufzeit. Cloudflare Access ist Zusatzschutz und ersetzt nicht die KF20-Authentifizierung; verschlüsseltes Offsite-Backup und Restore-Test gehören zur Abnahme.
- Das Paket enthält bewusst keinen Backend-Produktcode, kein Konto, keinen Cloud-Dienst und keinen echten Schlüssel. Die Umsetzung ist in G2-A1/A2/A3 sowie G2-B1/B2 zerlegt und benötigt jeweils ein neues GO.
- GitHub-Commit `301fc252699eb307f3103d33d84a7415e912d8e1`; Release-Workflow `33410290070` bestand Serververträge, Android-JVM-Tests, Lint, Debug-Build und alle Instrumentierungstests auf API 35.
- Prerelease und APK: https://github.com/Keen4e/KF20/releases/tag/g2-s0-2026-08-31 · `KF20-g2-s0-2026-08-31.apk`.

Stand: 2026-08-31

## Repository

- GitHub: `Keen4e/KF20`
- Entwicklungsbranch: `codex/kf20-rebuild`
- Draft-PR: `#1`
- `main` ist nicht die aktuelle Entwicklungsquelle.

## Implementierter Stand

- G1-B3 implementiert ein automatisiertes Sicherheitsnetz: ein reiner JSON-Codec hält alte und aktuelle Tages-, Mess- und Gesprächsdaten kompatibel, die Altchat-Migration ist idempotent, Export-Schema 4 wird vollständig geprüft, und Android-Instrumentierung deckt echten Keystore/AES-GCM-Speicher sowie die zentralen Compose-Flows ab.
- Branch- und APK-Release-Workflow starten nun zusätzlich einen API-35-Emulator und führen `connectedDebugAndroidTest` aus. Die Paket-APK wird erst nach Serververträgen, JVM-Tests, Lint, Build und Instrumentierung veröffentlicht.
- G1-E1 abgeschlossen: Die KI-Schätzung bleibt die Basisportion; Portion, Gramm, Stück, EL und TL skalieren Kalorien, Protein, Fett und Carbs gemeinsam. Grammangaben benötigen ein bestätigtes Basisgewicht, Stück/EL/TL zusätzlich bestätigte Gramm je Einheit. Zubereitungszustand und sichtbare Annahmen werden im verschlüsselten Tageslog und Export-Schema 4 erhalten.
- Release-Prozess ergänzt: `release/current.json` beschreibt den eindeutigen Paket-Tag und öffentliche Notizen. `.github/workflows/release-apk.yml` wiederholt Servertests, Android-JVM-Tests, Lint und APK-Build und veröffentlicht nur danach ein GitHub-Prerelease. Der erste Release ist `g1-e1-2026-08-31`.
- Health Connect ist als Paket G1-H1 vorgeschlagen: opt-in Lesen aktiver Kalorien und Trainingseinheiten, mit expliziter Berechtigungsverwaltung, Quellenanzeige und Dublettenkontrolle. Es wurde noch kein Health-Connect-Produktcode begonnen.
- Die Feature-Lückenanalyse vom 28.08.2026 liegt in `docs/FEATURE_GAPS.md`. Sie trennt vorhandenen Kern, partielle Prototypen und fehlende Produkt-/Releasefunktionen und zerlegt die nächsten Optionen in einzeln entscheidbare Pakete mit P50/P80. Es wurde dabei kein Produktcode geändert.
- Native Android-App in Kotlin/Compose
- vier Haupttabs im aktuellen lokalen Stand: Tag, Statistik, Chat, Einstellungen
- G1-C1a abgeschlossen: Kalorien- und Makrostatistik stehen am Anfang der Tagesseite; ein einzelnes Plus unten rechts öffnet die Auswahl Nahrung, Morgenwerte und Tagesabschluss. Die bisherige große Tageserfassungskarte ist entfallen. Die Navigation lautet Tag, Statistik, Chat, Einstellungen.
- G1-C1b abgeschlossen: Nahrung, Morgenwerte und Tagesabschluss öffnen jeweils als eigene Bottom-Sheet-Erfassung. Der Tagesabschluss zeigt die aktuelle Bilanz und speichert Tracker-Gesamtverbrauch plus optionale Notiz, ohne den Wert nochmals als Trainingskalorien zu verbuchen.
- G1-C2 abgeschlossen: Jede Statistikzeitreihe kann zwischen Tageswerten und rollierendem 7-Tage-Durchschnitt umgeschaltet werden. Das Fenster umfasst den aktuellen und die sechs vorherigen Kalendertage; fehlende Ernährungs- oder Messwerte werden ausgelassen statt als Null erfunden.
- drei sofort umschaltbare, lokal gespeicherte Styleguides: Performance Dark (Standard), Health Light und Data Athlete
- mehrere benannte, AES-GCM-verschlüsselte Gespräche mit eigener Historie, lokaler Volltextsuche, bestätigtem Löschen und automatischer Migration des bisherigen Einzelverlaufs in den Hauptchat
- vollständige KF20-Wortmarke im App-Kopf als echte Bildmarke statt der gestauchten ovalen Vektorvariante; vollständiges KF20-Launcher-Icon und echte Material-Icons in der Hauptnavigation
- vereinfachter, gestufter Tagesablauf: Zielstand sehen, Mahlzeit beschreiben/fotografieren/einsprechen, KI-Ergebnis prüfen, speichern
- grafisches Tagesdashboard mit Kalorien- und Makroringen; Nahrung bleibt KI-gestützt, Sport und Messwerte laufen gemeinsam über den Morgen-Check
- Morgen-Check als große, von unten kommende Erfassungsfläche nach der UI-Referenz: Sport-, Energie- und Hungerregler, Gewicht/KFA, optional Hals/Bauch, Live-Zielvorschau sowie „Tag starten“/„Später“
- Refeed-Modell aus dem privaten Chat als dokumentierte Produktregel: Energie 1–4 = 30 %, 5–7 = 50 %, 8–10 = 70 % der Sport-kcal; das Add-on erhöht das Tagesziel und die Carbs
- gemeinsame Tagesliste für Nahrung, Sport und Messwerte einschließlich gezielter Löschaktionen
- Tagesplanung für Nahrung: KI-Ergebnisse und Standards können als „jetzt gegessen“ oder „später geplant“ gespeichert werden; geplante Werte haben eine eigene Prognose und werden erst nach Bestätigung in Ist-Bilanz und Statistik übernommen
- grafischer Statistikbereich für Kalorien, Makros, Gewicht, Körperfett, Sport sowie Hunger/Energie
- der 7-Tage-Überblick liegt ausschließlich unter Statistik und ist dort die Voreinstellung
- konsistente Farbcodierung und zunächst eingeklappte Mahlzeitenerfassung zur Reduktion visueller Komplexität
- Tagesdashboard für Kalorien, Protein, Fett und Carbs
- Mahlzeitenbeschreibung, Kameraaufnahme und optionales Mikrofon über geschützte Server-API; freie Einträge erfordern eine erfolgreiche KI-Auswertung
- Sportfelder aus dem Export bleiben im Datenmodell; der tägliche Primärfluss erfasst den Sportverbrauch laut Tracker direkt im Morgen-Check
- Messfelder aus dem Export: Gewicht, KF Waage, Hals, Bauch, Hunger, Energie; Hals und Bauch sind im Morgen-Check optional einblendbar
- Navy-KFA nur mit konfigurierter Körpergröße
- Standards/Routinen, Tagesziele, Startwerte und optionale Ziele
- verschlüsselte lokale Speicherung, Erinnerungen, Aufgaben, Projekte, private Dateiverweise und Fortschrittsfotos
- vollständiger lokaler JSON-Export ohne Server-Token sowie bestätigungspflichtige Löschung aller lokalen Daten, URI-Freigaben und des KF20-Keystore-Schlüssels
- JSON-Exportschema 3 enthält alle benannten Gespräche, den aktiven Gesprächsbezug und die Designauswahl; die Serverzugangsdaten bleiben ausgeschlossen
- optionaler Chat-Websearch mit sichtbaren Quellen im aktuellen lokalen Stand
- provider-neutrales Server-Interface mit separatem OpenAI-Adapter; Auswahl über `AI_PROVIDER`/`AI_MODEL`
- Server mit Auth-Token, Rate-/Größen-/Zeitlimits und ohne Chat-Inhaltslogs
- automatische Provider- und HTTP-Vertragstests ohne echte Provideranfragen
- Debug-Testmodus unter Standards: eine aus den belegten Chat-Aggregaten abgeleitete Woche kann mit einem Knopfdruck geladen werden; die Werte bleiben unverändert und werden nur auf die letzten sieben Tage gelegt
- G1-B1-Qualitätsfundament: eingecheckter Gradle-Wrapper 8.11.1 mit SHA-256-Prüfung, erste Android-JVM-Fachlogiktests sowie getrennte CI-Gates für Server, Android-Unit-Tests, Lint und Debug-Build
- G1-B2a-Struktur: gemeinsame Domänenmodelle liegen in `Kf20Models.kt`; Refeed-Faktor, adaptive Tagesziele und Navy-KFA liegen als reine, Android-unabhängige Funktionen in `DailyTargetLogic.kt`
- G1-B2b-Struktur: lokale Speicherung, Verschlüsselung und Export liegen in `Kf20Storage.kt`; Erinnerungen/Systemdienste in `Kf20Services.kt`; providerneutrale HTTP-Aufrufe in `Kf20ApiClient.kt`. `MainActivity.kt` enthält weiterhin Compose, aber keine direkten Infrastrukturimplementierungen mehr.

## Noch nicht produktionsbereit

- kein öffentliches Nutzerkonto-/Login-System
- kein produktiv bereitgestelltes HTTPS-Backend
- keine Datenbanksynchronisation oder Kontolöschung
- keine vollständigen KI-End-to-End-, Kamera-/Mikrofon- und Realgerät-Upgradetests; die lokalen Speicher-, Migrations- und zentralen UI-Verträge sind automatisiert abgedeckt
- KI-End-to-End-Fluss benötigt weiterhin ein produktionsnahes Backend und einen realen API-Key
- Kamera und Mikrofon müssen zusätzlich auf einem realen Android-Gerät geprüft werden
- kein signiertes Release-Bundle und kein Play-Store-Upload

## Verifikation

- G1-B3 GitHub: Der funktionale Commit `7ebaad5ead0c2627b02331fe36e42dfd51feb135` bestand im Branch-Workflow `33387922930` Serververträge, JVM-Codec-/Migrations-/Exporttests, Android-Lint, Debug-Build und alle acht Instrumentierungstests auf API 35. Diese acht Tests decken echten Android-Keystore/AES-GCM-Speicher, Storage-Rundreisen, die idempotente Altchat-Migration sowie Navigation, Nahrung, Morgenwerte, Tagesabschluss und Statistikumschaltung ab. Der Release-Workflow `33387983797` wiederholte alle Gates erfolgreich und veröffentlichte https://github.com/Keen4e/KF20/releases/tag/g1-b3-2026-08-31 mit `KF20-g1-b3-2026-08-31.apk`.
- G1-B3 lokal: Server-Syntax und alle fünf providerneutralen Vertragstests sind grün; die Kotlin-Hauptquellen kompilieren. Der vollständige Windows-Gradle-Lauf trifft weiterhin die bekannte Sandbox-Sperre auf Debug-Keystore-/Gradle-JAR-Locks, deshalb ist der erfolgreiche Linux-/Emulatorlauf der verbindliche Nachweis.
- G1-E1 GitHub: Die regulären Runs `33366732702` (Branch) und `33366736631` (Draft-PR) sind für den funktionalen Commit `5e5c8d715c10fc47ca119770d95efdda937cbe0f` vollständig grün. Der erste Release-Lauf erreichte nach grünen Android-Gates einen Shell-Quotingfehler im reinen Veröffentlichungs-Schritt. Commit `5a6e385683ccceb1292835614fd7a218636cc325` korrigierte diesen Schritt; Run `33367205499` bestand erneut Serververträge, JVM-Tests, Lint und APK-Build und veröffentlichte erfolgreich https://github.com/Keen4e/KF20/releases/tag/g1-e1-2026-08-31 mit `KF20-g1-e1-2026-08-31.apk`.
- G1-E1 lokal: Kotlin-Kompilierung und installierbarer Debug-APK-Build waren erfolgreich; die APK wurde auf Android 16 installiert und gestartet. Die reine Portionslogik besitzt vier neue JVM-Prüffälle. Server-Syntax und alle 5 providerneutralen Vertragstests sind grün. Der kombinierte lokale Android-Gesamtlauf trifft weiterhin die bekannte Windows-Dateisperre auf `ui-unit-api.jar`; deshalb sind GitHub Actions und der nachgelagerte Release-Workflow die verbindliche Vollprüfung.
- G1-C1b/C2: Workflow `33109832049` ist für den funktionalen Commit `2c4ca71d85d54d9cf580f046b4233464c3856bee` vollständig grün. Serververträge, Android-JVM-Tests einschließlich der neuen Rolling-Average-Fälle, Lint, Debug-Build und Upload des Artefakts `kf20-debug-apk` (ID `9662197741`) waren erfolgreich.
- G1-C1b/C2 lokal: Kotlin-Kompilierung und Debug-APK-Build erfolgreich; APK auf Android 16 installiert. Nahrung, Morgenwerte und Tagesabschluss wurden als getrennte Erfassungsflächen geöffnet und die Statistik sichtbar zwischen Tageswerten und `7-Tage-Ø` umgeschaltet. Der vollständige lokale JVM-Lauf traf erneut die bekannte Windows-Sandbox-Dateisperre auf einer Gradle-JAR; GitHub Actions lief ohne Workaround vollständig grün.
- G1-C1a: Workflow `33107141951` ist für den funktionalen Commit `8acfa952ca1ad5d7007583730157cac0524012c4` vollständig grün. Serververträge, Android-JVM-Tests, Lint, Debug-Build und Upload des Artefakts `kf20-debug-apk` (ID `9661057166`) waren erfolgreich.
- G1-C1a lokal: Debug-APK erfolgreich gebaut, auf Android 16 installiert und in der sichtbaren Browser-Emulatoransicht geprüft. Tagesstatistik steht zuerst, Plus-Menü enthält Nahrung/Morgenwerte/Tagesabschluss, und die Hauptnavigation lautet Tag/Statistik/Chat/Einstellungen.
- G1-B2b: Der erste Workflow `33004367088` zeigte einen fehlenden `android.os.Build`-Import. Der korrigierte funktionale Commit `6effd6996f6976c37638e36365caed4390dff7d7` wurde durch Workflow `33005372164` vollständig grün verifiziert: Serververträge, Android-JVM-Tests, Lint, Debug-Build und APK-Artefakt.
- G1-B2b lokal: `compileDebugKotlin` und ein lokaler Debug-APK-Build waren erfolgreich; nur die zwei bekannten Compose-Deprecation-Warnungen bleiben. Das APK wurde auf dem Android-16-Emulator installiert.
- Privater Telegram-Testimport vom 12.07.–26.08.2026: 29 Ernährungstage, 43 Messtage und 12 Sporttage wurden ausschließlich lokal strukturiert, in den Emulator geladen und dort verschlüsselt gespeichert. Rohchat, Importdatei und private Werte wurden nicht versioniert; der einmalige Importhelfer wurde vor dem abschließenden Build entfernt.
- G1-B2a lokal: Server-Syntaxchecks erfolgreich; `npm test` 5 von 5 Tests grün. Die lokale Android-Ausführung war in der aktuellen Windows-Shell mangels registrierter Java-Runtime nicht möglich.
- Workflow-Run `33001726047` ist für den funktionalen G1-B2a-Commit `2f9b8f48d7827c503f9c53323102b50d264e73e0` vollständig grün: Server-Syntax-/Vertragstests, sechs Android-JVM-Tests, Android-Lint, Debug-Build und APK-Upload waren erfolgreich.
- G1-B1 lokal: Der neue Gradle-Wrapper lädt und verifiziert Gradle 8.11.1 erfolgreich unter Java 21. `compileDebugKotlin` kompiliert den geänderten Android-Code; der anschließende vollständige Windows-Lauf wird weiterhin durch die bekannte Sandbox-Dateisperre auf Android-/Gradle-JARs blockiert und ist daher kein vollständiger lokaler Testnachweis.
- G1-B1 lokal: Server-Syntaxchecks erfolgreich; `node --test` 5 von 5 Tests grün.
- Workflow-Run `32700645622` ist für den funktionalen G1-B1-Commit `95cd618c131dabdc899cf86f850df5eab7243b68` vollständig grün: Server-Syntax-/Vertragstests, Android-JVM-Tests, Android-Lint, Debug-Build und APK-Upload waren erfolgreich.
- Der repository-basierte Agenten-Handoff liegt in `docs/PROJECT_STATUS.md`, `docs/PI_ROADMAP.md`, `docs/DECISIONS.md`, `docs/quality-baseline.md` und `AGENTS.md`. Status, nächste Aufgaben, Tokenprognosen, Entscheidungen und Verifikation sind damit ohne früheren Chat auf GitHub übernehmbar.
- Lokaler Servercheck: `node --check` für Einstieg und Adapter sowie `node --test`; 5 von 5 Tests erfolgreich.
- Lokaler Android-Check nach dem Diagramm-Redesign: `:app:compileDebugKotlin` erfolgreich; nur bestehende Deprecation-Warnungen.
- Android wird über `.github/workflows/android.yml` mit Gradle 8.11.1, Java 21 und Android 36 gebaut.
- Lokaler Android-Check nach Mahlzeitenplanung sowie Export-/Löschfunktion: `:app:compileDebugKotlin` erfolgreich; nur bekannte Deprecation-Warnungen.
- Lokaler Android-Check nach dem Morgen-Check-Umbau: `:app:compileDebugKotlin` erfolgreich. Der Tagesbildschirm und das gemeinsame Bottom Sheet wurden auf Android 16 geöffnet; Sport, Energie, Hunger, Gewicht/KFA und die Refeed-Zielvorschau werden vollständig gerendert.
- Funktionstest Morgen-Check auf Android 16: Energie wurde von 6 auf 8 verschoben (Faktor wechselte live von 50 auf 70 Prozent), „Tag starten“ speicherte die Werte, und ein erneuter Check ersetzte den Sport-Gesamtwert des Tages ohne Doppelzählung. Abschließender `:app:assembleDebug` sowie die 5 Server-Vertragstests waren erfolgreich.
- Style-Test auf Android 16: Performance Dark, Health Light und Data Athlete wurden in Standards nacheinander aktiviert; der Wechsel erfolgte ohne Neustart und Health Light blieb nach einem Kaltstart ausgewählt. Performance Dark wurde anschließend als gewünschter Testzustand wiederhergestellt.
- Abschließender Style-Build: `:app:assembleDebug` erfolgreich; das erzeugte APK wurde auf Android 16 installiert. Performance Dark, die gespeicherte Auswahl und das abgerundete KF20-Bildlogo wurden im laufenden Emulator visuell geprüft.
- Gesprächstest auf Android 16: benannter Chat und Schnell-Chat wurden angelegt, nach Kaltstart wiedergefunden und die lokale Suche auf einen einzelnen passenden Titel eingeschränkt. Der echte Upgrade-Pfad wurde zusätzlich mit der vorherigen APK geprüft: eine dort verschlüsselt gespeicherte Nachricht erschien nach Installation des neuen Builds verlustfrei im migrierten `Hauptchat`.
- Workflow-Run `32061733437` ist für den letzten funktionalen Commit `b0490aa61bffded278ff79451f9868e43149522b` vollständig grün: Server-Syntax-/Vertragstests, Android-Debug-Build und APK-Upload waren erfolgreich.
- Lokaler Servertest nach dem Abgleich: Syntaxchecks erfolgreich, `node --test` 5 von 5 Tests grün.
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

1. Mit dem Nutzer genau ein nächstes Produktpaket priorisieren; kein vorgeschlagenes Paket ohne neues GO beginnen.
2. Kamera und Mikrofon auf einem realen Android-Gerät testen.
3. Einen zweiten Provideradapter als Wechseltest implementieren, sobald der Zielanbieter feststeht.
4. Einen dauerhaften, nutzergeführten historischen Datenimport als eigenes Feature entwerfen; weiterhin keine privaten Nachrichten oder den Roh-Export ins Repository übernehmen.
5. Konto-/Backend-Entscheidung umsetzen und danach vollständige KI-End-to-End-Tests aufbauen.

## Blocker/Hinweise

- Ein portables Android-16-Emulator-Setup ist lokal eingerichtet. Durch sporadische Windows-Sandbox-Dateisperren bleibt GitHub Actions die maßgebliche vollständige Buildprüfung.
- Die Debug-Testwoche enthält ausschließlich abstrahierte Tagesaggregate. Der private Chat-Export bleibt außerhalb von Git und App-Paket.
- Für die KI-Funktionen ist ein serverseitiger OpenAI-API-Key mit API-Abrechnung nötig; eine ChatGPT-Subscription allein genügt nicht.
- Vor Store-Veröffentlichung gelten alle Punkte in `security-release-gates.md` und `play-store-checklist.md`.

