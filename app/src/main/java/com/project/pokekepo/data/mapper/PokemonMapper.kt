package com.project.pokekepo.data.mapper

import com.project.pokekepo.data.remote.dto.PokemonDetailDto
import com.project.pokekepo.data.remote.dto.PokemonSummaryDto
import com.project.pokekepo.domain.model.PokemonDetail
import com.project.pokekepo.domain.model.PokemonStat
import com.project.pokekepo.domain.model.PokemonSummary

/**
 * Mapper — mengubah data antar lapisan (DTO ↔ Domain ↔ Couchbase document).
 *
 * Alur: PokeAPI JSON → DTO → [toSummary]/[toDetail] → Model Domain → UI
 */
object PokemonMapper {

    /** Label statistik dalam bahasa Indonesia untuk tampilan detail. */
    private val statLabels = mapOf(
        "hp" to "HP",
        "attack" to "Serangan",
        "defense" to "Pertahanan",
        "special-attack" to "Ser. Spesial",
        "special-defense" to "Tahan. Spesial",
        "speed" to "Kecepatan",
    )

    /**
     * Mengubah item daftar dari API menjadi [PokemonSummary].
     * ID diekstrak dari URL untuk membangun URL sprite.
     */
    fun toSummary(dto: PokemonSummaryDto): PokemonSummary {
        val id = extractIdFromUrl(dto.url)
        return PokemonSummary(
            name = dto.name,
            url = dto.url,
            spriteUrl = buildSpriteUrl(id),
        )
    }

    /**
     * Mengubah response detail API menjadi [PokemonDetail].
     * Memisahkan kemampuan biasa vs tersembunyi; label stat ke bahasa Indonesia.
     */
    fun toDetail(dto: PokemonDetailDto): PokemonDetail {
        val visibleAbilities = mutableListOf<String>()
        val hiddenAbilities = mutableListOf<String>()

        dto.abilities.forEach { wrapper ->
            val abilityName = wrapper.ability?.name?.formatName() ?: return@forEach
            if (wrapper.isHidden == true) {
                hiddenAbilities.add(abilityName)
            } else {
                visibleAbilities.add(abilityName)
            }
        }

        return PokemonDetail(
            id = dto.id,
            name = dto.name.formatName(),
            abilities = visibleAbilities,
            hiddenAbilities = hiddenAbilities,
            types = dto.types.orEmpty().mapNotNull { it.type?.name?.formatName() },
            stats = dto.stats.orEmpty().mapNotNull { wrapper ->
                val statName = wrapper.stat?.name ?: return@mapNotNull null
                PokemonStat(
                    name = statLabels[statName] ?: statName.formatName(),
                    baseStat = wrapper.baseStat,
                )
            },
            height = dto.height,
            weight = dto.weight,
            spriteUrl = resolveImageUrl(dto),
        )
    }

    /** Membangun [PokemonSummary] dari dokumen cache Couchbase. */
    fun summaryFromDocument(name: String, url: String, spriteUrl: String = ""): PokemonSummary {
        val id = extractIdFromUrl(url)
        return PokemonSummary(
            name = name,
            url = url,
            spriteUrl = spriteUrl.ifBlank { buildSpriteUrl(id) },
        )
    }

    /** Membangun [PokemonDetail] dari dokumen cache Couchbase. */
    fun detailFromDocument(
        id: Int,
        name: String,
        abilities: List<String>,
        hiddenAbilities: List<String>,
        types: List<String>,
        stats: List<PokemonStat>,
        height: Int,
        weight: Int,
        spriteUrl: String,
    ): PokemonDetail {
        return PokemonDetail(
            id = id,
            name = name.formatName(),
            abilities = abilities,
            hiddenAbilities = hiddenAbilities,
            types = types,
            stats = stats,
            height = height,
            weight = weight,
            spriteUrl = spriteUrl.ifBlank { buildSpriteUrl(id) },
        )
    }

    /** Mengekstrak ID numerik dari URL PokeAPI (contoh: .../pokemon/7/ → 7). */
    fun extractIdFromUrl(url: String): Int {
        return url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
    }

    /** URL sprite default dari repositori GitHub PokeAPI. */
    fun buildSpriteUrl(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    }

    /** Prioritas: official artwork → fallback sprite. */
    private fun resolveImageUrl(dto: PokemonDetailDto): String {
        val officialArt = dto.sprites?.other?.officialArtwork?.frontDefault
        if (!officialArt.isNullOrBlank()) return officialArt
        return buildSpriteUrl(dto.id)
    }

    /** Format nama API (rain-dish) menjadi tampilan (Rain dish). */
    private fun String.formatName(): String {
        return replace('-', ' ').replaceFirstChar { it.uppercase() }
    }
}
