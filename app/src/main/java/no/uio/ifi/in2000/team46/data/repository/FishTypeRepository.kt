package no.uio.ifi.in2000.team46.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team46.data.local.database.dao.FishTypeDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FishType

class FishTypeRepository(
    private val dao: FishTypeDao
) {
    // Fetching all fish types from the database, and sorting them by name
    val allTypes: Flow<List<FishType>> =
        dao.getAll()                     // Flow<List<FishType>>
            .map { list -> list.sortedBy { it.name } }
}
