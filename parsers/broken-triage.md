# Broken sources triage (2026-10-20)

Automated probe + `getList` smoke against `433` `@Broken` parsers.

## Probe summary

| Probe | Count |
|-------|------:|
| ALIVE | 173 |
| DEAD_DNS | 129 |
| ERROR | 80 |
| CF | 10 |
| HTTP_522 | 9 |
| HTTP_429 | 7 |
| DEAD_HTTP | 6 |
| HTTP_401 | 6 |
| HTTP_403 | 3 |
| HTTP_523 | 2 |
| HTTP_500 | 2 |
| HTTP_525 | 2 |
| HTTP_521 | 1 |
| HTTP_526 | 1 |
| HTTP_456 | 1 |
| HTTP_436 | 1 |

## Smoke (`getList`, ALIVE only)

| Smoke | Count |
|-------|------:|
| SKIP | 260 |
| FAIL | 155 |
| PASS | 17 |
| CF | 1 |

## Revived (un-`@Broken`) — smoke PASS

- `ANIBEL`
- `ANIKIGA`
- `COCOMIC`
- `HENTAIORIGINES`
- `HUNTERSSCAN`
- `INSTAMANHWA`
- `KAIJUNO8`
- `KUROIMANGA`
- `LICHSUBS`
- `MANGADEX`
- `MANGAEFFECT`
- `MANGAGALAXY`
- `MANGAHUB_IO`
- `MANGAYY`
- `ONEPIECEEX`
- `PSUNICORN`
- `RIZZCOMIC`

## Follow-up

- **S1 done (2026-10-20):** all bare `@Broken` annotated with triage-derived reasons (`broken_reasons.tsv` + parser sources). Zero bare left.
- **S2 partial (2026-10-20):** live probes + shared template fallbacks + per-parser domain/selector fixes. Smoke re-run still required to confirm PASS counts.
- Full TSV: `parsers/broken-triage.tsv` (gitignored).

### S2 fixed / tentatively revived (un-`@Broken`, pending smoke)

| Source | Fix |
|--------|-----|
| KLIKMANGA | domain → `klikmanga.org` + `withoutAjax` (HTML list confirmed) |
| MANGASPARK | domain → `sparkmanga.net` + `withoutAjax` |
| MANGARBIC | domain → `mangalik.net` + `withoutAjax` |
| TECNOPROJECTS | `withoutAjax` (ajax was HTTP 405) |
| LEGIONSCANS | HeanCms → MangaReader at `legionscans.com` `listUrl=/wp/manga` |
| UZAYMANGA | `/search` → `/manga` + card selectors (`h3` / `a.group`) |
| ELDERMANGA | same as Uzay (`/manga` + new cards) |

### S2 shared base fixes (help many ALIVE-FAIL without per-parser edits)

- `MadaraParser`: on ajax HTTP 4xx/5xx or empty first unfiltered page → fall back to HTML `/?s=&post_type=wp-manga` listing.
- `MangaReaderParser`: if default `listUrl=/manga` 404s or returns empty on first page → retry `/series`.

### S2 partial (still `@Broken`)

- `NINEMANGA_*` (7): list `ul.direlist` → `div.manga-list`/`manga-item`; details/chapters still ad-gated redesign
- CATHARSISWORLD → `catharsisworld.com` (list still 404)
- SSSSCANLATOR → `yomu.com.br` (still 403)
- PROCHAN → `procomic.pro` (Turnstile / image striper)
- LUFFYMANGA / ATLANTISSCAN → CDN `ww*` aliases in `ConfigKey.Domain`
- DRAKESCANS: domain → `drakecomic.net` but site is SPA (not Themesia) — kept `@Broken`
- KOMIKAV / YUMEKOMIK: `/series` path guess wrong; re-`@Broken`

### S2 skipped (hard / needs RE)

~148 remaining hard ALIVE-FAIL (of original 155): CF/JS “Loading…”, parking/dead redirects, LineWebtoons API 500, MangaEsp/Brakeout engine swaps, Madara empty-list behind CF/timeouts, SpiderScans/Afrodit custom UIs, etc.

Re-run:

```fish
set -x BROKEN_TRIAGE_OUT parsers/broken-triage.tsv
set -x BROKEN_TRIAGE_SMOKE 1
./gradlew :parsers:test --tests '*BrokenSourcesTriage*'
```


## Deleted redirect duplicates (2026-10-20)

Removed parser stubs that only redirected to an already-existing source:

| Removed | Kept target |
|---------|-------------|
| CLOVERMANGA | WEBTOONHATTI |
| GHOSTFANSUB | GRIMELEK |
| GLORYMANGA | MANGAGEZGINI |
| HENTAI3Z | HENTAI20 |
| MANGA18H | HENTAI20 |
| KOMIKINDO_INFO | MANGASUSUKU |
| MANGATALE | IKIRU |
| NEKOPOST | KINGS_MANGA |
| SNOWSCANS | FLIXSCANS |
| VEXMANGA | VORTEXSCANS |
| VOIDSCANS | HIVECOMIC |
| MANGAGALAXY | VORTEXSCANS |
| MANGAEFFECT | MANGAREAD |
| INSTAMANHWA | MANHWADEN |
| IMMORTALUPDATES | MORTALSGROOVE |
| DRAGONTRANSLATION | DRAGONTRANSLATIONORG |
| MANGACUTE / MANGAPUMA / MANGAXYZ | MANGAKIO |
