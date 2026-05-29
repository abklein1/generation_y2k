package utility.music;

import entity.Radio.MusicGenre;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Collapses fine-grained external genre tags (MusicOSet / Spotify folksonomy,
 * e.g. {@code "dance pop"}, {@code "post-grunge"}, {@code "chicago drill"})
 * into the canonical {@link MusicGenre} set.
 *
 * <p>This runs <em>offline</em> inside the asset generator; the trimmed assets
 * committed to the repo already contain canonical names, so this class is not
 * on the runtime hot path. The rules are intentionally ordered: more specific,
 * genre-defining markers are tested before generic ones (e.g. {@code "pop
 * punk"} resolves to {@link MusicGenre#PUNK}, and {@code "pop rap"} to
 * {@link MusicGenre#HIP_HOP}, before the bare {@code "pop"} rule can fire).</p>
 */
public final class GenreCollapser {

    /** A single ordered rule: any keyword match maps to {@code genre}. */
    private static final class Rule {
        final MusicGenre genre;
        final String[] keywords;

        Rule(MusicGenre genre, String... keywords) {
            this.genre = genre;
            this.keywords = keywords;
        }
    }

    /**
     * Ordered collapse rules. The first rule with a keyword contained in the
     * (lowercased) raw tag wins, so order encodes priority.
     */
    private static final Rule[] RULES = {
            new Rule(MusicGenre.EMO, "emo", "screamo"),
            new Rule(MusicGenre.METAL, "metal", "metalcore", "deathcore",
                    "grindcore", "djent", "thrash", "mathcore", "doom"),
            new Rule(MusicGenre.PUNK, "punk", "ska", "hardcore"),
            new Rule(MusicGenre.HIP_HOP, "hip hop", "hip-hop", "rap", "trap",
                    "drill", "grime", "crunk", "hyphy", "bounce", "turntablism"),
            new Rule(MusicGenre.RNB, "r&b", "rnb", "soul", "funk", "motown",
                    "new jack", "quiet storm", "doo-wop", "doo wop",
                    "urban contemporary", "blues", "rhythm and blues"),
            new Rule(MusicGenre.COUNTRY, "country", "bluegrass", "honky",
                    "nashville", "cowboy", "western swing", "outlaw", "redneck"),
            new Rule(MusicGenre.LATIN, "latin", "reggaeton", "salsa", "bachata",
                    "cumbia", "mariachi", "banda", "regional mexican", "ranchera",
                    "tejano", "norteno", "merengue", "flamenco", "tango", "samba",
                    "spanish", "mexican", "brazilian", "bossa"),
            new Rule(MusicGenre.JAZZ, "jazz", "swing", "bebop", "big band",
                    "ragtime", "dixieland"),
            new Rule(MusicGenre.CLASSICAL, "classical", "baroque", "orchestra",
                    "opera", "symphony", "chamber", "choral", "compositional",
                    "renaissance", "early music", "gregorian"),
            new Rule(MusicGenre.SOUNDTRACK, "soundtrack", "score", "broadway",
                    "show tune", "musical", "hollywood", "disney", "video game",
                    "anime", "movie tunes"),
            new Rule(MusicGenre.POP, "pop", "boy band", "girl group", "idol",
                    "mellow gold", "neo mellow", "adult standards",
                    "adult contemporary", "easy listening", "lounge", "lilith",
                    // Soft/yacht/AOR-adjacent "rock" is pop-leaning adult
                    // music, not the hard/classic rock a ROCK station wants;
                    // route it to POP before the bare "rock" rule below.
                    "soft rock", "yacht rock", "mellow"),
            new Rule(MusicGenre.ELECTRONIC, "house", "techno", "edm", "electro",
                    "trance", "dubstep", "drum and bass", "dnb", "ambient",
                    "idm", "big room", "future bass", "electronica", "synth",
                    "downtempo", "breakbeat", "trip hop", "dance", "disco",
                    "eurodance", "hardstyle", "big beat", "glitch", "vaporwave",
                    "chillwave", "rave"),
            new Rule(MusicGenre.FOLK, "folk", "singer-songwriter", "americana",
                    "acoustic"),
            new Rule(MusicGenre.ROCK, "rock", "grunge", "new wave", "psychedelic",
                    "indie", "alternative", "surf", "shoegaze", "britpop",
                    "mod ", "jangle", "garage", "british invasion", "merseybeat",
                    "freakbeat", "permanent wave", "new romantic", "jam band",
                    "canterbury scene", "gaze"),
    };

    private GenreCollapser() {
    }

    /**
     * Collapse a single raw tag to a canonical genre.
     *
     * @param rawTag external genre label
     * @return the matching canonical genre, or {@link MusicGenre#OTHER}
     */
    public static MusicGenre collapse(String rawTag) {
        if (rawTag == null) {
            return MusicGenre.OTHER;
        }
        String tag = rawTag.toLowerCase(Locale.ROOT).trim();
        if (tag.isEmpty()) {
            return MusicGenre.OTHER;
        }
        for (Rule rule : RULES) {
            for (String keyword : rule.keywords) {
                if (tag.contains(keyword)) {
                    return rule.genre;
                }
            }
        }
        return MusicGenre.OTHER;
    }

    /**
     * Collapse a list of raw tags to the set of canonical genres they imply.
     * If every tag falls through to {@link MusicGenre#OTHER}, the result is a
     * single-element {@code {OTHER}} set; callers may treat that as "unknown".
     *
     * @param rawTags external genre labels (may be empty)
     * @return non-null set of canonical genres
     */
    public static Set<MusicGenre> collapseAll(Collection<String> rawTags) {
        Set<MusicGenre> genres = EnumSet.noneOf(MusicGenre.class);
        if (rawTags != null) {
            for (String tag : rawTags) {
                MusicGenre g = collapse(tag);
                if (g != MusicGenre.OTHER) {
                    genres.add(g);
                }
            }
        }
        if (genres.isEmpty()) {
            genres.add(MusicGenre.OTHER);
        }
        return genres;
    }
}
