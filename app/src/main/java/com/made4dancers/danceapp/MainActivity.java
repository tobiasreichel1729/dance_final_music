package com.made4dancers.danceapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.made4dancers.danceapp.databinding.ActivityMainBinding;
import com.made4dancers.danceapp.util.SettingsManager;
import com.made4dancers.danceapp.util.ThemeHelper;
import com.made4dancers.danceapp.util.Translations;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SettingsManager settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settings = SettingsManager.getInstance(this);
        applyThemeAndColors();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyThemeAndColors();
    }

    private void applyThemeAndColors() {
        String theme = settings.getTheme();
        int accentIndex = settings.getAccentColorIndex();
        String lang = settings.getLanguage();

        ThemeHelper.applyTheme(this, theme, accentIndex);
        int accent = ThemeHelper.getAccentColor(accentIndex);
        int bg = ThemeHelper.getBackgroundColor(theme);
        int surface = ThemeHelper.getSurfaceColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);
        int buttonBg = ThemeHelper.getButtonBgColor(theme);

        binding.getRoot().setBackgroundColor(bg);
        binding.settingsBtn.setImageTintList(ColorStateList.valueOf(onBg));

        GradientDrawable settingsCircle = new GradientDrawable();
        settingsCircle.setShape(GradientDrawable.OVAL);
        settingsCircle.setColor(ThemeHelper.getSurfaceVariantColor(theme));
        binding.settingsBtn.setBackground(settingsCircle);

        updateCard(binding.selectDancesCard, binding.selectDancesLabel, binding.selectDancesSublabel,
                Translations.getSelectDances(lang), getSelectedDancesSublabel(lang),
                surface, onSurface, theme, accentIndex);
        updateCard(binding.musicTimingCard, binding.musicTimingLabel, binding.musicTimingSublabel,
                Translations.getMusicTiming(lang), getMusicTimingSublabel(lang),
                surface, onSurface, theme, accentIndex);
        updateCard(binding.burstSettingsCard, binding.burstSettingsLabel, binding.burstSettingsSublabel,
                Translations.getBurstSettings(lang), getBurstSublabel(lang),
                surface, onSurface, theme, accentIndex);

        binding.startButton.setBackgroundColor(accent);
        binding.startButtonText.setText(Translations.getStart(lang));
        binding.startButtonText.setTextColor(ThemeHelper.getOnBackgroundColor("dark"));
    }

    private void updateCard(View card, View label, View sublabel,
                            String labelText, String sublabelText,
                            int surfaceColor, int textColor, String theme, int accentIndex) {
        card.setBackgroundColor(surfaceColor);
        if (label instanceof android.widget.TextView) {
            ((android.widget.TextView) label).setText(labelText);
            ((android.widget.TextView) label).setTextColor(textColor);
        }
        if (sublabel instanceof android.widget.TextView) {
            ((android.widget.TextView) sublabel).setText(sublabelText);
            ((android.widget.TextView) sublabel).setTextColor(
                    ThemeHelper.getOnSurfaceVariantColor(theme));
        }
    }

    private String getSelectedDancesSublabel(String lang) {
        List<String> dances = settings.getSelectedDancesList();
        if (dances.isEmpty()) {
            return Translations.getSelectDancesHint(lang);
        }
        return String.join(", ", dances);
    }

    private String getMusicTimingSublabel(String lang) {
        return settings.getMusicDuration() + "s / " + settings.getMusicPause() + "s";
    }

    private String getBurstSublabel(String lang) {
        int count = settings.getBurstCount();
        return Translations.getRoundsCount(lang, count);
    }

    private void setupListeners() {
        binding.settingsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        binding.selectDancesCard.setOnClickListener(v ->
                startActivity(new Intent(this, DanceSelectActivity.class)));

        binding.musicTimingCard.setOnClickListener(v ->
                startActivity(new Intent(this, MusicTimingActivity.class)));

        binding.burstSettingsCard.setOnClickListener(v ->
                startActivity(new Intent(this, BurstSettingsActivity.class)));

        binding.startButton.setOnClickListener(v -> {
            List<String> dances = settings.getSelectedDancesList();
            if (dances.isEmpty()) {
                startActivity(new Intent(this, DanceSelectActivity.class));
            } else {
                startActivity(new Intent(this, DanceTimerActivity.class));
            }
        });
    }
}
