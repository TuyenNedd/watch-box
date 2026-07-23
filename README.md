# Watch Box

**Free and open-licensed movie streaming app for Android TV**

Watch Box is a native Android TV application designed for TV Boxes. It is fully controllable via remote/D-pad and streams only Creative Commons / openly licensed movies with Vietnamese subtitle support.

---

## Features

- **Discover movies** — Home screen with hero banner and horizontally scrollable shelves
- **Search** — Vietnamese accent-insensitive search (diacritics removed automatically)
- **Movie details** — Poster, synopsis, metadata, source & license clearly displayed
- **Favorites** — Save favorite movies offline
- **Continue watching** — Automatically saves progress, resume from where you left off
- **Media3 player** — MP4/HLS playback with subtitle support and D-pad controls
- **Internet Archive** — Search licensed movies from the community library
- **Vietnamese UI** — App interface in Vietnamese for end users

---

## Screenshots

> The app is optimized for TV with a cinematic dark theme (navy background), coral accent, large typography, and clear D-pad focus indicators.

---

## Installation

### From Release (recommended)

1. Download `watch-box-v1.0.0-release.apk` from [Releases](https://github.com/TuyenNedd/watch-box/releases)
2. Copy to USB or install via ADB:
   ```bash
   adb install watch-box-v1.0.0-release.apk
   ```
3. Open **Watch Box** from the TV launcher

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

## Available Movies

| Movie | Source | License |
|-------|--------|---------|
| Big Buck Bunny | Blender Foundation | CC BY 3.0 |
| Sintel | Blender Foundation | CC BY 3.0 |
| Tears of Steel | Blender Foundation | CC BY 3.0 |
| Elephant's Dream | Blender Foundation | CC BY 2.5 |
| + Internet Archive | Community library | Various CC |

---

## Architecture

```
app/src/main/java/dev/watchbox/tv/
├── WatchBoxApplication.kt          # App container (manual DI)
├── MainActivity.kt                 # TV Activity + Compose entry
├── core/
│   ├── model/Movie.kt              # Domain models
│   └── util/TextNormalizer.kt      # Vietnamese search normalization
├── data/
│   ├── catalog/
│   │   ├── CatalogSource.kt        # Provider interface
│   │   ├── CuratedCatalogSource.kt # 4 CC seed movies
│   │   ├── InternetArchiveClient.kt# HTTP + JSON DTOs
│   │   ├── InternetArchiveMapper.kt# Defensive mapping + stream ranking
│   │   └── MovieRepository.kt      # Merge/dedupe/fallback
│   └── local/
│       ├── LibraryStore.kt          # Favorites/progress interface
│       └── PreferencesLibraryStore.kt # SharedPreferences implementation
├── ui/
│   ├── WatchBoxViewModel.kt        # StateFlow state machine
│   ├── WatchBoxApp.kt              # Navigation routes
│   ├── theme/                       # Colors, Typography, Theme
│   ├── components/                  # Hero, Card, Shelf, NavRail, States
│   └── screens/                     # Home, Search, Library, Details
└── player/
    ├── MediaItemFactory.kt          # Build Media3 items (HTTPS only)
    └── PlayerScreen.kt              # Fullscreen player + progress
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

## Content Policy

Watch Box **only** uses openly licensed content:
- Curated movies: Creative Commons from Blender Foundation
- Internet Archive: Only records with an explicit `licenseurl`
- Each movie displays its source and license on the details screen
- No scraping, no pirated sources, no ads

---

## Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feat/your-feature`
3. Commit changes: `git commit -m "feat: add your feature"`
4. Push: `git push origin feat/your-feature`
5. Open a Pull Request

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

## Credits

- [Blender Foundation](https://www.blender.org/) — Open Movies
- [Internet Archive](https://archive.org/) — Community media library
- [AndroidX Media3](https://developer.android.com/media/media3) — Video playback
- [Jetpack Compose for TV](https://developer.android.com/training/tv/playback/compose) — UI framework
