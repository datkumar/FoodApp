package com.phoenix.tech.foodapp.utils

object AppKeys {
    const val PREFS_STORE = "PREFS_STORE"
    const val AUTH_TOKEN = "AUTH_TOKEN"
    const val DB_NAME = "food_app_db"
    const val LOC_NAME = "loc_name"
    const val LOC_LAT = "loc_lat"
    const val LOC_LONG = "loc_long"
    const val LOC_IS_AUTO = "loc_is_auto"
}

class RestoSharedElemAnimateKeys(private val restoId: String) {
    val restoName = "restoName-$restoId"
    val img = "restoImg-$restoId"
    val rating = "rating-$restoId"
    val slaTime = "slaTime-$restoId"
    val slaDist = "slaDist-$restoId"
}

const val GEOCODE_BASE_URL = "https://api.geoapify.com/v1/geocode/"
const val SWIGGY_BASE_URL = "https://www.swiggy.com/"
const val RESTO_IMG_BASE_URL =
    "https://media-assets.swiggy.com/swiggy/image/upload/fl_lossy,f_auto,q_auto,w_660/"
const val POKE_BASE_URL = "https://pokeapi.co/api/v2/"
const val POKE_IMG_BASE_URL =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"