package com.phoenix.tech.foodapp.utils

object AppConstants {
    const val PREFS_STORE = "PREFS_STORE"
    const val AUTH_TOKEN = "AUTH_TOKEN"
    const val CSRF_TOKEN = "CSRF_TOKEN"
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