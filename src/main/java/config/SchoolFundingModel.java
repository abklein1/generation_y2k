package config;

import java.io.Serializable;

/**
 * Configuration class for school funding levels that affects capacity,
 * class sizes, and resource allocation. This model enables simulation
 * of schools with different funding levels, from underfunded schools
 * that may be overcrowded to well-funded schools with smaller classes.
 */
public class SchoolFundingModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Enum representing different funding levels and their associated parameters.
     */
    public enum FundingLevel {
        /**
         * Severely underfunded - overcrowded classes, limited resources.
         * Represents schools in low-income areas with budget constraints.
         */
        SEVERELY_UNDERFUNDED(
            "Severely Underfunded",
            1.4,    // classroomCapacityModifier - more students per room
            45,     // maxClassSize - increased for larger rooms
            35,     // optimalClassSize - adjusted for overcrowding
            0.5,    // specializedRoomModifier - fewer specialized rooms
            0.04,   // staffStudentRatio - fewer staff per student
            0.7     // roomCountModifier - fewer total rooms
        ),

        /**
         * Underfunded - larger than ideal classes, some resource constraints.
         */
        UNDERFUNDED(
            "Underfunded",
            1.2,    // classroomCapacityModifier
            35,     // maxClassSize
            28,     // optimalClassSize
            0.7,    // specializedRoomModifier
            0.05,   // staffStudentRatio
            0.85    // roomCountModifier
        ),

        /**
         * Adequate funding - standard class sizes, basic resources available.
         */
        ADEQUATE(
            "Adequate",
            1.0,    // classroomCapacityModifier
            30,     // maxClassSize
            25,     // optimalClassSize
            1.0,    // specializedRoomModifier
            0.06,   // staffStudentRatio
            1.0     // roomCountModifier
        ),

        /**
         * Well funded - smaller classes, good resources.
         */
        WELL_FUNDED(
            "Well Funded",
            0.85,   // classroomCapacityModifier
            28,     // maxClassSize
            22,     // optimalClassSize
            1.2,    // specializedRoomModifier
            0.07,   // staffStudentRatio
            1.15    // roomCountModifier
        ),

        /**
         * Excellently funded - small classes, abundant resources.
         * Represents well-funded private schools or affluent public schools.
         */
        EXCELLENTLY_FUNDED(
            "Excellently Funded",
            0.7,    // classroomCapacityModifier
            25,     // maxClassSize
            18,     // optimalClassSize
            1.5,    // specializedRoomModifier
            0.08,   // staffStudentRatio
            1.3     // roomCountModifier
        );

        private final String displayName;
        private final double classroomCapacityModifier;
        private final int maxClassSize;
        private final int optimalClassSize;
        private final double specializedRoomModifier;
        private final double staffStudentRatio;
        private final double roomCountModifier;

        FundingLevel(String displayName, double classroomCapacityModifier, int maxClassSize,
                     int optimalClassSize, double specializedRoomModifier,
                     double staffStudentRatio, double roomCountModifier) {
            this.displayName = displayName;
            this.classroomCapacityModifier = classroomCapacityModifier;
            this.maxClassSize = maxClassSize;
            this.optimalClassSize = optimalClassSize;
            this.specializedRoomModifier = specializedRoomModifier;
            this.staffStudentRatio = staffStudentRatio;
            this.roomCountModifier = roomCountModifier;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getClassroomCapacityModifier() {
            return classroomCapacityModifier;
        }

        public int getMaxClassSize() {
            return maxClassSize;
        }

        public int getOptimalClassSize() {
            return optimalClassSize;
        }

        public double getSpecializedRoomModifier() {
            return specializedRoomModifier;
        }

        public double getStaffStudentRatio() {
            return staffStudentRatio;
        }

        public double getRoomCountModifier() {
            return roomCountModifier;
        }
    }

    // ==================== Instance Fields ====================

    private FundingLevel fundingLevel;
    
    /** Override for max class size (0 to use funding level default) */
    private int maxClassSizeOverride;
    
    /** Override for optimal class size (0 to use funding level default) */
    private int optimalClassSizeOverride;
    
    /** Whether to allow overcrowding beyond max class size */
    private boolean allowOvercrowding;
    
    /** Maximum overcrowding percentage (e.g., 1.2 = 120% of max capacity) */
    private double maxOvercrowdingPercent;

    // ==================== Constructors ====================

    /**
     * Creates a SchoolFundingModel with default ADEQUATE funding.
     */
    public SchoolFundingModel() {
        this(FundingLevel.ADEQUATE);
    }

    /**
     * Creates a SchoolFundingModel with the specified funding level.
     *
     * @param fundingLevel the funding level
     */
    public SchoolFundingModel(FundingLevel fundingLevel) {
        this.fundingLevel = fundingLevel;
        this.maxClassSizeOverride = 0;
        this.optimalClassSizeOverride = 0;
        this.allowOvercrowding = false; // Disabled - sibling generation can cause unexpected overcrowding
        this.maxOvercrowdingPercent = 1.0; // 100% - no overcrowding allowed
    }

    // ==================== Capacity Calculation Methods ====================

    /**
     * Gets the maximum class size for this funding model.
     *
     * @return the maximum students per class
     */
    public int getMaxClassSize() {
        return maxClassSizeOverride > 0 ? maxClassSizeOverride : fundingLevel.getMaxClassSize();
    }

    /**
     * Gets the optimal (target) class size for this funding model.
     *
     * @return the optimal students per class
     */
    public int getOptimalClassSize() {
        return optimalClassSizeOverride > 0 ? optimalClassSizeOverride : fundingLevel.getOptimalClassSize();
    }

    /**
     * Calculates the number of classrooms needed for a given student population.
     * This accounts for both core subject teachers and non-core teaching staff
     * (language teachers, etc.) who also need dedicated classroom space.
     *
     * @param studentPopulation the total number of students
     * @param periodsPerDay the number of class periods per day
     * @return the recommended number of classrooms
     */
    public int calculateClassroomsNeeded(int studentPopulation, int periodsPerDay) {
        // Each student takes ~6-8 classes, classrooms are used all periods
        // Formula: (students * classes_per_student) / (periods * optimal_class_size)
        int classesPerStudent = 7; // Average
        int totalClassSlots = studentPopulation * classesPerStudent;
        int slotsPerClassroom = periodsPerDay * getOptimalClassSize();

        int baseClassrooms = (int) Math.ceil((double) totalClassSlots / slotsPerClassroom);

        // Additional classrooms for non-core teaching staff who need dedicated rooms.
        // Language teachers are the primary gap: the school offers ~5 languages, each
        // requiring 1+ teachers. They need their own classrooms just like core teachers.
        // Estimate: ~1 language teacher per 120 students (scales with school size).
        int languageTeacherRooms = Math.max(2, studentPopulation / 120);

        // Small buffer for substitute teachers who teach elective sections (Keyboarding,
        // Philosophy, etc.) and any other teaching staff overflow.
        int additionalBuffer = Math.max(2, studentPopulation / 600);

        int totalClassrooms = baseClassrooms + languageTeacherRooms + additionalBuffer;
        return (int) Math.ceil(totalClassrooms * fundingLevel.getRoomCountModifier());
    }

    /**
     * Calculates the number of specialized rooms (labs, studios, etc.) for a given population.
     *
     * @param baseCount the standard count of specialized rooms
     * @return the adjusted count based on funding level
     */
    public int calculateSpecializedRooms(int baseCount) {
        return Math.max(1, (int) Math.round(baseCount * fundingLevel.getSpecializedRoomModifier()));
    }

    /**
     * Calculates the number of sections needed for a class based on student demand.
     *
     * @param studentDemand the number of students needing this class
     * @return the number of sections to create
     */
    public int calculateSectionsNeeded(int studentDemand) {
        if (studentDemand <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) studentDemand / getOptimalClassSize());
    }

    /**
     * Calculates the physical capacity of a classroom.
     *
     * @param baseCapacity the standard classroom capacity
     * @return the adjusted capacity based on funding level
     */
    public int calculateClassroomCapacity(int baseCapacity) {
        return (int) Math.round(baseCapacity * fundingLevel.getClassroomCapacityModifier());
    }

    /**
     * Calculates the minimum staff needed for a given student population.
     *
     * @param studentPopulation the total number of students
     * @return the minimum number of staff
     */
    public int calculateMinimumStaff(int studentPopulation) {
        return (int) Math.ceil(studentPopulation * fundingLevel.getStaffStudentRatio());
    }

    /**
     * Calculates the physical capacity of the school (maximum possible enrollment).
     *
     * @param classroomCount the number of classrooms
     * @param periodsPerDay the number of periods per day
     * @return the physical capacity
     */
    public int calculatePhysicalCapacity(int classroomCount, int periodsPerDay) {
        // Each classroom can hold maxClassSize students per period
        // But students take multiple classes, so divide by average classes per student
        int classesPerStudent = 7;
        return (classroomCount * periodsPerDay * getMaxClassSize()) / classesPerStudent;
    }

    /**
     * Calculates the optimal capacity of the school (comfortable enrollment level).
     *
     * @param classroomCount the number of classrooms
     * @param periodsPerDay the number of periods per day
     * @return the optimal capacity
     */
    public int calculateOptimalCapacity(int classroomCount, int periodsPerDay) {
        int classesPerStudent = 7;
        return (classroomCount * periodsPerDay * getOptimalClassSize()) / classesPerStudent;
    }

    /**
     * Calculates the overcrowding level given current enrollment.
     *
     * @param enrolled the current enrollment
     * @param optimalCapacity the optimal capacity
     * @return the overcrowding ratio (1.0 = at optimal, >1.0 = overcrowded)
     */
    public double calculateOvercrowdingLevel(int enrolled, int optimalCapacity) {
        if (optimalCapacity <= 0) {
            return 0;
        }
        return (double) enrolled / optimalCapacity;
    }

    /**
     * Checks if the school would be considered overcrowded at the given enrollment.
     *
     * @param enrolled the enrollment count
     * @param optimalCapacity the optimal capacity
     * @return true if overcrowded
     */
    public boolean isOvercrowded(int enrolled, int optimalCapacity) {
        return calculateOvercrowdingLevel(enrolled, optimalCapacity) > 1.1; // 10% tolerance
    }

    /**
     * Gets the maximum allowed enrollment considering overcrowding limits.
     *
     * @param physicalCapacity the physical capacity
     * @return the maximum enrollment
     */
    public int getMaxAllowedEnrollment(int physicalCapacity) {
        if (!allowOvercrowding) {
            return physicalCapacity;
        }
        return (int) (physicalCapacity * maxOvercrowdingPercent);
    }

    // ==================== School Expansion Methods ====================

    /**
     * Checks if the school can expand to meet student demand.
     * Only ADEQUATE and better funded schools can afford expansion.
     *
     * @return true if the school can expand
     */
    public boolean canExpandToMeetDemand() {
        return fundingLevel.ordinal() >= FundingLevel.ADEQUATE.ordinal();
    }

    /**
     * Gets the maximum number of additional classrooms that can be added through expansion.
     * Better funded schools can add more classrooms.
     *
     * @return the maximum number of classrooms that can be added
     */
    public int getMaxExpansionClassrooms() {
        switch (fundingLevel) {
            case EXCELLENTLY_FUNDED:
                return 15;
            case WELL_FUNDED:
                return 10;
            case ADEQUATE:
                return 5;
            default:
                return 0;  // Underfunded schools cannot expand
        }
    }

    /**
     * Gets the maximum number of additional portables that can be added through expansion.
     * Portables are a cheaper alternative for schools that can't afford full expansion.
     * Ironically, underfunded schools often rely more on portables.
     *
     * @return the maximum number of portables that can be added
     */
    public int getMaxExpansionPortables() {
        switch (fundingLevel) {
            case SEVERELY_UNDERFUNDED:
                return 8;  // Rely heavily on cheap portable solutions
            case UNDERFUNDED:
                return 6;
            case ADEQUATE:
                return 4;
            case WELL_FUNDED:
                return 2;
            case EXCELLENTLY_FUNDED:
                return 1;  // Prefer permanent classrooms
            default:
                return 4;
        }
    }

    /**
     * Gets the maximum number of additional teachers that can be hired through expansion.
     * Tied to funding level and ability to pay salaries.
     *
     * @return the maximum number of teachers that can be hired
     */
    public int getMaxExpansionTeachers() {
        switch (fundingLevel) {
            case EXCELLENTLY_FUNDED:
                return 20;
            case WELL_FUNDED:
                return 15;
            case ADEQUATE:
                return 8;
            case UNDERFUNDED:
                return 3;
            case SEVERELY_UNDERFUNDED:
                return 1;
            default:
                return 5;
        }
    }

    // ==================== Getters and Setters ====================

    public FundingLevel getFundingLevel() {
        return fundingLevel;
    }

    public void setFundingLevel(FundingLevel fundingLevel) {
        this.fundingLevel = fundingLevel;
    }

    public int getMaxClassSizeOverride() {
        return maxClassSizeOverride;
    }

    public void setMaxClassSizeOverride(int maxClassSizeOverride) {
        this.maxClassSizeOverride = maxClassSizeOverride;
    }

    public int getOptimalClassSizeOverride() {
        return optimalClassSizeOverride;
    }

    public void setOptimalClassSizeOverride(int optimalClassSizeOverride) {
        this.optimalClassSizeOverride = optimalClassSizeOverride;
    }

    public boolean isAllowOvercrowding() {
        return allowOvercrowding;
    }

    public void setAllowOvercrowding(boolean allowOvercrowding) {
        this.allowOvercrowding = allowOvercrowding;
    }

    public double getMaxOvercrowdingPercent() {
        return maxOvercrowdingPercent;
    }

    public void setMaxOvercrowdingPercent(double maxOvercrowdingPercent) {
        this.maxOvercrowdingPercent = maxOvercrowdingPercent;
    }

    /**
     * Gets the classroom capacity modifier from the funding level.
     *
     * @return the classroom capacity modifier
     */
    public double getClassroomCapacityModifier() {
        return fundingLevel.getClassroomCapacityModifier();
    }

    /**
     * Gets the specialized room modifier from the funding level.
     *
     * @return the specialized room modifier
     */
    public double getSpecializedRoomModifier() {
        return fundingLevel.getSpecializedRoomModifier();
    }

    /**
     * Gets the staff-to-student ratio from the funding level.
     *
     * @return the staff-to-student ratio
     */
    public double getStaffStudentRatio() {
        return fundingLevel.getStaffStudentRatio();
    }

    /**
     * Gets the room count modifier from the funding level.
     *
     * @return the room count modifier
     */
    public double getRoomCountModifier() {
        return fundingLevel.getRoomCountModifier();
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a funding model for an underfunded school.
     *
     * @return an underfunded school funding model
     */
    public static SchoolFundingModel createUnderfunded() {
        return new SchoolFundingModel(FundingLevel.UNDERFUNDED);
    }

    /**
     * Creates a funding model for an adequately funded school.
     *
     * @return an adequately funded school funding model
     */
    public static SchoolFundingModel createAdequate() {
        return new SchoolFundingModel(FundingLevel.ADEQUATE);
    }

    /**
     * Creates a funding model for a well-funded school.
     *
     * @return a well-funded school funding model
     */
    public static SchoolFundingModel createWellFunded() {
        return new SchoolFundingModel(FundingLevel.WELL_FUNDED);
    }

    @Override
    public String toString() {
        return "SchoolFundingModel{" +
                "fundingLevel=" + fundingLevel.getDisplayName() +
                ", maxClassSize=" + getMaxClassSize() +
                ", optimalClassSize=" + getOptimalClassSize() +
                ", allowOvercrowding=" + allowOvercrowding +
                '}';
    }
}
