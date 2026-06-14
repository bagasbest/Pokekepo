package com.project.pokekepo.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.project.pokekepo.navigation.MainTab
import com.project.pokekepo.presentation.home.HomeScreen
import com.project.pokekepo.presentation.profile.ProfileScreen

/**
 * Layar utama setelah login — bottom tab Home dan Profile.
 *
 * Alur: pengguna pilih tab → tampilkan [HomeScreen] atau [ProfileScreen].
 * Klik Pokemon di Home → callback [onPokemonClick] → NavHost buka Detail.
 */
@Composable
fun MainScreen(
    onPokemonClick: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }

    Scaffold(
        bottomBar = {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Home,
                    onClick = { selectedTab = MainTab.Home },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = navItemColors,
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Profile,
                    onClick = { selectedTab = MainTab.Profile },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = navItemColors,
                )
            }
        },
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
            MainTab.Home -> HomeScreen(
                onPokemonClick = onPokemonClick,
            )
            MainTab.Profile -> ProfileScreen(
                onLogout = onLogout,
            )
        }
        }
    }
}
