package utility;

import entity.Staff;
import entity.Student;
import entity.StudentBlock;
import entity.TeacherBlock;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Verifies that students meet graduation requirements and handles
 * students with missing required classes.
 *
 * Extracted from EnhancedStudentScheduleAssigner (Phase 1e).
 */
public class GraduationVerifier {

    public static class StudentSchedulePolicyStatus {
        private final Student student;
        private final String grade;
        private final List<String> requiredClasses;
        private final List<String> requestedClasses;
        private final List<String> scheduledClasses;
        private final List<String> missingRequiredClasses;
        private final List<String> missingRequestedClasses;
        private final List<String> recoveryTargetClasses;
        private final List<String> missingOptionalClasses;
        private final boolean optionalOffBlocksAllowed;

        private StudentSchedulePolicyStatus(Student student,
                String grade,
                List<String> requiredClasses,
                List<String> requestedClasses,
                List<String> scheduledClasses,
                List<String> missingRequiredClasses,
                List<String> missingRequestedClasses,
                List<String> recoveryTargetClasses,
                List<String> missingOptionalClasses,
                boolean optionalOffBlocksAllowed) {
            this.student = student;
            this.grade = grade;
            this.requiredClasses = List.copyOf(requiredClasses);
            this.requestedClasses = List.copyOf(requestedClasses);
            this.scheduledClasses = List.copyOf(scheduledClasses);
            this.missingRequiredClasses = List.copyOf(missingRequiredClasses);
            this.missingRequestedClasses = List.copyOf(missingRequestedClasses);
            this.recoveryTargetClasses = List.copyOf(recoveryTargetClasses);
            this.missingOptionalClasses = List.copyOf(missingOptionalClasses);
            this.optionalOffBlocksAllowed = optionalOffBlocksAllowed;
        }

        public Student getStudent() {
            return student;
        }

        public String getGrade() {
            return grade;
        }

        public List<String> getRequiredClasses() {
            return requiredClasses;
        }

        public List<String> getRequestedClasses() {
            return requestedClasses;
        }

        public List<String> getScheduledClasses() {
            return scheduledClasses;
        }

        public List<String> getMissingRequiredClasses() {
            return missingRequiredClasses;
        }

        public List<String> getMissingRequestedClasses() {
            return missingRequestedClasses;
        }

        public List<String> getRecoveryTargetClasses() {
            return recoveryTargetClasses;
        }

        public List<String> getMissingOptionalClasses() {
            return missingOptionalClasses;
        }

        public boolean isOptionalOffBlocksAllowed() {
            return optionalOffBlocksAllowed;
        }

        public boolean meetsCompletionPolicy() {
            if (optionalOffBlocksAllowed) {
                return missingRequiredClasses.isEmpty();
            }
            return missingRequestedClasses.isEmpty();
        }

        public boolean shouldReturnToPool() {
            if (requestedClasses.isEmpty()) {
                return false;
            }
            if (meetsCompletionPolicy()) {
                return false;
            }
            return (double) missingRequestedClasses.size() / requestedClasses.size() > 0.5;
        }
    }

    /**
     * Functional interface for attempting to schedule a missing class.
     * This decouples GraduationVerifier from the specific assignment logic in ESSA.
     */
    @FunctionalInterface
    public interface MissingClassScheduler {
        boolean attemptToSchedule(Student student, String className, HashMap<Integer, Staff> staffHashMap);
    }

    // Reference to StudentPool for proper unassignment when students are returned
    // to pool
    private static entity.StudentPool currentStudentPool = null;

    /** Sets the student pool reference for proper unassignment. */
    public static void setStudentPool(entity.StudentPool pool) {
        currentStudentPool = pool;
    }

    /**
     * Verifies that students have all required classes for graduation.
     * Reports students with missing requirements and attempts to fix schedules.
     *
     * @param studentHashMap the enrolled students
     * @param staffHashMap   the staff
     * @param scheduler      callback to attempt scheduling a missing class
     */
    public static void verifyGraduationRequirements(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            MissingClassScheduler scheduler) {
        verifyGraduationRequirements(studentHashMap, staffHashMap, scheduler, true);
    }

    public static void verifyGraduationRequirements(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            MissingClassScheduler scheduler,
            boolean allowRemoval) {
        GameLogger.logScheduling("\n=== Verifying Graduation Requirements ===");

        Map<String, List<String>> missingByGrade = new HashMap<>();
        missingByGrade.put("Freshman", new ArrayList<>());
        missingByGrade.put("Sophomore", new ArrayList<>());
        missingByGrade.put("Junior", new ArrayList<>());
        missingByGrade.put("Senior", new ArrayList<>());

        List<Student> studentsToRemove = new ArrayList<>();
        Map<Student, List<String>> studentMissingClasses = new HashMap<>();
        int studentsRetainedWithGaps = 0;
        int studentsRetainedWithOptionalOffBlocks = 0;
        int studentsWithMissingReqs = 0;
        int totalMissingClasses = 0;

        for (Student student : studentHashMap.values()) {
            StudentSchedulePolicyStatus initialStatus = evaluateStudentSchedulePolicy(student);
            String grade = initialStatus.getGrade();

            if (!initialStatus.getMissingRequiredClasses().isEmpty()) {
                studentsWithMissingReqs++;
                totalMissingClasses += initialStatus.getMissingRequiredClasses().size();

                for (String missingClass : initialStatus.getMissingRequiredClasses()) {
                    boolean fixed = scheduler.attemptToSchedule(student, missingClass, staffHashMap);
                    if (fixed) {
                        GameLogger.logScheduling("  Filled graduation requirement for " +
                                student.studentName.getFirstName() + " " +
                                student.studentName.getLastName() + ": " + missingClass);
                    }
                }
            }

            StudentSchedulePolicyStatus finalStatus = evaluateStudentSchedulePolicy(student);
            if (!finalStatus.getMissingRequiredClasses().isEmpty()) {
                for (String missingClass : finalStatus.getMissingRequiredClasses()) {
                    missingByGrade.get(grade).add(
                            student.studentName.getFirstName() + " " +
                                    student.studentName.getLastName() + " missing " + missingClass);
                }
            }

            if (finalStatus.meetsCompletionPolicy()) {
                if (finalStatus.isOptionalOffBlocksAllowed() && !finalStatus.getMissingOptionalClasses().isEmpty()) {
                    studentsRetainedWithOptionalOffBlocks++;
                    GameLogger.logScheduling("  Allowing off blocks for " +
                            student.studentName.getFirstName() + " " +
                            student.studentName.getLastName() + " (" + grade + ") - optional gaps: " +
                            String.join(", ", finalStatus.getMissingOptionalClasses()));
                }
                continue;
            }

            if (allowRemoval && finalStatus.shouldReturnToPool()) {
                studentsToRemove.add(student);
                studentMissingClasses.put(student, finalStatus.getMissingRequestedClasses());
            } else {
                studentsRetainedWithGaps++;
                GameLogger.logScheduling("  Retaining " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " (" + grade + ") with required gaps: " +
                        String.join(", ", finalStatus.getMissingRequiredClasses()) +
                        (finalStatus.getMissingOptionalClasses().isEmpty()
                                ? ""
                                : " | optional gaps: " + String.join(", ", finalStatus.getMissingOptionalClasses())));
            }
        }

        GameLogger.logScheduling("Students with missing requirements: " + studentsWithMissingReqs);
        GameLogger.logScheduling("Total missing class assignments: " + totalMissingClasses);
        GameLogger.logScheduling("Students retained with partial schedules: " + studentsRetainedWithGaps);
        GameLogger.logScheduling("Students retained with allowed off blocks: " + studentsRetainedWithOptionalOffBlocks);

        for (Map.Entry<String, List<String>> entry : missingByGrade.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                GameLogger.logScheduling(entry.getKey() + " issues (" + entry.getValue().size() + "):");
                int count = 0;
                for (String issue : entry.getValue()) {
                    if (count < 5)
                        GameLogger.logScheduling("  - " + issue);
                    count++;
                }
                if (count > 5)
                    GameLogger.logScheduling("  ... and " + (count - 5) + " more");
            }
        }

        if (allowRemoval && !studentsToRemove.isEmpty()) {
            GameLogger.logScheduling("Returning " + studentsToRemove.size() +
                    " students with critically incomplete schedules (>50% of requests missing)");
            returnStudentsToPool(studentsToRemove, studentMissingClasses, studentHashMap);
        } else if (!allowRemoval && !studentsToRemove.isEmpty()) {
            GameLogger.logScheduling("Deferring removal for " + studentsToRemove.size() +
                    " critically incomplete students until recovery attempts are exhausted");
        } else {
            GameLogger.logScheduling("No students need to be removed - all retained with current schedules");
        }
    }

    public static StudentSchedulePolicyStatus evaluateStudentSchedulePolicy(Student student) {
        String grade = student.studentStatistics.getGradeLevel();
        List<String> requiredClasses = getRequiredClassesForGrade(grade);
        List<String> requestedClasses = new ArrayList<>(StudentClassDeterminer.determineStudentClasses(student));
        List<String> scheduledClasses = getScheduledClassNames(student);

        List<String> missingRequired = new ArrayList<>();
        for (String requirement : requiredClasses) {
            if (!hasRequiredClass(scheduledClasses, requirement)) {
                missingRequired.add(requirement);
            }
        }

        List<String> missingRequested = requestedClasses.stream()
                .filter(className -> !scheduledClasses.contains(className))
                .distinct()
                .collect(Collectors.toList());

        boolean optionalOffBlocksAllowed = "Junior".equals(grade) || "Senior".equals(grade);
        List<String> recoveryTargets = new ArrayList<>();
        Set<String> recoveryTargetSet = new LinkedHashSet<>();
        for (String requirement : missingRequired) {
            String requestedTarget = findRequestedClassForRequirement(missingRequested, requirement);
            String recoveryTarget = requestedTarget != null ? requestedTarget : requirement;
            recoveryTargets.add(recoveryTarget);
            recoveryTargetSet.add(recoveryTarget);
        }

        List<String> missingOptional = new ArrayList<>();
        if (optionalOffBlocksAllowed) {
            for (String className : missingRequested) {
                if (!recoveryTargetSet.contains(className)) {
                    missingOptional.add(className);
                }
            }
        }

        return new StudentSchedulePolicyStatus(student, grade, requiredClasses, requestedClasses, scheduledClasses,
                missingRequired, missingRequested, recoveryTargets, missingOptional, optionalOffBlocksAllowed);
    }

    // -------------------------------------------------- requirement definitions

    /** Gets the required classes for a specific grade level. */
    public static List<String> getRequiredClassesForGrade(String grade) {
        List<String> required = new ArrayList<>();
        switch (grade) {
            case "Freshman":
                required.add("English I");
                required.add("Math");
                required.add("Biology");
                required.add("History");
                required.add("Health");
                break;
            case "Sophomore":
                required.add("English II");
                required.add("Math");
                required.add("Science");
                required.add("History");
                break;
            case "Junior":
                required.add("English");
                required.add("Math");
                required.add("Science");
                required.add("US History");
                break;
            case "Senior":
                required.add("English");
                required.add("Government");
                break;
        }
        return required;
    }

    /** Gets the class names from a student's schedule. */
    public static List<String> getScheduledClassNames(Student student) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .map(StudentBlock::getClassName)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the scheduled classes include the required class (or equivalent).
     */
    public static boolean hasRequiredClass(List<String> scheduled, String required) {
        for (String className : scheduled) {
            if (classMatchesRequirement(className, required)) {
                return true;
            }
        }
        return false;
    }

    /** Finds an equivalent class for a graduation requirement. */
    public static String findEquivalentClass(String required, String grade) {
        String reqLower = required.toLowerCase();
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();

        for (String className : classSections.keySet()) {
            String classLower = className.toLowerCase();

            if (reqLower.contains("math") &&
                    (classLower.contains("algebra") || classLower.contains("geometry")))
                return className;
            if (reqLower.contains("science") &&
                    (classLower.contains("biology") || classLower.contains("chemistry")))
                return className;
            if (reqLower.contains("history") &&
                    (classLower.contains("history") || classLower.contains("geography")))
                return className;
        }
        return null;
    }

    private static boolean classMatchesRequirement(String className, String required) {
        String reqLower = required.toLowerCase();
        String classLower = className.toLowerCase();

        if (classLower.contains(reqLower)) {
            return true;
        }
        if (reqLower.equals("math")) {
            return classLower.contains("algebra") || classLower.contains("geometry") ||
                    classLower.contains("calculus") || classLower.contains("precalculus") ||
                    classLower.contains("trigonometry") || classLower.contains("statistics") ||
                    classLower.contains("fundamentals of math") || classLower.contains("financial literacy");
        }
        if (reqLower.equals("science")) {
            return classLower.contains("biology") || classLower.contains("chemistry") ||
                    classLower.contains("physics") || classLower.contains("anatomy") ||
                    classLower.contains("environmental") || classLower.contains("earth") ||
                    classLower.contains("physical science");
        }
        if (reqLower.equals("history")) {
            return classLower.contains("history") || classLower.contains("geography") ||
                    classLower.contains("government") || classLower.contains("civics");
        }
        if (reqLower.equals("english")) {
            return classLower.contains("english") || classLower.contains("literature") ||
                    classLower.contains("composition");
        }
        if (reqLower.equals("us history")) {
            return classLower.contains("us history") || classLower.contains("u.s. history") ||
                    classLower.contains("ap united states history") || classLower.contains("ap us history");
        }
        if (reqLower.equals("government")) {
            return classLower.contains("government") || classLower.contains("civics");
        }
        return false;
    }

    private static String findRequestedClassForRequirement(List<String> requestedClasses, String requirement) {
        for (String className : requestedClasses) {
            if (classMatchesRequirement(className, requirement)) {
                return className;
            }
        }
        return null;
    }

    // -------------------------------------------------- student removal

    /**
     * Returns students with missing requirements to the pool.
     */
    private static void returnStudentsToPool(List<Student> studentsToRemove,
            Map<Student, List<String>> studentMissingClasses,
            HashMap<Integer, Student> studentHashMap) {

        GameLogger.logScheduling("\n=== Returning Students with Unfulfilled Requirements to Pool ===");
        GameLogger.logScheduling("Students being returned to pool: " + studentsToRemove.size());

        Map<String, Integer> removedByGrade = new HashMap<>();
        removedByGrade.put("Freshman", 0);
        removedByGrade.put("Sophomore", 0);
        removedByGrade.put("Junior", 0);
        removedByGrade.put("Senior", 0);

        for (Student student : studentsToRemove) {
            String grade = student.studentStatistics.getGradeLevel();
            List<String> missing = studentMissingClasses.get(student);

            updateSiblingRelationshipsForRemovedStudent(student);
            student.setInHighSchool(false);
            student.studentStatistics.getStudentSchedule().clear();
            removeStudentFromAllSections(student);

            Integer keyToRemove = null;
            for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
                if (entry.getValue().equals(student)) {
                    keyToRemove = entry.getKey();
                    break;
                }
            }
            if (keyToRemove != null)
                studentHashMap.remove(keyToRemove);

            if (currentStudentPool != null) {
                currentStudentPool.unassignFromSchool(student);
            }

            removedByGrade.merge(grade, 1, Integer::sum);

            if (removedByGrade.values().stream().mapToInt(Integer::intValue).sum() <= 10) {
                GameLogger.logScheduling("  Returned to pool: " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " (" + grade + ") - missing: " +
                        String.join(", ", missing));
            }
        }

        GameLogger.logScheduling("\nStudents returned to pool by grade:");
        for (Map.Entry<String, Integer> entry : removedByGrade.entrySet()) {
            if (entry.getValue() > 0) {
                GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue());
            }
        }
        GameLogger.logScheduling("Total students returned to pool: " + studentsToRemove.size());
        GameLogger.logScheduling("These students are marked as 'not in high school' and can be re-enrolled later");
        GameLogger.logScheduling("Remaining enrolled students: " + studentHashMap.size());
    }

    /** Updates sibling relationships when a student is removed from school. */
    private static void updateSiblingRelationshipsForRemovedStudent(Student removedStudent) {
        ArrayList<Student> siblingsInSchool = removedStudent.studentStatistics.getSiblingsInSchool();
        for (Student sibling : siblingsInSchool) {
            sibling.studentStatistics.removeSiblingsInSchool(removedStudent);
            sibling.studentStatistics.addSiblingsNotInSchool(removedStudent);
        }
        ArrayList<Student> siblingsNotInSchool = removedStudent.studentStatistics.getSiblingsNotInSchool();
        for (Student sibling : siblingsNotInSchool) {
            if (sibling.studentStatistics.getSiblingsInSchool().contains(removedStudent)) {
                sibling.studentStatistics.removeSiblingsInSchool(removedStudent);
                sibling.studentStatistics.addSiblingsNotInSchool(removedStudent);
            }
        }
    }

    /** Removes a student from all class sections they were enrolled in. */
    private static void removeStudentFromAllSections(Student student) {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        for (Map.Entry<String, List<SectionManager.ClassSection>> entry : classSections.entrySet()) {
            for (SectionManager.ClassSection section : entry.getValue()) {
                if (section.getEnrolledStudents().contains(student)) {
                    section.getEnrolledStudents().remove(student);
                    TeacherBlock block = section.getTeacherBlock();
                    if (block.getClassPopulation() != null) {
                        block.getClassPopulation().remove(student);
                    }
                }
            }
        }
    }
}
