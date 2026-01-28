package utility;

import entity.Staff;
import entity.StandardSchool;
import entity.Student;
import entity.StudentPool;
import entity.Town;
import view.GameView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for assigning students from a population pool to schools.
 * Handles enrollment, grade-level organization, and scheduling.
 */
public class StudentAssignmentService {

    /**
     * Assigns students from the town's pool to a school based on school capacity.
     *
     * @param town the town containing the student pool
     * @param school the school to assign students to
     * @param view the game view for output
     * @return the number of students assigned
     */
    public static int assignStudentsToSchool(Town town, StandardSchool school, GameView view) {
        StudentPool pool = town.getStudentPool();
        int capacity = school.getOptimalCapacity(); // Use optimal capacity instead of deprecated method
        
        return assignStudentsToSchool(pool, school, capacity, view);
    }

    /**
     * Assigns a specific number of students from the pool to a school.
     *
     * @param pool the student pool
     * @param school the school to assign students to
     * @param count the number of students to assign
     * @param view the game view for output
     * @return the number of students actually assigned
     */
    public static int assignStudentsToSchool(StudentPool pool, StandardSchool school, int count, GameView view) {
        List<Student> unassigned = pool.getUnassignedStudents();
        int toAssign = Math.min(count, unassigned.size());
        
        view.appendOutput("Assigning " + toAssign + " students to " + school.getSchoolName());
        
        // Create a HashMap for grade-level organization (for compatibility with existing code)
        HashMap<Integer, Student> assignedMap = new HashMap<>();
        
        int assigned = 0;
        for (int i = 0; i < toAssign && i < unassigned.size(); i++) {
            Student student = unassigned.get(i);
            if (pool.assignToSchool(student, school)) {
                assignedMap.put(assigned, student);
                assigned++;
            }
        }
        
        // Organize students by grade level in the school
        school.setStudentGradeClass(assignedMap, view);
        
        view.appendOutput("Successfully assigned " + assigned + " students to " + school.getSchoolName());
        return assigned;
    }

    /**
     * Assigns students to grade levels within a school.
     * Uses the school's existing setStudentGradeClass method.
     *
     * @param pool the student pool
     * @param school the school
     * @param view the game view for output
     */
    public static void organizeByGradeLevel(StudentPool pool, StandardSchool school, GameView view) {
        HashMap<Integer, Student> studentMap = pool.getStudentsBySchoolAsMap(school);
        school.setStudentGradeClass(studentMap, view);
    }

    /**
     * Schedules all students assigned to a school using the enhanced scheduler.
     *
     * @param pool the student pool
     * @param school the school
     * @param staffMap the staff assigned to the school
     * @param view the game view for output
     */
    public static void scheduleStudents(StudentPool pool, StandardSchool school, 
                                        HashMap<Integer, Staff> staffMap, GameView view) {
        HashMap<Integer, Student> studentMap = pool.getStudentsBySchoolAsMap(school);
        
        view.appendOutput("Scheduling " + studentMap.size() + " students...");
        
        try {
            EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                    studentMap, staffMap, school, view);
        } catch (Exception e) {
            view.appendOutput("Error during scheduling: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Assigns seating for all students in a school.
     *
     * @param school the school
     */
    public static void assignSeating(StandardSchool school) {
        StudentSeatingAssigner.seatInitialStudents(school);
    }

    /**
     * Enrolls a new student mid-year.
     *
     * @param pool the student pool
     * @param student the student to enroll
     * @param school the school to enroll in
     * @param view the game view for output
     * @return true if enrollment was successful
     */
    public static boolean enrollStudent(StudentPool pool, Student student, StandardSchool school, GameView view) {
        if (!pool.assignToSchool(student, school)) {
            view.appendOutput("Failed to enroll student: not in pool");
            return false;
        }
        
        // Add to appropriate grade class
        String grade = student.studentStatistics.getGradeLevel();
        HashMap<Integer, Student> gradeClass = school.getStudentGradeClass(grade);
        if (gradeClass != null) {
            gradeClass.put(gradeClass.size(), student);
            view.appendOutput("Enrolled " + student.studentName.getFullName() + " as a " + grade);
            return true;
        }
        
        view.appendOutput("Failed to enroll student: unknown grade level");
        return false;
    }

    /**
     * Transfers a student from one school to another.
     *
     * @param pool the student pool
     * @param student the student to transfer
     * @param fromSchool the current school
     * @param toSchool the destination school
     * @param view the game view for output
     * @return true if transfer was successful
     */
    public static boolean transferStudent(StudentPool pool, Student student, 
                                          StandardSchool fromSchool, StandardSchool toSchool, GameView view) {
        // Remove from current school's grade class
        String grade = student.studentStatistics.getGradeLevel();
        HashMap<Integer, Student> fromGradeClass = fromSchool.getStudentGradeClass(grade);
        if (fromGradeClass != null) {
            // Find and remove the student
            Integer keyToRemove = null;
            for (Map.Entry<Integer, Student> entry : fromGradeClass.entrySet()) {
                if (entry.getValue().equals(student)) {
                    keyToRemove = entry.getKey();
                    break;
                }
            }
            if (keyToRemove != null) {
                fromGradeClass.remove(keyToRemove);
            }
        }
        
        // Transfer in pool
        if (!pool.transferStudent(student, toSchool)) {
            view.appendOutput("Failed to transfer student: pool transfer failed");
            return false;
        }
        
        // Add to new school's grade class
        HashMap<Integer, Student> toGradeClass = toSchool.getStudentGradeClass(grade);
        if (toGradeClass != null) {
            toGradeClass.put(toGradeClass.size(), student);
            view.appendOutput("Transferred " + student.studentName.getFullName() + 
                    " from " + fromSchool.getSchoolName() + " to " + toSchool.getSchoolName());
            return true;
        }
        
        view.appendOutput("Failed to complete transfer: unknown grade level at destination");
        return false;
    }

    /**
     * Withdraws a student from a school (returns to unassigned pool).
     *
     * @param pool the student pool
     * @param student the student to withdraw
     * @param school the school to withdraw from
     * @param view the game view for output
     * @return true if withdrawal was successful
     */
    public static boolean withdrawStudent(StudentPool pool, Student student, StandardSchool school, GameView view) {
        // Remove from grade class
        String grade = student.studentStatistics.getGradeLevel();
        HashMap<Integer, Student> gradeClass = school.getStudentGradeClass(grade);
        if (gradeClass != null) {
            Integer keyToRemove = null;
            for (Map.Entry<Integer, Student> entry : gradeClass.entrySet()) {
                if (entry.getValue().equals(student)) {
                    keyToRemove = entry.getKey();
                    break;
                }
            }
            if (keyToRemove != null) {
                gradeClass.remove(keyToRemove);
            }
        }
        
        // Unassign in pool
        pool.unassignFromSchool(student);
        view.appendOutput("Withdrew " + student.studentName.getFullName() + " from " + school.getSchoolName());
        return true;
    }

    /**
     * Gets the count of students by grade level for a school.
     *
     * @param pool the student pool
     * @param school the school
     * @return map of grade level to student count
     */
    public static Map<String, Integer> getEnrollmentByGrade(StudentPool pool, StandardSchool school) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Freshman", 0);
        counts.put("Sophomore", 0);
        counts.put("Junior", 0);
        counts.put("Senior", 0);
        
        for (Student student : pool.getStudentsBySchool(school)) {
            String grade = student.studentStatistics.getGradeLevel();
            counts.merge(grade, 1, Integer::sum);
        }
        
        return counts;
    }
}
