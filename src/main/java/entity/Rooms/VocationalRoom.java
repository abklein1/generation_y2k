package entity.Rooms;

import entity.Staff;

import java.util.ArrayList;
import java.util.List;


public class VocationalRoom implements Room {

    private int roomCapacity;
    private int numOfConnections;
    private int windowCount;
    private String roomName;
    private int numOfDoors;
    private int staffCap;
    private int studentCap;
    private String roomNumber;
    private boolean studentRestriction;
    private String classRoomType;
    private List<Staff> staffAssign;

    public VocationalRoom() {
        this.roomCapacity = 0;
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
    }

    @Override
    public void reset() {

    }

    @Override
    public void setRoomCapacity(int capacity) {
        this.roomCapacity = capacity;
    }

    @Override
    public int getConnections() {
        return this.numOfConnections;
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
        return roomCapacity - 1;
    }

    @Override
    public int getStaffCapacity() {
        return staffCap;
    }

    public String getRoomName() {
        return this.roomName;
    }

    @Override
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public List<Staff> getAssignedStaff() {
        return this.staffAssign;
    }

    @Override
    public void setAssignedStaff(Staff staff) {
        staffAssign.add(staff);
    }

    @Override
    public void removeAssignedStaff(Staff staff) {
        staffAssign.remove(staff);
    }

    @Override
    public String toString() {
        return this.roomName;
    }
}
