package entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages a pool of students with tracking for school assignments.
 * This allows students to exist independently of schools and be assigned/transferred as needed.
 */
public class StudentPool implements Serializable {

    private static final long serialVersionUID = 1L;

    // All students in the pool, keyed by a unique ID
    private final HashMap<Integer, Student> allStudents;

    // Tracks which school each student is assigned to (null = unassigned)
    private final Map<Student, StandardSchool> studentAssignments;

    // Counter for generating unique student IDs
    private int nextStudentId;

    /**
     * Creates a new empty StudentPool.
     */
    public StudentPool() {
        this.allStudents = new HashMap<>();
        this.studentAssignments = new HashMap<>();
        this.nextStudentId = 0;
    }

    // ==================== Adding Students ====================

    /**
     * Adds a student to the pool.
     *
     * @param student the student to add
     * @return the ID assigned to the student
     */
    public int addStudent(Student student) {
        int id = nextStudentId++;
        allStudents.put(id, student);
        studentAssignments.put(student, null); // Initially unassigned
        return id;
    }

    /**
     * Adds multiple students to the pool.
     *
     * @param students the list of students to add
     */
    public void addStudents(List<Student> students) {
        for (Student student : students) {
            addStudent(student);
        }
    }

    /**
     * Adds students from a HashMap (for compatibility with existing code).
     *
     * @param studentHashMap the HashMap of students to add
     */
    public void addStudentsFromMap(HashMap<Integer, Student> studentHashMap) {
        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            // Use the existing key if it's larger than our counter
            int id = entry.getKey();
            if (id >= nextStudentId) {
                nextStudentId = id + 1;
            }
            allStudents.put(id, entry.getValue());
            studentAssignments.put(entry.getValue(), null);
        }
    }

    // ==================== Assignment Management ====================

    /**
     * Assigns a student to a school.
     *
     * @param student the student to assign
     * @param school the school to assign to
     * @return true if assignment was successful, false if student not in pool
     */
    public boolean assignToSchool(Student student, StandardSchool school) {
        if (!studentAssignments.containsKey(student)) {
            return false;
        }
        studentAssignments.put(student, school);
        return true;
    }

    /**
     * Unassigns a student from their current school.
     *
     * @param student the student to unassign
     * @return the school they were assigned to, or null if not assigned
     */
    public StandardSchool unassignFromSchool(Student student) {
        StandardSchool previousSchool = studentAssignments.get(student);
        if (previousSchool != null) {
            studentAssignments.put(student, null);
        }
        return previousSchool;
    }

    /**
     * Transfers a student from one school to another.
     *
     * @param student the student to transfer
     * @param newSchool the new school
     * @return true if transfer was successful
     */
    public boolean transferStudent(Student student, StandardSchool newSchool) {
        if (!studentAssignments.containsKey(student)) {
            return false;
        }
        studentAssignments.put(student, newSchool);
        return true;
    }

    /**
     * Gets the school a student is assigned to.
     *
     * @param student the student
     * @return the school, or null if unassigned
     */
    public StandardSchool getAssignedSchool(Student student) {
        return studentAssignments.get(student);
    }

    /**
     * Checks if a student is assigned to any school.
     *
     * @param student the student
     * @return true if assigned to a school
     */
    public boolean isAssigned(Student student) {
        return studentAssignments.get(student) != null;
    }

    // ==================== Querying Students ====================

    /**
     * Gets all students in the pool.
     *
     * @return HashMap of all students
     */
    public HashMap<Integer, Student> getAllStudents() {
        return new HashMap<>(allStudents);
    }

    /**
     * Gets all unassigned students.
     *
     * @return list of unassigned students
     */
    public List<Student> getUnassignedStudents() {
        return studentAssignments.entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Gets all students assigned to a specific school.
     *
     * @param school the school
     * @return list of students assigned to that school
     */
    public List<Student> getStudentsBySchool(StandardSchool school) {
        return studentAssignments.entrySet().stream()
                .filter(entry -> school.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Gets students assigned to a school as a HashMap (for compatibility).
     *
     * @param school the school
     * @return HashMap of students assigned to that school
     */
    public HashMap<Integer, Student> getStudentsBySchoolAsMap(StandardSchool school) {
        HashMap<Integer, Student> result = new HashMap<>();
        int index = 0;
        for (Map.Entry<Integer, Student> entry : allStudents.entrySet()) {
            Student student = entry.getValue();
            if (school.equals(studentAssignments.get(student))) {
                result.put(index++, student);
            }
        }
        return result;
    }

    /**
     * Gets unassigned students by grade level.
     *
     * @param gradeLevel the grade level (e.g., "Freshman", "Sophomore", etc.)
     * @return list of unassigned students at that grade level
     */
    public List<Student> getUnassignedByGradeLevel(String gradeLevel) {
        return getUnassignedStudents().stream()
                .filter(s -> gradeLevel.equals(s.studentStatistics.getGradeLevel()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all students by grade level (regardless of assignment).
     *
     * @param gradeLevel the grade level
     * @return list of students at that grade level
     */
    public List<Student> getByGradeLevel(String gradeLevel) {
        return allStudents.values().stream()
                .filter(s -> gradeLevel.equals(s.studentStatistics.getGradeLevel()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all students who are of high school age (eligible for scheduling).
     *
     * @return list of high school age students
     */
    public List<Student> getHighSchoolStudents() {
        return allStudents.values().stream()
                .filter(Student::isInHighSchool)
                .collect(Collectors.toList());
    }

    /**
     * Gets all students who are NOT of high school age (not eligible for scheduling).
     * These are typically younger or older siblings.
     *
     * @return list of non-high-school age students
     */
    public List<Student> getNonHighSchoolStudents() {
        return allStudents.values().stream()
                .filter(s -> !s.isInHighSchool())
                .collect(Collectors.toList());
    }

    /**
     * Gets the count of high school age students.
     *
     * @return count of high school age students
     */
    public int getHighSchoolCount() {
        return (int) allStudents.values().stream()
                .filter(Student::isInHighSchool)
                .count();
    }

    /**
     * Gets the count of non-high-school age students.
     *
     * @return count of non-high-school age students
     */
    public int getNonHighSchoolCount() {
        return (int) allStudents.values().stream()
                .filter(s -> !s.isInHighSchool())
                .count();
    }

    // ==================== Statistics ====================

    /**
     * Gets the total number of students in the pool.
     *
     * @return total student count
     */
    public int getTotalCount() {
        return allStudents.size();
    }

    /**
     * Gets the number of unassigned students.
     *
     * @return unassigned student count
     */
    public int getUnassignedCount() {
        return (int) studentAssignments.values().stream()
                .filter(school -> school == null)
                .count();
    }

    /**
     * Gets the number of students assigned to a specific school.
     *
     * @param school the school
     * @return assigned student count for that school
     */
    public int getAssignedCount(StandardSchool school) {
        return (int) studentAssignments.values().stream()
                .filter(s -> school.equals(s))
                .count();
    }

    /**
     * Checks if there are any unassigned students.
     *
     * @return true if there are unassigned students
     */
    public boolean hasUnassigned() {
        return studentAssignments.values().stream().anyMatch(school -> school == null);
    }

    /**
     * Gets a student by their ID.
     *
     * @param id the student ID
     * @return the student, or null if not found
     */
    public Student getStudentById(int id) {
        return allStudents.get(id);
    }

    /**
     * Removes a student from the pool entirely.
     *
     * @param student the student to remove
     * @return true if the student was removed
     */
    public boolean removeStudent(Student student) {
        Integer keyToRemove = null;
        for (Map.Entry<Integer, Student> entry : allStudents.entrySet()) {
            if (entry.getValue().equals(student)) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove != null) {
            allStudents.remove(keyToRemove);
            studentAssignments.remove(student);
            return true;
        }
        return false;
    }

    /**
     * Clears all students from the pool.
     */
    public void clear() {
        allStudents.clear();
        studentAssignments.clear();
        nextStudentId = 0;
    }

    @Override
    public String toString() {
        return "StudentPool{" +
                "total=" + getTotalCount() +
                ", highSchool=" + getHighSchoolCount() +
                ", nonHighSchool=" + getNonHighSchoolCount() +
                ", unassigned=" + getUnassignedCount() +
                '}';
    }
}
