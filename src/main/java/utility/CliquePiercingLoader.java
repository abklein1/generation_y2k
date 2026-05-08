package utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and caches clique-specific piercing data from clique_piercings.json.
 * Each clique entry is expected to contain "female" and "male" sub-objects.
 * Cliques that have not yet been filled in (no gender keys) are skipped
 * and will fall back to generic piercing logic during generation.
 */
public final class CliquePiercingLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PIERCINGS_PATH =
            "src/main/java/Resources/Cliques/clique_piercings.json";
    private static final String YEAR = "2004";

    private static final String[] SLOT_KEYS = {
            "left ear", "right ear", "nose", "lips",
            "eyebrow", "tongue", "navel"
    };

    private static boolean loaded = false;

    // clique -> gender ("female"/"male") -> CliquePiercingData
    private static final Map<String, Map<String, CliquePiercingData>> piercingData =
            new HashMap<>();

    private CliquePiercingLoader() {
    }

    /**
     * Holds the piercing catalog for one clique+gender combination.
     */
    public static class CliquePiercingData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<String, List<String>> piercingsBySlot;
        private final List<String> colors;
        private final List<String> materials;
        private final List<String> jewels;

        CliquePiercingData(Map<String, List<String>> piercingsBySlot,
                           List<String> colors, List<String> materials,
                           List<String> jewels) {
            this.piercingsBySlot = piercingsBySlot;
            this.colors = colors;
            this.materials = materials;
            this.jewels = jewels;
        }

        public Map<String, List<String>> getPiercingsBySlot() {
            return Collections.unmodifiableMap(piercingsBySlot);
        }

        public List<String> getColors() {
            return Collections.unmodifiableList(colors);
        }

        public List<String> getMaterials() {
            return Collections.unmodifiableList(materials);
        }

        public List<String> getJewels() {
            return Collections.unmodifiableList(jewels);
        }

        /**
         * Returns true if any slot has at least one piercing type defined.
         */
        public boolean hasAnyPiercings() {
            for (List<String> types : piercingsBySlot.values()) {
                if (!types.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    // ---- Public API ----

    /**
     * Returns the piercing types available for a clique/gender/slot.
     *
     * @param clique   the clique name
     * @param gender   "Female" or "Male"
     * @param slotName the slot key (e.g. "left ear", "nose")
     * @return list of piercing type strings, or empty list
     */
    public static List<String> getPiercingTypes(String clique, String gender,
                                                String slotName) {
        CliquePiercingData data = resolve(clique, gender);
        if (data == null) {
            return List.of();
        }
        return data.piercingsBySlot.getOrDefault(slotName, List.of());
    }

    /**
     * Returns the color palette for a clique/gender combination.
     */
    public static List<String> getColors(String clique, String gender) {
        CliquePiercingData data = resolve(clique, gender);
        return data == null ? List.of() : data.getColors();
    }

    /**
     * Returns the material options for a clique/gender combination.
     */
    public static List<String> getMaterials(String clique, String gender) {
        CliquePiercingData data = resolve(clique, gender);
        return data == null ? List.of() : data.getMaterials();
    }

    /**
     * Returns the jewel/gemstone options for a clique/gender combination.
     * An empty list means jewels should not be applied to this clique's
     * piercings (the default for cliques whose materials are metals only).
     */
    public static List<String> getJewels(String clique, String gender) {
        CliquePiercingData data = resolve(clique, gender);
        return data == null ? List.of() : data.getJewels();
    }

    /**
     * Returns the full piercing data for a clique/gender.
     */
    public static CliquePiercingData getData(String clique, String gender) {
        return resolve(clique, gender);
    }

    /**
     * Returns true if the clique has any non-empty piercing entries
     * (for at least one gender), meaning clique-specific selection
     * should be used instead of the generic fallback.
     */
    public static boolean hasPiercingData(String clique) {
        ensureLoaded();
        Map<String, CliquePiercingData> genderMap = piercingData.get(clique);
        if (genderMap == null) {
            return false;
        }
        for (CliquePiercingData data : genderMap.values()) {
            if (data.hasAnyPiercings()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the clique has non-empty piercing data for
     * the specific gender.
     */
    public static boolean hasPiercingData(String clique, String gender) {
        CliquePiercingData data = resolve(clique, gender);
        return data != null && data.hasAnyPiercings();
    }

    // ---- Resolution ----

    /**
     * Resolves the CliquePiercingData for a clique+gender.
     * Returns null if the clique has no data for that gender.
     */
    private static CliquePiercingData resolve(String clique, String gender) {
        ensureLoaded();
        Map<String, CliquePiercingData> genderMap = piercingData.get(clique);
        if (genderMap == null) {
            return null;
        }
        return genderMap.get(gender.toLowerCase());
    }

    // ---- Loading ----

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadPiercings();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load clique piercing data", e);
        }
    }

    private static void loadPiercings() throws IOException, ParseException {
        JSONObject root = (JSONObject) new JSONParser().parse(
                new FileReader(PIERCINGS_PATH, StandardCharsets.UTF_8));
        JSONObject yearData = (JSONObject) root.get(YEAR);

        for (Object key : yearData.keySet()) {
            String cliqueName = (String) key;
            JSONObject cliqueObj = (JSONObject) yearData.get(cliqueName);

            if (!isGenderedEntry(cliqueObj)) {
                continue;
            }

            Map<String, CliquePiercingData> genderMap = new HashMap<>();
            if (cliqueObj.containsKey("female")) {
                genderMap.put("female",
                        parseSlotData((JSONObject) cliqueObj.get("female")));
            }
            if (cliqueObj.containsKey("male")) {
                genderMap.put("male",
                        parseSlotData((JSONObject) cliqueObj.get("male")));
            }
            piercingData.put(cliqueName, genderMap);
        }
    }

    /**
     * Detects whether a clique JSON object contains gendered sub-objects.
     * Cliques without "female"/"male" keys are incomplete and skipped.
     */
    private static boolean isGenderedEntry(JSONObject cliqueObj) {
        return cliqueObj.containsKey("female") || cliqueObj.containsKey("male");
    }

    private static CliquePiercingData parseSlotData(JSONObject obj) {
        Map<String, List<String>> slots = new HashMap<>();
        for (String slotKey : SLOT_KEYS) {
            slots.put(slotKey, readStringList(obj.get(slotKey)));
        }

        List<String> colors = readStringList(obj.get("colors"));
        List<String> materials = readStringList(obj.get("materials"));
        List<String> jewels = readStringList(obj.get("jewels"));

        return new CliquePiercingData(slots, colors, materials, jewels);
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
