package com.made4dancers.danceapp;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.made4dancers.danceapp.util.SettingsManager;
import com.made4dancers.danceapp.util.ThemeHelper;
import com.made4dancers.danceapp.util.Translations;

import java.util.Locale;

public class BurstSettingsActivity extends AppCompatActivity {

    private static final int COUNT_MIN = 1;
    private static final int COUNT_MAX = 10;
    private static final int PAUSE_MIN = 0;
    private static final int PAUSE_MAX = 60;

    private SettingsManager settingsManager;
    private String theme;
    private int accentIndex;
    private String lang;

    private TextView headerTitle;
    private ImageView backBtn;
    private TextView countLabel;
    private SeekBar countSlider;
    private TextView countMin;
    private TextView countMax;
    private TextView pauseLabel;
    private SeekBar pauseSlider;
    private TextView pauseMin;
    private TextView pauseMax;

    private int currentCount;
    private int currentPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burst_settings);

        settingsManager = SettingsManager.getInstance(this);
        theme = settingsManager.getTheme();
        accentIndex = settingsManager.getAccentColorIndex();
        lang = settingsManager.getLanguage();
        currentCount = settingsManager.getBurstCount();
        currentPause = settingsManager.getBurstPause();

        initViews();
        applyTheme();
        setupSliders();
    }

    private void initViews() {
        headerTitle = findViewById(R.id.headerTitle);
        backBtn = findViewById(R.id.backBtn);
        countLabel = findViewById(R.id.countLabel);
        countSlider = findViewById(R.id.countSlider);
        countMin = findViewById(R.id.countMin);
        countMax = findViewById(R.id.countMax);
        pauseLabel = findViewById(R.id.pauseLabel);
        pauseSlider = findViewById(R.id.pauseSlider);
        pauseMin = findViewById(R.id.pauseMin);
        pauseMax = findViewById(R.id.pauseMax);

        headerTitle.setText(Translations.getRounds(lang));
        backBtn.setOnClickListener(v -> finish());

        int countProgress = currentCount - COUNT_MIN;
        countSlider.setProgress(countProgress);
        updateCountLabel(currentCount);

        int pauseProgress = currentPause - PAUSE_MIN;
        pauseSlider.setProgress(pauseProgress);
        updatePauseLabel(currentPause);
    }

    private void applyTheme() {
        ThemeHelper.applyTheme(this, theme, accentIndex);

        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int onSurfaceVar = ThemeHelper.getOnSurfaceVariantColor(theme);

        getWindow().getDecorView().setBackgroundColor(bg);

        headerTitle.setTextColor(onBg);
        countLabel.setTextColor(onBg);
        pauseLabel.setTextColor(onBg);
        countMin.setTextColor(onSurfaceVar);
        countMax.setTextColor(onSurfaceVar);
        pauseMin.setTextColor(onSurfaceVar);
        pauseMax.setTextColor(onSurfaceVar);

        if (backBtn != null) {
            backBtn.setImageTintList(ColorStateList.valueOf(onBg));
            backBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }

        int accent = ThemeHelper.getAccentColor(accentIndex);
        countSlider.setProgressTintList(ColorStateList.valueOf(accent));
        countSlider.setThumbTintList(ColorStateList.valueOf(accent));
        pauseSlider.setProgressTintList(ColorStateList.valueOf(accent));
        pauseSlider.setThumbTintList(ColorStateList.valueOf(accent));
    }

    private void setupSliders() {
        countSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateCountLabel(COUNT_MIN + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentCount = COUNT_MIN + seekBar.getProgress();
                settingsManager.setBurstCount(currentCount);
                updateCountLabel(currentCount);
            }
        });

        pauseSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePauseLabel(PAUSE_MIN + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentPause = PAUSE_MIN + seekBar.getProgress();
                settingsManager.setBurstPause(currentPause);
                updatePauseLabel(currentPause);
            }
        });
    }

    private void updateCountLabel(int value) {
        countLabel.setText(Translations.getRoundsCount(lang, value));
    }

    private void updatePauseLabel(int value) {
        pauseLabel.setText(Translations.getRoundPause(lang, value));
    }
}
