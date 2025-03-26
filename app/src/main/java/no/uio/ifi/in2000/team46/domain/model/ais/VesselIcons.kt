package no.uio.ifi.in2000.team46.domain.model.ais

import android.graphics.Color

object VesselIcons {
    data class VesselStyle(
        val color: Int,
        val iconType: String
    )

    val VESSEL_STYLES = mapOf(
        VesselTypes.AMBULANSEBÅT to VesselStyle(Color.parseColor("#FFE74C3C"), "ambulance"),
        VesselTypes.ANTIFOURENSNING to VesselStyle(Color.parseColor("#FF95A5A6"), "cleanup"),
        VesselTypes.BØYE_MED_AIS to VesselStyle(Color.parseColor("#FFE67E22"), "buoy"),
        VesselTypes.DYKKERFARTØY to VesselStyle(Color.parseColor("#FF34495E"), "diving"),
        VesselTypes.FISKEFARTØY to VesselStyle(Color.parseColor("#FFF39C12"), "fishing"),
        VesselTypes.FRAKTEFARTØY to VesselStyle(Color.parseColor("#FF2ECC71"), "cargo"),
        VesselTypes.HØYHASTIGHETSFARTØY to VesselStyle(Color.parseColor("#FF2980B9"), "speedboat"),
        VesselTypes.MILITÆRT_FARTØY to VesselStyle(Color.parseColor("#FF7F8C8D"), "military"),
        VesselTypes.PASSASJERFARTØY to VesselStyle(Color.parseColor("#FF3498DB"), "passenger"),
        VesselTypes.POLITI to VesselStyle(Color.parseColor("#FF8E44AD"), "police_new"),
        VesselTypes.TANKSKIP to VesselStyle(Color.parseColor("#FF8E44AD"), "tanker"),
        VesselTypes.TAUBÅT to VesselStyle(Color.parseColor("#FFE74C3C"), "tug"),
        VesselTypes.ANDRE_FARTØY to VesselStyle(Color.parseColor("#FF95A5A6"), "other")
    )

    fun getVesselStyle(vesselType: Int): VesselStyle {
        return VESSEL_STYLES[vesselType] ?: VESSEL_STYLES[VesselTypes.ANDRE_FARTØY]!!
    }
}