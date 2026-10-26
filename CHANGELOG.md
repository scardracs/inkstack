# Changelog

## 1.0.2

Version `1.0.2` / `versionCode` 4. (Retagged: replaces the earlier `v1.0.2` APK.)

### Fixes

- Glass selection / chrome icons: derive foreground from `surfaceContainerHigh` luminance (not theme mode) so Material You light container tints no longer swallow white icons; selection bar uses an opaque fill.
- Selection quick actions: resolve icons via an explicit `R.id`→`R.drawable` map. Reflecting into AppCompat `MenuItemImpl` fails on Android 14+ (empty IconButtons that looked like missing / same-color icons).

### Build

- Enable Gradle parallel, build cache, and configuration-cache; raise daemon heaps for local builds.
- Drop redundant AGP-default `gradle.properties` flags; disable unused View Binding; remove ignored ProGuard `-optimizationpasses`.
- CI: cache Android SDK (`actions/cache@v6`), one Gradle invocation for debug assemble + unit tests, skip `lintVital` on the release job.

### Dependencies

- Checked catalog against current stable Maven / Google Maven releases (Compose BOM `2026.09.00`, AGP `9.4.1`, Kotlin `2.4.21`, Room `2.8.5`, etc. already current).
- Bump OkHttp `5.5.0` → `5.5.1`.
- Bump AndroidX WebKit `1.17.1` → `1.18.0`.

## 1.0.1

Patch after `v1.0.0` never shipped: CI failed on `VersionIdTest` (still expected `0.1.0`). Version `1.0.1` / `versionCode` 3.

### Distribution & docs

- Drop Play Store / F-Droid packaging surfaces: remove `fastlane/`, `store/`, and F-Droid `metadata/` (screenshots and store copy).
- Keep the app icon at `artwork/icon.png` for Discord RPC; point `app_icon_url` at that path on `main`.
- Document GitHub-only signed releases in README (link to `CHANGELOG.md` and latest release).
- Keystore docs: rotate via a new GitHub Release, not Play App Signing.

### Weblate / i18n

- Remove Weblate config (`.weblate`) and unused `about_app_translation*` preference strings from all locales.
- Fix corrupted Portuguese `reader_navigation_inverted*` strings that had Weblate URLs as values.
- Translations stay in-repo under `app/src/main/res/values-*/` only.

### Code consolidation (selection / lists / CI)

- Shared `SelectionChromeController` for manga lists and bookmarks multi-select.
- Shared `CoverWithTitle`, `EmptyStatePane` / `ErrorStatePane`, and `confirmMarkAsCompleted` (History / Favourites).
- Shared menu includes (`include_action_share` / `save` / `select_all`) used by `mode_*.xml`.
- Move menu→chrome action mapping to `MenuChromeActions`.
- CI: composite action `.github/actions/setup-android` for JDK / Android SDK / Gradle bootstrap on build and release jobs.
- Ignore local Cursor skills under `.cursor/skills/` in `.gitignore`.

### Fixes

- Align `VersionIdTest` with the shipped version so `:app:testDebugUnitTest` passes and tagged releases can publish.

### Notes

- `v1.0.0` produced no GitHub Release (unit tests failed before the signed release job). Prefer this tag for the first signed APK.

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
