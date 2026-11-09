## task_003_time_up_screen_with_30_s_extension_and_app_icons - Time-up screen with +30 s extension and app icons
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 100%
> Confidence: 90%
> Progress: 90%
> Complexity: Medium
> Theme: Implementation delivery
> Reminder: Update status/understanding/confidence/progress and linked request/backlog references when you edit this doc.
> Indicators reviewed: 2026-11-09 20:07:06

# AI Context
- Summary: Time-up screen at expiry with a +30 s extension that doubles the next block, and app icons in app lists.
- Keywords: time, screen, extension, app, icons, cooldown, doubling
- Use when: changing expiry presentation, the extension rule or app list rendering.
- Skip when: changing cooldown re-entry enforcement or warning behavior.

# Definition of Done (DoD)
- [x] The backlog scope is implemented.
- [x] Acceptance criteria are covered (JVM for the engine rule; screen, relaunch and icons remain device checks 17-18 in the README).
- [x] Validation passes.
- [ ] Meaningful waves followed ADR 009: affected docs updated and the repo left commit-ready without automatic commits.

# Backlog
- `item_006_time_up_screen_with_30_s_extension_and_app_icons`

# Acceptance criteria
- AC1: At expiry with the target in the foreground, the time-up screen is shown with the app icon, name and block duration; OK and back go HOME.
- AC2: "+30 s" cancels the running block, grants 30 seconds, relaunches the app and doubles the next block; repeated grants double again, capped by the duration bounds.
- AC3: The doubled duration and the extension allowance are persisted and survive a restart; a block that runs to its end resets the next one to the rule duration.
- AC4: Re-entry during a block still sends HOME without an extension.
- AC5: The app picker and rule list show launcher icons, decoded off the main thread and cached.
- AC6: The rule is documented in the README; JVM tests cover the engine rule; lint, unit tests and debug assembly pass.

# Plan
- [x] Wave 1: Engine rule. `PackageRuntimeState.cooldownMillis` holds the block duration of the cycle; `SessionEngine.extensionOffer` and `grantExtension`; `doubledDurationMillis` capped by `maxDurationMillis`; JVM tests.
- [x] Wave 2: Android adapter. `TimeUpActivity` started by the service on `GoHome(ALLOWANCE_EXPIRED)` (HOME fallback), `ExtensionRequests` in-process channel, `EngineEffect.LaunchApp` relaunches the target. Re-entry keeps HOME.
- [x] Wave 3: Launcher icons (`AppIcon`, LRU cache, IO decoding) in the rule list and picker; picker list loaded off the main thread. README rule and device checklist items 17-18.
- [ ] Wave 4: Device check on the Pixel 8 (README checklist items 3, 17, 18), then finish through Logics closeout.

# Validation
- 2026-11-09: `./gradlew --offline testDebugUnitTest lintDebug assembleDebug` with Temurin 17.0.20.1+1: BUILD SUCCESSFUL. Lint analysis forced with `--rerun`: no issues found (warningsAsErrors).
- 47 JVM tests, 0 failures, 0 skipped (35 before this task; 12 added: ExtensionTest 9, DurationBoundsTest 2, SessionRuntimeTest 1).
- APK: app/build/outputs/apk/debug/app-debug.apk. No device was used.

# Report
- Rule as implemented: at expiry the block starts with duration D (the pending doubled duration if any, else the rule duration) and D is stored. "+30 s" during that block cancels it, sets 30 s of allowance (no warning) and stores 2D, capped at `maxDurationMillis(now)`. The next expiry applies 2D; a further grant stores 4D, and so on. A block that runs to its end clears the stored duration; disabling or removing the rule clears it too. A block restored from an older state without a stored duration offers no extension.
- AC1/AC4: StopScrollAccessibilityService.showTimeUp and goHome; device check pending.
- AC2/AC3: ExtensionTest, SessionRuntimeTest (persistence and LaunchApp), DurationBoundsTest (cap).
- AC5: AppIcon.kt, MainActivity rule card and picker; device check pending.
- AC6: README "Extension rule (+30 s)"; validation above.

# Links
- Request: `req_002_time_up_screen_extension_and_app_icons`
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |
