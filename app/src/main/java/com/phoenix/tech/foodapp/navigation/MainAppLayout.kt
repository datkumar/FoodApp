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
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.phoenix.tech.foodapp.data.network.NetworkObserver
import com.phoenix.tech.foodapp.data.repository.AuthRepository
import com.phoenix.tech.foodapp.data.repository.CartRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.ui.components.ConnectivityBanner
import com.phoenix.tech.foodapp.ui.components.CustomTopBar
import com.phoenix.tech.foodapp.ui.components.LocationSelectionSheet
import com.phoenix.tech.foodapp.ui.screens.AppSettingsScreen
import com.phoenix.tech.foodapp.ui.screens.CartScreen
import com.phoenix.tech.foodapp.ui.screens.HomeScreen
import com.phoenix.tech.foodapp.ui.screens.PokemonDetailsScreen
import com.phoenix.tech.foodapp.ui.screens.PokemonListingScreen
import com.phoenix.tech.foodapp.ui.screens.ProfileScreen
import com.phoenix.tech.foodapp.ui.screens.RestaurantDetailsScreen
import com.phoenix.tech.foodapp.ui.state.AppSettingsViewModel
import com.phoenix.tech.foodapp.ui.state.AppSettingsViewModelFactory
import com.phoenix.tech.foodapp.ui.state.HomeViewModel
import com.phoenix.tech.foodapp.ui.state.HomeViewModelFactory
import com.phoenix.tech.foodapp.ui.state.LocationResult
import com.phoenix.tech.foodapp.ui.state.LocationViewModel
import com.phoenix.tech.foodapp.ui.state.LocationViewModelFactory
import kotlinx.coroutines.launch


const val MainLayoutPermsTag = "MAIN_LAYOUT_PERMS"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    cartRepository: CartRepository,
    authRepository: AuthRepository,
    sessionManager: SessionManager,
    onLogout: () -> Unit
) {
    // Navigation state
    val mainAppNavCtl = rememberNavController()
    val currBackStackState by mainAppNavCtl.currentBackStackEntryAsState()
    val currDestination = currBackStackState?.destination
    // App Context
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Initialize the Network state observer
    val networkObserver = remember { NetworkObserver(context) }
    val isConnected by networkObserver.isConnected.collectAsStateWithLifecycle(initialValue = true)

    // Observe Cart State
    val cartItems by cartRepository.allCartItems.collectAsState(initial = emptyList())
    val cartCount = cartItems.sumOf { it.quantity }  // Total items count (Derived state)


    // ViewModel state init.
    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(sessionManager, application)
    )
    // HomeViewModel needs a reference to LocationViewModel so it can call
    // updateLocationNameFromSwiggy() after a successful restaurant fetch.
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(sessionManager, locationViewModel)
    )

    val savedLocation by locationViewModel.savedLocation.collectAsState()

    // Whether Permissions popup or Location Selection should be shown
    var showAppPermissionSettings by remember { mutableStateOf(false) }
    var showLocationSelectionSheet by remember { mutableStateOf(false) }

    // rememberLauncherForActivityResult must be called unconditionally at the
    // Composable scope level — it cannot be inside a LaunchedEffect or lambda.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        Log.d(MainLayoutPermsTag, "INSI=DE Permission Launcher")

        // ---- Location ----
        val locationWasRequested = perms.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (locationWasRequested) {
            val locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (locationGranted) {
                Log.d(MainLayoutPermsTag, "Permission callback: location GRANTED")
                // Kick off coordinate detection now that we have permission.
                // resolveAndSaveLocation() does NOT call reverse-geocoding —
                // it saves coords with "Current Location" as a placeholder name.
                // HomeViewModel will patch the real name from the Swiggy response.
                locationViewModel.viewModelScope.launch {
                    when (val result = locationViewModel.resolveAndSaveLocation()) {
                        is LocationResult.GpsDisabled ->
                            Toast.makeText(
                                context,
                                "GPS is off. Enable location services for accurate results.",
                                Toast.LENGTH_SHORT
                            ).show()

                        is LocationResult.Failure ->
                            Toast.makeText(
                                context,
                                "Couldn't detect location. Using Pune as fallback.",
                                Toast.LENGTH_SHORT
                            ).show()

                        is LocationResult.Success ->
                            Log.d(
                                MainLayoutPermsTag,
                                "Permission callback: location resolved successfully"
                            )

                        else ->
                            Log.d(
                                MainLayoutPermsTag,
                                "Permission callback: unexpected result $result"
                            )
                    }
                }
            } else {
                // User denied location. Check if they ticked "Don't ask again" —
                // shouldShowRequestPermissionRationale returns FALSE in that case.
                Log.d(MainLayoutPermsTag, "Permission callback: location DENIED")
                // Resolution is complete (denied) — unblock HomeViewModel
                locationViewModel.markResolutionComplete()

                val activity = context as Activity
                val canAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (!canAskAgain) {
                    // "Don't ask again" was selected — direct user to system settings
                    Log.d(
                        MainLayoutPermsTag,
                        "Permission callback: rationale suppressed → showing settings dialog"
                    )
                    showAppPermissionSettings = true
                } else {
                    // User dismissed the dialog but can still be asked again later
                    Log.d(
                        MainLayoutPermsTag,
                        "Permission callback: denied but can ask again → using Pune fallback"
                    )
                    Toast.makeText(
                        context, "Using default location (Pune)", Toast.LENGTH_SHORT
                    ).show()
                }
                // Apply Pune fallback so the app isn't stuck in a loading state
                locationViewModel.viewModelScope.launch {
                    sessionManager.setLocationDetails(locationViewModel.getPuneFallback())
                }
            }
        } else {
            Log.d(MainLayoutPermsTag, "Permission callback: location was not in this request")
        }

        // ---- Camera ----
        val cameraWasRequested = perms.containsKey(Manifest.permission.CAMERA)
        if (cameraWasRequested) {
            val cameraGranted = perms[Manifest.permission.CAMERA] == true
            if (cameraGranted) {
                Log.d(MainLayoutPermsTag, "Permission callback: camera GRANTED")
                Toast.makeText(context, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(MainLayoutPermsTag, "Permission callback: camera DENIED")
                Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(MainLayoutPermsTag, "Permission callback: camera was not in this request")
        }
    }

    // -------------------------------------------------------------------------
    // One-shot startup: check permission and either detect or request
    // -------------------------------------------------------------------------
    // LaunchedEffect(Unit) runs exactly once when MainAppLayout first enters
    // the composition. We do NOT use hasLocationPermission as a key because
    // checkSelfPermission is a snapshot read and won't recompose this block
    // when the permission changes at runtime — that case is handled entirely
    // inside the permissionLauncher callback above.
    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            Log.d(
                MainLayoutPermsTag,
                "Startup: permission already granted — detecting location"
            )
            // Permission was granted in a previous session.
            // Detect coordinates immediately; name will be patched by HomeViewModel.
            when (val result = locationViewModel.resolveAndSaveLocation()) {
                is LocationResult.GpsDisabled ->
                    Toast.makeText(
                        context,
                        "Location Services are OFF. Using saved/fallback location.",
                        Toast.LENGTH_LONG
                    ).show()

                is LocationResult.Failure ->
                    Log.w(
                        MainLayoutPermsTag,
                        "Startup: location detection failed, fallback applied"
                    )

                is LocationResult.Success ->
                    Log.d(MainLayoutPermsTag, "Startup: location detected successfully")

                else ->
                    Log.d(MainLayoutPermsTag, "Startup: unexpected result $result")
            }
        } else {
            Log.d(
                MainLayoutPermsTag,
                "Startup: no permission — launching request dialog"
            )
            // First-time launch (or permission was revoked).
            // The result lands in permissionLauncher's callback.
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val requestCameraPermission = {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCameraPermission) {
            Log.d(MainLayoutPermsTag, "Camera: requesting permission")
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        } else {
            // Already granted — open the camera directly
            Log.d(MainLayoutPermsTag, "Camera access already granted. Open camera here...")
        }
    }

    // What to show in the TopBar location chip.
    // Falls back to "Detecting..." while savedLocation is null (initial load).
    val locationDisplayed = savedLocation?.name ?: "Detecting..."

    fun isTabRoot(): Boolean {
        if (currDestination == null) return false
        // Check if the current destination is any of our "Root" tab screens
        return currDestination.hasRoute(HomeRoute::class) ||
                currDestination.hasRoute(ProfileRoute::class) ||
                currDestination.hasRoute(AppSettingsRoute::class) ||
                currDestination.hasRoute(PokeListingRoute::class)
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
            currDestination?.hasRoute(RestaurantDetailsRoute::class) == true -> {
                // Extracting args from the backstack entry
                val args = currBackStackState?.toRoute<RestaurantDetailsRoute>()
                args?.restoName ?: "Restaurant Info"
            }

            currDestination?.hasRoute(PokeListingRoute::class) == true -> "Pokemons"
            currDestination?.hasRoute(PokemonDetailsRoute::class) == true -> {
                val args = currBackStackState?.toRoute<PokemonDetailsRoute>()
                args?.name ?: "Pokemon Info"
            }

            currDestination?.hasRoute(CartRoute::class) == true -> "My Cart"
            currDestination?.hasRoute(ProfileRoute::class) == true -> "My Profile"
            currDestination?.hasRoute(AppSettingsRoute::class) == true -> "Settings"
            currDestination?.hasRoute(HomeRoute::class) == true -> "Zwiggy"
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
                Triple(HomeTabRoute, "Home", painterResource(R.drawable.ic_home)),
                Triple(ProfileTabRoute, "Profile", painterResource(R.drawable.ic_user)),
                Triple(SettingsTabRoute, "Settings", painterResource(R.drawable.ic_settings)),
                Triple(PokeTabRoute, "Pokemon", painterResource(R.drawable.ic_pokeball))
            )
            val tabStartDestinations = listOf(
                HomeRoute, ProfileTabRoute, SettingsTabRoute,
                PokeListingRoute
            )

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

    Box() {
        // ENTIRE PAGE'S LAYOUT
        SharedTransitionLayout { // For hosting shared-element transition animations
            Scaffold(
                topBar = {
                    // Dynamically set TopBar
                    CustomTopBar(
                        title = getTopBarTitle(),
                        isHomeRoute = currDestination?.hasRoute(HomeRoute::class) == true,
                        isCartRoute = currDestination?.hasRoute(CartRoute::class) == true,
                        showBack = !(isTabRoot()),
                        cartCount = cartCount,
                        displayedLocation = locationDisplayed,
                        onBackClick = { mainAppNavCtl.navigateUp() },
                        onCartClick = { mainAppNavCtl.navigate(CartRoute) },
                        onLocationClick = { showLocationSelectionSheet = true }
                    )
                },
                bottomBar = { BottomNavBar() }
            ) { innerPadding ->

                NavHost(
                    navController = mainAppNavCtl,
                    startDestination = HomeTabRoute, // Default tab
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
                    navigation<HomeTabRoute>(
                        startDestination = HomeRoute // Default screen within Default tab
                    ) {
                        // HomeScreen
                        composable<HomeRoute> {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onRestaurantClick = { restoId: String, restoName: String, imgId: String,
                                                      avgRating: Double, totalRatingsString: String,
                                                      slaTime: String, slaDist: String, cuisines: String ->
                                    mainAppNavCtl.navigate(
                                        RestaurantDetailsRoute(
                                            restoId = restoId,
                                            restoName = restoName,
                                            imgId = imgId,
                                            avgRating = avgRating,
                                            totalRatingsString = totalRatingsString,
                                            slaTime = slaTime,
                                            slaDist = slaDist,
                                            cuisines = cuisines
                                        )
                                    )
                                },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable,
                            )
                        }
                        // Restaurant Details screen
                        composable<RestaurantDetailsRoute> { backStackEntry ->
                            val args: RestaurantDetailsRoute = backStackEntry.toRoute()
                            RestaurantDetailsScreen(
                                restoId = args.restoId,
                                restoName = args.restoName,
                                imgId = args.imgId,
                                avgRating = args.avgRating,
                                totalRatingsString = args.totalRatingsString,
                                slaTime = args.slaTime,
                                slaDist = args.slaDist,
                                cuisines = args.cuisines,
                                cartRepository = cartRepository,
                                sessionManager = sessionManager,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable,
                            )
                        }
                    }
                    // PROFILE-TAB branch
                    navigation<ProfileTabRoute>(startDestination = ProfileRoute) {
                        composable<ProfileRoute> {
                            ProfileScreen(
                                onRequestCameraPermission = { requestCameraPermission() },
                                onNavigateBack = { mainAppNavCtl.popBackStack() }
                            )
                        }
                    }
                    // SETTINGS-TAB branch
                    navigation<SettingsTabRoute>(startDestination = AppSettingsRoute) {
                        composable<AppSettingsRoute> {
                            val appSettingsViewModel: AppSettingsViewModel = viewModel(
                                factory = AppSettingsViewModelFactory(authRepository)
                            )
                            AppSettingsScreen(
                                viewModel = appSettingsViewModel,
                                onLogoutNav = onLogout
                            )
                        }
                    }

                    // POKEMON-TAB branch
                    navigation<PokeTabRoute>(startDestination = PokeListingRoute) {
                        // Pokemon Listing screen
                        composable<PokeListingRoute> {
                            PokemonListingScreen(
                                onPokeCardClick = { id: Int, name: String ->
                                    mainAppNavCtl.navigate(PokemonDetailsRoute(id, name))
                                }
                            )
                        }
                        // Pokemon Details screen
                        composable<PokemonDetailsRoute> { backStackEntry ->
                            val args: PokemonDetailsRoute = backStackEntry.toRoute()
                            PokemonDetailsScreen(args.id, args.name)
                        }
                    }

                    // CART page (Independent leaf)
                    composable<CartRoute> {
                        CartScreen(
                            cartRepository = cartRepository,
                            sessionManager = sessionManager,
                            onNavigateBack = { mainAppNavCtl.popBackStack() },
                            cartEmptyRedirect = {
                                mainAppNavCtl.navigate(HomeRoute) {
                                    // Good practice: Clear backstack when redirecting to Home from empty cart
                                    popUpTo(HomeRoute) { inclusive = true }
                                }
                            }
                        )
                    }
                }


            }
        }

        // CONNECTIVITY BANNER (Top Layer - Outside Scaffold)
        ConnectivityBanner(isConnected = isConnected)

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

}