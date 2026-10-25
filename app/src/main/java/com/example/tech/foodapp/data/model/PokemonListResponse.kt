package com.example.tech.foodapp.data.model

import kotlinx.serialization.Serializable


@Serializable
data class PokemonListResponse(
    val results: List<PokemonListItemDto>
)

@Serializable
data class PokemonListItemDto(
    val name: String,
    val url: String // Format: "https://pokeapi.co/api/v2/pokemon/1/"
)

@Serializable
data class PokemonListItem(
    val id: Int,
    val name: String,
    val imgUrl: String,
)