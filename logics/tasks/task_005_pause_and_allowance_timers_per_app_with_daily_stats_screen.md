## task_005_pause_and_allowance_timers_per_app_with_daily_stats_screen - Pause and allowance timers per app with daily stats screen
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 90%
> Confidence: 85%
> Progress: 90%
> Complexity: Medium
> Theme: Implementation delivery
> Reminder: Update status/understanding/confidence/progress and linked request/backlog references when you edit this doc.
> Owner: paul.mondou@circle-mobility.com
> Indicators reviewed: 2026-11-12 12:17:51

# AI Context
- Summary: Allowance timer while the app is open, pause timer while it is closed; a full pause refills the allowance. Main screen shows today's usage and both timers.
- Keywords: pause, allowance, detente, timers, reset, daily usage, stats, progress
- Use when: changing how the allowance refills, the pause away from an app, daily usage accounting or the main screen timers.
- Skip when: changing the time-up screen, the +30 s rule or the warning pill.

# Definition of Done (DoD)
- [x] The backlog scope is implemented.
- [x] Acceptance criteria are covered (JVM for AC1-AC5 and the AC6 status mapping; AC6 rendering remains a device check).
- [x] Validation passes.
- [ ] Meaningful waves followed ADR 009: affected docs updated and the repo left commit-ready without automatic commits.

# Backlog
- `item_008_pause_and_allowance_timers_per_app_with_daily_stats_screen`

# Acceptance criteria
- AC1: Engine: while the app is in the foreground the allowance decreases and no pause accrues; after leaving, the allowance is frozen and the pause accrues.
- AC2: Engine: reopening before the pause duration resets the pause and resumes the exact remaining allowance.
- AC3: Engine: once the pause duration is reached away from the app, the allowance is full again, the warning is re-armed and a pending doubled block is cleared.
- AC4: Today's usage is tracked per app by local day, survives restarts, and is kept when a rule is disabled.
- AC5: Persistence: states saved by the previous version decode and behave conservatively (a state saved while running starts its pause at restore).
- AC6: The main screen shows, per enabled app, today's usage, an allowance bar and a pause bar (or the running block), updated every second.
- AC7: Fake-clock JVM tests cover the four transitions; `./gradlew test assembleDebug` passes; README and development guide describe the rule.

# Plan
- [x] Wave 1: Engine. `PackageRuntimeState` gains `pausedSinceWallMillis`, `usageDay`, `usedTodayMillis`; `SessionEngine` starts the pause on leaving, clears it on reopening, refills the allowance once the pause is reached; daily usage by local day through `Clock.localDay`. Fake-clock tests for the four transitions, restore of previous-version states, store decoding of a previous-version payload.
- [x] Wave 2: UI. Pure `timerStatus` view model in the engine package (tested); the main screen rule cards show today's usage, an allowance bar and a pause bar or the running block, refreshed every second from the persisted runtime state.
- [x] Wave 3: README and development guide; `./gradlew test assembleDebug`.
- [ ] Wave 4: Device check on the Pixel 8 (development guide checklist items 7, 16, 19-21), then finish through Logics closeout.

# Validation
- 2026-11-12: `./gradlew test assembleDebug lintDebug` with Temurin 17.0.20.1+1: BUILD SUCCESSFUL. Lint (warningsAsErrors): no issues.
- 73 JVM tests per variant (debug and release), 0 failures, 0 skipped (47 before this task; added PauseTimerTest 18, TimerStatusTest 7, StopScrollStoreTest 1; 6 existing tests that encoded the previous never-refilled allowance were adapted).
- APK: app/build/outputs/apk/debug/app-debug.apk. No device was used.

# Report
- Rule as implemented: leaving an app with part of the allowance used stores `pausedSinceWallMillis`; reopening clears it and resumes the remaining allowance; once `cooldownMillis ?: rule.cooldownMillis` has elapsed away from the app, the engine refills the allowance, re-arms the warning and clears a pending doubled block. The pause uses wall time like the block.
- Daily usage: `usageDay` (local epoch day through `Clock.localDay`) and `usedTodayMillis`, charged on every tick, capped at the allowance consumed, extensions included, kept when a rule is disabled.
- Migration: additive fields with defaults; a state without a pause start (saved while open, or by the previous version) starts its pause at restore. StopScrollStoreTest decodes a previous-version payload.
- UI: `timerStatus` (engine package) maps a persisted state to READY, IN_USE, PAUSING, BLOCKED or DISABLED; `RuleTimers` renders today's usage and two Material 3 linear progress bars on each rule card, refreshed every second.
- AC1-AC3: PauseTimerTest transitions 1-4. AC4: PauseTimerTest daily usage, SessionEngineTest disable. AC5: PauseTimerTest restore, StopScrollStoreTest. AC6: TimerStatusTest, device check pending. AC7: validation above, README and docs/DEVELOPMENT.md.

# Links
- Request: `req_004_pause_and_allowance_timers_with_daily_stats`
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |
