## task_002_review_findings_android_mvp_runtime_and_rule_persistence - Review findings: Android MVP runtime and rule persistence
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 100%
> Confidence: 95%
> Progress: 85%
> Complexity: Medium
> Theme: Implementation delivery
> Reminder: Update status/understanding/confidence/progress and linked request/backlog references when you edit this doc.
> Owner: paul.mondou@live.fr
> Indicators reviewed: 2026-11-09 16:52:25

# AI Context
- Summary: Correct five runtime and persistence defects before MVP device acceptance.
- Keywords: review, findings, android, mvp, runtime, rule, persistence
- Use when: fixing foreground accounting, startup readiness, rule transactions, warnings and duration bounds.
- Skip when: claiming real-device acceptance; that remains in task_001.

# Definition of Done (DoD)
- [x] The backlog scope is implemented.
- [x] Acceptance criteria have JVM regression coverage; Android device checks remain in task_001.
- [ ] Validation passes.
- [ ] Meaningful waves followed ADR 009: affected docs updated and the repo left commit-ready without automatic commits.

# Backlog
- `item_005_review_findings_android_mvp_runtime_and_rule_persistence`

# Acceptance criteria
- AC1: A direct monitored-app-to-StopScroll transition pauses allowance consumption; returning resumes the exact remaining allowance.
- AC2: Startup events cannot overwrite persisted runtime or bypass a cooldown; a foreground target is reconciled after runtime and rules are loaded without requiring another window event.
- AC3: Overlapping rule changes preserve all independent changes in persisted state.
- AC4: A pending or visible warning cannot remain over an unrelated app or HOME after exit/expiry, and disconnect cancels pending display callbacks.
- AC5: Duration inputs that would overflow conversion or deadline calculation are rejected without changing the saved rule.
- AC6: Any selected corrections have regression evidence at the affected integration boundary, with device-dependent checks clearly distinguished from JVM engine tests.

# Priority
- Priority: High
- Rationale: Restore the core accounting and enforcement guarantees before the device wave.

# Plan
- [x] Wave 0: Promote req_001 to one bounded correction slice; retain task_001 device acceptance.
- [x] Wave 1: Implement F1/F2 foreground and serialized startup, F3 transactional edits, F4 warning ownership and F5 duration bounds with regression tests.
- [ ] Wave 2: Run offline lint, unit tests and APK assembly; record results and limitations.
- [ ] Wave 3: Validate Logics, record AC evidence and close only the correction task.

# Validation
- 2026-11-09: All main and test Kotlin files compiled directly using the cached Kotlin 2.0.21 compiler, Compose and serialization plugins, Android 35 API jar and the existing generated R jar (resources are unchanged).
- `python3 build/review-fixes-validation/verify-cached-kotlin.py` passed: JUnit 4.13.2, **35 tests, zero failures** (17 existing engine tests and 18 added tests). Output: build/review-fixes-validation/kotlin-junit.log. This is direct compiler/JUnit evidence, not Gradle lint/test/build success.
- Required command: `./gradlew --offline lintDebug testDebugUnitTest assembleDebug`, with JAVA_HOME set to the installed Temurin 17.0.20.1+1 and its bin directory prepended to PATH. It failed before configuration because the wrapper could not create gradle-8.11.1-bin.zip.lck in the read-only user cache: `java.io.FileNotFoundException: ... (Read-only file system)`.
- Retried with a writable copy of the existing Gradle caches and wrapper under /tmp, using GRADLE_USER_HOME. Gradle failed with `Could not determine a usable wildcard IP for this machine.`
- Retried without a persistent daemon and with IPv4 preferred: `./gradlew --offline --no-daemon lintDebug testDebugUnitTest assembleDebug --stacktrace`. The single-use daemon failed with `java.net.SocketException: Operation not permitted` at TcpIncomingConnector.accept / ServerSocketChannel.open. Local sockets are prohibited by the sandbox. No missing offline dependency was reported; dependency resolution was not reached.
- No corrected APK was assembled. The pre-existing app/build/outputs/apk/debug/app-debug.apk is 10,047,335 bytes, dated 2026-09-19, and does not contain these corrections. Do not install it as the fixed APK.
- No device operations, adb, installation or push were performed.

# Regression evidence
| Finding | JVM evidence | Implementation |
| --- | --- | --- |
| F1 / AC1 | SessionRuntimeTest: own activity pauses and resumes exact remaining time; transient system UI classification | ForegroundPolicy and service foreground resolution |
| F2 / AC2 | SessionRuntimeTest: early events cannot write before load, restored cooldown enforced without another event, already-open target charged, current owner read after load, new rule reconciled immediately | StoredSnapshot and SessionRuntime readiness barrier, serialized commits and rule updates |
| F3 / AC3 | StopScrollStoreTest: simultaneous disable/disable and remove/add against a real temporary DataStore preserve all independent changes and unrelated runtime | StopScrollStore.updateRules transaction and RulesViewModel mutation |
| F4 / AC4 | WarningControllerTest: exit, 1-second expiry before HOME, wrong owner, delayed expired effect, rule removal, transient window, screen lock and disconnect | WarningController ownership checks and main-thread WarningOverlay without queued show callbacks; hide removes timeout callback |
| F5 / AC5 | DurationBoundsTest: invalid/oversized input, conversion boundary, current wall-time headroom, saturating legacy cooldown and re-entry | MainActivity rejects unsafe values before save; DurationBounds and SessionEngine safe deadline |
| AC6 | 18 regression tests plus the existing 17 tests pass through direct JUnit | Android window selection, Handler/WindowManager behavior and Pixel 8 acceptance remain device checks in task_001 |

# Report
- Implementation and JVM regression validation are complete. Required Gradle validation, corrected APK generation and both commits remain blocked by the execution environment.
- The sandbox mounts .git read-only: `git add` and `git commit` fail with `Unable to create .git/index.lock: Read-only file system`. No commit was created. The initial INDEX and req_001 contents are preserved exactly as the first requested commit patch at build/review-fixes-validation/01-review-draft.patch; `git apply --check --cached` passes. Initial INDEX blob: 12d38357f933f697b8e9db58a9aeaa28c488c2cb.
- On the hub, stage that patch with `git apply --cached build/review-fixes-validation/01-review-draft.patch`, then commit `new[MD]: record Android MVP review findings`. This stages the original capture while leaving the groomed working files in place.
- Then run the required offline Gradle command outside this sandbox, record its exact result and the new APK size, and complete this correction task through Logics closeout after attaching commit-backed evidence. Commit the correction with `fix[KT/MD]: correct runtime startup rules warnings and duration bounds`.
- Leave task_001 at 85% with Wave 4 open. The hub and owner retain the Pixel 8 device checklist.

# Links
- Request: `req_001_review_findings_android_mvp_runtime_and_rule_persistence`
- Product brief(s): (none yet)
- Architecture decision(s): (none yet)

# Executions
| When | Runner | Run id | Title | State | PR | Notes |
| --- | --- | --- | --- | --- | --- | --- |
