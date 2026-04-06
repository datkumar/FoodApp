import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity // or Activity

class LocationPermissionHelper(private val activity: FragmentActivity) {

    // Launcher for foreground location
    private val locationPermissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            when {
                fineGranted -> {
                    // Best case - precise location
                    onLocationPermissionGranted(true)
                }

                coarseGranted -> {
                    // Approximate location (still usable for nearby restaurants)
                    onLocationPermissionGranted(false)
                }

                else -> {
                    onLocationPermissionDenied()
                }
            }
        }

    // Optional: Background location launcher (request separately after foreground is granted)
    private val backgroundLocationLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                // Background granted
            } else {
                // Handle denial
            }
        }

    fun checkAndRequestLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
            onLocationPermissionGranted(true)
            return
        }

        // Show rationale if user previously denied (but not "Don't ask again")
        if (shouldShowRationale()) {
            showRationaleDialog()
        } else {
            // First time or "Don't ask again" already handled elsewhere
            requestLocationPermission()
        }
    }

    private fun shouldShowRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Location Permission Required")
            .setMessage("We need access to your location to show restaurants and food options near you, estimate delivery time, and provide accurate tracking.")
            .setPositiveButton("Continue") { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton("Cancel") { _, _ ->
                onLocationPermissionDenied()
            }
            .setCancelable(false)
            .show()
    }

    // Called when permission is permanently denied ("Don't ask again")
    private fun goToAppSettings() {
        AlertDialog.Builder(activity)
            .setTitle("Permission Denied")
            .setMessage("Location permission is required for core features. Please enable it in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                }
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                onLocationPermissionDenied()
            }
            .show()
    }

    // Fallback when permission is denied
    private fun onLocationPermissionDenied() {
        // Graceful degradation for food delivery app:
        // 1. Let user manually enter / select delivery address
        // 2. Show popular restaurants in major cities or default location
        // 3. Use IP-based approximate location (if you have backend support)
        // 4. Show a banner: "Enable location for better recommendations"

        showManualAddressFallback()
    }

    private fun onLocationPermissionGranted(isPrecise: Boolean) {
        // Start using FusedLocationProviderClient
        // fetchCurrentLocation()
        // loadNearbyRestaurants()
    }

    // Example fallback UI
    private fun showManualAddressFallback() {
        // Show bottom sheet / dialog / screen to enter pin code or full address
        // You can also pre-fill with last used address from SharedPreferences
        AlertDialog.Builder(activity)
            .setTitle("Enter Delivery Address")
            .setMessage("We couldn't access your location. Please enter your delivery address manually.")
            .setPositiveButton("Enter Address") { _, _ ->
                // Navigate to address input screen
            }
            .show()
    }

    // Optional: Request background location (call only after foreground granted)
    fun requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Show strong rationale first (Play Store requires justification)
                AlertDialog.Builder(activity)
                    .setTitle("Allow location all the time?")
                    .setMessage("This helps with real-time order tracking even when the app is closed.")
                    .setPositiveButton("Allow") { _, _ ->
                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                    .setNegativeButton("Not now", null)
                    .show()
            }
        }
    }
}