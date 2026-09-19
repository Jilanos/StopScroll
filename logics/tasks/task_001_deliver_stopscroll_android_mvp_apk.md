## task_001_deliver_stopscroll_android_mvp_apk - Deliver StopScroll Android MVP APK
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 90%
> Confidence: 85%
> Progress: 70%
> Complexity: High
> Theme: Implementation delivery
> Reminder: Update status/understanding/confidence/progress and linked request/backlog references when you edit this doc.
> Indicators reviewed: 2026-09-19 17:49:22
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
- [ ] 1. Wave 0: Read confirmed resume-remaining-time decisions in logics/scaffold/android-mvp-decisions.md. Retain 5/10-minute defaults unless the user specifies alternatives. Inspect Android/JDK/SDK and Pixel 8 access; record exact installed Android version for validation.
- [ ] 2. Wave 1: Implement the Android foundation and persistent rule editor; initialize source-locale fr i18n only when implementing user-facing copy. Verify a debug APK build and configuration persistence.
- [ ] 3. Wave 2: Implement and test the pure Kotlin session/cooldown engine with fake clocks, serialized transitions and recovery semantics.
- [ ] 4. Wave 3: Connect AccessibilityService, overlay warning, HOME actions and service status UI; verify foreground ownership and cleanup.
- [ ] 5. Wave 4: Execute the Instagram/device checklist, document build/install/permissions, record evidence and limitations, then use Logics closeout only when acceptance criteria are met.
- [ ] ADR 009 checkpoint: update affected Logics docs during each meaningful wave and leave the repo commit-ready.
- [ ] Keep commit creation under operator control; do not force one commit per micro-step.
- [ ] GATE: do not close until lint, audit, and scaffold validation pass.

# Backlog
- `item_001_bootstrap_android_app_and_persistent_app_rules`
- `item_002_implement_deterministic_session_and_cooldown_engine`
- `item_003_connect_accessibility_detection_warning_and_home_enforcement`
- `item_004_validate_apk_and_document_installation_and_permissions`

# Definition of Done (DoD)
- [ ] A clean checkout builds the installable APK and the app implements the complete request acceptance contract.
- [ ] Engine tests and recorded Pixel 8 Instagram scenarios verify pause/resume, warning, HOME, cooldown and restart persistence.
- [ ] Android build/lint/tests and Logics validation pass; README build, install and permission instructions match the delivered APK.
- [ ] Meaningful waves followed ADR 009: affected docs updated and the repo left commit-ready without automatic commits.

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
- (no validation recorded yet)

# Report
- Not started.

# Links
- Request: `req_000_stopscroll_android_mvp_for_per_opening_app_limits`
- Product brief(s): `prod_001_stopscroll_minimal_android_self_control_app`
- Architecture decision(s): (none yet)
