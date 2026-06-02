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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and caches coordinated color schemes from color_schemes.json.
 *
 * <p>A scheme is a named, curated list of colors that read well
 * together (e.g. {@code "dark"} = mostly black). Each clique/gender may
 * map to one or more allowed schemes; outfit generation picks a single
 * scheme per outfit and colors every garment from it, so a person never
 * ends up in a garish, randomly assigned color clash, and cliques like
 * Emo and Goth stay almost entirely in black.</p>
 *
 * <p>Mirrors the conventions used by {@link OutfitTypeLoader} and the
 * other clique loaders: synchronous file load, year hardcoded to
 * {@code "2004"}, and accessors return unmodifiable views.</p>
 */
public final class ColorSchemeLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SCHEMES_PATH =
            "/Resources/Cliques/color_schemes.json";
    private static final String YEAR = "2004";

    private static boolean loaded = false;

    // scheme name -> ordered color list
    private static final Map<String, List<String>> schemes =
            new LinkedHashMap<>();
    // clique name -> allowed scheme names
    private static final Map<String, List<String>> cliqueSchemes =
            new LinkedHashMap<>();
    // fallback scheme names when a clique has no explicit mapping
    private static final List<String> defaultSchemes = new ArrayList<>();

    private ColorSchemeLoader() {
    }

    // ---- Public API ----

    /**
     * Returns the ordered color list for the named scheme, or an empty
     * list when no such scheme exists.
     */
    public static List<String> getSchemeColors(String schemeName) {
        ensureLoaded();
        if (schemeName == null) {
            return List.of();
        }
        return Collections.unmodifiableList(
                schemes.getOrDefault(schemeName, List.of()));
    }

    /**
     * Returns the allowed scheme names for a clique, falling back to the
     * default scheme list when the clique has no explicit mapping.
     */
    public static List<String> getSchemesForClique(String clique) {
        ensureLoaded();
        List<String> mapped = clique == null ? null : cliqueSchemes.get(clique);
        if (mapped == null || mapped.isEmpty()) {
            return Collections.unmodifiableList(defaultSchemes);
        }
        return Collections.unmodifiableList(mapped);
    }

    /**
     * Returns the configured default scheme names.
     */
    public static List<String> getDefaultSchemes() {
        ensureLoaded();
        return Collections.unmodifiableList(defaultSchemes);
    }

    /**
     * Returns true when at least one color scheme has been loaded.
     */
    public static boolean hasSchemes() {
        ensureLoaded();
        return !schemes.isEmpty();
    }

    // ---- Loading ----

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadSchemes();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to load color scheme data", e);
        }
    }

    private static void loadSchemes() throws IOException, ParseException {
        JSONObject root;
        try (var reader = ResourceAccess.reader(SCHEMES_PATH)) {
            root = (JSONObject) new JSONParser().parse(reader);
        }
        JSONObject yearData = (JSONObject) root.get(YEAR);
        if (yearData == null) {
            return;
        }

        Object schemesObj = yearData.get("schemes");
        if (schemesObj instanceof JSONObject obj) {
            for (Object key : obj.keySet()) {
                schemes.put(key.toString(), readStringList(obj.get(key)));
            }
        }

        Object cliqueObj = yearData.get("cliqueSchemes");
        if (cliqueObj instanceof JSONObject obj) {
            for (Object key : obj.keySet()) {
                cliqueSchemes.put(key.toString(), readStringList(obj.get(key)));
            }
        }

        defaultSchemes.addAll(readStringList(yearData.get("default")));
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
