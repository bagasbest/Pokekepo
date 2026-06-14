package com.project.pokekepo.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.project.pokekepo.domain.model.PokemonDetail
import com.project.pokekepo.domain.model.PokemonStat
import com.project.pokekepo.presentation.components.DetailShimmer
import com.project.pokekepo.ui.theme.PokemonBlue
import com.project.pokekepo.ui.theme.PokemonRed
import com.project.pokekepo.ui.theme.PokemonYellow
import org.koin.androidx.compose.koinViewModel

/**
 * Layar detail Pokemon — menampilkan data lengkap dari PokeAPI.
 *
 * Section: gambar official artwork, tipe, info fisik, kemampuan, statistik.
 * Data di-load via [DetailViewModel.loadDetail] saat [pokemonName] berubah.
 * Saat loading, tampil [DetailShimmer] (skeleton top-aligned) sebelum [DetailContent].
 */
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    pokemonName: String,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pokemonName) {
        viewModel.loadDetail(pokemonName)
    }

    when {
        // Skeleton shimmer — posisi sama dengan DetailContent (bukan centered)
        uiState.isLoading -> {
            DetailShimmer(modifier = modifier.fillMaxSize())
        }
        uiState.detail != null -> {
            DetailContent(
                detail = uiState.detail!!,
                modifier = modifier,
            )
        }
        else -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = uiState.errorMessage ?: "Data tidak ditemukan",
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.loadDetail(pokemonName) }) {
                    Text("Coba Lagi")
                }
            }
        }
    }
}

/** Konten scrollable detail: header, tipe, fisik, kemampuan, stat. */
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    detail: PokemonDetail,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (detail.spriteUrl.isNotBlank()) {
                    GlideImage(
                        model = detail.spriteUrl,
                        contentDescription = detail.name,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit,
                    )
                }

                Text(
                    text = detail.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                )

                if (detail.types.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        detail.types.forEach { typeName ->
                            TypeChip(typeName = typeName)
                        }
                    }
                }

                Text(
                    text = "#${String.format("%03d", detail.id)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            DetailSection(title = "Informasi Fisik") {
                InfoRow(label = "Tinggi", value = formatHeight(detail.height))
                InfoRow(label = "Berat", value = formatWeight(detail.weight))
            }
        }

        if (detail.abilities.isNotEmpty() || detail.hiddenAbilities.isNotEmpty()) {
            item {
                DetailSection(title = "Kemampuan") {
                    detail.abilities.forEach { ability ->
                        Text(
                            text = "• $ability",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    detail.hiddenAbilities.forEach { ability ->
                        Text(
                            text = "• $ability (Tersembunyi)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }

        if (detail.stats.isNotEmpty()) {
            item {
                DetailSection(title = "Statistik") {
                    detail.stats.forEach { stat ->
                        StatRow(stat = stat)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/** Section dengan judul dan card wrapper di layar detail. */
@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                content()
            }
        }
    }
}

/** Chip berwarna untuk tipe Pokemon (Water, Fire, dll.). */
@Composable
private fun TypeChip(typeName: String) {
    val chipColor = typeColor(typeName)
    SuggestionChip(
        onClick = { },
        label = { Text(typeName) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = chipColor.copy(alpha = 0.25f),
            labelColor = chipColor,
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = chipColor.copy(alpha = 0.5f),
        ),
    )
}

/** Baris label + nilai untuk tinggi/berat Pokemon. */
@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** Baris statistik sederhana: nama stat di kiri, angka di kanan. */
@Composable
private fun StatRow(stat: PokemonStat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stat.name,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stat.baseStat.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun formatHeight(decimeters: Int): String {
    if (decimeters <= 0) return "-"
    val meters = decimeters / 10.0
    return if (meters % 1.0 == 0.0) "${meters.toInt()} m" else "$meters m"
}

private fun formatWeight(hectograms: Int): String {
    if (hectograms <= 0) return "-"
    val kg = hectograms / 10.0
    return if (kg % 1.0 == 0.0) "${kg.toInt()} kg" else "$kg kg"
}

private fun typeColor(typeName: String): Color {
    return when (typeName.lowercase()) {
        "fire" -> Color(0xFFFF5722)
        "water" -> PokemonBlue
        "grass" -> Color(0xFF4CAF50)
        "electric" -> PokemonYellow
        "ice" -> Color(0xFF00BCD4)
        "fighting" -> Color(0xFFD32F2F)
        "poison" -> Color(0xFF9C27B0)
        "ground" -> Color(0xFF795548)
        "flying" -> Color(0xFF03A9F4)
        "psychic" -> Color(0xFFE91E63)
        "bug" -> Color(0xFF8BC34A)
        "rock" -> Color(0xFF795548)
        "ghost" -> Color(0xFF673AB7)
        "dragon" -> Color(0xFF673AB7)
        "dark" -> Color(0xFF424242)
        "steel" -> Color(0xFF607D8B)
        "fairy" -> Color(0xFFF48FB1)
        "normal" -> Color(0xFF9E9E9E)
        else -> PokemonRed
    }
}
