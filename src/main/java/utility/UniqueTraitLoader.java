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
 * Loads and caches unique physical/behavioral trait descriptions from
 * unique_traits.json. Traits are organized by sentiment category
 * (positive, neutral, negative) and body-part subcategory
 * (eyes, nose, ears, mouth, face, hair, skin, body, misc).
 */
public final class UniqueTraitLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TRAITS_PATH =
            "src/main/java/Resources/unique_traits.json";

    private static final String[] CATEGORIES = {"positive", "neutral", "negative"};
    private static final String[] SUBCATEGORIES = {
            "eyes", "nose", "ears", "mouth", "face", "hair", "skin", "body", "misc"
    };

    private static boolean loaded = false;

    // category -> subcategory -> list of trait strings
    private static final Map<String, Map<String, List<String>>> traitData =
            new HashMap<>();

    private UniqueTraitLoader() {
    }

    /**
     * Returns the available subcategory keys.
     */
    public static String[] getSubcategories() {
        return SUBCATEGORIES.clone();
    }

    /**
     * Returns the trait strings for a given category and subcategory.
     *
     * @param category    "positive", "neutral", or "negative"
     * @param subcategory body-part key (e.g. "eyes", "nose")
     * @return unmodifiable list of trait strings, or empty list if none found
     */
    public static List<String> getTraits(String category, String subcategory) {
        ensureLoaded();
        Map<String, List<String>> subMap = traitData.get(category);
        if (subMap == null) {
            return List.of();
        }
        List<String> traits = subMap.get(subcategory);
        return traits == null ? List.of() : Collections.unmodifiableList(traits);
    }

    // ---- Loading ----

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadTraits();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load unique trait data", e);
        }
    }

    private static void loadTraits() throws IOException, ParseException {
        JSONObject root = (JSONObject) new JSONParser().parse(
                new FileReader(TRAITS_PATH, StandardCharsets.UTF_8));

        for (String category : CATEGORIES) {
            JSONObject categoryObj = (JSONObject) root.get(category);
            if (categoryObj == null) {
                continue;
            }
            Map<String, List<String>> subMap = new HashMap<>();
            for (String subcategory : SUBCATEGORIES) {
                subMap.put(subcategory, readStringList(categoryObj.get(subcategory)));
            }
            traitData.put(category, subMap);
        }
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
