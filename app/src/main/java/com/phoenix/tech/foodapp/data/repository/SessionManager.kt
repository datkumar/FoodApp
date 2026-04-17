package com.phoenix.tech.foodapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.phoenix.tech.foodapp.data.model.UserLocation
import com.phoenix.tech.foodapp.utils.AppKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


// Create DataStore instance via Extension property
private val Context.dataStore by preferencesDataStore(AppKeys.PREFS_STORE)

class SessionManager(context: Context) {
    private val _dataStore = context.dataStore

    private val _keys = object {
        // Auth Prefs keys
        val authToken = stringPreferencesKey(AppKeys.AUTH_TOKEN)

        // Location Prefs keys
        val locName = stringPreferencesKey(AppKeys.LOC_NAME)
        val locLat = doublePreferencesKey(AppKeys.LOC_LAT)
        val locLong = doublePreferencesKey(AppKeys.LOC_LONG)
        val locIsAuto = booleanPreferencesKey(AppKeys.LOC_IS_AUTO)
    }

    suspend fun setAuthToken(token: String) {
        // Save token to disk
        _dataStore.edit { preferences ->
            preferences[_keys.authToken] = token
        }
    }

    // Get token as a Flow
    val authToken: Flow<String?> = _dataStore.data.map { preferences ->
        preferences[_keys.authToken]
    }

    suspend fun clearAuthSession() {
        _dataStore.edit { prefs ->
            prefs.remove(_keys.authToken)
            // Note: Don't clear location data so we won't have to re-detect
        }
    }

    val userLocation: Flow<UserLocation?> = _dataStore.data
        .map { prefs ->
            val locName = prefs[_keys.locName]
            if (locName.isNullOrEmpty()) null
            else {
                UserLocation(
                    name = locName,
                    latitude = prefs[_keys.locLat] ?: 0.0,
                    longitude = prefs[_keys.locLong] ?: 0.0,
                    isAutoDetected = prefs[_keys.locIsAuto] ?: false
                )
            }
        }
        .distinctUntilChanged() // Prevents identical emissions from triggering downstream logic (effects)

    suspend fun setLocationDetails(loc: UserLocation) {
        _dataStore.edit { prefs ->
            prefs[_keys.locName] = loc.name
            prefs[_keys.locLat] = loc.latitude
            prefs[_keys.locLong] = loc.longitude
            prefs[_keys.locIsAuto] = loc.isAutoDetected
        }
    }
}