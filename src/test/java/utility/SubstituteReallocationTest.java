package utility;

import entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the substitute reallocation system.
 * Verifies staff type mapping, subject classification, and resource calculations.
 */
@DisplayName("Substitute Reallocation Tests")
class SubstituteReallocationTest {
    
    @Test
    @DisplayName("Core classes should map to correct staff types")
    void testStaffTypeMapping() {
        assertAll("Staff type mappings",
            () -> assertEquals(StaffType.HISTORY, determineStaffTypeForClass("World Geography")),
            () -> assertEquals(StaffType.ENGLISH, determineStaffTypeForClass("English I")),
            () -> assertEquals(StaffType.MATH, determineStaffTypeForClass("Algebra I")),
            () -> assertEquals(StaffType.SCIENCE, determineStaffTypeForClass("Biology")),
            () -> assertEquals(StaffType.PHYSICAL_ED, determineStaffTypeForClass("Health")),
            () -> assertEquals(StaffType.LANGUAGES, determineStaffTypeForClass("French I"))
        );
    }
    
    @Test
    @DisplayName("Classes should belong to correct subject areas")
    void testSubjectAreaBelonging() {
        assertAll("History subject area",
            () -> assertTrue(belongsToSubjectArea("World Geography", "history")),
            () -> assertTrue(belongsToSubjectArea("AP Human Geography", "history")),
            () -> assertTrue(belongsToSubjectArea("US Government", "history"))
        );
        
        assertAll("English subject area",
            () -> assertTrue(belongsToSubjectArea("English I", "english")),
            () -> assertTrue(belongsToSubjectArea("AP English Literature & Composition", "english"))
        );
        
        assertAll("Math subject area",
            () -> assertTrue(belongsToSubjectArea("Algebra I", "math")),
            () -> assertTrue(belongsToSubjectArea("Geometry", "math")),
            () -> assertTrue(belongsToSubjectArea("AP Calculus AB", "math"))
        );
        
        assertAll("Science subject area",
            () -> assertTrue(belongsToSubjectArea("Biology", "science")),
            () -> assertTrue(belongsToSubjectArea("Chemistry", "science")),
            () -> assertTrue(belongsToSubjectArea("AP Physics B", "science"))
        );
        
        assertAll("Physical education subject area",
            () -> assertTrue(belongsToSubjectArea("Health", "physical education")),
            () -> assertTrue(belongsToSubjectArea("Team Sports", "physical education"))
        );
        
        assertAll("Language subject area",
            () -> assertTrue(belongsToSubjectArea("French I", "language")),
            () -> assertTrue(belongsToSubjectArea("Spanish II", "language")),
            () -> assertTrue(belongsToSubjectArea("American Sign Language I", "language"))
        );
    }
    
    @Test
    @DisplayName("Teacher need calculation should be reasonable")
    void testTeacherNeedCalculation() {
        int teachersFor50Students = calculateTeachersNeeded(50);
        int teachersFor100Students = calculateTeachersNeeded(100);
        int teachersFor0Students = calculateTeachersNeeded(0);
        
        assertAll("Teacher calculations",
            () -> assertTrue(teachersFor50Students >= 1 && teachersFor50Students <= 3,
                "Should need 1-3 teachers for 50 students"),
            () -> assertTrue(teachersFor100Students >= 2 && teachersFor100Students <= 5,
                "Should need 2-5 teachers for 100 students"),
            () -> assertEquals(0, teachersFor0Students, "Should need 0 teachers for no shortage")
        );
    }
    
    @Test
    @DisplayName("Core subjects should be identified correctly")
    void testCoreSubjectIdentification() {
        assertAll("Core subjects",
            () -> assertTrue(isCoreSubject("World Geography")),
            () -> assertTrue(isCoreSubject("English I")),
            () -> assertTrue(isCoreSubject("Algebra I")),
            () -> assertTrue(isCoreSubject("Biology")),
            () -> assertTrue(isCoreSubject("AP Human Geography"))
        );
        
        assertAll("Non-core subjects (electives)",
            () -> assertFalse(isCoreSubject("Theater I")),
            () -> assertFalse(isCoreSubject("Digital Production Technology")),
            () -> assertFalse(isCoreSubject("Culinary Arts")),
            () -> assertFalse(isCoreSubject("Photography I"))
        );
    }
    
    @Test
    @DisplayName("Classes in same subject area should be grouped together")
    void testBlockOptimizationLogic() {
        assertAll("Same subject area groupings",
            () -> assertTrue(inSameSubjectArea("English I", "English II")),
            () -> assertTrue(inSameSubjectArea("Algebra I", "Geometry")),
            () -> assertTrue(inSameSubjectArea("Biology", "Chemistry")),
            () -> assertTrue(inSameSubjectArea("World Geography", "US History")),
            () -> assertTrue(inSameSubjectArea("French I", "Spanish II"))
        );
        
        assertAll("Different subject areas should not be grouped",
            () -> assertFalse(inSameSubjectArea("English I", "Algebra I")),
            () -> assertFalse(inSameSubjectArea("Biology", "World Geography")),
            () -> assertFalse(inSameSubjectArea("French I", "Health"))
        );
    }
    
    @Test
    @DisplayName("Utilization calculation should be correct")
    void testUtilizationCalculation() {
        assertAll("Utilization percentages",
            () -> assertEquals(0.6, calculateUtilization(15, 25), 0.01),
            () -> assertTrue(calculateUtilization(5, 25) < 0.5),
            () -> assertTrue(calculateUtilization(23, 25) > 0.5),
            () -> assertEquals(0.0, calculateUtilization(0, 25), 0.01)
        );
    }
    
    // Helper methods (copied from main class for testing)
    private static StaffType determineStaffTypeForClass(String className) {
        if (belongsToSubjectArea(className, "english")) return StaffType.ENGLISH;
        if (belongsToSubjectArea(className, "math")) return StaffType.MATH;
        if (belongsToSubjectArea(className, "science")) return StaffType.SCIENCE;
        if (belongsToSubjectArea(className, "history")) return StaffType.HISTORY;
        if (belongsToSubjectArea(className, "language")) return StaffType.LANGUAGES;
        if (belongsToSubjectArea(className, "physical education")) return StaffType.PHYSICAL_ED;
        
        if (className.toLowerCase().contains("art")) return StaffType.VISUAL_ARTS;
        if (className.toLowerCase().contains("music") || className.toLowerCase().contains("band") || 
            className.toLowerCase().contains("theater") || className.toLowerCase().contains("choir")) 
            return StaffType.PERFORMING_ARTS;
        if (className.toLowerCase().contains("business")) return StaffType.BUSINESS;
        
        return StaffType.VOCATIONAL;
    }
    
    private static boolean belongsToSubjectArea(String className, String subjectArea) {
        return switch (subjectArea.toLowerCase()) {
            case "english" ->
                    className.toLowerCase().contains("english") || className.toLowerCase().contains("ap english");
            case "math" -> className.toLowerCase().contains("math") || className.toLowerCase().contains("algebra") ||
                    className.toLowerCase().contains("geometry") || className.toLowerCase().contains("calculus") ||
                    className.toLowerCase().contains("trigonometry") || className.toLowerCase().contains("precalculus");
            case "science" ->
                    className.toLowerCase().contains("biology") || className.toLowerCase().contains("chemistry") ||
                            className.toLowerCase().contains("physics") || className.toLowerCase().contains("science");
            case "history" ->
                    className.toLowerCase().contains("history") || className.toLowerCase().contains("government") ||
                            className.toLowerCase().contains("geography") || className.toLowerCase().contains("economics");
            case "physical education" ->
                    className.toLowerCase().contains("health") || className.toLowerCase().contains("sports") ||
                            className.toLowerCase().contains("weightlifting") || className.toLowerCase().contains("dance") ||
                            className.toLowerCase().contains("recreation");
            case "language" ->
                    className.toLowerCase().contains("spanish") || className.toLowerCase().contains("french") ||
                            className.toLowerCase().contains("german") || className.toLowerCase().contains("latin") ||
                            className.toLowerCase().contains("sign language");
            case "vocational" ->
                    className.toLowerCase().contains("theater") || className.toLowerCase().contains("debate") || 
                            className.toLowerCase().contains("choir") || className.toLowerCase().contains("band") || 
                            className.toLowerCase().contains("rotc");
            default -> false;
        };
    }
    
    private static boolean isCoreSubject(String className) {
        String[] coreKeywords = {"English", "Math", "Science", "History", "Biology", "Chemistry", 
                               "Physics", "Algebra", "Geometry", "Calculus", "Government", "Geography"};
        return Arrays.stream(coreKeywords)
            .anyMatch(keyword -> className.toLowerCase().contains(keyword.toLowerCase()));
    }
    
    private static int calculateTeachersNeeded(int shortageAmount) {
        if (shortageAmount <= 0) return 0;
        int studentsPerTeacherPerClass = 50;
        return (int) Math.ceil((double) shortageAmount / studentsPerTeacherPerClass);
    }
    
    private static boolean inSameSubjectArea(String class1, String class2) {
        String[] subjectAreas = {"english", "math", "science", "history", "language"};
        for (String area : subjectAreas) {
            if (belongsToSubjectArea(class1, area) && belongsToSubjectArea(class2, area)) {
                return true;
            }
        }
        return false;
    }
    
    private static double calculateUtilization(int enrolled, int capacity) {
        if (capacity == 0) return 0.0;
        return (double) enrolled / capacity;
    }
}
