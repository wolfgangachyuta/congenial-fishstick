# AURELION Technical Release Audit

**Audit date:** 18 November 2026  
**Audited revision:** `011b7428bf526afaa7b4395f3f49f66d07c41304`  
**Scope:** Repository build configuration, Android manifest, app entry point, overlay service, lifecycle owner, local stores, animation controller, and privacy documentation.

This is a source-level audit. Findings marked for device validation have not been confirmed on physical hardware. No claim of a successful release build is made by this document.

## Executive assessment

AURELION has a coherent early architecture: a Kotlin/Compose activity, a dedicated overlay service, local preference stores, and a controller for animation and dialogue. It is not yet ready for a release-quality Android 16 / API 36 build.

The primary blockers are the outdated Android Gradle Plugin/toolchain, the absence of a Gradle Wrapper, and the foreground-service declaration required before targeting API 34 or later. Permission and service lifecycle handling also need hardening. The repository currently has no automated test source set visible in the audited tree and no launcher icon resources.

## Findings

### P0 — Resolve before raising target SDK

1. **Foreground service has no declared type.**  
   `OverlayService` calls `startForeground()` and remains active to host a persistent user-visible overlay, but its manifest declaration has no `android:foregroundServiceType`. Apps targeting API 34+ must declare an appropriate type and the matching permission. The correct type must be selected from the actual use case; `specialUse` must not be added as a guess. If it is the only defensible category, its subtype declaration and runtime behavior must be reviewed and tested.

2. **The service startup path needs a compatibility review.**  
   The service immediately enters foreground state in `onCreate()`, then registers a receiver, creates a Compose overlay and starts several long-lived coroutines. We must verify behavior across Android 8–16, including notification permission denial, overlay permission revocation, process recreation, and system restrictions on foreground-service starts.

### P1 — Build and reproducibility

3. **Android Gradle Plugin is too old for API 36.**  
   The project uses AGP 8.2.2. Android's current compatibility guidance lists AGP 8.9.1 as the minimum for API 36. Upgrade AGP and its compatible Gradle version together, then validate Kotlin/Compose compatibility rather than changing one version in isolation.

4. **No Gradle Wrapper is present in the repository tree.**  
   There is no `gradlew`, `gradlew.bat`, or `gradle/wrapper/gradle-wrapper.properties`. CI currently installs a globally selected Gradle 8.2. Add and commit the official wrapper so local and CI builds use the same pinned Gradle distribution.

5. **CI only builds a debug APK.**  
   The current workflow is manual-only and runs `assembleDebug`. It does not run unit tests, lint, release assembly, or dependency verification. A release pipeline should be introduced only after signing is configured securely; secrets must never be committed.

### P1 — Service and permission reliability

6. **Overlay permission is checked in the Activity, not defensively in the service.**  
   The Activity checks `Settings.canDrawOverlays()` before starting the service, but the service itself does not guard `WindowManager.addView()`. Permission can change between the check and view attachment, or while the service is running. Add a clear failure path and cleanup so a revoked permission does not leave a crashing or partially initialized service.

7. **The notification-permission callback ignores the result.**  
   The Activity starts the service whether the user grants or denies `POST_NOTIFICATIONS`. Android permits foreground services in this situation, but the user experience and disclosure should be intentional. The UI should explain the consequence and reflect the actual running state rather than assuming startup succeeded.

8. **The Activity's running state is only local Compose state.**  
   `companionRunning` starts as false and is set optimistically after requesting service startup. It is not reconciled when the Activity returns from Settings, when the service is stopped by the system, or when the overlay is hidden. Replace this with a reliable service-state signal or a lifecycle-aware status query.

9. **Battery-optimization exemption is requested directly from Settings.**  
   The settings screen launches the system exemption request without first explaining the benefit, checking whether an exemption is already active, or offering a clear alternative. AURELION should first be made battery-conscious under normal system limits. Retain a special exemption request only if device testing demonstrates that it is genuinely necessary.

### P1 — Runtime behavior and lifecycle

10. **Service initialization and overlay attachment are not guarded as one transaction.**  
    If receiver registration, Compose setup, or `addView()` fails partway through `onCreate()`, the service can be left with partially initialized resources. Startup should be made exception-safe, with deterministic cleanup and a user-visible failure state.

11. **Overlay bounds use deprecated display metrics APIs.**  
    `refreshBounds()` uses `defaultDisplay.getMetrics()`. Modern Android devices have window insets, cutouts, multi-window and changing display configurations. Bounds need to be based on current window/display metrics and tested in portrait, landscape, split-screen and on devices with cutouts.

12. **The overlay lifecycle owner does not clear its ViewModelStore.**  
    The custom lifecycle owner destroys its lifecycle but never clears its ViewModelStore. No ViewModels are currently evident in the overlay, so this is not necessarily a present leak, but cleanup should be completed as part of lifecycle hardening.

13. **Animation actions have mixed concurrency controls.**  
    Movement and speech use `withExclusiveControl()`, while tap expressions use `playExpression()` directly. The controller's `isPerformingAction` check is not an atomic lock. Concurrent interactions can overwrite poses or return to idle unexpectedly. Consolidate action arbitration and add tests for overlapping speech, movement, sleep and taps.

### P2 — Product completeness and trust

14. **No launcher icon/adaptive icon is present in the audited resource tree.**  
    The app needs a proper branded launcher icon and a notification icon designed for Android's monochrome notification treatment. The current notification uses `android.R.drawable.ic_menu_view`.

15. **No automated tests are present in the audited repository tree.**  
    Add tests for preference persistence, stat clamping and drift, animation/action arbitration, and service-related logic that can be tested without a device. Add a physical-device QA matrix for overlay behavior.

16. **Backup behavior is enabled without explicit backup rules.**  
    The manifest sets `android:allowBackup="true"`. Decide deliberately whether local companion state should transfer through Android backup. Document that choice and add explicit data-extraction/backup rules if needed.

17. **Privacy documentation is a set of principles, not yet a verified data inventory.**  
    The current code inspected in this audit uses local SharedPreferences and a power-connected broadcast receiver; no network client, analytics SDK or location access was found in the inspected source files. This is not a complete dependency or binary audit. The final privacy statement must be based on a full dependency/source review and a network-traffic check.

18. **The manifest requests battery-optimization exemption capability.**  
    The app declares `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`. Keep this only if the user-facing core function truly requires direct exemption and the request is justified. Otherwise remove both the permission and the corresponding UI flow.

## Recommended remediation order

1. Pin the build with the Gradle Wrapper; upgrade AGP/Gradle to versions compatible with API 36.
2. Establish a clean baseline build and add lint/test tasks to CI.
3. Design the foreground-service model for Android 14–16; choose and document a valid service type before changing `targetSdk`.
4. Harden service startup, overlay permission revocation, notification-permission handling and service-state reporting.
5. Modernize display bounds and complete lifecycle cleanup.
6. Fix action concurrency and add unit tests.
7. Decide backup and battery-exemption behavior; complete the privacy/data inventory.
8. Add branded launcher/notification assets and only then prepare signed release packaging.

## Current configuration snapshot

- Application ID: `com.aurelion.app`
- Minimum SDK: 26
- Compile SDK: 34
- Target SDK: 33
- Version: `0.1.0-phase1` (versionCode 1)
- Android Gradle Plugin: 8.2.2
- Kotlin: 1.9.22
- CI Gradle: 8.2 (installed globally; no wrapper)
- Release minification: disabled
- Release signing: not configured
- Visible automated tests: none in the audited tree

## Audit boundaries

This audit does not certify behavior on any physical device, does not claim that the latest commit builds successfully, and does not certify the full dependency graph or packaged APK. Those require a reproducible build, test execution, dependency review and device validation.


---

## Remediation progress — 18 November 2026

This addendum supersedes the earlier configuration snapshot above.

### Implemented in the repository

- Updated Android Gradle Plugin to **8.13.2**.
- Updated Kotlin and the Compose compiler plugin to **2.4.21**, aligned with the selected Gradle/AGP toolchain.
- Updated CI to use **Gradle 8.13**, **JDK 17**, and Android SDK / Build Tools **36**.
- Set both `compileSdk` and `targetSdk` to **36**.
- Declared the overlay host as a `specialUse` foreground service, added `FOREGROUND_SERVICE_SPECIAL_USE`, and documented the service subtype in the manifest.
- Updated the service to call the typed `startForeground()` overload on Android 14 and later.
- Added safe-drawing insets to the Activity content for edge-to-edge Android versions.
- Added a defensive overlay-permission check before service startup, handled overlay attachment failures, tracked actual service state, and made service cleanup conditional on acquired resources.
- Set the user-started overlay service to `START_NOT_STICKY`.
- Added CI triggers for pushes and pull requests, plus lint and unit-test tasks. Concurrent runs on the same ref are cancelled to avoid redundant builds.

### Validation status

The workflow has been triggered against the upgraded toolchain. At the time of this addendum, the latest run has not yet completed. **No successful build or lint result is claimed here.** The new API 36 target also still requires physical-device validation.

### Remaining work

1. Confirm a clean API 36 CI build and resolve any compiler or lint failures.
2. Add the official Gradle Wrapper (scripts, wrapper JAR, and pinned distribution properties) so local builds do not depend on a globally installed Gradle.
3. Complete service-start failure reporting and permission-revocation testing.
4. Replace deprecated display-bound calculations with modern window metrics and insets.
5. Review the battery-exemption request and remove it unless device testing demonstrates a genuine need.
6. Complete backup/data-extraction rules and a full privacy/dependency/network audit.
7. Add unit tests, a launcher/adaptive icon, and a proper notification icon.
8. Configure secure release signing only after the release build is validated.
