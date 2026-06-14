package com.project.pokekepo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.pokekepo.core.util.AuthField
import com.project.pokekepo.core.util.AuthValidator
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
    val fieldErrors: Map<AuthField, String> = emptyMap(),
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

    /** Menghapus error pada field yang sedang diedit. */
    fun clearFieldError(field: AuthField) {
        if (_uiState.value.fieldErrors.containsKey(field)) {
            _uiState.value = _uiState.value.copy(
                fieldErrors = _uiState.value.fieldErrors - field,
                errorMessage = null,
            )
        }
    }

    /** Memproses login setelah validasi form lokal. */
    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val validation = AuthValidator.validateLogin(trimmedEmail, password)

        if (!validation.isValid) {
            _uiState.value = AuthUiState(fieldErrors = validation.fieldErrors)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = loginUseCase(trimmedEmail, password)) {
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

    /** Memproses registrasi setelah validasi form lokal. */
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val validation = AuthValidator.validateRegister(
            name = trimmedName,
            email = trimmedEmail,
            password = password,
            confirmPassword = confirmPassword,
        )

        if (!validation.isValid) {
            _uiState.value = AuthUiState(fieldErrors = validation.fieldErrors)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = registerUseCase(trimmedName, trimmedEmail, password)) {
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
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successUser = null,
            fieldErrors = emptyMap(),
        )
    }
}
