package com.phoenix.tech.foodapp.ui.state

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.phoenix.tech.foodapp.data.model.GeocodingEntry
import com.phoenix.tech.foodapp.data.model.UserLocation
import com.phoenix.tech.foodapp.data.network.HttpClient.geoApi
import com.phoenix.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

//val LocationVMTag = "LOCATION_VM"

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


class LocationViewModel(
    private val sessionManager: SessionManager,
    application: Application
) : AndroidViewModel(application) {
    // Detect current location (internally resolved from GPS, Wi-Fi, and last requested value)
    private val fusedLocationClient = LocationServices
        .getFusedLocationProviderClient(application)

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
    private val _results = MutableStateFlow<List<GeocodingEntry>>(emptyList())
    val results = _results.asStateFlow()

    init {
        setupSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _query
                .debounce(400) // Wait 500ms after the last character typed
                .filter { it.length > 2 } // Only search if 2+ characters
                .distinctUntilChanged() // Don't search if the query is the same as last time
                .collect { debouncedQuery ->
                    performSearch(debouncedQuery)
                }
        }
    }


    private suspend fun performSearch(searchTerm: String) {
        _isLoading.value = true
        try {
            // Directly call the API here
            val response = geoApi.searchLocation(searchTerm)
            _results.value = response.results
        } catch (e: Exception) {
            _results.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isEmpty()) {
            _results.value = emptyList()
        }
    }

    private fun searchLocations(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = geoApi.searchLocation(query)
                _results.value = response.results
            } catch (e: Exception) {
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
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
        }
    }

    // Called from the UI button or the Heartbeat
    @SuppressLint("MissingPermission")
    fun resolveAndSaveLocation(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isDetecting.value = true
            val hasPermission = ContextCompat.checkSelfPermission(
                getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                val detected = fetchLocationInternal()
                if (detected != null) {
                    sessionManager.setLocationDetails(detected)
                } else if (savedLocation.value == null) {
                    sessionManager.setLocationDetails(getPuneFallback())
                }
            } else if (savedLocation.value == null) {
                sessionManager.setLocationDetails(getPuneFallback())
            }
            _isDetecting.value = false
            onComplete?.invoke()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLocationInternal(): UserLocation? {
        try {
            val loc = fusedLocationClient.lastLocation.await() ?: return null
            val response = geoApi.searchByCoordinates(
                loc.latitude, loc.longitude
            )
            val locName = response.results.firstOrNull()?.formattedName ?: "Current Location"
            return UserLocation(
                name = locName,
                latitude = loc.latitude,
                longitude = loc.longitude,
                isAutoDetected = true
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun getPuneFallback() = UserLocation(
        name = "Pune, Maharashtra",
        latitude = 18.5204,
        longitude = 73.8567,
        isAutoDetected = false
    )

//    fun resolveAndSaveLocation() {
//        viewModelScope.launch {
//            val hasPermission = ContextCompat.checkSelfPermission(
//                getApplication(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//
//            // Logic: If permission is granted, try to get coordinates.
//            // If that fails OR if no permission, use Pune.
//            val resolvedLoc = if (hasPermission) {
//                fetchCurrentLocation() ?: getPuneFallback()
//            } else {
//                getPuneFallback()
//            }
//
//            sessionManager.setLocationDetails(resolvedLoc)
//        }
//    }

//    @SuppressLint("MissingPermission")
//    private suspend fun fetchCurrentLocation(): UserLocation? {
//        return try {
//            // .await() now works because of the library mentioned above
//            val loc = fusedLocationClient.lastLocation.await()
//
//            if (loc != null) {
//                // Call your GeoApi
//                val response = geoApi.searchByCoordinates(
//                    lat = loc.latitude,
//                    lon = loc.longitude
//                )
//
//                val bestResult = response.results.firstOrNull()
//                val addressName = bestResult?.formattedName ?: "Current Location"
//
//                UserLocation(
//                    name = addressName,
//                    latitude = loc.latitude,
//                    longitude = loc.longitude,
//                    isAutoDetected = true
//                )
//            } else null
//        } catch (e: Exception) {
//            null // Fallback logic in resolveAndSaveLocation will catch this null
//        }
//    }

}


//@OptIn(FlowPreview::class)
//class LocationViewModel(
//    private val sessionManager: SessionManager,
//) : ViewModel() {
//    private val _query = MutableStateFlow("")
//    val query = _query.asStateFlow()
//
//    private val _results = MutableStateFlow<List<GeocodingEntry>>(emptyList())
//    val results = _results.asStateFlow()
//
//    private val _loading = MutableStateFlow(false)
//    val loading = _loading.asStateFlow()
//
//    // For waiting while Device location is being detected
//    private val _isDetecting = MutableStateFlow(false)
//    val isDetecting = _isDetecting.asStateFlow()
//
//    val savedLocation = sessionManager.userLocation.stateIn(
//        viewModelScope, SharingStarted.Eagerly, null
//    )
//
//    init {
//        viewModelScope.launch {
//            _query.debounce(600)
//                .filter { it.trim().length >= 3 }
//                .distinctUntilChanged()
//                .collect { performSearch(it) }
//        }
//    }
//
//    fun onQueryChange(newQuery: String) {
//        _query.value = newQuery
//    }
//
//    private suspend fun performSearch(text: String) {
//        _loading.value = true
//        try {
//            val response = HttpClient.geoApi.searchLocation(text)
//            _results.value = response.results
//        } catch (e: Exception) {
//            _results.value = emptyList()
//        } finally {
//            _loading.value = false
//        }
//    }
//
//    fun selectLocation(result: GeocodingEntry) {
//        viewModelScope.launch {
//            sessionManager.setLocationDetails(
//                UserLocation(
//                    name = result.formattedName,
//                    latitude = result.lat,
//                    longitude = result.lon,
//                    isAutoDetected = false
//                )
//            )
//        }
//    }
//
//    // In LocationViewModel
//    fun fetchCurrentLocation(
//        context: Context,
//        onComplete: () -> Unit
//    ) {
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//        _isDetecting.value = true // Start specific loading
//
//        try {
//            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                if (location != null) {
//                    Log.d(LocationVMTag, "LOCATION COORDINATES DETECTED")
//                    viewModelScope.launch {
//                        // Call the API to get the ACTUAL name for these coordinates
//                        performReverseGeocode(location.latitude, location.longitude)
//                        _isDetecting.value = false
//                        onComplete() // Dismiss sheet only AFTER API returns
//                    }
//                } else {
//                    _isDetecting.value = false
//                    // Optional: Handle case where GPS is off
//                }
//            }.addOnFailureListener {
//                _isDetecting.value = false
//            }
//        } catch (e: SecurityException) {
//            _isDetecting.value = false
//        }
//    }
//
//    private suspend fun performReverseGeocode(lat: Double, lon: Double) {
//        try {
//            // This makes the network call to turn coordinates into a name
//            val response = HttpClient.geoApi.searchByCoordinates(lat, lon)
//            val result = response.results.firstOrNull()
//            Log.d(LocationVMTag, result.toString())
//
//            if (result != null) {
//                Log.d(LocationVMTag, result.toString())
//                // SAVE TO SESSION MANAGER
//                sessionManager.setLocationDetails(
//                    UserLocation(
//                        name = result.formattedName,
//                        latitude = lat,
//                        longitude = lon,
//                        isAutoDetected = true
//                    )
//                )
//            } else {
//                Log.d(LocationVMTag, "REV. GEOCODING gave NULL RESPONSE")
//            }
//        } catch (e: Exception) {
//            Log.d(LocationVMTag, "ERROR DURING REVERSE GEOCODING")
//            Log.d(LocationVMTag, e.message.toString())
//            // Fallback: if API fails, save coordinates with a placeholder name
//            sessionManager.setLocationDetails(
//                UserLocation(
//                    "Unknown Location",
//                    lat, lon,
//                    true
//                )
//            )
//        }
//    }
//}