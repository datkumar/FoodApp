package com.phoenix.tech.foodapp.data.repository

import com.phoenix.tech.foodapp.data.local.CartDao
import com.phoenix.tech.foodapp.data.local.entities.CartItemEntity
import com.phoenix.tech.foodapp.data.local.entities.CartItemWithRestaurant
import com.phoenix.tech.foodapp.data.local.entities.RestaurantEntity
import kotlinx.coroutines.flow.Flow


class CartRepository(
    private val cartDao: CartDao
) {
    // Room returns a Flow, so this is automatically "live"
    val allCartItems: Flow<List<CartItemEntity>> = cartDao.getAllCartItems()

    // This is the "Power User" flow that includes the Restaurant data
    val cartWithRestoFlow: Flow<List<CartItemWithRestaurant>> =
        cartDao.getCartWithRestaurantDetails()


    suspend fun addToCart(
        restaurant: RestaurantEntity,
        cartItem: CartItemEntity
    ) {
        cartDao.insertRestaurant(restaurant) // Ensure resto exists first
        cartDao.insertOrUpdateItem(cartItem)
    }

    suspend fun updateQuantity(itemId: String, restoId: String, newQty: Int) {
        if (newQty <= 0) {
            cartDao.removeItem(itemId)
            // Cleanup check
            val remainingItems = cartDao.getItemCountForRestaurant(restoId)
            if (remainingItems == 0) {
                cartDao.removeRestaurant(restoId)
            }
        } else {
            cartDao.updateQuantity(itemId, newQty)
        }
    }

    suspend fun clearCart() {
        cartDao.clearAllCartData()
    }
}