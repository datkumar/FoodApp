package com.phoenix.tech.foodapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.phoenix.tech.foodapp.data.model.LoginRequest
import com.phoenix.tech.foodapp.data.model.UserData
import com.phoenix.tech.foodapp.data.network.AuthApi
import com.phoenix.tech.foodapp.data.network.HttpClient
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Base64

val AuthRepoTag = "AUTH_REPOSITORY"

class AuthRepository(
    private val api: AuthApi = HttpClient.authApi,
    private val sessionManager: SessionManager, // Inject Session Data
    private val cartRepository: CartRepository // Inject Cart Data
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loginUser(
        email: String, pswd: String
    ): Result<UserData> {
        try {
            val encodedPswd = Base64.getEncoder().encodeToString(pswd.toByteArray())
            // Build and fire Login request
            val request = LoginRequest(
                userName = email,
                password = encodedPswd
            )
            val response = api.login(request)

            if (response.isSuccessful) { // 2xx status codes
                val body = response.body()
                // Status 200 AND Data exists
                if (body?.statusCode == 200 && body.data != null) {
                    val userData: UserData = body.data
                    sessionManager.saveUserInfo(userData)
                    return Result.success(userData)
                }
                return Result.failure(
                    Exception(body?.statusMessage ?: "Data missing")
                )
            } else {
                // Handle non-2xx HTTP errors
                return Result.failure(
                    Exception("Server Error: HTTP ${response.code()}")
                )
            }
        } catch (e: Exception) {
            println("$AuthRepoTag: Caught exception: ${e.message}") // Add this temporarily
            return handleException(e)
        }
    }

    suspend fun logoutUser() {
        // 1. Clear the Auth Token
        sessionManager.clearUserInfo()
        // 2. Clear the Room database so the next user has an empty cart
        cartRepository.clearCart()
        // 3. (Optional) Call your API if you have a /logout endpoint
        // 3. (Optional) Invalidate in-memory tokens if you have a Singleton HttpClient
    }

    private fun <T> handleException(e: Exception): Result<T> {
        val message = when (e) {
            is UnknownHostException -> "No internet connection"
            is SocketTimeoutException -> "Server timed out"
            else -> e.localizedMessage ?: "Unexpected error"
        }
        return Result.failure(
            Exception(message)
        )
    }
}