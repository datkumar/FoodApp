package com.phoenix.tech.foodapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phoenix.tech.foodapp.data.model.RestoDetailsRestoInfo

@Entity(
    tableName = "restaurants"
)
data class RestaurantEntity(
    @PrimaryKey val restoId: String,
    val name: String,
    val cloudinaryImageId: String,
    val areaName: String
)


// Converts RestoInfo model object (parsed API json) into DB Restaurant Table entry
fun RestoDetailsRestoInfo.toEntity() = RestaurantEntity(
    restoId = this.id,
    name = this.name,
    cloudinaryImageId = this.cloudinaryImageId,
    areaName = this.areaName
)

