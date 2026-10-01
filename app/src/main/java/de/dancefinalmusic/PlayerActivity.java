package de.dancefinalmusic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

public class PlayerActivity extends AppCompatActivity implements SingleDancePlayer.StateListener {

    private static final int TEMPO_MIN = 50;
    private static final int TEMPO_MAX = 150;
    private static final int TEMPO_STEP = 5;
    private static final int BPM_MAX = 200;

    private SettingsManager settings;
    private String theme;
    private int accentIndex;
    private int accentColor;
    private String lang;

    private ImageView backBtn;
    private TextView headerTitle;
    private ImageView overflowBtn;
    private ImageView albumArt;
    private TextView songTitle;
    private TextView songSubtitle;
    private SeekBar progressBar;
    private TextView currentTime;
    private TextView totalTime;
    private ImageButton prevBtn;
    private ImageButton playPauseBtn;
    private ImageButton nextBtn;
    private Button queueBtn;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean dragging = false;
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            uiHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        settings = SettingsManager.getInstance(this);
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        lang = settings.getLanguage();

        bindViews();
        applyTheme();
        SingleDancePlayer.getInstance().addListener(this);
        refreshNowPlaying();
        uiHandler.post(progressRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = ThemeHelper.getEffectiveTheme(this, settings.getTheme());
        accentIndex = settings.getAccentColorIndex();
        accentColor = ThemeHelper.getAccentColor(accentIndex);
        lang = settings.getLanguage();
        applyTheme();
        refreshNowPlaying();
        uiHandler.removeCallbacks(progressRunnable);
        uiHandler.post(progressRunnable);
        String dance = SingleDancePlayer.getInstance().getPlayingDance();
        if (!SingleDancePlayer.getInstance().isPlaying() && dance != null) {
            de.dancefinalmusic.util.FolderGuard.ensureFolder(
                    this, settings.getDanceStyle(), dance);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(progressRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SingleDancePlayer.getInstance().removeListener(this);
        uiHandler.removeCallbacks(progressRunnable);
    }

    @Override
    public void onBackPressed() {
        MainActivity.goToMain(this);
    }

    private void bindViews() {
        backBtn = findViewById(R.id.backBtn);
        headerTitle = findViewById(R.id.headerTitle);
        overflowBtn = findViewById(R.id.overflowBtn);
        albumArt = findViewById(R.id.albumArt);
        songTitle = findViewById(R.id.songTitle);
        songSubtitle = findViewById(R.id.songSubtitle);
        progressBar = findViewById(R.id.progressBar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        prevBtn = findViewById(R.id.prevBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        nextBtn = findViewById(R.id.nextBtn);
        queueBtn = findViewById(R.id.queueBtn);

        backBtn.setOnClickListener(v -> MainActivity.goToMain(this));
        overflowBtn.setOnClickListener(v -> showOptionsDialog());
        prevBtn.setOnClickListener(v -> SingleDancePlayer.getInstance().previous());
        nextBtn.setOnClickListener(v -> SingleDancePlayer.getInstance().next());
        playPauseBtn.setOnClickListener(v -> SingleDancePlayer.getInstance().togglePlayPause());
        queueBtn.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, QueueActivity.class)));

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int dur = SingleDancePlayer.getInstance().getCurrentDurationMs();
                    if (dur > 0) {
                        currentTime.setText(formatTime(dur * progress / 1000));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                dragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int dur = SingleDancePlayer.getInstance().getCurrentDurationMs();
                if (dur > 0) {
                    SingleDancePlayer.getInstance().seekTo(dur * seekBar.getProgress() / 1000);
                }
                dragging = false;
            }
        });
    }

    private void applyTheme() {
        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int onSurfaceVar = ThemeHelper.getOnSurfaceVariantColor(theme);

        ThemeHelper.applyTheme(this, theme, ThemeHelper.getAccentColorIndex(accentIndex));

        findViewById(android.R.id.content).getRootView().setBackgroundColor(bg);

        headerTitle.setText(Translations.getNowPlayingTitle(lang));
        headerTitle.setTextColor(onBg);
        songTitle.setTextColor(onBg);
        songSubtitle.setTextColor(onSurfaceVar);
        currentTime.setTextColor(onSurfaceVar);
        totalTime.setTextColor(onSurfaceVar);

        if (backBtn != null) {
            backBtn.setImageTintList(ColorStateList.valueOf(onBg));
            backBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }
        if (overflowBtn != null) {
            overflowBtn.setImageTintList(ColorStateList.valueOf(onBg));
            overflowBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }

        progressBar.setProgressTintList(ColorStateList.valueOf(accentColor));
        progressBar.setThumbTintList(ColorStateList.valueOf(accentColor));

        playPauseBtn.setBackgroundTintList(null);
        GradientDrawable playBg = new GradientDrawable();
        playBg.setShape(GradientDrawable.OVAL);
        playBg.setColor(accentColor);
        playPauseBtn.setBackground(playBg);

        prevBtn.setImageTintList(ColorStateList.valueOf(onBg));
        nextBtn.setImageTintList(ColorStateList.valueOf(onBg));

        queueBtn.setBackgroundTintList(null);
        GradientDrawable qBg = new GradientDrawable();
        qBg.setCornerRadius(dpToPx(12));
        qBg.setStroke(dpToPx(2), accentColor);
        qBg.setColor(Color.TRANSPARENT);
        queueBtn.setBackground(qBg);
        queueBtn.setTextColor(accentColor);
    }

    @Override
    public void onPlaybackStateChanged() {
        runOnUiThread(this::refreshNowPlaying);
    }

    private String currentDance() {
        return SingleDancePlayer.getInstance().getPlayingDance();
    }

    private void refreshNowPlaying() {
        SingleDancePlayer player = SingleDancePlayer.getInstance();
        String name = player.getCurrentName();
        int nominal = player.getCurrentNominalBpm();
        String dance = player.getPlayingDance();
        int target = settings.getBpmTarget(dance);

        if (name == null || name.isEmpty()) {
            name = Translations.getQueueLoading(lang);
        }
        songTitle.setText(name);
        if (dance != null && !dance.isEmpty()) {
            headerTitle.setText(Translations.getDanceName(lang, dance));
        } else {
            headerTitle.setText(Translations.getNowPlayingTitle(lang));
        }

        songSubtitle.setText(buildModeSubtitle(player, nominal, target));
        songSubtitle.setTextColor(subtitleColor());

        if (player.isPaused()) {
            playPauseBtn.setImageResource(R.drawable.ic_play);
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
        }

        loadAlbumArt(player.getCurrentUri());
        updateProgress();
    }

    /**
     * In dark mode the BPM/speed line is a dimmed white so it stays readable
     * without pulling attention away from the track title.
     */
    private int subtitleColor() {
        if (!"light".equals(theme)) {
            int white = android.graphics.Color.WHITE;
            int alpha = Math.round(255 * 0.45f);
            return (alpha << 24) | (white & 0x00FFFFFF);
        }
        return ThemeHelper.getOnSurfaceVariantColor(theme);
    }

    private String buildModeSubtitle(SingleDancePlayer player, int nominal, int target) {
        if (settings.getSpeedMode() == SettingsManager.SPEED_MODE_BPM) {
            if (target > 0) return target + " BPM";
            if (nominal > 0) return nominal + " BPM";
            return Translations.getOff(lang);
        }
        return Translations.getTempoPercent(lang, settings.getTempo());
    }

    private void updateProgress() {
        SingleDancePlayer player = SingleDancePlayer.getInstance();
        int dur = player.getCurrentDurationMs();
        int pos = player.getCurrentPositionMs();
        if (dur > 0) {
            if (!dragging) {
                progressBar.setMax(1000);
                progressBar.setProgress((int) (1000L * pos / dur));
            }
            currentTime.setText(formatTime(pos));
            totalTime.setText(formatTime(dur));
        } else {
            progressBar.setProgress(0);
            currentTime.setText("0:00");
            totalTime.setText("0:00");
        }
    }

    private void loadAlbumArt(final Uri uri) {
        if (uri == null) return;
        new Thread(() -> {
            Bitmap art = null;
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(this, uri);
                byte[] data = mmr.getEmbeddedPicture();
                if (data != null) {
                    art = android.graphics.BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            } catch (Exception e) {
                art = null;
            } finally {
                try {
                    mmr.release();
                } catch (Exception ignored) {
                }
            }
            final Bitmap result = art;
            runOnUiThread(() -> {
                if (result != null) {
                    albumArt.setImageBitmap(result);
                } else {
                    albumArt.setImageDrawable(getDrawable(R.drawable.ic_music_note));
                }
            });
        }).start();
    }

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        int surface = ThemeHelper.getSurfaceColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(20);
        root.setPadding(pad, dpToPx(16), pad, dpToPx(8));

        GradientDrawable card = new GradientDrawable();
        card.setCornerRadius(dpToPx(24));
        card.setColor(surface);
        root.setBackground(card);

        // Title drawn by us so it follows the app theme instead of the light platform dialog
        root.addView(ThemeHelper.createDialogTitle(this, Translations.getPlaybackOptions(lang), theme));

        // Mode toggle: highlighted text, one click switches between BPM and Speed
        TextView toggleChip = new TextView(this);
        toggleChip.setTextSize(15);
        toggleChip.setGravity(Gravity.CENTER);
        toggleChip.setTextColor(ThemeHelper.getOnSurfaceColor(theme));
        toggleChip.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setCornerRadius(dpToPx(20));
        chipBg.setColor(ThemeHelper.getSurfaceColor(theme));
        toggleChip.setBackground(chipBg);
        root.addView(toggleChip);

        TextView modeHint = new TextView(this);
        modeHint.setTextSize(13);
        modeHint.setGravity(Gravity.CENTER);
        modeHint.setTextColor(ThemeHelper.getOnSurfaceVariantColor(theme));
        modeHint.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(6));
        root.addView(modeHint);

        SeekBar slider = new SeekBar(this);
        slider.setProgressTintList(ColorStateList.valueOf(accentColor));
        slider.setThumbTintList(ColorStateList.valueOf(accentColor));
        root.addView(slider);

        TextView val = new TextView(this);
        val.setTextSize(14);
        val.setGravity(Gravity.END);
        val.setTextColor(ThemeHelper.getOnSurfaceVariantColor(theme));
        root.addView(val);

        Runnable updateChips = () -> {
            boolean bpmMode = settings.getSpeedMode() == SettingsManager.SPEED_MODE_BPM;
            String speed = Translations.getSpeedLabel(lang);
            String bpm = Translations.getBpmLabel(lang);
            String text = speed + "/" + bpm;
            SpannableString sp = new SpannableString(text);
            int start = bpmMode ? speed.length() + 1 : 0;
            int end = bpmMode ? text.length() : speed.length();
            sp.setSpan(new ForegroundColorSpan(accentColor), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            toggleChip.setText(sp);
            modeHint.setText(bpmMode
                    ? Translations.getBpmScopeHint(lang, currentDance())
                    : Translations.getSpeedScopeHint(lang));
        };
        Runnable updateSlider = () -> {
            int danceTarget = settings.getBpmTarget(currentDance());
            if (settings.getSpeedMode() == SettingsManager.SPEED_MODE_BPM) {
                slider.setMax(BPM_MAX);
                slider.setProgress(danceTarget);
                val.setText(danceTarget == 0
                        ? Translations.getOff(lang) : danceTarget + " BPM");
            } else {
                slider.setMax((TEMPO_MAX - TEMPO_MIN) / TEMPO_STEP);
                slider.setProgress(Math.round((settings.getTempo() * 100 - TEMPO_MIN) / TEMPO_STEP));
                val.setText(Translations.getTempoPercent(lang, settings.getTempo()));
            }
        };

        toggleChip.setOnClickListener(v -> {
            boolean bpmMode = settings.getSpeedMode() == SettingsManager.SPEED_MODE_BPM;
            settings.setSpeedMode(bpmMode
                    ? SettingsManager.SPEED_MODE_TEMPO : SettingsManager.SPEED_MODE_BPM);
            SingleDancePlayer.getInstance().updateTempo();
            refreshNowPlaying();
            updateChips.run();
            updateSlider.run();
        });

        slider.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (settings.getSpeedMode() == SettingsManager.SPEED_MODE_BPM) {
                    val.setText(progress == 0 ? Translations.getOff(lang) : progress + " BPM");
                } else {
                    int pct = TEMPO_MIN + progress * TEMPO_STEP;
                    val.setText(Translations.getTempoPercent(lang, pct / 100f));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (settings.getSpeedMode() == SettingsManager.SPEED_MODE_BPM) {
                    settings.setBpmTarget(currentDance(), seekBar.getProgress());
                } else {
                    int pct = TEMPO_MIN + seekBar.getProgress() * TEMPO_STEP;
                    settings.setTempo(pct / 100f);
                }
                SingleDancePlayer.getInstance().updateTempo();
            }
        });

        updateChips.run();
        updateSlider.run();

        // Fade and applause belong to the dance final and are configured there
        builder.setView(root);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(dialog -> refreshNowPlaying());
        AlertDialog dialog = builder.create();
        dialog.show();
        ThemeHelper.styleDialog(dialog, theme, accentIndex);
    }

    private void styleChip(TextView chip, boolean active) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(20));
        if (active) {
            bg.setColor(accentColor);
            chip.setTextColor(Color.WHITE);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            bg.setColor(ThemeHelper.getSurfaceColor(theme));
            chip.setTextColor(ThemeHelper.getOnSurfaceVariantColor(theme));
            chip.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        chip.setBackground(bg);
        chip.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
    }

    private LinearLayout makeSwitchRow(String label, boolean checked,
                                       Switch.OnCheckedChangeListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dpToPx(8), 0, 0);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(15);
        tv.setTextColor(ThemeHelper.getOnSurfaceColor(theme));
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tv);

        Switch sw = new Switch(this);
        sw.setChecked(checked);
        ThemeHelper.tintSwitch(sw, accentColor, theme);
        sw.setOnCheckedChangeListener(listener);
        row.addView(sw);
        return row;
    }

    private abstract static class SimpleSeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
    }

    private String formatTime(int ms) {
        int totalSeconds = ms / 1000;
        return (totalSeconds / 60) + ":" + String.format(java.util.Locale.GERMANY, "%02d", totalSeconds % 60);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
