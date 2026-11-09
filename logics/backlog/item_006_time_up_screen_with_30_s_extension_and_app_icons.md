## item_006_time_up_screen_with_30_s_extension_and_app_icons - Time-up screen with +30 s extension and app icons
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 100%
> Confidence: 90%
> Progress: 90%
> Complexity: Medium
> Theme: Operator workflow and runtime integration
> Reminder: Update status/understanding/confidence/progress and linked request/task references when you edit this doc.
> Indicators reviewed: 2026-11-09 20:07:06

# AI Context
- Summary: Time-up screen at expiry with a +30 s extension that doubles the next block, and app icons in app lists.
- Keywords: time, screen, extension, app, icons, cooldown, doubling
- Use when: changing expiry presentation, the extension rule or app list rendering.
- Skip when: changing cooldown re-entry enforcement or warning behavior.

# Problem
Owner request (French): "quand l'appli se ferme affiche une page de l'app stop scroll qui permet d'avoir +30s pour un doublage du temps off, si tu peux également rajouter les logos des apps à coté des noms".
At allowance expiry, open a StopScroll screen instead of plain HOME. It shows the app name and icon, says time is up and offers "+30 s" and "OK".
"+30 s" grants 30 seconds of use now; in exchange the following block for that app is doubled. Each further grant doubles again, bounded by the existing duration bounds.
"OK" and back go to the launcher with the normal block.
Re-entry during a block keeps the current HOME behavior.
Show each app's launcher icon next to its name in the app picker and the rule list.

# Scope
- In:
  - Engine extension rule (grant, doubling, cap, reset) with persisted state and JVM tests
  - TimeUpActivity started by the accessibility service at expiry, in-process extension request channel, target relaunch
  - Launcher icons in the rule list and app picker, README rule documentation
- Out:
  - Time-up screen on cooldown re-entry (HOME kept), device acceptance (task_001 checklist)

# Acceptance criteria
- AC1: At expiry with the target in the foreground, the time-up screen is shown with the app icon, name and block duration; OK and back go HOME.
- AC2: "+30 s" cancels the running block, grants 30 seconds, relaunches the app and doubles the next block; repeated grants double again, capped by the duration bounds.
- AC3: The doubled duration and the extension allowance are persisted and survive a restart; a block that runs to its end resets the next one to the rule duration.
- AC4: Re-entry during a block still sends HOME without an extension.
- AC5: The app picker and rule list show launcher icons, decoded off the main thread and cached.
- AC6: The rule is documented in the README; JVM tests cover the engine rule; lint, unit tests and debug assembly pass.

# AC Traceability
- request-AC1 -> This backlog slice. Proof: AC1: At expiry with the target in the foreground, the time-up screen is shown with the app icon, name and block duration; OK and back go HOME.
- request-AC2 -> This backlog slice. Proof: AC2: "+30 s" cancels the running block, grants 30 seconds, relaunches the app and doubles the next block; repeated grants double again, capped by the duration bounds.
- request-AC3 -> This backlog slice. Proof: AC3: The doubled duration and the extension allowance are persisted and survive a restart; a block that runs to its end resets the next one to the rule duration.
- request-AC4 -> This backlog slice. Proof: AC4: Re-entry during a block still sends HOME without an extension.
- request-AC5 -> This backlog slice. Proof: AC5: The app picker and rule list show launcher icons, decoded off the main thread and cached.
- request-AC6 -> This backlog slice. Proof: AC6: The rule is documented in the README; JVM tests cover the engine rule; lint, unit tests and debug assembly pass.

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
- Request: `logics/request/req_002_time_up_screen_extension_and_app_icons.md`
- Primary task(s): (none yet)

# Priority
- Priority: Medium
- Rationale: Owner feature request on a working MVP; device acceptance of the MVP stays in task_001.

# Notes
- Hybrid rationale: Derived from request `req_002_time_up_screen_extension_and_app_icons` and kept bounded to one coherent delivery slice.
- Source file: `logics/request/req_002_time_up_screen_extension_and_app_icons.md`.
- Generated locally by logics-manager.

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |


# Tasks
- `task_003_time_up_screen_with_30_s_extension_and_app_icons`
