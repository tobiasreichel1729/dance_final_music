package de.dancefinalmusic;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.databinding.ActivityMainBinding;
import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SingleDancePlayer.StateListener {

    private static final int DURATION_MIN = 10;
    private static final int DURATION_MAX = 300;
    private static final int DURATION_STEP = 5;
    private static final int PAUSE_MIN = 0;
    private static final int PAUSE_MAX = 60;
    private static final int ROUNDS_MIN = 1;
    private static final int ROUNDS_MAX = 10;
    private static final int BURST_PAUSE_MIN = 0;
    private static final int BURST_PAUSE_MAX = 60;
    private static final int TEMPO_MIN = 50;
    private static final int TEMPO_MAX = 150;
    private static final int TEMPO_STEP = 5;

    private static final long HOLD_INITIAL_DELAY_MS = 400;
    private static final long HOLD_MIN_DELAY_MS = 40;

    private ActivityMainBinding binding;
    private SettingsManager settings;
    private String lang = "de";
    private final Handler holdHandler = new Handler(Looper.getMainLooper());
    private long holdDelayMs = HOLD_INITIAL_DELAY_MS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settings = SettingsManager.getInstance(this);
        lang = settings.getLanguage();
        applyThemeAndColors();
        setupListeners();
        updateValues();
        de.dancefinalmusic.util.FolderGuard.promptOnFirstRun(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lang = settings.getLanguage();
        applyThemeAndColors();
        updateValues();
        SingleDancePlayer.getInstance().addListener(this);
        rebuildSingleDanceList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SingleDancePlayer.getInstance().removeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SingleDancePlayer.getInstance().stop();
    }

    @Override
    public void onPlaybackStateChanged() {
        runOnUiThread(this::rebuildSingleDanceList);
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
        int onSurfaceVariant = ThemeHelper.getOnSurfaceVariantColor(theme);

        binding.getRoot().setBackgroundColor(bg);
        binding.appTitle.setTextColor(onBg);
        binding.settingsBtn.setImageTintList(ColorStateList.valueOf(onBg));
        binding.settingsBtn.setBackground(ThemeHelper.createCircleBackground(theme));

        binding.danceStyleCard.setBackgroundColor(surface);
        binding.selectDancesCard.setBackgroundColor(surface);
        binding.singleDanceCard.setBackgroundColor(surface);
        binding.musicTimingCard.setBackgroundColor(surface);
        binding.burstSettingsCard.setBackgroundColor(surface);

        binding.styleHeader.setTextColor(accent);
        binding.selectDancesLabel.setTextColor(accent);
        binding.selectDancesSublabel.setTextColor(onSurfaceVariant);
        binding.singleDanceTitle.setTextColor(accent);
        binding.musicTimingLabel.setTextColor(accent);
        binding.burstSettingsLabel.setTextColor(accent);

        binding.durationLabel.setTextColor(onSurface);
        binding.pauseLabel.setTextColor(onSurface);
        binding.tempoLabel.setTextColor(onSurface);
        binding.roundsLabel.setTextColor(onSurface);
        binding.burstPauseLabel.setTextColor(onSurface);
        binding.durationValue.setTextColor(onBg);
        binding.pauseValue.setTextColor(onBg);
        binding.tempoValue.setTextColor(onBg);
        binding.roundsValue.setTextColor(onBg);
        binding.burstPauseValue.setTextColor(onBg);

        updateStyleButtons(accent, theme);
        styleStepperButton(binding.durationMinusBtn, accent);
        styleStepperButton(binding.durationPlusBtn, accent);
        styleStepperButton(binding.pauseMinusBtn, accent);
        styleStepperButton(binding.pausePlusBtn, accent);
        styleStepperButton(binding.tempoMinusBtn, accent);
        styleStepperButton(binding.tempoPlusBtn, accent);
        styleStepperButton(binding.roundsMinusBtn, accent);
        styleStepperButton(binding.roundsPlusBtn, accent);
        styleStepperButton(binding.burstPauseMinusBtn, accent);
        styleStepperButton(binding.burstPausePlusBtn, accent);

        binding.startButton.setBackgroundColor(accent);
        binding.startButtonText.setTextColor(Color.WHITE);

        updateLabels(lang);
    }

    private void updateStyleButtons(int accent, String theme) {
        applyToggleButton(binding.standardBtn, "standard".equals(settings.getDanceStyle()), accent, theme);
        applyToggleButton(binding.lateinBtn, "latein".equals(settings.getDanceStyle()), accent, theme);
    }

    private void applyToggleButton(Button btn, boolean active, int accent, String theme) {
        if (btn == null) return;
        btn.setBackgroundTintList(null);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(10));
        if (active) {
            drawable.setColor(accent);
            btn.setTextColor(Color.WHITE);
        } else {
            drawable.setColor(ThemeHelper.getButtonBgColor(theme));
            drawable.setStroke(dpToPx(1), ThemeHelper.getButtonBorderColor(theme));
            btn.setTextColor(ThemeHelper.getOnSurfaceColor(theme));
        }
        btn.setBackground(drawable);
    }

    private void styleStepperButton(Button btn, int accent) {
        if (btn == null) return;
        btn.setBackgroundTintList(null);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(10));
        drawable.setColor(accent);
        btn.setBackground(drawable);
        btn.setTextColor(Color.WHITE);
    }

    private void updateLabels(String lang) {
        binding.styleHeader.setText(Translations.getDanceStyle(lang));
        binding.standardBtn.setText(Translations.getStandardLabel(lang));
        binding.lateinBtn.setText(Translations.getLateinLabel(lang));
        binding.selectDancesLabel.setText(Translations.getSelectDances(lang));
        binding.selectDancesSublabel.setText(getSelectedDancesSublabel(lang));
        binding.singleDanceTitle.setText(Translations.getSingleDanceTitle(lang));
        binding.musicTimingLabel.setText(Translations.getMusicTiming(lang));
        binding.durationLabel.setText(Translations.getMusicDuration(lang));
        binding.pauseLabel.setText(Translations.getMusicPause(lang));
        binding.tempoLabel.setText(Translations.getTempo(lang));
        binding.burstSettingsLabel.setText(Translations.getBurstSettings(lang));
        binding.roundsLabel.setText(Translations.getRoundsCountLabel(lang));
        binding.burstPauseLabel.setText(Translations.getBurstPause(lang));
        binding.startButtonText.setText(Translations.getStart(lang));
    }

    private void updateValues() {
        binding.durationValue.setText(settings.getMusicDuration() + "s");
        binding.pauseValue.setText(settings.getMusicPause() + "s");
        binding.tempoValue.setText(Math.round(settings.getTempo() * 100) + "%");
        binding.roundsValue.setText(String.valueOf(settings.getBurstCount()));
        binding.burstPauseValue.setText(settings.getBurstPause() + "s");
    }

    private String getSelectedDancesSublabel(String lang) {
        List<String> dances = settings.getSelectedDancesList();
        if (dances.isEmpty()) {
            return Translations.getSelectDancesHint(lang);
        }
        List<String> display = new ArrayList<>();
        for (String dance : dances) {
            display.add(Translations.getDanceName(lang, dance));
        }
        return String.join(", ", display);
    }

    private void setupListeners() {
        binding.settingsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        binding.selectDancesCard.setOnClickListener(v ->
                startActivity(new Intent(this, DanceSelectActivity.class)));

        binding.standardBtn.setOnClickListener(v -> setDanceStyle("standard"));
        binding.lateinBtn.setOnClickListener(v -> setDanceStyle("latein"));

        setupHoldToRepeat(binding.durationMinusBtn, () -> changeDuration(-DURATION_STEP));
        setupHoldToRepeat(binding.durationPlusBtn, () -> changeDuration(DURATION_STEP));
        setupHoldToRepeat(binding.pauseMinusBtn, () -> changePause(-1));
        setupHoldToRepeat(binding.pausePlusBtn, () -> changePause(1));
        setupHoldToRepeat(binding.tempoMinusBtn, () -> changeTempo(-TEMPO_STEP));
        setupHoldToRepeat(binding.tempoPlusBtn, () -> changeTempo(TEMPO_STEP));
        setupHoldToRepeat(binding.roundsMinusBtn, () -> changeRounds(-1));
        setupHoldToRepeat(binding.roundsPlusBtn, () -> changeRounds(1));
        setupHoldToRepeat(binding.burstPauseMinusBtn, () -> changeBurstPause(-1));
        setupHoldToRepeat(binding.burstPausePlusBtn, () -> changeBurstPause(1));

        binding.durationValue.setOnClickListener(v ->
                startInlineEdit(binding.durationValue, settings.getMusicDuration(),
                        DURATION_MIN, DURATION_MAX, DURATION_STEP, value -> {
                            settings.setMusicDuration(value);
                            updateValues();
                        }));
        binding.pauseValue.setOnClickListener(v ->
                startInlineEdit(binding.pauseValue, settings.getMusicPause(),
                        PAUSE_MIN, PAUSE_MAX, 1, value -> {
                            settings.setMusicPause(value);
                            updateValues();
                        }));
        binding.roundsValue.setOnClickListener(v ->
                startInlineEdit(binding.roundsValue, settings.getBurstCount(),
                        ROUNDS_MIN, ROUNDS_MAX, 1, value -> {
                            settings.setBurstCount(value);
                            updateValues();
                        }));
        binding.burstPauseValue.setOnClickListener(v ->
                startInlineEdit(binding.burstPauseValue, settings.getBurstPause(),
                        BURST_PAUSE_MIN, BURST_PAUSE_MAX, 1, value -> {
                            settings.setBurstPause(value);
                            updateValues();
                        }));
        binding.tempoValue.setOnClickListener(v ->
                startInlineEdit(binding.tempoValue, Math.round(settings.getTempo() * 100),
                        TEMPO_MIN, TEMPO_MAX, TEMPO_STEP, value -> {
                            settings.setTempo(value / 100f);
                            SingleDancePlayer.getInstance().updateTempo();
                            updateValues();
                        }));

        binding.startButton.setOnClickListener(v -> {
            SingleDancePlayer.getInstance().stop();
            List<String> dances = settings.getSelectedDancesList();
            if (dances.isEmpty()) {
                startActivity(new Intent(this, DanceSelectActivity.class));
            } else {
                if (!de.dancefinalmusic.util.FolderGuard.ensureFolders(this, dances)) {
                    return;
                }
                startActivity(new Intent(this, DanceTimerActivity.class));
            }
        });
    }

    private void rebuildSingleDanceList() {
        LinearLayout list = binding.singleDanceList;
        list.removeAllViews();

        String theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        int accent = ThemeHelper.getAccentColor(settings.getAccentColorIndex());
        String style = settings.getDanceStyle();
        String playingDance = SingleDancePlayer.getInstance().getPlayingDance();

        for (String dance : settings.getAvailableDances(style)) {
            boolean isPlaying = dance.equals(playingDance) && SingleDancePlayer.getInstance().isPlaying();
            Button btn = new Button(this);
            btn.setText(Translations.getDanceName(settings.getLanguage(), dance));
            btn.setTextSize(11);
            btn.setAllCaps(false);
            btn.setPadding(dpToPx(2), 0, dpToPx(2), 0);
            btn.setMinWidth(0);
            btn.setMinHeight(0);
            applyToggleButton(btn, isPlaying, accent, theme);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    dpToPx(44), 1f);
            lp.setMargins(dpToPx(1), 0, dpToPx(1), 0);
            btn.setOnClickListener(v -> {
                SingleDancePlayer player = SingleDancePlayer.getInstance();
                if (dance.equals(player.getPlayingDance())) {
                    startActivity(new Intent(this, PlayerActivity.class));
                } else {
                    if (!de.dancefinalmusic.util.FolderGuard.ensureFolder(this, style, dance)) {
                        return;
                    }
                    player.start(this, style, dance);
                    startActivity(new Intent(this, PlayerActivity.class));
                }
            });
            list.addView(btn, lp);
        }
    }

    private void changeTempo(int delta) {
        int percent = clamp(Math.round(settings.getTempo() * 100) + delta, TEMPO_MIN, TEMPO_MAX);
        settings.setTempo(percent / 100f);
        SingleDancePlayer.getInstance().updateTempo();
        updateValues();
    }

    private void setDanceStyle(String style) {
        if (style.equals(settings.getDanceStyle())) return;
        settings.setDanceStyle(style);
        applyThemeAndColors();
        rebuildSingleDanceList();
    }

    private void setupHoldToRepeat(Button btn, Runnable action) {
        Runnable repeat = new Runnable() {
            @Override
            public void run() {
                action.run();
                holdDelayMs = Math.max(HOLD_MIN_DELAY_MS, (long) (holdDelayMs * 0.85));
                holdHandler.postDelayed(this, holdDelayMs);
            }
        };

        btn.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    holdHandler.removeCallbacksAndMessages(null);
                    holdDelayMs = HOLD_INITIAL_DELAY_MS;
                    action.run();
                    holdHandler.postDelayed(repeat, holdDelayMs);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    holdHandler.removeCallbacksAndMessages(null);
                    return true;
                default:
                    return true;
            }
        });
    }

    private void changeDuration(int delta) {
        int value = clamp(settings.getMusicDuration() + delta, DURATION_MIN, DURATION_MAX);
        settings.setMusicDuration(value);
        updateValues();
    }

    private void changePause(int delta) {
        int value = clamp(settings.getMusicPause() + delta, PAUSE_MIN, PAUSE_MAX);
        settings.setMusicPause(value);
        updateValues();
    }

    private void changeRounds(int delta) {
        int value = clamp(settings.getBurstCount() + delta, ROUNDS_MIN, ROUNDS_MAX);
        settings.setBurstCount(value);
        updateValues();
    }

    private void changeBurstPause(int delta) {
        int value = clamp(settings.getBurstPause() + delta, BURST_PAUSE_MIN, BURST_PAUSE_MAX);
        settings.setBurstPause(value);
        updateValues();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void startInlineEdit(TextView tv, int currentValue, int min, int max, int step,
                                 java.util.function.IntConsumer onValue) {
        ViewGroup parent = (ViewGroup) tv.getParent();
        int index = parent.indexOfChild(tv);

        EditText edit = new EditText(this);
        edit.setInputType(InputType.TYPE_CLASS_NUMBER);
        edit.setText(String.valueOf(currentValue));
        edit.setSelectAllOnFocus(true);
        edit.setSingleLine(true);
        edit.setGravity(tv.getGravity());
        edit.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize());
        edit.setTextColor(tv.getCurrentTextColor());
        edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edit.setLayoutParams(tv.getLayoutParams());

        parent.addView(edit, index);
        parent.removeView(tv);

        edit.requestFocus();
        edit.post(() -> {
            edit.selectAll();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
        });

        edit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                commitInlineEdit(parent, edit, index, tv, min, max, step, onValue);
                return true;
            }
            return false;
        });
        edit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) commitInlineEdit(parent, edit, index, tv, min, max, step, onValue);
        });
    }

    private void commitInlineEdit(ViewGroup parent, EditText edit, int index, TextView tv,
                                  int min, int max, int step, java.util.function.IntConsumer onValue) {
        edit.setOnEditorActionListener(null);
        edit.setOnFocusChangeListener(null);

        String text = edit.getText().toString().trim();
        if (!text.isEmpty()) {
            try {
                int value = Integer.parseInt(text);
                if (step > 1) {
                    value = Math.round((float) value / step) * step;
                }
                onValue.accept(clamp(value, min, max));
            } catch (NumberFormatException ignored) {
            }
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);

        parent.removeView(edit);
        parent.addView(tv, index);
        updateValues();
    }

    public static void goToMain(android.app.Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
}
