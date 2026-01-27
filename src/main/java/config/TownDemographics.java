package config;

import constants.SimConstants;
import entity.StaffType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for town demographics that controls population generation.
 * This allows for independent configuration of student and staff populations
 * separate from school capacity constraints.
 */
public class TownDemographics implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== Population Sizes ====================
    
    /** Total number of students to generate for the town */
    private int totalStudentPopulation = SimConstants.DEFAULT_STUDENT_POPULATION;
    
    /** Total number of staff to generate for the town */
    private int totalStaffPopulation = SimConstants.DEFAULT_STAFF_POPULATION;
    
    /** Extra students beyond school capacity (for transfer pool, rival schools, etc.) */
    private double extraStudentPoolPercent = SimConstants.DEFAULT_EXTRA_STUDENT_POOL_PERCENT;
    
    /** Extra staff beyond school needs (for substitutes, future hiring, etc.) */
    private double extraStaffPoolPercent = SimConstants.DEFAULT_EXTRA_STAFF_POOL_PERCENT;

    // ==================== Student Demographics ====================
    
    /** Distribution of students by grade level (should sum to 1.0) */
    private Map<String, Double> gradeDistribution;
    
    /** Distribution of student genders */
    private Map<String, Double> genderDistribution;
    
    /** Distribution of student income levels */
    private Map<String, Double> incomeDistribution;
    
    /** Distribution of student races/ethnicities (affects name selection) */
    private Map<String, Double> raceDistribution;

    // ==================== Staff Demographics ====================
    
    /** Distribution of staff by type/subject */
    private Map<StaffType, Double> staffTypeDistribution;
    
    /** Minimum years of experience for staff */
    private int minStaffExperience = SimConstants.STAFF_MIN_EXPERIENCE;
    
    /** Maximum years of experience for staff */
    private int maxStaffExperience = SimConstants.STAFF_MAX_EXPERIENCE;
    
    /** Minimum age for staff */
    private int minStaffAge = SimConstants.STAFF_MIN_AGE;
    
    /** Maximum age for staff */
    private int maxStaffAge = SimConstants.STAFF_MAX_AGE;

    // ==================== Stat Distribution Settings ====================
    
    /** Mean intelligence for students (based on IQ distribution, mean 100) */
    private double studentIntelligenceMean = SimConstants.STUDENT_POP_INTELLIGENCE_MEAN;
    
    /** Standard deviation for student intelligence */
    private double studentIntelligenceStdDev = SimConstants.STUDENT_POP_INTELLIGENCE_STANDARD_DEVIATION;
    
    /** Mean intelligence for staff (based on IQ distribution, mean 100) */
    private double staffIntelligenceMean = SimConstants.TEACHER_POP_INTELLIGENCE_MEAN;
    
    /** Standard deviation for staff intelligence */
    private double staffIntelligenceStdDev = SimConstants.TEACHER_POP_INTELLIGENCE_STANDARD_DEVIATION;

    /**
     * Creates a TownDemographics with default values.
     */
    public TownDemographics() {
        initializeDefaultDistributions();
    }

    /**
     * Creates a TownDemographics with specified population sizes.
     *
     * @param totalStudentPopulation the total number of students
     * @param totalStaffPopulation the total number of staff
     */
    public TownDemographics(int totalStudentPopulation, int totalStaffPopulation) {
        this();
        this.totalStudentPopulation = totalStudentPopulation;
        this.totalStaffPopulation = totalStaffPopulation;
    }

    /**
     * Initializes default distribution values based on SimConstants.
     * All distribution values are centralized in SimConstants for consistency.
     */
    private void initializeDefaultDistributions() {
        // Grade distribution - slightly higher freshman as some drop out
        gradeDistribution = new HashMap<>();
        gradeDistribution.put("Freshman", SimConstants.GRADE_DISTRIBUTION_FRESHMAN);
        gradeDistribution.put("Sophomore", SimConstants.GRADE_DISTRIBUTION_SOPHOMORE);
        gradeDistribution.put("Junior", SimConstants.GRADE_DISTRIBUTION_JUNIOR);
        gradeDistribution.put("Senior", SimConstants.GRADE_DISTRIBUTION_SENIOR);

        // Gender distribution
        genderDistribution = new HashMap<>();
        genderDistribution.put("Male", SimConstants.GENDER_DISTRIBUTION_MALE);
        genderDistribution.put("Female", SimConstants.GENDER_DISTRIBUTION_FEMALE);

        // Income distribution (Low 25%, Middle 60%, High 15% - original distribution)
        incomeDistribution = new HashMap<>();
        incomeDistribution.put("Low", SimConstants.INCOME_DISTRIBUTION_LOW);
        incomeDistribution.put("Middle", SimConstants.INCOME_DISTRIBUTION_MIDDLE);
        incomeDistribution.put("High", SimConstants.INCOME_DISTRIBUTION_HIGH);

        // Race distribution (based on US Census data approximations)
        raceDistribution = new HashMap<>();
        raceDistribution.put("White", SimConstants.RACE_DISTRIBUTION_WHITE);
        raceDistribution.put("Hispanic", SimConstants.RACE_DISTRIBUTION_HISPANIC);
        raceDistribution.put("Black", SimConstants.RACE_DISTRIBUTION_BLACK);
        raceDistribution.put("Asian", SimConstants.RACE_DISTRIBUTION_ASIAN);
        raceDistribution.put("Other", SimConstants.RACE_DISTRIBUTION_OTHER);

        // Staff type distribution (core subjects have more teachers)
        staffTypeDistribution = new HashMap<>();
        staffTypeDistribution.put(StaffType.ENGLISH, SimConstants.STAFF_TYPE_ENGLISH);
        staffTypeDistribution.put(StaffType.MATH, SimConstants.STAFF_TYPE_MATH);
        staffTypeDistribution.put(StaffType.SCIENCE, SimConstants.STAFF_TYPE_SCIENCE);
        staffTypeDistribution.put(StaffType.HISTORY, SimConstants.STAFF_TYPE_HISTORY);
        staffTypeDistribution.put(StaffType.LANGUAGES, SimConstants.STAFF_TYPE_LANGUAGES);
        staffTypeDistribution.put(StaffType.PHYSICAL_ED, SimConstants.STAFF_TYPE_PHYSICAL_ED);
        staffTypeDistribution.put(StaffType.VISUAL_ARTS, SimConstants.STAFF_TYPE_VISUAL_ARTS);
        staffTypeDistribution.put(StaffType.PERFORMING_ARTS, SimConstants.STAFF_TYPE_PERFORMING_ARTS);
        staffTypeDistribution.put(StaffType.COMP_SCI, SimConstants.STAFF_TYPE_COMP_SCI);
        staffTypeDistribution.put(StaffType.VOCATIONAL, SimConstants.STAFF_TYPE_VOCATIONAL);
        staffTypeDistribution.put(StaffType.BUSINESS, SimConstants.STAFF_TYPE_BUSINESS);
        staffTypeDistribution.put(StaffType.CONSUMER_SCI, SimConstants.STAFF_TYPE_CONSUMER_SCI);
        staffTypeDistribution.put(StaffType.PRINCIPAL, SimConstants.STAFF_TYPE_PRINCIPAL);
        staffTypeDistribution.put(StaffType.VICE_PRINCIPAL, SimConstants.STAFF_TYPE_VICE_PRINCIPAL);
        staffTypeDistribution.put(StaffType.GUIDANCE, SimConstants.STAFF_TYPE_GUIDANCE);
        staffTypeDistribution.put(StaffType.MAINTENANCE, SimConstants.STAFF_TYPE_MAINTENANCE);
        staffTypeDistribution.put(StaffType.LUNCH, SimConstants.STAFF_TYPE_LUNCH);
        staffTypeDistribution.put(StaffType.OFFICE, SimConstants.STAFF_TYPE_OFFICE);
        staffTypeDistribution.put(StaffType.LIBRARY, SimConstants.STAFF_TYPE_LIBRARY);
        staffTypeDistribution.put(StaffType.NURSE, SimConstants.STAFF_TYPE_NURSE);
        staffTypeDistribution.put(StaffType.SUB, SimConstants.STAFF_TYPE_SUB);
    }

    // ==================== Population Size Getters/Setters ====================

    public int getTotalStudentPopulation() {
        return totalStudentPopulation;
    }

    public void setTotalStudentPopulation(int totalStudentPopulation) {
        this.totalStudentPopulation = totalStudentPopulation;
    }

    public int getTotalStaffPopulation() {
        return totalStaffPopulation;
    }

    public void setTotalStaffPopulation(int totalStaffPopulation) {
        this.totalStaffPopulation = totalStaffPopulation;
    }

    public double getExtraStudentPoolPercent() {
        return extraStudentPoolPercent;
    }

    public void setExtraStudentPoolPercent(double extraStudentPoolPercent) {
        this.extraStudentPoolPercent = extraStudentPoolPercent;
    }

    public double getExtraStaffPoolPercent() {
        return extraStaffPoolPercent;
    }

    public void setExtraStaffPoolPercent(double extraStaffPoolPercent) {
        this.extraStaffPoolPercent = extraStaffPoolPercent;
    }

    /**
     * Gets the total students to generate including the extra pool.
     *
     * @return total students to generate
     */
    public int getTotalStudentsToGenerate() {
        return (int) (totalStudentPopulation * (1 + extraStudentPoolPercent));
    }

    /**
     * Gets the total staff to generate including the extra pool.
     *
     * @return total staff to generate
     */
    public int getTotalStaffToGenerate() {
        return (int) (totalStaffPopulation * (1 + extraStaffPoolPercent));
    }

    // ==================== Student Demographics Getters/Setters ====================

    public Map<String, Double> getGradeDistribution() {
        return new HashMap<>(gradeDistribution);
    }

    public void setGradeDistribution(Map<String, Double> gradeDistribution) {
        this.gradeDistribution = new HashMap<>(gradeDistribution);
    }

    public Map<String, Double> getGenderDistribution() {
        return new HashMap<>(genderDistribution);
    }

    public void setGenderDistribution(Map<String, Double> genderDistribution) {
        this.genderDistribution = new HashMap<>(genderDistribution);
    }

    public Map<String, Double> getIncomeDistribution() {
        return new HashMap<>(incomeDistribution);
    }

    public void setIncomeDistribution(Map<String, Double> incomeDistribution) {
        this.incomeDistribution = new HashMap<>(incomeDistribution);
    }

    public Map<String, Double> getRaceDistribution() {
        return new HashMap<>(raceDistribution);
    }

    public void setRaceDistribution(Map<String, Double> raceDistribution) {
        this.raceDistribution = new HashMap<>(raceDistribution);
    }

    // ==================== Staff Demographics Getters/Setters ====================

    public Map<StaffType, Double> getStaffTypeDistribution() {
        return new HashMap<>(staffTypeDistribution);
    }

    public void setStaffTypeDistribution(Map<StaffType, Double> staffTypeDistribution) {
        this.staffTypeDistribution = new HashMap<>(staffTypeDistribution);
    }

    public int getMinStaffExperience() {
        return minStaffExperience;
    }

    public void setMinStaffExperience(int minStaffExperience) {
        this.minStaffExperience = minStaffExperience;
    }

    public int getMaxStaffExperience() {
        return maxStaffExperience;
    }

    public void setMaxStaffExperience(int maxStaffExperience) {
        this.maxStaffExperience = maxStaffExperience;
    }

    public int getMinStaffAge() {
        return minStaffAge;
    }

    public void setMinStaffAge(int minStaffAge) {
        this.minStaffAge = minStaffAge;
    }

    public int getMaxStaffAge() {
        return maxStaffAge;
    }

    public void setMaxStaffAge(int maxStaffAge) {
        this.maxStaffAge = maxStaffAge;
    }

    // ==================== Stat Distribution Getters/Setters ====================

    public double getStudentIntelligenceMean() {
        return studentIntelligenceMean;
    }

    public void setStudentIntelligenceMean(double studentIntelligenceMean) {
        this.studentIntelligenceMean = studentIntelligenceMean;
    }

    public double getStudentIntelligenceStdDev() {
        return studentIntelligenceStdDev;
    }

    public void setStudentIntelligenceStdDev(double studentIntelligenceStdDev) {
        this.studentIntelligenceStdDev = studentIntelligenceStdDev;
    }

    public double getStaffIntelligenceMean() {
        return staffIntelligenceMean;
    }

    public void setStaffIntelligenceMean(double staffIntelligenceMean) {
        this.staffIntelligenceMean = staffIntelligenceMean;
    }

    public double getStaffIntelligenceStdDev() {
        return staffIntelligenceStdDev;
    }

    public void setStaffIntelligenceStdDev(double staffIntelligenceStdDev) {
        this.staffIntelligenceStdDev = staffIntelligenceStdDev;
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the number of students to generate for a specific grade level.
     *
     * @param gradeLevel the grade level
     * @return the number of students for that grade
     */
    public int getStudentCountForGrade(String gradeLevel) {
        Double percent = gradeDistribution.get(gradeLevel);
        if (percent == null) {
            return 0;
        }
        return (int) (getTotalStudentsToGenerate() * percent);
    }

    /**
     * Gets the number of staff to generate for a specific staff type.
     *
     * @param staffType the staff type
     * @return the number of staff for that type
     */
    public int getStaffCountForType(StaffType staffType) {
        Double percent = staffTypeDistribution.get(staffType);
        if (percent == null) {
            return 0;
        }
        return (int) (getTotalStaffToGenerate() * percent);
    }

    /**
     * Validates that all distributions sum to approximately 1.0.
     *
     * @return true if distributions are valid
     */
    public boolean validateDistributions() {
        double gradeSum = gradeDistribution.values().stream().mapToDouble(d -> d).sum();
        double genderSum = genderDistribution.values().stream().mapToDouble(d -> d).sum();
        double incomeSum = incomeDistribution.values().stream().mapToDouble(d -> d).sum();
        double raceSum = raceDistribution.values().stream().mapToDouble(d -> d).sum();
        double staffSum = staffTypeDistribution.values().stream().mapToDouble(d -> d).sum();

        double tolerance = 0.01;
        return Math.abs(gradeSum - 1.0) < tolerance
                && Math.abs(genderSum - 1.0) < tolerance
                && Math.abs(incomeSum - 1.0) < tolerance
                && Math.abs(raceSum - 1.0) < tolerance
                && Math.abs(staffSum - 1.0) < tolerance;
    }

    @Override
    public String toString() {
        return "TownDemographics{" +
                "students=" + totalStudentPopulation +
                ", staff=" + totalStaffPopulation +
                ", extraStudentPool=" + (extraStudentPoolPercent * 100) + "%" +
                ", extraStaffPool=" + (extraStaffPoolPercent * 100) + "%" +
                '}';
    }
}
