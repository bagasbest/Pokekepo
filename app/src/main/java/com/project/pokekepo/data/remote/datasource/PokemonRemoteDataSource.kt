package com.project.pokekepo.data.remote.datasource

import com.project.pokekepo.data.mapper.PokemonMapper
import com.project.pokekepo.data.remote.api.PokeApiService
import com.project.pokekepo.domain.model.PokemonDetail
import com.project.pokekepo.domain.model.PokemonSummary

/**
 * Sumber data Pokemon dari jaringan (PokeAPI).
 */
class PokemonRemoteDataSource(
    private val api: PokeApiService,
) {

    /**
     * Mengambil halaman daftar Pokemon dari API.
     */
    suspend fun fetchPokemonPage(limit: Int, offset: Int): List<PokemonSummary> {
        val response = api.getPokemonList(limit = limit, offset = offset)
        return response.results.map(PokemonMapper::toSummary)
    }

    /**
     * Mengambil detail Pokemon ber nama dari API.
     */
    suspend fun fetchPokemonDetail(name: String): PokemonDetail {
        val response = api.getPokemonDetail(name.lowercase())
        return PokemonMapper.toDetail(response)
    }

    /**
     * Mengunduh seluruh indeks nama Pokemon untuk pencarian.
     */
    suspend fun fetchAllPokemonNames(): List<PokemonSummary> {
        val response = api.getPokemonList(limit = 1300, offset = 0)
        return response.results.map(PokemonMapper::toSummary)
    }
}
