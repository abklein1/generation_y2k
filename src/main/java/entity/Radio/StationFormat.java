package entity.Radio;

import constants.SimConstants;
import utility.BillboardSongLoader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Format/flavor of an FM radio station. Each format owns the
 * <em>selection strategy</em> used to pick the next song to play, expressed
 * as a {@link #candidatePool(LocalDate, Random)} plus a per-song
 * {@link #weight(Song)}. Keeping those two pieces separate lets the shared
 * {@link #pickSong(LocalDate, Random, MusicGenre)} apply an optional genre
 * filter uniformly across every format.
 *
 * <p>{@link #GENRE_ROTATION} is the broad pool used by genre-targeted
 * stations (see {@link StationType}); the genre itself lives on the station,
 * not the format, which avoids one enum constant per genre.</p>
 */
public enum StationFormat {

    /**
     * Top 40 hits: weighted random over the current chart week, biased
     * heavily toward songs at the very top of the chart.
     */
    TOP_40 {
        @Override
        protected List<Song> candidatePool(LocalDate now, Random rng) {
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(now);
            if (week == null) {
                return new ArrayList<>();
            }
            List<Song> chart = BillboardSongLoader.getChart(week);
            if (chart.isEmpty()) {
                return new ArrayList<>();
            }
            int topN = Math.min(SimConstants.RADIO_TOPN_WEIGHTED, chart.size());
            return new ArrayList<>(chart.subList(0, topN));
        }

        @Override
        protected int weight(Song song) {
            return Math.max(1,
                    SimConstants.RADIO_TOPN_WEIGHTED + 1 - song.getPosition());
        }
    },

    /**
     * Adult contemporary: songs with peakPos <= 20 from the last 12
     * weeks, weighted by how long they have stayed on the chart.
     */
    ADULT_CONTEMPORARY {
        @Override
        protected List<Song> candidatePool(LocalDate now, Random rng) {
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(now);
            if (week == null) {
                return new ArrayList<>();
            }
            List<Song> pool = new ArrayList<>();
            LocalDate cursor = week;
            for (int i = 0; i < 12 && cursor != null; i++) {
                for (Song s : BillboardSongLoader.getChart(cursor)) {
                    if (s.getPeakPos() <= 20) {
                        pool.add(s);
                    }
                }
                cursor = BillboardSongLoader.findChartWeekOnOrBefore(
                        cursor.minusDays(1));
            }
            return pool;
        }

        @Override
        protected int weight(Song song) {
            return Math.max(1, song.getWeeksOnChart());
        }
    },

    /**
     * Throwback: random week 8-15 years before {@code now}, then a
     * position-weighted pick from that week's chart.
     */
    THROWBACK {
        @Override
        protected List<Song> candidatePool(LocalDate now, Random rng) {
            int yearsBack = 8 + rng.nextInt(8); // 8..15
            LocalDate target = now.minusYears(yearsBack);
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(target);
            if (week == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(BillboardSongLoader.getChart(week));
        }

        @Override
        protected int weight(Song song) {
            return Math.max(1, 101 - song.getPosition());
        }
    },

    /**
     * Oldies / random: any chart week at least
     * {@link SimConstants#RADIO_OLDIES_MIN_AGE_YEARS} years before
     * {@code now}, then a peak-position-weighted pick (so iconic #1s
     * dominate). The age floor keeps the format from playing songs that are
     * merely older than the sim date (e.g. early-2000s hits in a 2004 game).
     */
    OLDIES_RANDOM {
        @Override
        protected List<Song> candidatePool(LocalDate now, Random rng) {
            LocalDate cutoff =
                    now.minusYears(SimConstants.RADIO_OLDIES_MIN_AGE_YEARS);
            List<LocalDate> weeks =
                    BillboardSongLoader.getAllChartWeeksBefore(cutoff);
            if (weeks.isEmpty()) {
                return new ArrayList<>();
            }
            LocalDate week = weeks.get(rng.nextInt(weeks.size()));
            return new ArrayList<>(BillboardSongLoader.getChart(week));
        }

        @Override
        protected int weight(Song song) {
            return Math.max(1, 101 - song.getPeakPos());
        }
    },

    /**
     * Mix: uniform random over the last 4 chart weeks.
     */
    MIX {
        @Override
        protected List<Song> candidatePool(LocalDate now, Random rng) {
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(now);
            if (week == null) {
                return new ArrayList<>();
            }
            List<Song> pool = new ArrayList<>();
            LocalDate cursor = week;
            for (int i = 0; i < 4 && cursor != null; i++) {
                pool.addAll(BillboardSongLoader.getChart(cursor));
                cursor = BillboardSongLoader.findChartWeekOnOrBefore(
                        cursor.minusDays(1));
            }
            return pool;
        }

        @Override
        protected int weight(Song song) {
            return 1;
        }
    },

    /**
     * Genre rotation: a broad pool spanning the last
     * {@link SimConstants#RADIO_GENRE_WEEKS} chart weeks, weighted toward
     * peak hits. On its own this behaves like a wide "recent hits" station;
     * paired with a {@code MusicGenre} on a {@link StationType} it becomes a
     * genre station via the genre filter in {@link #pickSong}.
     */
    GENRE_ROTATION {
        @Override
        protected List<Song> candidatePool(LocalDate now, Random rng) {
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(now);
            if (week == null) {
                return new ArrayList<>();
            }
            List<Song> pool = new ArrayList<>();
            LocalDate cursor = week;
            for (int i = 0; i < SimConstants.RADIO_GENRE_WEEKS && cursor != null;
                    i++) {
                pool.addAll(BillboardSongLoader.getChart(cursor));
                cursor = BillboardSongLoader.findChartWeekOnOrBefore(
                        cursor.minusDays(1));
            }
            return pool;
        }

        @Override
        protected int weight(Song song) {
            return Math.max(1, 101 - song.getPeakPos());
        }
    },

    /**
     * Decade format: plays a year-bounded slice of chart history (e.g. "All
     * 80s"), weighted toward the era's biggest hits. The year range itself
     * lives on the {@link StationType} (a station is data, the format is the
     * strategy), so the bare format has no inherent pool - {@link StationType}
     * supplies the pool via {@link #pickFromPool(List, MusicGenre, Random)}.
     */
    DECADE {
        @Override
        protected List<Song> candidatePool(LocalDate now, Random rng) {
            return new ArrayList<>();
        }

        @Override
        protected int weight(Song song) {
            return Math.max(1, 101 - song.getPeakPos());
        }
    };

    /**
     * Build the candidate song pool for this format at the given date.
     *
     * @param now current sim date
     * @param rng RNG for formats that sample a random week
     * @return a mutable, possibly-empty pool of candidate songs
     */
    protected abstract List<Song> candidatePool(LocalDate now, Random rng);

    /**
     * Per-song selection weight (higher = more likely). Must return >= 1 for
     * songs that should be selectable.
     */
    protected abstract int weight(Song song);

    /**
     * Pick the next song with no genre constraint.
     */
    public Song pickSong(LocalDate now, Random rng) {
        return pickSong(now, rng, null, null);
    }

    /**
     * Pick the next song, optionally restricted to a single target genre.
     */
    public Song pickSong(LocalDate now, Random rng, MusicGenre genre) {
        return pickSong(now, rng, asGenreSet(genre), null);
    }

    /**
     * Pick the next song, optionally restricted to one or more target genres
     * (any-match) and avoiding recently-played songs.
     *
     * <p>When {@code genres} is non-empty the candidate pool is filtered to
     * songs tagged with at least one of those genres. If the filter would
     * leave the pool empty (e.g. an era with little genre coverage), the
     * unfiltered pool is used so the station still plays something rather than
     * going silent. The {@code recent} exclusion is applied the same way:
     * songs matching a recently-played title/performer are removed, but only
     * when at least one fresh candidate remains.</p>
     *
     * @param now    current sim date
     * @param rng    RNG for selection
     * @param genres target genres (any-match), or {@code null}/empty for the
     *               broad format pool
     * @param recent recently-played songs to avoid, or {@code null}/empty
     * @return the chosen song, or {@code null} if no songs are available
     */
    public Song pickSong(LocalDate now, Random rng, Set<MusicGenre> genres,
                         Collection<Song> recent) {
        return pickWeighted(candidatePool(now, rng), genres, recent,
                this::weight, rng);
    }

    /**
     * Select from a caller-supplied pool, peak-hit weighted, with an optional
     * single-genre filter. Used by decade stations whose pool is a year-bounded
     * slice built by {@link StationType} rather than one of the format pools.
     */
    public static Song pickFromPool(List<Song> pool, MusicGenre genre,
                                    Random rng) {
        return pickFromPool(pool, asGenreSet(genre), rng, null);
    }

    /**
     * Select from a caller-supplied pool, peak-hit weighted, with an optional
     * any-match genre filter and recently-played exclusion.
     *
     * @param pool   candidate songs (e.g. all songs from the 1980s)
     * @param genres optional genre filter (any-match), or {@code null}/empty
     * @param rng    RNG for selection
     * @param recent recently-played songs to avoid, or {@code null}/empty
     * @return the chosen song, or {@code null} if the pool is empty
     */
    public static Song pickFromPool(List<Song> pool, Set<MusicGenre> genres,
                                    Random rng, Collection<Song> recent) {
        return pickWeighted(pool, genres, recent,
                s -> Math.max(1, 101 - s.getPeakPos()), rng);
    }

    private static Set<MusicGenre> asGenreSet(MusicGenre genre) {
        return genre == null ? null : EnumSet.of(genre);
    }

    /**
     * Shared selection pipeline: optionally filter {@code pool} to the target
     * genres (any-match), then drop recently-played songs, then weighted-pick
     * using {@code weighter}. Each filter falls back to the pre-filter pool
     * when it would otherwise leave nothing to play, so a station never goes
     * silent.
     */
    private static Song pickWeighted(List<Song> pool, Set<MusicGenre> genres,
                                     Collection<Song> recent,
                                     Weighter weighter, Random rng) {
        if (pool == null || pool.isEmpty()) {
            return null;
        }
        if (genres != null && !genres.isEmpty()) {
            List<Song> filtered = new ArrayList<>();
            for (Song s : pool) {
                if (!Collections.disjoint(s.getGenres(), genres)) {
                    filtered.add(s);
                }
            }
            if (!filtered.isEmpty()) {
                pool = filtered;
            }
        }
        if (recent != null && !recent.isEmpty()) {
            Set<String> recentKeys = new HashSet<>();
            for (Song s : recent) {
                recentKeys.add(songKey(s));
            }
            List<Song> fresh = new ArrayList<>();
            for (Song s : pool) {
                if (!recentKeys.contains(songKey(s))) {
                    fresh.add(s);
                }
            }
            if (!fresh.isEmpty()) {
                pool = fresh;
            }
        }
        return weightedPick(pool, weighter, rng);
    }

    /**
     * Identity used for recently-played matching: a song is "the same song"
     * if it shares a title and performer, regardless of which chart week it
     * was drawn from.
     */
    private static String songKey(Song song) {
        return (song.getTitle() + '\u0001' + song.getPerformer())
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Functional interface for per-song integer weights; kept as an
     * inner type so the enum stays self-contained.
     */
    @FunctionalInterface
    private interface Weighter {
        int weight(Song song);
    }

    /**
     * Pick a song from {@code pool} where each song's selection
     * probability is proportional to {@code weighter.weight(song)}.
     */
    private static Song weightedPick(List<Song> pool, Weighter weighter,
                                     Random rng) {
        long total = 0L;
        for (Song s : pool) {
            total += Math.max(0, weighter.weight(s));
        }
        if (total <= 0L) {
            return pool.get(rng.nextInt(pool.size()));
        }
        long target = (long) (rng.nextDouble() * total);
        long running = 0L;
        for (Song s : pool) {
            running += Math.max(0, weighter.weight(s));
            if (running > target) {
                return s;
            }
        }
        return pool.get(pool.size() - 1);
    }

    /**
     * Human-readable label for inspector / future UI.
     */
    public String displayLabel() {
        switch (this) {
            case TOP_40: return "Top 40";
            case ADULT_CONTEMPORARY: return "Adult Contemporary";
            case THROWBACK: return "Throwback";
            case OLDIES_RANDOM: return "Oldies";
            case MIX: return "Mix";
            case GENRE_ROTATION: return "Genre";
            case DECADE: return "Decade";
            default: return name();
        }
    }
}
