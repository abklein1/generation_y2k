package utility;

import java.util.Set;

/**
 * Picks a regional location key for the simulation based on keywords in
 * the school name. Centralizes the mapping that previously lived inside
 * the {@code Weather} constructor so that other systems (e.g. the radio
 * generator) can share the same E/W classification.
 *
 * <p>Region keys correspond to the classpath CSV files under
 * {@code /Resources/Weather/}.</p>
 */
public final class LocationSelector {

    public static final String ALASKA = "alaska";
    public static final String AUSTIN = "austin";
    public static final String CHICAGO = "chicago";
    public static final String KANSAS = "kansas";
    public static final String LOS_ANGELES = "los_angeles";
    public static final String MACON = "macon";
    public static final String NEW_YORK = "new_york";
    public static final String PHOENIX = "phoenix";
    public static final String SPOKANE = "spokane";
    public static final String WEST_PALM = "west_palm";

    private static final Set<String> EAST_OF_MISSISSIPPI =
            Set.of(CHICAGO, MACON, NEW_YORK, WEST_PALM);

    private static final Set<String> WEST_OF_MISSISSIPPI =
            Set.of(ALASKA, AUSTIN, KANSAS, LOS_ANGELES, PHOENIX, SPOKANE);

    private LocationSelector() {
    }

    /**
     * Pick a region key based on keywords in the school name. Mirrors the
     * historical logic from {@code Weather(String schoolName)}.
     *
     * @param schoolName the generated school name
     * @return one of the {@link #ALASKA}, {@link #AUSTIN}, etc. constants
     */
    public static String pick(String schoolName) {
        String[] locations;
        if (schoolName.contains("Forest") || schoolName.contains("Poplar") || schoolName.contains("Mountain") ||
                schoolName.contains("Summit") || schoolName.contains("Peak")) {
            locations = new String[]{ALASKA, SPOKANE, NEW_YORK};
            return locations[GameRandom.nextInt(0, 2)];
        } else if (schoolName.contains("Ocean") || schoolName.contains("Sea") || schoolName.contains("Bay") ||
                schoolName.contains("Cape") || schoolName.contains("Shore") || schoolName.contains("Sound") ||
                schoolName.contains("Port") || schoolName.contains("Palm") || schoolName.contains("Palmetto")) {
            locations = new String[]{ALASKA, SPOKANE, LOS_ANGELES, WEST_PALM, NEW_YORK};
            return locations[GameRandom.nextInt(0, 4)];
        } else if (schoolName.contains("Prairie") || schoolName.contains("Valley") || schoolName.contains("Grande") ||
                schoolName.contains("Grand") || schoolName.contains("Lake")) {
            locations = new String[]{AUSTIN, KANSAS};
            return locations[GameRandom.nextInt(0, 1)];
        } else if (schoolName.contains("Desert") || schoolName.contains("Canyon")) {
            locations = new String[]{PHOENIX, LOS_ANGELES};
            return locations[GameRandom.nextInt(0, 1)];
        } else {
            locations = new String[]{ALASKA, AUSTIN, KANSAS, LOS_ANGELES, MACON,
                    NEW_YORK, PHOENIX, SPOKANE, WEST_PALM};
            return locations[GameRandom.nextInt(0, 8)];
        }
    }

    /**
     * @param region a region key returned by {@link #pick(String)}
     * @return true if the region is geographically east of the Mississippi River.
     */
    public static boolean isEastOfMississippi(String region) {
        if (EAST_OF_MISSISSIPPI.contains(region)) {
            return true;
        }
        if (WEST_OF_MISSISSIPPI.contains(region)) {
            return false;
        }
        throw new IllegalArgumentException("Unknown region: " + region);
    }
}
