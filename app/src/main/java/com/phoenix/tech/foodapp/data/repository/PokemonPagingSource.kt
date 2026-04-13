package com.phoenix.tech.foodapp.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phoenix.tech.foodapp.data.model.PokemonListItem
import com.phoenix.tech.foodapp.data.network.PokeApi
import com.phoenix.tech.foodapp.utils.POKE_IMG_BASE_URL


private fun formattedPokemonName(name: String): String {
    return name
        .replace("-", " ") // Remove Dashes
        .replaceFirstChar { it.uppercase() } // Capitalize first letter
}

class PokemonPagingSource(
    private val api: PokeApi
) : PagingSource<Int, PokemonListItem>() {

    override fun getRefreshKey(
        state: PagingState<Int, PokemonListItem>
    ): Int {
        // ALWAYS return 0 to force every refresh to start from the first Pokemon
        return 0
    }

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, PokemonListItem> {
        try {
            // params.key is null on the very first load OR after a manual refresh
            val offset = params.key ?: 0
            val response = api.fetchPokemonList(limit = params.loadSize, offset = offset)

            // Map the API response to your UI model
            val mappedPokemons = response.results.mapNotNull { entry ->
                // Extract ID from Details URL
                val id = entry.url.split("/").filter { it.isNotEmpty() }.last().toInt()

                // Ignore the "Special Forms" IDs like 10025
                if (id > 1025) return@mapNotNull null

                PokemonListItem(
                    id = id,
                    name = formattedPokemonName(entry.name),
                    imgUrl = "$POKE_IMG_BASE_URL/$id.png" // Prepend image base-url
                )
            }
            return LoadResult.Page(
                data = mappedPokemons,
                prevKey = null, // ensures the user can NEVER scroll "up" to load more
                // Stop loading once we reach the end
                nextKey = if (mappedPokemons.isEmpty() || offset >= 1020) null else offset + params.loadSize
            )

        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}