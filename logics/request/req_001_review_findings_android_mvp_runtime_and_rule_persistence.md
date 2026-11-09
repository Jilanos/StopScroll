## req_001_review_findings_android_mvp_runtime_and_rule_persistence - Review findings: Android MVP runtime and rule persistence
> From version: 1.0.0
> Schema version: 1.0
> Status: Ready
> Understanding: 100%
> Confidence: 95%
> Complexity: Medium
> Theme: Code review
> Reminder: Update status/understanding/confidence and linked backlog/task references when you edit this doc.

# AI Context
- Summary: Five code-review findings affecting foreground accounting, service startup, rule writes, warning cleanup and duration validation.
- Keywords: review, findings, android, mvp, runtime, rule, persistence
- Use when: selecting corrective work before Android MVP device acceptance.
- Skip when: treating this review as implementation or real-device validation evidence.

# Needs
- P1: Pause the target allowance when the user switches directly to the StopScroll activity.
- P1: Gate event processing on complete runtime/rule initialization and reconcile the current foreground after initialization.
- P2: Make rule read-modify-write operations atomic so overlapping UI actions cannot overwrite one another.
- P2: Cancel pending and visible warnings on target exit and allowance expiry, and check ownership before displaying a warning.
- P2: Reject durations that overflow milliseconds or the cooldown wall-clock deadline.
- These are candidates for corrective work, not a committed implementation scope. No backlog, task or product brief is created by this review.

# Priority
- Priority: High
- Rationale: Foreground accounting and startup defects can violate the core allowance and cooldown behavior before device acceptance.

# Context
- Reviewed the entire implementation introduced after ea8e1b0 through f200e70. The worktree was clean before this review. Findings are based on source-level control flow; device reproduction remains pending.
- F1 / P1 — app/src/main/java/com/stopscroll/app/service/StopScrollAccessibilityService.kt:69-71,115-122: the resolver returns null for StopScroll's own package, and the event handler discards null. Switching directly from a monitored app to StopScroll through recent apps therefore leaves the previous engine session running. The one-second ticks can consume the remaining allowance and start a cooldown while the user edits settings. Only the accessibility overlay should be transient; the full activity must cause a foreground transition.
- F2 / P1 — app/src/main/java/com/stopscroll/app/service/StopScrollAccessibilityService.kt:52-65,69-71,83-90: restore is launched asynchronously, rule collection is launched separately, and dispatch is immediately available. An event can acquire the mutex between restore and the first rule emission; SessionEngine.onForeground returns without starting a session because its rules are empty. Loading rules does not reconcile the foreground, and onTick never starts a session, so a static target screen can remain unenforced until another window event. An event that acquires the mutex before restore can additionally persist an empty engine snapshot over the saved runtime. A mutex serializes operations but does not establish initialization readiness.
- F3 / P2 — app/src/main/java/com/stopscroll/app/ui/RulesViewModel.kt:31-32 and app/src/main/java/com/stopscroll/app/data/StopScrollStore.kt:31-36: readRules occurs outside the DataStore edit transaction. If two mutations read the same list while a previous write is pending, the second whole-list replacement loses the first change. For example, rapidly disabling two rules can leave the first enabled again. Move the transformation into the transaction, or serialize the complete read-modify-write sequence.
- F4 / P2 — app/src/main/java/com/stopscroll/app/service/StopScrollAccessibilityService.kt:69-71,96-100 and app/src/main/java/com/stopscroll/app/service/WarningOverlay.kt:27-55: exit and expiry never hide the warning; only destruction, another show or the four-second timeout hides it. Leaving just after the warning shows leaves it over another app; a one-second allowance leaves it over HOME after enforcement. ShowWarning.packageName is discarded, so a delayed effect can also display after ownership changed. This contradicts the cleanup requirement in item_003's scope.
- F5 / P2 — app/src/main/java/com/stopscroll/app/ui/MainActivity.kt:191-193,229-230 and app/src/main/java/com/stopscroll/app/engine/SessionEngine.kt:111: any positive Long seconds value is accepted before unchecked multiplication by 1000. For example, 9223372036854776 seconds becomes -9223372036854775616 milliseconds. A usage limit then expires immediately, while a negative cooldown can expire immediately on re-entry. A multiplication-safe upper boundary also needs to leave room for adding the current wall time to a cooldown.
- Existing known work was checked with Logics status, health and bounded searches. Device acceptance is already explicitly pending in task_001; it is not presented as a new finding here.
- Validation: the initial Gradle invocation failed because JAVA_HOME was unset and java was absent from PATH. With JAVA_HOME pointing to the installed Temurin 17.0.20.1+1 JDK, `./gradlew lintDebug testDebugUnitTest assembleDebug` succeeded (51 actionable tasks: 1 executed, 50 up-to-date). The existing test report records 17 engine tests, zero failures and zero errors; it contains no Android adapter or persistence-concurrency tests. This was an incremental validation, not a clean build.
- Validation: `adb devices -l` returned no connected devices. The installed `rtk` is a release-management CLI and does not support shell-command wrappers, so validation used raw commands.

# Acceptance criteria
- AC1: A direct monitored-app-to-StopScroll transition pauses allowance consumption; returning resumes the exact remaining allowance.
- AC2: Startup events cannot overwrite persisted runtime or bypass a cooldown; a foreground target is reconciled after runtime and rules are loaded without requiring another window event.
- AC3: Overlapping rule changes preserve all independent changes in persisted state.
- AC4: A pending or visible warning cannot remain over an unrelated app or HOME after exit/expiry, and disconnect cancels pending display callbacks.
- AC5: Duration inputs that would overflow conversion or deadline calculation are rejected without changing the saved rule.
- AC6: Any selected corrections have regression evidence at the affected integration boundary, with device-dependent checks clearly distinguished from JVM engine tests.

# Definition of Ready (DoR)
- [x] Problem statement is explicit and user impact is clear.
- [x] Scope boundaries (in/out) are explicit: capture only; no implementation or delivery closure.
- [x] Acceptance criteria are testable.
- [x] Dependencies and known risks are listed: Android integration verification requires a device or suitable test harness; JVM engine coverage does not establish adapter correctness.

# Companion docs
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)

# References
- app/src/main/java/com/stopscroll/app/service/StopScrollAccessibilityService.kt
- app/src/main/java/com/stopscroll/app/service/WarningOverlay.kt
- app/src/main/java/com/stopscroll/app/ui/RulesViewModel.kt
- app/src/main/java/com/stopscroll/app/ui/MainActivity.kt
- app/src/main/java/com/stopscroll/app/data/StopScrollStore.kt
- app/src/main/java/com/stopscroll/app/engine/SessionEngine.kt
- app/src/test/java/com/stopscroll/app/engine/SessionEngineTest.kt
- logics/tasks/task_001_deliver_stopscroll_android_mvp_apk.md

# Backlog
- `item_005_review_findings_android_mvp_runtime_and_rule_persistence`

# Delivery selection
- 2026-11-09: The owner selected all five findings for immediate correction. One backlog slice and task own F1-F5, regression tests and build evidence. The initial review capture is preserved in build/review-fixes-validation/01-review-draft.patch for the first requested commit because the sandbox mounts .git read-only.
- No device or installation work is authorized in this correction run. Device acceptance stays in task_001.
