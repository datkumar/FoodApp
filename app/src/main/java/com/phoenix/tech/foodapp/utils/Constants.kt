package com.phoenix.tech.foodapp.utils

object AppKeys {
    // DataStore SharedPrefs and Local DB names
    const val PREFS_STORE = "PREFS_STORE"
    const val DB_NAME = "FOOD_APP_DB"

    // Room DB Entity Table names
    const val DB_TABLE_RESTAURANTS = "restaurants"
    const val DB_TABLE_CART_ITEMS = "cart_items"
    const val DB_COL_RESTO_ID = "restoId"

    // User Data key names
    const val USER_TOKEN = "USER_TOKEN"
    const val USER_EMAIL = "USER_EMAIL"
    const val USER_PHONE = "USER_PHONE"
    const val USER_USERNAME = "USER_USERNAME"
    const val USER_ROLE = "USER_ROLE"
    const val USER_PFP = "USER_PFP" // Profile Picture
    const val USER_FIRSTNAME = "USER_FIRSTNAME"
    const val USER_LASTNAME = "USER_LASTNAME"
    const val USER_ORG = "USER_ORG"

    // Location Data key names
    const val LOC_NAME = "LOC_NAME"
    const val LOC_LAT = "LOC_LAT"
    const val LOC_LONG = "LOC_LONG"
    const val LOC_IS_AUTO = "LOC_IS_AUTO"
}

class RestoSharedElemAnimateKeys(restoId: String) {
    val restoName = "restoName-$restoId"
    val img = "restoImg-$restoId"
}

const val GEOCODE_BASE_URL = "https://api.geoapify.com/v1/geocode/"
const val SWIGGY_BASE_URL = "https://www.swiggy.com/"
const val RESTO_IMG_BASE_URL =
    "https://media-assets.swiggy.com/swiggy/image/upload/fl_lossy,f_auto,q_auto,w_660/"
const val POKE_BASE_URL = "https://pokeapi.co/api/v2/"
const val POKE_IMG_BASE_URL =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"