package com.project.pokekepo.domain.model

/**
 * Model domain — lapisan tengah Clean Architecture.
 *
 * Kelas-kelas di sini tidak bergantung pada Android, Retrofit, atau Couchbase.
 * Hanya berisi data murni yang dipakai ViewModel dan Use Case.
 */

/** Data pengguna yang login (nama, email, foto profil opsional). */
data class User(
    val name: String,
    val email: String,
    val profileImageBase64: String? = null,
)

/**
 * Ringkasan Pokemon untuk daftar Home dan hasil pencarian.
 * @param url URL detail dari PokeAPI (dipakai untuk ekstrak ID).
 * @param spriteUrl URL gambar sprite; kosong jika belum di-cache.
 */
data class PokemonSummary(
    val name: String,
    val url: String,
    val spriteUrl: String = "",
) {
    /**
     * Mengembalikan URL gambar yang siap ditampilkan Glide.
     * Fallback ke sprite GitHub jika [spriteUrl] kosong.
     */
    fun resolveImageUrl(): String {
        if (spriteUrl.isNotBlank()) return spriteUrl
        val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    }
}

/** Satu statistik base Pokemon (mis. HP, Serangan) dengan label bahasa Indonesia. */
data class PokemonStat(
    val name: String,
    val baseStat: Int,
)

/**
 * Detail lengkap Pokemon dari PokeAPI.
 * @param height Tinggi dalam desimeter (API PokeAPI).
 * @param weight Berat dalam hectogram (API PokeAPI).
 */
data class PokemonDetail(
    val id: Int,
    val name: String,
    val abilities: List<String>,
    val hiddenAbilities: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val stats: List<PokemonStat> = emptyList(),
    val height: Int = 0,
    val weight: Int = 0,
    val spriteUrl: String = "",
)
