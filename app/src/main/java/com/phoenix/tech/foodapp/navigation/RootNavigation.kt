package com.phoenix.tech.foodapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RootNavigation(
    startDestination: Any, // ensure only MainAppGraph | AuthGraph
    cartRepository: CartRepository,
    sessionManager: SessionManager
) {
    val rootNavCtl = rememberNavController()
    val resolvedStartDest =
        if (startDestination is MainAppGraphRoute) MainAppGraphRoute else AuthGraphRoute

    NavHost(
        navController = rootNavCtl,
        startDestination = resolvedStartDest
    ) {
        // Auth Graph
        navigation<AuthGraphRoute>(startDestination = LoginRoute) {
            composable<LoginRoute> {
                // Obtain the ViewModel here
                val loginViewModel: LoginViewModel = viewModel()

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
                cartRepository = cartRepository,
                sessionManager = sessionManager
            )
        }
    }
}