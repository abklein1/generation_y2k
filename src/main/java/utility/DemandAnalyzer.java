package utility;

import config.SchoolFundingModel;
import entity.StaffType;
import entity.Student;

import java.util.*;

import static constants.SchoolConstants.TOTAL_SCHOOL_PERIODS;

/**
 * Analyzes student demand to determine class lists, staff needs, sections,
 * and room requirements. This is the single source of truth for demand
 * aggregation, replacing both {@code ESSA.analyzeDemandWithTraits()} and
 * the similar logic in
 * {@link CurriculumRequirementsCalculator#analyzeRequirements}.
 *
 * Created as part of Phase 2a.
 */
public class DemandAnalyzer {

    // ---------------------------------------------------------------- result
    /**
     * Immutable result of a demand analysis.
     */
    public static class DemandResult {
        private final Map<String, Integer> classDemand; // className -> studentCount
        private final Map<String, Set<Student>> classStudents; // className -> interested students
        private final Map<StaffType, Integer> staffDemandByType; // staffType -> total student-slots
        private final Map<StaffType, Integer> teachersNeeded; // staffType -> teachers required
        private final Map<String, Integer> sectionsNeeded; // className -> section count
        private final Map<String, String> roomTypeByClass; // className -> room type string
        private final Map<String, Integer> roomsNeeded; // roomType -> count
        private final int totalStudents;

        DemandResult(Map<String, Integer> classDemand,
                Map<String, Set<Student>> classStudents,
                Map<StaffType, Integer> staffDemandByType,
                Map<StaffType, Integer> teachersNeeded,
                Map<String, Integer> sectionsNeeded,
                Map<String, String> roomTypeByClass,
                Map<String, Integer> roomsNeeded,
                int totalStudents) {
            this.classDemand = Collections.unmodifiableMap(new LinkedHashMap<>(classDemand));
            this.classStudents = Collections.unmodifiableMap(classStudents);
            this.staffDemandByType = Collections.unmodifiableMap(new EnumMap<>(staffDemandByType));
            this.teachersNeeded = Collections.unmodifiableMap(new EnumMap<>(teachersNeeded));
            this.sectionsNeeded = Collections.unmodifiableMap(new LinkedHashMap<>(sectionsNeeded));
            this.roomTypeByClass = Collections.unmodifiableMap(new LinkedHashMap<>(roomTypeByClass));
            this.roomsNeeded = Collections.unmodifiableMap(new LinkedHashMap<>(roomsNeeded));
            this.totalStudents = totalStudents;
        }

        /** Class name -> number of students requesting it. */
        public Map<String, Integer> getClassDemand() {
            return classDemand;
        }

        /** Class name -> set of interested students. */
        public Map<String, Set<Student>> getClassStudents() {
            return classStudents;
        }

        /** StaffType -> aggregate student-slots across all classes of that type. */
        public Map<StaffType, Integer> getStaffDemandByType() {
            return staffDemandByType;
        }

        /** StaffType -> number of teachers required. */
        public Map<StaffType, Integer> getTeachersNeeded() {
            return teachersNeeded;
        }

        /** Alias so callers can use {@code demand.staffNeeds()} as in the plan. */
        public Map<StaffType, Integer> staffNeeds() {
            return teachersNeeded;
        }

        /** Class name -> number of sections required. */
        public Map<String, Integer> getSectionsNeeded() {
            return sectionsNeeded;
        }

        /** Room type string (e.g. "Classroom", "ScienceLab") -> count needed. */
        public Map<String, Integer> getRoomsNeeded() {
            return roomsNeeded;
        }

        /** Class name -> preferred room type used during adaptation. */
        public Map<String, String> getRoomTypeByClass() {
            return roomTypeByClass;
        }

        /** Alias so callers can use {@code demand.roomNeeds()} as in the plan. */
        public Map<String, Integer> roomNeeds() {
            return roomsNeeded;
        }

        public int getTotalStudents() {
            return totalStudents;
        }
    }

    // ------------------------------------------------------------- analysis

    /**
     * Full demand analysis using an explicit class-lists map.
     *
     * @param classLists   student -> ordered list of class names (from
     *                     {@link StudentClassDeterminer#determineStudentClasses})
     * @param fundingModel used for section / room sizing
     * @return aggregated demand
     */
    public static DemandResult analyze(Map<Student, List<String>> classLists,
            SchoolFundingModel fundingModel) {
        // 1. Aggregate demand by class name
        Map<String, Integer> classDemand = new LinkedHashMap<>();
        Map<String, Set<Student>> classStudents = new LinkedHashMap<>();

        for (Map.Entry<Student, List<String>> entry : classLists.entrySet()) {
            Student student = entry.getKey();
            for (String className : entry.getValue()) {
                classDemand.merge(className, 1, Integer::sum);
                classStudents.computeIfAbsent(className, k -> new LinkedHashSet<>()).add(student);
            }
        }

        int optimalClassSize = fundingModel != null ? fundingModel.getOptimalClassSize() : 25;
        int classesPerTeacher = 4; // 4x4 block schedule

        // 2. Aggregate demand by StaffType
        Map<StaffType, Integer> staffDemandByType = new EnumMap<>(StaffType.class);
        for (Map.Entry<String, Integer> entry : classDemand.entrySet()) {
            StaffType type = CurriculumRequirementsCalculator.mapClassToStaffType(entry.getKey());
            staffDemandByType.merge(type, entry.getValue(), Integer::sum);
        }

        // 3. Sections needed per class
        Map<String, Integer> sectionsNeeded = new LinkedHashMap<>();
        Map<StaffType, Integer> totalSectionsByType = new EnumMap<>(StaffType.class);

        for (Map.Entry<String, Integer> entry : classDemand.entrySet()) {
            String className = entry.getKey();
            int demand = entry.getValue();
            int sections = Math.max(1, (int) Math.ceil((double) demand / optimalClassSize));
            sectionsNeeded.put(className, sections);

            StaffType type = CurriculumRequirementsCalculator.mapClassToStaffType(className);
            totalSectionsByType.merge(type, sections, Integer::sum);
        }

        // 4. Teachers needed per StaffType
        Map<StaffType, Integer> teachersNeeded = new EnumMap<>(StaffType.class);
        for (Map.Entry<StaffType, Integer> entry : totalSectionsByType.entrySet()) {
            int teachers = Math.max(1, (int) Math.ceil((double) entry.getValue() / classesPerTeacher));
            teachersNeeded.put(entry.getKey(), teachers);
        }

        // Add support staff estimates
        int totalStudents = classLists.size();
        addSupportStaffEstimates(teachersNeeded, totalStudents);

        // 5. Room requirements
        Map<String, String> roomTypeByClass = new LinkedHashMap<>();
        Map<String, Integer> roomsNeeded = new LinkedHashMap<>();
        calculateRoomNeeds(classDemand, sectionsNeeded, roomTypeByClass, roomsNeeded, totalStudents);

        return new DemandResult(classDemand, classStudents, staffDemandByType,
                teachersNeeded, sectionsNeeded, roomTypeByClass, roomsNeeded, totalStudents);
    }

    /**
     * Convenience overload: determines class lists from a student map, then
     * analyzes.
     *
     * @param studentHashMap students to analyze
     * @param fundingModel   funding model for section sizing
     * @return aggregated demand
     */
    public static DemandResult analyze(HashMap<Integer, Student> studentHashMap,
            SchoolFundingModel fundingModel) {
        Map<Student, List<String>> classLists = StudentClassDeterminer.determineAllClasses(studentHashMap);
        return analyze(classLists, fundingModel);
    }

    /**
     * Populates the SectionManager demand tracker from a DemandResult,
     * bridging between the new demand-first pipeline and the existing
     * scheduling infrastructure in ESSA.
     */
    public static void populateSectionManagerDemand(DemandResult result) {
        SectionManager.clearAll();
        Map<String, SectionManager.StudentDemand> demandTracker = SectionManager.getDemandTracker();

        for (Map.Entry<String, Set<Student>> entry : result.getClassStudents().entrySet()) {
            String className = entry.getKey();
            Set<Student> students = entry.getValue();
            SectionManager.StudentDemand demand = new SectionManager.StudentDemand(
                    className, students.size(), students);
            demandTracker.put(className, demand);
        }
    }

    /** Exposes room-type mapping for scheduling diagnostics and expansion heuristics. */
    public static String getRoomTypeForClass(String className) {
        return mapClassToRoomType(className);
    }

    // --------------------------------------------------- private helpers

    private static void addSupportStaffEstimates(Map<StaffType, Integer> teachersNeeded, int totalStudents) {
        teachersNeeded.putIfAbsent(StaffType.PRINCIPAL, 1);
        teachersNeeded.putIfAbsent(StaffType.VICE_PRINCIPAL, Math.max(1, totalStudents / 400));
        teachersNeeded.putIfAbsent(StaffType.GUIDANCE, Math.max(2, totalStudents / 300));
        teachersNeeded.putIfAbsent(StaffType.NURSE, Math.max(1, totalStudents / 600));
        teachersNeeded.putIfAbsent(StaffType.LIBRARY, Math.max(1, totalStudents / 500));
        teachersNeeded.putIfAbsent(StaffType.OFFICE, Math.max(2, totalStudents / 400));
        teachersNeeded.putIfAbsent(StaffType.MAINTENANCE, Math.max(2, totalStudents / 300));
        teachersNeeded.putIfAbsent(StaffType.LUNCH, Math.max(3, totalStudents / 200));
        teachersNeeded.putIfAbsent(StaffType.SUB, Math.max(10, (int) (totalStudents * 0.01)));
    }

    /**
     * Maps classes to room types and calculates how many of each room type
     * are needed.
     */
    private static void calculateRoomNeeds(Map<String, Integer> classDemand,
            Map<String, Integer> sectionsNeeded,
            Map<String, String> roomTypeByClass,
            Map<String, Integer> roomsNeeded,
            int totalStudents) {
        // Map each class to its room type
        for (String className : classDemand.keySet()) {
            roomTypeByClass.put(className, mapClassToRoomType(className));
        }

        // Aggregate sections by room type
        Map<String, Integer> sectionsByRoomType = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : sectionsNeeded.entrySet()) {
            String roomType = roomTypeByClass.getOrDefault(entry.getKey(), "Classroom");
            sectionsByRoomType.merge(roomType, entry.getValue(), Integer::sum);
        }

        // Each room can host TOTAL_SCHOOL_PERIODS sections per day
        for (Map.Entry<String, Integer> entry : sectionsByRoomType.entrySet()) {
            int rooms = Math.max(1, (int) Math.ceil((double) entry.getValue() / TOTAL_SCHOOL_PERIODS));
            roomsNeeded.put(entry.getKey(), rooms);
        }

        // Non-teaching rooms scale with student population
        roomsNeeded.putIfAbsent("Office", Math.max(3, totalStudents / 200));
        roomsNeeded.putIfAbsent("Lunchroom", Math.max(1, totalStudents / 300));
        roomsNeeded.putIfAbsent("Library", Math.max(1, totalStudents / 500));
        roomsNeeded.putIfAbsent("Hallway", Math.max(9, totalStudents / 60));
        roomsNeeded.putIfAbsent("Bathroom", Math.max(4, totalStudents / 100));
        roomsNeeded.putIfAbsent("UtilityRoom", Math.max(2, totalStudents / 400));
    }

    /**
     * Determines the room type string for a given class name.
     */
    private static String mapClassToRoomType(String className) {
        StaffType type = CurriculumRequirementsCalculator.mapClassToStaffType(className);
        return switch (type) {
            case SCIENCE -> "ScienceLab";
            case VISUAL_ARTS -> "ArtStudio";
            case PERFORMING_ARTS -> {
                String lower = className.toLowerCase();
                yield (lower.contains("drama") || lower.contains("theater") || lower.contains("theatre"))
                        ? "DramaRoom"
                        : "MusicRoom";
            }
            case PHYSICAL_ED -> "Gym";
            case VOCATIONAL -> "VocationalRoom";
            case COMP_SCI -> "ComputerLab";
            default -> "Classroom";
        };
    }

    // ------------------------------------------------------ logging helpers

    /** Logs a summary of the demand analysis. */
    public static void logDemandSummary(DemandResult result) {
        GameLogger.logScheduling("=== DEMAND ANALYSIS SUMMARY ===");
        GameLogger.logScheduling("Total students: " + result.totalStudents);
        GameLogger.logScheduling("Unique classes requested: " + result.classDemand.size());

        GameLogger.logScheduling("--- Staff needs by type ---");
        result.teachersNeeded.entrySet().stream()
                .sorted(Comparator.<Map.Entry<StaffType, Integer>, Integer>comparing(Map.Entry::getValue).reversed())
                .forEach(e -> GameLogger.logScheduling("  " + e.getKey() + ": " + e.getValue() + " teachers"));

        GameLogger.logScheduling("--- Room needs ---");
        result.roomsNeeded.forEach((type, count) -> GameLogger.logScheduling("  " + type + ": " + count));

        // Language detail (for debugging)
        GameLogger.logScheduling("--- Language class demand ---");
        String[] languages = { "Spanish I", "Spanish II", "French I", "French II",
                "German I", "German II", "Latin I", "Latin II",
                "American Sign Language I", "American Sign Language II" };
        for (String lang : languages) {
            int d = result.classDemand.getOrDefault(lang, 0);
            if (d > 0) {
                GameLogger.logScheduling("  " + lang + ": " + d + " students");
            }
        }
    }
}
