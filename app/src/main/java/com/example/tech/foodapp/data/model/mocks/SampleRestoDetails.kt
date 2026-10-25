package com.example.tech.foodapp.data.model.mocks

import android.content.Context
import com.example.tech.foodapp.data.model.RestoDetailsCategoryFoodItem
import com.example.tech.foodapp.data.model.RestoDetailsRestoInfo
import com.example.tech.foodapp.data.model.RestoDetailsRestoInfoSla

object SampleRestoDetails {
    fun sampleRestoDetailsRestoInfo(context: Context? = null): RestoDetailsRestoInfo {
        return fallbackRestoInfo
    }

    fun sampleFoodItem(context: Context? = null): RestoDetailsCategoryFoodItem {
        return fallbackFoodItem
    }

    private val fallbackRestoInfo = RestoDetailsRestoInfo(
        id = "732719",
        name = "Oven Story Pizza",
        cloudinaryImageId = "RX_THUMBNAIL/IMAGES/VENDOR/2026/3/5/7cdc93ac-fc55-47f8-a908-ed8bf95cf5fd_732719.JPG",
        cuisines = listOf("Pizzas, Pastas"),
        avgRating = 4.4,
        totalRatingsString = "15K+ ratings",
        city = "Pune",
        locality = "Wakad Road",
        areaName = "Hinjawadi",
        sla = RestoDetailsRestoInfoSla(
            deliveryTime = 29,
            slaString = "25-30 mins"
        ),
    )

    private val fallbackFoodItem = RestoDetailsCategoryFoodItem(
        id = "823617",
        name = "Paneer Punjabi",
        description = "Serves 3 | Must Try! Based on unique recipe of Paneer cooked in creamy onion, tomato gravy with a hint of blanched palak",
        isVeg = 1,
        defaultPrice = 38000,
    )
}