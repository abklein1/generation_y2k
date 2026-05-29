package entity.Radio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A single chart entry (one (week, position) row) from the Billboard Hot 100
 * dataset. Songs are immutable values produced by
 * {@code BillboardSongLoader} and consumed by radio stations to populate
 * "now playing" metadata.
 *
 * <p>The {@code genres} set is populated at parse time by
 * {@code BillboardSongLoader} via {@code MusicGenreLoader}. It is kept
 * {@link Serializable} with a stable {@code serialVersionUID}; saves written
 * before this field existed deserialize it as {@code null}, which
 * {@link #getGenres()} treats as "unknown" (empty set).</p>
 */
public final class Song implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate chartWeek;
    private final int position;
    private final String title;
    private final String performer;
    private final int peakPos;
    private final int weeksOnChart;
    private final Set<MusicGenre> genres;

    /**
     * Construct a song with no genre metadata (empty genre set). Retained for
     * callers/tests that do not need enrichment.
     */
    public Song(LocalDate chartWeek, int position, String title,
                String performer, int peakPos, int weeksOnChart) {
        this(chartWeek, position, title, performer, peakPos, weeksOnChart,
                EnumSet.noneOf(MusicGenre.class));
    }

    /**
     * Construct a song with resolved canonical genres.
     *
     * @param genres canonical genres for this entry; copied defensively and
     *               may be empty for an unmatched song
     */
    public Song(LocalDate chartWeek, int position, String title,
                String performer, int peakPos, int weeksOnChart,
                Set<MusicGenre> genres) {
        this.chartWeek = chartWeek;
        this.position = position;
        this.title = title;
        this.performer = performer;
        this.peakPos = peakPos;
        this.weeksOnChart = weeksOnChart;
        this.genres = (genres == null || genres.isEmpty())
                ? EnumSet.noneOf(MusicGenre.class)
                : EnumSet.copyOf(genres);
    }

    public LocalDate getChartWeek() {
        return chartWeek;
    }

    public int getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getPerformer() {
        return performer;
    }

    public int getPeakPos() {
        return peakPos;
    }

    public int getWeeksOnChart() {
        return weeksOnChart;
    }

    /**
     * @return immutable set of canonical genres for this song; empty when the
     *         song is unmatched or when loaded from a pre-genre save.
     */
    public Set<MusicGenre> getGenres() {
        if (genres == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(genres);
    }

    @Override
    public String toString() {
        return "\"" + title + "\" by " + performer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Song)) {
            return false;
        }
        Song other = (Song) o;
        return position == other.position
                && Objects.equals(chartWeek, other.chartWeek)
                && Objects.equals(title, other.title)
                && Objects.equals(performer, other.performer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chartWeek, position, title, performer);
    }
}
