package utility;

import entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the StaffPool staff type matching.
 * Verifies that staff with null types can be matched for assignment,
 * typed staff match correctly, and substitutes can fill any role.
 */
@DisplayName("Staff Pool Matching Tests")
class StaffPoolMatchingTest {
    
    private StaffPool pool;
    
    @BeforeEach
    void setUp() {
        pool = new StaffPool();
    }
    
    @Test
    @DisplayName("Staff with null type should be available for any subject")
    void testNullTypeStaffCanBeMatched() {
        // Create staff with null type (simulating newly generated staff)
        Staff newStaff = new Staff();
        assertNull(newStaff.teacherStatistics.getStaffType(), "New staff should have null type");
        
        pool.addStaff(newStaff);
        
        // Should be able to get this staff for any subject
        assertAll("Null-type staff should be available for all subjects",
            () -> assertTrue(pool.getAvailableStaffForSubject(StaffType.MATH).contains(newStaff)),
            () -> assertTrue(pool.getAvailableStaffForSubject(StaffType.ENGLISH).contains(newStaff)),
            () -> assertTrue(pool.getAvailableStaffForSubject(StaffType.SCIENCE).contains(newStaff))
        );
    }
    
    @Test
    @DisplayName("Typed staff should only match their subject")
    void testTypedStaffStillMatchCorrectly() {
        Staff mathTeacher = new Staff();
        mathTeacher.teacherStatistics.setStaffType(StaffType.MATH);
        pool.addStaff(mathTeacher);
        
        List<Staff> mathStaff = pool.getAvailableStaffForSubject(StaffType.MATH);
        List<Staff> englishStaff = pool.getAvailableStaffForSubject(StaffType.ENGLISH);
        
        assertAll("Typed staff matching",
            () -> assertTrue(mathStaff.contains(mathTeacher), "Math teacher should be available for MATH"),
            () -> assertFalse(englishStaff.contains(mathTeacher), "Math teacher should NOT be available for ENGLISH")
        );
    }
    
    @Test
    @DisplayName("Substitute staff should match all subjects")
    void testSubstituteStaffMatchesAllSubjects() {
        Staff substitute = new Staff();
        substitute.teacherStatistics.setStaffType(StaffType.SUB);
        pool.addStaff(substitute);
        
        assertAll("Substitute should be available for all subjects",
            () -> assertTrue(pool.getAvailableStaffForSubject(StaffType.MATH).contains(substitute)),
            () -> assertTrue(pool.getAvailableStaffForSubject(StaffType.ENGLISH).contains(substitute)),
            () -> assertTrue(pool.getAvailableStaffForSubject(StaffType.SCIENCE).contains(substitute)),
            () -> assertTrue(pool.getAvailableStaffForSubject(StaffType.HISTORY).contains(substitute))
        );
    }
    
    @Test
    @DisplayName("Mixed pool should return correct staff for each subject")
    void testMixedPoolStaffMatching() {
        // Create a realistic mix of staff
        Staff nullTypeStaff1 = new Staff();
        Staff nullTypeStaff2 = new Staff();
        Staff mathTeacher = new Staff();
        mathTeacher.teacherStatistics.setStaffType(StaffType.MATH);
        Staff englishTeacher = new Staff();
        englishTeacher.teacherStatistics.setStaffType(StaffType.ENGLISH);
        Staff substitute = new Staff();
        substitute.teacherStatistics.setStaffType(StaffType.SUB);
        
        pool.addStaff(nullTypeStaff1);
        pool.addStaff(nullTypeStaff2);
        pool.addStaff(mathTeacher);
        pool.addStaff(englishTeacher);
        pool.addStaff(substitute);
        
        List<Staff> mathAvailable = pool.getAvailableStaffForSubject(StaffType.MATH);
        List<Staff> englishAvailable = pool.getAvailableStaffForSubject(StaffType.ENGLISH);
        
        assertAll("Mixed pool matching",
            () -> assertEquals(4, mathAvailable.size(), "Should have 4 staff available for MATH"),
            () -> assertTrue(mathAvailable.contains(nullTypeStaff1)),
            () -> assertTrue(mathAvailable.contains(mathTeacher)),
            () -> assertTrue(mathAvailable.contains(substitute)),
            () -> assertFalse(mathAvailable.contains(englishTeacher)),
            () -> assertEquals(4, englishAvailable.size(), "Should have 4 staff available for ENGLISH"),
            () -> assertTrue(englishAvailable.contains(englishTeacher)),
            () -> assertFalse(englishAvailable.contains(mathTeacher))
        );
    }
    
    @Test
    @DisplayName("Assigned staff should not be available in pool")
    void testAssignedStaffNotAvailable() {
        Staff teacher = new Staff();
        pool.addStaff(teacher);
        StandardSchool school = new StandardSchool();
        
        // Initially should be available
        List<Staff> beforeAssignment = pool.getAvailableStaffForSubject(StaffType.MATH);
        assertTrue(beforeAssignment.contains(teacher), "Staff should be available before assignment");
        
        // Assign to school
        pool.assignToSchool(teacher, school);
        
        // Now should NOT be available
        List<Staff> afterAssignment = pool.getAvailableStaffForSubject(StaffType.MATH);
        assertFalse(afterAssignment.contains(teacher), "Staff should NOT be available after assignment");
    }
}
