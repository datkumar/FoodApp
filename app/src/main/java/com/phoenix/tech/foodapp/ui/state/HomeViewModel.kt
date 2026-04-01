package com.phoenix.tech.foodapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phoenix.tech.foodapp.data.repository.RestoListRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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


//class HomeViewModel(
//    private val sessionManager: SessionManager,
//    private val repository: RestoListRepository = RestoListRepository()
//) : ViewModel() {
//
//    // 1. Observe Location from SessionManager
//    val userLocation = sessionManager.userLocation.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = null
//    )
//
//    // 2. Transform Location into PagingData
//    // flatMapLatest ensures that if location changes, the old pager is cancelled
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val restoPagingFlow = userLocation.flatMapLatest { loc ->
//        if (loc == null) {
//            flowOf(PagingData.empty())
//        } else {
//            repository.getRestoPagingFlow(
//                loc.latitude, loc.longitude
//            )
//        }
//    }.cachedIn(viewModelScope) // Crucial: survives config changes
//
//    init {
//        checkInitialLocation()
//    }
//
//    private fun checkInitialLocation() {
//        viewModelScope.launch {
//            if (sessionManager.userLocation.firstOrNull() == null) {
//                setDefaultLocation()
//            }
//        }
//    }
//
//    private suspend fun setDefaultLocation() {
//        val defaultLoc = UserLocation(
//            name = "Pune, Maharashtra",
//            latitude = 18.5204,
//            longitude = 73.8567,
//            isAutoDetected = false
//        )
//        sessionManager.setLocationDetails(defaultLoc)
//    }
//}

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val repository: RestoListRepository = RestoListRepository()
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
        .apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        sessionManager.userLocation,
        refreshTrigger
    ) { location, _ -> location }
        .flatMapLatest { location ->
            if (location == null) {
                flowOf(HomeUiState.Error("No location selected"))
            } else {
                fetchRestaurantsFlow(location.latitude, location.longitude)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )


    private fun fetchRestaurantsFlow(lat: Double, lng: Double) = flow {
        emit(HomeUiState.Loading)
        try {
            val data = repository.getHomeRestaurants(lat, lng)
            emit(HomeUiState.Success(data))
        } catch (e: Exception) {
            emit(HomeUiState.Error(e.message ?: "Failed to load restaurants"))
        }
    }

    fun refreshRestaurants() {
        refreshTrigger.tryEmit(Unit)
    }


//    // 1. Transform the DataStore Flow into a StateFlow
//    // This solves the ".value" error and gives the VM a constant "peek" at the location
//    val userLocation = sessionManager.userLocation.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = null
//    )

//    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
//    val uiState = _uiState.asStateFlow()

//    init {
//        // Start the observer immediately
//        observeLocationUpdates()
//        checkInitialLocation()
//    }

//    private fun checkInitialLocation() {
//        viewModelScope.launch {
//            // Wait a moment for DataStore to emit the first value
//            val current = sessionManager.userLocation.firstOrNull()
//
//            if (current == null) {
//                // Option A: Trigger Auto-Detect (Requires Context/Permission check)
//                // Option B: Set a Default Fallback (Recommended for first-run UX)
//                setDefaultLocation()
//            }
//        }
//    }
//
//    private suspend fun setDefaultLocation() {
//        // Set a popular default (e.g., your Pune coordinates)
//        // so the user sees food immediately instead of an empty screen
//        val defaultLoc = UserLocation(
//            name = "Pune, Maharashtra",
//            latitude = 18.5204,
//            longitude = 73.8567,
//            isAutoDetected = false
//        )
//        sessionManager.setLocationDetails(defaultLoc)
//    }
//
//    private fun observeLocationUpdates() {
//        viewModelScope.launch {
//            userLocation.collect { loc ->
//                if (loc != null) {
//                    // Automatically fetch whenever location changes
//                    fetchRestaurants(loc.latitude, loc.longitude)
//                } else {
//                    // Show a specific state if no location is set yet
//                    _uiState.value =
//                        HomeUiState.Error("Please select a location to see restaurants")
//                }
//            }
//        }
//    }
//
//    private suspend fun fetchRestaurants(
//        lat: Double, lng: Double, forceRefresh: Boolean = false
//    ) {
//        _uiState.value = HomeUiState.Loading
//        try {
//            val restaurants = repository.getHomeRestaurants(
//                lat = lat,
//                lng = lng,
//                forceRefresh = forceRefresh
//            )
//            _uiState.value = HomeUiState.Success(restaurants)
//        } catch (e: Exception) {
//            _uiState.value = HomeUiState.Error(e.message ?: "Failed to load restaurants")
//        }
//    }
//
//    fun refreshRestaurants() {
//        // 3. Use the .value we gained from stateIn
//        val currentLoc = userLocation.value
//        if (currentLoc != null) {
//            viewModelScope.launch {
//                fetchRestaurants(
//                    lat = currentLoc.latitude,
//                    lng = currentLoc.longitude,
//                    forceRefresh = true
//                )
//            }
//        }
//    }
}
