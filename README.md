# Pokekepo

Aplikasi Android daftar Pokemon dengan autentikasi lokal, cache offline, dan arsitektur Clean Architecture.

## Fitur

- Login & registrasi pengguna (Couchbase Lite + BCrypt)
- Tab Home (daftar Pokemon + pencarian debounce) dan Profile
- Infinite scroll 10 item per halaman (Paging 3 + Retrofit PokeAPI)
- Detail Pokemon: tipe, statistik, kemampuan, info fisik, official artwork
- Dukungan offline via cache Couchbase
- Foto profil disimpan sebagai Base64 lokal
- Jetpack Compose, Navigation 3, Koin, Glide, Shimmer

## Dokumentasi Lengkap

📖 **[docs/ARSITEKTUR.md](docs/ARSITEKTUR.md)** — penjelasan alur aplikasi, lapisan arsitektur, online/offline, dan diagram alur data (Bahasa Indonesia).

Setiap file `.kt` juga memiliki komentar KDoc di fungsi/kelas penting untuk memudahkan pemahaman alur kode.

## Stack

| Layer | Teknologi |
|-------|-----------|
| UI | Jetpack Compose, Material 3 |
| Arsitektur | Clean Architecture (Domain / Data / Presentation) |
| DI | Koin |
| Network | Retrofit + Coroutines |
| Database | Couchbase Lite |
| Sesi | DataStore Preferences |
| Navigation | Navigation 3 |
| Paging | Paging 3 |
| Image | Glide Compose |
| Loading UI | Compose Shimmer |

## Struktur Paket

```
com.project.pokekepo/
├── core/          # Network, database, util, DI
├── domain/        # Model, repository interface, use case
├── data/          # Remote/local data source, repository impl
├── navigation/    # NavKey & NavHost
└── presentation/  # Screen & ViewModel
```

## Cara Menjalankan

1. Clone repository ini
2. Buka dengan Android Studio (compileSdk 37+)
3. Sync Gradle
4. Jalankan di emulator/perangkat (minSdk 23)

## Alur Singkat

```
App Start → Koin init → NavHost cek sesi
  → Login/Register → Main (Home + Profile)
  → Home: Paging list / Search → Detail Pokemon
  → Profile: foto Base64 + logout
```

Detail lengkap: [docs/ARSITEKTUR.md](docs/ARSITEKTUR.md)

## Penguji

Undang `wibowo@code.id` sebagai collaborator di GitHub/GitLab sebelum submission.

## API

Menggunakan [PokeAPI](https://pokeapi.co/api/v2) — tidak berafiliasi dengan PokeAPI resmi.
