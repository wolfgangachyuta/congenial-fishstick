# AURELION — Development Roadmap
*Vision: build a believable, delightful, battery-conscious digital eagle.*

This roadmap is aspirational. A feature is not complete merely because it appears here. Items should be moved to **Done** only after implementation, testing, and documentation are verified.

## Guiding priorities

1. **Reliability before spectacle** — the overlay must behave predictably.
2. **Creature before chatbot** — AURELION's body language and autonomy are the core.
3. **Low distraction** — no guilt, spam, or attention traps.
4. **Battery-aware autonomy** — activity is sparse, bounded, and user-configurable.
5. **Privacy by default** — local-first; online features are opt-in.
6. **Accessibility and control** — easy pause, hide, mute, reset, and customize.

## Status legend
- [ ] Planned
- [x] Implemented foundation (verify against current code before release)
- 🚧 In progress
- 🧪 Experimental

---

## Phase 0 — Buildable creature foundation
- [x] Native Android/Kotlin project
- [x] Compose-based UI foundation
- [x] Eagle animation state model
- [x] Idle/blink/expression behaviors
- [x] Build workflow for debug APK
- [ ] Add a documentation-linked feature status page that stays accurate

## Phase 1 — Make the floating eagle dependable
**Goal:** The overlay should survive real-world Android usage without feeling fragile.

- [ ] Test overlay permission flow on fresh install and after permission revocation
- [ ] Test foreground-service start/stop, app swipe-away, process death, and reboot behavior
- [ ] Prevent duplicate overlays or duplicate service instances
- [ ] Add safe recovery when overlay attachment fails
- [ ] Validate drag boundaries across rotation, display changes, and insets
- [ ] Verify saved position is restored and clamped to visible bounds
- [ ] Add a user-visible pause/hide control
- [ ] Define graceful behavior when battery saver or background restrictions apply
- [ ] Add crash-safe service state transitions
- [ ] Test on multiple Android versions and screen sizes

**Exit criteria:** no stuck overlay, no duplicate eagle, no invisible-but-running service, and predictable permission recovery.

## Phase 2 — The eagle feels alive
**Goal:** Replace disconnected random animations with a coherent behavior system.

- [ ] Behavioral state machine: Awake, Curious, Wandering, Resting, Excited, Startled, Sleepy, Sleeping
- [ ] State transition rules and weighted event selection
- [ ] Animation priority system (movement > interaction > ambient idle)
- [ ] Interruptible, cancellation-safe animation routines
- [ ] Personality parameters: boldness, curiosity, energy, sociability, calmness
- [ ] Behavior cooldowns to prevent repetition
- [ ] Micro-behaviors: head tilt, feather ruffle, wing stretch, foot adjustment, scanning
- [ ] Context-aware idle timing (not constant motion)
- [ ] Debug behavior panel to inspect state, timers, and chosen actions

**Exit criteria:** actions feel intentional, repetition is controlled, and state transitions never conflict.

## Phase 3 — Touch, play, and trust
- [ ] Tap: glance, chirp, or acknowledge
- [ ] Long-press: open a compact interaction menu
- [ ] Pet/scratch gesture: affectionate reaction with clear boundaries
- [ ] Swipe: nudge, move, or playful dodge (configurable)
- [ ] Double-tap: quick toggle of companion controls
- [ ] Drag-to-perch with snap points
- [ ] Touch reaction variety and cooldowns
- [ ] No accidental blocking of underlying app interactions
- [ ] Interaction settings and gesture customization

**Exit criteria:** touch interactions are discoverable, reliable, and never obstruct normal phone use.

## Phase 4 — Perches, flight, and screen choreography
- [ ] Perch profiles for corners, edges, and preferred locations
- [ ] Flight paths with smooth curves and easing
- [ ] Takeoff/landing anticipation and follow-through
- [ ] Optional screen-edge cling behavior
- [ ] Short idle flights and landing poses
- [ ] User-defined favorite perches
- [ ] Flight frequency slider and "stay still" mode
- [ ] Safe areas around system UI, cutouts, and keyboard
- [ ] Animation speed and movement intensity controls

## Phase 5 — A tiny world
- [ ] Day/night cycle using local time without requiring network
- [ ] Weather-inspired moods (offline/manual mode first; live weather optional)
- [ ] Ambient world states: breezy, warm, rainy, snowy, quiet night
- [ ] Seasonal cosmetic accents
- [ ] Rare but non-disruptive discoveries
- [ ] Environmental animation overlays that can be disabled

## Phase 6 — Sound and accessibility
- [ ] Optional chirps and wing sounds
- [ ] Separate volume and mute controls
- [ ] Sound only when permitted by user settings and device audio context
- [ ] Haptic feedback (optional)
- [ ] Reduce motion mode
- [ ] High-contrast settings and scalable controls
- [ ] Respect silent mode / accessibility expectations
- [ ] Caption or text representation for meaningful sounds

## Phase 7 — Personality & memory (local-first)
- [ ] Personality presets (Gentle, Curious, Mischievous, Regal, Sleepy)
- [ ] Local relationship progression that is transparent and resettable
- [ ] Lightweight memory of user-chosen interactions—not sensitive profiling
- [ ] Affection, curiosity, energy, and comfort values with no punitive decay
- [ ] Habits/preferences that can be inspected and erased
- [ ] Personality sliders for user customization
- [ ] "No guilt" policy: never shame users for ignoring or not caring for the eagle

## Phase 8 — Conversation (optional, not the core)
- [ ] Text dialogue bubbles
- [ ] Small set of authored character lines
- [ ] Contextual reactions before any generative AI
- [ ] Optional voice output / TTS
- [ ] Optional AI conversation provider
- [ ] Clear controls for online processing, retention, and costs
- [ ] Character-aware conversation tone without pretending to be human
- [ ] Quiet mode and conversation off switch

## Phase 9 — Companion utility abilities
Ideas for useful, non-intrusive features:
- [ ] Focus companion: optional Pomodoro sessions with idle perch animation
- [ ] Gentle time reminders (fully configurable; no nagging)
- [ ] Study buddy: silent presence during study; celebratory wing stretch after session
- [ ] Quick note capture via explicit user action
- [ ] Pocket weather mood (opt-in)
- [ ] Calendar glance (opt-in, limited permissions, never read private event details unnecessarily)
- [ ] Customizable "do not disturb" windows

## Phase 10 — Personalization & collection
- [ ] Feather/color palettes
- [ ] Accessories and seasonal skins
- [ ] Perch backgrounds or tiny habitats
- [ ] Animation packs
- [ ] Earnable cosmetics through play, not paid pressure
- [ ] Photo mode / pose selection
- [ ] Shareable companion snapshots (user-triggered only)
- [ ] Restore/export customization settings

## Phase 11 — Performance, polish, and release
- [ ] Profile CPU, memory, battery, and frame pacing
- [ ] Reduce idle wakeups and unnecessary recomposition
- [ ] Stress-test long sessions and screen transitions
- [ ] Automated tests for state transitions and boundaries
- [ ] Device matrix testing
- [ ] Accessibility review
- [ ] Security/privacy review for optional online integrations
- [ ] App icon, onboarding, and store-ready screenshots
- [ ] Versioning, release notes, and signed release process
- [ ] Publish a stable release with clear support expectations

---

# ⚡ Fast-track: "Crazy Nice" features to prioritize ASAP

These are high delight-to-effort ideas. Implement the first few before adding complex AI.

### 1. Eagle Mood System
A compact mood engine that influences animation selection and timing. Mood is not a random label—it changes behavior weights.

### 2. Tap Reactions
Tap once: the eagle looks at you. Tap again: a different response. Include a few rare reactions, but never make them so rare that users feel manipulated.

### 3. Wing Stretch & Feather Ruffle
Short, expressive micro-animations between idle states. These can make a character feel dramatically more alive without large new systems.

### 4. Favorite Perch
Let the user drag the eagle to a location and mark it as its favorite. The eagle can occasionally return there.

### 5. Time-of-Day Personality
Morning: alert and curious. Afternoon: relaxed. Evening: quieter. Night: sleep/rest. Use local clock only; no location permission required.

### 6. Tiny Chirp Pack
Optional, muted by default or explicitly enabled during setup. Add distinct sounds for tap, landing, curiosity, and sleep.

### 7. Focus Mode
A small timer with the eagle perched calmly. During a session, reduce movement; on completion, the eagle gives a tiny celebration.

### 8. "What's That?" Curiosity
An explicit tap on a curiosity button triggers a playful look-around sequence or a short authored line—no screen monitoring needed.

### 9. Snapshot Mode
Choose a pose, hide UI chrome, and take a user-triggered companion snapshot for sharing.

### 10. Easter Egg Flight
Occasional short flight patterns, such as a figure-eight or dramatic hop, triggered manually or at low-frequency random intervals.

## Suggested first sprint (small but visible)
1. Behavior state machine (Awake/Resting/Curious/Excited)
2. Tap-to-react with 4–6 reactions
3. Favorite perch and drag snapping
4. Micro-animation pass (stretch, ruffle, head tilt)
5. Battery/lifecycle audit
6. Update README and keep status honest

**Do not start with a full AI chatbot.** First make the eagle's movement, personality, and interactions memorable. A convincing creature will make any later conversational feature much more special.
