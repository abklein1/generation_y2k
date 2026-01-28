package utility;

import entity.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Test cases for the StaffPool staff type matching fix.
 * Verifies that staff with null types can now be matched for assignment.
 */
public class StaffPoolMatchingTest {
    
    private StaffPool pool;
    
    @Before
    public void setUp() {
        pool = new StaffPool();
    }
    
    @Test
    public void testNullTypeStaffCanBeMatched() {
        System.out.println("=== Testing Null Type Staff Matching ===");
        
        // Create staff with null type (simulating newly generated staff)
        Staff newStaff = new Staff();
        // Don't set a staff type - this simulates newly generated staff
        assertNull("New staff should have null type", newStaff.teacherStatistics.getStaffType());
        
        pool.addStaff(newStaff);
        
        // Should be able to get this staff for any subject
        List<Staff> mathStaff = pool.getAvailableStaffForSubject(StaffType.MATH);
        assertTrue("Null-type staff should be available for MATH", mathStaff.contains(newStaff));
        
        List<Staff> englishStaff = pool.getAvailableStaffForSubject(StaffType.ENGLISH);
        assertTrue("Null-type staff should be available for ENGLISH", englishStaff.contains(newStaff));
        
        List<Staff> scienceStaff = pool.getAvailableStaffForSubject(StaffType.SCIENCE);
        assertTrue("Null-type staff should be available for SCIENCE", scienceStaff.contains(newStaff));
        
        System.out.println("✓ Null-type staff can be matched for assignment");
    }
    
    @Test
    public void testTypedStaffStillMatchCorrectly() {
        System.out.println("=== Testing Typed Staff Still Match Correctly ===");
        
        // Create staff with specific type
        Staff mathTeacher = new Staff();
        mathTeacher.teacherStatistics.setStaffType(StaffType.MATH);
        pool.addStaff(mathTeacher);
        
        // Should be available for MATH
        List<Staff> mathStaff = pool.getAvailableStaffForSubject(StaffType.MATH);
        assertTrue("Math teacher should be available for MATH", mathStaff.contains(mathTeacher));
        
        // Should NOT be available for ENGLISH (unless we want to change that behavior)
        // Currently the fix only adds null-type matching, typed staff still match exactly
        List<Staff> englishStaff = pool.getAvailableStaffForSubject(StaffType.ENGLISH);
        assertFalse("Math teacher should NOT be available for ENGLISH", englishStaff.contains(mathTeacher));
        
        System.out.println("✓ Typed staff still match correctly");
    }
    
    @Test
    public void testSubstituteStaffMatchesAllSubjects() {
        System.out.println("=== Testing Substitute Staff Matches All Subjects ===");
        
        // Create substitute teacher
        Staff substitute = new Staff();
        substitute.teacherStatistics.setStaffType(StaffType.SUB);
        pool.addStaff(substitute);
        
        // Substitutes should be available for any subject
        assertTrue("SUB should be available for MATH", 
                  pool.getAvailableStaffForSubject(StaffType.MATH).contains(substitute));
        assertTrue("SUB should be available for ENGLISH", 
                  pool.getAvailableStaffForSubject(StaffType.ENGLISH).contains(substitute));
        assertTrue("SUB should be available for SCIENCE", 
                  pool.getAvailableStaffForSubject(StaffType.SCIENCE).contains(substitute));
        assertTrue("SUB should be available for HISTORY", 
                  pool.getAvailableStaffForSubject(StaffType.HISTORY).contains(substitute));
        
        System.out.println("✓ Substitute staff matches all subjects");
    }
    
    @Test
    public void testMixedPoolStaffMatching() {
        System.out.println("=== Testing Mixed Pool Staff Matching ===");
        
        // Create a realistic mix of staff
        Staff nullTypeStaff1 = new Staff(); // null type
        Staff nullTypeStaff2 = new Staff(); // null type
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
        
        // Query for MATH - should get: null-types, math teacher, substitute
        List<Staff> mathAvailable = pool.getAvailableStaffForSubject(StaffType.MATH);
        assertEquals("Should have 4 staff available for MATH", 4, mathAvailable.size());
        assertTrue("Should include null-type staff", mathAvailable.contains(nullTypeStaff1));
        assertTrue("Should include null-type staff", mathAvailable.contains(nullTypeStaff2));
        assertTrue("Should include math teacher", mathAvailable.contains(mathTeacher));
        assertTrue("Should include substitute", mathAvailable.contains(substitute));
        assertFalse("Should NOT include english teacher", mathAvailable.contains(englishTeacher));
        
        // Query for ENGLISH - should get: null-types, english teacher, substitute
        List<Staff> englishAvailable = pool.getAvailableStaffForSubject(StaffType.ENGLISH);
        assertEquals("Should have 4 staff available for ENGLISH", 4, englishAvailable.size());
        assertTrue("Should include null-type staff", englishAvailable.contains(nullTypeStaff1));
        assertTrue("Should include english teacher", englishAvailable.contains(englishTeacher));
        assertTrue("Should include substitute", englishAvailable.contains(substitute));
        assertFalse("Should NOT include math teacher", englishAvailable.contains(mathTeacher));
        
        System.out.println("✓ Mixed pool staff matching works correctly");
        System.out.println("  MATH available: " + mathAvailable.size() + " staff");
        System.out.println("  ENGLISH available: " + englishAvailable.size() + " staff");
    }
    
    @Test
    public void testAssignedStaffNotAvailable() {
        System.out.println("=== Testing Assigned Staff Not Available ===");
        
        // Create staff and a school
        Staff teacher = new Staff();
        pool.addStaff(teacher);
        
        StandardSchool school = new StandardSchool();
        
        // Initially should be available
        List<Staff> beforeAssignment = pool.getAvailableStaffForSubject(StaffType.MATH);
        assertTrue("Staff should be available before assignment", beforeAssignment.contains(teacher));
        
        // Assign to school
        pool.assignToSchool(teacher, school);
        
        // Now should NOT be available (only unassigned staff are returned)
        List<Staff> afterAssignment = pool.getAvailableStaffForSubject(StaffType.MATH);
        assertFalse("Staff should NOT be available after assignment", afterAssignment.contains(teacher));
        
        System.out.println("✓ Assigned staff are correctly excluded from available pool");
    }
}
