package com.project.pokekepo

import android.app.Application
import com.project.pokekepo.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Titik masuk aplikasi Android.
 *
 * Alur: Android sistem memanggil [onCreate] → Koin diinisialisasi →
 * semua modul DI ([appModules]) terdaftar → siap dipakai oleh [MainActivity].
 */
class PokekepoApplication : Application() {
    /**
     * Menginisialisasi Koin saat aplikasi pertama kali dibuat.
     * Koin akan meng-inject dependensi (database, API, ViewModel, dll.) ke seluruh app.
     */
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PokekepoApplication)
            modules(appModules)
        }
    }
}
