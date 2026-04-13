package com.phoenix.tech.foodapp.data.repository

import com.phoenix.tech.foodapp.data.model.PokemonListItem
import com.phoenix.tech.foodapp.data.model.PokemonListItemDto
import com.phoenix.tech.foodapp.data.network.HttpClient
import com.phoenix.tech.foodapp.data.network.PokeApi
import com.phoenix.tech.foodapp.utils.POKE_IMG_BASE_URL

class PokemonListRepository(
    private val api: PokeApi = HttpClient.pokeApi
) {
    private val cachedPokemonList = mutableListOf<PokemonListItem>()

    // Pagination cursors
    private var currentOffset = 0
    private val limit = 20
    var hasMore: Boolean = true
        private set

    suspend fun getPokemonList(forceRefresh: Boolean = false): List<PokemonListItem> {
        if (forceRefresh) {
            currentOffset = 0
            cachedPokemonList.clear()
            hasMore = true
        }
        try {
            val response = api.fetchPokemonList(
                limit = limit,
                offset = currentOffset
            )

            val mappedPokemons = response.results.map { entry: PokemonListItemDto ->
                // Extract ID from Details URL (https://pokeapi.co/api/v2/pokemon/51/ -> 51)
                val id = entry.url.split("/").last { it.isNotEmpty() }.toInt()

                // Construct Pokemon List-Item
                PokemonListItem(
                    id = id,
                    // Capitalize first letter of name
                    name = entry.name.replaceFirstChar { it.uppercase() },
                    // prepend image base url
                    imgUrl = "$POKE_IMG_BASE_URL$id.png"
                )
            }
            cachedPokemonList.addAll(mappedPokemons)

            // Move cursor ahead by current "limit" amount
            currentOffset += limit

            // If full "limit" amount of pokemons were received in current call,
            // there are more pokemons ahead
            hasMore = (mappedPokemons.size == limit)

            return cachedPokemonList.toList()

        } catch (_: Exception) {
            return emptyList()
        }
    }

}