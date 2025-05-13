package no.uio.ifi.in2000.team46.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.entities.FishType
/**
 * fishtypedao provides access to the fish_type table in the local database.
 * it supports reading all fish types, inserting multiple entries, and checking table size.
 */
@Dao
interface FishTypeDao {

    // returns all fish types ordered alphabetically by name as a flow
    @Query("SELECT * FROM fish_type ORDER BY name")
    fun getAll(): Flow<List<FishType>>

    // inserts a list of fish types, ignores entries that would cause conflicts
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(types: List<FishType>)

    // returns the number of rows currently in the fish_type table
    @Query("SELECT COUNT(*) FROM fish_type")
    suspend fun getCount(): Int
}
