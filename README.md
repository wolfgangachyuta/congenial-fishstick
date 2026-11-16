# AURELION

A native Android app: a floating virtual eagle companion that lives on your phone — roaming, perching, speaking, and reacting, even while you're using other apps.

## Status: Phase 1 — the eagle's core look and idle animation

This first push gets a buildable app on screen: a procedurally drawn golden eagle (no art assets yet) breathing, blinking, and slowly turning its head, inside a normal app screen. The floating cross-app overlay (the headline feature) comes in Phase 2.

## Build order

1. **Eagle character & animation** ← we are here
2. Floating overlay (SYSTEM_ALERT_WINDOW + foreground service)
3. Autonomous movement & perching
4. Random speech bubbles & dialogue
5. Touch interactions & pet attributes (energy/happiness/affection/curiosity/hunger)
6. Optional AI conversation mode + Text-to-Speech
7. Performance pass + final APK

## Getting the APK (no computer needed)

Every push to `main` triggers a GitHub Actions build. To get the installable APK on your phone:

1. Open this repo in the GitHub app (or github.com in your browser).
2. Go to the **Actions** tab.
3. Open the latest "Build AURELION APK" run — wait for the green check.
4. Scroll to **Artifacts** and download `aurelion-debug-apk`.
5. Unzip it (any file manager / zip app on Android can do this) to get `app-debug.apk`, then tap it to install (you'll need to allow "install unknown apps" for your browser/files app the first time).

If a build run turns red (fails), open it and copy the error text from the failing step — share it back and it'll get fixed in the next push.
