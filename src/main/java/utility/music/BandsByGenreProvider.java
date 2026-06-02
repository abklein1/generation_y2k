package utility.music;

import entity.Radio.MusicGenre;
import entity.Radio.Song;
import utility.BillboardSongLoader;
import utility.GameRandom;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds and caches a {@code genre -> weighted list of performers} index from
 * the Billboard chart data, restricted to an era-appropriate window so a 2004
 * setting yields period-correct artists rather than later acts.
 *
 * <p>Performers are weighted so favorite-band picks lean toward acts that are
 * both <em>recent</em> and <em>chart-topping</em> relative to the {@link
 * #END_YEAR} game start:</p>
 * <ul>
 *   <li><b>Recency:</b> charting within the last 3 years counts most, the last
 *       10 years next, and older material counts only a little.</li>
 *   <li><b>Chart position:</b> a song's peak position scales its contribution,
 *       so #1 hits weigh far more than tail-of-the-chart entries. For material
 *       older than the 10-year window the popularity curve steepens with age,
 *       simulating that the further back you go the more likely a student only
 *       remembers the era's biggest hits rather than deep cuts.</li>
 * </ul>
 *
 * <p>A performer's weight is the sum of these per-song contributions, so
 * prolific, high-charting, recent acts rise to the top of a genre's roster.
 * {@link #pickAnyBand()} ignores all weighting for callers (e.g. Outcasts) who
 * should draw a completely random act from any genre.</p>
 */
public final class BandsByGenreProvider {

    /** First chart year included (inclusive). */
    static final int START_YEAR = 1979;
    /** Last chart year included (inclusive) - the 2004 game start. */
    static final int END_YEAR = 2004;

    /** Songs within this many years of {@link #END_YEAR} get the top tier. */
    static final int RECENT_YEARS = 3;
    /** Songs within this many years of {@link #END_YEAR} get the middle tier. */
    static final int DECADE_YEARS = 10;

    /** Recency multipliers (last 3 years strongly favored, older fades fast). */
    static final double WEIGHT_RECENT = 6.0;
    static final double WEIGHT_DECADE = 2.0;
    static final double WEIGHT_OLDER = 0.3;

    /**
     * Exponent that penalizes acts spanning many genres, sharpening genre fit.
     * An act tagged with N genres contributes only {@code 1 / N^exponent} of
     * its weight to each, so a focused punk band outweighs a rapper who merely
     * carries a stray PUNK tag in the punk roster.
     */
    static final double GENRE_FIT_EXPONENT = 2.0;

    /**
     * Per-year growth of the chart-position exponent for songs older than the
     * decade window. Each year past the window makes the popularity curve a
     * little steeper, so only the era's biggest hits keep meaningful weight.
     */
    static final double POPULARITY_AGE_STEP = 0.2;
    /** Cap on the popularity exponent so the oldest weights stay sensible. */
    static final double POPULARITY_MAX_EXPONENT = 4.0;

    /** Earliest future month (relative to "now") a tastemaker can reach. */
    static final int FUTURE_MIN_MONTHS = 3;
    /** Latest future month (relative to "now") a tastemaker can reach. */
    static final int FUTURE_MAX_MONTHS = 8;

    /**
     * Weight given to a curated band in a genre that has no charting acts at
     * all - roughly one recent #1 hit's worth, so the genre is still drawable.
     */
    static final double CURATED_FALLBACK_WEIGHT = WEIGHT_RECENT;

    private static volatile boolean loaded = false;
    private static final Map<MusicGenre, List<WeightedBand>> byGenre =
            new EnumMap<>(MusicGenre.class);
    private static final Map<MusicGenre, Double> totalWeight =
            new EnumMap<>(MusicGenre.class);
    private static final List<String> allBands = new ArrayList<>();

    // Future windows are the same for every student in a generation run, so
    // cache the (small) per-reference-date index rather than rescanning.
    private static final Map<LocalDate, Map<MusicGenre, List<WeightedBand>>>
            futureCache = new HashMap<>();

    private BandsByGenreProvider() {
    }

    /** A performer paired with their accumulated selection weight. */
    private static final class WeightedBand {
        final String name;
        final double weight;

        WeightedBand(String name, double weight) {
            this.name = name;
            this.weight = weight;
        }
    }

    /**
     * @param genre a canonical genre
     * @return an immutable, possibly-empty list of era-appropriate performers
     *         for that genre, ordered most-weighted first
     */
    public static List<String> bandsFor(MusicGenre genre) {
        ensureLoaded();
        List<WeightedBand> bands = byGenre.get(genre);
        if (bands == null || bands.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>(bands.size());
        for (WeightedBand band : bands) {
            names.add(band.name);
        }
        return Collections.unmodifiableList(names);
    }

    /**
     * Picks a single performer for a genre using the recency/chart-position
     * weighting, so recent #1 hits are far likelier than old tail entries.
     *
     * @param genre a canonical genre
     * @return a weighted-random performer, or {@code null} if none exist
     */
    public static String pickBand(MusicGenre genre) {
        ensureLoaded();
        return weightedPick(byGenre.get(genre));
    }

    /**
     * Picks a band a few months in the sim's future for a given genre, used by
     * "tastemaker" students who like acts before they break. Candidates are
     * songs charting in {@code [referenceDate + 3mo, referenceDate + 8mo]},
     * weighted by eventual chart peak so the picks become genuine hits.
     *
     * @param genre         a canonical genre
     * @param referenceDate the current sim date
     * @return a weighted-random future performer, or {@code null} if none chart
     *         in that window for the genre
     */
    public static String pickFutureBand(MusicGenre genre, LocalDate referenceDate) {
        if (referenceDate == null) {
            return null;
        }
        return weightedPick(futureIndex(referenceDate).get(genre));
    }

    /** Weighted-random selection over a (possibly null/empty) weighted list. */
    private static String weightedPick(List<WeightedBand> bands) {
        if (bands == null || bands.isEmpty()) {
            return null;
        }
        double total = 0.0;
        for (WeightedBand band : bands) {
            total += band.weight;
        }
        if (total <= 0.0) {
            return bands.get(GameRandom.nextInt(bands.size())).name;
        }
        double roll = GameRandom.nextDouble() * total;
        for (WeightedBand band : bands) {
            roll -= band.weight;
            if (roll <= 0.0) {
                return band.name;
            }
        }
        return bands.get(bands.size() - 1).name;
    }

    private static synchronized Map<MusicGenre, List<WeightedBand>> futureIndex(
            LocalDate referenceDate) {
        return futureCache.computeIfAbsent(referenceDate,
                BandsByGenreProvider::buildFutureIndex);
    }

    private static Map<MusicGenre, List<WeightedBand>> buildFutureIndex(
            LocalDate referenceDate) {
        LocalDate from = referenceDate.plusMonths(FUTURE_MIN_MONTHS);
        LocalDate to = referenceDate.plusMonths(FUTURE_MAX_MONTHS);
        Map<MusicGenre, Map<String, Double>> acc = new EnumMap<>(MusicGenre.class);
        for (Song song : BillboardSongLoader.getSongsInDateRange(from, to)) {
            String performer = cleanPerformer(song.getPerformer());
            if (performer == null) {
                continue;
            }
            // Future songs have no recency penalty; weight purely by how big a
            // hit they become, so tastemakers "call" real breakouts.
            accumulate(acc, performer, song, positionWeight(song));
        }
        Map<MusicGenre, List<WeightedBand>> index = new EnumMap<>(MusicGenre.class);
        for (Map.Entry<MusicGenre, Map<String, Double>> e : acc.entrySet()) {
            List<WeightedBand> bands = new ArrayList<>();
            for (Map.Entry<String, Double> b : e.getValue().entrySet()) {
                bands.add(new WeightedBand(b.getKey(), b.getValue()));
            }
            bands.sort(Comparator.comparingDouble(
                    (WeightedBand wb) -> wb.weight).reversed());
            index.put(e.getKey(), bands);
        }
        return index;
    }

    /**
     * Picks a uniformly random performer from any genre, ignoring recency and
     * popularity weighting entirely. Used for listeners (e.g. Outcasts) whose
     * taste is deliberately eclectic and unpredictable.
     *
     * @return a completely random era-appropriate performer, or {@code null}
     *         if the index is empty
     */
    public static String pickAnyBand() {
        ensureLoaded();
        if (allBands.isEmpty()) {
            return null;
        }
        return allBands.get(GameRandom.nextInt(allBands.size()));
    }

    /** Force the index to be built if it has not been already. Idempotent. */
    public static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        Map<MusicGenre, Map<String, Double>> acc = new EnumMap<>(MusicGenre.class);
        for (Song song : BillboardSongLoader.getSongsInYears(START_YEAR, END_YEAR)) {
            String performer = cleanPerformer(song.getPerformer());
            if (performer == null) {
                continue;
            }
            accumulate(acc, performer, song,
                    recencyWeight(song) * positionWeight(song));
        }
        mergeCuratedBands(acc);
        java.util.LinkedHashSet<String> distinct = new java.util.LinkedHashSet<>();
        for (Map.Entry<MusicGenre, Map<String, Double>> e : acc.entrySet()) {
            List<WeightedBand> bands = new ArrayList<>();
            double total = 0.0;
            for (Map.Entry<String, Double> b : e.getValue().entrySet()) {
                bands.add(new WeightedBand(b.getKey(), b.getValue()));
                total += b.getValue();
                distinct.add(b.getKey());
            }
            bands.sort(Comparator.comparingDouble(
                    (WeightedBand wb) -> wb.weight).reversed());
            byGenre.put(e.getKey(), bands);
            totalWeight.put(e.getKey(), total);
        }
        allBands.addAll(distinct);
        loaded = true;
    }

    /**
     * Folds curated, scene-defining acts (see {@link CuratedGenreBandsLoader})
     * into each genre's accumulator at a weight on par with that genre's top
     * charting act. This lets bands the Billboard Hot 100 under-represents -
     * non-charting acts and post-2004 breakouts such as the 2004 emo wave -
     * still surface as favorites. A curated act that also charted in the genre
     * keeps the higher of its earned or curated weight (never double-counted).
     */
    private static void mergeCuratedBands(
            Map<MusicGenre, Map<String, Double>> acc) {
        for (Map.Entry<MusicGenre, List<String>> e
                : CuratedGenreBandsLoader.getBandsByGenre().entrySet()) {
            Map<String, Double> genreAcc =
                    acc.computeIfAbsent(e.getKey(), k -> new LinkedHashMap<>());
            double weight = topChartingWeight(genreAcc);
            for (String band : e.getValue()) {
                if (band == null || band.trim().isEmpty()) {
                    continue;
                }
                genreAcc.merge(band.trim(), weight, Math::max);
            }
        }
    }

    /**
     * The weight of the genre's current top charting act, used to size curated
     * bands so each is as likely as the most popular real act. Falls back to
     * {@link #CURATED_FALLBACK_WEIGHT} for a genre with no charting acts.
     */
    private static double topChartingWeight(Map<String, Double> genreAcc) {
        double max = 0.0;
        for (double w : genreAcc.values()) {
            if (w > max) {
                max = w;
            }
        }
        return max > 0.0 ? max : CURATED_FALLBACK_WEIGHT;
    }

    /**
     * Adds a song's weight to each of its (non-OTHER) genres for a performer,
     * split by the genre-fit penalty so multi-genre acts contribute less to any
     * single genre than focused acts do.
     */
    private static void accumulate(Map<MusicGenre, Map<String, Double>> acc,
                                   String performer, Song song, double base) {
        if (base <= 0.0) {
            return;
        }
        List<MusicGenre> genres = effectiveGenres(song);
        if (genres.isEmpty()) {
            return;
        }
        double contribution = base / Math.pow(genres.size(), GENRE_FIT_EXPONENT);
        for (MusicGenre genre : genres) {
            acc.computeIfAbsent(genre, k -> new LinkedHashMap<>())
                    .merge(performer, contribution, Double::sum);
        }
    }

    /**
     * The non-OTHER genres of a song, after correcting a known tagging
     * artifact: the offline collapser maps the keyword "hardcore" to PUNK,
     * which mislabels "hardcore hip hop/rap" acts as punk. Since no act is
     * genuinely both mainstream rap and punk, PUNK is dropped whenever HIP_HOP
     * is present, keeping rappers out of punk/metal-leaning rosters.
     */
    private static List<MusicGenre> effectiveGenres(Song song) {
        boolean hasHipHop = song.getGenres().contains(MusicGenre.HIP_HOP);
        List<MusicGenre> genres = new ArrayList<>();
        for (MusicGenre genre : song.getGenres()) {
            if (genre == MusicGenre.OTHER) {
                continue;
            }
            if (genre == MusicGenre.PUNK && hasHipHop) {
                continue;
            }
            genres.add(genre);
        }
        return genres;
    }

    /** Recency tier multiplier based on how recent the song's chart week is. */
    private static double recencyWeight(Song song) {
        if (song.getChartWeek() == null) {
            return WEIGHT_OLDER;
        }
        int yearsAgo = END_YEAR - song.getChartWeek().getYear();
        if (yearsAgo < RECENT_YEARS) {
            return WEIGHT_RECENT;
        }
        if (yearsAgo < DECADE_YEARS) {
            return WEIGHT_DECADE;
        }
        return WEIGHT_OLDER;
    }

    /**
     * Chart-position multiplier in (0, 1]: a peak of #1 scores 1.0 and weight
     * falls off toward the bottom of the Hot 100, so top-of-chart acts are
     * favored. For songs older than the {@link #DECADE_YEARS} window the curve
     * is raised to a power that grows with age, so the deep cuts of decades
     * past fade out and only the era's biggest hits stay memorable.
     */
    private static double positionWeight(Song song) {
        int peak = song.getPeakPos();
        if (peak <= 0) {
            peak = 100;
        }
        peak = Math.min(100, peak);
        double base = (101 - peak) / 100.0;

        int yearsAgo = song.getChartWeek() == null
                ? DECADE_YEARS
                : END_YEAR - song.getChartWeek().getYear();
        if (yearsAgo < DECADE_YEARS) {
            return base;
        }
        double exponent = Math.min(POPULARITY_MAX_EXPONENT,
                1.0 + (yearsAgo - DECADE_YEARS + 1) * POPULARITY_AGE_STEP);
        return Math.pow(base, exponent);
    }

    /**
     * Normalizes a Billboard performer credit into a band name suitable for
     * merch and flavor text. Featured-artist credits are trimmed to the lead
     * act so we never produce a "{@code X Featuring Y} hoodie"; blank or
     * collaboration-only credits are dropped.
     */
    private static String cleanPerformer(String performer) {
        if (performer == null) {
            return null;
        }
        String name = performer.trim();
        if (name.isEmpty()) {
            return null;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        for (String marker : new String[]{" featuring ", " feat. ", " feat ",
                " with ", " duet with ", " & "}) {
            int idx = lower.indexOf(marker);
            if (idx > 0) {
                name = name.substring(0, idx).trim();
                break;
            }
        }
        return name.isEmpty() ? null : name;
    }

    /** Reset the cache. Visible for tests. */
    static synchronized void resetForTests() {
        loaded = false;
        byGenre.clear();
        totalWeight.clear();
        allBands.clear();
        futureCache.clear();
    }
}
