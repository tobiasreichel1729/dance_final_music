package com.made4dancers.danceapp;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.made4dancers.danceapp.databinding.ActivityDanceStyleBinding;
import com.made4dancers.danceapp.util.SettingsManager;
import com.made4dancers.danceapp.util.ThemeHelper;
import com.made4dancers.danceapp.util.Translations;

public class DanceStyleActivity extends AppCompatActivity {

    private ActivityDanceStyleBinding binding;
    private SettingsManager settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDanceStyleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settings = SettingsManager.getInstance(this);
        applyThemeAndColors();
        setupListeners();
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

        binding.btnBack.setImageTintList(ColorStateList.valueOf(onBg));
        binding.btnBack.setBackground(ThemeHelper.createCircleBackground(theme));

        String currentStyle = settings.getDanceStyle();
        boolean standardSelected = "standard".equals(currentStyle);

        applyStyleButton(binding.btnStandard, Translations.getStandardLabel(lang),
                standardSelected, accent, buttonBg, onSurface);
        applyStyleButton(binding.btnLatein, Translations.getLateinLabel(lang),
                !standardSelected, accent, buttonBg, onSurface);
    }

    private void applyStyleButton(Button button, String text, boolean selected,
                                  int accentColor, int buttonBgColor, int textColor) {
        button.setText(text);
        button.setTextColor(textColor);
        if (selected) {
            button.setBackgroundColor(accentColor);
        } else {
            button.setBackgroundColor(buttonBgColor);
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnStandard.setOnClickListener(v -> {
            settings.setDanceStyle("standard");
            finish();
        });

        binding.btnLatein.setOnClickListener(v -> {
            settings.setDanceStyle("latein");
            finish();
        });
    }
}
