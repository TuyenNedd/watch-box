# Watch Box Android TV Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver a buildable native Android TV app that discovers and plays explicitly licensed free films with a Vietnamese ten-foot UI, remote navigation, favorites, progress, search, and subtitle support.

**Architecture:** One Kotlin Android app module uses Compose for TV and Media3. Domain models and source contracts isolate a curated open-film source from an Internet Archive HTTP source; a repository merges them, while a local preferences gateway owns favorites/progress and a ViewModel exposes immutable screen state.

**Tech Stack:** Kotlin 2.1.x, Android Gradle Plugin 8.9.x, Gradle 8.11.x, Compose BOM, Compose for TV Material, Navigation Compose, Lifecycle ViewModel, OkHttp, kotlinx.serialization, Coil, Media3 ExoPlayer, JUnit 5/4-compatible JVM tests, kotlinx-coroutines-test.

---

## File structure

```text
app/src/main/java/dev/watchbox/tv/
  WatchBoxApplication.kt                 app container
  MainActivity.kt                        TV activity and root composition
  core/model/Movie.kt                    domain models and playback source
  core/util/TextNormalizer.kt            accent-insensitive Vietnamese matching
  data/catalog/CatalogSource.kt          provider boundary
  data/catalog/CuratedCatalogSource.kt   legal seed catalog
  data/catalog/InternetArchiveClient.kt  HTTP and serialization DTOs
  data/catalog/InternetArchiveMapper.kt  defensive mapping and stream ranking
  data/catalog/MovieRepository.kt        merge/search/detail orchestration
  data/local/LibraryStore.kt             favorites/progress contract
  data/local/PreferencesLibraryStore.kt  SharedPreferences persistence
  ui/WatchBoxViewModel.kt                immutable app state and events
  ui/WatchBoxApp.kt                      routes and navigation
  ui/components/                         hero, cards, shelves, states, nav rail
  ui/screens/                            home, search, library, details
  ui/theme/                              TV colors, typography, theme
  player/PlayerScreen.kt                 Media3 lifecycle and progress
app/src/test/java/dev/watchbox/tv/        behavior-focused JVM tests
```

### Task 1: Project shell and TV manifest

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`, `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/{strings,colors,themes}.xml`
- Create: `app/src/main/res/drawable/{app_icon,tv_banner}.xml`
- Create: `.gitignore`

- [ ] **Step 1: Configure the build**

Use repositories `google()` and `mavenCentral()`, Java/Kotlin 17, `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`, Compose, BuildConfig disabled, release shrinking, and dependencies for TV Material, navigation, lifecycle, coroutines, serialization, OkHttp, Coil, and Media3.

- [ ] **Step 2: Add the TV application declaration**

Declare internet/network-state permissions; `android.software.leanback` required, touchscreen not required, landscape/noHistory false, `LEANBACK_LAUNCHER`, app banner, backup disabled, and cleartext disabled.

- [ ] **Step 3: Generate and verify Gradle wrapper**

Run: `gradle wrapper --gradle-version 8.11.1` then `./gradlew tasks --all`  
Expected: Gradle resolves the Android plugin and lists app build tasks.

- [ ] **Step 4: Commit**

Commit message: `build: scaffold native Android TV project`

### Task 2: Domain model and pure utilities (TDD)

**Files:**
- Test: `app/src/test/java/dev/watchbox/tv/core/util/TextNormalizerTest.kt`
- Test: `app/src/test/java/dev/watchbox/tv/core/model/PlaybackProgressTest.kt`
- Create: `app/src/main/java/dev/watchbox/tv/core/model/Movie.kt`
- Create: `app/src/main/java/dev/watchbox/tv/core/util/TextNormalizer.kt`

- [ ] **Step 1: Write failing normalization tests**

Cover `"Điện Ảnh Việt" -> "dien anh viet"`, lowercase/trim/collapsed whitespace, and matching an original title when the Vietnamese title differs.

- [ ] **Step 2: Run RED**

Run: `./gradlew testDebugUnitTest --tests '*TextNormalizerTest'`  
Expected: compilation failure because `normalizeForSearch` does not exist.

- [ ] **Step 3: Implement minimal domain API**

Create immutable `Movie`, `MovieDetails`, `PlaybackSource`, `SubtitleTrack`, `LicenseInfo`, and `PlaybackProgress`. Enforce HTTPS in `PlaybackSource.isPlayable`; implement `PlaybackProgress.shouldClear(durationMs)` as duration known and either at least 95% watched or fewer than 60 seconds remain.

- [ ] **Step 4: Implement normalization and verify GREEN**

Use `java.text.Normalizer`, remove combining marks, map `đ/Đ`, lowercase with `Locale.ROOT`, strip punctuation, and collapse whitespace. Run all Task 2 tests and expect PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: add movie domain and Vietnamese search utilities`

### Task 3: Licensed sources and Archive mapping (TDD)

**Files:**
- Test: `app/src/test/java/dev/watchbox/tv/data/catalog/InternetArchiveMapperTest.kt`
- Test: `app/src/test/java/dev/watchbox/tv/data/catalog/CuratedCatalogSourceTest.kt`
- Create: `app/src/main/java/dev/watchbox/tv/data/catalog/CatalogSource.kt`
- Create: `app/src/main/java/dev/watchbox/tv/data/catalog/CuratedCatalogSource.kt`
- Create: `app/src/main/java/dev/watchbox/tv/data/catalog/InternetArchiveClient.kt`
- Create: `app/src/main/java/dev/watchbox/tv/data/catalog/InternetArchiveMapper.kt`

- [ ] **Step 1: Write failing mapper tests**

Fixture an Archive search document and metadata file list. Assert licensed records map, unlicensed/malformed records are skipped, HTTPS artwork is generated, H.264 MP4 outranks OGV/torrent/thumbnails, URL path segments are encoded, and unsupported/no-video metadata returns no playable source.

- [ ] **Step 2: Run RED**

Run: `./gradlew testDebugUnitTest --tests '*InternetArchiveMapperTest'`  
Expected: failure because mapper types do not exist.

- [ ] **Step 3: Implement source contract and curated provider**

`CatalogSource` exposes `featured()`, `search(query)`, and `details(id)`. Seed Big Buck Bunny, Sintel, Tears of Steel, and Elephant's Dream with stable HTTPS sample MP4s, Vietnamese descriptions, Creative Commons source/license links, and subtitle metadata only when a verified track exists.

- [ ] **Step 4: Implement Archive transport and mapper**

OkHttp requests advanced search with `mediatype:movies`, `licenseurl:*`, and the open-source movies collection. kotlinx.serialization DTOs use defaults and ignore unknown keys. Metadata resolution selects a compatible `.mp4`, `.m4v`, `.webm`, or HLS file under a documented ranking and builds `https://archive.org/download/{identifier}/{encodedName}`.

- [ ] **Step 5: Run GREEN and full tests**

Run: `./gradlew testDebugUnitTest`  
Expected: all tests PASS with no uncaught parsing errors.

- [ ] **Step 6: Commit**

Commit message: `feat: add licensed open movie catalog sources`

### Task 4: Repository and local library (TDD)

**Files:**
- Test: `app/src/test/java/dev/watchbox/tv/data/catalog/MovieRepositoryTest.kt`
- Test: `app/src/test/java/dev/watchbox/tv/data/local/LibraryStateTest.kt`
- Create: `app/src/main/java/dev/watchbox/tv/data/catalog/MovieRepository.kt`
- Create: `app/src/main/java/dev/watchbox/tv/data/local/LibraryStore.kt`
- Create: `app/src/main/java/dev/watchbox/tv/data/local/PreferencesLibraryStore.kt`

- [ ] **Step 1: Write failing repository tests**

Use in-memory fake sources. Assert local featured data is returned when remote fails, duplicate IDs are removed in stable order, local search is immediate and accent-insensitive, remote errors do not erase local hits, and detail lookup falls through sources.

- [ ] **Step 2: Write failing library-state tests**

Assert favorite toggling is idempotent and progress is removed at completion while useful progress is retained.

- [ ] **Step 3: Run RED**

Run: `./gradlew testDebugUnitTest --tests '*MovieRepositoryTest' --tests '*LibraryStateTest'`  
Expected: failures because repository/store behavior is absent.

- [ ] **Step 4: Implement and run GREEN**

Implement source merge with per-source failure isolation. Store favorites as a string set and progress as a JSON map; filter invalid negative values and perform synchronous in-process state updates followed by `apply()` persistence. Run the complete unit suite and expect PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: add resilient repository and local watch library`

### Task 5: App state and navigation

**Files:**
- Test: `app/src/test/java/dev/watchbox/tv/ui/WatchBoxViewModelTest.kt`
- Create: `app/src/main/java/dev/watchbox/tv/WatchBoxApplication.kt`
- Create: `app/src/main/java/dev/watchbox/tv/ui/WatchBoxViewModel.kt`
- Create: `app/src/main/java/dev/watchbox/tv/ui/WatchBoxApp.kt`
- Create: `app/src/main/java/dev/watchbox/tv/MainActivity.kt`

- [ ] **Step 1: Write failing ViewModel tests**

Assert launch exposes curated content before remote completion, retry clears an error, two-character search triggers remote lookup, stale search responses are discarded, favorite state updates immediately, and resume position is surfaced with selected details.

- [ ] **Step 2: Run RED**

Run: `./gradlew testDebugUnitTest --tests '*WatchBoxViewModelTest'`  
Expected: failure because app state/events do not exist.

- [ ] **Step 3: Implement app container and state machine**

Construct clients/repository/store once in `WatchBoxApplication`. Expose `StateFlow<WatchBoxUiState>`, cancellable load/search jobs, selected details, route-safe IDs, favorites, progress, and user-safe Vietnamese errors.

- [ ] **Step 4: Add routes and run GREEN**

Routes: `home`, `search`, `favorites`, `continue`, `details/{id}`, `player/{id}`. Pass IDs, never serialized objects. Run all unit tests and expect PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: add Watch Box state and TV navigation`

### Task 6: Ten-foot Compose UI

**Files:**
- Create: `app/src/main/java/dev/watchbox/tv/ui/theme/{Color,Type,Theme}.kt`
- Create: `app/src/main/java/dev/watchbox/tv/ui/components/{FeaturedHero,MovieCard,MovieShelf,NavigationRail,UiStates}.kt`
- Create: `app/src/main/java/dev/watchbox/tv/ui/screens/{HomeScreen,SearchScreen,LibraryScreen,DetailsScreen}.kt`

- [ ] **Step 1: Implement theme and focus contract**

Use navy surfaces, coral focus/accent, white/high-contrast text, 48dp minimum targets, visible border + scale focus treatment, reduced-motion-safe transitions, and semantic content descriptions.

- [ ] **Step 2: Implement reusable components**

Create a 16:9 card with Coil fallback, hero gradient, horizontally scrolling shelves with stable keys, side navigation, loading skeleton, offline banner, empty state, and retry state. Preserve focus when shelf content updates.

- [ ] **Step 3: Implement screens**

Home presents hero/shelves, Search presents a focusable field and results grid, Library supports favorites/continue modes, and Details presents actions, metadata, source, license, and playback availability. All visible copy is in `strings.xml`.

- [ ] **Step 4: Compile and lint**

Run: `./gradlew :app:compileDebugKotlin lintDebug`  
Expected: BUILD SUCCESSFUL with no lint errors.

- [ ] **Step 5: Commit**

Commit message: `feat: build remote-first cinematic TV interface`

### Task 7: Media3 player and progress

**Files:**
- Test: `app/src/test/java/dev/watchbox/tv/player/MediaItemFactoryTest.kt`
- Create: `app/src/main/java/dev/watchbox/tv/player/MediaItemFactory.kt`
- Create: `app/src/main/java/dev/watchbox/tv/player/PlayerScreen.kt`

- [ ] **Step 1: Write failing media item tests**

Assert a playable HTTPS source creates a MediaItem, Vietnamese SRT/VTT tracks receive language `vi`, MIME is inferred safely, and invalid/non-HTTPS source is rejected.

- [ ] **Step 2: Run RED**

Run: `./gradlew testDebugUnitTest --tests '*MediaItemFactoryTest'`  
Expected: failure because `MediaItemFactory` is absent.

- [ ] **Step 3: Implement factory and player**

Create Media3 `MediaItem` with subtitle configurations. Build ExoPlayer with a TV `PlayerView`, restore position, persist progress every 10 seconds and on lifecycle stop, clear near completion, keep screen on during playback, release exactly once, and render Vietnamese loading/error/back controls.

- [ ] **Step 4: Run GREEN**

Run: `./gradlew testDebugUnitTest :app:compileDebugKotlin`  
Expected: all tests PASS and player compiles.

- [ ] **Step 5: Commit**

Commit message: `feat: add Media3 playback and resume progress`

### Task 8: Product verification and delivery

**Files:**
- Modify only files implicated by verification findings.

- [ ] **Step 1: Run full quality gate**

Run: `./gradlew clean testDebugUnitTest lintDebug assembleDebug --stacktrace`  
Expected: BUILD SUCCESSFUL, zero failed tests, zero lint errors, and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 2: Inspect package and manifest**

Use `apkanalyzer manifest print app/build/outputs/apk/debug/app-debug.apk` (or `aapt2 dump badging`) and verify package `dev.watchbox.tv`, LEANBACK launcher, banner, min SDK 26, target SDK 35, no touchscreen requirement, and cleartext disabled.

- [ ] **Step 3: Review requirements and diff**

Re-read the design, map each success criterion to code/evidence, run `git diff --check`, inspect `git status`, and dispatch independent spec then code-quality review. Fix every critical/important finding and repeat verification.

- [ ] **Step 4: Commit fixes**

Commit message: `chore: verify and polish Android TV release`

- [ ] **Step 5: Push and open PR**

Push `feat/android-tv-watch-box` with the GitHub integration and create a PR targeting `main`, including implemented features, exact commands/tests, APK path, legal-source limitation, and any environment limitation.

## Plan self-review

All design success criteria map to Tasks 1–8. Paths and public type names are consistent, no deferred placeholders remain, behavior-bearing work has an explicit failing-test step before implementation, and configuration/generated shell work is isolated from TDD tasks.