package de.dancefinalmusic;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import de.dancefinalmusic.util.MusicFocusManager;
import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.VolumeFader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DanceSession {

    private static final String TAG = "DanceSession";

    public static final int PHASE_IDLE = 0;
    public static final int PHASE_MUSIC = 1;
    public static final int PHASE_PAUSE_BETWEEN_MUSIC = 2;
    public static final int PHASE_ROUND_BREAK = 3;

    private static final int FADE_IN_MS = 400;
    private static final int FADE_OUT_MS = 3000;
    private static final int FADE_OUT_SHORT_MS = 500;
    private static final int FADE_OUT_SECONDS = 3;

    private static DanceSession instance;

    private SettingsManager settings;
    private Context appContext;

    private int currentPhase = PHASE_IDLE;
    private int currentDanceIndex = 0;
    private int currentRound = 0;
    private int totalRounds = 1;
    private boolean isRunning = false;
    private boolean finishing = false;
    private int timeRemaining = 0;

    private List<String> selectedDances;

    private MediaPlayer mediaPlayer;
    private float lastAppliedTempo = 1.0f;
    private MediaPlayer fadingPlayer;
    private MediaPlayer preloadedPlayer;
    private MediaPlayer applausePlayer;
    private Uri preloadedUri;
    private MusicFocusManager focusManager;

    private int applauseWaitSeconds = 8;

    private final Stack<int[]> history = new Stack<>();

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            timeRemaining--;

            if (timeRemaining <= 0) {
                timeRemaining = 0;
                stopMusic();
                advancePhase(true);
            }

            notifyListeners();
            if (isRunning) {
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    private final MediaPlayer.OnCompletionListener onSongComplete = mp -> {
        if (!isRunning || currentPhase != PHASE_MUSIC) return;
        advanceFromMusicFinished();
    };

    public interface StateListener {
        void onStateChanged();
    }

    private final List<WeakReference<StateListener>> listeners = new ArrayList<>();

    private DanceSession() {
    }

    public static synchronized DanceSession getInstance() {
        if (instance == null) {
            instance = new DanceSession();
        }
        return instance;
    }

    // --- Listener management ---

    public synchronized void addListener(StateListener listener) {
        synchronized (listeners) {
            listeners.add(new WeakReference<>(listener));
        }
    }

    public synchronized void removeListener(StateListener listener) {
        synchronized (listeners) {
            listeners.removeIf(ref -> ref.get() == listener || ref.get() == null);
        }
    }

    private void notifyListeners() {
        List<StateListener> targets = new ArrayList<>();
        synchronized (listeners) {
            for (WeakReference<StateListener> ref : listeners) {
                StateListener l = ref.get();
                if (l != null) targets.add(l);
            }
        }
        for (StateListener l : targets) {
            l.onStateChanged();
        }
    }

    // --- State accessors ---

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized boolean isFinishing() {
        return finishing;
    }

    public synchronized boolean isActive() {
        return isRunning || finishing;
    }

    public synchronized int getPhase() {
        return currentPhase;
    }

    public synchronized int getTimeRemaining() {
        return timeRemaining;
    }

    public synchronized int getCurrentDanceIndex() {
        return currentDanceIndex;
    }

    public synchronized int getCurrentRound() {
        return currentRound;
    }

    public synchronized int getTotalRounds() {
        return totalRounds;
    }

    public synchronized List<String> getSelectedDances() {
        if (selectedDances != null && !selectedDances.isEmpty()) {
            return new ArrayList<>(selectedDances);
        }
        if (settings != null) {
            List<String> pending = settings.getSelectedDancesList();
            if (pending != null && !pending.isEmpty()) {
                return new ArrayList<>(pending);
            }
        }
        return new ArrayList<>();
    }

    public void attach(Context context) {
        synchronized (this) {
            if (settings == null) {
                this.settings = SettingsManager.getInstance(context);
            }
            if (appContext == null) {
                this.appContext = context.getApplicationContext();
            }
            if (selectedDances == null || selectedDances.isEmpty()) {
                selectedDances = settings.getSelectedDancesList();
            }
        }
    }

    public synchronized boolean hasHistory() {
        return !history.isEmpty();
    }

    public synchronized String getCurrentDanceName() {
        if (selectedDances == null || currentDanceIndex < 0 || currentDanceIndex >= selectedDances.size()) {
            return null;
        }
        return selectedDances.get(currentDanceIndex);
    }

    public synchronized String getNextDanceName() {
        if (selectedDances == null) return null;
        if (currentPhase == PHASE_PAUSE_BETWEEN_MUSIC
                && currentDanceIndex + 1 < selectedDances.size()) {
            return selectedDances.get(currentDanceIndex + 1);
        }
        if (currentPhase == PHASE_ROUND_BREAK && !selectedDances.isEmpty()) {
            return selectedDances.get(0);
        }
        return null;
    }

    // --- Control ---

    public void start(Context context) {
        synchronized (this) {
            if (isRunning) return;
            this.settings = SettingsManager.getInstance(context);
            this.appContext = context.getApplicationContext();
            selectedDances = settings.getSelectedDancesList();
        }
        if (selectedDances == null || selectedDances.isEmpty()) return;

        final Uri firstSong;
        synchronized (this) {
            firstSong = getNextRandomUri();
        }
        if (firstSong == null) {
            notifyListeners();
            return;
        }

        synchronized (this) {
            currentDanceIndex = 0;
            currentRound = 0;
            totalRounds = settings.getBurstCount();
            if (totalRounds < 1) totalRounds = 1;
            isRunning = true;
            finishing = false;
            history.clear();

            currentPhase = PHASE_MUSIC;
            timeRemaining = settings.getMusicDuration();
            releasePlayer(applausePlayer);
            applausePlayer = null;
        }

        if (focusManager == null) {
            focusManager = new MusicFocusManager(appContext, new MusicFocusManager.FocusListener() {
                @Override
                public void onFocusLoss() {
                    postToMain(() -> {
                        if (isRunning) {
                            stopDance();
                        }
                    });
                }

                @Override
                public void onFocusGain() {
                }
            });
        }
        focusManager.request();

        playSong(firstSong);
        preloadNextSongForNext();

        notifyListeners();
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    public void stopDance() {
        timerHandler.removeCallbacks(timerRunnable);
        synchronized (this) {
            isRunning = false;
            finishing = false;
            currentPhase = PHASE_IDLE;
            currentDanceIndex = 0;
            currentRound = 0;
            timeRemaining = 0;
            history.clear();
        }
        releaseAllPlayers();
        if (focusManager != null) {
            focusManager.abandon();
        }
        notifyListeners();
    }

    public void skipForward() {
        if (!isRunning) return;

        pushHistory();
        stopMusic(false);
        advancePhase(false);
        notifyListeners();
        if (isRunning) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    public void goBack() {
        synchronized (this) {
            if (!isRunning || history.isEmpty()) return;
        }

        stopMusic(false);
        timerHandler.removeCallbacks(timerRunnable);

        int[] prev = history.pop();
        synchronized (this) {
            currentRound = prev[0];
            currentDanceIndex = prev[1];
            currentPhase = PHASE_MUSIC;
            timeRemaining = settings.getMusicDuration();
        }
        Uri song = getNextRandomUri();
        playSong(song);
        preloadNextSongForNext();
        notifyListeners();
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    // --- Playback helpers ---

    private void releaseAllPlayers() {
        releasePlayer(mediaPlayer);
        mediaPlayer = null;
        releasePlayer(fadingPlayer);
        fadingPlayer = null;
        releasePlayer(preloadedPlayer);
        preloadedPlayer = null;
        preloadedUri = null;
        releasePlayer(applausePlayer);
        applausePlayer = null;
    }

    private void releasePlayer(MediaPlayer mp) {
        if (mp == null) return;
        try {
            if (mp.isPlaying()) mp.stop();
            mp.release();
        } catch (Exception e) {
            Log.e(TAG, "Error releasing MediaPlayer", e);
        }
    }

    private void playApplause() {
        if (!settings.isApplauseEnabled()) return;
        releasePlayer(applausePlayer);
        applausePlayer = null;
        try {
            applausePlayer = MediaPlayer.create(appContext, R.raw.applause);
            if (applausePlayer != null) {
                try {
                    int dur = applausePlayer.getDuration();
                    if (dur > 0) {
                        applauseWaitSeconds = Math.max(1, (int) Math.ceil(dur / 1000.0));
                    }
                } catch (Exception ignored) {
                }
                applausePlayer.setVolume(1.0f, 1.0f);
                applausePlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    if (applausePlayer == mp) applausePlayer = null;
                });
                applausePlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "Error playing applause: " + what);
                    mp.release();
                    if (applausePlayer == mp) applausePlayer = null;
                    return true;
                });
                applausePlayer.start();
                Log.d(TAG, "Playing applause");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing applause", e);
            applausePlayer = null;
        }
        if (applausePlayer == null) {
            applauseWaitSeconds = 0;
        }
    }

    private Uri getNextRandomUri() {
        if (selectedDances == null || currentDanceIndex >= selectedDances.size()) return null;
        String style = settings.getDanceStyle();
        String danceName = selectedDances.get(currentDanceIndex);
        return settings.getRandomAudioFromFolder(appContext, style, danceName);
    }

    private void preloadNextSong() {
        releasePlayer(preloadedPlayer);
        preloadedPlayer = null;
        preloadedUri = null;

        Uri nextUri = getNextRandomUri();
        if (nextUri == null) return;

        final Uri preloadUri = nextUri;
        MediaPlayer player = new MediaPlayer();
        lastAppliedTempo = 1.0f; // fresh MediaPlayer starts at 1.0
        player.setVolume(1.0f, 1.0f);
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());

        new Thread(() -> {
            try {
                player.setDataSource(appContext, preloadUri);
                player.prepare();
                applyTempo(player);
                postToMain(() -> {
                    if (preloadedPlayer != null) {
                        releasePlayer(preloadedPlayer);
                    }
                    preloadedPlayer = player;
                    preloadedUri = preloadUri;
                    Log.d(TAG, "Preloaded: " + preloadUri);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error preloading", e);
                player.release();
            }
        }).start();
    }

    private void playSong(Uri uri) {
        releasePlayer(mediaPlayer);
        mediaPlayer = null;
        releasePlayer(fadingPlayer);
        fadingPlayer = null;
        releasePlayer(applausePlayer);
        applausePlayer = null;

        if (uri == null) return;

        if (preloadedPlayer != null && preloadedUri != null && preloadedUri.equals(uri)) {
            mediaPlayer = preloadedPlayer;
            preloadedPlayer = null;
            preloadedUri = null;
            mediaPlayer.setOnCompletionListener(onSongComplete);
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Playback error: " + what);
                advanceFromMusicFinished();
                return true;
            });
            applyTempo(mediaPlayer);
            VolumeFader.reset();
            if (settings.isFadeEnabled()) {
                VolumeFader.fadeIn(mediaPlayer, FADE_IN_MS);
            }
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            Log.d(TAG, "Playing preloaded: " + uri);
            return;
        }

        releasePlayer(preloadedPlayer);
        preloadedPlayer = null;
        preloadedUri = null;

        final Uri playUri = uri;
        MediaPlayer player = new MediaPlayer();
        lastAppliedTempo = 1.0f; // fresh MediaPlayer starts at 1.0
        player.setVolume(1.0f, 1.0f);
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        player.setOnCompletionListener(onSongComplete);
        player.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Playback error: " + what);
            advanceFromMusicFinished();
            return true;
        });

        new Thread(() -> {
            try {
                player.setDataSource(appContext, playUri);
                player.prepare();
                applyTempo(player);
                postToMain(() -> {
                    if (isRunning && currentPhase == PHASE_MUSIC) {
                        mediaPlayer = player;
                        VolumeFader.reset();
                        if (settings.isFadeEnabled()) {
                            VolumeFader.fadeIn(mediaPlayer, FADE_IN_MS);
                        }
                        mediaPlayer.start();
                        Log.d(TAG, "Playing: " + playUri);
                    } else {
                        player.release();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error preparing", e);
                player.release();
            }
        }).start();
    }

    private void applyTempo(MediaPlayer mp) {
        if (mp == null) return;
        try {
            float tempo = settings.getTempo();
            // Push the value through even at 1.0 so returning to 100% really resets the speed
            if (Math.abs(tempo - lastAppliedTempo) > 0.001f) {
                mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(tempo));
                lastAppliedTempo = tempo;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying tempo", e);
        }
    }

    private void stopMusic() {
        stopMusic(true);
    }

    private void stopMusic(boolean naturalEnd) {
        MediaPlayer mp = mediaPlayer;
        if (mp == null) return;
        mediaPlayer = null;
        if (settings.isFadeEnabled()) {
            fadingPlayer = mp;
            int fadeMs = naturalEnd ? FADE_OUT_MS : FADE_OUT_SHORT_MS;
            Log.d(TAG, "stopMusic: fading out (fade enabled)");
            VolumeFader.fadeOut(mp, fadeMs, () -> postToMain(() -> {
                Log.d(TAG, "stopMusic: fade-out done, releasing player");
                if (fadingPlayer == mp) {
                    fadingPlayer = null;
                    releasePlayer(mp);
                }
            }));
        } else {
            Log.d(TAG, "stopMusic: fade disabled, immediate stop");
            try {
                if (mp.isPlaying()) mp.pause();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping music", e);
            }
            releasePlayer(mp);
        }
    }

    // --- Timer logic ---

    private void pushHistory() {
        history.push(new int[]{currentRound, currentDanceIndex});
    }

    private void advanceFromMusicFinished() {
        pushHistory();
        stopMusic();
        advancePhase(true);
        notifyListeners();
        if (isRunning) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private void advancePhase(boolean applauseAtEnd) {
        switch (currentPhase) {
            case PHASE_MUSIC:
                advanceFromPauseOrNextDance(applauseAtEnd);
                break;

            case PHASE_PAUSE_BETWEEN_MUSIC:
                nextDanceOrNextRound();
                break;

            case PHASE_ROUND_BREAK:
                startNextRound();
                break;

            default:
                break;
        }
    }

    private void advanceFromPauseOrNextDance(boolean applauseAtEnd) {
        boolean hasNextDance = currentDanceIndex + 1 < selectedDances.size();
        boolean hasNextRound = currentRound + 1 < totalRounds;

        if (hasNextDance) {
            int pause = settings.getMusicPause();
            if (applauseAtEnd && (settings.isApplauseEnabled() || settings.isFadeEnabled())) {
                int minGap = 0;
                if (settings.isApplauseEnabled()) {
                    playApplause();
                    minGap = Math.max(minGap, applauseWaitSeconds);
                }
                if (settings.isFadeEnabled()) {
                    minGap = Math.max(minGap, FADE_OUT_SECONDS);
                }
                currentPhase = PHASE_PAUSE_BETWEEN_MUSIC;
                timeRemaining = Math.max(pause, minGap);
            } else if (pause > 0) {
                currentPhase = PHASE_PAUSE_BETWEEN_MUSIC;
                timeRemaining = pause;
            } else {
                nextDanceOrNextRound();
            }
        } else if (hasNextRound) {
            int breakTime = settings.getBurstPause();
            if (applauseAtEnd && (settings.isApplauseEnabled() || settings.isFadeEnabled())) {
                int minGap = 0;
                if (settings.isApplauseEnabled()) {
                    playApplause();
                    minGap = Math.max(minGap, applauseWaitSeconds);
                }
                if (settings.isFadeEnabled()) {
                    minGap = Math.max(minGap, FADE_OUT_SECONDS);
                }
                currentDanceIndex++;
                currentRound++;
                currentPhase = PHASE_ROUND_BREAK;
                timeRemaining = Math.max(breakTime, minGap);
            } else if (breakTime > 0) {
                currentDanceIndex++;
                currentRound++;
                currentPhase = PHASE_ROUND_BREAK;
                timeRemaining = breakTime;
            } else {
                nextDanceOrNextRound();
            }
        } else {
            nextDanceOrNextRound();
        }
    }

    private void nextDanceOrNextRound() {
        currentDanceIndex++;
        releasePlayer(mediaPlayer);
        mediaPlayer = null;

        if (currentDanceIndex < selectedDances.size()) {
            currentPhase = PHASE_MUSIC;
            timeRemaining = settings.getMusicDuration();
            Uri song = getNextRandomUri();
            playSong(song);
            preloadNextSongForNext();
        } else {
            currentRound++;
            if (currentRound < totalRounds) {
                int breakTime = settings.getBurstPause();
                if (breakTime > 0) {
                    currentPhase = PHASE_ROUND_BREAK;
                    timeRemaining = breakTime;
                } else {
                    startNextRound();
                }
            } else {
                finishSession();
            }
        }
    }

    private void finishSession() {
        timerHandler.removeCallbacks(timerRunnable);

        final MediaPlayer mp;
        synchronized (this) {
            if (!isRunning) return;
            finishing = true;
            currentPhase = PHASE_IDLE;
            currentDanceIndex = 0;
            currentRound = 0;
            timeRemaining = 0;
            history.clear();
            mp = mediaPlayer;
            mediaPlayer = null;
        }

        releasePlayer(preloadedPlayer);
        preloadedPlayer = null;
        preloadedUri = null;

        if (mp == null) {
            if (fadingPlayer != null) {
                uiHandler.postDelayed(this::completeSession, FADE_OUT_MS);
            } else {
                completeSession();
            }
            return;
        }

        if (settings.isFadeEnabled()) {
            Log.d(TAG, "finishSession: fading out final dance");
            fadingPlayer = mp;
            VolumeFader.fadeOut(mp, FADE_OUT_MS, () -> postToMain(() -> {
                if (fadingPlayer == mp) {
                    fadingPlayer = null;
                    releasePlayer(mp);
                }
                completeSession();
            }));
        } else {
            releasePlayer(mp);
            completeSession();
        }
        notifyListeners();
    }

    private void completeSession() {
        synchronized (this) {
            if (!finishing) return;
            finishing = false;
            isRunning = false;
            currentPhase = PHASE_IDLE;
        }
        if (focusManager != null) {
            focusManager.abandon();
        }
        playApplause();
        notifyListeners();
    }

    private void startNextRound() {
        currentDanceIndex = 0;
        currentPhase = PHASE_MUSIC;
        timeRemaining = settings.getMusicDuration();
        Uri song = getNextRandomUri();
        playSong(song);
        preloadNextSongForNext();
    }

    private void preloadNextSongForNext() {
        if (currentDanceIndex + 1 < selectedDances.size()) {
            currentDanceIndex++;
            preloadNextSong();
            currentDanceIndex--;
        } else if (currentRound + 1 < totalRounds) {
            preloadNextSong();
        }
    }

    private void postToMain(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            uiHandler.post(r);
        }
    }
}
