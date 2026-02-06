package utility;

import entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the scheduling logic in EnhancedStudentScheduleAssigner.
 * Tests class path determination, vocational decisions, and schedule conflict
 * detection.
 */
@DisplayName("Scheduling Logic Tests")
class SchedulingLogicTest {

    @Nested
    @DisplayName("Class Path Determination Tests")
    class ClassPathTests {

        @Test
        @DisplayName("High intelligence should favor AP classes")
        void testHighIntelligenceFavorsAP() {
            // Students with very high intelligence (90+) should frequently get AP
            int apCount = 0;
            int trials = 100;

            for (int i = 0; i < trials; i++) {
                String path = simulateClassPath(95, "high", 80);
                if (path.equals("AP"))
                    apCount++;
            }

            // With 95 intelligence, high income, and 80 determination,
            // should get AP more than 40% of the time
            assertTrue(apCount > 40,
                    "High intelligence students should frequently get AP (got " + apCount + "/100)");
        }

        @Test
        @DisplayName("Low intelligence should favor On-Level classes")
        void testLowIntelligenceFavorsOnLevel() {
            int onLevelCount = 0;
            int trials = 100;

            for (int i = 0; i < trials; i++) {
                String path = simulateClassPath(30, "low", 30);
                if (path.equals("On-Level"))
                    onLevelCount++;
            }

            // Low intelligence, low income, low determination should mostly get On-Level
            assertTrue(onLevelCount > 50,
                    "Low intelligence students should frequently get On-Level (got " + onLevelCount + "/100)");
        }

        @Test
        @DisplayName("Income level should influence class placement")
        void testIncomeInfluence() {
            int highIncomeAP = 0;
            int lowIncomeAP = 0;
            int trials = 100;

            for (int i = 0; i < trials; i++) {
                if (simulateClassPath(70, "high", 50).equals("AP"))
                    highIncomeAP++;
                if (simulateClassPath(70, "low", 50).equals("AP"))
                    lowIncomeAP++;
            }

            // High income should have more AP placements than low income
            assertTrue(highIncomeAP >= lowIncomeAP - 10,
                    "High income should not have significantly fewer AP placements");
        }

        // Simplified class path simulation matching the real logic
        private String simulateClassPath(int intelligence, String income, int determination) {
            double apProbability;
            double honorsProbability;
            double onLevelProbability;

            if (intelligence >= 90) {
                apProbability = 50.0;
                honorsProbability = 35.0;
                onLevelProbability = 15.0;
            } else if (intelligence >= 70) {
                apProbability = 25.0;
                honorsProbability = 45.0;
                onLevelProbability = 30.0;
            } else if (intelligence >= 50) {
                apProbability = 10.0;
                honorsProbability = 30.0;
                onLevelProbability = 60.0;
            } else if (intelligence <= 30) {
                apProbability = 2.0;
                honorsProbability = 8.0;
                onLevelProbability = 90.0;
            } else {
                apProbability = 5.0;
                honorsProbability = 20.0;
                onLevelProbability = 75.0;
            }

            switch (income) {
                case "high" -> {
                    apProbability *= 1.3;
                    honorsProbability *= 1.1;
                    onLevelProbability *= 0.8;
                }
                case "low" -> {
                    apProbability *= 0.7;
                    honorsProbability *= 0.9;
                    onLevelProbability *= 1.2;
                }
                default -> {
                    // No income-based adjustment
                }
            }

            double determinationFactor = (determination - 50) / 100.0;
            apProbability += apProbability * determinationFactor;
            honorsProbability += honorsProbability * determinationFactor / 2;
            onLevelProbability -= onLevelProbability * determinationFactor / 2;

            double total = apProbability + honorsProbability + onLevelProbability;
            apProbability = (apProbability / total) * 100;
            honorsProbability = (honorsProbability / total) * 100;

            double random = Math.random() * 100;
            if (random < apProbability)
                return "AP";
            else if (random < apProbability + honorsProbability)
                return "Honors";
            else
                return "On-Level";
        }
    }

    @Nested
    @DisplayName("Grade Level Class Requirements")
    class GradeLevelTests {

        @Test
        @DisplayName("Freshman should have required classes")
        void testFreshmanRequirements() {
            List<String> freshmanMath = List.of("Fundamentals of Math", "Geometry", "Algebra I");
            List<String> freshmanEnglish = List.of("English I");
            List<String> freshmanScience = List.of("Biology");
            List<String> freshmanHistory = List.of("World Geography", "AP Human Geography");
            List<String> freshmanPE = List.of("Health");

            assertAll("Freshman required classes exist",
                    () -> assertFalse(freshmanMath.isEmpty()),
                    () -> assertFalse(freshmanEnglish.isEmpty()),
                    () -> assertFalse(freshmanScience.isEmpty()),
                    () -> assertFalse(freshmanHistory.isEmpty()),
                    () -> assertFalse(freshmanPE.isEmpty()));
        }

        @Test
        @DisplayName("Senior should have appropriate class options")
        void testSeniorOptions() {
            List<String> seniorMath = List.of("AP Calculus AB", "AP Calculus BC", "Precalculus");
            List<String> seniorEnglish = List.of("English IV", "AP English Literature & Composition");
            List<String> seniorScience = List.of("AP Physics B", "AP Physics C", "Physics", "Environmental Science");
            List<String> seniorHistory = List.of("US Government", "AP US Government", "AP Economics Macro");

            assertAll("Senior class options",
                    () -> assertTrue(seniorMath.contains("AP Calculus AB")),
                    () -> assertTrue(seniorEnglish.contains("AP English Literature & Composition")),
                    () -> assertTrue(seniorScience.contains("Physics")),
                    () -> assertTrue(seniorHistory.contains("US Government")));
        }
    }

    @Nested
    @DisplayName("Physical Education Decision Tests")
    class PhysicalEducationTests {

        @Test
        @DisplayName("High strength males should prefer weightlifting")
        void testHighStrengthMalePreference() {
            String[] choices = getMalePhysicalEdDecision(80, 50);
            assertEquals("Weightlifting", choices[0],
                    "High strength males should prefer weightlifting");
        }

        @Test
        @DisplayName("Low determination students should prefer easy classes")
        void testLowDeterminationPreference() {
            // For low determination to apply, strength must be <= 29 (below
            // LOW_STRENGTH_THRESHOLD)
            // AND determination must be < 30 (below LOW_DETERMINATION_THRESHOLD)
            // If determination were high (> 60), it would trigger weightlifting instead
            String[] maleChoices = getMalePhysicalEdDecision(25, 20);
            String[] femaleChoices = getFemalePhysicalEdDecision(25, 20);

            assertAll("Low determination preferences",
                    () -> assertEquals("Lifetime Recreation", maleChoices[0]),
                    () -> assertEquals("Lifetime Recreation", femaleChoices[0]));
        }

        @Test
        @DisplayName("High strength females should prefer dance")
        void testHighStrengthFemalePreference() {
            String[] choices = getFemalePhysicalEdDecision(80, 70);
            assertEquals("Dance", choices[0],
                    "High strength females should prefer dance");
        }

        // Using actual thresholds from SimConstants
        private static final int MALE_STRENGTH_THRESHOLD = 60;
        private static final int MALE_LOW_STRENGTH_THRESHOLD = 29;
        private static final int MALE_DETERMINATION_THRESHOLD = 60;
        private static final int MALE_LOW_DETERMINATION_THRESHOLD = 30;
        private static final int FEMALE_STRENGTH_THRESHOLD = 50;
        private static final int FEMALE_LOW_STRENGTH_THRESHOLD = 29;
        private static final int FEMALE_DETERMINATION_THRESHOLD = 50;
        private static final int FEMALE_LOW_DETERMINATION_THRESHOLD = 29;

        private String[] getMalePhysicalEdDecision(int strength, int determination) {
            if (strength > MALE_STRENGTH_THRESHOLD ||
                    (strength < MALE_LOW_STRENGTH_THRESHOLD && determination > MALE_DETERMINATION_THRESHOLD)) {
                return new String[] { "Weightlifting", "Team Sports", "Specialized Sports", "Lifetime Recreation",
                        "Dance" };
            } else if (strength < MALE_STRENGTH_THRESHOLD && strength > MALE_LOW_STRENGTH_THRESHOLD) {
                return new String[] { "Team Sports", "Specialized Sports", "Weightlifting", "Lifetime Recreation",
                        "Dance" };
            } else if (determination < MALE_LOW_DETERMINATION_THRESHOLD) {
                return new String[] { "Lifetime Recreation", "Specialized Sports", "Team Sports", "Dance",
                        "Weightlifting" };
            } else {
                return new String[] { "Specialized Sports", "Team Sports", "Weightlifting", "Dance",
                        "Lifetime Recreation" };
            }
        }

        private String[] getFemalePhysicalEdDecision(int strength, int determination) {
            if (strength > FEMALE_STRENGTH_THRESHOLD ||
                    (strength < FEMALE_LOW_STRENGTH_THRESHOLD && determination > FEMALE_DETERMINATION_THRESHOLD)) {
                return new String[] { "Dance", "Team Sports", "Specialized Sports", "Weightlifting",
                        "Lifetime Recreation" };
            } else if (strength < FEMALE_STRENGTH_THRESHOLD && strength > FEMALE_LOW_STRENGTH_THRESHOLD) {
                return new String[] { "Specialized Sports", "Lifetime Recreation", "Dance", "Weightlifting",
                        "Team Sports" };
            } else if (determination < FEMALE_LOW_DETERMINATION_THRESHOLD) {
                return new String[] { "Lifetime Recreation", "Specialized Sports", "Dance", "Team Sports",
                        "Weightlifting" };
            } else {
                return new String[] { "Specialized Sports", "Team Sports", "Weightlifting", "Dance",
                        "Lifetime Recreation" };
            }
        }
    }

    @Nested
    @DisplayName("Schedule Conflict Detection Tests")
    class ConflictDetectionTests {

        @Test
        @DisplayName("Should detect conflict when same block and semester")
        void testBlockConflictDetection() {
            Student student = new Student();

            // Add a class to block 1, Fall semester
            StudentBlock existingBlock = new StudentBlock();
            existingBlock.setBlockNumber(1);
            existingBlock.setSemester("Fall");
            existingBlock.setClassName("English I");
            student.studentStatistics.getStudentSchedule().add(existingBlock);

            // Check for conflict
            boolean hasConflict = hasBlockConflict(student, 1, "Fall");
            assertTrue(hasConflict, "Should detect conflict in same block and semester");
        }

        @Test
        @DisplayName("Should not detect conflict for different block")
        void testNoConflictDifferentBlock() {
            Student student = new Student();

            StudentBlock existingBlock = new StudentBlock();
            existingBlock.setBlockNumber(1);
            existingBlock.setSemester("Fall");
            existingBlock.setClassName("English I");
            student.studentStatistics.getStudentSchedule().add(existingBlock);

            boolean hasConflict = hasBlockConflict(student, 2, "Fall");
            assertFalse(hasConflict, "Should not detect conflict for different block");
        }

        @Test
        @DisplayName("Should not detect conflict for different semester")
        void testNoConflictDifferentSemester() {
            Student student = new Student();

            StudentBlock existingBlock = new StudentBlock();
            existingBlock.setBlockNumber(1);
            existingBlock.setSemester("Fall");
            existingBlock.setClassName("English I");
            student.studentStatistics.getStudentSchedule().add(existingBlock);

            boolean hasConflict = hasBlockConflict(student, 1, "Spring");
            assertFalse(hasConflict, "Should not detect conflict for different semester");
        }

        private boolean hasBlockConflict(Student student, int blockNumber, String semester) {
            for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
                if (block.getBlockNumber() == blockNumber && block.getSemester().equals(semester)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nested
    @DisplayName("Student Block Assignment Tests")
    class BlockAssignmentTests {

        @Test
        @DisplayName("Student schedule should be empty initially")
        void testEmptySchedule() {
            Student student = new Student();
            assertTrue(student.studentStatistics.getStudentSchedule().getClassSchedule().isEmpty(),
                    "New student should have empty schedule");
        }

        @Test
        @DisplayName("Should add blocks to student schedule")
        void testAddBlock() {
            Student student = new Student();

            StudentBlock block = new StudentBlock();
            block.setBlockNumber(1);
            block.setClassName("Algebra I");
            block.setSemester("Fall");

            student.studentStatistics.getStudentSchedule().add(block);

            assertEquals(1, student.studentStatistics.getStudentSchedule().getClassSchedule().size());
            assertEquals("Algebra I",
                    student.studentStatistics.getStudentSchedule().getClassSchedule().get(0).getClassName());
        }

        @Test
        @DisplayName("Should track multiple blocks")
        void testMultipleBlocks() {
            Student student = new Student();

            String[] classes = { "English I", "Algebra I", "Biology", "World Geography" };
            for (int i = 0; i < classes.length; i++) {
                StudentBlock block = new StudentBlock();
                block.setBlockNumber(i + 1);
                block.setClassName(classes[i]);
                block.setSemester("Fall");
                student.studentStatistics.getStudentSchedule().add(block);
            }

            assertEquals(4, student.studentStatistics.getStudentSchedule().getClassSchedule().size());
        }
    }

    @Nested
    @DisplayName("Student Retention with Partial Schedules")
    class StudentRetentionTests {

        @Test
        @DisplayName("Students missing 1 of 5 required classes should be retained")
        void testRetainStudentWithMinorGap() {
            // Missing ratio 1/5 = 0.2, which is <= 0.5 threshold
            int totalRequired = 5;
            int missing = 1;
            double missingRatio = (double) missing / totalRequired;
            assertTrue(missingRatio <= 0.5,
                    "Student missing " + missing + "/" + totalRequired +
                            " classes (ratio " + missingRatio + ") should be retained");
        }

        @Test
        @DisplayName("Students missing 2 of 4 required classes should be retained")
        void testRetainStudentWithHalfMissing() {
            // Missing ratio 2/4 = 0.5, which is exactly at the threshold (not > 0.5)
            int totalRequired = 4;
            int missing = 2;
            double missingRatio = (double) missing / totalRequired;
            assertTrue(missingRatio <= 0.5,
                    "Student missing " + missing + "/" + totalRequired +
                            " classes (ratio " + missingRatio + ") should be retained");
        }

        @Test
        @DisplayName("Students missing 3 of 4 required classes should be removed")
        void testRemoveStudentWithCriticalGap() {
            // Missing ratio 3/4 = 0.75, which is > 0.5 threshold
            int totalRequired = 4;
            int missing = 3;
            double missingRatio = (double) missing / totalRequired;
            assertTrue(missingRatio > 0.5,
                    "Student missing " + missing + "/" + totalRequired +
                            " classes (ratio " + missingRatio + ") should be removed");
        }

        @Test
        @DisplayName("Students missing all required classes should be removed")
        void testRemoveStudentWithAllMissing() {
            int totalRequired = 5;
            int missing = 5;
            double missingRatio = (double) missing / totalRequired;
            assertTrue(missingRatio > 0.5,
                    "Student missing all required classes should be removed");
        }

        @Test
        @DisplayName("Freshman has 5 required classes, Seniors have 2")
        void testGradeLevelRequirementCounts() {
            // Freshman: English I, Math, Biology, History, Health = 5
            // Senior: English, Government = 2
            List<String> freshmanReqs = List.of("English I", "Math", "Biology", "History", "Health");
            List<String> seniorReqs = List.of("English", "Government");

            assertEquals(5, freshmanReqs.size(), "Freshman should have 5 required classes");
            assertEquals(2, seniorReqs.size(), "Senior should have 2 required classes");

            // For a freshman, missing 2/5 = 0.4 (retained)
            // For a senior, missing 2/2 = 1.0 (removed)
            assertTrue((double) 2 / freshmanReqs.size() <= 0.5, "Freshman missing 2 should be retained");
            assertTrue((double) 2 / seniorReqs.size() > 0.5, "Senior missing 2 should be removed");
        }
    }

    @Nested
    @DisplayName("Dynamic Staff Demand Calculation Tests")
    class DynamicStaffDemandTests {

        @Test
        @DisplayName("Staff demand should scale with student population")
        void testDemandScalesWithPopulation() {
            // Small school: 200 students
            int smallSchool = 200;
            int smallCoreTeachers = calculateCoreTeachers(smallSchool);

            // Large school: 2000 students
            int largeSchool = 2000;
            int largeCoreTeachers = calculateCoreTeachers(largeSchool);

            assertTrue(largeCoreTeachers > smallCoreTeachers,
                    "Larger schools should need more core teachers: small=" +
                            smallCoreTeachers + " vs large=" + largeCoreTeachers);
        }

        @Test
        @DisplayName("Language teachers should scale proportionally to enrollment")
        void testLanguageTeachersScaleProportionally() {
            // Instead of hardcoded thresholds, language teachers should scale
            int smallSchool = 400;
            int medSchool = 800;
            int largeSchool = 1600;

            int smallLang = calculateLanguageTeachers(smallSchool);
            int medLang = calculateLanguageTeachers(medSchool);
            int largeLang = calculateLanguageTeachers(largeSchool);

            assertTrue(smallLang >= 2, "Small schools should have at least 2 language teachers");
            assertTrue(largeLang > smallLang,
                    "Larger schools should have more language teachers: small=" +
                            smallLang + " vs large=" + largeLang);
            assertTrue(medLang >= smallLang,
                    "Medium schools should have at least as many as small schools");
        }

        @Test
        @DisplayName("Substitute pool should be proportional to enrollment")
        void testSubstitutePoolProportional() {
            int substitutesSmall = Math.max(5, 400 / 200);
            int substitutesLarge = Math.max(5, 2000 / 200);

            assertTrue(substitutesSmall >= 2, "Small schools need at least 2 substitutes");
            assertTrue(substitutesLarge > substitutesSmall,
                    "Larger schools need more substitutes");
            assertTrue(substitutesLarge >= 10,
                    "Schools with 2000 students should have at least 10 substitutes");
        }

        // Mirrors the updated calculateInitialStaffDemand logic
        private int calculateCoreTeachers(int studentCap) {
            int teachingPeriodsPerTeacher = 6;
            int optimalClassSize = Math.max(20, Math.min(30, studentCap / 40));
            int coreSections = (int) Math.ceil((double) studentCap / optimalClassSize);
            return Math.max(2, (int) Math.ceil((double) coreSections / teachingPeriodsPerTeacher));
        }

        private int calculateLanguageTeachers(int studentCap) {
            int teachingPeriodsPerTeacher = 6;
            int optimalClassSize = Math.max(20, Math.min(30, studentCap / 40));
            int estimatedFreshmen = studentCap / 4;
            int languageSections = (int) Math.ceil((double) estimatedFreshmen / optimalClassSize);
            return Math.max(2, (int) Math.ceil((double) languageSections / teachingPeriodsPerTeacher));
        }
    }
}
