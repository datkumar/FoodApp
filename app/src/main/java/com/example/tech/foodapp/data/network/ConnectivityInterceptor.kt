package com.example.tech.foodapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ConnectivityInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        if (!cm.isCurrentlyUsable()) {
            // Throw a custom exception that your Repository can catch
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

class NoConnectivityException : IOException("No internet connection")