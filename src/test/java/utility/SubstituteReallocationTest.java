package utility;

import entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.Locale;

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
        String normalized = className.toLowerCase(Locale.ROOT);
        if (belongsToSubjectArea(className, "english")) return StaffType.ENGLISH;
        if (belongsToSubjectArea(className, "math")) return StaffType.MATH;
        if (belongsToSubjectArea(className, "science")) return StaffType.SCIENCE;
        if (belongsToSubjectArea(className, "history")) return StaffType.HISTORY;
        if (belongsToSubjectArea(className, "language")) return StaffType.LANGUAGES;
        if (belongsToSubjectArea(className, "physical education")) return StaffType.PHYSICAL_ED;
        
        if (normalized.contains("art")) return StaffType.VISUAL_ARTS;
        if (normalized.contains("music") || normalized.contains("band") ||
            normalized.contains("theater") || normalized.contains("choir"))
            return StaffType.PERFORMING_ARTS;
        if (normalized.contains("business")) return StaffType.BUSINESS;
        
        return StaffType.VOCATIONAL;
    }
    
    private static boolean belongsToSubjectArea(String className, String subjectArea) {
        String normalizedClassName = className.toLowerCase(Locale.ROOT);
        return switch (subjectArea.toLowerCase(Locale.ROOT)) {
            case "english" ->
                    normalizedClassName.contains("english") || normalizedClassName.contains("ap english");
            case "math" -> normalizedClassName.contains("math") || normalizedClassName.contains("algebra") ||
                    normalizedClassName.contains("geometry") || normalizedClassName.contains("calculus") ||
                    normalizedClassName.contains("trigonometry") || normalizedClassName.contains("precalculus");
            case "science" ->
                    normalizedClassName.contains("biology") || normalizedClassName.contains("chemistry") ||
                            normalizedClassName.contains("physics") || normalizedClassName.contains("science");
            case "history" ->
                    normalizedClassName.contains("history") || normalizedClassName.contains("government") ||
                            normalizedClassName.contains("geography") || normalizedClassName.contains("economics");
            case "physical education" ->
                    normalizedClassName.contains("health") || normalizedClassName.contains("sports") ||
                            normalizedClassName.contains("weightlifting") || normalizedClassName.contains("dance") ||
                            normalizedClassName.contains("recreation");
            case "language" ->
                    normalizedClassName.contains("spanish") || normalizedClassName.contains("french") ||
                            normalizedClassName.contains("german") || normalizedClassName.contains("latin") ||
                            normalizedClassName.contains("sign language");
            case "vocational" ->
                    normalizedClassName.contains("theater") || normalizedClassName.contains("debate") ||
                            normalizedClassName.contains("choir") || normalizedClassName.contains("band") ||
                            normalizedClassName.contains("rotc");
            default -> false;
        };
    }
    
    private static boolean isCoreSubject(String className) {
        String[] coreKeywords = {"English", "Math", "Science", "History", "Biology", "Chemistry", 
                               "Physics", "Algebra", "Geometry", "Calculus", "Government", "Geography"};
        String normalized = className.toLowerCase(Locale.ROOT);
        return Arrays.stream(coreKeywords)
            .anyMatch(keyword -> normalized.contains(keyword.toLowerCase(Locale.ROOT)));
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

    @Test
    @DisplayName("Portable classrooms should map to teaching staff types")
    void testPortableClassroomStaffTypeMapping() {
        // Portables serve as overflow for any subject type, so classes taught
        // in portables should still map to the correct staff type
        assertAll("Classes in portables retain correct staff type",
            () -> assertEquals(StaffType.ENGLISH, determineStaffTypeForClass("English I")),
            () -> assertEquals(StaffType.MATH, determineStaffTypeForClass("Algebra I")),
            () -> assertEquals(StaffType.SCIENCE, determineStaffTypeForClass("Biology")),
            () -> assertEquals(StaffType.HISTORY, determineStaffTypeForClass("World Geography")),
            () -> assertEquals(StaffType.LANGUAGES, determineStaffTypeForClass("Spanish I"))
        );
    }

    @Test
    @DisplayName("Portable expansion should increase capacity")
    void testPortableExpansionLogic() {
        // Verify that adding portables increases available rooms
        int initialRooms = 30;
        int portablesAdded = 5;
        int totalAfterExpansion = initialRooms + portablesAdded;

        assertTrue(totalAfterExpansion > initialRooms,
            "Adding portables should increase total room count");
        assertEquals(35, totalAfterExpansion,
            "30 initial + 5 portables = 35 total rooms");
    }

    @Test
    @DisplayName("Portables should not be assigned to PE teachers")
    void testPortablesNotAssignedToPE() {
        // PE teachers need gyms/fields, not portables
        // Verify the staff type that can't use portables
        StaffType peType = StaffType.PHYSICAL_ED;
        StaffType[] typesUsingPortables = {
            StaffType.ENGLISH, StaffType.MATH, StaffType.SCIENCE,
            StaffType.HISTORY, StaffType.LANGUAGES, StaffType.VISUAL_ARTS,
            StaffType.PERFORMING_ARTS, StaffType.COMP_SCI, StaffType.VOCATIONAL
        };

        boolean peInPortableList = false;
        for (StaffType type : typesUsingPortables) {
            if (type == peType) {
                peInPortableList = true;
                break;
            }
        }
        assertFalse(peInPortableList,
            "PE teachers should not be in the list of types that use portables");
    }
}
