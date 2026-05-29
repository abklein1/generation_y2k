package utility.traits;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utility.io.ResourceAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic loader for any 2-level trait JSON file of the shape
 * {@code category -> subcategory -> [trait strings]}.
 *
 * <p>Unlike the older trait loader this class replaces, no category or
 * subcategory keys are hard-coded; the structure is discovered at parse
 * time. Loaded datasets are cached by canonical file path so repeated
 * calls with the same path are essentially free.</p>
 */
public final class TraitDatasetLoader {

    private static final Map<String, TraitDataset> CACHE = new ConcurrentHashMap<>();

    private TraitDatasetLoader() {
    }

    /**
     * Loads (or returns the cached) {@link TraitDataset} for the JSON
     * file at the given classpath path.
     *
     * @param path classpath path to a trait JSON file
     * @return the parsed dataset (never null)
     * @throws RuntimeException if the file cannot be read or parsed
     */
    public static TraitDataset load(String path) {
        TraitDataset cached = CACHE.get(path);
        if (cached != null) {
            return cached;
        }
        TraitDataset dataset = parse(path);
        CACHE.put(path, dataset);
        return dataset;
    }

    /**
     * Clears the in-memory cache.  Intended for tests.
     */
    public static void resetCache() {
        CACHE.clear();
    }

    private static TraitDataset parse(String path) {
        try {
            JSONObject root;
            try (var reader = ResourceAccess.reader(toResourcePath(path))) {
                root = (JSONObject) new JSONParser().parse(reader);
            }

            List<String> categories = new ArrayList<>();
            // LinkedHashSet preserves first-seen order while deduping
            // across categories.
            LinkedHashSet<String> subcategoryUnion = new LinkedHashSet<>();
            Map<String, Map<String, List<String>>> data = new LinkedHashMap<>();

            for (Object catKey : root.keySet()) {
                String category = catKey.toString();
                Object catVal = root.get(catKey);
                if (!(catVal instanceof JSONObject categoryObj)) {
                    continue;
                }
                categories.add(category);
                Map<String, List<String>> subMap = new LinkedHashMap<>();
                for (Object subKey : categoryObj.keySet()) {
                    String subcategory = subKey.toString();
                    subcategoryUnion.add(subcategory);
                    subMap.put(subcategory,
                            readStringList(categoryObj.get(subKey)));
                }
                data.put(category, subMap);
            }

            return new TraitDataset(
                    categories,
                    new ArrayList<>(subcategoryUnion),
                    data);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(
                    "Failed to load trait dataset: " + path, e);
        }
    }

    private static String toResourcePath(String path) {
        String normalized = path.replace('\\', '/');
        if (normalized.startsWith("src/main/java/")) {
            normalized = normalized.substring("src/main/java".length());
        } else if (normalized.startsWith("src/main/resources/")) {
            normalized = normalized.substring("src/main/resources".length());
        } else if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
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
