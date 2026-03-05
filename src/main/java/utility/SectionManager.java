package utility;

import entity.Staff;
import entity.StaffType;
import entity.Student;
import entity.TeacherBlock;

import java.util.*;
import java.util.stream.Collectors;

import static constants.SchedulingConstants.*;

/**
 * Manages class sections, demand tracking, and waitlists.
 * Owns the shared state that was previously scattered across
 * EnhancedStudentScheduleAssigner's static fields.
 *
 * Extracted from EnhancedStudentScheduleAssigner (Phase 1c).
 */
public class SectionManager {

    // -------------------------------------------------- shared state (formerly in
    // ESSA)
    private static final Map<String, List<ClassSection>> classSections = new HashMap<>();
    private static final Map<String, StudentDemand> demandTracker = new HashMap<>();
    private static final Map<String, Set<Student>> classWaitlists = new HashMap<>();
    private static final Map<String, ShortageInfo> criticalShortages = new HashMap<>();

    // -------------------------------------------------- promoted inner types

    /**
     * Represents a section of a class taught by a specific teacher in a specific
     * block.
     * Promoted from EnhancedStudentScheduleAssigner.ClassSection.
     */
    public static class ClassSection {
        private final String className;
        private final Staff teacher;
        private final TeacherBlock teacherBlock;
        private final int capacity;
        private final Set<Student> enrolledStudents;

        public ClassSection(String className, Staff teacher, TeacherBlock teacherBlock, int capacity) {
            this.className = className;
            this.teacher = teacher;
            this.teacherBlock = teacherBlock;
            this.capacity = capacity;
            this.enrolledStudents = new HashSet<>();
        }

        public void addStudent(Student student) {
            enrolledStudents.add(student);
        }

        public void removeStudent(Student student) {
            enrolledStudents.remove(student);
        }

        public boolean isFull() {
            return enrolledStudents.size() >= capacity;
        }

        public String getClassName() {
            return className;
        }

        public Staff getTeacher() {
            return teacher;
        }

        public TeacherBlock getTeacherBlock() {
            return teacherBlock;
        }

        public int getCapacity() {
            return capacity;
        }

        public Set<Student> getEnrolledStudents() {
            return enrolledStudents;
        }
    }

    /**
     * Tracks student demand for a class.
     * Promoted from EnhancedStudentScheduleAssigner.StudentDemand.
     */
    public record StudentDemand(String className, int totalDemand, Set<Student> interestedStudents) {
    }

    /**
     * Information about a scheduling shortage.
     * Promoted from EnhancedStudentScheduleAssigner.ShortageInfo.
     */
    public static class ShortageInfo {
        public final String className;
        public final int studentDemand;
        public final int sectionsNeeded;
        public final StaffType staffTypeRequired;

        public ShortageInfo(String className, int demand, int sections, StaffType type) {
            this.className = className;
            this.studentDemand = demand;
            this.sectionsNeeded = sections;
            this.staffTypeRequired = type;
        }

        @Override
        public String toString() {
            return className + ": " + studentDemand + " students need " + sectionsNeeded +
                    " sections (requires " + staffTypeRequired + ")";
        }
    }

    // -------------------------------------------------- lifecycle

    /** Clears all tracking state for a fresh scheduling run. */
    public static void clearAll() {
        classSections.clear();
        demandTracker.clear();
        classWaitlists.clear();
        criticalShortages.clear();
    }

    // -------------------------------------------------- state accessors

    public static Map<String, List<ClassSection>> getClassSections() {
        return classSections;
    }

    public static Map<String, StudentDemand> getDemandTracker() {
        return demandTracker;
    }

    public static Map<String, Set<Student>> getClassWaitlists() {
        return classWaitlists;
    }

    /** Gets a summary of critical shortages for reporting. */
    public static Map<String, ShortageInfo> getCriticalShortages() {
        return new HashMap<>(criticalShortages);
    }

    /** Clears tracked shortages (call before new scheduling run). */
    public static void clearShortages() {
        criticalShortages.clear();
    }

    // -------------------------------------------------- section creation

    /**
     * Creates optimal sections with minimum enrollment constraints.
     */
    public static void createOptimalSections(HashMap<Integer, Staff> staffHashMap, int currentOptimalClassSize) {
        GameLogger.logScheduling("Creating optimal sections with minimum enrollment constraints...");

        // Debug: Show what classes are actually in teacher blocks
        GameLogger.logScheduling("=== TEACHER BLOCK CLASS NAMES ===");
        Map<String, Integer> blocksByClass = new HashMap<>();
        for (Staff staff : staffHashMap.values()) {
            for (TeacherBlock block : staff.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                String className = block.getClassName();
                blocksByClass.merge(className, 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> entry : blocksByClass.entrySet()) {
            GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue() + " blocks");
        }

        for (Map.Entry<String, StudentDemand> entry : demandTracker.entrySet()) {
            String className = entry.getKey();
            StudentDemand demand = entry.getValue();

            if (className.equals("World Geography") || className.equals("Health")
                    || className.equals("AP Human Geography")) {
                GameLogger.logScheduling("DEBUG: Processing " + className + " with demand: " + demand.totalDemand());
                List<Staff> qualifiedTeachers = getQualifiedTeachers(className, staffHashMap);
                GameLogger.logScheduling(
                        "DEBUG: Found " + qualifiedTeachers.size() + " qualified teachers for " + className);
                for (Staff teacher : qualifiedTeachers) {
                    GameLogger.logScheduling("DEBUG: Teacher " + teacher.teacherName.getFirstName() + " " +
                            teacher.teacherName.getLastName() + " can teach " + className);
                }
            }

            if (isCoreSubject(className)) {
                if (demand.totalDemand() >= MIN_CLASS_SIZE) {
                    createSectionsForClass(className, demand, staffHashMap, currentOptimalClassSize);
                } else {
                    GameLogger.logScheduling("WARNING: Core class " + className +
                            " has insufficient demand: " + demand.totalDemand());
                    createSectionsForClass(className, demand, staffHashMap, currentOptimalClassSize);
                }
            } else {
                int minRequired = className.contains("AP") ? MIN_AP_CLASS_SIZE : MIN_ELECTIVE_SIZE;
                if (demand.totalDemand() >= minRequired) {
                    createSectionsForClass(className, demand, staffHashMap, currentOptimalClassSize);
                } else {
                    GameLogger.logScheduling("Canceling elective " + className +
                            " due to insufficient enrollment: " + demand.totalDemand());
                    classWaitlists.put(className, demand.interestedStudents());
                }
            }
        }

        // Debug: Compare demand vs sections created
        GameLogger.logScheduling("=== DEMAND VS SECTIONS COMPARISON ===");
        for (Map.Entry<String, StudentDemand> entry : demandTracker.entrySet()) {
            String className = entry.getKey();
            int demand = entry.getValue().totalDemand();
            List<ClassSection> sections = classSections.get(className);
            int sectionCount = sections != null ? sections.size() : 0;
            int capacity = sections != null ? sections.stream().mapToInt(s -> s.capacity).sum() : 0;

            if (demand > 0 && (sectionCount == 0 || capacity < demand * 0.5)) {
                GameLogger.logScheduling("  GAP: " + className + " - demand: " + demand +
                        ", sections: " + sectionCount + ", capacity: " + capacity);
            }
        }
    }

    /**
     * Creates sections for a specific class based on demand and available teachers.
     */
    public static void createSectionsForClass(String className, StudentDemand demand,
            HashMap<Integer, Staff> staffHashMap, int currentOptimalClassSize) {
        List<Staff> availableTeachers = getQualifiedTeachers(className, staffHashMap);

        int studentDemand = demand.totalDemand();
        int sectionsNeeded = (int) Math.ceil((double) studentDemand / currentOptimalClassSize);

        if (availableTeachers.isEmpty()) {
            StaffType neededType = CurriculumRequirementsCalculator.mapClassToStaffType(className);
            GameLogger.logScheduling("CRITICAL SHORTAGE: No qualified teachers found for " + className);
            GameLogger.logScheduling("  - Student demand: " + studentDemand + " students");
            GameLogger.logScheduling("  - Sections needed: " + sectionsNeeded);
            GameLogger.logScheduling("  - Staff type required: " + neededType);
            GameLogger.logScheduling("  - This is a " + (isCoreSubject(className) ? "CORE" : "ELECTIVE") + " subject");
            trackShortage(className, studentDemand, sectionsNeeded, neededType);
            return;
        }

        boolean isHighDemand = studentDemand > 500 || className.equals("World Geography");
        if (isHighDemand) {
            GameLogger.logScheduling("=== SECTION CREATION: " + className + " ===");
            GameLogger.logScheduling("Demand: " + studentDemand + " students, Sections needed: " + sectionsNeeded);
            GameLogger.logScheduling("Available teachers: " + availableTeachers.size());
            for (Staff teacher : availableTeachers) {
                GameLogger.logScheduling("Teacher: " + teacher.teacherName.getFirstName() + " " +
                        teacher.teacherName.getLastName());
                List<TeacherBlock> blocks = teacher.teacherStatistics.getTeacherSchedule()
                        .getBlocksByClassName(className);
                GameLogger.logScheduling("  Available blocks for " + className + ": " + blocks.size());
                for (TeacherBlock block : blocks) {
                    GameLogger.logScheduling("    " + block.getSemester() + " Block " + block.getBlockNumber() +
                            " in " + block.getRoom().getRoomName() +
                            " (capacity: " + block.getRoom().getStudentCapacity() + ")");
                }
            }
        }

        List<ClassSection> sections = new ArrayList<>();
        int totalBlocksCreated = 0;

        for (Staff teacher : availableTeachers) {
            List<TeacherBlock> availableBlocks = teacher.teacherStatistics.getTeacherSchedule()
                    .getBlocksByClassName(className);
            for (TeacherBlock block : availableBlocks) {
                int sectionCapacity = block.getRoom().getStudentCapacity();
                ClassSection section = new ClassSection(className, teacher, block, sectionCapacity);
                sections.add(section);
                totalBlocksCreated++;
                if (isHighDemand) {
                    GameLogger.logScheduling("Created section: " + className + " with " +
                            teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() +
                            " in " + block.getRoom().getRoomName() +
                            " (Block " + block.getBlockNumber() + ", " + block.getSemester() +
                            ", Capacity: " + sectionCapacity + ")");
                }
            }
        }

        classSections.put(className, sections);

        int totalCapacity = sections.stream().mapToInt(s -> s.capacity).sum();
        GameLogger.logScheduling("Created " + sections.size() + " sections for " + className +
                " (demand: " + studentDemand + ", needed: " + sectionsNeeded +
                ", capacity: " + totalCapacity + ")");

        if (totalCapacity < studentDemand) {
            int shortfall = studentDemand - totalCapacity;
            int additionalSectionsNeeded = (int) Math.ceil((double) shortfall / currentOptimalClassSize);
            GameLogger.logScheduling("CAPACITY SHORTFALL for " + className + ":");
            GameLogger.logScheduling("  - Student demand: " + studentDemand);
            GameLogger.logScheduling("  - Total capacity: " + totalCapacity);
            GameLogger.logScheduling("  - Shortfall: " + shortfall + " students");
            GameLogger.logScheduling("  - Additional sections needed: " + additionalSectionsNeeded);
            GameLogger.logScheduling("  - Sections created: " + sections.size() + "/" + sectionsNeeded + " needed");
            if (isCoreSubject(className)) {
                GameLogger.logScheduling("  - CRITICAL: This is a CORE subject - students may not graduate!");
            }
        } else if (totalBlocksCreated < sectionsNeeded) {
            GameLogger.logScheduling("NOTE: " + className + " has fewer sections than optimal (" +
                    totalBlocksCreated + "/" + sectionsNeeded +
                    ") - class sizes will exceed optimal of " + currentOptimalClassSize);
        }

        if (isHighDemand) {
            GameLogger.logScheduling("=== " + className + " Section Distribution ===");
            Map<String, Integer> blockDistribution = new HashMap<>();
            for (ClassSection section : sections) {
                String key = section.getTeacherBlock().getSemester() + " Block "
                        + section.getTeacherBlock().getBlockNumber();
                blockDistribution.put(key, blockDistribution.getOrDefault(key, 0) + 1);
            }
            for (Map.Entry<String, Integer> entry : blockDistribution.entrySet()) {
                GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue() + " section(s)");
            }
            GameLogger.logScheduling("=== End Distribution ===");
        }
    }

    /**
     * Creates language sections with strict semester requirements.
     */
    public static void createSectionsForLanguageSequence(String level1Class, String level2Class,
            int totalStudents, HashMap<Integer, Staff> staffHashMap) {
        List<Staff> level1Teachers = getQualifiedTeachers(level1Class, staffHashMap);
        List<Staff> level2Teachers = getQualifiedTeachers(level2Class, staffHashMap);

        if (level1Teachers.isEmpty() || level2Teachers.isEmpty()) {
            GameLogger.logScheduling("WARNING: No qualified teachers found for " + level1Class + " or " + level2Class);
            return;
        }

        List<Map.Entry<Staff, TeacherBlock>> level1Blocks = getTeacherBlocksForSemester(level1Teachers, level1Class, "Fall");
        List<Map.Entry<Staff, TeacherBlock>> level2Blocks = getTeacherBlocksForSemester(level2Teachers, level2Class,
                "Spring");
        if (level1Blocks.isEmpty() || level2Blocks.isEmpty()) {
            GameLogger.logScheduling("WARNING: No usable semester blocks found for " + level1Class +
                    " or " + level2Class);
            return;
        }

        int averageCapacity = (int) level1Blocks.stream()
                .mapToInt(entry -> entry.getValue().getRoom().getStudentCapacity())
                .average()
                .orElse(25);
        int neededSections = Math.max(1, (totalStudents + averageCapacity - 1) / averageCapacity);

        GameLogger.logScheduling("Creating " + neededSections + " sections each for " + level1Class +
                " (Fall) and " + level2Class + " (Spring)");

        List<ClassSection> level1Sections = new ArrayList<>();
        List<ClassSection> level2Sections = new ArrayList<>();

        for (int i = 0; i < neededSections && i < level1Blocks.size(); i++) {
            Map.Entry<Staff, TeacherBlock> teacherBlock = level1Blocks.get(i);
            Staff teacher = teacherBlock.getKey();
            TeacherBlock fallBlock = teacherBlock.getValue();
            ClassSection section = new ClassSection(level1Class, teacher, fallBlock,
                    fallBlock.getRoom().getStudentCapacity());
            level1Sections.add(section);
            GameLogger.logScheduling("Created " + level1Class + " section: Fall Block " +
                    fallBlock.getBlockNumber() + " with " +
                    teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
        }

        for (int i = 0; i < neededSections && i < level2Blocks.size(); i++) {
            Map.Entry<Staff, TeacherBlock> teacherBlock = level2Blocks.get(i);
            Staff teacher = teacherBlock.getKey();
            TeacherBlock springBlock = teacherBlock.getValue();
            ClassSection section = new ClassSection(level2Class, teacher, springBlock,
                    springBlock.getRoom().getStudentCapacity());
            level2Sections.add(section);
            GameLogger.logScheduling("Created " + level2Class + " section: Spring Block " +
                    springBlock.getBlockNumber() + " with " +
                    teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
        }

        classSections.put(level1Class, level1Sections);
        classSections.put(level2Class, level2Sections);

        GameLogger.logScheduling("Language sections created: " + level1Sections.size() + " Fall sections, " +
                level2Sections.size() + " Spring sections");
    }

    // -------------------------------------------------- teacher / section helpers

    /** Finds a teacher block in the specified semester. */
    public static TeacherBlock findBlockBySemester(Staff teacher, String className, String targetSemester) {
        List<TeacherBlock> blocks = findBlocksBySemester(teacher, className, targetSemester);
        return blocks.isEmpty() ? null : blocks.get(0);
    }

    public static List<TeacherBlock> findBlocksBySemester(Staff teacher, String className, String targetSemester) {
        List<TeacherBlock> availableBlocks = teacher.teacherStatistics.getTeacherSchedule()
                .getBlocksByClassName(className);
        return availableBlocks.stream()
                .filter(block -> block.getSemester().equals(targetSemester))
                .collect(Collectors.toList());
    }

    private static List<Map.Entry<Staff, TeacherBlock>> getTeacherBlocksForSemester(List<Staff> teachers, String className,
            String targetSemester) {
        List<Map.Entry<Staff, TeacherBlock>> blocks = new ArrayList<>();
        for (Staff teacher : teachers) {
            for (TeacherBlock block : findBlocksBySemester(teacher, className, targetSemester)) {
                blocks.add(new AbstractMap.SimpleEntry<>(teacher, block));
            }
        }
        return blocks;
    }

    /** Returns teachers that have blocks assigned for the given class. */
    public static List<Staff> getQualifiedTeachers(String className, HashMap<Integer, Staff> staffHashMap) {
        return staffHashMap.values().stream()
                .filter(teacher -> teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)
                        .size() > 0)
                .collect(Collectors.toList());
    }

    /** Calculates average room capacity across teachers. */
    public static int calculateAverageRoomCapacity(List<Staff> teachers) {
        return teachers.stream()
                .mapToInt(teacher -> teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()
                        .stream().findFirst()
                        .map(block -> block.getRoom().getStudentCapacity())
                        .orElse(25))
                .sum() / Math.max(1, teachers.size());
    }

    /** Returns the classes the student needs for the given subject area. */
    public static List<String> getStudentClassesForSubject(Student student, String subjectArea) {
        List<String> studentClasses = StudentClassDeterminer.determineStudentClasses(student);
        return studentClasses.stream()
                .filter(className -> belongsToSubjectArea(className, subjectArea))
                .collect(Collectors.toList());
    }

    /**
     * Gets all classes that belong to a subject area from current demand tracker.
     */
    public static List<String> getClassesInSubjectArea(String subjectArea) {
        return demandTracker.keySet().stream()
                .filter(className -> belongsToSubjectArea(className, subjectArea))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------- shortage tracking

    public static void trackShortage(String className, int demand, int sectionsNeeded, StaffType staffType) {
        criticalShortages.put(className, new ShortageInfo(className, demand, sectionsNeeded, staffType));
    }

    // -------------------------------------------------- classification helpers

    /**
     * Determines if a class belongs to a specific subject area.
     * Keywords are aligned with
     * CurriculumRequirementsCalculator.mapClassToStaffType().
     */
    public static boolean belongsToSubjectArea(String className, String subjectArea) {
        String lowerName = className.toLowerCase();
        return switch (subjectArea.toLowerCase()) {
            case "english" ->
                lowerName.contains("english") || lowerName.contains("literature") ||
                        lowerName.contains("composition") || lowerName.contains("journalism");
            case "math" ->
                lowerName.contains("math") || lowerName.contains("algebra") ||
                        lowerName.contains("geometry") || lowerName.contains("calculus") ||
                        lowerName.contains("trigonometry") || lowerName.contains("precalculus") ||
                        lowerName.contains("statistics") || lowerName.contains("financial");
            case "science" ->
                lowerName.contains("biology") || lowerName.contains("chemistry") ||
                        lowerName.contains("physics") || lowerName.contains("science") ||
                        lowerName.contains("anatomy") || lowerName.contains("environmental") ||
                        lowerName.contains("genetics");
            case "history" ->
                lowerName.contains("history") || lowerName.contains("government") ||
                        lowerName.contains("geography") || lowerName.contains("economics") ||
                        lowerName.contains("civics");
            case "physical education" ->
                lowerName.contains("health") || lowerName.contains("sports") ||
                        lowerName.contains("weightlifting") || lowerName.contains("dance") ||
                        lowerName.contains("recreation") || lowerName.contains("physical education") ||
                        lowerName.contains("pe");
            case "language" ->
                lowerName.contains("spanish") || lowerName.contains("french") ||
                        lowerName.contains("german") || lowerName.contains("latin") ||
                        lowerName.contains("sign language") || lowerName.contains("asl");
            case "visual arts" ->
                lowerName.contains("art") || lowerName.contains("drawing") ||
                        lowerName.contains("painting") || lowerName.contains("sculpture") ||
                        lowerName.contains("ceramics") || lowerName.contains("photography");
            case "performing arts" ->
                lowerName.contains("band") || lowerName.contains("choir") ||
                        lowerName.contains("orchestra") || lowerName.contains("music") ||
                        lowerName.contains("drama") || lowerName.contains("theater") ||
                        lowerName.contains("theatre");
            case "computer science" ->
                lowerName.contains("computer") || lowerName.contains("programming") ||
                        lowerName.contains("coding") || lowerName.contains("technology") ||
                        lowerName.contains("keyboarding");
            case "vocational" ->
                lowerName.contains("woodworking") || lowerName.contains("auto") ||
                        lowerName.contains("shop") || lowerName.contains("culinary") ||
                        lowerName.contains("welding") || lowerName.contains("construction") ||
                        lowerName.contains("hvac") || lowerName.contains("electrical");
            case "business" ->
                lowerName.contains("business") || lowerName.contains("accounting") ||
                        lowerName.contains("marketing") || lowerName.contains("entrepreneurship");
            case "consumer science" ->
                lowerName.contains("home economics") || lowerName.contains("consumer") ||
                        lowerName.contains("family") || lowerName.contains("child development");
            default -> false;
        };
    }

    /** Checks if a class is a core academic subject. */
    public static boolean isCoreSubject(String className) {
        String[] coreKeywords = { "English", "Math", "Science", "History", "Biology", "Chemistry",
                "Physics", "Algebra", "Geometry", "Calculus", "Government", "Geography" };
        return java.util.Arrays.stream(coreKeywords)
                .anyMatch(keyword -> className.toLowerCase().contains(keyword.toLowerCase()));
    }
}
