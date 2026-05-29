package entity.Radio;

import constants.SimConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RadioStation")
class RadioStationTest {

    private static final LocalDate SIM_DATE = LocalDate.of(2004, 7, 24);

    @Test
    @DisplayName("'The ...' nicknames lead with the frequency")
    void testDisplayNameTheStyle() {
        RadioStation s = new RadioStation("WKRP", "The Mix", 94.3,
                StationFormat.TOP_40);
        assertEquals("94.3 The Mix - WKRP-FM", s.displayName());
    }

    @Test
    @DisplayName("Plain-name nicknames lead with the name, then frequency")
    void testDisplayNamePlainStyle() {
        RadioStation s = new RadioStation("KSTM", "Storm", 101.5,
                StationFormat.TOP_40);
        assertEquals("Storm 101.5 - KSTM-FM", s.displayName());
    }

    @Test
    @DisplayName("First tick selects a song and resets the rotation timer")
    void testFirstTickPicksSong() {
        RadioStation s = new RadioStation("KABC", "Hot", 101.5,
                StationFormat.TOP_40);
        assertNull(s.getCurrentSong());
        s.tick(SIM_DATE, new Random(1L));
        assertNotNull(s.getCurrentSong(), "First tick should pick a song");
        assertEquals(SimConstants.RADIO_SONG_MINUTES, s.getMinutesUntilChange());
    }

    @Test
    @DisplayName("Song does not change for RADIO_SONG_MINUTES - 1 ticks")
    void testSongPersistsBeforeRotation() {
        RadioStation s = new RadioStation("KXYZ", "Mix", 96.1,
                StationFormat.TOP_40);
        Random rng = new Random(42L);
        s.tick(SIM_DATE, rng);
        Song first = s.getCurrentSong();
        for (int i = 0; i < SimConstants.RADIO_SONG_MINUTES - 1; i++) {
            s.tick(SIM_DATE, rng);
            assertEquals(first, s.getCurrentSong(),
                    "Song should not rotate at minute " + (i + 1));
        }
    }

    @Test
    @DisplayName("Song rotates after RADIO_SONG_MINUTES ticks")
    void testSongRotatesAfterTimer() {
        RadioStation s = new RadioStation("WMIX", "Magic", 98.5,
                StationFormat.TOP_40);
        Random rng = new Random(7L);
        s.tick(SIM_DATE, rng);
        Song first = s.getCurrentSong();
        // Tick through the entire window; the (timer+1)th tick should rotate.
        for (int i = 0; i < SimConstants.RADIO_SONG_MINUTES; i++) {
            s.tick(SIM_DATE, rng);
        }
        Song after = s.getCurrentSong();
        assertNotNull(after);
        // The pool is large enough that with high probability the
        // selection is different; if equal, retry once before failing.
        if (first.equals(after)) {
            for (int i = 0; i < SimConstants.RADIO_SONG_MINUTES; i++) {
                s.tick(SIM_DATE, rng);
            }
            after = s.getCurrentSong();
        }
        assertNotSame(first, after,
                "Expected a different song after the rotation window");
    }

    @Test
    @DisplayName("OLDIES_RANDOM picks weeks strictly before the sim date")
    void testOldiesUsesPastWeeks() {
        RadioStation s = new RadioStation("KOLD", "Classic", 100.7,
                StationFormat.OLDIES_RANDOM);
        Random rng = new Random(123L);
        for (int i = 0; i < 100; i++) {
            s.resetSong();
            s.tick(SIM_DATE, rng);
            Song song = s.getCurrentSong();
            assertNotNull(song);
            assertTrue(song.getChartWeek().isBefore(SIM_DATE),
                    "Oldies song chart week " + song.getChartWeek()
                            + " is not strictly before " + SIM_DATE);
        }
    }

    @Test
    @DisplayName("OLDIES_RANDOM never plays songs newer than the age floor")
    void testOldiesRespectsMinimumAge() {
        RadioStation s = new RadioStation("KOLD", "Classic", 100.7,
                StationFormat.OLDIES_RANDOM);
        Random rng = new Random(2024L);
        LocalDate newest =
                SIM_DATE.minusYears(SimConstants.RADIO_OLDIES_MIN_AGE_YEARS);
        for (int i = 0; i < 200; i++) {
            s.resetSong();
            s.tick(SIM_DATE, rng);
            Song song = s.getCurrentSong();
            assertNotNull(song);
            assertTrue(song.getChartWeek().isBefore(newest),
                    "Oldies song chart week " + song.getChartWeek()
                            + " is newer than the " 
                            + SimConstants.RADIO_OLDIES_MIN_AGE_YEARS
                            + "-year floor (" + newest + ")");
        }
    }

    @Test
    @DisplayName("TOP_40 weighting biases selection toward low chart positions")
    void testTop40Weighting() {
        RadioStation s = new RadioStation("WTOP", "Hot", 92.3,
                StationFormat.TOP_40);
        Random rng = new Random(456L);
        Map<Integer, Integer> positionCounts = new HashMap<>();
        int trials = 1000;
        for (int i = 0; i < trials; i++) {
            s.resetSong();
            s.tick(SIM_DATE, rng);
            Song song = s.getCurrentSong();
            assertNotNull(song);
            positionCounts.merge(song.getPosition(), 1, Integer::sum);
        }
        int topTen = 0;
        int bottomTen = 0; // positions 31..40
        for (Map.Entry<Integer, Integer> e : positionCounts.entrySet()) {
            if (e.getKey() <= 10) {
                topTen += e.getValue();
            } else if (e.getKey() >= 31 && e.getKey() <= 40) {
                bottomTen += e.getValue();
            }
        }
        assertTrue(topTen > bottomTen,
                "Top 10 should be picked more often than positions 31-40 "
                        + "(top10=" + topTen + ", bottom10=" + bottomTen + ")");
    }

    @Test
    @DisplayName("A station never replays a song within its no-repeat window")
    void testNoRepeatWithinWindow() {
        RadioStation s = new RadioStation("WTOP", "Hot", 92.3,
                StationFormat.TOP_40);
        Random rng = new Random(99L);
        int window = s.getStationType().noRepeatWindow();
        java.util.List<String> sequence = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            s.resetSong();
            s.tick(SIM_DATE, rng);
            Song song = s.getCurrentSong();
            assertNotNull(song);
            sequence.add(song.getTitle() + "|" + song.getPerformer());
        }
        for (int i = 0; i < sequence.size(); i++) {
            for (int j = Math.max(0, i - window); j < i; j++) {
                assertNotEquals(sequence.get(j), sequence.get(i),
                        "Song at index " + i + " (" + sequence.get(i)
                                + ") repeats within the last " + window
                                + " selections");
            }
        }
    }

    @Test
    @DisplayName("Stations with different formats produce different song pools")
    void testDifferentFormatsDifferentPools() {
        RadioStation top = new RadioStation("WTOP", "Hot", 92.3,
                StationFormat.TOP_40);
        RadioStation oldies = new RadioStation("KOLD", "Classic", 100.7,
                StationFormat.OLDIES_RANDOM);
        Random rng = new Random(789L);
        Set<LocalDate> topWeeks = new HashSet<>();
        Set<LocalDate> oldiesWeeks = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            top.resetSong();
            top.tick(SIM_DATE, rng);
            topWeeks.add(top.getCurrentSong().getChartWeek());
            oldies.resetSong();
            oldies.tick(SIM_DATE, rng);
            oldiesWeeks.add(oldies.getCurrentSong().getChartWeek());
        }
        // Top 40 always pulls from the same chart week (the most recent
        // Saturday on or before SIM_DATE), oldies samples the deep history.
        assertEquals(1, topWeeks.size(),
                "TOP_40 should always pull from the same week, got "
                        + topWeeks);
        assertTrue(oldiesWeeks.size() > 1,
                "OLDIES_RANDOM should sample multiple weeks, got "
                        + oldiesWeeks);
    }
}
