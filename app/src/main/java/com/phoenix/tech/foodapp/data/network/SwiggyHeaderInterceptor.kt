package com.phoenix.tech.foodapp.data.network

import com.phoenix.tech.foodapp.utils.SWIGGY_BASE_URL
import okhttp3.Interceptor
import okhttp3.Response


// Ensure it is only attached to the swiggyClient in RetrofitClient
class SwiggyHeaderInterceptor : Interceptor {

    // A simple in-memory storage for the session cookie
    private var sessionCookie: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .header(
                "User-Agent",
                "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:148.0) Gecko/20100101 Firefox/148.0"
            )
            .header("Origin", SWIGGY_BASE_URL)
            .header("Referer", SWIGGY_BASE_URL)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        // If we have a cookie from a previous GET call, attach it
        sessionCookie?.let {
            requestBuilder.header("Cookie", it)
        }

        val response = chain.proceed(requestBuilder.build())

        // Extract cookies (including CSRF) from the GET response to use in the next POST
        val cookies = response.headers("Set-Cookie")
        if (cookies.isNotEmpty()) {
            sessionCookie = cookies.joinToString("; ") { it.split(";")[0] }
        }

        return response
    }
}
