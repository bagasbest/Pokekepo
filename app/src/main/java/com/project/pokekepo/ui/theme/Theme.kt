package com.project.pokekepo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PokemonYellow,
    onPrimary = Color.Black,
    secondary = PokemonRed,
    tertiary = PokemonBlue,
    background = PokemonDark,
    onBackground = Color.White,
    surface = Color(0xFF252540),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF323256),
    onSurfaceVariant = Color(0xFFB0B0C8),
    outline = Color(0xFF3D3D5C),
)

private val LightColorScheme = lightColorScheme(
    primary = PokemonYellow,
    onPrimary = Color.Black,
    secondary = PokemonBlue,
    onSecondary = Color.White,
    tertiary = PokemonYellow,
    onTertiary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black,
    outline = Color(0xFFBDBDBD),
)

/**
 * Tema Material 3 aplikasi Pokekepo.
 *
 * Dark mode: background navy ([PokemonDark]), aksen kuning ([PokemonYellow]).
 * Light mode: background putih, teks hitam (dipakai layar auth).
 */
@Composable
fun PokekepoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = (LocalContext.current as Activity).applicationContext
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
