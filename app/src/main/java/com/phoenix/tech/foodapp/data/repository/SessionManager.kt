package com.phoenix.tech.foodapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.phoenix.tech.foodapp.data.model.UserData
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
        // User Data keys
        val userToken = stringPreferencesKey(AppKeys.USER_TOKEN)
        val userEmail = stringPreferencesKey(AppKeys.USER_EMAIL)
        val userPhone = stringPreferencesKey(AppKeys.USER_PHONE)
        val userUsername = stringPreferencesKey(AppKeys.USER_USERNAME)
        val userFirstName = stringPreferencesKey(AppKeys.USER_FIRSTNAME)
        val userLastName = stringPreferencesKey(AppKeys.USER_LASTNAME)
        val userRole = stringPreferencesKey(AppKeys.USER_ROLE)
        val userPfp = stringPreferencesKey(AppKeys.USER_PFP)

        // Location Prefs keys
        val locName = stringPreferencesKey(AppKeys.LOC_NAME)
        val locLat = doublePreferencesKey(AppKeys.LOC_LAT)
        val locLong = doublePreferencesKey(AppKeys.LOC_LONG)
        val locIsAuto = booleanPreferencesKey(AppKeys.LOC_IS_AUTO)
    }

    // Check non-empty token exists to decide if authenticated
    val isAuthenticated: Flow<Boolean> = _dataStore.data.map { prefs ->
        !prefs[_keys.userToken].isNullOrBlank()
    }.distinctUntilChanged()

    // Expose token as a Flow (could be used in OkHttp headers)
    val authToken: Flow<String?> = _dataStore.data.map { prefs -> prefs[_keys.userToken] }

    val userData: Flow<UserData?> = _dataStore.data.map { prefs ->
        // If token is null, return whole user object as null
        val token = prefs[_keys.userToken] ?: return@map null
        UserData(
            token = token,
            email = prefs[_keys.userEmail] ?: "",
            mobileNumber = prefs[_keys.userPhone] ?: "",
            userName = prefs[_keys.userUsername] ?: "",
            profilePicture = prefs[_keys.userPfp] ?: "",
            firstName = prefs[_keys.userFirstName] ?: "",
            lastName = prefs[_keys.userLastName] ?: "",
            roleName = prefs[_keys.userRole] ?: "",
            orgName = prefs[_keys.userFirstName] ?: ""
        )
    }.distinctUntilChanged()

    val userLocation: Flow<UserLocation?> = _dataStore.data.map { prefs ->
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
    }.distinctUntilChanged()
    // Prevents identical emissions from triggering downstream logic (effects)


    suspend fun saveUserInfo(userData: UserData) {
        _dataStore.edit { prefs ->
            prefs[_keys.userToken] = userData.token
            prefs[_keys.userEmail] = userData.email
            prefs[_keys.userPhone] = userData.mobileNumber
            prefs[_keys.userUsername] = userData.userName
            prefs[_keys.userFirstName] = userData.firstName
            prefs[_keys.userLastName] = userData.lastName
            prefs[_keys.userRole] = userData.roleName
            prefs[_keys.userPfp] = userData.profilePicture
        }
    }

    suspend fun clearUserInfo() {
        _dataStore.edit { prefs ->
            prefs.remove(_keys.userToken)
            prefs.remove(_keys.userEmail)
            prefs.remove(_keys.userPhone)
            prefs.remove(_keys.userUsername)
            prefs.remove(_keys.userFirstName)
            prefs.remove(_keys.userLastName)
            prefs.remove(_keys.userRole)
            prefs.remove(_keys.userPfp)
            // Note: Don't clear location data so we won't have to re-detect
        }
    }

    suspend fun setLocationDetails(loc: UserLocation) {
        _dataStore.edit { prefs ->
            prefs[_keys.locName] = loc.name
            prefs[_keys.locLat] = loc.latitude
            prefs[_keys.locLong] = loc.longitude
            prefs[_keys.locIsAuto] = loc.isAutoDetected
        }
    }
}