package no.uio.ifi.in2000.team46.domain.ais

// vesseltypes defines static AIS ship type codes used for mapping vessel categories

object VesselTypes {
    const val AMBULANSEBAT = 58
    const val ANTIFOURENSNING = 59
    const val BOYE_MED_AIS = 31
    const val DYKKERFARTOY = 33
    const val FISKEFARTOY = 30
    const val FRAKTEFARTOY = 70
    const val HØYHASTIGHETSFARTOY = 40
    const val MILITERT_FARTOY = 35
    const val PASSASJERFARTOY = 60
    const val POLITI = 55
    const val SAR = 51
    const val TANKSKIP = 80
    const val TAUBAT = 52
    const val ANDRE_FARTOY = 90

    val ALL_TYPES = mapOf(
        "Ambulansebåt" to AMBULANSEBAT,
        "Antiforurensning" to ANTIFOURENSNING,
        "Bøye med AIS-signal" to BOYE_MED_AIS,
        "Dykkerfartøy" to DYKKERFARTOY,
        "Fiskefartøy" to FISKEFARTOY,
        "Fraktefartøy" to FRAKTEFARTOY,
        "Høyhastighetsfartøy" to HØYHASTIGHETSFARTOY,
        "Militært fartøy" to MILITERT_FARTOY,
        "Passasjerfartøy" to PASSASJERFARTOY,
        "Politi" to POLITI,
        "SAR" to SAR,
        "Tankskip" to TANKSKIP,
        "Taubåt" to TAUBAT,
        "Andre fartøy" to ANDRE_FARTOY
    )
}