package utility;

import config.SchoolFundingModel;
import entity.Staff;
import entity.StaffType;
import entity.StandardSchool;
import entity.Student;
import entity.Town;
import view.GameView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.SchoolConstants.TOTAL_SCHOOL_PERIODS;

/**
 * Main orchestration service for assigning populations to schools.
 * Coordinates student and staff assignment using the Town population pools.
 * 
 * Supports two modes:
 * 1. Capacity-based (legacy): Assigns students up to school capacity
 * 2. Demand-driven (new): Analyzes curriculum needs first, then assigns staff
 * by type
 */
public class SchoolAssignmentService {

    // ==================== New Demand-First Pipeline (Phase 3a)
    // ====================

    /**
     * Populates a school using the demand-first pipeline.
     * <p>
     * Flow:
     * <ol>
     * <li>Assign students to school from pool</li>
     * <li>Determine class lists for all enrolled students</li>
     * <li>Aggregate demand via {@link DemandAnalyzer}</li>
     * <li>Generate/assign teachers to meet demand</li>
     * <li>Build/adapt school rooms based on demand</li>
     * <li>Create teacher blocks and sections</li>
     * <li>Assign students to sections (simplified ESSA)</li>
     * <li>Optimize and verify</li>
     * <li>Seat students</li>
     * </ol>
     *
     * @param town   the town containing population pools
     * @param school the school to populate
     * @param view   the game view for output
     */
    public static void populateSchoolFromStudentDemand(Town town, StandardSchool school, GameView view) {
        view.appendOutput("Populating " + school.getSchoolName() + " using demand-first pipeline...");

        // Add school to town if not already present
        if (!town.getSchools().contains(school)) {
            town.addSchool(school);
        }

        SchoolFundingModel fundingModel = school.getFundingModel();

        // 1. Assign students to school from pool
        view.appendOutput("Step 1: Assigning students to school...");
        int maxStudents = fundingModel.isAllowOvercrowding()
                ? fundingModel.getMaxAllowedEnrollment(school.getPhysicalCapacity())
                : school.getTargetEnrollment();
        int enrolled = StudentAssignmentService.assignStudentsToSchool(
                town.getStudentPool(), school, maxStudents, view);
        school.setCurrentEnrollment(enrolled);
        view.appendOutput("  Assigned " + enrolled + " students");

        // 2. Determine class lists for all enrolled students
        view.appendOutput("Step 2: Determining class lists...");
        HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
        StudentClassDeterminer.clearCache();
        Map<Student, List<String>> classLists = StudentClassDeterminer.determineAllClasses(studentMap);

        // 3. Aggregate demand
        view.appendOutput("Step 3: Aggregating demand...");
        DemandAnalyzer.DemandResult demand = DemandAnalyzer.analyze(classLists, fundingModel);
        DemandAnalyzer.logDemandSummary(demand);

        // 4. Generate/assign teachers to meet demand
        view.appendOutput("Step 4: Assigning staff by demand...");
        int staffAssigned = StaffAssignmentService.assignStaffByDemand(
                town.getStaffPool(), school, demand.staffNeeds(), view);

        // 5. Build/adapt school rooms based on demand
        view.appendOutput("Step 5: Adapting school rooms to demand...");
        Director.adaptSchoolToDemand(school, demand.roomNeeds(), view);

        // 6. Create teacher blocks and sections
        view.appendOutput("Step 6: Creating teacher blocks and sections...");
        HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);

        // Populate the SectionManager demand tracker from the DemandResult
        DemandAnalyzer.populateSectionManagerDemand(demand);

        // Configure class sizes from funding
        TeacherBlockBuilder.configureClassSizesFromFunding(fundingModel);
        TeacherBlockBuilder.createDemandDrivenTeacherBlocks(studentMap, staffMap, school, view);
        SectionManager.createOptimalSections(staffMap, TeacherBlockBuilder.getCurrentOptimalClassSize());

        // 7. Assign students to sections (simplified ESSA)
        view.appendOutput("Step 7: Assigning students to sections...");
        EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                studentMap, staffMap, school, view, town.getStudentPool(), false);
        StudentAssignmentService.syncSchoolEnrollmentFromPool(town.getStudentPool(), school, view);

        // 8. Optimize and verify (already done inside ESSA, but log completion)
        view.appendOutput("Step 8: Optimization and verification complete.");

        // 9. Seat students
        view.appendOutput("Step 9: Assigning seating...");
        StudentSeatingAssigner.seatInitialStudents(school);

        view.appendOutput("Demand-first pipeline complete: " + enrolled + " students, " +
                staffAssigned + " staff");
    }

    // ==================== Legacy Methods (deprecated in Phase 3b)
    // ====================

    /**
     * Fully populates a school from the town's population pools using the legacy
     * capacity-based approach.
     *
     * @deprecated Use
     *             {@link #populateSchoolFromStudentDemand(Town, StandardSchool, GameView)}
     *             instead for the demand-first pipeline.
     *
     * @param town   the town containing population pools
     * @param school the school to populate
     * @param view   the game view for output
     */
    @Deprecated
    public static void populateSchool(Town town, StandardSchool school, GameView view) {
        view.appendOutput("Populating " + school.getSchoolName() + " from town pool...");

        // Add school to town if not already present
        if (!town.getSchools().contains(school)) {
            town.addSchool(school);
        }

        // Get school capacity for student assignment (now uses optimal capacity)
        int studentCapacity = school.getOptimalCapacity();

        // Assign students first
        int studentsAssigned = StudentAssignmentService.assignStudentsToSchool(
                town.getStudentPool(), school, studentCapacity, view);

        // Update school enrollment count
        school.setCurrentEnrollment(studentsAssigned);

        // Assign staff based on student count and school needs
        int staffAssigned = StaffAssignmentService.assignStaffToSchool(
                town, school, studentsAssigned, view);

        // Schedule students (includes class scheduling via
        // createDemandDrivenTeacherBlocks)
        HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
        HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);

        try {
            view.appendOutput("Scheduling students...");
            EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                    studentMap, staffMap, school, view, town.getStudentPool());
            StudentAssignmentService.syncSchoolEnrollmentFromPool(town.getStudentPool(), school, view);

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
     * Populates a school using demand-driven staffing.
     *
     * @deprecated Use
     *             {@link #populateSchoolFromStudentDemand(Town, StandardSchool, GameView)}
     *             instead for the improved demand-first pipeline.
     *
     * @param town   the town containing population pools
     * @param school the school to populate
     * @param view   the game view for output
     */
    @Deprecated
    public static void populateSchoolDemandDriven(Town town, StandardSchool school, GameView view) {
        view.appendOutput("Demand-driven staffing now delegates to the unified demand-first pipeline...");
        populateSchoolFromStudentDemand(town, school, view);
    }

    /**
     * Populates a school with all available students from the pool.
     * This allows for overcrowding scenarios in underfunded schools.
     *
     * @param town   the town containing population pools
     * @param school the school to populate
     * @param view   the game view for output
     */
    public static void populateSchoolFully(Town town, StandardSchool school, GameView view) {
        int availableStudents = town.getStudentPool().getUnassignedCount();
        int maxCapacity = school.getFundingModel().getMaxAllowedEnrollment(school.getPhysicalCapacity());
        int studentsToAssign = Math.min(availableStudents, maxCapacity);

        populateSchool(town, school, studentsToAssign,
                school.getMinimumStaffRequirements(), view);
    }

    /**
     * Populates a school with a specific number of students and staff.
     *
     * @param town         the town containing population pools
     * @param school       the school to populate
     * @param studentCount the number of students to assign
     * @param staffCount   the number of staff to assign
     * @param view         the game view for output
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
     * @param town   the town
     * @param school the school
     * @param view   the game view for output
     */
    public static void completeScheduling(Town town, StandardSchool school, GameView view) {
        // Get maps for scheduling (class scheduling is handled by
        // EnhancedStudentScheduleAssigner)
        HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
        HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);

        try {
            view.appendOutput("Scheduling students...");
            EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                    studentMap, staffMap, school, view, town.getStudentPool());

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
     * @param town   the town
     * @param school the school
     * @return HashMap of students
     */
    public static HashMap<Integer, Student> getStudentHashMap(Town town, StandardSchool school) {
        return town.getStudentPool().getStudentsBySchoolAsMap(school);
    }

    /**
     * Gets the HashMap of staff assigned to a school (for compatibility).
     *
     * @param town   the town
     * @param school the school
     * @return HashMap of staff
     */
    public static HashMap<Integer, Staff> getStaffHashMap(Town town, StandardSchool school) {
        return town.getStaffPool().getStaffBySchoolAsMap(school);
    }

    /**
     * Gets statistics about the school population.
     *
     * @param town   the town
     * @param school the school
     * @return a summary string
     */
    public static String getPopulationSummary(Town town, StandardSchool school) {
        int students = town.getStudentPool().getAssignedCount(school);
        int staff = town.getStaffPool().getAssignedCount(school);
        int unassignedStudents = town.getStudentPool().getUnassignedCount();
        int unassignedStaff = town.getStaffPool().getUnassignedCount();

        return String.format("School: %s\n" +
                "  Students: %d (optimal capacity: %d, physical: %d)\n" +
                "  Staff: %d (minimum: %d)\n" +
                "  Available students in town: %d\n" +
                "  Available staff in town: %d",
                school.getSchoolName(),
                students, school.getOptimalCapacity(), school.getPhysicalCapacity(),
                staff, school.getMinimumStaffRequirements(),
                unassignedStudents, unassignedStaff);
    }

    /**
     * Checks if a school is fully staffed based on minimum requirements.
     *
     * @param town   the town
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
     * @param town   the town
     * @param school the school
     * @return true if the school is at or over optimal capacity
     */
    public static boolean isAtStudentCapacity(Town town, StandardSchool school) {
        int enrolled = town.getStudentPool().getAssignedCount(school);
        int capacity = school.getOptimalCapacity();
        return enrolled >= capacity;
    }

    /**
     * Checks if a school is overcrowded (exceeds optimal capacity).
     *
     * @param town   the town
     * @param school the school
     * @return true if the school is overcrowded
     */
    public static boolean isOvercrowded(Town town, StandardSchool school) {
        int enrolled = town.getStudentPool().getAssignedCount(school);
        return enrolled > school.getOptimalCapacity();
    }

    /**
     * Gets the overcrowding percentage for a school.
     *
     * @param town   the town
     * @param school the school
     * @return the overcrowding percentage (e.g., 1.2 = 120% of optimal)
     */
    public static double getOvercrowdingLevel(Town town, StandardSchool school) {
        int enrolled = town.getStudentPool().getAssignedCount(school);
        int optimal = school.getOptimalCapacity();
        return optimal > 0 ? (double) enrolled / optimal : 0;
    }

    // ==================== Scheduling Retry with Expansion ====================

    /** Maximum number of retry attempts for scheduling */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /** Threshold of students with incomplete schedules to trigger retry (10%) */
    private static final double INCOMPLETE_SCHEDULE_THRESHOLD = 0.10;

    /** Number of classrooms to add per retry */
    private static final int CLASSROOMS_PER_RETRY = 5;

    /** Maximum iterative recovery passes for the post-generation expansion flow. */
    private static final int MAX_RECOVERY_ITERATIONS = 6;

    /**
     * Populates a school using the demand-first pipeline with automatic retry.
     * <p>
     * With the demand-first approach, capacity shortages are rare because the
     * pipeline guarantees that rooms and teachers exist for the calculated
     * demand. The only failures are scheduling conflicts (block collisions),
     * so retries are narrowed to re-running just the student assignment step
     * (step 7) with relaxed constraints.
     *
     * @param town   the town containing population pools
     * @param school the school to populate
     * @param view   the game view for output
     */
    public static void populateSchoolWithRetry(Town town, StandardSchool school, GameView view) {
        view.appendOutput("Populating " + school.getSchoolName() + " with retry-enabled scheduling...");

        // First pass: use the new demand-first pipeline
        populateSchoolFromStudentDemand(town, school, view);

        // Check if retry is needed
        SchedulingReport report = analyzeSchedulingSuccess(town, school);

        view.appendOutput("=== SCHEDULING REPORT (Initial) ===");
        view.appendOutput("  Total students: " + report.totalStudents);
        view.appendOutput("  Students with complete schedules: " + report.completeSchedules);
        view.appendOutput("  Students with incomplete schedules: " + report.incompleteSchedules);
        view.appendOutput("  Completion rate: " + String.format("%.1f%%", report.completionRate * 100));

        // Retry loop: only re-run assignment (step 7) with expanded capacity
        int attempt = 1;
        while (report.incompleteRate > INCOMPLETE_SCHEDULE_THRESHOLD && attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;
            view.appendOutput("=== SCHEDULING RETRY ATTEMPT " + attempt + " ===");

            HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
            HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);
            int targetAdditionalTeachers = Math.max(1, Math.min(
                    school.getFundingModel().getMaxExpansionTeachers(),
                    (int) Math.ceil((double) report.totalMissingRequests /
                            Math.max(1, TeacherBlockBuilder.getCurrentOptimalClassSize() * 2))));

            Map<String, Integer> roomNeeds = estimateAdditionalRoomNeeds(report, school);
            if (!roomNeeds.isEmpty()) {
                view.appendOutput("  Reacting to room bottlenecks before retry...");
                Director.adaptSchoolToDemand(school, roomNeeds, view);
            }

            if (targetAdditionalTeachers > 0) {
                view.appendOutput("  Hiring against current shortage profile before retry...");
                Map<StaffType, Integer> staffNeeds = estimateStaffNeeds(report, targetAdditionalTeachers);
                StaffAssignmentService.assignAdditionalTeachers(town, school, staffNeeds, view);
                staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);
            }

            if (roomNeeds.isEmpty() && targetAdditionalTeachers == 0) {
                int addedClassrooms = school.addClassrooms(CLASSROOMS_PER_RETRY, view);
                view.appendOutput("  Added " + addedClassrooms + " classrooms. New total: " +
                        school.getClassrooms().length);
            }

            // Clear schedules and re-run the scheduling step with new resources
            clearStudentSchedules(town, school);

            try {
                EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                        studentMap, staffMap, school, view, town.getStudentPool(), false);
                StudentAssignmentService.syncSchoolEnrollmentFromPool(town.getStudentPool(), school, view);
                StudentSeatingAssigner.seatInitialStudents(school);
            } catch (Exception e) {
                view.appendOutput("  Error during retry scheduling: " + e.getMessage());
                e.printStackTrace();
            }

            report = analyzeSchedulingSuccess(town, school);

            view.appendOutput("  Completion rate: " + String.format("%.1f%%", report.completionRate * 100));

            if (report.incompleteRate <= INCOMPLETE_SCHEDULE_THRESHOLD) {
                view.appendOutput("  Scheduling successful - within threshold");
            } else if (attempt >= MAX_RETRY_ATTEMPTS) {
                view.appendOutput("  Max retry attempts reached. Proceeding with current results.");
            }
        }

        rerunSchedulingPass(town, school, view, true, "Finalizing retry scheduling policy...");

        view.appendOutput("=== FINAL SCHOOL STATISTICS ===");
        view.appendOutput("  Classrooms: " + school.getClassrooms().length);
        view.appendOutput("  Optimal capacity: " + school.getOptimalCapacity());
        view.appendOutput("  Physical capacity: " + school.getPhysicalCapacity());
        view.appendOutput("  Current enrollment: " + school.getCurrentEnrollment());
    }

    /**
     * Clears all student schedules for a school in preparation for retry.
     */
    private static void clearStudentSchedules(Town town, StandardSchool school) {
        HashMap<Integer, Student> students = town.getStudentPool().getStudentsBySchoolAsMap(school);
        for (Student student : students.values()) {
            student.studentStatistics.getStudentSchedule().clear();
        }
        GameLogger.logScheduling("Cleared schedules for " + students.size() + " students for retry");
    }

    /**
     * Analyzes scheduling success rate for a school.
     */
    private static SchedulingReport analyzeSchedulingSuccess(Town town, StandardSchool school) {
        HashMap<Integer, Student> students = town.getStudentPool().getStudentsBySchoolAsMap(school);

        int totalStudents = students.size();
        int completeSchedules = 0;
        int incompleteSchedules = 0;
        int totalMissingRequests = 0;
        int totalRequiredMissingRequests = 0;
        int totalOptionalMissingRequests = 0;
        int studentsWithOptionalGaps = 0;
        int studentsEligibleForRemoval = 0;
        Map<String, Integer> missingRequestsByClass = new HashMap<>();
        Map<String, Integer> missingRequestsByGrade = new HashMap<>();
        Map<StaffType, Integer> missingRequestsByStaffType = new HashMap<>();
        Map<String, Integer> missingRequestsByRoomType = new HashMap<>();

        for (Student student : students.values()) {
            GraduationVerifier.StudentSchedulePolicyStatus status =
                    GraduationVerifier.evaluateStudentSchedulePolicy(student);

            if (status.meetsCompletionPolicy()) {
                completeSchedules++;
            } else {
                incompleteSchedules++;
                missingRequestsByGrade.merge(status.getGrade(), status.getRecoveryTargetClasses().size(), Integer::sum);
                for (String className : status.getRecoveryTargetClasses()) {
                    totalMissingRequests++;
                    missingRequestsByClass.merge(className, 1, Integer::sum);
                    StaffType type = CurriculumRequirementsCalculator.mapClassToStaffType(className);
                    missingRequestsByStaffType.merge(type, 1, Integer::sum);
                    missingRequestsByRoomType.merge(DemandAnalyzer.getRoomTypeForClass(className), 1, Integer::sum);
                }
            }

            totalRequiredMissingRequests += status.getMissingRequiredClasses().size();
            totalOptionalMissingRequests += status.getMissingOptionalClasses().size();
            if (!status.getMissingOptionalClasses().isEmpty()) {
                studentsWithOptionalGaps++;
            }
            if (status.shouldReturnToPool()) {
                studentsEligibleForRemoval++;
            }
        }

        return new SchedulingReport(totalStudents, completeSchedules, incompleteSchedules, totalMissingRequests,
                totalRequiredMissingRequests, totalOptionalMissingRequests, studentsWithOptionalGaps,
                studentsEligibleForRemoval, missingRequestsByClass, missingRequestsByGrade,
                missingRequestsByStaffType, missingRequestsByRoomType);
    }

    // ==================== Post-Generation Expansion ====================

    /**
     * Expands a school to meet student demand by adding classrooms, portables, and
     * teachers.
     * This method is used after initial generation to address capacity shortages.
     * Only adequately funded (or better) schools can expand with classrooms;
     * underfunded schools may add portables as a cheaper alternative.
     *
     * @param town   the town containing population pools
     * @param school the school to expand
     * @param view   the game view for output
     * @return an expansion report with details of what was added
     */
    public static ExpansionReport expandSchoolToMeetDemand(Town town, StandardSchool school, GameView view) {
        return expandSchoolToMeetDemand(town, school, null, view);
    }

    /**
     * Builds a reusable summary of current scheduling gaps for a school.
     */
    public static SchedulingGapSummary getSchedulingGapSummary(Town town, StandardSchool school) {
        return SchedulingGapSummary.fromReport(analyzeSchedulingSuccess(town, school));
    }

    /**
     * Appends a concise scheduling-gap report to the given view.
     */
    public static void appendSchedulingGapReport(Town town, StandardSchool school, GameView view, int limit) {
        SchedulingGapSummary summary = getSchedulingGapSummary(town, school);
        for (String line : summary.toDebugLines(limit)) {
            view.appendOutput(line);
        }
    }

    /**
     * Expands a school to meet student demand by adding classrooms, portables, and
     * teachers.
     * When a RoomConnector is provided, newly added rooms are integrated into the
     * school graph
     * so they appear on the school map and can be used for pathfinding.
     *
     * @param town          the town containing population pools
     * @param school        the school to expand
     * @param roomConnector the room connector to update (can be null)
     * @param view          the game view for output
     * @return an expansion report with details of what was added
     */
    public static ExpansionReport expandSchoolToMeetDemand(Town town, StandardSchool school,
            RoomConnector roomConnector, GameView view) {
        view.appendOutput("=== ANALYZING SCHOOL EXPANSION NEEDS ===");

        SchedulingReport initialReport = analyzeSchedulingSuccess(town, school);
        SchedulingReport scheduleReport = initialReport;

        int classroomsAdded = 0;
        int portablesAdded = 0;
        int teachersAdded = 0;
        boolean expansionOccurred = false;

        // Check if expansion is needed
        if (scheduleReport.incompleteSchedules == 0) {
            view.appendOutput("  School is meeting demand - no expansion needed");
            view.appendOutput("  Completion rate: " + String.format("%.1f%%", scheduleReport.completionRate * 100));
            view.appendOutput("  Required shortages: " + scheduleReport.totalRequiredMissingRequests);
            view.appendOutput("  Optional off-block gaps: " + scheduleReport.totalOptionalMissingRequests);
            return new ExpansionReport(0, 0, 0, false);
        }

        view.appendOutput("  Current incomplete schedule rate: " +
                String.format("%.1f%%", scheduleReport.incompleteRate * 100));
        view.appendOutput("  Required shortages: " + scheduleReport.totalRequiredMissingRequests);
        view.appendOutput("  Actionable missing requests: " + scheduleReport.totalMissingRequests);
        view.appendOutput("  Optional off-block gaps: " + scheduleReport.totalOptionalMissingRequests);
        logSchedulingGaps(view, scheduleReport);

        for (int iteration = 1; iteration <= MAX_RECOVERY_ITERATIONS && scheduleReport.incompleteSchedules > 0; iteration++) {
            view.appendOutput("=== RECOVERY ITERATION " + iteration + " ===");

            int teachingRoomCountBefore = getTotalTeachingRoomCount(school);
            int classroomsBefore = school.getClassrooms().length;
            int portablesBefore = school.getPortables().length;

            int targetTeachers = estimateTeacherTargets(scheduleReport, school);
            Map<StaffType, Integer> staffNeeds = estimateStaffNeeds(scheduleReport, targetTeachers);
            int teachersAddedThisRound = 0;
            if (!staffNeeds.isEmpty()) {
                view.appendOutput("  Expanding staffing for missing graduation-critical demand...");
                teachersAddedThisRound = StaffAssignmentService.assignAdditionalTeachers(
                        town, school, staffNeeds, view);
                teachersAdded += teachersAddedThisRound;
            }

            Map<String, Integer> roomNeeds = estimateAdditionalRoomNeeds(scheduleReport, school);
            if (!roomNeeds.isEmpty()) {
                view.appendOutput("  Expanding or adapting rooms for highest-demand shortages...");
                Director.adaptSchoolToDemand(school, roomNeeds, view);
            }

            int teachingRoomCountAfter = getTotalTeachingRoomCount(school);
            int roomGrowth = Math.max(0, teachingRoomCountAfter - teachingRoomCountBefore);
            classroomsAdded += Math.max(0, school.getClassrooms().length - classroomsBefore);
            portablesAdded += Math.max(0, school.getPortables().length - portablesBefore);
            boolean resourcesAdded = teachersAddedThisRound > 0 || roomGrowth > 0;
            expansionOccurred = expansionOccurred || resourcesAdded;

            if (resourcesAdded && roomConnector != null) {
                view.appendOutput("  Integrating new rooms into school map...");
                roomConnector.integrateNewRooms(school, view);
            }

            SchedulingReport newReport = rerunSchedulingPass(town, school, view, false, "  Re-scheduling students with recovery resources...");

            view.appendOutput("=== ITERATION RESULT ===");
            view.appendOutput("  Before: " + String.format("%.1f%%", scheduleReport.completionRate * 100) +
                    " complete (" + scheduleReport.completeSchedules + "/" + scheduleReport.totalStudents + ")");
            view.appendOutput("  After: " + String.format("%.1f%%", newReport.completionRate * 100) +
                    " complete (" + newReport.completeSchedules + "/" + newReport.totalStudents + ")");
            view.appendOutput("  Remaining required shortages: " + newReport.totalRequiredMissingRequests);
            view.appendOutput("  Remaining actionable shortages: " + newReport.totalMissingRequests);

            boolean improved = newReport.totalMissingRequests < scheduleReport.totalMissingRequests
                    || newReport.completeSchedules > scheduleReport.completeSchedules;
            scheduleReport = newReport;

            if (scheduleReport.incompleteSchedules == 0) {
                view.appendOutput("  Recovery complete - all students now meet the scheduling policy");
                break;
            }

            if (!improved) {
                if (scheduleReport.studentsEligibleForRemoval > 0) {
                    view.appendOutput("  Recovery stalled; remaining cases exceed the severe-missing threshold");
                } else {
                    view.appendOutput("  Recovery stalled - no measurable improvement this round");
                }
                break;
            }
        }

        SchedulingReport finalizedReport = rerunSchedulingPass(town, school, view, true,
                "=== FINALIZING SCHEDULES ===");
        int studentsRescheduled = Math.max(0, finalizedReport.completeSchedules - initialReport.completeSchedules);
        double newCompletionRate = finalizedReport.completionRate;

        // Report results
        view.appendOutput("=== EXPANSION COMPLETE ===");
        view.appendOutput("  Classrooms added: " + classroomsAdded);
        view.appendOutput("  Portables added: " + portablesAdded);
        view.appendOutput("  Teachers hired: " + teachersAdded);
        view.appendOutput("  Students rescheduled: " + studentsRescheduled);
        view.appendOutput("  New optimal capacity: " + school.getOptimalCapacity());
        view.appendOutput("  New physical capacity: " + school.getPhysicalCapacity());
        view.appendOutput("  New completion rate: " + String.format("%.1f%%", newCompletionRate * 100));
        view.appendOutput("  Remaining optional off-block gaps: " + finalizedReport.totalOptionalMissingRequests);

        return new ExpansionReport(classroomsAdded, portablesAdded, teachersAdded, expansionOccurred,
                initialReport.completionRate, newCompletionRate, studentsRescheduled);
    }

    /**
     * Estimates staff needs based on current gaps and available slots.
     * Analyzes which subject areas have the most scheduling failures.
     *
     * @param town           the town
     * @param school         the school
     * @param targetTeachers the number of teachers to distribute
     * @return a map of staff types to needed counts
     */
    private static Map<StaffType, Integer> estimateStaffNeeds(SchedulingReport report, int targetTeachers) {
        Map<StaffType, Integer> needs = new HashMap<>();
        if (targetTeachers <= 0) {
            return needs;
        }

        if (report.missingRequestsByStaffType.isEmpty()) {
            needs.put(StaffType.MATH, 1);
            return needs;
        }

        List<Map.Entry<StaffType, Integer>> sortedNeeds = report.missingRequestsByStaffType.entrySet().stream()
                .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
                .toList();
        int totalMissing = sortedNeeds.stream().mapToInt(Map.Entry::getValue).sum();
        int assigned = 0;

        for (Map.Entry<StaffType, Integer> entry : sortedNeeds) {
            int share = (int) Math.floor((double) entry.getValue() * targetTeachers / Math.max(1, totalMissing));
            if (share > 0) {
                needs.put(entry.getKey(), share);
                assigned += share;
            }
        }

        int index = 0;
        while (assigned < targetTeachers && !sortedNeeds.isEmpty()) {
            StaffType type = sortedNeeds.get(index % sortedNeeds.size()).getKey();
            needs.merge(type, 1, Integer::sum);
            assigned++;
            index++;
        }

        return needs;
    }

    private static Map<String, Integer> estimateAdditionalRoomNeeds(SchedulingReport report, StandardSchool school) {
        Map<String, Integer> roomNeeds = new HashMap<>();
        int classSize = Math.max(1, school.getFundingModel().getOptimalClassSize());

        for (Map.Entry<String, Integer> entry : report.missingRequestsByRoomType.entrySet()) {
            int additionalSections = (int) Math.ceil((double) entry.getValue() / classSize);
            int additionalRooms = (int) Math.ceil((double) additionalSections / TOTAL_SCHOOL_PERIODS);
            if (additionalRooms <= 0) {
                continue;
            }

            int currentRooms = getCurrentRoomCount(school, entry.getKey());
            roomNeeds.put(entry.getKey(), currentRooms + additionalRooms);
        }

        return roomNeeds;
    }

    private static int getCurrentRoomCount(StandardSchool school, String roomType) {
        return switch (roomType) {
            case "ScienceLab" -> school.getScienceLabs().length;
            case "ArtStudio" -> school.getArtStudios().length;
            case "DramaRoom" -> school.getDramaRooms().length;
            case "MusicRoom" -> school.getMusicRooms().length;
            case "Gym" -> school.getGyms().length;
            case "VocationalRoom" -> school.getVocationalRooms().length;
            case "ComputerLab" -> school.getComputerLabs().length;
            default -> school.getClassrooms().length + school.getPortables().length + getOverflowTeachingRoomCount(school);
        };
    }

    private static int estimateTeacherTargets(SchedulingReport report, StandardSchool school) {
        if (report.totalMissingRequests <= 0) {
            return 0;
        }
        int classSize = Math.max(1, school.getFundingModel().getOptimalClassSize());
        return Math.max(1, (int) Math.ceil((double) report.totalMissingRequests / classSize));
    }

    private static int getOverflowTeachingRoomCount(StandardSchool school) {
        return school.getLibraries().length
                + school.getAuditoriums().length
                + school.getConferenceRooms().length
                + school.getLunchrooms().length;
    }

    private static int getTotalTeachingRoomCount(StandardSchool school) {
        return school.getClassrooms().length
                + school.getScienceLabs().length
                + school.getArtStudios().length
                + school.getDramaRooms().length
                + school.getMusicRooms().length
                + school.getGyms().length
                + school.getVocationalRooms().length
                + school.getComputerLabs().length
                + school.getPortables().length
                + getOverflowTeachingRoomCount(school);
    }

    private static SchedulingReport rerunSchedulingPass(Town town, StandardSchool school, GameView view,
            boolean allowCriticalRemoval, String label) {
        view.appendOutput(label);
        HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
        HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);

        try {
            EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                    studentMap, staffMap, school, view, town.getStudentPool(), allowCriticalRemoval);
            StudentAssignmentService.syncSchoolEnrollmentFromPool(town.getStudentPool(), school, view);
            StudentSeatingAssigner.seatInitialStudents(school);
        } catch (Exception e) {
            view.appendOutput("  Error during re-scheduling: " + e.getMessage());
            e.printStackTrace();
        }

        return analyzeSchedulingSuccess(town, school);
    }

    private static void logSchedulingGaps(GameView view, SchedulingReport report) {
        for (String line : SchedulingGapSummary.fromReport(report).toDebugLines(5)) {
            view.appendOutput(line);
        }
    }

    /**
     * Report class for expansion results.
     */
    public static class ExpansionReport {
        public final int classroomsAdded;
        public final int portablesAdded;
        public final int teachersHired;
        public final boolean expansionOccurred;
        public final double previousCompletionRate;
        public final double newCompletionRate;
        public final int studentsRescheduled;

        public ExpansionReport(int classrooms, int portables, int teachers, boolean occurred) {
            this(classrooms, portables, teachers, occurred, 0.0, 0.0, 0);
        }

        public ExpansionReport(int classrooms, int portables, int teachers, boolean occurred,
                double previousRate, double newRate, int rescheduled) {
            this.classroomsAdded = classrooms;
            this.portablesAdded = portables;
            this.teachersHired = teachers;
            this.expansionOccurred = occurred;
            this.previousCompletionRate = previousRate;
            this.newCompletionRate = newRate;
            this.studentsRescheduled = rescheduled;
        }

        /**
         * Gets the improvement in completion rate as a percentage.
         *
         * @return the improvement (e.g., 0.15 for 15% improvement)
         */
        public double getCompletionRateImprovement() {
            return newCompletionRate - previousCompletionRate;
        }
    }

    /**
     * Public-facing snapshot of scheduling bottlenecks for reporting and tests.
     */
    public static class SchedulingGapSummary {
        public final int totalStudents;
        public final int completeSchedules;
        public final int incompleteSchedules;
        public final int totalMissingRequests;
        public final int totalRequiredMissingRequests;
        public final int totalOptionalMissingRequests;
        public final int studentsWithOptionalGaps;
        public final int studentsEligibleForRemoval;
        public final double completionRate;
        public final Map<String, Integer> missingRequestsByClass;
        public final Map<String, Integer> missingRequestsByGrade;
        public final Map<StaffType, Integer> missingRequestsByStaffType;
        public final Map<String, Integer> missingRequestsByRoomType;

        private SchedulingGapSummary(int totalStudents, int completeSchedules, int incompleteSchedules,
                int totalMissingRequests, int totalRequiredMissingRequests, int totalOptionalMissingRequests,
                int studentsWithOptionalGaps, int studentsEligibleForRemoval, double completionRate,
                Map<String, Integer> missingRequestsByClass, Map<String, Integer> missingRequestsByGrade,
                Map<StaffType, Integer> missingRequestsByStaffType,
                Map<String, Integer> missingRequestsByRoomType) {
            this.totalStudents = totalStudents;
            this.completeSchedules = completeSchedules;
            this.incompleteSchedules = incompleteSchedules;
            this.totalMissingRequests = totalMissingRequests;
            this.totalRequiredMissingRequests = totalRequiredMissingRequests;
            this.totalOptionalMissingRequests = totalOptionalMissingRequests;
            this.studentsWithOptionalGaps = studentsWithOptionalGaps;
            this.studentsEligibleForRemoval = studentsEligibleForRemoval;
            this.completionRate = completionRate;
            this.missingRequestsByClass = new HashMap<>(missingRequestsByClass);
            this.missingRequestsByGrade = new HashMap<>(missingRequestsByGrade);
            this.missingRequestsByStaffType = new HashMap<>(missingRequestsByStaffType);
            this.missingRequestsByRoomType = new HashMap<>(missingRequestsByRoomType);
        }

        private static SchedulingGapSummary fromReport(SchedulingReport report) {
            return new SchedulingGapSummary(report.totalStudents, report.completeSchedules, report.incompleteSchedules,
                    report.totalMissingRequests, report.totalRequiredMissingRequests,
                    report.totalOptionalMissingRequests, report.studentsWithOptionalGaps,
                    report.studentsEligibleForRemoval, report.completionRate, report.missingRequestsByClass,
                    report.missingRequestsByGrade, report.missingRequestsByStaffType, report.missingRequestsByRoomType);
        }

        public List<String> toDebugLines(int limit) {
            List<String> lines = new java.util.ArrayList<>();
            lines.add("Scheduling gap summary:");
            lines.add("  Total students: " + totalStudents);
            lines.add("  Complete schedules: " + completeSchedules);
            lines.add("  Incomplete schedules: " + incompleteSchedules);
            lines.add("  Completion rate: " + String.format("%.1f%%", completionRate * 100));
            lines.add("  Actionable unmet requests: " + totalMissingRequests);
            lines.add("  Required shortages: " + totalRequiredMissingRequests);
            lines.add("  Optional off-block gaps: " + totalOptionalMissingRequests);
            lines.add("  Students with optional gaps: " + studentsWithOptionalGaps);
            lines.add("  Students eligible for removal: " + studentsEligibleForRemoval);

            if (!missingRequestsByClass.isEmpty()) {
                lines.add("  Highest unmet classes:");
                missingRequestsByClass.entrySet().stream()
                        .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
                        .limit(limit)
                        .forEach(entry -> lines.add("    " + entry.getKey() + ": " + entry.getValue()));
            }

            if (!missingRequestsByGrade.isEmpty()) {
                lines.add("  Unmet requests by grade:");
                missingRequestsByGrade.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> lines.add("    " + entry.getKey() + ": " + entry.getValue()));
            }

            if (!missingRequestsByStaffType.isEmpty()) {
                lines.add("  Subject bottlenecks:");
                missingRequestsByStaffType.entrySet().stream()
                        .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
                        .limit(limit)
                        .forEach(entry -> lines.add("    " + entry.getKey() + ": " + entry.getValue()));
            }

            if (!missingRequestsByRoomType.isEmpty()) {
                lines.add("  Room bottlenecks:");
                missingRequestsByRoomType.entrySet().stream()
                        .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
                        .limit(limit)
                        .forEach(entry -> lines.add("    " + entry.getKey() + ": " + entry.getValue()));
            }

            return lines;
        }

        public String toDebugString(int limit) {
            return String.join(System.lineSeparator(), toDebugLines(limit));
        }
    }

    /**
     * Report class for scheduling analysis.
     */
    private static class SchedulingReport {
        final int totalStudents;
        final int completeSchedules;
        final int incompleteSchedules;
        final int totalMissingRequests;
        final int totalRequiredMissingRequests;
        final int totalOptionalMissingRequests;
        final int studentsWithOptionalGaps;
        final int studentsEligibleForRemoval;
        final double completionRate;
        final double incompleteRate;
        final Map<String, Integer> missingRequestsByClass;
        final Map<String, Integer> missingRequestsByGrade;
        final Map<StaffType, Integer> missingRequestsByStaffType;
        final Map<String, Integer> missingRequestsByRoomType;

        SchedulingReport(int total, int complete, int incomplete, int totalMissingRequests,
                int totalRequiredMissingRequests, int totalOptionalMissingRequests,
                int studentsWithOptionalGaps, int studentsEligibleForRemoval,
                Map<String, Integer> missingRequestsByClass, Map<String, Integer> missingRequestsByGrade,
                Map<StaffType, Integer> missingRequestsByStaffType, Map<String, Integer> missingRequestsByRoomType) {
            this.totalStudents = total;
            this.completeSchedules = complete;
            this.incompleteSchedules = incomplete;
            this.totalMissingRequests = totalMissingRequests;
            this.totalRequiredMissingRequests = totalRequiredMissingRequests;
            this.totalOptionalMissingRequests = totalOptionalMissingRequests;
            this.studentsWithOptionalGaps = studentsWithOptionalGaps;
            this.studentsEligibleForRemoval = studentsEligibleForRemoval;
            this.completionRate = total > 0 ? (double) complete / total : 0;
            this.incompleteRate = total > 0 ? (double) incomplete / total : 0;
            this.missingRequestsByClass = new HashMap<>(missingRequestsByClass);
            this.missingRequestsByGrade = new HashMap<>(missingRequestsByGrade);
            this.missingRequestsByStaffType = new HashMap<>(missingRequestsByStaffType);
            this.missingRequestsByRoomType = new HashMap<>(missingRequestsByRoomType);
        }
    }
}
