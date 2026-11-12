## req_004_pause_and_allowance_timers_with_daily_stats - Pause and allowance timers per app with daily stats screen
> From version: 1.0.0
> Schema version: 1.0
> Status: Ready
> Understanding: 100%
> Confidence: 90%
> Complexity: Medium
> Theme: Runtime enforcement and UI
> Reminder: Update status/understanding/confidence and linked backlog/task references when you edit this doc.

# AI Context
- Summary: Per app, an allowance timer runs while the app is open and a pause timer runs while it is closed; a full pause away from the app refills the allowance. The main screen shows today's usage and both timers.
- Keywords: pause, allowance, detente, timers, reset, daily usage, stats, progress
- Use when: changing how the allowance refills, the pause away from an app, daily usage accounting or the main screen timers.
- Skip when: changing the time-up screen, the +30 s rule or the warning pill.

# Needs
- Owner request (French): "une fois l'app bloquée ouverte, le timer de XX s se met en pause et n'est jamais réinitialisé sans blocage." The retained allowance only refills after a full block, so short sessions spread over a day end in a block even with long breaks between them.
- Per blocked app, two timers: the allowance timer (temps de détente, allowed use) and the pause timer (time away from the app needed to refill the allowance).
- App open: the allowance timer runs, the pause timer is stopped.
- App closed: the allowance timer stops, the pause timer runs.
- App reopened before the pause is reached: the pause timer goes back to zero and the allowance resumes where it was.
- Pause reached: the allowance is refilled.
- On opening StopScroll, show for each blocked app today's usage and the current state of both timers (progress bars consistent with the existing Material 3 interface).

# Priority
- Priority: High
- Rationale: The current rule contradicts the intended use (a real break should refill the allowance); it is the owner's main complaint on the device.

# Context
- Session model so far: retained per-app allowance, a wall-clock block at exhaustion, +30 s doubling the next block (req_000, req_002).
- The pause required away from the app is the rule's "Durée de la pause", or the pending doubled duration after a +30 s; reaching it counts as a completed block and clears the doubling.
- The pause timer uses wall time like the block, so it keeps running across a reboot. Time when the service was not observing an open app is neither charged to the allowance nor credited to the pause.
- Stored runtime state gains optional fields with defaults; states saved by the previous version still load.

# Acceptance criteria
- AC1: Engine: while the app is in the foreground the allowance decreases and no pause accrues; after leaving, the allowance is frozen and the pause accrues.
- AC2: Engine: reopening before the pause duration resets the pause and resumes the exact remaining allowance.
- AC3: Engine: once the pause duration is reached away from the app, the allowance is full again, the warning is re-armed and a pending doubled block is cleared.
- AC4: Today's usage is tracked per app by local day, survives restarts, and is kept when a rule is disabled.
- AC5: Persistence: states saved by the previous version decode and behave conservatively (a state saved while running starts its pause at restore).
- AC6: The main screen shows, per enabled app, today's usage, an allowance bar and a pause bar (or the running block), updated every second.
- AC7: Fake-clock JVM tests cover the four transitions; `./gradlew test assembleDebug` passes; README and development guide describe the rule.

# Definition of Ready (DoR)
- [x] Problem statement is explicit and user impact is clear.
- [x] Scope boundaries (in/out) are explicit: no change to the time-up screen, the warning or the +30 s doubling rule beyond clearing it after a full pause.
- [x] Acceptance criteria are testable.
- [x] Dependencies and known risks are listed: wall-clock changes shift the pause like the block; device check needed for the main screen refresh.

# Companion docs
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)

# References
- app/src/main/java/com/stopscroll/app/engine/SessionEngine.kt
- app/src/main/java/com/stopscroll/app/engine/Model.kt
- app/src/main/java/com/stopscroll/app/service/SessionRuntime.kt
- app/src/main/java/com/stopscroll/app/ui/RulesScreen.kt

# Backlog
- `item_008_pause_and_allowance_timers_per_app_with_daily_stats_screen`

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |
