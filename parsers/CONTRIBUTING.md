# Adding a manga source

Add parser classes under `src/main/kotlin/org/inkstack/parsers/site/` and annotate them with `@MangaSourceParser`.

Prefer extending an existing site engine (Madara, MangaReader, etc.) when the target site uses one.
