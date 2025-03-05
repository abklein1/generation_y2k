package entity.Rooms;//*******************************************************************
//  entity.Rooms.Bathroom.java
//  Description: This represents a bathroom object. Implements entity.Rooms.Room
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import entity.Staff;
import entity.Student;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bathroom extends roomBase {

    public Bathroom() {
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = "init";
        this.numOfDoors = 0;
        this.staffCap = 0;
        this.studentCap = 0;
        this.roomNumber = null;
        this.restrictF = false;
        this.restrictM = false;
        this.studentRestriction = false;
        this.stallNumber = 0;
        this.staffAssign = new ArrayList<>();
        this.students = new ArrayList<>();
        this.seatingArrangements = new HashMap<>();
    }
}
