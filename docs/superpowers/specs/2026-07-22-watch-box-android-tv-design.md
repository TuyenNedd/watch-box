# Watch Box Android TV — Product Design

**Date:** 2026-07-22  
**Status:** Approved by delegated product decision (the user explicitly requested autonomous decisions without checkpoints)

## Goal

Build a production-shaped Android TV application in Vietnamese that lets people discover and play films that are explicitly free/open-licensed, supports Vietnamese subtitle tracks when available, and remains easy to operate entirely with a TV remote.

## Success criteria

- Installs as a native TV application and appears in the Android TV launcher.
- Every primary action works with D-pad, select, and back; focus is always visible.
- Home, search, movie details, favorites, continue-watching, and playback are functional.
- Playback uses Media3/ExoPlayer and supports HLS/MP4 plus sidecar subtitle tracks.
- Catalog data comes from a curated open-film collection and the Internet Archive public API, restricted to records that declare a license.
- Source and license are visible so the application does not present unknown third-party streams as authorized content.
- Network/loading/empty/error states are understandable in Vietnamese.
- A debug APK can be built from the repository with documented Gradle commands.

## Product and visual direction

Watch Box uses a cinematic dark navy background, warm coral accent, large high-contrast typography, generous spacing, and 16:9 artwork. The home screen starts with a featured hero and then horizontally scrollable shelves. Focused cards scale slightly, gain a coral border, and expose useful metadata. The layout targets a ten-foot viewing distance rather than adapting a phone UI.

The Vietnamese interface uses short labels: “Trang chủ”, “Tìm kiếm”, “Yêu thích”, “Xem tiếp”, “Xem ngay”, and “Thử lại”. Movie badges show year, runtime, source, subtitle language, and license.

## Approaches considered

### 1. Native Android TV + legal open catalog — selected

Kotlin, Compose for TV, Media3, a small repository layer, Internet Archive APIs, and a curated set of Blender Open Movies. This offers correct remote focus, native playback, clear source boundaries, and no backend operating cost.

### 2. WebView wrapper around free streaming sites — rejected

It is faster to prototype but has poor D-pad accessibility, unstable scraping, aggressive advertising, security risks, and no reliable way to establish content rights.

### 3. Native client plus a custom aggregation backend — deferred

A backend could normalize many providers and add accounts, but it creates hosting, moderation, privacy, and maintenance obligations that are unnecessary for the first useful product.

## Legal content policy

The app does not scrape or embed unverified movie sites. Initial featured items are Creative Commons Blender Open Movies hosted through stable public sample mirrors. Search and discovery use Internet Archive records that expose a `licenseurl`; detail loading selects a playable video derivative from that record. Each detail screen displays the source and license. The architecture allows future providers only through the same explicit `CatalogSource` contract and license metadata.

References:
- [Internet Archive API information](https://help.archive.org/help/api-information/)
- [Internet Archive search documentation](https://archive.org/help/aboutsearch.htm)
- [Sintel open-movie downloads and subtitles](https://durian.blender.org/download/)
- [Big Buck Bunny Creative Commons project](https://peach.blender.org/)

## Architecture

A single Android application module uses a small MVVM structure:

- `core/model`: immutable domain objects (`Movie`, `MovieDetails`, `PlaybackSource`).
- `data/catalog`: curated catalog, Internet Archive API client, JSON parsing, and a repository that merges/deduplicates sources.
- `data/local`: favorites and playback progress persisted with `SharedPreferences` behind a testable interface.
- `ui`: one activity, immutable `WatchBoxUiState`, a ViewModel, TV routes, reusable hero/cards/shelves, and a theme.
- `player`: Media3 player screen and progress callbacks.

The repository exposes suspend functions and never leaks transport DTOs to the UI. Dependencies are constructed in a small app container; no dependency-injection framework is needed for this scope.

## Data flow

1. On launch, `WatchBoxViewModel` immediately publishes curated films so the app remains useful while offline.
2. It requests a licensed open-movie page from Internet Archive and merges unique identifiers into additional shelves.
3. Selecting a card navigates by movie ID. Details are resolved from the curated source or Internet Archive metadata.
4. Selecting “Xem ngay” resolves the best TV-friendly MP4/HLS file and opens the player.
5. The player restores saved position, reports progress periodically, and clears progress near completion.
6. Search is debounced in the ViewModel, keeps the last successful result while loading, and ignores stale responses.
7. Favorite IDs and playback positions are persisted locally without accounts or personal data collection.

## Screens and behavior

### Home

A featured hero contains backdrop, title, short Vietnamese synopsis, metadata, and “Xem ngay”/“Chi tiết” actions. Shelves include “Phim mở nổi bật”, “Kho phim cộng đồng”, and “Xem tiếp” when progress exists. The first actionable element receives focus.

### Search

A TV-friendly search field opens the on-screen keyboard. Results use a responsive card grid. Empty query shows suggestions; no results and network failures have dedicated states. Search covers local titles immediately and remote licensed records when the query has at least two characters.

### Details

Backdrop, poster, title, original title, description, metadata chips, source/license, favorite toggle, and watch/resume action. If no compatible stream exists, playback is disabled and the reason is shown.

### Favorites and continue watching

Both are derived from catalog items plus local IDs/progress. Empty states link back to discovery. No login is required.

### Player

Fullscreen Media3 `PlayerView` is optimized for remote controls. Select toggles controls, play/pause keys work, left/right seek, back returns to details, and a Vietnamese subtitle track is attached when the selected movie supplies one. Playback errors show an actionable Vietnamese message rather than leaving a black screen.

## Error handling

- Curated data is always available even when remote APIs fail.
- Repository errors are converted to user-safe Vietnamese messages; raw exceptions are not rendered.
- HTTP calls use connection/read timeouts and cancellation.
- JSON fields are optional and malformed items are skipped, not fatal to the whole page.
- Only HTTPS URLs are accepted for remote media, artwork, and subtitle tracks.
- Archive file selection rejects metadata, thumbnails, torrents, and unsupported containers.

## Testing strategy

Development follows red/green/refactor for behavior-bearing code. JVM tests cover URL validation, Archive JSON mapping, playable-file ranking, catalog merging/deduplication, Vietnamese search normalization, and progress completion rules. Android build/lint plus unit tests verify integration. UI behavior is additionally checked through a launch smoke test when an emulator/device is available; lack of an emulator must be reported rather than hidden.

## Scope boundaries

Included: films, single-video playback, licensed open sources, favorites, progress, search, Vietnamese UI, and subtitle-track support.

Not included: user accounts, downloads/DRM, pirated aggregators, live TV, series/episode management, casting, recommendations based on profiling, comments, ads, subscriptions, or a custom backend.

## Self-review

The design contains no placeholders, all listed screens map to a data path, legal source restrictions are consistent with playback behavior, and the scope is one independently buildable Android TV product.