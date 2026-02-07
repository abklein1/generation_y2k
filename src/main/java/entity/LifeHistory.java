package entity;

import java.time.LocalDate;
import java.util.*;

/**
 * An ordered collection of life events for a player character.
 * Provides methods to add events, sort them chronologically,
 * and format them grouped by year for display.
 *
 * Display format example:
 *   1989
 *   September 15 - You are born to parents Sarah and Michael.
 *
 *   1992
 *   May 15 - Your brother, James, is born.
 *
 *   1996
 *   June 2 - Your parents get a divorce.
 *   June 22 - You move out of your apartment.
 */
public class LifeHistory {

    private final List<LifeEvent> events;

    public LifeHistory() {
        this.events = new ArrayList<>();
    }

    /**
     * Add a pre-constructed LifeEvent.
     */
    public void addEvent(LifeEvent event) {
        events.add(event);
    }

    /**
     * Convenience method to add an event by date and description.
     */
    public void addEvent(LocalDate date, String description) {
        events.add(new LifeEvent(date, description));
    }

    /**
     * Returns an unmodifiable view of the events list.
     */
    public List<LifeEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Sorts all events chronologically by date.
     */
    public void sort() {
        Collections.sort(events);
    }

    /**
     * Groups events by year in chronological order.
     *
     * @return a TreeMap of year to list of events for that year
     */
    public Map<Integer, List<LifeEvent>> getEventsByYear() {
        sort();
        TreeMap<Integer, List<LifeEvent>> byYear = new TreeMap<>();
        for (LifeEvent event : events) {
            byYear.computeIfAbsent(event.getDate().getYear(), k -> new ArrayList<>()).add(event);
        }
        return byYear;
    }

    /**
     * Formats the entire history as a year-by-year display string.
     */
    public String formatHistory() {
        StringBuilder sb = new StringBuilder();
        Map<Integer, List<LifeEvent>> byYear = getEventsByYear();

        for (Map.Entry<Integer, List<LifeEvent>> entry : byYear.entrySet()) {
            sb.append(entry.getKey()).append("\n");
            for (LifeEvent event : entry.getValue()) {
                sb.append(event.getFormattedLine()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Appends the formatted history to a JTextArea, one line at a time,
     * grouped by year.
     *
     * @param textArea the text area to append to
     */
    public void appendToTextArea(javax.swing.JTextArea textArea) {
        Map<Integer, List<LifeEvent>> byYear = getEventsByYear();

        for (Map.Entry<Integer, List<LifeEvent>> entry : byYear.entrySet()) {
            textArea.append(entry.getKey() + "\n");
            for (LifeEvent event : entry.getValue()) {
                textArea.append(event.getFormattedLine() + "\n");
            }
            textArea.append("\n");
        }
    }

    public int size() {
        return events.size();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }
}
