package com.phoenix.tech.foodapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.phoenix.tech.foodapp.data.network.HttpClient
import com.phoenix.tech.foodapp.data.network.PokeApi

class PokemonListRepository(
    // Public so PagingSource can use it
    val api: PokeApi = HttpClient.pokeApi
) {
    // The Repository now just exposes the Pager
    fun getPokemonPager() = Pager(
        config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { PokemonPagingSource(api) }
    ).flow

}