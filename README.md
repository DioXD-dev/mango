# 🥭 MangoPlayer

Material You Android music player — built with Jetpack Compose, Media3/ExoPlayer, and synchronized lyrics via LRCLIB.

---

## Features

| Feature | Status |
|---|---|
| Local music library (MediaStore) | ✅ |
| Background playback (Media3 Service) | ✅ |
| Material You dynamic colors (Android 12+) | ✅ |
| Album art blurred background | ✅ |
| Mini player bar above nav | ✅ |
| Synchronized lyrics (LRCLIB) | ✅ |
| Songs / Albums / Artists / Playlists tabs | ✅ |
| Shuffle & repeat modes | ✅ |
| Swipe album art to skip | ✅ |
| Dark theme | ✅ |
| Notification controls | ✅ (via Media3) |

---

## Quick Start

### Option A — Build via GitHub Actions (Recommended)

1. Fork this repo
2. Go to **Actions → Build MangoPlayer APK → Run workflow**
3. Download the APK artifact when done
4. Enable "Install from unknown sources" on your phone and install

### Option B — Build locally (Android Studio)

```bash
# Prerequisites: JDK 17, Android SDK 35
git clone https://github.com/YOUR_USERNAME/MangoPlayer
cd MangoPlayer
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Option C — Build in Termux (on-device)

```bash
pkg install git openjdk-17
git clone https://github.com/YOUR_USERNAME/MangoPlayer
cd MangoPlayer
chmod +x gradlew
./gradlew assembleDebug
```

---

## Project Structure

```
MangoPlayer/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── kotlin/com/example/mangoplayer/
│   │   ├── MainActivity.kt          # Entry point
│   │   ├── MangoApp.kt             # Application class (Coil setup)
│   │   ├── data/
│   │   │   └── MediaScanner.kt     # MediaStore query & grouping
│   │   ├── model/
│   │   │   └── Song.kt             # Data models (Song, Album, Artist…)
│   │   ├── service/
│   │   │   └── AudioService.kt     # Media3 MediaSessionService
│   │   ├── viewmodel/
│   │   │   └── PlayerViewModel.kt  # Central state + MediaController
│   │   └── ui/
│   │       ├── theme/
│   │       │   ├── MangoTheme.kt   # Material You + Palette theming
│   │       │   └── MangoTypography.kt
│   │       ├── navigation/
│   │       │   └── Navigation.kt   # NavHost routes
│   │       ├── components/
│   │       │   └── MiniPlayerBar.kt
│   │       └── screen/
│   │           ├── LibraryScreen.kt
│   │           ├── NowPlayingScreen.kt
│   │           ├── LyricsScreen.kt  # LRCLIB API + synced scroll
│   │           ├── AlbumsScreen.kt
│   │           ├── ArtistsScreen.kt
│   │           ├── PlaylistsScreen.kt
│   │           └── SettingsScreen.kt
│   └── res/
│       └── values/
│           ├── strings.xml
│           └── themes.xml
├── .github/workflows/build.yml      # CI: builds APK automatically
├── build.gradle.kts
├── app/build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Lyrics

Lyrics are fetched automatically from [LRCLIB](https://lrclib.net) (free, no API key needed).
- Synced `.lrc` format with millisecond timestamps
- Auto-scrolls to the active line as the song plays
- Falls back gracefully if lyrics aren't available

---

## Permissions Required

| Permission | Why |
|---|---|
| `READ_MEDIA_AUDIO` (Android 13+) | Scan local music files |
| `READ_EXTERNAL_STORAGE` (Android ≤12) | Scan local music files |
| `FOREGROUND_SERVICE` | Background playback |
| `INTERNET` | Fetch lyrics from LRCLIB |
| `POST_NOTIFICATIONS` | Media notification on Android 13+ |

---

## Customization

- **Accent color**: Material You automatically uses your wallpaper colors on Android 12+. On older devices, edit `buildPaletteColorScheme()` in `MangoTheme.kt`.
- **Crossfade / gapless**: Configurable in Settings screen (wire to ExoPlayer config in `AudioService`).
- **Custom font**: Replace system font in `MangoTypography.kt`.

---

## Contributing

PRs welcome! Planned features:
- [ ] Equalizer (media effects)
- [ ] Last.fm scrobbling
- [ ] Sleep timer
- [ ] Car mode
- [ ] Android Auto support
