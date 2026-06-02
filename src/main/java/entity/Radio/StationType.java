package entity.Radio;

import constants.SimConstants;
import utility.BillboardSongLoader;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Defines a kind of radio station the generator can spin up: a display
 * {@code label}, the underlying {@link StationFormat} selection strategy, and
 * an optional {@link MusicGenre} target.
 *
 * <p>Broad stations (Top 40, Oldies, Mix, ...) have a {@code null} target
 * genre and behave exactly like their format. Genre stations pair
 * {@link StationFormat#GENRE_ROTATION} with a target genre so song selection
 * is filtered to that genre - this keeps a single rotation format instead of
 * one enum constant per genre.</p>
 *
 * <p>The {@link #catalog()} is the data structure the
 * {@code RadioStationGenerator} draws from when choosing a handful of stations
 * for a game.</p>
 */
public final class StationType implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String label;
    private final StationFormat format;
    private final Set<MusicGenre> targetGenres;
    private final int startYear;
    private final int endYear;

    public StationType(String label, StationFormat format,
                       MusicGenre targetGenre) {
        this(label, format, targetGenre, 0, 0);
    }

    public StationType(String label, StationFormat format,
                       MusicGenre targetGenre, int startYear, int endYear) {
        this(label, format, toGenreSet(targetGenre), startYear, endYear);
    }

    /**
     * Full constructor allowing a station to target several genres at once
     * (a song matches if it carries <em>any</em> of them). Single-genre and
     * broad stations are special cases with a one-element or empty set.
     */
    public StationType(String label, StationFormat format,
                       Set<MusicGenre> targetGenres, int startYear,
                       int endYear) {
        this.label = label;
        this.format = format;
        this.targetGenres = (targetGenres == null || targetGenres.isEmpty())
                ? EnumSet.noneOf(MusicGenre.class)
                : EnumSet.copyOf(targetGenres);
        this.startYear = startYear;
        this.endYear = endYear;
    }

    private static Set<MusicGenre> toGenreSet(MusicGenre genre) {
        return genre == null
                ? EnumSet.noneOf(MusicGenre.class)
                : EnumSet.of(genre);
    }

    /** A broad (non-genre) station backed directly by a format. */
    public static StationType broad(String label, StationFormat format) {
        return new StationType(label, format, (MusicGenre) null);
    }

    /** A genre-targeted station over the shared rotation pool. */
    public static StationType genre(String label, MusicGenre genre) {
        return new StationType(label, StationFormat.GENRE_ROTATION, genre);
    }

    /**
     * A station over the shared rotation pool that targets several genres at
     * once - a song qualifies if it carries any of them. Used for blended
     * formats such as a modern-alternative station spanning alternative rock,
     * pop punk, and emo.
     *
     * @param label  display label (e.g. "Alternative")
     * @param genres the genres this station accepts (any-match)
     */
    public static StationType multiGenre(String label, MusicGenre... genres) {
        EnumSet<MusicGenre> set = EnumSet.noneOf(MusicGenre.class);
        for (MusicGenre g : genres) {
            if (g != null) {
                set.add(g);
            }
        }
        return new StationType(label, StationFormat.GENRE_ROTATION, set, 0, 0);
    }

    /**
     * A decade station: a year-bounded throwback mix (no genre filter).
     *
     * @param label     display label (e.g. "All 80s")
     * @param startYear first year of the decade window (inclusive)
     * @param endYear   last year of the decade window (inclusive)
     */
    public static StationType decade(String label, int startYear, int endYear) {
        return new StationType(label, StationFormat.DECADE, (MusicGenre) null,
                startYear, endYear);
    }

    /**
     * A decade station filtered to a (broadly era-appropriate) genre, e.g.
     * "80s Rock" or "90s Pop". Use only genres that existed across the chosen
     * decades; avoid era-specific tags like EMO/ELECTRONIC for older windows.
     */
    public static StationType decade(String label, int startYear, int endYear,
                                     MusicGenre genre) {
        return new StationType(label, StationFormat.DECADE, genre,
                startYear, endYear);
    }

    /** Convenience for the always-present oldies station. */
    public static StationType oldies() {
        return broad("Oldies", StationFormat.OLDIES_RANDOM);
    }

    /** Convenience for the always-present top-40 station. */
    public static StationType top40() {
        return broad("Top 40", StationFormat.TOP_40);
    }

    /** Convenience for the always-present Christian station. */
    public static StationType christian() {
        return genre("Christian", MusicGenre.CHRISTIAN);
    }

    /**
     * The full set of station types a game can choose from: broad formats
     * plus one station per general genre.
     *
     * @return a fresh, mutable list of all available station types
     */
    public static List<StationType> catalog() {
        List<StationType> all = new ArrayList<>();
        all.add(broad("Top 40", StationFormat.TOP_40));
        all.add(broad("Adult Contemporary", StationFormat.ADULT_CONTEMPORARY));
        all.add(broad("Throwback", StationFormat.THROWBACK));
        all.add(broad("Oldies", StationFormat.OLDIES_RANDOM));
        all.add(broad("Mix", StationFormat.MIX));
        all.add(genre("Pop", MusicGenre.POP));
        all.add(genre("Rock", MusicGenre.ROCK));
        all.add(genre("Hip-Hop", MusicGenre.HIP_HOP));
        all.add(genre("R&B", MusicGenre.RNB));
        all.add(genre("Country", MusicGenre.COUNTRY));
        all.add(genre("Christian", MusicGenre.CHRISTIAN));
        all.add(genre("Dance", MusicGenre.ELECTRONIC));
        all.add(genre("Latin", MusicGenre.LATIN));
        all.add(genre("Metal", MusicGenre.METAL));
        all.add(genre("Jazz", MusicGenre.JAZZ));
        // Modern alternative: alternative rock + pop punk + emo. Drawing from
        // the recent GENRE_ROTATION window keeps it era-appropriate (the
        // early-2000s pop-punk/emo wave) and distinct from the Metal station.
        all.add(multiGenre("Alternative", MusicGenre.ROCK, MusicGenre.PUNK,
                MusicGenre.EMO));
        // Decade throwback formats. Strict-decade mixes plus a few
        // decade+genre stations using only broadly era-appropriate genres.
        all.add(decade("All 70s", 1970, 1979));
        all.add(decade("All 80s", 1980, 1989));
        all.add(decade("All 90s", 1990, 1999));
        all.add(decade("70s-80s Rock", 1970, 1989, MusicGenre.ROCK));
        all.add(decade("80s Rock", 1980, 1989, MusicGenre.ROCK));
        all.add(decade("90s Rock", 1990, 1999, MusicGenre.ROCK));
        all.add(decade("80s Pop", 1980, 1989, MusicGenre.POP));
        all.add(decade("90s Pop", 1990, 1999, MusicGenre.POP));
        return all;
    }

    public String getLabel() {
        return label;
    }

    public StationFormat getFormat() {
        return format;
    }

    /**
     * @return the primary target genre (the lowest-ordinal genre for a
     *         multi-genre station), or {@code null} for a broad station.
     */
    public MusicGenre getTargetGenre() {
        Set<MusicGenre> genres = genres();
        return genres.isEmpty() ? null : genres.iterator().next();
    }

    /**
     * @return an unmodifiable view of every genre this station accepts; empty
     *         for a broad station.
     */
    public Set<MusicGenre> getTargetGenres() {
        return Collections.unmodifiableSet(genres());
    }

    public boolean isGenreStation() {
        return !genres().isEmpty();
    }

    /** Null-safe accessor (pre-multi-genre saves deserialize the set as null). */
    private Set<MusicGenre> genres() {
        return targetGenres == null
                ? Collections.<MusicGenre>emptySet()
                : targetGenres;
    }

    /** @return true for a year-bounded decade station. */
    public boolean isDecadeStation() {
        return startYear > 0;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    /**
     * Pick the next song for a station of this type. Decade stations draw from
     * a year-bounded pool (optionally genre-filtered); all other types delegate
     * to their format, passing along any target genre.
     */
    public Song pickSong(LocalDate now, Random rng) {
        return pickSong(now, rng, null);
    }

    /**
     * Pick the next song for a station of this type, avoiding any recently
     * played songs. Decade stations draw from a year-bounded pool (optionally
     * genre-filtered); all other types delegate to their format, passing along
     * any target genre and the recency exclusion.
     *
     * @param now    current sim date
     * @param rng    RNG for selection
     * @param recent recently-played songs to avoid, or {@code null}/empty
     * @return the chosen song, or {@code null} if no songs are available
     */
    public Song pickSong(LocalDate now, Random rng, Collection<Song> recent) {
        Set<MusicGenre> genres = genres();
        if (isDecadeStation()) {
            List<Song> pool =
                    BillboardSongLoader.getSongsInYears(startYear, endYear);
            return StationFormat.pickFromPool(pool, genres, rng, recent);
        }
        return format.pickSong(now, rng, genres, recent);
    }

    /**
     * Number of most-recent selections this station type remembers and avoids
     * replaying. Top 40's small library uses a short window so its biggest
     * hits can recur through the day; Oldies uses a long window to stay fresh.
     *
     * @return the no-repeat window size (always >= 1)
     */
    public int noRepeatWindow() {
        switch (format) {
            case TOP_40:
                return SimConstants.RADIO_NO_REPEAT_WINDOW_TOP_40;
            case OLDIES_RANDOM:
                return SimConstants.RADIO_NO_REPEAT_WINDOW_OLDIES;
            default:
                return SimConstants.RADIO_NO_REPEAT_WINDOW;
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
