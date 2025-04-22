package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fish_type")
data class FishType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
