package com.example.eduuuu;
import java.util.HashMap;
import java.util.Map;

public class DocentPreferences {
    private static Map<Integer, Boolean> skipWarnings = new HashMap<>();

    public static boolean skipNagekekenWarning(int docentId) {
        return skipWarnings.getOrDefault(docentId, false);
    }

    public static void setSkipNagekekenWarning(int docentId, boolean skip) {
        skipWarnings.put(docentId, skip);
        // eventueel ook opslaan in database:
        // UPDATE Docent SET SkipNagekekenWarning = ? WHERE Docent_ID = ?
    }
}