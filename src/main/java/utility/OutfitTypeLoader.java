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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and caches outfit recipe definitions from outfit_types.json.
 * Each recipe lists the required and optional layer keys plus an
 * optional {@code maxLayers} map controlling how many items may stack
 * inside a given layer.
 *
 * <p>The layer keys here match the inventory category keys used by
 * {@link CliqueClothingLoader} so generation can pull from the
 * corresponding clique inventories without translation.</p>
 *
 * <p>Mirrors the conventions used by the other clique loaders: file
 * loaded synchronously on first access, year hardcoded to
 * {@code "2004"}, and accessors return unmodifiable views.</p>
 */
public final class OutfitTypeLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String OUTFIT_TYPES_PATH =
            "src/main/java/Resources/Cliques/outfit_types.json";
    private static final String YEAR = "2004";

    private static boolean loaded = false;

    // outfit type name -> definition
    private static final Map<String, OutfitTypeData> outfitTypes =
            new LinkedHashMap<>();

    private OutfitTypeLoader() {
    }

    /**
     * Holds a single outfit recipe.
     */
    public static class OutfitTypeData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final String description;
        private final List<String> requiredLayers;
        private final List<String> optionalLayers;
        private final Map<String, Integer> maxLayers;

        OutfitTypeData(String name, String description,
                       List<String> requiredLayers,
                       List<String> optionalLayers,
                       Map<String, Integer> maxLayers) {
            this.name = name;
            this.description = description;
            this.requiredLayers = requiredLayers;
            this.optionalLayers = optionalLayers;
            this.maxLayers = maxLayers;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getRequiredLayers() {
            return Collections.unmodifiableList(requiredLayers);
        }

        public List<String> getOptionalLayers() {
            return Collections.unmodifiableList(optionalLayers);
        }

        public Map<String, Integer> getMaxLayers() {
            return Collections.unmodifiableMap(maxLayers);
        }

        /**
         * Returns the maximum number of items allowed in the given
         * layer, defaulting to 1 when not specified.
         */
        public int getMaxForLayer(String layer) {
            Integer max = maxLayers.get(layer);
            return max == null ? 1 : max;
        }

        /**
         * Returns true if this recipe contains the given layer key in
         * either the required or optional list.
         */
        public boolean hasLayer(String layer) {
            return requiredLayers.contains(layer)
                    || optionalLayers.contains(layer);
        }
    }

    // ---- Public API ----

    /**
     * Returns the recipe with the given name, or {@code null} when no
     * such recipe exists.
     */
    public static OutfitTypeData getOutfitType(String name) {
        ensureLoaded();
        return outfitTypes.get(name);
    }

    /**
     * Returns the names of every loaded outfit recipe in declaration
     * order.
     */
    public static List<String> getOutfitTypeNames() {
        ensureLoaded();
        return List.copyOf(outfitTypes.keySet());
    }

    /**
     * Returns every loaded recipe in declaration order.
     */
    public static List<OutfitTypeData> getAllOutfitTypes() {
        ensureLoaded();
        return List.copyOf(outfitTypes.values());
    }

    /**
     * Returns true when at least one outfit recipe has been loaded.
     */
    public static boolean hasOutfitTypes() {
        ensureLoaded();
        return !outfitTypes.isEmpty();
    }

    // ---- Loading ----

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadOutfitTypes();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load outfit type data", e);
        }
    }

    private static void loadOutfitTypes() throws IOException, ParseException {
        JSONObject root = (JSONObject) new JSONParser().parse(
                new FileReader(OUTFIT_TYPES_PATH, StandardCharsets.UTF_8));
        JSONObject yearData = (JSONObject) root.get(YEAR);
        if (yearData == null) {
            return;
        }

        for (Object key : yearData.keySet()) {
            String outfitName = (String) key;
            Object value = yearData.get(outfitName);
            if (!(value instanceof JSONObject obj)) {
                continue;
            }
            outfitTypes.put(outfitName, parseOutfitType(outfitName, obj));
        }
    }

    private static OutfitTypeData parseOutfitType(String name, JSONObject obj) {
        String description = obj.get("description") == null
                ? "" : obj.get("description").toString();
        List<String> required = readStringList(obj.get("required"));
        List<String> optional = readStringList(obj.get("optional"));
        Map<String, Integer> maxLayers = readIntMap(obj.get("maxLayers"));
        return new OutfitTypeData(name, description, required, optional,
                maxLayers);
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

    private static Map<String, Integer> readIntMap(Object value) {
        Map<String, Integer> result = new HashMap<>();
        if (value instanceof JSONObject obj) {
            for (Object key : obj.keySet()) {
                Object raw = obj.get(key);
                if (raw == null) {
                    continue;
                }
                try {
                    result.put(key.toString(),
                            Integer.parseInt(raw.toString()));
                } catch (NumberFormatException ignored) {
                    // Skip entries that aren't valid integers.
                }
            }
        }
        return result;
    }
}
