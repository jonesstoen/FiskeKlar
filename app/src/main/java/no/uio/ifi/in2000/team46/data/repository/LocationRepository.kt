package no.uio.ifi.in2000.team46.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

// locationrepository provides methods to retrieve the user's current or last known location
// uses fusedlocationprovider and handles runtime permission checks internally

class LocationRepository(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // attempts to quickly retrieve the last known location if permissions are granted
    @SuppressLint("MissingPermission")
    suspend fun getFastLocation(): Location? {

        // check if location permissions are granted
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        // get last known location if available
        val last = fusedLocationClient.lastLocation.await()
        if (last != null) return last

        // fallback: request current high accuracy location
        return fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .await()
    }

    // public method that wraps fast location retrieval with try-catch
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            getFastLocation()
        } catch (e: SecurityException) {
            null
        }
    }
}
