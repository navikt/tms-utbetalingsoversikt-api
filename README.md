# dittnav-ktor-template

Kan brukes som utgangspunkt for å opprette nye Ktor-apper for Team DittNAV.

# Tilpasse repo-et
1. Søk etter og erstatt `dittnav-ktor-template` med det som skal være navnet på den nye appen.
2. Skift navnet på mappen `src/main/kotlin/no/nav/personbruker/template` til noe som passer for den nye appen. NB! unngå bidestreker i navnet.
3. Oppdatert pakkenavnene, søk etter og erstatt `personbruker.teamplate.api` med `personbruker.<mappenavnet fra steg to>.api`.
4. Verifiser at appen kan starte, som beskrevet i #kom

# Kom i gang
1. Bygg dittnav-ktor-template ved å kjøre `gradle build`
1. Start appens avhengigheter ved å kjøre `docker-compose up -d`
1. Start appen lokalt ved å kjøre `gradle runServer`
1. Appen nås på `http://localhost:8101/person/dittnav-ktor-template`
   * F.eks. via `curl http://localhost:8101/person/dittnav-ktor-template/internal/isAlive`

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på github.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-personbruker.
