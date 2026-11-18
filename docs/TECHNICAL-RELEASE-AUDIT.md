# AURELION Technical Release Audit

**Updated:** 18 November 2026  
**Repository:** wolfgangachyuta/congenial-fishstick  
**Goal:** A private/direct-distribution Android release that meets modern Android engineering standards. Google Play publication is not planned.

## Latest verified CI result

**PASS** — GitHub Actions run [#44332116353](https://github.com/wolfgangachyuta/congenial-fishstick/actions/runs/44332116353), commit `fefd2c43dff72ba35d6e7d91cc9cb8f9de712292`.

The run completed successfully:
- Debug and release lint.
- JVM unit tests.
- Debug APK assembly.
- Minified release APK assembly.
- Release Android App Bundle assembly.
- Debug and unsigned release artifact uploads.

The separate Gradle dependency-submission workflow also completed successfully on the same source revision. Dependabot is configured for weekly Gradle and GitHub Actions updates.

The workflow had no release-signing secrets configured during this run. Therefore the signing-key decode and signature-verification steps were correctly skipped, and the release APK/AAB artifacts are **unsigned**. The debug APK is signed with the standard debug key and is suitable for device testing, not public distribution.

## Implemented

### Toolchain and dependencies
- Android Gradle Plugin 8.13.2, Kotlin 2.4.21, Compose compiler plugin, JDK 17.
- Gradle Wrapper 8.14.7 with a pinned distribution checksum.
- compileSdk 36, targetSdk 36, minSdk 26, versionName 0.1.0.
- AndroidX Core KTX 1.17.0, Activity Compose 1.13.0, Lifecycle 2.10.0, Compose BOM 2026.06.01. This keeps the project on the API 36-compatible Compose 1.11 line; Compose 1.12 requires compileSdk 37 and a newer AGP baseline.
- GitHub Actions pinned to immutable Node 24-compatible commits.

### Runtime and permissions
- Declared specialUse foreground service with matching permission, subtype metadata, and typed foreground promotion on Android 14+.
- Defensive overlay permission checks, guarded service startup, visible startup errors, service-state reporting, cleanup, and notification stop action.
- Modern window metrics and system-bar/cutout insets on Android 11+, with legacy display-metrics fallback.
- Manual sleep suppresses speech and autonomous movement.
- Serialized eagle actions prevent taps from overwriting speech or flight.
- Removed battery-exemption permission and direct exemption shortcut.

### Privacy and maintenance
- No INTERNET permission or network integration in the current app.
- Explicit backup and device-transfer rules for the three local preference files.
- Standalone privacy notice, release-signing guide, security policy, changelog, and development checklist.
- Dependabot updates Gradle and workflow dependencies weekly.
- Gradle dependency graph submission is enabled.

### Build and quality
- JVM tests cover pet-stat bounds, invalid preference values, and action/sleep behaviour.
- Release build uses R8 code shrinking and resource shrinking.
- CI runs debug/release lint, unit tests, debug APK, and release APK/AAB builds.
- CI only runs for app/build/workflow/dependency configuration changes; documentation-only edits do not trigger a full Android build.

## Remaining gates before a distributable 0.1.0 APK

1. **Release signing:** generate and safely back up the release keystore, then configure the four repository Actions secrets documented in `docs/RELEASE-SIGNING.md`. CI will then sign and verify APK/AAB outputs.
2. **Physical-device QA:** install the debug APK and test overlay permission grant/revocation, notification permission, notification stop action, drag/position persistence, cutouts/system bars, rotation, lock/unlock, battery saver, sleep/wake, and several hours of battery behaviour.
3. **Final package inspection:** inspect the signed APK manifest and permissions and confirm there is no unexpected network traffic.
4. **Update test:** install a signed build, then verify that a later build signed with the same keystore updates it without uninstalling.

## Distribution constraints

- No Google Play publication is intended.
- Unsigned release APK/AAB outputs are engineering artifacts, not installable production packages.
- Never commit a keystore, passwords, signing secrets, or generated private keys.
- Keep the initial release at versionName 0.1.0 / versionCode 1; increment versionCode for every later installable release.
- Do not create a public release until the signing and physical-device gates above are complete.

## Honest status

The source, CI, tests, lint, debug build, and minified unsigned release builds are green. A signed release cannot be produced until the repository owner supplies the signing secrets, and device-specific behaviour cannot be certified without physical-device testing.
