# Sicherheits- und Release-Gates

Die aktuelle Codebasis ist **nicht** bereit für die öffentliche Veröffentlichung. Diese Punkte sind vor einem externen Test zwingend zu erledigen:

1. **Authentifizierung:** Der Server braucht eine echte Nutzeranmeldung sowie Zugriffskontrolle. CORS ist keine Zugangskontrolle für eine Android-App.
2. **Transport:** Ausschließlich HTTPS, mit TLS-Konfiguration und einer produktiven Domain.
3. **Geheimnisse:** `OPENAI_API_KEY` nur im Secret Store des Hosts; niemals in Git, APK, Build-Logs oder Supporttickets.
4. **Missbrauchsschutz:** Rate Limits pro Nutzer, Größenlimits, Zeitlimits und Monitoring ohne Chat-Inhalte.
5. **Datenspeicherung:** Definierte Aufbewahrungsfristen, Kontolöschung und ein Backup-/Restore-Konzept.
6. **Client-Speicher:** Chatverlauf und Auth-Token verschlüsselt per Android Keystore/EncryptedSharedPreferences speichern.
7. **Abhängigkeiten:** Android- und Server-Abhängigkeiten locken, prüfen und vor Release aktualisieren.
8. **Tests:** API- und UI-Tests, manueller Test auf realem Gerät sowie Play-Pre-Launch-Report.

