# Taskfall · Visual System Spec

## Direction

Soft low-poly paper objects on a deep blue-violet glass backdrop. Cards have clear outlines and restrained shadows; baskets use one hue plus a symbol and a sound cue. The overall tone is calm momentum rather than urgency.

## Tokens

- Surface: system Material.Regular glass; opaque card faces only where required for legibility.
- Accent roles: Today = warm coral, Later = soft cyan, Delegate = mellow violet, Undecided = warm amber.
- Text: theme title/body/label roles; no fixed pixel colors or Material components.
- Radius: generous, tactile corners; outline: 1–2dp equivalent; particle count low.

## Core components

Card: title, source index, grip affordance, state feedback, two-line truncation. Basket: icon, label, count, color, hover halo. CaptureEditor: multiline paste field, count/limit, confirm action. ResultSummary: grouped rows, edit affordance, screenshot action. PauseMenu: Resume, Reset, Exit.

## Motion

Spawn uses a short ease-in; falling is linear and slow. Drop confirmation has a small scale/opacity response and haptic. No camera movement, no looping background motion. A reduced-motion setting disables particles and shortens card travel.

## Rejected directions

Neon arcade was rejected because it raises urgency and weakens readability. White productivity dashboard was rejected because it removes spatial warmth and makes the experience feel like a list with decoration.
