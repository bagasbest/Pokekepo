package com.project.pokekepo.core.util

/**
 * Validasi input form Login dan Register sebelum dikirim ke repository.
 *
 * Dipanggil oleh [com.project.pokekepo.presentation.auth.AuthViewModel] saat
 * pengguna menekan tombol Masuk/Daftar. Jika validasi gagal, error ditampilkan
 * langsung di field yang bermasalah (bukan menunggu response server).
 *
 * Alur:
 * 1. UI kirim input → ViewModel panggil [validateLogin] / [validateRegister]
 * 2. Jika [AuthValidationResult.isValid] = false → tampilkan error per field
 * 3. Jika valid → lanjut ke Use Case → Repository
 */
object AuthValidator {

    /** Pola regex sederhana untuk memastikan format email benar (user@domain.com). */
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /**
     * Memvalidasi form Login.
     * @param email Email yang sudah di-trim dari ViewModel.
     * @param password Kata sandi (tidak di-trim agar spasi di awal/akhir tetap dihitung).
     * @return Hasil validasi berisi map error per field; kosong jika semua valid.
     */
    fun validateLogin(email: String, password: String): AuthValidationResult {
        val errors = mutableMapOf<AuthField, String>()

        validateEmail(email)?.let { errors[AuthField.EMAIL] = it }
        validatePassword(password)?.let { errors[AuthField.PASSWORD] = it }

        return AuthValidationResult(errors)
    }

    /**
     * Memvalidasi form Register.
     * Mengecek nama, email, kata sandi, dan kesamaan konfirmasi kata sandi.
     */
    fun validateRegister(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): AuthValidationResult {
        val errors = mutableMapOf<AuthField, String>()

        validateName(name)?.let { errors[AuthField.NAME] = it }
        validateEmail(email)?.let { errors[AuthField.EMAIL] = it }
        validatePassword(password)?.let { errors[AuthField.PASSWORD] = it }

        when {
            confirmPassword.isBlank() -> errors[AuthField.CONFIRM_PASSWORD] = "Konfirmasi kata sandi wajib diisi"
            password != confirmPassword -> errors[AuthField.CONFIRM_PASSWORD] = "Konfirmasi kata sandi tidak cocok"
        }

        return AuthValidationResult(errors)
    }

    /** Mengembalikan pesan error nama, atau null jika nama valid. */
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Nama wajib diisi"
            name.length < 2 -> "Nama minimal 2 karakter"
            name.length > 50 -> "Nama maksimal 50 karakter"
            else -> null
        }
    }

    /** Mengembalikan pesan error email, atau null jika email valid. */
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email wajib diisi"
            !emailRegex.matches(email) -> "Format email tidak valid"
            else -> null
        }
    }

    /** Mengembalikan pesan error kata sandi, atau null jika kata sandi valid. */
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Kata sandi wajib diisi"
            password.length < 6 -> "Kata sandi minimal 6 karakter"
            else -> null
        }
    }
}

/** Identifikasi field form auth yang bisa memiliki error validasi. */
enum class AuthField {
    NAME,
    EMAIL,
    PASSWORD,
    CONFIRM_PASSWORD,
}

/**
 * Hasil validasi form — berisi error per field.
 * @param fieldErrors Map field → pesan error dalam bahasa Indonesia.
 */
data class AuthValidationResult(
    val fieldErrors: Map<AuthField, String>,
) {
    /** True jika tidak ada error (form siap dikirim ke repository). */
    val isValid: Boolean get() = fieldErrors.isEmpty()

    /** Mengambil pesan error untuk field tertentu; null jika field valid. */
    fun messageFor(field: AuthField): String? = fieldErrors[field]
}
