## Inkstack contribution guidelines

- Prefer fixing or implementing work that already has a clear scope.
- **Performance matters.** Prefer performance over cosmetic refactors when they conflict.
- Avoid drive-by README/metadata churn.
- Avoid new dependencies unless necessary; APK size still matters.

### Manga sources

Sources live under [`parsers/`](parsers/).

- Add parser classes under `parsers/src/main/kotlin/org/inkstack/parsers/site/` and annotate them with `@MangaSourceParser`.
- Prefer extending an existing site engine (Madara, MangaReader, etc.) when the target site uses one.
- Build parsers with `./gradlew :parsers:compileKotlin`.
