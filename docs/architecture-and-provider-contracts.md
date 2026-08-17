# Architektur und Provider-Verträge

## Systemgrenze

```text
Android-App -> HTTPS KF20 API -> KI-Provider
             -> künftige Konto-/Datenbankdienste
```

Der Android-Client enthält keine Provider-SDKs, API-Schlüssel oder providerspezifischen Antworttypen. Er kennt nur die KF20-API-Verträge.

## Android

- Kotlin und Jetpack Compose
- lokale sensible Daten verschlüsselt über AES-GCM und Android Keystore
- Bildaufnahme als temporäre JPEG-Data-URL nur für eine Analyse
- freie Textbeschreibung direkt in der App; optionale Spracheingabe über Android Speech Recognizer wird lokal in denselben Texteingabekanal überführt
- Nährwertwerte bleiben nach der KI-Antwort editierbar

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

Der aktuelle Server ruft OpenAI Responses auf. Die nächste Server-Refaktorierung führt ein internes Interface ein:

```text
AiProvider.chat(messages, memories, webSearch) -> { text, sources }
AiProvider.analyzeNutrition(description, imageDataUrl) -> NutritionEstimate
```

Provider-Auswahl erfolgt ausschließlich über Serverkonfiguration (`AI_PROVIDER`, Modellname und Secret Store). Ein neuer Provider muss die stabilen KF20-Responses erzeugen. Der Android-Client wird dafür nicht geändert.

Web-Recherche ist eine optionale Capability. Ein Provider ohne Recherche muss einen klaren Capability-Fehler zurückgeben; er darf keine Quellen erfinden.

## Aktuelle Sicherheitsgrenze

Der Prototyp verwendet einen einzelnen statischen Bearer-Token und ist nicht öffentlich betreibbar. Vor einem externen Test sind echte Nutzeridentität, Tokenrotation, Kontolöschung, Datenbanktrennung und HTTPS erforderlich. Details stehen in `security-release-gates.md`.

## Änderungsregel

Jede Änderung an Request-/Responsefeldern aktualisiert diese Datei, Android und Server atomar. Abwärtskompatible Ergänzungen sind zu bevorzugen.

