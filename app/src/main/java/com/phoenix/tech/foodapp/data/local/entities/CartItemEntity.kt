package com.phoenix.tech.foodapp.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.phoenix.tech.foodapp.data.model.RestoDetailsCategoryFoodItem

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["restoId"],
            childColumns = ["restoId"],
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
        parentColumn = "restoId",
        entityColumn = "restoId"
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