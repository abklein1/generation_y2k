package entity.Radio;

import constants.SimConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
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
    private final StationType type;

    private Song currentSong;
    private int minutesUntilChange;
    /**
     * Most-recently-played songs (newest last), used to avoid replaying a
     * track within {@link StationType#noRepeatWindow()} selections. Capped to
     * that window. May be {@code null} when restored from a pre-history save.
     */
    private Deque<Song> recentSongs = new ArrayDeque<>();

    /**
     * Construct a broad station from a bare {@link StationFormat} (no genre
     * target). Retained for existing callers and tests.
     */
    public RadioStation(String callSign, String nickname, double frequencyMhz,
                        StationFormat format) {
        this(callSign, nickname, frequencyMhz,
                StationType.broad(format.displayLabel(), format));
    }

    /**
     * Construct a station from a {@link StationType}, which may carry a target
     * genre for genre stations.
     */
    public RadioStation(String callSign, String nickname, double frequencyMhz,
                        StationType type) {
        this.callSign = callSign;
        this.nickname = nickname;
        this.frequencyMhz = frequencyMhz;
        this.type = type;
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

    /** @return the underlying selection format (genre or broad). */
    public StationFormat getFormat() {
        return type.getFormat();
    }

    /** @return the full station type, including any target genre. */
    public StationType getStationType() {
        return type;
    }

    /** @return the target genre for genre stations, or {@code null}. */
    public MusicGenre getTargetGenre() {
        return type.getTargetGenre();
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
        if (recentSongs == null) {
            recentSongs = new ArrayDeque<>();
        }
        if (currentSong == null || minutesUntilChange <= 0) {
            Song next = type.pickSong(now, rng, recentSongs);
            if (next != null) {
                currentSong = next;
                recordRecent(next);
            }
            minutesUntilChange = SimConstants.RADIO_SONG_MINUTES;
            return;
        }
        minutesUntilChange--;
    }

    /**
     * Append {@code song} to the recently-played history and trim it back to
     * this station's {@link StationType#noRepeatWindow()}.
     */
    private void recordRecent(Song song) {
        recentSongs.addLast(song);
        int window = Math.max(1, type.noRepeatWindow());
        while (recentSongs.size() > window) {
            recentSongs.removeFirst();
        }
    }

    /**
     * Force a fresh song selection on the next tick.
     */
    public void resetSong() {
        currentSong = null;
        minutesUntilChange = 0;
    }

    /**
     * Human-readable dial label. Stations branded as "The ..." lead with the
     * frequency (e.g. {@code "103.1 The Buzz - WKRP-FM"}); stations branded
     * with a plain name lead with the name (e.g. {@code "Storm 101.5 - WKRP-FM"}),
     * mirroring how real FM stations present those two naming styles.
     *
     * @return formatted dial label including the call sign
     */
    public String displayName() {
        String freq = String.format(Locale.US, "%.1f", frequencyMhz);
        String suffix = " - " + callSign + "-FM";
        if (nickname == null) {
            return freq + suffix;
        }
        if (nickname.startsWith("The ")) {
            return freq + " " + nickname + suffix;
        }
        return nickname + " " + freq + suffix;
    }

    @Override
    public String toString() {
        return displayName();
    }
}
