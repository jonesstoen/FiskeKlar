package no.uio.ifi.in2000.team46.data.repository

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team46.data.local.database.dao.FavoriteLocationDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation
import org.json.JSONArray
import org.json.JSONObject

class FavoriteRepository (
    private val favoriteLocationDao: FavoriteLocationDao
) {
    fun getFavoriteById(id: Int): Flow<FavoriteLocation?> {
        return favoriteLocationDao.getFavoriteById(id)
    }

    suspend fun insertFavorite(favorite: FavoriteLocation) {
        favoriteLocationDao.insert(favorite)
    }

    suspend fun deleteFavorite(favorite: FavoriteLocation) {
        favoriteLocationDao.delete(favorite)
    }

    // Hjelpefunksjon for å konvertere JSON-streng til liste med punkter
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

    // Hjelpefunksjon for å konvertere liste med punkter til JSON-streng
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

    // Hjelpefunksjon for å beregne areal av et polygon (for områder)
    fun calculateAreaInSquareKm(points: List<Pair<Double, Double>>): Double {
        if (points.size < 3) return 0.0

        var area = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            val p1 = points[i]
            val p2 = points[j]

            // Konverterer lat/lng til tilnærmet kartesiske koordinater
            val factor = Math.cos(Math.toRadians((p1.first + p2.first) / 2))
            val x1 = p1.second * factor
            val y1 = p1.first
            val x2 = p2.second * factor
            val y2 = p2.first

            area += (x1 * y2 - x2 * y1)
        }

        // Absoluttverdi og skaler til ca. km²
        return Math.abs(area) * 111.0 * 111.0 / 2.0
    }
}