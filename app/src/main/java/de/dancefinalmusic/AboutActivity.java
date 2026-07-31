package de.dancefinalmusic;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

public class AboutActivity extends AppCompatActivity {

    private SettingsManager settings;
    private String theme;
    private int accentColor;
    private String lang;

    private TextView headerTitle;
    private ImageView backBtn;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        settings = SettingsManager.getInstance(this);
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentColor = ThemeHelper.getAccentColor(settings.getAccentColorIndex());
        lang = settings.getLanguage();

        bindViews();
        applyTheme();
        updateLabels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentColor = ThemeHelper.getAccentColor(settings.getAccentColorIndex());
        lang = settings.getLanguage();
        applyTheme();
        updateLabels();
    }

    private void bindViews() {
        headerTitle = findViewById(R.id.headerTitle);
        backBtn = findViewById(R.id.backBtn);
        scrollView = findViewById(R.id.scrollView);

        backBtn.setOnClickListener(v -> MainActivity.goToMain(this));
    }

    @Override
    public void onBackPressed() {
        MainActivity.goToMain(this);
    }

    private void applyTheme() {
        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);

        findViewById(android.R.id.content).getRootView().setBackgroundColor(bg);

        if (headerTitle != null) headerTitle.setTextColor(onBg);
        if (backBtn != null) {
            backBtn.setImageTintList(ColorStateList.valueOf(onBg));
            backBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }

        if (scrollView != null) {
            applyThemeToViewGroup(scrollView);
        }
    }

    private void applyThemeToViewGroup(View view) {
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int variant = ThemeHelper.getOnSurfaceVariantColor(theme);

        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    if (tv.getText() != null) {
                        String text = tv.getText().toString();
                        if (text.equals("Dance Final Music")) {
                            tv.setTextColor(accentColor);
                        } else if (text.equals(getResources().getString(R.string.app_name)) ||
                                text.contains("Copyright") || text.contains("Danke") ||
                                text.contains("Thanks") || text.contains("License") ||
                                text.contains("Licence") || text.contains("Kontakt") ||
                                text.contains("Contact")) {
                            tv.setTextColor(accentColor);
                        } else {
                            tv.setTextColor(variant);
                        }
                    }
                }
                if (child instanceof android.view.ViewGroup) {
                    applyThemeToViewGroup(child);
                }
            }
        }
    }

    private void updateLabels() {
        if (headerTitle != null) {
            headerTitle.setText(Translations.getAbout(lang));
        }
    }
}
