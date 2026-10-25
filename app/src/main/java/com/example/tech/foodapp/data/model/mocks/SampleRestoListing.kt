package com.example.tech.foodapp.data.model.mocks

import android.content.Context
import com.example.tech.foodapp.data.model.RestoListingItemData
import com.example.tech.foodapp.data.model.RestoListingItemSla

object SampleRestoListing {

    fun sampleRestoListingItem(context: Context): RestoListingItemData {
        return fallbackRestoListingItem;
    }

    private val fallbackRestoListingItem = RestoListingItemData(
        id = "732719",
        name = "Theobroma",
        cloudinaryImageId = "RX_THUMBNAIL/IMAGES/VENDOR/2026/3/5/7cdc93ac-fc55-47f8-a908-ed8bf95cf5fd_732719.JPG",
        cuisines = listOf("Desserts"),
        avgRating = 4.4,
        totalRatingsString = "1.7k+",
        locality = "Hinjawadi",
        areaName = "Hinjawadi",
        sla = RestoListingItemSla(
            deliveryTime = 29,
            slaString = "25-30 mins",
            lastMileTravelString = "1.6 km"
        ),
    )
}