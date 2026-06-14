package com.project.pokekepo.data.local.datasource

import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Function
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.project.pokekepo.core.database.CouchbaseManager
import com.project.pokekepo.core.util.PasswordHasher
import com.project.pokekepo.data.local.entity.PokemonDetailDocument
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.pokekepo.data.local.entity.PokemonStatDocument
import com.project.pokekepo.data.local.entity.PokemonIndexDocument
import com.project.pokekepo.data.local.entity.PokemonListDocument
import com.project.pokekepo.data.local.entity.UserDocument
import com.project.pokekepo.data.mapper.PokemonMapper
import com.project.pokekepo.domain.model.PokemonDetail
import com.project.pokekepo.domain.model.PokemonStat
import com.project.pokekepo.domain.model.PokemonSummary
import com.project.pokekepo.domain.model.User

/**
 * Data source lokal — akses langsung ke Couchbase Lite.
 *
 * [AuthLocalDataSource]: CRUD pengguna (register, login, profil, foto).
 * [PokemonLocalDataSource]: cache list, detail, dan indeks pencarian Pokemon.
 */

/** Menyimpan dan memverifikasi akun pengguna di koleksi `users`. */
class AuthLocalDataSource(
    private val couchbaseManager: CouchbaseManager,
) {

    private val users
        get() = couchbaseManager.collection(CouchbaseManager.COLLECTION_USERS)

    /** Mendaftarkan pengguna baru; gagal jika email sudah ada. */
    suspend fun registerUser(name: String, email: String, passwordHash: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        if (users.getDocument(normalizedEmail) != null) {
            return Result.failure(IllegalStateException("Email sudah terdaftar"))
        }

        val document = UserDocument(
            name = name.trim(),
            email = normalizedEmail,
            passwordHash = passwordHash,
        )
        users.save(document.toMutableDocument(normalizedEmail))
        return Result.success(document.toUser())
    }

    /** Memverifikasi email dan hash password; mengembalikan [User] jika cocok. */
    suspend fun loginUser(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val document = users.getDocument(normalizedEmail)
            ?: return Result.failure(IllegalStateException("Email atau kata sandi salah"))

        val userDoc = document.toUserDocument()
        if (!PasswordHasher.verify(password, userDoc.passwordHash)) {
            return Result.failure(IllegalStateException("Email atau kata sandi salah"))
        }

        return Result.success(userDoc.toUser())
    }

    /**
     * Mengambil data profil lengkap pengguna berdasarkan email.
     */
    suspend fun getUserProfile(email: String): User? {
        val normalizedEmail = email.trim().lowercase()
        val document = users.getDocument(normalizedEmail) ?: return null
        return document.toUserDocument().toUser()
    }

    /**
     * Menyimpan foto profil pengguna dalam bentuk Base64 ke Couchbase.
     */
    suspend fun saveProfileImage(email: String, profileImageBase64: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val document = users.getDocument(normalizedEmail)
            ?: return Result.failure(IllegalStateException("Pengguna tidak ditemukan"))

        val userDoc = document.toUserDocument().copy(profileImageBase64 = profileImageBase64)
        users.save(userDoc.toMutableDocument(normalizedEmail))
        return Result.success(userDoc.toUser())
    }

    private fun UserDocument.toUser(): User {
        return User(
            name = name,
            email = email,
            profileImageBase64 = profileImageBase64,
        )
    }

    private fun UserDocument.toMutableDocument(id: String): MutableDocument {
        return MutableDocument(id).apply {
            setString("type", UserDocument.TYPE)
            setString("name", name)
            setString("email", email)
            setString("passwordHash", passwordHash)
            if (!profileImageBase64.isNullOrBlank()) {
                setString("profileImageBase64", profileImageBase64)
            }
            setLong("createdAt", createdAt)
        }
    }

    private fun com.couchbase.lite.Document.toUserDocument(): UserDocument {
        return UserDocument(
            name = getString("name").orEmpty(),
            email = getString("email").orEmpty(),
            passwordHash = getString("passwordHash").orEmpty(),
            profileImageBase64 = getString("profileImageBase64"),
            createdAt = getLong("createdAt"),
        )
    }
}

/** Menyimpan cache Pokemon: daftar paging, detail, dan indeks pencarian. */
class PokemonLocalDataSource(
    private val couchbaseManager: CouchbaseManager,
    private val gson: Gson,
) {

    private val listCollection
        get() = couchbaseManager.collection(CouchbaseManager.COLLECTION_POKEMON_LIST)

    private val detailCollection
        get() = couchbaseManager.collection(CouchbaseManager.COLLECTION_POKEMON_DETAIL)

    private val indexCollection
        get() = couchbaseManager.collection(CouchbaseManager.COLLECTION_POKEMON_INDEX)

    /** Menyimpan satu halaman hasil paging ke Couchbase dengan offset urut. */
    suspend fun savePokemonPage(items: List<PokemonSummary>, offset: Int) {
        items.forEachIndexed { index, item ->
            val doc = PokemonListDocument(
                name = item.name,
                url = item.url,
                listOffset = offset + index,
                spriteUrl = item.spriteUrl,
            )
            listCollection.save(doc.toMutableDocument("${item.name}_$offset"))
        }
    }

    /** Membaca cache halaman daftar berdasarkan offset dan limit (query Couchbase). */
    suspend fun getCachedPage(offset: Int, limit: Int): List<PokemonSummary> {
        val query = QueryBuilder
            .select(
                SelectResult.property("name"),
                SelectResult.property("url"),
                SelectResult.property("spriteUrl"),
            )
            .from(DataSource.collection(listCollection))
            .where(
                Expression.property("type").equalTo(Expression.string(PokemonListDocument.TYPE))
                    .and(Expression.property("listOffset").greaterThanOrEqualTo(Expression.intValue(offset))),
            )
            .orderBy(com.couchbase.lite.Ordering.property("listOffset").ascending())
            .limit(Expression.intValue(limit))

        val result = query.execute()
        val items = mutableListOf<PokemonSummary>()
        result.forEach { row ->
            val name = row.getString("name") ?: return@forEach
            val url = row.getString("url") ?: return@forEach
            val spriteUrl = row.getString("spriteUrl").orEmpty()
            items.add(PokemonMapper.summaryFromDocument(name, url, spriteUrl))
        }
        return items
    }

    /** Menyimpan detail Pokemon ke koleksi `pokemon_detail` (key = nama lowercase). */
    suspend fun savePokemonDetail(detail: PokemonDetail) {
        val doc = PokemonDetailDocument(
            pokemonId = detail.id,
            name = detail.name,
            abilities = detail.abilities,
            hiddenAbilities = detail.hiddenAbilities,
            types = detail.types,
            stats = detail.stats.map { PokemonStatDocument(name = it.name, baseStat = it.baseStat) },
            height = detail.height,
            weight = detail.weight,
            spriteUrl = detail.spriteUrl,
        )
        detailCollection.save(doc.toMutableDocument(detail.name.lowercase()))
    }

    /** Membaca detail dari cache; null jika belum pernah di-fetch. */
    suspend fun getCachedDetail(name: String): PokemonDetail? {
        val document = detailCollection.getDocument(name.lowercase()) ?: return null
        val detailDoc = document.toDetailDocument()
        return PokemonMapper.detailFromDocument(
            id = detailDoc.pokemonId,
            name = detailDoc.name,
            abilities = detailDoc.abilities,
            hiddenAbilities = detailDoc.hiddenAbilities,
            types = detailDoc.types,
            stats = detailDoc.stats.map { PokemonStat(name = it.name, baseStat = it.baseStat) },
            height = detailDoc.height,
            weight = detailDoc.weight,
            spriteUrl = detailDoc.spriteUrl,
        )
    }

    /** Menyimpan seluruh indeks nama untuk pencarian offline. */
    suspend fun saveSearchIndex(items: List<PokemonSummary>) {
        items.forEach { item ->
            val doc = PokemonIndexDocument(name = item.name, url = item.url)
            indexCollection.save(doc.toMutableDocument(item.name))
        }
    }

    /** Pencarian partial match (LIKE) pada nama Pokemon; max 20 hasil. */
    suspend fun searchByName(query: String): List<PokemonSummary> {
        if (query.isBlank()) return emptyList()

        val normalizedQuery = query.trim().lowercase()
        val queryBuilder = QueryBuilder
            .select(
                SelectResult.property("name"),
                SelectResult.property("url"),
            )
            .from(DataSource.collection(indexCollection))
            .where(
                Expression.property("type").equalTo(Expression.string(PokemonIndexDocument.TYPE))
                    .and(
                        Function.lower(Expression.property("name"))
                            .like(Expression.string("%$normalizedQuery%")),
                    ),
            )
            .limit(Expression.intValue(20))

        val result = queryBuilder.execute()
        val items = mutableListOf<PokemonSummary>()
        result.forEach { row ->
            val name = row.getString("name") ?: return@forEach
            val url = row.getString("url") ?: return@forEach
            items.add(PokemonMapper.summaryFromDocument(name, url))
        }
        return items
    }

    /** Mengecek apakah indeks pencarian sudah pernah di-sync. */
    suspend fun hasSearchIndex(): Boolean {
        val query = QueryBuilder
            .select(SelectResult.expression(Expression.intValue(1)))
            .from(DataSource.collection(indexCollection))
            .where(Expression.property("type").equalTo(Expression.string(PokemonIndexDocument.TYPE)))
            .limit(Expression.intValue(1))

        return query.execute().allResults().isNotEmpty()
    }

    private fun PokemonListDocument.toMutableDocument(id: String): MutableDocument {
        return MutableDocument(id).apply {
            setString("type", PokemonListDocument.TYPE)
            setString("name", name)
            setString("url", url)
            setInt("listOffset", listOffset)
            setString("spriteUrl", spriteUrl)
            setLong("cachedAt", cachedAt)
        }
    }

    private fun PokemonDetailDocument.toMutableDocument(id: String): MutableDocument {
        return MutableDocument(id).apply {
            setString("type", PokemonDetailDocument.TYPE)
            setInt("pokemonId", pokemonId)
            setString("name", name)
            setArray("abilities", com.couchbase.lite.MutableArray().apply {
                abilities.forEach { addString(it) }
            })
            setArray("hiddenAbilities", com.couchbase.lite.MutableArray().apply {
                hiddenAbilities.forEach { addString(it) }
            })
            setArray("types", com.couchbase.lite.MutableArray().apply {
                types.forEach { addString(it) }
            })
            setString("statsJson", gson.toJson(stats))
            setInt("height", height)
            setInt("weight", weight)
            setString("spriteUrl", spriteUrl)
            setLong("cachedAt", cachedAt)
        }
    }

    private fun PokemonIndexDocument.toMutableDocument(id: String): MutableDocument {
        return MutableDocument(id).apply {
            setString("type", PokemonIndexDocument.TYPE)
            setString("name", name)
            setString("url", url)
        }
    }

    private fun com.couchbase.lite.Document.toDetailDocument(): PokemonDetailDocument {
        val abilityList = getArray("abilities")?.toList()?.mapNotNull { it as? String }.orEmpty()
        val hiddenList = getArray("hiddenAbilities")?.toList()?.mapNotNull { it as? String }.orEmpty()
        val typeList = getArray("types")?.toList()?.mapNotNull { it as? String }.orEmpty()
        val statsJson = getString("statsJson").orEmpty()
        val statList = if (statsJson.isBlank()) {
            emptyList()
        } else {
            gson.fromJson<List<PokemonStatDocument>>(
                statsJson,
                object : TypeToken<List<PokemonStatDocument>>() {}.type,
            )
        }
        return PokemonDetailDocument(
            pokemonId = getInt("pokemonId"),
            name = getString("name").orEmpty(),
            abilities = abilityList,
            hiddenAbilities = hiddenList,
            types = typeList,
            stats = statList,
            height = getInt("height"),
            weight = getInt("weight"),
            spriteUrl = getString("spriteUrl").orEmpty(),
        )
    }
}
