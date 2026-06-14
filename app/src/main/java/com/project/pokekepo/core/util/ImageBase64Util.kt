package com.project.pokekepo.core.util

import android.content.Context
import android.net.Uri
import android.util.Base64

/**
 * Membantu mengubah gambar dari galeri menjadi teks Base64 untuk disimpan di database lokal.
 */
object ImageBase64Util {

    /**
     * Membaca gambar dari URI lalu mengubahnya menjadi string Base64.
     */
    fun encodeFromUri(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
            }
        }.getOrNull()
    }

    /**
     * Mengubah string Base64 menjadi URL data agar bisa ditampilkan dengan Glide.
     */
    fun toDataUri(base64: String, mimeType: String = "image/jpeg"): String {
        return "data:$mimeType;base64,$base64"
    }
}
