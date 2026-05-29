package utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utility.io.ResourceAccess;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and caches clique-specific haircut data from clique_haircuts.json.
 * Each clique entry contains "female" and "male" sub-objects, each of which
 * contains race sub-objects ("white", "black", "api", "hispanic", "2prace")
 * with hair-length keys mapping to style lists, plus "dyes" and "highlights".
 * Cliques that have not yet been filled in (no gender keys) are skipped
 * and will fall back to generic hair logic during generation.
 */
public final class CliqueHaircutLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String HAIRCUTS_PATH =
            "/Resources/Cliques/clique_haircuts.json";
    private static final String YEAR = "2004";

    private static final String[] MALE_LENGTH_KEYS = {
            "very short", "short", "chin-length", "long",
            "shoulder-length", "waist-length"
    };

    private static final String[] FEMALE_LENGTH_KEYS = {
            "short", "chin-length", "neck-length",
            "shoulder-length", "waist-length", "extremely long"
    };

    private static final String[] RACE_KEYS = {
            "white", "black", "api", "hispanic", "2prace"
    };

    private static boolean loaded = false;

    // clique -> gender -> race -> CliqueHaircutData
    private static final Map<String, Map<String, Map<String, CliqueHaircutData>>> haircutData =
            new HashMap<>();

    private CliqueHaircutLoader() {
    }

    /**
     * Holds the haircut catalog for one clique+gender+race combination.
     */
    public static class CliqueHaircutData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<String, List<String>> stylesByLength;
        private final List<String> dyes;
        private final List<String> highlights;

        CliqueHaircutData(Map<String, List<String>> stylesByLength,
                          List<String> dyes, List<String> highlights) {
            this.stylesByLength = stylesByLength;
            this.dyes = dyes;
            this.highlights = highlights;
        }

        public Map<String, List<String>> getStylesByLength() {
            return Collections.unmodifiableMap(stylesByLength);
        }

        public List<String> getDyes() {
            return Collections.unmodifiableList(dyes);
        }

        public List<String> getHighlights() {
            return Collections.unmodifiableList(highlights);
        }

        public boolean hasAnyData() {
            for (List<String> styles : stylesByLength.values()) {
                if (!styles.isEmpty()) {
                    return true;
                }
            }
            return !dyes.isEmpty() || !highlights.isEmpty();
        }
    }

    // ---- Public API ----

    /**
     * Returns the style options for a clique/gender/race/hairLength.
     */
    public static List<String> getStyles(String clique, String gender,
                                         String race, String hairLength) {
        CliqueHaircutData data = resolve(clique, gender, race);
        if (data == null) {
            return List.of();
        }
        return data.stylesByLength.getOrDefault(hairLength, List.of());
    }

    /**
     * Returns the dye color options for a clique/gender/race combination.
     */
    public static List<String> getDyes(String clique, String gender, String race) {
        CliqueHaircutData data = resolve(clique, gender, race);
        return data == null ? List.of() : data.getDyes();
    }

    /**
     * Returns the highlight color options for a clique/gender/race combination.
     */
    public static List<String> getHighlights(String clique, String gender, String race) {
        CliqueHaircutData data = resolve(clique, gender, race);
        return data == null ? List.of() : data.getHighlights();
    }

    /**
     * Returns true if the clique has any non-empty haircut entries
     * (for at least one gender+race).
     */
    public static boolean hasHaircutData(String clique) {
        ensureLoaded();
        Map<String, Map<String, CliqueHaircutData>> genderMap = haircutData.get(clique);
        if (genderMap == null) {
            return false;
        }
        for (Map<String, CliqueHaircutData> raceMap : genderMap.values()) {
            for (CliqueHaircutData data : raceMap.values()) {
                if (data.hasAnyData()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the clique has non-empty haircut data for the
     * specific gender and race.
     */
    public static boolean hasHaircutData(String clique, String gender, String race) {
        CliqueHaircutData data = resolve(clique, gender, race);
        return data != null && data.hasAnyData();
    }

    // ---- Resolution ----

    private static CliqueHaircutData resolve(String clique, String gender, String race) {
        ensureLoaded();
        Map<String, Map<String, CliqueHaircutData>> genderMap = haircutData.get(clique);
        if (genderMap == null) {
            return null;
        }
        Map<String, CliqueHaircutData> raceMap = genderMap.get(gender.toLowerCase());
        if (raceMap == null) {
            return null;
        }
        return raceMap.get(race.toLowerCase());
    }

    // ---- Loading ----

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadHaircuts();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load clique haircut data", e);
        }
    }

    private static void loadHaircuts() throws IOException, ParseException {
        JSONObject root;
        try (var reader = ResourceAccess.reader(HAIRCUTS_PATH)) {
            root = (JSONObject) new JSONParser().parse(reader);
        }
        JSONObject yearData = (JSONObject) root.get(YEAR);

        for (Object key : yearData.keySet()) {
            String cliqueName = (String) key;
            JSONObject cliqueObj = (JSONObject) yearData.get(cliqueName);

            if (!isGenderedEntry(cliqueObj)) {
                continue;
            }

            Map<String, Map<String, CliqueHaircutData>> genderMap = new HashMap<>();
            if (cliqueObj.containsKey("female")) {
                genderMap.put("female",
                        parseGenderData((JSONObject) cliqueObj.get("female"),
                                FEMALE_LENGTH_KEYS));
            }
            if (cliqueObj.containsKey("male")) {
                genderMap.put("male",
                        parseGenderData((JSONObject) cliqueObj.get("male"),
                                MALE_LENGTH_KEYS));
            }
            haircutData.put(cliqueName, genderMap);
        }
    }

    private static boolean isGenderedEntry(JSONObject cliqueObj) {
        return cliqueObj.containsKey("female") || cliqueObj.containsKey("male");
    }

    private static Map<String, CliqueHaircutData> parseGenderData(
            JSONObject genderObj, String[] lengthKeys) {
        Map<String, CliqueHaircutData> raceMap = new HashMap<>();
        for (String raceKey : RACE_KEYS) {
            JSONObject raceObj = (JSONObject) genderObj.get(raceKey);
            if (raceObj != null) {
                raceMap.put(raceKey, parseRaceData(raceObj, lengthKeys));
            }
        }
        return raceMap;
    }

    private static CliqueHaircutData parseRaceData(JSONObject obj,
                                                   String[] lengthKeys) {
        Map<String, List<String>> styles = new HashMap<>();
        for (String lengthKey : lengthKeys) {
            styles.put(lengthKey, readStringList(obj.get(lengthKey)));
        }

        List<String> dyes = readStringList(obj.get("dyes"));
        List<String> highlights = readStringList(obj.get("highlights"));

        return new CliqueHaircutData(styles, dyes, highlights);
    }

    private static List<String> readStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof JSONArray array) {
            for (Object entry : array) {
                if (entry != null) {
                    result.add(entry.toString());
                }
            }
        }
        return result;
    }
}
