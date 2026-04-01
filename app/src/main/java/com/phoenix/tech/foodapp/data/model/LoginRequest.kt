package com.phoenix.tech.foodapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val userName: String,
    val password: String, // We will encode this before sending
    val source: String = "mobileApp",
    val parentProcessTag: String = "Midas-receivingActivityPost-172.16.1.182-8020--29252-305445",
    val stationName: String = "Station one",
)