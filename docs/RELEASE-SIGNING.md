# AURELION Release Signing

AURELION is distributed directly as an Android APK; it is not being prepared for Google Play publication. Every installable release APK must nevertheless be signed.

## Why the signing key matters

Android accepts an update only when it is signed with the same signing identity as the installed app. Keep one release keystore for the lifetime of this app, store secure offline backups, and never commit it to Git or send it in chat.

If the keystore is lost, users generally cannot install a normal update over the existing app. They may have to uninstall first, which can remove local app data.

## Generate a release keystore

On a trusted computer with JDK 17 installed, run:

    keytool -genkeypair -v -keystore aurelion-release.jks -alias aurelion -keyalg RSA -keysize 2048 -validity 10000

Choose strong, unique passwords. Keep the keystore and passwords in a secure password manager and at least one encrypted offline backup.

## Add GitHub Actions secrets

In the repository, open Settings, then Secrets and variables, then Actions. Create these repository secrets:

- RELEASE_KEYSTORE_BASE64 — the keystore file encoded as one base64 string.
- RELEASE_KEYSTORE_PASSWORD — the keystore password.
- RELEASE_KEY_ALIAS — aurelion (or the alias chosen when generating the key).
- RELEASE_KEY_PASSWORD — the key password.

On Linux, encode the keystore with:

    base64 -w 0 aurelion-release.jks

On macOS:

    base64 < aurelion-release.jks | tr -d '\n'

Paste the encoded output into RELEASE_KEYSTORE_BASE64. Do not paste the keystore or passwords into a source file, issue, commit, or chat.

## What CI does

When all four secrets are present, GitHub Actions decodes the keystore into the runner's temporary directory, signs the release APK and AAB, verifies both signatures, and uploads the signed outputs as the aurelion-signed-release artifact.

If the secrets are absent, CI still builds unsigned release outputs for engineering checks. Those unsigned files are not suitable for installation or distribution.

The workflow does not publish a GitHub Release automatically. Publishing or sharing a release remains a deliberate human decision.

## Versioning

The initial release is versionName 0.1.0 and versionCode 1. Increase versionCode for every subsequent installable release. Never change the signing key between versions.
