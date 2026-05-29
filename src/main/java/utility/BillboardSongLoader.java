package utility;

import entity.Radio.MusicGenre;
import entity.Radio.Song;
import utility.io.ResourceAccess;
import utility.music.MusicGenreLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Lazy-loaded index of the Billboard Hot 100 dataset
 * ({@code Resources/Music/song_popularity.csv}).
 *
 * <p>The CSV header is
 * {@code chart_week,current_week,title,performer,last_week,peak_pos,wks_on_chart}.
 * Titles can contain commas wrapped in double quotes (e.g.
 * {@code "Pisces, Aquarius And Jones Ltd."}), so we parse with a small
 * RFC-style quoted-field splitter rather than {@code String.split(",")}.</p>
 *
 * <p>The full dataset (~330K rows) is held in memory, indexed two ways:
 * <ul>
 *   <li>{@code chartWeek -> sorted List<Song>} for fast week lookups.</li>
 *   <li>A {@link NavigableSet} of all chart weeks for "find chart on or
 *       before this date" lookups (since chart weeks are weekly Saturdays).</li>
 * </ul>
 */
public final class BillboardSongLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CSV_PATH =
            "/Resources/Music/song_popularity.csv";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("M/d/yyyy");

    private static volatile boolean loaded = false;
    private static final Map<LocalDate, List<Song>> chartByWeek =
            new HashMap<>();
    private static final NavigableSet<LocalDate> allChartWeeks = new TreeSet<>();

    private BillboardSongLoader() {
    }

    /**
     * Force the CSV to be parsed if it has not been already. Idempotent.
     */
    public static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            loadCsv();
            loaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Billboard song chart", e);
        }
    }

    /**
     * @param week a chart-week date (must match a Billboard Saturday exactly)
     * @return immutable list of the 100 songs that charted that week, sorted
     *         by chart position; empty list if the week is unknown.
     */
    public static List<Song> getChart(LocalDate week) {
        ensureLoaded();
        List<Song> chart = chartByWeek.get(week);
        if (chart == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(chart);
    }

    /**
     * Find the most recent chart week with a date {@code <= now}.
     *
     * @param now any sim date
     * @return chart-week date if one exists at or before {@code now}, or
     *         {@code null} if {@code now} predates the dataset.
     */
    public static LocalDate findChartWeekOnOrBefore(LocalDate now) {
        ensureLoaded();
        return allChartWeeks.floor(now);
    }

    /**
     * @param now any sim date
     * @return all chart weeks strictly before {@code now}, in ascending order.
     */
    public static List<LocalDate> getAllChartWeeksBefore(LocalDate now) {
        ensureLoaded();
        return new ArrayList<>(allChartWeeks.headSet(now, false));
    }

    /**
     * @return total number of chart weeks indexed (mostly used by tests).
     */
    public static int getChartWeekCount() {
        ensureLoaded();
        return allChartWeeks.size();
    }

    /**
     * Collect every charted song whose chart-week year falls within the
     * inclusive range {@code [startYear, endYear]}. Used by decade-format
     * radio stations to build a year-bounded candidate pool.
     *
     * @param startYear first calendar year to include (inclusive)
     * @param endYear   last calendar year to include (inclusive)
     * @return all songs from chart weeks in that year range; empty if none
     */
    public static List<Song> getSongsInYears(int startYear, int endYear) {
        ensureLoaded();
        List<Song> out = new ArrayList<>();
        for (LocalDate week : allChartWeeks) {
            int year = week.getYear();
            if (year >= startYear && year <= endYear) {
                List<Song> chart = chartByWeek.get(week);
                if (chart != null) {
                    out.addAll(chart);
                }
            }
        }
        return out;
    }

    /**
     * Reset the cache. Visible for tests.
     */
    static synchronized void resetForTests() {
        loaded = false;
        chartByWeek.clear();
        allChartWeeks.clear();
    }

    private static void loadCsv() throws IOException {
        Map<LocalDate, List<Song>> tempByWeek = new HashMap<>();
        try (BufferedReader br = new BufferedReader(
                ResourceAccess.reader(CSV_PATH))) {
            // Skip header
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] fields = parseCsvLine(line);
                if (fields.length < 7) {
                    continue;
                }
                try {
                    LocalDate week = LocalDate.parse(fields[0], DATE_FORMAT);
                    int position = Integer.parseInt(fields[1].trim());
                    String title = fields[2];
                    String performer = fields[3];
                    int peakPos = Integer.parseInt(fields[5].trim());
                    int weeksOnChart = Integer.parseInt(fields[6].trim());
                    Set<MusicGenre> genres =
                            MusicGenreLoader.genresFor(title, performer);
                    Song song = new Song(week, position, title, performer,
                            peakPos, weeksOnChart, genres);
                    tempByWeek.computeIfAbsent(week, k -> new ArrayList<>(100))
                            .add(song);
                } catch (RuntimeException ignore) {
                    // Skip any malformed row rather than failing whole load.
                }
            }
        }
        for (Map.Entry<LocalDate, List<Song>> e : tempByWeek.entrySet()) {
            e.getValue().sort((a, b) -> Integer.compare(a.getPosition(), b.getPosition()));
        }
        chartByWeek.putAll(tempByWeek);
        allChartWeeks.addAll(tempByWeek.keySet());
    }

    /**
     * Minimal CSV field splitter that honors double-quoted fields and treats
     * doubled quotes as escaped quote characters. Sufficient for the Billboard
     * dataset (no embedded newlines).
     */
    static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>(8);
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == ',') {
                    out.add(current.toString());
                    current.setLength(0);
                } else if (c == '"' && current.length() == 0) {
                    inQuotes = true;
                } else {
                    current.append(c);
                }
            }
        }
        out.add(current.toString());
        return out.toArray(new String[0]);
    }
}
