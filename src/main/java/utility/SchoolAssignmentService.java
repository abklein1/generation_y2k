package utility;

import config.SchoolFundingModel;
import entity.Staff;
import entity.StaffType;
import entity.StandardSchool;
import entity.Student;
import entity.StudentPool;
import entity.Town;
import view.GameView;

import java.util.HashMap;
import java.util.Map;

/**
 * Main orchestration service for assigning populations to schools.
 * Coordinates student and staff assignment using the Town population pools.
 * 
 * Supports two modes:
 * 1. Capacity-based (legacy): Assigns students up to school capacity
 * 2. Demand-driven (new): Analyzes curriculum needs first, then assigns staff by type
 */
public class SchoolAssignmentService {

    /**
     * Fully populates a school from the town's population pools using the legacy
     * capacity-based approach. Use populateSchoolDemandDriven for the new approach.
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
        
        // Schedule students (includes class scheduling via createDemandDrivenTeacherBlocks)
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
        
        view.appendOutput("School population complete: " + studentsAssigned + " students, " + 
                staffAssigned + " staff");
    }

    /**
     * Populates a school using demand-driven staffing.
     * This approach:
     * 1. First analyzes what classes students need based on curriculum requirements
     * 2. Calculates how many teachers of each type are needed
     * 3. Assigns staff by type to meet those needs
     * 4. Then assigns students (can exceed optimal capacity for overcrowding scenarios)
     * 5. Finally schedules students with guaranteed graduation requirements
     *
     * @param town the town containing population pools
     * @param school the school to populate
     * @param view the game view for output
     */
    public static void populateSchoolDemandDriven(Town town, StandardSchool school, GameView view) {
        view.appendOutput("Populating " + school.getSchoolName() + " using demand-driven staffing...");
        
        // Add school to town if not already present
        if (!town.getSchools().contains(school)) {
            town.addSchool(school);
        }
        
        SchoolFundingModel fundingModel = school.getFundingModel();
        
        // Step 1: Assign students FIRST to know actual school enrollment
        view.appendOutput("Assigning students to school...");
        int maxStudents = fundingModel.isAllowOvercrowding() 
            ? fundingModel.getMaxAllowedEnrollment(school.getPhysicalCapacity())
            : school.getOptimalCapacity();
        
        int studentsAssigned = StudentAssignmentService.assignStudentsToSchool(
                town.getStudentPool(), school, maxStudents, view);
        
        view.appendOutput("  Assigned " + studentsAssigned + " students (max capacity: " + maxStudents + ")");
        
        // Step 2: Analyze curriculum requirements based on ACTUAL school enrollment (not town pool)
        view.appendOutput("Analyzing curriculum requirements for enrolled students...");
        // Get only the students assigned to this school
        StudentPool schoolStudents = new StudentPool();
        for (Student student : town.getStudentPool().getStudentsBySchool(school)) {
            schoolStudents.addStudent(student);
        }
        
        CurriculumRequirementsCalculator.CurriculumAnalysis analysis = 
            CurriculumRequirementsCalculator.analyzeRequirements(schoolStudents, fundingModel);
        
        // Log the analysis
        view.appendOutput("  Enrolled students by grade: " + analysis.getStudentsByGrade());
        view.appendOutput("  Staff needed for enrolled students: " + analysis.getStaffRequirements().getTotalStaff());
        
        // Log any warnings
        for (String warning : analysis.getWarnings()) {
            view.appendOutput("  WARNING: " + warning);
        }
        
        // Step 3: Assign staff by type based on curriculum requirements
        view.appendOutput("Assigning staff by curriculum demand...");
        Map<StaffType, Integer> staffNeeds = analysis.getStaffRequirements().getTeachersByType();
        int staffAssigned = StaffAssignmentService.assignStaffByDemand(
                town.getStaffPool(), school, staffNeeds, view);
        
        // Update school enrollment
        school.setCurrentEnrollment(studentsAssigned);
        
        // Report overcrowding status
        if (school.isOvercrowded()) {
            view.appendOutput("  NOTE: School is overcrowded at " + 
                String.format("%.1f%%", school.getOvercrowdingLevel() * 100) + " of optimal capacity");
        }
        
        // Step 4: Schedule students with demand awareness (includes class scheduling)
        HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
        HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);
        
        try {
            view.appendOutput("Scheduling students with graduation requirements...");
            EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                    studentMap, staffMap, school, view, town.getStudentPool());
            
            view.appendOutput("Assigning seating...");
            StudentSeatingAssigner.seatInitialStudents(school);
        } catch (Exception e) {
            view.appendOutput("Error during scheduling: " + e.getMessage());
            e.printStackTrace();
        }
        
        view.appendOutput("School population complete: " + studentsAssigned + " students, " + 
                staffAssigned + " staff");
        view.appendOutput("  Optimal capacity: " + school.getOptimalCapacity());
        view.appendOutput("  Physical capacity: " + school.getPhysicalCapacity());
        view.appendOutput("  Current enrollment: " + school.getCurrentEnrollment());
    }

    /**
     * Populates a school with all available students from the pool.
     * This allows for overcrowding scenarios in underfunded schools.
     *
     * @param town the town containing population pools
     * @param school the school to populate
     * @param view the game view for output
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
        // Get maps for scheduling (class scheduling is handled by EnhancedStudentScheduleAssigner)
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
     * @param town the town
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
     * @param town the town
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

    /**
     * Populates a school using demand-driven staffing with automatic retry and expansion.
     * If too many students have incomplete schedules, this will:
     * 1. Add more classrooms to the school
     * 2. Re-run the scheduling process
     * 3. Repeat up to MAX_RETRY_ATTEMPTS times
     *
     * @param town the town containing population pools
     * @param school the school to populate
     * @param view the game view for output
     */
    public static void populateSchoolWithRetry(Town town, StandardSchool school, GameView view) {
        view.appendOutput("Populating " + school.getSchoolName() + " with retry-enabled scheduling...");
        
        int attempt = 0;
        boolean schedulingSuccessful = false;
        
        while (attempt < MAX_RETRY_ATTEMPTS && !schedulingSuccessful) {
            attempt++;
            
            if (attempt > 1) {
                view.appendOutput("=== SCHEDULING RETRY ATTEMPT " + attempt + " ===");
                
                // Expand school capacity
                int addedClassrooms = school.addClassrooms(CLASSROOMS_PER_RETRY, view);
                view.appendOutput("  Added " + addedClassrooms + " classrooms. New total: " + 
                                 school.getClassrooms().length);
                view.appendOutput("  New optimal capacity: " + school.getOptimalCapacity());
                view.appendOutput("  New physical capacity: " + school.getPhysicalCapacity());
                
                // Clear and reset student schedules for retry
                clearStudentSchedules(town, school);
            }
            
            // Run the demand-driven population
            populateSchoolDemandDriven(town, school, view);
            
            // Check scheduling success rate
            SchedulingReport report = analyzeSchedulingSuccess(town, school);
            
            view.appendOutput("=== SCHEDULING REPORT (Attempt " + attempt + ") ===");
            view.appendOutput("  Total students: " + report.totalStudents);
            view.appendOutput("  Students with complete schedules: " + report.completeSchedules);
            view.appendOutput("  Students with incomplete schedules: " + report.incompleteSchedules);
            view.appendOutput("  Completion rate: " + String.format("%.1f%%", report.completionRate * 100));
            
            if (report.incompleteRate <= INCOMPLETE_SCHEDULE_THRESHOLD) {
                schedulingSuccessful = true;
                view.appendOutput("  Scheduling successful - within threshold");
            } else {
                view.appendOutput("  WARNING: " + String.format("%.1f%%", report.incompleteRate * 100) + 
                                 " of students have incomplete schedules (threshold: " + 
                                 String.format("%.0f%%", INCOMPLETE_SCHEDULE_THRESHOLD * 100) + ")");
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    view.appendOutput("  Will retry with expanded capacity...");
                } else {
                    view.appendOutput("  Max retry attempts reached. Proceeding with current results.");
                }
            }
        }
        
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
            student.studentStatistics.getStudentSchedule().getClassSchedule().clear();
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
        
        for (Student student : students.values()) {
            int expectedClasses = getExpectedClassCount(student);
            int actualClasses = student.studentStatistics.getStudentSchedule().getClassSchedule().size();
            
            if (actualClasses >= expectedClasses - 1) { // Allow 1 class short as "complete"
                completeSchedules++;
            } else {
                incompleteSchedules++;
            }
        }
        
        return new SchedulingReport(totalStudents, completeSchedules, incompleteSchedules);
    }

    /**
     * Gets the expected number of classes for a student based on grade level.
     */
    private static int getExpectedClassCount(Student student) {
        String grade = student.studentStatistics.getGradeLevel();
        return switch (grade) {
            case "Freshman" -> 8;  // Core + Language + PE + Elective
            case "Sophomore" -> 6;
            case "Junior" -> 6;
            case "Senior" -> 6;
            default -> 6;
        };
    }

    // ==================== Post-Generation Expansion ====================

    /**
     * Expands a school to meet student demand by adding classrooms, portables, and teachers.
     * This method is used after initial generation to address capacity shortages.
     * Only adequately funded (or better) schools can expand with classrooms;
     * underfunded schools may add portables as a cheaper alternative.
     *
     * @param town the town containing population pools
     * @param school the school to expand
     * @param view the game view for output
     * @return an expansion report with details of what was added
     */
    public static ExpansionReport expandSchoolToMeetDemand(Town town, StandardSchool school, GameView view) {
        view.appendOutput("=== ANALYZING SCHOOL EXPANSION NEEDS ===");
        
        SchoolFundingModel fundingModel = school.getFundingModel();
        SchedulingReport scheduleReport = analyzeSchedulingSuccess(town, school);
        
        int classroomsAdded = 0;
        int portablesAdded = 0;
        int teachersAdded = 0;
        
        // Check if expansion is needed
        if (scheduleReport.incompleteRate <= INCOMPLETE_SCHEDULE_THRESHOLD) {
            view.appendOutput("  School is meeting demand - no expansion needed");
            view.appendOutput("  Completion rate: " + String.format("%.1f%%", scheduleReport.completionRate * 100));
            return new ExpansionReport(0, 0, 0, false);
        }
        
        view.appendOutput("  Current incomplete schedule rate: " + 
                         String.format("%.1f%%", scheduleReport.incompleteRate * 100));
        view.appendOutput("  Funding level: " + fundingModel.getFundingLevel().getDisplayName());
        
        // Calculate how many additional resources we need
        int studentsWithGaps = scheduleReport.incompleteSchedules;
        int additionalCapacityNeeded = Math.max(1, studentsWithGaps / 20);  // Rough estimate
        
        // Determine what type of expansion is possible based on funding
        if (fundingModel.canExpandToMeetDemand()) {
            // Adequately funded schools can add permanent classrooms
            int maxClassrooms = fundingModel.getMaxExpansionClassrooms();
            int classroomsToAdd = Math.min(additionalCapacityNeeded, maxClassrooms);
            
            if (classroomsToAdd > 0) {
                view.appendOutput("  Adding " + classroomsToAdd + " permanent classrooms...");
                classroomsAdded = school.addClassrooms(classroomsToAdd, view);
                view.appendOutput("  New classroom total: " + school.getClassrooms().length);
            }
            
            // Try to hire additional teachers
            int maxTeachers = fundingModel.getMaxExpansionTeachers();
            int teachersToHire = Math.min(classroomsAdded, maxTeachers);
            
            if (teachersToHire > 0) {
                view.appendOutput("  Attempting to hire " + teachersToHire + " additional teachers...");
                Map<StaffType, Integer> staffNeeds = estimateStaffNeeds(town, school, teachersToHire);
                teachersAdded = StaffAssignmentService.assignAdditionalTeachers(
                        town, school, staffNeeds, view);
                view.appendOutput("  Hired " + teachersAdded + " additional teachers");
            }
        } else {
            // Underfunded schools can only add portables
            view.appendOutput("  School funding insufficient for permanent expansion");
        }
        
        // All schools can add portables (cheaper option)
        int maxPortables = fundingModel.getMaxExpansionPortables();
        int portablesToAdd = Math.min(
                Math.max(1, additionalCapacityNeeded - classroomsAdded), 
                maxPortables
        );
        
        if (portablesToAdd > 0 && !fundingModel.canExpandToMeetDemand()) {
            // Only add portables if we couldn't add permanent classrooms
            view.appendOutput("  Adding " + portablesToAdd + " portable classrooms...");
            portablesAdded = school.addPortables(portablesToAdd, view);
            view.appendOutput("  New portable total: " + school.getPortables().length);
        }
        
        boolean expansionOccurred = classroomsAdded > 0 || portablesAdded > 0 || teachersAdded > 0;
        
        // Re-run scheduling if expansion occurred
        int studentsRescheduled = 0;
        double newCompletionRate = scheduleReport.completionRate;
        
        if (expansionOccurred) {
            view.appendOutput("=== RE-SCHEDULING STUDENTS ===");
            
            // Get student and staff maps for re-scheduling
            HashMap<Integer, Student> studentMap = town.getStudentPool().getStudentsBySchoolAsMap(school);
            HashMap<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);
            
            // Note: Class scheduling for new teachers is handled by
            // EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced() below
            
            // Re-run student scheduling
            try {
                view.appendOutput("  Re-scheduling students with new resources...");
                EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(
                        studentMap, staffMap, school, view, town.getStudentPool());
                
                view.appendOutput("  Re-assigning seating...");
                StudentSeatingAssigner.seatInitialStudents(school);
                
                // Analyze new scheduling success rate
                SchedulingReport newReport = analyzeSchedulingSuccess(town, school);
                studentsRescheduled = newReport.completeSchedules - scheduleReport.completeSchedules;
                newCompletionRate = newReport.completionRate;
                
                view.appendOutput("=== SCHEDULING IMPROVEMENT ===");
                view.appendOutput("  Before: " + String.format("%.1f%%", scheduleReport.completionRate * 100) + 
                                 " complete (" + scheduleReport.completeSchedules + "/" + scheduleReport.totalStudents + ")");
                view.appendOutput("  After: " + String.format("%.1f%%", newCompletionRate * 100) + 
                                 " complete (" + newReport.completeSchedules + "/" + newReport.totalStudents + ")");
                view.appendOutput("  Improvement: " + studentsRescheduled + " additional students scheduled");
                
            } catch (Exception e) {
                view.appendOutput("  Error during re-scheduling: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Report results
        view.appendOutput("=== EXPANSION COMPLETE ===");
        view.appendOutput("  Classrooms added: " + classroomsAdded);
        view.appendOutput("  Portables added: " + portablesAdded);
        view.appendOutput("  Teachers hired: " + teachersAdded);
        view.appendOutput("  Students rescheduled: " + studentsRescheduled);
        view.appendOutput("  New optimal capacity: " + school.getOptimalCapacity());
        view.appendOutput("  New physical capacity: " + school.getPhysicalCapacity());
        view.appendOutput("  New completion rate: " + String.format("%.1f%%", newCompletionRate * 100));
        
        return new ExpansionReport(classroomsAdded, portablesAdded, teachersAdded, expansionOccurred,
                                   scheduleReport.completionRate, newCompletionRate, studentsRescheduled);
    }

    /**
     * Estimates staff needs based on current gaps and available slots.
     * Analyzes which subject areas have the most scheduling failures.
     *
     * @param town the town
     * @param school the school
     * @param targetTeachers the number of teachers to distribute
     * @return a map of staff types to needed counts
     */
    private static Map<StaffType, Integer> estimateStaffNeeds(Town town, StandardSchool school, int targetTeachers) {
        Map<StaffType, Integer> needs = new HashMap<>();
        
        // Distribute teachers across core subjects proportionally
        // This is a simplified estimation - a more sophisticated version would
        // analyze actual scheduling failures by subject
        int perSubject = Math.max(1, targetTeachers / 4);
        int remainder = targetTeachers - (perSubject * 4);
        
        needs.put(StaffType.MATH, perSubject);
        needs.put(StaffType.ENGLISH, perSubject);
        needs.put(StaffType.SCIENCE, perSubject);
        needs.put(StaffType.HISTORY, perSubject + remainder);
        
        return needs;
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
     * Report class for scheduling analysis.
     */
    private static class SchedulingReport {
        final int totalStudents;
        final int completeSchedules;
        final int incompleteSchedules;
        final double completionRate;
        final double incompleteRate;

        SchedulingReport(int total, int complete, int incomplete) {
            this.totalStudents = total;
            this.completeSchedules = complete;
            this.incompleteSchedules = incomplete;
            this.completionRate = total > 0 ? (double) complete / total : 0;
            this.incompleteRate = total > 0 ? (double) incomplete / total : 0;
        }
    }
}
