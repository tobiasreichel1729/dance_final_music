package de.dancefinalmusic;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

public class MusicTimingActivity extends AppCompatActivity {

    private static final int DURATION_MIN = 10;
    private static final int DURATION_MAX = 300;
    private static final int DURATION_STEP = 5;
    private static final int PAUSE_MIN = 0;
    private static final int PAUSE_MAX = 60;
    private static final int PAUSE_STEP = 1;

    private SettingsManager settingsManager;
    private String theme;
    private int accentIndex;
    private String lang;

    private TextView headerTitle;
    private ImageView backBtn;
    private TextView durationLabel;
    private SeekBar durationSlider;
    private TextView durationMin;
    private TextView durationMax;
    private TextView pauseLabel;
    private SeekBar pauseSlider;
    private TextView pauseMin;
    private TextView pauseMax;

    private int currentDuration;
    private int currentPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_timing);

        settingsManager = SettingsManager.getInstance(this);
        theme = ThemeHelper.getEffectiveTheme(this, settingsManager.getTheme());
        accentIndex = settingsManager.getAccentColorIndex();
        lang = settingsManager.getLanguage();
        currentDuration = settingsManager.getMusicDuration();
        currentPause = settingsManager.getMusicPause();

        initViews();
        applyTheme();
        setupSliders();
    }

    private void initViews() {
        headerTitle = findViewById(R.id.headerTitle);
        backBtn = findViewById(R.id.backBtn);
        durationLabel = findViewById(R.id.durationLabel);
        durationSlider = findViewById(R.id.durationSlider);
        durationMin = findViewById(R.id.durationMin);
        durationMax = findViewById(R.id.durationMax);
        pauseLabel = findViewById(R.id.pauseLabel);
        pauseSlider = findViewById(R.id.pauseSlider);
        pauseMin = findViewById(R.id.pauseMin);
        pauseMax = findViewById(R.id.pauseMax);

        headerTitle.setText(Translations.getMusicTiming(lang));
        backBtn.setOnClickListener(v -> MainActivity.goToMain(this));

        int durationProgress = (currentDuration - DURATION_MIN) / DURATION_STEP;
        durationSlider.setProgress(durationProgress);
        updateDurationLabel(currentDuration);

        int pauseProgress = (currentPause - PAUSE_MIN) / PAUSE_STEP;
        pauseSlider.setProgress(pauseProgress);
        updatePauseLabel(currentPause);
    }

    @Override
    public void onBackPressed() {
        MainActivity.goToMain(this);
    }

    private void applyTheme() {
        ThemeHelper.applyTheme(this, theme, accentIndex);

        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int onSurfaceVar = ThemeHelper.getOnSurfaceVariantColor(theme);

        getWindow().getDecorView().setBackgroundColor(bg);

        headerTitle.setTextColor(onBg);
        durationLabel.setTextColor(onBg);
        pauseLabel.setTextColor(onBg);
        durationMin.setTextColor(onSurfaceVar);
        durationMax.setTextColor(onSurfaceVar);
        pauseMin.setTextColor(onSurfaceVar);
        pauseMax.setTextColor(onSurfaceVar);

        if (backBtn != null) {
            backBtn.setImageTintList(ColorStateList.valueOf(onBg));
            backBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }

        int accent = ThemeHelper.getAccentColor(accentIndex);
        durationSlider.setProgressTintList(ColorStateList.valueOf(accent));
        durationSlider.setThumbTintList(ColorStateList.valueOf(accent));
        pauseSlider.setProgressTintList(ColorStateList.valueOf(accent));
        pauseSlider.setThumbTintList(ColorStateList.valueOf(accent));

        durationMin.setText(DURATION_MIN + "s");
        durationMax.setText(DURATION_MAX + "s");
        pauseMin.setText(PAUSE_MIN + "s");
        pauseMax.setText(PAUSE_MAX + "s");
    }

    private void setupSliders() {
        durationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = DURATION_MIN + progress * DURATION_STEP;
                updateDurationLabel(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = DURATION_MIN + seekBar.getProgress() * DURATION_STEP;
                currentDuration = value;
                settingsManager.setMusicDuration(currentDuration);
                updateDurationLabel(currentDuration);
            }
        });

        pauseSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = PAUSE_MIN + progress * PAUSE_STEP;
                updatePauseLabel(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = PAUSE_MIN + seekBar.getProgress() * PAUSE_STEP;
                currentPause = value;
                settingsManager.setMusicPause(currentPause);
                updatePauseLabel(currentPause);
            }
        });
    }

    private void updateDurationLabel(int value) {
        durationLabel.setText(Translations.getMusicDurationSeconds(lang, value));
    }

    private void updatePauseLabel(int value) {
        pauseLabel.setText(Translations.getMusicPauseSeconds(lang, value));
    }
}
