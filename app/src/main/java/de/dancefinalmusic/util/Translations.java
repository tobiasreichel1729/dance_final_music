package de.dancefinalmusic.util;

public class Translations {

    private Translations() {
    }

    public static String getAppTitle(String lang) {
        return "de".equals(lang) ? "Dance Final Music" : "Dance Final Music";
    }

    public static String getSettings(String lang) {
        return "de".equals(lang) ? "Einstellungen" : "Settings";
    }

    public static String getBack(String lang) {
        return "de".equals(lang) ? "Zurück" : "Back";
    }

    public static String getDanceStyle(String lang) {
        return "de".equals(lang) ? "Tanzstil" : "Dance Style";
    }

    public static String getStandardLabel(String lang) {
        return "de".equals(lang) ? "Standard" : "Standard";
    }

    public static String getLateinLabel(String lang) {
        return "de".equals(lang) ? "Latein" : "Latin";
    }

    public static String getSelectDances(String lang) {
        return "de".equals(lang) ? "Tänze auswählen" : "Select Dances";
    }

    public static String getLevel(String lang) {
        return "de".equals(lang) ? "Stufe" : "Level";
    }

    public static String getMusicTiming(String lang) {
        return "de".equals(lang) ? "Musikeinstellungen" : "Music Timing";
    }

    public static String getMusicDuration(String lang) {
        return "de".equals(lang) ? "Musikdauer" : "Music Duration";
    }

    public static String getMusicPause(String lang) {
        return "de".equals(lang) ? "Musikpause" : "Music Break";
    }

    public static String getMusicDurationSeconds(String lang, int value) {
        return "de".equals(lang) ? "Musikdauer (Sek): " + value + "s" : "Music Duration (sec): " + value + "s";
    }

    public static String getMusicPauseSeconds(String lang, int value) {
        return "de".equals(lang) ? "Pause zwischen (Sek): " + value + "s" : "Break between (sec): " + value + "s";
    }

    public static String getBurstSettings(String lang) {
        return "de".equals(lang) ? "Runden" : "Rounds";
    }

    public static String getRounds(String lang) {
        return "de".equals(lang) ? "Runden" : "Rounds";
    }

    public static String getRoundsCountLabel(String lang) {
        return "de".equals(lang) ? "Anzahl Runden" : "Number of Rounds";
    }

    public static String getRoundsCount(String lang, int count) {
        return "de".equals(lang) ? "Anzahl Runden: " + count : "Number of Rounds: " + count;
    }

    public static String getRoundPause(String lang, int seconds) {
        return "de".equals(lang) ? "Pause zwischen Runden: " + seconds + "s" : "Break between Rounds: " + seconds + "s";
    }

    public static String getBurstCount(String lang) {
        return "de".equals(lang) ? "Runden" : "Rounds";
    }

    public static String getBurstPause(String lang) {
        return "de".equals(lang) ? "Pause zwischen Runden" : "Break between Rounds";
    }

    public static String getStart(String lang) {
        return "de".equals(lang) ? "Starten" : "Start";
    }

    public static String getStop(String lang) {
        return "de".equals(lang) ? "Stoppen" : "Stop";
    }

    public static String getSelectAll(String lang) {
        return "de".equals(lang) ? "Alle auswählen" : "Select All";
    }

    public static String getDeselectAll(String lang) {
        return "de".equals(lang) ? "Alle abwählen" : "Deselect All";
    }

    public static String getConfirm(String lang) {
        return "de".equals(lang) ? "Bestätigen" : "Confirm";
    }

    public static String getCancel(String lang) {
        return "de".equals(lang) ? "Abbrechen" : "Cancel";
    }

    public static String getRemainingTime(String lang) {
        return "de".equals(lang) ? "Verbleibende Zeit" : "Remaining Time";
    }

    public static String getCurrentDance(String lang) {
        return "de".equals(lang) ? "Aktueller Tanz" : "Current Dance";
    }

    public static String getBurst(String lang) {
        return "de".equals(lang) ? "Burst" : "Burst";
    }

    public static String getPauseLabel(String lang) {
        return "de".equals(lang) ? "Pause" : "Break";
    }

    public static String getFinished(String lang) {
        return "de".equals(lang) ? "Beendet" : "Finished";
    }

    public static String getNextDance(String lang) {
        return "de".equals(lang) ? "Nächster Tanz" : "Next Dance";
    }

    public static String getMusicManagement(String lang) {
        return "de".equals(lang) ? "Musikverwaltung" : "Music Management";
    }

    public static String getImportMusic(String lang) {
        return "de".equals(lang) ? "Musik importieren" : "Import Music";
    }

    public static String getRemoveMusic(String lang) {
        return "de".equals(lang) ? "Musik entfernen" : "Remove Music";
    }

    public static String getThemeSettings(String lang) {
        return "de".equals(lang) ? "Design-Einstellungen" : "Theme Settings";
    }

    public static String getDarkMode(String lang) {
        return "de".equals(lang) ? "Dunkler Modus" : "Dark Mode";
    }

    public static String getLightMode(String lang) {
        return "de".equals(lang) ? "Heller Modus" : "Light Mode";
    }

    public static String getSystemMode(String lang) {
        return "de".equals(lang) ? "System" : "System";
    }

    public static String getAccentColor(String lang) {
        return "de".equals(lang) ? "Akzentfarbe" : "Accent Color";
    }

    public static String getLanguageSettings(String lang) {
        return "de".equals(lang) ? "Spracheinstellungen" : "Language Settings";
    }

    public static String getGerman(String lang) {
        return "de".equals(lang) ? "Deutsch" : "German";
    }

    public static String getEnglish(String lang) {
        return "de".equals(lang) ? "Englisch" : "English";
    }

    public static String getAbout(String lang) {
        return "de".equals(lang) ? "Über" : "About";
    }

    public static String getContact(String lang) {
        return "de".equals(lang) ? "Kontakt" : "Contact";
    }

    public static String getSupport(String lang) {
        return "de".equals(lang) ? "Unterstütze mich" : "Support me";
    }

    public static String getBpmScopeHint(String lang, String dance) {
        String name = dance != null && !dance.isEmpty() ? dance : "";
        return "de".equals(lang)
                ? "BPM – nur für den Einzeltanz" + (name.isEmpty() ? "" : " (" + name + ")")
                : "BPM - single dance only" + (name.isEmpty() ? "" : " (" + name + ")");
    }

    public static String getSpeedScopeHint(String lang) {
        return "de".equals(lang)
                ? "Speed – gilt für das Dance Final"
                : "Speed - applies to the dance final";
    }

    public static String getCopyrights(String lang) {
        return "de".equals(lang) ? "Urheberrechte" : "Copyrights";
    }

    public static String getThanks(String lang) {
        return "de".equals(lang) ? "Danke" : "Thanks";
    }

    public static String getNoMusicSet(String lang) {
        return "de".equals(lang) ? "Kein Musikordner" : "No music folder";
    }

    public static String getFolderSet(String lang) {
        return "de".equals(lang) ? "Ordner ausgewählt" : "Folder selected";
    }

    public static String getFolderAccessLost(String lang) {
        return "de".equals(lang) ? "Zugriff verloren – Ordner erneut wählen" : "Access lost - re-select folder";
    }

    public static String getMusicAccessLostHint(String lang) {
        return "de".equals(lang) ? "Kein Zugriff auf den Musikordner. Bitte in den Einstellungen erneut auswählen."
                : "No access to the music folder. Please re-select it in the settings.";
    }

    public static String getNoFolderTitle(String lang) {
        return "de".equals(lang) ? "Musikordner fehlen" : "No music folders yet";
    }

    public static String getNoFolderMessage(String lang) {
        return "de".equals(lang)
                ? "Wähle in den Einstellungen einen Musikordner für jeden Tanz aus, damit die App Titel und BPM der Tracks lesen kann."
                : "Choose a music folder for each dance in the settings so the app can read the track names and BPM.";
    }

    public static String getNoFolderForDances(String lang, String dances) {
        String base = "de".equals(lang)
                ? "Für folgende Tänze ist noch kein Musikordner ausgewählt: "
                : "No music folder selected for: ";
        return base + dances;
    }

    public static String getOpenSettings(String lang) {
        return "de".equals(lang) ? "Zu den Einstellungen" : "Open settings";
    }

    public static String getNotNow(String lang) {
        return "de".equals(lang) ? "Später" : "Not now";
    }

    public static String getSingleDanceTitle(String lang) {
        return "de".equals(lang) ? "Einzeltanz abspielen" : "Play Single Dance";
    }

    public static String getTempo(String lang) {
        return "de".equals(lang) ? "Tempo" : "Tempo";
    }

    public static String getSpeedLabel(String lang) {
        return "de".equals(lang) ? "Tempo" : "Speed";
    }

    public static String getBpmLabel(String lang) {
        return "BPM";
    }

    public static String getTempoPercent(String lang, float tempo) {
        return "de".equals(lang) ? "Tempo: " + Math.round(tempo * 100) + "%"
                : "Tempo: " + Math.round(tempo * 100) + "%";
    }

    public static String getOff(String lang) {
        return "de".equals(lang) ? "Aus" : "Off";
    }

    public static String getAuto(String lang) {
        return "de".equals(lang) ? "Auto" : "Auto";
    }

    public static String getBpmDialogHint(String lang) {
        return "de".equals(lang)
                ? "0 = Auto (nutzt das Tempo %)"
                : "0 = Auto (uses tempo %)";
    }

    public static String getNowPlaying(String lang) {
        return "de".equals(lang) ? "Aktuell" : "Now playing";
    }

    public static String getQueueTitle(String lang) {
        return "de".equals(lang) ? "Warteschlange" : "Queue";
    }

    public static String getQueueHint(String lang) {
        return "de".equals(lang)
                ? "Tippe auf einen Song, um ihn sofort abzuspielen"
                : "Tap a song to play it immediately";
    }

    public static String getQueueEmpty(String lang) {
        return "de".equals(lang)
                ? "Keine Warteschlange. Starte einen Einzeltanz."
                : "No queue. Start a single dance.";
    }

    public static String getQueueLoading(String lang) {
        return "de".equals(lang) ? "Wird geladen..." : "Loading...";
    }

    public static String getNowPlayingTitle(String lang) {
        return "de".equals(lang) ? "Wiedergabe" : "Now Playing";
    }

    public static String getPlaybackOptions(String lang) {
        return "de".equals(lang) ? "Wiedergabeoptionen" : "Playback Options";
    }

    public static String getEffects(String lang) {
        return "de".equals(lang) ? "Musik-Effekte" : "Music Effects";
    }

    public static String getFadeOut(String lang) {
        return "de".equals(lang) ? "Sanftes Ausblenden" : "Fade out";
    }

    public static String getApplause(String lang) {
        return "de".equals(lang) ? "Applaus am Ende" : "Applause at the end";
    }


    public static String getSelectDancesHint(String lang) {
        return "de".equals(lang) ? "Bitte wähle zuerst Tänze aus" : "Please select dances first";
    }

    public static String getSeconds(String lang) {
        return "de".equals(lang) ? "Sekunden" : "Seconds";
    }

    public static String getSkip(String lang) {
        return "de".equals(lang) ? "Überspringen" : "Skip";
    }

    /**
     * Dance names double as persistence keys for the selected-dance lists, BPM targets
     * and music folder paths, so the stored name stays stable while only the visible
     * text is localized. Renames go through SettingsManager.RENAMED_DANCES.
     */
    public static String getDanceName(String lang, String dance) {
        if (dance == null || dance.isEmpty()) return "";
        if ("de".equals(lang)) return dance;
        switch (dance) {
            case "Walzer":
                return "Walz";
            case "Wiener Walzer":
                return "Viennese Waltz";
            default:
                return dance;
        }
    }

}
