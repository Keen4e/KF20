# Sicherheits- und Release-Gates

Die aktuelle Codebasis ist **nicht** bereit für die öffentliche Veröffentlichung. Diese Punkte sind vor einem externen Test zwingend zu erledigen:

1. **Authentifizierung:** Der Server braucht eine echte Nutzeranmeldung sowie Zugriffskontrolle. CORS ist keine Zugangskontrolle für eine Android-App.
2. **Transport:** Ausschließlich HTTPS, mit TLS-Konfiguration und einer produktiven Domain.
3. **Geheimnisse:** Provider-API-Keys nur im Secret Store des Hosts; niemals in Git, APK, Build-Logs oder Supporttickets.
4. **Missbrauchsschutz:** Rate Limits pro Nutzer, Größenlimits, Zeitlimits und Monitoring ohne Chat-Inhalte.
5. **Datenspeicherung:** Lokaler Export und vollständige lokale Löschung sind implementiert. Offen bleiben definierte serverseitige Aufbewahrungsfristen, Kontolöschung sowie ein produktives Backup-/Restore-Konzept.
6. **Client-Speicher:** Gesundheitsdaten, Chatverlauf und Test-Token werden mit einem AES-GCM-Schlüssel aus dem Android Keystore verschlüsselt gespeichert. Die bestätigte lokale Löschung entfernt Daten, URI-Freigaben und Schlüssel. Ein künftiger Auth-Token muss denselben Schutz erhalten.
7. **Abhängigkeiten:** Android- und Server-Abhängigkeiten locken, prüfen und vor Release aktualisieren.
8. **Tests:** API- und UI-Tests, manueller Test auf realem Gerät sowie Play-Pre-Launch-Report.

## Erfüllte private G2-K1-Grenzen

- Provider-Schlüssel und Tunnel-Token sind nur als nicht versionierte Host-Umgebungswerte vorgesehen; die APK enthält sie nicht.
- Die Brücke besitzt keine Datenbank oder Uploadablage, setzt `Cache-Control: no-store`, vergibt Request-IDs und protokolliert bei Fehlern nur Anfrage-ID, Provider-ID und stabilen Fehlercode.
- Text-, Chat- und Bildgrößen sind begrenzt; Bilder werden anhand der tatsächlich decodierten Binärgröße geprüft. OpenAI-Anfragen setzen `store: false`.
- Das Container-Dateisystem ist read-only, der Prozess läuft ohne Root und der normale Compose-Betrieb veröffentlicht keinen Host-Port.

Diese Grenzen machen die Brücke nicht zu einem öffentlichen Mehrnutzer-Backend. Getrennte Clients, Scopes, OAuth/Tokenrotation, Audit und zentrale Gesundheitsdaten benötigen die G2-D-Pakete.

