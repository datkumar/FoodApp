package com.phoenix.tech.foodapp.data.network

import com.phoenix.tech.foodapp.BuildConfig
import com.phoenix.tech.foodapp.data.model.GeocodingResponse
import com.phoenix.tech.foodapp.data.model.ReverseGeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

private const val geoApiKey = BuildConfig.GEO_API_KEY

interface GeoApi {
    @GET("autocomplete") // or "search" for complete word
    suspend fun searchLocation(
        @Query("text") text: String,
        @Query("limit") limit: Int = 6,
        @Query("format") format: String = "json",
        @Query("apiKey") apiKey: String = geoApiKey
    ): GeocodingResponse

    @GET("reverse")
    suspend fun searchByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("type") type: String = "city", // or "street"
        @Query("format") format: String = "json",
        @Query("apiKey") apiKey: String = geoApiKey
    ): ReverseGeocodingResponse
}


//https://api.geoapify.com/v1/
//https://api.geoapify.com/v1/geocode/reverse?lat=28.6139&lon=77.2088&format=json&apiKey=YOUR_API_KEY
//https://api.geoapify.com/v1/geocode/reverse?lat=18.5912716&lon=73.7389089&format=json&apiKey=YOUR_API_KEY