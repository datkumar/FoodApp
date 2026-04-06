package com.phoenix.tech.foodapp.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phoenix.tech.foodapp.data.model.UserLocation
import com.phoenix.tech.foodapp.data.repository.RestoListRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class HomeViewModelFactory(
    private val sessionManager: SessionManager,
    private val locationViewModel: LocationViewModel   // injected so HomeVM can patch the name
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(sessionManager, locationViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

const val HomeVMTag = "HOME_VM"

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val locationViewModel: LocationViewModel,
    private val repository: RestoListRepository = RestoListRepository()
) : ViewModel() {

    // Bumped-up whenever the user manually initiates refresh
    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    // The lat/lng of the last successfully fetched restaurant list.
    // Used to skip redundant network calls when neither location nor refresh trigger changed.
    private var lastFetchedLocation: UserLocation? = null

    // Tracks the timestamp we handled last so we can distinguish a brand-new
    // manual refresh from a re-emission of the same timestamp after resubscription.
    private var lastHandledRefresh = refreshTrigger.value


    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        // Only re-emit when lat/lng actually changes, not on every DataStore write
        // (e.g. when we patch the name via updateLocationNameFromSwiggy)
        sessionManager.userLocation.distinctUntilChanged { old, new ->
            old?.latitude == new?.latitude && old?.longitude == new?.longitude
        },
        refreshTrigger
    ) { location, timestamp -> location to timestamp }
        .flatMapLatest { (location, timestamp) ->
            if (location == null) {
                // Location not yet resolved — show spinner, don't fetch yet
                Log.d(HomeVMTag, "uiState: location is null, waiting...")
                flowOf(HomeUiState.Loading)
            } else {
                val isManualRefresh = timestamp > lastHandledRefresh
                // Check if we actually need a network call
                val isNewLocation = location.latitude != lastFetchedLocation?.latitude ||
                        location.longitude != lastFetchedLocation?.longitude

                // Advance the cursor regardless of whether we fetch, so the same
                // timestamp doesn't re-trigger a fetch on the next combine emission.
                lastHandledRefresh = timestamp

                if (isNewLocation || isManualRefresh) {
                    Log.d(
                        HomeVMTag,
                        "uiState: fetching — newLocation=$isNewLocation, manualRefresh=$isManualRefresh"
                    )
                    fetchRestaurantsFlow(location)
                } else {
                    // Coords and refresh trigger are unchanged — reuse existing state
                    Log.d(
                        HomeVMTag,
                        "uiState: skipping fetch — location and trigger unchanged"
                    )
                    flowOf(uiState.value)
                }

            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun refreshRestaurants() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    private fun fetchRestaurantsFlow(location: UserLocation) = flow {
        emit(HomeUiState.Loading)
        try {
            val data = repository.getHomeRestaurants(
                location.latitude,
                location.longitude,
                forceRefresh = true
            )
            lastFetchedLocation = location

            // If this was an auto-detected location, use swiggyApi's
            // "locality" string as the display name rather than a reverse-geocoding call.
            // For user-searched locations the name already came from the GeoAPI and should be preserved
            if (location.isAutoDetected) {
                locationViewModel.updateLocationNameFromSwiggy(data)
            } else {
                Log.d(
                    "HOME_VM",
                    "fetchRestaurantsFlow: manual location — keeping name '${location.name}'"
                )
            }

            emit(HomeUiState.Success(data))

        } catch (e: Exception) {

            Log.e(HomeVMTag, "fetchRestaurantsFlow: network error", e)
            emit(HomeUiState.Error(e.message ?: "Failed to load restaurants"))
        }
    }

}