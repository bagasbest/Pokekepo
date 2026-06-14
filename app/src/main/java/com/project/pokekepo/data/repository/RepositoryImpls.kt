package com.project.pokekepo.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.project.pokekepo.core.network.NetworkMonitor
import com.project.pokekepo.core.util.DispatcherProvider
import com.project.pokekepo.core.util.PasswordHasher
import com.project.pokekepo.core.util.Resource
import com.project.pokekepo.data.local.datasource.AuthLocalDataSource
import com.project.pokekepo.data.local.datasource.PokemonLocalDataSource
import com.project.pokekepo.data.local.datasource.PokemonPagingSource
import com.project.pokekepo.data.local.datasource.SessionLocalDataSource
import com.project.pokekepo.data.remote.datasource.PokemonRemoteDataSource
import com.project.pokekepo.domain.model.PokemonDetail
import com.project.pokekepo.domain.model.PokemonSummary
import com.project.pokekepo.domain.model.User
import com.project.pokekepo.domain.repository.AuthRepository
import com.project.pokekepo.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Implementasi repository — jembatan antara domain dan data source.
 *
 * [AuthRepositoryImpl]: validasi input → hash password → Couchbase + DataStore sesi.
 * [PokemonRepositoryImpl]: online/offline strategy → PokeAPI + cache Couchbase.
 */

class AuthRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : AuthRepository {

    /**
     * Registrasi: validasi → hash BCrypt → simpan user ke Couchbase → simpan sesi.
     */
    override suspend fun register(name: String, email: String, password: String): Resource<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Resource.Error("Semua kolom wajib diisi")
        }
        if (password.length < 6) {
            return Resource.Error("Kata sandi minimal 6 karakter")
        }

        return withContext(dispatcherProvider.io) {
            val hash = PasswordHasher.hash(password)
            authLocalDataSource.registerUser(name, email, hash)
                .fold(
                    onSuccess = { user ->
                        sessionLocalDataSource.saveSession(user.name, user.email)
                        Resource.Success(user)
                    },
                    onFailure = { error ->
                        Resource.Error(error.message ?: "Registrasi gagal")
                    },
                )
        }
    }

    /**
     * Login: verifikasi email + password hash di Couchbase → simpan sesi ke DataStore.
     */
    override suspend fun login(email: String, password: String): Resource<User> {
        if (email.isBlank() || password.isBlank()) {
            return Resource.Error("Email dan kata sandi wajib diisi")
        }

        return withContext(dispatcherProvider.io) {
            authLocalDataSource.loginUser(email, password)
                .fold(
                    onSuccess = { user ->
                        sessionLocalDataSource.saveSession(user.name, user.email)
                        Resource.Success(user)
                    },
                    onFailure = { error ->
                        Resource.Error(error.message ?: "Login gagal")
                    },
                )
        }
    }

    /** Menghapus email/nama dari DataStore — pengguna harus login ulang. */
    override suspend fun logout() {
        withContext(dispatcherProvider.io) {
            sessionLocalDataSource.clearSession()
        }
    }

    override fun getSessionUser(): Flow<User?> = sessionLocalDataSource.observeSession()

    override suspend fun getCurrentUser(): User? {
        return withContext(dispatcherProvider.io) {
            sessionLocalDataSource.getSession()
        }
    }

    override suspend fun getUserProfile(email: String): Resource<User> {
        return withContext(dispatcherProvider.io) {
            val user = authLocalDataSource.getUserProfile(email)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Profil tidak ditemukan")
            }
        }
    }

    override suspend fun saveProfileImage(email: String, profileImageBase64: String): Resource<User> {
        return withContext(dispatcherProvider.io) {
            if (profileImageBase64.isBlank()) {
                return@withContext Resource.Error("Gambar tidak valid")
            }
            authLocalDataSource.saveProfileImage(email, profileImageBase64)
                .fold(
                    onSuccess = { user -> Resource.Success(user) },
                    onFailure = { error ->
                        Resource.Error(error.message ?: "Gagal menyimpan foto profil")
                    },
                )
        }
    }
}

class PokemonRepositoryImpl(
    private val localDataSource: PokemonLocalDataSource,
    private val remoteDataSource: PokemonRemoteDataSource,
    private val networkMonitor: NetworkMonitor,
    private val dispatcherProvider: DispatcherProvider,
) : PokemonRepository {

    /**
     * Membuat Pager Paging 3 dengan [PokemonPagingSource].
     * Page size 10 item; factory baru setiap refresh.
     */
    override fun getPokemonPager(): Flow<PagingData<PokemonSummary>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = PAGE_SIZE,
            ),
            pagingSourceFactory = {
                PokemonPagingSource(
                    localDataSource = localDataSource,
                    remoteDataSource = remoteDataSource,
                    networkMonitor = networkMonitor,
                )
            },
        ).flow
    }

    /**
     * Detail Pokemon: baca cache → jika online fetch API & update cache → fallback cache offline.
     */
    override suspend fun getPokemonDetail(name: String): Resource<PokemonDetail> {
        return withContext(dispatcherProvider.io) {
            try {
                val cached = localDataSource.getCachedDetail(name)
                val isOnline = networkMonitor.checkCurrentConnectivity()

                if (cached != null && !isOnline) {
                    return@withContext Resource.Success(cached)
                }

                if (isOnline) {
                    val remote = remoteDataSource.fetchPokemonDetail(name)
                    localDataSource.savePokemonDetail(remote)
                    Resource.Success(remote)
                } else if (cached != null) {
                    Resource.Success(cached)
                } else {
                    Resource.Error("Tidak ada koneksi internet dan data belum tersimpan")
                }
            } catch (e: Exception) {
                val cached = localDataSource.getCachedDetail(name)
                if (cached != null) {
                    Resource.Success(cached)
                } else {
                    Resource.Error(e.message ?: "Gagal memuat detail Pokemon")
                }
            }
        }
    }

    /**
     * Pencarian: pastikan indeks ada → filter LIKE di Couchbase (max 20 hasil).
     */
    override suspend fun searchPokemon(query: String): Resource<List<PokemonSummary>> {
        return withContext(dispatcherProvider.io) {
            try {
                if (query.isBlank()) {
                    return@withContext Resource.Success(emptyList())
                }

                if (!localDataSource.hasSearchIndex() && networkMonitor.checkCurrentConnectivity()) {
                    syncSearchIndexInternal()
                }

                val results = localDataSource.searchByName(query)
                Resource.Success(results)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Pencarian gagal")
            }
        }
    }

    override suspend fun syncSearchIndex(): Resource<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                syncSearchIndexInternal()
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Gagal menyinkronkan indeks pencarian")
            }
        }
    }

    /** Mengunduh ~1300 nama Pokemon ke indeks lokal jika belum ada dan online. */
    private suspend fun syncSearchIndexInternal() {
        if (!networkMonitor.checkCurrentConnectivity()) return
        if (localDataSource.hasSearchIndex()) return
        val allNames = remoteDataSource.fetchAllPokemonNames()
        localDataSource.saveSearchIndex(allNames)
    }

    companion object {
        const val PAGE_SIZE = 10
    }
}
