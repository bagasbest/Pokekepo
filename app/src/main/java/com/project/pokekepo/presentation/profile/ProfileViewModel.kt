package com.project.pokekepo.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.pokekepo.core.util.Resource
import com.project.pokekepo.domain.model.User
import com.project.pokekepo.domain.usecase.GetSessionUseCase
import com.project.pokekepo.domain.usecase.GetUserProfileUseCase
import com.project.pokekepo.domain.usecase.LogoutUseCase
import com.project.pokekepo.domain.usecase.SaveProfileImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** State UI layar profil pengguna. */
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isSavingImage: Boolean = false,
    val message: String? = null,
)

/**
 * ViewModel layar Profile — tampilkan profil, foto, dan logout.
 *
 * Alur foto: picker galeri → Base64 → [saveProfileImage] → Couchbase.
 */
class ProfileViewModel(
    private val getSessionUseCase: GetSessionUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val saveProfileImageUseCase: SaveProfileImageUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Memuat data profil pengguna yang sedang login dari database lokal.
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val session = getSessionUseCase.current()
            if (session == null) {
                _uiState.value = ProfileUiState(isLoading = false, message = "Sesi tidak ditemukan")
                return@launch
            }

            when (val result = getUserProfileUseCase(session.email)) {
                is Resource.Success -> {
                    _uiState.value = ProfileUiState(
                        user = result.data,
                        isLoading = false,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = ProfileUiState(
                        user = session,
                        isLoading = false,
                        message = result.message,
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    /**
     * Menyimpan foto profil yang sudah diubah menjadi Base64.
     */
    fun saveProfileImage(base64: String) {
        val email = _uiState.value.user?.email ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingImage = true, message = null)
            when (val result = saveProfileImageUseCase(email, base64)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        user = result.data,
                        isSavingImage = false,
                        message = "Foto profil berhasil disimpan",
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSavingImage = false,
                        message = result.message,
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    /** Logout: hapus sesi DataStore → NavHost redirect ke Login. */
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
