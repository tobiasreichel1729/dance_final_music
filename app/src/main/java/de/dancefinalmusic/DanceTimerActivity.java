package de.dancefinalmusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

import java.util.List;

public class DanceTimerActivity extends AppCompatActivity implements DanceSession.StateListener {

    private SettingsManager settings;
    private String theme;
    private int accentIndex;
    private int accentColor;
    private String lang;

    private final DanceSession session = DanceSession.getInstance();

    private TextView headerTitle;
    private ImageView backBtn;
    private TextView phaseLabel;
    private TextView nextDanceLabel;
    private TimerRingView timerRing;
    private TextView timeText;
    private TextView progressText;
    private LinearLayout dotsContainer;
    private Button startStopButton;
    private LinearLayout skipBackRow;
    private Button skipDanceBtn;
    private Button backDanceBtn;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dance_timer);

        settings = SettingsManager.getInstance(this);
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        lang = settings.getLanguage();

        session.attach(this);
        bindViews();
        setupGestures();
        applyTheme();
        updateLabels();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        lang = settings.getLanguage();
        session.addListener(this);
        applyTheme();
        updateLabels();
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        session.removeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.removeListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        setContentView(R.layout.activity_dance_timer);
        bindViews();
        applyTheme();
        updateLabels();
        updateUI();
    }

    @Override
    public void onBackPressed() {
        session.stopDance();
        stopSessionService();
        MainActivity.goToMain(this);
    }

    @Override
    public void onStateChanged() {
        runOnUiThread(this::updateUI);
    }

    private void bindViews() {
        headerTitle = findViewById(R.id.headerTitle);
        backBtn = findViewById(R.id.backBtn);
        phaseLabel = findViewById(R.id.phaseLabel);
        nextDanceLabel = findViewById(R.id.nextDanceLabel);
        timerRing = findViewById(R.id.timerRing);
        timeText = findViewById(R.id.timeText);
        progressText = findViewById(R.id.progressText);
        dotsContainer = findViewById(R.id.dotsContainer);
        startStopButton = findViewById(R.id.startStopButton);
        skipBackRow = findViewById(R.id.skipBackRow);
        skipDanceBtn = findViewById(R.id.skipDanceBtn);
        backDanceBtn = findViewById(R.id.backDanceBtn);

        backBtn.setOnClickListener(v -> {
            session.stopDance();
            stopSessionService();
            MainActivity.goToMain(this);
        });

        startStopButton.setOnClickListener(v -> {
            if (session.isRunning()) {
                session.stopDance();
                stopSessionService();
            } else {
                startSession();
            }
        });

        skipDanceBtn.setOnClickListener(v -> {
            if (session.isRunning()) {
                session.skipForward();
            }
        });

        backDanceBtn.setOnClickListener(v -> {
            if (session.isRunning() && session.hasHistory()) {
                session.goBack();
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
                            if (session.isRunning() && session.hasHistory()) {
                                session.goBack();
                            }
                        } else {
                            if (session.isRunning()) {
                                session.skipForward();
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

    private void startSession() {
        List<String> dances = settings.getSelectedDancesList();
        if (dances.isEmpty()) return;

        String firstDance = dances.get(0);
        if (settings.getRandomAudioFromFolder(this, settings.getDanceStyle(), firstDance) == null) {
            Toast.makeText(this,
                    Translations.getMusicAccessLostHint(lang) + " (" + firstDance + ")",
                    Toast.LENGTH_LONG).show();
            return;
        }

        requestNotificationPermissionIfNeeded();
        session.start(this);
        startForegroundService(new Intent(this, DanceSessionService.class)
                .setAction(DanceSessionService.ACTION_START));
    }

    private void stopSessionService() {
        startService(new Intent(this, DanceSessionService.class)
                .setAction(DanceSessionService.ACTION_STOP));
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    // --- UI ---

    private void applyTheme() {
        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int surface = ThemeHelper.getSurfaceColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceVariantColor(theme);

        ThemeHelper.applyTheme(this, theme, accentIndex);

        findViewById(android.R.id.content).getRootView().setBackgroundColor(bg);

        if (headerTitle != null) headerTitle.setTextColor(onBg);
        if (backBtn != null) {
            backBtn.setImageTintList(ColorStateList.valueOf(onBg));
            backBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }
        if (phaseLabel != null) {
            phaseLabel.setTextColor(accentColor);
            GradientDrawable box = new GradientDrawable();
            box.setCornerRadius(dpToPx(20));
            box.setColor(surface);
            box.setStroke(dpToPx(2), accentColor);
            phaseLabel.setBackground(box);
        }
        if (nextDanceLabel != null) nextDanceLabel.setTextColor(onSurface);
        if (timeText != null) timeText.setTextColor(onBg);
        if (progressText != null) progressText.setTextColor(onSurface);

        if (timerRing != null) {
            timerRing.setColors(
                    ThemeHelper.getTimerBgColor(accentIndex),
                    ThemeHelper.getOnSurfaceVariantColor(theme),
                    accentColor);
        }

        if (startStopButton != null) updateStartStopButtonColor();

        if (skipDanceBtn != null) {
            skipDanceBtn.setBackgroundTintList(null);
            GradientDrawable skipBg = new GradientDrawable();
            skipBg.setCornerRadius(dpToPx(14));
            skipBg.setColor(accentColor);
            skipDanceBtn.setBackground(skipBg);
            skipDanceBtn.setTextColor(Color.WHITE);
        }

        if (backDanceBtn != null) {
            backDanceBtn.setBackgroundTintList(null);
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
        startStopButton.setBackgroundTintList(null);
        GradientDrawable btnDrawable = new GradientDrawable();
        btnDrawable.setCornerRadius(dpToPx(14));
        btnDrawable.setColor(session.isRunning() ? 0xFFE53935 : accentColor);
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
        if (timeText != null) timeText.setText(formatTime(session.getTimeRemaining()));
        updateRing();
        updatePhaseLabel();
        updateProgressText();
        updateStartStopButtonColor();
        updateStartStopButtonText();
        updateSkipBackVisibility();
        rebuildDots();
    }

    private void updateRing() {
        if (timerRing == null) return;
        int total = 1;
        switch (session.getPhase()) {
            case DanceSession.PHASE_MUSIC:
                total = settings.getMusicDuration();
                break;
            case DanceSession.PHASE_PAUSE_BETWEEN_MUSIC:
                total = settings.getMusicPause();
                break;
            case DanceSession.PHASE_ROUND_BREAK:
                total = settings.getBurstPause();
                break;
            default:
                break;
        }
        if (total < 1) total = 1;
        float remainingFraction = Math.max(0f, Math.min(1f,
                session.getTimeRemaining() / (float) total));
        timerRing.setProgressFraction(1f - remainingFraction);
    }

    private void updateSkipBackVisibility() {
        if (skipBackRow == null) return;
        if (session.isRunning()) {
            skipBackRow.setVisibility(View.VISIBLE);
            if (backDanceBtn != null) {
                backDanceBtn.setEnabled(session.hasHistory());
                backDanceBtn.setAlpha(session.hasHistory() ? 1.0f : 0.3f);
            }
        } else {
            skipBackRow.setVisibility(View.GONE);
        }
    }

    private void updatePhaseLabel() {
        if (phaseLabel == null || nextDanceLabel == null) return;
        List<String> dances = session.getSelectedDances();
        switch (session.getPhase()) {
            case DanceSession.PHASE_IDLE:
                if (dances != null && !dances.isEmpty()) {
                    phaseLabel.setText(dances.get(0));
                } else {
                    phaseLabel.setText("");
                }
                nextDanceLabel.setText("");
                break;
            case DanceSession.PHASE_MUSIC:
                if (dances != null && session.getCurrentDanceIndex() < dances.size()) {
                    phaseLabel.setText(dances.get(session.getCurrentDanceIndex()));
                }
                nextDanceLabel.setText("");
                break;
            case DanceSession.PHASE_PAUSE_BETWEEN_MUSIC:
                phaseLabel.setText(Translations.getPauseLabel(lang));
                if (dances != null && session.getCurrentDanceIndex() + 1 < dances.size()) {
                    nextDanceLabel.setText("\u2192 " + dances.get(session.getCurrentDanceIndex() + 1));
                } else {
                    nextDanceLabel.setText("");
                }
                break;
            case DanceSession.PHASE_ROUND_BREAK:
                phaseLabel.setText(Translations.getPauseLabel(lang) + " (" +
                        (session.getCurrentRound() + 1) + "/" + session.getTotalRounds() + ")");
                if (dances != null && !dances.isEmpty()) {
                    nextDanceLabel.setText("\u2192 " + dances.get(0));
                } else {
                    nextDanceLabel.setText("");
                }
                break;
        }
    }

    private void updateProgressText() {
        if (progressText == null) return;
        List<String> dances = session.getSelectedDances();
        if (dances == null || dances.isEmpty()) {
            progressText.setText("0/0");
        } else {
            String roundInfo = session.getTotalRounds() > 1
                    ? " (" + (session.getCurrentRound() + 1) + "/" + session.getTotalRounds() + ")"
                    : "";
            progressText.setText((session.getCurrentDanceIndex() + 1) + "/"
                    + dances.size() + roundInfo);
        }
    }

    private void updateStartStopButtonText() {
        if (startStopButton == null) return;
        startStopButton.setText(session.isRunning()
                ? Translations.getStop(lang) : Translations.getStart(lang));
    }

    private void rebuildDots() {
        if (dotsContainer == null) return;
        dotsContainer.removeAllViews();

        List<String> dances = session.getSelectedDances();
        int total = dances != null ? dances.size() : 0;
        if (total == 0) return;

        int currentIndex = session.getCurrentDanceIndex();
        for (int i = 0; i < total; i++) {
            View dot = new View(this);
            int size = dpToPx(10);
            int margin = dpToPx(3);

            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);

            if (i <= currentIndex) {
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
