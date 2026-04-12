package entity.Rooms;
//*******************************************************************
//  entity.Rooms.Stairwell.java
//  Description: This represents a stairwell object. Stairwells are
//  part of the school backbone and connect hallways across different
//  floors. They are limited to exactly 2 connections (one per floor)
//  and cannot have rooms branch off of them.
//  Bugs:
//
//  @author     Alex Klein
//  @version    04102026
//*******************************************************************

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a stairwell in the school. Stairwells participate in the
 * backbone graph alongside hallways and courtyards, but are constrained
 * to exactly two connections -- conceptually linking two hallways on
 * different floors. Not all schools have stairwells; single-story
 * schools will have none.
 */
public class Stairwell extends Room {

    private int connectsFloorA;
    private int connectsFloorB;

    public Stairwell() {
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
        this.connectsFloorA = 0;
        this.connectsFloorB = 0;
    }

    public int getConnectsFloorA() {
        return connectsFloorA;
    }

    public void setConnectsFloorA(int floor) {
        this.connectsFloorA = floor;
    }

    public int getConnectsFloorB() {
        return connectsFloorB;
    }

    public void setConnectsFloorB(int floor) {
        this.connectsFloorB = floor;
    }
}
