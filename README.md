# StopScroll

StopScroll is an Android app that limits how long you may stay inside chosen apps per opening,
then sends you back to the launcher and blocks re-entry for a cooldown period. The user interface
is in French. Distribution is a manually installed debug APK; there is no Play Store build.

The allowance is *retained across openings*: leaving the target app pauses consumption instead of
resetting it. If you use 2 minutes of a 5-minute allowance and come back ten minutes later, you get
the remaining 3 minutes. Only exhausting the allowance starts a cooldown.

## Prerequisites

| Tool | Version used for the delivered build |
| --- | --- |
| JDK | Temurin 17.0.20.1+1 (any JDK 17 works) |
| Android SDK platform | android-35 |
| Android SDK build-tools | 35.0.0 |
| Android SDK platform-tools | 37.0.1 (for `adb`) |
| Gradle | 8.11.1, via the checked-in wrapper |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |

Minimum supported device: Android 8.0 (API 26). Target SDK: 35.

Point the build at your SDK with a `local.properties` file at the repository root (it is
gitignored):

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Alternatively export `ANDROID_HOME`. Set `JAVA_HOME` to your JDK 17 installation.

## Build

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Validate

```bash
./gradlew lintDebug testDebugUnitTest assembleDebug
```

`testDebugUnitTest` runs the session engine and runtime coordinator tests against a fake clock:
pause and resume (including the StopScroll activity), startup readiness and foreground reconciliation,
warning ownership and cleanup, duration bounds, expiry, cooldown re-entry and expiry, restart and
reboot recovery, rule edits and package independence. A JVM DataStore test exercises overlapping
rule transactions against a temporary preferences file. `lintDebug` runs with `warningsAsErrors`.

## Install

With adb:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Manually: copy `app-debug.apk` to the phone, open it from the Files app and allow installing from
that source when prompted.

## Activate the accessibility service

StopScroll enforces nothing until you enable it yourself; it cannot grant itself access.

1. Open **Paramètres → Accessibilité → Applications installées → StopScroll**.
2. Enable the service and confirm the system dialog.
3. On recent Android versions an APK installed outside an app store lands in *restricted settings*.
   If the toggle is greyed out, open **Paramètres → Applications → StopScroll → ⋮ → Autoriser les
   paramètres restreints**, then enable the service again. See
   [Android restricted settings](https://support.google.com/android/answer/12623953).
4. Return to StopScroll. The banner at the top of the main screen must read **Service actif**.

The app requests no other permission: no Usage Access, no `SYSTEM_ALERT_WINDOW`, no notifications,
no exact alarms, no device administrator, no root. Package visibility is scoped to launchable apps
through a `<queries>` declaration rather than `QUERY_ALL_PACKAGES`.

## Configure a rule

1. Tap **Ajouter une application** and pick an installed app (Instagram is
   `com.instagram.android`). StopScroll itself and your current launcher are excluded so
   enforcement can never trap you in a HOME loop.
2. Enter the allowed duration and the block duration, both in seconds. Both must be whole numbers
   greater than zero. Values exceeding safe millisecond conversion or cooldown deadline bounds
   are rejected; invalid input leaves the saved rule untouched.
3. The switch on each card enables or disables the rule. Disabling clears that app's remaining
   allowance and any running cooldown. A duration change applies to the next session and leaves a
   running cooldown unchanged.

Defaults for a newly added app are 5 minutes of use and 10 minutes of cooldown.

## What happens at runtime

- The service watches which ordinary app owns the foreground. It reads window and package events
  only — never screen text, screenshots, credentials or browsing activity. Nothing is transmitted.
- 30 seconds before the allowance runs out, a short non-touchable overlay appears above the target
  app for 4 seconds. There is no sound or vibration. For an allowance of 30 seconds or less the
  warning is shown immediately on opening instead. One warning per allowance cycle: it does not
  reappear each time you reopen the app.
- At the deadline the cooldown starts and StopScroll invokes the system HOME action if the target
  is still in the foreground. If something else is in the foreground at that moment, the cooldown
  is still committed and HOME is applied on the next confirmed entry into the target.
- During cooldown, opening the target sends you HOME again. Repeated attempts do not extend the
  deadline. Once it expires, the next opening starts a full new allowance.
- Each package has its own rule, allowance and cooldown; they never overwrite each other.
- The keyboard, the notification shade, permission dialogs and StopScroll's own overlay do not
  create or end a session.

## Persistence and recovery

Rules, remaining allowance, warning state and cooldown are stored with DataStore. While a session
runs, the state is checkpointed every second.

- A normal exit or service disconnect snapshots the exact remaining time.
- An abrupt process loss can roll the remaining allowance back by at most 1 second.
- Time during which the service was not running is never charged against your allowance, and the
  allowance is never silently reset to its full configured value.
- Allowance consumption uses monotonic time. Cooldown uses a stored wall-clock deadline so it keeps
  running across a reboot.

## Known limitations

- HOME returns to the launcher. It does not force-stop the target app and it is not an OS-level
  launch prohibition. You may briefly see the app before its foreground event is handled.
- Because cooldown deadlines are wall-clock based, changing the system clock changes when a
  cooldown ends.
- No protection is claimed while the accessibility service is disabled or StopScroll is
  force-stopped, and there is no PIN or anti-tamper mechanism. This is a voluntary self-control
  tool, by design.
- Resistance to repeated forced crashes is not claimed.
- Supported usage mode is a single foreground app on one phone. Split-screen, picture-in-picture,
  work profiles, cloned packages and browser versions of the target sites are outside the initial
  guarantee.
- Overlay support must be confirmed on the actual device; `TYPE_ACCESSIBILITY_OVERLAY` availability
  depends on the OEM build.
- OEM battery management may stop the service on some devices. StopScroll does not request a
  battery exemption and does not silently change any system setting.

## Device acceptance checklist

This checklist has **not** been executed: no Android device was reachable from the build
environment when the APK was produced, so no real-device success is claimed. Record the exact
device model, Android version, API level and build number when you run it.

Suggested rule for the run: Instagram, 45 seconds allowed, 60 seconds blocked.

1. Install the APK, enable the accessibility service and confirm the banner reads *Service actif*.
2. Open Instagram: a warning appears after 15 seconds (30 seconds remaining; target tolerance
   2 seconds). Leave while it is visible: it must disappear over the launcher or another app.
   Also try a 1-second allowance: no warning may remain over HOME after expiry.
3. HOME is invoked near 45 seconds.
4. Reopening during the cooldown sends you HOME again, within about 1 second of the entry.
5. Repeated attempts do not push the cooldown deadline further out.
6. After 60 seconds a new opening is allowed with a full fresh allowance.
7. Leave Instagram before expiry, including a direct switch into StopScroll settings through
   recent apps; wait, reopen: the remaining time resumes where it stopped.
8. Navigate between screens inside Instagram: the session is not reset.
9. Open the notification shade and the keyboard: the session is not reset.
10. Lock and unlock the screen: consumption is paused while locked.
11. Have another app in the foreground when the allowance expires: that app is not ejected, and
    HOME applies on the next entry into Instagram.
12. Configure two selected apps: their allowances and cooldowns stay independent.
13. Disable a rule: its allowance and cooldown are cleared and it is no longer enforced.
14. Remove Instagram from the list, or test on a device without Instagram: nothing breaks.
15. Force-stop StopScroll or toggle the accessibility service off and on: rules, remaining
    allowance and cooldown are restored. If Instagram is already open when the service connects,
    enforce its restored allowance or cooldown without waiting for another window event.
16. Reboot the phone: the cooldown is still running and the remaining allowance is intact.

## Project layout

| Path | Contents |
| --- | --- |
| `app/src/main/java/com/stopscroll/app/engine/` | Pure Kotlin session and cooldown state machine, with an injected clock |
| `app/src/main/java/com/stopscroll/app/service/` | AccessibilityService adapter, warning overlay, Android clock |
| `app/src/main/java/com/stopscroll/app/data/` | DataStore persistence for rules and runtime state |
| `app/src/main/java/com/stopscroll/app/ui/` | Compose screen, view model, app picker, service status |
| `app/src/test/java/` | Fake-clock engine tests |

## Workflow

Product and delivery documentation lives in [`logics/`](logics/). Use `logics-manager status` to
inspect the next workflow action. Confirmed product decisions are recorded in
[`logics/scaffold/android-mvp-decisions.md`](logics/scaffold/android-mvp-decisions.md).
