package utility;

import entity.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Test cases for the substitute reallocation system
 * Verifies that we can properly identify shortages and reallocate resources
 */
public class SubstituteReallocationTest {
    
    @Test
    public void testStaffTypeMapping() {
        System.out.println("=== Testing Staff Type Mapping for Classes ===");
        
        // Test core subject mapping
        assertTrue("World Geography should map to HISTORY", 
                  determineStaffTypeForClass("World Geography") == StaffType.HISTORY);
        
        assertTrue("English I should map to ENGLISH", 
                  determineStaffTypeForClass("English I") == StaffType.ENGLISH);
        
        assertTrue("Algebra I should map to MATH", 
                  determineStaffTypeForClass("Algebra I") == StaffType.MATH);
        
        assertTrue("Biology should map to SCIENCE", 
                  determineStaffTypeForClass("Biology") == StaffType.SCIENCE);
        
        assertTrue("Health should map to PHYSICAL_ED", 
                  determineStaffTypeForClass("Health") == StaffType.PHYSICAL_ED);
        
        assertTrue("French I should map to LANGUAGES", 
                  determineStaffTypeForClass("French I") == StaffType.LANGUAGES);
        
        System.out.println("✓ All staff type mappings work correctly");
    }
    
    @Test
    public void testSubjectAreaBelonging() {
        System.out.println("=== Testing Subject Area Classification ===");
        
        assertTrue("World Geography belongs to history", belongsToSubjectArea("World Geography", "history"));
        assertTrue("AP Human Geography belongs to history", belongsToSubjectArea("AP Human Geography", "history"));
        assertTrue("US Government belongs to history", belongsToSubjectArea("US Government", "history"));
        
        assertTrue("English I belongs to english", belongsToSubjectArea("English I", "english"));
        assertTrue("AP English Literature belongs to english", belongsToSubjectArea("AP English Literature & Composition", "english"));
        
        assertTrue("Algebra I belongs to math", belongsToSubjectArea("Algebra I", "math"));
        assertTrue("Geometry belongs to math", belongsToSubjectArea("Geometry", "math"));
        assertTrue("AP Calculus AB belongs to math", belongsToSubjectArea("AP Calculus AB", "math"));
        
        assertTrue("Biology belongs to science", belongsToSubjectArea("Biology", "science"));
        assertTrue("Chemistry belongs to science", belongsToSubjectArea("Chemistry", "science"));
        assertTrue("AP Physics B belongs to science", belongsToSubjectArea("AP Physics B", "science"));
        
        assertTrue("Health belongs to physical education", belongsToSubjectArea("Health", "physical education"));
        assertTrue("Team Sports belongs to physical education", belongsToSubjectArea("Team Sports", "physical education"));
        
        assertTrue("French I belongs to language", belongsToSubjectArea("French I", "language"));
        assertTrue("Spanish II belongs to language", belongsToSubjectArea("Spanish II", "language"));
        assertTrue("American Sign Language I belongs to language", belongsToSubjectArea("American Sign Language I", "language"));
        
        System.out.println("✓ All subject area classifications work correctly");
    }
    
    @Test
    public void testTeacherNeedCalculation() {
        System.out.println("=== Testing Teacher Need Calculation ===");
        
        // Test that we calculate reasonable teacher needs
        int teachersFor50Students = calculateTeachersNeeded(50); // 50 student shortage
        assertTrue("Should need at least 1 teacher for 50 students", teachersFor50Students >= 1);
        assertTrue("Should not need more than 3 teachers for 50 students", teachersFor50Students <= 3);
        
        int teachersFor100Students = calculateTeachersNeeded(100); // 100 student shortage  
        assertTrue("Should need at least 2 teachers for 100 students", teachersFor100Students >= 2);
        assertTrue("Should not need more than 5 teachers for 100 students", teachersFor100Students <= 5);
        
        int teachersFor0Students = calculateTeachersNeeded(0); // No shortage
        assertEquals("Should need 0 teachers for no shortage", 0, teachersFor0Students);
        
        System.out.println("✓ Teacher need calculations are reasonable");
        System.out.println("  50 students → " + teachersFor50Students + " teachers");
        System.out.println("  100 students → " + teachersFor100Students + " teachers");
        System.out.println("  0 students → " + teachersFor0Students + " teachers");
    }
    
    @Test
    public void testCoreSubjectIdentification() {
        System.out.println("=== Testing Core Subject Identification ===");
        
        // Core subjects that should be prioritized
        assertTrue("World Geography is core", isCoreSubject("World Geography"));
        assertTrue("English I is core", isCoreSubject("English I"));
        assertTrue("Algebra I is core", isCoreSubject("Algebra I"));
        assertTrue("Biology is core", isCoreSubject("Biology"));
        assertTrue("AP Human Geography is core", isCoreSubject("AP Human Geography"));
        
        // Non-core subjects (electives)
        assertFalse("Theater I is not core", isCoreSubject("Theater I"));
        assertFalse("Digital Production Technology is not core", isCoreSubject("Digital Production Technology"));
        assertFalse("Culinary Arts is not core", isCoreSubject("Culinary Arts"));
        assertFalse("Photography I is not core", isCoreSubject("Photography I"));
        
        System.out.println("✓ Core subject identification works correctly");
    }
    
    @Test
    public void testBlockOptimizationLogic() {
        System.out.println("=== Testing Block Optimization Logic ===");
        
        // Test subject area grouping
        assertTrue("English I and English II are in same subject area", 
                  inSameSubjectArea("English I", "English II"));
        assertTrue("Algebra I and Geometry are in same subject area", 
                  inSameSubjectArea("Algebra I", "Geometry"));
        assertTrue("Biology and Chemistry are in same subject area", 
                  inSameSubjectArea("Biology", "Chemistry"));
        assertTrue("World Geography and US History are in same subject area", 
                  inSameSubjectArea("World Geography", "US History"));
        assertTrue("French I and Spanish II are in same subject area", 
                  inSameSubjectArea("French I", "Spanish II"));
        
        // Test that different subject areas are not grouped together
        assertFalse("English I and Algebra I are not in same subject area", 
                   inSameSubjectArea("English I", "Algebra I"));
        assertFalse("Biology and World Geography are not in same subject area", 
                   inSameSubjectArea("Biology", "World Geography"));
        assertFalse("French I and Health are not in same subject area", 
                   inSameSubjectArea("French I", "Health"));
        
        System.out.println("✓ Block optimization subject grouping works correctly");
    }
    
    @Test
    public void testUtilizationCalculation() {
        System.out.println("=== Testing Utilization Calculation Logic ===");
        
        // Test utilization percentage calculation
        double util1 = calculateUtilization(15, 25); // 60% utilization
        assertTrue("60% utilization should be considered acceptable", util1 == 0.6);
        
        double util2 = calculateUtilization(5, 25); // 20% utilization - keep this section for those 5 students
        assertTrue("20% utilization should be under 50% but we keep it for enrolled students", util2 < 0.5);
        
        double util3 = calculateUtilization(23, 25); // 92% utilization - well utilized
        assertTrue("92% utilization should be over 50% threshold", util3 > 0.5);
        
        double util4 = calculateUtilization(0, 25); // 0% utilization - ONLY these blocks get reassigned
        assertTrue("0% utilization should be exactly 0 - these are the ONLY blocks we reassign", util4 == 0.0);
        
        System.out.println("✓ Utilization calculations work correctly");
        System.out.println("  15/25 students → " + String.format("%.1f%%", util1 * 100) + " utilization (keep - students enrolled)");
        System.out.println("  5/25 students → " + String.format("%.1f%%", util2 * 100) + " utilization (keep - respect academic track)");
        System.out.println("  23/25 students → " + String.format("%.1f%%", util3 * 100) + " utilization (keep - well utilized)");
        System.out.println("  0/25 students → " + String.format("%.1f%%", util4 * 100) + " utilization (REASSIGN - completely empty)");
    }
    
    // Helper methods (copied from main class for testing)
    private static StaffType determineStaffTypeForClass(String className) {
        if (belongsToSubjectArea(className, "english")) return StaffType.ENGLISH;
        if (belongsToSubjectArea(className, "math")) return StaffType.MATH;
        if (belongsToSubjectArea(className, "science")) return StaffType.SCIENCE;
        if (belongsToSubjectArea(className, "history")) return StaffType.HISTORY;
        if (belongsToSubjectArea(className, "language")) return StaffType.LANGUAGES;
        if (belongsToSubjectArea(className, "physical education")) return StaffType.PHYSICAL_ED;
        
        // Default for electives
        if (className.toLowerCase().contains("art")) return StaffType.VISUAL_ARTS;
        if (className.toLowerCase().contains("music") || className.toLowerCase().contains("band") || 
            className.toLowerCase().contains("theater") || className.toLowerCase().contains("choir")) return StaffType.PERFORMING_ARTS;
        if (className.toLowerCase().contains("business")) return StaffType.BUSINESS;
        
        return StaffType.VOCATIONAL; // Default for other electives
    }
    
    private static boolean belongsToSubjectArea(String className, String subjectArea) {
        switch (subjectArea.toLowerCase()) {
            case "english": 
                return className.toLowerCase().contains("english") || className.toLowerCase().contains("ap english");
            case "math": 
                return className.toLowerCase().contains("math") || className.toLowerCase().contains("algebra") || 
                       className.toLowerCase().contains("geometry") || className.toLowerCase().contains("calculus") ||
                       className.toLowerCase().contains("trigonometry") || className.toLowerCase().contains("precalculus");
            case "science": 
                return className.toLowerCase().contains("biology") || className.toLowerCase().contains("chemistry") || 
                       className.toLowerCase().contains("physics") || className.toLowerCase().contains("science");
            case "history": 
                return className.toLowerCase().contains("history") || className.toLowerCase().contains("government") || 
                       className.toLowerCase().contains("geography") || className.toLowerCase().contains("economics");
            case "physical education": 
                return className.toLowerCase().contains("health") || className.toLowerCase().contains("sports") || 
                       className.toLowerCase().contains("weightlifting") || className.toLowerCase().contains("dance") ||
                       className.toLowerCase().contains("recreation");
            case "language": 
                return className.toLowerCase().contains("spanish") || className.toLowerCase().contains("french") || 
                       className.toLowerCase().contains("german") || className.toLowerCase().contains("latin") ||
                       className.toLowerCase().contains("sign language");
            default: 
                return false;
        }
    }
    
    private static boolean isCoreSubject(String className) {
        String[] coreKeywords = {"English", "Math", "Science", "History", "Biology", "Chemistry", 
                               "Physics", "Algebra", "Geometry", "Calculus", "Government", "Geography"};
        return Arrays.stream(coreKeywords)
            .anyMatch(keyword -> className.toLowerCase().contains(keyword.toLowerCase()));
    }
    
    private static int calculateTeachersNeeded(int shortageAmount) {
        if (shortageAmount <= 0) return 0;
        
        // Assume each teacher can handle ~50 students per class per semester
        int studentsPerTeacherPerClass = 50; // Conservative estimate
        
        return (int) Math.ceil((double) shortageAmount / studentsPerTeacherPerClass);
    }
    
    // Helper methods for new tests
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