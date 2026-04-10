package com.phoenix.tech.foodapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoenix.tech.foodapp.data.repository.PokemonListRepository
import com.phoenix.tech.foodapp.ui.components.PokemonListingCard
import com.phoenix.tech.foodapp.ui.state.PokemonListViewModel
import com.phoenix.tech.foodapp.ui.state.PokemonListViewModelFactory

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PokemonListingScreen(
    viewModel: PokemonListViewModel = viewModel(
        factory = PokemonListViewModelFactory(PokemonListRepository())
    ),
    onPokeCardClick: (id: Int, name: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isPaginating by viewModel.isPaginating.collectAsState()

    // To keep track of how far down we've scrolled
    val gridState = rememberLazyGridState()

    // Infinite Scroll Logic: Detect if we've scrolled near the end
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
            // Trigger load when 5 items away from the bottom
            lastVisibleItem != null && lastVisibleItem.index >= gridState.layoutInfo.totalItemsCount - 4
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isPaginating && !isRefreshing) {
            viewModel.loadPokemon(isRefresh = false)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadPokemon(isRefresh = true) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2), // 2 items per row
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = uiState,
                key = { it.id } // Performance optimization
            ) { pokemon ->
                PokemonListingCard(
                    pokemon = pokemon,
                    onClick = { onPokeCardClick(pokemon.id, pokemon.name) }
                )
            }

            // Show a loading spinner at the bottom when paginating
            if (isPaginating) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}