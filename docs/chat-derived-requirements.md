# Aus dem bisherigen Chat abgeleitete Anforderungen

Dieser Katalog basiert auf einer Auswertung des bereitgestellten privaten Chat-Exports. Er enthält nur verallgemeinerte Produktanforderungen und keine persönlichen Messwerte, Nachrichten oder Bilder.

## Primärer Arbeitsbereich: Tages- und Gesundheitslog

Der bisherige Agent wird hauptsächlich als fortlaufender persönlicher Tagesbegleiter verwendet:

- Mahlzeiten per Text, Bild und Rezept erfassen.
- Kalorien, Protein, Kohlenhydrate, Fett und Tagesbilanz nachvollziehen.
- Sport, Gewicht, Körperwerte, Energie und Hunger als Zeitreihe erfassen.
- Tagesplanung anhand bereits erfasster und geplanter Mahlzeiten.
- Standardmahlzeiten und persönliche Präferenzen ausdrücklich speichern.
- Tages-, Wochen- und Verlaufsstatistiken sowie ein Dashboard erzeugen.
- Fortschrittsbilder speichern und zeitlich vergleichen.

## Wiederkehrende Agenten-Aktionen

- Tägliche Erinnerungen und geplante Nachfragen.
- Revisionssichere Tageslogs: Der Agent gleicht neue Einträge mit dem richtigen Datum ab und korrigiert sie sichtbar statt Informationen still zu überschreiben.
- Import/Abgleich historischer Chat- und Logdaten.
- Robuste Wiederholungslogik für fehlgeschlagene Hintergrundaufgaben, jedoch mit sichtbarem Status statt endloser stiller Versuche.

## Ergänzende Arbeitsbereiche

- Dateiverwaltung, Dashboard-/Webseitenbau und Projektanalyse.
- Bildanalyse sowie Bild-/Videoerstellung für Fortschrittsdarstellungen.
- Web-Recherche, etwa zu Speisekarten oder Lebensmitteln, mit Quellen.
- GitHub-/Code-Arbeit als separater Projektbereich.

## Produktpriorität

1. Verschlüsseltes Tageslog, Tagesbilanz, Standardmahlzeiten und Erinnerungen.
2. Foto-/Belegeingabe, Korrekturen und Dashboard mit Verlauf.
3. Recherche für Lebensmittel/Speisekarten und Aufgaben/Projekte.
4. Dateien, Code, GitHub und kreative Medien.

## Sicherheits- und Gesundheitsgrenzen

- Gesundheitsdaten gelten als besonders schützenswert und werden lokal verschlüsselt gespeichert.
- KF20 macht keine Diagnose und ersetzt keine ärztliche oder ernährungsmedizinische Beratung.
- Jede automatisch geschätzte Nährwertangabe muss als Schätzung erkennbar bleiben und vom Nutzer korrigierbar sein.

