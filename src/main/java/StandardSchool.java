//*******************************************************************
//  StandardSchool.java
//  Description: This is the implementation of a standard school based
//  on the implementation of the school plan interface
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************


public class StandardSchool implements SchoolPlan {

    String schoolName;

    @Override
    public void setBathrooms(int number) {
        Bathroom[] bathrooms = new Bathroom[number];
        for (int i = 0; i < number; i++) {
            bathrooms[i] = new Bathroom();
            if(i % 2 == 0){
                bathrooms[i].setRoomName("Female_Bathroom" + i);
                bathrooms[i].setRoomRestrictions(true, false);
            } else {
                bathrooms[i].setRoomName("Male_Bathroom" + i);
                bathrooms[i].setRoomRestrictions(false, true);
            }
        }
    }

    @Override
    public void setBreakrooms(int number) {
        Breakroom[] breakrooms = new Breakroom[number];
        for (int i = 0; i < number; i++) {
            breakrooms[i] = new Breakroom();
            breakrooms[i].setRoomName("Breakroom" + i);
            breakrooms[i].setStudentRestriction(true);
        }
    }

    @Override
    public void setClassrooms(int number) {
        Classroom[] classrooms = new Classroom[number];
        for (int i = 0; i < number; i++) {
            classrooms[i] = new Classroom();
            classrooms[i].setRoomName("Classroom" + i);
        }
    }

    @Override
    public void setComputerLabs(int number){
        ComputerLab[] computerLabs = new ComputerLab[number];
        for (int i = 0; i < number; i++){
            computerLabs[i] = new ComputerLab();
            computerLabs[i].setRoomName("ComputerLab" + i);
            computerLabs[i].setWindowCount(0);
        }
    }

    @Override
    public void setCourtyards(int number) {
        Courtyard[] courtyards = new Courtyard[number];
        for (int i = 0; i < number; i++) {
            courtyards[i] = new Courtyard();
            courtyards[i].setRoomName("Courtyard" + i);
            courtyards[i].setWindowCount(0);
            courtyards[i].setConnections(4);
        }
    }

    @Override
    public void setGyms(int number) {
        Gym[] gyms = new Gym[number];
        for (int i = 0; i < number; i++) {
            gyms[i] = new Gym();
            gyms[i].setRoomName("Gym" + i);
        }
    }

    @Override
    public void setHallways(int number) {
        Hallway[] hallways = new Hallway[number];
        for (int i = 0; i < number; i++) {
            hallways[i] = new Hallway();
            hallways[i].setRoomName("Hallway" + i);
        }
    }

    @Override
    public void setLibraries(int number) {
        LibraryR[] libraries = new LibraryR[number];
        for (int i = 0; i < number; i++) {
            libraries[i] = new LibraryR();
            libraries[i].setRoomName("Library" + i);
        }
    }

    @Override
    public void setLunchrooms(int number) {
        Lunchroom[] lunchrooms = new Lunchroom[number];
        for (int i = 0; i < number; i++) {
            lunchrooms[i] = new Lunchroom();
            lunchrooms[i].setRoomName("Lunchroom" + i);
        }
    }

    @Override
    public void setOffices(int number) {
        Office[] offices = new Office[number];
        for (int i = 0; i < number; i++) {
            offices[i] = new Office();
            if(i == 0) {
                offices[i].setRoomName("Principal's Office");
                offices[i].setDoors(1);
                offices[i].setWindowCount(3);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(6);
                offices[i].setConnections(1);
                offices[i].setRoomNumber(100);
            } else if(i == 1) {
                offices[i].setRoomName("Front Office");
                offices[i].setDoors(2);
                offices[i].setWindowCount(3);
                offices[i].setInitialStaff(2);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(15);
                offices[i].setConnections(2);
                offices[i].setRoomNumber(101);
            } else {
                offices[i].setRoomName("Office" + i);
            }
            offices[i].setStudentRestriction(true);

        }
    }

    @Override
    public void setUtilityRooms(int number) {
        UtilityRoom[] utilityrooms = new UtilityRoom[number];
        for (int i = 0; i < number; i++) {
            utilityrooms[i] = new UtilityRoom();
            utilityrooms[i].setRoomName("UtilityRoom" + i);
            utilityrooms[i].setStudentRestriction(true);
        }
    }

    public void setSchoolName(String name) {
        this.schoolName = name;
    }

}
