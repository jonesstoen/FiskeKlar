package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.FishType

@Dao
interface FishTypeDao {
    @Query("SELECT * FROM fish_type ORDER BY name")
    fun getAll(): Flow<List<FishType>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(types: List<FishType>)
    @Query("SELECT COUNT(*) FROM fish_type")
    suspend fun getCount(): Int
}