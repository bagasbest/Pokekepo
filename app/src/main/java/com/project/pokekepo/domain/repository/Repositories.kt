package com.project.pokekepo.domain.repository

import androidx.paging.PagingData
import com.project.pokekepo.core.util.Resource
import com.project.pokekepo.domain.model.PokemonDetail
import com.project.pokekepo.domain.model.PokemonSummary
import com.project.pokekepo.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository autentikasi — diimplementasi oleh [AuthRepositoryImpl].
 *
 * Alur auth: Register/Login → hash password → simpan ke Couchbase →
 * simpan sesi ke DataStore → [getSessionUser] mengembalikan Flow untuk NavHost.
 */
interface AuthRepository {
    /** Mendaftarkan pengguna baru; email harus unik. */
    suspend fun register(name: String, email: String, password: String): Resource<User>

    /** Login dengan email dan password; menyimpan sesi jika berhasil. */
    suspend fun login(email: String, password: String): Resource<User>

    /** Menghapus sesi login dari DataStore. */
    suspend fun logout()

    /** Flow reaktif pengguna yang sedang login (null = belum login). */
    fun getSessionUser(): Flow<User?>

    /** Mengambil sesi saat ini sekali (non-Flow). */
    suspend fun getCurrentUser(): User?

    /** Profil lengkap termasuk foto Base64 dari Couchbase. */
    suspend fun getUserProfile(email: String): Resource<User>

    /** Menyimpan foto profil sebagai string Base64 ke Couchbase. */
    suspend fun saveProfileImage(email: String, profileImageBase64: String): Resource<User>
}

/**
 * Kontrak repository Pokemon — diimplementasi oleh [PokemonRepositoryImpl].
 *
 * Alur data: online → PokeAPI → cache Couchbase; offline → baca cache saja.
 */
interface PokemonRepository {
    /** Pager infinite scroll 10 item/halaman untuk layar Home. */
    fun getPokemonPager(): Flow<PagingData<PokemonSummary>>

    /** Detail Pokemon by name; cache-first dengan refresh API bila online. */
    suspend fun getPokemonDetail(name: String): Resource<PokemonDetail>

    /** Pencarian lokal berdasarkan indeks nama di Couchbase. */
    suspend fun searchPokemon(query: String): Resource<List<PokemonSummary>>

    /** Mengunduh seluruh nama Pokemon ke indeks lokal (sekali, untuk pencarian). */
    suspend fun syncSearchIndex(): Resource<Unit>
}
