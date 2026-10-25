package com.example.tech.foodapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.tech.foodapp.data.repository.PokemonListRepository
import com.example.tech.foodapp.ui.components.PokemonListingCard
import com.example.tech.foodapp.ui.state.PokemonListViewModel
import com.example.tech.foodapp.ui.state.PokemonListViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun PokemonListingScreen(
    viewModel: PokemonListViewModel = viewModel(
        factory = PokemonListViewModelFactory(PokemonListRepository())
    ),
    onPokeCardClick: (id: Int, name: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    // Collect Paging State
    val pagingItems = viewModel.pokemonPagingFlow.collectAsLazyPagingItems()
    // Refresh state
    val refreshState = rememberPullToRefreshState()
    // To keep track of how far down we've scrolled
    val gridState = rememberLazyGridState()

    // Determine if we should show the "Scroll to Top" button
    val showScrollToTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 12 }
    }

    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    // Clear the data and start over
                    pagingItems.refresh()
                    // Can also wait a tiny bit or wait for the state to change
                    // Immediately snap the scroll position back to the top
                    gridState.scrollToItem(0)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Dim the list slightly while refreshing to show "work" is happening
                        alpha = if (isRefreshing) 0.5f else 1.0f
                    },
            ) {
                // Loaded pokemon entries
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey { it.id }
                ) { index ->
                    pagingItems[index]?.let { pokemon ->
                        PokemonListingCard(
                            pokemon = pokemon,
                            onClick = {
                                onPokeCardClick(pokemon.id, pokemon.name)
                            }
                        )
                    }
                }

                // Append loading (pagination) spinner
                if (pagingItems.loadState.append is LoadState.Loading) {
                    item(
                        span = { GridItemSpan(2) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            CircularWavyProgressIndicator(
                                Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        // Scroll to Top FAB
        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        gridState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                contentColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text("Scroll to Top")
            }
        }
    }
}