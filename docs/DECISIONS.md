# KF20 Entscheidungslog

Stand: 2026-09-01

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
| D-012 | ACCEPTED | G1-B3 ist als nächstes PI-Paket freigegeben. | Vor Health Connect werden Speicherung, Export, Upgrade und kritische Compose-Flows automatisiert abgesichert; neue Produktfunktionen bleiben außerhalb dieses Pakets. |
| D-013 | ACCEPTED | KF20 bleibt local-first; ein Konto und Cloud-Sync sind optional. | Lokale Erfassung und Statistik funktionieren offline. Der erste Sync umfasst strukturierte Daten ohne Fortschrittsbilder und behandelt Konflikte sichtbar. |
| D-014 | ACCEPTED | Das KI-Gateway unterstützt KF20-verwaltete Schlüssel, verschlüsselte Benutzerschlüssel (BYOK) und einen manuellen ChatGPT-Plus-Begleitmodus. | OpenAI direkt, Anthropic direkt und OpenRouter sind getrennte Datenwege. Es gibt keinen stillen Provider-, Vermittler- oder Kostenträgerwechsel. |
| D-015 | ACCEPTED | Provider-Secrets werden in einem eigenen Vault-Lebenszyklus getrennt von Gesundheitsdaten gehalten. | Secrets erscheinen nicht in APK, Git, Logs, Analytics, Datenexporten oder normalen Gesundheitsdaten-Backups und werden bei Widerruf/Kontolöschung entfernt. |
| D-016 | ACCEPTED | Die private Alpha läuft bevorzugt containerisiert auf dem Homeserver über Cloudflare Tunnel; die externe Beta nutzt bevorzugt Cloud Run `europe-west3` und Supabase `eu-central-1`. | Keine offenen Inbound-Ports in der Alpha; externe Primärsysteme später in Frankfurt. Portables Deployment und versionierte Migrationen halten Android vom Betriebsort unabhängig. |
| D-017 | ACCEPTED | G2-S0 legt nur Strategie und Paketgrenzen fest. | Es aktiviert keinen Cloud-Dienst, erstellt kein Konto und hinterlegt keinen echten Schlüssel; jede Umsetzung benötigt ein separates GO. |
| D-018 | ACCEPTED | G2-K1 wird als zustandslose KI-Brücke umgesetzt; Konto, Datenbank und Sync bleiben zunächst geparkt. | Gesundheitsdaten bleiben lokal in der App. Text und Fotos werden nur für die jeweilige Anfrage übertragen; die Brücke speichert sie nicht. Hermes/MCP bleibt das gesonderte Gate G2-M1. |
| D-019 | ACCEPTED | Die Anleitung für zentrales Backend, MCP, Telegram, Health Bridge, Home Assistant, Fotos, Audit und Import wird vollständig als Folge-Backlog aufgenommen. | Weil „Backend ist zentrale Datenquelle“ D-018 widerspricht, beginnt keine Zentralisierung stillschweigend. G2-D0 muss die führende Datenquelle zuerst ausdrücklich neu entscheiden; danach folgen kleine GO-Gates gemäß `MCP_BACKEND_GAP_ANALYSIS.md`. |
| D-020 | ACCEPTED | KF20 wird zunächst als vollständige Local-first-App weiterentwickelt; die App bleibt die führende Datenquelle. | Ernährung, Körperwerte, Fotos, Statistik, Einstellungen und Chatverlauf bleiben lokal. Nur ausdrücklich angeforderte KI-Inhalte verlassen das Gerät über die zustandslose G2-K1-Brücke. Zentrales Backend, Datenbank, Sync, MCP und Telegram-Schreibzugriff folgen erst nach einem späteren neuen GO. D-020 beantwortet die in D-019/G2-D0 offene Richtungsfrage zugunsten der App. |
