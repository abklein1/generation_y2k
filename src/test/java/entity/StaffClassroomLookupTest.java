package entity;

import entity.Rooms.Classroom;
import entity.Rooms.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Staff classroom lookup")
class StaffClassroomLookupTest {

    @Test
    @DisplayName("Unassigned staff are resolved once and later lookups skip the school scan")
    void testUnassignedStaffResolvedOnce() {
        StandardSchool school = new StandardSchool();
        school.classrooms = new Classroom[0];

        Staff staff = new Staff();
        staff.teacherName.setFirstName("Darren");
        staff.teacherName.setLastName("Freitas");
        staff.teacherStatistics.setStaffType(StaffType.SUB);

        assertFalse(staff.hasResolvedAssignedClassroom());
        assertNull(school.getClassroomByStaff(staff));
        assertTrue(staff.hasResolvedAssignedClassroom());
        assertNull(staff.getAssignedClassroom());

        // A later assignment after the miss must still be visible once recorded.
        Classroom classroom = new Classroom();
        classroom.setAssignedStaff(staff);
        assertSame(classroom, school.getClassroomByStaff(staff));
    }

    @Test
    @DisplayName("Assigning a teacher to a room caches the classroom without a lookup")
    void testAssignmentCachesClassroom() {
        StandardSchool school = new StandardSchool();
        Staff staff = new Staff();
        Classroom classroom = new Classroom();

        classroom.setAssignedStaff(staff);

        assertTrue(staff.hasResolvedAssignedClassroom());
        assertSame(classroom, staff.getAssignedClassroom());
        assertSame(classroom, school.getClassroomByStaff(staff));
    }

    @Test
    @DisplayName("A cached miss still finds the teacher after they are added to a room list")
    void testSearchFindsAssignedTeacherWhenCacheCleared() {
        StandardSchool school = new StandardSchool();
        Staff staff = new Staff();
        Classroom classroom = new Classroom();
        classroom.setAssignedStaff(staff);
        school.classrooms = new Classroom[] { classroom };

        staff.clearAssignedClassroomResolution();
        Room found = school.getClassroomByStaff(staff);

        assertSame(classroom, found);
        assertTrue(staff.hasResolvedAssignedClassroom());
        assertSame(classroom, staff.getAssignedClassroom());
    }

    @Test
    @DisplayName("Removing a teacher from a room forgets the cached assignment")
    void testRemoveClearsCachedClassroom() {
        Staff staff = new Staff();
        Classroom classroom = new Classroom();
        classroom.setAssignedStaff(staff);
        assertSame(classroom, staff.getAssignedClassroom());

        classroom.removeAssignedStaff(staff);
        assertFalse(staff.hasResolvedAssignedClassroom());
        assertNull(staff.getAssignedClassroom());
    }
}
