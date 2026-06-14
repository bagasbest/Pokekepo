# Dokumentasi Arsitektur — Pokekepo

Aplikasi Android daftar Pokemon dengan autentikasi lokal, cache offline, dan **Clean Architecture**.

---

## Daftar Isi

1. [Ringkasan](#ringkasan)
2. [Alur Aplikasi Secara Keseluruhan](#alur-aplikasi-secara-keseluruhan)
3. [Lapisan Arsitektur](#lapisan-arsitektur)
4. [Alur Fitur Utama](#alur-fitur-utama)
5. [Penyimpanan Data](#penyimpanan-data)
6. [Strategi Online / Offline](#strategi-online--offline)
7. [Dependency Injection (Koin)](#dependency-injection-koin)
8. [Navigasi](#navigasi)
9. [Stack Teknologi](#stack-teknologi)
10. [Struktur Paket](#struktur-paket)

---

## Ringkasan

Pokekepo menampilkan daftar Pokemon dari [PokeAPI](https://pokeapi.co/) dengan:

- **Login/Register** lokal (Couchbase Lite + BCrypt)
- **Infinite scroll** 10 item/halaman (Paging 3)
- **Pencarian** dengan debounce 400 ms (indeks lokal)
- **Detail Pokemon** lengkap (tipe, stat, kemampuan, foto)
- **Profil** dengan foto Base64
- **Mode offline** via cache Couchbase

---

## Alur Aplikasi Secara Keseluruhan

```
[Android Start]
      │
      ▼
PokekepoApplication.onCreate()
      │  → startKoin (injeksi dependensi)
      ▼
MainActivity.onCreate()
      │  → PokekepoTheme + PokekepoNavHost
      ▼
AppViewModel.observeSession()  ← DataStore (sesi login)
      │
      ├── Belum login ──► LoginScreen / RegisterScreen
      │                        │
      │                        ▼ (sukses)
      └── Sudah login ──► MainScreen
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
               HomeScreen          ProfileScreen
                    │
                    ▼ (klik Pokemon)
               DetailScreen
```

---

## Lapisan Arsitektur

Aplikasi dibagi 4 lapisan dengan aturan ketergantungan:

```
Presentation  →  Domain  ←  Data
     │              │         │
  ViewModel    Use Case   Repository
  Compose UI   Model      DataSource
                          Remote / Local
```

| Lapisan | Folder | Tugas |
|---------|--------|-------|
| **Presentation** | `presentation/` | UI Compose, ViewModel, state UI |
| **Domain** | `domain/` | Model, kontrak Repository, Use Case |
| **Data** | `data/` | Implementasi Repository, API, Couchbase |
| **Core** | `core/` | DI, network, database, utilitas |

**Aturan:** Domain tidak boleh import Android/Retrofit/Couchbase. Presentation hanya panggil Use Case, bukan Repository langsung.

---

## Alur Fitur Utama

### 1. Registrasi & Login

```
LoginScreen / RegisterScreen
      │  AuthViewModel.login() / register()
      ▼
LoginUseCase / RegisterUseCase
      ▼
AuthRepositoryImpl
      ├── PasswordHasher (BCrypt) — hash/verify password
      ├── AuthLocalDataSource — simpan/baca user di Couchbase
      └── SessionLocalDataSource — simpan sesi ke DataStore
      ▼
AppViewModel.sessionUser berubah → NavHost redirect ke Main
```

### 2. Daftar Pokemon (Home)

```
HomeScreen
      │  HomeViewModel.pokemonPager (Paging 3)
      ▼
GetPokemonPagerUseCase
      ▼
PokemonRepositoryImpl.getPokemonPager()
      ▼
PokemonPagingSource.load(offset)
      ├── PokemonLocalDataSource.getCachedPage()  ← cache dulu
      ├── NetworkMonitor.checkCurrentConnectivity()
      └── PokemonRemoteDataSource.fetchPokemonPage()  ← jika online
      ▼
PokemonListItem (Card + GlideImage)
```

### 3. Pencarian Pokemon

```
HomeSearchBar → updateSearchQuery()
      │  debounce 400 ms
      ▼
SearchPokemonUseCase
      ▼
PokemonLocalDataSource.searchByName()  ← query LIKE Couchbase
      │
      └── (pertama kali) SyncSearchIndexUseCase → unduh ~1300 nama ke indeks
```

### 4. Detail Pokemon

```
DetailScreen → DetailViewModel.loadDetail(name)
      ▼
GetPokemonDetailUseCase
      ▼
PokemonRepositoryImpl.getPokemonDetail()
      ├── getCachedDetail() dari Couchbase
      ├── fetchPokemonDetail() dari PokeAPI (jika online)
      └── savePokemonDetail() ke cache
      ▼
PokemonMapper.toDetail() — DTO → Domain
      ▼
DetailContent — tipe, fisik, kemampuan, stat
```

### 5. Profil & Foto

```
ProfileScreen → picker galeri
      ▼
ImageBase64Util.encodeFromUri()
      ▼
ProfileViewModel.saveProfileImage()
      ▼
SaveProfileImageUseCase → AuthLocalDataSource → Couchbase
      ▼
GlideImage (data URI Base64) tampilkan avatar
```

### 6. Logout

```
ProfileViewModel.logout()
      ▼
LogoutUseCase → SessionLocalDataSource.clearSession()
      ▼
AppViewModel.sessionUser = null → NavHost ke LoginScreen
```

---

## Penyimpanan Data

### Couchbase Lite (`pokekepo_db`)

| Koleksi | Isi | Dipakai untuk |
|---------|-----|---------------|
| `users` | Akun, hash password, foto Base64 | Auth |
| `pokemon_list` | Cache halaman list + offset | Paging Home |
| `pokemon_detail` | Detail lengkap per nama | Detail offline |
| `pokemon_index` | Indeks nama + URL | Pencarian |

### DataStore Preferences (`session_prefs`)

| Key | Isi |
|-----|-----|
| `user_email` | Email pengguna login |
| `user_name` | Nama pengguna login |

Sesi disimpan di DataStore (bukan Couchbase) agar Flow reaktif untuk redirect navigasi.

---

## Strategi Online / Offline

| Fitur | Online | Offline |
|-------|--------|---------|
| List Pokemon | Fetch API → cache | Baca cache Couchbase |
| Detail | Fetch API → update cache | Baca cache jika ada |
| Pencarian | Sync indeks (sekali) → cari lokal | Cari dari indeks lokal |
| Login/Register | — | Selalu lokal (Couchbase) |
| Profil | — | Selalu lokal |

`NetworkMonitor` mengecek `NET_CAPABILITY_VALIDATED` sebelum memanggil API.

---

## Dependency Injection (Koin)

Diinisialisasi di `PokekepoApplication`:

| Modul | Isi |
|-------|-----|
| `coreModule` | DispatcherProvider |
| `networkModule` | PokeApiService, NetworkMonitor |
| `databaseModule` | CouchbaseManager, Gson, SessionLocalDataSource |
| `dataSourceModule` | AuthLocal, PokemonLocal, PokemonRemote |
| `repositoryModule` | AuthRepositoryImpl, PokemonRepositoryImpl |
| `useCaseModule` | Semua Use Case |
| `viewModelModule` | Semua ViewModel |

---

## Navigasi

Menggunakan **Navigation 3** dengan type-safe keys (`AppNavKey`):

| Key | Layar |
|-----|-------|
| `Login` | Login |
| `Register` | Registrasi |
| `Main` | Home + Profile (bottom tab) |
| `Detail(pokemonName)` | Detail Pokemon |

Back stack dikelola `PokekepoNavHost`. Redirect login/logout otomatis via `LaunchedEffect(sessionUser)`.

---

## Stack Teknologi

| Area | Library |
|------|---------|
| UI | Jetpack Compose, Material 3 |
| Arsitektur | Clean Architecture |
| DI | Koin 4 |
| Network | Retrofit, OkHttp, Gson |
| Database | Couchbase Lite 3 |
| Sesi | DataStore Preferences |
| Paging | Paging 3 |
| Navigation | Navigation 3 |
| Gambar | Glide Compose |
| Loading | Compose Shimmer |
| Password | BCrypt |
| Async | Kotlin Coroutines + Flow |

---

## Struktur Paket

```
com.project.pokekepo/
├── PokekepoApplication.kt      # Entry: init Koin
├── MainActivity.kt             # Entry: Compose UI
├── core/
│   ├── database/               # CouchbaseManager
│   ├── di/                     # Modul Koin
│   ├── network/                # Retrofit, NetworkMonitor
│   └── util/                   # Resource, Hash, Base64, Dispatcher
├── domain/
│   ├── model/                  # User, PokemonSummary, PokemonDetail
│   ├── repository/             # Interface Auth & Pokemon
│   └── usecase/                # Login, Paging, Search, dll.
├── data/
│   ├── remote/                 # API, DTO, RemoteDataSource
│   ├── local/                  # Couchbase, PagingSource, Session
│   ├── mapper/                 # DTO ↔ Domain
│   └── repository/             # Implementasi repository
├── navigation/                 # NavKey, NavHost
├── presentation/
│   ├── auth/                   # Login, Register
│   ├── home/                   # List + search
│   ├── detail/                 # Detail Pokemon
│   ├── profile/                # Profil + logout
│   ├── main/                   # Tab + AppViewModel
│   └── components/             # ListItem, Shimmer
└── ui/theme/                   # Warna, tipografi, tema
```

---

## Cara Menjalankan

1. Clone repository
2. Buka di Android Studio (compileSdk 37+)
3. Sync Gradle
4. Run di emulator/perangkat (minSdk 23)

---

## API

Menggunakan [PokeAPI](https://pokeapi.co/api/v2) — tidak berafiliasi dengan PokeAPI resmi.

## Penguji

Undang `wibowo@code.id` sebagai collaborator sebelum submission.
