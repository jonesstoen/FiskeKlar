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

class LocationRepository(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    // method for getting the last known location fast if already available
    @SuppressLint("MissingPermission")
    suspend fun getFastLocation(): Location? {

        //check permissions
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        // try to get the last known location
        val last = fusedLocationClient.lastLocation.await()
        if (last != null) return last

        // if no last known location, get the current location
        return fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .await()
    }


    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            getFastLocation()
        } catch (e: SecurityException) {
            null
        }
    }
}