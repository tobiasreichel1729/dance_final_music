package de.dancefinalmusic.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class DanceCodeFilter {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

    private DanceCodeFilter() {
    }

    /**
     * Detects the dance encoded in a file name using the conventions of the
     * "Regensburg Tanzmusik" collection. Returns the app's canonical dance
     * name (Samba, Cha-Cha-Cha, Rumba, Paso Doble, Jive) or null if no dance
     * could be detected.
     */
    public static String detectDance(String fileName) {
        String base = fileName;
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        String[] tokens = TOKEN_PATTERN.split(base);

        String[] upper = new String[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            upper[i] = tokens[i].toUpperCase(Locale.ROOT);
        }

        String lastCode = null;
        for (String token : upper) {
            String dance = codeDance(token);
            if (dance != null) {
                lastCode = dance;
            }
        }
        if (lastCode != null) {
            return lastCode;
        }

        for (int i = 0; i < upper.length; i++) {
            if ("TRW".equals(upper[i])) {
                for (int j = i + 1; j < upper.length; j++) {
                    String dance = trwLetterDance(upper[j]);
                    if (dance != null) {
                        return dance;
                    }
                }
            }
        }

        for (String token : upper) {
            String dance = wordDance(token);
            if (dance != null) {
                return dance;
            }
        }

        for (int i = 0; i < upper.length - 1; i++) {
            if ("PASO".equals(upper[i]) && "DOBLE".equals(upper[i + 1])) {
                return "Paso Doble";
            }
        }

        return null;
    }

    /**
     * Extracts the nominal tempo ("BPM") label from a file name using the
     * collection's convention (the number directly after the dance code,
     * e.g. 32 in "107_CC_32_Downtown" or 43 in "BABY JI 43 BPM"). Returns 0
     * when no such value is present.
     */
    public static int parseNominalBpm(String fileName) {
        String base = fileName;
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        String[] tokens = TOKEN_PATTERN.split(base);
        String[] upper = new String[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            upper[i] = tokens[i].toUpperCase(Locale.ROOT);
        }

        int codeIdx = -1;
        for (int i = 0; i < upper.length; i++) {
            if (codeDance(upper[i]) != null) {
                codeIdx = i;
            }
        }
        if (codeIdx == -1) {
            for (int i = 0; i < upper.length; i++) {
                if ("TRW".equals(upper[i])) {
                    for (int j = i + 1; j < upper.length; j++) {
                        if (trwLetterDance(upper[j]) != null) {
                            codeIdx = j;
                        }
                    }
                }
            }
        }
        if (codeIdx != -1) {
            for (int j = codeIdx + 1; j < upper.length; j++) {
                if (isNumeric(upper[j])) {
                    return Integer.parseInt(upper[j]);
                }
            }
        }
        return 0;
    }

    private static boolean isNumeric(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    public static boolean belongsTo(String fileName, String danceName) {
        if (danceName == null || danceName.isEmpty()) {
            return true;
        }
        String detected = detectDance(fileName);
        return detected == null || detected.equals(danceName);
    }

    private static String codeDance(String token) {
        switch (token) {
            case "CC":
            case "CHA":
            case "CH":
                return "Cha-Cha-Cha";
            case "RB":
            case "RU":
                return "Rumba";
            case "SB":
            case "SA":
                return "Samba";
            case "JI":
                return "Jive";
            case "PD":
                return "Paso Doble";
            default:
                return null;
        }
    }

    private static String trwLetterDance(String token) {
        switch (token) {
            case "C":
                return "Cha-Cha-Cha";
            case "J":
                return "Jive";
            case "R":
                return "Rumba";
            case "S":
                return "Samba";
            default:
                return null;
        }
    }

    private static String wordDance(String token) {
        switch (token) {
            case "CHACHA":
                return "Cha-Cha-Cha";
            case "JIVE":
                return "Jive";
            case "SAMBA":
                return "Samba";
            case "RUMBA":
                return "Rumba";
            default:
                return null;
        }
    }
}
