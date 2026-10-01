package de.dancefinalmusic.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class ThemeHelper {

    public static final int[] ACCENT_COLORS = {
            0xFFE53935, // red
            0xFFFB8C00, // orange
            0xFFFDD835, // yellow
            0xFF9CCC65, // light green
            0xFF43A047, // green
            0xFF26A69A, // teal
            0xFF1E88E5, // blue
            0xFF3949AB, // indigo
            0xFF8E24AA, // purple
            0xFFD81B60  // pink
    };

    public static final String[] ACCENT_COLOR_NAMES_DE = {
            "Rot",
            "Orange",
            "Gelb",
            "Hellgrün",
            "Grün",
            "Türkis",
            "Blau",
            "Indigo",
            "Lila",
            "Rosa"
    };

    public static final String[] ACCENT_COLOR_NAMES_EN = {
            "Red",
            "Orange",
            "Yellow",
            "Light Green",
            "Green",
            "Teal",
            "Blue",
            "Indigo",
            "Purple",
            "Pink"
    };

    // Dark theme colors (violet-tinted)
    private static final int DARK_BACKGROUND = 0xFF14111C;
    private static final int DARK_SURFACE = 0xFF1D1929;
    private static final int DARK_SURFACE_VARIANT = 0xFF2A2540;
    private static final int DARK_ON_BACKGROUND = 0xFFEAE6F5;
    private static final int DARK_ON_SURFACE = 0xFFEAE6F5;
    private static final int DARK_ON_SURFACE_VARIANT = 0xFFB6AFCE;
    private static final int DARK_BORDER = 0xFF3B3454;
    private static final int DARK_BUTTON_BG = 0xFF262140;
    private static final int DARK_BUTTON_HOVER = 0xFF352E52;

    // Light theme colors (lavender-tinted)
    private static final int LIGHT_BACKGROUND = 0xFFF4F1FA;
    private static final int LIGHT_SURFACE = 0xFFFFFFFF;
    private static final int LIGHT_SURFACE_VARIANT = 0xFFECE8F6;
    private static final int LIGHT_ON_BACKGROUND = 0xFF211C2E;
    private static final int LIGHT_ON_SURFACE = 0xFF211C2E;
    private static final int LIGHT_ON_SURFACE_VARIANT = 0xFF5F5877;
    private static final int LIGHT_BORDER = 0xFFD6D0E8;
    private static final int LIGHT_BUTTON_BG = 0xFFFFFFFF;
    private static final int LIGHT_BUTTON_HOVER = 0xFFE7E2F2;

    private ThemeHelper() {
    }

    public static int getAccentColor(int index) {
        int clamped = Math.max(0, Math.min(9, index));
        return ACCENT_COLORS[clamped];
    }

    public static int getAccentColorIndex(int index) {
        return Math.max(0, Math.min(9, index));
    }

    public static String getEffectiveTheme(Context context, String theme) {
        if (context == null || !"system".equals(theme)) return theme;
        int nightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES ? "dark" : "light";
    }

    public static int getBackgroundColor(String theme) {
        return "light".equals(theme) ? LIGHT_BACKGROUND : DARK_BACKGROUND;
    }

    public static int getSurfaceColor(String theme) {
        return "light".equals(theme) ? LIGHT_SURFACE : DARK_SURFACE;
    }

    public static int getSurfaceVariantColor(String theme) {
        return "light".equals(theme) ? LIGHT_SURFACE_VARIANT : DARK_SURFACE_VARIANT;
    }

    public static int getOnBackgroundColor(String theme) {
        return "light".equals(theme) ? LIGHT_ON_BACKGROUND : DARK_ON_BACKGROUND;
    }

    public static int getOnSurfaceColor(String theme) {
        return "light".equals(theme) ? LIGHT_ON_SURFACE : DARK_ON_SURFACE;
    }

    public static int getOnSurfaceVariantColor(String theme) {
        return "light".equals(theme) ? LIGHT_ON_SURFACE_VARIANT : DARK_ON_SURFACE_VARIANT;
    }

    public static int getBorderColor(String theme) {
        return "light".equals(theme) ? LIGHT_BORDER : DARK_BORDER;
    }

    /**
     * The app themes its UI by hand, but dialogs use the platform (light) theme,
     * so the window background and buttons have to be colored explicitly.
     */
    public static void styleDialog(androidx.appcompat.app.AlertDialog dialog, String theme, int accentIndex) {
        if (dialog == null) return;
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(getSurfaceColor(theme)));
        }
        int[] buttons = {
                android.content.DialogInterface.BUTTON_NEGATIVE,
                android.content.DialogInterface.BUTTON_NEUTRAL,
                android.content.DialogInterface.BUTTON_POSITIVE
        };
        for (int which : buttons) {
            android.widget.Button button = dialog.getButton(which);
            if (button != null) {
                button.setTextColor(getAccentColor(accentIndex));
            }
        }
    }

    public static android.widget.TextView createDialogTitle(Activity activity, String text, String theme) {
        android.widget.TextView title = new android.widget.TextView(activity);
        title.setText(text);
        title.setTextSize(18);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setTextColor(getOnSurfaceColor(theme));
        title.setPadding(dpToPx(activity, 4), 0, dpToPx(activity, 4), dpToPx(activity, 8));
        return title;
    }

    public static int dpToPx(Activity activity, int dp) {
        return Math.round(dp * activity.getResources().getDisplayMetrics().density);
    }

    public static int getButtonBorderColor(String theme) {
        return "light".equals(theme) ? LIGHT_ON_BACKGROUND : DARK_BORDER;
    }

    public static int getButtonBgColor(String theme) {
        return "light".equals(theme) ? LIGHT_BUTTON_BG : DARK_BUTTON_BG;
    }

    public static int getButtonHoverColor(String theme) {
        return "light".equals(theme) ? LIGHT_BUTTON_HOVER : DARK_BUTTON_HOVER;
    }

    public static int getTimerBgColor(int accentIndex) {
        int color = getAccentColor(accentIndex);
        return Color.argb(
                (int) (0.15 * 255),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    public static void applyTheme(Activity activity, String theme, int accentIndex) {
        if (activity == null) return;

        Window window = activity.getWindow();
        if (window == null) return;

        boolean light = "light".equals(theme);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(light);
            controller.setAppearanceLightNavigationBars(light);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setStatusBarColor(getSurfaceColor(theme));
            window.setNavigationBarColor(getBackgroundColor(theme));
        }
    }

    public static GradientDrawable createCircleBackground(String theme) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(getSurfaceVariantColor(theme));
        return circle;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public static void tintSwitch(android.widget.Switch sw, int accent, String theme) {
        if (sw == null) return;
        int thumbChecked = accent;
        int thumbUnchecked = "light".equals(theme) ? 0xFFF5F5F5 : 0xFFD9D5E6;
        int trackChecked = withAlpha(accent, 0x80);
        int trackUnchecked = getBorderColor(theme);
        int[][] states = new int[][]{{android.R.attr.state_checked}, {}};
        sw.setThumbTintList(new android.content.res.ColorStateList(
                states, new int[]{thumbChecked, thumbUnchecked}));
        sw.setTrackTintList(new android.content.res.ColorStateList(
                states, new int[]{trackChecked, trackUnchecked}));
    }
}
