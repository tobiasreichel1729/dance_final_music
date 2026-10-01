package de.dancefinalmusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.Translations;

public class DanceSessionService extends Service
        implements DanceSession.StateListener, SingleDancePlayer.StateListener {

    private static final String CHANNEL_ID = "dance_session";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_START = "de.dancefinalmusic.action.START";
    public static final String ACTION_STOP = "de.dancefinalmusic.action.STOP";
    public static final String ACTION_SKIP = "de.dancefinalmusic.action.SKIP";
    public static final String ACTION_BACK = "de.dancefinalmusic.action.BACK";
    public static final String ACTION_PLAY = "de.dancefinalmusic.action.PLAY";
    public static final String ACTION_PAUSE = "de.dancefinalmusic.action.PAUSE";
    public static final String ACTION_NEXT = "de.dancefinalmusic.action.NEXT";
    public static final String ACTION_PREV = "de.dancefinalmusic.action.PREV";

    private static final String WAKELOCK_TAG = "de.dancefinalmusic:session";

    private DanceSession session;
    private PowerManager.WakeLock wakeLock;
    private MediaSessionCompat mediaSession;
    private Bitmap placeholderArt;
    private Bitmap scaledArt;
    private Bitmap scaledArtSource;
    private static final int ART_SIZE = 512;

    @Override
    public void onCreate() {
        super.onCreate();
        session = DanceSession.getInstance();
        session.addListener(this);
        SingleDancePlayer.getInstance().addListener(this);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire();
        }
        initMediaSession();
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "DanceSessionService");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                SingleDancePlayer player = SingleDancePlayer.getInstance();
                if (player.isPlaying() && player.isPaused()) {
                    player.togglePlayPause();
                }
                publishMediaState();
            }

            @Override
            public void onPause() {
                SingleDancePlayer player = SingleDancePlayer.getInstance();
                if (player.isPlaying() && !player.isPaused()) {
                    player.togglePlayPause();
                }
                publishMediaState();
            }

            @Override
            public void onStop() {
                stopEverything();
                publishMediaState();
            }

            @Override
            public void onSkipToNext() {
                SingleDancePlayer.getInstance().next();
                publishMediaState();
            }

            @Override
            public void onSkipToPrevious() {
                SingleDancePlayer.getInstance().previous();
                publishMediaState();
            }

            @Override
            public void onSeekTo(long pos) {
                SingleDancePlayer.getInstance().seekTo((int) pos);
                publishMediaState();
            }
        });
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        publishMediaState();
    }

    private void stopEverything() {
        if (session.isRunning()) {
            session.stopDance();
        }
        if (SingleDancePlayer.getInstance().isPlaying()) {
            SingleDancePlayer.getInstance().stop();
        }
    }

    private Bitmap getAlbumArt() {
        Bitmap trackArt = SingleDancePlayer.getInstance().getCurrentArt();
        if (trackArt != null) {
            // Keyed on bitmap identity, not size: different tracks of the same dance
            // (or two dances) frequently have covers of identical dimensions, and a
            // size check would keep serving the previous track's artwork.
            if (scaledArt == null || scaledArtSource != trackArt) {
                scaledArt = scaleArt(trackArt);
                scaledArtSource = trackArt;
            }
            return scaledArt;
        }
        if (placeholderArt == null) {
            // A raster drawable: the adaptive launcher icon cannot be decoded by BitmapFactory
            placeholderArt = BitmapFactory.decodeResource(getResources(), R.drawable.album_placeholder);
        }
        return placeholderArt;
    }

    // Lock screen and shade panels need a reasonably large bitmap; embedded covers are often tiny
    private static Bitmap scaleArt(Bitmap src) {
        int minSide = Math.min(src.getWidth(), src.getHeight());
        if (minSide >= ART_SIZE) return src;
        float factor = (float) ART_SIZE / Math.max(1, minSide);
        int w = Math.max(1, Math.round(src.getWidth() * factor));
        int h = Math.max(1, Math.round(src.getHeight() * factor));
        try {
            return Bitmap.createScaledBitmap(src, w, h, true);
        } catch (Exception e) {
            return src;
        }
    }

    private void publishMediaState() {
        if (mediaSession == null) return;

        String lang = SettingsManager.getInstance(this).getLanguage();
        boolean timerActive = session.isActive();
        boolean playerActive = SingleDancePlayer.getInstance().isPlaying();

        String title;
        String artist;
        if (timerActive) {
            String dance = session.getCurrentDanceName();
            title = dance != null
                    ? Translations.getDanceName(lang, dance) : getString(R.string.app_name);
            artist = getString(R.string.app_name);
        } else if (playerActive) {
            String dance = SingleDancePlayer.getInstance().getPlayingDance();
            String track = SingleDancePlayer.getInstance().getCurrentName();
            title = track != null && !track.isEmpty() ? track : getString(R.string.app_name);
            artist = dance != null && !dance.isEmpty()
                    ? Translations.getDanceName(lang, dance) : getString(R.string.app_name);
        } else {
            title = getString(R.string.app_name);
            artist = Translations.getFinished(lang);
        }

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, getString(R.string.app_name))
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                        SingleDancePlayer.getInstance().getCurrentDurationMs())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getAlbumArt())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, getAlbumArt())
                .build());

        long actions = PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_STOP
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;

        int state;
        if (!timerActive && !playerActive) {
            state = PlaybackStateCompat.STATE_STOPPED;
            actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP;
        } else if (timerActive) {
            state = PlaybackStateCompat.STATE_PLAYING;
        } else {
            state = SingleDancePlayer.getInstance().isPaused()
                    ? PlaybackStateCompat.STATE_PAUSED : PlaybackStateCompat.STATE_PLAYING;
            actions |= PlaybackStateCompat.ACTION_SEEK_TO;
        }

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(state, SingleDancePlayer.getInstance().getCurrentPositionMs(),
                        timerActive ? 1.0f : SingleDancePlayer.getInstance().getCurrentSpeed())
                .build());

        mediaSession.setActive(timerActive || playerActive);
        scheduleRefresh();
    }

    // Keeps position and speed in the media session (lock screen, Bluetooth, ...) current
    private void scheduleRefresh() {
        handler.removeCallbacks(refreshTask);
        if (!session.isActive() && !SingleDancePlayer.getInstance().isPlaying()) return;
        handler.postDelayed(refreshTask, 1000);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            publishMediaState();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_STOP.equals(action)) {
            stopEverything();
            stopSelf();
            return START_NOT_STICKY;
        }
        if (ACTION_SKIP.equals(action)) {
            session.skipForward();
            return START_NOT_STICKY;
        }
        if (ACTION_BACK.equals(action)) {
            session.goBack();
            return START_NOT_STICKY;
        }
        if (ACTION_PLAY.equals(action) || ACTION_PAUSE.equals(action)
                || ACTION_NEXT.equals(action) || ACTION_PREV.equals(action)) {
            handleTransport(action);
            publishMediaState();
            return START_NOT_STICKY;
        }

        startInForeground();
        publishMediaState();
        if (!session.isActive() && !SingleDancePlayer.getInstance().isPlaying()) {
            stopForegroundCompat();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void handleTransport(String action) {
        SingleDancePlayer player = SingleDancePlayer.getInstance();
        switch (action) {
            case ACTION_PLAY:
                if (player.isPlaying() && player.isPaused()) {
                    player.togglePlayPause();
                }
                break;
            case ACTION_PAUSE:
                if (player.isPlaying() && !player.isPaused()) {
                    player.togglePlayPause();
                }
                break;
            case ACTION_NEXT:
                player.next();
                break;
            case ACTION_PREV:
                player.previous();
                break;
            default:
                break;
        }
    }

    private void startInForeground() {
        Notification notification = buildNotification();
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
    }

    private Notification buildNotification() {
        createChannel();

        String lang = SettingsManager.getInstance(this).getLanguage();
        String appName = getString(R.string.app_name);

        String title = appName;
        String text = "";
        boolean timerRunning = session.isActive();
        boolean playerPlaying = SingleDancePlayer.getInstance().isPlaying();

        if (timerRunning) {
            String dance = session.getCurrentDanceName();
            if (dance != null) {
                title = Translations.getDanceName(lang, dance);
            }
            StringBuilder sb = new StringBuilder();
            int phase = session.getPhase();
            if (phase == DanceSession.PHASE_PAUSE_BETWEEN_MUSIC) {
                sb.append(Translations.getPauseLabel(lang));
            } else if (phase == DanceSession.PHASE_ROUND_BREAK) {
                sb.append(Translations.getPauseLabel(lang))
                        .append(" (").append(session.getCurrentRound() + 1)
                        .append("/").append(session.getTotalRounds()).append(")");
            } else if (phase == DanceSession.PHASE_MUSIC) {
                sb.append(Translations.getCurrentDance(lang));
            }
            if (sb.length() > 0) {
                sb.append(" – ");
            }
            sb.append(formatTime(session.getTimeRemaining()));
String next = session.getNextDanceName();
            if (next != null) {
                sb.append("  ·  ").append("→ ").append(Translations.getDanceName(lang, next));
            }
            text = sb.toString();
        } else if (playerPlaying) {
            String dance = SingleDancePlayer.getInstance().getPlayingDance();
            String track = SingleDancePlayer.getInstance().getCurrentName();
            if (dance != null && !dance.isEmpty()) {
                title = Translations.getDanceName(lang, dance);
            }
            StringBuilder sb = new StringBuilder();
            if (track != null && !track.isEmpty()) {
                sb.append(track);
            }
            int nominal = SingleDancePlayer.getInstance().getCurrentNominalBpm();
            if (nominal > 0) {
                sb.append(sb.length() > 0 ? "  ·  " : "").append(nominal).append(" BPM");
            }
            text = sb.toString();
        } else {
            text = Translations.getFinished(lang);
        }

        Intent contentIntent;
        if (session.isActive()) {
            contentIntent = new Intent(this, DanceTimerActivity.class);
        } else if (SingleDancePlayer.getInstance().isPlaying()) {
            contentIntent = new Intent(this, PlayerActivity.class);
        } else {
            contentIntent = new Intent(this, MainActivity.class);
        }
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentPi = PendingIntent.getActivity(this, 0, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent skipIntent = new Intent(this, DanceSessionService.class).setAction(ACTION_SKIP);
        PendingIntent skipPi = PendingIntent.getForegroundService(this, 1, skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, DanceSessionService.class).setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getForegroundService(this, 2, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        boolean showTransport = !session.isActive() && playerPlaying;

        NotificationCompat.Action skipAction = new NotificationCompat.Action.Builder(0,
                Translations.getSkip(lang), skipPi).build();
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(0,
                Translations.getStop(lang), stopPi).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(contentPi)
                .setOngoing(timerRunning || playerPlaying)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(getAlbumArt())
                .addAction(skipAction)
                .addAction(stopAction);

        MediaStyle mediaStyle = new MediaStyle()
                .setMediaSession(mediaSession.getSessionToken());

        if (showTransport) {
            boolean paused = SingleDancePlayer.getInstance().isPaused();
            builder.addAction(new NotificationCompat.Action.Builder(
                            R.drawable.ic_skip_previous, "", transportPi(ACTION_PREV, 3)).build());
            builder.addAction(new NotificationCompat.Action.Builder(
                            paused ? R.drawable.ic_play : R.drawable.ic_pause, "",
                            transportPi(paused ? ACTION_PLAY : ACTION_PAUSE, 4)).build());
            builder.addAction(new NotificationCompat.Action.Builder(
                            R.drawable.ic_skip_next, "", transportPi(ACTION_NEXT, 5)).build());
            mediaStyle.setShowActionsInCompactView(2, 3, 4);
        } else {
            mediaStyle.setShowActionsInCompactView(0, 1);
        }

        builder.setStyle(mediaStyle);
        return builder.build();
    }

    private PendingIntent transportPi(String action, int requestCode) {
        Intent intent = new Intent(this, DanceSessionService.class).setAction(action);
        return PendingIntent.getService(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createChannel() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW));
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onStateChanged() {
        publishMediaState();
        if (!session.isActive() && !SingleDancePlayer.getInstance().isPlaying()) {
            stopForegroundCompat();
            stopSelf();
            return;
        }
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    @Override
    public void onPlaybackStateChanged() {
        // Cover art is read on a worker thread, so this callback can arrive off the
        // main thread; media session and notification updates must happen on it.
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onStateChanged();
        } else {
            handler.post(this::onStateChanged);
        }
    }

    private void stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(refreshTask);
        session.removeListener(this);
        SingleDancePlayer.getInstance().removeListener(this);
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }
}
