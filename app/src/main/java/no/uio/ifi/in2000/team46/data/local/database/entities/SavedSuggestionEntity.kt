package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_suggestions")
data class SavedSuggestionEntity(
    @PrimaryKey val name: String,
    val fishCount: Int,
    val latitude: Double,
    val longitude: Double,
    val isRead: Boolean = false
)
