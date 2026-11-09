## task_001_deliver_stopscroll_android_mvp_apk - Deliver StopScroll Android MVP APK
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 95%
> Confidence: 90%
> Progress: 85%
> Complexity: High
> Theme: Implementation delivery
> Reminder: Update status/understanding/confidence/progress and linked request/backlog references when you edit this doc.
> Indicators reviewed: 2026-11-09 16:59:44
> Owner: paul.mondou@circle-mobility.com

# AI Context
- Summary: Deliver four slices in dependency order with confirmed pause/resume semantics and real-device acceptance..
- Keywords: deliver, stopscroll, android, mvp, apk
- Use when: implementing the MVP after reading the decision register.
- Skip when: closing delivery when only documentation exists.

# Context
- Deliver a Kotlin/Compose APK with retained foreground allowance and per-package cooldowns, validated against Instagram on the Pixel 8. Read logics/scaffold/android-mvp-decisions.md for confirmed choices and implementation defaults.

# Priority
- Priority: High
- Rationale: This is the first functional MVP delivery; implement its four dependent slices before optional product expansion.

# Plan
- [x] 1. Wave 0: Read confirmed resume-remaining-time decisions in logics/scaffold/android-mvp-decisions.md. Retain 5/10-minute defaults unless the user specifies alternatives. Inspect Android/JDK/SDK and Pixel 8 access; record exact installed Android version for validation.
- [x] 2. Wave 1: Implement the Android foundation and persistent rule editor; initialize source-locale fr i18n only when implementing user-facing copy. Verify a debug APK build and configuration persistence.
- [x] 3. Wave 2: Implement and test the pure Kotlin session/cooldown engine with fake clocks, serialized transitions and recovery semantics.
- [x] 4. Wave 3: Connect AccessibilityService, overlay warning, HOME actions and service status UI; verify foreground ownership and cleanup.
- [ ] 5. Wave 4: Execute the Instagram/device checklist, document build/install/permissions, record evidence and limitations, then use Logics closeout only when acceptance criteria are met.
- [x] ADR 009 checkpoint: update affected Logics docs during each meaningful wave and leave the repo commit-ready.
- [x] Keep commit creation under operator control; do not force one commit per micro-step.
- [ ] GATE: do not close until lint, audit, and scaffold validation pass.

# Backlog
- `item_001_bootstrap_android_app_and_persistent_app_rules`
- `item_002_implement_deterministic_session_and_cooldown_engine`
- `item_003_connect_accessibility_detection_warning_and_home_enforcement`
- `item_004_validate_apk_and_document_installation_and_permissions`

# Definition of Done (DoD)
- [x] A clean checkout builds the installable APK and the app implements the complete request acceptance contract.
- [ ] Engine tests and recorded Pixel 8 Instagram scenarios verify pause/resume, warning, HOME, cooldown and restart persistence.
- [x] Android build/lint/tests and Logics validation pass; README build, install and permission instructions match the delivered APK.
- [x] Meaningful waves followed ADR 009: affected docs updated and the repo left commit-ready without automatic commits.

# AC Traceability
- request-AC1 -> `item_001_bootstrap_android_app_and_persistent_app_rules`. Proof deferred to slice closeout.
- request-AC2 -> `item_001_bootstrap_android_app_and_persistent_app_rules`. Proof deferred to slice closeout.
- request-AC3 -> `item_002_implement_deterministic_session_and_cooldown_engine`. Proof deferred to slice closeout.
- request-AC4 -> `item_002_implement_deterministic_session_and_cooldown_engine`. Proof deferred to slice closeout.
- request-AC5 -> `item_002_implement_deterministic_session_and_cooldown_engine`. Proof deferred to slice closeout.
- request-AC6 -> `item_002_implement_deterministic_session_and_cooldown_engine`. Proof deferred to slice closeout.
- request-AC7 -> `item_002_implement_deterministic_session_and_cooldown_engine`. Proof deferred to slice closeout.
- request-AC3 -> `item_003_connect_accessibility_detection_warning_and_home_enforcement`. Proof deferred to slice closeout.
- request-AC4 -> `item_003_connect_accessibility_detection_warning_and_home_enforcement`. Proof deferred to slice closeout.
- request-AC5 -> `item_003_connect_accessibility_detection_warning_and_home_enforcement`. Proof deferred to slice closeout.
- request-AC6 -> `item_003_connect_accessibility_detection_warning_and_home_enforcement`. Proof deferred to slice closeout.
- request-AC8 -> `item_003_connect_accessibility_detection_warning_and_home_enforcement`. Proof deferred to slice closeout.
- request-AC1 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC2 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC3 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC4 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC5 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC6 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC7 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC8 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC9 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.
- request-AC10 -> `item_004_validate_apk_and_document_installation_and_permissions`. Proof deferred to slice closeout.

# Validation
- Toolchain recorded (Wave 0): Temurin JDK 17.0.20.1+1, Android SDK platform android-35, build-tools 35.0.0, platform-tools 37.0.1, Gradle 8.11.1, AGP 8.7.3, Kotlin 2.0.21. No Android device was reachable from the build environment, so no exact Pixel 8 OS/API/build could be recorded.
- `./gradlew lintDebug testDebugUnitTest assembleDebug` passed on a clean `git clone` of commit 36dd29e, with only JAVA_HOME and ANDROID_HOME set and no local.properties. APK produced at app/build/outputs/apk/debug/app-debug.apk.
- 17 fake-clock engine tests pass: duplicate events, non-selected apps, pause on leaving and on screen lock, warning boundary for long and short sessions, no re-warning after resume, expiry with and without the target in the foreground, cooldown re-entry and non-extension, cooldown expiry, package independence, restart recovery, reboot recovery, rule disable, duration change.
- `logics-manager flow validate task_001_deliver_stopscroll_android_mvp_apk`, `logics-manager lint` and `logics-manager audit` are clear.
- NOT executed: the real-device Instagram checklist (request-AC9 device half, and the device half of AC3-AC8). The checklist is written in README.md but no device was available. No real-device success is claimed.

# Report
- Waves 0-4 implemented in commits f1761ad, 4fd5a34 and 36dd29e: Gradle/Compose foundation with DataStore-backed rule editor, pure Kotlin session engine with an injected clock, AccessibilityService adapter with accessibility overlay warning and GLOBAL_ACTION_HOME enforcement, and the README build/install/permission documentation.
- The i18n contract is recorded as not applicable: the app is single-locale French and its copy lives in Android string resources, which the JSON `catalog.path` contract cannot address.
- Remaining before closeout: run the README device acceptance checklist on the Pixel 8 and record the exact OS version, API level and build number.

# Links
- Request: `req_000_stopscroll_android_mvp_for_per_opening_app_limits`
- Product brief(s): `prod_001_stopscroll_minimal_android_self_control_app`
- Architecture decision(s): (none yet)

# Review correction checkpoint
- 2026-11-09 checkpoint: F1-F5 corrections are implemented under task_002 with extracted runtime/warning policies, atomic DataStore edits and safe duration bounds. All main/test Kotlin sources compile with cached Kotlin 2.0.21 and Compose/serialization plugins; direct JUnit evidence is recorded in task_002. The required offline Gradle build is blocked by sandbox cache writes and daemon sockets, and no corrected APK has been produced. Wave 4 and the Pixel 8 checklist remain open at 85%; the hub must build the corrected APK before installation. README adds direct-to-StopScroll, warning cleanup and startup reconciliation scenarios.
