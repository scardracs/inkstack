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

- ~155 ALIVE with smoke FAIL (empty list / wrong selectors / 404 endpoints) — fix by template family.
- Remaining bare `@Broken` (~211) mostly CF/ERROR/HTTP_4xx without DNS death — annotate on next pass.
- Full TSV: `parsers/broken-triage.tsv` (gitignored).

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
