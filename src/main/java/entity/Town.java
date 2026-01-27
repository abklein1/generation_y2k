package entity;

import config.TownDemographics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a town that contains the population pools and schools.
 * The Town entity manages the overall population (students and staff) that can be
 * assigned to one or more schools. This allows for:
 * - Independent demographic configuration
 * - Population pools for mid-year enrollments, substitutes, rival schools
 * - Multiple schools sharing the same population pool
 */
public class Town implements Serializable {

    private static final long serialVersionUID = 1L;

    private String townName;
    private StudentPool studentPool;
    private StaffPool staffPool;
    private List<StandardSchool> schools;
    private TownDemographics demographics;
    private String[] townColors; // Colors that can be used for school spirit items

    /**
     * Creates a new Town with empty pools and no schools.
     */
    public Town() {
        this.schools = new ArrayList<>();
        this.studentPool = new StudentPool();
        this.staffPool = new StaffPool();
    }

    /**
     * Creates a new Town with the specified name.
     *
     * @param townName the name of the town
     */
    public Town(String townName) {
        this();
        this.townName = townName;
    }

    /**
     * Creates a new Town with the specified name and demographics.
     *
     * @param townName the name of the town
     * @param demographics the demographics configuration for population generation
     */
    public Town(String townName, TownDemographics demographics) {
        this(townName);
        this.demographics = demographics;
    }

    // ==================== Getters and Setters ====================

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public StudentPool getStudentPool() {
        return studentPool;
    }

    public void setStudentPool(StudentPool studentPool) {
        this.studentPool = studentPool;
    }

    public StaffPool getStaffPool() {
        return staffPool;
    }

    public void setStaffPool(StaffPool staffPool) {
        this.staffPool = staffPool;
    }

    public List<StandardSchool> getSchools() {
        return new ArrayList<>(schools);
    }

    public TownDemographics getDemographics() {
        return demographics;
    }

    public void setDemographics(TownDemographics demographics) {
        this.demographics = demographics;
    }

    public String[] getTownColors() {
        return townColors != null ? townColors.clone() : null;
    }

    public void setTownColors(String[] townColors) {
        this.townColors = townColors != null ? townColors.clone() : null;
    }

    // ==================== School Management ====================

    /**
     * Adds a school to the town.
     *
     * @param school the school to add
     */
    public void addSchool(StandardSchool school) {
        if (school != null && !schools.contains(school)) {
            schools.add(school);
        }
    }

    /**
     * Removes a school from the town.
     *
     * @param school the school to remove
     * @return true if the school was removed, false otherwise
     */
    public boolean removeSchool(StandardSchool school) {
        return schools.remove(school);
    }

    /**
     * Gets a school by name.
     *
     * @param schoolName the name of the school
     * @return the school with the given name, or null if not found
     */
    public StandardSchool getSchoolByName(String schoolName) {
        for (StandardSchool school : schools) {
            if (school.getSchoolName().equals(schoolName)) {
                return school;
            }
        }
        return null;
    }

    /**
     * Gets the number of schools in the town.
     *
     * @return the number of schools
     */
    public int getSchoolCount() {
        return schools.size();
    }

    // ==================== Population Statistics ====================

    /**
     * Gets the total number of students in the town (assigned and unassigned).
     *
     * @return the total student population
     */
    public int getTotalStudentPopulation() {
        return studentPool.getTotalCount();
    }

    /**
     * Gets the total number of staff in the town (assigned and unassigned).
     *
     * @return the total staff population
     */
    public int getTotalStaffPopulation() {
        return staffPool.getTotalCount();
    }

    /**
     * Gets the number of unassigned students available for enrollment.
     *
     * @return the number of unassigned students
     */
    public int getAvailableStudentCount() {
        return studentPool.getUnassignedCount();
    }

    /**
     * Gets the number of unassigned staff available for hiring.
     *
     * @return the number of unassigned staff
     */
    public int getAvailableStaffCount() {
        return staffPool.getUnassignedCount();
    }

    // ==================== Convenience Methods ====================

    /**
     * Checks if the town has any schools.
     *
     * @return true if the town has at least one school
     */
    public boolean hasSchools() {
        return !schools.isEmpty();
    }

    /**
     * Checks if there are unassigned students available.
     *
     * @return true if there are unassigned students
     */
    public boolean hasAvailableStudents() {
        return studentPool.hasUnassigned();
    }

    /**
     * Checks if there are unassigned staff available.
     *
     * @return true if there are unassigned staff
     */
    public boolean hasAvailableStaff() {
        return staffPool.hasUnassigned();
    }

    @Override
    public String toString() {
        return "Town{" +
                "name='" + townName + '\'' +
                ", schools=" + schools.size() +
                ", students=" + getTotalStudentPopulation() +
                ", staff=" + getTotalStaffPopulation() +
                '}';
    }
}
