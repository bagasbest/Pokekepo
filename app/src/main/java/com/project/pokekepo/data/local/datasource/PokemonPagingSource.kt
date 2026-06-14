package com.project.pokekepo.data.local.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.project.pokekepo.core.network.NetworkMonitor
import com.project.pokekepo.data.remote.datasource.PokemonRemoteDataSource
import com.project.pokekepo.domain.model.PokemonSummary

/**
 * Sumber paging Pokemon — menggabungkan cache Couchbase dan PokeAPI.
 *
 * Alur per halaman ([load]):
 * 1. Baca cache lokal untuk offset saat ini
 * 2. Jika cache cukup ATAU offline → pakai cache
 * 3. Jika online dan cache kurang → fetch API → simpan ke Couchbase
 * 4. Kembalikan [LoadResult.Page] dengan nextKey/prevKey untuk infinite scroll
 */
class PokemonPagingSource(
    private val localDataSource: PokemonLocalDataSource,
    private val remoteDataSource: PokemonRemoteDataSource,
    private val networkMonitor: NetworkMonitor,
) : PagingSource<Int, PokemonSummary>() {

    /**
     * Memuat satu halaman data (offset + limit).
     * Key = offset dalam daftar Pokemon (0, 10, 20, ...).
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonSummary> {
        val offset = params.key ?: 0
        val limit = params.loadSize

        return try {
            val cached = localDataSource.getCachedPage(offset, limit)
            val isOnline = networkMonitor.checkCurrentConnectivity()

            val items = if (cached.size >= limit || !isOnline) {
                cached
            } else {
                val remoteItems = remoteDataSource.fetchPokemonPage(limit, offset)
                if (remoteItems.isNotEmpty()) {
                    localDataSource.savePokemonPage(remoteItems, offset)
                }
                remoteItems.ifEmpty { cached }
            }

            val nextKey = if (items.size < limit) null else offset + limit
            val prevKey = if (offset == 0) null else maxOf(0, offset - limit)

            LoadResult.Page(
                data = items,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            val cached = localDataSource.getCachedPage(offset, limit)
            if (cached.isNotEmpty()) {
                LoadResult.Page(
                    data = cached,
                    prevKey = if (offset == 0) null else maxOf(0, offset - limit),
                    nextKey = if (cached.size < limit) null else offset + limit,
                )
            } else {
                LoadResult.Error(e)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PokemonSummary>): Int? = null
}
