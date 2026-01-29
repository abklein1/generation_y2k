package entity.Rooms;
//*******************************************************************
//  entity.Rooms.Portable.java
//  Description: This represents a portable classroom object. Portables are
//  temporary classroom structures typically found in school parking lots,
//  courtyards, or near athletic fields. They are more common in underfunded
//  schools and cannot connect to hallways.
//  Bugs:
//
//  @author     Alex Klein
//  @version    01282026
//*******************************************************************

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a portable classroom - a temporary modular building used as
 * additional classroom space. Portables are more common at underfunded schools
 * (present in approximately 1/3 of American schools) and can only connect to
 * outdoor spaces like fields, courtyards, and parking lots.
 */
public class Portable extends Room {

    /** Flag indicating this room is a portable structure */
    private boolean isPortable = true;

    /**
     * Creates a new Portable classroom with default values.
     */
    public Portable() {
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = null;
        this.numOfDoors = 0;
        this.staffCap = 0;
        this.studentCap = 0;
        this.roomNumber = null;
        this.studentRestriction = false;
        this.classRoomType = null;
        this.staffAssign = new ArrayList<>();
        this.students = new ArrayList<>();
        this.seatingArrangements = new HashMap<>();
    }

    /**
     * Checks if this room is a portable structure.
     *
     * @return true since this is always a portable
     */
    public boolean isPortable() {
        return isPortable;
    }

    /**
     * Gets a display name that indicates this is a portable classroom.
     *
     * @return the room name with "Portable" prefix if not already included
     */
    public String getDisplayName() {
        if (roomName != null && roomName.startsWith("Portable")) {
            return roomName;
        }
        return "Portable " + (roomName != null ? roomName : "Classroom");
    }
}
