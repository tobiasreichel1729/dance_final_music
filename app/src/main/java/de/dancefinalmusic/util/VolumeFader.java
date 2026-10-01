package de.dancefinalmusic.util;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

public final class VolumeFader {

    private static final int STEPS = 20;
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static float lastVolume = 1.0f;

    private VolumeFader() {
    }

    public static float getLastVolume() {
        return lastVolume;
    }

    public static void reset() {
        lastVolume = 1.0f;
    }

    public static void fadeOut(MediaPlayer mp, int durationMs, Runnable onDone) {
        if (mp == null) {
            if (onDone != null) onDone.run();
            return;
        }
        fade(mp, lastVolume, 0.0f, durationMs, onDone);
    }

    public static void fadeIn(MediaPlayer mp, int durationMs) {
        if (mp == null) return;
        try {
            mp.setVolume(0.0f, 0.0f);
        } catch (Exception ignored) {
        }
        fade(mp, 0.0f, 1.0f, durationMs, null);
    }

    private static void fade(final MediaPlayer mp, float from, float to, int durationMs, final Runnable onDone) {
        final long delay = Math.max(10, durationMs / STEPS);
        final float[] current = {from};
        final Runnable step = new Runnable() {
            @Override
            public void run() {
                if (to > from) {
                    current[0] += (to - from) / STEPS;
                    if (current[0] > to) current[0] = to;
                } else {
                    current[0] -= (from - to) / STEPS;
                    if (current[0] < to) current[0] = to;
                }
                try {
                    mp.setVolume(current[0], current[0]);
                    lastVolume = current[0];
                } catch (Exception ignored) {
                }
                if (Math.abs(current[0] - to) > 0.001f) {
                    HANDLER.postDelayed(this, delay);
                } else if (onDone != null) {
                    onDone.run();
                }
            }
        };
        HANDLER.postDelayed(step, delay);
    }
}
