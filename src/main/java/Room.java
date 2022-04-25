//*******************************************************************
//  Room.java
//  Description: This is a room interface used to supply methods for Room type objects
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

public interface Room {

    void reset();

    void setRoomCapacity(int capacity);

    void setConnections(int connections);

    void setWindowCount(int windows);

    void setRoomName(String roomName);

    void setDoors(int doors);

    void setInitialStaff(int staffCount);

    void setInitialStudents(int studentCount);

    void setRoomNumber(int roomNumber);

    void setStudentRestriction(boolean studentRestriction);

    int getStudentCapacity();

    int getStaffCapacity();

}
