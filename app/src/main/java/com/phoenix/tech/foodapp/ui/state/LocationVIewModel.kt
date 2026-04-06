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
    val savedLocation: StateFlow<UserLocation?> = sessionManager.userLocation.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Location detection ongoing state
    private val _isDetecting = MutableStateFlow(false)
    val isDetecting = _isDetecting.asStateFlow()

    // GeoApi location details fetching state
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
    suspend fun resolveAndSaveLocation(forceRefresh: Boolean = false): LocationResult {
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
            _isDetecting.value = false
            return LocationResult.GpsDisabled
        }

        val currLocCoord = detectLocCoordinates()

        return if (currLocCoord != null) {
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
            _isDetecting.value = false
            LocationResult.Success
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
            _isDetecting.value = false
            LocationResult.Failure
        }

    }


    /**
     * Checks if the current saved location is the "Pune Fallback".
     * We use this to determine if we should try to replace it with real GPS data.
     */
    fun isAtFallback(location: UserLocation?): Boolean {
        if (location == null) {
            return true
        }
        val fallback = getPuneFallback()
        return (location.latitude == fallback.latitude &&
                location.longitude == fallback.longitude)
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
}