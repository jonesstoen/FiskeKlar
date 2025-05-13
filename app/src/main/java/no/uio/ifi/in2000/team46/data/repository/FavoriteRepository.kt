package no.uio.ifi.in2000.team46.data.repository

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.dao.FavoriteLocationDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation
import org.json.JSONArray
import org.json.JSONObject

// favoriterepository provides data operations for managing favorite locations
// it includes helpers for serializing area points and estimating polygon area

class FavoriteRepository(
    private val favoriteLocationDao: FavoriteLocationDao
) {
    // retrieves a favorite location by its id
    fun getFavoriteById(id: Int): Flow<FavoriteLocation?> {
        return favoriteLocationDao.getFavoriteById(id)
    }

    // retrieves all favorite locations as a reactive flow
    fun getAllFavoritesFlow(): Flow<List<FavoriteLocation>> {
        return favoriteLocationDao.getAllFavoritesFlow()
    }

    // inserts a new favorite location
    suspend fun insertFavorite(favorite: FavoriteLocation) {
        favoriteLocationDao.insert(favorite)
    }

    // updates an existing favorite location
    suspend fun updateFavorite(favorite: FavoriteLocation) {
        favoriteLocationDao.update(favorite)
    }

    // deletes a specific favorite location
    suspend fun deleteFavorite(favorite: FavoriteLocation) {
        favoriteLocationDao.delete(favorite)
    }

    // parses a json string of area points into a list of lat/lng pairs
    fun getAreaPoints(favorite: FavoriteLocation): List<Pair<Double, Double>> {
        return favorite.areaPoints?.let {
            try {
                val jsonArray = JSONArray(it)
                List(jsonArray.length()) { i ->
                    val point = jsonArray.getJSONObject(i)
                    Pair(point.getDouble("lat"), point.getDouble("lng"))
                }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    // serializes a list of lat/lng pairs into a json string
    fun pointsToJsonString(points: List<Pair<Double, Double>>): String {
        val jsonArray = JSONArray()
        points.forEach { (lat, lng) ->
            val jsonPoint = JSONObject()
            jsonPoint.put("lat", lat)
            jsonPoint.put("lng", lng)
            jsonArray.put(jsonPoint)
        }
        return jsonArray.toString()
    }

    // estimates the area of a polygon defined by a list of lat/lng points (in km²)
    fun calculateAreaInSquareKm(points: List<Pair<Double, Double>>): Double {
        if (points.size < 3) return 0.0

        var area = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            val p1 = points[i]
            val p2 = points[j]

            // convert lat/lng to approximate cartesian coordinates
            val factor = Math.cos(Math.toRadians((p1.first + p2.first) / 2))
            val x1 = p1.second * factor
            val y1 = p1.first
            val x2 = p2.second * factor
            val y2 = p2.first

            area += (x1 * y2 - x2 * y1)
        }

        // return absolute area scaled to square kilometers
        return Math.abs(area) * 111.0 * 111.0 / 2.0
    }
}
