package com.phoenix.tech.foodapp.ui.state

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.phoenix.tech.foodapp.data.model.GeocodingEntry
import com.phoenix.tech.foodapp.data.model.UserLocation
import com.phoenix.tech.foodapp.data.network.HttpClient.geoApi
import com.phoenix.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt

val LocationVMTag = "LOCATION_VM"


class LocationViewModelFactory(
    private val sessionManager: SessionManager,
    private val application: Application // Use Application instead of Activity Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(sessionManager, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class LocationResult {
    object Success : LocationResult()
    object GpsDisabled : LocationResult() // GPS hardware is OFF
    object PermissionDenied : LocationResult()
    object Failure : LocationResult()
}


class LocationViewModel(
    private val sessionManager: SessionManager,
    application: Application
) : AndroidViewModel(application) {
    // Detect current location (internally resolved from GPS, Wi-Fi, and last requested value)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Expose the saved location from DataStore to the UI
    val savedLocation = sessionManager.userLocation.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Location detection loading state
    private val _isDetecting = MutableStateFlow(false)
    val isDetecting = _isDetecting.asStateFlow()

    // GeoApi location fetch loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Search query term state
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    // Location search results state
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<GeocodingEntry>> = _query
        .debounce(400)
        .distinctUntilChanged()
        .flatMapLatest { searchTerm -> // Use flatMapLatest for cleaner flow handling
            flow {
                if (searchTerm.length <= 2) {
                    emit(emptyList())
                    return@flow
                }

                _isLoading.value = true
                val results = try {
                    geoApi.searchLocation(searchTerm).results
                } catch (e: Exception) {
                    emptyList()
                }
                _isLoading.value = false
                emit(results)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun selectLocation(result: GeocodingEntry) {
        viewModelScope.launch {
            sessionManager.setLocationDetails(
                UserLocation(
                    name = result.formattedName,
                    latitude = result.lat,
                    longitude = result.lon,
                    isAutoDetected = false
                )
            )
            // Clear search state after selection
            _query.value = ""
        }
    }


    /** * Checks if the current saved location is the "Pune Fallback".
     * We use this to determine if we should try to replace it with real GPS data.
     */
    fun isAtFallback(location: UserLocation?): Boolean {
        val fallback = getPuneFallback()
        if (location == null) return true
        return (location.latitude == fallback.latitude && location.longitude == fallback.longitude)
    }

    /**
     * Determines if we should attempt an auto-refresh.
     * We only auto-refresh if:
     * 1. The current location is null or the Pune Fallback.
     * 2. OR the current location was previously auto-detected (and might be stale).
     */
    fun shouldAutoUpdateLoc(currentLoc: UserLocation?): Boolean {
        return isAtFallback(currentLoc) || currentLoc?.isAutoDetected == true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    suspend fun resolveAndSaveLocation(forceRefresh: Boolean = false): LocationResult {
        _isDetecting.value = true

        // Use first() to get the snapshot immediately for the check
        val currSavedLoc = sessionManager.userLocation.first()

        // 1. Access LocationManager via getApplication()
        val context = getApplication<Application>()
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 2. Check if Location Services (GPS/Network) are toggled ON in Android Settings
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnabled) {
            _isDetecting.value = false
            // Fallback to Pune if we have nothing saved, so the app isn't broken
            if (currSavedLoc == null) {
                sessionManager.setLocationDetails(getPuneFallback())
            }
            return LocationResult.GpsDisabled
        }

        // 3. Proceed with actual detection
        val detected = detectLocationAndFetchDetails() // Your existing FusedLocation logic

        return if (detected != null) {
            sessionManager.setLocationDetails(detected)
            _isDetecting.value = false
            LocationResult.Success
        } else {
            // If detection fails (e.g. timeout), set Pune as fallback
            if (currSavedLoc == null) {
                sessionManager.setLocationDetails(getPuneFallback())
            }
            _isDetecting.value = false
            LocationResult.Failure
        }


//        // A location is "Truly Manual" only if it's NOT Pune and NOT AutoDetected
//        val isRealManualLocation = currSavedLoc != null &&
//                !currSavedLoc.isAutoDetected &&
//                !isAtFallback(currSavedLoc)
//
//        if (isRealManualLocation && !forceRefresh) {
//            Log.d(LocationVMTag, "Manual location detected. Aborting auto-refresh.")
//            _isDetecting.value = false
//            return true
//        }
//
//        return try {
//            val detectedLocDetails = detectLocationAndFetchDetails()
//            if (detectedLocDetails != null) {
//                sessionManager.setLocationDetails(detectedLocDetails)
//                true
//            } else {
//                // If detection fails and we have nothing, set Pune
//                if (currSavedLoc == null) {
//                    sessionManager.setLocationDetails(getPuneFallback())
//                }
//                false
//            }
//        } catch (e: Exception) {
//            Log.e(LocationVMTag, "Error in resolveAndSaveLocation", e)
//            false
//        } finally {
//            _isDetecting.value = false
//        }


//        val context = getApplication<Application>()
//        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        // 1. Manual Check
//        val isPune = currSavedLoc?.latitude == fallback.latitude &&
//                currSavedLoc.longitude == fallback.longitude
//        val isManual = currSavedLoc != null && !currSavedLoc.isAutoDetected && !isPune
//
//        // NEW LOGIC: Only abort if it's manual AND we aren't being forced to refresh
//        if (isManual && !forceRefresh) {
//            Log.d(LocationVMTag, "Manual location detected. Aborting auto-refresh.")
//            _isDetecting.value = false
//            return true
//        }
//        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//
//        val hasPermission = ContextCompat.checkSelfPermission(
//            context, Manifest.permission.ACCESS_COARSE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//
//        var didSucceed = false
//
//        try {
//            if (hasPermission && isGpsEnabled) {
//                val detectedLocDetails = detectLocationAndFetchDetails()
//                if (detectedLocDetails != null) {
//                    sessionManager.setLocationDetails(detectedLocDetails)
//                    didSucceed = true
//                } else if (currSavedLoc == null) {
//                    sessionManager.setLocationDetails(fallback)
//                }
//            } else if (currSavedLoc == null) {
//                sessionManager.setLocationDetails(fallback)
//            }
//        } catch (e: Exception) {
//            Log.e(LocationVMTag, "Error in resolveAndSaveLocation", e)
//        } finally {
//            _isDetecting.value = false
//        }
//        return didSucceed
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private suspend fun detectLocationAndFetchDetails(): UserLocation? {
        try {
            // Create a token that we can use for this specific one-shot request
            val cts = CancellationTokenSource()
            // Fetch current latest location (with 5s timeout)
            val currLoc = withTimeoutOrNull(5000) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()
            }
            // Fallback to cached if fresh failed
            val cachedLoc = fusedLocationClient.lastLocation.await()

            // Pick the best available: Fresh > Cached > Null
            val finalLoc = currLoc ?: cachedLoc ?: return null
            Log.d(LocationVMTag, "CURRENT LOCATION: $currLoc")
            Log.d(LocationVMTag, "CACHED LOCATION:  $cachedLoc")
            // Safe logging for debugging
            val age = cachedLoc?.let {
                (it.elapsedRealtimeAgeMillis / 1000.0).roundToInt()
            } ?: "N/A"
            Log.d(
                LocationVMTag,
                "Using location which is Fresh: ${currLoc != null}, Cached Age: $age sec"
            )
            val response = geoApi.searchByCoordinates(
                lat = finalLoc.latitude,
                lon = finalLoc.longitude
            )
            val locName = response.results.firstOrNull()?.formattedName ?: "Current Location"
            return UserLocation(
                name = locName,
                latitude = finalLoc.latitude,
                longitude = finalLoc.longitude,
                isAutoDetected = true
            )
        } catch (e: Exception) {
            Log.e(LocationVMTag, "Detection failed", e)
            return null
        }
    }

    fun getPuneFallback() = UserLocation(
        name = "Pune, Maharashtra",
        latitude = 18.5204,
        longitude = 73.8567,
        isAutoDetected = false
    )
}