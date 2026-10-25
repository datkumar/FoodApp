package com.example.tech.foodapp.data.repository


import android.util.Log
import com.example.tech.foodapp.data.model.RestoListResponse
import com.example.tech.foodapp.data.model.RestoListUpdateRequest
import com.example.tech.foodapp.data.model.RestoListingItem
import com.example.tech.foodapp.data.network.HttpClient
import com.example.tech.foodapp.data.network.SwiggyApi

const val RestoRepoTag = "RESTO_LIST_REPO"


class RestoListRepository(
    private val api: SwiggyApi = HttpClient.swiggyApi
) {
    var cachedRestaurantListing: MutableList<RestoListingItem> = mutableListOf()
        private set // ViewModel can read it, but only the repository can modify it

    // Keep track of which location the cached restaurants belongs to
    private var cachedLat: Double? = null
    private var cachedLng: Double? = null

    // Pagination cursors
    private var nextOffset: String? = null
    private var widgetOffset: Map<String, String>? = null
    private var csrfToken: String? = null
    var hasMore: Boolean = true
        private set

    private var isLoadingPage = false

    suspend fun getHomeRestaurants(
        lat: Double, lng: Double,
        forceRefresh: Boolean = false
    ): List<RestoListingItem> {
        // Check if coordinates changed or if force refresh was requested
        val isNewLocation = lat != cachedLat || lng != cachedLng
        if (forceRefresh || isNewLocation) {
            clearCachedRestaurants() // Wipe cache when force-refresh specified
        }
        // Return cached restaurant if non-empty valid one exists
        if (cachedRestaurantListing.isNotEmpty()) return cachedRestaurantListing

        try {
            // Pass the dynamic coordinates to the API
            val response = api.fetchRestaurantsList(lat, lng)

            // Store pagination cursors from first response
            csrfToken = response.csrfToken
            nextOffset = response.data.pageOffset?.nextOffset
            widgetOffset = response.data.pageOffset?.widgetOffset
            hasMore = nextOffset != null

            val initial = extractRestaurants(response)
            cachedRestaurantListing.addAll(initial)
            cachedLat = lat; cachedLng = lng
            return cachedRestaurantListing.toList()

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun loadNextPage(lat: Double, lng: Double): List<RestoListingItem> {
        if (!hasMore || isLoadingPage) return emptyList()
        return try {
            val httpResponse = api.updateRestaurantsList(
                RestoListUpdateRequest(
                    lat = lat,
                    lng = lng,
                    nextOffset = nextOffset,
                    widgetOffset = widgetOffset,
                    csrf = csrfToken
                )
            )

            if (!httpResponse.isSuccessful) {
                Log.w(
                    RestoRepoTag,
                    "loadNextPage: blocked or error — HTTP ${httpResponse.code()}, header: ${httpResponse.headers()["x-amzn-waf-action"]}"
                )
                return emptyList()
            }

            val response = httpResponse.body() ?: return emptyList()

            widgetOffset = response.data.pageOffset?.widgetOffset
            val newItems = extractRestaurants(response)
            hasMore = newItems.isNotEmpty()
            cachedRestaurantListing.addAll(newItems)
            newItems

//            // Update cursors for the next page
//            widgetOffset = response.data.pageOffset?.widgetOffset
//            // nextOffset stays the same per observed API behaviour
//            // csrfToken becomes null after first page — keep the original
//
//            val newItems = extractRestaurants(response)
//            hasMore = newItems.isNotEmpty()
//            cachedRestaurantListing.addAll(newItems)
//            newItems
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } finally {
            isLoadingPage = false  // always reset, even on error
        }
    }

    /** Pulls restaurants from whichever card has the grid listing */
    private fun extractRestaurants(response: RestoListResponse): List<RestoListingItem> {
        return response.data.cards
            .mapNotNull { it.card.card.gridElements?.infoWithStyle?.restaurants }
            .flatten()
    }

    fun clearCachedRestaurants() {
        cachedRestaurantListing.clear()
        cachedLat = null; cachedLng = null
        csrfToken = null
        nextOffset = null
        widgetOffset = null
        hasMore = true
    }

}