package utility;

import entity.Staff;
import entity.StandardSchool;
import entity.Student;
import entity.Town;
import view.GameView;

import java.util.HashMap;

/**
 * Main orchestration service for assigning populations to schools.
 * Coordinates student and staff assignment using the Town population pools.
 */
public class SchoolAssignmentService {

    /**
     * Fully populates a school from the town's population pools.
     * This is the main entry point for assigning people to a school.
     *
     * @param town the town containing population pools
     * @param school the school to populate
     * @param view the game view for output
     */
    public static void populateSchool(Town town, StandardSchool school, GameView view) {
        view.appendOutput("Populating " + school.getSchoolName() + " from town pool...");
        
        // Add school to town if not already present
        if (!town.getSchools().contains(school)) {
            town.addSchool(school);
        }
        
        // Get school capacity for student assignment
        int studentCapacity = school.getTotalStudentCapacity();
        
        // Assign students first
        int studentsAssigned = StudentAssignmentService.assignStudentsToSchool(
                town.getStudentPool(), school, studentCapacity, view);
        
        // Assign staff based on student count and school needs
        int staffAssigned = StaffAssignmentService.assignStaffToSchool(
                town, school, studentsAssigned, view);
        
        // Assign classes to staff schedules
        StaffAssignmentService.assignClassesToStaff(town.getStaffPool(), school, view);
        
        // Schedule students
        HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
        HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);
        
        try {
            view.appendOutput("Scheduling students...");
            EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                    studentMap, staffMap, school, view);
            
            view.appendOutput("Assigning seating...");
            StudentSeatingAssigner.seatInitialStudents(school);
        } catch (Exception e) {
            view.appendOutput("Error during scheduling: " + e.getMessage());
            e.printStackTrace();
        }
        
        view.appendOutput("School population complete: " + studentsAssigned + " students, " + 
                staffAssigned + " staff");
    }

    /**
     * Populates a school with a specific number of students and staff.
     *
     * @param town the town containing population pools
     * @param school the school to populate
     * @param studentCount the number of students to assign
     * @param staffCount the number of staff to assign
     * @param view the game view for output
     */
    public static void populateSchool(Town town, StandardSchool school, 
                                      int studentCount, int staffCount, GameView view) {
        view.appendOutput("Populating " + school.getSchoolName() + " with " + 
                studentCount + " students and " + staffCount + " staff...");
        
        // Add school to town if not already present
        if (!town.getSchools().contains(school)) {
            town.addSchool(school);
        }
        
        // Assign students
        int studentsAssigned = StudentAssignmentService.assignStudentsToSchool(
                town.getStudentPool(), school, studentCount, view);
        
        // Assign staff
        int staffAssigned = StaffAssignmentService.assignStaffToSchool(
                town.getStaffPool(), school, staffCount, studentsAssigned, view);
        
        // Complete scheduling
        completeScheduling(town, school, view);
        
        view.appendOutput("School population complete: " + studentsAssigned + " students, " + 
                staffAssigned + " staff");
    }

    /**
     * Completes the scheduling process for a school.
     * Call this after students and staff have been assigned.
     *
     * @param town the town
     * @param school the school
     * @param view the game view for output
     */
    public static void completeScheduling(Town town, StandardSchool school, GameView view) {
        // Assign classes to staff schedules
        StaffAssignmentService.assignClassesToStaff(town.getStaffPool(), school, view);
        
        // Get maps for scheduling
        HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
        HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);
        
        try {
            view.appendOutput("Scheduling students...");
            EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                    studentMap, staffMap, school, view);
            
            view.appendOutput("Assigning seating...");
            StudentSeatingAssigner.seatInitialStudents(school);
        } catch (Exception e) {
            view.appendOutput("Error during scheduling: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the HashMap of students assigned to a school (for compatibility).
     *
     * @param town the town
     * @param school the school
     * @return HashMap of students
     */
    public static HashMap<Integer, Student> getStudentHashMap(Town town, StandardSchool school) {
        return town.getStudentPool().getStudentsBySchoolAsMap(school);
    }

    /**
     * Gets the HashMap of staff assigned to a school (for compatibility).
     *
     * @param town the town
     * @param school the school
     * @return HashMap of staff
     */
    public static HashMap<Integer, Staff> getStaffHashMap(Town town, StandardSchool school) {
        return town.getStaffPool().getStaffBySchoolAsMap(school);
    }

    /**
     * Gets statistics about the school population.
     *
     * @param town the town
     * @param school the school
     * @return a summary string
     */
    public static String getPopulationSummary(Town town, StandardSchool school) {
        int students = town.getStudentPool().getAssignedCount(school);
        int staff = town.getStaffPool().getAssignedCount(school);
        int unassignedStudents = town.getStudentPool().getUnassignedCount();
        int unassignedStaff = town.getStaffPool().getUnassignedCount();
        
        return String.format("School: %s\n" +
                "  Students: %d (capacity: %d)\n" +
                "  Staff: %d (minimum: %d)\n" +
                "  Available students in town: %d\n" +
                "  Available staff in town: %d",
                school.getSchoolName(),
                students, school.getTotalStudentCapacity(),
                staff, school.getMinimumStaffRequirements(),
                unassignedStudents, unassignedStaff);
    }

    /**
     * Checks if a school is fully staffed based on minimum requirements.
     *
     * @param town the town
     * @param school the school
     * @return true if the school has enough staff
     */
    public static boolean isFullyStaffed(Town town, StandardSchool school) {
        int assigned = town.getStaffPool().getAssignedCount(school);
        int required = school.getMinimumStaffRequirements();
        return assigned >= required;
    }

    /**
     * Checks if a school is at student capacity.
     *
     * @param town the town
     * @param school the school
     * @return true if the school is at or over capacity
     */
    public static boolean isAtStudentCapacity(Town town, StandardSchool school) {
        int enrolled = town.getStudentPool().getAssignedCount(school);
        int capacity = school.getTotalStudentCapacity();
        return enrolled >= capacity;
    }
}
