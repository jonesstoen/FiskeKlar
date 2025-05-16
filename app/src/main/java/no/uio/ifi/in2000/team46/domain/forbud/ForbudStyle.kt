package no.uio.ifi.in2000.team46.domain.forbud

import android.graphics.Color

//WArNINGS: this class is not used, but is part of the forbud api which is not used in the app
object ForbudStyle {
    const val ICON_TYPE = "forbud"
    val COLOR = Color.parseColor("#FFA500") // Oransje farge

    fun getIconType(): String = ICON_TYPE
    fun getColor(): Int = COLOR
}