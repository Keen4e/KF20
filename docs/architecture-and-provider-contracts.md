# Architektur und Provider-Verträge

## Systemgrenze

```text
Android-App -> HTTPS KF20 API -> KI-Provider
             -> künftige Konto-/Datenbankdienste
```

Der Android-Client enthält keine Provider-SDKs, API-Schlüssel oder providerspezifischen Antworttypen. Er kennt nur die KF20-API-Verträge.

## Android

- Kotlin und Jetpack Compose
- gemeinsame Domänenmodelle liegen in `Kf20Models.kt`; Android-unabhängige Refeed-, Tagesziel- und Navy-KFA-Berechnungen liegen in `DailyTargetLogic.kt` und werden mit JVM-Tests abgesichert
- `MainActivity.kt` enthält weiterhin den Compose-Zustand und die Bildschirmkomposition, greift aber nur noch über klar benannte Grenzen auf Infrastruktur zu
- `Kf20Storage.kt` kapselt lokale Speicherung, Verschlüsselung, Export und Gesamtlöschung
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
  "sources": [{ "title": "Quelle", "url": "https://..." }]
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

Der Health-Endpunkt meldet neben `status` die serverseitige Provider-ID und deren Capabilities. Er enthält keine Secrets, Modellprompts oder Nutzerdaten.

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
  }
}
```

## Provider-Grenze

Der Server verwendet bereits ein internes Interface:

```text
AiProvider.chat(messages, memories, webSearch) -> { text, sources }
AiProvider.analyzeNutrition(description, imageDataUrl) -> NutritionEstimate
```

Provider-Auswahl erfolgt ausschließlich über Serverkonfiguration (`AI_PROVIDER`, `AI_MODEL` und Secret Store). `server/src/providers/openai.js` implementiert den ersten Adapter. Ein neuer Provider wird unter `server/src/providers/` ergänzt und muss die stabilen KF20-Responses erzeugen. Der Android-Client wird dafür nicht geändert.

Web-Recherche ist eine optionale Capability. Ein Provider ohne Recherche muss einen klaren Capability-Fehler zurückgeben; er darf keine Quellen erfinden.

Providerfehler werden ohne Anfrageinhalte protokolliert und in stabile, generische KF20-Fehler übersetzt. Providerantworten für Nährwerte werden vor der Rückgabe nochmals gegen den KF20-Vertrag validiert.

## Aktuelle Sicherheitsgrenze

Der Prototyp verwendet einen einzelnen statischen Bearer-Token und ist nicht öffentlich betreibbar. Vor einem externen Test sind echte Nutzeridentität, Tokenrotation, Kontolöschung, Datenbanktrennung und HTTPS erforderlich. Details stehen in `security-release-gates.md`.

## Änderungsregel

Jede Änderung an Request-/Responsefeldern aktualisiert diese Datei, Android und Server atomar. Abwärtskompatible Ergänzungen sind zu bevorzugen.

