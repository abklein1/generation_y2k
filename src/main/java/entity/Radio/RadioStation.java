package entity.Radio;

import constants.SimConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Random;

/**
 * One procedurally-generated FM radio station. Holds the immutable
 * identifying metadata (call sign, nickname, frequency, format) and the
 * runtime "now playing" state that is advanced by {@link #tick}.
 *
 * <p>Stations are owned by the {@link Radio} container and ticked once
 * per simulated minute by the simulation engine.</p>
 */
public final class RadioStation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String callSign;
    private final String nickname;
    private final double frequencyMhz;
    private final StationFormat format;

    private Song currentSong;
    private int minutesUntilChange;

    public RadioStation(String callSign, String nickname, double frequencyMhz,
                        StationFormat format) {
        this.callSign = callSign;
        this.nickname = nickname;
        this.frequencyMhz = frequencyMhz;
        this.format = format;
        this.currentSong = null;
        this.minutesUntilChange = 0;
    }

    public String getCallSign() {
        return callSign;
    }

    public String getNickname() {
        return nickname;
    }

    public double getFrequencyMhz() {
        return frequencyMhz;
    }

    public StationFormat getFormat() {
        return format;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    /**
     * @return remaining sim-minutes before this station picks a new song.
     *         0 means the next call to {@link #tick} will rotate the song.
     */
    public int getMinutesUntilChange() {
        return minutesUntilChange;
    }

    /**
     * Advance one simulated minute. If no song has been picked yet, or
     * the rotation timer has expired, asks the format to choose a fresh
     * song and resets the timer.
     *
     * @param now the current sim date (used by date-aware formats)
     * @param rng RNG for deterministic selection (typically {@code GameRandom})
     */
    public void tick(LocalDate now, Random rng) {
        if (currentSong == null || minutesUntilChange <= 0) {
            Song next = format.pickSong(now, rng);
            if (next != null) {
                currentSong = next;
            }
            minutesUntilChange = SimConstants.RADIO_SONG_MINUTES;
            return;
        }
        minutesUntilChange--;
    }

    /**
     * Force a fresh song selection on the next tick.
     */
    public void resetSong() {
        currentSong = null;
        minutesUntilChange = 0;
    }

    /**
     * @return human-readable line like {@code "94.3 The Mix - WKRP-FM"}.
     */
    public String displayName() {
        String freq = String.format(Locale.US, "%.1f", frequencyMhz);
        return freq + " " + nickname + " - " + callSign + "-FM";
    }

    @Override
    public String toString() {
        return displayName();
    }
}
