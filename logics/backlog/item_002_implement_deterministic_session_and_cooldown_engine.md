## item_002_implement_deterministic_session_and_cooldown_engine - Implement deterministic session and cooldown engine
> From version: 1.0.0
> Schema version: 1.0
> Status: Ready
> Understanding: 90%
> Confidence: 85%
> Progress: 0%
> Complexity: High
> Theme: Session enforcement
> Reminder: Update status/understanding/confidence/progress and linked request/task references when you edit this doc.
> Indicators reviewed: 2026-09-19 16:20:06

# AI Context
- Summary: Pause and resume remaining usage; only exhaustion starts cooldown. Test clock and recovery behavior without Android..
- Keywords: implement, deterministic, session, cooldown, engine
- Use when: implementing timing transitions or fake-clock tests.
- Skip when: building overlays or package selection UI.

# Problem
- Timers must remain correct across repeated events, departures and recovery without blocking the wrong app.

# Scope
- In:
  - Implement a pure Kotlin state machine with injected clock and serialized events; model foreground ownership, session deadline, warning sent and per-package cooldown.
  - Implement confirmed resume policy: pause remaining foreground allowance on ordinary app exit or lock; resume on entry; only exhausted allowance starts cooldown. Test the 5-minute allowance / 2-minute use / 10-minute absence / 3-minute remaining example.
  - Use monotonic deadlines within a boot. Persist remaining usage and warning state on exit/disconnect with at most 1-second active checkpoint intervals; restore checkpoints after abrupt loss without charging unobserved time. Persist cooldown independently and use bounded wall-clock recovery across reboot; document manual clock-change limitations.
  - Persist cooldown before HOME so failed HOME actions cannot grant a new session. Retrying entry must not move the cooldown deadline.
  - Apply changes to limits on the next session; disabling a rule stops enforcement and clears its session/cooldown under the proposed self-control policy.
- Out:
  - OS integration and tamper-resistant timekeeping.

# Acceptance criteria
- AC1: Fake-clock tests prove duplicate foreground events do not reset deadlines and warning occurs at most once per session.
- AC2: Tests cover exact expiry, no ejection of unrelated apps, per-package cooldown, repeated blocked openings, cooldown expiry and the approved exit/reopen policy.
- AC3: Restart/reboot recovery and rule mutation behavior are specified and tested; no unnecessary daily quota logic is introduced.

# AC Traceability
- request-AC3 -> This backlog slice. Proof: Pending implementation: verify pause/resume with 2 of 5 minutes consumed, duplicate events and lock/unlock.
- request-AC4 -> This backlog slice. Proof: Pending implementation: verify one warning at 30 seconds remaining, short limits and resumed sessions.
- request-AC5 -> This backlog slice. Proof: Pending implementation: verify persisted expiry transition and HOME only for the current target.
- request-AC6 -> This backlog slice. Proof: Pending implementation: verify immediate cooldown re-entry rejection, fixed deadline and package independence.
- request-AC7 -> This backlog slice. Proof: Pending implementation: verify retained usage, warning and cooldown state across restart and reboot.

# Decision framing
- Product framing: Not needed
- Architecture framing: Not needed

# Links
- Product brief(s): `prod_001_stopscroll_minimal_android_self_control_app`
- Architecture decision(s): (none yet)
- Request: `req_000_stopscroll_android_mvp_for_per_opening_app_limits`
- Primary task(s): `task_001_deliver_stopscroll_android_mvp_apk`

# Priority
- Priority: High — defines the core behavior independently of Android events.
- Rationale: Set by scaffold input or defaulted for grooming.
