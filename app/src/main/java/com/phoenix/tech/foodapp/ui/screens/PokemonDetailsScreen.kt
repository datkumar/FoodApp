package com.phoenix.tech.foodapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.data.repository.PokemonDetailsRepository
import com.phoenix.tech.foodapp.ui.components.ErrorPage
import com.phoenix.tech.foodapp.ui.state.PokemonDetailsUiState
import com.phoenix.tech.foodapp.ui.state.PokemonDetailsViewModel
import com.phoenix.tech.foodapp.ui.state.PokemonDetailsViewModelFactory

@Composable
fun PokemonDetailsScreen(
    id: Int, name: String,
    viewModel: PokemonDetailsViewModel = viewModel(
        factory = PokemonDetailsViewModelFactory(PokemonDetailsRepository())
    ),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(id) {
        viewModel.getPokemonDetails(id)
    }

    when (val state = uiState) {
        is PokemonDetailsUiState.Success -> {
            Column {
                AsyncImage(
                    model = state.pokemonDetails.sprites.frontDefault,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
                // Loop through stats, types, etc.
            }
        }

        is PokemonDetailsUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_pokeball_colored),
                    contentDescription = "Pokeball Icon",
                    modifier = Modifier.height(200.dp),
                    contentScale = ContentScale.FillHeight,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Fetching Pokemon details...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        is PokemonDetailsUiState.Error -> {
            ErrorPage("Could not fetch $name information")
        }
    }
}