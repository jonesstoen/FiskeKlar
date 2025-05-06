
# Velkommen til gruppe 46 i IN2000 – Våren 2025

## Gruppen består av:
- **Bendik Mendes Dahl** (bendikmd@uio.no)  
- **Johannes Støen** (johastoe@uio.no)  
- **Mohammad Ali Shakil** (mohas@uio.no)  
- **Kaja Mamelund Bradal** (kajambra@uio.no)  
- **Maria Helena Rogne** (marihrog@uio.no)  
- **Artin Akbari** (artina@uio.no)

##  Innholdsfortegnelse

1. [Om appen – VærBit](#værbit)  
2. [Hvordan kjøre appen](#hvordan-kjøre-appen)  
   1. [Krav](#krav)  
   2. [Bygg og kjør](#bygg-og-kjør)  
   3. [Funksjoner](#funksjoner)  
3. [Skjermbilder](#skjermbilder)  
4. [Avhengigheter og biblioteker](#avhengigheter-og-biblioteker)  
5. [Biblioteker utenfor pensum](#biblioteker-utenfor-pensum)  
   1. [MapLibre](#maplibre)  
   2. [Coil](#coil)  
   3. [NetCDF-Java / CDM](#netcdf-java--cdm)  
   4. [KSP (Kotlin Symbol Processing)](#ksp-kotlin-symbol-processing)  
6. [Tillatelser appen krever](#tillatelser-appen-krever)


## VærBit 

//må bestemme oss for navn 
<p align="center">
  <img src="https://github.uio.no/IN2000-V25/team-46/raw/fda1b4150d3e8b074ebcb3d61234382fb63a5f04/app/src/main/res/drawable/app_logo.png" alt="VærBit Logo" width="200"/>
</p>

En Android-app utviklet i Kotlin med Jetpack Compose, laget for hobbyfiskere og sjøentusiaster. Appen viser fartøydata, værvarsler, vind- og strømforhold i sanntid på et interaktivt kart, og gir mulighet for å loggføre egne fangster.

## Hvordan kjøre appen

1. **Krav**
   - Android 8.0 (API 26) eller høyere( ***må dobbeltsjekke dette***)
   - Lokasjonstillatelse (`ACCESS_FINE_LOCATION`)
   - Kamera-tilgang for å ta profil- og fangstbilder

2. **Byggyng og kjøring**
   - Åpne prosjektet i **Android Studio**
   - Kjør appen på en fysisk enhet eller emulator med lokasjon aktivert
   - Ved første oppstart vil appen be om nødvendige tillatelser, men den kjører også uten lokasjonstillatelse. 

3. **Funksjoner**
   - Viser AIS-data fra BarentsWatch, som gir live oppdatering om andre fartøy ute på sjøen.
   - Henter og visualiserer vind- og strømdata fra GRIB-filer
   - Viser MetAlerts (værvarsler) i kartet
   - Gir varsellyd og visuell markør hvis bruker er i et område med aktivt farevarsel
   - Lar brukeren loggføre fangst med bilde og posisjon
   - Profilside med brukerinformasjon og profilbilde

## Skjermbilder
Skjermbilder må leggs ti senere

##  Avhengigheter og biblioteker

Appen er bygget med følgende biblioteker:

| Bibliotek                        | Brukt til                | Forklaring                                                                                  |
| -------------------------------- | ------------------------ | ------------------------------------------------------------------------------------------- |
| **Jetpack Compose**              | UI                       | Deklarativt brukergrensesnitt – hovedrammeverk for skjermene                                |
| **Material 3**                   | Design                   | Komponenter for moderne design i Compose                                                    |
| **MapLibre GL**                  | Kartvisning              | Åpen kildekode-alternativ til Google Maps – brukes for å vise kart, AIS og værdata          |
| **Room**                         | Lokal database           | Lagrer fangstlogg og brukerinformasjon lokalt                                               |
| **Kotlinx.coroutines**           | Asynkron behandling      | Brukes for å hente og vise data uten å blokkere UI                                          |
| **Retrofit + Gson**              | API-kall og JSON-parsing | Brukt til å hente data fra BarentsWatch og MetAlerts *(ikke dekket i kurset)*               |
| **OkHttp + Logging Interceptor** | Nettverksdebugging       | Logger HTTP-kall for enkel feilsøking                                                       |
| **CDM / GRIB**                   | GRIB-parsing             | Brukes for å lese vind- og strømdata fra meteorologiske GRIB-filer *(ikke dekket i kurset)* |
| **AndroidX Location**            | Lokasjon                 | Henter sanntidsposisjon og varsler ved fare                                                 |
| **Coil**                         | Bildehåndtering          | Viser og lagrer bilder for fangst og profil                                                 |
| **KSP**                          | Kompileringsstøtte       | Brukes av Room og Hilt til å generere nødvendig kode                                        |
| **ViewModel + LiveData**         | Tilstandshåndtering      | For å binde data til UI på en reaktiv måte                                                  |



##  Biblioteker utenfor pensum

For å oppnå funksjonaliteten vi ønsket, har vi brukt noen biblioteker som **ikke er dekket i pensum** for IN2000. Her er en kort forklaring på dem:

###  MapLibre

>Et åpen kildekode-alternativ til Google Maps. Vi bruker det til å vise kart, fartøy (AIS), vind, strøm og værvarsler og det lar oss tegne kart, lag og markører med god kontroll og fleksibilitet.  Det gir god kontroll over kartstil og data uten lisensbegrensninger.

[Dokumentasjon](https://maplibre.org/maplibre-native/android/api/)


###  Coil
>Et moderne og lett bibliotek for bildehåndtering i Jetpack Compose. Brukes til å vise bilder av fangster og profilbilde på en enkel og effektiv måte.

[Dokumentasjon](https://coil-kt.github.io/coil/getting_started/)

### NetCDF-Java / CDM

>Et Java-bibliotek utviklet av Unidata som implementerer Common Data Model (CDM) for å lese og skrive vitenskapelige datasett, inkludert GRIB1 og GRIB2. Vi bruker dette biblioteket til å lese meteorologiske GRIB-filer og hente ut vind- og strømdata for visning i appen. NetCDF-Java gir en enhetlig tilgang til data og støtter ulike filformater, noe som gjør det til et kraftig verktøy for håndtering av komplekse datasett. igjennom kotlin sin java støtte var det ingen problem å bruke dette. 

[Dokumentasjon-NetCDF](https://docs.unidata.ucar.edu/netcdf-java/current/userguide/)
[Dokumentasjon- CDM](https://docs.unidata.ucar.edu/netcdf-java/current/userguide/common_data_model_overview.html)


### KSP (Kotlin Symbol Processing)
>Et verktøy som brukes til å generere kode under kompilering. Room og Hilt benytter KSP for å generere databindings- og injeksjonskode automatisk, noe som reduserer boilerplate og forbedrer ytelse.

[Dokumentasjon](https://kotlinlang.org/docs/ksp-overview.html)

##  Tillatelser appen krever

| Tillatelse                    | Forklaring                                                      |
| ----------------------------- | --------------------------------------------------------------- |
| `ACCESS_FINE_LOCATION`        | Brukes for å vise brukerens posisjon og gi relevante værvarsler |
| `CAMERA`                      | Brukes for å ta bilder til fangstlogg og profil                 |
| `READ/WRITE_EXTERNAL_STORAGE` | Brukes hvis bilder lagres i filsystemet                         |

