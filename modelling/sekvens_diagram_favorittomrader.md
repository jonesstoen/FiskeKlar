Sekvensdiagram for favorittområde

```mermaid
sequenceDiagram
    participant Bruker
    participant App
    participant Database
    participant KartVisning

    Bruker->>App: Åpner favorittområder
    App->>Database: Hent alle lagrede favorittområder
    Database-->>App: Returnerer resultat

    alt Lagrede favorittområder fra før
        App-->>Bruker: Viser liste over favorittområder

        alt Bruker velger et område
            Bruker->>App: Trykk på område
            App->>Database: Hent detaljert info
            Database-->>App: Områdedetaljer
            App-->>Bruker: Viser fangstdata og alternativer

            alt Velger: Legg til fangst
                Bruker->>App: Går til fangstregistrering
                App->>Bruker: Viser skjema
                Bruker->>App: Trykk: Lagre
                App->>Database: Lagre fangstdata
                Database-->>App: Bekreftelse
                App-->>Bruker: Oppdatert info
            else Velger: Vis på kart
                Bruker->>App: Trykk: Vis på kart
                App->>KartVisning: Zoom til område
                KartVisning-->>Bruker: Viser område markert
            end

            else Bruker trykker: Slett
                Bruker->>App: Bekrefter sletting
                App->>Database: Slett favoritt
                Database-->>App: Bekreftelse
                App-->>Bruker: Oppdatert liste
        end

    else Tom liste med favorittområder
        App-->>Bruker: Viser melding: Trykk + for å legge til favoritter
    end
```