package com.project.pokekepo.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.pokekepo.domain.model.User
import com.project.pokekepo.domain.usecase.GetSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel level aplikasi — mengamati sesi login global.
 *
 * Dipakai [PokekepoNavHost] untuk menentukan layar awal (Login vs Main)
 * dan redirect otomatis saat logout/login.
 */
class AppViewModel(
    private val getSessionUseCase: GetSessionUseCase,
) : ViewModel() {

    private val _sessionUser = MutableStateFlow<User?>(null)
    val sessionUser: StateFlow<User?> = _sessionUser.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeSession()
    }

    /** Mengikuti perubahan sesi dari DataStore secara reaktif. */
    private fun observeSession() {
        viewModelScope.launch {
            getSessionUseCase().collect { user ->
                _sessionUser.value = user
                _isLoading.value = false
            }
        }
    }
}
