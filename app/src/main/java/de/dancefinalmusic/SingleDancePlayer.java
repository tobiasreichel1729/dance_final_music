package de.dancefinalmusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import de.dancefinalmusic.util.DanceCodeFilter;
import de.dancefinalmusic.util.MusicFocusManager;
import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.VolumeFader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SingleDancePlayer {

    private static final String TAG = "SingleDancePlayer";
    private static final long FADE_MS = 800;

    private static SingleDancePlayer instance;

    private SettingsManager settings;
    private Context appContext;
    private MediaPlayer player;
    private MusicFocusManager focusManager;
    private String style;
    private String danceName;
    private boolean preparing;

    private List<Uri> queue = new ArrayList<>();
    private int currentIndex = -1;
    private String currentName = "";
    private int currentNominalBpm = 0;
    private Bitmap currentArt;
    private boolean fadingOut;
    private int generation = 0;
    private float lastAppliedSpeed = 1.0f;

    public interface StateListener {
        void onPlaybackStateChanged();
    }

    private final List<WeakReference<StateListener>> listeners = new ArrayList<>();

    private SingleDancePlayer() {
    }

    public static synchronized SingleDancePlayer getInstance() {
        if (instance == null) {
            instance = new SingleDancePlayer();
        }
        return instance;
    }

    public synchronized boolean isPlaying() {
        return player != null || preparing;
    }

    public synchronized String getPlayingDance() {
        return danceName;
    }

    public synchronized List<Uri> getQueue() {
        return new ArrayList<>(queue);
    }

    public synchronized int getCurrentIndex() {
        return currentIndex;
    }

    public synchronized String getCurrentName() {
        return currentName;
    }

    public synchronized int getCurrentNominalBpm() {
        return currentNominalBpm;
    }

    public synchronized boolean isPaused() {
        return player != null && !preparing && !player.isPlaying();
    }

    public synchronized Uri getCurrentUri() {
        if (currentIndex < 0 || currentIndex >= queue.size()) return null;
        return queue.get(currentIndex);
    }

    public synchronized int getCurrentPositionMs() {
        if (player == null) return 0;
        try {
            return player.getCurrentPosition();
        } catch (Exception e) {
            return 0;
        }
    }

    public synchronized int getCurrentDurationMs() {
        if (player == null) return 0;
        try {
            return player.getDuration();
        } catch (Exception e) {
            return 0;
        }
    }

    public synchronized float getCurrentSpeed() {
        if (player == null || preparing) return 0f;
        try {
            return player.getPlaybackParams().getSpeed();
        } catch (Exception e) {
            return 0f;
        }
    }

    /** Embedded cover of the current track, or null while it is still being read. */
    public synchronized Bitmap getCurrentArt() {
        return currentArt;
    }

    private void loadArtAsync(final Uri uri) {
        currentArt = null;
        if (uri == null) return;
        new Thread(() -> {
            Bitmap art = null;
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(appContext, uri);
                byte[] data = mmr.getEmbeddedPicture();
                if (data != null) {
                    art = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            } catch (Exception e) {
                art = null;
            } finally {
                try {
                    mmr.release();
                } catch (Exception ignored) {
                }
            }
            final Bitmap result = art;
            synchronized (SingleDancePlayer.this) {
                if (generation > 0) currentArt = result;
            }
            notifyListeners();
        }).start();
    }

    public void togglePlayPause() {
        final MediaPlayer mp;
        synchronized (this) {
            if (player == null) return;
            mp = player;
        }
        try {
            if (mp.isPlaying()) {
                mp.pause();
            } else {
                VolumeFader.reset();
                mp.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling play/pause", e);
        }
        notifyListeners();
    }

    public void seekTo(int positionMs) {
        final MediaPlayer mp;
        synchronized (this) {
            if (player == null) return;
            mp = player;
        }
        try {
            if (positionMs < 0) positionMs = 0;
            int dur = mp.getDuration();
            if (dur > 0 && positionMs > dur) positionMs = dur;
            mp.seekTo(positionMs);
        } catch (Exception e) {
            Log.e(TAG, "Error seeking", e);
        }
    }

    public void next() {
        synchronized (this) {
            if (queue.isEmpty()) return;
            playAt((currentIndex + 1) % queue.size());
        }
    }

    public void previous() {
        synchronized (this) {
            if (queue.isEmpty()) return;
            if (queue.size() == 1) {
                seekTo(0);
                return;
            }
            playAt((currentIndex - 1 + queue.size()) % queue.size());
        }
    }

    public String getQueueName(int index) {
        final Uri uri;
        final String folder;
        final String dance;
        synchronized (this) {
            if (index < 0 || index >= queue.size()) return "";
            uri = queue.get(index);
            folder = style != null && danceName != null
                    ? settings.getMusicFolderPath(style, danceName) : null;
            dance = danceName;
        }
        String name = folder != null ? settings.getCachedFileName(folder, dance, uri) : null;
        if (name == null) {
            name = settings.getFileName(appContext, uri);
        }
        return cleanName(name) != null ? cleanName(name) : "";
    }

    public String getQueueNominalBpm(int index) {
        String name = getQueueName(index);
        int bpm = DanceCodeFilter.parseNominalBpm(name);
        return bpm > 0 ? String.valueOf(bpm) : "";
    }

    public float computeSpeedPct(int target, int nominal) {
        if (target > 0 && nominal > 0) {
            return Math.max(0.5f, Math.min(2.0f, target / (float) nominal)) * 100f;
        }
        return settings.getTempo() * 100f;
    }

    public void addListener(StateListener listener) {
        synchronized (listeners) {
            listeners.add(new WeakReference<>(listener));
        }
    }

    public void removeListener(StateListener listener) {
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
            l.onPlaybackStateChanged();
        }
    }

    public void start(Context context, String style, String danceName) {
        synchronized (this) {
            if (this.danceName != null && this.danceName.equals(danceName) && (player != null || preparing)) {
                return;
            }
            stopInternal();
            this.style = style;
            this.danceName = danceName;
            this.preparing = true;
        }
        this.settings = SettingsManager.getInstance(context);
        this.appContext = context.getApplicationContext();

        List<Uri> files = settings.getDanceAudioFiles(context, style, danceName);
        synchronized (this) {
            generation++;
            if (files.isEmpty()) {
                stopInternal();
                this.style = null;
                this.danceName = null;
                this.preparing = false;
                queue = new ArrayList<>();
                currentIndex = -1;
                currentName = "";
                currentNominalBpm = 0;
                notifyListeners();
                return;
            }
            queue = new ArrayList<>(files);
            Collections.shuffle(queue);
            currentIndex = -1;
            currentName = "";
            currentNominalBpm = 0;
        }
        if (focusManager == null) {
            focusManager = new MusicFocusManager(appContext, new MusicFocusManager.FocusListener() {
                @Override
                public void onFocusLoss() {
                    stop();
                }

                @Override
                public void onFocusGain() {
                }
            });
        }
        focusManager.request();
        notifyListeners();
        try {
            appContext.startForegroundService(new Intent(appContext, DanceSessionService.class)
                    .setAction(DanceSessionService.ACTION_START));
        } catch (Exception e) {
            Log.e(TAG, "Error starting foreground service", e);
        }
        playNext();
    }

    public void stop() {
        final int gen;
        final MediaPlayer mp;
        synchronized (this) {
            if (fadingOut) {
                return;
            }
            gen = ++generation;
            mp = player;
            queue = new ArrayList<>();
            currentIndex = -1;
            currentName = "";
            currentNominalBpm = 0;
            fadingOut = true;
        }
        fadeOrStop(mp, () -> {
            synchronized (SingleDancePlayer.this) {
                if (gen != generation) return;
                stopInternal();
                style = null;
                danceName = null;
            }
            if (focusManager != null) {
                focusManager.abandon();
            }
            notifyListeners();
        });
    }

    public synchronized boolean isFadingOut() {
        return fadingOut;
    }

    public void playAt(int index) {
        synchronized (this) {
            if (index < 0 || index >= queue.size()) return;
            generation++;
            currentIndex = index;
            currentName = "";
            currentNominalBpm = 0;
        }
        notifyListeners();
        playCurrent();
    }

    public void updateTempo() {
        MediaPlayer mp;
        int nominal;
        synchronized (this) {
            mp = player;
            nominal = currentNominalBpm;
        }
        if (mp != null) {
            applyTempo(mp, nominal);
        }
    }

    private void stopInternal() {
        fadingOut = false;
        if (player != null) {
            try {
                player.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing player", e);
            }
            player = null;
        }
        preparing = false;
    }

    private void fadeOrStop(MediaPlayer mp, Runnable onStopped) {
        if (mp == null || settings == null || !settings.isFadeEnabled()) {
            onStopped.run();
            return;
        }
        VolumeFader.fadeOut(mp, (int) FADE_MS, onStopped);
    }

    private void playNext() {
        synchronized (this) {
            if (queue.isEmpty()) return;
            generation++;
            currentIndex = (currentIndex + 1) % queue.size();
            currentName = "";
            currentNominalBpm = 0;
        }
        playCurrent();
    }

    private void playCurrent() {
        final String wantedDance;
        final String wantedStyle;
        final Uri uri;
        final int gen;
        synchronized (this) {
            if (currentIndex < 0 || currentIndex >= queue.size()) return;
            uri = queue.get(currentIndex);
            wantedDance = danceName;
            wantedStyle = style;
            gen = generation;
        }
        if (wantedDance == null || appContext == null || uri == null) return;

        final String folder = settings.getMusicFolderPath(wantedStyle, wantedDance);
        String name = settings.getCachedFileName(folder, wantedDance, uri);
        if (name == null) {
            name = settings.getFileName(appContext, uri);
        }
        final String trackName = name;
        final int nominalBpm = DanceCodeFilter.parseNominalBpm(trackName != null ? trackName : uri.toString());

        final MediaPlayer mp = new MediaPlayer();
        mp.setVolume(1.0f, 1.0f);
        synchronized (this) {
            lastAppliedSpeed = 1.0f; // a fresh MediaPlayer always starts at 1.0
        }
        mp.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        mp.setOnCompletionListener(mp2 -> {
            synchronized (SingleDancePlayer.this) {
                currentName = "";
                currentNominalBpm = 0;
            }
            playNext();
        });
        mp.setOnErrorListener((mp2, what, extra) -> {
            Log.e(TAG, "Playback error: " + what);
            playNext();
            return true;
        });

        new Thread(() -> {
            try {
                mp.setDataSource(appContext, uri);
                mp.prepare();
                applyTempo(mp, nominalBpm);
                synchronized (SingleDancePlayer.this) {
                    if (gen != generation) {
                        mp.release();
                        return;
                    }
                    if (player != null) {
                        try {
                            player.release();
                        } catch (Exception e) {
                            Log.e(TAG, "Error releasing player", e);
                        }
                        player = null;
                    }
                    if (!wantedDance.equals(danceName)) {
                        mp.release();
                        return;
                    }
                    player = mp;
                    preparing = false;
                    currentName = cleanName(trackName);
                    currentNominalBpm = nominalBpm;
                    if (nominalBpm > 0) {
                        settings.setReferenceBpm(danceName, nominalBpm);
                    }
                    loadArtAsync(uri);
                }
                VolumeFader.reset();
                if (settings.isFadeEnabled()) {
                    VolumeFader.fadeIn(mp, 500);
                }
                mp.start();
                notifyListeners();
            } catch (Exception e) {
                Log.e(TAG, "Error preparing", e);
                try {
                    mp.release();
                } catch (Exception ignored) {
                }
                playNext();
            }
        }).start();
    }

    private void applyTempo(MediaPlayer mp, int nominalBpm) {
        if (mp == null) return;
        try {
            float speed = computeSpeed(nominalBpm);
            Log.d(TAG, "applyTempo: mode=" + (settings.getSpeedMode() == SettingsManager.SPEED_MODE_BPM ? "BPM" : "SPEED")
                    + " nominal=" + nominalBpm + " target=" + settings.getBpmTarget(currentDanceName())
                    + " tempo=" + settings.getTempo() + " -> speed=" + speed);
            // Always push the value through, including 1.0, otherwise switching back to
            // 100% would keep the old speed until the user drags the slider.
            if (Math.abs(speed - lastAppliedSpeed) > 0.001f) {
                mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(speed));
                lastAppliedSpeed = speed;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying tempo", e);
        }
    }

    private synchronized String currentDanceName() {
        return danceName;
    }

    private float computeSpeed(int nominalBpm) {
        // The two settings are independent: speed mode always uses the stored tempo,
        // BPM mode uses the per-dance target and only falls back to the tempo when unset.
        if (settings.getSpeedMode() == SettingsManager.SPEED_MODE_BPM) {
            String dance;
            synchronized (this) {
                dance = danceName;
            }
            int target = settings.getBpmTarget(dance);
            int baseline = settings.getEffectiveNominalBpm(dance, nominalBpm);
            if (target > 0 && baseline > 0) {
                return Math.max(0.5f, Math.min(2.0f, target / (float) baseline));
            }
        }
        return settings.getTempo();
    }

    private static String cleanName(String name) {
        if (name == null) return null;
        int dot = name.lastIndexOf('.');
        if (dot > 0 && name.length() - dot <= 5) {
            return name.substring(0, dot);
        }
        return name;
    }
}
