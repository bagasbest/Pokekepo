package com.project.pokekepo.data.local.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.project.pokekepo.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

/**
 * Menyimpan sesi login pengguna via DataStore Preferences.
 *
 * Alur: Login/Register sukses → [saveSession] → [observeSession] dipantau [AppViewModel]
 * → NavHost redirect ke Main. Logout → [clearSession] → kembali ke Login.
 *
 * DataStore dipakai (bukan Couchbase) karena sesi ringan dan perlu Flow reaktif.
 */
class SessionLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) {

    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")

    /** Menyimpan nama dan email pengguna yang sedang login. */
    suspend fun saveSession(name: String, email: String) {
        dataStore.edit { prefs ->
            prefs[userEmailKey] = email
            prefs[userNameKey] = name
        }
    }

    /** Menghapus sesi — dipanggil saat logout. */
    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(userEmailKey)
            prefs.remove(userNameKey)
        }
    }

    /** Flow reaktif sesi; emit null jika belum login. */
    fun observeSession(): Flow<User?> {
        return dataStore.data.map { prefs ->
            val email = prefs[userEmailKey] ?: return@map null
            val name = prefs[userNameKey] ?: return@map null
            User(name = name, email = email)
        }
    }

    /** Mengambil sesi sekali (suspend, non-Flow). */
    suspend fun getSession(): User? {
        val prefs = dataStore.data.first()
        val email = prefs[userEmailKey] ?: return null
        val name = prefs[userNameKey] ?: return null
        return User(name = name, email = email)
    }

    companion object {
        fun fromContext(context: Context): SessionLocalDataSource {
            return SessionLocalDataSource(context.sessionDataStore)
        }
    }
}
