package utility;

import config.TownDemographics;
import entity.Neighborhood;
import entity.Staff;
import entity.Student;
import entity.Town;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import view.GameView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Neighborhood Assignment Service")
class NeighborhoodAssignmentServiceTest {

    @Test
    @DisplayName("Generated neighborhood names should match their wealth tier")
    void testNeighborhoodNamesMatchTier() {
        GameRandom.reset();
        GameRandom.initialize(101L);

        Set<String> usedNames = new HashSet<>();
        String lowName = NeighborhoodNameLoader.generateUniqueNeighborhoodName("low", usedNames);
        usedNames.add(lowName);
        String middleName = NeighborhoodNameLoader.generateUniqueNeighborhoodName("middle", usedNames);
        usedNames.add(middleName);
        String highName = NeighborhoodNameLoader.generateUniqueNeighborhoodName("high", usedNames);

        assertAll("Generated neighborhood names should use the correct naming pool",
                () -> assertTrue(NeighborhoodNameLoader.isValidNameForWealthLevel("low", lowName)),
                () -> assertTrue(NeighborhoodNameLoader.isValidNameForWealthLevel("middle", middleName)),
                () -> assertTrue(NeighborhoodNameLoader.isValidNameForWealthLevel("high", highName)),
                () -> assertNotEquals(lowName, middleName),
                () -> assertNotEquals(middleName, highName));
    }

    @Test
    @DisplayName("Sibling-linked students should stay in the same neighborhood")
    void testSiblingHouseholdsStayTogether() {
        GameRandom.reset();
        GameRandom.initialize(202L);

        Town town = new Town("SiblingTown");
        Student student = createStudent("middle", true);
        Student sibling = createStudent("middle", false);
        student.studentStatistics.addSiblingsNotInSchool(sibling);
        sibling.studentStatistics.addSiblingsInSchool(student);

        town.getStudentPool().addStudent(student);
        town.getStudentPool().addStudent(sibling);

        NeighborhoodAssignmentService.assignNeighborhoods(town);

        assertEquals(student.studentStatistics.getNeighborhoodName(), sibling.studentStatistics.getNeighborhoodName());
    }

    @Test
    @DisplayName("Students should never jump more than one neighborhood wealth tier")
    void testStudentTierRestrictions() {
        GameRandom.reset();
        GameRandom.initialize(303L);

        Town town = new Town("IncomeTown");
        for (int i = 0; i < 12; i++) {
            town.getStudentPool().addStudent(createStudent("low", true));
            town.getStudentPool().addStudent(createStudent("high", true));
        }
        for (int i = 0; i < 8; i++) {
            town.getStudentPool().addStudent(createStudent("middle", true));
        }

        NeighborhoodAssignmentService.assignNeighborhoods(town);

        for (Student student : town.getStudentPool().getAllStudents().values()) {
            String incomeLevel = student.studentStatistics.getIncomeLevel();
            String neighborhoodWealth = student.studentStatistics.getNeighborhoodWealthLevel();
            if ("low".equals(incomeLevel)) {
                assertNotEquals("high", neighborhoodWealth, "Low-income students cannot be assigned to high-wealth neighborhoods");
            } else if ("high".equals(incomeLevel)) {
                assertNotEquals("low", neighborhoodWealth, "High-income students cannot be assigned to low-wealth neighborhoods");
            }
        }
    }

    @Test
    @DisplayName("All residents should be assigned without exceeding neighborhood capacity")
    void testNeighborhoodCapacityAndResidentCoverage() {
        GameRandom.reset();
        GameRandom.initialize(404L);

        Town town = new Town("CapacityTown");
        for (int i = 0; i < 18; i++) {
            String incomeLevel = (i % 3 == 0) ? "low" : (i % 3 == 1 ? "middle" : "high");
            town.getStudentPool().addStudent(createStudent(incomeLevel, i % 4 != 0));
        }
        for (int i = 0; i < 7; i++) {
            town.getStaffPool().addStaff(new Staff());
        }

        NeighborhoodAssignmentService.assignNeighborhoods(town);

        List<Neighborhood> neighborhoods = town.getNeighborhoods();
        int totalResidents = town.getStudentPool().getAllStudents().size() + town.getStaffPool().getAllStaff().size();
        int assignedResidents = neighborhoods.stream().mapToInt(Neighborhood::getCurrentPopulation).sum();

        assertAll("Neighborhood assignment coverage",
                () -> assertFalse(neighborhoods.isEmpty()),
                () -> assertEquals(totalResidents, assignedResidents),
                () -> assertTrue(neighborhoods.stream()
                        .allMatch(neighborhood -> neighborhood.getCurrentPopulation() <= neighborhood.getPopulationCapacity())),
                () -> assertTrue(neighborhoods.stream()
                        .anyMatch(neighborhood -> neighborhood.getCurrentPopulation() < neighborhood.getPopulationCapacity())),
                () -> assertTrue(town.getStudentPool().getAllStudents().values().stream()
                        .allMatch(student -> student.studentStatistics.getNeighborhoodName() != null)),
                () -> assertTrue(town.getStaffPool().getAllStaff().values().stream()
                        .allMatch(staff -> staff.teacherStatistics.getNeighborhoodName() != null)));
    }

    @Test
    @DisplayName("Town generation should honor demographics income distribution before neighborhood assignment")
    void testTownGenerationUsesDemographicsIncomeDistribution() {
        GameRandom.reset();
        GameRandom.initialize(505L);
        GameLogger.reset();

        TownDemographics demographics = new TownDemographics();
        demographics.setTotalStudentPopulation(20);
        demographics.setTotalStaffPopulation(0);
        demographics.setExtraStudentPoolPercent(0.0);
        demographics.setExtraStaffPoolPercent(0.0);

        Map<String, Double> incomeDistribution = new HashMap<>();
        incomeDistribution.put("Low", 0.0);
        incomeDistribution.put("Middle", 0.0);
        incomeDistribution.put("High", 1.0);
        demographics.setIncomeDistribution(incomeDistribution);

        Town town = TownPopulationGenerator.generateTown("DemographicsTown", demographics, new GameView());

        assertAll("All generated students should use the demographics-aware income distribution",
                () -> assertFalse(town.getNeighborhoods().isEmpty()),
                () -> assertTrue(town.getStudentPool().getAllStudents().values().stream()
                        .allMatch(student -> "high".equals(student.studentStatistics.getIncomeLevel()))),
                () -> assertTrue(town.getStudentPool().getAllStudents().values().stream()
                        .allMatch(student -> student.studentStatistics.getNeighborhoodName() != null)));
    }

    private Student createStudent(String incomeLevel, boolean inHighSchool) {
        Student student = new Student();
        student.studentStatistics.setIncomeLevel(incomeLevel);
        student.setInHighSchool(inHighSchool);
        return student;
    }
}
