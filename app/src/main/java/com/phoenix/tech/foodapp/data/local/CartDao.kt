package com.phoenix.tech.foodapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.phoenix.tech.foodapp.data.local.entities.CartItemEntity
import com.phoenix.tech.foodapp.data.local.entities.CartItemWithRestaurant
import com.phoenix.tech.foodapp.data.local.entities.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Transaction // Use @Transaction for relations to ensure consistency
    @Query("SELECT * FROM cart_items")
    fun getCartWithRestaurantDetails(): Flow<List<CartItemWithRestaurant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateItem(item: CartItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity)

    @Query("SELECT COUNT(*) FROM cart_items WHERE restoId = :restoId")
    suspend fun getItemCountForRestaurant(restoId: String): Int

    @Query("DELETE FROM restaurants WHERE restoId = :restoId")
    suspend fun removeRestaurant(restoId: String)

    @Query("DELETE FROM cart_items WHERE itemId = :itemId")
    suspend fun removeItem(itemId: String)

    @Query("UPDATE cart_items SET quantity = :qty WHERE itemId = :itemId")
    suspend fun updateQuantity(itemId: String, qty: Int)

}
