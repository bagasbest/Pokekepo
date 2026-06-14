package com.project.pokekepo.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.pokekepo.core.util.Resource
import com.project.pokekepo.domain.model.PokemonDetail
import com.project.pokekepo.domain.usecase.GetPokemonDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** State UI layar detail Pokemon. */
data class DetailUiState(
    val isLoading: Boolean = true,
    val detail: PokemonDetail? = null,
    val errorMessage: String? = null,
)

/**
 * ViewModel layar detail — memuat data Pokemon by name.
 *
 * Alur: [loadDetail] → GetPokemonDetailUseCase → cache/API → [DetailUiState].
 */
class DetailViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Memuat detail Pokemon; otomatis cache bila online, fallback cache bila offline.
     * @param name Nama Pokemon (lowercase dari navigasi).
     */
    fun loadDetail(name: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)
            when (val result = getPokemonDetailUseCase(name)) {
                is Resource.Success -> {
                    _uiState.value = DetailUiState(
                        isLoading = false,
                        detail = result.data,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = DetailUiState(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }
}
