package no.uio.ifi.in2000.team46.utils.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

// this file defines a helper class to request location permissions in a clean and reusable way
// it handles both fine and coarse location requests, and delivers result through a callback

class LocationPermissionManager(private val activity: ComponentActivity) {

    private var onPermissionResult: ((granted: Boolean) -> Unit)? = null
    // launcher used to request multiple permissions (fine and coarse)
    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            onPermissionResult?.invoke(granted)
        }
    // checks if location permission is granted, otherwise launches system dialog
    fun checkAndRequestPermission(onResult: (granted: Boolean) -> Unit) {
        onPermissionResult = onResult

        val fineLocationGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            onPermissionResult?.invoke(true)
        }
    }
}