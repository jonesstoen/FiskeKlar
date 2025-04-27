package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_suggestions")
data class ProcessedSuggestion(
    @PrimaryKey val locationName: String
)