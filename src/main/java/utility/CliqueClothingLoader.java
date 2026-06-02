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
 * Loads and caches clique-specific clothing inventory data from
 * clique_clothing.json. Each clique entry is expected to contain
 * "female" and "male" sub-objects holding inventory categories
 * (e.g. "outerwear", "tops", "bottoms", "one_piece", "shoes",
 * "accessories") plus shared palettes ("colors", "patterns",
 * "materials").
 *
 * <p>Cliques whose gendered objects contain no populated inventory
 * entries are still cached but {@link #hasClothingData(String, String)}
 * returns {@code false} for them so callers can fall back gracefully
 * while the schema is being populated.</p>
 *
 * <p>Mirrors the conventions used by {@link CliquePiercingLoader} and
 * {@link CliqueHaircutLoader}: synchronous file load, year hardcoded
 * to {@code "2004"}, and unmodifiable views returned from accessors.</p>
 */
public final class CliqueClothingLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CLOTHING_PATH =
            "/Resources/Cliques/clique_clothing.json";
    private static final String YEAR = "2004";

    private static final String[] CATEGORY_KEYS = {
            "outerwear", "tops", "bottoms", "one_piece",
            "shoes", "accessories"
    };

    private static boolean loaded = false;

    // clique -> gender ("female"/"male") -> CliqueClothingData
    private static final Map<String, Map<String, CliqueClothingData>> clothingData =
            new HashMap<>();

    private CliqueClothingLoader() {
    }

    /**
     * A single garment option drawn from a clique inventory category.
     * Carries the garment name plus the per-item brand, material, and
     * pattern descriptors that may legitimately attach to it. Keeping
     * these per-item (rather than in a clique-wide palette) avoids
     * nonsense pairings like "denim t-shirt" or "denim wristband".
     */
    public static class ClothingOption implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final List<String> brands;
        private final List<String> materials;
        private final List<String> patterns;

        ClothingOption(String name, List<String> brands,
                       List<String> materials, List<String> patterns) {
            this.name = name;
            this.brands = brands;
            this.materials = materials;
            this.patterns = patterns;
        }

        public String getName() {
            return name;
        }

        public List<String> getBrands() {
            return Collections.unmodifiableList(brands);
        }

        public List<String> getMaterials() {
            return Collections.unmodifiableList(materials);
        }

        public List<String> getPatterns() {
            return Collections.unmodifiableList(patterns);
        }
    }

    /**
     * Holds the clothing catalog for one clique+gender combination.
     */
    public static class CliqueClothingData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<String, List<ClothingOption>> optionsByCategory;
        private final List<String> colors;
        private final List<String> patterns;
        private final List<String> materials;

        CliqueClothingData(Map<String, List<ClothingOption>> optionsByCategory,
                           List<String> colors, List<String> patterns,
                           List<String> materials) {
            this.optionsByCategory = optionsByCategory;
            this.colors = colors;
            this.patterns = patterns;
            this.materials = materials;
        }

        /**
         * Returns a map from category to the list of garment names.
         * Retained for callers (and tests) that only need names; the
         * per-item descriptors are available via {@link #getOptionsByCategory()}.
         */
        public Map<String, List<String>> getItemsByCategory() {
            Map<String, List<String>> names = new HashMap<>();
            for (Map.Entry<String, List<ClothingOption>> entry
                    : optionsByCategory.entrySet()) {
                List<String> categoryNames = new ArrayList<>();
                for (ClothingOption option : entry.getValue()) {
                    categoryNames.add(option.getName());
                }
                names.put(entry.getKey(), categoryNames);
            }
            return Collections.unmodifiableMap(names);
        }

        public Map<String, List<ClothingOption>> getOptionsByCategory() {
            return Collections.unmodifiableMap(optionsByCategory);
        }

        public List<String> getColors() {
            return Collections.unmodifiableList(colors);
        }

        public List<String> getPatterns() {
            return Collections.unmodifiableList(patterns);
        }

        public List<String> getMaterials() {
            return Collections.unmodifiableList(materials);
        }

        /**
         * Returns true if any inventory category has at least one item.
         */
        public boolean hasAnyItems() {
            for (List<ClothingOption> items : optionsByCategory.values()) {
                if (!items.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    // ---- Public API ----

    /**
     * Returns the clothing item options for a clique/gender/category.
     *
     * @param clique   the clique name
     * @param gender   "Female" or "Male" (case-insensitive)
     * @param category the inventory category (e.g. "tops", "bottoms")
     * @return list of garment name strings, or empty list when missing
     */
    public static List<String> getItems(String clique, String gender,
                                        String category) {
        List<ClothingOption> options = getOptions(clique, gender, category);
        if (options.isEmpty()) {
            return List.of();
        }
        List<String> names = new ArrayList<>(options.size());
        for (ClothingOption option : options) {
            names.add(option.getName());
        }
        return names;
    }

    /**
     * Returns the garment options (name plus per-item brand/material/
     * pattern descriptors) for a clique/gender/category.
     *
     * @param clique   the clique name
     * @param gender   "Female" or "Male" (case-insensitive)
     * @param category the inventory category (e.g. "tops", "bottoms")
     * @return list of garment options, or empty list when missing
     */
    public static List<ClothingOption> getOptions(String clique, String gender,
                                                  String category) {
        CliqueClothingData data = resolve(clique, gender);
        if (data == null) {
            return List.of();
        }
        return data.optionsByCategory.getOrDefault(category, List.of());
    }

    /**
     * Returns the color palette for a clique/gender combination.
     */
    public static List<String> getColors(String clique, String gender) {
        CliqueClothingData data = resolve(clique, gender);
        return data == null ? List.of() : data.getColors();
    }

    /**
     * Returns the pattern palette for a clique/gender combination.
     */
    public static List<String> getPatterns(String clique, String gender) {
        CliqueClothingData data = resolve(clique, gender);
        return data == null ? List.of() : data.getPatterns();
    }

    /**
     * Returns the material palette for a clique/gender combination.
     */
    public static List<String> getMaterials(String clique, String gender) {
        CliqueClothingData data = resolve(clique, gender);
        return data == null ? List.of() : data.getMaterials();
    }

    /**
     * Returns the full clothing data for a clique/gender.
     */
    public static CliqueClothingData getData(String clique, String gender) {
        return resolve(clique, gender);
    }

    /**
     * Returns true if the clique has any populated clothing inventory
     * (for at least one gender), meaning clique-specific selection
     * should be used instead of falling back to an empty outfit.
     */
    public static boolean hasClothingData(String clique) {
        ensureLoaded();
        Map<String, CliqueClothingData> genderMap = clothingData.get(clique);
        if (genderMap == null) {
            return false;
        }
        for (CliqueClothingData data : genderMap.values()) {
            if (data.hasAnyItems()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the clique has populated clothing inventory for
     * the specific gender.
     */
    public static boolean hasClothingData(String clique, String gender) {
        CliqueClothingData data = resolve(clique, gender);
        return data != null && data.hasAnyItems();
    }

    // ---- Resolution ----

    /**
     * Resolves the CliqueClothingData for a clique+gender.
     * Returns null if the clique has no data for that gender.
     */
    private static CliqueClothingData resolve(String clique, String gender) {
        ensureLoaded();
        if (clique == null || gender == null) {
            return null;
        }
        Map<String, CliqueClothingData> genderMap = clothingData.get(clique);
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
            loadClothing();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load clique clothing data", e);
        }
    }

    private static void loadClothing() throws IOException, ParseException {
        JSONObject root;
        try (var reader = ResourceAccess.reader(CLOTHING_PATH)) {
            root = (JSONObject) new JSONParser().parse(reader);
        }
        JSONObject yearData = (JSONObject) root.get(YEAR);
        if (yearData == null) {
            return;
        }

        for (Object key : yearData.keySet()) {
            String cliqueName = (String) key;
            JSONObject cliqueObj = (JSONObject) yearData.get(cliqueName);

            if (!isGenderedEntry(cliqueObj)) {
                continue;
            }

            Map<String, CliqueClothingData> genderMap = new HashMap<>();
            if (cliqueObj.containsKey("female")) {
                genderMap.put("female",
                        parseCategoryData((JSONObject) cliqueObj.get("female")));
            }
            if (cliqueObj.containsKey("male")) {
                genderMap.put("male",
                        parseCategoryData((JSONObject) cliqueObj.get("male")));
            }
            clothingData.put(cliqueName, genderMap);
        }
    }

    /**
     * Detects whether a clique JSON object contains gendered sub-objects.
     * Cliques without "female"/"male" keys are incomplete and skipped.
     */
    private static boolean isGenderedEntry(JSONObject cliqueObj) {
        return cliqueObj.containsKey("female") || cliqueObj.containsKey("male");
    }

    private static CliqueClothingData parseCategoryData(JSONObject obj) {
        Map<String, List<ClothingOption>> categories = new HashMap<>();
        for (String categoryKey : CATEGORY_KEYS) {
            categories.put(categoryKey, readOptionList(obj.get(categoryKey)));
        }

        List<String> colors = readStringList(obj.get("colors"));
        List<String> patterns = readStringList(obj.get("patterns"));
        List<String> materials = readStringList(obj.get("materials"));

        return new CliqueClothingData(categories, colors, patterns, materials);
    }

    /**
     * Reads a category array whose entries may be either plain strings
     * (a bare garment name with no descriptors) or JSON objects with a
     * {@code name} plus optional {@code brands}/{@code materials}/
     * {@code patterns} arrays.
     */
    private static List<ClothingOption> readOptionList(Object value) {
        List<ClothingOption> result = new ArrayList<>();
        if (!(value instanceof JSONArray array)) {
            return result;
        }
        for (Object entry : array) {
            ClothingOption option = parseOption(entry);
            if (option != null) {
                result.add(option);
            }
        }
        return result;
    }

    private static ClothingOption parseOption(Object entry) {
        if (entry == null) {
            return null;
        }
        if (entry instanceof JSONObject obj) {
            Object nameValue = obj.get("name");
            if (nameValue == null) {
                return null;
            }
            return new ClothingOption(nameValue.toString(),
                    readStringList(obj.get("brands")),
                    readStringList(obj.get("materials")),
                    readStringList(obj.get("patterns")));
        }
        return new ClothingOption(entry.toString(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
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
