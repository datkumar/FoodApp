package com.phoenix.tech.foodapp.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.phoenix.tech.foodapp.BuildConfig
import com.phoenix.tech.foodapp.utils.GEOCODE_BASE_URL
import com.phoenix.tech.foodapp.utils.POKE_BASE_URL
import com.phoenix.tech.foodapp.utils.SWIGGY_BASE_URL
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


object HttpClient {

    private val contentType = "application/json".toMediaType()
    private val json = Json {
        ignoreUnknownKeys = true // Don't process keys we haven't defined in schema
        coerceInputValues = true
        isLenient = true
    }

    // Common logging for both APIs
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private const val AUTH_BASE_URL = BuildConfig.AUTH_BASE_URL

    // --- SWIGGY NETWORK SETUP ---
    // Keep reference to same instance
    private val swiggyHeaderInterceptor = SwiggyHeaderInterceptor()
    private val swiggyClient = OkHttpClient.Builder()
        .addInterceptor(swiggyHeaderInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val swiggyRetrofit = Retrofit.Builder()
        .baseUrl(SWIGGY_BASE_URL)
        .client(swiggyClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
    val swiggyApi: SwiggyApi = swiggyRetrofit.create(SwiggyApi::class.java)


    // --- AUTH NETWORK SETUP ---
    private val authClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // NO HeaderInterceptor here so we don't spoof Swiggy headers to company servers
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()
    private val authRetrofit = Retrofit.Builder()
        .baseUrl(AUTH_BASE_URL)
        .client(authClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
    val authApi: AuthApi = authRetrofit.create(AuthApi::class.java)

    // --- GEO-CODING SETUP ---
    private val geoClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()
    private val geoRetrofit = Retrofit.Builder()
        .baseUrl(GEOCODE_BASE_URL)
        .client(geoClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
    val geoApi: GeoApi = geoRetrofit.create(GeoApi::class.java)

    // --- POKE-API SETUP ---
    private val pokeClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()
    private val pokeRetrofit = Retrofit.Builder()
        .baseUrl(POKE_BASE_URL)
        .client(pokeClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
    val pokeApi: PokeApi = pokeRetrofit.create(PokeApi::class.java)
}