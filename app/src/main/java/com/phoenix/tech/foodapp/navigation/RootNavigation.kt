package com.phoenix.tech.foodapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.phoenix.tech.foodapp.data.repository.CartRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.ui.screens.LoginScreen
import com.phoenix.tech.foodapp.ui.state.LoginViewModel

@Composable
fun RootNavigation(
    startDestination: Any, // ensure only MainAppGraph | AuthGraph
    cartRepository: CartRepository,
    sessionManager: SessionManager
) {
    val rootNavCtl = rememberNavController()
    val resolvedStartDest = if (startDestination is MainAppGraph) MainAppGraph else AuthGraph

    NavHost(
        navController = rootNavCtl,
        startDestination = resolvedStartDest
    ) {
        // Auth Graph
        navigation<AuthGraph>(startDestination = Login) {
            composable<Login> {
                // Obtain the ViewModel here
                val loginViewModel: LoginViewModel = viewModel()

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        rootNavCtl.navigate(MainAppGraph) {
                            // Clear AuthGraph from backstack
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    }
                )
            }
        }
        // Main-App Graph
        composable<MainAppGraph> {
            MainAppLayout(
                cartRepository = cartRepository,
                sessionManager = sessionManager
            )
        }
    }
}