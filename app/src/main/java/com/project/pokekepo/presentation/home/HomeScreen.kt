package com.project.pokekepo.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.project.pokekepo.presentation.components.PokemonListItem
import com.project.pokekepo.presentation.components.PokemonListShimmer
import com.project.pokekepo.ui.theme.PokemonYellow
import org.koin.androidx.compose.koinViewModel

/**
 * Layar Home — daftar Pokemon dengan infinite scroll dan pencarian.
 *
 * Alur:
 * - Kosong query → [PokemonPagingList] (Paging 3 + card + Glide)
 * - Ada query → [SearchResultsList] (debounce dari ViewModel)
 * - Klik item → [onPokemonClick] navigasi ke Detail
 */
@Composable
fun HomeScreen(
    onPokemonClick: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val pokemonItems = viewModel.pokemonPager.collectAsLazyPagingItems()
    val loadState = pokemonItems.loadState

    Column(modifier = Modifier.fillMaxSize()) {
        HomeSearchBar(
            query = searchState.query,
            isSearching = searchState.isSearching,
            onQueryChange = viewModel::updateSearchQuery,
            onClear = viewModel::clearSearch,
        )

        if (searchState.isSearchActive) {
            SearchResultsList(
                searchState = searchState,
                onPokemonClick = onPokemonClick,
            )
        } else {
            PokemonPagingList(
                pokemonItems = pokemonItems,
                loadState = loadState,
                onPokemonClick = onPokemonClick,
            )
        }
    }
}

/** Kolom pencarian di bagian atas Home dengan indikator loading debounce. */
@Composable
private fun HomeSearchBar(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = { Text("Cari Pokemon…") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Cari")
        },
        trailingIcon = {
            when {
                isSearching && query.isNotBlank() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = PokemonYellow,
                    )
                }
                query.isNotBlank() -> {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                    }
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PokemonYellow,
            cursorColor = PokemonYellow,
        ),
    )
}

/** Menampilkan hasil pencarian lokal dari indeks Couchbase. */
@Composable
private fun SearchResultsList(
    searchState: SearchUiState,
    onPokemonClick: (String) -> Unit,
) {
    when {
        searchState.errorMessage != null -> {
            Text(
                text = searchState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
            )
        }
        !searchState.isSearching && searchState.results.isEmpty() -> {
            Text(
                text = "Pokemon \"${searchState.query}\" tidak ditemukan",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = searchState.results,
                    key = { it.name },
                ) { pokemon ->
                    PokemonListItem(
                        pokemon = pokemon,
                        onClick = { onPokemonClick(pokemon.name) },
                    )
                }
            }
        }
    }
}

/** Daftar Pokemon dengan Paging 3 — load 10 item per halaman, shimmer saat refresh. */
@Composable
private fun PokemonPagingList(
    pokemonItems: androidx.paging.compose.LazyPagingItems<com.project.pokekepo.domain.model.PokemonSummary>,
    loadState: androidx.paging.CombinedLoadStates,
    onPokemonClick: (String) -> Unit,
) {
    when {
        loadState.refresh is LoadState.Loading && pokemonItems.itemCount == 0 -> {
            PokemonListShimmer(modifier = Modifier.padding(top = 8.dp))
        }
        loadState.refresh is LoadState.Error && pokemonItems.itemCount == 0 -> {
            Text(
                text = (loadState.refresh as LoadState.Error).error.localizedMessage
                    ?: "Gagal memuat data Pokemon",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
            )
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    count = pokemonItems.itemCount,
                    key = pokemonItems.itemKey { it.name },
                ) { index ->
                    val pokemon = pokemonItems[index]
                    if (pokemon != null) {
                        PokemonListItem(
                            pokemon = pokemon,
                            onClick = { onPokemonClick(pokemon.name) },
                        )
                    }
                }

                when (val appendState = loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = PokemonYellow)
                            }
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Text(
                                text = appendState.error.localizedMessage ?: "Gagal memuat data",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}
