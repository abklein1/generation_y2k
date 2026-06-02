package utility;

import entity.*;
import view.GameView;

import java.util.*;
import java.util.stream.Collectors;

import static constants.SchedulingConstants.*;

/**
 * Enhanced StudentScheduleAssigner that incorporates minimum enrollment
 * requirements and load balancing while preserving all existing student
 * trait-based logic.
 *
 * After Phase 1 refactoring, this class delegates to:
 * <ul>
 * <li>{@link StudentClassDeterminer} – class determination logic</li>
 * <li>{@link TeacherBlockBuilder} – teacher block creation &amp; room
 * assignment</li>
 * <li>{@link SectionManager} – section creation &amp; tracking</li>
 * <li>{@link ScheduleOptimizer} – post-assignment optimization</li>
 * <li>{@link GraduationVerifier} – graduation verification</li>
 * </ul>
 *
 * This class retains the core student-to-section assignment pipeline.
 */
public class EnhancedStudentScheduleAssigner {

    // ================================================================
    // Public entry points
    // ================================================================

    /** Backward compatibility – calls enhanced version with null parameters. */
    public static void scheduleAllStudentsEnhanced(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        scheduleAllStudentsEnhanced(studentHashMap, staffHashMap, null, null, null, true);
    }

    /** Enhanced entry point (backward-compatible overload). */
    public static void scheduleAllStudentsEnhanced(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool,
            GameView view) {
        scheduleAllStudentsEnhanced(studentHashMap, staffHashMap, standardSchool, view, null, true);
    }

    /**
     * Enhanced entry point that performs demand analysis before assignment.
     *
     * @param studentHashMap the students to schedule
     * @param staffHashMap   the staff
     * @param standardSchool the school
     * @param view           the game view
     * @param studentPool    the student pool for proper unassignment (can be null)
     */
    public static void scheduleAllStudentsEnhanced(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool,
            GameView view,
            entity.StudentPool studentPool) {
        scheduleAllStudentsEnhanced(studentHashMap, staffHashMap, standardSchool, view, studentPool, true);
    }

    public static void scheduleAllStudentsEnhanced(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool,
            GameView view,
            entity.StudentPool studentPool,
            boolean allowCriticalRemoval) {
        GraduationVerifier.setStudentPool(studentPool);
        GameLogger.logScheduling("Starting enhanced scheduling for " + studentHashMap.size() + " students");

        // Debug: Show all staff by type
        GameLogger.logScheduling("=== STAFF BY TYPE ===");
        Map<String, Integer> staffByType = new HashMap<>();
        for (Staff staff : staffHashMap.values()) {
            Enum<?> type = staff.teacherStatistics.getStaffType();
            String typeName = type != null ? type.toString() : "NULL";
            staffByType.merge(typeName, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : staffByType.entrySet()) {
            GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue() + " staff");
        }

        // Configure class sizes based on school funding model
        if (standardSchool != null) {
            TeacherBlockBuilder.configureClassSizesFromFunding(standardSchool.getFundingModel());
        }

        // Preserve precomputed demand when the caller already analyzed this exact roster.
        if (!hasAlignedCachedDemand(studentHashMap)) {
            StudentClassDeterminer.clearCache();
        }

        // Clear all existing student schedules to prevent duplicates
        GameLogger.logScheduling("Clearing all existing student schedules...");
        for (Student student : studentHashMap.values()) {
            student.studentStatistics.getStudentSchedule().clear();
        }
        GameLogger.logScheduling("All schedules cleared - starting fresh assignment");

        // Phase 0: Analyze student demand FIRST
        GameLogger.logScheduling("=== PHASE 0: DEMAND-FIRST ANALYSIS ===");
        analyzeDemandWithTraits(studentHashMap, staffHashMap);

        // Phase 0.5: Create teacher blocks based on actual demand
        GameLogger.logScheduling("=== PHASE 0.5: DEMAND-DRIVEN TEACHER BLOCK CREATION ===");
        TeacherBlockBuilder.createDemandDrivenTeacherBlocks(studentHashMap, staffHashMap, standardSchool, view);

        // Phase 1: Refresh demand analysis
        GameLogger.logScheduling("=== PHASE 1: REFRESHING DEMAND ANALYSIS ===");

        // Phase 2: Create optimal sections based on demand-driven blocks
        SectionManager.createOptimalSections(staffHashMap, TeacherBlockBuilder.getCurrentOptimalClassSize());

        // Phase 2.5: Analyze resource shortages and reallocate substitutes
        ScheduleOptimizer.analyzeAndReallocateResources(studentHashMap, staffHashMap, standardSchool, view);

        // Phase 3: Assign students using enhanced algorithm
        assignStudentsWithOptimization(studentHashMap, staffHashMap);

        // Phase 3.5: Optimize block assignments within subject areas
        GameLogger.logScheduling("=== PHASE 3.5: POST-ASSIGNMENT BLOCK OPTIMIZATION ===");
        ScheduleOptimizer.optimizeBlockAssignmentsWithinSubjects(studentHashMap, staffHashMap);

        // Phase 4: Balance and optimize
        ScheduleOptimizer.balanceClassSizes();

        // Phase 5: Handle waitlisted students
        ScheduleOptimizer.processWaitlists(studentHashMap, staffHashMap,
                (student, className) -> findOptimalSection(student, className),
                (student, section) -> assignStudentToSection(student, section, true));

        // Final duplicate detection check
        detectAndReportDuplicates(studentHashMap);

        // Phase 6: Verify graduation requirements
        GraduationVerifier.verifyGraduationRequirements(studentHashMap, staffHashMap,
                (student, className, staff) -> attemptToScheduleMissingClass(student, className, staff),
                allowCriticalRemoval);

        printEnhancedStatistics();
    }

    // ================================================================
    // Demand analysis (uses StudentClassDeterminer + SectionManager)
    // ================================================================

    private static void analyzeDemandWithTraits(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("Analyzing student demand based on traits and requirements...");

        SectionManager.clearAll();

        Map<String, Set<Student>> classDemand = new HashMap<>();
        for (Student student : studentHashMap.values()) {
            List<String> studentClasses = StudentClassDeterminer.determineStudentClasses(student);
            for (String className : studentClasses) {
                classDemand.computeIfAbsent(className, k -> new HashSet<>()).add(student);
            }
        }

        Map<String, SectionManager.StudentDemand> demandTracker = SectionManager.getDemandTracker();
        for (Map.Entry<String, Set<Student>> entry : classDemand.entrySet()) {
            String className = entry.getKey();
            Set<Student> interestedStudents = entry.getValue();
            SectionManager.StudentDemand demand = new SectionManager.StudentDemand(
                    className, interestedStudents.size(), interestedStudents);
            demandTracker.put(className, demand);
            GameLogger.logScheduling("Demand for " + className + ": " + interestedStudents.size() + " students");
        }

        // Debug: Show language class demand
        GameLogger.logScheduling("=== LANGUAGE CLASS DEMAND ===");
        String[] languageClasses = { "Spanish I", "Spanish II", "French I", "French II",
                "German I", "German II", "Latin I", "Latin II",
                "American Sign Language I", "American Sign Language II" };
        for (String langClass : languageClasses) {
            int demand = demandTracker.containsKey(langClass) ? demandTracker.get(langClass).totalDemand() : 0;
            GameLogger.logScheduling("  " + langClass + ": " + demand + " students");
        }

        // Debug: Show science class demand
        GameLogger.logScheduling("=== SCIENCE CLASS DEMAND ===");
        String[] scienceClasses = { "Biology", "Chemistry", "Physics", "AP Biology", "AP Chemistry",
                "AP Physics B", "Environmental Science", "Anatomy and Physiology" };
        for (String sciClass : scienceClasses) {
            int demand = demandTracker.containsKey(sciClass) ? demandTracker.get(sciClass).totalDemand() : 0;
            GameLogger.logScheduling("  " + sciClass + ": " + demand + " students");
        }
    }

    // ================================================================
    // Student assignment pipeline (core logic that stays in ESSA)
    // ================================================================

    private static void assignStudentsWithOptimization(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("Assigning students with priority-based optimization (WITH DUPLICATE PREVENTION)...");

        List<Student> sortedStudents = studentHashMap.values().stream()
                .sorted((s1, s2) -> {
                    int priority1 = getGradePriority(s1.studentStatistics.getGradeLevel());
                    int priority2 = getGradePriority(s2.studentStatistics.getGradeLevel());
                    if (priority1 != priority2)
                        return Integer.compare(priority1, priority2);
                    return Integer.compare(s2.studentStatistics.getIntelligence(),
                            s1.studentStatistics.getIntelligence());
                })
                .collect(Collectors.toList());

        // PRIORITY PHASE 0: Language Assignment FIRST
        GameLogger.logScheduling("=== PRIORITY PHASE 0: Language Sequences (HIGHEST PRIORITY) ===");
        List<Student> freshmen = sortedStudents.stream()
                .filter(s -> s.studentStatistics.getGradeLevel().equals("Freshman"))
                .collect(Collectors.toList());
        if (!freshmen.isEmpty()) {
            assignSimpleLanguageSequences(freshmen, staffHashMap);
        }

        // PRIORITY PHASE 1: Core Academic Classes
        GameLogger.logScheduling("=== PRIORITY PHASE 1: Core Academic Classes ===");
        String[] coreAcademics = { "English", "Math", "Science", "History" };
        for (String subjectArea : coreAcademics) {
            GameLogger.logScheduling("Assigning " + subjectArea + " classes (CORE PRIORITY)...");
            assignSubjectWithPriorityAndRearrangement(subjectArea, sortedStudents, staffHashMap, true);
        }

        // PRIORITY PHASE 2: Required PE
        GameLogger.logScheduling("=== PRIORITY PHASE 2: Required Physical Education ===");
        assignSubjectWithPriorityAndRearrangement("Physical Education", sortedStudents, staffHashMap, true);

        // Standard language assignment for non-freshmen
        List<Student> nonFreshmen = sortedStudents.stream()
                .filter(s -> !s.studentStatistics.getGradeLevel().equals("Freshman"))
                .collect(Collectors.toList());
        if (!nonFreshmen.isEmpty()) {
            GameLogger.logScheduling("Assigning Language classes for non-freshmen (HIGH PRIORITY)...");
            assignSubjectWithPriorityAndRearrangement("Language", nonFreshmen, staffHashMap, true);
        }

        // PRIORITY PHASE 3: Electives and Vocational
        GameLogger.logScheduling("=== PRIORITY PHASE 3: Electives and Vocational ===");
        assignElectivesWithBalancing(sortedStudents, staffHashMap);

        GameLogger.logScheduling("=== Assignment Complete - Checking for Incomplete Schedules ===");
        checkForIncompleteSchedules(sortedStudents);
    }

    // ================================================================
    // Language assignment
    // ================================================================

    private static void assignSimpleLanguageSequences(List<Student> freshmen, HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("=== SIMPLIFIED LANGUAGE ASSIGNMENT: Ensuring Fall I -> Spring II ===");
        Map<String, List<Student>> languageGroups = new HashMap<>();

        for (Student student : freshmen) {
            List<String> languageClasses = getRequestedLanguageClasses(student);
            if (languageClasses.size() >= 2) {
                String languageBase = StudentClassDeterminer.getLanguageBase(languageClasses.get(0));
                languageGroups.computeIfAbsent(languageBase, k -> new ArrayList<>()).add(student);
            }
        }

        for (Map.Entry<String, List<Student>> entry : languageGroups.entrySet()) {
            String languageBase = entry.getKey();
            List<Student> students = entry.getValue();
            GameLogger.logScheduling("Processing " + languageBase + " for " + students.size() + " students");
            String level1Class = languageBase + " I";
            String level2Class = languageBase + " II";
            SectionManager.createSectionsForLanguageSequence(level1Class, level2Class, students.size(), staffHashMap);
            assignStudentsToStrictLanguageSequence(students, level1Class, level2Class, languageBase, staffHashMap);
        }
    }

    private static void assignStudentsToStrictLanguageSequence(List<Student> students, String level1Class,
            String level2Class, String languageBase, HashMap<Integer, Staff> staffHashMap) {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        List<SectionManager.ClassSection> level1Sections = classSections.get(level1Class);
        List<SectionManager.ClassSection> level2Sections = classSections.get(level2Class);

        if (level1Sections == null || level2Sections == null ||
                level1Sections.isEmpty() || level2Sections.isEmpty()) {
            GameLogger.logScheduling("ERROR: Insufficient sections for " + languageBase + " sequence");
            return;
        }

        int successCount = 0;
        int level1Index = 0;
        int level2Index = 0;

        for (Student student : students) {
            String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();
            boolean assigned = false;

            for (int attempt = 0; attempt < Math.max(level1Sections.size(), level2Sections.size())
                    && !assigned; attempt++) {
                SectionManager.ClassSection level1Section = level1Sections.get(level1Index % level1Sections.size());
                SectionManager.ClassSection level2Section = level2Sections.get(level2Index % level2Sections.size());

                boolean validSequence = level1Section.getTeacherBlock().getSemester().equals("Fall") &&
                        level2Section.getTeacherBlock().getSemester().equals("Spring");

                if (validSequence && !level1Section.isFull() && !level2Section.isFull() &&
                        !hasBlockConflict(student, level1Section.getTeacherBlock()) &&
                        !hasBlockConflict(student, level2Section.getTeacherBlock())) {
                    assignStudentToSection(student, level1Section, true);
                    assignStudentToSection(student, level2Section, true);
                    GameLogger.logScheduling("SUCCESS: " + studentName + " assigned " + languageBase +
                            " sequence [Fall " + level1Section.getTeacherBlock().getBlockNumber() +
                            " -> Spring " + level2Section.getTeacherBlock().getBlockNumber() + "]");
                    assigned = true;
                    successCount++;
                    level1Index = (level1Index + 1) % level1Sections.size();
                    level2Index = (level2Index + 1) % level2Sections.size();
                } else {
                    level1Index = (level1Index + 1) % level1Sections.size();
                    level2Index = (level2Index + 1) % level2Sections.size();
                }
            }

            if (!assigned) {
                GameLogger.logScheduling("FAILED: Could not assign " + languageBase + " sequence to " + studentName);
                trySimpleAlternativeLanguage(student, languageBase, staffHashMap);
            }
        }
        GameLogger.logScheduling("Language assignment results: " + successCount + "/" + students.size() +
                " students successfully assigned " + languageBase);
    }

    private static void trySimpleAlternativeLanguage(Student student, String failedLanguage,
            HashMap<Integer, Staff> staffHashMap) {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        String[] alternatives = { "Spanish", "French", "German", "Latin", "American Sign Language" };
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();

        for (String alt : alternatives) {
            if (alt.equals(failedLanguage))
                continue;
            String level1 = alt + " I";
            String level2 = alt + " II";
            List<SectionManager.ClassSection> alt1Sections = classSections.get(level1);
            List<SectionManager.ClassSection> alt2Sections = classSections.get(level2);

            if (alt1Sections != null && alt2Sections != null && !alt1Sections.isEmpty() && !alt2Sections.isEmpty()) {
                for (SectionManager.ClassSection s1 : alt1Sections) {
                    for (SectionManager.ClassSection s2 : alt2Sections) {
                        if (s1.getTeacherBlock().getSemester().equals("Fall") &&
                                s2.getTeacherBlock().getSemester().equals("Spring") &&
                                !s1.isFull() && !s2.isFull() &&
                                !hasBlockConflict(student, s1.getTeacherBlock()) &&
                                !hasBlockConflict(student, s2.getTeacherBlock())) {
                            assignStudentToSection(student, s1, true);
                            assignStudentToSection(student, s2, true);
                            GameLogger.logScheduling("ALTERNATIVE: " + studentName + " assigned " + alt +
                                    " sequence (fallback from " + failedLanguage + ")");
                            return;
                        }
                    }
                }
            }
        }
        GameLogger.logScheduling("CRITICAL: No language sequence available for " + studentName);
    }

    // ================================================================
    // Subject assignment with priority
    // ================================================================

    private static void assignSubjectWithPriorityAndRearrangement(String subjectArea, List<Student> students,
            HashMap<Integer, Staff> staffHashMap, boolean allowRearrangement) {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        GameLogger.logScheduling("Processing " + subjectArea + " for " + students.size() + " students (rearrangement: "
                + allowRearrangement + ")");

        for (Student student : students) {
            List<String> subjectClasses = SectionManager.getStudentClassesForSubject(student, subjectArea);

            if (!subjectClasses.isEmpty() && student.studentStatistics.getGradeLevel().equals("Freshman")) {
                GameLogger.logScheduling("Student " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " (" +
                        student.studentStatistics.getGradeLevel() + ") needs " +
                        subjectArea + " classes: " + subjectClasses);
            }

            for (String className : subjectClasses) {
                if (studentAlreadyHasClass(student, className)) {
                    if (student.studentStatistics.getGradeLevel().equals("Senior")) {
                        GameLogger.logScheduling("DUPLICATE PREVENTION: " + student.studentName.getFirstName() + " " +
                                student.studentName.getLastName() + " already has " + className +
                                " - skipping duplicate assignment");
                    }
                    continue;
                }

                boolean assigned = false;
                if (classSections.containsKey(className)) {
                    SectionManager.ClassSection bestSection = findOptimalSection(student, className);
                    if (bestSection != null) {
                        assignStudentToSection(student, bestSection, true);
                        assigned = true;
                    } else if (allowRearrangement) {
                        assigned = tryAssignWithRearrangement(student, className, subjectArea);
                    }

                    if (!assigned && student.studentStatistics.getGradeLevel().equals("Freshman")) {
                        GameLogger.logScheduling("WARNING: Could not assign " + className + " to " +
                                student.studentName.getFirstName() + " " +
                                student.studentName.getLastName() + " even with rearrangement");
                    }
                } else if (student.studentStatistics.getGradeLevel().equals("Freshman")) {
                    GameLogger.logScheduling("WARNING: No sections created for class " + className +
                            " needed by " + student.studentName.getFirstName() + " " +
                            student.studentName.getLastName());
                }
            }
        }
    }

    private static boolean tryAssignWithRearrangement(Student student, String className, String subjectArea) {
        if (tryMoveConflictingClass(student, className)) {
            return true;
        }
        return trySwapStudentIntoFullSection(student, className);
    }

    // ================================================================
    // Elective assignment
    // ================================================================

    private static void assignElectivesWithBalancing(List<Student> students, HashMap<Integer, Staff> staffHashMap) {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();

        for (Student student : students) {
            List<String> vocationalClasses = getRequestedElectiveClasses(student);

            for (String className : vocationalClasses) {
                if (studentAlreadyHasClass(student, className)) {
                    GameLogger.logScheduling("DUPLICATE PREVENTION: " + student.studentName.getFirstName() + " " +
                            student.studentName.getLastName() + " already has elective " + className +
                            " - skipping duplicate assignment");
                    continue;
                }
                if (classSections.containsKey(className)) {
                    SectionManager.ClassSection bestSection = findOptimalSection(student, className);
                    if (bestSection != null) {
                        assignStudentToSection(student, bestSection, true);
                    }
                }
            }
        }
    }

    // ================================================================
    // Core assignment methods (finding & assigning sections)
    // ================================================================

    /**
     * Finds the optimal section for a student (least filled, no conflicts).
     */
    static SectionManager.ClassSection findOptimalSection(Student student, String className) {
        List<SectionManager.ClassSection> sections = SectionManager.getClassSections().get(className);
        if (sections == null || sections.isEmpty()) {
            return null;
        }

        SectionManager.ClassSection bestSection = null;
        int minEnrollment = Integer.MAX_VALUE;

        for (SectionManager.ClassSection section : sections) {
            if (hasBlockConflict(student, section.getTeacherBlock())) {
                continue;
            }
            if (hasSubjectAreaConflict(student, className, section.getTeacherBlock().getSemester())) {
                continue;
            }
            if (section.isFull()) {
                continue;
            }

            int currentEnrollment = section.getEnrolledStudents().size();
            if (currentEnrollment < minEnrollment) {
                minEnrollment = currentEnrollment;
                bestSection = section;
            }
        }
        return bestSection;
    }

    /**
     * Central assignment method -- ALL schedule additions should go through here.
     */
    static void assignStudentToSection(Student student, SectionManager.ClassSection section, boolean logAssignment) {
        String className = section.getClassName();

        if (studentAlreadyHasClass(student, className)) {
            if (logAssignment) {
                GameLogger.logScheduling("DUPLICATE PREVENTION: " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " already has " + className + " - blocking assignment");
            }
            return;
        }

        if (hasBlockConflict(student, section.getTeacherBlock())) {
            if (logAssignment) {
                GameLogger.logScheduling("BLOCK CONFLICT PREVENTION: " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " already has a class in " +
                        section.getTeacherBlock().getSemester() + " Block " +
                        section.getTeacherBlock().getBlockNumber() + " - blocking " + className);
            }
            return;
        }

        if (hasSubjectAreaConflict(student, className, section.getTeacherBlock().getSemester())) {
            if (logAssignment) {
                GameLogger.logScheduling("SUBJECT AREA CONFLICT: " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " already has enough classes in this " +
                        "subject area for " + section.getTeacherBlock().getSemester() +
                        " - blocking " + className);
            }
            return;
        }

        StudentBlock studentBlock = new StudentBlock();
        studentBlock.setBlockNumber(section.getTeacherBlock().getBlockNumber());
        studentBlock.setClassName(section.getClassName());
        studentBlock.setTeacher(section.getTeacher());
        studentBlock.setSemester(section.getTeacherBlock().getSemester());
        studentBlock.setRoom(section.getTeacherBlock().getRoom());

        student.studentStatistics.getStudentSchedule().add(studentBlock);
        section.addStudent(student);
        section.getTeacherBlock().addStudentToBlock(student);

        if (logAssignment) {
            GameLogger.logScheduling("Assigned " + section.getClassName() + " to " +
                    student.studentName.getFirstName() + " " +
                    student.studentName.getLastName() + " with " +
                    section.getTeacher().teacherName.getFirstName() + " " +
                    section.getTeacher().teacherName.getLastName() +
                    " in room " + section.getTeacherBlock().getRoom().getRoomName() +
                    " (section enrollment: " + section.getEnrolledStudents().size() + ")");
        }
    }

    // ================================================================
    // Missing class handler (uses findOptimalSection + assignStudentToSection)
    // ================================================================

    private static boolean attemptToScheduleMissingClass(Student student, String className,
            HashMap<Integer, Staff> staffHashMap) {
        String targetClass = className;
        SectionManager.ClassSection bestSection = findOptimalSection(student, targetClass);

        if (bestSection == null) {
            String equivalent = GraduationVerifier.findEquivalentClass(className,
                    student.studentStatistics.getGradeLevel());
            if (equivalent != null && !studentAlreadyHasClass(student, equivalent)) {
                bestSection = findOptimalSection(student, equivalent);
                if (bestSection != null)
                    targetClass = equivalent;
            }
        }

        if (bestSection == null)
            return false;
        if (studentAlreadyHasClass(student, targetClass))
            return false;

        int scheduleSizeBefore = student.studentStatistics.getStudentSchedule().getClassSchedule().size();
        assignStudentToSection(student, bestSection, true);
        int scheduleSizeAfter = student.studentStatistics.getStudentSchedule().getClassSchedule().size();

        return scheduleSizeAfter > scheduleSizeBefore;
    }

    // ================================================================
    // Conflict detection helpers
    // ================================================================

    private static boolean hasBlockConflict(Student student, TeacherBlock block) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .anyMatch(studentBlock -> studentBlock.getBlockNumber() == block.getBlockNumber() &&
                        studentBlock.getSemester().equals(block.getSemester()));
    }

    static boolean hasSubjectAreaConflict(Student student, String className, String semester) {
        if (SectionManager.belongsToSubjectArea(className, "language"))
            return false;

        String[] subjectAreas = { "english", "math", "science", "history", "physical education" };
        String targetArea = null;
        for (String area : subjectAreas) {
            if (SectionManager.belongsToSubjectArea(className, area)) {
                targetArea = area;
                break;
            }
        }
        if (targetArea == null)
            return false;

        List<String> allNeeded = StudentClassDeterminer.determineStudentClasses(student);
        final String area = targetArea;
        long neededInArea = allNeeded.stream()
                .filter(c -> SectionManager.belongsToSubjectArea(c, area))
                .count();

        long alreadyScheduledInSemester = student.studentStatistics.getStudentSchedule()
                .getClassSchedule().stream()
                .filter(sb -> sb.getSemester().equals(semester) &&
                        SectionManager.belongsToSubjectArea(sb.getClassName(), area))
                .count();

        long maxPerSemester = (neededInArea + 1) / 2;
        return alreadyScheduledInSemester >= maxPerSemester;
    }

    private static boolean studentAlreadyHasClass(Student student, String className) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .anyMatch(block -> block.getClassName().equals(className));
    }

    // ================================================================
    // Helpers
    // ================================================================

    private static boolean hasAlignedCachedDemand(HashMap<Integer, Student> studentHashMap) {
        Map<Student, List<String>> cachedDemand = StudentClassDeterminer.getStudentClassCache();
        return cachedDemand.size() == studentHashMap.size()
                && cachedDemand.keySet().containsAll(studentHashMap.values());
    }

    private static List<String> getRequestedLanguageClasses(Student student) {
        return StudentClassDeterminer.determineStudentClasses(student).stream()
                .filter(className -> SectionManager.belongsToSubjectArea(className, "language"))
                .collect(Collectors.toList());
    }

    private static List<String> getRequestedElectiveClasses(Student student) {
        return StudentClassDeterminer.determineStudentClasses(student).stream()
                .filter(className -> !SectionManager.belongsToSubjectArea(className, "english"))
                .filter(className -> !SectionManager.belongsToSubjectArea(className, "math"))
                .filter(className -> !SectionManager.belongsToSubjectArea(className, "science"))
                .filter(className -> !SectionManager.belongsToSubjectArea(className, "history"))
                .filter(className -> !SectionManager.belongsToSubjectArea(className, "language"))
                .filter(className -> !SectionManager.belongsToSubjectArea(className, "physical education"))
                .collect(Collectors.toList());
    }

    private static boolean tryMoveConflictingClass(Student student, String className) {
        List<SectionManager.ClassSection> sections = SectionManager.getClassSections().get(className);
        if (sections == null) {
            return false;
        }

        for (SectionManager.ClassSection targetSection : sections) {
            if (targetSection.isFull()) {
                continue;
            }
            if (hasSubjectAreaConflict(student, className, targetSection.getTeacherBlock().getSemester())) {
                continue;
            }
            if (!hasBlockConflict(student, targetSection.getTeacherBlock())) {
                continue;
            }

            StudentBlock conflictingBlock = findConflictingScheduledBlock(student, targetSection.getTeacherBlock());
            if (conflictingBlock == null) {
                continue;
            }

            SectionManager.ClassSection currentSection = findSectionForScheduledBlock(conflictingBlock);
            if (currentSection == null) {
                continue;
            }

            SectionManager.ClassSection alternativeSection = findAlternativeSectionForStudent(
                    student, conflictingBlock.getClassName(), currentSection);
            if (alternativeSection == null) {
                continue;
            }

            moveStudentBetweenSections(student, currentSection, alternativeSection);
            if (!hasBlockConflict(student, targetSection.getTeacherBlock())) {
                assignStudentToSection(student, targetSection, true);
                GameLogger.logScheduling("REARRANGEMENT: Freed " + targetSection.getTeacherBlock().getSemester() +
                        " Block " + targetSection.getTeacherBlock().getBlockNumber() + " for " +
                        student.studentName.getFirstName() + " " + student.studentName.getLastName() +
                        " to take " + className);
                return true;
            }
        }

        return false;
    }

    private static boolean trySwapStudentIntoFullSection(Student student, String className) {
        List<SectionManager.ClassSection> sections = SectionManager.getClassSections().get(className);
        if (sections == null) {
            return false;
        }

        for (SectionManager.ClassSection fullSection : sections) {
            if (!fullSection.isFull()) {
                continue;
            }
            if (hasBlockConflict(student, fullSection.getTeacherBlock())) {
                continue;
            }
            if (hasSubjectAreaConflict(student, className, fullSection.getTeacherBlock().getSemester())) {
                continue;
            }

            for (Student enrolledStudent : new ArrayList<>(fullSection.getEnrolledStudents())) {
                SectionManager.ClassSection alternativeSection = findAlternativeSectionForStudent(
                        enrolledStudent, className, fullSection);
                if (alternativeSection == null) {
                    continue;
                }

                moveStudentBetweenSections(enrolledStudent, fullSection, alternativeSection);
                if (!fullSection.isFull()) {
                    assignStudentToSection(student, fullSection, true);
                    GameLogger.logScheduling("REARRANGEMENT: Swapped " + enrolledStudent.studentName.getFirstName() +
                            " " + enrolledStudent.studentName.getLastName() + " into another " + className +
                            " section to make room for " + student.studentName.getFirstName() + " " +
                            student.studentName.getLastName());
                    return true;
                }
            }
        }

        return false;
    }

    private static StudentBlock findConflictingScheduledBlock(Student student, TeacherBlock targetBlock) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .filter(studentBlock -> studentBlock.getBlockNumber() == targetBlock.getBlockNumber()
                        && studentBlock.getSemester().equals(targetBlock.getSemester()))
                .findFirst()
                .orElse(null);
    }

    private static SectionManager.ClassSection findAlternativeSectionForStudent(Student student, String className,
            SectionManager.ClassSection currentSection) {
        List<SectionManager.ClassSection> sections = SectionManager.getClassSections().get(className);
        if (sections == null) {
            return null;
        }

        StudentBlock currentBlock = findScheduledBlock(student, currentSection.getClassName(),
                currentSection.getTeacherBlock().getBlockNumber(), currentSection.getTeacherBlock().getSemester(),
                currentSection.getTeacher());
        if (currentBlock == null) {
            return null;
        }

        SectionManager.ClassSection bestSection = null;
        int minEnrollment = Integer.MAX_VALUE;
        for (SectionManager.ClassSection candidate : sections) {
            if (candidate == currentSection || candidate.isFull()) {
                continue;
            }
            if (hasBlockConflictExcluding(student, candidate.getTeacherBlock(), currentBlock)) {
                continue;
            }
            if (hasSubjectAreaConflictExcluding(student, className, candidate.getTeacherBlock().getSemester(),
                    currentBlock)) {
                continue;
            }

            int enrollment = candidate.getEnrolledStudents().size();
            if (enrollment < minEnrollment) {
                minEnrollment = enrollment;
                bestSection = candidate;
            }
        }

        return bestSection;
    }

    private static StudentBlock findScheduledBlock(Student student, String className, int blockNumber, String semester,
            Staff teacher) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .filter(block -> block.getClassName().equals(className)
                        && block.getBlockNumber() == blockNumber
                        && block.getSemester().equals(semester)
                        && Objects.equals(block.getTeacher(), teacher))
                .findFirst()
                .orElse(null);
    }

    private static SectionManager.ClassSection findSectionForScheduledBlock(StudentBlock scheduledBlock) {
        List<SectionManager.ClassSection> sections = SectionManager.getClassSections().get(scheduledBlock.getClassName());
        if (sections == null) {
            return null;
        }

        for (SectionManager.ClassSection section : sections) {
            TeacherBlock teacherBlock = section.getTeacherBlock();
            if (teacherBlock.getBlockNumber() == scheduledBlock.getBlockNumber()
                    && teacherBlock.getSemester().equals(scheduledBlock.getSemester())
                    && Objects.equals(section.getTeacher(), scheduledBlock.getTeacher())) {
                return section;
            }
        }
        return null;
    }

    private static boolean hasBlockConflictExcluding(Student student, TeacherBlock block, StudentBlock excludedBlock) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .filter(studentBlock -> !sameScheduledBlock(studentBlock, excludedBlock))
                .anyMatch(studentBlock -> studentBlock.getBlockNumber() == block.getBlockNumber()
                        && studentBlock.getSemester().equals(block.getSemester()));
    }

    private static boolean hasSubjectAreaConflictExcluding(Student student, String className, String semester,
            StudentBlock excludedBlock) {
        if (SectionManager.belongsToSubjectArea(className, "language")) {
            return false;
        }

        String[] subjectAreas = { "english", "math", "science", "history", "physical education" };
        String targetArea = null;
        for (String area : subjectAreas) {
            if (SectionManager.belongsToSubjectArea(className, area)) {
                targetArea = area;
                break;
            }
        }
        if (targetArea == null) {
            return false;
        }

        List<String> allNeeded = StudentClassDeterminer.determineStudentClasses(student);
        final String area = targetArea;
        long neededInArea = allNeeded.stream()
                .filter(c -> SectionManager.belongsToSubjectArea(c, area))
                .count();

        long alreadyScheduledInSemester = student.studentStatistics.getStudentSchedule()
                .getClassSchedule().stream()
                .filter(studentBlock -> !sameScheduledBlock(studentBlock, excludedBlock))
                .filter(studentBlock -> studentBlock.getSemester().equals(semester)
                        && SectionManager.belongsToSubjectArea(studentBlock.getClassName(), area))
                .count();

        long maxPerSemester = (neededInArea + 1) / 2;
        return alreadyScheduledInSemester >= maxPerSemester;
    }

    private static boolean sameScheduledBlock(StudentBlock left, StudentBlock right) {
        if (left == null || right == null) {
            return false;
        }
        return left.getClassName().equals(right.getClassName())
                && left.getBlockNumber() == right.getBlockNumber()
                && left.getSemester().equals(right.getSemester())
                && Objects.equals(left.getTeacher(), right.getTeacher());
    }

    private static void moveStudentBetweenSections(Student student, SectionManager.ClassSection fromSection,
            SectionManager.ClassSection toSection) {
        removeStudentFromSection(student, fromSection);
        assignStudentToSection(student, toSection, true);
    }

    private static void removeStudentFromSection(Student student, SectionManager.ClassSection section) {
        section.removeStudent(student);
        if (section.getTeacherBlock().getClassPopulation() != null) {
            section.getTeacherBlock().getClassPopulation().remove(student);
        }

        student.studentStatistics.getStudentSchedule().getClassSchedule().removeIf(block ->
                block.getClassName().equals(section.getClassName())
                        && block.getBlockNumber() == section.getTeacherBlock().getBlockNumber()
                        && block.getSemester().equals(section.getTeacherBlock().getSemester())
                        && Objects.equals(block.getTeacher(), section.getTeacher()));
    }

    private static int getGradePriority(String gradeLevel) {
        return switch (gradeLevel) {
            case "Senior" -> 1;
            case "Junior" -> 2;
            case "Sophomore" -> 3;
            case "Freshman" -> 4;
            default -> 5;
        };
    }

    // ================================================================
    // Reporting and diagnostics
    // ================================================================

    private static void checkForIncompleteSchedules(List<Student> students) {
        int incompleteByPolicy = 0;
        int incompleteByPeriod = 0;
        int periodConflictCount = 0;
        int optionalOffBlockCount = 0;

        for (Student student : students) {
            GraduationVerifier.StudentSchedulePolicyStatus status =
                    GraduationVerifier.evaluateStudentSchedulePolicy(student);
            List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
            int scheduleSize = schedule.size();
            String grade = status.getGrade();
            int expectedSize = StudentClassDeterminer.getExpectedScheduleSize(grade);
            String studentLabel = student.studentName.getFirstName() + " " +
                    student.studentName.getLastName() + " (" + grade + ")";

            if (!status.meetsCompletionPolicy()) {
                incompleteByPolicy++;
                GameLogger.logScheduling("INCOMPLETE SCHEDULE (policy): " + studentLabel +
                        " missing required targets " + status.getRecoveryTargetClasses() +
                        " with " + scheduleSize + "/" + expectedSize + " scheduled");
            } else if (status.isOptionalOffBlocksAllowed() && !status.getMissingOptionalClasses().isEmpty()) {
                optionalOffBlockCount++;
                GameLogger.logScheduling("OPTIONAL OFF BLOCKS: " + studentLabel +
                        " has allowed open periods due to unscheduled optional classes " +
                        status.getMissingOptionalClasses());
            }

            for (String semester : new String[] { "Fall", "Spring" }) {
                Set<Integer> coveredPeriods = new HashSet<>();
                Map<Integer, List<String>> periodClasses = new HashMap<>();
                for (StudentBlock block : schedule) {
                    if (block.getSemester().equals(semester)) {
                        coveredPeriods.add(block.getBlockNumber());
                        periodClasses.computeIfAbsent(block.getBlockNumber(), k -> new ArrayList<>())
                                .add(block.getClassName());
                    }
                }
                for (int period = 1; period <= 4; period++) {
                    if (!coveredPeriods.contains(period)) {
                        if (!status.meetsCompletionPolicy()) {
                            incompleteByPeriod++;
                            GameLogger.logScheduling("PERIOD GAP: " + studentLabel +
                                    " has no class for " + semester + " Period " + period);
                        }
                    }
                }
                for (Map.Entry<Integer, List<String>> entry : periodClasses.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        periodConflictCount++;
                        GameLogger.logScheduling("PERIOD CONFLICT: " + studentLabel +
                                " has " + entry.getValue().size() + " classes in " + semester +
                                " Period " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
                    }
                }
            }
        }

        GameLogger.logScheduling("=== SCHEDULE COMPLETENESS SUMMARY ===");
        GameLogger
                .logScheduling("Students missing completion policy: " + incompleteByPolicy + "/" + students.size());
        GameLogger.logScheduling("Students with allowed optional off blocks: " + optionalOffBlockCount);
        GameLogger.logScheduling("Total period gaps that violate policy: " + incompleteByPeriod);
        GameLogger.logScheduling("Total period conflicts (double-booked): " + periodConflictCount);
    }

    private static void detectAndReportDuplicates(HashMap<Integer, Student> studentHashMap) {
        GameLogger.logScheduling("=== FINAL SCHEDULE INTEGRITY CHECK ===");

        int studentsWithDuplicates = 0, totalDuplicates = 0;
        int studentsWithPeriodConflicts = 0, totalPeriodConflicts = 0;
        int studentsWithSubjectConflicts = 0, totalSubjectConflicts = 0;
        String[] coreSubjects = { "english", "math", "science", "history", "physical education" };

        for (Student student : studentHashMap.values()) {
            List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
            String studentLabel = student.studentName.getFirstName() + " " +
                    student.studentName.getLastName() + " (" +
                    student.studentStatistics.getGradeLevel() + ")";

            boolean hasDuplicates = false, hasPeriodConflicts = false, hasSubjectConflicts = false;

            Map<String, Integer> classCount = new HashMap<>();
            for (StudentBlock block : schedule)
                classCount.merge(block.getClassName(), 1, Integer::sum);
            for (Map.Entry<String, Integer> entry : classCount.entrySet()) {
                if (entry.getValue() > 1) {
                    if (!hasDuplicates) {
                        studentsWithDuplicates++;
                        hasDuplicates = true;
                    }
                    GameLogger.logScheduling("DUPLICATE CLASS: " + studentLabel +
                            " - " + entry.getKey() + ": " + entry.getValue() + " instances");
                    totalDuplicates += (entry.getValue() - 1);
                }
            }

            Map<String, List<String>> periodMap = new HashMap<>();
            for (StudentBlock block : schedule) {
                String key = block.getSemester() + "-" + block.getBlockNumber();
                periodMap.computeIfAbsent(key, k -> new ArrayList<>()).add(block.getClassName());
            }
            for (Map.Entry<String, List<String>> entry : periodMap.entrySet()) {
                if (entry.getValue().size() > 1) {
                    if (!hasPeriodConflicts) {
                        studentsWithPeriodConflicts++;
                        hasPeriodConflicts = true;
                    }
                    totalPeriodConflicts++;
                    GameLogger.logScheduling("PERIOD CONFLICT: " + studentLabel +
                            " - " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
                }
            }

            for (String semester : new String[] { "Fall", "Spring" }) {
                for (String subject : coreSubjects) {
                    List<String> subjectClassesInSemester = schedule.stream()
                            .filter(b -> b.getSemester().equals(semester) &&
                                    SectionManager.belongsToSubjectArea(b.getClassName(), subject))
                            .map(StudentBlock::getClassName).distinct().collect(Collectors.toList());
                    List<String> allNeeded = StudentClassDeterminer.determineStudentClasses(student);
                    long neededInArea = allNeeded.stream()
                            .filter(c -> SectionManager.belongsToSubjectArea(c, subject)).count();
                    long maxPerSemester = (neededInArea + 1) / 2;
                    if (subjectClassesInSemester.size() > maxPerSemester) {
                        if (!hasSubjectConflicts) {
                            studentsWithSubjectConflicts++;
                            hasSubjectConflicts = true;
                        }
                        totalSubjectConflicts++;
                        GameLogger.logScheduling("SUBJECT AREA CONFLICT: " + studentLabel +
                                " - " + semester + " " + subject + ": " +
                                String.join(", ", subjectClassesInSemester) +
                                " (max " + maxPerSemester + " allowed)");
                    }
                }
            }
        }

        GameLogger.logScheduling("=== SCHEDULE INTEGRITY SUMMARY ===");
        GameLogger.logScheduling("Duplicate class names: " + studentsWithDuplicates + " students, " +
                totalDuplicates + " total duplicates");
        GameLogger.logScheduling("Period conflicts: " + studentsWithPeriodConflicts + " students, " +
                totalPeriodConflicts + " total conflicts");
        GameLogger.logScheduling("Subject area conflicts: " + studentsWithSubjectConflicts + " students, " +
                totalSubjectConflicts + " total conflicts");
        int totalIssues = totalDuplicates + totalPeriodConflicts + totalSubjectConflicts;
        if (totalIssues == 0) {
            GameLogger.logScheduling("ALL SCHEDULES CLEAN - No integrity issues found!");
        } else {
            GameLogger.logScheduling(totalIssues + " ISSUES DETECTED across " + studentHashMap.size() + " students");
        }
        GameLogger.logScheduling("=== END SCHEDULE INTEGRITY CHECK ===");
    }

    private static void printEnhancedStatistics() {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        Map<String, Set<Student>> classWaitlists = SectionManager.getClassWaitlists();

        GameLogger.logScheduling("\n=== Enhanced Scheduling Statistics ===");
        int totalSections = 0, totalStudents = 0, underEnrolledSections = 0;
        int cancelledClasses = classWaitlists.size();

        for (Map.Entry<String, List<SectionManager.ClassSection>> entry : classSections.entrySet()) {
            String className = entry.getKey();
            List<SectionManager.ClassSection> sections = entry.getValue();
            totalSections += sections.size();
            int classTotal = sections.stream().mapToInt(s -> s.getEnrolledStudents().size()).sum();
            totalStudents += classTotal;
            double avgEnrollment = sections.isEmpty() ? 0 : (double) classTotal / sections.size();
            long underEnrolled = sections.stream()
                    .mapToInt(s -> s.getEnrolledStudents().size())
                    .filter(count -> count < MIN_CLASS_SIZE).count();
            underEnrolledSections += underEnrolled;
            GameLogger.logScheduling(className + ": " + sections.size() + " sections, " +
                    classTotal + " students, avg " + String.format("%.1f", avgEnrollment) + "/section" +
                    (underEnrolled > 0 ? " [" + underEnrolled + " under-enrolled]" : ""));
        }

        GameLogger.logScheduling("\nSummary:");
        GameLogger.logScheduling("Total sections created: " + totalSections);
        GameLogger.logScheduling("Total student assignments: " + totalStudents);
        GameLogger.logScheduling("Under-enrolled sections: " + underEnrolledSections);
        GameLogger.logScheduling("Cancelled classes: " + cancelledClasses);
        if (totalSections > 0) {
            GameLogger.logScheduling("Success rate: " + String.format("%.1f",
                    100.0 * (totalSections - underEnrolledSections) / totalSections)
                    + "% sections meet minimum enrollment");
        }
    }

    // ================================================================
    // Legacy compatibility: expose shortage info through SectionManager
    // ================================================================

    /** @deprecated Use {@link SectionManager#getCriticalShortages()} instead. */
    @Deprecated
    public static Map<String, SectionManager.ShortageInfo> getCriticalShortages() {
        return SectionManager.getCriticalShortages();
    }

    /** @deprecated Use {@link SectionManager#clearShortages()} instead. */
    @Deprecated
    public static void clearShortages() {
        SectionManager.clearShortages();
    }

    // Preserve ShortageInfo type alias for backward compatibility
    public static class ShortageInfo extends SectionManager.ShortageInfo {
        public ShortageInfo(String className, int demand, int sections, entity.StaffType type) {
            super(className, demand, sections, type);
        }
    }
}
