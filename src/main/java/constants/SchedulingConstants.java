package constants;

/**
 * Constants for the optimized scheduling system
 */
public final class SchedulingConstants {
    
    // Minimum enrollment constraints
    public static final int MIN_CLASS_SIZE = 12;
    public static final int MIN_ELECTIVE_SIZE = 8;
    public static final int MIN_AP_CLASS_SIZE = 15;
    
    // Optimal class size targets
    public static final int OPTIMAL_CLASS_SIZE_RATIO = 80; // Percentage of room capacity
    public static final int MAX_CLASS_SIZE_RATIO = 95;     // Maximum before overflow
    
    // Load balancing parameters
    public static final int MAX_OPTIMIZATION_ATTEMPTS = 5;
    public static final double BALANCE_THRESHOLD = 0.8;    // Ratio for considering balanced
    public static final int PRIORITY_ADJUSTMENT_ROUNDS = 2;
    
    // Scheduling priorities
    public static final int CORE_REQUIREMENT_PRIORITY = 1;
    public static final int GRADUATION_REQUIREMENT_PRIORITY = 2;
    public static final int ADVANCED_PLACEMENT_PRIORITY = 3;
    public static final int ELECTIVE_PRIORITY = 4;
    
    // Grade level preferences for scheduling order
    public static final int SENIOR_SCHEDULING_PRIORITY = 1;
    public static final int JUNIOR_SCHEDULING_PRIORITY = 2;
    public static final int SOPHOMORE_SCHEDULING_PRIORITY = 3;
    public static final int FRESHMAN_SCHEDULING_PRIORITY = 4;
    
    // Period distribution preferences
    public static final boolean BALANCE_ACROSS_PERIODS = true;
    public static final boolean PREFER_EARLY_PERIODS_FOR_CORE = true;
    public static final boolean AVOID_BACK_TO_BACK_HEAVY_SUBJECTS = true;
    
    // Advanced scheduling options
    public static final boolean ENABLE_SECTION_CONSOLIDATION = true;
    public static final boolean ENABLE_CROSS_PERIOD_BALANCING = true;
    public static final boolean ENABLE_TEACHER_PREFERENCE_MATCHING = false;
    
    // Performance tuning
    public static final int MAX_STUDENTS_PER_BATCH = 50;
    public static final int SCHEDULING_TIMEOUT_SECONDS = 30;
    public static final boolean ENABLE_PARALLEL_PROCESSING = false;
    
    // Analytics and reporting
    public static final boolean ENABLE_DETAILED_STATISTICS = true;
    public static final boolean GENERATE_CONFLICT_REPORTS = true;
    public static final boolean TRACK_OPTIMIZATION_METRICS = true;
    
    private SchedulingConstants() {
        // Prevent instantiation
    }
} 