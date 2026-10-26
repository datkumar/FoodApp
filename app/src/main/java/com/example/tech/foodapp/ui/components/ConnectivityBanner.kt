package com.example.tech.foodapp.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val ConnectivityBannerTag = "CONNECTIVITY_BANNER"

@Composable
fun ConnectivityBanner(isConnected: Boolean) {
    var showBanner by remember { mutableStateOf(false) }

    // Track if this is the very first time the component is receiving data
    // Don't show banner when first launch AND Connected
    var isInitialEmission by remember { mutableStateOf(true) }

    // Trigger green flash when coming back online
    LaunchedEffect(isConnected) {
        if (isInitialEmission) {
            isInitialEmission = false
            return@LaunchedEffect
        }
        if (isConnected) { // Show Connected Banner for only 2sec
            showBanner = true
            delay(2000.milliseconds)
            showBanner = false
        }
    }

    Log.d(ConnectivityBannerTag, "CONNECTIVITY STATUS: isConnected=$isConnected, flash=$showBanner")

    AnimatedVisibility(
        visible = !isConnected || showBanner,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.zIndex(99f) // FORCE to the very front
    ) {
        Surface(
            color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFE3D1D1),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp // Add shadow so you can see it over other elements
        ) {
            Box(
                modifier = Modifier
                    .statusBarsPadding() // This handles the notch/clock area
                    .padding(bottom = 4.dp), // Minimal padding for a slim look
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isConnected) "Back Online" else "No Internet Connection",
                    color = if (isConnected) Color.White else Color(0xFFF44336),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}