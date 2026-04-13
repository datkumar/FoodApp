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
    ): Int? {
        // This ensures that when you pull to refresh,
        // it tries to find the position you were looking at
        // or just starts from the beginning (0)
        return state.anchorPosition
            ?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(state.config.pageSize)
                    ?: anchorPage?.nextKey?.minus(state.config.pageSize)
            }
    }

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, PokemonListItem> {
        try {
            val offset = params.key ?: 0
            val response = api.fetchPokemonList(limit = params.loadSize, offset = offset)

            // Map the API response to your UI model
            val mappedPokemons = response.results.mapNotNull { entry ->
                // Extract ID from Details URL
                val id = entry.url.split("/").filter { it.isNotEmpty() }.last().toInt()
                // As we only want standard Pokemons, skip IDs > 10000
                if (id > 10000) return@mapNotNull null

                PokemonListItem(
                    id = id,
                    name = formattedPokemonName(entry.name),
                    imgUrl = "$POKE_IMG_BASE_URL/$id.png" // Prepend image base-url
                )
            }
            return LoadResult.Page(
                data = mappedPokemons,
                prevKey = if (offset == 0) null else offset - params.loadSize,
                nextKey = if (mappedPokemons.isEmpty() || offset >= 1025) null else offset + params.loadSize
            )

        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}