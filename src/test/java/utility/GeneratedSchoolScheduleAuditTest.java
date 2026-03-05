package utility;

import config.SchoolFundingModel;
import config.TownDemographics;
import entity.Rooms.Classroom;
import entity.Rooms.Room;
import entity.Staff;
import entity.StaffType;
import entity.StandardSchool;
import entity.TeacherBlock;
import entity.Town;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import view.GameView;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Generated School Schedule Audit")
class GeneratedSchoolScheduleAuditTest {

    @Test
    @DisplayName("Should produce a readable unmet-request report for a generated school")
    void testGeneratedSchoolSchedulingAudit() {
        long seed = 20260305L;
        GameRandom.reset();
        GameRandom.initialize(seed);

        TownDemographics demographics = new TownDemographics();
        demographics.setTotalStudentPopulation(240);
        demographics.setTotalStaffPopulation(20);
        demographics.setExtraStudentPoolPercent(0.0);
        demographics.setExtraStaffPoolPercent(0.0);

        GameView view = new GameView();
        GameLogger.reset();
        GameLogger.initialize(view, false);

        StandardSchool school = new StandardSchool();
        SchoolFundingModel fundingModel = new SchoolFundingModel(SchoolFundingModel.FundingLevel.UNDERFUNDED);
        new Director(school, fundingModel, demographics.getTotalStudentPopulation(), view);

        Town town = TownPopulationGenerator.generateTown("AuditTown", demographics, view);
        SchoolAssignmentService.populateSchoolWithRetry(town, school, view);

        SchoolAssignmentService.SchedulingGapSummary summary =
                SchoolAssignmentService.getSchedulingGapSummary(town, school);

        System.out.println("=== GENERATED SCHOOL SCHEDULE AUDIT ===");
        System.out.println("Seed: " + seed);
        System.out.println("Funding: " + fundingModel.getFundingLevel().getDisplayName());
        System.out.println(summary.toDebugString(8));
        printUtilizationDiagnostics(town, school);

        assertTrue(summary.totalStudents > 0, "Audit scenario should enroll students");
        assertNotNull(summary.toDebugString(8), "Audit report should be printable");
        assertNotNull(summary.missingRequestsByClass, "Missing-class summary should be available");
    }

    private void printUtilizationDiagnostics(Town town, StandardSchool school) {
        Map<Integer, Staff> staffMap = town.getStaffPool().getStaffBySchoolAsMap(school);
        List<Staff> teachingStaff = staffMap.values().stream()
                .filter(staff -> staff.teacherStatistics.getStaffType() instanceof StaffType type
                        && TeacherBlockBuilder.isTeachingStaffType(type))
                .toList();
        long idleTeachingStaff = teachingStaff.stream()
                .filter(staff -> staff.teacherStatistics.getTeacherSchedule().size() == 0)
                .count();

        List<Staff> historyTeachers = teachingStaff.stream()
                .filter(staff -> staff.teacherStatistics.getStaffType() == StaffType.HISTORY)
                .toList();
        long idleHistoryTeachers = historyTeachers.stream()
                .filter(staff -> staff.teacherStatistics.getTeacherSchedule().size() == 0)
                .count();
        long activeHistoryTeachers = historyTeachers.size() - idleHistoryTeachers;

        Set<Room> usedRooms = new HashSet<>();
        long totalTeacherBlocks = 0;
        long historyBlocks = 0;
        for (Staff staff : teachingStaff) {
            for (TeacherBlock block : staff.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                totalTeacherBlocks++;
                if (block.getRoom() != null) {
                    usedRooms.add(block.getRoom());
                }
                if (SectionManager.belongsToSubjectArea(block.getClassName(), "history")) {
                    historyBlocks++;
                }
            }
        }

        Classroom[] classrooms = school.getClassrooms();
        long assignedClassrooms = java.util.Arrays.stream(classrooms)
                .filter(classroom -> !classroom.getAssignedStaff().isEmpty())
                .count();
        long usedClassrooms = java.util.Arrays.stream(classrooms)
                .filter(usedRooms::contains)
                .count();
        long unusedClassrooms = classrooms.length - usedClassrooms;

        int historyDemand = SectionManager.getDemandTracker().entrySet().stream()
                .filter(entry -> SectionManager.belongsToSubjectArea(entry.getKey(), "history"))
                .mapToInt(entry -> entry.getValue().totalDemand())
                .sum();
        int historyCapacity = SectionManager.getClassSections().entrySet().stream()
                .filter(entry -> SectionManager.belongsToSubjectArea(entry.getKey(), "history"))
                .flatMap(entry -> entry.getValue().stream())
                .mapToInt(SectionManager.ClassSection::getCapacity)
                .sum();

        System.out.println("Utilization diagnostics:");
        System.out.println("  Teaching staff: " + teachingStaff.size() + " total, " +
                (teachingStaff.size() - idleTeachingStaff) + " active, " + idleTeachingStaff + " idle");
        System.out.println("  History teachers: " + historyTeachers.size() + " total, " +
                activeHistoryTeachers + " active, " + idleHistoryTeachers + " idle");
        System.out.println("  Teacher blocks: " + totalTeacherBlocks + " total, " + historyBlocks + " history");
        System.out.println("  Classrooms: " + classrooms.length + " total, " + assignedClassrooms +
                " assigned, " + usedClassrooms + " used, " + unusedClassrooms + " unused");
        System.out.println("  History demand/capacity: " + historyDemand + " demand, " + historyCapacity + " capacity");
    }
}
