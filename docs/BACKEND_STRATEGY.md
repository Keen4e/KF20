# KF20 Backend-, Sync- und KI-Provider-Strategie

Stand: 2026-08-31  
PI-Paket: G2-S0  
Status: ACCEPTED

## Zielbild

KF20 bleibt local-first: Die Android-App funktioniert für Erfassung, Verlauf und Auswertung auch ohne Cloud. Ein Backend ergänzt Anmeldung, verschlüsselten Mehrgeräte-Sync und KI-Aufrufe, übernimmt aber nicht still die Hoheit über die Gesundheitsdaten.

Aktueller Umsetzungsstand G2-K1/D-020: Konto, Fach-Datenbank und Sync sind geparkt; die App bleibt führende Datenquelle. Implementiert ist nur eine zustandslose KI-Brücke ohne Serverspeicherung. Die spätere Nutzeranforderung eines zentralen Backends für App, Telegram und MCP ist in `MCP_BACKEND_GAP_ANALYSIS.md` erfasst und benötigt vor Umsetzung ein neues ausdrückliches Backend-GO.

```text
Android-App
  |-- lokale verschlüsselte KF20-Datenbank (führend bei Offline-Nutzung)
  |-- HTTPS + Nutzer-Session
        |
        v
KF20 Backend
  |-- Identity und Sync API
  |-- Postgres mit strikter Nutzertrennung
  |-- KI-Gateway und Provider-Registry
  |-- Secret Vault / KMS
        |-- KF20-verwalteter Schlüssel
        |-- verschlüsselter Benutzerschlüssel (BYOK)
        v
  OpenAI direkt | Anthropic direkt | OpenRouter
```

Das Produkt trennt drei Dinge ausdrücklich:

1. **Gesundheitsdaten**: lokal führend und optional synchronisiert.
2. **KI-Inhalte**: nur für die konkrete Anfrage an den gewählten Provider übertragen.
3. **Zugangsschlüssel**: ausschließlich im Secret Vault; niemals Bestandteil der Gesundheitsdaten oder ihrer Exporte.

## Verbindliche Entscheidungen

### Local-first und optionaler Sync

- Erfassung und Statistik bleiben ohne Konto und ohne Netz nutzbar.
- Cloud-Sync ist opt-in. Die App erklärt vor der Aktivierung, welche Daten übertragen werden.
- Das Servermodell verwendet stabile Objekt-IDs, `updatedAt`, Löschmarker und eine serverseitige Revision. Dadurch sind Offline-Änderungen und nachvollziehbare Konfliktbehandlung möglich.
- Fachliche Einträge werden nicht durch eine bloße Reihenfolge „letzter Schreibzugriff gewinnt“ unbemerkt überschrieben. Nicht automatisch zusammenführbare Änderungen werden als Konflikt sichtbar gemacht.
- Bilder werden getrennt behandelt: Fortschrittsbilder bleiben zunächst lokal. Ein späterer Bild-Sync benötigt ein eigenes GO, verschlüsselten Objektspeicher, Aufbewahrungsregel und Löschtest.
- KI-Fotos sind temporäre Anfrageinhalte und werden weder als Sync-Objekt noch als Provider-Log gespeichert.

### Datenbank und Identität

- Ziel ist PostgreSQL mit zeilenweiser Nutzertrennung. Supabase in der **festen Region `eu-central-1` (Frankfurt)** ist die bevorzugte erste Betriebsoption, sofern ein technischer Spike Authentifizierung, Export, Löschung und Row-Level Security bestätigt. Eine unspezifische Regionen-Gruppe wird nicht verwendet.
- Die Architektur darf keinen Supabase-spezifischen Typ in Android-Fachmodelle einführen. Identity-, Sync- und Storage-Zugriffe liegen hinter KF20-eigenen Schnittstellen.
- Jede synchronisierte Zeile besitzt eine unveränderliche Nutzerzuordnung. Serverseitige Autorisierung wird für jeden Zugriff erzwungen; eine vom Client gesendete Benutzer-ID ist kein Berechtigungsnachweis.
- Kontoexport und vollständige serverseitige Kontolöschung sind vor einer externen Beta verpflichtend.

### Konkretes Hosting-Ziel

- Für die **private Alpha** ist der vorhandene Homeserver die bevorzugte erste Laufzeit: containerisierte Node-KF20-API und PostgreSQL in einem internen Docker-Netz, veröffentlicht ausschließlich über Cloudflare Tunnel. Es werden keine eingehenden Routerports für API, SSH oder PostgreSQL geöffnet.
- Cloudflare Access kann als zusätzliche äußere Schutzschicht dienen, ersetzt aber weder KF20-Nutzeridentität noch serverseitige Autorisierung. Ein gemeinsames Cloudflare-Service-Token wird nicht in der APK eingebettet.
- Der Homeserver benötigt automatische verschlüsselte Offsite-Backups, Wiederherstellungstest, Betriebssystem-/Containerupdates, minimale Firewall, Monitoring und eine dokumentierte Secret-Ablage. Strom- und Internet-Ausfall bleiben für die private Alpha akzeptierte Verfügbarkeitsrisiken.
- Für eine **externe Beta/Produktion** läuft die containerisierte Node-KF20-API einschließlich KI-Gateway bevorzugt auf **Google Cloud Run `europe-west3` (Frankfurt)**.
- Supabase Auth/PostgreSQL läuft im getrennten Supabase-Projekt in **`eu-central-1` (Frankfurt)**. Android greift für Fachdaten nicht direkt auf Tabellen zu, sondern über die KF20-Sync-API; Row-Level Security bleibt eine zweite Schutzlinie.
- Betreiber-Secrets liegen im Google Secret Manager. Der Cloud-Run-Dienst verwendet eine eigene Service-Identität mit minimalen Leserechten.
- BYOK-Werte werden als benutzerspezifisch verschlüsselter Geheimtext in einem getrennten Credential-Speicher gehalten. Der Schlüssel zur Envelope-Verschlüsselung liegt in Google Cloud KMS/Secret Manager und ist nur für das KI-Gateway zugänglich.
- Entwicklung und Produktion verwenden getrennte Google-Cloud- und Supabase-Projekte, Datenbanken, Service-Identitäten, Secrets und Budgets.
- GitHub Actions darf nach manueller Umgebungsfreigabe ein unveränderliches Container-Image deployen. Langfristige Cloud-Zugangsschlüssel werden nicht als Repository-Secret verwendet; vorgesehen ist kurzlebige Workload-Identity-Föderation.
- Eine eigene HTTPS-Domain wird erst in G2-A1 festgelegt. Keine konkrete Domain oder Serveradresse wird vor ihrer Bereitstellung in App oder Dokumentation behauptet.

Diese Festlegung bestimmt die gestufte Betriebsstrategie, nicht die Domänengrenzen. Der Server bleibt containerisiert und Datenbankschema/Migrationen werden versioniert, damit die private Alpha vom Homeserver später ohne Änderung der Android-Fachmodelle zu Cloud Run/Supabase wechseln kann.

### Vorhandener IONOS-Webspace

- Klassisches IONOS Shared Webhosting ist nicht das primäre Ziel für das dauerhaft laufende Node-KI-Gateway: Der SSH-Zugang ist eingeschränkt, systemweite Software kann nicht frei installiert werden und das Standardangebot ist auf Webdateien, PHP und MySQL/MariaDB ausgerichtet.
- Der vorhandene Webspace kann sinnvoll Domain/DNS, Landingpage, Datenschutzerklärung und Supportseiten tragen. Eine spätere API-Subdomain kann auf Cloud Run zeigen.
- Handelt es sich beim vorhandenen Vertrag tatsächlich um einen IONOS VPS oder Cloud Server mit administrativem Zugriff, wird er in G2-A1 als Container-Hosting-Alternative geprüft. Selbstbetrieb erfordert zusätzlich Patchmanagement, Firewall, Backups, Monitoring und Secret-Betrieb.
- Ohne positiven VPS-/Cloud-Server-Nachweis bleibt Cloud Run die bevorzugte API-Plattform. Es werden im Strategiegate keine IONOS-Zugangsdaten erhoben oder gespeichert.

## KI-Zugangsarten

KF20 bietet drei explizite Modi. Der aktuell aktive Modus und Provider sind in den Einstellungen und vor sensiblen Bildübertragungen sichtbar.

### 1. KF20 verwaltet

- KF20 verwendet einen serverseitigen Projekt- oder Service-Schlüssel des Betreibers.
- Der Schlüssel liegt ausschließlich im Secret Vault/KMS und wird nur im KI-Gateway zur Laufzeit aufgelöst.
- Nutzer sehen und erhalten den Schlüssel nie.
- Vor öffentlicher Nutzung gelten harte Limits pro Nutzer, Modell-Allowlist, globales Kostenlimit, Alarmierung, Rotation und eine Sperrmöglichkeit.
- Der Betreiber trägt die Providerkosten. Ein unbegrenzter gemeinsam genutzter persönlicher Schlüssel ist für eine öffentliche Beta nicht zulässig.

Der erste private Test darf einen zentralen OpenAI-Projektschlüssel nutzen. Andere zentral verwaltete Provider werden über dieselbe Registry möglich, aber jeweils separat freigegeben.

### 2. Eigener Schlüssel (BYOK)

- Der Nutzer wählt den direkten Provider `openai`, `anthropic` oder den Vermittler `openrouter` und hinterlegt einen eigenen Schlüssel.
- Direkter Anthropic-Zugang und OpenRouter sind unterschiedliche Datenwege. KF20 leitet einen Anthropic-Schlüssel nicht still über OpenRouter.
- Der Schlüssel wird transportverschlüsselt an einen dedizierten Credential-Endpunkt gesendet, mit einem benutzerspezifischen Schlüssel verschlüsselt und im Secret Vault gespeichert.
- Nur das KI-Gateway darf den Schlüssel für genau den gewählten Nutzer und Provider entschlüsseln. Datenbankadministratoren und Support-Oberflächen erhalten keinen Klartextzugriff.
- Der Klartextschlüssel erscheint weder in Logs, Analytics, Crashreports, Datenexporten, Backups noch in API-Antworten. Nach dem Speichern zeigt die App nur Status, Provider, optional die letzten vier Zeichen sowie Erstellungs-/Prüfdatum.
- Funktionen: Verbindung testen, ersetzen, widerrufen/löschen. Löschen muss Secret, Metadaten und Cache entfernen und wird automatisiert geprüft.
- Für OpenRouter wird OAuth mit PKCE bevorzugt, sobald der Betriebsmodus dies erlaubt. Manuelle Schlüsseleingabe bleibt ein klar gekennzeichneter Fallback.
- Der Benutzer trägt die Providerkosten und akzeptiert die Bedingungen des gewählten Providers. BYOK verhindert nicht, dass Anfrageinhalte an diesen Provider übertragen werden.

### 3. ChatGPT-Plus-Begleitmodus

- Eine ChatGPT-Plus-Subscription ist keine API-Berechtigung und wird nicht als automatischer Provider-Schlüssel behandelt.
- Der Begleitmodus darf Inhalte kontrolliert zum manuellen Verwenden in ChatGPT vorbereiten und Ergebnisse nach Nutzerprüfung übernehmen.
- Er ist nicht für automatische Hintergrundanalyse, nahtlosen Foto-Upload oder unbeaufsichtigte Agentenabläufe geeignet.
- KF20 fordert niemals ChatGPT-Passwort, Session-Cookie oder Browser-Token an.

## Providerneutrales KI-Gateway

Android spricht weiterhin ausschließlich mit stabilen KF20-Endpunkten. Provideradapter normalisieren Ein- und Ausgabe:

```text
AiProvider.chat(input) -> ChatResult
AiProvider.analyzeNutrition(input) -> NutritionEstimate
```

Die Provider-Registry führt mindestens:

- stabile Provider-ID und Anzeigename
- direkte Providerverbindung oder Vermittler
- Fähigkeiten: Text, Bildanalyse, strukturiertes Ergebnis, Web-Recherche
- zugelassene Modelle je Betriebsmodus
- Anfrage-, Bildgrößen- und Zeitlimits
- Datenregion/Aufbewahrung, soweit konfigurierbar

Die Auswahl besteht aus `credentialMode`, `providerId` und `modelId`. Sie wird serverseitig gegen die Registry geprüft. Provider, Modell und Zugangsart werden als nicht-sensitives Ausführungsmetadatum an die App zurückgegeben und mit einer bestätigten KI-Schätzung gespeichert.

### Routing- und Fallback-Regeln

- Kein stiller Wechsel von BYOK zu KF20-verwaltet oder umgekehrt.
- Kein stiller Wechsel zwischen direktem Provider und OpenRouter.
- Fehlt eine Fähigkeit, antwortet KF20 mit einem stabilen Capability-Fehler.
- Ein Fallback ist nur nach ausdrücklicher Konfiguration durch den Nutzer zulässig und zeigt vorab möglichen Kosten- und Datenwegwechsel.
- Providerfehler enthalten für den Client eine stabile Fehlerklasse, aber keine Schlüssel, Prompts oder Rohantworten.

## Nutzeroberfläche

Unter **Einstellungen → KI-Zugang** werden angeboten:

- Modus: `KF20 verwaltet`, `Eigener Schlüssel`, `Plus-Begleitmodus`
- Provider und Modell aus der serverseitigen Capability-Liste
- Status: verbunden/nicht verbunden, zuletzt geprüft, Schlüssel ersetzen oder löschen
- Verbindung testen, ohne Gesundheits- oder Chatdaten zu übertragen
- Kostenhinweis und optionales Nutzerbudget; im verwalteten Modus verbleibendes KF20-Kontingent
- verständlicher Hinweis zum konkreten Datenweg vor der ersten Text- oder Bildanalyse

Die App zeigt niemals ein bereits gespeichertes Secret im Klartext. Export und Gesamtlöschung behandeln Zugangsdaten getrennt: Der Datenexport schließt sie aus; Kontolöschung entfernt sie dennoch vollständig.

## Datenschutz, Betrieb und Beobachtbarkeit

- TLS ist für alle nicht-lokalen Verbindungen Pflicht.
- Logs enthalten Request-ID, Nutzer-Pseudonym, Provider-ID, Modell-ID, Dauer, Statusklasse und Token-/Kostenmetrik, aber keine Chattexte, Bilder, Gesundheitswerte oder Secrets.
- Prompts, Providerantworten und Bilder werden standardmäßig nicht dauerhaft serverseitig gespeichert.
- Rate Limits und Budgets gelten pro Nutzer und zusätzlich global. Kostenüberschreitungen stoppen vorhersehbar mit einer sichtbaren Meldung.
- Provider-Datenschutz und Aufbewahrung werden in der App verlinkt; Wechsel des Providers ist eine bewusste Nutzeraktion.
- Schlüsselrotation, Widerruf, Backup-Ausschluss, Secret-Löschung und Vorfallsreaktion werden vor externer Beta getestet.

## Umsetzungspakete nach diesem Strategiegate

G2-S0 entscheidet nur das Zielbild. Kein nachfolgendes Paket ist dadurch automatisch freigegeben.

| Paket | Inhalt | Abhängigkeit | P50 | P80 |
|---|---|---|---:|---:|
| G2-A1 | Private Backend-Basis: Homeserver/Docker über Cloudflare Tunnel, HTTPS, Healthcheck, Nutzeridentität, Backup-/Restore-Test und portables Deployment | G2-S0 | 55k | 90k |
| G2-A2 | KI-Gateway v1: Provider-Registry, zentraler OpenAI-Projektschlüssel, Limits und Ausführungsmetadaten | G2-A1 | 55k | 90k |
| G2-A3 | BYOK: verschlüsselter Credential-Vault, OpenAI-/Anthropic-/OpenRouter-Adapter, Test/Replace/Delete und kein stiller Fallback | G2-A2 | 75k | 120k |
| G2-B1 | Local-first Sync: Konto, Datenschema, Delta-Sync, Konflikte, Export und vollständige Kontolöschung; zunächst ohne Bilder | G2-A1 | 110k | 175k |
| G3-A1 | KI-E2E-Abnahme: Text, Foto, Mikrofon und Chat mit Fehler-, Kosten- und Providerwechseltests auf Realgerät | G2-A3 | 70k | 115k |
| G2-B2 | Optionaler verschlüsselter Fortschrittsbild-Sync mit Aufbewahrung und Löschtest | G2-B1 | 55k | 90k |

## Abnahme von G2-S0

- Managed, BYOK und Plus-Begleitmodus sind eindeutig getrennt.
- OpenAI direkt, Anthropic direkt und OpenRouter sind als getrennte Providerwege vorgesehen.
- Private Alpha auf Homeserver/Cloudflare Tunnel und späteres externes Hosting auf Cloud Run/Supabase sind als portable Stufen getrennt.
- Secrets, Gesundheitsdaten, KI-Anfrageinhalte und synchronisierte Bilder besitzen getrennte Lebenszyklen.
- Local-first, Konfliktbehandlung, Export und Löschung sind verbindlich beschrieben.
- Folgepakete besitzen klare Grenzen, Abhängigkeiten und P50/P80-Prognosen.
- Kein Anbieter, Konto, Schlüssel oder Cloud-Dienst wurde durch dieses Planungspaket aktiviert.
