package com.phoenix.tech.foodapp.data.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.phoenix.tech.foodapp.BuildConfig
import com.phoenix.tech.foodapp.utils.GEOCODE_BASE_URL
import com.phoenix.tech.foodapp.utils.POKE_BASE_URL
import com.phoenix.tech.foodapp.utils.SWIGGY_BASE_URL
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


object HttpClient {
    private var isInitialized = false

    // We need a way to pass context for the Cache and Interceptor
    // Ideally, call this once in your Application class or provide via Hilt
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        if (!isInitialized) {
            appContext = context.applicationContext
            isInitialized = true
        }
    }

    private val connectivityInterceptor by lazy { ConnectivityInterceptor(appContext) }
    private val cache by lazy { getCache(appContext) }

    private fun getCache(context: Context): Cache {
        val cacheSize = (5 * 1024 * 1024).toLong() // 5 MB
        return Cache(context.cacheDir, cacheSize)
    }

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

    private fun createCommonClientBuilder(timeoutSec: Long): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor(connectivityInterceptor) // Check internet before every call
            .addInterceptor(loggingInterceptor)
            .cache(cache) // Enable disk caching
            .connectTimeout(timeoutSec, TimeUnit.SECONDS)
    }

    // --- SWIGGY SETUP ---
    val swiggyApi: SwiggyApi by lazy {
        Retrofit.Builder()
            .baseUrl(SWIGGY_BASE_URL)
            .client(
                createCommonClientBuilder(30).addInterceptor(
                    SwiggyHeaderInterceptor()
                ).build()
            )
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(SwiggyApi::class.java)
    }

    // --- AUTH SETUP ---
    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(createCommonClientBuilder(15).build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AuthApi::class.java)
    }

    // --- GEO-CODING SETUP ---
    val geoApi: GeoApi by lazy {
        Retrofit.Builder()
            .baseUrl(GEOCODE_BASE_URL)
            .client(createCommonClientBuilder(10).build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GeoApi::class.java)
    }

    // --- POKE-API SETUP ---
    val pokeApi: PokeApi by lazy {
        Retrofit.Builder()
            .baseUrl(POKE_BASE_URL)
            .client(createCommonClientBuilder(10).build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(PokeApi::class.java)
    }
}