package com.phoenix.tech.foodapp.data.repository

import android.util.Base64.NO_WRAP
import android.util.Base64.encodeToString
import com.phoenix.tech.foodapp.data.model.LoginRequest
import com.phoenix.tech.foodapp.data.network.AuthApi
import com.phoenix.tech.foodapp.data.network.HttpClient
import java.net.SocketTimeoutException
import java.net.UnknownHostException

//val AuthRepoTag = "AUTH_REPOSITORY"

class AuthRepository(
    private val api: AuthApi = HttpClient.authApi,
    private val sessionManager: SessionManager, // Inject Session Data
    private val cartRepository: CartRepository // Inject Cart Data
) {
    suspend fun login(email: String, pswd: String): Result<String> {
        return try {
            val encodedPswd = encodeToString(pswd.toByteArray(), NO_WRAP)

            val request = LoginRequest(
                userName = email,
                password = encodedPswd
            )
            val response = api.login(request)

            if (response.isSuccessful) { // 2xx status codes
                val body = response.body()

                // FIX: Check if body is 200 AND data object actually exists
                if (body?.statusCode == 200) {
                    val token = body.data?.token // Safe call, no !!

                    if (token != null) {
                        sessionManager.setAuthToken(token) // Save to Prefs
                        Result.success(token)
                    } else {
                        // Case where 200 is returned but data block is missing
                        Result.failure(Exception("Login successful, but user data was missing."))
                    }
                } else {
                    // Handles internal 409/501 errors which might've been sent as 2xx status
                    val msg = body?.statusMessage ?: "Authentication failed"
                    Result.failure(Exception(msg))
                }
            } else {
                // Handle non-2xx HTTP errors
                val msg = "Server Error: HTTP ${response.code()}"
                Result.failure(Exception(msg))
            }

        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun logout() {
        // 1. Clear the Auth Token
        sessionManager.clearAuthSession()
        // 2. Clear the Room database so the next user has an empty cart
        cartRepository.clearCart()
        // 3. (Optional) Call your API if you have a /logout endpoint
        // 3. (Optional) Invalidate in-memory tokens if you have a Singleton HttpClient
        // HttpClient.clearToken()
    }

    private fun handleException(e: Exception): Result<String> {
        return when (e) {
            is UnknownHostException -> Result.failure(Exception("No internet connection"))
            is SocketTimeoutException -> Result.failure(Exception("Server timed out"))
            else -> Result.failure(Exception(e.localizedMessage ?: "Unexpected error"))
        }
    }
}