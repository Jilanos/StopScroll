## req_003_polished_ui_and_marketing_readme - Polished UI, launcher icon and marketing README
> From version: 1.0.0
> Schema version: 1.0
> Status: Done
> Understanding: 100%
> Confidence: 90%
> Complexity: Medium
> Theme: UI and presentation
> Reminder: Update status/understanding/confidence and linked backlog/task references when you edit this doc.

# AI Context
- Summary: Commercial-grade Material 3 UI on every screen, an adaptive launcher icon, device screenshots and a marketing README with cited screen-time figures.
- Keywords: polished, ui, theme, launcher, icon, onboarding, screenshots, marketing, readme
- Use when: changing the look of the app, its icon, the README or the screenshots.
- Skip when: changing session, cooldown or extension behavior.

# Needs
- Owner request (French): make a "commercial" version with nicer interfaces, then a GitHub README that makes people want to use the app, with screenshots and a few relevant screen-time figures. Angle: "s'autoriser du repos en scrollant mais avec contrôle".
- Coherent Material 3 theme (light and dark colors, typography, shapes, spacing) on the rule list, app picker, rule editor, time-up screen and warning overlay.
- Calm, positive copy. Clear empty state. An onboarding that explains the accessibility service honestly.
- An adaptive launcher icon that replaces the placeholder.
- Screenshots taken on the Pixel 8, stored under docs/screenshots/.
- Marketing README in English; build, tests and the device checklist move to docs/DEVELOPMENT.md.

# Priority
- Priority: Medium
- Rationale: Presentation work on a working app; no enforcement change.

# Context
- UI is Compose with material3 from the BOM; no new dependency may be added (offline Gradle cache).
- UI strings stay French (strings.xml). Behavior and the 47 JVM tests stay unchanged.

# Acceptance criteria
- AC1: Every screen uses one app theme with light and dark color schemes, a shared typography, shape and spacing scale.
- AC2: The rule list shows app icons in cards with durations, an empty state and a service status or setup card; an onboarding explains how it works and the accessibility permission.
- AC3: The app picker, rule editor, time-up screen and warning overlay use the new style without changing their behavior.
- AC4: The launcher icon is a custom adaptive icon with a monochrome layer.
- AC5: Device screenshots are stored under docs/screenshots/ and the owner's rules are restored after the shots.
- AC6: The README is a marketing page with cited figures and an honest permission and privacy section; developer content lives in docs/DEVELOPMENT.md.
- AC7: testDebugUnitTest, lintDebug and assembleDebug pass.

# Definition of Ready (DoR)
- [x] Problem statement is explicit and user impact is clear.
- [x] Scope boundaries (in/out) are explicit.
- [x] Acceptance criteria are testable.
- [x] Dependencies and known risks are listed: offline dependency set; the phone is the owner's personal device.

# Companion docs
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)

# References
- app/src/main/java/com/stopscroll/app/ui/
- app/src/main/res/
- README.md
- docs/DEVELOPMENT.md

# Backlog
- `item_007_polished_ui_launcher_icon_and_marketing_readme`

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |
