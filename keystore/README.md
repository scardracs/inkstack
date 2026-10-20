# Inkstack release signing

The private key (`inkstack-release.jks`) is **not** committed. Its SHA-256 fingerprint is pinned in
`AppValidator.TRUSTED_CERT_SHA256`.

## Public certificate fingerprints

| Algorithm | Fingerprint |
|-----------|-------------|
| SHA-256 | `5A:EE:EE:85:6E:5B:CA:C2:CC:98:72:A1:FD:8C:A8:49:74:A2:9B:8D:30:D7:56:61:24:8F:D9:F4:B6:B9:13:15` |
| SHA-1 | `AC:10:D0:48:04:7F:22:71:C4:21:F4:6D:9F:EF:E7:53:7C:D9:6C:BA` |

Public cert file: `inkstack-release.cer` (committed).

## Local release builds

1. Place `inkstack-release.jks` in this directory (gitignored).
2. Put keystore passwords in **`local.properties`** at the repo root (gitignored), e.g.:

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
`AppValidator.kt`, and ship a new GitHub Release signed with the new cert.
