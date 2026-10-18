# Inkstack

Manga reader for Android 16+.

## Layout

- `:app` — Android application (`org.inkstack`)
- `:parsers` — manga source parsers (in-tree)
- `:parsers-ksp` — KSP code generator for parsers

## Requirements

- Android 16 (API 36) or newer
- JDK 17

## Build

```fish
./gradlew :app:assembleDebug
```

GPL-3.0. See [LICENSE](LICENSE).
