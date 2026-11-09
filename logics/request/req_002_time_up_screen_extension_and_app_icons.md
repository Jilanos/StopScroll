## req_002_time_up_screen_extension_and_app_icons - Time-up screen with +30 s extension and app icons
> From version: 1.0.0
> Schema version: 1.0
> Status: Ready
> Understanding: 100%
> Confidence: 90%
> Complexity: Medium
> Theme: Runtime enforcement and UI
> Reminder: Update status/understanding/confidence and linked backlog/task references when you edit this doc.

# AI Context
- Summary: Replace HOME at allowance expiry with a StopScroll time-up screen offering +30 s against a doubled block, and show app icons in app lists.
- Keywords: time, screen, extension, app, icons, cooldown, doubling
- Use when: changing expiry presentation, the extension rule or app list rendering.
- Skip when: changing cooldown re-entry enforcement or warning behavior.

# Needs
- Owner request (French): "quand l'appli se ferme affiche une page de l'app stop scroll qui permet d'avoir +30s pour un doublage du temps off, si tu peux également rajouter les logos des apps à coté des noms".
- At allowance expiry, open a StopScroll screen instead of plain HOME. It shows the app name and icon, says time is up and offers "+30 s" and "OK".
- "+30 s" grants 30 seconds of use now; in exchange the following block for that app is doubled. Each further grant doubles again, bounded by the existing duration bounds.
- "OK" and back go to the launcher with the normal block.
- Re-entry during a block keeps the current HOME behavior.
- Show each app's launcher icon next to its name in the app picker and the rule list.

# Priority
- Priority: Medium
- Rationale: Owner feature request on a working MVP; device acceptance of the MVP stays in task_001.

# Context
- Session model: retained per-app allowance, then a wall-clock block (cooldown) during which entries are sent HOME (req_000, item_005).
- The engine is pure Kotlin with fake-clock tests; runtime state is persisted through SessionRuntime after every cycle.
- Package visibility already declares a launcher `<queries>` intent; icons need no new permission.

# Acceptance criteria
- AC1: At expiry with the target in the foreground, the time-up screen is shown with the app icon, name and block duration; OK and back go HOME.
- AC2: "+30 s" cancels the running block, grants 30 seconds, relaunches the app and doubles the next block; repeated grants double again, capped by the duration bounds.
- AC3: The doubled duration and the extension allowance are persisted and survive a restart; a block that runs to its end resets the next one to the rule duration.
- AC4: Re-entry during a block still sends HOME without an extension.
- AC5: The app picker and rule list show launcher icons, decoded off the main thread and cached.
- AC6: The rule is documented in the README; JVM tests cover the engine rule; lint, unit tests and debug assembly pass.

# Definition of Ready (DoR)
- [x] Problem statement is explicit and user impact is clear.
- [x] Scope boundaries (in/out) are explicit.
- [x] Acceptance criteria are testable.
- [x] Dependencies and known risks are listed: launching an activity from the accessibility service relies on the background-activity-start exemption for bound accessibility services; device behavior remains to be checked on the Pixel 8.

# Companion docs
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)

# References
- app/src/main/java/com/stopscroll/app/engine/SessionEngine.kt
- app/src/main/java/com/stopscroll/app/service/StopScrollAccessibilityService.kt
- app/src/main/java/com/stopscroll/app/ui/MainActivity.kt

# Backlog
- `item_006_time_up_screen_with_30_s_extension_and_app_icons`

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |
