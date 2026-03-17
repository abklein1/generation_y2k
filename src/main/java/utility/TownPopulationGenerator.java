package utility;

import config.DemographicsLoader;
import config.TownDemographics;
import entity.Staff;
import entity.StaffPool;
import entity.Student;
import entity.StudentPool;
import entity.Town;
import view.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Orchestrates the generation of town populations using demographics
 * configuration.
 * This generator creates students and staff independently of schools, allowing
 * for
 * flexible assignment to one or more schools.
 */
public class TownPopulationGenerator {

    /**
     * Generates a complete town with student and staff populations.
     *
     * @param townName     the name of the town
     * @param demographics the demographics configuration
     * @param view         the game view for output
     * @return the generated Town with populated pools
     */
    public static Town generateTown(String townName, TownDemographics demographics, GameView view) {
        Town town = new Town(townName, demographics);

        GameLogger.logGeneration("Generating town: " + townName);
        GameLogger.logGeneration("Demographics: " + demographics);

        // Generate students
        GameLogger.logGeneration("Generating student population...");
        generateStudentPopulation(town, demographics, view);

        // Generate staff
        GameLogger.logGeneration("Generating staff population...");
        generateStaffPopulation(town, demographics, view);

        GameLogger.logGeneration("Assigning neighborhoods...");
        NeighborhoodAssignmentService.assignNeighborhoods(town);

        GameLogger.logGeneration("Assigning cell phones...");
        CellPhoneAssignmentService.assignPhonesForTown(town, constants.SimConstants.STARTING_YEAR);

        GameLogger.logGeneration("Town generation complete. Total students: " + town.getTotalStudentPopulation() +
                ", Total staff: " + town.getTotalStaffPopulation());

        return town;
    }

    /**
     * Generates a town using the default demographics configuration.
     *
     * @param townName the name of the town
     * @param view     the game view for output
     * @return the generated Town
     */
    public static Town generateTown(String townName, GameView view) {
        TownDemographics demographics = DemographicsLoader.loadOrDefault();
        return generateTown(townName, demographics, view);
    }

    /**
     * Generates the student population for a town.
     *
     * @param town         the town to populate
     * @param demographics the demographics configuration
     * @param view         the game view for output
     */
    public static void generateStudentPopulation(Town town, TownDemographics demographics, GameView view) {
        int studentCount = demographics.getTotalStudentsToGenerate();
        StudentPool pool = town.getStudentPool();

        // Use a temporary HashMap for compatibility with existing generators
        HashMap<Integer, Student> tempMap = new HashMap<>();

        // Generate students using the existing StudentPopGenerator
        // Note: We don't pass school colors here - they can be set later per school
        StudentPopGenerator.generateStudentsWithDemographics(studentCount, tempMap, view, demographics);

        // Generate sibling relationships
        // Note: SiblingGenerator adds new students to the map
        SiblingGenerator.siblingGenerator(tempMap, studentCount, view);

        // Add all students to the pool
        pool.addStudentsFromMap(tempMap);

        GameLogger.logGeneration("Generated " + pool.getTotalCount() + " students (including siblings)");
    }

    /**
     * Generates the student population with specific school colors for braces.
     *
     * @param town         the town to populate
     * @param demographics the demographics configuration
     * @param schoolColors the school colors for braces band selection
     * @param view         the game view for output
     */
    public static void generateStudentPopulationWithColors(Town town, TownDemographics demographics,
            String[] schoolColors, GameView view) {
        int studentCount = demographics.getTotalStudentsToGenerate();
        StudentPool pool = town.getStudentPool();

        // Set school colors for braces generation
        if (schoolColors != null) {
            StudentPopGenerator.setSchoolColors(schoolColors);
            SiblingGenerator.setSchoolColors(schoolColors);
        }

        // Use a temporary HashMap for compatibility with existing generators
        HashMap<Integer, Student> tempMap = new HashMap<>();

        // Generate students
        StudentPopGenerator.generateStudentsWithDemographics(studentCount, tempMap, view, demographics);

        // Generate sibling relationships
        SiblingGenerator.siblingGenerator(tempMap, studentCount, view);

        // Add all students to the pool
        pool.addStudentsFromMap(tempMap);

        GameLogger.logGeneration("Generated " + pool.getTotalCount() + " students (including siblings)");
    }

    /**
     * Generates the staff population for a town.
     *
     * @param town         the town to populate
     * @param demographics the demographics configuration
     * @param view         the game view for output
     */
    public static void generateStaffPopulation(Town town, TownDemographics demographics, GameView view) {
        int staffCount = demographics.getTotalStaffToGenerate();
        StaffPool pool = town.getStaffPool();

        // Use a temporary HashMap for compatibility with existing generator
        HashMap<Integer, Staff> tempMap = new HashMap<>();

        // Generate staff using the existing TeacherPopGenerator
        TeacherPopGenerator.generateTeachers(staffCount, tempMap, view);

        // Add all staff to the pool
        pool.addStaffFromMap(tempMap);

        GameLogger.logGeneration("Generated " + pool.getTotalCount() + " staff members");
    }

    /**
     * Adds additional students to an existing town.
     * Useful for mid-year enrollments or expanding the town.
     *
     * @param town  the town to add students to
     * @param count the number of students to add
     * @param view  the game view for output
     * @return the list of newly generated students
     */
    public static List<Student> generateAdditionalStudents(Town town, int count, GameView view) {
        StudentPool pool = town.getStudentPool();
        TownDemographics demographics = town.getDemographics();

        HashMap<Integer, Student> tempMap = new HashMap<>();
        if (demographics != null) {
            StudentPopGenerator.generateStudentsWithDemographics(count, tempMap, view, demographics);
        } else {
            StudentPopGenerator.generateStudents(count, tempMap, view);
        }

        // Optionally generate siblings for new students
        SiblingGenerator.siblingGenerator(tempMap, count, view);

        // Add to pool
        pool.addStudentsFromMap(tempMap);
        NeighborhoodAssignmentService.assignNeighborhoodsForNewResidents(town);

        // Return the new students
        List<Student> newStudents = new ArrayList<>(tempMap.values());
        GameLogger.logGeneration("Added " + newStudents.size() + " new students to town");

        return newStudents;
    }

    /**
     * Adds additional staff to an existing town.
     * Useful for hiring new teachers or expanding the substitute pool.
     *
     * @param town  the town to add staff to
     * @param count the number of staff to add
     * @param view  the game view for output
     * @return the list of newly generated staff
     */
    public static List<Staff> generateAdditionalStaff(Town town, int count, GameView view) {
        StaffPool pool = town.getStaffPool();

        HashMap<Integer, Staff> tempMap = new HashMap<>();
        TeacherPopGenerator.generateTeachers(count, tempMap, view);

        // Add to pool
        pool.addStaffFromMap(tempMap);
        NeighborhoodAssignmentService.assignNeighborhoodsForNewResidents(town);

        // Return the new staff
        List<Staff> newStaff = new ArrayList<>(tempMap.values());
        GameLogger.logGeneration("Added " + newStaff.size() + " new staff members to town");

        return newStaff;
    }

    /**
     * Creates a small town for testing purposes.
     *
     * @param view the game view for output
     * @return a small test town
     */
    public static Town generateTestTown(GameView view) {
        TownDemographics demographics = DemographicsLoader.createTest();
        return generateTown("Test Town", demographics, view);
    }

    /**
     * Creates a small-sized town.
     *
     * @param townName the name of the town
     * @param view     the game view for output
     * @return a small town
     */
    public static Town generateSmallTown(String townName, GameView view) {
        TownDemographics demographics = DemographicsLoader.createSmall();
        return generateTown(townName, demographics, view);
    }

    /**
     * Creates a medium-sized town.
     *
     * @param townName the name of the town
     * @param view     the game view for output
     * @return a medium town
     */
    public static Town generateMediumTown(String townName, GameView view) {
        TownDemographics demographics = DemographicsLoader.createMedium();
        return generateTown(townName, demographics, view);
    }

    /**
     * Creates a large-sized town.
     *
     * @param townName the name of the town
     * @param view     the game view for output
     * @return a large town
     */
    public static Town generateLargeTown(String townName, GameView view) {
        TownDemographics demographics = DemographicsLoader.createLarge();
        return generateTown(townName, demographics, view);
    }
}
