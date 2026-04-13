package com.phoenix.tech.foodapp.data.repository

import com.phoenix.tech.foodapp.data.model.PokemonDetailsResponse
import com.phoenix.tech.foodapp.data.network.HttpClient
import com.phoenix.tech.foodapp.data.network.PokeApi

class PokemonDetailsRepository(
    private val api: PokeApi = HttpClient.pokeApi
) {
    suspend fun getPokemonDetails(pokemonId: Int): PokemonDetailsResponse? {
        try {
            val response = api.fetchPokemonDetails(id = pokemonId)
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}