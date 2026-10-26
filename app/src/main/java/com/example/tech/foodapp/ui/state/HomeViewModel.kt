package com.example.tech.foodapp.ui.state

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech.foodapp.data.model.RestoListingItem
import com.example.tech.foodapp.data.model.UserLocation
import com.example.tech.foodapp.data.repository.RestoListRepository
import com.example.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

const val HomeVMTag = "HOME_VM"

sealed class HomeUiState {
    data class Success(
        val restoListingItems: List<RestoListingItem>,
        val isLoadingMore: Boolean = false,  // spinner at bottom of list
        val hasMore: Boolean = true
    ) : HomeUiState()

    object Loading : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

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

    // This holds a "manual" override state that loadMoreRestaurants() writes to.
    // null means "no override — use whatever the base flow produces".
    private val _uiStateOverride = MutableStateFlow<HomeUiState?>(null)

    // Track the last successfully fetched base page data
    private var lastBaseSuccessState: HomeUiState.Success? = null


    private var fetchId = 0                 // bumps on every new fetch
    private var lastFetchFailed = false     // remembers that the last fetch errored


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalCoroutinesApi::class)
    val baseUiState: Flow<HomeUiState> = combine(
        // Only re-emit when lat/lng actually changes, not on every DataStore write
        // (e.g. when we patch the name via updateLocationNameFromSwiggy)
        sessionManager.userLocation.distinctUntilChanged { old, new ->
            (old?.latitude == new?.latitude) && (old?.longitude == new?.longitude)
                    && (old?.isAutoDetected == new?.isAutoDetected) // If manual changed to detected or vice-versa
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
                Log.d(HomeVMTag, "uiState: resolution in progress — holding at Loading")
                flowOf(HomeUiState.Loading)
            }

            (location == null) -> {
                // Resolution finished but somehow no location was set (shouldn't
                // happen since every path sets a fallback, but guard anyway)
                Log.w(HomeVMTag, "uiState: resolved but location is null — holding at Loading")
                flowOf(HomeUiState.Loading)
            }

            else -> {
                val last = lastFetchedLocation
                val cached = lastBaseSuccessState
                val isManualRefresh = timestamp > lastHandledRefresh
                val isNewLocation = (last == null)
                        || (location.latitude != last.latitude) || (location.longitude != last.longitude)
                        || (location.isAutoDetected != last.isAutoDetected)
                lastHandledRefresh = timestamp
                if (isNewLocation || isManualRefresh || cached == null) {
                    fetchRestaurantsFlow(location)
                } else {
                    flowOf(cached)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val uiState: StateFlow<HomeUiState> = combine(
        baseUiState,
        _uiStateOverride
    ) { base, override ->
        // If loadMoreRestaurants() has written something, show that.
        // Else, fall through to whatever the base flow produced.
        override ?: base
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )


    fun hardRefreshRestaurants() {
        // Wipe any leftover pagination state
        _uiStateOverride.value = null
        // Clear the cache so a fresh loading lifecycle triggers properly
        lastBaseSuccessState = null
        refreshTrigger.value = System.currentTimeMillis()
    }

    fun onConnectivityRestored() {
        if (lastFetchFailed) hardRefreshRestaurants()
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun loadMoreRestaurants() {
        val currentState = uiState.value

        // Only proceed if we're in a Success state with more pages available
        if (currentState !is HomeUiState.Success) return
        if (currentState.isLoadingMore || !currentState.hasMore) return

        val location = lastFetchedLocation ?: return

        val myFetch = fetchId
        viewModelScope.launch {
            _uiStateOverride.value = currentState.copy(isLoadingMore = true)
            try {
                repository.loadNextPage(location.latitude, location.longitude)
                if (myFetch == fetchId) {
                    _uiStateOverride.value = HomeUiState.Success(
                        repository.cachedRestaurantListing.toList(),
                        false,
                        repository.hasMore
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(HomeVMTag, "loadMore failed", e)
                if (myFetch == fetchId) {
                    _uiStateOverride.value = currentState.copy(isLoadingMore = false)
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun fetchRestaurantsFlow(location: UserLocation) = flow {
        fetchId++
        _uiStateOverride.value = null  // fresh fetch — clear any override from previous pagination
        emit(HomeUiState.Loading)
        try {
            val data = repository.getHomeRestaurants(
                lat = location.latitude, lng = location.longitude,
                forceRefresh = true
            )

            lastFetchedLocation = location
            lastFetchFailed = false
            if (location.isAutoDetected) {
                locationViewModel.updateLocationNameFromSwiggy(data, location)   // now takes location
            }

            val success = HomeUiState.Success(data, isLoadingMore = false, hasMore = repository.hasMore)
            lastBaseSuccessState = success
            emit(success)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            lastFetchFailed = true
            Log.e(HomeVMTag, "fetch failed", e)
            emit(HomeUiState.Error(e.message ?: "Failed to load restaurants"))
        }
    }

}