# Dance Final Music – Feature Overview

## Current Features (already implemented)

### Main Screen (MainActivity)
- Dance style selection: **Standard** and **Latein** (Latin).
- **Select dances** card opens the dance selection screen.
- **Play single dance** row: buttons for every dance of the current style; tapping a button starts/loops that dance's music directly, tapping again stops it.
- **Music timing** card: music duration, music break (pause between dances), tempo.
- **Rounds (Burst)** card: number of rounds and break between rounds.
- Stepper buttons with press-and-hold repeat for all numeric values; values can also be typed directly by tapping the value.
- Start button: opens the dance timer session (or dance selection if none chosen).
- Settings button opens the settings screen.

### Dance Style (DanceStyleActivity)
- Standalone screen to switch between Standard and Latin.

### Dance Selection (DanceSelectActivity)
- Lists the dances of the active style.
- Tap to select/deselect; selected dances get numbered in the order chosen.
- "Select all" / "Deselect all" buttons.
- Confirmation saves the selection for the current style (selection is stored per style).

### Settings (SettingsActivity)
- **Music management**: for each dance a folder can be assigned via Android's document tree picker (SAF, persistable read permission). Shows the selected folder name, "no music folder", or "access lost - re-select".
- **Dance style**: Standard / Latin.
- **Theme settings**: Dark / Light / System mode.
- **Accent color**: 10 selectable accent colors.
- **Language settings**: German / English.
- **About** screen.

### Dance Timer / Session (DanceTimerActivity)
- Plays the selected dances one after another.
- **Music duration** per dance (10–300 s, step 5 s).
- **Break/pause between dances** (0–60 s).
- **Rounds (burst mode)**: repeat the whole sequence; **break between rounds** (0–60 s).
- Countdown ring with animated progress.
- Remaining time display, progress text (dance x/y, round x/y), dots indicating progress.
- **Skip** next dance and **Back** to previous dance (with history stack) – buttons and horizontal swipe gestures.
- Preloading of the next song for a shorter gap between dances.
- Audio focus handling (music stops when focus is lost).

### Music Playback (SingleDancePlayer)
- Plays a random audio file from the assigned folder of a dance.
- Loops until stopped; tempo applied via MediaPlayer playback params.
- Queue-based playback: starts a shuffled queue of all tracks in the dance folder and advances automatically.
- `playAt(index)` jumps to any queue position (used by the queue list); `updateTempo()` re-applies speed live.
- Stop is fade-safe: a second stop during the fade is ignored, and the queue only advances when playback is actually running.
- Graceful stop with fade-out (Feature 3), fade-in on start.
- BPM targeting: speed is computed per track from the target BPM (nominal BPM parsed from the filename) with a 0.5–2.0 clamp; falls back to the percentage tempo when no target or nominal BPM is set.

### Queue (QueueActivity)
- Shows the visible next-songs list of the current single-dance playback, with index, track name, and BPM.
- Highlights the currently playing track; tapping a song jumps straight to it (tap the playing track again to stop).
- Header shows the current track with its BPM and the computed playback speed (%).
- Also reachable via the playlist button on the main screen; updates live during playback.
- The main screen shows a compact now-playing line plus the queue below the dance buttons.

### Tempo in BPM (TempoActivity + main screen)
- Main screen has a **BPM (target)** stepper next to the tempo control; "Aus"/Off means automatic per-track speed is off.
- Tapping the BPM label opens TempoActivity: a 20–400 BPM slider; "Off" is shown until a value is chosen (tap to enable at 100 BPM).
- The queue preview displays each track's nominal BPM and the resulting playback speed (%).

### Music Effects (SettingsActivity)
- **Fade out**: soft fade-out/overlap between dances (400 ms) and fade-in at song start; can be toggled in Settings.
- **Applause at the end**: plays a bundled synthetic applause sound after the last dance of the final round; can be toggled in Settings.

### Dance Timer / Session (DanceTimerActivity)
- Music now fades out softly when a dance ends (Feature 3) and fades in at the start of each song (uses a separate fading player so the next song can start while the old one fades).
- Plays the bundled applause after the last dance of the final round (Feature 4) when enabled.

### Music Timing (MusicTimingActivity)
- **Music duration** slider (10–300 s).
- **Break between dances** slider (0–60 s).
- **Tempo** slider (50–150 %).

### Burst / Rounds Settings (BurstSettingsActivity)
- **Number of rounds** slider (1–10).
- **Break between rounds** slider (0–60 s).

### General
- Music folders are stored per dance per style as URI trees.
- Folder contents are cached; random track selection.
- Dark/light/system theming applied consistently; 10 accent colors.
- Bilingual UI (German / English).
- About screen with copyright/contact info.

## Planned Features (to be integrated later – NOT yet implemented)

1. **Sort the music by dance (fix mixed dances)**
   - Problem: the dance folders on the phone (SD card `Regensburg Tanzmusik`) contain files of multiple dances mixed together (e.g. 54 Jive files inside the ChaCha folder). Currently the app picks a random file, so the wrong dance can be played.
   - Fix: when listing a folder's audio files, detect each file's dance from its filename and exclude files that do not belong to the requested dance.
   - Detected naming conventions (filename is the reliable source; ID3 album/genre tags mirror the folder and are NOT reliable):
     - `107_CC_32_Downtown.mp3`, `01 18_01_Sa_51_Yourgunum Anla.mp3`, `119_Ji_36_sesame street.mp3` → `NNN_CODE_BPM_Title`
     - `Title - Ji - 43.mp3`, `Title JI  43.mp3`, `Artist_Title RB.mp3`, `Daniel_01_Ru.mp3`, `TRW_204_C_Title_07.mp3` → code in title/suffix
     - Codes: `CC`/`Ch`/`CHA`/`CH` = ChaCha, `Ru`/`RB` = Rumba, `Sa`/`SB` = Samba, `Ji`/`JI` = Jive, `PD` = Paso Doble; TRW single letters `C`/`J`/`R`/`S`; full words `ChaCha`/`Jive`/`Rumba`/`Samba`/`Paso Doble`
   - Sorting rule:
     - If filename code is present and differs from the folder's dance → exclude the file.
     - If no code is present (`AudioTrack`, `Spur`, plain titles) → include (belongs to the folder's dance, confirmed via ID3 album tag and via measured tempo).
     - `Practice Latein` folder is an intentional mix of all Latin dances → include everything.
   - Current statistics of the pulled collection (696 files): 320 correct, 54 clearly mixed (Jive in ChaCha folder), 322 without a code.
   - Rhythm/BPM analysis (librosa, tested on the pulled files): tempo CAN be measured from the audio, and the unknowns' measured BPMs match their folders (ChaCha 117–123, Samba 99–103, Paso Doble 117). But BPM alone cannot reliably classify the dance because the tempo ranges overlap (ChaCha ~120–130 vs Paso Doble ~116–124; Samba ~100–104 vs Rumba ~96–108).
   - Rhythm-pattern classification was also tested (not just BPM): an onset-strength accent profile was extracted over the bar (16 sixteenth-note slots, folded both beat-aligned and downbeat-aligned via a phase search) for 30 files with known codes (6 per dance). Result: leave-one-out accuracy was only 20–47 % (random = 20 %); a tempo-corrected BPM + rhythm-profile combination reached 40 %. Tempo alone separated Jive and Paso Doble perfectly (6/6 each) but ChaCha/Samba/Rumba overlap. Within-dance variation swamps the between-dance differences (the cha-cha-cha / samba bounce / rumba box signatures are too subtle for standard onset-strength features, and reliable downbeat detection is the weak link). Robust classification would need research-grade models (e.g. madmom downbeat tracking + trained classifier), which is not feasible per file on a phone.
   → Decision: use filename codes to EXCLUDE conflicting files; otherwise trust the folder. For the mixed Jive files (54 in the ChaCha folder) this removes them reliably; the ~322 uncoded files stay in their folder's dance.
   - Implemented: `DanceCodeFilter` (app/src/main/java/de/dancefinalmusic/util/DanceCodeFilter.java) detects the dance from a filename (last code token wins: `CC`/`CHA`/`CH`, `RB`/`RU`, `SB`/`SA`, `JI`, `PD`; TRW single letters `C`/`J`/`R`/`S`; full words `ChaCha`/`Jive`/`Samba`/`Rumba`/`Paso Doble`). `SettingsManager.getAudioFilesFromFolder()` now takes the requested dance and excludes conflicting files at query time (cache key includes the dance). The `Practice Latein` folder is exempt (all files kept). Applied to both single-dance playback (`SingleDancePlayer`) and the timer session (`DanceTimerActivity`) since both go through `getRandomAudioFromFolder()`.
   - Data fixed on the phone as well: the 54 Jive files were physically moved from the `ChaCha (Tobi)` folder into the `Jive` folder, and the metadata (album + genre) of all 65 Jive files was corrected to `Jive` (they were mis-tagged as `ChaChaCha`, matching the wrong folder). Phone and local copy (`device_music/regensburg`) now match exactly (696 files). The app filter remains as a safety net for any future mixes.
