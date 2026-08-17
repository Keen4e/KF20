# KF20 Produktspezifikation

Stand: 2026-08-17

## Produktziel

KF20 verlagert die tägliche Zusammenarbeit mit dem persönlichen Agenten aus einem fortlaufenden Chat in eine strukturierte Android-App. Strukturierte Daten sollen schnell erfasst und ausgewertet werden; der freie Chat bleibt für Planung, Rückfragen und Recherche erhalten.

Der private Chat-Export ist Anforderungsquelle, aber kein Bestandteil des Repositories. Im Repository stehen ausschließlich abstrahierte Felder und Regeln.

## Hauptnavigation

Die untere Navigation besteht aus genau vier Haupttabs:

1. **Tag** – Startseite und Erfassung aller heutigen Daten
2. **Statistik** – Zeitreihen und Zielerreichung
3. **Standards** – wiederkehrende Mahlzeiten, Ziele und persönliche Einstellungen
4. **Chat** – freier KF20-Agentendialog

Sport und Messungen sind keine eigenen Haupttabs. Ihre Erfassung gehört zum jeweiligen Tag.

Die Navigation verwendet eindeutige Material-Icons mit Textlabeln. Im App-Kopf und als Launcher-Symbol wird die vollständige Wortmarke **KF20** verwendet, nicht nur der Buchstabe K.

## Tag

### Tagesübersicht

- aufgeräumte, visuell priorisierte Startansicht: Restkalorien zuerst, danach die vier Kernwerte und die Erfassung
- großer Kalorien-Fortschrittsring mit Restwert, Zielerreichung, Aufnahme, Sport und Bilanz
- farbcodierte Fortschrittsringe für Protein, Fett und Carbs
- Datum vor/zurück und Sprung zu heute
- verbleibende Kalorien sowie Aufnahme, Sportverbrauch und Bilanz
- Kalorien, Protein, Fett und Carbs jeweils mit Ziel, Istwert und prozentualer Erreichung
- Mahlzeiten des Tages mit allen vier Nährwerten und Löschmöglichkeit
- Nahrung kann als bereits gegessen oder für später geplant gespeichert werden
- geplante Nahrung erscheint separat in der Tagesprognose und fließt erst nach Bestätigung als gegessen in Ist-Bilanz, Makroringe und Statistik ein
- Schnellzugriff auf Standards
- schlanke Tageserfassung mit drei gleichwertigen Einstiegen: Nahrung, Sport und Messwerte
- gemeinsame Tagesliste für alle drei Eintragsarten; Nahrung zeigt Kalorien und Makros, Sport zeigt Training/Tracker, Messwerte zeigen Körper- und Befindenswerte

### Mahlzeit erfassen

Es gibt zwei primäre Eingänge:

- **Foto aufnehmen:** Kamera öffnen; das Foto unmittelbar KI-gestützt interpretieren.
- **Beschreiben:** die Mahlzeit frei als Text beschreiben und den Text KI-gestützt interpretieren.
- **Mikrofon:** optional in deutscher Sprache diktieren; das erkannte Transkript unmittelbar KI-gestützt interpretieren.

Vor dem Loggen einer neuen freien Mahlzeit ist immer eine erfolgreiche KI-Auswertung erforderlich. Ergebnisfelder: Name, Kalorien, Protein, Fett und Carbs sowie Konfidenz und Hinweis. Alle Werte bleiben vor dem Speichern korrigierbar. Das Foto wird nicht im Tageslog gespeichert. Falls auf dem Gerät keine Spracheingabe verfügbar ist, bleibt die Texteingabe vollständig nutzbar.

Die Oberfläche folgt einem gestuften Ablauf: Zunächst sind nur Beschreibung, Foto, Mikrofon und der zentrale KI-Auswertungsbutton sichtbar. Die korrigierbaren Ergebnisfelder erscheinen erst nach einer erfolgreichen Auswertung. So muss der Nutzer nicht schon vor der Analyse technische Nährwertfelder bearbeiten.

Die Mahlzeitenerfassung ist im Tagesdashboard zunächst als kompakte Schnellaktion dargestellt. Das ausführliche Formular wird erst beim Beschreiben oder nach Foto-/Mikrofoneingabe geöffnet, damit die Zielübersicht visuell im Vordergrund bleibt.

Ein bereits bestätigter Standard darf ohne erneute KI-Auswertung übernommen werden, da seine Nährwerte schon gespeichert sind.

Nach der KI-Prüfung entscheidet der Nutzer ausdrücklich zwischen **Jetzt gegessen** und **Später planen**. Geplante Einträge zeigen eine Prognose für Restkalorien und Makros. Sie können in der gemeinsamen Tagesliste als gegessen bestätigt oder gelöscht werden. Dieselbe Auswahl steht für gespeicherte Standards zur Verfügung.

### Sport erfassen

Belegte Felder aus dem Chat-Verlauf:

- Aktivität: Morgensport, Laufen, Fahrrad oder kein Training
- Trainingsverbrauch in kcal
- optionaler gesamter Tagesverbrauch laut Fitness-Tracker
- optionale Notiz, etwa dass ein Rückweg später folgt

Trainingskalorien fließen in die Tagesbilanz. Tracker-Gesamtverbrauch ist eine separate Statistik und darf nicht doppelt als Sportverbrauch gerechnet werden.

### Tageswerte erfassen

Belegte Felder aus dem Chat-Verlauf:

- Gewicht in kg
- Körperfett laut Waage in Prozent
- Halsumfang in cm
- Bauchumfang in cm
- Hunger von 0 bis 10
- Energie von 0 bis 10

Wenn Hals, Bauch und Körpergröße vorliegen, berechnet KF20 den Körperfettwert zusätzlich nach der Hodgdon-Beckett-/US-Navy-Formel für Männer. Ruhepuls, Schritte und andere nicht belegte Felder gehören nicht zum aktuellen Umfang.

## Statistik

- Zeitraum 7, 14 oder 30 Tage; 7 Tage sind die Voreinstellung
- aktuelle Tageszielerreichung für Kalorien, Protein, Fett und Carbs
- Kalorien als Tagesbalken mit Ziellinie
- Protein, Fett und Carbs als farbcodierte, normalisierte Verlaufslinien
- Gewichtsverlauf
- Körperfettverlauf getrennt nach Waage und Navy-Methode
- Sportverbrauch als Tagesbalken sowie Trainingstage und durchschnittlicher gemeldeter Tracker-Verbrauch
- Hunger und Energie als zweifarbiger Verlauf auf der Skala 0 bis 10
- Zugriff auf Fortschrittsfotos

Die Farbzuordnung bleibt in Tages- und Statistikansicht konsistent: Kalorien grün, Protein blau, Fett amber und Carbs korall. Diagramme zeigen bei fehlenden Daten einen verständlichen Leerzustand und dürfen keine Werte erfinden.

Einzelwerte und KI-Schätzungen dürfen nicht als medizinische Diagnose dargestellt werden.

## Standards

- beliebig viele wiederkehrende Mahlzeiten, insbesondere ein Standardfrühstück
- Name, Kalorien, Protein, Fett und Carbs je Standard
- Tagesziele für Kalorien, Protein, Fett und Carbs
- Startgewicht und Körpergröße
- optionale Ziele für Gewicht und Körperfett
- tägliche Erinnerung
- Zugriff auf ergänzende Aufgaben und private Dateien

Zielwerte werden nicht ausgedacht. Nicht gesetzte Ziele bleiben leer.

### Chat-Testwoche im Debug-Build

Debug-Builds bieten unter **Standards** die Aktion **Chat-Testwoche laden**. Sie legt eine reproduzierbare Testwoche mit den im privaten Chat belegten Tagesaggregaten für Kalorien, Protein, Fett, Carbs, Sport, Gewicht, Körperfett, Umfang, Hunger und Energie an. Die sieben ursprünglichen Tageswerte bleiben unverändert; nur ihre Daten werden auf die jeweils letzten sieben Kalendertage verschoben, damit sämtliche Diagramme sofort sichtbar geprüft werden können.

Die Aktion ist über `BuildConfig.DEBUG` auf Entwicklungsbuilds beschränkt. Weder der rohe Chat-Export noch persönliche Nachrichten werden in App oder Repository übernommen. Ein Release-Build darf diesen Testdaten-Schalter nicht anzeigen.

## Chat

- verschlüsselte lokale Historie
- freie Agentenantworten über den KF20-Server
- vom Nutzer bestätigte Langzeit-Erinnerungen
- optional zuschaltbare Web-Recherche
- Rechercheantworten zeigen sichtbare, anklickbare Quellen
- Serververbindung und Token verschlüsselt auf dem Gerät

## KI- und Providerregel

Der Android-Client verwendet ausschließlich die provider-neutrale KF20-Server-API. Der Server besitzt ein internes Provider-Interface für Chat und Nährwertanalyse; Auswahl und Modell erfolgen ausschließlich über `AI_PROVIDER` und `AI_MODEL`. Der aktuelle Prototyp nutzt den OpenAI-Adapter. Weitere Anbieter werden als Serveradapter ergänzt, ohne Android-Datenmodelle, Oberflächen oder die stabilen KF20-Endpunkte zu ändern. Eine ChatGPT-Subscription ist kein API-Zugang; für den Prototyp ist ein separat konfigurierter Server-API-Key erforderlich.

Provider-Capabilities werden explizit ausgewiesen. Wird etwa Web-Recherche verlangt, aber vom konfigurierten Anbieter nicht unterstützt, liefert der Server einen klaren Capability-Fehler und erfindet keine Quellen.

## Datenschutz

- Gesundheits- und Chatdaten lokal AES-GCM-verschlüsselt speichern.
- Vollständigen lokalen JSON-Export auf ausdrückliche Nutzeraktion anbieten; der Export schließt Zugangstoken aus und warnt sichtbar, dass die erzeugte Datei selbst nicht verschlüsselt ist.
- Alle lokalen KF20-Daten, gespeicherten Dateizugriffe und den Android-Keystore-Schlüssel nach einer eindeutigen Bestätigung vollständig löschen können.
- Keine Provider-Schlüssel in App, Git oder Logs.
- Keine Chat-Inhaltslogs im Serverbetrieb.
- Bilder nur für die konkrete Analyse übertragen und nicht serverseitig speichern.
- Serverseitiger Export, Kontolöschung und definierte Aufbewahrung sind vor Store-Release Pflicht, sobald Nutzerkonten eingeführt werden.

## Abnahmekriterien vor Play Store

- Alle oben genannten Funktionen auf mindestens einem realen Android-Gerät end-to-end getestet.
- Produktionskonto, Authentifizierung, HTTPS-Backend und Kontolöschung implementiert.
- Kamera, Texteingabe, Mikrofon, KI-Analyse, Standards, Korrektur und Statistiken verifiziert.
- Fehlerfälle ohne Datenverlust und mit verständlicher Meldung.
- Datenschutztext, Data-Safety-Angaben, Signierung, Store-Listing und Pre-Launch-Report abgeschlossen.
- Upload erst nach vollständiger Erfüllung der Release-Gates.

