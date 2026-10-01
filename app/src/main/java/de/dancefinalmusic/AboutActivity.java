package de.dancefinalmusic;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

public class AboutActivity extends AppCompatActivity {

    private static final String CONTACT_EMAIL = "tobiasreichel1729@gmail.com";
    private static final String SUPPORT_URL = "https://www.buymeacoffee.com/TobiasReichel";

    private SettingsManager settings;
    private String theme;
    private int accentColor;
    private String lang;

    private TextView headerTitle;
    private ImageView backBtn;
    private ScrollView scrollView;
    private TextView contactHeader;
    private TextView contactName;
    private TextView contactEmail;
    private TextView supportLabel;
    private ImageView contactQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        settings = SettingsManager.getInstance(this);
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentColor = ThemeHelper.getAccentColor(settings.getAccentColorIndex());
        lang = settings.getLanguage();

        bindViews();
        updateLabels();
        applyTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentColor = ThemeHelper.getAccentColor(settings.getAccentColorIndex());
        lang = settings.getLanguage();
        updateLabels();
        applyTheme();
    }

    private void bindViews() {
        headerTitle = findViewById(R.id.headerTitle);
        backBtn = findViewById(R.id.backBtn);
        scrollView = findViewById(R.id.scrollView);
        contactHeader = findViewById(R.id.contactHeader);
        contactName = findViewById(R.id.contactName);
        contactEmail = findViewById(R.id.contactEmail);
        supportLabel = findViewById(R.id.supportLabel);
        contactQr = findViewById(R.id.contactQr);

        backBtn.setOnClickListener(v -> MainActivity.goToMain(this));

        View.OnClickListener openSupport = v -> openSupportPage();
        if (contactQr != null) contactQr.setOnClickListener(openSupport);
        if (supportLabel != null) supportLabel.setOnClickListener(openSupport);
    }

    private void openSupportPage() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.aboutNoBrowser, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        MainActivity.goToMain(this);
    }

    private void applyTheme() {
        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);

        ThemeHelper.applyTheme(this, theme, settings.getAccentColorIndex());

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
        if (contactHeader != null) {
            contactHeader.setText(Translations.getContact(lang));
        }
        if (contactName != null) {
            contactName.setText("Tobias Reichel");
        }
        if (contactEmail != null) {
            contactEmail.setText(CONTACT_EMAIL);
        }
        if (supportLabel != null) {
            supportLabel.setText(Translations.getSupport(lang));
        }
    }
}
