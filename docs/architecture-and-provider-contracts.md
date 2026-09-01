# Architektur und Provider-Verträge

## Systemgrenze

```text
Android-App -> HTTPS KF20 KI-Brücke -> serverseitiges Secret -> KI-Provider

Späteres, noch nicht freigegebenes Ziel:
Android / Telegram / MCP -> gemeinsame Services -> PostgreSQL / Fotospeicher
```

Der Android-Client enthält keine Provider-SDKs, API-Schlüssel oder providerspezifischen Antworttypen. Er kennt nur die KF20-API-Verträge. Das verbindliche Betriebs-, Sync- und Secret-Zielbild steht in `BACKEND_STRATEGY.md`.

## Android

- Kotlin und Jetpack Compose
- gemeinsame Domänenmodelle liegen in `Kf20Models.kt`; Android-unabhängige Refeed-, Tagesziel- und Navy-KFA-Berechnungen liegen in `DailyTargetLogic.kt` und werden mit JVM-Tests abgesichert
- `MainActivity.kt` enthält weiterhin den Compose-Zustand und die Bildschirmkomposition, greift aber nur noch über klar benannte Grenzen auf Infrastruktur zu
- `Kf20Storage.kt` kapselt lokale Speicherung, Verschlüsselung, Export und Gesamtlöschung
- `Kf20DataCodec.kt` ist die Android-unabhängig testbare JSON-Grenze für Tageslog, Messwerte und Gespräche. `Kf20Storage.kt` übernimmt nur verschlüsselte Persistenz und delegiert Schema-Lesen, -Schreiben und die idempotente Altchat-Migration an diesen Codec
- `Kf20Services.kt` kapselt Erinnerungsplanung und Android-Systemdienste
- `Kf20ApiClient.kt` kapselt die providerneutralen KF20-HTTP-Aufrufe für Chat und Nährwertanalyse
- lokale sensible Daten verschlüsselt über AES-GCM und Android Keystore
- Bildaufnahme als temporäre JPEG-Data-URL nur für eine Analyse
- freie Textbeschreibung direkt in der App; optionale Spracheingabe über Android Speech Recognizer wird lokal in denselben Texteingabekanal überführt
- benannte Gespräche werden als getrennte, AES-GCM-verschlüsselte lokale Verläufe gespeichert; die Volltextsuche erfolgt ausschließlich im bereits entschlüsselten In-Memory-Zustand der App
- der bisherige einzelne `messages`-Speicher wird beim ersten Lesen in einen `Hauptchat` migriert; an die Chat-API gehen weiterhin nur Nachrichten des aktuell geöffneten Gesprächs
- Nährwertwerte bleiben nach der KI-Antwort editierbar
- die KI-Antwort bildet eine Basisportion; `FoodPortionLogic.kt` skaliert die vier Kernwerte nur aus bestätigter Menge und, wo erforderlich, Basisgewicht/Gramm je Einheit. Portionsmetadaten werden abwärtskompatibel mit jedem Tageslog gespeichert
- lokaler, nutzerinitiierter JSON-Export schließt Server-Token aus; die Datei selbst ist unverschlüsselt und wird nur an einen vom Nutzer gewählten Android-Speicherort geschrieben
- nicht-sensitive UI-Präferenzen wie der Styleguide liegen im selben lokalen Preference-Lebenszyklus, werden sofort angewendet und im JSON-Export unter `uiPreferences` ausgegeben; Schema 4 exportiert zusätzlich alle benannten Gespräche, die aktive Gesprächs-ID und Portionsmetadaten der Nahrung
- bestätigte lokale Gesamtlöschung entfernt verschlüsselte Preferences, persistierte URI-Freigaben, Erinnerungsalarm und Android-Keystore-Schlüssel
- JVM-Tests prüfen aktuelle und alte JSON-Schemata sowie Exportverträge; Android-Instrumentierungstests prüfen den echten Keystore-/AES-GCM-Pfad und Compose-Kernflüsse. Beide GitHub-Workflows führen diese Instrumentierung auf einem Android-Emulator aus

## Stabile KF20-API

### `POST /v1/chat`

Request:

```json
{
  "messages": [{ "role": "user", "content": "..." }],
  "memories": ["vom Nutzer bestätigte Erinnerung"],
  "webSearch": false
}
```

Response:

```json
{
  "text": "Antwort",
  "sources": [{ "title": "Quelle", "url": "https://..." }],
  "execution": { "provider": "openai", "credentialMode": "managed", "storage": "none" }
}
```

### `POST /v1/nutrition/analyze`

Request:

```json
{
  "description": "freie Textbeschreibung",
  "imageDataUrl": "data:image/jpeg;base64,..."
}
```

### `GET /healthz`

Der Health-Endpunkt meldet neben `status` die serverseitige Provider-ID, deren Capabilities, `mode: stateless-ai-bridge` und `storage: none`. Er enthält keine Secrets, Modellprompts oder Nutzerdaten.

Mindestens Beschreibung oder Bild ist erforderlich. Response:

```json
{
  "estimate": {
    "name": "Mahlzeit",
    "calories": 500,
    "protein": 35,
    "fat": 18,
    "carbs": 48,
    "confidence": "mittel",
    "note": "Portionsannahme ..."
  },
  "execution": { "provider": "openai", "credentialMode": "managed", "storage": "none" }
}
```

## Provider-Grenze

Der Server verwendet bereits ein internes Interface:

```text
AiProvider.chat(messages, memories, webSearch) -> { text, sources }
AiProvider.analyzeNutrition(description, imageDataUrl) -> NutritionEstimate
```

Im aktuellen Einzeltester-Prototyp erfolgt die Provider-Auswahl über Serverkonfiguration (`AI_PROVIDER`, `AI_MODEL` und Host-Secret). `server/src/providers/openai.js` implementiert den ersten Adapter. Die API gibt bereits die stabilen Metadaten `provider`, `credentialMode` und `storage` zurück. Eine nutzerwählbare Capability-Registry und BYOK bleiben ein späteres Paket.

Ein neuer Provider wird unter `server/src/providers/` ergänzt und muss die stabilen KF20-Responses erzeugen. Vorgesehen sind OpenAI direkt, Anthropic direkt und OpenRouter als getrennte Datenwege. Android erhält weder Provider-SDKs noch Secrets; UI-Erweiterungen zeigen lediglich die serverseitig angebotenen Modi und Fähigkeiten.

Web-Recherche ist eine optionale Capability. Ein Provider ohne Recherche muss einen klaren Capability-Fehler zurückgeben; er darf keine Quellen erfinden.

Providerfehler werden ohne Anfrageinhalte protokolliert und in stabile, generische KF20-Fehler übersetzt. Providerantworten für Nährwerte werden vor der Rückgabe nochmals gegen den KF20-Vertrag validiert.

Kein Adapter darf still von BYOK auf einen KF20-Schlüssel, zwischen direktem Provider und OpenRouter oder auf einen anderen Provider wechseln. Provider und Zugangsart werden als nicht-sensitives Ausführungsmetadatum zurückgegeben; Modellnamen werden aktuell bewusst nicht an den Client übertragen.

## Sync- und Secret-Grenze

- Aktuelle private Alpha: nur die containerisierte, zustandslose KF20-KI-Brücke auf dem Homeserver, ausschließlich über Cloudflare Tunnel erreichbar. Keine Gesundheitsdatenbank und kein Sync sind aktiviert.
- Ein zentrales Backend oder optionaler Sync ist nach D-020 zurückgestellt und bleibt bis zu einem neuen ausdrücklichen Backend-GO Architekturentwurf.
- Die lokale verschlüsselte Datenbank bleibt bei Offline-Nutzung führend; Cloud-Sync ist opt-in.
- Synchronisierte Fachobjekte verwenden stabile IDs, Revisionen, Zeitstempel und Löschmarker. Nicht sicher zusammenführbare Konflikte werden sichtbar statt still überschrieben.
- Nutzeridentität wird serverseitig aus der Session abgeleitet. Eine Client-Nutzer-ID erteilt keine Berechtigung.
- Gesundheitsdatenbank, KI-Anfrageinhalte und Provider-Secrets besitzen getrennte Speicher- und Löschpfade.
- Fortschrittsbilder sind nicht Teil der ersten Sync-Stufe. KI-Bilder werden nach der Anfrage verworfen.
- BYOK-Secrets werden verschlüsselt im Vault gehalten, nie über Lese-APIs zurückgegeben und von Exporten, Logs, Analytics und normalen Datenbank-Backups ausgeschlossen.

## Aktuelle Sicherheitsgrenze

Die private Alpha verwendet einen einzelnen statischen Bearer-Token hinter HTTPS/Cloudflare Tunnel und ist nur für den Einzeltest vorgesehen. Vor einem externen Test sind echte Nutzeridentität, getrennte Clients/Scopes, Tokenrotation, Kontolöschung und Autorisierung erforderlich. Details stehen in `security-release-gates.md`.

## Änderungsregel

Jede Änderung an Request-/Responsefeldern aktualisiert diese Datei, Android und Server atomar. Abwärtskompatible Ergänzungen sind zu bevorzugen.

