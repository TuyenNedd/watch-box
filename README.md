# Watch Box

**Movie streaming app for Android TV & phones — with Vietsub & Thuyet Minh**

Watch Box is a native Android TV application that aggregates movies from multiple free sources with Vietnamese subtitles and dubbing. Works on TV Boxes (via LEANBACK_LAUNCHER) and phones/tablets (standard launcher).

---

## Features

- **New movies daily** — Latest releases with Vietsub, Thuyet Minh, and Long Tieng from PhimAPI & OPhim
- **Multi-source fallback** — If one source goes down, others keep working seamlessly
- **Smart search** — Vietnamese accent-insensitive (works with/without diacritics)
- **Movie details** — Poster, synopsis, actors, categories, quality badge, source info
- **Favorites** — Save movies offline, no account needed
- **Continue watching** — Auto-saves progress, resume where you left off
- **Media3 player** — HLS/MP4 playback with D-pad controls and subtitle support
- **Works on both TV and phone** — TV launcher + standard Android launcher
- **Vietnamese UI** — Auto-detected based on device language (English fallback)
- **Open movies** — Bonus collection of Creative Commons films (always available offline)

---

## Movie Sources

| # | Source | Content | Fallback |
|---|--------|---------|----------|
| 1 | [PhimAPI](https://phimapi.com) (KKPhim) | New movies, Vietsub/Thuyet Minh, HD/FHD, HLS streams | Primary |
| 2 | [OPhim](https://ophim1.com) | Same catalog, different stream servers | Backup |
| 3 | Curated (Blender Foundation) | Big Buck Bunny, Sintel, Tears of Steel, Elephant's Dream | Always available |
| 4 | [Internet Archive](https://archive.org) | Community library with CC license | Additional |

---

## Installation

### From Release (recommended)

1. Download `watch-box-v1.0.1.apk` from [Releases](https://github.com/TuyenNedd/watch-box/releases)
2. Install via ADB or copy to USB:
   ```bash
   adb install watch-box-v1.0.1.apk
   ```
3. Open **Watch Box** from TV launcher or phone home screen

### Build from source

**Requirements:** JDK 17, Android SDK (API 35, Build Tools 35.0.0)

```bash
git clone https://github.com/TuyenNedd/watch-box.git
cd watch-box

# Configure SDK path
echo "sdk.dir=/path/to/your/android-sdk" > local.properties

# Build debug APK
export JAVA_HOME=/path/to/jdk-17
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

---

## Architecture

```
app/src/main/java/dev/watchbox/tv/
├── WatchBoxApplication.kt             # App container (manual DI)
├── MainActivity.kt                    # Activity + Compose entry
├── core/
│   ├── model/Movie.kt                 # Domain models
│   └── util/TextNormalizer.kt         # Vietnamese search normalization
├── data/
│   ├── catalog/
│   │   ├── CatalogSource.kt           # Provider interface
│   │   ├── PhimApiClient.kt           # PhimAPI HTTP client + DTOs
│   │   ├── PhimApiCatalogSource.kt    # PhimAPI → CatalogSource
│   │   ├── OPhimClient.kt             # OPhim HTTP client + DTOs
│   │   ├── OPhimCatalogSource.kt      # OPhim → CatalogSource (backup)
│   │   ├── CuratedCatalogSource.kt    # 4 CC seed movies
│   │   ├── InternetArchiveClient.kt   # Archive.org HTTP + DTOs
│   │   ├── InternetArchiveMapper.kt   # Defensive mapping + stream ranking
│   │   └── MovieRepository.kt         # Merge/dedupe/fallback across sources
│   └── local/
│       ├── LibraryStore.kt            # Favorites/progress interface
│       └── PreferencesLibraryStore.kt # SharedPreferences implementation
├── ui/
│   ├── WatchBoxViewModel.kt           # StateFlow state machine
│   ├── WatchBoxApp.kt                 # Navigation routes
│   ├── theme/                          # Colors, Typography, Theme
│   ├── components/                     # Hero, Card, Shelf, NavRail, States
│   └── screens/                        # Home, Search, Library, Details
└── player/
    ├── MediaItemFactory.kt            # Build Media3 items (HTTPS only)
    └── PlayerScreen.kt                # Fullscreen player + progress
```

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.1.20 |
| UI | Jetpack Compose for TV (tv-material 1.1.0) |
| Player | AndroidX Media3 ExoPlayer 1.6.0 |
| Navigation | Navigation Compose 2.8.9 |
| Networking | OkHttp 4.12.0 |
| Serialization | kotlinx-serialization 1.8.0 |
| Images | Coil 3.1.0 |
| Build | Gradle 8.11.1 + AGP 8.9.2 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

---

## Tests

```bash
export JAVA_HOME=/path/to/jdk-17
./gradlew testDebugUnitTest
```

**36 unit tests** covering:
- Vietnamese text normalization
- Internet Archive JSON mapping & stream ranking
- Curated catalog source validation
- Movie repository merge/dedupe/fallback
- Playback progress completion logic
- Library state (favorites/progress) behavior

---

## How Sources Work

The app uses a **multi-source fallback** architecture:

1. On launch, all sources are queried in parallel
2. Results are merged and deduplicated by movie slug
3. If a source fails (timeout, server down), others continue normally
4. Curated movies are always available even without internet
5. When viewing movie details, the app tries the source that listed the movie first

This means the app stays functional even if PhimAPI or OPhim go offline.

---

## Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feat/your-feature`
3. Commit changes: `git commit -m "feat: add your feature"`
4. Push: `git push origin feat/your-feature`
5. Open a Pull Request

### Adding a new movie source

Implement the `CatalogSource` interface and register it in `WatchBoxApplication.kt`:

```kotlin
interface CatalogSource {
    suspend fun featured(): List<Movie>
    suspend fun search(query: String): List<Movie>
    suspend fun details(id: String): MovieDetails?
}
```

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

## Credits

- [PhimAPI](https://phimapi.com) — Vietnamese movie metadata & streams
- [OPhim](https://ophim1.com) — Backup movie source
- [Blender Foundation](https://www.blender.org/) — Open Movies (CC)
- [Internet Archive](https://archive.org/) — Community media library
- [AndroidX Media3](https://developer.android.com/media/media3) — Video playback
- [Jetpack Compose for TV](https://developer.android.com/training/tv/playback/compose) — UI framework
