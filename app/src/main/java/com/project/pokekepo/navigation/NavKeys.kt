package com.project.pokekepo.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Kunci navigasi type-safe untuk Navigation 3.
 *
 * Alur rute aplikasi:
 * Login → Register (opsional) → Main (Home + Profile tabs) → Detail(pokemonName)
 *
 * Setiap [AppNavKey] adalah satu entry di back stack [PokekepoNavHost].
 */
@Serializable
sealed interface AppNavKey : NavKey {
    /** Layar login — titik masuk jika belum ada sesi. */
    @Serializable
    data object Login : AppNavKey

    /** Layar registrasi akun baru. */
    @Serializable
    data object Register : AppNavKey

    /** Layar utama dengan bottom tab Home dan Profile. */
    @Serializable
    data object Main : AppNavKey

    /** Layar detail Pokemon; [pokemonName] dari item yang diklik di Home. */
    @Serializable
    data class Detail(val pokemonName: String) : AppNavKey
}

/** Tab bottom navigation di dalam [MainScreen]. */
enum class MainTab {
    Home,
    Profile,
}
