package entity.Radio;

import constants.SimConstants;
import utility.BillboardSongLoader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Format/flavor of an FM radio station. Each format owns the selection
 * strategy used to pick the next song to play, keeping that logic out
 * of {@link RadioStation} itself.
 *
 * <p>Future genre-tagged formats (e.g. {@code GENRE_ROCK}) can be added
 * here without changing the call sites.</p>
 */
public enum StationFormat {

    /**
     * Top 40 hits: weighted random over the current chart week, biased
     * heavily toward songs at the very top of the chart.
     */
    TOP_40 {
        @Override
        public Song pickSong(LocalDate now, Random rng) {
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(now);
            if (week == null) {
                return null;
            }
            List<Song> chart = BillboardSongLoader.getChart(week);
            if (chart.isEmpty()) {
                return null;
            }
            int topN = Math.min(SimConstants.RADIO_TOPN_WEIGHTED, chart.size());
            List<Song> pool = chart.subList(0, topN);
            return weightedPick(pool, song ->
                    Math.max(1, SimConstants.RADIO_TOPN_WEIGHTED + 1 - song.getPosition()),
                    rng);
        }
    },

    /**
     * Adult contemporary: songs with peakPos <= 20 from the last 12
     * weeks, weighted by how long they have stayed on the chart.
     */
    ADULT_CONTEMPORARY {
        @Override
        public Song pickSong(LocalDate now, Random rng) {
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(now);
            if (week == null) {
                return null;
            }
            List<Song> pool = new ArrayList<>();
            LocalDate cursor = week;
            for (int i = 0; i < 12 && cursor != null; i++) {
                for (Song s : BillboardSongLoader.getChart(cursor)) {
                    if (s.getPeakPos() <= 20) {
                        pool.add(s);
                    }
                }
                cursor = BillboardSongLoader.findChartWeekOnOrBefore(cursor.minusDays(1));
            }
            if (pool.isEmpty()) {
                return null;
            }
            return weightedPick(pool, s -> Math.max(1, s.getWeeksOnChart()), rng);
        }
    },

    /**
     * Throwback: random week 8-15 years before {@code now}, then a
     * position-weighted pick from that week's chart.
     */
    THROWBACK {
        @Override
        public Song pickSong(LocalDate now, Random rng) {
            int yearsBack = 8 + rng.nextInt(8); // 8..15
            LocalDate target = now.minusYears(yearsBack);
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(target);
            if (week == null) {
                return null;
            }
            List<Song> chart = BillboardSongLoader.getChart(week);
            if (chart.isEmpty()) {
                return null;
            }
            return weightedPick(chart, s -> Math.max(1, 101 - s.getPosition()), rng);
        }
    },

    /**
     * Oldies / random: any chart week strictly before {@code now}, then
     * a peak-position-weighted pick (so iconic #1s dominate).
     */
    OLDIES_RANDOM {
        @Override
        public Song pickSong(LocalDate now, Random rng) {
            List<LocalDate> weeks =
                    BillboardSongLoader.getAllChartWeeksBefore(now);
            if (weeks.isEmpty()) {
                return null;
            }
            LocalDate week = weeks.get(rng.nextInt(weeks.size()));
            List<Song> chart = BillboardSongLoader.getChart(week);
            if (chart.isEmpty()) {
                return null;
            }
            return weightedPick(chart, s -> Math.max(1, 101 - s.getPeakPos()), rng);
        }
    },

    /**
     * Mix: uniform random over the last 4 chart weeks.
     */
    MIX {
        @Override
        public Song pickSong(LocalDate now, Random rng) {
            LocalDate week = BillboardSongLoader.findChartWeekOnOrBefore(now);
            if (week == null) {
                return null;
            }
            List<Song> pool = new ArrayList<>();
            LocalDate cursor = week;
            for (int i = 0; i < 4 && cursor != null; i++) {
                pool.addAll(BillboardSongLoader.getChart(cursor));
                cursor = BillboardSongLoader.findChartWeekOnOrBefore(cursor.minusDays(1));
            }
            if (pool.isEmpty()) {
                return null;
            }
            return pool.get(rng.nextInt(pool.size()));
        }
    };

    /**
     * Functional interface for per-song integer weights; kept as an
     * inner type so the enum stays self-contained.
     */
    @FunctionalInterface
    private interface Weighter {
        int weight(Song song);
    }

    public abstract Song pickSong(LocalDate now, Random rng);

    /**
     * Pick a song from {@code pool} where each song's selection
     * probability is proportional to {@code weighter.weight(song)}.
     */
    private static Song weightedPick(List<Song> pool, Weighter weighter, Random rng) {
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
            default: return name();
        }
    }
}
