package no.uio.ifi.in2000.team46.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team46.data.local.database.dao.FishTypeDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FishType

// fishtyperepository handles retrieval and insertion of fish types from the local database
// it exposes a flow of all fish types sorted alphabetically

class FishTypeRepository(
    private val dao: FishTypeDao
) {
    // flow that emits all fish types, sorted by name
    val allTypes: Flow<List<FishType>> =
        dao.getAll()
            .map { list -> list.sortedBy { it.name } }

    // inserts a single fish type (ignores if already exists)
    suspend fun insert(fishType: FishType) {
        dao.insertAll(listOf(fishType))
    }
}
