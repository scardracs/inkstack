# Inkstack

Manga reader for Android 9+ (`minSdk` 28). Built against API 37 (`compileSdk` / `targetSdk`).

## Layout

- `:app` — Android application (`org.inkstack`)
- `:parsers` — manga source parsers (in-tree, `org.inkstack.parsers`)
- `:parsers-ksp` — KSP code generator for parsers

## Requirements

- Android 9 (API 28) or newer to install
- `compileSdk` / `targetSdk` 37
- JDK 27 (toolchain; bytecode target 26)
- Optional: `local.properties` release signing keys (see [`keystore/README.md`](keystore/README.md))

## Build

```fish
./gradlew :app:assembleDebug
./gradlew :parsers:compileKotlin
```

Store metadata / screenshots: [`fastlane/README.md`](fastlane/README.md).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

GPL-3.0. See [LICENSE](LICENSE).
