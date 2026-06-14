package com.project.pokekepo.data.local.entity

/**
 * Struktur dokumen Couchbase Lite.
 *
 * Setiap dokumen punya field `type` untuk membedakan jenis data dalam satu koleksi.
 * Dipetakan ke/dari model domain via [AuthLocalDataSource] dan [PokemonLocalDataSource].
 */

/** Dokumen pengguna: akun login + hash password + foto profil Base64. */
data class UserDocument(
    val type: String = TYPE,
    val name: String,
    val email: String,
    val passwordHash: String,
    val profileImageBase64: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TYPE = "user"
    }
}

/** Cache satu Pokemon dalam daftar paging (offset + sprite). */
data class PokemonListDocument(
    val type: String = TYPE,
    val name: String,
    val url: String,
    val listOffset: Int,
    val spriteUrl: String = "",
    val cachedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TYPE = "pokemon_list"
    }
}

/** Cache detail Pokemon lengkap untuk mode offline. */
data class PokemonDetailDocument(
    val type: String = TYPE,
    val pokemonId: Int = 0,
    val name: String,
    val abilities: List<String>,
    val hiddenAbilities: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val stats: List<PokemonStatDocument> = emptyList(),
    val height: Int = 0,
    val weight: Int = 0,
    val spriteUrl: String = "",
    val cachedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TYPE = "pokemon_detail"
    }
}

/** Statistik tersimpan sebagai JSON di dokumen detail. */
data class PokemonStatDocument(
    val name: String,
    val baseStat: Int,
)

/** Indeks nama Pokemon untuk pencarian LIKE offline. */
data class PokemonIndexDocument(
    val type: String = TYPE,
    val name: String,
    val url: String,
) {
    companion object {
        const val TYPE = "pokemon_index"
    }
}
