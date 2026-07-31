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

public class ThemeHelper {

    public static final int[] ACCENT_COLORS = {
            0xFF1E88E5, // blue
            0xFFE53935, // red
            0xFF43A047, // green
            0xFFFB8C00, // orange
            0xFF8E24AA, // purple
            0xFF00ACC1, // cyan
            0xFFD81B60, // pink
            0xFF6D4C41, // brown
            0xFF546E7A, // blueGrey
            0xFFFFB300  // amber
    };

    public static final String[] ACCENT_COLOR_NAMES_DE = {
            "Blau",
            "Rot",
            "Grün",
            "Orange",
            "Lila",
            "Cyan",
            "Rosa",
            "Braun",
            "Blau-Grau",
            "Bernstein"
    };

    public static final String[] ACCENT_COLOR_NAMES_EN = {
            "Blue",
            "Red",
            "Green",
            "Orange",
            "Purple",
            "Cyan",
            "Pink",
            "Brown",
            "Blue-Grey",
            "Amber"
    };

    // Dark theme colors
    private static final int DARK_BACKGROUND = 0xFF121212;
    private static final int DARK_SURFACE = 0xFF1E1E1E;
    private static final int DARK_SURFACE_VARIANT = 0xFF2C2C2C;
    private static final int DARK_ON_BACKGROUND = 0xFFE0E0E0;
    private static final int DARK_ON_SURFACE = 0xFFE0E0E0;
    private static final int DARK_ON_SURFACE_VARIANT = 0xFFB0B0B0;
    private static final int DARK_BORDER = 0xFF3C3C3C;
    private static final int DARK_BUTTON_BG = 0xFF2A2A2A;
    private static final int DARK_BUTTON_HOVER = 0xFF3A3A3A;

    // Light theme colors
    private static final int LIGHT_BACKGROUND = 0xFFF5F5F5;
    private static final int LIGHT_SURFACE = 0xFFFFFFFF;
    private static final int LIGHT_SURFACE_VARIANT = 0xFFE8E8E8;
    private static final int LIGHT_ON_BACKGROUND = 0xFF1A1A1A;
    private static final int LIGHT_ON_SURFACE = 0xFF1A1A1A;
    private static final int LIGHT_ON_SURFACE_VARIANT = 0xFF555555;
    private static final int LIGHT_BORDER = 0xFFD0D0D0;
    private static final int LIGHT_BUTTON_BG = 0xFFFFFFFF;
    private static final int LIGHT_BUTTON_HOVER = 0xFFE0E0E0;

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

        int statusBarColor = getSurfaceColor(theme);
        window.setStatusBarColor(statusBarColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if ("light".equals(theme)) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            } else {
                window.getDecorView().setSystemUiVisibility(0);
            }
        }

        int navBarColor = getBackgroundColor(theme);
        window.setNavigationBarColor(navBarColor);
    }

    public static GradientDrawable createCircleBackground(String theme) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(getSurfaceVariantColor(theme));
        return circle;
    }
}
