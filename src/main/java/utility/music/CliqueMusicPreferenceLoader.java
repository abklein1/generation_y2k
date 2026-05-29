package utility.music;

import entity.Radio.MusicGenre;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches per-clique {@link MusicPreference} data from
 * {@code Resources/Cliques/clique_music_preferences.json}, following the lazy
 * static-cache pattern of {@code CliqueLoader}.
 *
 * <p>The JSON is keyed by year, then by clique name. Each clique entry has an
 * {@code openness} number and a {@code genres} object mapping canonical
 * {@link MusicGenre} names to signed weights. Keys beginning with {@code _}
 * are ignored, except {@code _default}, which becomes the fallback returned by
 * {@link #getPreference(String)} for any clique not present in the file.</p>
 */
public final class CliqueMusicPreferenceLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PATH =
            "src/main/java/Resources/Cliques/clique_music_preferences.json";
    private static final String YEAR = "2004";

    private static boolean loaded = false;
    private static final Map<String, MusicPreference> byClique = new HashMap<>();
    private static MusicPreference defaultPreference =
            new MusicPreference(new EnumMap<>(MusicGenre.class), 0.5);

    private CliqueMusicPreferenceLoader() {
    }

    /**
     * @param clique clique name (e.g. {@code "Emo"})
     * @return the clique's parsed preference, or the {@code _default}
     *         preference when the clique is absent
     */
    public static MusicPreference getPreference(String clique) {
        ensureLoaded();
        if (clique == null) {
            return defaultPreference;
        }
        return byClique.getOrDefault(clique, defaultPreference);
    }

    /** @return the fallback preference parsed from {@code _default}. */
    public static MusicPreference getDefaultPreference() {
        ensureLoaded();
        return defaultPreference;
    }

    /** @return true if an explicit entry exists for the clique. */
    public static boolean hasPreference(String clique) {
        ensureLoaded();
        return clique != null && byClique.containsKey(clique);
    }

    static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            load();
            loaded = true;
        } catch (IOException | ParseException e) {
            throw new RuntimeException(
                    "Failed to load clique music preferences", e);
        }
    }

    private static void load() throws IOException, ParseException {
        try (FileReader reader = new FileReader(PATH, StandardCharsets.UTF_8)) {
            JSONObject root = (JSONObject) new JSONParser().parse(reader);
            JSONObject yearData = (JSONObject) root.get(YEAR);
            if (yearData == null) {
                return;
            }
            for (Object keyObj : yearData.keySet()) {
                String name = String.valueOf(keyObj);
                Object value = yearData.get(keyObj);
                if (!(value instanceof JSONObject entry)) {
                    continue; // skip "_comment" strings and the like
                }
                MusicPreference pref = parsePreference(entry);
                if ("_default".equals(name)) {
                    defaultPreference = pref;
                } else if (!name.startsWith("_")) {
                    byClique.put(name, pref);
                }
            }
        }
    }

    private static MusicPreference parsePreference(JSONObject entry) {
        double openness = toDouble(entry.get("openness"), 0.5);
        Map<MusicGenre, Double> weights = new EnumMap<>(MusicGenre.class);
        Object genresObj = entry.get("genres");
        if (genresObj instanceof JSONObject genres) {
            for (Object gKey : genres.keySet()) {
                MusicGenre genre = MusicGenre.fromName(String.valueOf(gKey));
                if (genre == MusicGenre.OTHER
                        && !"OTHER".equalsIgnoreCase(String.valueOf(gKey))) {
                    continue; // unknown genre label; skip rather than mislabel
                }
                weights.put(genre, toDouble(genres.get(gKey), 0.0));
            }
        }
        return new MusicPreference(weights, openness);
    }

    private static double toDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    /** Reset the cache. Visible for tests. */
    static synchronized void resetForTests() {
        loaded = false;
        byClique.clear();
        defaultPreference = new MusicPreference(
                new EnumMap<>(MusicGenre.class), 0.5);
    }
}
