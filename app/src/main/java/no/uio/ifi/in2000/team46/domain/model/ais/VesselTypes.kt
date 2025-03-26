package no.uio.ifi.in2000.team46.domain.model.ais



object VesselTypes {
    val AMBULANSEBÅT = 58
    val ANTIFOURENSNING = 59
    val BØYE_MED_AIS = 31
    val DYKKERFARTØY = 33
    val FISKEFARTØY = 30
    val FRAKTEFARTØY = 70
    val HØYHASTIGHETSFARTØY = 40
    val MILITÆRT_FARTØY = 35
    val PASSASJERFARTØY = 60
    val POLITI = 55
    val SAR = 51
    val TANKSKIP = 80
    val TAUBÅT = 52
    val ANDRE_FARTØY = 90

    val ALL_TYPES = mapOf(
        "Ambulansebåt" to AMBULANSEBÅT,
        "Antiforurensning" to ANTIFOURENSNING,
        "Bøye med AIS-signal" to BØYE_MED_AIS,
        "Dykkerfartøy" to DYKKERFARTØY,
        "Fiskefartøy" to FISKEFARTØY,
        "Fraktefartøy" to FRAKTEFARTØY,
        "Høyhastighetsfartøy" to HØYHASTIGHETSFARTØY,
        "Militært fartøy" to MILITÆRT_FARTØY,
        "Passasjerfartøy" to PASSASJERFARTØY,
        "Politi" to POLITI,
        "SAR" to SAR,
        "Tankskip" to TANKSKIP,
        "Taubåt" to TAUBÅT,
        "Andre fartøy" to ANDRE_FARTØY
    )
}