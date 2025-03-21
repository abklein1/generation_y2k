package entity.Rooms;//*******************************************************************
//  entity.Rooms.Breakroom.java
//  Description: This represents a breakroom object. Implements entity.Rooms.Room
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import java.util.ArrayList;
import java.util.HashMap;

public class Breakroom extends Room {

    public Breakroom() {
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = "";
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
