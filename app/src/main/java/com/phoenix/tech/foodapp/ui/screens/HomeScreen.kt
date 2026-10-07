package com.phoenix.tech.foodapp.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.ui.components.ErrorPage
import com.phoenix.tech.foodapp.ui.components.NotFoundPage
import com.phoenix.tech.foodapp.ui.components.RestaurantCard
import com.phoenix.tech.foodapp.ui.state.HomeUiState
import com.phoenix.tech.foodapp.ui.state.HomeViewModel

private const val gridPadding = 8

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onRestaurantClick: (
        restoId: String, restoName: String, imgId: String,
        avgRating: Double, totalRatingsString: String,
        slaTime: String, slaDistance: String,
        cuisines: String,
    ) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Handle App Exit Confirmation
    BackHandler { showExitConfirmDialog = true }

    if (showExitConfirmDialog) {
        val localActivity = LocalActivity.current
        AlertDialog(
            title = {
                Text(
                    "Exit App",
                    style = MaterialTheme.typography.displayMedium
                )
            },
            text = { Text("Are you sure you want to leave Zwiggy?") },
            onDismissRequest = { showExitConfirmDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // This actually exits the activity
                        localActivity?.finishAndRemoveTask()
                    }
                ) {
                    Text("Yes I'm sure")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            },
        )
    }
    PullToRefreshBox(
        isRefreshing = uiState is HomeUiState.Loading,
        onRefresh = { viewModel.hardRefreshRestaurants() }, // force refetching
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                // TODO: Loading Skeleton UI
                // This will now show while the permission prompt is open
                // because the location in DataStore is still 'null'.
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.waiter),
                        contentDescription = "Waiter fetching...",
                        modifier = Modifier.height(200.dp),
                        contentScale = ContentScale.FillHeight,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Fetching Restaurants...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is HomeUiState.Success -> {
                if (state.restoListingItems.isEmpty()) {
                    NotFoundPage(msg = "Oops! No restaurants found here")
                } else {
                    // LazyVerticalGrid needs an explicit state object so we can
                    // read scroll position. Without this we have no way to know
                    // how far the user has scrolled.
                    val listState = rememberLazyGridState()

                    // derivedStateOf watches the scroll position and
                    // flips to true when the last visible item is within 3 of
                    // the end of the list. derivedStateOf is used here (instead
                    // of reading listState directly) so that recomposition only
                    // triggers when the boolean result actually flips, not on
                    // every single scroll pixel.
                    val shouldLoadMore by remember {
                        derivedStateOf {
                            val lastVisible =
                                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val total = listState.layoutInfo.totalItemsCount
                            total > 0 && lastVisible >= total - 3
                        }
                    }

                    // LaunchedEffect runs a side-effect tied to the
                    // value of shouldLoadMore. Whenever shouldLoadMore flips to
                    // true (user scrolled near the end) it calls loadMoreRestaurants().
                    // When shouldLoadMore flips back to false (new items pushed
                    // the end further away) nothing happens.
                    LaunchedEffect(shouldLoadMore) {
                        if (shouldLoadMore) viewModel.loadMoreRestaurants()
                    }

                    LazyVerticalGrid(
                        state = listState,
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(gridPadding.dp)
                    ) {
                        items(state.restoListingItems) { restaurant ->
                            RestaurantCard(
                                restaurant = restaurant.info,
                                onRestaurantClick = {
                                    onRestaurantClick(
                                        restaurant.info.id,
                                        restaurant.info.name,
                                        restaurant.info.cloudinaryImageId,
                                        restaurant.info.avgRating,
                                        restaurant.info.totalRatingsString,
                                        restaurant.info.sla.slaString,
                                        restaurant.info.sla.lastMileTravelString,
                                        restaurant.info.cuisines.joinToString()
                                    )
                                },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                modifier = Modifier.padding(gridPadding.dp),
                            )
                            Spacer(Modifier.height(5.dp))
                        }
                        // An extra item at the bottom of the grid
                        // that only appears while isLoadingMore is true.
                        // GridItemSpan(maxLineSpan) makes it stretch the full
                        // width instead of fitting into one column cell.
                        if (state.isLoadingMore) {
                            item(
                                span = { GridItemSpan(maxLineSpan) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularWavyProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }

            is HomeUiState.Error -> {
                ErrorPage(msg = "An error occurred while fetching restaurants")
            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    val context = LocalContext.current
//    val sampleRestaurant = SampleRestoListing.sampleRestoListingItem(context)
//    val restaurantList = List(10) { sampleRestaurant }
//
//    AppTheme(darkTheme = false) {
//        Scaffold() { contentPadding ->
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(1),
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(gridPadding.dp)
//            ) {
//                items(restaurantList) { restaurant ->
//                    RestaurantCardPreview(
//                        modifier = Modifier.padding(gridPadding.dp)
//                    )
//                }
//            }
//
//        }
//    }
//}