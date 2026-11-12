## item_008_pause_and_allowance_timers_per_app_with_daily_stats_screen - Pause and allowance timers per app with daily stats screen
> From version: 1.0.0
> Schema version: 1.0
> Status: Done
> Understanding: 90%
> Confidence: 85%
> Progress: 100%
> Complexity: High
> Theme: Operator workflow and runtime integration
> Reminder: Update status/understanding/confidence/progress and linked request/task references when you edit this doc.
> Indicators reviewed: 2026-11-12 12:17:51

# AI Context
- Summary: Allowance timer while the app is open, pause timer while it is closed; a full pause refills the allowance. Main screen shows today's usage and both timers.
- Keywords: pause, allowance, detente, timers, reset, daily usage, stats, progress
- Use when: changing how the allowance refills, the pause away from an app, daily usage accounting or the main screen timers.
- Skip when: changing the time-up screen, the +30 s rule or the warning pill.

# Problem
Owner request (French): "une fois l'app bloquée ouverte, le timer de XX s se met en pause et n'est jamais réinitialisé sans blocage." The retained allowance only refills after a full block, so short sessions spread over a day end in a block even with long breaks between them.
Per blocked app, two timers: the allowance timer (temps de détente, allowed use) and the pause timer (time away from the app needed to refill the allowance).
App open: the allowance timer runs, the pause timer is stopped.
App closed: the allowance timer stops, the pause timer runs.
App reopened before the pause is reached: the pause timer goes back to zero and the allowance resumes where it was.
Pause reached: the allowance is refilled.
On opening StopScroll, show for each blocked app today's usage and the current state of both timers (progress bars consistent with the existing Material 3 interface).

# Scope
- In:
  - SessionEngine pause timer, refill rule and daily usage accounting, with fake-clock tests
  - additive PackageRuntimeState fields and the decoding of states saved by the previous version
  - per-app timers and today's usage on the rule cards of the main screen
  - README and development guide
- Out:
  - time-up screen, warning pill and +30 s doubling (unchanged apart from clearing the doubling after a full pause)
  - usage history beyond the current day

# Acceptance criteria
- AC1: Engine: while the app is in the foreground the allowance decreases and no pause accrues; after leaving, the allowance is frozen and the pause accrues.
- AC2: Engine: reopening before the pause duration resets the pause and resumes the exact remaining allowance.
- AC3: Engine: once the pause duration is reached away from the app, the allowance is full again, the warning is re-armed and a pending doubled block is cleared.
- AC4: Today's usage is tracked per app by local day, survives restarts, and is kept when a rule is disabled.
- AC5: Persistence: states saved by the previous version decode and behave conservatively (a state saved while running starts its pause at restore).
- AC6: The main screen shows, per enabled app, today's usage, an allowance bar and a pause bar (or the running block), updated every second.
- AC7: Fake-clock JVM tests cover the four transitions; `./gradlew test assembleDebug` passes; README and development guide describe the rule.

# AC Traceability
- request-AC1 -> This backlog slice. Proof: AC1: Engine: while the app is in the foreground the allowance decreases and no pause accrues; after leaving, the allowance is frozen and the pause accrues.
- request-AC2 -> This backlog slice. Proof: AC2: Engine: reopening before the pause duration resets the pause and resumes the exact remaining allowance.
- request-AC3 -> This backlog slice. Proof: AC3: Engine: once the pause duration is reached away from the app, the allowance is full again, the warning is re-armed and a pending doubled block is cleared.
- request-AC4 -> This backlog slice. Proof: AC4: Today's usage is tracked per app by local day, survives restarts, and is kept when a rule is disabled.
- request-AC5 -> This backlog slice. Proof: AC5: Persistence: states saved by the previous version decode and behave conservatively (a state saved while running starts its pause at restore).
- request-AC6 -> This backlog slice. Proof: AC6: The main screen shows, per enabled app, today's usage, an allowance bar and a pause bar (or the running block), updated every second.
- request-AC7 -> This backlog slice. Proof: AC7: Fake-clock JVM tests cover the four transitions; `./gradlew test assembleDebug` passes; README and development guide describe the rule.

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
- Request: `logics/request/req_004_pause_and_allowance_timers_with_daily_stats.md`
- Primary task(s): (none yet)

# Priority
- Priority: High
- Rationale: The current rule contradicts the intended use and is the owner's main complaint on the device.

# Notes
- Hybrid rationale: Derived from request `req_004_pause_and_allowance_timers_with_daily_stats` and kept bounded to one coherent delivery slice.
- Source file: `logics/request/req_004_pause_and_allowance_timers_with_daily_stats.md`.
- Generated locally by logics-manager.
- Task `task_005_pause_and_allowance_timers_per_app_with_daily_stats_screen` was finished via `logics-manager flow finish task` on 2026-11-12.

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |


# Tasks
- `task_005_pause_and_allowance_timers_per_app_with_daily_stats_screen`
