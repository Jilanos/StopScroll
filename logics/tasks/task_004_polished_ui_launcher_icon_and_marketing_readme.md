## task_004_polished_ui_launcher_icon_and_marketing_readme - Polished UI, launcher icon and marketing README
> From version: 1.0.0
> Schema version: 1.0
> Status: Done
> Understanding: 100%
> Confidence: 90%
> Progress: 100%
> Complexity: Medium
> Theme: Implementation delivery
> Reminder: Update status/understanding/confidence/progress and linked request/backlog references when you edit this doc.
> Indicators reviewed: 2026-11-09 20:32:11

# AI Context
- Summary: Commercial-grade Material 3 UI on every screen, an adaptive launcher icon, device screenshots and a marketing README with cited screen-time figures.
- Keywords: polished, ui, theme, launcher, icon, onboarding, screenshots, marketing, readme
- Use when: changing the look of the app, its icon, the README or the screenshots.
- Skip when: changing session, cooldown or extension behavior.

# Definition of Done (DoD)
- [x] The backlog scope is implemented.
- [x] Acceptance criteria are covered.
- [x] Validation passes.
- [x] Meaningful waves followed ADR 009: affected docs updated and the repo left commit-ready without automatic commits.

# Backlog
- `item_007_polished_ui_launcher_icon_and_marketing_readme`

# Acceptance criteria
- AC1: Every screen uses one app theme with light and dark color schemes, a shared typography, shape and spacing scale.
- AC2: The rule list shows app icons in cards with durations, an empty state and a service status or setup card; an onboarding explains how it works and the accessibility permission.
- AC3: The app picker, rule editor, time-up screen and warning overlay use the new style without changing their behavior.
- AC4: The launcher icon is a custom adaptive icon with a monochrome layer.
- AC5: Device screenshots are stored under docs/screenshots/ and the owner's rules are restored after the shots.
- AC6: The README is a marketing page with cited figures and an honest permission and privacy section; developer content lives in docs/DEVELOPMENT.md.
- AC7: testDebugUnitTest, lintDebug and assembleDebug pass.

# Plan
- [x] Wave 1: Theme (`ui/theme/`: light and dark schemes, typography, shapes, spacing, in-app theme choice), onboarding, rule list with cards, setup card and empty state, full-screen app picker with suggestions and search, rule editor with presets, time-up screen, warning pill, adaptive launcher icon.
- [x] Wave 2: docs/DEVELOPMENT.md (developer content moved from the README) and the marketing README with cited figures.
- [x] Wave 3: Screenshots on the Pixel 8 (onboarding, rules, picker, editor, time-up, warning) under docs/screenshots/, status bar and gesture bar cropped, light theme, gallery in the README. Temporary rules (YouTube, Reddit, X with 30 s) were added through the UI. The phone locked during cleanup, so the owner rules and the current Instagram allowance were restored by rewriting the DataStore file and the display preferences (theme back to system) before `adb install -r`. Durations now use no-break spaces after the time-up shot showed "20 / min" wrapping.

# Validation
- 2026-11-09: `./gradlew --offline testDebugUnitTest lintDebug assembleDebug` with Temurin 17.0.20.1+1: BUILD SUCCESSFUL. Lint analysis forced with `lintAnalyzeDebug --rerun`: no issues found (warningsAsErrors).
- 47 JVM tests, 0 failures, 0 skipped (unchanged).
- Merged manifest: no INTERNET permission; only the library-generated DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION.
- Pixel 8: onboarding, rule list, picker, editor, warning pill and time-up screen checked visually. Final APK installed with `adb install -r`; accessibility service still enabled.
- Finish workflow executed on 2026-11-09.
- Linked backlog/request close verification passed.

# Report
- AC1: `ui/theme/Theme.kt` (teal light and dark schemes, typography, shapes, Spacing), `ThemePreference` (system, light, dark).
- AC2: `RulesScreen.kt` (cards with icons and duration pills, setup card, empty state), `OnboardingScreen.kt`.
- AC3: `AppPickerScreen.kt`, `RuleEditorScreen.kt` (same seconds validation, presets), `TimeUpActivity.kt`, `WarningOverlay.kt` (rounded pill). Engine and service untouched apart from the overlay view.
- AC4: `drawable/ic_launcher_*`, `mipmap-anydpi-v26/ic_launcher.xml` with a monochrome layer.
- AC5: docs/screenshots/; owner rules restored and verified on the device (Instagram 5 min / 20 min, Threads 2 min / 50 min).
- AC6: README.md (figures from DataReportal Digital 2025, Allcott et al. AER 2022, Gruening et al. PNAS 2023), docs/DEVELOPMENT.md.
- AC7: see Validation.
- Finished on 2026-11-09.
- Linked backlog item(s): `item_007_polished_ui_launcher_icon_and_marketing_readme`
- Related request(s): `req_003_polished_ui_and_marketing_readme`

# Links
- Request: `req_003_polished_ui_and_marketing_readme`
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |
