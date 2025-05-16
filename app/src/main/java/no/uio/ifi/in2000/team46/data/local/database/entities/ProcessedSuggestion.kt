package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// processedsuggestion keeps track of location suggestions that have already been handled
// it uses the location name as the primary key to ensure uniqueness

@Entity(tableName = "processed_suggestions")
data class ProcessedSuggestion(
    @PrimaryKey val locationName: String
)