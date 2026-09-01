# KF20 Produktspezifikation

Stand: 2026-08-27

## Produktziel

KF20 verlagert die tägliche Zusammenarbeit mit dem persönlichen Agenten aus einem fortlaufenden Chat in eine strukturierte Android-App. Strukturierte Daten sollen schnell erfasst und ausgewertet werden; der freie Chat bleibt für Planung, Rückfragen und Recherche erhalten.

Der private Chat-Export ist Anforderungsquelle, aber kein Bestandteil des Repositories. Im Repository stehen ausschließlich abstrahierte Felder und Regeln.

## Hauptnavigation

Die untere Navigation besteht aus genau vier Haupttabs:

1. **Tag** – Startseite und Erfassung aller heutigen Daten
2. **Statistik** – Zeitreihen und Zielerreichung
3. **Chat** – freier KF20-Agentendialog
4. **Einstellungen** – wiederkehrende Mahlzeiten/Standards, Ziele und persönliche Einstellungen

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
- Tagesstatistik für Kalorien, Protein, Fett und Carbs direkt am Seitenanfang
- genau ein schwebendes Plus unten rechts als Einstieg für Nahrung, Morgenwerte und Tagesabschluss
- gemeinsame Tagesliste für alle drei Eintragsarten; Nahrung zeigt Kalorien und Makros, Sport zeigt Training/Tracker, Messwerte zeigen Körper- und Befindenswerte

### Mahlzeit erfassen

Es gibt zwei primäre Eingänge:

- **Foto aufnehmen:** Kamera öffnen; das Foto unmittelbar KI-gestützt interpretieren.
- **Beschreiben:** die Mahlzeit frei als Text beschreiben und den Text KI-gestützt interpretieren.
- **Mikrofon:** optional in deutscher Sprache diktieren; das erkannte Transkript unmittelbar KI-gestützt interpretieren.

Vor dem Loggen einer neuen freien Mahlzeit ist immer eine erfolgreiche KI-Auswertung erforderlich. Ergebnisfelder: Name, Kalorien, Protein, Fett und Carbs sowie Konfidenz und Hinweis. Alle Werte bleiben vor dem Speichern korrigierbar. Das Foto wird nicht im Tageslog gespeichert. Falls auf dem Gerät keine Spracheingabe verfügbar ist, bleibt die Texteingabe vollständig nutzbar.

Die Oberfläche folgt einem gestuften Ablauf: Zunächst sind nur Beschreibung, Foto, Mikrofon und der zentrale KI-Auswertungsbutton sichtbar. Die korrigierbaren Ergebnisfelder erscheinen erst nach einer erfolgreichen Auswertung. So muss der Nutzer nicht schon vor der Analyse technische Nährwertfelder bearbeiten.

Die Mahlzeitenerfassung öffnet als eigenes, scrollbar bleibendes Popup. Zunächst sind nur Beschreibung, Foto, Mikrofon und KI-Auswertung sichtbar; die Ergebnisfelder folgen nach der Analyse. Das Tagesdashboard bleibt im Hintergrund unverändert.

Ein bereits bestätigter Standard darf ohne erneute KI-Auswertung übernommen werden, da seine Nährwerte schon gespeichert sind.

Nach der KI-Prüfung entscheidet der Nutzer ausdrücklich zwischen **Jetzt gegessen** und **Später planen**. Geplante Einträge zeigen eine Prognose für Restkalorien und Makros. Sie können in der gemeinsamen Tagesliste als gegessen bestätigt oder gelöscht werden. Dieselbe Auswahl steht für gespeicherte Standards zur Verfügung.

### Menge, Einheit und Zubereitungszustand

Die korrigierte KI-Antwort gilt zunächst für **eine analysierte Portion**. Vor dem Speichern kann der Nutzer die Menge als Portion, Gramm, Stück, Esslöffel oder Teelöffel angeben. Portionen werden direkt skaliert. Für Gramm ist das Gewicht der analysierten Basisportion erforderlich; für Stück, EL und TL zusätzlich das vom Nutzer bestätigte Gewicht je Einheit. KF20 erfindet keine pauschalen Stück-, EL- oder TL-Gewichte.

Kalorien, Protein, Fett und Carbs werden gemeinsam live auf die gewählte Menge skaliert und bleiben vor dem Speichern im Vordergrund. Der Nutzer kennzeichnet den Zubereitungszustand als nicht angegeben, roh oder zubereitet und kann die Annahme der KI korrigieren oder ergänzen. Menge, Einheit, bestätigte Gewichte, Zubereitungszustand und Annahme bleiben am Tageseintrag sichtbar, lokal gespeichert und im Export erhalten. Bestehende Einträge ohne diese Metadaten werden verlustfrei als eine Portion gelesen.

### Morgen-Check: Sport und Tageswerte

Sport und die täglichen Körper-/Befindenswerte werden gemeinsam als **Morgenwerte** erfasst. Das zentrale Plus öffnet die Auswahl Nahrung, Morgenwerte und Tagesabschluss. Die Tagesseite zeigt unterhalb des Kalorien- und Makro-Dashboards zusätzlich den Status **Morgen-Check offen/erledigt**. Morgenwerte öffnen eine große, von unten kommende und scrollbar bleibende Erfassungsfläche im Stil eines zusammenhängenden Tagesstarts.

Der primäre Morgen-Check enthält:

- Sportverbrauch laut Tracker als Schieberegler
- Energie von 1 bis 10 als Schieberegler
- Hunger von 0 bis 10 als Schieberegler
- Gewicht und Körperfett laut Waage als kompakte Eingabefelder
- Hals- und Bauchumfang als optional einblendbare Zusatzfelder
- eine live aktualisierte Vorschau für Kalorien-, Protein-, Fett- und Carbs-Ziel

Der Nutzer bestätigt alle Werte gemeinsam mit **Tag starten** oder verlässt die Erfassung mit **Später**. Ein erneutes Öffnen aktualisiert denselben Morgen-Check des Tages, statt doppelte Sporteinträge zu erzeugen. Nahrung bleibt davon getrennt und wird weiterhin per Text, Foto oder Mikrofon mit KI ausgewertet.

### Tagesabschluss

Der Tagesabschluss öffnet ein eigenes Popup mit der aktuellen Kalorien- und Makrobilanz. Er erfasst den gesamten Tagesverbrauch laut Tracker sowie eine optionale Tagesnotiz. Der Tracker-Gesamtverbrauch bleibt vom Sportverbrauch getrennt und darf nicht nochmals als verbrannte Trainingskalorien in die Bilanz eingehen. Ein erneutes Speichern aktualisiert den Tagesdatensatz.

Der aus dem Chat belegte Refeed-Faktor wird aus dem Energiewert abgeleitet:

- Energie 1–4: 30 Prozent
- Energie 5–7: 50 Prozent
- Energie 8–10: 70 Prozent

Das dynamische Kalorienziel entspricht dem konfigurierten Basisziel plus Sportverbrauch mal Refeed-Faktor. Das Add-on wird im aktuellen Modell den Carbs zugerechnet; Protein- und Fettziel bleiben unverändert. Die Tagesansicht zeigt das so berechnete Ziel unmittelbar nach dem Speichern.

### Weitere Sportdaten

Belegte Felder aus dem Chat-Verlauf:

- Aktivität: Morgensport, Laufen, Fahrrad oder kein Training
- Trainingsverbrauch in kcal
- optionaler gesamter Tagesverbrauch laut Fitness-Tracker
- optionale Notiz, etwa dass ein Rückweg später folgt

Trainingskalorien fließen in die Tagesbilanz. Tracker-Gesamtverbrauch ist eine separate Statistik und darf nicht doppelt als Sportverbrauch gerechnet werden.

### Gespeicherte Tageswerte

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
- sichtbarer Umschalter **Tageswerte / 7-Tage-Ø**; der rollierende Durchschnitt verwendet je Punkt den aktuellen und die sechs vorherigen Kalendertage, ignoriert fehlende Messwerte und erfindet keine Nullwerte für fehlende Ernährungstage

Die Farbzuordnung bleibt in Tages- und Statistikansicht konsistent: Kalorien grün, Protein blau, Fett amber und Carbs korall. Diagramme zeigen bei fehlenden Daten einen verständlichen Leerzustand und dürfen keine Werte erfinden.

Einzelwerte und KI-Schätzungen dürfen nicht als medizinische Diagnose dargestellt werden.

## Einstellungen

Die Seite **Einstellungen** enthält Standards/Routinen, Tagesziele, persönliche Start- und Zielwerte, Erinnerungen, Designauswahl sowie lokale Datenfunktionen.

### Standards

- beliebig viele wiederkehrende Mahlzeiten, insbesondere ein Standardfrühstück
- Name, Kalorien, Protein, Fett und Carbs je Standard
- Tagesziele für Kalorien, Protein, Fett und Carbs
- Startgewicht und Körpergröße
- optionale Ziele für Gewicht und Körperfett
- tägliche Erinnerung
- Zugriff auf ergänzende Aufgaben und private Dateien
- gespeicherte Designauswahl für die gesamte App: Performance Dark, Health Light oder Data Athlete

**Performance Dark** ist für neue Installationen der Standard und orientiert sich an der dunklen Morgen-Check-Referenz. **Health Light** bietet die helle bisherige Farbwelt. **Data Athlete** nutzt eine technischere dunkelblaue Oberfläche. Der Wechsel erfolgt sofort, bleibt nach einem Neustart erhalten und verändert keine fachlichen Daten oder Berechnungen.

Zielwerte werden nicht ausgedacht. Nicht gesetzte Ziele bleiben leer.

### Chat-Testwoche im Debug-Build

Debug-Builds bieten unter **Einstellungen** die Aktion **Chat-Testwoche laden**. Sie legt eine reproduzierbare Testwoche mit den im privaten Chat belegten Tagesaggregaten für Kalorien, Protein, Fett, Carbs, Sport, Gewicht, Körperfett, Umfang, Hunger und Energie an. Die sieben ursprünglichen Tageswerte bleiben unverändert; nur ihre Daten werden auf die jeweils letzten sieben Kalendertage verschoben, damit sämtliche Diagramme sofort sichtbar geprüft werden können.

Die Aktion ist über `BuildConfig.DEBUG` auf Entwicklungsbuilds beschränkt. Weder der rohe Chat-Export noch persönliche Nachrichten werden in App oder Repository übernommen. Ein Release-Build darf diesen Testdaten-Schalter nicht anzeigen.

## Chat

- mehrere benannte Gespräche mit jeweils eigener, verschlüsselter lokaler Historie
- Gesprächsübersicht zum Anlegen, Wechseln und bestätigten Löschen einzelner Gespräche
- lokale Volltextsuche über Gesprächstittel und Nachrichten; Suchbegriffe und Treffer verlassen das Gerät nicht
- ein vorhandener Einzelverlauf wird beim ersten Start verlustfrei als **Hauptchat** übernommen
- nur die Nachrichten des aktuell geöffneten Gesprächs werden als Kontext an den KF20-Server gesendet
- freie Agentenantworten über den KF20-Server
- vom Nutzer bestätigte Langzeit-Erinnerungen
- optional zuschaltbare Web-Recherche
- Rechercheantworten zeigen sichtbare, anklickbare Quellen
- Serververbindung und Token verschlüsselt auf dem Gerät

## KI- und Providerregel

Der Android-Client verwendet ausschließlich die provider-neutrale KF20-Server-API. Der Server besitzt ein internes Provider-Interface für Chat und Nährwertanalyse. Der aktuelle Prototyp nutzt den OpenAI-Adapter. Weitere Anbieter werden als Serveradapter ergänzt, ohne Android-Fachmodelle oder die stabilen KF20-Endpunkte zu ändern.

Die Einstellungen bieten künftig drei ausdrücklich getrennte Zugangsarten:

- **KF20 verwaltet:** ein serverseitiger Projekt-/Service-Schlüssel des Betreibers mit Nutzerlimits und Kostenkontrolle
- **Eigener Schlüssel (BYOK):** benutzereigener Schlüssel für OpenAI direkt, Anthropic direkt oder OpenRouter; verschlüsselt im separaten Secret Vault
- **ChatGPT-Plus-Begleitmodus:** manueller, nutzergeprüfter Austausch ohne automatische API-Nutzung

Eine ChatGPT-Subscription ist kein API-Zugang. KF20 fordert niemals ChatGPT-Passwort, Session-Cookie oder Browser-Token an. Provider, Modell, Zugangsart und Datenweg sind für den Nutzer sichtbar. Es gibt keinen stillen Wechsel zwischen verwaltetem und eigenem Schlüssel, direktem Provider und OpenRouter oder zu einem anderen Provider.

Provider-Capabilities werden explizit ausgewiesen. Wird etwa Web-Recherche verlangt, aber vom konfigurierten Anbieter nicht unterstützt, liefert der Server einen klaren Capability-Fehler und erfindet keine Quellen.

### Private KI-Brücke G2-K1

Für den privaten Einzeltest kann die KF20-API containerisiert auf dem Homeserver laufen und ausschließlich über einen ausgehenden Cloudflare Tunnel per HTTPS erreichbar sein. Die Brücke besitzt keine Nutzerkonten, Gesundheitsdatenbank oder Synchronisation. Sie verarbeitet Chat-, Text- und Fotoanfragen nur im Arbeitsspeicher, setzt beim OpenAI-Adapter `store: false` und schreibt weder Anfrageinhalte noch Providerantworten in Logs oder Dateien. Jede Antwort enthält nicht-sensitive Ausführungsmetadaten für Provider, Zugangsart und `storage: none`; jede Anfrage erhält eine Request-ID.

Der zentrale Provider-Schlüssel verbleibt auf dem Server. Android speichert nur Brückenadresse und separaten privaten Alpha-Zugangstoken verschlüsselt und bietet einen Healthcheck. Ein Essensfoto ist auf 1 MB Binärdaten begrenzt, wird als neu komprimiertes JPEG übertragen und nicht in das Tageslog übernommen. Die Nutzeroberfläche weist vor der Analyse auf die einmalige Übertragung hin.

Die Anleitung für ein zentrales Backend mit MCP, Telegram, Health Bridge, Home Assistant, dauerhaften Fotos und Audit ist vollständig in `docs/MCP_BACKEND_GAP_ANALYSIS.md` erfasst. Sie ersetzt die aktuelle Local-first-Führung erst nach einer ausdrücklichen G2-D0-Entscheidung.

## Datenschutz

- Gesundheits- und Chatdaten lokal AES-GCM-verschlüsselt speichern.
- Cloud-Sync bleibt optional und local-first; Erfassung und Statistik funktionieren ohne Konto und ohne Netz.
- Zugangsschlüssel werden getrennt von Gesundheits- und Chatdaten in einem Secret Vault gespeichert, niemals synchronisiert oder exportiert und bei Kontolöschung vollständig entfernt.
- Vollständigen lokalen JSON-Export auf ausdrückliche Nutzeraktion anbieten; Schema 4 enthält benannte Gespräche, aktiven Gesprächsbezug, Designauswahl und die transparenten Portionsmetadaten der Nahrung, schließt Zugangstoken aus und warnt sichtbar, dass die erzeugte Datei selbst nicht verschlüsselt ist.
- Alle lokalen KF20-Daten, gespeicherten Dateizugriffe und den Android-Keystore-Schlüssel nach einer eindeutigen Bestätigung vollständig löschen können.
- Keine Provider-Schlüssel in App, Git oder Logs.
- Keine Chat-Inhaltslogs im Serverbetrieb.
- Die private KI-Brücke besitzt keine Fachdatenbank; ihre Antworten tragen `storage: none`. Der externe KI-Anbieter verarbeitet die übermittelten Inhalte nach seinen eigenen API-Bedingungen.
- Bilder nur für die konkrete Analyse übertragen und nicht serverseitig speichern.
- Serverseitiger Export, Kontolöschung und definierte Aufbewahrung sind vor Store-Release Pflicht, sobald Nutzerkonten eingeführt werden.
- Fortschrittsbilder bleiben bis zu einem separat freigegebenen Bild-Sync lokal. KI-Fotos sind nur temporäre Anfrageinhalte und werden nicht als Sync-Objekt gespeichert.

### Kompatibilitäts- und Qualitätsgarantien

- Bestehende verschlüsselte Tageslogs und Messwerte bleiben nach App-Upgrades lesbar; unbekannte oder beschädigte Einzelobjekte dürfen andere gültige Datensätze nicht verwerfen.
- Der frühere einzelne Chatverlauf wird höchstens einmal in den benannten `Hauptchat` übernommen. Bereits vorhandene benannte Gespräche werden durch die Migration weder ersetzt noch dupliziert.
- Export-Schema 4 enthält alle fachlichen lokalen Datenbereiche und Portionsmetadaten, aber keine API-Zugangsdaten.
- Diese Garantien werden durch reine JVM-Vertrags- und Migrationstests sowie Android-Instrumentierungstests für Keystore, Speicher und die kritischen Erfassungs- und Navigationsflüsse abgesichert.

## Abnahmekriterien vor Play Store

- Alle oben genannten Funktionen auf mindestens einem realen Android-Gerät end-to-end getestet.
- Produktionskonto, Authentifizierung, HTTPS-Backend und Kontolöschung implementiert.
- Kamera, Texteingabe, Mikrofon, KI-Analyse, Standards, Korrektur und Statistiken verifiziert.
- Fehlerfälle ohne Datenverlust und mit verständlicher Meldung.
- Datenschutztext, Data-Safety-Angaben, Signierung, Store-Listing und Pre-Launch-Report abgeschlossen.
- Upload erst nach vollständiger Erfüllung der Release-Gates.

