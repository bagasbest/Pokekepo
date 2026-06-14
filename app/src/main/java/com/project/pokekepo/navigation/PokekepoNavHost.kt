package com.project.pokekepo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.project.pokekepo.presentation.auth.LoginScreen
import com.project.pokekepo.presentation.auth.RegisterScreen
import com.project.pokekepo.presentation.detail.DetailScreen
import com.project.pokekepo.presentation.main.AppViewModel
import com.project.pokekepo.presentation.main.MainScreen
import org.koin.androidx.compose.koinViewModel

/**
 * NavHost utama aplikasi — mengatur seluruh alur layar.
 *
 * Alur navigasi:
 * 1. [AppViewModel] mengamati sesi login via DataStore
 * 2. Jika belum login → [AppNavKey.Login]; sudah login → [AppNavKey.Main]
 * 3. Klik Pokemon di Home → push [AppNavKey.Detail]
 * 4. Back dari Detail → pop back stack
 *
 * Navigation 3 dipakai dengan back stack dan entry provider per [AppNavKey].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokekepoNavHost(
    appViewModel: AppViewModel = koinViewModel(),
) {
    val sessionUser by appViewModel.sessionUser.collectAsStateWithLifecycle()
    val isLoading by appViewModel.isLoading.collectAsStateWithLifecycle()

    val startDestination = if (sessionUser != null) AppNavKey.Main else AppNavKey.Login
    val backStack = rememberNavBackStack(startDestination)

    /**
     * Redirect otomatis saat status login berubah.
     * Misalnya: logout → kembali ke Login; login sukses → ke Main.
     */
    LaunchedEffect(sessionUser, isLoading) {
        if (isLoading) return@LaunchedEffect
        if (sessionUser != null && backStack.lastOrNull() !is AppNavKey.Main && backStack.lastOrNull() !is AppNavKey.Detail) {
            backStack.clear()
            backStack.add(AppNavKey.Main)
        } else if (sessionUser == null && backStack.lastOrNull() !is AppNavKey.Login && backStack.lastOrNull() !is AppNavKey.Register) {
            backStack.clear()
            backStack.add(AppNavKey.Login)
        }
    }

    val provider = entryProvider<NavKey> {
        entry<AppNavKey.Login> {
            LoginScreen(
                onNavigateToRegister = { backStack.add(AppNavKey.Register) },
                onLoginSuccess = {
                    backStack.clear()
                    backStack.add(AppNavKey.Main)
                },
            )
        }
        entry<AppNavKey.Register> {
            RegisterScreen(
                onNavigateToLogin = { backStack.removeLastOrNull() },
                onRegisterSuccess = {
                    backStack.clear()
                    backStack.add(AppNavKey.Main)
                },
            )
        }
        entry<AppNavKey.Main> {
            MainScreen(
                onPokemonClick = { name ->
                    backStack.add(AppNavKey.Detail(pokemonName = name))
                },
                onLogout = {},
            )
        }
        entry<AppNavKey.Detail> { key ->
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(key.pokemonName.replaceFirstChar { it.uppercase() }) },
                        navigationIcon = {
                            IconButton(onClick = { backStack.removeLastOrNull() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                )
                            }
                        },
                    )
                },
            ) { innerPadding ->
                DetailScreen(
                    pokemonName = key.pokemonName,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = provider,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
    )
}
