package com.phoenix.tech.foodapp.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.data.model.PokemonDetailsResponse
import com.phoenix.tech.foodapp.data.model.Sprites
import com.phoenix.tech.foodapp.data.model.Type
import com.phoenix.tech.foodapp.data.repository.PokemonDetailsRepository
import com.phoenix.tech.foodapp.ui.components.ErrorPage
import com.phoenix.tech.foodapp.ui.state.PokemonDetailsUiState
import com.phoenix.tech.foodapp.ui.state.PokemonDetailsViewModel
import com.phoenix.tech.foodapp.ui.state.PokemonDetailsViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun PokemonDetailsScreen(
    id: Int, name: String,
    viewModel: PokemonDetailsViewModel = viewModel(
        factory = PokemonDetailsViewModelFactory(PokemonDetailsRepository())
    ),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Manage MediaPlayer lifecycle
    val mediaPlayer = remember { MediaPlayer() }
    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    val playSound = { url: String ->
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { it.start() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(id) {
        viewModel.getPokemonDetails(id)
    }

    when (val state = uiState) {
        is PokemonDetailsUiState.Success -> {
            PokemonDetailsContent(
                pokemon = state.pokemonDetails,
                onPlayCry = playSound
            )
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


@Composable
fun PokemonDetailsContent(
    pokemon: PokemonDetailsResponse,
    onPlayCry: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sprite Images Carousel & Audio
        item {
            PokemonSpriteCarousel(
                sprites = pokemon.sprites,
                cryUrl = pokemon.cries.latest,
                onPlayCry = onPlayCry
            )
        }
        // Title & Types
        item {
            PokemonHeader(
                name = pokemon.name,
                types = pokemon.types
            )
        }
        // Physical Traits
        item {
            PokemonPhysicalStats(
                weight = pokemon.weight,
                height = pokemon.height
            )
        }
        // Base Stats Title
        item {
            Text(
                "Base Stats",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
        // Stats as progress bars
        items(pokemon.stats) { stat ->
            PokemonStatBar(
                statName = stat.stat.name,
                value = stat.baseStat.toInt(),
                max = 255
            )
        }
        // Bottom space
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PokemonSpriteCarousel(
    sprites: Sprites,
    cryUrl: String,
    onPlayCry: (String) -> Unit
) {
    val urls = remember(sprites) {
        listOfNotNull(
            sprites.frontDefault,
            sprites.backDefault,
            sprites.frontShiny,
            sprites.backShiny
        )
    }
    val pagerState = rememberPagerState(pageCount = { urls.size })

    // Keep the auto-scroll logic inside the specific component it controls
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (urls.isNotEmpty()) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % urls.size)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        HorizontalPager(state = pagerState) { page ->
            AsyncImage(
                model = urls[page],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Audio Button
        FloatingActionButton(
            onClick = { onPlayCry(cryUrl) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                painterResource(R.drawable.ic_play),
                contentDescription = "Play Cry"
            )
        }
    }
}

@Composable
fun PokemonHeader(name: String, types: List<Type>) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { typeEntry ->
                // You can get fancy here later and change colors based on type name
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = typeEntry.type.name.uppercase(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    shape = CircleShape
                )
            }
        }
    }
}

@Composable
fun PokemonPhysicalStats(weight: Long, height: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Weight (PokeAPI stores it in hectograms, so divide by 10 for kg)
        StatItem(label = "Weight", value = "${weight / 10.0} kg")

        // Divider
        VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp)

        // Height (PokeAPI stores it in decimeters, so divide by 10 for meters)
        StatItem(label = "Height", value = "${height / 10.0} m")
    }
}


@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PokemonStatBar(
    statName: String, value: Int, max: Int = 255
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statName.uppercase().replace("-", " "),
            modifier = Modifier.weight(0.35f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .weight(0.5f)
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            // Internal Progress Fill
            val progress = value.toFloat() / max
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        // Color changes based on how high the stat is
                        when {
                            value > 120 -> Color(0xFF4CAF50) // High: Green
                            value > 60 -> Color(0xFFFFC107)  // Medium: Amber
                            else -> Color(0xFFF44336)        // Low: Red
                        }
                    )
            )
        }

        Text(
            text = value.toString(),
            modifier = Modifier.weight(0.15f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}