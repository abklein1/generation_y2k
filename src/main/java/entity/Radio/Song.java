package entity.Radio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A single chart entry (one (week, position) row) from the Billboard Hot 100
 * dataset. Songs are immutable values produced by
 * {@code BillboardSongLoader} and consumed by radio stations to populate
 * "now playing" metadata.
 *
 * <p>Future genre-tag work can extend this POJO with a {@code genre}
 * field without breaking the save format because the field is
 * {@link Serializable} with a stable {@code serialVersionUID}.</p>
 */
public final class Song implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate chartWeek;
    private final int position;
    private final String title;
    private final String performer;
    private final int peakPos;
    private final int weeksOnChart;

    public Song(LocalDate chartWeek, int position, String title,
                String performer, int peakPos, int weeksOnChart) {
        this.chartWeek = chartWeek;
        this.position = position;
        this.title = title;
        this.performer = performer;
        this.peakPos = peakPos;
        this.weeksOnChart = weeksOnChart;
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
