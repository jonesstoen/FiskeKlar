package no.uio.ifi.in2000.team46.utils.ais


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import no.uio.ifi.in2000.team46.R
import org.maplibre.android.maps.Style

object VesselIconHelper {
    private const val TAG = "VesselIconHelper"
    /**
     * Call this function when your map style is loaded to add all vessel icons.
     */
    fun addVesselIconsToStyle(context: Context, style: Style) {
        Log.d(TAG, "Adding vessel icons to style")
        addIcon(context, style, "fishing", R.drawable.ic_vessel_fishing)
        addIcon(context, style, "cargo", R.drawable.ic_vessel_cargo)
        addIcon(context, style, "speedboat", R.drawable.ic_vessel_speedboat)
        addIcon(context, style, "military", R.drawable.ic_vessel_military)
        addIcon(context, style, "passenger", R.drawable.ic_vessel_passenger)
        addIcon(context, style, "police_new", R.drawable.ic_vessel_police_new)
        addIcon(context, style, "sar", R.drawable.ic_vessel_sar)
        addIcon(context, style, "tanker", R.drawable.ic_vessel_tanker)
        addIcon(context, style, "tug", R.drawable.ic_vessel_tug)
        addIcon(context, style, "other", R.drawable.ic_vessel_other)
    }

    private fun addIcon(context: Context, style: Style, iconId: String, @DrawableRes drawableRes: Int) {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        val bitmap = drawable?.toBitmap()
        if (bitmap != null) {
            style.addImage(iconId, bitmap)
            Log.d(TAG, "Icon $iconId added successfully")
        } else {
            Log.e(TAG, "Failed to decode resource for icon: $iconId")
        }
    }

    private fun Drawable.toBitmap(): Bitmap? {
        if (this is BitmapDrawable) {
            return bitmap
        }
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}
