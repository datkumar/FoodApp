package com.phoenix.tech.foodapp.navigation

import kotlinx.serialization.Serializable

// AUTH stuff
@Serializable object AuthGraph
@Serializable object Login

//----------------------------------
// MAIN-APP stuff
@Serializable object MainAppGraph

// Tab Root routes
@Serializable object HomeTab
@Serializable object ProfileTab
@Serializable object SettingsTab

// Leaf Destination Routes
@Serializable object Home
@Serializable object Profile
@Serializable object Settings
@Serializable object Cart

@Serializable
data class RestaurantDetails(
    val restoId: String,
    val restoName: String
)

