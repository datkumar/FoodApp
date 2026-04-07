package com.phoenix.tech.foodapp.ui.state

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
    sessionManager: SessionManager,
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


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        // Only re-emit when lat/lng actually changes, not on every DataStore write
        // (e.g. when we patch the name via updateLocationNameFromSwiggy)
        sessionManager.userLocation.distinctUntilChanged { old, new ->
            old?.latitude == new?.latitude && old?.longitude == new?.longitude
        },
        refreshTrigger,
        locationViewModel.isResolvingLocation
    ) { location, timestamp, isResolving ->
        Triple(location, timestamp, isResolving)
    }.flatMapLatest { (location, timestamp, isResolving) ->

        when {
            // Still waiting for startup permission/GPS resolution to finish —
            // hold at Loading so we don't fetch with stale coords
            isResolving -> {
                Log.d(
                    HomeVMTag,
                    "uiState: resolution in progress — holding at Loading"
                )
                flowOf(HomeUiState.Loading)
            }

            (location == null) -> {
                // Resolution finished but somehow no location was set (shouldn't
                // happen since every path sets a fallback, but guard anyway)
                Log.w(
                    HomeVMTag,
                    "uiState: resolved but location is null — holding at Loading"
                )
                flowOf(HomeUiState.Loading)
            }

            else -> {
                val isManualRefresh = timestamp > lastHandledRefresh
                // Check if we actually need a network call
                val isNewLocation = location.latitude != lastFetchedLocation?.latitude ||
                        location.longitude != lastFetchedLocation?.longitude
                lastHandledRefresh = timestamp

                if (isNewLocation || isManualRefresh) {
                    Log.d(
                        HomeVMTag,
                        "uiState: fetching — newLocation=$isNewLocation, manualRefresh=$isManualRefresh"
                    )
                    fetchRestaurantsFlow(location)
                } else {
                    Log.d(
                        HomeVMTag,
                        "uiState: skipping fetch — location and trigger unchanged"
                    )
                    flowOf(uiState.value)
                }
            }
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    fun refreshRestaurants() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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