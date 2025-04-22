package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fishing_log")
data class FishingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val time : String,
    val location: String,
    val fishType: String,
    val weight: Double,
    val notes : String? = null,
    val imageUri: String? = null
)