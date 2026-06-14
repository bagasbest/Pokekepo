package com.project.pokekepo.data.remote.api

import com.project.pokekepo.data.remote.dto.PokemonDetailDto
import com.project.pokekepo.data.remote.dto.PokemonListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Antarmuka Retrofit untuk memanggil endpoint PokeAPI.
 */
interface PokeApiService {

    /**
     * Mengambil daftar Pokemon dengan pagination.
     */
    @GET("api/v2/pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): PokemonListResponseDto

    /**
     * Mengambil detail Pokemon ber nama.
     */
    @GET("api/v2/pokemon/{name}")
    suspend fun getPokemonDetail(
        @Path("name") name: String,
    ): PokemonDetailDto
}
