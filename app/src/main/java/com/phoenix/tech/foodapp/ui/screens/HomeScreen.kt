package com.phoenix.tech.foodapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.phoenix.tech.foodapp.data.model.SampleRestoListing
import com.phoenix.tech.foodapp.ui.components.RestaurantCard
import com.phoenix.tech.foodapp.ui.components.RestaurantCardPreview
import com.phoenix.tech.foodapp.ui.state.HomeUiState
import com.phoenix.tech.foodapp.ui.state.HomeViewModel
import com.phoenix.tech.foodapp.ui.theme.AppTheme

/*
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRestaurantClick: (restoId: String, restoName: String) -> Unit
) {
    // 1. Collect the paging items
    val restoItems = viewModel.restoPagingFlow.collectAsLazyPagingItems()
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // 2. Check loading state for the PullToRefreshBox
    val isRefreshing = restoItems.loadState.refresh is LoadState.Loading

    val localActivity = LocalActivity.current

    BackHandler { showExitConfirmDialog = true }

    if (showExitConfirmDialog) {
        ExitAlertDialog(
            onDismiss = {
                // Hide the dialog
                showExitConfirmDialog = false
            },
            onClick = {
                // Close current open Activity i.e. the app
                localActivity?.finishAndRemoveTask()
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { restoItems.refresh() }, // Paging 3 built-in refresh
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            // 3. Render Items
            items(
                count = restoItems.itemCount,
                key = restoItems.itemKey { it.info.id }
            ) { index ->
                val restaurant = restoItems[index]
                if (restaurant != null) {
                    RestaurantCard(
                        restaurant = restaurant.info,
                        onRestaurantClick = {
                            onRestaurantClick(restaurant.info.id, restaurant.info.name)
                        },
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            // Handle "Loading More" (Append) state
            if (restoItems.loadState.append is LoadState.Loading) {
                item(span = { GridItemSpan(2) }) {
                    CircularWavyProgressIndicator()
                }
            }

            // Handle "Initial Load" Errors
            if (restoItems.loadState.refresh is LoadState.Error) {
                item(span = { GridItemSpan(2) }) {
//                    ErrorUI(onRetry = { restoItems.retry() })
                    Text("Error in item")
                }
            }
        }

        //  Handle Empty State
        if (restoItems.itemCount == 0 && !isRefreshing) {
            CircularWavyProgressIndicator()
        }
    }
}

@Composable
fun ExitAlertDialog(
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onClick) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Exit App") },
        text = { Text("Are you sure you want to leave Zwiggy?") }
    )
}
*/




@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onRestaurantClick: (restoId: String, restoName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Pull to Refresh Fix
    val pullToRefreshState = rememberPullToRefreshState()

    // Check if VM is currently loading to sync the indicator
    val isRefreshing = uiState is HomeUiState.Loading

    // The PullToRefreshBox handles the "Refreshing" spinner internally
    // based on this boolean.
//    val isRefreshing = uiState is HomeUiState.kt.Loading && (uiState as? HomeUiState.kt.Loading)?.isPullToRefresh == true

    // Handle App Exit Confirmation
    BackHandler {  showExitConfirmDialog = true }

    if (showExitConfirmDialog) {
        val localActivity = LocalActivity.current
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // This actually exits the activity
                        localActivity?.finishAndRemoveTask()
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Exit App") },
            text = { Text("Are you sure you want to leave Zwiggy?") }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshRestaurants() }, // force refetching
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                // TODO: Shimmering skeleton UI
            }

            is HomeUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(state.restoListingItems) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant.info,
                            onRestaurantClick = {
                                onRestaurantClick(
                                    restaurant.info.id,
                                    restaurant.info.name,
                                )
                            },
                            modifier = Modifier.padding(
                                horizontal = 6.dp, vertical = 8.dp
                            )

                        )
                    }
                }
            }

            is HomeUiState.Error -> {
                // TODO: Error UI here
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val sampleRestaurant = SampleRestoListing.sampleRestoListingItem(context)
    val restaurantList = List(10) { sampleRestaurant }

    AppTheme(darkTheme = false) {
        Scaffold() { contentPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(8.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                items(restaurantList) { restaurant ->
                    RestaurantCardPreview(
//                        restaurant = restaurant.info,
                        modifier = Modifier.padding(
                            horizontal = 6.dp, vertical = 8.dp
                        )
                    )
                }
            }

        }
    }
}