package com.example.tech.foodapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech.foodapp.data.local.entities.CartItemEntity
import com.example.tech.foodapp.data.local.entities.RestaurantEntity
import com.example.tech.foodapp.data.model.RestoDetailsResult
import com.example.tech.foodapp.data.repository.CartRepository
import com.example.tech.foodapp.data.repository.RestoDetailsRepository
import com.example.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed class RestoDetailsUiState {
    object Loading : RestoDetailsUiState()

    data class Success(
        val restoDetailsResult: RestoDetailsResult
    ) : RestoDetailsUiState()

    data class Error(val message: String) : RestoDetailsUiState()
}

class RestoDetailsViewModelFactory(
    private val cartRepository: CartRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RestoDetailsViewModel(cartRepository, sessionManager) as T
    }
}

class RestoDetailsViewModel(
    private val cartRepository: CartRepository,
    private val sessionManager: SessionManager,
    private val apiRepository: RestoDetailsRepository = RestoDetailsRepository()
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<RestoDetailsUiState>(RestoDetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // Observe the database (returns Flow<List<CartItemEntity>>)
    val cartItems = cartRepository.allCartItems.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun fetchRestaurantDetails(restoId: String) {
        viewModelScope.launch {
            _uiState.value = RestoDetailsUiState.Loading
            try {
                // 1. Get the current location from DataStore (asynchronously)
                val location = sessionManager.userLocation.firstOrNull()

                if (location != null) {
                    // 2. Pass ID + Lat + Lng to the repository
                    val restoDetailsResult = apiRepository.getRestaurantDetails(
                        restoId = restoId,
                        lat = location.latitude,
                        lng = location.longitude
                    )

                    _uiState.value = if (restoDetailsResult != null) {
                        RestoDetailsUiState.Success(restoDetailsResult)
                    } else {
                        RestoDetailsUiState.Error("Data empty")
                    }
                } else {
                    _uiState.value = RestoDetailsUiState.Error("Location not set")
                }
            } catch (e: Exception) {
                _uiState.value = RestoDetailsUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    // Mutation: Incr/Decr/Set
    fun updateCart(
        restaurant: RestaurantEntity,
        item: CartItemEntity,
        newQty: Int
    ) {
        viewModelScope.launch {
            if (newQty <= 0) {
                // Use the repository's updateQuantity logic which handles deletion
                cartRepository.updateQuantity(item.itemId, restaurant.restoId, 0)
            } else {
                // Normal add/update logic
                cartRepository.addToCart(restaurant, item.copy(quantity = newQty))
            }
        }
    }
}