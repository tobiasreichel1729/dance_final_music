# Dance Final Music

An Android app for dance competition practice (Standard and Latin). It plays music for
the dances you selected, times each dance, and keeps the screen awake so you can
practice hands-free. Music folders are read directly from the phone, so no file ever
leaves the device.

- **Package:** `de.dancefinalmusic`
- **Language:** Java (no Kotlin)
- **Min SDK:** 26 (Android 8.0) · **Target SDK:** 36

## What the app does

You pick a dance style (Standard or Latin), choose which dances you want to practise,
assign a music folder to each dance, and start a session. The app then works through
your dances one after another, playing a track per dance for a set duration, pausing
between dances, and repeating everything for the configured number of rounds. A
foreground service publishes the current dance to the lock screen and the notification
shade, with media transport controls, so playback can be controlled without unlocking
the phone.

There is also a single-dance mode: tap any dance to start looping that dance's music
right away, browse the queue, and jump to any track in it.

### Features

**Session and timer**
- Plays the selected dances one after another, in the order you selected them
- Music duration (10–300 s) and break between dances (0–60 s)
- Rounds/burst mode with a break between rounds
- Animated countdown ring, remaining time, and progress (dance x/y, round x/y)
- Skip to next dance and back to the previous one, via buttons or swipe gestures
- Next track is preloaded so the gap between dances stays short
- Stepper controls with press-and-hold repeat; values can also be typed directly

**Music playback**
- Random track from the assigned folder, or a shuffled queue of the whole folder
- Tempo as a percentage (50–150 %) or as a per-dance BPM target
- Nominal BPM parsed from the filename, with a learned per-dance reference BPM as
  fallback; playback speed is clamped to a safe range
- Fade-out at the end of a dance and fade-in at the start of the next
- Optional applause after the last dance of the final round
- Graceful audio focus handling: music yields when another app takes focus
- Embedded cover art on the lock screen, in the shade, and in the player

**Playlist and filtering**
- Queue view with index, track name, and BPM; tap any track to jump to it
- Per-dance folder assignment via the Android document tree picker (SAF) with
  persistable read permission
- Folder access is checked and reported as lost rather than failing silently
- Filename-based dance detection (`DanceCodeFilter`) filters out files that belong to a
  different dance than the folder they sit in, since real collections are often mixed
- Folder contents are cached; a dedicated practice folder is left untouched

**Interface**
- German and English
- Dark, light, and system themes, with 10 accent colors
- Landscape layout for the player
- Stepper/slider settings for timing, rounds, and BPM

## Project structure

```
app/src/main/java/de/dancefinalmusic/
├── MainActivity.java          Start screen: style, dances, timing, rounds, now playing
├── DanceStyleActivity.java    Standalone Standard/Latin switch
├── DanceSelectActivity.java   Dance selection with ordering and per-dance BPM
├── DanceTimerActivity.java    Session UI: countdown ring, skip/back, gestures
├── SettingsActivity.java      Music folders, theme, accent color, language
├── MusicTimingActivity.java   Duration, break, tempo
├── BurstSettingsActivity.java Rounds and break between rounds
├── PlayerActivity.java        Single-dance player
├── QueueActivity.java         Queue of upcoming tracks
├── AboutActivity.java         About and support link
├── TimerRingView.java         Custom countdown ring view

├── DanceSession.java          Session state machine (phases, rounds, history)
├── SingleDancePlayer.java     Single-dance playback, queue, art, speed
├── DanceSessionService.java   Foreground service, media session, notification

└── util/
    ├── SettingsManager.java   SharedPreferences, dance lists, folder access, caching
    ├── DanceCodeFilter.java   Detects a file's dance from its filename
    ├── MusicFocusManager.java Audio focus handling
    ├── VolumeFader.java       Fade-in / fade-out envelope
    ├── FolderGuard.java       Verifies a dance still has a usable folder
    ├── ThemeHelper.java       Theme and accent color resolution
    └── Translations.java      German/English strings
```

Roughly speaking: `*Activity` classes are screens, `DanceSession` and
`SingleDancePlayer` own the playback and timing logic, `DanceSessionService` bridges
that state to Android's media session (lock screen, shade, Bluetooth, media buttons),
and `util/` holds the cross-cutting pieces.

`FEATURES.md` documents the feature set in more detail, including the reasoning behind
the filename-based dance filter.

## Building

Requirements:

- JDK 17
- Android SDK with platform 36 and build tools (the Gradle wrapper fetches Gradle 8.11.1)
- `local.properties` pointing at your SDK, or `ANDROID_HOME` set in the environment

```bash
# local.properties
sdk.dir=/path/to/Android/sdk
```

Debug build:

```bash
./gradlew assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk
```

Install on a connected device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Release build:

```bash
./gradlew assembleRelease
```

For a signed release build, create `keystore.properties` next to `build.gradle`:

```properties
storeFile=dancefinalmusic.keystore
storePassword=<password>
keyAlias=<alias>
keyPassword=<password>
```

The keystore and `keystore.properties` are git-ignored, so keep them out of version
control.

## Permissions

The app asks for very little:

- `WAKE_LOCK`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` — keeps the
  session running with the screen off
- `POST_NOTIFICATIONS` — the playback notification (requested at runtime on Android 13+)

Music is accessed through the system folder picker, so the app only ever sees the
folders you explicitly grant it.

## Support

If the app is useful to you, a coffee goes a long way:

**https://www.buymeacoffee.com/TobiasReichel**

Scan to open the support page:

<img src="app/src/main/res/drawable-nodpi/qr_contact.png" alt="QR code linking to the support page" width="220">

The same link is available in the app under About, as text and as this QR code.

## AI assistance disclosure

This project was developed with AI assistance. The code, layout XML, resources, and
documentation were written in collaboration with an AI coding assistant (OpenCode,
running on a Claude-based model), working interactively under the direction of the
author.

To be transparent about what that means in practice:

- **Generated code.** A substantial share of the source was drafted or rewritten by the
  assistant. That includes the playback and session logic, the foreground service and
  media session integration, several activities, most of the layout files, and the
  German/English string tables.
- **Reviewed and verified.** All generated code was read, adjusted, compiled, and
  exercised on a physical device before being committed. Behaviour that was not
  confirmed that way should be considered unverified.
- **Author responsibility.** The design decisions, feature scope, music-collection
  decisions, and the decision to ship are the author's. The assistant had no
  independent goals in the project.
- **No training on your code.** Nothing in this repository was submitted to a model
  provider for training. The assistant accessed only this repository and the connected
  Android device.

The signing key was never shared with the assistant and is not part of this repository.