package com.example.tech.foodapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GeocodingResponse(
    val results: List<GeocodingEntry> = emptyList()
)

@Serializable
data class ReverseGeocodingResponse(
    val results: List<GeocodingEntry> = emptyList()
)

@Serializable
data class GeocodingEntry(
    @SerialName("place_id") val placeId: String,
    val lat: Double,
    val lon: Double,
    val city: String? = null,
    val country: String? = null,
    @SerialName("formatted") val formattedName: String
)


@Serializable
data class UserLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isAutoDetected: Boolean
)

