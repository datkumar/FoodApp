package com.phoenix.tech.foodapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.phoenix.tech.foodapp.data.model.LoginRequest
import com.phoenix.tech.foodapp.data.model.LoginResponse
import com.phoenix.tech.foodapp.data.model.UserData
import com.phoenix.tech.foodapp.data.network.AuthApi
import com.phoenix.tech.foodapp.data.network.HttpClient
import com.phoenix.tech.foodapp.data.network.HttpClient.json
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Base64

const val AuthRepoTag = "AUTH_REPOSITORY"

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

            if (response.isSuccessful) { // 2xx HTTP codes
                val body = response.body()
                // Status 200 AND Data exists
                if (body?.statusCode == 200 && body.data != null) {
                    // Save session on successful authentication
                    val userData: UserData = body.data
                    sessionManager.saveUserInfo(userData)
                    return Result.success(userData)
                }
                return Result.failure(
                    Exception(body?.statusMessage ?: "Data missing")
                )

            } else { // Non-2xx HTTP codes
                // Read raw JSON string from errorBody()
                val errorJsonString = response.errorBody()?.string()
                // Parse the JSON into LoginResponse object
                val parsedErrorResponse = try {
                    errorJsonString?.let {
                        json.decodeFromString<LoginResponse>(it)
                    }
                } catch (_: Exception) {
                    null
                }
                // Get message from parsed response, or fallback
                val errorMessage = parsedErrorResponse?.statusMessage ?: "Invalid Credentials"

                if (response.code() == 401) { // Unauthorized
                    return Result.failure(Exception(errorMessage))
                }
                // Other HTTP codes than 401
                return Result.failure(
                    Exception("Server Error (${response.code()}): $errorMessage")
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