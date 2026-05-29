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
 * Loads and caches clique-specific item decoration data from
 * {@code clique_decorations.json}.  Decorations describe how a clique
 * accessorizes possessions like cell phones, backpacks, lockers, etc.
 * and are deliberately separate from the trait/condition descriptor
 * system: traits describe what an item <i>is</i>, decorations describe
 * how its owner has personalized it.
 *
 * <p>The JSON is shaped as:</p>
 * <pre>
 * year -&gt; clique -&gt; gender -&gt; itemType -&gt; slot -&gt; [strings]
 *                              -&gt; "colors": [strings]
 * </pre>
 *
 * <p>An empty slot list (or a missing slot/itemType) means the loader
 * has no opinion for that combination; callers fall through to skipping
 * decoration for the slot.</p>
 *
 * <p>The design mirrors {@link CliquePiercingLoader} so the two systems
 * remain conceptually parallel.</p>
 */
public final class CliqueDecorationLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DECORATIONS_PATH =
            "/Resources/Cliques/clique_decorations.json";
    private static final String YEAR = "2004";
    private static final String COLORS_KEY = "colors";

    private static boolean loaded = false;

    // clique -> gender ("female"/"male") -> CliqueDecorationData
    private static final Map<String, Map<String, CliqueDecorationData>> data =
            new HashMap<>();

    private CliqueDecorationLoader() {
    }

    /**
     * Holds the decoration catalog for one clique+gender combination.
     */
    public static final class CliqueDecorationData implements Serializable {
        private static final long serialVersionUID = 1L;

        // itemType -> slot -> decoration types
        private final Map<String, Map<String, List<String>>> decorationsByItem;
        private final List<String> colors;

        CliqueDecorationData(Map<String, Map<String, List<String>>> decorationsByItem,
                             List<String> colors) {
            this.decorationsByItem = decorationsByItem;
            this.colors = colors;
        }

        public Map<String, Map<String, List<String>>> getDecorationsByItem() {
            return Collections.unmodifiableMap(decorationsByItem);
        }

        public List<String> getColors() {
            return Collections.unmodifiableList(colors);
        }

        /**
         * @return the slot map for the given item type, or an empty
         *         immutable map if the clique has no entries for that
         *         item type
         */
        public Map<String, List<String>> getSlotsForItem(String itemType) {
            Map<String, List<String>> slots = decorationsByItem.get(itemType);
            return slots == null ? Map.of() : Collections.unmodifiableMap(slots);
        }

        /**
         * @return true if any slot for the given item type has at least
         *         one decoration option defined
         */
        public boolean hasItemEntries(String itemType) {
            Map<String, List<String>> slots = decorationsByItem.get(itemType);
            if (slots == null) {
                return false;
            }
            for (List<String> options : slots.values()) {
                if (options != null && !options.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    // ---- Public API ----

    /**
     * Returns the decoration options for a clique/gender/itemType/slot
     * combination.
     *
     * @param clique   clique name (e.g. {@code "Emo"})
     * @param gender   {@code "Female"} or {@code "Male"} (case-insensitive)
     * @param itemType item-type key (e.g. {@code "cellphone"})
     * @param slot     slot key on the item (e.g. {@code "case"})
     * @return list of decoration descriptors, or empty list when none
     */
    public static List<String> getDecorationTypes(String clique, String gender,
                                                  String itemType, String slot) {
        CliqueDecorationData entry = resolve(clique, gender);
        if (entry == null) {
            return List.of();
        }
        Map<String, List<String>> slots = entry.getSlotsForItem(itemType);
        return slots.getOrDefault(slot, List.of());
    }

    /**
     * Returns the color palette for a clique/gender combination.  The
     * palette is shared across all item types (a clique's colors carry
     * over from earrings to phones to backpacks).
     */
    public static List<String> getColors(String clique, String gender) {
        CliqueDecorationData entry = resolve(clique, gender);
        return entry == null ? List.of() : entry.getColors();
    }

    /**
     * Returns the slot keys defined under {@code itemType} for the
     * given clique/gender.  Useful to drive decoration rolls without
     * hard-coding slot lists in Java.
     */
    public static List<String> getSlots(String clique, String gender, String itemType) {
        CliqueDecorationData entry = resolve(clique, gender);
        if (entry == null) {
            return List.of();
        }
        return new ArrayList<>(entry.getSlotsForItem(itemType).keySet());
    }

    /**
     * @return the full decoration data for the clique/gender, or
     *         {@code null} if no data is registered
     */
    public static CliqueDecorationData getData(String clique, String gender) {
        return resolve(clique, gender);
    }

    /**
     * @return true if the clique has at least one populated decoration
     *         slot for the given item type, in any gender
     */
    public static boolean hasDecorationData(String clique, String itemType) {
        ensureLoaded();
        Map<String, CliqueDecorationData> genderMap = data.get(clique);
        if (genderMap == null) {
            return false;
        }
        for (CliqueDecorationData entry : genderMap.values()) {
            if (entry.hasItemEntries(itemType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the specific clique/gender pair has at least one
     *         populated decoration slot for the given item type
     */
    public static boolean hasDecorationData(String clique, String gender,
                                            String itemType) {
        CliqueDecorationData entry = resolve(clique, gender);
        return entry != null && entry.hasItemEntries(itemType);
    }

    /**
     * Resets the in-memory cache.  Intended for tests.
     */
    public static synchronized void reset() {
        data.clear();
        loaded = false;
    }

    // ---- Resolution ----

    private static CliqueDecorationData resolve(String clique, String gender) {
        ensureLoaded();
        if (clique == null || gender == null) {
            return null;
        }
        Map<String, CliqueDecorationData> genderMap = data.get(clique);
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
            loadDecorations();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load clique decoration data", e);
        }
    }

    private static void loadDecorations() throws IOException, ParseException {
        JSONObject root;
        try (var reader = ResourceAccess.reader(DECORATIONS_PATH)) {
            root = (JSONObject) new JSONParser().parse(reader);
        }
        JSONObject yearData = (JSONObject) root.get(YEAR);
        if (yearData == null) {
            return;
        }

        for (Object cliqueKey : yearData.keySet()) {
            String cliqueName = (String) cliqueKey;
            Object cliqueVal = yearData.get(cliqueKey);
            if (!(cliqueVal instanceof JSONObject cliqueObj)) {
                continue;
            }

            Map<String, CliqueDecorationData> genderMap = new HashMap<>();
            if (cliqueObj.containsKey("female")) {
                genderMap.put("female",
                        parseGenderEntry((JSONObject) cliqueObj.get("female")));
            }
            if (cliqueObj.containsKey("male")) {
                genderMap.put("male",
                        parseGenderEntry((JSONObject) cliqueObj.get("male")));
            }
            if (!genderMap.isEmpty()) {
                data.put(cliqueName, genderMap);
            }
        }
    }

    private static CliqueDecorationData parseGenderEntry(JSONObject genderObj) {
        Map<String, Map<String, List<String>>> decorationsByItem = new HashMap<>();
        List<String> colors = new ArrayList<>();

        for (Object key : genderObj.keySet()) {
            String keyStr = (String) key;
            Object val = genderObj.get(key);
            if (COLORS_KEY.equals(keyStr)) {
                colors = readStringList(val);
                continue;
            }
            // Anything else is treated as an item-type bucket whose
            // value is a {slot -> [strings]} object.
            if (val instanceof JSONObject itemObj) {
                Map<String, List<String>> slotMap = new HashMap<>();
                for (Object slotKey : itemObj.keySet()) {
                    slotMap.put((String) slotKey,
                            readStringList(itemObj.get(slotKey)));
                }
                decorationsByItem.put(keyStr, slotMap);
            }
        }

        return new CliqueDecorationData(decorationsByItem, colors);
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
