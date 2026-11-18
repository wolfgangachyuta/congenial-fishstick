# Architecture Overview
AURELION is a native Android application written in Kotlin and using Jetpack Compose for its UI.

## Components

### MainActivity
The app's entry point and setup surface. It presents the companion preview and handles the relevant permission/setup flow and service start/stop actions.

### OverlayService
A foreground service responsible for the floating eagle surface. It creates and hosts a Compose view using Android's window manager overlay, manages position/dragging, movement between perches, and persistence of placement.

### EagleAnimationController
Coordinates expressive animation states, idle behavior, blinking, and temporary animation control. Exclusive-control patterns are intended to prevent ambient behavior from fighting scripted movement or interactions.

### EagleState
An enumeration/model of poses and expressions available to the animation layer. It represents visual states, not the complete creature's behavioral personality.

## Intended evolution
As the project grows, consider separating these responsibilities:
- **CreatureBrain:** chooses behavior and schedules actions.
- **MoodModel:** tracks temporary mood and stable personality traits.
- **InteractionController:** translates gestures into reactions.
- **MovementController:** plans routes and manages takeoff/landing.
- **AnimationDirector:** resolves animation priority and interruption/cancellation.
- **SettingsRepository:** centralizes user settings and persistence.

Do not introduce abstractions just for their own sake. Add them when they prevent responsibilities from becoming entangled.

## Lifecycle and concurrency
Overlay and animation work must be cancellation-aware and scoped to the service lifecycle. Avoid overlapping animation jobs, stale movement loops, duplicate overlay views, and persistence writes from invalid state. The behavior engine should have one clear owner of action selection, while the animation layer executes the chosen action.

## Diagram (conceptual)
```
MainActivity
  └── setup / permissions / service controls
        └── OverlayService
              ├── overlay view + Compose UI
              ├── movement and perching
              ├── animation controller
              └── persisted position

Future:
OverlayService
  └── CreatureBrain
       ├── MoodModel
       ├── InteractionController
       ├── MovementController
       └── AnimationDirector
```
