# Changelog

All notable AURELION changes are recorded here.

## 0.1.0 — First public beta

### Added
- Floating golden eagle companion hosted in an Android overlay.
- Draggable placement with saved position.
- Autonomous movement between screen perches.
- Idle expressions, blinking, speech bubbles, and interactive menu.
- Sleep/wake, feed, and pet interactions with local companion stats.
- Adjustable talkativeness and movement frequency.
- Android 16 / API 36 target with Android 8.0 / API 26 minimum support.
- Foreground-service notification with an explicit stop action.
- Adaptive launcher icon, monochrome icon, and dedicated notification icon.
- Local privacy notice, backup rules, release-signing guide, and automated CI.

### Engineering
- Pinned Gradle Wrapper, Android Gradle Plugin, Kotlin, and JDK toolchain.
- Debug and release lint, JVM unit tests, debug APK, and release APK/AAB CI builds.
- Optional GitHub Actions release signing through repository secrets.
- Release builds use R8 code shrinking and resource shrinking.

### Known release limitations
- The release signing keystore must be created and stored by the repository owner; unsigned CI artifacts are not installable production packages.
- Physical-device testing across Android versions and OEM variants is still required.
- AURELION is experimental; behaviour and battery use may vary by device.
