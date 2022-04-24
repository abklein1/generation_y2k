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


    @Override
    public void setBathrooms(int number) {
        Bathroom[] bathrooms = new Bathroom[number];
        for (int i = 0; i < number; i++) {
            bathrooms[i].setRoomName("Bathroom" + i);
        }
    }

    @Override
    public void setBreakrooms(int number) {
        Breakroom[] breakrooms = new Breakroom[number];
        for (int i = 0; i < number; i++) {
            breakrooms[i].setRoomName("Breakroom" + i);
            breakrooms[i].setStudentRestriction(true);
        }
    }

    @Override
    public void setClassrooms(int number) {
        Classroom[] classrooms = new Classroom[number];
        for (int i = 0; i < number; i++) {
            classrooms[i].setRoomName("Classroom" + i);
        }
    }

    @Override
    public void setCourtyards(int number) {
        Courtyard[] courtyards = new Courtyard[number];
        for (int i = 0; i < number; i++) {
            courtyards[i].setRoomName("Courtyard" + i);
        }
    }

    @Override
    public void setGyms(int number) {
        Gym[] gyms = new Gym[number];
        for (int i = 0; i < number; i++) {
            gyms[i].setRoomName("Gym" + i);
        }
    }

    @Override
    public void setHallways(int number) {
        Hallway[] hallways = new Hallway[number];
        for (int i = 0; i < number; i++) {
            hallways[i].setRoomName("Hallway" + i);
        }
    }

    @Override
    public void setLibraries(int number) {
        LibraryR[] libraries = new LibraryR[number];
        for (int i = 0; i < number; i++) {
            libraries[i].setRoomName("Library" + i);
        }
    }

    @Override
    public void setLunchrooms(int number) {
        Lunchroom[] lunchrooms = new Lunchroom[number];
        for (int i = 0; i < number; i++) {
            lunchrooms[i].setRoomName("Lunchroom" + i);
        }
    }

    @Override
    public void setOffices(int number) {
        Office[] offices = new Office[number];
        for (int i = 0; i < number; i++) {
            offices[i].setRoomName("Office" + i);
            offices[i].setStudentRestriction(true);
        }
    }

    @Override
    public void setUtilityRooms(int number) {
        UtilityRoom[] utilityrooms = new UtilityRoom[number];
        for (int i = 0; i < number; i++) {
            utilityrooms[i].setRoomName("UtilityRoom" + i);
            utilityrooms[i].setStudentRestriction(true);
        }
    }

}
