package com.project.pokekepo.core.util

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Mengenkripsi kata sandi agar tidak disimpan dalam bent teks biasa.
 */
object PasswordHasher {

    /**
     * Mengubah kata sandi menjadi hash yang aman untuk disimpan.
     */
    fun hash(password: String): String = BCrypt.withDefaults().hashToString(12, password.toCharArray())

    /**
     * Memverifikasi apakah kata sandi cocok dengan hash yang tersimpan.
     */
    fun verify(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}
