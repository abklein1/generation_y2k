package entity.Radio;

import constants.SimConstants;
import entity.Time;
import entity.TransitMode;
import utility.GameRandom;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Container for the FM radio stations broadcasting in a given town /
 * school region. Owned by world state ({@code Town} or {@code SchoolController})
 * and ticked once per simulated minute.
 *
 * <p>Maintains a derived RNG seeded off the world seed so that DJ
 * selections are deterministic per save and do not perturb the main
 * {@code GameRandom} stream used by world generation.</p>
 */
public final class Radio implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final long RADIO_SEED_SALT = 0xDA7AC0DEL;

    private final List<RadioStation> stations;

    private transient Random rng;

    public Radio(List<RadioStation> stations) {
        this.stations = new ArrayList<>(stations);
    }

    /**
     * @return immutable view of all stations.
     */
    public List<RadioStation> getStations() {
        return Collections.unmodifiableList(stations);
    }

    /**
     * @param frequencyMhz exact tuned frequency
     * @return the station broadcasting at that frequency, or {@code null}.
     */
    public RadioStation getStation(double frequencyMhz) {
        for (RadioStation s : stations) {
            if (Double.compare(s.getFrequencyMhz(), frequencyMhz) == 0) {
                return s;
            }
        }
        return null;
    }

    /**
     * Picks a station for a student listening during a bus or car commute.
     * Car and carpool riders are much less likely to tune to an oldies
     * station than bus riders.
     *
     * @param mode how the student is commuting
     * @param rng  RNG for the per-student roll
     * @return a station from this dial, or {@code null} if none exist
     */
    public RadioStation pickStationForCommute(TransitMode mode, Random rng) {
        if (stations.isEmpty() || mode == null || rng == null) {
            return null;
        }
        if (!playsRadioDuringCommute(mode)) {
            return null;
        }
        boolean inCar = mode == TransitMode.DRIVE || mode == TransitMode.CARPOOL;
        long totalWeight = 0L;
        for (RadioStation station : stations) {
            totalWeight += commuteSelectionWeight(station, inCar);
        }
        if (totalWeight <= 0L) {
            return stations.get(rng.nextInt(stations.size()));
        }
        long target = (long) (rng.nextDouble() * totalWeight);
        long running = 0L;
        for (RadioStation station : stations) {
            running += commuteSelectionWeight(station, inCar);
            if (running > target) {
                return station;
            }
        }
        return stations.get(stations.size() - 1);
    }

    /**
     * @return true when this commute mode typically has a radio on
     *         (bus, personal car, or carpool — not walking).
     */
    public static boolean playsRadioDuringCommute(TransitMode mode) {
        return mode == TransitMode.BUS
                || mode == TransitMode.DRIVE
                || mode == TransitMode.CARPOOL;
    }

    /**
     * Builds a log line describing what a student hears on the dial.
     */
    public static String formatCommuteListeningEntry(RadioStation station, Song song) {
        if (station == null || song == null) {
            return null;
        }
        return "Listening to " + station.displayName() + ": " + song;
    }

    private static int commuteSelectionWeight(RadioStation station, boolean inCar) {
        if (inCar && station.getFormat() == StationFormat.OLDIES_RANDOM) {
            return SimConstants.RADIO_COMMUTE_OLDIES_WEIGHT_CAR;
        }
        return SimConstants.RADIO_COMMUTE_STATION_WEIGHT;
    }

    /**
     * Advance every station by one simulated minute and return any stations
     * whose song actually changed (initial tune-in is excluded).
     *
     * @param time current sim time
     * @return stations that rotated to a new song this tick
     */
    public List<RadioStation> tickAndCollectSongChanges(Time time) {
        return tickAndCollectSongChanges(time, rng());
    }

    /**
     * Tick variant that accepts an explicit RNG (useful for tests).
     *
     * @param time current sim time
     * @param rng  RNG used by date-aware formats
     * @return stations that rotated to a new song this tick
     */
    public List<RadioStation> tickAndCollectSongChanges(Time time, Random rng) {
        if (time == null) {
            return Collections.emptyList();
        }
        LocalDate now = time.getCurrentDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        List<RadioStation> changed = new ArrayList<>();
        for (RadioStation station : stations) {
            Song before = station.getCurrentSong();
            station.tick(now, rng);
            Song after = station.getCurrentSong();
            if (before != null && after != null && !before.equals(after)) {
                changed.add(station);
            }
        }
        return changed;
    }

    /**
     * Advance every station by one simulated minute. Uses the radio's
     * own derived RNG so song-selection rolls do not consume bits from
     * {@link GameRandom}.
     *
     * @param time current sim time
     */
    public void tick(Time time) {
        tick(time, rng());
    }

    /**
     * Tick variant that accepts an explicit RNG (useful for tests).
     *
     * @param time current sim time
     * @param rng  RNG used by date-aware formats
     */
    public void tick(Time time, Random rng) {
        if (time == null) {
            return;
        }
        LocalDate now = time.getCurrentDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        for (RadioStation s : stations) {
            s.tick(now, rng);
        }
    }

    private Random rng() {
        if (rng == null) {
            long seed = GameRandom.isInitialized()
                    ? GameRandom.getSeed() ^ RADIO_SEED_SALT
                    : System.currentTimeMillis();
            rng = new Random(seed);
        }
        return rng;
    }
}
