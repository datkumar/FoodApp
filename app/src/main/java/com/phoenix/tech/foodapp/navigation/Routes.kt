package com.phoenix.tech.foodapp.navigation

import kotlinx.serialization.Serializable

// AUTH stuff
@Serializable object AuthGraphRoute
@Serializable object LoginRoute

//----------------------------------
// MAIN-APP stuff
@Serializable object MainAppGraphRoute

// Tab Root routes
@Serializable object HomeTabRoute
@Serializable object ProfileTabRoute
@Serializable object SettingsTabRoute

// Leaf Destination Routes
@Serializable object HomeRoute
@Serializable object ProfileRoute
@Serializable object AppSettingsRoute
@Serializable object CartRoute

@Serializable
data class RestaurantDetailsRoute(
    val restoId: String,
    val restoName: String,
    val imgId: String,
    val avgRating: Double = 0.0,
    val totalRatingsString: String = "Count N/A",
    val slaTime: String = "ETA Unknown",
    val slaDist: String = "Distance Unknown",
    val cuisines: String = "N/A"
)

