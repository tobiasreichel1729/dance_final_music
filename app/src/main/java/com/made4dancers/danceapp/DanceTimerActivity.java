package com.made4dancers.danceapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.made4dancers.danceapp.util.SettingsManager;
import com.made4dancers.danceapp.util.ThemeHelper;
import com.made4dancers.danceapp.util.Translations;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DanceTimerActivity extends AppCompatActivity {

    private static final String TAG = "DanceTimer";

    private static final int PHASE_IDLE = 0;
    private static final int PHASE_MUSIC = 1;
    private static final int PHASE_PAUSE_BETWEEN_MUSIC = 2;
    private static final int PHASE_ROUND_BREAK = 3;

    private SettingsManager settings;
    private String theme;
    private int accentIndex;
    private int accentColor;
    private String lang;

    private TextView headerTitle;
    private ImageView backBtn;
    private TextView phaseLabel;
    private TextView nextDanceLabel;
    private FrameLayout timerCircle;
    private TextView timeText;
    private TextView statusText;
    private TextView progressText;
    private LinearLayout dotsContainer;
    private Button startStopButton;
    private LinearLayout skipBackRow;
    private Button skipDanceBtn;
    private Button backDanceBtn;

    private int currentPhase = PHASE_IDLE;
    private int currentDanceIndex = 0;
    private int currentRound = 0;
    private int totalRounds = 1;
    private boolean isRunning = false;
    private int timeRemaining = 0;

    private List<String> selectedDances;

    private MediaPlayer mediaPlayer;
    private MediaPlayer preloadedPlayer;
    private Uri preloadedUri;

    private final Stack<int[]> history = new Stack<>();

    private GestureDetector gestureDetector;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            timeRemaining--;

            if (timeRemaining <= 0) {
                timeRemaining = 0;
                stopMusic();
                advancePhase();
            }

            updateUI();
            if (isRunning) {
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    private final MediaPlayer.OnCompletionListener onSongComplete = mp -> {
        if (!isRunning || currentPhase != PHASE_MUSIC) return;
        advanceFromMusicFinished();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dance_timer);

        settings = SettingsManager.getInstance(this);
        theme = settings.getTheme();
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        lang = settings.getLanguage();

        bindViews();
        setupGestures();
        applyTheme();
        updateLabels();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = settings.getTheme();
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        lang = settings.getLanguage();
        applyTheme();
        updateLabels();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        isRunning = false;
        releaseAllPlayers();
    }

    @Override
    public void onBackPressed() {
        if (isRunning && !history.isEmpty()) {
            goBack();
        } else {
            stopDance();
            super.onBackPressed();
        }
    }

    private void bindViews() {
        headerTitle = findViewById(R.id.headerTitle);
        backBtn = findViewById(R.id.backBtn);
        phaseLabel = findViewById(R.id.phaseLabel);
        nextDanceLabel = findViewById(R.id.nextDanceLabel);
        timerCircle = findViewById(R.id.timerCircle);
        timeText = findViewById(R.id.timeText);
        statusText = findViewById(R.id.statusText);
        progressText = findViewById(R.id.progressText);
        dotsContainer = findViewById(R.id.dotsContainer);
        startStopButton = findViewById(R.id.startStopButton);
        skipBackRow = findViewById(R.id.skipBackRow);
        skipDanceBtn = findViewById(R.id.skipDanceBtn);
        backDanceBtn = findViewById(R.id.backDanceBtn);

        backBtn.setOnClickListener(v -> {
            if (isRunning && !history.isEmpty()) {
                goBack();
            } else {
                stopDance();
                finish();
            }
        });

        startStopButton.setOnClickListener(v -> {
            if (isRunning) {
                stopDance();
            } else {
                startDance();
            }
        });

        skipDanceBtn.setOnClickListener(v -> {
            if (isRunning) {
                skipForward();
            }
        });

        backDanceBtn.setOnClickListener(v -> {
            if (isRunning && !history.isEmpty()) {
                goBack();
            }
        });
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            if (isRunning && !history.isEmpty()) {
                                goBack();
                            }
                        } else {
                            if (isRunning) {
                                skipForward();
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    // --- Skip / Back ---

    private void skipForward() {
        if (!isRunning) return;

        pushHistory();
        stopMusic();
        advancePhase();
        updateUI();
        if (isRunning) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private void goBack() {
        if (!isRunning || history.isEmpty()) return;

        stopMusic();
        timerHandler.removeCallbacks(timerRunnable);

        int[] prev = history.pop();
        currentRound = prev[0];
        currentDanceIndex = prev[1];

        currentPhase = PHASE_MUSIC;
        timeRemaining = settings.getMusicDuration();
        Uri song = getNextRandomUri();
        playSong(song);
        preloadNextSongForNext();
        updateUI();
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void pushHistory() {
        history.push(new int[]{currentRound, currentDanceIndex});
    }

    // --- Music playback with preloading ---

    private void releaseAllPlayers() {
        releasePlayer(mediaPlayer);
        mediaPlayer = null;
        releasePlayer(preloadedPlayer);
        preloadedPlayer = null;
        preloadedUri = null;
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

    private Uri getNextRandomUri() {
        if (selectedDances == null || currentDanceIndex >= selectedDances.size()) return null;
        String style = settings.getDanceStyle();
        String danceName = selectedDances.get(currentDanceIndex);
        return settings.getRandomAudioFromFolder(this, style, danceName);
    }

    private void preloadNextSong() {
        releasePlayer(preloadedPlayer);
        preloadedPlayer = null;
        preloadedUri = null;

        Uri nextUri = getNextRandomUri();
        if (nextUri == null) return;

        final Uri preloadUri = nextUri;
        MediaPlayer player = new MediaPlayer();
        player.setVolume(1.0f, 1.0f);

        new Thread(() -> {
            try {
                player.setDataSource(this, preloadUri);
                player.prepare();
                runOnUiThread(() -> {
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
        player.setVolume(1.0f, 1.0f);
        player.setOnCompletionListener(onSongComplete);
        player.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Playback error: " + what);
            advanceFromMusicFinished();
            return true;
        });

        new Thread(() -> {
            try {
                player.setDataSource(this, playUri);
                player.prepare();
                runOnUiThread(() -> {
                    if (isRunning && currentPhase == PHASE_MUSIC) {
                        mediaPlayer = player;
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

    private void stopMusic() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping music", e);
            }
        }
    }

    // --- Timer logic ---

    private void startDance() {
        selectedDances = settings.getSelectedDancesList();
        if (selectedDances.isEmpty()) return;

        currentDanceIndex = 0;
        currentRound = 0;
        totalRounds = settings.getBurstCount();
        if (totalRounds < 1) totalRounds = 1;
        isRunning = true;
        history.clear();

        currentPhase = PHASE_MUSIC;
        timeRemaining = settings.getMusicDuration();

        Uri firstSong = getNextRandomUri();
        playSong(firstSong);
        preloadNextSongForNext();

        updateUI();
        timerHandler.postDelayed(timerRunnable, 1000);
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

    private void stopDance() {
        timerHandler.removeCallbacks(timerRunnable);
        isRunning = false;
        currentPhase = PHASE_IDLE;
        currentDanceIndex = 0;
        currentRound = 0;
        timeRemaining = 0;
        history.clear();
        releaseAllPlayers();
        updateUI();
    }

    private void advanceFromMusicFinished() {
        pushHistory();
        stopMusic();
        advancePhase();
        updateUI();
        if (isRunning) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private void advancePhase() {
        switch (currentPhase) {
            case PHASE_MUSIC:
                advanceFromPauseOrNextDance();
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

    private void advanceFromPauseOrNextDance() {
        int pause = settings.getMusicPause();
        if (pause > 0) {
            currentPhase = PHASE_PAUSE_BETWEEN_MUSIC;
            timeRemaining = pause;
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
                stopDance();
            }
        }
    }

    private void startNextRound() {
        currentDanceIndex = 0;
        currentPhase = PHASE_MUSIC;
        timeRemaining = settings.getMusicDuration();
        Uri song = getNextRandomUri();
        playSong(song);
        preloadNextSongForNext();
    }

    // --- UI ---

    private void applyTheme() {
        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int surface = ThemeHelper.getSurfaceColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceVariantColor(theme);

        findViewById(android.R.id.content).getRootView().setBackgroundColor(bg);

        if (headerTitle != null) headerTitle.setTextColor(onBg);
        if (backBtn != null) {
            backBtn.setImageTintList(ColorStateList.valueOf(onBg));
            backBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }
        if (phaseLabel != null) phaseLabel.setTextColor(accentColor);
        if (nextDanceLabel != null) nextDanceLabel.setTextColor(onSurface);
        if (timeText != null) timeText.setTextColor(onBg);
        if (statusText != null) statusText.setTextColor(accentColor);
        if (progressText != null) progressText.setTextColor(onSurface);

        if (timerCircle != null) {
            int timerBg = ThemeHelper.getTimerBgColor(accentIndex);
            GradientDrawable timerDrawable = new GradientDrawable();
            timerDrawable.setShape(GradientDrawable.OVAL);
            timerDrawable.setColor(timerBg);
            timerDrawable.setStroke(dpToPx(3), accentColor);
            timerCircle.setBackground(timerDrawable);
        }

        if (startStopButton != null) updateStartStopButtonColor();

        if (skipDanceBtn != null) {
            GradientDrawable skipBg = new GradientDrawable();
            skipBg.setCornerRadius(dpToPx(14));
            skipBg.setColor(accentColor);
            skipDanceBtn.setBackground(skipBg);
            skipDanceBtn.setTextColor(Color.WHITE);
        }

        if (backDanceBtn != null) {
            GradientDrawable backBg = new GradientDrawable();
            backBg.setCornerRadius(dpToPx(14));
            backBg.setStroke(dpToPx(2), accentColor);
            backBg.setColor(Color.TRANSPARENT);
            backDanceBtn.setBackground(backBg);
            backDanceBtn.setTextColor(accentColor);
        }

        rebuildDots();
    }

    private void updateStartStopButtonColor() {
        if (startStopButton == null) return;
        GradientDrawable btnDrawable = new GradientDrawable();
        btnDrawable.setCornerRadius(dpToPx(14));
        btnDrawable.setColor(isRunning ? 0xFFE53935 : accentColor);
        startStopButton.setBackground(btnDrawable);
        startStopButton.setTextColor(Color.WHITE);
    }

    private void updateLabels() {
        if (headerTitle != null) {
            headerTitle.setText(Translations.getRemainingTime(lang));
        }
        if (backDanceBtn != null) {
            backDanceBtn.setText("\u2190 " + Translations.getBack(lang));
        }
        if (skipDanceBtn != null) {
            skipDanceBtn.setText(Translations.getSkip(lang) + " \u2192");
        }
    }

    private void updateUI() {
        if (timeText != null) timeText.setText(formatTime(timeRemaining));
        updatePhaseLabel();
        updateStatusText();
        updateProgressText();
        updateStartStopButtonColor();
        updateStartStopButtonText();
        updateSkipBackVisibility();
        rebuildDots();
    }

    private void updateSkipBackVisibility() {
        if (skipBackRow == null) return;
        if (isRunning) {
            skipBackRow.setVisibility(View.VISIBLE);
            if (backDanceBtn != null) {
                backDanceBtn.setEnabled(!history.isEmpty());
                backDanceBtn.setAlpha(history.isEmpty() ? 0.3f : 1.0f);
            }
        } else {
            skipBackRow.setVisibility(View.GONE);
        }
    }

    private void updatePhaseLabel() {
        if (phaseLabel == null) return;
        if (nextDanceLabel == null) return;
        switch (currentPhase) {
            case PHASE_IDLE:
                phaseLabel.setText("");
                nextDanceLabel.setVisibility(View.GONE);
                break;
            case PHASE_MUSIC:
                if (selectedDances != null && currentDanceIndex < selectedDances.size()) {
                    phaseLabel.setText(selectedDances.get(currentDanceIndex));
                }
                nextDanceLabel.setVisibility(View.GONE);
                break;
            case PHASE_PAUSE_BETWEEN_MUSIC:
                phaseLabel.setText(Translations.getPauseLabel(lang));
                if (selectedDances != null && currentDanceIndex + 1 < selectedDances.size()) {
                    nextDanceLabel.setText("\u2192 " + selectedDances.get(currentDanceIndex + 1));
                    nextDanceLabel.setVisibility(View.VISIBLE);
                } else {
                    nextDanceLabel.setVisibility(View.GONE);
                }
                break;
            case PHASE_ROUND_BREAK:
                phaseLabel.setText(Translations.getPauseLabel(lang) + " (" +
                        (currentRound + 1) + "/" + totalRounds + ")");
                if (selectedDances != null && !selectedDances.isEmpty()) {
                    nextDanceLabel.setText("\u2192 " + selectedDances.get(0));
                    nextDanceLabel.setVisibility(View.VISIBLE);
                } else {
                    nextDanceLabel.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void updateStatusText() {
        if (statusText == null) return;
        switch (currentPhase) {
            case PHASE_MUSIC:
                statusText.setText(Translations.getPlaying(lang));
                break;
            case PHASE_PAUSE_BETWEEN_MUSIC:
            case PHASE_ROUND_BREAK:
                statusText.setText(Translations.getPaused(lang));
                break;
            case PHASE_IDLE:
            default:
                statusText.setText(Translations.getStart(lang));
                break;
        }
    }

    private void updateProgressText() {
        if (progressText == null) return;
        if (selectedDances == null || selectedDances.isEmpty()) {
            progressText.setText("0/0");
        } else {
            String roundInfo = totalRounds > 1 ? " (" + (currentRound + 1) + "/" + totalRounds + ")" : "";
            progressText.setText((currentDanceIndex + 1) + "/" + selectedDances.size() + roundInfo);
        }
    }

    private void updateStartStopButtonText() {
        if (startStopButton == null) return;
        startStopButton.setText(isRunning ? Translations.getStop(lang) : Translations.getStart(lang));
    }

    private void rebuildDots() {
        if (dotsContainer == null) return;
        dotsContainer.removeAllViews();

        int total = selectedDances != null ? selectedDances.size() : 0;
        if (total == 0) return;

        for (int i = 0; i < total; i++) {
            View dot = new View(this);
            int size = dpToPx(10);
            int margin = dpToPx(3);

            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);

            if (i <= currentDanceIndex) {
                dotBg.setColor(accentColor);
            } else {
                dotBg.setColor(Color.TRANSPARENT);
                dotBg.setStroke(dpToPx(2), ThemeHelper.getOnSurfaceVariantColor(theme));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            dot.setBackground(dotBg);
            dotsContainer.addView(dot);
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
