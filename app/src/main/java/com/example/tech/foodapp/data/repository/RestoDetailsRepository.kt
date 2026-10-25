package com.example.tech.foodapp.data.repository

import com.example.tech.foodapp.data.model.RestoDetailsResult
import com.example.tech.foodapp.data.network.HttpClient
import com.example.tech.foodapp.data.network.SwiggyApi


class RestoDetailsRepository(
    private val api: SwiggyApi = HttpClient.swiggyApi
) {
    suspend fun getRestaurantDetails(
        restoId: String,
        lat: Double,
        lng: Double
    ): RestoDetailsResult? {
        try {
//            val response = api.fetchRestaurantDetails(restoId)
            // Pass the dynamic coordinates to the API call
            val response = api.fetchRestaurantDetails(
                restoId = restoId,
                lat = lat,
                lng = lng
            )
            val allCards = response.data.cards

            // 1. Find Restaurant Info Header
            val restoInfo = allCards.firstNotNullOfOrNull { it.card?.card?.info }
                ?: throw Exception("Restaurant info not found")

            // 2. Find the Menu Group
            val groupedCard = allCards.firstOrNull { it.groupedCard != null }?.groupedCard
                ?: throw Exception("Menu data not found")

            val mainGroupCards = groupedCard.cardGroupMap.regularMenu.cards

            // 3. Filter and Parse Menu Categories
            // We exclude the first few/last few cards as they are usually offers/license info
            val restoMenu = mainGroupCards
                .map { it.card.card }
                .filter { it.title != null && (it.itemCards != null || it.subCategories != null) }

            return RestoDetailsResult(
                restoInfo = restoInfo,
                categoryItems = restoMenu
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}