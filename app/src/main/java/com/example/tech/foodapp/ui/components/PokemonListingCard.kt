package com.example.tech.foodapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tech.foodapp.R
import com.example.tech.foodapp.data.model.PokemonListItem

@Composable
fun PokemonListingCard(
    pokemon: PokemonListItem,
    onClick: () -> Unit
) {
    Card(
        onClick = { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ID Tag at Top-Right
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pokemon.imgUrl)
                        .crossfade(true) // Smooth alpha transition
                        .crossfade(250)
                        .build(),
                    contentDescription = pokemon.name,
                    contentScale = ContentScale.Fit,
                    // Placeholder to reduce layout shift
                    placeholder = painterResource(R.drawable.ic_pokeball_colored),
                    error = painterResource(R.drawable.ic_pokeball),
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    pokemon.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}