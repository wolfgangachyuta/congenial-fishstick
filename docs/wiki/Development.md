# Development Guide

## Requirements

- Android Studio with Android SDK Platform 36 and Build Tools 36.0.0.
- JDK 17.
- The checked-in Gradle Wrapper (Gradle 8.14.7).

## Build

Run these commands from the repository root:

- ./gradlew --no-daemon lintDebug testDebugUnitTest
- ./gradlew --no-daemon assembleDebug
- ./gradlew --no-daemon assembleRelease bundleRelease

The release APK and App Bundle are currently unsigned. Do not distribute them as installable production packages until release signing is configured.

## Continuous integration

GitHub Actions runs on pushes and pull requests targeting main, as well as manual dispatch. It performs lint, JVM unit tests, a debug APK build, and unsigned release APK/AAB assembly. Debug and unsigned release outputs are uploaded as separate short-lived artifacts.

## Testing checklist

- [ ] Fresh install and first launch on Android 8.0 (API 26) and Android 16 (API 36).
- [ ] Grant, deny, and revoke overlay permission.
- [ ] Grant and deny notification permission on Android 13+.
- [ ] Start and stop repeatedly; confirm no duplicate overlays or lingering notification.
- [ ] Stop the companion using its notification action.
- [ ] Drag and confirm position is restored after stop/restart.
- [ ] Rotate, resize, use split-screen, and test display cutouts/system bars.
- [ ] Lock/unlock, background/foreground, and test process recreation.
- [ ] Test battery saver and manufacturer-specific background restrictions.
- [ ] Leave the companion running for several hours and inspect battery use.
- [ ] Confirm movement, speech, sleep, wake, feed, pet, and tap actions do not conflict.
- [ ] Verify backup/restore and device transfer.
- [ ] Inspect the packaged APK permissions and capture network traffic to confirm no unexpected data flow.
- [ ] Check accessibility, contrast, and reduced-motion behaviour.

## Contribution style

- Keep changes focused and explain the user-visible result.
- Avoid introducing libraries for trivial features.
- Prefer lifecycle-safe, cancellation-aware code.
- Include testing notes in pull requests.
- Update documentation whenever behaviour, data handling, or permission use changes.
