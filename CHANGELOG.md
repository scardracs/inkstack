# Changelog

## 1.0.1

Patch release after `v1.0.0` CI failed on unit tests (`VersionIdTest` still expected `0.1.0`).

### Fixes

- Align `VersionIdTest` with the shipped version name so `:app:testDebugUnitTest` passes in CI.
- Selection chrome DRY: shared `SelectionChromeController`, menu includes, `CoverWithTitle`, shared empty/error panes, and `confirmMarkAsCompleted`.
- CI: composite action `.github/actions/setup-android` for JDK/SDK/Gradle bootstrap on build and release jobs.

### Notes

- `v1.0.0` never published a GitHub Release (build job failed before the signed release job). Use this tag for the first signed APK.

## 1.0.0

First stable Inkstack release.

### UI

- App shell and screens migrated to Jetpack Compose (lists, details, settings, favourites, suggestions, updates, bookmarks, related manga, secondary hosts).
- Dialogs, bottom sheets, browser, and auth chrome hosted in Compose (WebView via `AndroidView` where needed).
- Frosted glass top/bottom chrome, fast scroller, and adaptive sheets.
- Multi-select uses a Compose glass action bar instead of the system ActionMode overlay; icons use app drawables with readable tint.
- Manga grid titles sit under the cover (theme `onSurface`) so they stay readable in light and dark themes; cover bottom gradient overlay removed.
- Shared filter header on manga lists; pull-to-refresh and overflow menus restored on list screens.
- Main search opens global results from every tab.
- Details: chapters/pages as an inline Compose pane; read actions live in the chapters pane (FAB removed); overflow menus restored.
- Cover badges for reading progress, favourite, pin, and saved.

### Reader

- Webtoon-only reading mode.
- Tap grid, zoom, and chapter edge loading restored.
- Reader chrome wired to webtoon list scroll.
- Chapter, page, and scroll position persisted and restored for webtoon.

### Sources & parsers

- Broken-sources triage probe and smoke harness.
- Working parsers revived; Madara/MangaReader list fallbacks.
- Unreachable sources annotated `@Broken` with triage reasons.
- Redirect-only parser stubs removed.

### Reliability & privacy

- Crash reporting via GitHub Issues (ACRA/Sentry removed); optional consent and token-gated builds.
- Kotlin deprecation/nullability cleanup; obsolete compiler flags dropped; Hilt ViewModel Compose support.

### Build & CI

- Version `1.0.0` (`versionCode` 2); overridable via `-PversionName` / `-PversionCode`.
- CI: debug assemble + unit tests on PRs/main; signed release assemble on `main` when keystore secrets are present.
- CI: pushing a `v*` tag builds a signed release APK and publishes a GitHub Release (signing secrets required).
- GitHub Actions majors updated; CI noise from manifest merge / parsers / Actions silenced where possible.
