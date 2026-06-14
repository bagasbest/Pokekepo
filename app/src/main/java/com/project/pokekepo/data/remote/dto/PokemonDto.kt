package com.project.pokekepo.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object (DTO) — struktur JSON response PokeAPI.
 *
 * Gson memetakan JSON ke kelas ini saat Retrofit menerima response.
 * Selanjutnya [PokemonMapper] mengubah DTO menjadi model domain.
 */

/** Response GET /api/v2/pokemon?limit=&offset= */
data class PokemonListResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonSummaryDto>,
)

/** Satu item dalam daftar Pokemon (hanya name + url). */
data class PokemonSummaryDto(
    val name: String,
    val url: String,
)

/** Response GET /api/v2/pokemon/{name} — detail lengkap. */
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val abilities: List<AbilityWrapperDto>,
    val types: List<TypeWrapperDto>?,
    val stats: List<StatWrapperDto>?,
    val sprites: SpritesDto?,
)

data class AbilityWrapperDto(
    val ability: AbilityDto?,
    @SerializedName("is_hidden")
    val isHidden: Boolean?,
)

data class AbilityDto(
    val name: String,
)

data class TypeWrapperDto(
    val slot: Int,
    val type: TypeDto?,
)

data class TypeDto(
    val name: String,
)

data class StatWrapperDto(
    @SerializedName("base_stat")
    val baseStat: Int,
    val stat: StatDto?,
)

data class StatDto(
    val name: String,
)

data class SpritesDto(
    val other: OtherSpritesDto?,
)

data class OtherSpritesDto(
    @SerializedName("official-artwork")
    val officialArtwork: OfficialArtworkDto?,
)

data class OfficialArtworkDto(
    @SerializedName("front_default")
    val frontDefault: String?,
)
