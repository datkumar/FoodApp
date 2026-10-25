package com.example.tech.foodapp.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.tech.foodapp.data.model.RestoDetailsCategoryFoodItem
import com.example.tech.foodapp.utils.AppKeys

@Entity(
    tableName = AppKeys.DB_TABLE_CART_ITEMS,
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = [AppKeys.DB_COL_RESTO_ID],
            childColumns = [AppKeys.DB_COL_RESTO_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CartItemEntity(
    @PrimaryKey val itemId: String, // Swiggy IDs are usually unique, but restoId + itemId is safer
    val restoId: String,
    val name: String,
    val price: Int,
    val quantity: Int,
    val isVeg: Int
)


data class CartItemWithRestaurant(
    @Embedded val cartItem: CartItemEntity,
    @Relation(
        parentColumn = AppKeys.DB_COL_RESTO_ID,
        entityColumn = AppKeys.DB_COL_RESTO_ID
    )
    val restaurant: RestaurantEntity
)


// Converts FoodItem model object (parsed API json) into DB CartItem Table entry
fun RestoDetailsCategoryFoodItem.toCartEntity(restoId: String) = CartItemEntity(
    itemId = this.id,
    restoId = restoId,
    name = this.name,
    price = this.price ?: this.defaultPrice ?: 0,
    quantity = 1,
    isVeg = this.isVeg ?: 1
)