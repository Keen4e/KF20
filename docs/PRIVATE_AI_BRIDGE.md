# Private zustandslose KI-Brücke

Stand: 2026-09-01 · Paket G2-K1

## Zweck und Grenze

Die Brücke verarbeitet ausschließlich eine gerade angeforderte Chat- oder Nährwertanalyse. Sie besitzt keine Datenbank, keine Benutzerkonten und keinen Sync. Text, freigegebene Erinnerungen und ein optionales Essensfoto liegen nur während der Anfrage im Arbeitsspeicher und werden von KF20 nicht in Dateien oder Logs geschrieben. Die App speichert das vom Nutzer bestätigte Ergebnis lokal; das übertragene Foto wird nicht in das Tageslog übernommen.

Der konfigurierte KI-Anbieter verarbeitet die Anfrage nach seinen eigenen API-Bedingungen. Der OpenAI-Adapter setzt bei jeder Responses-API-Anfrage `store: false`. Das ist keine Zusage über technisch notwendige, vom Anbieter definierte Sicherheits- oder Missbrauchsprotokolle.

## Homeserver vorbereiten

Voraussetzungen sind Docker mit Compose, ein ausschließlich für KF20 verwendeter OpenAI-Projektschlüssel und ein Cloudflare-Tunnel-Token. ChatGPT Plus enthält keinen API-Schlüssel und ersetzt die nutzungsabhängige API-Abrechnung nicht.

1. Repository auf dem Homeserver auschecken und auf `codex/kf20-rebuild` wechseln.
2. `server/.env.example` nach `server/.env` und `deploy/.env.example` nach `deploy/.env` kopieren.
3. In `server/.env` einen langen zufälligen `KF20_API_TOKEN` und den OpenAI-Projektschlüssel setzen. In `deploy/.env` nur den Tunnel-Token setzen. Beide Dateien auf den Dienstbenutzer beschränken und niemals committen, chatten oder in Screenshots zeigen.
4. Im Cloudflare-Dashboard einen Tunnel-Hostname auf `http://kf20-api:8787` routen. Es wird kein Router-Port geöffnet.
5. Im Verzeichnis `deploy/` ausführen:

```sh
docker compose config
docker compose up -d --build
docker compose ps
```

Die API und der Tunnel teilen ein eigenes Docker-Netz; die API benötigt ausgehenden HTTPS-Zugriff zum KI-Anbieter. Es gibt im normalen Betrieb kein veröffentlichtes Host-Port-Mapping. Für einen Test direkt auf dem Homeserver kann stattdessen ohne Tunnel gestartet werden:

```sh
docker compose -f compose.yaml -f compose.local.yaml up -d --build kf20-api
curl http://127.0.0.1:8787/healthz
```

Eine bereite Brücke meldet `mode: stateless-ai-bridge` und `storage: none`. Der Healthcheck enthält keine Schlüssel, Prompts oder Nutzerdaten.

## Android verbinden und prüfen

Unter **Einstellungen → Serververbindung** werden die HTTPS-Adresse und der separate KF20-Zugangstoken eingetragen. **Verbindung testen** muss Provider und „keine KF20-Serverspeicherung“ anzeigen. Der OpenAI-Schlüssel wird niemals in die APK oder die App-Einstellungen kopiert.

## Betrieb

- Logs dürfen nur Anfrage-ID und stabile Fehlercodes enthalten, niemals Text, Bild, Providerantwort oder Token.
- `server/.env` und `deploy/.env` gehören nicht in normale App-Datensicherungen. Sicherungen der Hostkonfiguration müssen separat verschlüsselt werden.
- Bei Verdacht auf Offenlegung zuerst den betroffenen Provider- oder Tunnel-Schlüssel widerrufen, dann den `KF20_API_TOKEN` ersetzen und die App neu verbinden.
- Vor Updates `docker compose pull` und `docker compose up -d --build` ausführen; danach Healthcheck, Text- und Foto-Test durchführen.
- Diese gemeinsame Alpha-Zugangskontrolle ist nur für den privaten Einzeltest bestimmt. Vor externen Testern sind Nutzeridentität, individuelle Autorisierung und Missbrauchsmonitoring ein eigenes GO-Paket.

## Nicht Bestandteil von G2-K1

Konten, Gesundheitsdatenbank, Cloud-Sync, BYOK-Schlüssel anderer Benutzer, öffentliches Hosting sowie ein Hermes-/MCP-Schreibkanal sind nicht aktiviert. Hermes ist als separates Gate G2-M1 geplant, damit kein Agent ungeprüft lokale Gesundheitsdaten verändern kann.
