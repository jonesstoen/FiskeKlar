package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_location")
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val locationType: String, //Enten område eller punkt
    val latitude: Double,
    val longitude: Double,
    val areaPoints: String? = null,
    val notes: String? = null,
    val targetFishTypes: String? = null,
    val createdAt : Long = System.currentTimeMillis()
)