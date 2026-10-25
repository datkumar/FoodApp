package com.example.tech.foodapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech.foodapp.data.local.entities.CartItemEntity
import com.example.tech.foodapp.data.local.entities.RestaurantEntity
import com.example.tech.foodapp.data.repository.CartRepository
import com.example.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModelFactory(
    private val cartRepository: CartRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RestoDetailsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                // Pass both dependencies here
                RestoDetailsViewModel(cartRepository, sessionManager) as T
            }

            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                CartViewModel(cartRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    // Sum up all items: quantity * (price / 100)
    val cartTotal: StateFlow<Double> = repository.allCartItems
        .map { items ->
            // Use 100.0 to ensure the result is a Double
            items.sumOf { it.quantity * it.price } / 100.0
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0 // Matches the Double type
        )

    val itemCount: StateFlow<Int> = repository.allCartItems
        .map { items ->
            items.sumOf { it.quantity }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )


    // Now grouping by the Restaurant object
    val groupedItems: StateFlow<Map<RestaurantEntity, List<CartItemEntity>>> =
        repository.cartWithRestoFlow
            .map { list ->
                // Group the list of CartItemWithRestaurant by the 'restaurant' field
                list.groupBy(
                    keySelector = { it.restaurant },
                    valueTransform = { it.cartItem }
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap()
            )

    fun updateQty(itemId: String, restoId: String, newQty: Int) {
        viewModelScope.launch {
            repository.updateQuantity(itemId, restoId, newQty)
        }
    }
}