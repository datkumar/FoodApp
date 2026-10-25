package com.example.tech.foodapp.data.network

import com.example.tech.foodapp.data.model.RestoDetailsResponse
import com.example.tech.foodapp.data.model.RestoListResponse
import com.example.tech.foodapp.data.model.RestoListUpdateRequest
import retrofit2.Response
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
    ): Response<RestoListResponse>

    @GET("mapi/menu/pl")
    suspend fun fetchRestaurantDetails(
        @Query("restaurantId") restoId: String, // required
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("page-type") pageType: String = "REGULAR_MENU",
        @Query("complete-menu") complete: Boolean = true
    ): RestoDetailsResponse
}