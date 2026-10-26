package com.example.tech.foodapp.ui.state

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech.foodapp.data.model.GeocodingEntry
import com.example.tech.foodapp.data.model.RestoListingItem
import com.example.tech.foodapp.data.model.UserLocation
import com.example.tech.foodapp.data.network.HttpClient.geoApi
import com.example.tech.foodapp.data.repository.SessionManager
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    // Lock to only allow one actor to edit shared state at a time and prevent races
    private val resolveMutex = Mutex()

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
                delay(5.minutes)
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
        .debounce(400.milliseconds)
        .distinctUntilChanged()
        .flatMapLatest { searchTerm -> // flatMapLatest for cleaner flow handling
            flow {
                if (searchTerm.length <= 2) {
                    emit(emptyList())
                    return@flow
                }

                _isLoading.value = true
                val results = try {
                    geoApi.searchLocation(searchTerm).results
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(LocationVMTag, "Geocoding search failed for '$searchTerm'", e)
                    emptyList()
                } finally {
                    _isLoading.value = false
                }
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
                    latitude = result.lat, longitude = result.lon,
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

    /** Calculates distance between two coordinates in metres */
    private fun calcDistanceDifference(oldLat: Double, oldLon: Double, currLat: Double, currLon: Double): Float {
        val out = FloatArray(1)
        Location.distanceBetween(oldLat, oldLon, currLat, currLon, out)
        return out[0]
    }

    // The UI calls this. It runs in viewModelScope, so dismissing the sheet can't abort it.
    fun resolveAndSaveLocationAsync(): Deferred<LocationResult> {
        return viewModelScope.async {
            resolveAndSaveLocation()
        }
    }


    // Returns (latitude, longitude) or null if we couldn't get a fix in time.
    // Tries a fresh high-accuracy fix first (5 s timeout), then falls back to
    // the last cached location. Returns null only if both are unavailable.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private suspend fun detectLocCoordinates(): Pair<Double, Double>? {
        val cts = CancellationTokenSource()
        return try {
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMaxUpdateAgeMillis(30_000)
                .setDurationMillis(8_000)
                .build()
            val fresh = withTimeoutOrNull(10.seconds) {
                fusedLocationClient.getCurrentLocation(request, cts.token).await()
            }
            val loc = fresh ?: fusedLocationClient.lastLocation.await()
                ?.takeIf { it.elapsedRealtimeAgeMillis < 2.minutes.inWholeMilliseconds }
            loc?.let { it.latitude to it.longitude }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LocationVMTag, "detectCoordinates failed", e); null
        } finally {
            cts.cancel()
        }
    }

    // Called externally only when resolution ends without going through
    // resolveAndSaveLocation() — i.e. the permission-denied path.
    fun markResolutionComplete() {
        _isResolvingLocation.value = false
    }

    @SuppressLint("MissingPermission")
    suspend fun resolveAndSaveLocation(): LocationResult = resolveMutex.withLock {
        _isDetecting.value = true
        try {
            val lm = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val prev: UserLocation? = sessionManager.userLocation.first()

            if (!enabled) {
                if (prev == null) sessionManager.setLocationDetails(getPuneFallback())
                return@withLock LocationResult.GpsDisabled
            }
            val coords = detectLocCoordinates()
            if (coords == null) {
                if (prev == null) sessionManager.setLocationDetails(getPuneFallback())
                return@withLock LocationResult.Failure
            }

            val sameSpot: Boolean = (prev != null) && calcDistanceDifference(
                prev.latitude, prev.longitude,
                coords.first, coords.second
            ) < 150f

            when {
                prev != null && sameSpot && prev.isAutoDetected -> Unit          // nothing to do
                prev != null && sameSpot ->                                       // manual -> auto, same place
                    sessionManager.setLocationDetails(prev.copy(isAutoDetected = true))

                else -> sessionManager.setLocationDetails(
                    UserLocation(
                        name = "Detecting...",
                        latitude = coords.first, longitude = coords.second,
                        isAutoDetected = true
                    )
                )
            }
            LocationResult.Success

        } finally {
            _isDetecting.value = false
            _isResolvingLocation.value = false
        }
    }


    suspend fun updateLocationNameFromSwiggy(restaurants: List<RestoListingItem>, forLoc: UserLocation) {
        val name = restaurants.take(5).firstNotNullOfOrNull { item ->
            listOf(item.info.locality, item.info.areaName).firstOrNull { it.isNotBlank() }
        }
        val saved = sessionManager.userLocation.first() ?: return

        // Drop the result if the user has since moved to a different or manual location
        if (!saved.isAutoDetected || saved.latitude != forLoc.latitude || saved.longitude != forLoc.longitude) return

        sessionManager.setLocationDetails(
            saved.copy(name = name ?: "Current location")
        )
    }
}