package utility.music;

import entity.Radio.MusicGenre;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utility.io.ResourceAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Runtime resolver from a Billboard {@code (title, performer)} pair to a set of
 * canonical {@link MusicGenre}s, backed by the trimmed assets generated offline
 * by {@code GenreAssetGenerator}.
 *
 * <p>Follows the lazy static-cache pattern of {@code CliqueLoader} /
 * {@code BillboardSongLoader}. The committed assets already contain canonical
 * genre names, so this class performs no genre collapse - only normalization,
 * collaboration splitting, and lookup. Resolution order:</p>
 *
 * <ol>
 *   <li>exact song-level match ({@code song_genres.tsv})</li>
 *   <li>whole-performer artist match ({@code artist_genres.tsv} or overrides)</li>
 *   <li>collaboration split: union of every matched collaborator's genres</li>
 * </ol>
 *
 * <p>Unknown performers/songs yield an empty set; genres are never invented at
 * runtime. All loading is resilient: a missing or malformed asset file leaves
 * the corresponding index empty rather than throwing, so a chart load can never
 * be aborted by genre data.</p>
 */
public final class MusicGenreLoader implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String DIR = "/Resources/Music/";
    private static final String ARTIST_GENRES = DIR + "artist_genres.tsv";
    private static final String SONG_GENRES = DIR + "song_genres.tsv";
    private static final String OVERRIDES =
            DIR + "billboard_artist_genre_overrides.json";

    private static volatile boolean loaded = false;
    private static final Map<String, Set<MusicGenre>> byArtistKey =
            new HashMap<>();
    private static final Map<String, Set<MusicGenre>> bySongKey =
            new HashMap<>();

    private MusicGenreLoader() {
    }

    /**
     * Resolve the canonical genres for a chart entry.
     *
     * @param title     song title as written in the Billboard CSV
     * @param performer performer credit as written in the Billboard CSV
     * @return an immutable, possibly-empty set of canonical genres
     */
    public static Set<MusicGenre> genresFor(String title, String performer) {
        ensureLoaded();

        Set<MusicGenre> song = bySongKey.get(
                PerformerNormalizer.songKey(title, performer));
        if (song != null && !song.isEmpty()) {
            return Collections.unmodifiableSet(song);
        }

        Set<MusicGenre> whole =
                byArtistKey.get(PerformerNormalizer.normalize(performer));
        if (whole != null && !whole.isEmpty()) {
            return Collections.unmodifiableSet(whole);
        }

        EnumSet<MusicGenre> union = EnumSet.noneOf(MusicGenre.class);
        for (String part : PerformerNormalizer.splitCollaborators(performer)) {
            Set<MusicGenre> parts =
                    byArtistKey.get(PerformerNormalizer.normalize(part));
            if (parts != null) {
                union.addAll(parts);
            }
        }
        return Collections.unmodifiableSet(union);
    }

    /** Force assets to be parsed if they have not been already. Idempotent. */
    public static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loadTsv(ARTIST_GENRES, byArtistKey, 1);
        loadTsv(SONG_GENRES, bySongKey, 0);
        loadOverrides();
        loaded = true;
    }

    /**
     * Load a {@code key<TAB>...<TAB>genres} asset. The key is always column 0;
     * {@code genreCol} is the (0-based) column holding the comma-separated
     * canonical genre names. Comment/blank lines are skipped.
     */
    private static void loadTsv(String path,
                                Map<String, Set<MusicGenre>> target,
                                int genreCol) {
        if (!ResourceAccess.exists(path)) {
            return;
        }
        try (BufferedReader br = new BufferedReader(ResourceAccess.reader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                String[] cols = line.split("\t", -1);
                if (cols.length <= genreCol) {
                    continue;
                }
                String key = cols[0];
                Set<MusicGenre> genres = parseGenres(cols[cols.length - 1]);
                if (!key.isEmpty() && !genres.isEmpty()) {
                    target.put(key, genres);
                }
            }
        } catch (IOException ignore) {
            // Resilient: leave index partial/empty rather than failing.
        }
    }

    /**
     * Optional curated overrides for performers MusicOSet misses (pre-1962 /
     * post-2018 tail, spelling variants). JSON object of
     * {@code "Artist Name": ["POP", "ROCK"]}; names are normalized on load so
     * curators can write human-readable names. Overrides win over generated
     * artist genres.
     */
    private static void loadOverrides() {
        if (!ResourceAccess.exists(OVERRIDES)) {
            return;
        }
        try (var fr = ResourceAccess.reader(OVERRIDES)) {
            Object parsed = new JSONParser().parse(fr);
            if (!(parsed instanceof JSONObject root)) {
                return;
            }
            for (Object keyObj : root.keySet()) {
                String name = String.valueOf(keyObj);
                Object value = root.get(keyObj);
                EnumSet<MusicGenre> genres = EnumSet.noneOf(MusicGenre.class);
                if (value instanceof JSONArray arr) {
                    for (Object g : arr) {
                        MusicGenre genre = MusicGenre.fromName(String.valueOf(g));
                        if (genre != MusicGenre.OTHER) {
                            genres.add(genre);
                        }
                    }
                }
                String key = PerformerNormalizer.normalize(name);
                if (!key.isEmpty() && !genres.isEmpty()) {
                    byArtistKey.put(key, genres);
                }
            }
        } catch (Exception ignore) {
            // Overrides are optional; ignore malformed files.
        }
    }

    private static Set<MusicGenre> parseGenres(String csv) {
        EnumSet<MusicGenre> genres = EnumSet.noneOf(MusicGenre.class);
        if (csv == null || csv.isBlank()) {
            return genres;
        }
        for (String tok : csv.split(",")) {
            MusicGenre g = MusicGenre.fromName(tok);
            if (g != MusicGenre.OTHER) {
                genres.add(g);
            }
        }
        return genres;
    }

    /** Reset the cache. Visible for tests. */
    static synchronized void resetForTests() {
        loaded = false;
        byArtistKey.clear();
        bySongKey.clear();
    }
}
