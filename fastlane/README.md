# Fastlane metadata (Play Store / F-Droid)

Store-facing text lives under `metadata/android/en-US/`. Screenshots are **not** checked in as binaries here; capture them from a device or emulator and place PNG files in the paths below.

## Phone screenshots

Target directory:

```text
metadata/android/en-US/images/phoneScreenshots/
```

Expected filenames (order matters for some publish pipelines):

| File | Suggested content |
|------|-------------------|
| `1_home.png` | Home / continue reading |
| `2_reader.png` | In-reader view (pages, controls) |
| `3_library.png` | Library grid or list |
| `4_explore.png` | Browse / search / sources |
| `5_settings.png` | Settings or about |

### Capture workflow

1. Install a **release** or **debug** build on a phone or AVD with a representative library (covers, progress, at least one source enabled).
2. Use the same **locale** as the metadata folder (`en-US`) and a consistent **theme** (light or dark — pick one set for all shots).
3. Hide sensitive data (account emails, tokens, exact URLs if needed).
4. Capture at the store-recommended aspect ratio (typically **9:16** phone; min long edge often 1080px — check current Play Console guidance).
5. Copy files into `phoneScreenshots/` with the exact names above.

### adb (physical device)

With USB debugging enabled:

```fish
set dest (pwd)/metadata/android/en-US/images/phoneScreenshots
mkdir -p $dest
adb exec-out screencap -p > $dest/1_home.png
# Navigate in the app, then repeat for 2_reader.png … 5_settings.png
```

Rename or re-capture until all five files exist.

### Emulator

Use the emulator toolbar **Screenshot** action, or the same `adb exec-out screencap -p` command against the emulator’s serial (`adb devices`).

### Validate before upload

- Five PNGs present, correct names, readable at thumbnail size.
- `title.txt`, `short_description.txt`, and `full_description.txt` match the app’s current branding and features.
- Changelog file `changelogs/<versionCode>.txt` matches `versionCode` in `app/build.gradle.kts` (currently `2` for 1.0.0). Full notes live in `CHANGELOG.md`.

## F-Droid

F-Droid can consume Fastlane metadata when packaged in the repo or release branch. Ensure screenshot PNGs and changelogs are included in the tag you submit, and that proprietary blobs are not added under `metadata/`.

## Supply / Play Console

You can upload the `en-US` folder with [supply](https://docs.fastlane.tools/actions/supply/) or paste the same text in Play Console. Keep `versionCode` in changelogs aligned with Gradle when shipping updates.
