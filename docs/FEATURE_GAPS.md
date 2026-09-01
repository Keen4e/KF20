# KF20 Feature-Lückenanalyse

Stand: 2026-08-31

## Ziel und Methode

Diese Analyse gleicht `SPEC.md`, den abstrahierten Anforderungskatalog aus dem privaten Chat, den aktuellen Android-/Servercode und die Release-Gates ab. Sie enthält keine privaten Chatwerte und nimmt keine Produktänderung vor. Tokenwerte sind Roh-Tokenprognosen; die Plus-Wochenanzeige bleibt die verbindliche Kapazitätskalibrierung.

## Belastbarer vorhandener Kern

Der aktuelle Stand ist mehr als ein UI-Mockup:

- Tagesdashboard mit Kalorien, Protein, Fett, Carbs, Planung und gemeinsamer Tagesliste
- getrennte Erfassung für Nahrung, Morgenwerte und Tagesabschluss
- Nahrung per Text, Kamera oder Mikrofon mit korrigierbarer KI-Antwort hinter einer providerneutralen Server-API
- Morgenwerte für die aus dem Chat belegten Felder; keine erfundenen Felder wie Ruhepuls oder Schritte
- Statistik für 7/14/30 Tage sowie Tageswerte oder rollierenden 7-Tage-Durchschnitt
- Standards, Ziele, Profil, eine tägliche Erinnerung, lokale Verschlüsselung, Export und Gesamtlöschung
- mehrere lokale Chats, Suche, Erinnerungen, Aufgaben, Projekte, private Dateiverweise und Fortschrittsfotos
- CI-Gates für Serververträge, Android-JVM-Tests, Lint und Debug-APK

## Lücken nach Produktwirkung

| ID | Lücke | Aktueller Stand | Warum relevant | Priorität |
|---|---|---|---|---|
| F-01 | KI-Brücke privat betreiben | Docker-/Cloudflare-Konfiguration, Limits, Metadaten und App-Verbindungstest sind mit G2-K1 vorhanden; ein echter Host, Domain, Tunnel-Token und API-Schlüssel sind absichtlich nicht im Repository. | Nach externer Einrichtung sind Text-, Foto-, Mikrofonanalyse und Chat für den privaten Tester nutzbar. | G2-K1 DONE |
| F-12 | Zentrale App-/Telegram-/MCP-Datenquelle | Vollständig abgeglichen in `MCP_BACKEND_GAP_ANALYSIS.md`; D-020 priorisiert aber die Local-first-App. | Eine spätere Zentralisierung braucht Migration, Datenhoheits- und Löschentscheidung. | DEFERRED UNTIL NEW BACKEND GO |
| F-02 | Korrekturen und Revisionsverlauf | Tageswerte können aktualisiert oder entfernt werden; Mahlzeiten werden im Wesentlichen gelöscht und neu erfasst. Ein sichtbarer Änderungsverlauf fehlt. | Der Chat verlangt nachvollziehbare Korrekturen statt stiller Überschreibung. | P0 |
| F-03 | Backup wiederherstellen und historischen Import anbieten | JSON-Export existiert; ein Restore/Import fehlt. Der Telegram-Import war ein lokaler Einmalvorgang und die Testwoche ist Debug-only. | Gerätewechsel, Wiederherstellung und dauerhafte Übernahme alter Daten sind nicht möglich. | P0 |
| F-04 | Zielwerte korrekt initialisieren | Die Spezifikation verlangt leere, nicht erfundene Ziele; der Code setzt aktuell 2.000 kcal, 150 g Protein, 70 g Fett und 200 g Carbs. | Das verfälscht Zielerreichung bei neuen Installationen und widerspricht der verbindlichen Spezifikation. | P0 |
| F-05 | Kritische Daten- und UI-Flows automatisiert sichern | Reine Berechnungen und Serververträge sind getestet. Speicher-/Exportmigration, Restore und Compose-Kernflows sind nicht abgedeckt. | Jede weitere Funktion erhöht sonst das Risiko von Datenverlust und Regressionen. | P0 technisch |
| F-06 | Standards und Erinnerungen alltagstauglich machen | Standards lassen sich anlegen/löschen, aber nicht bearbeiten, ordnen oder duplizieren. Es gibt genau eine generische Tageserinnerung. | Wiederkehrende Mahlzeiten, Morgen-Check und Tagesabschluss sollen mit wenigen Schritten funktionieren. | P1 |
| F-07 | Fortschritt visuell vergleichen | Fotos werden privat verknüpft und angezeigt, aber nicht gelöscht, datiert korrigiert oder nebeneinander verglichen. Gewichts-/KF-Ziele werden nicht als Fortschrittsprognose zusammengefasst. | Der Chat nennt Fortschrittsdarstellungen ausdrücklich als Kernnutzen. | P1 |
| F-08 | Fehlerfälle und Offline-Verhalten | Netzfehler werden angezeigt; es gibt keine Warteschlange, gespeicherte Entwürfe, kontrollierte Wiederholung oder Statushistorie. | Unterbrochene KI-Anfragen dürfen keine Eingaben verlieren und nicht still endlos wiederholt werden. | P1 |
| F-09 | Statistiken erklärbar und untersuchbar machen | Diagramme und Zeiträume existieren. Datenpunkte sind nicht antippbar; Ausreißer, fehlende Tage und Zielwechsel werden nicht erklärt. | Eine schöne Kurve allein beantwortet nicht, wodurch sich ein Trend geändert hat. | P2 |
| F-10 | Aufgaben/Projekte/Dateien mit Agentenlogik verbinden | Diese Bereiche speichern lokale Listen bzw. Dateiverweise, sind aber nicht mit Chat, Erinnerungen oder Dateianalyse verbunden. | Aktuell wirken sie wie isolierte Zusatzprototypen und lenken vom Gesundheitskern ab. | P2 |
| F-11 | Externe Beta-/Store-Reife | Kein Produktionskonto, serverseitige Löschung/Trennung, Realgeräteabnahme, Release-Signierung oder Store-Freigabe. | Pflicht vor einem externen Test oder Play-Store-Upload. | P0 vor externem Test |
| F-12 | Aktivitätsdaten aus Health Connect | Sportverbrauch wird bislang manuell im Morgen-Check erfasst. Eine explizite Health-Connect-Verbindung, Berechtigungsverwaltung, Quellenanzeige und Dublettenkontrolle fehlen. | Aktive Kalorien und Trainingseinheiten können direkt vom Android-Gerät übernommen werden, ohne zusätzliche unbelegte Gesundheitsfelder einzuführen. | P1 |

## Vorgeschlagene, getrennt entscheidbare Pakete

| Reihenfolge | Paket | Ergebnis | Abhängigkeit | P50 | P80 |
|---:|---|---|---|---:|---:|
| 1 | G1-D0 – Zielwerte und Produktwahrheit | keine erfundenen Ziele; sauberer Erststart/Setup; widerspruchsfreie Dokumentation | aktueller Stand | 12k | 20k |
| 2 | G1-B3 – Daten-, Migrations- und UI-Tests | Speicher-, Export-, Upgrade- und Kernflow-Sicherheitsnetz | aktueller Stand | 35k | 55k |
| 3 | G1-D1 – Bearbeiten und Revisionslog | Tageswerte/Mahlzeiten korrigieren; sichtbare Änderungshistorie | G1-B3 | 45k | 70k |
| 4 | G1-D2 – Import und Restore | versionierter JSON-Restore plus nutzergeführter historischer Import mit Vorschau | G1-B3 | 45k | 75k |
| 5 | G1-D3 – Standards und Tagesroutinen | Standards bearbeiten/ordnen; getrennte Morgen-/Tagesabschluss-Erinnerungen | G1-B3 | 35k | 55k |
| 6 | G1-D4 – Fortschritt und Statistikdetails | Fotovergleich/-löschung, Zieltrend und antippbare Diagrammdetails | G1-B3 | 45k | 75k |
| 7 | G1-S1 – Supplements | Präparate, Inhaltsstoffe/Einheiten, Einnahmezeiten, Tagesbilanz, Doppelungswarnungen und Arztauszug | G1-B3 | 55k | 90k |
| 8 | G1-H1 – Health Connect Basis | opt-in Verbindung, Leseberechtigung, aktive Kalorien und Trainingseinheiten; Quelle, Berechtigungsentzug und Dubletten transparent behandeln | G1-B3 | 55k | 90k |
| 9 | G2-A1 – Private Backend-Basis | portables Docker-Deployment auf Homeserver, Cloudflare Tunnel, HTTPS, Healthcheck, Identität und Backup/Restore | G2-S0 | 55k | 90k |
| 10 | G2-A2 – KI-Gateway v1 | Provider-Registry, zentraler OpenAI-Projektschlüssel, Limits und Ausführungsmetadaten | G2-A1 | 55k | 90k |
| 11 | G2-A3 – BYOK und weitere Provider | Credential-Vault, direkte OpenAI-/Anthropic- und OpenRouter-Adapter sowie explizites Routing | G2-A2 | 75k | 120k |
| 12 | G2-B1 – Local-first Datensync | Konto, Delta-Sync, Konflikte, Export und Kontolöschung; zunächst ohne Bilder | G2-A1 | 110k | 175k |
| 13 | G3-A1 – KI-End-to-End-Abnahme | Text, Foto, Mikrofon, Chat, Fehlerfälle, Kostenpfade und Providerwechsel auf Realgerät | G2-A3 | 70k | 115k |
| 14 | G6/G7 – Externe Beta und Store | Monitoring, Release-AAB, Datenschutz- und Store-Gates | G2-A3/G2-B1/G3-A1 | 300k | 480k |

## Empfehlung für das nächste Gate

G2-K1 ist als kleine zustandslose KI-Brücke abgeschlossen. D-020 priorisiert nun wieder lokale App-Pakete; welches davon folgt, entscheidet der Nutzer separat. Die zentrale Backend-/MCP-/Telegram-Stufe bleibt bis zu einem neuen Backend-GO zurückgestellt. Eine private Testumgebung darf nicht als öffentliche Beta ausgegeben werden.

## Bewusst nicht vorgeschlagen

- Ruhepuls, Schritte oder andere nicht separat bestätigte Gesundheitsfelder; G1-H1 liest zunächst nur aktive Kalorien und Trainingseinheiten
- direkter OpenAI-Schlüssel in der APK
- automatisches Speichern einer KI-Schätzung ohne Nutzerprüfung
- Play-Store-Upload vor Konto-, Datenschutz-, Realgeräte- und Release-Gates
- Ausbau der isolierten Projekt-/Dateibereiche vor dem Gesundheitskern
- ausführliche Trainingsbibliothek im aktuellen PI; der Nutzer hat sie ausdrücklich auf später verschoben

## Entscheidungsregel

Keines der vorgeschlagenen Pakete ist freigegeben. Der Nutzer entscheidet vor dem nächsten Schritt ausdrücklich `GO`, `SPLIT`, `DEFER` oder `DROP`.
