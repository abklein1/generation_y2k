package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a single event in a player character's life history.
 * Events are displayed chronologically, grouped by year.
 *
 * Each event has a date and a description string. The description
 * is plain text that will be written as flavor text by the designer.
 * Future extensions can add event types, stat modifiers, etc.
 */
public class LifeEvent implements Comparable<LifeEvent> {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH);

    private LocalDate date;
    private String description;

    public LifeEvent(LocalDate date, String description) {
        this.date = date;
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Formats the event as a single display line.
     * Example: "May 15 - Your brother, James, is born."
     */
    public String getFormattedLine() {
        return date.format(DISPLAY_FORMAT) + " - " + description;
    }

    @Override
    public int compareTo(LifeEvent other) {
        return this.date.compareTo(other.date);
    }

    @Override
    public String toString() {
        return getFormattedLine();
    }
}
