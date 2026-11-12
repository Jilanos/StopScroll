# StopScroll: development guide

Back to the [README](../README.md).

StopScroll is an Android app that limits how long you may stay inside chosen apps,
then sends you back to the launcher and blocks re-entry for a cooldown period. The user interface
is in French. Distribution is a manually installed debug APK; there is no Play Store build.

Each app has two timers:

- the **allowance** (*temps de détente*) runs only while the app is in the foreground;
- the **pause** runs only while the app is closed, from the moment you leave it with part of the
  allowance used.

| Situation | Allowance | Pause |
| --- | --- | --- |
| App open | runs | stopped |
| App closed | stopped | runs |
| App reopened before the pause is reached | resumes where it was | back to zero |
| Pause reached while away | refilled (full) | cleared |

With a 5-minute allowance and a 10-minute pause: use 2 minutes, leave, come back 4 minutes later
and you get the remaining 3 minutes; the next exit starts a new 10-minute pause. Stay away
10 minutes and the next opening starts with 5 full minutes. Exhausting the allowance starts a
cooldown (a pause imposed by StopScroll) during which openings are sent HOME.

The pause required away from the app is the rule's *Durée de la pause*, or the pending doubled
duration after a **+30 s**. Reaching it counts as a completed block and clears that doubling.

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
the allowance and pause timers (open, closed, reopened before the pause, pause reached), daily
usage by local day, the main screen timer status, pause and resume (including the StopScroll activity), startup readiness and foreground reconciliation,
warning ownership and cleanup, duration bounds, expiry, cooldown re-entry and expiry, restart and
reboot recovery, restore of states saved by the previous version, rule edits, package independence,
and the +30 s extension with its doubled block. JVM DataStore tests exercise overlapping
rule transactions and the decoding of a runtime payload saved by the previous version against a
temporary preferences file. `lintDebug` runs with `warningsAsErrors`.

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
4. Return to StopScroll. The card at the top of the main screen must read **StopScroll veille**.
   While the service is off, that card shows the setup steps above and a button to the settings.

The app requests no other permission: no Usage Access, no `SYSTEM_ALERT_WINDOW`, no notifications,
no exact alarms, no device administrator, no root. The time-up screen is started by the
accessibility service, which Android allows without an overlay permission. Package visibility is scoped to launchable apps
through a `<queries>` declaration rather than `QUERY_ALL_PACKAGES`.

## Configure a rule

1. Tap **Ajouter une app** and pick an installed app (each app is listed with its icon; common
   social apps appear first under *Suggestions*, and a search field filters the list). StopScroll
   itself and your current launcher are excluded so enforcement can never trap you in a HOME loop.
   Picking an app that already has a rule opens that rule.
2. Set the allowed duration (*Temps de détente*) and the block duration (*Durée de la pause*).
   Preset chips fill the field; the field itself takes seconds. Both must be whole numbers
   greater than zero. Values exceeding safe millisecond conversion or cooldown deadline bounds
   are rejected; invalid input leaves the saved rule untouched.
3. Tap a card to edit its rule, or remove it with **Retirer cette application** (a snackbar offers
   **Annuler** to put it back). The switch on each card enables or disables the rule. Disabling clears that app's remaining
   allowance, its pause and any running cooldown; today's usage is kept. A duration change applies to the next session and leaves a
   running cooldown unchanged.

Defaults for a newly added app are 5 minutes of use and 10 minutes of cooldown.

## What happens at runtime

- The service watches which ordinary app owns the foreground. It reads window and package events
  only. It never reads screen text, screenshots, credentials or browsing activity. Nothing is transmitted.
- 30 seconds before the allowance runs out, a short non-touchable pill appears at the top of the
  target app for 4 seconds. There is no sound or vibration. For an allowance of 30 seconds or less the
  warning is shown immediately on opening instead. One warning per allowance cycle: it does not
  reappear each time you reopen the app.
- At the deadline the cooldown starts. If the target is still in the foreground, StopScroll covers it
  with its own *Temps écoulé* screen showing the app icon, its name and the pause duration. **OK**
  or the back button goes to the launcher. If the screen cannot be started, the system HOME action
  is used instead. If something else is in the foreground at that moment, the cooldown is still
  committed and HOME is applied on the next confirmed entry into the target.
- The time-up screen also offers **+30 s**. See *Extension rule* below.
- During cooldown, opening the target sends you HOME again. Repeated attempts do not extend the
  deadline. Once it expires, the next opening starts a full new allowance.
- Leaving the target with part of the allowance used starts its pause. The launcher, another app,
  StopScroll itself and a locked screen all count as away. The service refills the allowance as soon
  as the pause is reached, without waiting for the next opening.
- Each package has its own rule, allowance and cooldown; they never overwrite each other.

### Extension rule (+30 s)

- **+30 s** cancels the block that just started, grants 30 seconds of allowance and reopens the
  app. In exchange, the block applied when those 30 seconds run out is twice the block that was
  cancelled.
- Each further **+30 s** doubles again: with a 10-minute block, the blocks become 20, 40, 80
  minutes and so on. The doubled duration is capped at the largest duration accepted as input
  for the current date, so a deadline never overflows.
- The 30 seconds behave like any allowance: leaving the app pauses them, and no 30-second warning
  is shown for them. Staying away for the doubled duration refills the allowance.
- The doubled duration applies to one block only. Once a block runs to its end, or the user stays
  away for the doubled duration, the next cycle uses the configured block duration again. Disabling or removing the rule also clears it.
- The extension is offered only on the time-up screen shown at expiry. Reopening the app during a
  block sends you HOME, without any extension. A block restored from a version without this
  feature offers no extension.
- The keyboard, the notification shade, permission dialogs and StopScroll's own overlay do not
  create or end a session.

## Persistence and recovery

Rules, remaining allowance, warning state, the start of a running pause, today's usage, cooldown
and the current block duration (including a doubled one after **+30 s**) are stored with DataStore. While a session
runs, the state is checkpointed every second.

- A normal exit or service disconnect snapshots the exact remaining time.
- An abrupt process loss can roll the remaining allowance back by at most 1 second.
- Time during which the service was not running is never charged against your allowance. A state
  saved while the app was open (abrupt loss, or a state saved by the previous version) starts its
  pause at restore, so that unobserved time is not credited to the pause either.
- A pause started before a restart keeps running: it is stored as a wall-clock start time.
- Allowance consumption uses monotonic time. Cooldown uses a stored wall-clock deadline so it keeps
  running across a reboot. If the wall clock moves backwards during a pause, the pause restarts
  from the new time instead of stretching.
- Today's usage is stored per app with its local calendar day; a new day starts again from zero.
  The new fields have defaults, so runtime state saved by the previous version still loads.

## Interface

- Each app card on the main screen shows today's usage (*Aujourd'hui*), an allowance bar
  (*Détente*, remaining over total) and a pause bar (*Pause*: time left while the app is closed,
  *arrêtée* while it is open, or the remaining imposed pause). The values come from the state the
  service persists and move every second while the screen is visible.
- The first launch shows a short explainer (*Comment ça marche*). The overflow menu of the main
  screen opens it again and offers the theme: *Comme le système*, *Clair* or *Sombre*. These
  display preferences live in their own SharedPreferences file and never touch rules or sessions.
- The launcher icon is an adaptive icon with a monochrome layer for themed icons.

## Known limitations

- HOME returns to the launcher. It does not force-stop the target app and it is not an OS-level
  launch prohibition. You may briefly see the app before its foreground event is handled.
- Because cooldown deadlines and pauses are wall-clock based, changing the system clock changes
  when a cooldown or a pause ends.
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

1. Install the APK, enable the accessibility service and confirm the card reads *StopScroll veille*.
2. Open Instagram: a warning appears after 15 seconds (30 seconds remaining; target tolerance
   2 seconds). Leave while it is visible: it must disappear over the launcher or another app.
   Also try a 1-second allowance: no warning may remain over HOME after expiry.
3. The *Temps écoulé* screen appears near 45 seconds, with Instagram's icon and a 1 min pause.
   **OK** returns to the launcher.
4. Reopening during the cooldown sends you HOME again, within about 1 second of the entry.
5. Repeated attempts do not push the cooldown deadline further out.
6. After 60 seconds a new opening is allowed with a full fresh allowance.
7. Leave Instagram before expiry, including a direct switch into StopScroll settings through
   recent apps; wait less than 60 seconds, reopen: the remaining time resumes where it stopped.
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
16. Reboot the phone: the cooldown is still running, and the remaining allowance is intact unless
    its pause ran out meanwhile, in which case it is full again.
17. At expiry, tap **+30 s**: Instagram reopens with 30 seconds and no warning. At the next expiry
    the screen announces a 2-minute block; tap **+30 s** again and the next one is 4 minutes. Let a
    block run out: the following cycle is back to 45 seconds and a 1-minute block.
18. The rule list and the app picker show each app's icon next to its name.
19. Use Instagram 20 seconds, leave and wait 60 seconds: the allowance is full again (Instagram
    card in StopScroll, and the next opening lasts 45 seconds).
20. Use Instagram 20 seconds, leave, reopen after 30 seconds, leave again: the pause bar starts
    again from zero and needs a full 60 seconds.
21. Open StopScroll while Instagram's pause is running: its card shows today's usage, the frozen
    allowance and a pause bar that moves every second; while blocked, the pause bar shows the
    remaining imposed pause.

## Project layout

| Path | Contents |
| --- | --- |
| `app/src/main/java/com/stopscroll/app/engine/` | Pure Kotlin allowance, pause and cooldown state machine with an injected clock, and the timer status shown on the main screen |
| `app/src/main/java/com/stopscroll/app/service/` | AccessibilityService adapter, warning overlay, Android clock |
| `app/src/main/java/com/stopscroll/app/data/` | DataStore persistence for rules and runtime state |
| `app/src/main/java/com/stopscroll/app/ui/` | Compose screens (onboarding, rules, app picker, rule editor, time-up), view model, app icons, service status |
| `app/src/main/java/com/stopscroll/app/ui/theme/` | Material 3 color schemes, typography, shapes, spacing and the in-app theme choice |
| `docs/screenshots/` | Device screenshots used by the README |
| `app/src/test/java/` | Fake-clock engine tests |

## Workflow

Product and delivery documentation lives in [`logics/`](../logics/). Use `logics-manager status` to
inspect the next workflow action. Confirmed product decisions are recorded in
[`logics/scaffold/android-mvp-decisions.md`](../logics/scaffold/android-mvp-decisions.md).
