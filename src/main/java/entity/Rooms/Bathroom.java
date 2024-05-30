package entity.Rooms;//*******************************************************************
//  entity.Rooms.Bathroom.java
//  Description: This represents a bathroom object. Implements entity.Rooms.Room
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import entity.Staff;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Bathroom implements Room, Serializable {

    private int roomCapacity;
    private int numOfConnections;
    private int windowCount;
    private String roomName;
    private int numOfDoors;
    private int staffCap;
    private int studentCap;
    private String roomNumber;
    private boolean restrictM;
    private boolean restrictF;
    private boolean studentRestriction;
    private int stallNumber;
    private final List<Staff> staffAssign;

    public Bathroom() {
        this.roomCapacity = 0;
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
    }

    @Override
    public void reset() {

    }

    @Override
    public void setRoomCapacity(int capacity) {
        this.roomCapacity = capacity;
    }

    @Override
    public void setConnections(int connections) {
        this.numOfConnections = connections;
    }

    @Override
    public void setWindowCount(int windows) {
        this.windowCount = windows;
    }

    @Override
    public void setDoors(int doors) {
        this.numOfDoors = doors;
    }

    @Override
    public void setInitialStaff(int staffCount) {
        this.staffCap = staffCount;
    }

    @Override
    public void setInitialStudents(int studentCount) {
        this.studentCap = studentCount;
    }

    @Override
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public void setStudentRestriction(boolean studentRestriction) {
        this.studentRestriction = studentRestriction;
    }

    @Override
    public int getStudentCapacity() {
        return studentCap;
    }

    @Override
    public int getStaffCapacity() {
        return staffCap;
    }

    public void setRoomRestrictions(boolean restrictM, boolean restrictF) {
        this.restrictM = restrictM;
        this.restrictF = restrictF;
    }

    public String getRoomName() {
        return this.roomName;
    }

    @Override
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setStallNumber(int stallNumber) {
        this.stallNumber = stallNumber;
    }
    @Override
    public int getConnections() {return this.numOfConnections;}
    @Override
    public String toString() {
        return this.roomName;
    }

    @Override
    public void setAssignedStaff(Staff staff) {
        staffAssign.add(staff);
    }

    @Override
    public List<Staff> getAssignedStaff() {
        return this.staffAssign;
    }

    @Override
    public void removeAssignedStaff(Staff staff) {
        staffAssign.remove(staff);
    }

    public void setStudentCap() {this.studentCap = roomCapacity - staffCap;}

}
