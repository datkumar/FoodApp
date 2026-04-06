package com.phoenix.tech.foodapp.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phoenix.tech.foodapp.data.model.UserLocation
import com.phoenix.tech.foodapp.data.repository.RestoListRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.navigation.MainLayoutPermsTag
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
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class HomeViewModel(
    private val sessionManager: SessionManager,
    private val repository: RestoListRepository = RestoListRepository()
) : ViewModel() {
    // Unique timestamp to force a refresh
    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    // Track the last location we successfully fetched for to prevent duplicates
    private var lastFetchedLocation: UserLocation? = null

    // Initialize with the current time so the FIRST load
    // doesn't "force" unless the repository says it's a new location.
    private var lastHandledRefresh = refreshTrigger.value

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        sessionManager.userLocation.distinctUntilChanged { old, new ->
            // Ignore metadata changes. Only react to actual coordinate shifts.
            old?.latitude == new?.latitude && old?.longitude == new?.longitude
        },
        refreshTrigger
    ) { location, timestamp ->
        location to timestamp
    }
        .flatMapLatest { (location, timestamp) ->
            if (location == null) {
                // Stay in Loading state while waiting for permission/detection
                // we have Pune fallback if location is null
                flowOf(HomeUiState.Loading)
            } else {
                val isManualRefresh = timestamp > lastHandledRefresh
                // Check if we actually need a network call
                val isNewLocation = location.latitude != lastFetchedLocation?.latitude ||
                        location.longitude != lastFetchedLocation?.longitude

                lastHandledRefresh = timestamp
                Log.d(
                    MainLayoutPermsTag,
                    "#### isNewLocation" + isNewLocation + "isManualRefresh" + isManualRefresh
                )
                // FIX: Only fetch if it's a brand new location OR a manual pull-to-refresh
                if (isNewLocation || isManualRefresh) {
                    fetchRestaurantsFlow(location)
                } else {
                    // Keep the current success state if location hasn't actually changed
                    // This prevents the "Pune -> Real Location" double-flash
                    flowOf(uiState.value)
                }


//                // Check if this specific emission was caused by a call to refreshRestaurants()
//                val isManualRefresh = timestamp > lastHandledRefresh
//
//                // Update the tracker for the next emission
//                lastHandledRefresh = timestamp
//
//                fetchRestaurantsFlow(
//                    lat = location.latitude,
//                    lng = location.longitude,
//                    forceRefresh = isManualRefresh
//                )
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
                forceRefresh = true // Usually true if we reached this point
            )
            lastFetchedLocation = location
            emit(HomeUiState.Success(data))
        } catch (e: Exception) {
            emit(HomeUiState.Error(e.message ?: "Failed to load restaurants"))
        }
    }

//    private fun fetchRestaurantsFlow(
//        lat: Double, lng: Double,
//        forceRefresh: Boolean
//    ) = flow {
//        emit(HomeUiState.Loading)
//        try {
//            val data = repository.getHomeRestaurants(lat, lng, forceRefresh)
//            emit(HomeUiState.Success(data))
//        } catch (e: Exception) {
//            emit(HomeUiState.Error(e.message ?: "Failed to load restaurants"))
//        }
//    }
}