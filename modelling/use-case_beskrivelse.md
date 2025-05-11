## Use-case 1 – Ser kartskjerm med de ulike menyvalgene:
- Navn: Se kartskjerm med valgt informasjonslag 
- Aktør: Hobbyfisker
- Prebetingelser: Bruker har dekning og er koblet til nettverk (Wi-Fi / 4G)
- Postbetingelser: Brukeren ser kartet med ønsket informasjon aktivert.

#### Hovedflyt:
1.	Bruker åpner appen og klikker på «Kart» fra startskjermen
2.	Kartet vises uten aktive lag
3.	Brukeren trykker på «kartlag»-knappen.
4.	Brukeren velger et informasjonslag, f.eks. båttrafikk.
5.	Appen henter data fra relevant API og viser det på kartet.
6.	Brukeren kan også aktivere flere lag som farevarsler, vind, bølger osv.
7.	Kartet oppdateres dynamisk etter hvert valg.

#### Alternativ flyt - varsel om at man befinner seg i en faresone:
1.	Appen sjekker kontinuerlig brukerens nåværende posisjon opp mot farevarslene.
2.	Dersom posisjonen faller innenfor et farevarselområde, vises et popup-varsel: «Du befinner deg i en faresone!».
3.	Brukeren kan deretter velge å ignorere eller få mer informasjon om farevarselet.
4.	Dersom brukeren beveger seg ut av faresonen, fjernes popup-varslet automatisk


## Use-case 2 – Bruke SOS-knappen i nødstilfeller:
- Navn: Bruke SOS-knapp for å få hjelp eller se nærmeste fartøy
- Aktør: Hobbyfisker, andre fiskere
- Prebetingelser: Bruker har dekning og GPS aktivert for brukeren. Appen har tillatelse til å bruke posisjonstjenester.
- Postbetingelser: Brukeren får vist nærmeste fartøy, vedkommende nåværende posisjon og nødnumre. Brukeren kan ringe nødnumre direkte fra appen

#### Hovedflyt:
1.	Brukeren åpner appen og trykker på SOS-knappen.
2.	Appen henter nåværende posisjon.
3.	Det sendes forespørsel til barentswatch-API og henter informasjon om nærmeste fartøy.
4.	Appen viser en liste over nærmeste fartøy, inkludert navn, type og avstand.
5.	Hvert fartøy i listen har en knapp «Vis på kart», som åpner kartvisning med fartøy og brukerposisjon.
6.	Appen viser også en liste over nødnumre (f.eks. Redningsselskapet, Politi, Brannvesen, Ambulanse).
7.	Brukeren kan trykke på et nødnummer for å åpne telefonappen og ringe.

#### Alternativ flyt - Ingen fartøy i nærheten:
-	Appen viser en melding: “Ingen fartøy i nærheten”. Brukeren kan likevel ringe nødnumre.

## Use-case 3 – Opprette en fangst i fiskelogg:
- Navn: Se fiskeloggen, legge til fangst, slette fangst og se fangstdetaljer
- Aktør: Hobbyfiskere
- Prebetingelser: Brukeren har tilgang til appen og er logget inn.
- Postbetingelser: Bruker kan se en oppdatert liste over fangster. Brukeren kan legge til eller slette fangster, og se detaljer per fangst.

#### Hovedflyt
1. Brukeren åpner Fiskelogg fra hjemskjermen.

2. Appen sender forespørsel til databasen for å hente tidligere fangster.

3. Hvis det finnes fangster, vises disse i en liste med dato, sted og art.

4. Hvis det ikke finnes fangster, vises meldingen: «Ingen fangster enda. Trykk + for å legge til».

5. Brukeren trykker på «+ Legg til fangst».

6. Appen spør: «Fikk du fangst?»

    - Hvis Ja: - Skjema med felter for sted, art, vekt, antall, dato, notater og bilde vises.

    - Hvis Nei: - Den skjuler feltene for art, vekt og antall, men lar bruker fylle ut sted og notat (nullfangst).

7. Brukeren fyller inn nødvendig informasjon og trykker Lagre.

8. Lagrer fangsten i databasen.

9. Fiskeloggen oppdateres med den nye fangsten.

#### Alternativ flyt 1 - Slette en fangst
1. Brukeren trykker på søppelbøtte-ikonet ved en fangst i listen.
2. Ber om bekreftelse fra brukeren: «Er du sikker på at du vil slette denne fangsten?»
3. Brukeren bekrefter.
4. Fangsten blir slettet fra databasen.
5. Fiskeloggen oppdateres og fangsten fjernes.

#### Alternativ flyt 2 - Se detaljer om en fangst

1. Brukeren trykker på en fangst i listen.

2. Appen åpner en detaljvisning som viser:

    - Dato

    - Fangstart

    - Vekt og antall

    - Lokasjon

    - Notater

    - Bilde

3. Brukeren kan gå tilbake til listen via en tilbakeknapp.

## Use-case 4 – Lagre mine favorittområder for å fiske:
- Navn: Se favorittområder, legge til favorittområde, slette favorittområde og se detaljer
- Aktør: Hobbyfiskere
- Prebetingelser: Brukeren har tilgang til appen og er logget inn
- Postbetingelser: Listen over favorittområder er oppdatert.
Brukeren kan se detaljer, legge til fangster, slette eller se området på kartet.

#### Hovedflyt
1. Brukeren åpner Favorittområder fra hjemskjermen.

2. Appen henter listen over lagrede favorittområder fra databasen.

3. Hvis områder finnes, vises de i en liste med navn og kartvisning.

4. Hvis listen er tom, vises meldingen: «Ingen favorittområder ennå. Trykk + for å legge til.»

5. Brukeren trykker på «+» for å registrere et nytt område.

6. Appen spør brukeren om type område:

    - Punkt (markør på kart)

    - Polygon (område tegnet på kartet)

7. Brukeren markerer området på kartet og fyller ut navn, beskrivelse og eventuelt notat.

8. Brukeren trykker Lagre, og området lagres i databasen.

9. Listen over favoritter oppdateres.

#### Alternativ flyt 1 - Se detaljer for et favorittområde
1. Brukeren trykker på et område i favorittlisten.

2. Appen viser detaljer - Navn, beskrivelse, statistikk over fangster. Beste fangst, siste fangst

3. Brukeren kan:

    3a. Trykke «Legg til fangst», og blir sendt til fiskelogg skjema

    3b. Trykke «Vis på kart», og området vises i kartvisning med markering

#### Alternativ flyt 2 - Slette et spesfikk favorittområde
1. Brukeren trykker på søppelbøtte-ikonet ved et område

2. Brukeren får meldingen: «Er du sikker på at du vil slette dette området?»

3. Brukeren bekrefter

4. Området slettes fra databasen

5. Listen oppdateres i favorittområde siden

#### Alternativ flyt 3 - Slette alle favorittområder
1. Brukeren trykker på søppelboks i favorittområde-"hovedsiden"

2. Brukeren får en advarsel: «Dette vil slette alle lagrede favorittområder. Fortsette?»

3. Ved bekreftelse slettes alle områder og siden viser en tom liste

#### Alternativ flyt 4 - Favorittområde som lagres med eksisterende navn
1. Brukeren skriver inn et navn som allerede finnes i listen over favorittområder.

2. Validerer navnet og oppdager duplikaten.

3. Appen viser feilmelding: «Et favorittsted med dette navnet finnes allerede. Velg et annet navn.»

4. Brukeren må endre navnet for å kunne lagre området.

## Use-case 5 – Se været for i dag eller tre dager frem i tid
- Navn: Se værvarsel for valgt posisjon
- Aktør: Hobbyfisker
- Prebetingelser: Brukeren er koblet til internett Posisjonstjenester er aktivert hvis brukeren vil se været der de er.
- Postbetingelser: Brukeren har sett oppdatert værinfo for valgt sted

#### Hovedflyt
1. Brukeren åpner værvarsel fra hjemskjerm eller kart.

2. Det blir hentet posisjonen til brukeren.

3. Det sendes forespørsel til MET API om værdata for posisjonen.

4. Siden viser dagens vær + 3-dagers varsel.

5. Brukeren kan bla mellom dagene og evt. bytte område

#### Alternativ flyt 1 - Søker etter et annet område manuelt
1. Brukeren velger "Søk etter område"

2. Appen viser søkefelt

3. Brukeren velger nytt område

4. Henter og viser værdata for valgt område

