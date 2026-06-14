package com.project.pokekepo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.pokekepo.core.util.Resource
import com.project.pokekepo.domain.model.User
import com.project.pokekepo.domain.usecase.LoginUseCase
import com.project.pokekepo.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** State UI layar Login dan Register. */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successUser: User? = null,
)

/**
 * ViewModel autentikasi — menangani login dan registrasi.
 *
 * Alur: UI kirim email/password → [login]/[register] → Use Case → Repository →
 * Couchbase + DataStore → [successUser] → NavHost pindah ke Main.
 */
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Memproses login; update [uiState] dengan hasil sukses atau error. */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = loginUseCase(email, password)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(successUser = result.data)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState(errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    /** Memproses registrasi; validasi konfirmasi password di sini. */
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (password != confirmPassword) {
                _uiState.value = AuthUiState(errorMessage = "Konfirmasi kata sandi tidak cocok")
                return@launch
            }
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = registerUseCase(name, email, password)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState(successUser = result.data)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState(errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    /** Reset pesan error/sukses setelah navigasi. */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successUser = null)
    }
}
