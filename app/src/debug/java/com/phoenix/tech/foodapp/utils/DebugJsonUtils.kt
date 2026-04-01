package com.phoenix.tech.foodapp.utils

import android.content.Context
import kotlinx.serialization.json.Json

val debugJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    prettyPrint = true
}


/**
 * Generic helper to parse any JSON from the debug assets folder.
 */
inline fun <reified T> Context.parseDebugJson(fileName: String): T {
    return assets.open(fileName).bufferedReader().use {
        debugJson.decodeFromString<T>(it.readText())
    }
}