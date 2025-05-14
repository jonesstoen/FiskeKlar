Aktivitetsdiagram for favoritt-steder

```mermaid
flowchart TD
    Start((Start)) --> FinnFavoritter{Finnes lagrede favorittsteder?}

    FinnFavoritter -->|Ja| VelgFavoritt[Velg et favorittsted]
    VelgFavoritt --> VisDetaljer[Vis detaljer for valgt sted]

    VisDetaljer --> Valg{Hva vil du gjøre?}
    Valg -->|Legg til fangst| LeggFangst[Registrer fangstdata]
    Valg -->|Vis på kart| VisKart[Vis favorittsted på kart]
    Valg -->|Slett dette stedet| BekreftSlett[Vis bekreftelsesdialog]
    Valg --> |Se værvarselet for dette stedet| VisVærvarsel[Vis værmeldingen for stedet via værvarsel-skjermen]

    LeggFangst --> LagreFangst[Lagre fangst]
    LagreFangst --> Slutt
    VisVærvarsel --> Slutt


    VisKart --> Slutt((Slutt))
    BekreftSlett -->|Slett| SlettSted[Sletter valgt sted]
    BekreftSlett -->|Avbryt| Slutt
    SlettSted --> Slutt

    FinnFavoritter -->|Nei| LeggTilFavoritt[Trykk + for å legge til nytt favorittsted]
    LeggTilFavoritt --> ValgType{Velg type favoritt}
    ValgType -->|Punkt| VelgPunkt[Velg punkt på kart og fyll inn detaljer]
    ValgType -->|Område| TegnOmråde[Tegn polygon og fyll inn detaljer]
    VelgPunkt --> Slutt((Slutt))
    TegnOmråde --> Slutt((Slutt))

    VelgFavoritt --> ValgSlettAlle{Ønsker du å slette alle favoritter?}
    ValgSlettAlle -->|Ja| BekreftSlettAlle[Vis bekreftelse]
    BekreftSlettAlle -->|Slett alle| SlettAlleFavoritter[Sletter alle favoritter]
    BekreftSlettAlle -->|Avbryt| Slutt((Slutt))
    SlettAlleFavoritter --> Slutt((Slutt))
```