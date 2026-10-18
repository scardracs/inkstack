# Store listing checklist (IT / EN)

Use with `fastlane/metadata/android/en-US/` and Play Console / F-Droid submission.

## Contenuto / Content

| Item | EN | IT (optional Play translation) |
|------|----|--------------------------------|
| Title | Inkstack | Inkstack |
| Short description | See `short_description.txt` | Reader manga offline, sorgenti, sync, scrobbling, UI Material Expressive |
| Full description | See `full_description.txt` | Tradurre bullet: offline, parser/sorgenti, backup/sync, scrobbling, tema, privacy |
| Changelog | `changelogs/1.txt` (versionCode 1 → 0.1.0) | Stesso contenuto o riassunto in italiano |

## Asset grafici / Graphics

- [ ] Icona app 512×512 (Play) — launcher/adaptive icon del progetto
- [ ] Feature graphic 1024×500 (Play) — **non** in fastlane; preparare separatamente se serve
- [ ] Screenshot telefono: `1_home.png` … `5_settings.png` (vedi `fastlane/README.md`)
- [ ] Nessun dato personale visibile negli screenshot

## Build e policy

- [ ] `versionName` 0.1.0 / `versionCode` 1 allineati a `changelogs/1.txt`
- [ ] Target SDK / min SDK documentati (Android 16+)
- [ ] Licenza GPL-3.0 indicata in store / F-Droid metadata
- [ ] Data safety / privacy form (Play): account scrobbling opzionali, backup locale/remoto secondo implementazione
- [ ] F-Droid: build riproducibile, nessun SDK non libero non dichiarato

## Pre-upload rapido

1. Riempire `phoneScreenshots/` con cinque PNG.
2. Rileggere `full_description.txt` rispetto alle funzioni reali della build.
3. Eseguire `./gradlew :app:assembleRelease` (o flavor pubblicato) e testare su dispositivo.
4. Caricare metadata + APK/AAB.
