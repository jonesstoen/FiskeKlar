package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fishing_log")
data class FishingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val location: String,
    val fishType: String,
    val weight: Double,
    val date: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String? = null,
    val imageUri: String? = null,
    val count: Int = 1 // default = 1

)