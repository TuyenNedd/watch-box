# Watch Box

**Movie streaming app for Android TV & phones — Vietsub, Thuyet Minh, Long Tieng**

Watch Box is a native Android app that aggregates movies from PhimAPI and OPhim with Vietnamese subtitles and dubbing. Works on TV Boxes (LEANBACK_LAUNCHER) and phones/tablets.

---

## Features

- **New movies daily** — Latest releases with Vietsub, Thuyet Minh, Long Tieng
- **Multi-source fallback** — PhimAPI (primary) + OPhim (backup), auto-failover
- **Smart search** — Vietnamese accent-insensitive (works with/without diacritics)
- **Movie details** — Poster, synopsis, actors, categories, country, quality badge
- **Series support** — Episode list with individual stream links
- **Favorites** — Save movies offline, no account needed
- **Continue watching** — Auto-saves progress, resume where you left off
- **Media3 player** — HLS streaming with D-pad controls
- **Works on both TV and phone** — TV launcher + standard Android launcher
- **Vietnamese UI** — Auto-detected by device language (English fallback)

---

## Movie Sources

| # | Source | Role | Stream Server |
|---|--------|------|---------------|
| 1 | [PhimAPI](https://phimapi.com) | Primary | kkphimplayer |
| 2 | [OPhim](https://ophim1.com) | Backup | opstream |

Both sources provide the same movie catalog but use different stream servers. If one goes down, the other keeps working seamlessly.

---

## Installation

### From Release (recommended)

1. Download `watch-box-v1.0.1.apk` from [Releases](https://github.com/TuyenNedd/watch-box/releases)
2. Install:
   ```bash
   adb install watch-box-v1.0.1.apk
   ```
   Or copy APK to USB and install from File Manager on TV Box.
3. Open **Watch Box** from TV launcher or phone home screen

### Build from source

**Requirements:** JDK 17, Android SDK (API 35, Build Tools 35.0.0)

```bash
git clone https://github.com/TuyenNedd/watch-box.git
cd watch-box
echo "sdk.dir=/path/to/your/android-sdk" > local.properties
export JAVA_HOME=/path/to/jdk-17
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

## Architecture

```
app/src/main/java/dev/watchbox/tv/
├── WatchBoxApplication.kt             # App container (manual DI)
├── MainActivity.kt                    # Activity + Compose entry
├── core/
│   ├── model/Movie.kt                 # Domain: Movie, MovieDetails, Episode, PlaybackSource
│   └── util/TextNormalizer.kt         # Vietnamese accent-insensitive search
├── data/
│   ├── catalog/
│   │   ├── CatalogSource.kt           # Provider interface
│   │   ├── PhimApiClient.kt           # PhimAPI HTTP client + DTOs
│   │   ├── PhimApiCatalogSource.kt    # PhimAPI → CatalogSource (primary)
│   │   ├── OPhimClient.kt             # OPhim HTTP client + DTOs
│   │   ├── OPhimCatalogSource.kt      # OPhim → CatalogSource (backup)
│   │   └── MovieRepository.kt         # Merge/dedupe/fallback across sources
│   └── local/
│       ├── LibraryStore.kt            # Favorites/progress interface
│       └── PreferencesLibraryStore.kt # SharedPreferences implementation
├── ui/
│   ├── WatchBoxViewModel.kt           # StateFlow state machine
│   ├── WatchBoxApp.kt                 # Navigation routes
│   ├── theme/                          # Colors, Typography, Theme
│   ├── components/                     # Hero, Card (with badges), Shelf, NavRail
│   └── screens/                        # Home, Search, Library, Details (with episodes)
└── player/
    ├── MediaItemFactory.kt            # Build Media3 items (HLS)
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

**20 unit tests** covering:
- Vietnamese text normalization
- Movie repository merge/dedupe/fallback
- Playback progress completion logic
- Library state (favorites/progress) behavior

---

## How It Works

1. On launch, PhimAPI and OPhim are queried in parallel
2. Results are merged and deduplicated by movie slug
3. If PhimAPI fails → OPhim results are shown (and vice versa)
4. Movie cards display quality (HD/FHD) and language (Vietsub/TM) badges
5. For series: episode list is shown on details screen
6. Selecting an episode → Media3 player streams HLS directly

---

## Adding a New Source

Implement the `CatalogSource` interface and register in `WatchBoxApplication.kt`:

```kotlin
interface CatalogSource {
    suspend fun featured(): List<Movie>
    suspend fun search(query: String): List<Movie>
    suspend fun details(id: String): MovieDetails?
}
```

---

## Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feat/your-feature`
3. Commit: `git commit -m "feat: add your feature"`
4. Push: `git push origin feat/your-feature`
5. Open a Pull Request

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

## Credits

- [PhimAPI](https://phimapi.com) — Movie metadata & HLS streams
- [OPhim](https://ophim1.com) — Backup movie source
- [AndroidX Media3](https://developer.android.com/media/media3) — Video playback
- [Jetpack Compose for TV](https://developer.android.com/training/tv/playback/compose) — UI framework
