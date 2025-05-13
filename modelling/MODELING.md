## Om diagrammene og våre valg for modelleringen

Denne "modelling" mappen innholder både use-case diagram og use-case beskrivelse, sekvensdiagram, aktivitetsdiagram og klassediagrma. Det er blitt brukt mermaid til å utforme diagrammene, utenom use-case diagrammet. Der har vi valgt å bruke draw.io for å gjøre det enklere og lettere å tegne, ettersom det er et verktøy man er kjent fra tidligere emner. 

### Valg av diagrammer og begrunnelse:
#### 1. Use-case og use-case beskrivelse
##### Filer: use_case.png, use-case_beskrivelse.md

Modelleringen startet med å utvikle use-case diagram for å få en visualisering av hovedfunksjonalitetene til appen, samt relasjonen mellom aktørene og systemet. Dette hjalp oss med å identifisere og vise hva som er funskjonene til appen, og hvilke handlinger brukeren kan utføre. I tillegg skrev vi detaljerte use-case-beskrivelser for hver funksjonalitet. Beskrivelsen viser hvordan appen fungerer i praksis basert på bestemte use-case oppgaver. Dette er for å tydelig dokumentere de viktigste brukerreisene og funskjonalitetene: åpne kart, bruke SOS-funksjonen, registrere fangst, lagre favorittsteder og se værdata.



#### 2. Sekvensdiagrammer
#### Filer: 
- sekvens_diagram_SOS.md
- sekvens_diagram_kartskjerm.md
- sekvens_diagram_fiskelogg.md
- sekvens_diagram_favorittsteder.md
- sekvens_diagram_vaervarsel.md

Det er laget totalt 5 ulike sekvensdiagramer for de ulike funksjonalitetene. Vi valgte å lage ett diagram per hovedfunksjonalitet i appen (kart, SOS, fiskelogg, favorittsteder og værvarsel), slik at vi får belyst hver delprosess detaljert. Dette er for å visualisere hvordan appen kommuniserer med databasen, kartvisningen og eksterne API-er. De gir innsikt i hvordan ulike komponenter kommuniserer og i hvilken rekkefølge hendelser skjer. Ved å bruke alt og opt i Mermaid kunne vi tydelig skille mellom hovedflyt og alternative scenarier, noe som gjør diagrammene både informative og lette å lese.

#### 3. Klassediagram
#### Filer: klasse_diagram.md

Klassediagrammet gir en strukturert oversikt over appens indre arkitektur, inkludert objektene vi benytter, deres egenskaper og metodene som hører til hver klasse. Vi valgte å bruke klassediagram fordi det er særlig nyttig for å:

- Visualisere datastrukturen i appen (f.eks. hvordan en Fangst er bygd opp med art, vekt, bilde osv.).

- Avdekke sammenhenger og avhengigheter mellom objekter, som at Hobbyfisker har en Fiskelog, og kan legge til Favorittsted.

- Skille mellom modellklasser (som Fangst, Favorittsted, Værvarsel) og tjenester eller API-er (Database, FiskeInfoAPI, VærdataAPI).

Vi valgte å inkludere både attributter og metoder for å gjøre diagrammet mer konkret og knytte det tettere til faktisk funksjonalitet i appen. Multiplikiteter ble brukt der det er naturlig, f.eks. at én fiskelog inneholder mange fangster (1..*), og at en hobbyfisker kan ha flere favorittsteder. Diagrammet er viktig i videre utvikling fordi det fungerer som et grunnlag for klasser i koden, spesielt hvis man bruker OOP. I tillegg hjelper det nye utviklere å raskt forstå datastrukturen.

#### 4. Aktivitetsdiagram
#### Filer: 
- aktivitetsdiagram_favorittsteder.md
- aktivtetsdiagram_kart.md
- aktivitetsdiagram_SOS.md

Vi valgte å bruke aktivitetsdiagrammer for å illustrere flyten av kontroll og beslutninger i de mest interaktive delene av applikasjonen. Diagrammene viser hvordan brukeren navigerer gjennom funksjonaliteten og hvordan appen responderer avhengig av valgene som tas.

Aktivitetsdiagrammer er nyttige fordi:

- De fremhever valg, grener og betingelser i brukerflyten.

- De visualiserer kompleks logikk på en intuitiv måte.

- De hjelper med å planlegge og forstå hva som skjer bak hver knapp og hvert valg i UI.

For å holde modellen oversiktlig og fokusert, er det valgt å lage tre aktivitetsdiagrammer – ett for hver funksjon der det skjer mest logikk og brukeren har mange valg. Disse representerer de mest dynamiske delene av appen. Målet var ikke å vise alle funksjoner med aktivitetsdiagram, men å bruke dem for å få frem et annet perspektiv – nemlig brukerens beslutningspunkter og systemets reaksjoner.
