# Watch Box

**Cross-platform movie streaming app — Android, TV, and Web**

Watch Box aggregates movies from PhimAPI and OPhim with Vietnamese subtitles and dubbing. Built with Kotlin Multiplatform and Compose Multiplatform, it runs natively on Android phones, TV Boxes, and web browsers from a single codebase.

---

## Platforms

| Platform | How to Run | UI |
|----------|------------|-----|
| Android Phone/Tablet | Install APK, open from home screen | Touch, scroll, tap |
| Android TV / TV Box | Install APK, open from TV launcher | D-pad, remote |
| Web Browser | Open deployed URL or run locally | Mouse, keyboard |

---

## Features

- **New movies daily** — Latest releases with Vietsub, Thuyet Minh, Long Tieng
- **Multi-source fallback** — PhimAPI (primary) + OPhim (backup)
- **Smart search** — Vietnamese accent-insensitive
- **Series support** — Episode list with individual stream links
- **Favorites & Continue Watching** — Persisted locally (no account needed)
- **HLS streaming** — Media3 ExoPlayer on Android, HTML5 + HLS.js on Web
- **Responsive UI** — Material 3, works with touch, mouse, and D-pad
- **Vietnamese localization** — Auto-detected by device/browser language

---

## Installation

### Android (phone + TV)

Download `watch-box-v1.0.1.apk` from [Releases](https://github.com/TuyenNedd/watch-box/releases) and install:

```bash
adb install watch-box-v1.0.1.apk
```

### Web (local development)

```bash
git clone https://github.com/TuyenNedd/watch-box.git
cd watch-box
export JAVA_HOME=/path/to/jdk-17
./gradlew :webApp:wasmJsBrowserDevelopmentRun
# Opens at http://localhost:8080
```

### Build from source

```bash
# Android APK
./gradlew :androidApp:assembleDebug
# Output: androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Web distribution
./gradlew :webApp:wasmJsBrowserDistribution
# Output: webApp/build/dist/wasmJs/productionExecutable/
```

---

## Architecture

```
watch-box/
├── shared/                        # Kotlin Multiplatform shared module
│   └── src/
│       ├── commonMain/            # 90%+ of the code lives here
│       │   ├── core/model/        # Movie, Episode, PlaybackSource
│       │   ├── core/util/         # Vietnamese text normalization
│       │   ├── data/catalog/      # PhimApiClient, OPhimClient, Repository (Ktor)
│       │   ├── data/local/        # LibraryStore interface
│       │   ├── ui/                # Screens, Components, Theme, ViewModel
│       │   └── player/            # PlatformPlayerView (expect)
│       ├── androidMain/           # Media3 ExoPlayer, SharedPreferences
│       └── wasmJsMain/            # HTML5 video + HLS.js, localStorage
├── androidApp/                    # Android entry (phone + TV launcher)
└── webApp/                        # Web entry (Kotlin/Wasm → browser)
```

### Platform-specific code (expect/actual):

| Concern | Android | Web |
|---------|---------|-----|
| HTTP | Ktor CIO engine | Ktor Js engine |
| Video player | Media3 ExoPlayer | HTML5 `<video>` + HLS.js |
| Local storage | SharedPreferences | localStorage |
| Image loading | Coil 3 (Android) | Coil 3 (Wasm) |

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.1.20 |
| UI | Compose Multiplatform 1.7.3 (Material 3) |
| Player (Android) | AndroidX Media3 ExoPlayer 1.6.0 |
| Player (Web) | HTML5 video + HLS.js |
| HTTP | Ktor Client 3.1.1 |
| Serialization | kotlinx-serialization 1.8.0 |
| Images | Coil 3.1.0 |
| Build | Gradle 8.11.1 + AGP 8.9.2 |
| Android Min SDK | 26 (Android 8.0) |
| Web Target | Kotlin/Wasm (wasmJs) |

---

## Movie Sources

| # | Source | Role | Stream Server |
|---|--------|------|---------------|
| 1 | [PhimAPI](https://phimapi.com) | Primary | kkphimplayer |
| 2 | [OPhim](https://ophim1.com) | Backup | opstream |

---

## Development

### Prerequisites

- JDK 17
- Android SDK (API 35, Build Tools 35.0.0)
- Node.js (for Kotlin/Wasm dev server)

### Common commands

```bash
export JAVA_HOME=/path/to/jdk-17

# Run web dev server (hot reload)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Build Android debug APK
./gradlew :androidApp:assembleDebug

# Build web production bundle
./gradlew :webApp:wasmJsBrowserDistribution

# Run tests
./gradlew :shared:allTests
```

### Deploy web to GitHub Pages

```bash
./gradlew :webApp:wasmJsBrowserDistribution
# Upload webApp/build/dist/wasmJs/productionExecutable/ to your hosting
```

---

## Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feat/your-feature`
3. Commit: `git commit -m "feat: add your feature"`
4. Push: `git push origin feat/your-feature`
5. Open a Pull Request

### Adding a new movie source

Implement the `CatalogSource` interface in `shared/src/commonMain/`:

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

- [PhimAPI](https://phimapi.com) — Movie metadata & HLS streams
- [OPhim](https://ophim1.com) — Backup movie source
- [JetBrains](https://www.jetbrains.com/) — Kotlin Multiplatform & Compose Multiplatform
- [AndroidX Media3](https://developer.android.com/media/media3) — Android video playback
- [HLS.js](https://github.com/video-dev/hls.js/) — Web HLS playback
