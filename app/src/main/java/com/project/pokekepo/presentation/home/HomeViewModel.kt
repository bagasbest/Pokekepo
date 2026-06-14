package com.project.pokekepo.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.project.pokekepo.core.util.Resource
import com.project.pokekepo.domain.model.PokemonSummary
import com.project.pokekepo.domain.usecase.GetPokemonPagerUseCase
import com.project.pokekepo.domain.usecase.SearchPokemonUseCase
import com.project.pokekepo.domain.usecase.SyncSearchIndexUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** State pencarian di layar Home. */
data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<PokemonSummary> = emptyList(),
    val errorMessage: String? = null,
) {
    val isSearchActive: Boolean get() = query.isNotBlank()
}

/**
 * ViewModel layar Home — daftar paging + pencarian dengan debounce.
 *
 * Alur:
 * - [pokemonPager]: Paging 3 infinite scroll 10 item/halaman
 * - [updateSearchQuery]: debounce 400ms → [SearchPokemonUseCase]
 * - init: sync indeks pencarian ke Couchbase (sekali, bila online)
 */
class HomeViewModel(
    getPokemonPagerUseCase: GetPokemonPagerUseCase,
    private val searchPokemonUseCase: SearchPokemonUseCase,
    private val syncSearchIndexUseCase: SyncSearchIndexUseCase,
) : ViewModel() {

    val pokemonPager = getPokemonPagerUseCase().cachedIn(viewModelScope)

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            syncSearchIndexUseCase()
        }
        observeSearchWithDebounce()
    }

    /**
     * Menunggu pengguna berhenti mengetik sebelum menjalankan pencarian.
     */
    private fun observeSearchWithDebounce() {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _searchState.value = SearchUiState()
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    /**
     * Memperbarui kata kunci pencarian dari kolom input.
     */
    fun updateSearchQuery(query: String) {
        _searchState.value = _searchState.value.copy(
            query = query,
            errorMessage = null,
            isSearching = query.isNotBlank(),
            results = if (query.isBlank()) emptyList() else _searchState.value.results,
        )
        queryFlow.value = query
    }

    /** Menghapus pencarian dan kembali ke daftar utama. */
    fun clearSearch() {
        queryFlow.value = ""
        _searchState.value = SearchUiState()
    }

    private suspend fun performSearch(query: String) {
        _searchState.value = _searchState.value.copy(isSearching = true, errorMessage = null)
        when (val result = searchPokemonUseCase(query)) {
            is Resource.Success -> {
                _searchState.value = _searchState.value.copy(
                    query = query,
                    isSearching = false,
                    results = result.data,
                )
            }
            is Resource.Error -> {
                _searchState.value = _searchState.value.copy(
                    query = query,
                    isSearching = false,
                    errorMessage = result.message,
                    results = emptyList(),
                )
            }
            Resource.Loading -> Unit
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 400L
    }
}
