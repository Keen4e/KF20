# Abgleich: zentrales Backend, MCP und Telegram

Stand: 2026-09-01

Diese Datei überführt die Nutzeranleitung vom 1. September 2026 in prüfbare KF20-Arbeitspakete. Sie ist eine Anforderungs- und Lückenanalyse, keine Behauptung, dass die genannten Funktionen bereits implementiert sind.

## Architekturkonflikt vor Umsetzung

Die Anleitung verlangt das Backend als zentrale Datenquelle für App und Telegram. Die aktuell akzeptierte Entscheidung D-018 hält Gesundheitsdaten dagegen zunächst ausschließlich in der App und erlaubt nur eine zustandslose KI-Brücke. Beides kann nicht gleichzeitig die führende Datenquelle sein. Deshalb bleibt G2-K1 unverändert; die Zentralisierung beginnt erst nach dem separaten Gate G2-D0. Eine spätere Zentralisierung muss Offline-Nutzung, Datenhoheit, Löschung und Migration ausdrücklich neu entscheiden.

## Funktionsabgleich

| Bereich | Aktueller Stand nach G2-K1 | Fehlender Zielumfang |
|---|---|---|
| KI | providerneutrale Text-/Foto-/Chat-Brücke ohne Serverspeicherung | kein Bestandteil der zentralen Fachdatenspeicherung |
| Datenhaltung | AES-GCM-verschlüsselt lokal in Android | PostgreSQL-Schema, Migrationen, Objekt-IDs, UTC-Zeitstempel, Nutzerzeitzone, Soft Delete, Versionierung |
| App-REST | nur `/v1/chat`, `/v1/nutrition/analyze`, `/healthz` | alle beschriebenen `/api/v1`-Tages-, Mahlzeit-, Körper-, Health-, Foto-, Report- und Audit-Endpunkte |
| Auth | gemeinsamer privater Alpha-Bearer-Token | getrennte Clients, gehashte/rotierbare Tokens oder OAuth2, Scopes und serverseitige Nutzeridentität |
| MCP | nicht vorhanden | HTTPS Streamable HTTP, 19 Tools, sieben Resources, Schema-Validierung und Scope-Prüfung |
| Telegram | vorhandener Export nur als Anforderungsquelle | robuster Adapter, gemeinsamer Service-Layer, `message_id`-Idempotenz, Retry/Outbox, Bildübernahme |
| Mahlzeiten | lokal erstellen/löschen, KI-Schätzung und Portionsskalierung | serverseitige Idempotenz, Korrektur/Ersetzen, Soft Delete, optimistic locking und Audit |
| Tracker | lokale Sport-/Morgenwerte; Health Connect als G1-H1 geplant | Upload-Bridge, Rohquellen-ID, Deduplikation, überlappungsfeste Aggregate, Retry-Queue und Status |
| Home Assistant | nicht vorhanden | Gewicht/KFA-Connector mit `null`/`unavailable` statt Schätzung und dokumentiertem Quellenstatus |
| Fotos | lokale URI für Fortschrittsfotos; KI-Foto nur temporär | dauerhafter verschlüsselter Objektspeicher, Hash, Größe, Ansicht, Metadaten, Soft Delete und Audit |
| Import | lokaler JSON-Export; kein Restore | versionierter, wiederholbarer Dry-run/Import mit Fehlerbericht und Herkunft |
| Reports | lokale Statistiken und rollierender 7-Tage-Durchschnitt | deterministischer gemeinsamer Balance-/Wochenreport-Service für REST und MCP |
| Audit | nicht vorhanden | append-only Historie mit Akteur, Quelle, Grund, Request-ID, Zeitstempel und Vorher/Nachher ohne Secrets |

## Verbindliche Datenregeln für die Folgepakete

- Fachdatum ist `YYYY-MM-DD`; Ereigniszeitpunkte werden in UTC gespeichert, die IANA-Zeitzone des Nutzers separat.
- Jedes Fachobjekt besitzt stabile ID, `source`, `created_at`, `updated_at`, `version` und bei Löschung einen Löschmarker.
- Jede Schreiboperation verlangt einen mandanten- und operationsgebundenen `idempotency_key`. Telegram verwendet zusätzlich die Kombination aus Chat-, Thread- und `message_id`; Health Uploads verwenden Quelle und `raw_source_id`.
- REST, MCP, Telegram und Imports rufen dieselbe Serviceschicht auf. Geschäftslogik wird nicht in Transportadaptern dupliziert.
- Makros, Tagesbilanz und Wochenaggregate sind deterministische Funktionen. Fehlende Tracker-, Home-Assistant-, Gewichts- oder KFA-Werte bleiben fehlend.
- Schätzungen erhalten `estimated`, `uncertainty` und eine Nutzerbestätigung. Korrekturen ersetzen die aktuelle Version und erzeugen Audit, statt eine doppelte Mahlzeit anzuhängen.
- Fotos werden getrennt von relationalen Metadaten in einem privaten Objektspeicher gehalten; Zugriff erfolgt nur autorisiert und zeitlich begrenzt. KI-Eingabefotos bleiben davon getrennte temporäre Anfragen.
- Secrets, rohe Tokens, Passwörter und vertrauliche Inhalte erscheinen nie in Antworten, Logs, Audit, Reports oder Chatnachrichten.

## Geplante Schnittstellengrenzen

```text
Android REST ─┐
Telegram ─────┼─> Auth/Scopes -> gemeinsame Application Services -> PostgreSQL
MCP/HTTPS ────┘                         │                         -> privater Fotospeicher
Health Bridge -> Ingest/Queue/Dedupe ───┤
Home Assistant -> Connector ────────────┘

Android/Telegram -> zustandslose KI-Brücke -> konfigurierbarer KI-Provider
```

MCP wird unter einer konfigurierbaren HTTPS-Basis, standardmäßig Pfad `/mcp`, als Streamable HTTP angeboten. Öffentliches Klartext-HTTP ist verboten; `http://127.0.0.1` ist nur Entwicklung. Die genaue MCP-SDK-Version und das Transportverhalten werden bei Beginn von G2-M1 gegen die dann aktuelle offizielle MCP-Spezifikation geprüft.

### Vollständiges MCP-Inventar für G2-M1

Die 19 geforderten Toolnamen sind: `get_current_datetime`, `get_user_profile`, `get_day_log`, `list_day_logs`, `search_logs`, `create_meal`, `update_meal`, `correct_meal`, `delete_meal`, `add_tracker_reading`, `get_tracker_readings`, `get_body_measurements`, `sync_home_assistant_body_data`, `get_health_bridge_status`, `save_photo`, `list_photos`, `calculate_daily_balance`, `get_weekly_report` und `get_audit_history`.

Die sieben Resources sind `profile://current`, `diet://day/{date}`, `diet://week/{iso_week}`, `health://latest`, `health://bridge/status`, `photos://date/{date}` und `audit://day/{date}`. Schreibtools verlangen `idempotency_key`, Quellangabe und bei Updates eine erwartete Version. Profile geben Körpergröße, nullable Alter/Geschlecht, Ziele, Makroziele, Refeed-Regeln und Präferenzen, aber niemals Secrets zurück. Tageslogs umfassen die ausdrücklich geforderten Mahlzeiten, Körper-/Tracker-/Training-/Hunger-/Energiewerte, Bilanz, Fotos und nullable Mikronährstoffe. Die detaillierten JSON-Schemas werden in G2-D0 versioniert, bevor Code davon abhängt.

### Vollständiges REST-Inventar für G2-D2

Vorgesehen sind exakt `GET /api/v1/days/{date}`, `POST /api/v1/days/{date}/meals`, `PATCH /api/v1/meals/{meal_id}`, `DELETE /api/v1/meals/{meal_id}`, `GET /api/v1/body/latest`, `GET /api/v1/body/history`, `GET /api/v1/health/latest`, `POST /api/v1/health-sync`, `GET /api/v1/health-sync/status`, `POST /api/v1/photos`, `GET /api/v1/photos`, `GET /api/v1/reports/weekly` und `GET /api/v1/audit`. Sie verwenden dieselben Services wie MCP und Telegram.

Die Autorisierung plant getrennte App-, Telegram- und MCP-Clients sowie die Scopes `diet:read`, `diet:write`, `health:read`, `health:write`, `photos:read`, `photos:write` und `admin`. Tokens werden nur gehasht oder über einen externen Identity-Dienst geprüft, nie im Klartext persistiert. Der heutige G2-K1-Token besitzt diese Mehrclient-/Scope-Funktion ausdrücklich noch nicht.

## Paketzerlegung und Tokenprognose

| Gate | Ergebnis | Voraussetzung | P50 | P80 |
|---|---|---|---:|---:|
| G2-D0 | bewusste Entscheidung „Backend zentral“ vs. „App führend“, Datenklassifikation, ER-Modell und OpenAPI/MCP-Verträge | G2-K1 | 25k | 40k |
| G2-D1 | PostgreSQL, Migrationen, Service-Layer, Audit, Idempotenz, optimistic locking und deterministische Bilanzen | G2-D0 | 90k | 145k |
| G2-D2 | REST v1, getrennte Clients, Token-Hashes/Rotation, Scopes, Limits und Autorisierungstests | G2-D1 | 80k | 130k |
| G2-M1 | MCP Streamable HTTP mit den 19 geforderten Tools und sieben Resources auf derselben Serviceschicht | G2-D2 | 80k | 130k |
| G2-T1 | Telegram-Adapter, Outbox/Retry, Nachrichten- und Bildidempotenz sowie JSON-Migration | G2-M1 | 90k | 150k |
| G2-H2 | Health-Bridge-Upload/Queue/Dedupe und Home-Assistant-Connector | G2-D2 | 90k | 150k |
| G2-P1 | privater dauerhafter Fotospeicher, Metadaten, Zugriff, Löschung und Audit | G2-D2 | 55k | 90k |
| G2-E1 | Parallelitäts-, Wiederholungs-, Restore-, Security- und gemeinsamer App/Telegram/MCP-E2E-Test | G2-T1, G2-H2, G2-P1 | 70k | 115k |

Die Anleitung ist damit vollständig als Ziel-Backlog erfasst, aber noch nicht als ein einziges unkontrolliertes Paket gestartet. Vor jedem Gate gelten weiterhin GO, SPLIT, DEFER oder DROP und die 70-Prozent-Kapazitätsregel.

## Geforderte Testmatrix für die Folgepakete

Mindestens abzudecken sind doppelte Telegram- und Health-Nachrichten, Mahlzeitenkorrektur als Ersetzen, fehlender Home-Assistant-KFA, valides Home-Assistant-Gewicht, falsche/überlappende Aggregate, Tagesbilanz, 7-Tage-Gewichtsaverage, Foto-Upload, MCP-Authentifizierung, unberechtigtes Schreiben, Audit, parallele Updates und Wiederholung nach Netzfehler. Ein Test gilt nur als erfüllt, wenn sein tatsächlicher Lauf mit Commit und Workflow dokumentiert ist.
