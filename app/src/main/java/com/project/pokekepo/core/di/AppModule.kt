package com.project.pokekepo.core.di

import com.project.pokekepo.core.database.CouchbaseManager
import com.project.pokekepo.core.network.NetworkMonitor
import com.project.pokekepo.core.network.RetrofitFactory
import com.project.pokekepo.core.util.DefaultDispatcherProvider
import com.project.pokekepo.core.util.DispatcherProvider
import com.project.pokekepo.data.local.datasource.AuthLocalDataSource
import com.project.pokekepo.data.local.datasource.PokemonLocalDataSource
import com.project.pokekepo.data.local.datasource.SessionLocalDataSource
import com.project.pokekepo.data.remote.api.PokeApiService
import com.project.pokekepo.data.remote.datasource.PokemonRemoteDataSource
import com.project.pokekepo.data.repository.AuthRepositoryImpl
import com.project.pokekepo.data.repository.PokemonRepositoryImpl
import com.project.pokekepo.domain.repository.AuthRepository
import com.project.pokekepo.domain.repository.PokemonRepository
import com.project.pokekepo.domain.usecase.GetPokemonDetailUseCase
import com.project.pokekepo.domain.usecase.GetPokemonPagerUseCase
import com.project.pokekepo.domain.usecase.GetSessionUseCase
import com.project.pokekepo.domain.usecase.GetUserProfileUseCase
import com.project.pokekepo.domain.usecase.SaveProfileImageUseCase
import com.project.pokekepo.domain.usecase.LoginUseCase
import com.project.pokekepo.domain.usecase.LogoutUseCase
import com.project.pokekepo.domain.usecase.RegisterUseCase
import com.project.pokekepo.domain.usecase.SearchPokemonUseCase
import com.project.pokekepo.domain.usecase.SyncSearchIndexUseCase
import com.project.pokekepo.presentation.auth.AuthViewModel
import com.project.pokekepo.presentation.detail.DetailViewModel
import com.project.pokekepo.presentation.home.HomeViewModel
import com.project.pokekepo.presentation.main.AppViewModel
import com.project.pokekepo.presentation.profile.ProfileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Modul jaringan: Retrofit API PokeAPI dan pemantau koneksi internet. */
val networkModule = module {
    single<PokeApiService> { RetrofitFactory.createPokeApiService(isDebug = true) }
    single { NetworkMonitor(get()) }
}

/** Modul database: Couchbase, Gson, dan DataStore sesi login. */
val databaseModule = module {
    single { CouchbaseManager(androidContext()) }
    single { com.google.gson.Gson() }
    single { SessionLocalDataSource.fromContext(androidContext()) }
}

/** Modul data source: akses langsung ke Couchbase dan PokeAPI. */
val dataSourceModule = module {
    single { AuthLocalDataSource(get()) }
    single { PokemonLocalDataSource(get(), get()) }
    single { PokemonRemoteDataSource(get()) }
}

/** Modul repository: implementasi kontrak domain (Auth + Pokemon). */
val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<PokemonRepository> { PokemonRepositoryImpl(get(), get(), get(), get()) }
}

/** Modul use case: logika bisnis yang dipanggil ViewModel. */
val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetSessionUseCase(get()) }
    factory { GetPokemonPagerUseCase(get()) }
    factory { GetPokemonDetailUseCase(get()) }
    factory { SearchPokemonUseCase(get()) }
    factory { SyncSearchIndexUseCase(get()) }
    factory { GetUserProfileUseCase(get()) }
    factory { SaveProfileImageUseCase(get()) }
}

/** Modul utilitas inti: dispatcher coroutine untuk operasi IO. */
val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}

/** Modul ViewModel: satu ViewModel per layar/fitur. */
val viewModelModule = module {
    viewModel { AppViewModel(get()) }
    viewModel { AuthViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { DetailViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
}

/**
 * Daftar semua modul Koin yang di-load saat aplikasi start.
 *
 * Urutan alur dependensi:
 * core → network/database → dataSource → repository → useCase → viewModel
 */
val appModules = listOf(
    coreModule,
    networkModule,
    databaseModule,
    dataSourceModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)
