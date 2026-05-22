package entity.Radio;

import constants.SimConstants;
import entity.Time;
import entity.TransitMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Radio commute listening")
class RadioCommuteTest {

    private static Radio buildDialWithOldies() {
        List<RadioStation> stations = new ArrayList<>();
        stations.add(new RadioStation("WTOP", "Hot", 92.3, StationFormat.TOP_40));
        stations.add(new RadioStation("WMIX", "Mix", 96.1, StationFormat.MIX));
        stations.add(new RadioStation("KOLD", "Classic", 100.7, StationFormat.OLDIES_RANDOM));
        stations.add(new RadioStation("KACU", "The River", 104.5,
                StationFormat.ADULT_CONTEMPORARY));
        return new Radio(stations);
    }

    @Test
    @DisplayName("playsRadioDuringCommute is true for bus, drive, and carpool only")
    void testPlaysRadioDuringCommute() {
        assertTrue(Radio.playsRadioDuringCommute(TransitMode.BUS));
        assertTrue(Radio.playsRadioDuringCommute(TransitMode.DRIVE));
        assertTrue(Radio.playsRadioDuringCommute(TransitMode.CARPOOL));
        assertFalse(Radio.playsRadioDuringCommute(TransitMode.WALK));
    }

    @Test
    @DisplayName("pickStationForCommute returns null for walkers")
    void testWalkersDoNotPickStation() {
        Radio radio = buildDialWithOldies();
        assertNull(radio.pickStationForCommute(TransitMode.WALK, new Random(1L)));
    }

    @Test
    @DisplayName("formatCommuteListeningEntry includes station dial name and song")
    void testFormatCommuteListeningEntry() {
        RadioStation station = new RadioStation("WKRP", "The Mix", 94.3,
                StationFormat.TOP_40);
        Song song = new Song(LocalDate.of(2004, 7, 24), 1,
                "Confessions Part II", "Usher", 1, 13);
        String entry = Radio.formatCommuteListeningEntry(station, song);
        assertNotNull(entry);
        assertTrue(entry.contains("94.3 The Mix - WKRP-FM"));
        assertTrue(entry.contains("Confessions Part II"));
        assertTrue(entry.contains("Usher"));
    }

    @Test
    @DisplayName("Car commuters rarely pick the oldies station over many trials")
    void testCarCommutersAvoidOldies() {
        Radio radio = buildDialWithOldies();
        Map<String, Integer> counts = new HashMap<>();
        int trials = 5000;
        for (int i = 0; i < trials; i++) {
            RadioStation picked = radio.pickStationForCommute(
                    TransitMode.DRIVE, new Random(i));
            counts.merge(picked.getCallSign(), 1, Integer::sum);
        }
        int oldies = counts.getOrDefault("KOLD", 0);
        double oldiesRate = oldies / (double) trials;
        assertTrue(oldiesRate < 0.08,
                "Car oldies rate should be well below uniform (~25%), got "
                        + oldiesRate);
    }

    @Test
    @DisplayName("Bus commuters pick oldies at roughly uniform rate")
    void testBusCommutersPickOldiesNearUniform() {
        Radio radio = buildDialWithOldies();
        int oldies = 0;
        int trials = 5000;
        for (int i = 0; i < trials; i++) {
            RadioStation picked = radio.pickStationForCommute(
                    TransitMode.BUS, new Random(i + 999));
            if (picked.getFormat() == StationFormat.OLDIES_RANDOM) {
                oldies++;
            }
        }
        double oldiesRate = oldies / (double) trials;
        assertTrue(oldiesRate > 0.15 && oldiesRate < 0.35,
                "Bus oldies rate should cluster near 25%, got " + oldiesRate);
    }

    @Test
    @DisplayName("Car oldies weight is lower than the default station weight")
    void testWeightConstants() {
        assertTrue(SimConstants.RADIO_COMMUTE_OLDIES_WEIGHT_CAR
                < SimConstants.RADIO_COMMUTE_STATION_WEIGHT);
    }

    @Test
    @DisplayName("First station tick is not reported as a song change")
    void testInitialTuneInIsNotSongChange() {
        RadioStation station = new RadioStation("WTOP", "Hot", 92.3,
                StationFormat.TOP_40);
        Radio radio = new Radio(List.of(station));
        Time time = new Time();
        List<RadioStation> changed =
                radio.tickAndCollectSongChanges(time, new Random(1L));
        assertTrue(changed.isEmpty());
        assertNotNull(station.getCurrentSong());
    }

    @Test
    @DisplayName("tickAndCollectSongChanges reports a station after its song rotates")
    void testDetectsSongRotation() {
        RadioStation station = new RadioStation("WTOP", "Hot", 92.3,
                StationFormat.TOP_40);
        Radio radio = new Radio(List.of(station));
        Time time = new Time();
        Random rng = new Random(42L);
        radio.tickAndCollectSongChanges(time, rng);
        Song first = station.getCurrentSong();
        assertNotNull(first);

        List<RadioStation> changed = List.of();
        for (int i = 0; i < SimConstants.RADIO_SONG_MINUTES + 2; i++) {
            time.stepForwardMinutes(1);
            changed = radio.tickAndCollectSongChanges(time, rng);
            if (!changed.isEmpty()) {
                break;
            }
        }
        assertFalse(changed.isEmpty());
        assertEquals(station, changed.get(0));
        assertNotNull(station.getCurrentSong());
        assertFalse(first.equals(station.getCurrentSong()));
    }
}
