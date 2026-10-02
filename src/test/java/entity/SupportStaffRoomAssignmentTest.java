package entity;

import entity.Rooms.Breakroom;
import entity.Rooms.LibraryR;
import entity.Rooms.Lunchroom;
import entity.Rooms.Office;
import entity.Rooms.Room;
import entity.Rooms.UtilityRoom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utility.RoomAssignment;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Support staff room assignment")
class SupportStaffRoomAssignmentTest {

    @Test
    @DisplayName("Front office, admin, lunch, library, and maintenance share their dedicated rooms")
    void testSupportStaffShareDedicatedRooms() {
        StandardSchool school = buildSchool();
        HashMap<Integer, Staff> staff = new HashMap<>();
        int index = 0;

        Staff principal = addStaff(staff, index++, StaffType.PRINCIPAL);
        Staff vp1 = addStaff(staff, index++, StaffType.VICE_PRINCIPAL);
        Staff vp2 = addStaff(staff, index++, StaffType.VICE_PRINCIPAL);
        Staff vp3 = addStaff(staff, index++, StaffType.VICE_PRINCIPAL);
        Staff office1 = addStaff(staff, index++, StaffType.OFFICE);
        Staff office2 = addStaff(staff, index++, StaffType.OFFICE);
        Staff office3 = addStaff(staff, index++, StaffType.OFFICE);
        Staff nurse1 = addStaff(staff, index++, StaffType.NURSE);
        Staff nurse2 = addStaff(staff, index++, StaffType.NURSE);
        Staff guidance1 = addStaff(staff, index++, StaffType.GUIDANCE);
        Staff guidance2 = addStaff(staff, index++, StaffType.GUIDANCE);
        Staff guidance3 = addStaff(staff, index++, StaffType.GUIDANCE);
        Staff librarian = addStaff(staff, index++, StaffType.LIBRARY);
        Staff lunch1 = addStaff(staff, index++, StaffType.LUNCH);
        Staff lunch2 = addStaff(staff, index++, StaffType.LUNCH);
        Staff maintenance = addStaff(staff, index++, StaffType.MAINTENANCE);
        Staff sub = addStaff(staff, index, StaffType.SUB);

        RoomAssignment.ensureSupportStaffHaveRooms(school, staff);

        assertEquals("Principal's Office", roomName(principal));
        assertEquals("Vice Principal's Office", roomName(vp1));
        assertEquals("Vice Principal's Office", roomName(vp2));
        assertEquals("Vice Principal's Office", roomName(vp3));
        assertEquals("Front Office", roomName(office1));
        assertEquals("Front Office", roomName(office2));
        assertEquals("Front Office", roomName(office3));
        assertEquals("Nurse's Office", roomName(nurse1));
        assertEquals("Nurse's Office", roomName(nurse2));
        assertTrue(roomName(guidance1).contains("Guidance"));
        assertTrue(roomName(guidance2).contains("Guidance"));
        assertTrue(roomName(guidance3).contains("Guidance"));
        assertEquals("Library", roomName(librarian));
        assertEquals("Cafeteria", roomName(lunch1));
        assertEquals("Cafeteria", roomName(lunch2));
        assertEquals("Boiler Room", roomName(maintenance));
        assertSame(null, sub.getAssignedClassroom());
        assertEquals(3, namedOffice(school, "Front Office").getAssignedStaff().size());
        assertEquals(3, namedOffice(school, "Vice Principal's Office").getAssignedStaff().size());
    }

    @Test
    @DisplayName("Lookup after assignment finds support staff in their workspace")
    void testClassroomLookupFindsSupportStaff() {
        StandardSchool school = buildSchool();
        HashMap<Integer, Staff> staffMap = new HashMap<>();
        Staff clerk = addStaff(staffMap, 0, StaffType.OFFICE);

        RoomAssignment.ensureSupportStaffHaveRooms(school, staffMap);

        assertSame(namedOffice(school, "Front Office"), school.getClassroomByStaff(clerk));
    }

    private static StandardSchool buildSchool() {
        StandardSchool school = new StandardSchool();
        school.offices = new Office[] {
                namedOffice("Principal's Office", 1),
                namedOffice("Vice Principal's Office", 1),
                namedOffice("Guidance Councilor's Office", 1),
                namedOffice("Front Office", 2),
                namedOffice("Nurse's Office", 2)
        };

        Lunchroom cafeteria = new Lunchroom();
        cafeteria.setRoomName("Cafeteria");
        cafeteria.setInitialStaff(4);
        school.lunchrooms = new Lunchroom[] { cafeteria };

        LibraryR library = new LibraryR();
        library.setRoomName("Library");
        library.setInitialStaff(2);
        school.libraries = new LibraryR[] { library };

        UtilityRoom utility = new UtilityRoom();
        utility.setRoomName("Boiler Room");
        utility.setInitialStaff(2);
        school.utilityrooms = new UtilityRoom[] { utility };

        Breakroom breakroom = new Breakroom();
        breakroom.setRoomName("Staff Breakroom");
        breakroom.setInitialStaff(10);
        school.breakrooms = new Breakroom[] { breakroom };

        return school;
    }

    private static Office namedOffice(String name, int staffCap) {
        Office office = new Office();
        office.setRoomName(name);
        office.setInitialStaff(staffCap);
        return office;
    }

    private static Office namedOffice(StandardSchool school, String name) {
        for (Office office : school.offices) {
            if (name.equals(office.getRoomName())) {
                return office;
            }
        }
        throw new AssertionError("Missing office " + name);
    }

    private static Staff addStaff(HashMap<Integer, Staff> staff, int index, StaffType type) {
        Staff member = new Staff();
        member.teacherStatistics.setStaffType(type);
        staff.put(index, member);
        return member;
    }

    private static String roomName(Staff staff) {
        Room room = staff.getAssignedClassroom();
        return room != null ? room.getRoomName() : null;
    }
}
