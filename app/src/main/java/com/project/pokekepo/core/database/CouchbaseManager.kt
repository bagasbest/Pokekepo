package com.project.pokekepo.core.database

import android.content.Context
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration

/**
 * Mengelola koneksi dan koleksi Couchbase Lite.
 *
 * Alur penyimpanan lokal:
 * - [COLLECTION_USERS] → data akun login (hash password, foto profil Base64)
 * - [COLLECTION_POKEMON_LIST] → cache halaman daftar Pokemon (paging)
 * - [COLLECTION_POKEMON_DETAIL] → cache detail Pokemon per nama
 * - [COLLECTION_POKEMON_INDEX] → indeks nama untuk pencarian offline
 *
 * Di-inject via Koin dan dipakai oleh [AuthLocalDataSource] serta [PokemonLocalDataSource].
 */
class CouchbaseManager(context: Context) {

    val database: Database

    init {
        CouchbaseLite.init(context)
        val config = DatabaseConfiguration()
        database = Database(DB_NAME, config)
        ensureCollections()
    }

    /**
     * Membuat koleksi Couchbase jika belum ada.
     * Dipanggil sekali saat manager pertama kali dibuat.
     */
    private fun ensureCollections() {
        listOf(
            COLLECTION_USERS,
            COLLECTION_POKEMON_LIST,
            COLLECTION_POKEMON_DETAIL,
            COLLECTION_POKEMON_INDEX,
        ).forEach { name ->
            if (database.getCollection(name) == null) {
                database.createCollection(name)
            }
        }
    }

    /**
     * Mengembalikan koleksi Couchbase berdasarkan nama.
     * @throws IllegalArgumentException jika koleksi belum dibuat.
     */
    fun collection(name: String) = requireNotNull(database.getCollection(name)) {
        "Collection $name belum dibuat"
    }

    companion object {
        const val DB_NAME = "pokekepo_db"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_POKEMON_LIST = "pokemon_list"
        const val COLLECTION_POKEMON_DETAIL = "pokemon_detail"
        const val COLLECTION_POKEMON_INDEX = "pokemon_index"
    }
}
