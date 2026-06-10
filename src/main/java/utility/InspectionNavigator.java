package utility;

import entity.Staff;
import entity.Student;
import entity.Rooms.Room;

/**
 * Routes a clicked entity link to the appropriate inspection view. Clicking a
 * student or staff link behaves as if that entity had been selected in the
 * rich grade/staff inspection window; clicking a room opens the room
 * inspection.
 */
public interface InspectionNavigator {

    /**
     * Opens (or focuses) the inspection window for the student's grade and
     * selects the student so all of their tabs populate.
     *
     * @param student the student to inspect
     */
    void navigateToStudent(Student student);

    /**
     * Opens (or focuses) the staff inspection window and selects the staff
     * member.
     *
     * @param staff the staff member to inspect
     */
    void navigateToStaff(Staff staff);

    /**
     * Opens the room inspection view for the given room.
     *
     * @param room the room to inspect
     */
    void navigateToRoom(Room room);
}
