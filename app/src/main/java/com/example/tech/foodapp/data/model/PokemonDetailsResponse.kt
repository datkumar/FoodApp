package com.example.tech.foodapp.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailsResponse(
    val id: Long,
    val name: String,
    val weight: Long,
    val height: Long,
    val sprites: Sprites,
    val stats: List<Stat>,
    val types: List<Type>,
    val cries: Cries,
    val abilities: List<Ability>,

//    @SerialName("base_experience") val baseExperience: Long,
//    val forms: List<Species>,
//    @SerialName("game_indices")  val gameIndices: List<GameIndex>,
//    @SerialName("held_items") val heldItems: List<HeldItem>,
//    @SerialName("is_default") val isDefault: Boolean,
//    @SerialName("location_area_encounters") val locationAreaEncounters: String,
//    val moves: List<Move>,
//    val order: Long,
//    @SerialName("past_abilities") val pastAbilities: List<PastAbility>,
//    @SerialName("past_stats") val pastStats: List<PastStat>,
//    @SerialName("past_types") val pastTypes: JsonArray,
//    val species: Species
)


@Serializable
data class Type(
    val slot: Long,
    val type: Species
)

@Serializable
data class Species(
    val name: String,
    val url: String
)

@Serializable
data class Cries(
    val latest: String,
    val legacy: String
)

@Serializable
data class Sprites(
    @SerialName("back_default") val backDefault: String?,
    @SerialName("back_female") val backFemale: String?,
    @SerialName("back_shiny") val backShiny: String?,
    @SerialName("back_shiny_female") val backShinyFemale: String? = null,
    @SerialName("front_default") val frontDefault: String?,
    @SerialName("front_female") val frontFemale: String?,
    @SerialName("front_shiny") val frontShiny: String?,
    @SerialName("front_shiny_female") val frontShinyFemale: String?,
//    val other: Other? = null,
    val animated: Sprites? = null
//    val versions: Versions? = null,
)

//@Serializable
//data class DreamWorld(
//    @SerialName("front_default") val frontDefault: String,
//    @SerialName("front_female") val frontFemale: String? = null
//)
//
//@Serializable
//data class Home(
//    @SerialName("front_default") val frontDefault: String,
//    @SerialName("front_female") val frontFemale: String,
//    @SerialName("front_shiny") val frontShiny: String,
//    @SerialName("front_shiny_female") val frontShinyFemale: String
//)

@Serializable
data class Ability(
    val ability: Species? = null,
    @SerialName("is_hidden") val isHidden: Boolean,
    val slot: Long
)


//@Serializable
//data class Other(
//    @SerialName("dream_world") val dreamWorld: DreamWorld,
//    val home: Home,
//    @SerialName("official-artwork") val officialArtwork: OfficialArtwork,
//    val showdown: Sprites? = null
//)


//@Serializable
//data class OfficialArtwork(
//    @SerialName("front_default") val frontDefault: String,
//    @SerialName("front_shiny") val frontShiny: String
//)

@Serializable
data class Stat(
    @SerialName("base_stat") val baseStat: Long,
    val effort: Long,
    val stat: Species
)

//@Serializable
//data class GameIndex(
//    @SerialName("game_index")
//    val gameIndex: Long,
//
//    val version: Species
//)

//@Serializable
//data class HeldItem(
//    val item: Species,
//
//    @SerialName("version_details")
//    val versionDetails: List<VersionDetail>
//)

//@Serializable
//data class VersionDetail(
//    val rarity: Long,
//    val version: Species
//)

//@Serializable
//data class Move(
//    val move: Species,
//
//    @SerialName("version_group_details")
//    val versionGroupDetails: List<VersionGroupDetail>
//)

//@Serializable
//data class VersionGroupDetail(
//    @SerialName("level_learned_at")
//    val levelLearnedAt: Long,
//
//    @SerialName("move_learn_method")
//    val moveLearnMethod: Species,
//
//    val order: Long? = null,
//
//    @SerialName("version_group")
//    val versionGroup: Species
//)

//@Serializable
//data class PastAbility(
//    val abilities: List<Ability>,
//    val generation: Species
//)

//@Serializable
//data class PastStat(
//    val generation: Species,
//    val stats: List<Stat>
//)


//@Serializable
//data class GenerationV(
//    @SerialName("black-white") val blackWhite: Sprites
//)

//@Serializable
//data class GenerationIv(
//    @SerialName("diamond-pearl") val diamondPearl: Sprites,
//    @SerialName("heartgold-soulsilver")val heartGoldSoulSilver: Sprites,
//    val platinum: Sprites
//)

//@Serializable
//data class Versions(
//    @SerialName("generation-i")
//    val generationI: GenerationI,
//
//    @SerialName("generation-ii")
//    val generationIi: GenerationIi,
//
//    @SerialName("generation-iii")
//    val generationIii: GenerationIii,
//
//    @SerialName("generation-iv")
//    val generationIv: GenerationIv,
//
//    @SerialName("generation-ix")
//    val generationIx: GenerationIx,
//
//    @SerialName("generation-v")
//    val generationV: GenerationV,
//
//    @SerialName("generation-vi")
//    val generationVi: Map<String, Home>,
//
//    @SerialName("generation-vii")
//    val generationVii: GenerationVii,
//
//    @SerialName("generation-viii")
//    val generationViii: GenerationViii
//)


//@Serializable
//data class GenerationI(
//    @SerialName("red-blue")val redBlue: RedBlue,
//    val yellow: RedBlue
//)

//@Serializable
//data class RedBlue(
//    @SerialName("back_default") val backDefault: String,
//    @SerialName("back_gray") val backGray: String,
//    @SerialName("back_transparent") val backTransparent: String,
//    @SerialName("front_default") val frontDefault: String,
//    @SerialName("front_gray") val frontGray: String,
//    @SerialName("front_transparent") val frontTransparent: String
//)

//@Serializable
//data class GenerationIi(
//    val crystal: Crystal,
//    val gold: Gold,
//    val silver: Gold
//)

//@Serializable
//data class Crystal(
//    @SerialName("back_default") val backDefault: String,
//    @SerialName("back_shiny") val backShiny: String,
//    @SerialName("back_shiny_transparent") val backShinyTransparent: String,
//    @SerialName("back_transparent") val backTransparent: String,
//    @SerialName("front_default") val frontDefault: String,
//    @SerialName("front_shiny") val frontShiny: String,
//    @SerialName("front_shiny_transparent") val frontShinyTransparent: String,
//    @SerialName("front_transparent") val frontTransparent: String
//)

//@Serializable
//data class Gold(
//    @SerialName("back_default")
//    val backDefault: String,
//
//    @SerialName("back_shiny")
//    val backShiny: String,
//
//    @SerialName("front_default")
//    val frontDefault: String,
//
//    @SerialName("front_shiny")
//    val frontShiny: String,
//
//    @SerialName("front_transparent")
//    val frontTransparent: String? = null
//)

//@Serializable
//data class GenerationIii(
//    val emerald: OfficialArtwork,
//
//    @SerialName("firered-leafgreen")
//    val fireredLeafgreen: Gold,
//
//    @SerialName("ruby-sapphire")
//    val rubySapphire: Gold
//)


//@Serializable
//data class GenerationIx(
//    @SerialName("scarlet-violet")
//    val scarletViolet: DreamWorld
//)


//@Serializable
//data class GenerationVii(
//    val icons: DreamWorld,
//    @SerialName("ultra-sun-ultra-moon") val ultraSunUltraMoon: Home
//)

//@Serializable
//data class GenerationViii(
//    @SerialName("brilliant-diamond-shining-pearl")
//    val brilliantDiamondShiningPearl: DreamWorld,
//
//    val icons: DreamWorld
//)


