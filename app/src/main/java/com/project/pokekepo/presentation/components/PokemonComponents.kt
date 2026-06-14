package com.project.pokekepo.presentation.components

/**
 * Komponen UI reusable — item list Pokemon, shimmer loading.
 *
 * Dipakai oleh [HomeScreen] (list + loading) dan [DetailScreen] (shimmer detail).
 * Shimmer detail meniru struktur [com.project.pokekepo.presentation.detail.DetailContent].
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.project.pokekepo.domain.model.PokemonSummary
import com.valentinilk.shimmer.shimmer

/**
 * Card item Pokemon di daftar Home — gambar Glide, nama, dan nomor dex.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PokemonListItem(
    pokemon: PokemonSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val imageUrl = pokemon.resolveImageUrl()
    val pokemonId = pokemon.url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                GlideImage(
                    model = imageUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "#${String.format("%03d", pokemonId)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Placeholder shimmer berbentuk card saat daftar Home pertama kali dimuat. */
@Composable
fun PokemonListShimmer(
    itemCount: Int = 8,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(itemCount) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmer(),
                    )
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth(0.55f)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(),
                        )
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .fillMaxWidth(0.25f)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Placeholder shimmer saat detail Pokemon sedang dimuat.
 *
 * Layout top-aligned (LazyColumn) meniru [com.project.pokekepo.presentation.detail.DetailContent]:
 * header (gambar 180dp, nama, chip tipe, nomor dex) dan tiga section card
 * (Informasi Fisik, Kemampuan, Statistik).
 *
 * Dipanggil dari [DetailScreen] saat [com.project.pokekepo.presentation.detail.DetailUiState.isLoading].
 */
@Composable
fun DetailShimmer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header: gambar, nama, tipe, nomor dex
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Placeholder gambar official artwork
                ShimmerBox(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                // Placeholder nama Pokemon (headlineLarge)
                ShimmerBox(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .height(32.dp)
                        .fillMaxWidth(0.55f)
                        .clip(MaterialTheme.shapes.small),
                )
                // Placeholder chip tipe (Fire, Water, dll.)
                ShimmerBox(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(width = 72.dp, height = 32.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                // Placeholder nomor dex (#005)
                ShimmerBox(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(14.dp)
                        .fillMaxWidth(0.2f)
                        .clip(MaterialTheme.shapes.small),
                )
            }
        }
        // Section Informasi Fisik — Tinggi & Berat
        item {
            DetailSectionShimmer(rowCount = 2, titleWidthFraction = 0.4f)
        }
        // Section Kemampuan — daftar ability
        item {
            DetailSectionShimmer(rowCount = 2, titleWidthFraction = 0.35f)
        }
        // Section Statistik — baris stat base
        item {
            DetailSectionShimmer(rowCount = 3, titleWidthFraction = 0.3f)
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** Box dasar dengan background [MaterialTheme.colorScheme.surfaceVariant] dan efek shimmer. */
@Composable
private fun ShimmerBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .shimmer(),
    )
}

/**
 * Skeleton satu section detail — judul + card berisi baris label/nilai.
 *
 * Meniru [com.project.pokekepo.presentation.detail.DetailSection] dengan placeholder shimmer.
 *
 * @param rowCount Jumlah baris placeholder di dalam card (mis. 2 untuk Tinggi/Berat).
 * @param titleWidthFraction Lebar relatif bar judul section (0.0–1.0).
 */
@Composable
private fun DetailSectionShimmer(
    rowCount: Int,
    titleWidthFraction: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Placeholder judul section (Informasi Fisik, Kemampuan, dll.)
        ShimmerBox(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .height(20.dp)
                .fillMaxWidth(titleWidthFraction)
                .clip(MaterialTheme.shapes.small),
        )
        // Card wrapper — sama dengan Surface di DetailSection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(rowCount) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Placeholder label (Tinggi, Blaze, HP, dll.)
                        ShimmerBox(
                            modifier = Modifier
                                .height(14.dp)
                                .fillMaxWidth(0.3f)
                                .clip(MaterialTheme.shapes.small),
                        )
                        // Placeholder nilai (1.1 m, 65, dll.)
                        ShimmerBox(
                            modifier = Modifier
                                .height(18.dp)
                                .fillMaxWidth(0.5f)
                                .clip(MaterialTheme.shapes.small),
                        )
                    }
                }
            }
        }
    }
}
