package no.uio.ifi.in2000.team46.domain.ais

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import androidx.core.content.ContextCompat
import no.uio.ifi.in2000.team46.R
import android.graphics.Canvas

// vesselicons provides a mapping between vessel types and their styled icons
// includes icon initialization, color mapping, and utility for loading vector drawables as bitmaps


object VesselIcons {
    data class VesselStyle(
        val color: Int,
        val iconType: String
    )

    private var icons: Map<String, Bitmap>? = null
    // loads and caches all vessel icons as bitmaps from vector drawables
    fun initializeIcons(context: Context) {
        if (icons == null) {
            icons = mapOf(
                "ambulance" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_ambulance),
                "cleanup" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_cleanup),
                "buoy" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_buoy),
                "diving" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_diving),
                "fishing" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_fishing),
                "cargo" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_cargo),
                "speedboat" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_speedboat),
                "military" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_military),
                "passenger" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_passenger),
                "police_new" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_police_new),
                "tanker" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_tanker),
                "tug" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_tug),
                "other" to getBitmapFromVectorDrawable(context, R.drawable.ic_vessel_other)
            )
        }
    }

    fun getIcons(): Map<String, Bitmap> = icons ?: error("Icons not initialized")


    private val VESSEL_STYLES = mapOf(
        VesselTypes.AMBULANSEBAT to VesselStyle(Color.parseColor("#FFE74C3C"), "ambulance"),
        VesselTypes.ANTIFOURENSNING to VesselStyle(Color.parseColor("#FF95A5A6"), "cleanup"),
        VesselTypes.BOYE_MED_AIS to VesselStyle(Color.parseColor("#FFE67E22"), "buoy"),
        VesselTypes.DYKKERFARTOY to VesselStyle(Color.parseColor("#FF34495E"), "diving"),
        VesselTypes.FISKEFARTOY to VesselStyle(Color.parseColor("#FFF39C12"), "fishing"),
        VesselTypes.FRAKTEFARTOY to VesselStyle(Color.parseColor("#FF2ECC71"), "cargo"),
        VesselTypes.HØYHASTIGHETSFARTOY to VesselStyle(Color.parseColor("#FF2980B9"), "speedboat"),
        VesselTypes.MILITERT_FARTOY to VesselStyle(Color.parseColor("#FF7F8C8D"), "military"),
        VesselTypes.PASSASJERFARTOY to VesselStyle(Color.parseColor("#FF3498DB"), "passenger"),
        VesselTypes.POLITI to VesselStyle(Color.parseColor("#FF8E44AD"), "police_new"),
        VesselTypes.TANKSKIP to VesselStyle(Color.parseColor("#FF8E44AD"), "tanker"),
        VesselTypes.TAUBAT to VesselStyle(Color.parseColor("#FFE74C3C"), "tug"),
        VesselTypes.ANDRE_FARTOY to VesselStyle(Color.parseColor("#FF95A5A6"), "other")
    )
    // loads a vector drawable and converts it to a bitmap
    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId) as VectorDrawable
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun getVesselStyle(vesselType: Int): VesselStyle {
        return VESSEL_STYLES[vesselType] ?: VESSEL_STYLES[VesselTypes.ANDRE_FARTOY]!!
    }
}
