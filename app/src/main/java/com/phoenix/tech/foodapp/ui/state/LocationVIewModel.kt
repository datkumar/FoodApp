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
import com.phoenix.tech.foodapp.data.model.RestoListingItem
import com.phoenix.tech.foodapp.data.model.UserLocation
import com.phoenix.tech.foodapp.data.network.HttpClient.geoApi
import com.phoenix.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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

private const val LocationVMTag = "LOCATION_VM"


class LocationViewModelFactory(
    private val sessionManager: SessionManager,
    private val application: Application // Use Application instead of Activity Context
) : ViewModelProvider.Factory {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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


@RequiresApi(Build.VERSION_CODES.TIRAMISU) class LocationViewModel(
    private val sessionManager: SessionManager,
    application: Application
) : AndroidViewModel(application) {
    // Detect current location (internally resolved from GPS, Wi-Fi, and last requested value)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Expose the saved location from DataStore to the UI
    val savedLocation: StateFlow<UserLocation?> = sessionManager.userLocation.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Location detection ongoing state
    // Used by LocationSelectionSheet as well as during Startup
    private val _isDetecting = MutableStateFlow(false)
    val isDetecting = _isDetecting.asStateFlow()

    // GeoApi location details fetching state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Starts as true so HomeViewModel waits before making its first fetch.
    // GPS off, or permission denied). Never goes back to true after that —
    // subsequent re-detections (manual refresh, location sheet) don't need to
    // block the UI since there's already a valid location displayed.
    // Used only during startup resolution
    private val _isResolvingLocation = MutableStateFlow(true)
    val isResolvingLocation = _isResolvingLocation.asStateFlow()

    // Search query term state
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    init {
        startPeriodicLocationUpdates()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startPeriodicLocationUpdates() {
        viewModelScope.launch {
            // Wait for startup resolution to finish before starting the loop —
            // otherwise the first tick could race with the initial detection
            isResolvingLocation.first { !it }

            while (true) {
                delay(5 * 60 * 1000L) // 5 mins

                // Only auto-refresh if the current location is auto-detected.
                // If the user manually searched for a location, respect that choice
                // and don't silently overwrite it with GPS coords.
                val current = sessionManager.userLocation.first()
                if (current?.isAutoDetected == true) {
                    Log.d(LocationVMTag, "Periodic update: triggering location refresh")
                    resolveAndSaveLocation()
                } else {
                    Log.d(
                        LocationVMTag,
                        "Periodic update: skipping — location is manual ('${current?.name}')"
                    )
                }
            }
        }
    }

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
                    Log.e(LocationVMTag, "Geocoding search failed for '$searchTerm'", e)
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
                    isAutoDetected = false // User-selected NOT Detected
                )
            )
            // Clear search state after selection
            _query.value = ""
        }
    }

    fun getPuneFallback() = UserLocation(
        name = "Pune, Maharashtra",
        latitude = 18.5204,
        longitude = 73.8567,
        isAutoDetected = false
    )


    // Updates the location name in DataStore after HomeViewModel fetches Swiggy restaurants.
    // Swiggy returns locality/areaName/city in each restaurant item — we use the first
    // non-empty value so we never need a reverse-geocoding call for auto-detected locations.
    suspend fun updateLocationNameFromSwiggy(restaurants: List<RestoListingItem>) {
        // Walk the first few items to find a non-blank locality or areaName
        val nameFromSwiggy = restaurants
            .take(5)
            .firstNotNullOfOrNull { item ->
                listOf(
                    item.info.locality, item.info.areaName,
                ).firstOrNull {
                    it.isNotBlank()
                }
            }

        if (nameFromSwiggy != null) {
            Log.d(LocationVMTag, "Updating location name from Swiggy: $nameFromSwiggy")
            // Preserve coords and isAutoDetected — only patch the display name
            val userLoc = sessionManager.userLocation.first()
            if (userLoc != null) {
                // Coordinates already present, just update name
                sessionManager.setLocationDetails(
                    userLoc.copy(name = nameFromSwiggy)
                )
            } else {
                Log.w(
                    LocationVMTag,
                    "updateLocationNameFromSwiggy: no saved location to patch name onto"
                )
            }
        } else {
            // Swiggy returned no usable locality strings — keep whatever name is already stored
            Log.w(
                LocationVMTag,
                "updateLocationNameFromSwiggy: no locality/areaName found in first 5 items"
            )
        }
    }


    // Returns (latitude, longitude) or null if we couldn't get a fix in time.
    // Tries a fresh high-accuracy fix first (5 s timeout), then falls back to
    // the last cached location. Returns null only if both are unavailable.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private suspend fun detectLocCoordinates(): Pair<Double, Double>? {
        return try {
            val cts = CancellationTokenSource()
            // Fresh location — can be null if the device hasn't got a satellite lock yet
            val freshLoc = withTimeoutOrNull(5_000) {
                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .await()
            }
            // Cached last-known position as a fallback
            val cachedLoc = fusedLocationClient.lastLocation.await()
            // Resolved final location result
            val finalLoc = freshLoc ?: cachedLoc
            if (finalLoc != null) {
                val ageSeconds = (finalLoc.elapsedRealtimeAgeMillis / 1000.0).roundToInt()
                Log.d(
                    LocationVMTag,
                    "detectCoordinates: fresh=${freshLoc != null}, " +
                            "cacheAge=${ageSeconds}s — using (${finalLoc.latitude}, ${finalLoc.longitude})"
                )
                finalLoc.latitude to finalLoc.longitude
            } else {
                // Neither a fresh fix nor a cached position is available.
                // This can happen on first boot, in airplane mode, or on an emulator.
                Log.w(LocationVMTag, "detectCoordinates: both fresh and cached locations are null")
                null
            }
        } catch (e: Exception) {
            Log.e(LocationVMTag, "detectCoordinates: exception during location fetch", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    suspend fun resolveAndSaveLocation(): LocationResult {
        _isDetecting.value = true

        // 1. Access LocationManager via getApplication()
        val context = getApplication<Application>()
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 2. Check if Location Services (GPS/Network) are toggled ON in Android Settings
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            Log.w(LocationVMTag, "resolveAndSaveLocation: GPS/Network provider disabled")
            // Only fall back to Pune if there is nothing saved yet — don't wipe a
            // previously chosen manual location just because GPS is currently off.
            val userLoc = sessionManager.userLocation.first()
            if (userLoc == null) {
                Log.d(LocationVMTag, "No saved location — applying Pune fallback")
                sessionManager.setLocationDetails(getPuneFallback())
            } else {
                Log.d(
                    LocationVMTag,
                    "GPS off but location already saved (${userLoc.name}) — keeping it"
                )
            }
            _isResolvingLocation.value = false; _isDetecting.value = false
            return LocationResult.GpsDisabled
        }

        val currLocCoord = detectLocCoordinates()

        if (currLocCoord != null) {
            // Save with placeholder name. HomeViewModel will overwrite the name with
            // the locality returned by Swiggy once the restaurant fetch completes.
            sessionManager.setLocationDetails(
                UserLocation(
                    name = "Detecting...",
                    latitude = currLocCoord.first,
                    longitude = currLocCoord.second,
                    isAutoDetected = true
                )
            )
            Log.d(
                LocationVMTag,
                "resolveAndSaveLocation: saved coords (${currLocCoord.first}, ${currLocCoord.second})"
            )
            _isResolvingLocation.value = false; _isDetecting.value = false
            return LocationResult.Success
        } else {
            Log.w(LocationVMTag, "resolveAndSaveLocation: coordinate detection failed")
            val current = sessionManager.userLocation.first()
            if (current == null) {
                Log.d(LocationVMTag, "No saved location — applying Pune fallback after failure")
                sessionManager.setLocationDetails(getPuneFallback())
            } else {
                Log.d(
                    LocationVMTag,
                    "Detection failed but prior location exists (${current.name}) — keeping it"
                )
            }
            _isResolvingLocation.value = false; _isDetecting.value = false
            return LocationResult.Failure
        }

    }

    // Called externally only when resolution ends without going through
    // resolveAndSaveLocation() — i.e. the permission-denied path.
    fun markResolutionComplete() {
        _isResolvingLocation.value = false
    }

}