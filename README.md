# SHS Tube

A premium Android media downloader powered by yt-dlp, built with Jetpack Compose and Material 3.

## Features

### Media Sniffer Engine (1DM-Style)
- Real-time URL interception via WebView's `shouldInterceptRequest`
- Regex-based media detection: `m3u8`, `mpd`, `mp4`, `webm`, `mkv`, `mp3`, `m4a`, `ogg`, `flac`, `wav`, `aac` and more
- Live badge counter on download icon (updates as media is found)
- Bottom sheet listing all detected files with quality/size info

### Browser
- Full-featured WebView browser with back/forward/refresh
- Smart URL bar with search support (auto-converts queries to YouTube search)
- Integrated sniffer — detects every network request in real time

### Powered by yt-dlp
- 1000+ supported platforms
- Best quality download (video + audio merge)
- HLS/DASH stream support
- Audio extraction (MP3, M4A, FLAC, etc.)

### Premium UI
- Deep Obsidian Black theme (#080C14)
- Metallic Blue accents (#0052D4)
- Copper Orange highlights (#F2994A)
- Material 3 components throughout
- Smooth animations, badge counters, bottom sheets

### Settings Hub
- Developer profile with bKash donation card
- Social links (Telegram, Facebook, Email)
- "Copy Number" button for bKash payment

## Architecture

```
com.shsshobuj.shstube/
├── config/
│   └── SecretConfig.kt       # Build-time secrets (no hardcoding)
├── service/
│   ├── UrlSnifferService.kt  # Core media detection engine
│   └── DownloadService.kt    # yt-dlp download manager
├── ui/
│   ├── main/                 # MainActivity, NavHost, ViewModel
│   ├── home/                 # Home screen with quick access
│   ├── browser/              # 1DM-style browser + sniffer UI
│   ├── library/              # Download progress/history
│   ├── settings/             # Developer hub & social links
│   └── theme/                # SHS Tube brand theme
└── SHSTubeApp.kt             # Application class
```

## Secrets Configuration

Secrets are **never hardcoded**. They are injected via `local.properties` at build time:

```properties
DEV_NAME=SHS Shobuj
BKASH_PAYMENT_NUMBER=01310211442
TELEGRAM_CHANNEL_URL=https://t.me/aamoviesofficial
FACEBOOK_PROFILE_URL=https://www.facebook.com/profile.php?id=61580853950299
SUPPORT_EMAIL=jdvijay878@gmail.com
```

The `app/build.gradle` reads these and injects them into `BuildConfig`. The `SecretConfig` object exposes them safely at runtime.

## Developer

**SHS Shobuj** — [Telegram](https://t.me/aamoviesofficial) | [Facebook](https://www.facebook.com/profile.php?id=61580853950299)

Support via bKash: `01310211442`

## Build

```bash
# Clone the repo
git clone https://github.com/hamsazzad/SHS-Tube.git

# Open in Android Studio
# Sync Gradle
# Build > Make Project

# Requirements:
# - Android Studio Iguana or newer
# - JDK 17+
# - minSdk 24 (Android 7.0)
# - targetSdk 34 (Android 14)
```

> **Note:** Do NOT run `gradle assemble` in CI without setting up yt-dlp binaries first.
> The app downloads yt-dlp/FFmpeg binaries on first launch automatically.

## License

Private — All rights reserved by SHS Shobuj.
