package utility;

import view.GameView;

import java.util.EnumMap;

/**
 * Centralized logging utility for the game.
 * Provides toggleable message categories that route output to the internal
 * console.
 * 
 * Categories are automatically configured based on game mode:
 * - New Game: Only STORY messages are enabled (to preserve discovery for
 * players)
 * - New Simulation: All categories are enabled (for debugging and development)
 */
public class GameLogger {

    /**
     * Message categories for filtering log output.
     */
    public enum Category {
        /** Student, teacher, and room generation messages */
        GENERATION,
        /** Friendship, sibling, and relationship connection messages */
        SOCIAL_LINKS,
        /** Class scheduling and student schedule assignment messages */
        SCHEDULING,
        /** Story and narrative messages (always enabled by default) */
        STORY,
        /** General debug messages and error logging */
        DEBUG
    }

    private static GameView view;
    private static final EnumMap<Category, Boolean> categoryEnabled = new EnumMap<>(Category.class);
    private static boolean initialized = false;

    // Static initialization - all categories disabled by default
    static {
        for (Category category : Category.values()) {
            categoryEnabled.put(category, false);
        }
    }

    /**
     * Initialize the logger with a GameView reference and configure categories
     * based on game mode.
     * This should only be called on the FIRST run to set defaults.
     * 
     * @param gameView   The GameView instance for output
     * @param isGameMode true for New Game mode (minimal logging), false for New
     *                   Simulation (full logging)
     */
    public static void initialize(GameView gameView, boolean isGameMode) {
        view = gameView;
        initialized = true;

        if (isGameMode) {
            // New Game mode: Only story messages for player experience
            categoryEnabled.put(Category.GENERATION, false);
            categoryEnabled.put(Category.SOCIAL_LINKS, false);
            categoryEnabled.put(Category.SCHEDULING, false);
            categoryEnabled.put(Category.STORY, true);
            categoryEnabled.put(Category.DEBUG, false);
        } else {
            // New Simulation mode: All logging enabled for debugging
            for (Category category : Category.values()) {
                categoryEnabled.put(category, true);
            }
        }
    }

    /**
     * Set the view reference only, without changing category settings.
     * Use this for subsequent runs to preserve user's debug menu selections.
     * 
     * @param gameView The GameView instance for output
     */
    public static void setView(GameView gameView) {
        view = gameView;
        initialized = true;
    }

    /**
     * Log a message under a specific category.
     * The message will only be output if the category is enabled.
     * 
     * @param category The message category
     * @param message  The message to log
     */
    public static void log(Category category, String message) {
        if (!initialized || view == null) {
            // Fallback: if not initialized, don't output anything
            return;
        }

        if (categoryEnabled.getOrDefault(category, false)) {
            view.appendOutput(message);
        }
    }

    /**
     * Enable or disable a specific category.
     * 
     * @param category The category to modify
     * @param enabled  true to enable, false to disable
     */
    public static void setEnabled(Category category, boolean enabled) {
        categoryEnabled.put(category, enabled);
    }

    /**
     * Check if a category is currently enabled.
     * 
     * @param category The category to check
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled(Category category) {
        return categoryEnabled.getOrDefault(category, false);
    }

    /**
     * Enable all logging categories.
     */
    public static void enableAll() {
        for (Category category : Category.values()) {
            categoryEnabled.put(category, true);
        }
    }

    /**
     * Disable all logging categories except STORY.
     */
    public static void disableAllExceptStory() {
        for (Category category : Category.values()) {
            categoryEnabled.put(category, category == Category.STORY);
        }
    }

    // ===== Convenience Methods =====

    /**
     * Log a generation message (student, teacher, room generation).
     * 
     * @param message The message to log
     */
    public static void logGeneration(String message) {
        log(Category.GENERATION, message);
    }

    /**
     * Log a social link message (friendships, siblings, relationships).
     * 
     * @param message The message to log
     */
    public static void logSocialLinks(String message) {
        log(Category.SOCIAL_LINKS, message);
    }

    /**
     * Log a scheduling message (class and student scheduling).
     * 
     * @param message The message to log
     */
    public static void logScheduling(String message) {
        log(Category.SCHEDULING, message);
    }

    /**
     * Log a story message (narrative content).
     * 
     * @param message The message to log
     */
    public static void logStory(String message) {
        log(Category.STORY, message);
    }

    /**
     * Log a debug message (general debugging and errors).
     * 
     * @param message The message to log
     */
    public static void logDebug(String message) {
        log(Category.DEBUG, message);
    }

    /**
     * Check if the logger has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Reset the logger to uninitialized state (useful for testing).
     */
    public static void reset() {
        view = null;
        initialized = false;
        for (Category category : Category.values()) {
            categoryEnabled.put(category, false);
        }
    }
}
