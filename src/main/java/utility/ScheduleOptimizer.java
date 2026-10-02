package utility;

import entity.Staff;
import entity.StaffType;
import entity.StandardSchool;
import entity.Student;
import entity.StudentBlock;
import entity.TeacherBlock;
import view.GameView;

import java.util.*;
import java.util.stream.Collectors;

import static constants.SchedulingConstants.*;

/**
 * Post-assignment optimization: class balancing, block reassignment,
 * resource reallocation, and waitlist processing.
 *
 * Extracted from EnhancedStudentScheduleAssigner (Phase 1d).
 */
public class ScheduleOptimizer {

    // -------------------------------------------------- inner types

    /** Tracks utilization of a class across its sections. */
    static class ClassUtilization {
        final String className;
        final int demand;
        final int totalCapacity;
        final int currentEnrollment;
        int emptyBlocks; // Mutable for optimization calculations

        ClassUtilization(String className, int demand, int totalCapacity,
                int currentEnrollment, int emptyBlocks) {
            this.className = className;
            this.demand = demand;
            this.totalCapacity = totalCapacity;
            this.currentEnrollment = currentEnrollment;
            this.emptyBlocks = emptyBlocks;
        }
    }

    record BlockReassignmentOpportunity(String fromClass, String toClass, int blocksToReassign,
            List<Staff> availableTeachers) {
    }

    /** Helper class to track resource shortages. */
    static class ResourceShortage {
        final String className;
        final int demandAmount;
        final int currentCapacity;
        final int shortageAmount;

        ResourceShortage(String className, int demandAmount, int currentCapacity, int shortageAmount) {
            this.className = className;
            this.demandAmount = demandAmount;
            this.currentCapacity = currentCapacity;
            this.shortageAmount = shortageAmount;
        }
    }

    // -------------------------------------------------- class-size balancing

    /** Load balancing after initial assignment. */
    public static void balanceClassSizes() {
        GameLogger.logScheduling("Balancing class sizes...");
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();

        for (Map.Entry<String, List<SectionManager.ClassSection>> entry : classSections.entrySet()) {
            String className = entry.getKey();
            List<SectionManager.ClassSection> sections = entry.getValue();
            if (sections.size() <= 1)
                continue;
            balanceSectionsForClass(className, sections);
        }
    }

    private static void balanceSectionsForClass(String className, List<SectionManager.ClassSection> sections) {
        int totalEnrolled = sections.stream()
                .mapToInt(s -> s.getEnrolledStudents().size())
                .sum();
        double averageEnrollment = (double) totalEnrolled / sections.size();

        for (int attempt = 0; attempt < MAX_OPTIMIZATION_ATTEMPTS; attempt++) {
            boolean madeChanges = false;
            sections.sort((s1, s2) -> Integer.compare(
                    s2.getEnrolledStudents().size(), s1.getEnrolledStudents().size()));

            for (int i = 0; i < sections.size() - 1; i++) {
                SectionManager.ClassSection overSection = sections.get(i);
                SectionManager.ClassSection underSection = sections.get(sections.size() - 1 - i);

                if (overSection.getEnrolledStudents().size() <= averageEnrollment)
                    break;
                if (underSection.getEnrolledStudents().size() >= averageEnrollment)
                    break;

                Student movableStudent = findMovableStudent(overSection, underSection);
                if (movableStudent != null) {
                    moveStudentBetweenSections(movableStudent, overSection, underSection);
                    madeChanges = true;
                }
            }
            if (!madeChanges)
                break;
        }
    }

    private static Student findMovableStudent(SectionManager.ClassSection fromSection,
            SectionManager.ClassSection toSection) {
        String fromSemester = fromSection.getTeacherBlock().getSemester();
        String toSemester = toSection.getTeacherBlock().getSemester();
        boolean crossSemester = !fromSemester.equals(toSemester);

        for (Student student : fromSection.getEnrolledStudents()) {
            if (!hasBlockConflict(student, toSection.getTeacherBlock())) {
                String className = toSection.getClassName();
                long currentCount = student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                        .mapToLong(block -> block.getClassName().equals(className) ? 1 : 0).sum();
                if (currentCount > 1)
                    continue;

                // When moving across semesters, check that the target semester
                // won't exceed the subject area limit (e.g. two math classes).
                if (crossSemester &&
                        EnhancedStudentScheduleAssigner.hasSubjectAreaConflict(student, className, toSemester)) {
                    continue;
                }

                return student;
            }
        }
        return null;
    }

    private static void moveStudentBetweenSections(Student student,
            SectionManager.ClassSection fromSection, SectionManager.ClassSection toSection) {
        String className = toSection.getClassName();
        long currentCount = student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .mapToLong(block -> block.getClassName().equals(className) ? 1 : 0).sum();
        if (currentCount > 1) {
            GameLogger.logScheduling(
                    "DUPLICATE PREVENTION: Blocking move of " + student.studentName.getFirstName() + " " +
                            student.studentName.getLastName() + " for " + className +
                            " - already has " + currentCount + " instances");
            return;
        }

        fromSection.removeStudent(student);
        if (fromSection.getTeacherBlock().getClassPopulation() != null) {
            fromSection.getTeacherBlock().getClassPopulation().removeIf(existing -> existing == student);
        }
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        schedule.removeIf(block -> block.getClassName().equals(fromSection.getClassName()) &&
                block.getBlockNumber() == fromSection.getTeacherBlock().getBlockNumber() &&
                block.getSemester().equals(fromSection.getTeacherBlock().getSemester()));

        // Re-add to new section using the standard assignment path
        StudentBlock studentBlock = new StudentBlock();
        studentBlock.setBlockNumber(toSection.getTeacherBlock().getBlockNumber());
        studentBlock.setClassName(toSection.getClassName());
        studentBlock.setTeacher(toSection.getTeacher());
        studentBlock.setSemester(toSection.getTeacherBlock().getSemester());
        studentBlock.setRoom(toSection.getTeacherBlock().getRoom());
        student.studentStatistics.getStudentSchedule().add(studentBlock);
        toSection.addStudent(student);
        toSection.getTeacherBlock().addStudentToBlock(student);

        GameLogger.logScheduling("Moved " + student.studentName.getFirstName() + " " +
                student.studentName.getLastName() + " from section " +
                fromSection.getTeacherBlock().getBlockNumber() + " to " +
                toSection.getTeacherBlock().getBlockNumber() + " for " + fromSection.getClassName());
    }

    // -------------------------------------------------- block optimization

    /**
     * Optimizes block assignments within subject areas after student assignment.
     */
    public static void optimizeBlockAssignmentsWithinSubjects(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("=== BLOCK ASSIGNMENT OPTIMIZATION WITHIN SUBJECT AREAS ===");

        String[] subjectAreas = {
                "English", "Math", "Science", "History", "Language",
                "Physical Education", "Visual Arts", "Performing Arts",
                "Computer Science", "Vocational", "Business", "Consumer Science"
        };

        for (String subjectArea : subjectAreas) {
            GameLogger.logScheduling("Optimizing " + subjectArea + " block assignments...");
            optimizeSubjectArea(subjectArea, studentHashMap, staffHashMap);
        }

        GameLogger.logScheduling("=== END BLOCK OPTIMIZATION ===");
    }

    private static void optimizeSubjectArea(String subjectArea, HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        List<String> subjectClasses = SectionManager.getClassesInSubjectArea(subjectArea);
        if (subjectClasses.isEmpty()) {
            GameLogger.logScheduling("No classes found for " + subjectArea);
            return;
        }

        Map<String, SectionManager.StudentDemand> demandTracker = SectionManager.getDemandTracker();
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        List<ClassUtilization> utilizations = new ArrayList<>();

        for (String className : subjectClasses) {
            SectionManager.StudentDemand demand = demandTracker.get(className);
            List<SectionManager.ClassSection> sections = classSections.get(className);

            if (demand != null) {
                int totalCapacity = 0, currentEnrollment = 0, emptyBlocks = 0;
                if (sections != null) {
                    totalCapacity = sections.stream().mapToInt(SectionManager.ClassSection::getCapacity).sum();
                    currentEnrollment = sections.stream().mapToInt(s -> s.getEnrolledStudents().size()).sum();
                    for (SectionManager.ClassSection section : sections) {
                        if (section.getEnrolledStudents().size() == 0)
                            emptyBlocks++;
                    }
                }
                utilizations.add(new ClassUtilization(className, demand.totalDemand(),
                        totalCapacity, currentEnrollment, emptyBlocks));
            }
        }

        GameLogger.logScheduling("=== " + subjectArea.toUpperCase() + " UTILIZATION ANALYSIS ===");
        for (ClassUtilization util : utilizations) {
            double utilizationPercent = util.totalCapacity > 0
                    ? (double) util.currentEnrollment / util.totalCapacity * 100
                    : 0;
            GameLogger.logScheduling(String.format(
                    "%s: Demand=%d, Capacity=%d, Enrolled=%d (%.1f%%), Empty blocks=%d",
                    util.className, util.demand, util.totalCapacity, util.currentEnrollment,
                    utilizationPercent, util.emptyBlocks));
        }

        List<BlockReassignmentOpportunity> opportunities = findReassignmentOpportunities(utilizations, subjectArea,
                staffHashMap);
        for (BlockReassignmentOpportunity opportunity : opportunities) {
            executeBlockReassignment(opportunity, staffHashMap);
        }

        rebalanceEmptySectionsWithStaffType(subjectArea, utilizations, staffHashMap);
    }

    // -------------------------------------------------- resource reallocation

    /**
     * Analyzes resource shortages and reallocates substitutes to address demand.
     */
    public static void analyzeAndReallocateResources(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap, StandardSchool school, GameView view) {
        GameLogger.logScheduling("=== RESOURCE ANALYSIS AND SUBSTITUTE REALLOCATION ===");

        List<Staff> availableSubstitutes = StaffAssignmentService.getTeachersOfType(staffHashMap, StaffType.SUB);
        int totalSubstitutes = availableSubstitutes.size();
        GameLogger.logScheduling("Available substitutes: " + totalSubstitutes);

        List<ResourceShortage> shortages = identifyResourceShortages();

        shortages.sort((s1, s2) -> {
            boolean s1Core = SectionManager.isCoreSubject(s1.className);
            boolean s2Core = SectionManager.isCoreSubject(s2.className);
            if (s1Core && !s2Core)
                return -1;
            if (s2Core && !s1Core)
                return 1;
            return Integer.compare(s2.shortageAmount, s1.shortageAmount);
        });

        GameLogger.logScheduling("=== DEMAND vs CAPACITY ANALYSIS ===");
        for (ResourceShortage shortage : shortages) {
            GameLogger.logScheduling(shortage.className + ": Need " + shortage.demandAmount +
                    ", Have capacity for " + shortage.currentCapacity +
                    ", Shortage: " + shortage.shortageAmount);
        }

        int substitutesUsed = 0;
        for (ResourceShortage shortage : shortages) {
            if (substitutesUsed >= totalSubstitutes) {
                GameLogger.logScheduling("No more substitutes available for reallocation");
                break;
            }
            if (shortage.shortageAmount > 0 && SectionManager.isCoreSubject(shortage.className)) {
                int teachersNeeded = calculateTeachersNeeded(shortage);
                int teachersToAllocate = Math.min(teachersNeeded, totalSubstitutes - substitutesUsed);
                if (teachersToAllocate > 0) {
                    boolean success = reallocateSubstitutesToClass(shortage.className, teachersToAllocate,
                            availableSubstitutes, substitutesUsed, staffHashMap);
                    if (success) {
                        substitutesUsed += teachersToAllocate;
                        GameLogger.logScheduling(
                                "Reallocated " + teachersToAllocate + " substitutes to " + shortage.className);
                        SectionManager.StudentDemand demand = SectionManager.getDemandTracker().get(shortage.className);
                        if (demand != null) {
                            SectionManager.getClassSections().remove(shortage.className);
                            SectionManager.createSectionsForClass(shortage.className, demand, staffHashMap,
                                    TeacherBlockBuilder.getCurrentOptimalClassSize());
                        }
                    } else {
                        GameLogger.logScheduling(
                                "Failed to reallocate substitutes to " + shortage.className + " (no available rooms)");
                    }
                }
            }
        }

        if (substitutesUsed > 0 && school != null) {
            GameLogger.logScheduling("Rebuilding teacher blocks and sections after substitute reallocation...");
            TeacherBlockBuilder.ensureTeachersHaveRooms(staffHashMap, school, view);
            SectionManager.getClassSections().clear();
            SectionManager.getClassWaitlists().clear();
            SectionManager.clearShortages();
            TeacherBlockBuilder.createDemandDrivenTeacherBlocks(studentHashMap, staffHashMap, school, view);
            SectionManager.createOptimalSections(staffHashMap, TeacherBlockBuilder.getCurrentOptimalClassSize());
        }

        GameLogger.logScheduling("=== REALLOCATION SUMMARY ===");
        GameLogger.logScheduling("Total substitutes used: " + substitutesUsed + "/" + totalSubstitutes);
        GameLogger.logScheduling("Remaining substitutes: " + (totalSubstitutes - substitutesUsed));
        GameLogger.logScheduling("=== END RESOURCE ANALYSIS ===");
    }

    // -------------------------------------------------- waitlist processing

    /** Processes waitlists for cancelled classes. */
    public static void processWaitlists(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            java.util.function.BiFunction<Student, String, SectionManager.ClassSection> sectionFinder,
            java.util.function.BiConsumer<Student, SectionManager.ClassSection> assigner) {
        GameLogger.logScheduling("Processing waitlists for cancelled classes...");
        Map<String, Set<Student>> classWaitlists = SectionManager.getClassWaitlists();
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();

        for (Map.Entry<String, Set<Student>> entry : classWaitlists.entrySet()) {
            String cancelledClass = entry.getKey();
            Set<Student> waitlistedStudents = entry.getValue();

            GameLogger.logScheduling("Finding alternatives for " + waitlistedStudents.size() +
                    " students waitlisted for " + cancelledClass);

            for (Student student : waitlistedStudents) {
                List<String> alternatives = findAlternativeClasses(cancelledClass, student);
                boolean assigned = false;
                for (String alternative : alternatives) {
                    if (classSections.containsKey(alternative)) {
                        SectionManager.ClassSection bestSection = sectionFinder.apply(student, alternative);
                        if (bestSection != null) {
                            assigner.accept(student, bestSection);
                            assigned = true;
                            break;
                        }
                    }
                }
                if (!assigned) {
                    GameLogger.logScheduling("Could not find alternative for " +
                            student.studentName.getFirstName() + " " +
                            student.studentName.getLastName() +
                            " (waitlisted for " + cancelledClass + ")");
                }
            }
        }
    }

    private static List<String> findAlternativeClasses(String cancelledClass, Student student) {
        List<String> alternatives = new ArrayList<>();
        if (cancelledClass.toLowerCase().contains("art"))
            alternatives.addAll(Arrays.asList("2D Studio Art I", "Photography I", "Digital Production Technology"));
        else if (cancelledClass.toLowerCase().contains("theater"))
            alternatives.addAll(Arrays.asList("Debate", "Choir", "Film Production"));
        else if (cancelledClass.toLowerCase().contains("music"))
            alternatives.addAll(Arrays.asList("Choir", "Concert Band", "Jazz Band"));
        else if (cancelledClass.toLowerCase().contains("programming"))
            alternatives.addAll(Arrays.asList("Digital Production Technology", "Computer Aided Drafting I"));
        return alternatives;
    }

    // -------------------------------------------------- private helpers

    private static boolean hasBlockConflict(Student student, TeacherBlock block) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .anyMatch(studentBlock -> studentBlock.getBlockNumber() == block.getBlockNumber() &&
                        studentBlock.getSemester().equals(block.getSemester()));
    }

    private static List<ResourceShortage> identifyResourceShortages() {
        List<ResourceShortage> shortages = new ArrayList<>();
        Map<String, SectionManager.StudentDemand> demandTracker = SectionManager.getDemandTracker();
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();

        for (Map.Entry<String, SectionManager.StudentDemand> entry : demandTracker.entrySet()) {
            String className = entry.getKey();
            SectionManager.StudentDemand demand = entry.getValue();
            List<SectionManager.ClassSection> sections = classSections.get(className);
            int currentCapacity = 0;
            if (sections != null) {
                currentCapacity = sections.stream().mapToInt(SectionManager.ClassSection::getCapacity).sum();
            }
            int shortage = demand.totalDemand() - currentCapacity;
            shortages.add(new ResourceShortage(className, demand.totalDemand(), currentCapacity, shortage));
        }
        return shortages;
    }

    private static int calculateTeachersNeeded(ResourceShortage shortage) {
        if (shortage.shortageAmount <= 0)
            return 0;
        int studentsPerTeacherPerClass = 50;
        return (int) Math.ceil((double) shortage.shortageAmount / studentsPerTeacherPerClass);
    }

    private static boolean reallocateSubstitutesToClass(String className, int teachersNeeded,
            List<Staff> availableSubstitutes, int startIndex,
            HashMap<Integer, Staff> staffHashMap) {
        StaffType targetType = determineStaffTypeForClass(className);
        if (targetType == null) {
            GameLogger.logScheduling("Cannot determine staff type for " + className);
            return false;
        }
        for (int i = 0; i < teachersNeeded && (startIndex + i) < availableSubstitutes.size(); i++) {
            Staff substitute = availableSubstitutes.get(startIndex + i);
            substitute.teacherStatistics.setStaffType(targetType);
            GameLogger.logScheduling("Reallocated substitute " + substitute.teacherName.getFirstName() + " " +
                    substitute.teacherName.getLastName() + " to " + targetType + " for " + className);
        }
        return true;
    }

    private static StaffType determineStaffTypeForClass(String className) {
        if (SectionManager.belongsToSubjectArea(className, "english"))
            return StaffType.ENGLISH;
        if (SectionManager.belongsToSubjectArea(className, "math"))
            return StaffType.MATH;
        if (SectionManager.belongsToSubjectArea(className, "science"))
            return StaffType.SCIENCE;
        if (SectionManager.belongsToSubjectArea(className, "history"))
            return StaffType.HISTORY;
        if (SectionManager.belongsToSubjectArea(className, "language"))
            return StaffType.LANGUAGES;
        if (SectionManager.belongsToSubjectArea(className, "physical education"))
            return StaffType.PHYSICAL_ED;
        if (className.toLowerCase().contains("art"))
            return StaffType.VISUAL_ARTS;
        if (className.toLowerCase().contains("music") || className.toLowerCase().contains("band") ||
                className.toLowerCase().contains("theater") || className.toLowerCase().contains("choir"))
            return StaffType.PERFORMING_ARTS;
        if (className.toLowerCase().contains("business"))
            return StaffType.BUSINESS;
        return StaffType.VOCATIONAL;
    }

    // -------------------------------------------------- block reassignment

    private static List<BlockReassignmentOpportunity> findReassignmentOpportunities(
            List<ClassUtilization> utilizations, String subjectArea, HashMap<Integer, Staff> staffHashMap) {
        List<BlockReassignmentOpportunity> opportunities = new ArrayList<>();

        List<ClassUtilization> underutilized = utilizations.stream()
                .filter(u -> u.emptyBlocks > 0)
                .sorted((u1, u2) -> Integer.compare(u2.emptyBlocks, u1.emptyBlocks)).toList();

        List<ClassUtilization> overdemanded = utilizations.stream()
                .filter(u -> u.demand > u.totalCapacity)
                .sorted((u1, u2) -> Integer.compare((u2.demand - u2.totalCapacity), (u1.demand - u1.totalCapacity)))
                .toList();

        GameLogger.logScheduling("Classes with empty blocks: " + underutilized.size());
        GameLogger.logScheduling("Classes with unmet demand: " + overdemanded.size());

        for (ClassUtilization overdemand : overdemanded) {
            for (ClassUtilization underutil : underutilized) {
                if (underutil.emptyBlocks > 0) {
                    List<Staff> sharedTeachers = findTeachersWhoCanTeachBoth(underutil.className, overdemand.className,
                            staffHashMap);
                    if (!sharedTeachers.isEmpty()) {
                        int blocksToReassign = Math.min(underutil.emptyBlocks,
                                Math.min(sharedTeachers.size(),
                                        (overdemand.demand - overdemand.totalCapacity + 24) / 25));
                        if (blocksToReassign > 0) {
                            opportunities.add(new BlockReassignmentOpportunity(
                                    underutil.className, overdemand.className, blocksToReassign,
                                    sharedTeachers.subList(0, Math.min(blocksToReassign, sharedTeachers.size()))));
                            underutil.emptyBlocks -= blocksToReassign;
                            GameLogger.logScheduling("Found opportunity: Reassign " + blocksToReassign +
                                    " blocks from " + underutil.className + " to " + overdemand.className);
                        }
                    }
                }
            }
        }
        return opportunities;
    }

    private static void executeBlockReassignment(BlockReassignmentOpportunity opportunity,
            HashMap<Integer, Staff> staffHashMap) {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        GameLogger.logScheduling("Executing reassignment: " + opportunity.blocksToReassign +
                " blocks from " + opportunity.fromClass + " to " + opportunity.toClass);

        int blocksReassigned = 0;
        for (Staff teacher : opportunity.availableTeachers) {
            if (blocksReassigned >= opportunity.blocksToReassign)
                break;
            List<TeacherBlock> fromBlocks = teacher.teacherStatistics.getTeacherSchedule()
                    .getBlocksByClassName(opportunity.fromClass);
            for (TeacherBlock block : fromBlocks) {
                if (blocksReassigned >= opportunity.blocksToReassign)
                    break;
                List<SectionManager.ClassSection> fromSections = classSections.get(opportunity.fromClass);
                if (fromSections != null) {
                    SectionManager.ClassSection correspondingSection = fromSections.stream()
                            .filter(s -> s.getTeacherBlock().equals(block)).findFirst().orElse(null);
                    if (correspondingSection != null && correspondingSection.getEnrolledStudents().size() == 0) {
                        block.setClassName(opportunity.toClass);
                        blocksReassigned++;
                        GameLogger.logScheduling("Reassigned " + teacher.teacherName.getFirstName() + " " +
                                teacher.teacherName.getLastName() + "'s " + block.getSemester() +
                                " Block " + block.getBlockNumber() + " from " + opportunity.fromClass +
                                " to " + opportunity.toClass + " (was completely empty)");
                    }
                }
            }
        }

        if (blocksReassigned > 0) {
            GameLogger.logScheduling("Successfully reassigned " + blocksReassigned + " blocks");
            List<SectionManager.ClassSection> fromSections = classSections.get(opportunity.fromClass);
            if (fromSections != null) {
                fromSections.removeIf(s -> s.getEnrolledStudents().isEmpty() &&
                        s.getTeacherBlock().getClassName().equals(opportunity.toClass));
            }
            for (Staff teacher : opportunity.availableTeachers) {
                for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule()
                        .getBlocksByClassName(opportunity.toClass)) {
                    List<SectionManager.ClassSection> toSections = classSections.computeIfAbsent(
                            opportunity.toClass, k -> new ArrayList<>());
                    boolean alreadyExists = toSections.stream()
                            .anyMatch(s -> s.getTeacherBlock().equals(block));
                    if (!alreadyExists) {
                        SectionManager.ClassSection newSection = new SectionManager.ClassSection(
                                opportunity.toClass, teacher, block,
                                block.getRoom() != null ? block.getRoom().getStudentCapacity() : 25);
                        toSections.add(newSection);
                        GameLogger.logScheduling("Created new section for " + opportunity.toClass +
                                " (" + block.getSemester() + " Block " + block.getBlockNumber() + ")");
                    }
                }
            }
        }
    }

    private static void rebalanceEmptySectionsWithStaffType(String subjectArea,
            List<ClassUtilization> utilizations, HashMap<Integer, Staff> staffHashMap) {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();

        List<ClassUtilization> stillEmpty = utilizations.stream()
                .filter(u -> u.emptyBlocks > 0)
                .sorted((u1, u2) -> Integer.compare(u2.emptyBlocks, u1.emptyBlocks))
                .collect(Collectors.toList());

        List<ClassUtilization> stillOverdemanded = utilizations.stream()
                .filter(u -> u.demand > u.currentEnrollment)
                .sorted((u1, u2) -> Integer.compare(
                        (u2.demand - u2.currentEnrollment), (u1.demand - u1.currentEnrollment)))
                .collect(Collectors.toList());

        if (stillEmpty.isEmpty() || stillOverdemanded.isEmpty())
            return;

        GameLogger.logScheduling("=== STAFFTYPE-BASED REBALANCING FOR " + subjectArea.toUpperCase() + " ===");
        GameLogger.logScheduling("Still empty sections: " + stillEmpty.stream().mapToInt(u -> u.emptyBlocks).sum());
        GameLogger.logScheduling("Classes with unmet demand: " + stillOverdemanded.size());

        int totalReassigned = 0;
        for (ClassUtilization overdemand : stillOverdemanded) {
            if (overdemand.demand <= overdemand.currentEnrollment)
                continue;
            StaffType targetType = CurriculumRequirementsCalculator.mapClassToStaffType(overdemand.className);

            for (ClassUtilization underutil : stillEmpty) {
                if (underutil.emptyBlocks <= 0)
                    continue;
                List<Staff> teachersWithEmptyBlocks = findTeachersWithEmptyBlocksInDiscipline(
                        underutil.className, targetType, staffHashMap);

                for (Staff teacher : teachersWithEmptyBlocks) {
                    if (underutil.emptyBlocks <= 0 || overdemand.demand <= overdemand.currentEnrollment)
                        break;
                    TeacherBlock emptyBlock = findEmptyBlockForClass(teacher, underutil.className);
                    if (emptyBlock != null) {
                        String oldClassName = emptyBlock.getClassName();
                        emptyBlock.setClassName(overdemand.className);
                        GameLogger.logScheduling("StaffType-Reassigned: " + teacher.teacherName.getFirstName() + " " +
                                teacher.teacherName.getLastName() + "'s " + emptyBlock.getSemester() +
                                " Block " + emptyBlock.getBlockNumber() + " from " + oldClassName +
                                " to " + overdemand.className);
                        underutil.emptyBlocks--;
                        totalReassigned++;
                        SectionManager.ClassSection newSection = new SectionManager.ClassSection(
                                overdemand.className, teacher, emptyBlock, emptyBlock.getBlockPopulation());
                        classSections.computeIfAbsent(overdemand.className, k -> new ArrayList<>()).add(newSection);
                    }
                }
            }
        }
        if (totalReassigned > 0) {
            GameLogger.logScheduling("Total blocks reassigned via StaffType flexibility: " + totalReassigned);
        }
    }

    private static List<Staff> findTeachersWithEmptyBlocksInDiscipline(String className, StaffType targetType,
            HashMap<Integer, Staff> staffHashMap) {
        List<Staff> result = new ArrayList<>();
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();

        for (Staff staff : staffHashMap.values()) {
            StaffType staffType = (StaffType) staff.teacherStatistics.getStaffType();
            if (staffType != targetType)
                continue;
            List<TeacherBlock> blocks = staff.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className);
            for (TeacherBlock block : blocks) {
                List<SectionManager.ClassSection> sections = classSections.get(className);
                if (sections != null) {
                    for (SectionManager.ClassSection section : sections) {
                        if (section.getTeacherBlock().equals(block) && section.getEnrolledStudents().size() == 0) {
                            result.add(staff);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static TeacherBlock findEmptyBlockForClass(Staff teacher, String className) {
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        List<TeacherBlock> blocks = teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className);
        for (TeacherBlock block : blocks) {
            List<SectionManager.ClassSection> sections = classSections.get(className);
            if (sections != null) {
                for (SectionManager.ClassSection section : sections) {
                    if (section.getTeacherBlock().equals(block) && section.getEnrolledStudents().size() == 0) {
                        sections.remove(section);
                        return block;
                    }
                }
            }
        }
        return null;
    }

    private static List<Staff> findTeachersWhoCanTeachBoth(String fromClass, String toClass,
            HashMap<Integer, Staff> staffHashMap) {
        List<Staff> qualifiedTeachers = new ArrayList<>();
        Map<String, List<SectionManager.ClassSection>> classSections = SectionManager.getClassSections();
        StaffType toClassType = CurriculumRequirementsCalculator.mapClassToStaffType(toClass);
        StaffType fromClassType = CurriculumRequirementsCalculator.mapClassToStaffType(fromClass);

        for (Staff teacher : staffHashMap.values()) {
            StaffType teacherType = (StaffType) teacher.teacherStatistics.getStaffType();
            boolean canTeachFromClass = (teacherType == fromClassType || teacherType == StaffType.SUB);
            boolean canTeachToClass = (teacherType == toClassType || teacherType == StaffType.SUB);
            if (!canTeachFromClass || !canTeachToClass)
                continue;

            List<TeacherBlock> fromBlocks = teacher.teacherStatistics.getTeacherSchedule()
                    .getBlocksByClassName(fromClass);
            boolean hasEmptyBlocks = false;
            for (TeacherBlock block : fromBlocks) {
                List<SectionManager.ClassSection> sections = classSections.get(fromClass);
                if (sections != null) {
                    for (SectionManager.ClassSection section : sections) {
                        if (section.getTeacherBlock().equals(block) && section.getEnrolledStudents().size() == 0) {
                            hasEmptyBlocks = true;
                            break;
                        }
                    }
                }
                if (hasEmptyBlocks)
                    break;
            }
            if (hasEmptyBlocks)
                qualifiedTeachers.add(teacher);
        }
        return qualifiedTeachers;
    }
}
