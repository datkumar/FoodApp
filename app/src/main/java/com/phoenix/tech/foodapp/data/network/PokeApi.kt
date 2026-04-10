package com.phoenix.tech.foodapp.data.network

import com.phoenix.tech.foodapp.data.model.PokemonDetailsResponse
import com.phoenix.tech.foodapp.data.model.PokemonListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApi {

    @GET("pokemon")
    suspend fun fetchPokemonList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun fetchPokemonDetails(
        @Path("id") id: Int
    ): PokemonDetailsResponse

}