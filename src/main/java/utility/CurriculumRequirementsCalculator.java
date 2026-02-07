package utility;

import config.SchoolFundingModel;
import entity.StaffType;
import entity.Student;
import entity.StudentPool;

import java.util.*;

/**
 * Calculates curriculum requirements based on student pool demographics.
 * This class analyzes student demand to determine:
 * - How many sections of each class are needed
 * - How many teachers of each type are required
 * - Which classes have critical shortages
 *
 * This enables demand-driven school staffing rather than arbitrary limits.
 */
public class CurriculumRequirementsCalculator {
    
    // Grade levels in scheduling priority order
    private static final String[] GRADE_LEVELS = {"Freshman", "Sophomore", "Junior", "Senior"};
    
    // Core subject keywords for classification
    private static final Set<String> CORE_KEYWORDS = Set.of(
        "english", "math", "algebra", "geometry", "calculus", "trigonometry", "precalculus",
        "science", "biology", "chemistry", "physics",
        "history", "geography", "government", "economics"
    );

    // ==================== Data Classes ====================

    /**
     * Represents requirements for a single class/course.
     */
    public static class ClassRequirement {
        private final String className;
        private final StaffType staffType;
        private final int studentDemand;
        private final int sectionsNeeded;
        private final int teachersNeeded;
        private final boolean isCoreSubject;
        private final List<String> gradesTaking;

        public ClassRequirement(String className, StaffType staffType, int studentDemand,
                                int sectionsNeeded, int teachersNeeded, boolean isCoreSubject,
                                List<String> gradesTaking) {
            this.className = className;
            this.staffType = staffType;
            this.studentDemand = studentDemand;
            this.sectionsNeeded = sectionsNeeded;
            this.teachersNeeded = teachersNeeded;
            this.isCoreSubject = isCoreSubject;
            this.gradesTaking = new ArrayList<>(gradesTaking);
        }

        public String getClassName() { return className; }
        public StaffType getStaffType() { return staffType; }
        public int getStudentDemand() { return studentDemand; }
        public int getSectionsNeeded() { return sectionsNeeded; }
        public int getTeachersNeeded() { return teachersNeeded; }
        public boolean isCoreSubject() { return isCoreSubject; }
        public List<String> getGradesTaking() { return new ArrayList<>(gradesTaking); }

        @Override
        public String toString() {
            return String.format("%s: %d students, %d sections, %d teachers (%s)",
                className, studentDemand, sectionsNeeded, teachersNeeded, staffType);
        }
    }

    /**
     * Represents total staff requirements by type.
     */
    public static class StaffRequirements {
        private final Map<StaffType, Integer> teachersByType;
        private final Map<StaffType, List<ClassRequirement>> classesByType;
        private final int totalTeachingStaff;
        private final int totalSupportStaff;

        public StaffRequirements(Map<StaffType, Integer> teachersByType,
                                 Map<StaffType, List<ClassRequirement>> classesByType,
                                 int totalSupportStaff) {
            this.teachersByType = new HashMap<>(teachersByType);
            this.classesByType = new HashMap<>(classesByType);
            this.totalTeachingStaff = teachersByType.values().stream().mapToInt(i -> i).sum();
            this.totalSupportStaff = totalSupportStaff;
        }

        public Map<StaffType, Integer> getTeachersByType() { return new HashMap<>(teachersByType); }
        public Map<StaffType, List<ClassRequirement>> getClassesByType() { return new HashMap<>(classesByType); }
        public int getTotalTeachingStaff() { return totalTeachingStaff; }
        public int getTotalSupportStaff() { return totalSupportStaff; }
        public int getTotalStaff() { return totalTeachingStaff + totalSupportStaff; }

        public int getRequiredTeachers(StaffType type) {
            return teachersByType.getOrDefault(type, 0);
        }
    }

    /**
     * Complete curriculum analysis result.
     */
    public static class CurriculumAnalysis {
        private final Map<String, ClassRequirement> classRequirements;
        private final StaffRequirements staffRequirements;
        private final Map<String, Integer> studentsByGrade;
        private final int totalStudents;
        private final List<String> warnings;

        public CurriculumAnalysis(Map<String, ClassRequirement> classRequirements,
                                  StaffRequirements staffRequirements,
                                  Map<String, Integer> studentsByGrade,
                                  List<String> warnings) {
            this.classRequirements = new HashMap<>(classRequirements);
            this.staffRequirements = staffRequirements;
            this.studentsByGrade = new HashMap<>(studentsByGrade);
            this.totalStudents = studentsByGrade.values().stream().mapToInt(i -> i).sum();
            this.warnings = new ArrayList<>(warnings);
        }

        public Map<String, ClassRequirement> getClassRequirements() { return new HashMap<>(classRequirements); }
        public StaffRequirements getStaffRequirements() { return staffRequirements; }
        public Map<String, Integer> getStudentsByGrade() { return new HashMap<>(studentsByGrade); }
        public int getTotalStudents() { return totalStudents; }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }

        public ClassRequirement getRequirement(String className) {
            return classRequirements.get(className);
        }
    }

    // ==================== Main Analysis Methods ====================

    /**
     * Analyzes student pool and calculates all curriculum requirements.
     *
     * @param pool the student pool to analyze
     * @param fundingModel the school funding model for capacity calculations
     * @return complete curriculum analysis
     */
    public static CurriculumAnalysis analyzeRequirements(StudentPool pool, SchoolFundingModel fundingModel) {
        List<String> warnings = new ArrayList<>();
        
        // Count students by grade level
        Map<String, Integer> studentsByGrade = countStudentsByGrade(pool);
        
        // Analyze demand for each class
        Map<String, ClassDemand> classDemand = analyzeClassDemand(pool);
        
        // Calculate sections and teachers needed
        Map<String, ClassRequirement> classRequirements = new HashMap<>();
        Map<StaffType, Integer> teachersByType = new EnumMap<>(StaffType.class);
        Map<StaffType, Integer> totalSectionsByType = new EnumMap<>(StaffType.class);
        Map<StaffType, List<ClassRequirement>> classesByType = new EnumMap<>(StaffType.class);
        
        int classesPerTeacher = 4; // In a 4x4 block schedule, use 4 to ensure adequate staffing
        
        for (Map.Entry<String, ClassDemand> entry : classDemand.entrySet()) {
            String className = entry.getKey();
            ClassDemand demand = entry.getValue();
            
            StaffType staffType = mapClassToStaffType(className);
            boolean isCoreSubject = isCoreSubject(className);
            
            // Calculate sections needed
            int sectionsNeeded = fundingModel.calculateSectionsNeeded(demand.totalStudents);
            
            // Adjust for full-year vs semester courses
            if (isFullYearCourse(className)) {
                sectionsNeeded = sectionsNeeded * 2; // Need sections for both semesters
            }
            
            // Calculate teachers needed for this class (for the ClassRequirement object)
            int teachersNeeded = Math.max(1, (int) Math.ceil((double) sectionsNeeded / classesPerTeacher));
            
            ClassRequirement req = new ClassRequirement(
                className, staffType, demand.totalStudents, sectionsNeeded,
                teachersNeeded, isCoreSubject, demand.gradesTaking
            );
            
            classRequirements.put(className, req);
            
            // Aggregate total sections by staff type (sum, not max)
            totalSectionsByType.merge(staffType, sectionsNeeded, Integer::sum);
            classesByType.computeIfAbsent(staffType, k -> new ArrayList<>()).add(req);
        }
        
        // Calculate teachers needed by type based on total sections
        for (Map.Entry<StaffType, Integer> entry : totalSectionsByType.entrySet()) {
            int totalSections = entry.getValue();
            int teachersNeeded = Math.max(1, (int) Math.ceil((double) totalSections / classesPerTeacher));
            teachersByType.put(entry.getKey(), teachersNeeded);
        }
        
        // Calculate support staff needs based on total student population
        int totalStudents = studentsByGrade.values().stream().mapToInt(i -> i).sum();
        int supportStaff = calculateSupportStaffNeeds(totalStudents, fundingModel);
        
        // Add support staff to requirements
        addSupportStaffRequirements(teachersByType, totalStudents, fundingModel);
        
        StaffRequirements staffRequirements = new StaffRequirements(
            teachersByType, classesByType, supportStaff
        );
        
        // Generate warnings for potential issues
        generateWarnings(classRequirements, studentsByGrade, warnings);
        
        return new CurriculumAnalysis(classRequirements, staffRequirements, studentsByGrade, warnings);
    }

    /**
     * Simplified analysis that returns just staff requirements by type.
     *
     * @param pool the student pool
     * @param fundingModel the funding model
     * @return map of staff type to required count
     */
    public static Map<StaffType, Integer> calculateStaffNeeds(StudentPool pool, SchoolFundingModel fundingModel) {
        CurriculumAnalysis analysis = analyzeRequirements(pool, fundingModel);
        return analysis.getStaffRequirements().getTeachersByType();
    }

    // ==================== Helper Methods ====================

    private static Map<String, Integer> countStudentsByGrade(StudentPool pool) {
        Map<String, Integer> counts = new HashMap<>();
        for (String grade : GRADE_LEVELS) {
            counts.put(grade, pool.getByGradeLevel(grade).size());
        }
        return counts;
    }

    private static class ClassDemand {
        int totalStudents;
        List<String> gradesTaking = new ArrayList<>();
        
        void addDemand(int count, String grade) {
            totalStudents += count;
            if (!gradesTaking.contains(grade)) {
                gradesTaking.add(grade);
            }
        }
    }

    private static Map<String, ClassDemand> analyzeClassDemand(StudentPool pool) {
        Map<String, ClassDemand> demand = new HashMap<>();
        
        // Analyze each student to determine what classes they need
        for (Student student : pool.getAllStudents().values()) {
            if (!student.isInHighSchool()) {
                continue; // Skip non-high-school students
            }
            
            String grade = student.studentStatistics.getGradeLevel();
            List<String> neededClasses = determineStudentClasses(student);
            
            for (String className : neededClasses) {
                demand.computeIfAbsent(className, k -> new ClassDemand()).addDemand(1, grade);
            }
        }
        
        return demand;
    }

    /**
     * Determines what classes a student needs based on their grade and traits.
     */
    private static List<String> determineStudentClasses(Student student) {
        List<String> classes = new ArrayList<>();
        
        String year = student.studentStatistics.getGradeLevel();
        int intelligence = student.studentStatistics.getIntelligence();
        int determination = student.studentStatistics.getDetermination();
        String income = student.studentStatistics.getIncomeLevel();
        
        // Determine academic path (AP, Honors, On-Level)
        String path = determineAcademicPath(intelligence, income, determination);
        
        // Add required classes for each subject area
        classes.addAll(getEnglishClasses(year, path));
        classes.addAll(getMathClasses(year, path));
        classes.addAll(getScienceClasses(year, path));
        classes.addAll(getHistoryClasses(year, path));
        classes.addAll(getLanguageClasses(year));
        classes.addAll(getPhysEdClasses(year));
        classes.addAll(getElectiveClasses(year, student));
        
        return classes;
    }

    private static String determineAcademicPath(int intelligence, String income, int determination) {
        // High intelligence OR high determination + middle/high income -> AP eligible
        if (intelligence >= 120 || (determination >= 80 && !income.equals("Low"))) {
            return "AP";
        } else if (intelligence >= 100 || determination >= 60) {
            return "Honors";
        }
        return "On-Level";
    }

    private static List<String> getEnglishClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add("English I");
            case "Sophomore" -> classes.add("English II");
            case "Junior" -> classes.add(path.equals("AP") ? "AP English Language & Composition" : "English III");
            case "Senior" -> classes.add(path.equals("AP") ? "AP English Literature & Composition" : "English IV");
        }
        return classes;
    }

    private static List<String> getMathClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> {
                if (path.equals("AP") || path.equals("Honors")) {
                    classes.add("Algebra I");
                    classes.add("Geometry");
                } else {
                    classes.add("Fundamentals of Math");
                    classes.add("Algebra I");
                }
            }
            case "Sophomore" -> {
                if (path.equals("AP") || path.equals("Honors")) {
                    classes.add("Geometry");
                    classes.add("Algebra II");
                } else {
                    classes.add("Algebra I");
                    classes.add("Geometry");
                }
            }
            case "Junior" -> {
                if (path.equals("AP")) {
                    classes.add("Precalculus");
                    classes.add("AP Statistics");
                } else if (path.equals("Honors")) {
                    classes.add("Algebra II");
                    classes.add("Precalculus");
                } else {
                    classes.add("Algebra II");
                }
            }
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP Calculus AB");
                } else if (path.equals("Honors")) {
                    classes.add("Precalculus");
                }
                // On-level seniors may not take math
            }
        }
        return classes;
    }

    private static List<String> getScienceClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add("Biology");
            case "Sophomore" -> {
                if (path.equals("AP") || path.equals("Honors")) {
                    classes.add("Chemistry");
                } else {
                    classes.add("Earth and Space Science");
                }
            }
            case "Junior" -> {
                if (path.equals("AP")) {
                    classes.add("AP Chemistry");
                } else {
                    classes.add("Anatomy and Physiology");
                }
            }
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP Physics B");
                } else if (path.equals("Honors")) {
                    classes.add("Physics");
                } else {
                    classes.add("Environmental Science");
                }
            }
        }
        return classes;
    }

    private static List<String> getHistoryClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add(path.equals("AP") ? "AP Human Geography" : "World Geography");
            case "Sophomore" -> classes.add(path.equals("AP") ? "AP World History" : "World History");
            case "Junior" -> classes.add(path.equals("AP") ? "AP US History" : "US History");
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP US Government");
                    classes.add("AP Economics Macro");
                } else {
                    classes.add("US Government");
                }
            }
        }
        return classes;
    }

    private static List<String> getLanguageClasses(String year) {
        List<String> classes = new ArrayList<>();
        // Freshmen take language - simplified distribution
        if (year.equals("Freshman")) {
            // Use a distribution to spread across languages
            int choice = Randomizer.setRandom(0, 4);
            switch (choice) {
                case 0 -> { classes.add("Spanish I"); classes.add("Spanish II"); }
                case 1 -> { classes.add("French I"); classes.add("French II"); }
                case 2 -> { classes.add("German I"); classes.add("German II"); }
                case 3 -> { classes.add("American Sign Language I"); classes.add("American Sign Language II"); }
                case 4 -> { classes.add("Latin I"); classes.add("Latin II"); }
            }
        }
        return classes;
    }

    private static List<String> getPhysEdClasses(String year) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add("Health");
            case "Sophomore" -> {
                int choice = Randomizer.setRandom(0, 3);
                switch (choice) {
                    case 0 -> classes.add("Team Sports");
                    case 1 -> classes.add("Weightlifting");
                    case 2 -> classes.add("Specialized Sports");
                    case 3 -> classes.add("Lifetime Recreation");
                }
            }
        }
        return classes;
    }

    private static List<String> getElectiveClasses(String year, Student student) {
        List<String> classes = new ArrayList<>();
        int numElectives = switch (year) {
            case "Freshman", "Sophomore" -> 1;
            case "Junior", "Senior" -> 2;
            default -> 0;
        };
        
        // Simplified elective selection
        String[] electives = {
            "Art I", "Band", "Choir", "Drama",
            "Computer Science I", "Journalism",
            "Woodworking", "Auto Shop", "Culinary Arts"
        };
        
        for (int i = 0; i < numElectives; i++) {
            int choice = Randomizer.setRandom(0, electives.length - 1);
            classes.add(electives[choice]);
        }
        
        return classes;
    }

    /**
     * Maps a class name to the appropriate staff type.
     */
    public static StaffType mapClassToStaffType(String className) {
        String lowerName = className.toLowerCase();
        
        // English classes
        if (lowerName.contains("english") || lowerName.contains("literature") ||
            lowerName.contains("composition") || lowerName.contains("journalism")) {
            return StaffType.ENGLISH;
        }
        
        // Math classes
        if (lowerName.contains("math") || lowerName.contains("algebra") ||
            lowerName.contains("geometry") || lowerName.contains("calculus") ||
            lowerName.contains("trigonometry") || lowerName.contains("precalculus") ||
            lowerName.contains("statistics") || lowerName.contains("financial")) {
            return StaffType.MATH;
        }
        
        // Science classes
        if (lowerName.contains("biology") || lowerName.contains("chemistry") ||
            lowerName.contains("physics") || lowerName.contains("science") ||
            lowerName.contains("anatomy") || lowerName.contains("environmental") ||
            lowerName.contains("genetics")) {
            return StaffType.SCIENCE;
        }
        
        // History/Social Studies classes
        if (lowerName.contains("history") || lowerName.contains("geography") ||
            lowerName.contains("government") || lowerName.contains("economics") ||
            lowerName.contains("civics")) {
            return StaffType.HISTORY;
        }
        
        // Language classes
        if (lowerName.contains("spanish") || lowerName.contains("french") ||
            lowerName.contains("german") || lowerName.contains("latin") ||
            lowerName.contains("sign language") || lowerName.contains("asl")) {
            return StaffType.LANGUAGES;
        }
        
        // Physical Education classes
        if (lowerName.contains("health") || lowerName.contains("sports") ||
            lowerName.contains("weightlifting") || lowerName.contains("recreation") ||
            lowerName.contains("physical education") || lowerName.contains("pe")) {
            return StaffType.PHYSICAL_ED;
        }
        
        // Visual Arts
        if (lowerName.contains("art") || lowerName.contains("drawing") ||
            lowerName.contains("painting") || lowerName.contains("sculpture") ||
            lowerName.contains("ceramics") || lowerName.contains("photography")) {
            return StaffType.VISUAL_ARTS;
        }
        
        // Performing Arts
        if (lowerName.contains("band") || lowerName.contains("choir") ||
            lowerName.contains("orchestra") || lowerName.contains("music") ||
            lowerName.contains("drama") || lowerName.contains("theater") ||
            lowerName.contains("theatre")) {
            return StaffType.PERFORMING_ARTS;
        }
        
        // Computer Science
        if (lowerName.contains("computer") || lowerName.contains("programming") ||
            lowerName.contains("coding") || lowerName.contains("technology")) {
            return StaffType.COMP_SCI;
        }
        
        // Vocational
        if (lowerName.contains("woodworking") || lowerName.contains("auto") ||
            lowerName.contains("shop") || lowerName.contains("culinary") ||
            lowerName.contains("welding") || lowerName.contains("construction") ||
            lowerName.contains("hvac") || lowerName.contains("electrical")) {
            return StaffType.VOCATIONAL;
        }
        
        // Business
        if (lowerName.contains("business") || lowerName.contains("accounting") ||
            lowerName.contains("marketing") || lowerName.contains("entrepreneurship")) {
            return StaffType.BUSINESS;
        }
        
        // Consumer Science
        if (lowerName.contains("home economics") || lowerName.contains("consumer") ||
            lowerName.contains("family") || lowerName.contains("child development")) {
            return StaffType.CONSUMER_SCI;
        }
        
        // Default to elective/substitute
        return StaffType.SUB;
    }

    /**
     * Determines if a class is a core subject (required for graduation).
     */
    public static boolean isCoreSubject(String className) {
        String lowerName = className.toLowerCase();
        return CORE_KEYWORDS.stream().anyMatch(lowerName::contains);
    }

    /**
     * Determines if a class runs for the full year (both semesters).
     */
    private static boolean isFullYearCourse(String className) {
        // Language sequences are typically full year
        String lowerName = className.toLowerCase();
        return lowerName.contains("spanish") || lowerName.contains("french") ||
               lowerName.contains("german") || lowerName.contains("latin") ||
               lowerName.contains("sign language");
    }

    private static int calculateSupportStaffNeeds(int totalStudents, SchoolFundingModel fundingModel) {
        // Base support staff needs
        int principal = 1;
        int vicePrincipals = Math.max(1, totalStudents / 400);
        int guidanceCounselors = Math.max(2, totalStudents / 300);
        int nurses = Math.max(1, totalStudents / 600);
        int librarians = Math.max(1, totalStudents / 500);
        int officeStaff = Math.max(2, totalStudents / 400);
        int maintenance = Math.max(2, totalStudents / 300);
        int lunchStaff = Math.max(3, totalStudents / 200);
        int substitutes = Math.max(10, (int)(totalStudents * 0.01)); // 10 per 1000 students, minimum 10
        
        return principal + vicePrincipals + guidanceCounselors + nurses + 
               librarians + officeStaff + maintenance + lunchStaff + substitutes;
    }

    private static void addSupportStaffRequirements(Map<StaffType, Integer> requirements, 
                                                    int totalStudents, 
                                                    SchoolFundingModel fundingModel) {
        requirements.put(StaffType.PRINCIPAL, 1);
        requirements.put(StaffType.VICE_PRINCIPAL, Math.max(1, totalStudents / 400));
        requirements.put(StaffType.GUIDANCE, Math.max(2, totalStudents / 300));
        requirements.put(StaffType.NURSE, Math.max(1, totalStudents / 600));
        requirements.put(StaffType.LIBRARY, Math.max(1, totalStudents / 500));
        requirements.put(StaffType.OFFICE, Math.max(2, totalStudents / 400));
        requirements.put(StaffType.MAINTENANCE, Math.max(2, totalStudents / 300));
        requirements.put(StaffType.LUNCH, Math.max(3, totalStudents / 200));
        requirements.put(StaffType.SUB, Math.max(10, (int)(totalStudents * 0.01))); // 10 per 1000 students, minimum 10
    }

    private static void generateWarnings(Map<String, ClassRequirement> requirements,
                                         Map<String, Integer> studentsByGrade,
                                         List<String> warnings) {
        // Check for classes with very high demand
        for (ClassRequirement req : requirements.values()) {
            if (req.getStudentDemand() > 500 && req.isCoreSubject()) {
                warnings.add("High demand for " + req.getClassName() + 
                           ": " + req.getStudentDemand() + " students need " + req.getSectionsNeeded() + " sections");
            }
        }
        
        // Check for grade imbalances
        int totalStudents = studentsByGrade.values().stream().mapToInt(i -> i).sum();
        for (Map.Entry<String, Integer> entry : studentsByGrade.entrySet()) {
            double percent = (double) entry.getValue() / totalStudents;
            if (percent > 0.35) {
                warnings.add("Grade imbalance: " + entry.getKey() + " has " + 
                           String.format("%.1f%%", percent * 100) + " of students");
            }
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Gets a summary of curriculum requirements for display.
     */
    public static String getSummary(CurriculumAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Curriculum Requirements Analysis ===\n\n");
        
        sb.append("Students by Grade:\n");
        for (Map.Entry<String, Integer> entry : analysis.getStudentsByGrade().entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("  Total: ").append(analysis.getTotalStudents()).append("\n\n");
        
        sb.append("Staff Requirements by Type:\n");
        for (Map.Entry<StaffType, Integer> entry : analysis.getStaffRequirements().getTeachersByType().entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("  Total Teaching Staff: ").append(analysis.getStaffRequirements().getTotalTeachingStaff()).append("\n");
        sb.append("  Total Support Staff: ").append(analysis.getStaffRequirements().getTotalSupportStaff()).append("\n");
        sb.append("  Grand Total: ").append(analysis.getStaffRequirements().getTotalStaff()).append("\n\n");
        
        if (!analysis.getWarnings().isEmpty()) {
            sb.append("Warnings:\n");
            for (String warning : analysis.getWarnings()) {
                sb.append("  - ").append(warning).append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * Prints curriculum analysis to console.
     */
    public static void printAnalysis(CurriculumAnalysis analysis) {
        GameLogger.logScheduling(getSummary(analysis));
        
        GameLogger.logScheduling("Core Class Requirements:");
        analysis.getClassRequirements().values().stream()
            .filter(ClassRequirement::isCoreSubject)
            .sorted((a, b) -> Integer.compare(b.getStudentDemand(), a.getStudentDemand()))
            .forEach(req -> GameLogger.logScheduling("  " + req));
        
        GameLogger.logScheduling("\nElective Class Requirements:");
        analysis.getClassRequirements().values().stream()
            .filter(req -> !req.isCoreSubject())
            .sorted((a, b) -> Integer.compare(b.getStudentDemand(), a.getStudentDemand()))
            .limit(20)
            .forEach(req -> GameLogger.logScheduling("  " + req));
    }
}
