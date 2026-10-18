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

## Release signing

Upload / release certificate fingerprints (public). Private keystore stays local — see [`keystore/README.md`](keystore/README.md).

| Algorithm | Fingerprint |
|-----------|-------------|
| SHA-256 | `5A:EE:EE:85:6E:5B:CA:C2:CC:98:72:A1:FD:8C:A8:49:74:A2:9B:8D:30:D7:56:61:24:8F:D9:F4:B6:B9:13:15` |
| SHA-1 | `AC:10:D0:48:04:7F:22:71:C4:21:F4:6D:9F:EF:E7:53:7C:D9:6C:BA` |

Public cert: [`keystore/inkstack-release.cer`](keystore/inkstack-release.cer).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

GPL-3.0. See [LICENSE](LICENSE).
