package de.dancefinalmusic.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.json.JSONArray;

import android.provider.DocumentsContract;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SettingsManager {

    private static final String PREFS_NAME = "DanceFinalMusic";
    private static final String KEY_DANCE_STYLE = "danceStyle";
    private static final String KEY_DANCE_LEVEL = "danceLevel";
    private static final String KEY_SELECTED_DANCES = "selectedDances";
    private static final String KEY_SELECTED_DANCES_BY_STYLE = "selectedDancesByStyle";
    private static final String KEY_MUSIC_DURATION = "musicDuration";
    private static final String KEY_MUSIC_PAUSE = "musicPause";
    private static final String KEY_BURST_COUNT = "burstCount";
    private static final String KEY_BURST_PAUSE = "burstPause";
    private static final String KEY_THEME = "theme";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ACCENT_COLOR_INDEX = "accentColorIndex";
    private static final String KEY_MUSIC_FOLDER_PATHS = "musicFolderPaths";
    private static final String KEY_TEMPO = "tempo";
    private static final String KEY_BPM_TARGET = "bpmTarget";
    private static final String KEY_BPM_REFERENCE = "bpmReference";
    private static final String KEY_SPEED_MODE = "speedMode";
    private static final String KEY_APPLAUSE_ENABLED = "applauseEnabled";
    private static final String KEY_FADE_ENABLED = "fadeEnabled";
    private static final String KEY_RENAMED_DANCES_DONE = "renamedDancesMigrated";

    private static final String DEFAULT_DANCE_STYLE = "standard";
    private static final String DEFAULT_DANCE_LEVEL = "D";
    private static final String DEFAULT_SELECTED_DANCES = "[]";
    private static final String DEFAULT_SELECTED_DANCES_BY_STYLE = "{}";
    private static final int DEFAULT_MUSIC_DURATION = 60;
    private static final int DEFAULT_MUSIC_PAUSE = 30;
    private static final int DEFAULT_BURST_COUNT = 1;
    private static final int DEFAULT_BURST_PAUSE = 60;
    private static final String DEFAULT_THEME = "system";
    private static final String DEFAULT_LANGUAGE = "de";
    private static final int DEFAULT_ACCENT_COLOR_INDEX = 0;
    private static final String DEFAULT_MUSIC_FOLDER_PATHS = "{}";
    private static final float DEFAULT_TEMPO = 1.0f;
    private static final int DEFAULT_BPM_TARGET = 0;
    private static final boolean DEFAULT_APPLAUSE_ENABLED = true;
    private static final boolean DEFAULT_FADE_ENABLED = true;

    private static SettingsManager instance;
    private final SharedPreferences prefs;
    private final List<OnSettingsChangeListener> listeners = new ArrayList<>();
    private final Random random = new Random();

    public interface OnSettingsChangeListener {
        void onSettingChanged(String key, Object newValue);
    }

    private SettingsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        migrateLegacySelectedDances();
        migrateRenamedDances();
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context);
        }
        return instance;
    }

    public void addListener(OnSettingsChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnSettingsChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String key, Object value) {
        for (OnSettingsChangeListener listener : listeners) {
            listener.onSettingChanged(key, value);
        }
    }

    // Dance Style
    public String getDanceStyle() {
        return prefs.getString(KEY_DANCE_STYLE, DEFAULT_DANCE_STYLE);
    }

    public void setDanceStyle(String danceStyle) {
        prefs.edit().putString(KEY_DANCE_STYLE, danceStyle).apply();
        notifyListeners(KEY_DANCE_STYLE, danceStyle);
    }

    // Dance Level
    public String getDanceLevel() {
        return prefs.getString(KEY_DANCE_LEVEL, DEFAULT_DANCE_LEVEL);
    }

    public void setDanceLevel(String danceLevel) {
        prefs.edit().putString(KEY_DANCE_LEVEL, danceLevel).apply();
        notifyListeners(KEY_DANCE_LEVEL, danceLevel);
    }

    // Selected Dances
    public String getSelectedDances() {
        return prefs.getString(KEY_SELECTED_DANCES, DEFAULT_SELECTED_DANCES);
    }

    public void setSelectedDances(String selectedDances) {
        prefs.edit().putString(KEY_SELECTED_DANCES, selectedDances).apply();
        notifyListeners(KEY_SELECTED_DANCES, selectedDances);
    }

    public List<String> getSelectedDancesList() {
        return getSelectedDancesList(getDanceStyle());
    }

    public List<String> getSelectedDancesList(String style) {
        try {
            JSONObject root = new JSONObject(getSelectedDancesByStyle());
            if (root.has(style)) {
                JSONArray array = root.getJSONArray(style);
                List<String> dances = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    dances.add(array.getString(i));
                }
                return dances;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<String> dances = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(getSelectedDances());
            for (int i = 0; i < array.length(); i++) {
                dances.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dances;
    }

    public void setSelectedDancesList(List<String> dances) {
        setSelectedDancesList(getDanceStyle(), dances);
    }

    public void setSelectedDancesList(String style, List<String> dances) {
        try {
            JSONObject root = new JSONObject(getSelectedDancesByStyle());
            JSONArray array = new JSONArray();
            for (String dance : dances) {
                array.put(dance);
            }
            root.put(style, array);
            prefs.edit().putString(KEY_SELECTED_DANCES_BY_STYLE, root.toString()).apply();
            notifyListeners(KEY_SELECTED_DANCES_BY_STYLE, root.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getSelectedDancesByStyle() {
        return prefs.getString(KEY_SELECTED_DANCES_BY_STYLE, DEFAULT_SELECTED_DANCES_BY_STYLE);
    }

    private void migrateLegacySelectedDances() {
        try {
            JSONObject root = new JSONObject(getSelectedDancesByStyle());
            if (root.length() > 0) return;
            JSONArray legacy = new JSONArray(getSelectedDances());
            if (legacy.length() == 0) return;
            root.put(getDanceStyle(), legacy);
            prefs.edit()
                    .putString(KEY_SELECTED_DANCES_BY_STYLE, root.toString())
                    .remove(KEY_SELECTED_DANCES)
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Dances whose canonical name changed. Old -> new. Dance names are keys into the
    // selected-dance lists, BPM prefs and music folder paths, so existing entries have to
    // be rewritten or the user's folders, BPMs and selection would silently disappear.
    private static final Map<String, String> RENAMED_DANCES = new HashMap<>();

    static {
        RENAMED_DANCES.put("Langsamer Foxtrott", "Slow Fox");
        RENAMED_DANCES.put("Langsamer Foxtroot", "Slow Fox");
    }

    private void migrateRenamedDances() {
        if (prefs.getBoolean(KEY_RENAMED_DANCES_DONE, false)) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_SELECTED_DANCES_BY_STYLE,
                renameInDancesJson(prefs.getString(KEY_SELECTED_DANCES_BY_STYLE, DEFAULT_SELECTED_DANCES_BY_STYLE)));
        editor.putString(KEY_SELECTED_DANCES,
                renameInLegacyDancesJson(prefs.getString(KEY_SELECTED_DANCES, DEFAULT_SELECTED_DANCES)));
        editor.putString(KEY_MUSIC_FOLDER_PATHS,
                renameInFolderPathsJson(prefs.getString(KEY_MUSIC_FOLDER_PATHS, DEFAULT_MUSIC_FOLDER_PATHS)));

        for (String old : RENAMED_DANCES.keySet()) {
            String renamed = RENAMED_DANCES.get(old);
            for (String base : new String[]{KEY_BPM_TARGET, KEY_BPM_REFERENCE}) {
                String oldKey = base + "_" + old;
                if (prefs.contains(oldKey)) {
                    editor.putInt(base + "_" + renamed, prefs.getInt(oldKey, 0));
                    editor.remove(oldKey);
                }
            }
        }

        editor.putBoolean(KEY_RENAMED_DANCES_DONE, true).apply();
        clearAudioCache();
    }

    private static String renameInDancesJson(String json) {
        try {
            JSONObject root = new JSONObject(json);
            for (java.util.Iterator<String> it = root.keys(); it.hasNext(); ) {
                String style = it.next();
                JSONArray array = root.optJSONArray(style);
                if (array == null) continue;
                JSONArray renamed = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    String dance = array.getString(i);
                    renamed.put(RENAMED_DANCES.containsKey(dance)
                            ? RENAMED_DANCES.get(dance) : dance);
                }
                root.put(style, renamed);
            }
            return root.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return json;
        }
    }

    private static String renameInLegacyDancesJson(String json) {
        try {
            JSONArray array = new JSONArray(json);
            JSONArray renamed = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                String dance = array.getString(i);
                renamed.put(RENAMED_DANCES.containsKey(dance)
                        ? RENAMED_DANCES.get(dance) : dance);
            }
            return renamed.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return json;
        }
    }

    private static String renameInFolderPathsJson(String json) {
        try {
            JSONObject root = new JSONObject(json);
            for (java.util.Iterator<String> it = root.keys(); it.hasNext(); ) {
                JSONObject styleObj = root.optJSONObject(it.next());
                if (styleObj == null) continue;
                for (String old : RENAMED_DANCES.keySet()) {
                    if (styleObj.has(old)) {
                        styleObj.put(RENAMED_DANCES.get(old), styleObj.getString(old));
                        styleObj.remove(old);
                    }
                }
            }
            return root.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return json;
        }
    }

    // Music Duration
    public int getMusicDuration() {
        return prefs.getInt(KEY_MUSIC_DURATION, DEFAULT_MUSIC_DURATION);
    }

    public void setMusicDuration(int duration) {
        int clamped = Math.max(10, Math.min(300, duration));
        prefs.edit().putInt(KEY_MUSIC_DURATION, clamped).apply();
        notifyListeners(KEY_MUSIC_DURATION, clamped);
    }

    // Music Pause
    public int getMusicPause() {
        return prefs.getInt(KEY_MUSIC_PAUSE, DEFAULT_MUSIC_PAUSE);
    }

    public void setMusicPause(int pause) {
        int clamped = Math.max(0, Math.min(60, pause));
        prefs.edit().putInt(KEY_MUSIC_PAUSE, clamped).apply();
        notifyListeners(KEY_MUSIC_PAUSE, clamped);
    }

    // Burst Count
    public int getBurstCount() {
        return prefs.getInt(KEY_BURST_COUNT, DEFAULT_BURST_COUNT);
    }

    public void setBurstCount(int count) {
        int clamped = Math.max(1, Math.min(10, count));
        prefs.edit().putInt(KEY_BURST_COUNT, clamped).apply();
        notifyListeners(KEY_BURST_COUNT, clamped);
    }

    // Burst Pause (break between rounds)
    public int getBurstPause() {
        return prefs.getInt(KEY_BURST_PAUSE, DEFAULT_BURST_PAUSE);
    }

    public void setBurstPause(int pause) {
        int clamped = Math.max(1, Math.min(30, pause));
        prefs.edit().putInt(KEY_BURST_PAUSE, clamped).apply();
        notifyListeners(KEY_BURST_PAUSE, clamped);
    }

    // Theme
    public String getTheme() {
        return prefs.getString(KEY_THEME, DEFAULT_THEME);
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
        notifyListeners(KEY_THEME, theme);
    }

    // Language
    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public void setLanguage(String language) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
        notifyListeners(KEY_LANGUAGE, language);
    }

    // Accent Color Index
    public int getAccentColorIndex() {
        return prefs.getInt(KEY_ACCENT_COLOR_INDEX, DEFAULT_ACCENT_COLOR_INDEX);
    }

    public void setAccentColorIndex(int index) {
        int clamped = Math.max(0, Math.min(9, index));
        prefs.edit().putInt(KEY_ACCENT_COLOR_INDEX, clamped).apply();
        notifyListeners(KEY_ACCENT_COLOR_INDEX, clamped);
    }

    // Tempo (playback speed, 0.5x - 1.5x)
    public static final int SPEED_MODE_TEMPO = 0;
    public static final int SPEED_MODE_BPM = 1;

    public float getTempo() {
        return prefs.getFloat(KEY_TEMPO, DEFAULT_TEMPO);
    }

    public void setTempo(float tempo) {
        float clamped = Math.max(0.5f, Math.min(1.5f, tempo));
        prefs.edit().putFloat(KEY_TEMPO, clamped).apply();
        notifyListeners(KEY_TEMPO, clamped);
    }

    // BPM target per dance (0 = auto, uses percentage-based tempo)
    public int getBpmTarget(String dance) {
        if (dance == null) return 0;
        return prefs.getInt(KEY_BPM_TARGET + "_" + dance, DEFAULT_BPM_TARGET);
    }

    public void setBpmTarget(String dance, int bpm) {
        if (dance == null) return;
        int clamped = Math.max(0, Math.min(200, bpm));
        prefs.edit().putInt(KEY_BPM_TARGET + "_" + dance, clamped).apply();
        notifyListeners(KEY_BPM_TARGET + "_" + dance, clamped);
    }

    // Reference BPM per dance, learned from track file names (e.g. "107_Sa_51_...").
    // Used as the baseline when a track carries no readable BPM of its own.
    public int getReferenceBpm(String dance) {
        if (dance == null) return 0;
        return prefs.getInt(KEY_BPM_REFERENCE + "_" + dance, 0);
    }

    public void setReferenceBpm(String dance, int bpm) {
        if (dance == null || bpm <= 0) return;
        if (bpm == getReferenceBpm(dance)) return;
        prefs.edit().putInt(KEY_BPM_REFERENCE + "_" + dance, bpm).apply();
    }

    // Baseline a BPM target is applied to: the track's own BPM, else the dance reference
    public int getEffectiveNominalBpm(String dance, int trackNominalBpm) {
        if (trackNominalBpm > 0) return trackNominalBpm;
        return getReferenceBpm(dance);
    }

    // Which speed control is active: percentage tempo or BPM target
    public int getSpeedMode() {
        return prefs.getInt(KEY_SPEED_MODE, SPEED_MODE_TEMPO);
    }

    public void setSpeedMode(int mode) {
        int clamped = (mode == SPEED_MODE_BPM) ? SPEED_MODE_BPM : SPEED_MODE_TEMPO;
        prefs.edit().putInt(KEY_SPEED_MODE, clamped).apply();
        notifyListeners(KEY_SPEED_MODE, clamped);
    }

    // Applause in final round
    public boolean isApplauseEnabled() {
        return prefs.getBoolean(KEY_APPLAUSE_ENABLED, DEFAULT_APPLAUSE_ENABLED);
    }

    public void setApplauseEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_APPLAUSE_ENABLED, enabled).apply();
        notifyListeners(KEY_APPLAUSE_ENABLED, enabled);
    }

    // Fade-out / fade-in
    public boolean isFadeEnabled() {
        return prefs.getBoolean(KEY_FADE_ENABLED, DEFAULT_FADE_ENABLED);
    }

    public void setFadeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_FADE_ENABLED, enabled).apply();
        notifyListeners(KEY_FADE_ENABLED, enabled);
    }

    // Music Folder Paths (stored as URI strings)
    public String getMusicFolderPathsRaw() {
        return prefs.getString(KEY_MUSIC_FOLDER_PATHS, DEFAULT_MUSIC_FOLDER_PATHS);
    }

    public void setMusicFolderPathsRaw(String json) {
        prefs.edit().putString(KEY_MUSIC_FOLDER_PATHS, json).apply();
        notifyListeners(KEY_MUSIC_FOLDER_PATHS, json);
    }

    public String getMusicFolderPath(String style, String danceName) {
        try {
            JSONObject root = new JSONObject(getMusicFolderPathsRaw());
            if (root.has(style)) {
                JSONObject styleObj = root.getJSONObject(style);
                if (styleObj.has(danceName)) {
                    return styleObj.getString(danceName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    // True if at least one dance has a music folder configured
    public boolean hasAnyMusicFolder() {
        try {
            JSONObject root = new JSONObject(getMusicFolderPathsRaw());
            for (java.util.Iterator<String> it = root.keys(); it.hasNext(); ) {
                JSONObject styleObj = root.optJSONObject(it.next());
                if (styleObj == null) continue;
                for (java.util.Iterator<String> d = styleObj.keys(); d.hasNext(); ) {
                    String uri = styleObj.optString(d.next(), "");
                    if (uri != null && !uri.isEmpty()) return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    // All dances of the given style that have no folder configured yet
    public List<String> getDancesWithoutFolder(String style, List<String> dances) {
        List<String> missing = new ArrayList<>();
        if (dances == null) return missing;
        for (String dance : dances) {
            String path = getMusicFolderPath(style, dance);
            if (path == null || path.isEmpty()) missing.add(dance);
        }
        return missing;
    }

    public void setMusicFolderPath(String style, String danceName, String folderUri) {        try {
            JSONObject root = new JSONObject(getMusicFolderPathsRaw());
            JSONObject styleObj;
            if (root.has(style)) {
                styleObj = root.getJSONObject(style);
            } else {
                styleObj = new JSONObject();
            }
            styleObj.put(danceName, folderUri);
            root.put(style, styleObj);
            prefs.edit().putString(KEY_MUSIC_FOLDER_PATHS, root.toString()).apply();
            clearAudioCache();
            notifyListeners(KEY_MUSIC_FOLDER_PATHS, root.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeMusicFolderPath(String style, String danceName) {
        try {
            JSONObject root = new JSONObject(getMusicFolderPathsRaw());
            if (root.has(style)) {
                JSONObject styleObj = root.getJSONObject(style);
                styleObj.remove(danceName);
                if (styleObj.length() == 0) {
                    root.remove(style);
                } else {
                    root.put(style, styleObj);
                }
                prefs.edit().putString(KEY_MUSIC_FOLDER_PATHS, root.toString()).apply();
                notifyListeners(KEY_MUSIC_FOLDER_PATHS, root.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static final java.util.Map<String, List<Uri>> audioCache = new HashMap<>();
    private static final java.util.Map<String, Map<Uri, String>> audioNameCache = new HashMap<>();

    public void clearAudioCache() {
        audioCache.clear();
        audioNameCache.clear();
    }

    public String getCachedFileName(String folderUriStr, String danceName, Uri uri) {
        if (uri == null) return null;
        String cacheKey = folderUriStr + "|" + (danceName == null ? "" : danceName);
        Map<Uri, String> names = audioNameCache.get(cacheKey);
        return names != null ? names.get(uri) : null;
    }

    public List<Uri> getAudioFilesFromFolder(Context context, String folderUriStr) {
        return getAudioFilesFromFolder(context, folderUriStr, null);
    }

    public List<Uri> getAudioFilesFromFolder(Context context, String folderUriStr, String danceName) {
        if (folderUriStr == null || folderUriStr.isEmpty()) return new ArrayList<>();

        String cacheKey = folderUriStr + "|" + (danceName == null ? "" : danceName);
        if (audioCache.containsKey(cacheKey)) {
            return audioCache.get(cacheKey);
        }

        List<Uri> audioFiles = new ArrayList<>();
        boolean queryOk = false;

        try {
            Uri treeUri = Uri.parse(folderUriStr);
            String treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId);

            boolean practiceFolder = isPracticeFolder(context, treeUri, treeDocumentId);

            String[] projection = {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
            };

            try (Cursor cursor = context.getContentResolver().query(
                    childrenUri, projection, null, null, null)) {
                if (cursor != null) {
                    queryOk = true;
                    while (cursor.moveToNext()) {
                        String docId = cursor.getString(0);
                        String mimeType = cursor.getString(1);
                        String name = cursor.getString(2);

                        boolean isAudio = (mimeType != null && mimeType.startsWith("audio/")) ||
                                (name != null && isAudioFileName(name));
                        if (!isAudio) continue;
                        if (danceName != null && !practiceFolder && !DanceCodeFilter.belongsTo(name, danceName)) {
                            continue;
                        }
                        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
                        audioFiles.add(fileUri);
                        if (name != null) {
                            Map<Uri, String> names = audioNameCache.computeIfAbsent(cacheKey, k -> new HashMap<>());
                            names.put(fileUri, name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (queryOk) {
            audioCache.put(cacheKey, audioFiles);
        }
        return audioFiles;
    }

    private static boolean isAudioFileName(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".mp3") || lower.endsWith(".m4a") ||
                lower.endsWith(".wav") || lower.endsWith(".ogg") ||
                lower.endsWith(".flac") || lower.endsWith(".aac") ||
                lower.endsWith(".wma");
    }

    private static boolean isPracticeFolder(Context context, Uri treeUri, String treeDocumentId) {
        try {
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId);
            try (Cursor cursor = context.getContentResolver().query(
                    docUri,
                    new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String name = cursor.getString(0);
                    return name != null && name.contains("Practice Latein");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasFolderAccess(Context context, String folderUriStr) {
        if (folderUriStr == null || folderUriStr.isEmpty()) return false;
        try {
            Uri treeUri = Uri.parse(folderUriStr);
            String treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId);
            try (Cursor cursor = context.getContentResolver().query(
                    childrenUri, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                    null, null, null)) {
                return cursor != null;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public Uri getRandomAudioFromFolder(Context context, String style, String danceName) {
        List<Uri> files = getDanceAudioFiles(context, style, danceName);
        if (files.isEmpty()) return null;
        return files.get(random.nextInt(files.size()));
    }

    public List<Uri> getDanceAudioFiles(Context context, String style, String danceName) {
        String folderPath = getMusicFolderPath(style, danceName);
        if (folderPath.isEmpty()) return new ArrayList<>();
        return getAudioFilesFromFolder(context, folderPath, danceName);
    }

    public String getFileName(Context context, Uri uri) {
        if (uri == null) return "";
        try (Cursor cursor = context.getContentResolver().query(
                uri, new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null && !name.isEmpty()) return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : uri.toString();
    }

    // Available Dances (all for style, no level filtering)
    public String[] getAvailableDances(String style) {
        if ("standard".equals(style)) {
            return new String[]{"Walzer", "Tango", "Wiener Walzer", "Slow Fox", "Quickstep"};
        } else if ("latein".equals(style)) {
            return new String[]{"Samba", "Cha-Cha-Cha", "Rumba", "Paso Doble", "Jive"};
        }
        return new String[]{};
    }

    // Available Dances (with level)
    public String[] getAvailableDances(String style, String level) {
        if ("standard".equals(style)) {
            if ("BAS".equals(level)) {
                return new String[]{"Walzer", "Tango", "Wiener Walzer"};
            } else {
                return new String[]{"Walzer", "Tango", "Wiener Walzer", "Slow Fox", "Quickstep"};
            }
        } else if ("latein".equals(style)) {
            if ("BAS".equals(level)) {
                return new String[]{"Samba", "Cha-Cha-Cha", "Rumba"};
            } else {
                return new String[]{"Samba", "Cha-Cha-Cha", "Rumba", "Paso Doble", "Jive"};
            }
        }
        return new String[]{};
    }

    // Reset
    public void resetAll() {
        prefs.edit().clear().apply();
        notifyListeners("reset", null);
    }
}
