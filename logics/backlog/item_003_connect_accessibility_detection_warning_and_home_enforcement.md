## item_003_connect_accessibility_detection_warning_and_home_enforcement - Connect accessibility detection warning and HOME enforcement
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 90%
> Confidence: 85%
> Progress: 70%
> Complexity: High
> Theme: Android accessibility
> Reminder: Update status/understanding/confidence/progress and linked request/task references when you edit this doc.
> Indicators reviewed: 2026-09-19 17:49:22

# AI Context
- Summary: Translate package transitions into engine events and return HOME without ejecting unrelated apps..
- Keywords: connect, accessibility, detection, warning, home, enforcement
- Use when: implementing accessibility, warnings and activation status.
- Skip when: changing build tools or distribution.

# Problem
- The state engine needs reliable app transition input and Android warning/HOME actions.

# Scope
- In:
  - Declare a user-enabled AccessibilityService, observe relevant foreground/window events across packages and deduplicate activity transitions.
  - Treat keyboard, notification shade, permission dialogs and the service warning as transient system UI, not fresh openings. Define screen-lock behavior from the chosen session policy.
  - Show a short non-focusable non-touchable accessibility warning overlay at the warning boundary and clean it up on target exit, expiry or disconnect.
  - Use performGlobalAction(GLOBAL_ACTION_HOME); check its result, avoid unbounded retry loops and expose failures honestly.
  - On disconnect cancel callbacks/overlays and on reconnection reconcile persisted state before accepting a target entry. Do not navigate HOME from a stale callback while another app is foreground.
  - Show accessibility state and an activation link in Compose. Minimize capabilities; avoid reading view text or requesting window-content retrieval unless foreground identification proves it necessary.
- Out:
  - Force stopping applications, disabling user controls and requesting unnecessary permissions.

# Acceptance criteria
- AC1: On the validation device Instagram starts one session, warns once and returns HOME on expiry within the documented timing tolerance.
- AC2: Reopening Instagram during cooldown returns HOME on its first confirmed event without extending the cooldown, while other apps remain usable.
- AC3: Internal navigation and transient UI do not restart the timer; permission revocation/disconnect removes overlays and the UI reports that protection is off.

# AC Traceability
- request-AC3 -> This backlog slice. Proof: Pending implementation: verify pause/resume with 2 of 5 minutes consumed, duplicate events and lock/unlock.
- request-AC4 -> This backlog slice. Proof: Pending implementation: verify one warning at 30 seconds remaining, short limits and resumed sessions.
- request-AC5 -> This backlog slice. Proof: Pending implementation: verify persisted expiry transition and HOME only for the current target.
- request-AC6 -> This backlog slice. Proof: Pending implementation: verify immediate cooldown re-entry rejection, fixed deadline and package independence.
- request-AC8 -> This backlog slice. Proof: Pending implementation: verify service activation/status and minimal package-level access.

# Decision framing
- Product framing: Not needed
- Architecture framing: Not needed

# Links
- Product brief(s): `prod_001_stopscroll_minimal_android_self_control_app`
- Architecture decision(s): (none yet)
- Request: `req_000_stopscroll_android_mvp_for_per_opening_app_limits`
- Primary task(s): `task_001_deliver_stopscroll_android_mvp_apk`

# Priority
- Priority: High — delivers the actual Android interruption loop.
- Rationale: Set by scaffold input or defaulted for grooming.
