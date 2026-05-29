package utility;

import constants.SimConstants;
import entity.Radio.Radio;
import entity.Radio.RadioStation;
import entity.Radio.StationFormat;
import entity.Radio.StationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Builds the per-game roster of FM radio stations.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Decide how many stations exist (between
 *       {@link SimConstants#RADIO_MIN_STATIONS} and
 *       {@link SimConstants#RADIO_MAX_STATIONS}).</li>
 *   <li>Determine the W/K call-sign prefix from the school name's
 *       region via {@link LocationSelector}.</li>
 *   <li>Allocate non-overlapping odd-tenths frequencies on the
 *       U.S. FM grid.</li>
 *   <li>Assign exactly one {@link StationFormat#OLDIES_RANDOM}
 *       station and (for {@code n >= 2}) one {@code TOP_40} station, then
 *       fill the remaining slots with a random mix drawn from the
 *       {@link StationType#catalog()} (broad formats and genre stations)
 *       without repeats.</li>
 *   <li>Pull a unique nickname per station from
 *       {@link RadioNicknameLoader}.</li>
 * </ul>
 */
public final class RadioStationGenerator {

    private RadioStationGenerator() {
    }

    /**
     * Generate a fresh radio roster for the given school name.
     *
     * @param schoolName generated school name (used to pick region)
     * @param random     RNG used for all selections (typically {@code GameRandom})
     * @return a populated {@link Radio} container
     */
    public static Radio generate(String schoolName, Random random) {
        String region = LocationSelector.pick(schoolName);
        boolean east = LocationSelector.isEastOfMississippi(region);
        char prefix = east ? 'W' : 'K';

        int minStations = SimConstants.RADIO_MIN_STATIONS;
        int maxStations = SimConstants.RADIO_MAX_STATIONS;
        int stationCount = minStations + random.nextInt(maxStations - minStations + 1);

        List<Double> frequencies = pickFrequencies(stationCount, random);
        List<StationType> types = pickStationTypes(stationCount, random);
        Set<String> usedCallSigns = new HashSet<>();
        Set<String> usedNicknames = new HashSet<>();

        List<RadioStation> stations = new ArrayList<>(stationCount);
        for (int i = 0; i < stationCount; i++) {
            String callSign = generateUniqueCallSign(prefix, usedCallSigns, random);
            String nickname =
                    RadioNicknameLoader.pickRandomNickname(random, usedNicknames);
            if (nickname == null) {
                nickname = types.get(i).getLabel();
            }
            usedNicknames.add(nickname);
            stations.add(new RadioStation(callSign, nickname,
                    frequencies.get(i), types.get(i)));
        }
        return new Radio(stations);
    }

    /**
     * Allocate {@code n} distinct frequencies from the U.S. FM odd-tenths
     * grid (88.1, 88.3, ..., 107.9 = 100 slots).
     */
    static List<Double> pickFrequencies(int n, Random random) {
        List<Double> grid = new ArrayList<>();
        double min = SimConstants.RADIO_FREQ_MIN;
        double max = SimConstants.RADIO_FREQ_MAX;
        double step = SimConstants.RADIO_FREQ_STEP;
        for (double freq = min; freq <= max + 1e-9; freq += step) {
            grid.add(Math.round(freq * 10.0) / 10.0);
        }
        Collections.shuffle(grid, random);
        List<Double> picks = new ArrayList<>(grid.subList(0, Math.min(n, grid.size())));
        Collections.sort(picks);
        return picks;
    }

    /**
     * Choose station types for {@code n} stations. Always includes exactly one
     * {@link StationFormat#OLDIES_RANDOM} station; if {@code n >= 2} also
     * always includes a {@code TOP_40} station. Remaining slots draw without
     * replacement from the rest of the {@link StationType#catalog()} (other
     * broad formats and genre stations), giving each game a varied dial.
     *
     * @param n      number of stations to fill
     * @param random RNG for selection
     * @return a shuffled list of exactly {@code n} station types
     */
    static List<StationType> pickStationTypes(int n, Random random) {
        List<StationType> chosen = new ArrayList<>(n);
        chosen.add(StationType.oldies());
        if (n >= 2) {
            chosen.add(StationType.top40());
        }

        // Everything else in the catalog is fair game for the remaining slots.
        List<StationType> remaining = new ArrayList<>();
        for (StationType t : StationType.catalog()) {
            StationFormat f = t.getFormat();
            if (f == StationFormat.OLDIES_RANDOM || f == StationFormat.TOP_40) {
                continue; // already guaranteed above; avoid duplicates
            }
            remaining.add(t);
        }
        Collections.shuffle(remaining, random);

        int idx = 0;
        while (chosen.size() < n && idx < remaining.size()) {
            chosen.add(remaining.get(idx++));
        }
        // Defensive: if the catalog is ever smaller than n, pad with Mix.
        while (chosen.size() < n) {
            chosen.add(StationType.broad("Mix", StationFormat.MIX));
        }
        Collections.shuffle(chosen, random);
        return chosen;
    }

    /**
     * Build a unique 4-character call sign of the form
     * {@code prefix + 3 letters} (e.g. {@code WXYZ}).
     */
    static String generateUniqueCallSign(char prefix, Set<String> used,
                                         Random random) {
        // 26^3 = 17576 possible suffixes - effectively unbounded for our needs.
        for (int attempt = 0; attempt < 200; attempt++) {
            char a = (char) ('A' + random.nextInt(26));
            char b = (char) ('A' + random.nextInt(26));
            char c = (char) ('A' + random.nextInt(26));
            String candidate = "" + prefix + a + b + c;
            if (used.add(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Unable to generate a unique call sign with prefix " + prefix);
    }
}
