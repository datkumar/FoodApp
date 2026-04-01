//package com.phoenix.tech.foodapp.data.repository
//
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.phoenix.tech.foodapp.data.model.RestoListPageOffset
//import com.phoenix.tech.foodapp.data.model.RestoListUpdateRequest
//import com.phoenix.tech.foodapp.data.model.RestoListingItem
//import com.phoenix.tech.foodapp.data.network.SwiggyApi
//
//class RestoListPagingSource(
//    private val api: SwiggyApi,
//    private val lat: Double,
//    private val lng: Double
//) : PagingSource<RestoListPageOffset, RestoListingItem>() {
//
//    override suspend fun load(
//        params: LoadParams<RestoListPageOffset>
//    ): LoadResult<RestoListPageOffset, RestoListingItem> {
//        return try {
//            val position = params.key
//            val isInitialLoad = position == null
//
//            val response = if (isInitialLoad) {
//                api.fetchRestaurantsList(lat, lng)
//            } else {
//                api.updateRestaurantsList(
//                    RestoListUpdateRequest(
//                        lat = lat,
//                        lng = lng,
//                        nextOffset = position.nextOffset,
//                        widgetOffset = position.widgetOffset
//                    )
//                )
//            }
//
//            val restaurants = if (isInitialLoad) {
//                // For the first call, target the "Main Listing" card specifically
//                // instead of flattening everything (which includes Top Brands)
//                // Based on your notes, this is likely at index 4
//                response.data.cards.getOrNull(4)?.card?.card?.gridElements?.infoWithStyle?.restaurants
//                    ?: response.data.cards.flatMap {
//                        it.card.card.gridElements?.infoWithStyle?.restaurants ?: emptyList()
//                    }
//            } else {
//                // The Update API is usually more streamlined
//                response.data.cards.flatMap {
//                    it.card.card.gridElements?.infoWithStyle?.restaurants ?: emptyList()
//                }
//            }
//
//            // Filter out any potential empty items or duplicates if necessary
//            val distinctRestaurants = restaurants.distinctBy { it.info.id }
//
//            LoadResult.Page(
//                data = distinctRestaurants,
//                prevKey = null,
//                // If nextOffset is null or empty, pagination stops
//                nextKey = response.data.pageOffset?.takeIf { it.nextOffset?.isNotEmpty() == true }
//            )
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//
//
////    override suspend fun load(
////        params: LoadParams<RestoListPageOffset>
////    ): LoadResult<RestoListPageOffset, RestoListingItem> {
////        return try {
////            val position = params.key
////
////            val response = if (position == null) {
////                // Initial Load
////                api.fetchRestaurantsList(lat, lng)
////            } else {
////                // Subsequent Pages
////                api.updateRestaurantsList(
////                    RestoListUpdateRequest(
////                        lat = lat,
////                        lng = lng,
////                        nextOffset = position.nextOffset,
////                        widgetOffset = position.widgetOffset,
////                        // If Swiggy requires it in body, you may need to
////                        // parse the csrf from the first response and pass it here
////                        csrf = null
////                    )
////                )
////            }
////
////            // Swiggy response usually has many "cards". We find the one containing restaurants.
////            val restaurants = response.data.cards
////                .mapNotNull { it.card.card.gridElements?.infoWithStyle?.restaurants }
////                .flatten()
////
////            LoadResult.Page(
////                data = restaurants,
////                prevKey = null,
////                nextKey = response.data.pageOffset // This contains nextOffset for the next call
////            )
////        } catch (e: Exception) {
////            LoadResult.Error(e)
////        }
////    }
//
//    override fun getRefreshKey(
//        state: PagingState<RestoListPageOffset, RestoListingItem>
//    ): RestoListPageOffset? = null
//}