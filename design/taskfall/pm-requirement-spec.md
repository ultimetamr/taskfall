# Taskfall · PM Requirement Spec

## Intent

Taskfall turns up to 20 copied to-dos into a short spatial sorting session. Users make one decision per card: Today, Later, Delegate, or Undecided. The app is a lightweight tool, so Shared Space is preferred and the camera never moves.

## Quality contract

- A complete 20-card run fits within 4 minutes.
- Cards arrive one every 2 seconds and never accelerate.
- Text longer than 26 Chinese characters is rendered as two truncated lines.
- Every card remains recoverable: baskets allow re-grab and the result page allows editing.
- The app works with gaze + pinch and has controller fallback; voice and eye-data APIs are not required.
- Settings and the latest 30 local sessions are saved.

## Non-goals

Reminders, calendar sync, realtime collaboration, accounts, ads, payments, community UGC, complex object recognition, posture scoring, and medical/fitness conclusions.

## Assumptions

Input is plain text with one task per line or pasted item. Empty lines are ignored. A session is considered complete after all cards are sorted or auto-enter the undecided pool.
