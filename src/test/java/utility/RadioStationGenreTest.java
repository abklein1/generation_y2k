package utility;

import constants.SimConstants;
import entity.Radio.RadioStation;
import entity.Radio.StationFormat;
import entity.Radio.StationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RadioStationGenerator station-type mix")
class RadioStationGenreTest {

    @Test
    @DisplayName("pickStationTypes returns exactly n types with one Oldies + Top 40")
    void testPickStationTypesStructure() {
        for (int n = SimConstants.RADIO_MIN_STATIONS;
                n <= SimConstants.RADIO_MAX_STATIONS; n++) {
            List<StationType> types =
                    RadioStationGenerator.pickStationTypes(n, new Random(n));
            assertEquals(n, types.size(), "wrong count for n=" + n);

            long oldies = types.stream()
                    .filter(t -> t.getFormat() == StationFormat.OLDIES_RANDOM)
                    .count();
            assertEquals(1, oldies, "expected exactly one Oldies for n=" + n);

            boolean hasTop40 = types.stream()
                    .anyMatch(t -> t.getFormat() == StationFormat.TOP_40);
            assertTrue(hasTop40, "expected a Top 40 station for n=" + n);
        }
    }

    @Test
    @DisplayName("Genre stations appear across generated dials")
    void testGenreStationsAppear() {
        boolean sawGenre = false;
        for (long seed = 0; seed < 30 && !sawGenre; seed++) {
            sawGenre = RadioStationGenerator.pickStationTypes(8, new Random(seed))
                    .stream().anyMatch(StationType::isGenreStation);
        }
        assertTrue(sawGenre,
                "genre stations should show up on at least one 8-station dial");
    }

    @Test
    @DisplayName("Generated rosters honor the 5-8 station bounds")
    void testGeneratedRosterCount() {
        for (long seed = 0; seed < 25; seed++) {
            int count = RadioStationGenerator
                    .generate("Generic High", new Random(seed))
                    .getStations().size();
            assertTrue(count >= SimConstants.RADIO_MIN_STATIONS
                            && count <= SimConstants.RADIO_MAX_STATIONS,
                    "roster size " + count + " out of bounds for seed " + seed);
        }
    }

    @Test
    @DisplayName("Station types within a roster have distinct labels")
    void testDistinctLabels() {
        List<RadioStation> stations = RadioStationGenerator
                .generate("Generic High", new Random(9L)).getStations();
        Set<String> labels = new HashSet<>();
        for (RadioStation s : stations) {
            assertTrue(labels.add(s.getStationType().getLabel()),
                    "duplicate station-type label: "
                            + s.getStationType().getLabel());
        }
    }
}
