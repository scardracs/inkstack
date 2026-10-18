# Inkstack release signing

The private key (`inkstack-release.jks`) is **not** committed. Its SHA-256 fingerprint is pinned in
`AppValidator.TRUSTED_CERT_SHA256`.

## Local release builds

1. Place `inkstack-release.jks` in this directory (gitignored).
2. Put store/key passwords in **`local.properties`** at the repo root (gitignored), e.g.:

```properties
RELEASE_STORE_FILE=keystore/inkstack-release.jks
RELEASE_STORE_PASSWORD=…
RELEASE_KEY_ALIAS=inkstack
RELEASE_KEY_PASSWORD=…
```

3. `./gradlew :app:assembleRelease`

## CI

Set repository secrets:

- `RELEASE_STORE_FILE_BASE64` — `base64 -w0 keystore/inkstack-release.jks`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS` (default `inkstack`)
- `RELEASE_KEY_PASSWORD`

The GitHub Actions workflow decodes the keystore before `assembleRelease`.

## Rotate

If the key is lost or compromised, generate a new keystore, update `TRUSTED_CERT_SHA256` in
`AppValidator.kt`, and ship an update signed with the new cert (Play App Signing: upload key only).
