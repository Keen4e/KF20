# KF20 Agenten-Funktionskatalog

KF20 soll die bisherige tägliche Zusammenarbeit mit dem Agenten mobil abbilden. Jede Funktion ist einer klaren Berechtigungsklasse zugeordnet; keine externe Änderung erfolgt ohne sichtbare Freigabe in der App.

## 1. Gespräche und Denken

- Mehrere benannte Gespräche mit durchsuchbarem Verlauf.
- Laufende Aufgaben mit Plan, Fortschritt, Ergebnissen und offenen Entscheidungen.
- Antworten in Echtzeit, inklusive Quellen bei Web-Recherche.
- Vom Nutzer bestätigte Langzeit-Erinnerungen.

## 2. Recherche und Dateien

- Web-Recherche mit Quelllinks und Datum.
- Upload, Lesen und Zusammenfassen von Bildern, PDFs, Dokumenten und Quellcode.
- Generierte Dateien, Bilder und Berichte als herunterladbare Ergebnisse.

## 3. Projekte und Programmierung

- Verbundene Projekte durchsuchen und analysieren.
- Änderungen als nachvollziehbare Diff-Vorschau erzeugen.
- Tests/Builds anstoßen und Ergebnisse anzeigen.
- Dateischreibvorgänge nur nach expliziter Bestätigung.

## 4. GitHub

- OAuth-Verbindung zum eigenen GitHub-Konto.
- Repositories, Branches, Issues, Pull Requests und CI-Status lesen.
- Commits, Pushes, Kommentare, Labels und PRs nur nach einer klaren Freigabe mit Zielangabe.

## 5. Bilder und Inhalte

- Bildgenerierung und -bearbeitung mit einer Vorschau.
- Speicherung ausschließlich nach Auswahl des Nutzers.
- Keine Verwendung fremder Bilder als Bearbeitungsziel ohne Nutzerfreigabe.

## Tool-Freigaben

| Klasse | Beispiele | Verhalten |
|---|---|---|
| Lesen | Websuche, Repository lesen, Datei analysieren | direkt möglich, Ergebnis wird sichtbar belegt |
| Lokale Änderung | Datei erzeugen, Chat löschen, Bild speichern | vor Ausführung bestätigen |
| Externe Änderung | GitHub Push/PR, Nachricht senden, Deployment | immer Ziel, Wirkung und Rückgängig-Option zeigen; explizite Freigabe nötig |
| Sensibel | Zugangsdaten, Finanzen, Konto-/Rechteverwaltung | nie in Chats speichern; getrennte sichere Anmeldung |

## Umsetzungsschichten

1. **Android-App:** Gespräch, Aufgaben, Freigabeoberfläche, verschlüsselter lokaler Speicher und Datei-Auswahl.
2. **KF20-Server:** Authentifizierung, Modellaufrufe, Tool-Orchestrierung, Rate Limits und Audit-Ereignisse ohne Chat-Inhaltslogs.
3. **Nutzerverbindungen:** OAuth/Token-Aufbewahrung je Dienst, minimaler Berechtigungsumfang und jederzeit trennbar.

## Kein stiller Funktionsumfang

Kalender, E-Mail, Kontakte, Zahlungen, Standort, Nachrichtenversand und Geräteautomatisierung werden erst nach einer eigenständigen Entscheidung und Berechtigungsprüfung ergänzt.

