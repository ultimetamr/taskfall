# 空间便签瀑布（Taskfall）project guidance

This project implements the Taskfall spatial todo-sorting MVP for PICO Project Swan / PICO OS 6. It is a Shared Space, planar-window tool: cards fall inside the comfort field and are moved into four stable destination baskets.

Key files:

- `app/src/main/java/com/openai/taskfall/domain/` — non-UI models and business rules.
- `app/src/main/java/com/openai/taskfall/data/` — local repository abstraction and recent-session storage boundary.
- `app/src/main/java/com/openai/taskfall/ui/sorting/` — unidirectional UI state, ViewModel, screen, cards, baskets, pause/result surfaces.
- `app/src/main/java/com/openai/taskfall/platform/` — Spatial Application, launcher Activity, and `mainApp` entry.
- `app/src/test/` — boundary tests for the 20-card flow and Chinese truncation.

All 2D UI must use SpatialUI components and be wrapped in `PicoTheme`; Material/Material3 is forbidden. The system `Material.Regular` window glass remains enabled, so the root does not paint an opaque background. Prefer gaze + pinch, keep controller callbacks equivalent, avoid camera movement, preserve re-grab/edit recovery, and add future modules through `HandInput`, `ControllerInput`, `GrabInteractable`, `AudioCue`, `Haptics`, `TutorialStep`, `PauseMenu`, and `ScreenshotExporter` boundaries.

Build after PICO setup with `./gradlew :app:testDebugUnitTest` and `./gradlew :app:assembleDebug`. Install/launch on a connected Swan/emulator only after the local Android and PICO toolchains are available.

Current verified baseline:

- Package/application entry: `com.openai.taskfall` / `.platform.LaunchActivity`.
- Launcher label is `空间便签瀑布`; generated launcher icon resources live under `app/src/main/res/mipmap-*`, with the retained master artwork at `artifacts/branding/space-note-waterfall-icon-master.png`.
- `SharedPreferencesTaskfallRepository` persists the last input, reduce-motion setting, and the latest 30 session summaries.
- Cards use PICO OS 6 planar `dragAndDropSource`/`dragAndDropTarget`; click-card then click-basket remains the controller fallback.
- Focus Bloom sources accidentally mixed into the module were preserved under `archive/focusbloom/` and are excluded from the Taskfall APK.
- Final emulator verification passed on PICO OS 6.0.0 Swan x86_64 (`emulator-5554`), with screenshot at `artifacts/taskfall-final-verified.png` and no crash-buffer/AndroidRuntime errors.

<!-- pico-cli:plugin-context:pico-spatial-agentic-tools:start -->
## Plugin Context

Also read `./PICO-SPATIAL-AGENTIC-TOOLS.AGENTS.md` for PICO Spatial plugin guidance.
<!-- pico-cli:plugin-context:pico-spatial-agentic-tools:end -->
