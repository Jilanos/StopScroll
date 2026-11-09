## item_007_polished_ui_launcher_icon_and_marketing_readme - Polished UI, launcher icon and marketing README
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 100%
> Confidence: 90%
> Progress: 85%
> Complexity: Medium
> Theme: UI and presentation
> Reminder: Update status/understanding/confidence/progress and linked request/task references when you edit this doc.
> Indicators reviewed: 2026-11-09 20:32:11

# AI Context
- Summary: Commercial-grade Material 3 UI on every screen, an adaptive launcher icon, device screenshots and a marketing README with cited screen-time figures.
- Keywords: polished, ui, theme, launcher, icon, onboarding, screenshots, marketing, readme
- Use when: changing the look of the app, its icon, the README or the screenshots.
- Skip when: changing session, cooldown or extension behavior.

# Problem
Owner request (French): make a "commercial" version with nicer interfaces, then a GitHub README that makes people want to use the app, with screenshots and a few relevant screen-time figures. Angle: "s'autoriser du repos en scrollant mais avec contrôle".
Coherent Material 3 theme (light and dark colors, typography, shapes, spacing) on the rule list, app picker, rule editor, time-up screen and warning overlay.
Calm, positive copy. Clear empty state. An onboarding that explains the accessibility service honestly.
An adaptive launcher icon that replaces the placeholder.
Screenshots taken on the Pixel 8, stored under docs/screenshots/.
Marketing README in English; build, tests and the device checklist move to docs/DEVELOPMENT.md.

# Scope
- In:
  - App theme, onboarding, rule list, app picker, rule editor, time-up screen, warning overlay, launcher icon
  - Device screenshots, marketing README, docs/DEVELOPMENT.md
- Out:
  - Any change to the session engine, persistence or enforcement rules
  - Store listing, release signing, translations

# Acceptance criteria
- AC1: Every screen uses one app theme with light and dark color schemes, a shared typography, shape and spacing scale.
- AC2: The rule list shows app icons in cards with durations, an empty state and a service status or setup card; an onboarding explains how it works and the accessibility permission.
- AC3: The app picker, rule editor, time-up screen and warning overlay use the new style without changing their behavior.
- AC4: The launcher icon is a custom adaptive icon with a monochrome layer.
- AC5: Device screenshots are stored under docs/screenshots/ and the owner's rules are restored after the shots.
- AC6: The README is a marketing page with cited figures and an honest permission and privacy section; developer content lives in docs/DEVELOPMENT.md.
- AC7: testDebugUnitTest, lintDebug and assembleDebug pass.

# AC Traceability
- request-AC1 -> This backlog slice. Proof: AC1: Every screen uses one app theme with light and dark color schemes, a shared typography, shape and spacing scale.
- request-AC2 -> This backlog slice. Proof: AC2: The rule list shows app icons in cards with durations, an empty state and a service status or setup card; an onboarding explains how it works and the accessibility permission.
- request-AC3 -> This backlog slice. Proof: AC3: The app picker, rule editor, time-up screen and warning overlay use the new style without changing their behavior.
- request-AC4 -> This backlog slice. Proof: AC4: The launcher icon is a custom adaptive icon with a monochrome layer.
- request-AC5 -> This backlog slice. Proof: AC5: Device screenshots are stored under docs/screenshots/ and the owner's rules are restored after the shots.
- request-AC6 -> This backlog slice. Proof: AC6: The README is a marketing page with cited figures and an honest permission and privacy section; developer content lives in docs/DEVELOPMENT.md.
- request-AC7 -> This backlog slice. Proof: AC7: testDebugUnitTest, lintDebug and assembleDebug pass.

# Decision framing
- Product framing: Not needed
- Product signals: (none detected)
- Product follow-up: No product brief follow-up is expected based on current signals.
- Architecture framing: Not needed
- Architecture signals: (none detected)
- Architecture follow-up: No architecture decision follow-up is expected based on current signals.

# Links
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)
- Request: `logics/request/req_003_polished_ui_and_marketing_readme.md`
- Primary task(s): (none yet)

# Priority
- Priority: Medium
- Rationale: Default until groomed.

# Notes
- Hybrid rationale: Derived from request `req_003_polished_ui_and_marketing_readme` and kept bounded to one coherent delivery slice.
- Source file: `logics/request/req_003_polished_ui_and_marketing_readme.md`.
- Generated locally by logics-manager.

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |


# Tasks
- `task_004_polished_ui_launcher_icon_and_marketing_readme`
