package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// fishtype represents a fish species or category that can be selected or referenced in the app
// it is stored in its own table and identified by a unique id and a name

@Entity(tableName = "fish_type")
data class FishType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
