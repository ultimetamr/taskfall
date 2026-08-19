# Taskfall · 空间便签瀑布

Taskfall is a Shared Space PICO Spatial MVP for sorting up to 20 copied to-dos into Today, Later, Delegate, or Undecided. The product design package lives in `design/taskfall/`; the implementation follows the same comfort-first contract.

## Current implementation

- Pure Kotlin domain model for card truncation, input parsing, safe fallback, and bounded 2-second spawning.
- ViewModel state flow for Home → Capture → Sorting → Paused → Result/Edit.
- SpatialUI screen shell with `PicoTheme`, built-in `Text`, `TextArea`, and `Button`, plus custom hoverable card/basket surfaces.
- Controller fallback is represented by the same click/grab callbacks as gaze + pinch, so no eye-data or voice API is required.
- Local repository interface is ready for SharedPreferences/DataStore wiring; the in-memory implementation keeps the MVP runnable without accounts or network.
- Boundary tests cover 20 Chinese tasks and the 26-character truncation rule.

## Build note

The official `pico-cli project create` path was attempted but the host is missing Java 17+, Android SDK, PICO_HOME, and the local Spatial template/knowledge graph. Network initialization also could not reach GitHub. The project files are therefore checked in with the official PICO package coordinates and can be opened in Android Studio 2025.1.x after the local PICO OS 6 toolchain is initialized.

## Intended verification

```text
pico-cli project create --name taskfall --package com.openai.taskfall --template planar --sdk-type spatial --dir . --force
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

The app deliberately uses a planar `DefaultWindowContainer` in Shared Space. No Full Space camera movement, anchors, environment mesh, or spatial scene is required for this MVP.
