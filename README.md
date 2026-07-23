# Watch Box 📺

**Ứng dụng Android TV xem phim miễn phí từ nguồn Creative Commons / Open License**

Watch Box là ứng dụng native Android TV được thiết kế cho TV Box, điều khiển hoàn toàn bằng remote, hiển thị phim có bản quyền mở (Creative Commons) với hỗ trợ Vietsub.

---

## Tính năng

- 🎬 **Khám phá phim** — Trang chủ với hero banner, shelves phim nổi bật
- 🔍 **Tìm kiếm** — Hỗ trợ tiếng Việt không dấu (accent-insensitive)
- 📖 **Chi tiết phim** — Poster, mô tả, metadata, nguồn & license rõ ràng
- ❤️ **Yêu thích** — Lưu phim yêu thích offline
- ▶️ **Xem tiếp** — Tự động lưu tiến trình, xem tiếp từ lần dừng trước
- 🎥 **Trình phát Media3** — Hỗ trợ MP4/HLS, phụ đề, điều khiển D-pad
- 🌐 **Internet Archive** — Tìm kiếm phim có license từ kho cộng đồng
- 🇻🇳 **Giao diện tiếng Việt** — Toàn bộ UI bằng tiếng Việt

---

## Ảnh chụp màn hình

> Ứng dụng được tối ưu cho TV với giao diện cinematic tối (navy), accent coral, typography lớn và focus rõ ràng qua D-pad.

---

## Cài đặt

### Từ Release (khuyến nghị)

1. Tải file `watch-box-v1.0.0-release.apk` từ [Releases](https://github.com/TuyenNedd/watch-box/releases)
2. Copy vào USB hoặc dùng `adb install`:
   ```bash
   adb install watch-box-v1.0.0-release.apk
   ```
3. Mở ứng dụng **Watch Box** từ TV launcher

### Build từ source

**Yêu cầu:** JDK 17, Android SDK (API 35, Build Tools 35.0.0)

```bash
git clone https://github.com/TuyenNedd/watch-box.git
cd watch-box

# Cấu hình SDK path
echo "sdk.dir=/path/to/your/android-sdk" > local.properties

# Build debug APK
export JAVA_HOME=/path/to/jdk-17
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

---

## Phim có sẵn

| Phim | Nguồn | License |
|------|--------|---------|
| Big Buck Bunny | Blender Foundation | CC BY 3.0 |
| Sintel | Blender Foundation | CC BY 3.0 |
| Tears of Steel | Blender Foundation | CC BY 3.0 |
| Elephant's Dream | Blender Foundation | CC BY 2.5 |
| + Internet Archive | Kho cộng đồng | Nhiều loại CC |

---

## Kiến trúc

```
app/src/main/java/dev/watchbox/tv/
├── WatchBoxApplication.kt          # App container (DI thủ công)
├── MainActivity.kt                 # TV Activity + Compose entry
├── core/
│   ├── model/Movie.kt              # Domain models
│   └── util/TextNormalizer.kt      # Vietnamese search normalization
├── data/
│   ├── catalog/
│   │   ├── CatalogSource.kt        # Provider interface
│   │   ├── CuratedCatalogSource.kt # 4 phim CC seed
│   │   ├── InternetArchiveClient.kt# HTTP + JSON DTOs
│   │   ├── InternetArchiveMapper.kt# Defensive mapping + stream ranking
│   │   └── MovieRepository.kt      # Merge/dedupe/fallback
│   └── local/
│       ├── LibraryStore.kt          # Interface favorites/progress
│       └── PreferencesLibraryStore.kt # SharedPreferences impl
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

| Thành phần | Công nghệ |
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

## Chính sách nội dung

Watch Box **chỉ** sử dụng nội dung có bản quyền mở:
- Phim curated: Creative Commons từ Blender Foundation
- Internet Archive: Chỉ records có `licenseurl` rõ ràng
- Mỗi phim hiển thị nguồn và license trên màn hình chi tiết
- Không scrape, không nguồn pirate, không quảng cáo

---

## Đóng góp

1. Fork repository
2. Tạo branch: `git checkout -b feat/your-feature`
3. Commit: `git commit -m "feat: add your feature"`
4. Push: `git push origin feat/your-feature`
5. Mở Pull Request

---

## License

MIT License — xem [LICENSE](LICENSE) để biết chi tiết.

---

## Credits

- [Blender Foundation](https://www.blender.org/) — Open Movies
- [Internet Archive](https://archive.org/) — Community media library
- [AndroidX Media3](https://developer.android.com/media/media3) — Video playback
- [Jetpack Compose for TV](https://developer.android.com/training/tv/playback/compose) — UI framework
