# KF20 Qualitätsbaseline

Stand: 2026-09-01
PI-Paket: G2-K1

## Verbindliches Qualitätsgate

Jede Änderung unter `android/` oder `server/` muss in GitHub Actions folgende Schritte bestehen:

1. Server-Syntaxcheck und die providerneutralen Vertragstests
2. Android-JVM-Unit-Tests
3. Android-Lint
4. Android-Debug-Build und APK-Artefakt
5. Android-Instrumentierungstests auf einem API-35-Emulator
6. bei Paketabschluss Veröffentlichung genau dieser geprüften APK als eindeutig versioniertes GitHub-Prerelease

Android verwendet ausschließlich den eingecheckten Gradle-Wrapper 8.11.1 mit verifizierter Distributions-Prüfsumme. Der lokale Ein-Befehl-Check steht im `README.md`.

## Ausgangsbefund aus G1-A

- Die Android-Implementierung liegt überwiegend in einer rund 3.000 Zeilen großen `MainActivity.kt`; UI, Fachlogik, Speicherung und Netzwerk sind noch eng gekoppelt.
- Vor G1-B1 existierten keine Android-Unit- oder UI-Tests.
- Die frühere CI führte nur Servertests und den Android-Debug-Build aus; Lint und Android-Tests waren keine Gates.
- Ein Gradle-Wrapper fehlte, weshalb lokale Builds von einer extern installierten Gradle-Version abhingen.
- Produktspezifikation und Code weichen bei den Standardwerten für Nährwertziele voneinander ab: Die Spezifikation verlangt leere, nicht erfundene Ziele, während der Prototyp feste Startwerte setzt.

## Mit G1-B1 geschlossen

- Gradle-Wrapper und SHA-256-Prüfung sind versioniert.
- CI verwendet den Wrapper und besitzt getrennte Gates für Server, Android-Unit-Tests, Lint und Debug-Build.
- Erste JVM-Tests sichern Refeed-Bänder, dynamische Tagesziele und die Eingabegrenzen der Navy-KFA-Berechnung.

## Mit G1-B2a geschlossen

- Die gemeinsamen Domänenmodelle liegen nicht mehr in der Compose-Einstiegsdatei.
- Refeed-Faktor, adaptive Tagesziele und Navy-KFA sind Android-unabhängig gekapselt und direkt per JVM-Test ausführbar.
- Zusätzliche Tests sichern fehlende Energieangaben, negative Sportkalorien und ungültige Körpergrößen ab.

## Mit G1-B2b geschlossen

- Lokale Speicherung, Verschlüsselung, Export und Datenlöschung liegen außerhalb der Compose-Datei.
- Erinnerungsplanung und Android-Systemdienste liegen in einer eigenen Infrastrukturgrenze.
- Chat- und Nährwertanfragen laufen über einen separaten providerneutralen API-Client.
- Der vollständige CI-Lauf `33005372164` bestand Serververträge, JVM-Tests, Lint, Debug-Build und APK-Upload.

## Mit G1-C1b und G1-C2 geschlossen

- Nahrung, Morgenwerte und Tagesabschluss sind als drei getrennte Bottom-Sheet-Flows vom zentralen Plus erreichbar und im Android-16-Emulator geprüft.
- Der Tagesabschluss hält Tracker-Gesamtverbrauch und optionale Tagesnotiz getrennt von verbrannten Trainingskalorien.
- Reine JVM-Tests sichern den rollierenden Durchschnitt, das Sieben-Tage-Fenster und die Behandlung fehlender Werte ohne erfundene Nullen.
- Workflow `33109832049` bestand für Commit `2c4ca71d85d54d9cf580f046b4233464c3856bee` Serververträge, Android-JVM-Tests, Lint, Debug-Build und APK-Upload.

## G1-E1-Prüffälle

- Portionen skalieren die vier Kernwerte gemeinsam.
- Gramm benötigen ein positives Basisgewicht.
- Stück, EL und TL benötigen zusätzlich ein positives, bestätigtes Gewicht je Einheit; fehlende Werte werden nicht ersetzt.
- Ungültige oder nicht positive Mengen erzeugen keinen speicherbaren Nährwertsatz.
- Bestehende Tageslogs ohne Portionsobjekt bleiben als eine Portion lesbar; neue Einträge werden in Export-Schema 4 vollständig ausgegeben.

## Mit G1-B3 geschlossen

- Reine Codec-Tests sichern aktuelle Tageslogs, alte Einträge ohne Portion, tolerantes Überspringen beschädigter Einzelobjekte sowie alte Messwert-Feldnamen.
- Die Migration vom früheren Einzelchat in den `Hauptchat` ist als idempotenter Vertrag getestet.
- Der Exportvertrag prüft Schema 4, alle lokalen Datenbereiche und den Ausschluss von Serverzugangsdaten.
- Android-Instrumentierung prüft echte AES-GCM-/Keystore-Rundreisen, verschlüsselte Storage-Rundreisen und die einmalige Altchat-Migration.
- Compose-Instrumentierung öffnet Navigation, Plus-Auswahl, Nahrungserfassung, Morgenwerte, Tagesabschluss und die Statistik-Umschaltung.
- Branch- und Release-Workflow führen die Instrumentierung auf einem API-35-Emulator aus; ein Release entsteht erst nach allen Gates.

## Offene Qualitätsrisiken

1. Compose-Zustand und Bildschirmkomposition sind weiterhin groß und sollten erst nach priorisierten Produktentscheidungen weiter zerlegt werden.
2. Der Widerspruch bei automatisch gesetzten Nährwertzielen ist in einem eigenen Produkt-Gate zu entscheiden und zu korrigieren.
3. Kamera, Mikrofon, reale Provideraufrufe und vollständige APK-zu-APK-Upgrades benötigen weiterhin Realgerät-/E2E-Tests.

Diese Baseline ändert keine Produktfunktion. Jedes weitere Arbeitspaket wird vor Beginn separat priorisiert.

## G2-S0-Prüfung

- Das Paket ändert nur Spezifikation, Architektur, Roadmap und Agentenübergabe; es aktiviert keinen Dienst und enthält keine Secrets.
- Dokumentverweise, Paket-IDs, P50/P80-Werte und der Release-Descriptor werden auf Konsistenz geprüft.
- Gemäß Prozessregel wird die unveränderte App dennoch durch das vollständige Release-Workflow-Gate gebaut und als eigenes Debug-Prerelease veröffentlicht.

## G2-K1-Prüffälle

- Healthcheck weist den Modus `stateless-ai-bridge` und `storage: none` aus, ohne Secrets zu veröffentlichen.
- Chat und Nährwertanalyse liefern providerneutrale Ausführungsmetadaten; Antworten tragen `Cache-Control: no-store` und eine Request-ID.
- Fehlformatierte sowie übergroße Bilddaten werden vor dem Provideraufruf abgewiesen; Logs enthalten keine Anfrageinhalte.
- OpenAI-Chat und -Nährwertanalyse setzen nachweislich `store: false`.
- Docker Compose lässt sich ohne echte Secrets syntaktisch auflösen; der Normalbetrieb veröffentlicht keinen Host-Port, der lokale Override bindet nur an `127.0.0.1`.
- Android kompiliert mit dem erweiterten Antwortvertrag und zeigt Provider/keine Serverspeicherung sowie einen Verbindungstest an.
- Zentrale Datenbank-, MCP-, Telegram-, Health- und Fotoanforderungen sind als nicht implementierte Folgepakete kenntlich; kein Test darf deren Funktion vortäuschen.
