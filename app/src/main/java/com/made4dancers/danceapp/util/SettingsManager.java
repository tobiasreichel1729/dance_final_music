package com.made4dancers.danceapp.util;

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

    private static final String PREFS_NAME = "Made4Dancers";
    private static final String KEY_DANCE_STYLE = "danceStyle";
    private static final String KEY_DANCE_LEVEL = "danceLevel";
    private static final String KEY_SELECTED_DANCES = "selectedDances";
    private static final String KEY_MUSIC_DURATION = "musicDuration";
    private static final String KEY_MUSIC_PAUSE = "musicPause";
    private static final String KEY_BURST_COUNT = "burstCount";
    private static final String KEY_BURST_PAUSE = "burstPause";
    private static final String KEY_THEME = "theme";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ACCENT_COLOR_INDEX = "accentColorIndex";
    private static final String KEY_MUSIC_FOLDER_PATHS = "musicFolderPaths";

    private static final String DEFAULT_DANCE_STYLE = "standard";
    private static final String DEFAULT_DANCE_LEVEL = "D";
    private static final String DEFAULT_SELECTED_DANCES = "[]";
    private static final int DEFAULT_MUSIC_DURATION = 60;
    private static final int DEFAULT_MUSIC_PAUSE = 10;
    private static final int DEFAULT_BURST_COUNT = 1;
    private static final int DEFAULT_BURST_PAUSE = 10;
    private static final String DEFAULT_THEME = "dark";
    private static final String DEFAULT_LANGUAGE = "de";
    private static final int DEFAULT_ACCENT_COLOR_INDEX = 0;
    private static final String DEFAULT_MUSIC_FOLDER_PATHS = "{}";

    private static SettingsManager instance;
    private final SharedPreferences prefs;
    private final List<OnSettingsChangeListener> listeners = new ArrayList<>();
    private final Random random = new Random();

    public interface OnSettingsChangeListener {
        void onSettingChanged(String key, Object newValue);
    }

    private SettingsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        JSONArray array = new JSONArray();
        for (String dance : dances) {
            array.put(dance);
        }
        setSelectedDances(array.toString());
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

    public void setMusicFolderPath(String style, String danceName, String folderUri) {
        try {
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

    public void clearAudioCache() {
        audioCache.clear();
    }

    public List<Uri> getAudioFilesFromFolder(Context context, String folderUriStr) {
        if (folderUriStr == null || folderUriStr.isEmpty()) return new ArrayList<>();

        if (audioCache.containsKey(folderUriStr)) {
            return audioCache.get(folderUriStr);
        }

        List<Uri> audioFiles = new ArrayList<>();

        try {
            Uri treeUri = Uri.parse(folderUriStr);
            String treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId);

            String[] projection = {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
            };

            try (Cursor cursor = context.getContentResolver().query(
                    childrenUri, projection, null, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String docId = cursor.getString(0);
                        String mimeType = cursor.getString(1);
                        String name = cursor.getString(2);

                        if (mimeType != null && mimeType.startsWith("audio/")) {
                            Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
                            audioFiles.add(fileUri);
                        } else if (name != null) {
                            String lower = name.toLowerCase();
                            if (lower.endsWith(".mp3") || lower.endsWith(".m4a") ||
                                    lower.endsWith(".wav") || lower.endsWith(".ogg") ||
                                    lower.endsWith(".flac") || lower.endsWith(".aac") ||
                                    lower.endsWith(".wma")) {
                                Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
                                audioFiles.add(fileUri);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        audioCache.put(folderUriStr, audioFiles);
        return audioFiles;
    }

    public Uri getRandomAudioFromFolder(Context context, String style, String danceName) {
        String folderPath = getMusicFolderPath(style, danceName);
        if (folderPath.isEmpty()) return null;

        List<Uri> files = getAudioFilesFromFolder(context, folderPath);
        if (files.isEmpty()) return null;

        return files.get(random.nextInt(files.size()));
    }

    // Available Dances (all for style, no level filtering)
    public String[] getAvailableDances(String style) {
        if ("standard".equals(style)) {
            return new String[]{"Walzer", "Tango", "Wiener Walzer", "Langsamer Foxtrott", "Quickstep"};
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
                return new String[]{"Walzer", "Tango", "Wiener Walzer", "Langsamer Foxtrott", "Quickstep"};
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
