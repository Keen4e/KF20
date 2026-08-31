# KF20 Entscheidungslog

Stand: 2026-08-31

Nur bestätigte, dauerhaft relevante Entscheidungen werden hier aufgenommen. Neue Einträge erhalten eine fortlaufende ID und verändern frühere Entscheidungen nicht still; Ablösungen verweisen auf die ersetzte ID.

| ID | Status | Entscheidung | Begründung / Folge |
|---|---|---|---|
| D-001 | ACCEPTED | Die Hauptnavigation besteht aus Tag, Statistik, Standards und Chat. | Sport und Messwerte gehören zum Tag; der 7-Tage-Überblick zur Statistik. |
| D-002 | ACCEPTED | Android bleibt KI-providerneutral. | Providerwahl, Modell und Schlüssel liegen ausschließlich hinter der KF20-Server-API. |
| D-003 | ACCEPTED | Strukturierte Gesundheitsfelder werden nur aus bestätigten oder abstrahiert belegten Anforderungen übernommen. | Ruhepuls, Schritte und andere unbelegte Felder werden nicht ergänzt. |
| D-004 | ACCEPTED | PI-Arbeit wird vor jedem Paket priorisiert. | Erlaubte Entscheidungen sind GO, SPLIT, DEFER und DROP. Ohne GO keine Umsetzung. |
| D-005 | ACCEPTED | Pro Woche werden höchstens 70 Prozent des Codex-Kontingents geplant. | 30 Prozent bleiben Reserve; tatsächlicher Plus-Verbrauch kalibriert die Roh-Tokenprognosen. |
| D-006 | ACCEPTED | Der Entwicklungsbranch `codex/kf20-rebuild` und Draft-PR #1 sind bis zu einem eigenen Merge-Gate die aktuelle Quelle. | `main` enthält nicht den aktuellen Entwicklungsstand. |
| D-007 | ACCEPTED | Projektstatus, nächste Aufgaben, Verifikation und Entscheidungen müssen vollständig im Repository liegen. | Ein neuer KI-Agent muss ohne Zugriff auf frühere Chats übernehmen können. |
| D-008 | ACCEPTED | D-001 wird bei Reihenfolge und Benennung abgelöst: Die Hauptnavigation lautet Tag, Statistik, Chat, Einstellungen. | Der Nutzer hat Chat vor Einstellungen und Einstellungen ganz rechts bestätigt; Sport und Messwerte bleiben Bestandteil des Tags. |
| D-009 | ACCEPTED | Supplement-Erfassung wird in die Roadmap aufgenommen; eine ausführliche Trainingsbibliothek bleibt vorerst zurückgestellt. | Der Nutzer hat Supplements bestätigt und Training ausdrücklich auf später verschoben. |
| D-010 | ACCEPTED | Jedes abgeschlossene PI-Paket erhält nach grünen Qualitätsgates eine installierbare APK als eindeutig versioniertes GitHub-Prerelease. | Actions-Artefakte allein sind zeitlich begrenzt; ein Paket gilt erst mit dokumentiertem Release-Link als DONE. |
| D-011 | ACCEPTED | Das direkte Lesen passender Aktivitätsdaten aus Android Health Connect wird als eigenes Paket G1-H1 geplant. | Zunächst werden nur aktive Kalorien und Trainingseinheiten gelesen; weitere Gesundheitsfelder brauchen eine separate Produktentscheidung und eigene Berechtigungsbegründung. |
