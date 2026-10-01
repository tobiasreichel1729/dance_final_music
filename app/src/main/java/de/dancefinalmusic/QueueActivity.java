package de.dancefinalmusic;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.dancefinalmusic.util.SettingsManager;
import de.dancefinalmusic.util.ThemeHelper;
import de.dancefinalmusic.util.Translations;

import java.util.List;

public class QueueActivity extends AppCompatActivity implements SingleDancePlayer.StateListener {

    private SettingsManager settingsManager;
    private String theme;
    private int accentIndex;
    private String lang;

    private ImageView backBtn;
    private TextView headerTitle;
    private TextView queueInfo;
    private TextView queueHint;
    private LinearLayout queueList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        settingsManager = SettingsManager.getInstance(this);
        theme = ThemeHelper.getEffectiveTheme(this, settingsManager.getTheme());
        accentIndex = settingsManager.getAccentColorIndex();
        lang = settingsManager.getLanguage();

        initViews();
        applyTheme();
        SingleDancePlayer.getInstance().addListener(this);
        rebuild();
    }

    @Override
    protected void onDestroy() {
        SingleDancePlayer.getInstance().removeListener(this);
        super.onDestroy();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        headerTitle = findViewById(R.id.headerTitle);
        queueInfo = findViewById(R.id.queueInfo);
        queueHint = findViewById(R.id.queueHint);
        queueList = findViewById(R.id.queueList);

        headerTitle.setText(Translations.getQueueTitle(lang));
        queueHint.setText(Translations.getQueueHint(lang));
        backBtn.setOnClickListener(v -> MainActivity.goToMain(this));
    }

    @Override
    public void onBackPressed() {
        MainActivity.goToMain(this);
    }

    private void applyTheme() {
        ThemeHelper.applyTheme(this, theme, accentIndex);

        int bg = ThemeHelper.getBackgroundColor(theme);
        int onBg = ThemeHelper.getOnBackgroundColor(theme);
        int onSurfaceVar = ThemeHelper.getOnSurfaceVariantColor(theme);

        getWindow().getDecorView().setBackgroundColor(bg);

        headerTitle.setTextColor(onBg);
        queueHint.setTextColor(onSurfaceVar);

        if (backBtn != null) {
            backBtn.setImageTintList(ColorStateList.valueOf(onBg));
            backBtn.setBackground(ThemeHelper.createCircleBackground(theme));
        }
    }

    @Override
    public void onPlaybackStateChanged() {
        runOnUiThread(this::rebuild);
    }

    private void rebuild() {
        queueList.removeAllViews();

        SingleDancePlayer player = SingleDancePlayer.getInstance();
        int index = player.getCurrentIndex();
        String name = player.getCurrentName();
        int nominal = player.getCurrentNominalBpm();
        int target = settingsManager.getBpmTarget(player.getPlayingDance());
        int accent = ThemeHelper.getAccentColor(accentIndex);
        int surface = ThemeHelper.getSurfaceColor(theme);
        int onSurface = ThemeHelper.getOnSurfaceColor(theme);

        if (!player.isPlaying()) {
            queueInfo.setVisibility(View.GONE);
            queueHint.setVisibility(View.GONE);
            TextView empty = new TextView(this);
            empty.setText(Translations.getQueueEmpty(lang));
            empty.setTextSize(14);
            empty.setTextColor(onSurface);
            queueList.addView(empty);
            return;
        }

        queueInfo.setVisibility(View.VISIBLE);
        queueHint.setVisibility(View.VISIBLE);

        String current = name != null && !name.isEmpty() ? name : Translations.getQueueLoading(lang);
        String bpmSuffix = nominal > 0 ? " (" + nominal + " BPM)" : "";
        String speedSuffix = (target > 0 && nominal > 0)
                ? " - " + Math.round(player.computeSpeedPct(target, nominal)) + "%" : "";
        queueInfo.setText(Translations.getNowPlaying(lang) + ": " + current + bpmSuffix + speedSuffix);

        List<?> q = player.getQueue();
        for (int i = 0; i < q.size(); i++) {
            TextView row = new TextView(this);
            String trackName = player.getQueueName(i);
            String bpm = player.getQueueNominalBpm(i);
            String speed = "";
            if (target > 0 && !bpm.isEmpty()) {
                speed = " - " + Math.round(player.computeSpeedPct(target, Integer.parseInt(bpm))) + "%";
            }
            row.setText((i + 1) + ". " + trackName + (bpm.isEmpty() ? "" : " (" + bpm + " BPM)") + speed);
            row.setTextSize(13);
            row.setPadding(dpToPx(8), dpToPx(10), dpToPx(8), dpToPx(10));
            row.setMaxLines(1);
            row.setEllipsize(android.text.TextUtils.TruncateAt.END);
            if (i == index) {
                row.setBackgroundColor(accent);
                row.setTextColor(Color.WHITE);
            } else {
                row.setBackgroundColor(surface);
                row.setTextColor(onSurface);
            }
            final int pos = i;
            row.setOnClickListener(v -> {
                if (pos == player.getCurrentIndex()) {
                    player.stop();
                } else {
                    player.playAt(pos);
                }
            });
            queueList.addView(row);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
