package entity.Radio;

/**
 * Canonical, gameplay-facing music genre taxonomy.
 *
 * <p>External datasets (MusicOSet / Spotify) expose ~1,500 fine-grained
 * folksonomy tags such as {@code "dance pop"} or {@code "post-grunge"}.
 * Those raw labels are collapsed into this small fixed set <em>offline</em>
 * (see {@code utility.music.GenreCollapser}) so the rest of the game never
 * has to reason about raw tags. {@link #OTHER} is the safe fallback for
 * anything that does not map and for unknown songs.</p>
 */
public enum MusicGenre {
    POP,
    ROCK,
    HIP_HOP,
    RNB,
    COUNTRY,
    ELECTRONIC,
    PUNK,
    EMO,
    METAL,
    JAZZ,
    LATIN,
    FOLK,
    CLASSICAL,
    SOUNDTRACK,
    OTHER;

    /**
     * Parse a canonical genre name (case-insensitive, trimmed) as written in
     * the trimmed asset files. Unknown or blank input resolves to
     * {@link #OTHER} rather than throwing, so a malformed asset row can never
     * abort a chart load.
     *
     * @param name canonical name such as {@code "HIP_HOP"} or {@code "pop"}
     * @return the matching genre, or {@link #OTHER} when unrecognized
     */
    public static MusicGenre fromName(String name) {
        if (name == null) {
            return OTHER;
        }
        String key = name.trim().toUpperCase();
        if (key.isEmpty()) {
            return OTHER;
        }
        try {
            return MusicGenre.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
