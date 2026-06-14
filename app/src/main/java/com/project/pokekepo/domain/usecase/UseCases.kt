package com.project.pokekepo.domain.usecase

import androidx.paging.PagingData
import com.project.pokekepo.core.util.Resource
import com.project.pokekepo.domain.model.PokemonDetail
import com.project.pokekepo.domain.model.PokemonSummary
import com.project.pokekepo.domain.model.User
import com.project.pokekepo.domain.repository.AuthRepository
import com.project.pokekepo.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case — satu aksi bisnis per kelas.
 *
 * Alur: ViewModel memanggil Use Case → Use Case memanggil Repository.
 * ViewModel tidak boleh langsung akses data source.
 */

/** Login pengguna dengan email dan kata sandi. */
class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> =
        authRepository.login(email, password)
}

/** Registrasi akun baru. */
class RegisterUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(name: String, email: String, password: String): Resource<User> =
        authRepository.register(name, email, password)
}

/** Logout — hapus sesi dari DataStore. */
class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.logout()
}

/**
 * Mengamati sesi login pengguna.
 * Dipakai [AppViewModel] untuk redirect Login ↔ Main.
 */
class GetSessionUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = authRepository.getSessionUser()
    suspend fun current(): User? = authRepository.getCurrentUser()
}

/** Mengembalikan Flow Paging 3 daftar Pokemon untuk layar Home. */
class GetPokemonPagerUseCase(
    private val pokemonRepository: PokemonRepository,
) {
    operator fun invoke(): Flow<PagingData<PokemonSummary>> = pokemonRepository.getPokemonPager()
}

/** Mengambil detail Pokemon by name (online + cache offline). */
class GetPokemonDetailUseCase(
    private val pokemonRepository: PokemonRepository,
) {
    suspend operator fun invoke(name: String): Resource<PokemonDetail> =
        pokemonRepository.getPokemonDetail(name)
}

/** Mencari Pokemon berdasarkan nama (indeks lokal Couchbase). */
class SearchPokemonUseCase(
    private val pokemonRepository: PokemonRepository,
) {
    suspend operator fun invoke(query: String): Resource<List<PokemonSummary>> =
        pokemonRepository.searchPokemon(query)
}

/**
 * Menyinkronkan indeks nama Pokemon ke Couchbase untuk pencarian offline.
 * Dipanggil saat Home pertama kali dibuka.
 */
class SyncSearchIndexUseCase(
    private val pokemonRepository: PokemonRepository,
) {
    suspend operator fun invoke() = pokemonRepository.syncSearchIndex()
}

/** Mengambil profil lengkap pengguna termasuk foto dari Couchbase. */
class GetUserProfileUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): Resource<User> =
        authRepository.getUserProfile(email)
}

/** Menyimpan foto profil sebagai Base64 ke database lokal. */
class SaveProfileImageUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, profileImageBase64: String): Resource<User> =
        authRepository.saveProfileImage(email, profileImageBase64)
}
