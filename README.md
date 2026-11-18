# AURELION 🦅
### A living eagle. A companion in your pocket. A little wildness on your screen.

<p align="center">
  <img src="https://github.com/wolfgangachyuta/congenial-fishstick/actions/workflows/build.yml/badge.svg" alt="Android APK build">
  <img src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform: Android">
  <img src="https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Language: Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="UI: Jetpack Compose">
  <img src="https://img.shields.io/badge/min%20Android%20SDK-26-brightgreen?logo=android" alt="Minimum Android SDK 26">
  <img src="https://img.shields.io/badge/status-experimental-orange" alt="Project status: Experimental">
  <img src="https://img.shields.io/badge/docs-available-blue?logo=readthedocs&logoColor=white" alt="Documentation available">
</p>


AURELION is an Android experiment in making a digital companion feel less like an app and more like a creature. A golden eagle lives in a floating overlay above your other apps: it perches, wanders, blinks, rests, reacts, and—over time—develops a personality shaped by how you interact with it.

> **Design principle:** AURELION should feel like a little living creature that happens to inhabit your phone—not an assistant that happens to look like an eagle.

---

## ✨ What is AURELION?

AURELION is a native Android virtual eagle companion. Its home is not limited to one app screen. The long-term vision is a small, persistent presence that can share your screen space, exhibit lifelike behavior, respond to touch, and optionally communicate with you.

This project combines:
- **Character animation** — expressive poses and idle behaviors.
- **A floating companion** — a draggable overlay that can live above other apps.
- **Autonomy** — movement, perching, resting, and choosing what to do without constant prompting.
- **Interaction** — tapping, petting, playful reactions, and evolving relationship mechanics.
- **Optional intelligence** — future conversational features that remain opt-in.

AURELION is deliberately creature-first. Conversation and AI are extensions of its life, not its entire identity.

---

## 🪶 Current development status

The project is actively evolving. Implemented foundations include:

- Native Android app built with Kotlin.
- Jetpack Compose UI.
- A floating overlay hosted through Android's window system.
- Foreground service for overlay presence.
- Draggable eagle placement.
- Position persistence.
- Autonomous movement between perch locations.
- Animation-state controller with multiple expressions and poses.
- Random idle behavior and blinking.
- GitHub Actions workflow for building a debug APK.

Features described as planned or experimental in this repository's documentation are **not necessarily implemented yet**. See the [Roadmap](docs/ROADMAP.md) for the planned sequence.

---

## 🚀 Getting AURELION on your phone

### Option A — Download the latest build artifact
1. Open the repository's **Actions** tab.
2. Select the latest successful **Build AURELION APK** workflow run.
3. Download the `aurelion-debug-apk` artifact.
4. Extract the ZIP and locate `app-debug.apk`.
5. Install it on your Android device. You may need to allow your file manager or browser to install apps from unknown sources.

Build artifacts are temporary workflow outputs and may not always be available. The workflow also assembles unsigned release APK/AAB outputs for engineering validation; these are not installable production packages until signed.

### Option B — Build it yourself
Requirements:
- Android Studio (current stable recommended)
- Android SDK
- JDK version supported by the project's Gradle configuration

1. Clone the repository.
2. Open the project in Android Studio.
3. Allow Gradle sync to complete.
4. Run the app on an Android device or emulator.

The project uses Android SDK 36 (Android 16) as its compile and target SDK, supports Android 8.0 (API 26) and later, and builds with the checked-in Gradle Wrapper.

---

## 🧭 How it works

At a high level:

1. **MainActivity** provides the app entry screen and handles setup, permission requests, and starting or stopping the companion.
2. **OverlayService** hosts the eagle in a floating Compose view, handles placement, movement, and overlay lifecycle.
3. **EagleAnimationController** manages the eagle's expressive animation states and idle transitions.
4. **EagleState** defines the poses and expressions the companion can display.

The architecture is intentionally being separated into presentation, movement, and behavior so future systems—such as a personality engine or interaction model—can grow without turning the companion into one giant controller.

See [Architecture](docs/wiki/Architecture.md).

---

## 🎮 The companion's design

AURELION is envisioned as a creature with rhythms, not a notification machine.

### Creature behaviors
- **Perching:** pauses at different locations.
- **Wandering:** moves around the screen occasionally.
- **Resting:** becomes quieter and less active.
- **Curiosity:** looks toward activity or responds to interaction.
- **Playfulness:** displays energetic expressions and movement.
- **Sleep:** enters a low-activity state during quiet periods (planned).

### Relationship & personality
Future behavior may be shaped by interaction, time, care, and the eagle's temperament. Any relationship or personality system should remain playful, transparent, and resettable—not manipulative or guilt-driven.

---

## 🗺️ Roadmap

The roadmap is organized around making AURELION **stable first, alive second, magical third**.

- **Foundation:** reliability, lifecycle, movement, and battery-conscious behavior.
- **Creature:** coherent behavioral states, reactions, touch interaction, and personality.
- **Atmosphere:** sounds, ambient effects, time-of-day awareness, and richer animation.
- **Companion:** optional dialogue, accessibility, and conversational intelligence.
- **Ecosystem:** customization, unlockables, tools, and community-created behaviors.

See the complete [Roadmap](docs/ROADMAP.md).

---

## 📚 Documentation & Wiki

- [Wiki Home](docs/wiki/Home.md)
- [Features](docs/wiki/Features.md)
- [Architecture](docs/wiki/Architecture.md)
- [Personality & Behavior](docs/wiki/Personality.md)
- [Development Guide](docs/wiki/Development.md)
- [Privacy & Permissions](docs/wiki/Privacy-and-Permissions.md)
- [Feature Laboratory](docs/wiki/Ideas.md)

These pages are maintained in the repository under `docs/wiki/` as wiki-ready Markdown. They can be copied into GitHub Wiki pages if/when the GitHub Wiki is enabled separately.

---

## 🔐 Privacy philosophy

AURELION should never needlessly collect personal data to feel alive. The current app has no Internet permission or network integration. Companion position, pet-care values, and pacing preferences remain local in SharedPreferences; Android backup/transfer rules include those settings. Future cloud, AI, analytics, or online features must be optional, clearly explained, and designed so users know what leaves their device.

See [Privacy & Permissions](docs/wiki/Privacy-and-Permissions.md).

---

## 🛠️ Technology

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Platform:** Android
- **Overlay:** Android WindowManager / application overlay
- **Background presence:** Foreground service
- **Persistence:** Local preferences
- **Automation:** GitHub Actions build workflow

---

## 🧪 Development status & expectations

AURELION is a work in progress. Expect rough edges, unfinished behaviors, missing features, and changes in direction.

Bug reports, ideas, design discussions, and contributions are welcome. Before opening a contribution, please review the [Development Guide](docs/wiki/Development.md).

---

## 🌟 The long-term dream

One day, AURELION might notice that it has been perched in one place for a while, stretch its wings, fly to another corner, react when you gently tap its head, become curious about a new environment, retreat when it needs rest, and quietly keep you company without demanding attention.

It should be delightful in small moments—and easy to ignore when you need your phone to yourself.

**AURELION: let a little wildness live on your screen.**

---

## License

Check the repository for its license file. If no license has been added, the project is currently not explicitly licensed for reuse.
