## item_005_review_findings_android_mvp_runtime_and_rule_persistence - Review findings: Android MVP runtime and rule persistence
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 100%
> Confidence: 90%
> Progress: 85%
> Complexity: High
> Theme: Operator workflow and runtime integration
> Reminder: Update status/understanding/confidence/progress and linked request/task references when you edit this doc.
> Indicators reviewed: 2026-11-09 16:52:25

# AI Context
- Summary: Correct five runtime and persistence defects before MVP device acceptance.
- Keywords: review, findings, android, mvp, runtime, rule, persistence
- Use when: fixing foreground accounting, startup readiness, rule transactions, warnings and duration bounds.
- Skip when: claiming real-device acceptance; that remains in task_001.

# Problem
P1: Pause the target allowance when the user switches directly to the StopScroll activity.
P1: Gate event processing on complete runtime/rule initialization and reconcile the current foreground after initialization.
P2: Make rule read-modify-write operations atomic so overlapping UI actions cannot overwrite one another.
P2: Cancel pending and visible warnings on target exit and allowance expiry, and check ownership before displaying a warning.
P2: Reject durations that overflow milliseconds or the cooldown wall-clock deadline.
The owner selected all five findings for immediate correction. Device acceptance remains outside this slice.

# Scope
- In:
  - F1-F5 corrections, JVM regression tests, offline lint/build and debug APK evidence
- Out:
  - Device installation, adb, Pixel 8 acceptance, publishing and unrelated feature changes

# Acceptance criteria
- AC1: A direct monitored-app-to-StopScroll transition pauses allowance consumption; returning resumes the exact remaining allowance.
- AC2: Startup events cannot overwrite persisted runtime or bypass a cooldown; a foreground target is reconciled after runtime and rules are loaded without requiring another window event.
- AC3: Overlapping rule changes preserve all independent changes in persisted state.
- AC4: A pending or visible warning cannot remain over an unrelated app or HOME after exit/expiry, and disconnect cancels pending display callbacks.
- AC5: Duration inputs that would overflow conversion or deadline calculation are rejected without changing the saved rule.
- AC6: Any selected corrections have regression evidence at the affected integration boundary, with device-dependent checks clearly distinguished from JVM engine tests.

# AC Traceability
- request-AC1 -> This backlog slice. Proof: AC1: A direct monitored-app-to-StopScroll transition pauses allowance consumption; returning resumes the exact remaining allowance.
- request-AC2 -> This backlog slice. Proof: AC2: Startup events cannot overwrite persisted runtime or bypass a cooldown; a foreground target is reconciled after runtime and rules are loaded without requiring another window event.
- request-AC3 -> This backlog slice. Proof: AC3: Overlapping rule changes preserve all independent changes in persisted state.
- request-AC4 -> This backlog slice. Proof: AC4: A pending or visible warning cannot remain over an unrelated app or HOME after exit/expiry, and disconnect cancels pending display callbacks.
- request-AC5 -> This backlog slice. Proof: AC5: Duration inputs that would overflow conversion or deadline calculation are rejected without changing the saved rule.
- request-AC6 -> This backlog slice. Proof: AC6: Any selected corrections have regression evidence at the affected integration boundary, with device-dependent checks clearly distinguished from JVM engine tests.

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
- Request: `logics/request/req_001_review_findings_android_mvp_runtime_and_rule_persistence.md`
- Primary task(s): (none yet)

# Priority
- Priority: High
- Rationale: These defects break retained allowance and enforcement; fix before device acceptance.

# Notes
- Hybrid rationale: Derived from request `req_001_review_findings_android_mvp_runtime_and_rule_persistence` and kept bounded to one coherent delivery slice.
- Source file: `logics/request/req_001_review_findings_android_mvp_runtime_and_rule_persistence.md`.
- Generated locally by logics-manager.

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |


# Tasks
- `task_002_review_findings_android_mvp_runtime_and_rule_persistence`
