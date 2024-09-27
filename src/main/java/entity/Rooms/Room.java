package entity.Rooms;//*******************************************************************
//  entity.Rooms.Room.java
//  Description: This is a room interface used to supply methods for entity.Rooms.Room type objects
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import entity.Staff;
import entity.Student;

import java.util.HashMap;
import java.util.List;

public interface Room {

    void reset();

    int getConnections();

    void setConnections(int connections);

    void setWindowCount(int windows);

    void setDoors(int doors);

    void setInitialStaff(int staffCount);

    void setInitialStudents(int studentCount);

    void setRoomNumber(String roomNumber);

    void setStudentRestriction(boolean studentRestriction);

    int getStudentCapacity();

    int getStaffCapacity();

    String getRoomName();

    void setRoomName(String roomName);

    List<Staff> getAssignedStaff();

    void setAssignedStaff(Staff staff);

    void removeAssignedStaff(Staff staff);

    void setSeatArrangement();

    Student[][] getSeatArrangement();

    Student getStudentInSeat(int x, int y);

    int[] getStudentSeatCoordinate(Student student);

    void addStudentToSeat(Student student, int x, int y);

    void removeStudentFromSeat(Student student);

    void swapStudentSeats(Student student1, Student student2);

    int getRoomCapacity();

    void setStudentCap(int studentCap);

    void addStudent(Student student);

    List<Student> getStudents();

    void setPeriodSeatingArrangement(int period, Student[][] seatArrangement);

    HashMap<Integer, Student[][]> getPeriodSeatingArrangement();

    void initializeSeatingArrangements(int totalPeriods);

}
