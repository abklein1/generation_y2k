package entity.Rooms;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Virtual room representing an off-campus lunch destination.
 * Not placed on the room graph or RoomConnector -- students assigned here
 * disappear from the physical grid and reappear when lunch ends.
 * One singleton instance per school.
 */
public class OffCampus extends Room {

    public OffCampus() {
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = "Off Campus";
        this.numOfDoors = 0;
        this.staffCap = 0;
        this.studentCap = Integer.MAX_VALUE;
        this.roomNumber = "OC-0";
        this.studentRestriction = false;
        this.staffAssign = new ArrayList<>();
        this.students = new ArrayList<>();
        this.seatingArrangements = new HashMap<>();
    }

    @Override
    public boolean isInstructionalSpace() {
        return false;
    }
}
