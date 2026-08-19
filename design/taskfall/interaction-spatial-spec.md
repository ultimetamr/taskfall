# Taskfall · Interaction & Spatial Spec

## Task model

1. Paste or type up to 20 lines.
2. Confirm and enter the sorting field.
3. Look at a falling card, pinch/grab it, and drag it into a basket; release to classify.
4. If ignored, the card settles into Undecided.
5. Re-grab from any basket or edit from the result list.
6. Export a screenshot and return Home.

## Spatial concept review

### Hypotheses

1. **Falling field**: cards drift down in the comfort zone; destinations are a stable lower arc. Highest spatial value, low engineering risk.
2. **Orbit carousel**: cards orbit the user and are selected from a radial ring. More spatial novelty, but higher gaze travel and motion risk.
3. **Desk tableau**: cards appear on a virtual desk with hand sorting. Comfortable, but less immediate and less distinct from a 2D board.

Selected: Falling field. It best matches the 4-minute goal, keeps eye travel bounded, and makes “do nothing” a safe outcome. Orbit was rejected for motion/fatigue; desk was rejected for weaker time/space value.

## State graph

Home → Onboarding → Capture → Sorting → Result/Edit → Home. Sorting may enter Paused; PauseMenu offers Resume, Reset, Exit. Result offers Edit, Screenshot, Home. Back from Sorting opens PauseMenu; back from Capture returns Home.

## Interaction contract

- Primary: gaze focus + pinch; pinch hold maps to grab, hand movement maps to drag, release maps to drop.
- Fallback: controller ray + trigger, with the same grab/drop state machine.
- GrabInteractable cards and baskets expose hover, pressed, grabbed, and rejected feedback.
- Haptics: soft tick on focus, short confirm on accepted drop, low pulse on rejected drop.
- AudioCue: distinct soft chimes for Today, Later, Delegate; muted lower note for Undecided.
- No camera motion. Card speed is constant and capped. Reduce Motion lowers card offset and particle count.

## Comfort

Cards spawn within the central comfort field, at least 56dp hit targets. Key text is fixed in the comfort view. After 10 minutes of continuous use, show a break prompt. No Full Space movement is used.
