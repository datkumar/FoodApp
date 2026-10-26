package com.example.tech.foodapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kotlin.time.Duration.Companion.milliseconds

class NetworkObserver(context: Context) {
    private val cm = context.applicationContext.getSystemService(ConnectivityManager::class.java)

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    val isConnected: Flow<Boolean> = callbackFlow {
        var current: Network? = null
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                current = network
                trySend(caps.isUsable())
            }

            override fun onLost(network: Network) {
                if (network == current) {
                    current = null
                    trySend(false)
                } // ignore stale networks
            }
        }
        cm.registerDefaultNetworkCallback(callback)
        trySend(cm.isCurrentlyUsable())
        awaitClose {
            cm.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        // swallow sub-second drops (Wi-Fi <-> cellular, INTERNET arriving before VALIDATED)
        .transformLatest { connected ->
            if (!connected) {
                delay(800.milliseconds)
            }
            emit(connected)
        }
}

fun NetworkCapabilities.isUsable() = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        && hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

fun ConnectivityManager.isCurrentlyUsable() = getNetworkCapabilities(activeNetwork)?.isUsable() == true
