# Development Guide
## Requirements
- Android Studio
- Android SDK and compatible JDK/Gradle setup
- Android device or emulator for overlay testing

## Build
1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync.
4. Run the app on a device or emulator.
5. Grant overlay permissions when prompted and test the companion flow.

For APKs, check the GitHub Actions workflow and download a successful debug build artifact when available.

## Testing checklist
- [ ] Fresh install and onboarding
- [ ] Overlay permission grant, denial, revocation, and return
- [ ] Start and stop repeatedly without duplicate overlays
- [ ] Drag, reposition, and restore location
- [ ] Rotation, resize, display cutouts, and system bars
- [ ] App backgrounding and process recreation
- [ ] Battery saver and background restrictions
- [ ] Long-running idle session
- [ ] Animation cancellation and interruption
- [ ] Accessibility and reduced-motion behavior

## Contribution style
- Keep changes focused and explain the user-visible result.
- Avoid introducing libraries for a trivial feature.
- Prefer lifecycle-safe, cancellation-aware code.
- Include testing notes in PR descriptions.
- Update documentation when behavior or permission use changes.

## Suggested issue format
**Problem:** What is wrong or missing?
**Expected:** What should happen?
**Actual:** What happens?
**Device/OS:** Where was it observed?
**Steps:** Minimal reproduction.
**Logs:** Relevant, redacted logs only.
