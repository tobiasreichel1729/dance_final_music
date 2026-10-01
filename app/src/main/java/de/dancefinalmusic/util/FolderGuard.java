package de.dancefinalmusic.util;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import java.util.List;

import de.dancefinalmusic.SettingsActivity;

/**
 * Guards every entry point that needs music folders, so a new user is sent to the
 * settings instead of silently getting an empty player.
 */
public final class FolderGuard {

    private FolderGuard() {
    }

    private static long lastPromptAt = 0L;

    /** True when at least one music folder has been configured. */
    public static boolean hasAnyFolder(Activity activity) {
        return SettingsManager.getInstance(activity).hasAnyMusicFolder();
    }

    /**
     * Asks for the folders of the given dances. Returns true when all of them are ready.
     * Shows a dialog with a direct link to the settings when something is missing.
     */
    public static boolean ensureFolders(Activity activity, List<String> dances) {
        SettingsManager settings = SettingsManager.getInstance(activity);
        String lang = settings.getLanguage();
        String style = settings.getDanceStyle();
        List<String> missing = settings.getDancesWithoutFolder(style, dances);
        if (missing.isEmpty()) return true;
        showDialog(activity, Translations.getNoFolderForDances(lang, String.join(", ", missing)));
        return false;
    }

    /**
     * Checks one dance: folder configured and still readable.
     * Returns true when playback may start.
     */
    public static boolean ensureFolder(Activity activity, String style, String dance) {
        SettingsManager settings = SettingsManager.getInstance(activity);
        String lang = settings.getLanguage();
        String folderPath = settings.getMusicFolderPath(style, dance);
        if (folderPath == null || folderPath.isEmpty()) {
            showDialog(activity, Translations.getNoFolderForDances(lang, dance));
            return false;
        }
        if (!settings.hasFolderAccess(activity, folderPath)) {
            Toast.makeText(activity, Translations.getMusicAccessLostHint(lang), Toast.LENGTH_LONG).show();
            showDialog(activity, Translations.getMusicAccessLostHint(lang));
            return false;
        }
        return true;
    }

    /** First-launch hint for a brand new user with nothing configured. */
    public static void promptOnFirstRun(Activity activity) {
        if (hasAnyFolder(activity)) return;
        long now = System.currentTimeMillis();
        if (now - lastPromptAt < 2000) return;
        lastPromptAt = now;
        showDialog(activity, Translations.getNoFolderMessage(SettingsManager.getInstance(activity).getLanguage()));
    }

    private static void showDialog(Activity activity, String message) {
        SettingsManager settings = SettingsManager.getInstance(activity);
        String lang = settings.getLanguage();
        String theme = settings.getTheme();
        int accentIndex = settings.getAccentColorIndex();
        int pad = ThemeHelper.dpToPx(activity, 20);

        android.widget.LinearLayout root = new android.widget.LinearLayout(activity);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, ThemeHelper.dpToPx(activity, 4));
        root.addView(ThemeHelper.createDialogTitle(activity, Translations.getNoFolderTitle(lang), theme));

        android.widget.TextView body = new android.widget.TextView(activity);
        body.setText(message);
        body.setTextSize(15);
        body.setTextColor(ThemeHelper.getOnSurfaceVariantColor(theme));
        root.addView(body);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setView(root)
                .setPositiveButton(Translations.getOpenSettings(lang), (d, w) ->
                        activity.startActivity(new Intent(activity, SettingsActivity.class)))
                .setNegativeButton(Translations.getNotNow(lang), null)
                .create();
        dialog.show();
        ThemeHelper.styleDialog(dialog, theme, accentIndex);
    }
}
