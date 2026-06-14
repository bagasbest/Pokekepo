package com.project.pokekepo.presentation.components

/**
 * Komponen UI reusable — item list Pokemon, shimmer loading.
 *
 * Dipakai oleh [HomeScreen] (list + loading) dan [DetailScreen] (shimmer detail).
 */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Card item Pokemon di daftar Home — gambar Glide + nama.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PokemonListItem(
    pokemon: PokemonSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val imageUrl = pokemon.resolveImageUrl()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlideImage(
                model = imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = pokemon.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp),
            )
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
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
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmer(),
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .height(20.dp)
                            .fillMaxWidth(0.6f)
                            .clip(MaterialTheme.shapes.small)
                            .shimmer(),
                    )
                }
            }
        }
    }
}

/** Placeholder shimmer saat detail Pokemon sedang dimuat. */
@Composable
fun DetailShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(MaterialTheme.shapes.large)
                .shimmer(),
        )
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .height(28.dp)
                .fillMaxWidth(0.5f)
                .clip(MaterialTheme.shapes.small)
                .shimmer(),
        )
        repeat(3) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .height(18.dp)
                    .fillMaxWidth(0.8f)
                    .clip(MaterialTheme.shapes.small)
                    .shimmer(),
            )
        }
    }
}
