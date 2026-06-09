package com.phoenix.tech.foodapp.data.model

import kotlinx.serialization.Serializable


@Serializable
data class LoginResponse(
    val statusCode: Int,
    val statusMessage: String,
    val data: UserData? = null, // Only present in SUCCESS-case response
)


@Serializable
data class UserData(
    val token: String,
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val mobileNumber: String,
    val orgName: String,
    val roleName: String,
    val profilePicture: String,
)
