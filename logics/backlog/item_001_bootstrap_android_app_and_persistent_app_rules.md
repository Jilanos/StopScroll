## item_001_bootstrap_android_app_and_persistent_app_rules - Bootstrap Android app and persistent app rules
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 90%
> Confidence: 85%
> Progress: 85%
> Complexity: Medium
> Theme: Android foundation
> Reminder: Update status/understanding/confidence/progress and linked request/task references when you edit this doc.
> Indicators reviewed: 2026-09-19 17:49:22

# AI Context
- Summary: Create the missing Android build and editable per-app rules with local persistence..
- Keywords: bootstrap, android, app, persistent, rules
- Use when: building the project, picker or rule editor.
- Skip when: changing session timing or accessibility event handling.

# Problem
- There is no Android code or build system and no editable persistent rule model.

# Scope
- In:
  - Create one Gradle Android app module with Kotlin, Compose, pinned dependencies and wrapper.
  - Implement French resource-based UI, app picker, rule editor, duration validation and DataStore repository.
  - Query launchable apps using scoped package visibility; exclude StopScroll and the default launcher from monitoring to avoid loops. Handle missing or removed Instagram gracefully.
  - Proposed defaults: usage 5 minutes, cooldown 10 minutes; positive integer seconds in storage and explicit units in UI. Initialize the project i18n contract when UI implementation starts.
- Out:
  - Multi-module clean architecture, database, dependency-injection framework and custom design system.

# Acceptance criteria
- AC1: The documented debug build produces an installable APK from a clean checkout.
- AC2: Instagram and a second installed launchable app can have different persisted rules; editing, deselection, invalid input and process recreation behave consistently.

# AC Traceability
- request-AC1 -> This backlog slice. Proof: Pending implementation: record clean wrapper build, APK path and installation result.
- request-AC2 -> This backlog slice. Proof: Pending implementation: verify app selection, per-app duration validation and saved rules.

# Decision framing
- Product framing: Not needed
- Architecture framing: Not needed

# Links
- Product brief(s): `prod_001_stopscroll_minimal_android_self_control_app`
- Architecture decision(s): (none yet)
- Request: `req_000_stopscroll_android_mvp_for_per_opening_app_limits`
- Primary task(s): `task_001_deliver_stopscroll_android_mvp_apk`

# Priority
- Priority: High — prerequisite for a buildable APK and every enforcement slice.
- Rationale: Set by scaffold input or defaulted for grooming.
