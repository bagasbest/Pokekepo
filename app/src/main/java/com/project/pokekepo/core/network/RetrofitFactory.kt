package com.project.pokekepo.core.network

import com.project.pokekepo.data.remote.api.PokeApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Membuat klien Retrofit untuk digunakan ke PokeAPI.
 */
object RetrofitFactory {

    private const val BASE_URL = "https://pokeapi.co/"

    /**
     * Membuat instance layanan API Pokemon.
     */
    fun createPokeApiService(isDebug: Boolean = false): PokeApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }
}
