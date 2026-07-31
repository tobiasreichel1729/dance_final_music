package de.dancefinalmusic;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.databinding.ActivityDanceStyleBinding;
import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

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
        String theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
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
                standardSelected, accent, buttonBg, onSurface,
                ThemeHelper.getButtonBorderColor(theme));
        applyStyleButton(binding.btnLatein, Translations.getLateinLabel(lang),
                !standardSelected, accent, buttonBg, onSurface,
                ThemeHelper.getButtonBorderColor(theme));
    }

    private void applyStyleButton(Button button, String text, boolean selected,
                                  int accentColor, int buttonBgColor, int textColor,
                                  int borderColor) {
        button.setText(text);
        button.setBackgroundTintList(null);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(10));
        if (selected) {
            drawable.setColor(accentColor);
            button.setTextColor(Color.WHITE);
        } else {
            drawable.setColor(buttonBgColor);
            drawable.setStroke(dpToPx(1), borderColor);
            button.setTextColor(textColor);
        }
        button.setBackground(drawable);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> MainActivity.goToMain(this));

        binding.btnStandard.setOnClickListener(v -> {
            settings.setDanceStyle("standard");
            MainActivity.goToMain(this);
        });

        binding.btnLatein.setOnClickListener(v -> {
            settings.setDanceStyle("latein");
            MainActivity.goToMain(this);
        });
    }

    @Override
    public void onBackPressed() {
        MainActivity.goToMain(this);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
