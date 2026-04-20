package com.phoenix.tech.foodapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.phoenix.tech.foodapp.data.local.AppDb
import com.phoenix.tech.foodapp.data.network.HttpClient
import com.phoenix.tech.foodapp.data.repository.AuthRepository
import com.phoenix.tech.foodapp.data.repository.CartRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.navigation.AuthGraphRoute
import com.phoenix.tech.foodapp.navigation.MainAppGraphRoute
import com.phoenix.tech.foodapp.navigation.RootNavigation
import com.phoenix.tech.foodapp.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize the Splash API BEFORE super.onCreate
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // To store resolved Start-Destination
        var startDest by mutableStateOf<Any?>(null)

        // Synchronous Initialization (Serial and Blocking):
        HttpClient.initialize(this)
        val database = AppDb.getDatabase(this)
        val sessionManager = SessionManager(this)
        val cartRepository = CartRepository(database.cartDao())
        val authRepository = AuthRepository(
            api = HttpClient.authApi,
            sessionManager = sessionManager,
            cartRepository = cartRepository
        )

        // Asynchronous Initialization:
        lifecycleScope.launch {
            val isAuthenticated = sessionManager.isAuthenticated.first()
            delay(200) // Tiny delay to ensure Splash is seen
            startDest = if (isAuthenticated) MainAppGraphRoute else AuthGraphRoute
        }

        // Keep showing the System Splash until startDest is null
        splashScreen.setKeepOnScreenCondition { startDest == null }
        // Define Animation between Splash Screen exit and App Screen start
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
            val scaleX = ObjectAnimator.ofFloat(splashScreenView.iconView, View.SCALE_X, 1f, 2.5f)
            val scaleY = ObjectAnimator.ofFloat(splashScreenView.iconView, View.SCALE_Y, 1f, 2.5f)
            AnimatorSet().apply {
                duration = 400L
                playTogether(fadeOut, scaleX, scaleY)
                doOnEnd { splashScreenView.remove() }
                start()
            }
        }

        setContent {
            AppTheme {
                startDest?.let { dest ->
                    RootNavigation(
                        startDestination = dest,
                        sessionManager = sessionManager,
                        authRepository = authRepository,
                        cartRepository = cartRepository,
                    )
                }
            }
        }
    }
}

