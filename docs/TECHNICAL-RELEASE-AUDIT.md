# AURELION Technical Release Audit

**Updated:** 18 November 2026  
**Repository:** wolfgangachyuta/congenial-fishstick  
**Goal:** A private/direct-distribution Android release that meets modern Android engineering standards. Google Play publication is not planned.

## Verified baseline

The GitHub Actions run for commit 83143cbe767637fd5b340c2ca6a340a96081881a completed successfully. It passed Kotlin/Android source validation, lint, the JVM unit-test task, debug APK assembly, and artifact upload using Android SDK 36.

Subsequent changes to the service, tests, icon resources, and release assembly require their own green CI run before being considered validated.

## Implemented in source

- Android Gradle Plugin 8.13.2, Kotlin 2.4.21, Compose compiler plugin, JDK 17, Gradle Wrapper 8.14.7 with a pinned distribution checksum.
- compileSdk 36, targetSdk 36, minSdk 26, release version name 0.1.0.
- CI triggers for pushes, pull requests, and manual dispatch; debug/release lint, JVM unit tests, debug APK, and release APK/AAB assembly. Optional signing uses repository Actions secrets; signed outputs are verified before upload.
- Declared specialUse foreground service with matching permission, subtype metadata, and typed foreground promotion on Android 14+.
- Defensive overlay permission checks, guarded service startup, service-state reporting, cleanup, and an explicit notification stop action.
- Modern window metrics and system-bar/cutout insets on Android 11+, with a legacy display-metrics fallback.
- Serialized eagle actions and a non-queuing expression path to prevent taps from overwriting speech or flight.
- Sanitized persisted pet stats and pacing settings, including non-finite values.
- Adaptive launcher icon, monochrome icon layer, and dedicated notification icon.
- Explicit backup and device-transfer rules for the three local preference files.
- Removed the battery-exemption permission and direct exemption settings shortcut.
- Manifest declares no Internet permission and cleartext traffic is disabled.

## Remaining validation and release gates

1. Get a clean CI run for the latest service, icons, tests, and release assembly changes.
2. Review lint warnings and R8/minified release build behaviour.
3. Configure the secure release-signing secrets described in docs/RELEASE-SIGNING.md. The signing keystore must be generated and kept outside Git.
4. Install and exercise the app on physical Android 8–16 devices. CI cannot validate overlay coordinates, OEM background policies, permission UX, battery use, or real notification behaviour.
5. Inspect the final APK/AAB manifest and dependency graph, and perform a network-traffic check.
6. Verify install, upgrade, uninstall/reinstall, backup/restore, and release-key update behaviour.

## Distribution constraints

- No Google Play publication is intended.
- Unsigned release artifacts are build outputs only; they are not suitable for direct installation/update distribution.
- Never commit a keystore, passwords, signing secrets, or generated private keys.
- Keep the first public version at 0.1.0 / versionCode 1; increment versionCode for every later installable release.

## Honest status

A successful CI build proves that the source compiles and configured checks pass on GitHub's runner. It does not prove that the overlay works correctly on every Android device. Physical-device QA and signing-key setup remain external release gates.
