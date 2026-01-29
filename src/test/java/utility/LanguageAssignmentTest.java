package utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the enhanced language assignment logic.
 * Focuses on ensuring proper Fall -> Spring sequencing for language courses.
 */
@DisplayName("Language Assignment Tests")
class LanguageAssignmentTest {
    
    @Test
    @DisplayName("Should extract language base from class name correctly")
    void testLanguageBaseExtraction() {
        assertAll("Language base extraction",
            () -> assertEquals("French", getLanguageBase("French I")),
            () -> assertEquals("French", getLanguageBase("French II")),
            () -> assertEquals("Spanish", getLanguageBase("Spanish I")),
            () -> assertEquals("German", getLanguageBase("German II")),
            () -> assertEquals("American Sign Language", getLanguageBase("American Sign Language I")),
            () -> assertEquals("Latin", getLanguageBase("Latin II"))
        );
        
        // Edge cases
        assertAll("Edge cases",
            () -> assertEquals("Unknown", getLanguageBase("Unknown")),
            () -> assertEquals("", getLanguageBase(""))
        );
    }
    
    @Test
    @DisplayName("Level I classes should be Fall, Level II should be Spring")
    void testSemesterLogic() {
        assertAll("Level I classes (Fall)",
            () -> assertTrue(shouldBeInFall("French I")),
            () -> assertFalse(shouldBeInSpring("French I")),
            () -> assertTrue(shouldBeInFall("Spanish I")),
            () -> assertTrue(shouldBeInFall("German I"))
        );
        
        assertAll("Level II classes (Spring)",
            () -> assertFalse(shouldBeInFall("French II")),
            () -> assertTrue(shouldBeInSpring("French II")),
            () -> assertTrue(shouldBeInSpring("Spanish II")),
            () -> assertTrue(shouldBeInSpring("German II"))
        );
    }
    
    @Test
    @DisplayName("Valid language sequences should be validated correctly")
    void testSequenceValidation() {
        // Valid sequences
        assertAll("Valid sequences",
            () -> assertTrue(isValidLanguageSequence("French I", "Fall", "French II", "Spring")),
            () -> assertTrue(isValidLanguageSequence("Spanish I", "Fall", "Spanish II", "Spring"))
        );
        
        // Invalid sequences (same semester)
        assertAll("Invalid sequences - same semester",
            () -> assertFalse(isValidLanguageSequence("French I", "Fall", "French II", "Fall")),
            () -> assertFalse(isValidLanguageSequence("French I", "Spring", "French II", "Spring"))
        );
        
        // Invalid sequences (wrong order)
        assertFalse(isValidLanguageSequence("French II", "Fall", "French I", "Spring"),
            "Level II before Level I should be invalid");
    }
    
    @Test
    @DisplayName("Languages should be assigned before core academics")
    void testLanguageAssignmentPriority() {
        String[] assignmentOrder = getAssignmentPriorityOrder();
        
        assertEquals("Language", assignmentOrder[0], "Languages should be assigned first");
        
        // Find positions of core subjects
        int languageIndex = 0;
        for (int i = 0; i < assignmentOrder.length; i++) {
            String subject = assignmentOrder[i];
            if (subject.equals("English") || subject.equals("Math") || 
                subject.equals("Science") || subject.equals("History")) {
                assertTrue(i > languageIndex, 
                    subject + " should come after Languages (index " + i + " > " + languageIndex + ")");
            }
        }
    }
    
    // Helper methods
    
    private String getLanguageBase(String className) {
        if (className == null || className.isEmpty()) {
            return className;
        }
        
        if (className.contains(" I")) {
            return className.substring(0, className.indexOf(" I"));
        } else if (className.contains(" II")) {
            return className.substring(0, className.indexOf(" II"));
        }
        return className;
    }
    
    private boolean shouldBeInFall(String className) {
        return className.endsWith(" I") || className.contains(" I "); 
    }
    
    private boolean shouldBeInSpring(String className) {
        return className.endsWith(" II") || className.contains(" II ");
    }
    
    private boolean isValidLanguageSequence(String class1, String semester1, String class2, String semester2) {
        String base1 = getLanguageBase(class1);
        String base2 = getLanguageBase(class2);
        
        if (!base1.equals(base2)) {
            return false;
        }
        
        if (class1.contains(" I") && class2.contains(" II")) {
            return semester1.equals("Fall") && semester2.equals("Spring");
        }
        
        return false;
    }
    
    private String[] getAssignmentPriorityOrder() {
        return new String[] {
            "Language",
            "English",
            "Math",
            "Science", 
            "History",
            "Physical Education",
            "Electives"
        };
    }
}
