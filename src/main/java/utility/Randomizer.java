package utility;

/**
 * Utility class for random number generation.
 * Now delegates to GameRandom for seedable, reproducible random generation.
 */
public class Randomizer {
    /**
     * Get a random integer between min and max (inclusive).
     * 
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Random integer in range [min, max]
     */
    public static Integer setRandom(int min, int max) {
        return GameRandom.nextInt(min, max);
    }
}
