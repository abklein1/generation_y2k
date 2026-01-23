package utility;

import java.util.Random;

/**
 * Centralized random number generator that supports seeding for reproducible game generation.
 * All random generation in the game should go through this class to ensure that providing
 * the same seed produces the same world/school.
 *
 * Usage:
 * - Call GameRandom.initialize() or GameRandom.initialize(seed) at game start
 * - Use GameRandom.nextInt(), nextDouble(), nextGaussian(), etc. throughout the game
 * - Save GameRandom.getSeed() to recreate the same world later
 */
public class GameRandom {
    private static Random random;
    private static long currentSeed;
    private static boolean initialized = false;

    /**
     * Initialize with a random seed (based on current time).
     * @return The generated seed (save this for recreating the same world)
     */
    public static long initialize() {
        currentSeed = System.currentTimeMillis();
        random = new Random(currentSeed);
        initialized = true;
        return currentSeed;
    }

    /**
     * Initialize with a specific seed to recreate a previous world.
     * @param seed The seed to use
     */
    public static void initialize(long seed) {
        currentSeed = seed;
        random = new Random(seed);
        initialized = true;
    }

    /**
     * Get the current seed. Save this value to recreate the same world later.
     * @return The current seed
     */
    public static long getSeed() {
        ensureInitialized();
        return currentSeed;
    }

    /**
     * Check if the random generator has been initialized.
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Reset the generator (for testing or starting a new game).
     */
    public static void reset() {
        random = null;
        currentSeed = 0;
        initialized = false;
    }

    /**
     * Get a random integer between min and max (inclusive).
     * This replaces Randomizer.setRandom() calls.
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Random integer in range [min, max]
     */
    public static int nextInt(int min, int max) {
        ensureInitialized();
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Get a random integer from 0 (inclusive) to bound (exclusive).
     * @param bound Upper bound (exclusive)
     * @return Random integer in range [0, bound)
     */
    public static int nextInt(int bound) {
        ensureInitialized();
        return random.nextInt(bound);
    }

    /**
     * Get a random long between min and max (inclusive).
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @return Random long in range [min, max)
     */
    public static long nextLong(long min, long max) {
        ensureInitialized();
        return min + (long) (random.nextDouble() * (max - min));
    }

    /**
     * Get a random double between 0.0 (inclusive) and 1.0 (exclusive).
     * This replaces Math.random() calls.
     * @return Random double in range [0.0, 1.0)
     */
    public static double nextDouble() {
        ensureInitialized();
        return random.nextDouble();
    }

    /**
     * Get a random double scaled to a maximum value.
     * @param max Maximum value (exclusive)
     * @return Random double in range [0.0, max)
     */
    public static double nextDouble(double max) {
        ensureInitialized();
        return random.nextDouble() * max;
    }

    /**
     * Get a Gaussian (normal) distributed random value.
     * Returns a value with mean 0 and standard deviation 1.
     * @return Random Gaussian value
     */
    public static double nextGaussian() {
        ensureInitialized();
        return random.nextGaussian();
    }

    /**
     * Get a Gaussian distributed value with specified mean and standard deviation.
     * Useful for generating stats like intelligence, charisma, etc.
     * @param mean The center of the distribution
     * @param stdDev The standard deviation
     * @return Random Gaussian value with given mean and stdDev
     */
    public static double nextGaussian(double mean, double stdDev) {
        ensureInitialized();
        return random.nextGaussian() * stdDev + mean;
    }

    /**
     * Get a random boolean value.
     * @return Random true or false
     */
    public static boolean nextBoolean() {
        ensureInitialized();
        return random.nextBoolean();
    }

    /**
     * Ensure the generator is initialized before use.
     * Auto-initializes with a random seed if not already done.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }

    /**
     * Format the seed as a user-friendly string (for display/input).
     * @return Seed as a string
     */
    public static String getSeedString() {
        return String.valueOf(currentSeed);
    }

    /**
     * Parse a seed from a string (for loading saved games).
     * @param seedString The seed string to parse
     * @return The parsed seed value
     * @throws NumberFormatException if the string is not a valid long
     */
    public static long parseSeed(String seedString) throws NumberFormatException {
        return Long.parseLong(seedString.trim());
    }
}

