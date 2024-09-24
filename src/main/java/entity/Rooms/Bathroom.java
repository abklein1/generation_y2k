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

public class Bathroom implements Room, Serializable {
    private final List<Staff> staffAssign;
    private final List<Student> students;
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
    private Student[][] seats;
    private HashMap<Integer, Student[][]> seatingArrangements;

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

    @Override
    public void reset() {

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
    public int getConnections() {
        return this.numOfConnections;
    }

    @Override
    public void setConnections(int connections) {
        this.numOfConnections = connections;
    }

    @Override
    public String toString() {
        return this.roomName;
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

    // TODO: Later we can make desk arrangements that are not only squares/rectangles
    @Override
    public void setSeatArrangement() {
        seats = new Student[1][getRoomCapacity()];
    }

    @Override
    public Student[][] getSeatArrangement() {
        return seats;
    }

    @Override
    public Student getStudentInSeat(int x, int y) {
        return seats[x][y];
    }

    @Override
    public int[] getStudentSeatCoordinate(Student student) {
        int[] coords = new int[2];
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                if (seats[i][j].equals(student)) {
                    coords[0] = i;
                    coords[1] = j;
                    return coords;
                }
            }
        }
        System.out.println("Can't find student " + student.studentName);
        return coords;
    }

    @Override
    public void addStudentToSeat(Student student, int x, int y) {
        if (seats[x][y] != null) {
            System.out.println(student.studentName + " can't be assigned to seat because there is already someone there!");
        } else {
            seats[x][y] = student;
        }
    }

    @Override
    public void removeStudentFromSeat(Student student) {
        int[] coords = getStudentSeatCoordinate(student);
        seats[coords[0]][coords[1]] = null;
    }

    @Override
    public void swapStudentSeats(Student student1, Student student2) {
        int[] coords1 = getStudentSeatCoordinate(student1);
        int[] coords2 = getStudentSeatCoordinate(student2);

        removeStudentFromSeat(student1);
        removeStudentFromSeat(student2);

        addStudentToSeat(student1, coords2[0], coords2[1]);
        addStudentToSeat(student2, coords1[0], coords1[1]);
    }

    @Override
    public void setStudentCap(int studentCap) {
        this.studentCap = studentCap;
    }

    @Override
    public int getRoomCapacity() {
        return this.studentCap + this.staffCap;
    }

    @Override
    public void addStudent(Student student) {
        students.add(student);
    }

    @Override
    public List<Student> getStudents() {
        return this.students;
    }

    @Override
    public void setPeriodSeatingArrangement(int period, Student[][] seatArrangement) {
        seatingArrangements.put(period, seatArrangement);
    }

    @Override
    public HashMap<Integer, Student[][]> getPeriodSeatingArrangement() {
        return seatingArrangements;
    }

    @Override
    public void initializeSeatingArrangements(int totalPeriods) {
        for(int period = 0; period < totalPeriods; period++) {
            setPeriodSeatingArrangement(period,getSeatArrangement());
        }
    }
}
