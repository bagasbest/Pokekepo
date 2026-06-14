package com.project.pokekepo.core.util

/**
 * Pembungkus status operasi async di seluruh aplikasi.
 *
 * Dipakai Repository dan ViewModel untuk menandai:
 * - [Loading] — sedang memproses
 * - [Success] — berhasil dengan data
 * - [Error] — gagal dengan pesan error
 */
sealed class Resource<out T> {
    /** Operasi berhasil; [data] berisi hasil. */
    data class Success<T>(val data: T) : Resource<T>()
    /** Operasi gagal; [message] ditampilkan ke pengguna. */
    data class Error(val message: String) : Resource<Nothing>()
    /** Operasi sedang berjalan. */
    data object Loading : Resource<Nothing>()
}
