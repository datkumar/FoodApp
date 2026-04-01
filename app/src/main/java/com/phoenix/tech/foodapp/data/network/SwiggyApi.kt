package com.phoenix.tech.foodapp.data.network

import com.phoenix.tech.foodapp.data.model.RestoDetailsResponse
import com.phoenix.tech.foodapp.data.model.RestoListResponse
import com.phoenix.tech.foodapp.data.model.RestoListUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SwiggyApi {
    @GET("dapi/restaurants/list/v5")
    suspend fun fetchRestaurantsList(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("is-seo-homepage-enabled") seo: Boolean = true,
        @Query("page_type") pageType: String = "DESKTOP_WEB_LISTING"
    ): RestoListResponse


    @POST("dapi/restaurants/list/update")
    suspend fun updateRestaurantsList(
        @Body request: RestoListUpdateRequest
    ): RestoListResponse

    @GET("mapi/menu/pl")
    suspend fun fetchRestaurantDetails(
        @Query("restaurantId") restoId: String, // required
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("page-type") pageType: String = "REGULAR_MENU",
        @Query("complete-menu") complete: Boolean = true
    ): RestoDetailsResponse
}

/*
  SWIGGY API Endpoints:

Restaurants Listing:
    https://www.swiggy.com/dapi/restaurants/list/v5?lat=18.5912716&lng=73.73890899999999&is-seo-homepage-enabled=true&page_type=DESKTOP_WEB_LISTING

    https://www.swiggy.com/dapi/menu/pl?page-type=REGULAR_MENU&complete-menu=true&lat=18.5912716&lng=73.73890899999999&restaurantId=3417&catalog_qa=undefined&submitAction=ENTER

    https://www.swiggy.com/dapi/restaurants/list/v5?lat=18.5912716&lng=73.7389089&is-seo-homepage-enabled=true&page_type=DESKTOP_WEB_LISTING

Restaurant Categorized Menu:
    https://www.swiggy.com/mapi/menu/pl?page-type=REGULAR_MENU&complete-menu=true&lat=18.5912716&lng=73.73890899999999&restaurantId=211120&submitAction=ENTER


    https://www.swiggy.com/mapi/menu/pl?page-type=REGULAR_MENU&complete-menu=true&lat=18.5912716&lng=73.73890899999999&restaurantId=4872&submitAction=ENTER

//4872

*/