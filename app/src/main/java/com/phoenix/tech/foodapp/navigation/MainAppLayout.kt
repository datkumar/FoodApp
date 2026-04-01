package com.phoenix.tech.foodapp.navigation

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.data.repository.CartRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.ui.components.CustomTopBar
import com.phoenix.tech.foodapp.ui.components.LocationSelectionSheet
import com.phoenix.tech.foodapp.ui.screens.CartScreen
import com.phoenix.tech.foodapp.ui.screens.HomeScreen
import com.phoenix.tech.foodapp.ui.screens.ProfileScreen
import com.phoenix.tech.foodapp.ui.screens.RestaurantDetailsScreen
import com.phoenix.tech.foodapp.ui.screens.SettingsScreen
import com.phoenix.tech.foodapp.ui.state.HomeViewModel
import com.phoenix.tech.foodapp.ui.state.HomeViewModelFactory
import com.phoenix.tech.foodapp.ui.state.LocationViewModel
import com.phoenix.tech.foodapp.ui.state.LocationViewModelFactory
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    cartRepository: CartRepository,
    sessionManager: SessionManager
) {
    // Navigation state
    val mainAppNavCtl = rememberNavController()
    val currBackStackState by mainAppNavCtl.currentBackStackEntryAsState()
    val currDestination = currBackStackState?.destination
    // App Context
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Location State
    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(sessionManager, application)
    )
    val savedLocation by locationViewModel.savedLocation.collectAsState()
    // Whether Permissions popup or Location Selection should be shown
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }
    var showLocationSelectionSheet by remember { mutableStateOf(false) }

    // Observe Cart items and their count
    val cartItems by cartRepository.allCartItems.collectAsState(initial = emptyList())
    // Derived state: calculate total quantity of all items in cart
    val cartCount = cartItems.sumOf { it.quantity }

    // Permission Handler
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms.values.any { it }
        if (granted) {
            locationViewModel.resolveAndSaveLocation()
        } else {
            // Check if user denied permanently
            val activity = context as Activity
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (!showRationale) {
                showPermissionSettingsDialog = true
            } else {
                // Denied once, trigger resolution (which will use Pune fallback)
                locationViewModel.resolveAndSaveLocation()
            }
        }
    }

    // 5-Minute Refresh & Initial Load Logic
    LaunchedEffect(Unit) {
        while (true) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                // This updates DataStore, which triggers HomeViewModel automatically
                locationViewModel.resolveAndSaveLocation()
            } else if (savedLocation == null) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            // Wait 5 minutes before checking/refreshing again
            delay(5 * 60 * 1000)
        }
    }

    val locationDisplayed = savedLocation?.name ?: "Detecting..."
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(sessionManager)
    )


    // Permanent Denial Dialog
//    if (showPermissionSettingsDialog) {
//        AlertDialog(
//            onDismissRequest = { showPermissionSettingsDialog = false },
//            title = {
//                Text("Location Permission")
//            },
//            text = {
//                Text("We need location access to find nearby restaurants. Please enable it in settings or use default location.")
//            },
//            confirmButton = {
//                TextButton(onClick = {
//                    val intent = Intent(
//                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    ).apply {
//                        data = Uri.fromParts(
//                            "package",
//                            context.packageName,
//                            null
//                        )
//                    }
//                    context.startActivity(intent)
//                    showPermissionSettingsDialog = false
//                }) { Text("Open Settings") }
//            },
//            dismissButton = {
//                TextButton(onClick = {
//                    locationViewModel.resolveAndSaveLocation() // This will trigger Pune fallback
//                    showPermissionSettingsDialog = false
//                }) { Text("Use Pune") }
//            }
//        )
//    }


    fun isTabRoot(): Boolean {
        if (currDestination == null) return false

        // Check if the current destination is any of our "Root" tab screens
        return currDestination.hasRoute(Home::class) ||
                currDestination.hasRoute(Profile::class) ||
                currDestination.hasRoute(Settings::class)
    }

    fun navigateToTab(route: Any) {
        mainAppNavCtl.navigate(route) {
            // Pop up to the very first destination (HomeTab)
            popUpTo(mainAppNavCtl.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }


    fun getTopBarTitle(): String {
        return when {
            currDestination?.hasRoute(RestaurantDetails::class) == true -> {
                // Extracting args from the backstack entry
                val args = currBackStackState?.toRoute<RestaurantDetails>()
                args?.restoName ?: "Restaurant Info"
            }

            currDestination?.hasRoute(Cart::class) == true -> "My Cart"
            currDestination?.hasRoute(Profile::class) == true -> "My Profile"
            currDestination?.hasRoute(Settings::class) == true -> "Settings"
            currDestination?.hasRoute(Home::class) == true -> "Zwiggy"
            else -> "Zwiggy"
        }
    }


    @Composable
    fun BottomNavBar() {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            val navItems = listOf(
                Triple(HomeTab, "Home", painterResource(R.drawable.ic_home)),
                Triple(ProfileTab, "Profile", painterResource(R.drawable.ic_user)),
                Triple(SettingsTab, "Settings", painterResource(R.drawable.ic_settings))
            )
            val tabStartDestinations = listOf(Home, ProfileTab, SettingsTab)

            navItems.forEachIndexed { index, (tab, label, icon) ->
                val isSelected = currDestination?.hierarchy?.any { it.hasRoute(tab::class) } == true
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navigateToTab(tabStartDestinations[index])
                    },
                    icon = {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant

                        )
                    },
                    label = {
                        Text(
                            text = label,
                            // Persist primary color and add weight when selected
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        // This is the "Halo" / Pill background
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        // Unselected states
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }


    // ENTIRE PAGE'S LAYOUT
    Scaffold(
        topBar = {
            // Dynamically set TopBar
            CustomTopBar(
                title = getTopBarTitle(),
                isHomeRoute = currDestination?.hasRoute(Home::class) == true,
                isCartRoute = currDestination?.hasRoute(Cart::class) == true,
                showBack = !(isTabRoot()),
                cartCount = cartCount,
                displayedLocation = locationDisplayed,
                onBackClick = { mainAppNavCtl.navigateUp() },
                onCartClick = { mainAppNavCtl.navigate(Cart) },
                onLocationClick = { showLocationSelectionSheet = true }
            )
        },
        bottomBar = { BottomNavBar() }
    ) { innerPadding ->
        NavHost(
            navController = mainAppNavCtl,
            startDestination = HomeTab, // Default tab
            modifier = Modifier.padding(innerPadding),
            // Navigation Transition Animations
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            // HOME-TAB branch
            navigation<HomeTab>(
                startDestination = Home // Default screen within Default tab
            ) {
                // HomeScreen
                composable<Home> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onRestaurantClick = { restoId: String, restoName: String ->
                            mainAppNavCtl.navigate(
                                RestaurantDetails(
                                    restoId = restoId,
                                    restoName = restoName
                                )
                            )
                        }
                    )
                }
                // Restaurant Details screen
                composable<RestaurantDetails> { backStackEntry ->
                    val args: RestaurantDetails = backStackEntry.toRoute()
                    RestaurantDetailsScreen(
                        restoId = args.restoId,
                        cartRepository = cartRepository,
                        sessionManager = sessionManager
                    )
                }
            }
            // PROFILE-TAB branch
            navigation<ProfileTab>(startDestination = Profile) {
                composable<Profile> { ProfileScreen() }
            }
            // SETTINGS-TAB branch
            navigation<SettingsTab>(startDestination = Settings) {
                composable<Settings> { SettingsScreen() }
            }
            // CART page (Independent leaf)
            composable<Cart> {
                CartScreen(
                    cartRepository = cartRepository,
                    sessionManager = sessionManager,
                    onNavigateBack = { mainAppNavCtl.popBackStack() },
                    cartEmptyRedirect = {
                        mainAppNavCtl.navigate(Home) {
                            // Good practice: Clear backstack when redirecting to Home from empty cart
                            popUpTo(Home) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
    // LOCATION SELECTION POP-UP
    if (showLocationSelectionSheet) {
        LocationSelectionSheet(
            viewModel = locationViewModel,
            onDismiss = { showLocationSelectionSheet = false }
        )
    }
}


