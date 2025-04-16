package no.uio.ifi.in2000.team46.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = false) val id: Int = 1, // alltid én bruker lagret
    val name: String,
    val username: String,
    val memberSince: String,
    val profileImageUri: String? = null
)
