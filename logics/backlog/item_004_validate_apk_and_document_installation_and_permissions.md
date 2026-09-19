## item_004_validate_apk_and_document_installation_and_permissions - Validate APK and document installation and permissions
> From version: 1.0.0
> Schema version: 1.0
> Status: In progress
> Understanding: 90%
> Confidence: 85%
> Progress: 85%
> Complexity: Medium
> Theme: MVP delivery
> Reminder: Update status/understanding/confidence/progress and linked request/task references when you edit this doc.
> Indicators reviewed: 2026-09-19 17:49:22

# AI Context
- Summary: Execute Instagram scenarios on the Pixel 8 and record results before claiming functional APK delivery..
- Keywords: validate, apk, document, installation, permissions
- Use when: validating the MVP or writing installation instructions.
- Skip when: claiming device behavior from unit tests alone.

# Problem
- A successful build alone does not establish that the service behaves correctly on a real phone.

# Scope
- In:
  - Run meaningful engine tests and Android build/lint checks, then install on the confirmed target device or record the device validation blocker.
  - Provide a manual scenario checklist using short durations and real Instagram, plus a second target for independence.
  - Verify persistence after app process recreation, service reconnect and device reboot; verify lock/unlock and unrelated foreground apps at expiry.
  - Document wrapper build commands, JDK/SDK versions, APK path, adb install -r and manual APK installation, service activation and device-specific restricted settings.
  - Record exact tested OS/device, results and known limitations; retain reproducible debug APK delivery without choosing a production signing/distribution system.
- Out:
  - Publishing to an app store, automated device farms and production release signing.

# Acceptance criteria
- AC1: Build/test results and executed device checklist steps are recorded with failures and remaining unverified cases.
- AC2: A user can follow the README to build, find, install and enable the APK; the functional claim is contingent on the Instagram device checks.
- AC3: No requirement is closed solely on an emulator substitute when Instagram enforcement has not been exercised.

# AC Traceability
- request-AC1 -> This backlog slice. Proof: Pending implementation: record clean wrapper build, APK path and installation result.
- request-AC2 -> This backlog slice. Proof: Pending implementation: verify app selection, per-app duration validation and saved rules.
- request-AC3 -> This backlog slice. Proof: Pending implementation: verify pause/resume with 2 of 5 minutes consumed, duplicate events and lock/unlock.
- request-AC4 -> This backlog slice. Proof: Pending implementation: verify one warning at 30 seconds remaining, short limits and resumed sessions.
- request-AC5 -> This backlog slice. Proof: Pending implementation: verify persisted expiry transition and HOME only for the current target.
- request-AC6 -> This backlog slice. Proof: Pending implementation: verify immediate cooldown re-entry rejection, fixed deadline and package independence.
- request-AC7 -> This backlog slice. Proof: Pending implementation: verify retained usage, warning and cooldown state across restart and reboot.
- request-AC8 -> This backlog slice. Proof: Pending implementation: verify service activation/status and minimal package-level access.
- request-AC9 -> This backlog slice. Proof: Pending implementation: record engine test results and actual Pixel 8 Instagram scenario evidence.
- request-AC10 -> This backlog slice. Proof: Pending implementation: follow README build/install/permission steps against the delivered APK.

# Decision framing
- Product framing: Not needed
- Architecture framing: Not needed

# Links
- Product brief(s): `prod_001_stopscroll_minimal_android_self_control_app`
- Architecture decision(s): (none yet)
- Request: `req_000_stopscroll_android_mvp_for_per_opening_app_limits`
- Primary task(s): `task_001_deliver_stopscroll_android_mvp_apk`

# Priority
- Priority: High — a functional APK and reproducible setup are the primary deliverable.
- Rationale: Set by scaffold input or defaulted for grooming.
