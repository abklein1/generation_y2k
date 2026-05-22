package utility;

import constants.SimConstants;
import entity.Radio.Radio;
import entity.Radio.RadioStation;
import entity.Radio.StationFormat;

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
 *       station; remaining slots draw from the remaining formats
 *       without repeats, with {@code TOP_40} prioritized.</li>
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
        List<StationFormat> formats = pickFormats(stationCount, random);
        Set<String> usedCallSigns = new HashSet<>();
        Set<String> usedNicknames = new HashSet<>();

        List<RadioStation> stations = new ArrayList<>(stationCount);
        for (int i = 0; i < stationCount; i++) {
            String callSign = generateUniqueCallSign(prefix, usedCallSigns, random);
            String nickname =
                    RadioNicknameLoader.pickRandomNickname(random, usedNicknames);
            if (nickname == null) {
                nickname = formats.get(i).displayLabel();
            }
            usedNicknames.add(nickname);
            stations.add(new RadioStation(callSign, nickname,
                    frequencies.get(i), formats.get(i)));
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
     * Choose formats for {@code n} stations. Always includes one
     * {@link StationFormat#OLDIES_RANDOM}; if {@code n >= 2} also
     * always includes {@link StationFormat#TOP_40}. Remaining slots
     * draw without replacement from the remaining formats.
     */
    static List<StationFormat> pickFormats(int n, Random random) {
        List<StationFormat> chosen = new ArrayList<>(n);
        chosen.add(StationFormat.OLDIES_RANDOM);
        if (n >= 2) {
            chosen.add(StationFormat.TOP_40);
        }
        List<StationFormat> remaining = new ArrayList<>();
        for (StationFormat f : StationFormat.values()) {
            if (!chosen.contains(f)) {
                remaining.add(f);
            }
        }
        Collections.shuffle(remaining, random);
        while (chosen.size() < n && !remaining.isEmpty()) {
            chosen.add(remaining.remove(0));
        }
        // If we still need more (n > total formats), repeat the pool.
        while (chosen.size() < n) {
            chosen.add(StationFormat.MIX);
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
