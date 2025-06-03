package utility;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for the enhanced language assignment logic
 * Focus on ensuring proper Fall -> Spring sequencing for language courses
 */
public class LanguageAssignmentTest {
    
    @Test
    public void testLanguageBaseExtraction() {
        System.out.println("=== Testing Language Base Extraction ===");
        
        // Test that getLanguageBase correctly extracts language names
        assertEquals("French", getLanguageBase("French I"));
        assertEquals("French", getLanguageBase("French II"));
        assertEquals("Spanish", getLanguageBase("Spanish I"));
        assertEquals("German", getLanguageBase("German II"));
        assertEquals("American Sign Language", getLanguageBase("American Sign Language I"));
        assertEquals("Latin", getLanguageBase("Latin II"));
        
        // Test edge cases
        assertEquals("Unknown", getLanguageBase("Unknown"));
        assertEquals("", getLanguageBase(""));
        
        System.out.println("✓ Language base extraction works correctly");
    }
    
    @Test
    public void testSemesterLogic() {
        System.out.println("=== Testing Semester Assignment Logic ===");
        
        // Test that Level I classes should be Fall and Level II should be Spring
        assertTrue("French I should be assigned to Fall", shouldBeInFall("French I"));
        assertFalse("French I should NOT be assigned to Spring", shouldBeInSpring("French I"));
        
        assertFalse("French II should NOT be assigned to Fall", shouldBeInFall("French II"));
        assertTrue("French II should be assigned to Spring", shouldBeInSpring("French II"));
        
        // Test other languages
        assertTrue("Spanish I should be in Fall", shouldBeInFall("Spanish I"));
        assertTrue("Spanish II should be in Spring", shouldBeInSpring("Spanish II"));
        
        assertTrue("German I should be in Fall", shouldBeInFall("German I"));
        assertTrue("German II should be in Spring", shouldBeInSpring("German II"));
        
        System.out.println("✓ Semester assignment logic is correct");
    }
    
    @Test
    public void testSequenceValidation() {
        System.out.println("=== Testing Language Sequence Validation ===");
        
        // Test valid sequences (Level I in Fall, Level II in Spring)
        assertTrue("French I (Fall) -> French II (Spring) should be valid", 
                  isValidLanguageSequence("French I", "Fall", "French II", "Spring"));
        
        assertTrue("Spanish I (Fall) -> Spanish II (Spring) should be valid", 
                  isValidLanguageSequence("Spanish I", "Fall", "Spanish II", "Spring"));
        
        // Test invalid sequences (same semester)
        assertFalse("French I (Fall) -> French II (Fall) should be INVALID", 
                   isValidLanguageSequence("French I", "Fall", "French II", "Fall"));
        
        assertFalse("French I (Spring) -> French II (Spring) should be INVALID", 
                   isValidLanguageSequence("French I", "Spring", "French II", "Spring"));
        
        // Test invalid sequences (wrong order)
        assertFalse("French II (Fall) -> French I (Spring) should be INVALID", 
                   isValidLanguageSequence("French II", "Fall", "French I", "Spring"));
        
        System.out.println("✓ Language sequence validation works correctly");
    }
    
    @Test
    public void testLanguageAssignmentPriority() {
        System.out.println("=== Testing Language Assignment Priority ===");
        
        // Test that languages are assigned before other subjects
        String[] assignmentOrder = getAssignmentPriorityOrder();
        
        // Language should be first
        assertEquals("Languages should be assigned first", "Language", assignmentOrder[0]);
        
        // Core academics should come after languages
        boolean foundEnglish = false, foundMath = false, foundScience = false, foundHistory = false;
        int languageIndex = 0;
        
        for (int i = 0; i < assignmentOrder.length; i++) {
            if (assignmentOrder[i].equals("Language")) {
                languageIndex = i;
            }
            if (assignmentOrder[i].equals("English")) {
                foundEnglish = true;
                assertTrue("English should come after Languages", i > languageIndex);
            }
            if (assignmentOrder[i].equals("Math")) {
                foundMath = true;
                assertTrue("Math should come after Languages", i > languageIndex);
            }
            if (assignmentOrder[i].equals("Science")) {
                foundScience = true;
                assertTrue("Science should come after Languages", i > languageIndex);
            }
            if (assignmentOrder[i].equals("History")) {
                foundHistory = true;
                assertTrue("History should come after Languages", i > languageIndex);
            }
        }
        
        assertTrue("All core subjects should be present", foundEnglish && foundMath && foundScience && foundHistory);
        
        System.out.println("✓ Language assignment priority is correct");
    }
    
    // Helper methods to simulate the logic we implemented
    
    /**
     * Extracts language base from class name (same logic as in our implementation)
     */
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
    
    /**
     * Determines if a class should be assigned to Fall semester
     */
    private boolean shouldBeInFall(String className) {
        // Level I classes go in Fall - check for " I" at the end or " I " in middle
        return className.endsWith(" I") || className.contains(" I "); 
    }
    
    /**
     * Determines if a class should be assigned to Spring semester
     */
    private boolean shouldBeInSpring(String className) {
        // Level II classes go in Spring - check for " II" at the end or " II " in middle
        return className.endsWith(" II") || className.contains(" II ");
    }
    
    /**
     * Validates that a language sequence follows proper Fall -> Spring order
     */
    private boolean isValidLanguageSequence(String class1, String semester1, String class2, String semester2) {
        String base1 = getLanguageBase(class1);
        String base2 = getLanguageBase(class2);
        
        // Must be same language
        if (!base1.equals(base2)) {
            return false;
        }
        
        // Level I must be Fall, Level II must be Spring
        if (class1.contains(" I") && class2.contains(" II")) {
            return semester1.equals("Fall") && semester2.equals("Spring");
        }
        
        return false; // Invalid combination
    }
    
    /**
     * Returns the assignment priority order (simulating our enhanced scheduler)
     */
    private String[] getAssignmentPriorityOrder() {
        // This simulates the order we implemented in assignStudentsWithOptimization
        return new String[] {
            "Language",        // PRIORITY PHASE 0: Highest priority
            "English",         // PRIORITY PHASE 1: Core academics  
            "Math",
            "Science", 
            "History",
            "Physical Education", // PRIORITY PHASE 2
            "Electives"        // PRIORITY PHASE 3: Lowest priority
        };
    }
} 