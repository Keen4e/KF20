# KF20 Qualitätsbaseline

Stand: 2026-08-27
PI-Paket: G1-C1b + G1-C2

## Verbindliches Qualitätsgate

Jede Änderung unter `android/` oder `server/` muss in GitHub Actions folgende Schritte bestehen:

1. Server-Syntaxcheck und die providerneutralen Vertragstests
2. Android-JVM-Unit-Tests
3. Android-Lint
4. Android-Debug-Build und APK-Artefakt

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

## Offene Qualitätsrisiken

1. Speicher-, Export- und Gesprächsmigration benötigen eigene Unit-/Migrationstests.
2. Kritische Compose-Flows benötigen Instrumentierungs- beziehungsweise UI-Tests.
3. Compose-Zustand und Bildschirmkomposition sind weiterhin groß und sollten erst nach priorisierten Produktentscheidungen weiter zerlegt werden.
4. Der Widerspruch bei automatisch gesetzten Nährwertzielen ist in einem eigenen Produkt-Gate zu entscheiden und zu korrigieren.
5. Kamera, Mikrofon und Upgrade-Pfade benötigen weiterhin Realgerät-Tests.

Diese Baseline ändert keine Produktfunktion. Jedes weitere Arbeitspaket wird vor Beginn separat priorisiert.
