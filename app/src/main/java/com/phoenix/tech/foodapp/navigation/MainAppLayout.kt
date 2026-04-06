package com.phoenix.tech.foodapp.navigation

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.viewModelScope
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
import com.phoenix.tech.foodapp.ui.state.LocationResult
import com.phoenix.tech.foodapp.ui.state.LocationViewModel
import com.phoenix.tech.foodapp.ui.state.LocationViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


val MainLayoutPermsTag = "MAIN_LAYOUT_PERMS"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
    var showAppPermissionSettings by remember { mutableStateOf(false) }
    var showLocationSelectionSheet by remember { mutableStateOf(false) }

    // Observe Cart items and their count
    val cartItems by cartRepository.allCartItems.collectAsState(initial = emptyList())
    // Derived state: calculate total quantity of all items in cart
    val cartCount = cartItems.sumOf { it.quantity }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        Log.d(MainLayoutPermsTag, "PERMISSION STATUSES:")

        // Check what was actually requested in this specific 'launch'
        val locationRequested = perms.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)
        val cameraRequested = perms.containsKey(Manifest.permission.CAMERA)

        // Extract results


        if (locationRequested) {
            val locationGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                    perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (locationGranted) {
                // Trigger detection the moment the dialog closes
                Log.d(MainLayoutPermsTag, "#### Location granted")
                locationViewModel.viewModelScope.launch {
                    val result = locationViewModel.resolveAndSaveLocation()
                    if (result is LocationResult.GpsDisabled) {
                        Toast.makeText(
                            context,
                            "Please enable GPS in Settings for accurate results.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // USER DENIED: Now we explicitly set Pune.
                // This triggers HomeViewModel to fetch Pune restaurants.
                Log.d(MainLayoutPermsTag, "#### Location denied")
                locationViewModel.viewModelScope.launch {
                    sessionManager.setLocationDetails(
                        loc = locationViewModel.getPuneFallback()
                    )
                }
                val activity = context as Activity
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (!shouldShowRationale) {
                    showAppPermissionSettings = true
                } else {
                    Toast.makeText(
                        context,
                        "Using default location (Pune)", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        // Handle Location Result ONLY if it was asked for
//        if (locationRequested) {
//            if (locationGranted) {
//                locationViewModel.viewModelScope.launch {
//                    val current = locationViewModel.savedLocation.value
//                    // Only resolve if we don't already have a manual location set
//                    if (current == null || current.isAutoDetected) {
//                        locationViewModel.resolveAndSaveLocation()
//                    }
//                }
//            } else {
//                val activity = context as Activity
//                val shouldShowReasoning = ActivityCompat.shouldShowRequestPermissionRationale(
//                    activity, Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//                if (!shouldShowReasoning) { // User denied permanently
//                    showAppPermissionSettings = true
//                } else { // User denied once, fall back to Pune for now
//                    locationViewModel.viewModelScope.launch {
//                        locationViewModel.resolveAndSaveLocation()
//                    }
//                    Toast.makeText(
//                        context, "Using default location (Pune)",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }

        // Handle Camera Result ONLY if it was asked for
        if (cameraRequested) {
            val cameraGranted = perms[Manifest.permission.CAMERA] == true

            if (cameraGranted) {
                Log.d(MainLayoutPermsTag, "Camera Access Granted")
                Toast.makeText(context, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(MainLayoutPermsTag, "Camera Access Denied")
                Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(
        hasLocationPermission // React to location permission changes
    ) {
        val currentLoc = sessionManager.userLocation.first()
        Log.d(
            MainLayoutPermsTag,
            "#### Location permission is already granted" + hasLocationPermission
        )
        if (hasLocationPermission) {
            // Even if permission is "Granted" (or "Only this time"),
            // the hardware GPS might be toggled off.
            val result = locationViewModel.resolveAndSaveLocation()

            if (result is LocationResult.GpsDisabled) {
                Toast.makeText(
                    context,
                    "Location Services are OFF. Using fallback value",
                    Toast.LENGTH_LONG
                ).show()
            }

//            val currentLoc = sessionManager.userLocation.first()
//            if (locationViewModel.shouldAutoUpdateLoc(currentLoc)) {
//                locationViewModel.resolveAndSaveLocation()
//            }
        } else {
            // Prompt for permission if we have no location yet
//            if (currentLoc == null) {
            Log.d(MainLayoutPermsTag, "#### Location permission not given")
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
//            } else {
//                Log.d(MainLayoutPermsTag, "Current location is not nil:" + currentLoc)
//            }


//            val currentLoc = sessionManager.userLocation.first()
//            if (currentLoc == null) {
//                permissionLauncher.launch(
//                    arrayOf(
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    )
//                )
//            }
        }

        // Enter periodic 5-min location refresh loop
        while (true) {
            delay(5 * 60 * 1000)
            if (hasLocationPermission) {
                locationViewModel.resolveAndSaveLocation()
            }
        }


//        // 1. Run IMMEDIATELY on launch or permission grant
//        val initialLoc = sessionManager.userLocation.first()
//        if (hasLocationPermission) {
//            if (locationViewModel.shouldAutoUpdateLoc(initialLoc)) {
//                locationViewModel.resolveAndSaveLocation()
//            }
//        } else if (initialLoc == null) {
//            // Trigger prompt for new users
//            permissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        }
//        // 2. Then enter the periodic loop
//        while (true) {
//            delay(5 * 60 * 1000)
//            val currentLoc = sessionManager.userLocation.first()
//            if (hasLocationPermission && locationViewModel.shouldAutoUpdateLoc(currentLoc)) {
//                locationViewModel.resolveAndSaveLocation()
//            }
//        }

//        while (true) {
//            // Always get the freshest snapshot from DataStore
//            val currentLoc = sessionManager.userLocation.first()
//
//            if (hasLocationPermission) {
//                // Trigger update if it's currently Null, Pune, or an old Auto-Detected loc
//                if (locationViewModel.shouldAutoUpdateLoc(currentLoc)) {
//                    Log.d(MainLayoutPermsTag, "Executing location fetch (Periodic/Initial)")
//                    // Attempt to detect and save latest current location
//                    locationViewModel.resolveAndSaveLocation()
//                }
//            } else { // No Permission
//
//                // If completely new user (null), trigger the prompt
//                if (currentLoc == null) {
//                    permissionLauncher.launch(
//                        arrayOf(
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION
//                        )
//                    )
//                    // We break here. The Launcher result or a manual setting
//                    // will change the state and re-trigger this effect.
//                    break
//                }
//                // If currentLoc is already Pune (from a previous denial), just stay silent.
//                break
//            }
//            // The 5-minute heartbeat
//            delay(5 * 60 * 1000)
//        }

//            val savedLoc = locationViewModel.savedLocation.value
//            val defaultLoc = locationViewModel.getPuneFallback()

//            val isLocEmptyOrDefault = (savedLoc == null) ||
//                    (savedLoc.latitude == defaultLoc.latitude && savedLoc.longitude == defaultLoc.longitude)
//
//            val shouldUpdateLoc = (isLocEmptyOrDefault || savedLoc.isAutoDetected)
//
//            if (hasPermission) {
//                if (shouldUpdateLoc) {
//                    // ATTEMPT TO DETECT LOCATION AND SAVE
//                    locationViewModel.resolveAndSaveLocation()
//                }
//            } else {
//                // If we don't have permission, and we are still at default/null, so
//                // Trigger the Permission Request prompt (handles "Ask every time" and fresh installs)
//                if (isLocEmptyOrDefault) {
//                    permissionLauncher.launch(
//                        arrayOf(
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION
//                        )
//                    )
//                }
//            }
//            delay(5 * 60 * 1000) // Wait 5min before next attempt
//        }
    }

    val requestCameraPermission = {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        } else {
            // Already have it! Logic to open scanner/camera goes here
            Log.d(MainLayoutPermsTag, "Camera access already granted. Open camera here...")
        }
    }

    val locationDisplayed = savedLocation?.name ?: "Detecting..."
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(sessionManager)
    )


    fun isTabRoot(): Boolean {
        if (currDestination == null) return false

        // Check if the current destination is any of our "Root" tab screens
        return currDestination.hasRoute(Home::class) ||
                currDestination.hasRoute(Profile::class) ||
                currDestination.hasRoute(AppSettings::class)
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
            currDestination?.hasRoute(AppSettings::class) == true -> "Settings"
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
                composable<Profile> {
                    ProfileScreen(
                        onRequestCameraPermission = { requestCameraPermission() },
                        onNavigateBack = { mainAppNavCtl.popBackStack() }
                    )
                }
            }
            // SETTINGS-TAB branch
            navigation<SettingsTab>(startDestination = AppSettings) {
                composable<AppSettings> { SettingsScreen() }
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
    // PERMISSION SETTINGS REDIRECT DIALOG
    if (showAppPermissionSettings) {
        AlertDialog(
            onDismissRequest = { showAppPermissionSettings = false },
            title = {
                Text("Location Permission Required")
            },
            text = {
                Text("You have permanently denied location access. To detect nearby restaurants, please enable it in the App Settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAppPermissionSettings = false
                        // Open System Settings for this specific app
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        ).apply {
                            data = Uri.fromParts(
                                "package",
                                context.packageName,
                                null
                            )
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppPermissionSettings = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    // LOCATION SELECTION POP-UP
    if (showLocationSelectionSheet) {
        LocationSelectionSheet(
            viewModel = locationViewModel,
            onDismiss = { showLocationSelectionSheet = false }
        )
    }
}


