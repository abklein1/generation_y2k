package utility.music;

import entity.Radio.MusicGenre;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utility.io.ResourceAccess;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and caches the curated {@code genre -> bands} supplement from
 * {@code Resources/Music/curated_genre_bands.json}, following the lazy
 * static-cache pattern of {@link CliqueMusicPreferenceLoader} /
 * {@link MusicGenreLoader}.
 *
 * <p>These are scene-defining acts that the Billboard Hot 100 under-represents:
 * either they never charted as singles, or their breakout post-dates the game's
 * 2004 cutoff (the 2004 emo wave is the canonical example). {@link
 * BandsByGenreProvider} folds them into the per-genre favorite-band rosters so
 * a clique that loves a genre still surfaces its defining bands.</p>
 *
 * <p>The JSON is a flat object mapping canonical {@link MusicGenre} names to an
 * array of band display names. Keys beginning with {@code _} (e.g. comments)
 * and unrecognized genre names are ignored. A missing or malformed file yields
 * an empty map rather than throwing, so the favorite-band system degrades to
 * pure Billboard data instead of failing.</p>
 */
public final class CuratedGenreBandsLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PATH =
            "/Resources/Music/curated_genre_bands.json";

    private static volatile boolean loaded = false;
    private static final Map<MusicGenre, List<String>> byGenre =
            new EnumMap<>(MusicGenre.class);

    private CuratedGenreBandsLoader() {
    }

    /**
     * @return an immutable map from genre to its curated band names; empty
     *         when no curated file is present. The returned lists are
     *         immutable views.
     */
    public static Map<MusicGenre, List<String>> getBandsByGenre() {
        ensureLoaded();
        return Collections.unmodifiableMap(byGenre);
    }

    /** Force the curated file to be parsed if it has not been already. */
    public static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            load();
        } catch (Exception ignore) {
            // Curated bands are optional; degrade to Billboard-only rosters.
            byGenre.clear();
        }
        loaded = true;
    }

    private static void load() throws IOException, ParseException {
        if (!ResourceAccess.exists(PATH)) {
            return;
        }
        try (var reader = ResourceAccess.reader(PATH)) {
            Object parsed = new JSONParser().parse(reader);
            if (!(parsed instanceof JSONObject root)) {
                return;
            }
            for (Object keyObj : root.keySet()) {
                String name = String.valueOf(keyObj);
                if (name.startsWith("_")) {
                    continue; // comments and metadata
                }
                MusicGenre genre = MusicGenre.fromName(name);
                if (genre == MusicGenre.OTHER
                        && !"OTHER".equalsIgnoreCase(name)) {
                    continue; // unknown genre label; skip rather than mislabel
                }
                if (!(root.get(keyObj) instanceof JSONArray arr)) {
                    continue;
                }
                List<String> bands = new ArrayList<>();
                for (Object b : arr) {
                    String band = String.valueOf(b).trim();
                    if (!band.isEmpty() && !bands.contains(band)) {
                        bands.add(band);
                    }
                }
                if (!bands.isEmpty()) {
                    byGenre.put(genre, bands);
                }
            }
        }
    }

    /** Reset the cache. Visible for tests. */
    static synchronized void resetForTests() {
        loaded = false;
        byGenre.clear();
    }
}
