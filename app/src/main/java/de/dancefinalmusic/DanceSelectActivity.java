package de.dancefinalmusic;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DanceSelectActivity extends AppCompatActivity {

    private SettingsManager settingsManager;
    private String theme;
    private int accentIndex;
    private String lang;
    private String danceStyle;
    private ArrayList<String> selectedDances;

    private TextView headerTitle;
    private ImageView backBtn;
    private Button selectAllBtn, deselectAllBtn;
    private ListView danceListView;
    private FrameLayout confirmButton;
    private TextView confirmText;

    private ArrayAdapter<String> adapter;
    private String[] availableDances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dance_select);

        settingsManager = SettingsManager.getInstance(this);
        theme = ThemeHelper.getEffectiveTheme(this, settingsManager.getTheme());
        accentIndex = settingsManager.getAccentColorIndex();
        lang = settingsManager.getLanguage();
        danceStyle = settingsManager.getDanceStyle();
        selectedDances = new ArrayList<>(settingsManager.getSelectedDancesList());

        initViews();
        applyTheme();
        setupSelectAllDeselectAll();
        setupConfirmButton();
        refreshDanceList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = ThemeHelper.getEffectiveTheme(this, settingsManager.getTheme());
        accentIndex = settingsManager.getAccentColorIndex();
        lang = settingsManager.getLanguage();
        danceStyle = settingsManager.getDanceStyle();
        applyTheme();
        refreshDanceList();
    }

    private void initViews() {
        headerTitle = findViewById(R.id.headerTitle);
        backBtn = findViewById(R.id.backBtn);
        selectAllBtn = findViewById(R.id.selectAllBtn);
        deselectAllBtn = findViewById(R.id.deselectAllBtn);
        danceListView = findViewById(R.id.danceListView);
        confirmButton = findViewById(R.id.confirmButton);
        confirmText = findViewById(R.id.confirmText);

        headerTitle.setText(Translations.getSelectDances(lang));
        backBtn.setOnClickListener(v -> MainActivity.goToMain(this));

        selectAllBtn.setText(Translations.getSelectAll(lang));
        deselectAllBtn.setText(Translations.getDeselectAll(lang));
        updateConfirmText();
    }

    @Override
    public void onBackPressed() {
        MainActivity.goToMain(this);
    }

    private void applyTheme() {
        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int surface = ThemeHelper.getSurfaceColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);
        int border = ThemeHelper.getBorderColor(theme);

        ThemeHelper.applyTheme(this, theme, accentIndex);
        findViewById(android.R.id.content).getRootView().setBackgroundColor(bg);

        headerTitle.setTextColor(onBg);
        selectAllBtn.setTextColor(onBg);
        deselectAllBtn.setTextColor(onBg);
        confirmText.setTextColor(Color.WHITE);

        backBtn.setImageTintList(ColorStateList.valueOf(onBg));
        backBtn.setBackground(ThemeHelper.createCircleBackground(theme));

        updateButtonStyle(selectAllBtn);
        updateButtonStyle(deselectAllBtn);
        updateConfirmButton();
    }

    private void updateButtonStyle(Button btn) {
        int surface = ThemeHelper.getSurfaceColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);
        int border = ThemeHelper.getButtonBorderColor(theme);
        btn.setBackgroundTintList(null);
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dpToPx(10));
        d.setColor(surface);
        d.setStroke(dpToPx(1), border);
        btn.setBackground(d);
        btn.setTextColor(onSurface);
    }

    private void updateConfirmButton() {
        int accent = ThemeHelper.getAccentColor(accentIndex);
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dpToPx(14));
        d.setColor(accent);
        confirmButton.setBackground(d);
    }

    private void setupSelectAllDeselectAll() {
        selectAllBtn.setOnClickListener(v -> {
            selectedDances.clear();
            selectedDances.addAll(Arrays.asList(availableDances));
            refreshAdapter();
        });

        deselectAllBtn.setOnClickListener(v -> {
            selectedDances.clear();
            refreshAdapter();
        });
    }

    private void refreshDanceList() {
        availableDances = settingsManager.getAvailableDances(danceStyle);

        ArrayList<String> newSelection = new ArrayList<>();
        for (String dance : selectedDances) {
            for (String available : availableDances) {
                if (dance.equals(available)) {
                    newSelection.add(dance);
                    break;
                }
            }
        }
        selectedDances = newSelection;

        ArrayList<String> sortedDances = new ArrayList<>();
        sortedDances.addAll(selectedDances);
        for (String dance : availableDances) {
            if (!sortedDances.contains(dance)) {
                sortedDances.add(dance);
            }
        }

        adapter = new ArrayAdapter<String>(this, R.layout.list_item_dance, R.id.danceName, sortedDances) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView danceName = view.findViewById(R.id.danceName);
                TextView checkMark = view.findViewById(R.id.checkMark);

                String dance = sortedDances.get(position);
                int selectedIndex = selectedDances.indexOf(dance);

                int surface = ThemeHelper.getSurfaceColor(theme);
                int onSurface = ThemeHelper.getOnSurfaceColor(theme);
                int accent = ThemeHelper.getAccentColor(accentIndex);

                if (selectedIndex >= 0) {
                    danceName.setText((selectedIndex + 1) + ". " + dance);
                    danceName.setTextColor(Color.WHITE);

                    GradientDrawable bg = new GradientDrawable();
                    bg.setCornerRadius(dpToPx(8));
                    bg.setColor(accent);
                    bg.setStroke(dpToPx(1), accent);
                    view.setBackground(bg);

                    int padding = dpToPx(8);
                    view.setPadding(padding, padding, padding, padding);

                    checkMark.setVisibility(View.GONE);
                } else {
                    danceName.setText(dance);
                    danceName.setTextColor(onSurface);

                    GradientDrawable bg = new GradientDrawable();
                    bg.setCornerRadius(dpToPx(8));
                    bg.setColor(surface);
                    int border = ThemeHelper.getBorderColor(theme);
                    bg.setStroke(dpToPx(1), border);
                    view.setBackground(bg);

                    int padding = dpToPx(8);
                    view.setPadding(padding, padding, padding, padding);

                    checkMark.setVisibility(View.GONE);
                }

                return view;
            }
        };

        danceListView.setAdapter(adapter);

        danceListView.setOnItemClickListener((parent, view, position, id) -> {
            String dance = sortedDances.get(position);
            if (selectedDances.contains(dance)) {
                selectedDances.remove(dance);
            } else {
                selectedDances.add(dance);
            }
            refreshAdapter();
            rebuildSortedList();
        });

        refreshAdapter();
    }

    private void rebuildSortedList() {
        if (adapter == null) return;
        ArrayList<String> sortedDances = new ArrayList<>();
        sortedDances.addAll(selectedDances);
        for (String dance : availableDances) {
            if (!sortedDances.contains(dance)) {
                sortedDances.add(dance);
            }
        }
        adapter.clear();
        adapter.addAll(sortedDances);
        adapter.notifyDataSetChanged();
    }

    private void refreshAdapter() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateConfirmText();
    }

    private void updateConfirmText() {
        confirmText.setText(Translations.getConfirm(lang) + " (" + selectedDances.size() + ")");
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            settingsManager.setSelectedDancesList(selectedDances);
            finish();
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
