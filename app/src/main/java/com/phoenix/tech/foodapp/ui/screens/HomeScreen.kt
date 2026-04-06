package com.phoenix.tech.foodapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.data.model.SampleRestoListing
import com.phoenix.tech.foodapp.ui.components.RestaurantCard
import com.phoenix.tech.foodapp.ui.components.RestaurantCardPreview
import com.phoenix.tech.foodapp.ui.state.HomeUiState
import com.phoenix.tech.foodapp.ui.state.HomeViewModel
import com.phoenix.tech.foodapp.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onRestaurantClick: (restoId: String, restoName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showExitConfirmDialog by remember { mutableStateOf(false) }


    // Handle App Exit Confirmation
    BackHandler { showExitConfirmDialog = true }

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
        isRefreshing = uiState is HomeUiState.Loading,
        onRefresh = { viewModel.refreshRestaurants() }, // force refetching
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Oops! No restaurants found at this location",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
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


            }

            is HomeUiState.Error -> {
                // TODO: Error UI here

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error fetching restaurants")
                }
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