package com.phoenix.tech.foodapp.data.network

import com.phoenix.tech.foodapp.data.model.LoginResponse
import com.phoenix.tech.foodapp.data.model.UserData
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.SocketTimeoutException

class MockAuthInterceptor : Interceptor {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Intercept only auth/login endpoint calls
        if (url.contains("login")) {
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            val requestBodyString = buffer.readUtf8()

            // -------------------------------------------------------------
            // TRIGGER CONDITIONS (Driven by input entered in LoginScreen)
            // -------------------------------------------------------------
            val isTimeoutCase = requestBodyString.contains("timeout@foodapp.com", ignoreCase = true)
            val isInvalidCredsCase =
                requestBodyString.contains("wrong@foodapp.com", ignoreCase = true)
                        || requestBodyString.contains("fail", ignoreCase = true)

            // 1. TIMEOUT CASE: Throws exception directly so AuthRepository catches SocketTimeoutException
            if (isTimeoutCase) {
                Thread.sleep(2500) // Simulate network delay before timing out
                throw SocketTimeoutException("Server timed out. Please try again.")
            }

            // 2. INVALID CREDENTIALS CASE: Returns HTTP 401
            if (isInvalidCredsCase) {
                val errorResponseBody = json.encodeToString(
                    LoginResponse(
                        statusCode = 401,
                        statusMessage = "Invalid Email or Password",
                        data = null
                    )
                )
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized")
                    .body(errorResponseBody.toResponseBody(jsonMediaType))
                    .build()
            }

            // 3. SUCCESS CASE: Returns HTTP 200 with complete UserData object (Default behavior)
            val successResponseObject = LoginResponse(
                statusCode = 200,
                statusMessage = "Success",
                data = UserData(
                    token = "portfolio_demo_jwt_token_987654321",
                    firstName = "John",
                    lastName = "Doe",
                    userName = "sample_user",
                    email = "test@foodapp.com",
                    mobileNumber = "9876543210",
                    orgName = "Zwiggy Inc",
                    roleName = "Customer",
                    profilePicture = "https://picsum.photos/seed/picsum/300/300"
                )
            )

            val successResponseBody = json.encodeToString(successResponseObject)

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(successResponseBody.toResponseBody(jsonMediaType))
                .build()
        }

        return chain.proceed(request)
    }
}