package com.example.tech.foodapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.tech.foodapp.data.repository.AuthRepository
import com.example.tech.foodapp.data.repository.CartRepository
import com.example.tech.foodapp.data.repository.SessionManager
import com.example.tech.foodapp.ui.screens.LoginScreen
import com.example.tech.foodapp.ui.state.LoginViewModel
import com.example.tech.foodapp.ui.state.LoginViewModelFactory

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RootNavigation(
    startDestination: Any, // ensure only MainAppGraph | AuthGraph
    authRepository: AuthRepository,
    cartRepository: CartRepository,
    sessionManager: SessionManager
) {
    val rootNavCtl = rememberNavController()
    val resolvedStartDest = if (startDestination is MainAppGraphRoute) MainAppGraphRoute else AuthGraphRoute

    NavHost(
        navController = rootNavCtl,
        startDestination = resolvedStartDest
    ) {
        // Auth Graph
        navigation<AuthGraphRoute>(startDestination = LoginRoute) {
            composable<LoginRoute> {
                // Initialize the shared ViewModel with the shared Repository
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(authRepository)
                )
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        rootNavCtl.navigate(MainAppGraphRoute) {
                            // Clear AuthGraph from backstack
                            popUpTo(AuthGraphRoute) { inclusive = true }
                        }
                    }
                )
            }
        }
        // Main-App Graph
        composable<MainAppGraphRoute> {
            MainAppLayout(
                authRepository = authRepository,
                cartRepository = cartRepository,
                sessionManager = sessionManager,
                onLogout = {
                    rootNavCtl.navigate(AuthGraphRoute) {
                        popUpTo(MainAppGraphRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}