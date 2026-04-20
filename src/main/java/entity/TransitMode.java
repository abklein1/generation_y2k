package entity;

/**
 * How a student commutes between their neighborhood and school.
 * Speed values are effective averages that account for stops, traffic, etc.
 */
public enum TransitMode {

    WALK("Walking", 3.0),
    BUS("School Bus", 15.0),
    DRIVE("Driving", 30.0),
    CARPOOL("Carpool", 25.0);

    private final String displayName;
    private final double speedMph;

    TransitMode(String displayName, double speedMph) {
        this.displayName = displayName;
        this.speedMph = speedMph;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getSpeedMph() {
        return speedMph;
    }

    /**
     * Calculates travel time in minutes for a given distance.
     *
     * @param distanceMiles the distance in miles
     * @return travel time in whole minutes (rounded up)
     */
    public int getTravelTimeMinutes(int distanceMiles) {
        if (distanceMiles <= 0) {
            return 0;
        }
        return (int) Math.ceil((distanceMiles / speedMph) * 60.0);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
