package no.uio.ifi.in2000.team46.domain.model.forbud

import android.graphics.Color

object ForbudStyle {
    const val ICON_TYPE = "forbud"
    val COLOR = Color.parseColor("#FFA500") // Oransje farge

    fun getIconType(): String = ICON_TYPE
    fun getColor(): Int = COLOR
}