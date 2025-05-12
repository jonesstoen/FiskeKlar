
```mermaid
classDiagram
    class Hobbyfisker {
        +brukerId: String
        +navn: String
        +leggTilFangst(f: Fangst)
        +leggTilFavorittomrade(f: Favorittomrade)
        +åpneKart()
        +brukeSOS()
        +seVærvarsel()
    }

    class Fiskelog {
        +fangster: List<Fangst>
        +leggTilFangst(f: Fangst)
        +slettFangst(fangstId: String)
        +hentFangster(): List<Fangst>
    }

    class Fangst {
        +id: String
        +art: String
        +vekt: Double
        +antall: Int
        +notater: String
        +dato: Date
        +leggTilBilde(bilde: String)
    }

    class Favorittomrade {
        +id: String
        +navn: String
        +beskrivelse: String
        +lokasjon: String
        +leggTilFangst(f: Fangst)
        +visOmradePaKart()
    }

    class Kart {
        +visKart()
        +visFarevarsler()
        +visBåttrafikk()
        +visBølger()
        +visRegn()
        +visVind()
        +visStrøm()
        +visDrift()
    }

    class Værvarsel {
        +posisjon: String
        +værdata: String
        +hentVærdata()
        +hentLangtidsvarsel()
    }

    class SOS {
        +posisjon: String
        +hentNåværendePosisjon()
        +hentNærmesteFartøy()
        +ringNødhjelp()
    }

    class Database {
        +lagreFangst(f: Fangst)
        +slettFangst(fangstId: String)
        +lagreFavorittomrade(f: Favorittomrade)
        +hentFavorittomrader(): List<Favorittomrade>
    }

    class VærdataAPI {
        +hentVær()
        +hentLangtidsvarsel()
    }

    class FiskeInfoAPI {
        +hentFartøy()
        +hentFarevarsler()
        +hentGRIBdata() %% Bølger, regn, vind, strøm, drift
    }

    %% Relasjoner med multiplikitet og kommentarer
    Hobbyfisker "1" --> "1" Fiskelog : bruker
    Hobbyfisker "1" --> "0..*" Favorittomrade : har
    Hobbyfisker "1" --> "1" Kart : benytter
    Hobbyfisker "1" --> "1" SOS : benytter
    Hobbyfisker "1" --> "1" Værvarsel : sjekker

    Fiskelog "1" --> "*" Fangst : består av
    Favorittomrade "1" --> "*" Fangst : inneholder

    Kart "1" --> "1" Værvarsel : henter
    Kart "1" --> "1" FiskeInfoAPI : henter data
    SOS "1" --> "1" FiskeInfoAPI : henter data

    Fiskelog --> Database : lagrer data i
    Favorittomrade --> Database : lagrer data i
    Værvarsel --> VærdataAPI : henter data fra
```