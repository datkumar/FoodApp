package com.example.tech.foodapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tech.foodapp.data.model.RestoDetailsRestoInfo
import com.example.tech.foodapp.utils.AppKeys

@Entity(
    tableName = AppKeys.DB_TABLE_RESTAURANTS
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

