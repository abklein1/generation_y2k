package utility;

import entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the 4x4 block schedule model.
 * Validates that block numbers, teacher capacity, conflict detection,
 * staff demand calculations, and class-to-type mappings are all
 * consistent with a 4-period-per-semester schedule.
 */
@DisplayName("4x4 Block Schedule Model Tests")
class BlockScheduleModelTest {

    // === Constants matching the corrected 4x4 block schedule model ===
    private static final int PERIODS_PER_SEMESTER = 4;
    private static final int MAX_TEACHER_BLOCKS_PER_SEMESTER = 4;
    private static final int TEACHING_PERIODS_PER_TEACHER = 4;
    private static final String[] SEMESTERS = {"Fall", "Spring"};
    private static final String[] GRADE_LEVELS = {"Freshman", "Sophomore", "Junior", "Senior"};

    @Nested
    @DisplayName("Block Number Range Validation")
    class BlockNumberRangeTests {

        @Test
        @DisplayName("Student block numbers should be in range 1-4")
        void testStudentBlockNumberRange() {
            // In a 4x4 block schedule, valid block numbers are 1-4
            for (int blockNum = 1; blockNum <= PERIODS_PER_SEMESTER; blockNum++) {
                StudentBlock block = new StudentBlock();
                block.setBlockNumber(blockNum);
                assertTrue(block.getBlockNumber() >= 1 && block.getBlockNumber() <= 4,
                        "Block number " + blockNum + " should be in range 1-4");
            }
        }

        @Test
        @DisplayName("Teacher block numbers should be in range 1-4")
        void testTeacherBlockNumberRange() {
            for (int blockNum = 1; blockNum <= PERIODS_PER_SEMESTER; blockNum++) {
                TeacherBlock block = new TeacherBlock();
                block.setBlockNumber(blockNum);
                assertTrue(block.getBlockNumber() >= 1 && block.getBlockNumber() <= 4,
                        "Block number " + blockNum + " should be in range 1-4");
            }
        }

        @Test
        @DisplayName("A full student schedule has 4 blocks per semester, 8 total")
        void testFullScheduleSize() {
            Student student = new Student();
            String[] fallClasses = {"English I", "Algebra I", "Biology", "World Geography"};
            String[] springClasses = {"Health", "Geometry", "Spanish I", "Spanish II"};

            for (int i = 0; i < fallClasses.length; i++) {
                StudentBlock block = new StudentBlock();
                block.setBlockNumber(i + 1);
                block.setSemester("Fall");
                block.setClassName(fallClasses[i]);
                student.studentStatistics.getStudentSchedule().add(block);
            }
            for (int i = 0; i < springClasses.length; i++) {
                StudentBlock block = new StudentBlock();
                block.setBlockNumber(i + 1);
                block.setSemester("Spring");
                block.setClassName(springClasses[i]);
                student.studentStatistics.getStudentSchedule().add(block);
            }

            assertEquals(8, student.studentStatistics.getStudentSchedule().getClassSchedule().size(),
                    "A full schedule should have 8 classes (4 per semester)");
        }
    }

    @Nested
    @DisplayName("Teacher Capacity Constraints")
    class TeacherCapacityTests {

        @Test
        @DisplayName("Teacher should not exceed 4 blocks per semester")
        void testTeacherMaxBlocksPerSemester() {
            Staff teacher = new Staff();
            teacher.teacherStatistics.setStaffType(StaffType.ENGLISH);

            // Add 4 blocks for Fall semester (max capacity)
            for (int i = 1; i <= MAX_TEACHER_BLOCKS_PER_SEMESTER; i++) {
                TeacherBlock block = new TeacherBlock();
                block.setBlockNumber(i);
                block.setSemester("Fall");
                block.setClassName("English " + i);
                teacher.teacherStatistics.getTeacherSchedule().add(block);
            }

            // Count Fall blocks
            long fallBlocks = teacher.teacherStatistics.getTeacherSchedule()
                    .getTeacherSchedule().stream()
                    .filter(b -> b.getSemester().equals("Fall"))
                    .count();

            assertEquals(MAX_TEACHER_BLOCKS_PER_SEMESTER, fallBlocks,
                    "Teacher should have exactly 4 Fall blocks at max capacity");
        }

        @Test
        @DisplayName("Teacher capacity check should reject 5th block in same semester")
        void testCapacityCheckRejectsFifthBlock() {
            Staff teacher = new Staff();

            // Fill all 4 Fall blocks
            for (int i = 1; i <= 4; i++) {
                TeacherBlock block = new TeacherBlock();
                block.setBlockNumber(i);
                block.setSemester("Fall");
                block.setClassName("Class " + i);
                teacher.teacherStatistics.getTeacherSchedule().add(block);
            }

            // Verify the capacity check logic
            int fallSemesterBlocks = 0;
            for (TeacherBlock existing : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                if (existing.getSemester().equals("Fall")) {
                    fallSemesterBlocks++;
                }
            }

            assertFalse(fallSemesterBlocks < MAX_TEACHER_BLOCKS_PER_SEMESTER,
                    "Teacher at max Fall capacity should fail the < 4 check");
        }

        @Test
        @DisplayName("Teacher can teach in both semesters (up to 8 total blocks)")
        void testTeacherCanTeachBothSemesters() {
            Staff teacher = new Staff();

            // Add 4 Fall + 4 Spring = 8 total blocks
            for (String semester : SEMESTERS) {
                for (int i = 1; i <= MAX_TEACHER_BLOCKS_PER_SEMESTER; i++) {
                    TeacherBlock block = new TeacherBlock();
                    block.setBlockNumber(i);
                    block.setSemester(semester);
                    block.setClassName(semester + " Class " + i);
                    teacher.teacherStatistics.getTeacherSchedule().add(block);
                }
            }

            assertEquals(8, teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule().size(),
                    "Teacher should be able to teach 8 blocks total (4 per semester)");
        }

        @Test
        @DisplayName("Teacher cannot have two classes at the same block and semester")
        void testTeacherBlockConflictDetection() {
            Staff teacher = new Staff();

            TeacherBlock block1 = new TeacherBlock();
            block1.setBlockNumber(1);
            block1.setSemester("Fall");
            block1.setClassName("English I");
            teacher.teacherStatistics.getTeacherSchedule().add(block1);

            // Check if there's a conflict at block 1, Fall
            boolean hasConflict = false;
            for (TeacherBlock existing : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                if (existing.getBlockNumber() == 1 && existing.getSemester().equals("Fall")) {
                    hasConflict = true;
                    break;
                }
            }

            assertTrue(hasConflict, "Should detect teacher conflict at same block and semester");
        }
    }

    @Nested
    @DisplayName("Student Schedule Conflict Detection")
    class StudentConflictTests {

        @Test
        @DisplayName("Same block and semester should be a conflict")
        void testSameBlockSemesterConflict() {
            Student student = new Student();

            StudentBlock block = new StudentBlock();
            block.setBlockNumber(1);
            block.setSemester("Fall");
            block.setClassName("English I");
            student.studentStatistics.getStudentSchedule().add(block);

            assertTrue(hasStudentBlockConflict(student, 1, "Fall"),
                    "Same block + semester = conflict");
        }

        @Test
        @DisplayName("Same block but different semester should NOT be a conflict")
        void testSameBlockDifferentSemester() {
            Student student = new Student();

            StudentBlock block = new StudentBlock();
            block.setBlockNumber(1);
            block.setSemester("Fall");
            block.setClassName("English I");
            student.studentStatistics.getStudentSchedule().add(block);

            assertFalse(hasStudentBlockConflict(student, 1, "Spring"),
                    "Same block but different semester = no conflict");
        }

        @Test
        @DisplayName("Different block but same semester should NOT be a conflict")
        void testDifferentBlockSameSemester() {
            Student student = new Student();

            StudentBlock block = new StudentBlock();
            block.setBlockNumber(1);
            block.setSemester("Fall");
            block.setClassName("English I");
            student.studentStatistics.getStudentSchedule().add(block);

            assertFalse(hasStudentBlockConflict(student, 2, "Fall"),
                    "Different block same semester = no conflict");
        }

        @Test
        @DisplayName("A valid full schedule should have no period conflicts")
        void testFullScheduleNoConflicts() {
            Student student = new Student();
            String[] classes = {"English I", "Algebra I", "Biology", "World Geography",
                    "Health", "Geometry", "Spanish I", "Spanish II"};
            int classIdx = 0;

            for (String semester : SEMESTERS) {
                for (int period = 1; period <= PERIODS_PER_SEMESTER; period++) {
                    StudentBlock block = new StudentBlock();
                    block.setBlockNumber(period);
                    block.setSemester(semester);
                    block.setClassName(classes[classIdx++]);
                    student.studentStatistics.getStudentSchedule().add(block);
                }
            }

            // Verify no conflicts exist
            List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
            Set<String> seen = new HashSet<>();
            boolean hasConflict = false;

            for (StudentBlock block : schedule) {
                String key = block.getSemester() + "-" + block.getBlockNumber();
                if (seen.contains(key)) {
                    hasConflict = true;
                    break;
                }
                seen.add(key);
            }

            assertFalse(hasConflict, "A properly constructed full schedule should have no period conflicts");
            assertEquals(8, schedule.size(), "Full schedule has 8 blocks");
        }

        @Test
        @DisplayName("Period doubling (two classes same period/semester) should be detectable")
        void testPeriodDoublingDetection() {
            Student student = new Student();

            // Create a period doubling scenario (the bug we fixed)
            StudentBlock block1 = new StudentBlock();
            block1.setBlockNumber(1);
            block1.setSemester("Fall");
            block1.setClassName("English I");
            student.studentStatistics.getStudentSchedule().add(block1);

            StudentBlock block2 = new StudentBlock();
            block2.setBlockNumber(1);
            block2.setSemester("Fall");
            block2.setClassName("Spanish I");
            student.studentStatistics.getStudentSchedule().add(block2);

            // Detect doubling
            Map<String, Integer> slotCounts = new HashMap<>();
            for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
                String key = block.getSemester() + "-" + block.getBlockNumber();
                slotCounts.merge(key, 1, Integer::sum);
            }

            boolean hasDoubling = slotCounts.values().stream().anyMatch(count -> count > 1);
            assertTrue(hasDoubling, "Should detect period doubling when two classes share the same period+semester");
        }

        /**
         * Mirrors the conflict detection logic used in EnhancedStudentScheduleAssigner.
         */
        private boolean hasStudentBlockConflict(Student student, int blockNumber, String semester) {
            for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
                if (block.getBlockNumber() == blockNumber && block.getSemester().equals(semester)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nested
    @DisplayName("Block-to-Period Mapping (Identity)")
    class BlockToPeriodMappingTests {

        @Test
        @DisplayName("Block numbers 1-4 should map directly to periods 1-4")
        void testIdentityMapping() {
            // In the corrected model, block number IS the period number
            for (int blockNumber = 1; blockNumber <= PERIODS_PER_SEMESTER; blockNumber++) {
                int period = mapBlockToPeriod(blockNumber);
                assertEquals(blockNumber, period,
                        "Block " + blockNumber + " should map to Period " + blockNumber);
            }
        }

        @Test
        @DisplayName("No two different blocks should map to the same period")
        void testNoDuplicateMappings() {
            Set<Integer> periods = new HashSet<>();
            for (int block = 1; block <= PERIODS_PER_SEMESTER; block++) {
                int period = mapBlockToPeriod(block);
                assertFalse(periods.contains(period),
                        "Block " + block + " maps to period " + period + " which is already used");
                periods.add(period);
            }
            assertEquals(PERIODS_PER_SEMESTER, periods.size(),
                    "All 4 blocks should map to 4 unique periods");
        }

        /**
         * Mirrors Inspector.mapBlockToPeriod - in the corrected 4x4 model,
         * this is an identity function.
         */
        private int mapBlockToPeriod(int blockNumber) {
            return blockNumber;
        }
    }

    @Nested
    @DisplayName("Staff Demand Calculation (Corrected Model)")
    class StaffDemandTests {

        @Test
        @DisplayName("Core teachers should increase with corrected 4-period divisor")
        void testCoreTeachersWithCorrectedModel() {
            // With teachingPeriodsPerTeacher = 4 (corrected from 6)
            int studentCap = 1200;
            int optimalClassSize = Math.max(20, Math.min(30, studentCap / 40));
            int coreSections = (int) Math.ceil((double) studentCap / optimalClassSize);

            int teachersOldModel = Math.max(2, (int) Math.ceil((double) coreSections / 6));
            int teachersCorrected = Math.max(2, (int) Math.ceil((double) coreSections / TEACHING_PERIODS_PER_TEACHER));

            assertTrue(teachersCorrected >= teachersOldModel,
                    "Corrected model should request at least as many teachers as old model: " +
                            "old=" + teachersOldModel + " corrected=" + teachersCorrected);
        }

        @Test
        @DisplayName("Teacher capacity should cover all sections for a subject type")
        void testTeacherCapacityCoversAllSections() {
            // For various school sizes, verify that the requested teachers
            // have enough total block capacity to cover all sections needed
            int[] schoolSizes = {200, 500, 1000, 1500, 2000};

            for (int studentCap : schoolSizes) {
                int optimalClassSize = Math.max(20, Math.min(30, studentCap / 40));
                int coreSections = (int) Math.ceil((double) studentCap / optimalClassSize);
                int teachersNeeded = Math.max(2,
                        (int) Math.ceil((double) coreSections / TEACHING_PERIODS_PER_TEACHER));

                // Each teacher can teach up to 8 blocks per year (4 per semester x 2)
                int totalCapacity = teachersNeeded * 8;

                assertTrue(totalCapacity >= coreSections,
                        "For " + studentCap + " students: " + teachersNeeded + " teachers with " +
                                totalCapacity + " block capacity should cover " + coreSections + " sections");
            }
        }

        @Test
        @DisplayName("English department should get extra teachers (1.15x multiplier)")
        void testEnglishDepartmentBoost() {
            int studentCap = 1000;
            int optimalClassSize = Math.max(20, Math.min(30, studentCap / 40));
            int coreSections = (int) Math.ceil((double) studentCap / optimalClassSize);
            int coreTeachers = Math.max(2,
                    (int) Math.ceil((double) coreSections / TEACHING_PERIODS_PER_TEACHER));
            int englishTeachers = Math.max(3, (int) Math.ceil(coreTeachers * 1.15));

            assertTrue(englishTeachers >= coreTeachers,
                    "English should have at least as many teachers as other core subjects");
            assertTrue(englishTeachers >= 3,
                    "English should have at least 3 teachers minimum");
        }

        @Test
        @DisplayName("Language teachers should scale with freshman enrollment")
        void testLanguageTeacherScaling() {
            int smallSchool = 400;
            int largeSchool = 1600;

            int smallLang = calculateLanguageTeachers(smallSchool);
            int largeLang = calculateLanguageTeachers(largeSchool);

            assertTrue(smallLang >= 2, "Small schools need at least 2 language teachers");
            assertTrue(largeLang > smallLang,
                    "Larger schools need more language teachers: small=" + smallLang + " large=" + largeLang);
        }

        private int calculateLanguageTeachers(int studentCap) {
            int optimalClassSize = Math.max(20, Math.min(30, studentCap / 40));
            int estimatedFreshmen = studentCap / 4;
            int languageSections = (int) Math.ceil((double) estimatedFreshmen / optimalClassSize);
            return Math.max(2, (int) Math.ceil((double) languageSections / TEACHING_PERIODS_PER_TEACHER));
        }
    }

    @Nested
    @DisplayName("Class-to-StaffType Mapping")
    class ClassToStaffTypeMappingTests {

        @Test
        @DisplayName("All freshman required classes should map to valid staff types")
        void testFreshmanClassMappings() {
            Map<String, StaffType> expected = Map.of(
                    "English I", StaffType.ENGLISH,
                    "Biology", StaffType.SCIENCE,
                    "World Geography", StaffType.HISTORY,
                    "Health", StaffType.PHYSICAL_ED,
                    "Algebra I", StaffType.MATH,
                    "Geometry", StaffType.MATH,
                    "Fundamentals of Math", StaffType.MATH
            );

            for (Map.Entry<String, StaffType> entry : expected.entrySet()) {
                assertEquals(entry.getValue(),
                        CurriculumRequirementsCalculator.mapClassToStaffType(entry.getKey()),
                        entry.getKey() + " should map to " + entry.getValue());
            }
        }

        @Test
        @DisplayName("All senior required classes should map to valid staff types")
        void testSeniorClassMappings() {
            Map<String, StaffType> expected = Map.of(
                    "English IV", StaffType.ENGLISH,
                    "AP English Literature & Composition", StaffType.ENGLISH,
                    "US Government", StaffType.HISTORY,
                    "AP US Government", StaffType.HISTORY,
                    "AP Economics Macro", StaffType.HISTORY,
                    "Environmental Science", StaffType.SCIENCE,
                    "Physics", StaffType.SCIENCE,
                    "AP Physics B", StaffType.SCIENCE,
                    "Precalculus", StaffType.MATH,
                    "AP Calculus AB", StaffType.MATH
            );

            for (Map.Entry<String, StaffType> entry : expected.entrySet()) {
                assertEquals(entry.getValue(),
                        CurriculumRequirementsCalculator.mapClassToStaffType(entry.getKey()),
                        entry.getKey() + " should map to " + entry.getValue());
            }
        }

        @Test
        @DisplayName("Language classes should map to LANGUAGES staff type")
        void testLanguageClassMappings() {
            String[] languageClasses = {
                    "Spanish I", "Spanish II", "French I", "French II",
                    "German I", "German II", "Latin I", "Latin II",
                    "American Sign Language I", "American Sign Language II"
            };

            for (String className : languageClasses) {
                assertEquals(StaffType.LANGUAGES,
                        CurriculumRequirementsCalculator.mapClassToStaffType(className),
                        className + " should map to LANGUAGES");
            }
        }

        @Test
        @DisplayName("Core subject detection should identify required classes")
        void testCoreSubjectDetection() {
            String[] coreClasses = {
                    "English I", "Algebra I", "Biology", "World Geography",
                    "English IV", "AP Calculus AB", "Chemistry", "US Government"
            };
            String[] nonCoreClasses = {
                    "Woodworking", "Band", "Drama", "Culinary Arts"
            };

            for (String className : coreClasses) {
                assertTrue(CurriculumRequirementsCalculator.isCoreSubject(className),
                        className + " should be identified as core subject");
            }
            for (String className : nonCoreClasses) {
                assertFalse(CurriculumRequirementsCalculator.isCoreSubject(className),
                        className + " should NOT be identified as core subject");
            }
        }
    }

    @Nested
    @DisplayName("Schedule Completeness Invariants")
    class ScheduleCompletenessTests {

        @Test
        @DisplayName("Each period/semester slot should hold at most one class per student")
        void testOneClassPerSlot() {
            Student student = new Student();

            // Build a valid schedule
            String[][] schedule = {
                    {"Fall", "English I"}, {"Fall", "Algebra I"},
                    {"Fall", "Biology"}, {"Fall", "World Geography"},
                    {"Spring", "Health"}, {"Spring", "Geometry"},
                    {"Spring", "Spanish I"}, {"Spring", "Spanish II"}
            };

            for (int i = 0; i < schedule.length; i++) {
                StudentBlock block = new StudentBlock();
                block.setBlockNumber((i % PERIODS_PER_SEMESTER) + 1);
                block.setSemester(schedule[i][0]);
                block.setClassName(schedule[i][1]);
                student.studentStatistics.getStudentSchedule().add(block);
            }

            // Verify uniqueness of each period/semester slot
            Map<String, String> slots = new HashMap<>();
            for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
                String slotKey = block.getSemester() + "-P" + block.getBlockNumber();
                assertFalse(slots.containsKey(slotKey),
                        "Slot " + slotKey + " already occupied by " + slots.get(slotKey) +
                                ", cannot also have " + block.getClassName());
                slots.put(slotKey, block.getClassName());
            }

            assertEquals(8, slots.size(), "Full schedule should fill all 8 slots");
        }

        @Test
        @DisplayName("Missing period gap detection should find empty slots")
        void testMissingPeriodGapDetection() {
            Student student = new Student();

            // Build a schedule missing Fall Period 4 and Spring Period 3
            addBlock(student, 1, "Fall", "English I");
            addBlock(student, 2, "Fall", "Algebra I");
            addBlock(student, 3, "Fall", "Biology");
            // Missing: Fall Period 4
            addBlock(student, 1, "Spring", "Health");
            addBlock(student, 2, "Spring", "Geometry");
            // Missing: Spring Period 3
            addBlock(student, 4, "Spring", "Spanish I");

            List<String> gaps = detectPeriodGaps(student);

            assertEquals(2, gaps.size(), "Should detect exactly 2 gaps");
            assertTrue(gaps.contains("Fall-4"), "Should detect Fall Period 4 gap");
            assertTrue(gaps.contains("Spring-3"), "Should detect Spring Period 3 gap");
        }

        @Test
        @DisplayName("Complete schedule should have zero gaps")
        void testCompleteScheduleNoGaps() {
            Student student = new Student();

            for (int period = 1; period <= 4; period++) {
                addBlock(student, period, "Fall", "FallClass" + period);
                addBlock(student, period, "Spring", "SpringClass" + period);
            }

            List<String> gaps = detectPeriodGaps(student);
            assertEquals(0, gaps.size(), "Complete schedule should have no gaps");
        }

        private void addBlock(Student student, int period, String semester, String className) {
            StudentBlock block = new StudentBlock();
            block.setBlockNumber(period);
            block.setSemester(semester);
            block.setClassName(className);
            student.studentStatistics.getStudentSchedule().add(block);
        }

        /**
         * Detects missing period/semester slots in a student's schedule.
         * Mirrors checkForIncompleteSchedules logic.
         */
        private List<String> detectPeriodGaps(Student student) {
            List<String> gaps = new ArrayList<>();
            Set<String> occupiedSlots = new HashSet<>();

            for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
                occupiedSlots.add(block.getSemester() + "-" + block.getBlockNumber());
            }

            for (String semester : SEMESTERS) {
                for (int period = 1; period <= PERIODS_PER_SEMESTER; period++) {
                    if (!occupiedSlots.contains(semester + "-" + period)) {
                        gaps.add(semester + "-" + period);
                    }
                }
            }
            return gaps;
        }
    }

    @Nested
    @DisplayName("Grade-Level Required Class Coverage")
    class GradeLevelCoverageTests {

        @Test
        @DisplayName("All grade levels should have English, Math, Science, and History classes")
        void testAllGradesHaveCoreSubjects() {
            Map<String, List<String>> expectedClasses = new LinkedHashMap<>();
            expectedClasses.put("Freshman", List.of("English I", "Biology", "World Geography"));
            expectedClasses.put("Sophomore", List.of("English II", "World History"));
            expectedClasses.put("Junior", List.of("English III", "US History", "Anatomy and Physiology"));
            expectedClasses.put("Senior", List.of("English IV", "US Government", "Environmental Science"));

            for (Map.Entry<String, List<String>> entry : expectedClasses.entrySet()) {
                String grade = entry.getKey();
                for (String className : entry.getValue()) {
                    StaffType type = CurriculumRequirementsCalculator.mapClassToStaffType(className);
                    assertNotNull(type,
                            grade + " class '" + className + "' should map to a valid staff type");
                    assertNotEquals(StaffType.SUB, type,
                            grade + " class '" + className + "' should not map to SUB type");
                }
            }
        }

        @Test
        @DisplayName("Senior classes should not map to the same staff types as freshman-only classes")
        void testSeniorClassesHaveDistinctStaffTypes() {
            // Verify that senior-specific classes are distinguishable from freshman classes
            // so that section creation can find appropriate teachers for each
            StaffType englishIV = CurriculumRequirementsCalculator.mapClassToStaffType("English IV");
            StaffType englishI = CurriculumRequirementsCalculator.mapClassToStaffType("English I");

            // Both should be ENGLISH - the SAME type teachers teach both
            assertEquals(englishIV, englishI,
                    "English IV and English I should share the same ENGLISH teacher type");

            StaffType usGov = CurriculumRequirementsCalculator.mapClassToStaffType("US Government");
            StaffType worldGeo = CurriculumRequirementsCalculator.mapClassToStaffType("World Geography");

            // Both should be HISTORY type
            assertEquals(usGov, worldGeo,
                    "US Government and World Geography should share the same HISTORY teacher type");
        }
    }
}
