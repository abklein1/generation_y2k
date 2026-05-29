package utility.music;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Shared normalization rules for matching Billboard performer strings to
 * external artist records.
 *
 * <p>This class is the single source of truth used by <em>both</em> the
 * offline asset generator ({@code GenreAssetGenerator}) and the runtime
 * matcher ({@code MusicGenreLoader}). If the two sides normalized
 * differently, generated keys would silently fail to match at runtime, so
 * all key derivation must go through here.</p>
 *
 * <p>Two operations are provided:</p>
 * <ul>
 *   <li>{@link #normalize(String)} - collapse a single name (or a whole
 *       performer string) to a stable match key.</li>
 *   <li>{@link #splitCollaborators(String)} - break a collaboration credit
 *       into individual artist names. This is only used as a <em>fallback</em>
 *       after a whole-performer match fails, so that band names containing
 *       {@code &}, {@code and}, or commas (e.g. "Earth, Wind & Fire") are
 *       matched intact first.</li>
 * </ul>
 */
public final class PerformerNormalizer {

    /**
     * Collaboration / credit separators, longest first so multi-word markers
     * win over their single-word substrings. Matched case-insensitively with
     * surrounding whitespace flexibility.
     */
    private static final Pattern COLLAB_SPLIT = Pattern.compile(
            "(?i)\\s*(?:"
                    + "\\bduet with\\b|"
                    + "\\bfeaturing\\b|\\bfeat\\.?|\\bft\\.?|"
                    + "\\bwith\\b|"
                    + "\\bintroducing\\b|\\bpresents\\b|"
                    + "\\bversus\\b|\\bvs\\.?|"
                    + "\\band\\b|&|,|\\+|/"
                    + ")\\s*");

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9 ]");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");
    private static final Pattern DIACRITICS =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private PerformerNormalizer() {
    }

    /**
     * Reduce a name to a stable lowercase match key: strip diacritics,
     * normalize {@code &} to {@code and}, drop punctuation, collapse
     * whitespace, and drop a leading {@code "the "}.
     *
     * @param raw any artist name, band name, or whole performer credit
     * @return a normalized key, or an empty string for null/blank input
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String s = Normalizer.normalize(raw, Normalizer.Form.NFD);
        s = DIACRITICS.matcher(s).replaceAll("");
        s = s.toLowerCase();
        s = s.replace("&", " and ");
        s = NON_ALNUM.matcher(s).replaceAll(" ");
        s = MULTI_SPACE.matcher(s).replaceAll(" ").trim();
        if (s.startsWith("the ")) {
            s = s.substring(4);
        }
        return s;
    }

    /**
     * Build the song-level match key from a Billboard title + performer pair.
     * Both the generator (reading MusicOSet's {@code billboard} tuple) and the
     * runtime loader (reading a chart row) must produce this identically.
     *
     * @param title     song title
     * @param performer full performer credit
     * @return {@code normalize(title) + "|" + normalize(performer)}
     */
    public static String songKey(String title, String performer) {
        return normalize(title) + "|" + normalize(performer);
    }

    /**
     * Split a collaboration credit into individual artist names. Empty and
     * blank fragments are dropped. The returned strings are <em>not</em>
     * normalized; callers should pass each through {@link #normalize(String)}.
     *
     * @param performer full performer credit (e.g. "Jay-Z Featuring Alicia
     *                  Keys")
     * @return individual artist fragments in original order
     */
    public static List<String> splitCollaborators(String performer) {
        List<String> out = new ArrayList<>();
        if (performer == null || performer.isBlank()) {
            return out;
        }
        for (String part : COLLAB_SPLIT.split(performer)) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }
}
