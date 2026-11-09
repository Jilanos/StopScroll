## req_000_stopscroll_android_mvp_for_per_opening_app_limits - StopScroll Android MVP for per-opening app limits
> From version: 1.0.0
> Schema version: 1.0
> Status: Ready
> Understanding: 90%
> Confidence: 85%
> Complexity: High
> Theme: Android MVP
> Reminder: Update status/understanding/confidence and linked backlog/task references when you edit this doc.
> Indicators reviewed: 2026-11-09 16:59:44

# AI Context
- Summary: Retained foreground allowance across openings, per-package cooldowns and Pixel 8 validation define the acceptance contract..
- Keywords: stopscroll, android, mvp, per, opening, app, limits
- Use when: scoping the MVP or checking requirements.
- Skip when: adding store publication, daily quotas or tamper resistance.

# Needs
- Build a functional installable Android APK using Kotlin and Jetpack Compose with a simple architecture.
- Select installed launchable applications to monitor, with Instagram (com.instagram.android) as the initial real-device target.
- Configure an independent maximum duration per opening and cooldown for each selected app.
- Detect foreground transitions with AccessibilityService, warn 30 seconds before expiry, send HOME at expiry, and send HOME immediately on attempts to reopen during cooldown.
- Persist configuration after process death and device restart; deliver build, APK installation and permission activation instructions.

# Context
- The repository currently contains only Git and Logics scaffolding; no Android project or toolchain has been established. This corpus defines future implementation, not a delivered APK.
- The user explicitly prioritizes a functional APK and simple architecture; the present request is to prepare the corpus and identify questions before development.
- Instagram is an explicitly requested technical target, not a client identity; retain its name and package for testability. Selection of additional launchable apps remains in MVP scope.
- User confirmed retaining remaining foreground usage across openings, Pixel 8 with the latest Android available for that model, French UI, overlay warning, cooldown persistence, freely editable rules and manual APK installation. Only initial duration defaults remain optional; detailed choices are in logics/scaffold/android-mvp-decisions.md.
- Proposed architecture: one Android app module, Compose Activity plus ViewModel, DataStore repository, a pure Kotlin session state machine and an AccessibilityService adapter owning its coroutine scope.
- Proposed minimum: Android 8/API 26. Validation target: Pixel 8; record its exact installed Android version/build rather than guessing what latest means. Pin compatible current build dependencies during implementation.
- Accessibility availability and event timing depend on Android and device behavior. HOME is a reactive return to the launcher, not force-stop or a secure OS-level launch prohibition.
- Foreground tracking must observe non-target transitions too; filtering accessibility events exclusively to Instagram would hide exits.
- Proposed warning: brief non-touchable accessibility overlay; avoid adding notification or general overlay permissions unless the selected design requires them.

# Acceptance criteria
- AC1: A clean checkout builds an installable debug APK with the checked-in Gradle wrapper and documented JDK/SDK prerequisites; Kotlin and Compose implement a single-module app.
- AC2: The user can select installed launchable apps, including Instagram when present, and save independent positive usage and cooldown durations. The main screen shows each selected app, allowed duration and block duration; invalid input is rejected without corrupting saved settings.
- AC3: A genuine foreground opening starts a new allowance or resumes its persisted remaining time. Leaving for another app or locking the screen pauses usage; after consuming 2 of 5 minutes, reopening even 10 minutes later grants exactly 3 minutes. Duplicate events and navigation within the same app do not reset it; a non-selected app never acquires a session.
- AC4: For a session longer than 30 seconds, one visible warning is issued when 30 seconds remain, with a target tolerance of 2 seconds on the supported device. For a duration of 30 seconds or less, the proposed behavior is one warning immediately on opening; both are verified without resetting the session.
- AC5: At the deadline the service starts a cooldown for that package and invokes GLOBAL_ACTION_HOME if the target is still foreground. While enabled, cooldown re-entry is rejected on the first confirmed target foreground event, targeting HOME within 1 second of receipt; it never ejects an unrelated foreground app.
- AC6: Repeated attempts during cooldown do not extend its deadline. At expiry a new opening is allowed. Independent packages do not overwrite each other's rules or cooldowns.
- AC7: App selections, durations, remaining usage allowance, warning state and cooldown survive service restart/reboot. Normal exits preserve exact remaining usage; abrupt process loss permits at most 1 second checkpoint rollback. Unknown disconnected time does not consume usage; cooldown continues by elapsed time. Recovery and clock assumptions are tested and documented without silently granting a full fresh allowance.
- AC8: The UI clearly reports accessibility enabled/disabled and links to system settings. The user explicitly enables the service; if disabled or force-stopped the app does not claim protection. No screen content, credentials or browsing activity are captured or transmitted.
- AC9: Pure state-machine tests cover duplicate events, warning/expiry boundaries, exits, reopening policy, cooldown re-entry and expiry, persistence recovery and independent apps. A device checklist verifies Instagram warning, HOME, re-entry, reboot and permission activation with recorded results and untested limitations.
- AC10: The README documents exact build commands, generated APK path, adb and manual installation, accessibility activation including restricted settings where applicable, troubleshooting and known limitations. No real-device success is claimed without execution evidence.

# Definition of Ready (DoR)
- [x] Problem statement is explicit and user impact is clear.
- [x] Scope boundaries (in/out) are explicit.
- [x] Acceptance criteria are testable.
- [x] Dependencies and known risks are listed.

# Companion docs
- Product brief(s): `prod_001_stopscroll_minimal_android_self_control_app`
- Architecture decision(s): (none yet)

# References
- logics/scaffold/android-mvp-decisions.md
- https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- https://developer.android.com/guide/topics/ui/accessibility/views/service
- https://developer.android.com/training/package-visibility/declaring
- https://support.google.com/android/answer/12623953?hl=en

# Backlog
- `item_001_bootstrap_android_app_and_persistent_app_rules`
- `item_002_implement_deterministic_session_and_cooldown_engine`
- `item_003_connect_accessibility_detection_warning_and_home_enforcement`
- `item_004_validate_apk_and_document_installation_and_permissions`

# Review correction checkpoint
- 2026-11-09 review: the MVP device acceptance remains pending in task_001 at 85%. Runtime and persistence corrections are tracked by req_001; no device result or new APK is claimed.
