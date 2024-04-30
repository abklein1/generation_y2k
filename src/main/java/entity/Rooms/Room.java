package entity.Rooms;//*******************************************************************
//  entity.Rooms.Room.java
//  Description: This is a room interface used to supply methods for entity.Rooms.Room type objects
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import entity.Staff;

import java.util.List;

public interface Room {

    void reset();

    void setRoomCapacity(int capacity);

    void setConnections(int connections);

    int getConnections();

    void setWindowCount(int windows);

    void setRoomName(String roomName);

    void setDoors(int doors);

    void setInitialStaff(int staffCount);

    void setInitialStudents(int studentCount);

    void setRoomNumber(String roomNumber);

    void setStudentRestriction(boolean studentRestriction);

    int getStudentCapacity();

    int getStaffCapacity();

    String getRoomName();

    void setAssignedStaff(Staff staff);

    List<Staff> getAssignedStaff();

    void removeAssignedStaff(Staff staff);

}
