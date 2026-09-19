# Android MVP decisions and validation notes

This document supplements the generated request chain. No Android implementation or device validation has happened yet.

## Confirmed user requirements

Kotlin + Jetpack Compose; selectable monitored apps; independent per-opening usage and cooldown durations; AccessibilityService foreground detection; warning 30 seconds before expiry; HOME at expiry and on cooldown re-entry; persisted settings; simple UI; functional APK plus build/install/permission instructions. Instagram (com.instagram.android) is the initial validation target. Additional app selection remains required.

## Open decisions

| ID | Question | Proposed choice, awaiting user response | Impact |
| --- | --- | --- | --- |
| D1 | What happens when leaving before expiry and reopening? | CONFIRMED: resume the remaining usage allowance. Pause consumption outside the target app and on screen lock; continue on next entry until exhausted. | The session spans multiple openings. Leaving early does not reset the allowance or start cooldown. |
| D2 | Which phone and Android version will validate the APK? | CONFIRMED: Pixel 8 running the latest Android offered for that model. Proposed minimum remains Android 8/API 26. | Record the exact installed OS/API/build at implementation and validation time; do not infer a fixed version from “latest”. |
| D3 | What initial durations should Instagram use? | 5 minutes usage, 10 minutes cooldown, editable per app. | Nonblocking configuration default. |
| D4 | What warning presentation and UI language? | CONFIRMED: French UI and visual warning above the target app. Implementation choice: short non-touchable accessibility overlay, without sound/vibration. | Determines warning integration. |
| D5 | Should active cooldown survive phone reboot? | CONFIRMED: retain cooldown after reboot. Offline wall-clock recovery with documented time-change limitations. | Runtime persistence is required in addition to settings persistence. |
| D6 | Can the user freely change/disable rules? | CONFIRMED: freely editable rules. Proposed detailed behavior: disable clears state; duration changes take effect next session, existing cooldown stays unchanged. | Voluntary self-control without PIN or anti-tamper features. |
| D7 | How is the MVP distributed? | CONFIRMED: manually installed APK. Use a debug APK initially; no Play Store submission. | Enables focused delivery. |

The user answered D1, D2 and the D4-D7 bundle during preparation. Only D3 remains an optional user preference; 5/10 minutes is a nonblocking provisional default. Implementation details below are explicit engineering choices, not additional user approvals. There is no remaining blocking product decision; actual device access is required before real-device acceptance.

## Session contract with retained foreground allowance

- Start a new allowance when a selected package becomes the actual foreground owner and has neither remaining allowance nor active cooldown. Resume an existing allowance on later openings.
- A switch between activities inside that package does not count as another opening.
- Leaving for another ordinary app, HOME, or screen lock snapshots remaining allowance and pauses its consumption without starting cooldown. Reopening restores that allowance; e.g. use 2 of 5 minutes, leave for 10 minutes, then reopen with 3 minutes remaining.
- Keyboard, notification shade, permission dialogs, and the warning overlay do not create new sessions. Keep the current deadline during brief transient UI, reconcile foreground ownership on return, and pause on confirmed screen lock or a different ordinary foreground app.
- Exhausting the allowance commits a cooldown even when transient UI covers the target. Invoke HOME only while the target is confirmed foreground; otherwise enforce on its next entry. At cooldown expiry, the next opening starts a full new allowance.
- Persist remaining allowance, warning state, foreground checkpoint and cooldown for each package. On normal exit/disconnect snapshot exact remaining time; while active checkpoint at most every second, with a documented maximum 1-second allowance rollback after abrupt process loss. On reconnect/reboot restore saved remaining allowance, pause any unobserved interval, and resume only after a confirmed foreground event. Never charge unknowable background time or silently reset to the full configured allowance. No resistance to repeated forced crashes is claimed.
- Cooldown begins at session expiry, not on each denied entry. It expires by elapsed time and is independent for each package.
- A selected app with usage <= 30 seconds gets an immediate single warning, proposed to preserve short testing durations. For longer sessions warn at 30 seconds remaining. Warning state belongs to the allowance cycle and survives pause/resume; do not warn again on each reopening.
- Serialize engine events; re-check ownership immediately before HOME. Handle false HOME return results without claiming successful enforcement.
- Single foreground phone usage is the supported validation mode. Split-screen, PiP, work profiles, cloned packages and browser versions are not part of the initial guarantee.

## Architecture and permission boundaries

One app module is sufficient: Compose UI/ViewModel, DataStore rules and runtime state, pure Kotlin engine, AccessibilityService as Android adapter. Use injected clock/scheduler boundaries and constructor wiring; no backend, account, analytics, database or DI framework is required.

Use accessibility window/package events, not screen text or screenshots. Observe exits as well as target entries. Query installed launchable apps through scoped package visibility, without assuming QUERY_ALL_PACKAGES is necessary. Exclude the app itself and the current launcher from selection to prevent loops.

The service requires manual user activation; it cannot silently grant itself accessibility access. Prefer TYPE_ACCESSIBILITY_OVERLAY for the brief warning, without adding SYSTEM_ALERT_WINDOW or notification permission by default. Validate overlay support on the actual device. No Usage Access, exact alarm, root or device administrator permission is planned.

Settings and cooldown persistence are confirmed. Persist paused usage allowance too so restart cannot silently reset the selected resume behavior. Monotonic time handles in-boot active usage and cooldown timing; reboot cooldown recovery relies on persisted wall-clock deadlines with bounded remaining time, while usage restores its checkpoint and excludes unobserved time. Changing the clock, force-stop, uninstalling, or disabling accessibility are not defended against. The UI must never imply protection when the service is unavailable.

## Development and delivery checks

1. Inspect installed JDK, Android SDK, adb and emulator/device access. Pin mutually compatible Gradle, Android plugin, Kotlin and Compose versions during implementation; do not invent a verified toolchain at corpus time.
2. Build from a clean checkout with the wrapper. Expected conventional command: ./gradlew assembleDebug; expected APK location: app/build/outputs/apk/debug/app-debug.apk. Confirm actual commands/paths in the implementation README.
3. Use ./gradlew testDebugUnitTest lintDebug or actual equivalent tasks. Fake-clock engine tests must verify behavior, not implementation structure.
4. Install with adb install -r app/build/outputs/apk/debug/app-debug.apk or a documented manual flow. Activate StopScroll in Android accessibility settings and verify service-enabled status.
5. On Instagram, use e.g. 45 seconds usage / 60 seconds cooldown: confirm a warning at 15 seconds, HOME near 45 seconds, rejected re-entry, unextended cooldown and a new allowed session after 60 seconds.
6. Exercise early exit/reopen under the selected policy, repeated internal events, system UI, lock/unlock, another foreground app at expiry, two independent selected apps, a disabled rule and missing Instagram.
7. Verify settings and runtime recovery after process/service restart and reboot. Record exact device/OS and executed evidence.
8. Explain that HOME returns to the launcher rather than killing Instagram. There can be a brief app flash before its foreground event is handled. No guarantee is made while accessibility is disabled or the app is force-stopped.
9. Document accessibility activation restrictions and OEM battery-management observations only when relevant to the tested device. Do not claim a blanket battery exemption or silently enable settings.

Missing phone access does not block the project scaffold or engine tests, but it blocks asserting that the APK works with Instagram on that phone.

## Platform references

- [AccessibilityService API, including GLOBAL_ACTION_HOME](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService)
- [Create an accessibility service](https://developer.android.com/guide/topics/ui/accessibility/views/service)
- [Declare package visibility needs](https://developer.android.com/training/package-visibility/declaring)
- [Android restricted settings help](https://support.google.com/android/answer/12623953?hl=en)

References were checked during corpus preparation. These platform constraints do not constitute device execution evidence.
