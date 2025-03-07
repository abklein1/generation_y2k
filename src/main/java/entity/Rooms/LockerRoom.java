package entity.Rooms;

import java.util.ArrayList;
import java.util.HashMap;

public class LockerRoom extends Room {

    public LockerRoom() {
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = null;
        this.numOfDoors = 0;
        this.staffCap = 0;
        this.studentCap = 0;
        this.roomNumber = null;
        this.studentRestriction = false;
        this.staffAssign = new ArrayList<>();
        this.students = new ArrayList<>();
        this.seatingArrangements = new HashMap<>();
    }
}
