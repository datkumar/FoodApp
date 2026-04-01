package com.phoenix.tech.foodapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.phoenix.tech.foodapp.data.local.AppDb
import com.phoenix.tech.foodapp.data.repository.CartRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.navigation.AuthGraph
import com.phoenix.tech.foodapp.navigation.MainAppGraph
import com.phoenix.tech.foodapp.navigation.RootNavigation
import com.phoenix.tech.foodapp.ui.theme.AppTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize DB and Repository
        val database = AppDb.getDatabase(this)
        val cartRepository = CartRepository(database.cartDao())

        val sessionManager = SessionManager(this)
        // Use a state to hold the start destination
        var startDestination by mutableStateOf<Any?>(null)

        lifecycleScope.launch {
            // Check for existing token and dynamically set start-dest
            val token = sessionManager.authToken.firstOrNull()
            startDestination = if (!token.isNullOrEmpty()) MainAppGraph else AuthGraph
        }

        setContent {
            AppTheme {
                // Only render navigation once we know where we are going
                startDestination?.let { destination ->
                    RootNavigation(
                        startDestination = destination,
                        cartRepository = cartRepository,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}

