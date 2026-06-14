package com.project.pokekepo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.pokekepo.navigation.PokekepoNavHost
import com.project.pokekepo.ui.theme.PokekepoTheme

/**
 * Activity utama yang menampilkan UI Compose.
 *
 * Alur: [onCreate] → [PokekepoTheme] (tema Material) → [PokekepoNavHost] (navigasi seluruh layar).
 * Semua layar (Login, Home, Detail, Profile) dirender dari NavHost ini.
 */
class MainActivity : ComponentActivity() {
    /**
     * Menyiapkan edge-to-edge layout dan menampilkan pohon composable aplikasi.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokekepoTheme {
                PokekepoNavHost()
            }
        }
    }
}
