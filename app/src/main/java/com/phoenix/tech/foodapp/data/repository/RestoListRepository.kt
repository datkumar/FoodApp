package com.phoenix.tech.foodapp.data.repository


import com.phoenix.tech.foodapp.data.model.RestoListingItem
import com.phoenix.tech.foodapp.data.network.HttpClient
import com.phoenix.tech.foodapp.data.network.SwiggyApi


class RestoListRepository(
    private val api: SwiggyApi = HttpClient.swiggyApi
) {
    private var cachedRestaurantListing: List<RestoListingItem>? = null

    // Keep track of which location the cached restaurants belongs to
    private var cachedLat: Double? = null
    private var cachedLng: Double? = null

    suspend fun getHomeRestaurants(
        lat: Double, lng: Double,
        forceRefresh: Boolean = false
    ): List<RestoListingItem> {
        // Check if coordinates changed or if force refresh was requested
        val isNewLocation = lat != cachedLat || lng != cachedLng
        if (forceRefresh || isNewLocation) {
            clearCachedList() // Wipe cache when force-refresh specified
        }
        // Return cached restaurant (of same location) if exists
        cachedRestaurantListing?.let { return it }

        try {
            // Pass the dynamic coordinates to the API
            val response = api.fetchRestaurantsList(lat, lng)

            val fetchedRestaurantsList = response
                .data.cards.getOrNull(1)
                ?.card?.card?.gridElements?.infoWithStyle
                ?.restaurants
                ?: emptyList()

            // Update cache and coordinates
            cachedRestaurantListing = fetchedRestaurantsList
            cachedLat = lat; cachedLng = lng

            return fetchedRestaurantsList
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun clearCachedList() {
        cachedRestaurantListing = null
        cachedLat = null; cachedLng = null
    }
    
}