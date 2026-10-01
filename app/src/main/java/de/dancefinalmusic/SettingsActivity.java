package de.dancefinalmusic;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;


public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settings;
    private String theme;
    private int accentIndex;
    private int accentColor;
    private String lang;

    private TextView headerTitle;
    private ImageView backBtn;
    private TextView musicSectionHeader;
    private LinearLayout musicListContainer;
    private TextView danceStyleSectionHeader;
    private TextView themeSectionHeader;
    private Button darkBtn;
    private Button systemBtn;
    private Button lightBtn;
    private TextView accentColorLabel;
    private LinearLayout accentColorFlow;
    private TextView languageSectionHeader;
    private TextView effectsSectionHeader;
    private TextView fadeLabel;
    private TextView applauseLabel;
    private Switch fadeSwitch;
    private Switch applauseSwitch;
    private Button deBtn;
    private Button enBtn;
    private Button standardBtn;
    private Button lateinBtn;
    private Button aboutBtn;

    private final View[] colorViews = new View[10];
    private int pendingDanceIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = SettingsManager.getInstance(this);
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        lang = settings.getLanguage();

        bindViews();
        applyTheme();
        populateMusicList();
        setupColorCircles();
        updateLabels();
        updateToggleStates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        lang = settings.getLanguage();
        applyTheme();
        populateMusicList();
        updateLabels();
        updateToggleStates();
        updateColorSelection();
    }

    @Override
    public void onBackPressed() {
        MainActivity.goToMain(this);
    }

    private void bindViews() {
        headerTitle = findViewById(R.id.headerTitle);
        backBtn = findViewById(R.id.backBtn);
        musicSectionHeader = findViewById(R.id.musicSectionHeader);
        musicListContainer = findViewById(R.id.musicListContainer);
        danceStyleSectionHeader = findViewById(R.id.danceStyleSectionHeader);
        themeSectionHeader = findViewById(R.id.themeSectionHeader);
        darkBtn = findViewById(R.id.darkBtn);
        systemBtn = findViewById(R.id.systemBtn);
        lightBtn = findViewById(R.id.lightBtn);
        accentColorLabel = findViewById(R.id.accentColorLabel);
        accentColorFlow = findViewById(R.id.accentColorFlow);
        languageSectionHeader = findViewById(R.id.languageSectionHeader);
        effectsSectionHeader = findViewById(R.id.effectsSectionHeader);
        fadeLabel = findViewById(R.id.fadeLabel);
        applauseLabel = findViewById(R.id.applauseLabel);
        fadeSwitch = findViewById(R.id.fadeSwitch);
        applauseSwitch = findViewById(R.id.applauseSwitch);
        standardBtn = findViewById(R.id.standardBtn);
        lateinBtn = findViewById(R.id.lateinBtn);
        deBtn = findViewById(R.id.deBtn);
        enBtn = findViewById(R.id.enBtn);
        aboutBtn = findViewById(R.id.aboutBtn);

        fadeSwitch.setChecked(settings.isFadeEnabled());
        applauseSwitch.setChecked(settings.isApplauseEnabled());

        fadeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                settings.setFadeEnabled(isChecked));
        applauseSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                settings.setApplauseEnabled(isChecked));

        backBtn.setOnClickListener(v -> MainActivity.goToMain(this));

        darkBtn.setOnClickListener(v -> {
            settings.setTheme("dark");
            theme = "dark";
            updateToggleStates();
            applyTheme();
            populateMusicList();
        });

        systemBtn.setOnClickListener(v -> {
            settings.setTheme("system");
            theme = ThemeHelper.getEffectiveTheme(this, "system");
            updateToggleStates();
            applyTheme();
            populateMusicList();
        });

        lightBtn.setOnClickListener(v -> {
            settings.setTheme("light");
            theme = "light";
            updateToggleStates();
            applyTheme();
            populateMusicList();
        });

        deBtn.setOnClickListener(v -> {
            settings.setLanguage("de");
            lang = "de";
            updateLabels();
            updateToggleStates();
            populateMusicList();
            applyTheme();
        });

        enBtn.setOnClickListener(v -> {
            settings.setLanguage("en");
            lang = "en";
            updateLabels();
            updateToggleStates();
            populateMusicList();
            applyTheme();
        });

        standardBtn.setOnClickListener(v -> {
            if (!"standard".equals(settings.getDanceStyle())) {
                settings.setDanceStyle("standard");
                updateToggleStates();
                applyTheme();
                populateMusicList();
            }
        });

        lateinBtn.setOnClickListener(v -> {
            if (!"latein".equals(settings.getDanceStyle())) {
                settings.setDanceStyle("latein");
                updateToggleStates();
                applyTheme();
                populateMusicList();
            }
        });

        aboutBtn.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
        });
    }

    private void applyTheme() {
        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);

        ThemeHelper.applyTheme(this, theme, accentIndex);

        findViewById(android.R.id.content).getRootView().setBackgroundColor(bg);

        if (headerTitle != null) headerTitle.setTextColor(onBg);
        if (backBtn != null) {
            backBtn.setImageTintList(ColorStateList.valueOf(onBg));
            backBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }
        if (musicSectionHeader != null) musicSectionHeader.setTextColor(accentColor);
        if (danceStyleSectionHeader != null) danceStyleSectionHeader.setTextColor(accentColor);
        if (themeSectionHeader != null) themeSectionHeader.setTextColor(accentColor);
        if (accentColorLabel != null) accentColorLabel.setTextColor(accentColor);
        if (languageSectionHeader != null) languageSectionHeader.setTextColor(accentColor);
        if (effectsSectionHeader != null) effectsSectionHeader.setTextColor(accentColor);
        if (fadeLabel != null) fadeLabel.setTextColor(onSurface);
        if (applauseLabel != null) applauseLabel.setTextColor(onSurface);

        ThemeHelper.tintSwitch(fadeSwitch, accentColor, theme);
        ThemeHelper.tintSwitch(applauseSwitch, accentColor, theme);

        applyButtonStyle(darkBtn);
        applyButtonStyle(systemBtn);
        applyButtonStyle(lightBtn);
        applyButtonStyle(standardBtn);
        applyButtonStyle(lateinBtn);
        applyButtonStyle(deBtn);
        applyButtonStyle(enBtn);
        applyAboutButtonStyle();

        updateToggleStates();
        updateColorSelection();
    }

    private void applyButtonStyle(Button btn) {
        if (btn == null) return;
        int bg = ThemeHelper.getButtonBgColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);
        int border = ThemeHelper.getButtonBorderColor(theme);

        btn.setBackgroundTintList(null);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(10));
        drawable.setColor(bg);
        drawable.setStroke(dpToPx(1), border);
        btn.setBackground(drawable);
        btn.setTextColor(onSurface);
    }

    private void applyAboutButtonStyle() {
        if (aboutBtn == null) return;
        int bg = ThemeHelper.getSurfaceColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);
        int border = ThemeHelper.getButtonBorderColor(theme);

        aboutBtn.setBackgroundTintList(null);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(10));
        drawable.setColor(bg);
        drawable.setStroke(dpToPx(1), border);
        aboutBtn.setBackground(drawable);
        aboutBtn.setTextColor(onSurface);
    }

    private void updateLabels() {
        if (headerTitle != null) headerTitle.setText(Translations.getSettings(lang));
        if (musicSectionHeader != null) musicSectionHeader.setText(Translations.getMusicManagement(lang));
        if (danceStyleSectionHeader != null) danceStyleSectionHeader.setText(Translations.getDanceStyle(lang));
        if (themeSectionHeader != null) themeSectionHeader.setText(Translations.getThemeSettings(lang));
        if (accentColorLabel != null) accentColorLabel.setText(Translations.getAccentColor(lang) + ":");
        if (languageSectionHeader != null) languageSectionHeader.setText(Translations.getLanguageSettings(lang));
        if (effectsSectionHeader != null) effectsSectionHeader.setText(Translations.getEffects(lang));
        if (fadeLabel != null) fadeLabel.setText(Translations.getFadeOut(lang));
        if (applauseLabel != null) applauseLabel.setText(Translations.getApplause(lang));
        if (aboutBtn != null) aboutBtn.setText(Translations.getAbout(lang));
        if (standardBtn != null) standardBtn.setText(Translations.getStandardLabel(lang));
        if (lateinBtn != null) lateinBtn.setText(Translations.getLateinLabel(lang));
    }

    private void updateToggleStates() {
        String storedTheme = settings.getTheme();
        updateToggleButton(darkBtn, "dark".equals(storedTheme));
        updateToggleButton(systemBtn, "system".equals(storedTheme));
        updateToggleButton(lightBtn, "light".equals(storedTheme));
        updateToggleButton(standardBtn, "standard".equals(settings.getDanceStyle()));
        updateToggleButton(lateinBtn, "latein".equals(settings.getDanceStyle()));
        updateToggleButton(deBtn, "de".equals(lang));
        updateToggleButton(enBtn, "en".equals(lang));
    }

    private void updateToggleButton(Button btn, boolean active) {
        if (btn == null) return;
        btn.setBackgroundTintList(null);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(10));
        if (active) {
            drawable.setColor(accentColor);
            btn.setTextColor(Color.WHITE);
        } else {
            drawable.setColor(ThemeHelper.getButtonBgColor(theme));
            drawable.setStroke(dpToPx(1), ThemeHelper.getButtonBorderColor(theme));
            btn.setTextColor(ThemeHelper.getOnSurfaceColor(theme));
        }
        btn.setBackground(drawable);
    }

    private void populateMusicList() {
        if (musicListContainer == null) return;
        musicListContainer.removeAllViews();

        String style = settings.getDanceStyle();
        String level = settings.getDanceLevel();
        String[] dances = settings.getAvailableDances(style, level);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);
        int variant = ThemeHelper.getOnSurfaceVariantColor(theme);
        int border = ThemeHelper.getBorderColor(theme);
        int surface = ThemeHelper.getSurfaceColor(theme);

        for (int i = 0; i < dances.length; i++) {
            String danceName = dances[i];
            final int danceIdx = i;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setCornerRadius(dpToPx(8));
            rowBg.setColor(surface);
            rowBg.setStroke(dpToPx(1), border);
            row.setBackground(rowBg);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.bottomMargin = dpToPx(6);
            row.setLayoutParams(rowParams);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView nameTv = new TextView(this);
            nameTv.setText(Translations.getDanceName(lang, danceName));
            nameTv.setTextColor(onSurface);
            nameTv.setTextSize(15);
            nameTv.setIncludeFontPadding(false);
            textCol.addView(nameTv);

            TextView pathTv = new TextView(this);
            String folderPath = settings.getMusicFolderPath(style, danceName);
            if (folderPath != null && !folderPath.isEmpty()) {
                String folderName = getFolderName(folderPath);
                if (folderName != null) {
                    pathTv.setText(folderName);
                } else if (settings.hasFolderAccess(this, folderPath)) {
                    pathTv.setText(Translations.getFolderSet(lang));
                } else {
                    pathTv.setText(Translations.getFolderAccessLost(lang));
                }
            } else {
                pathTv.setText(Translations.getNoMusicSet(lang));
            }
            pathTv.setTextColor(variant);
            pathTv.setTextSize(12);
            pathTv.setSingleLine(true);
            pathTv.setIncludeFontPadding(false);
            textCol.addView(pathTv);

            row.addView(textCol);

            Button addBtn = new Button(this);
            addBtn.setText("...");
            addBtn.setTextSize(16);
            addBtn.setTextColor(accentColor);
            addBtn.setBackgroundTintList(null);
            addBtn.setBackgroundColor(Color.TRANSPARENT);
            addBtn.setIncludeFontPadding(false);
            LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                    dpToPx(48), dpToPx(36)
            );
            addBtn.setLayoutParams(addParams);
            addBtn.setOnClickListener(v -> {
                pendingDanceIndex = danceIdx;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, 65536 + danceIdx);
            });
            row.addView(addBtn);

            musicListContainer.addView(row);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri treeUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            int danceIdx = requestCode - 65536;
            String style = settings.getDanceStyle();
            String level = settings.getDanceLevel();
            String[] dances = settings.getAvailableDances(style, level);

            if (danceIdx >= 0 && danceIdx < dances.length) {
                settings.setMusicFolderPath(style, dances[danceIdx], treeUri.toString());
                populateMusicList();
            }
        }
    }

    private void setupColorCircles() {
        if (accentColorFlow == null) return;

        int[] colorIds = {
                R.id.color1, R.id.color2, R.id.color3, R.id.color4, R.id.color5,
                R.id.color6, R.id.color7, R.id.color8, R.id.color9, R.id.color10
        };

        for (int i = 0; i < colorIds.length; i++) {
            View v = findViewById(colorIds[i]);
            if (v == null) continue;
            final int colorIdx = i;
            colorViews[i] = v;
            v.setOnClickListener(view -> {
                settings.setAccentColorIndex(colorIdx);
                accentIndex = colorIdx;
                accentColor = ThemeHelper.getAccentColor(colorIdx);
                updateColorSelection();
                applyTheme();
            });
        }
        updateColorSelection();
    }

    private void updateColorSelection() {
        int border = ThemeHelper.getBorderColor(theme);
        for (int i = 0; i < colorViews.length; i++) {
            View v = colorViews[i];
            if (v == null) continue;

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(ThemeHelper.ACCENT_COLORS[i]);

            if (i == accentIndex) {
                int ringColor = "light".equals(theme)
                        ? ThemeHelper.getOnBackgroundColor(theme)
                        : Color.WHITE;
                circle.setStroke(dpToPx(3), ringColor);
            } else {
                circle.setStroke(dpToPx(2), border);
            }
            v.setBackground(circle);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private String getFolderName(String treeUriStr) {
        try {
            Uri treeUri = Uri.parse(treeUriStr);
            String docId = DocumentsContract.getTreeDocumentId(treeUri);
            Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
            Cursor cursor = getContentResolver().query(documentUri,
                    new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                    null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
