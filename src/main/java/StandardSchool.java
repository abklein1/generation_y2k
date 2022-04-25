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
        System.out.println("   Generating " + number + " bathrooms...");
        for (int i = 0; i < number; i++) {
            bathrooms[i] = new Bathroom();
            if (i % 2 == 0) {
                bathrooms[i].setRoomName("Female_Bathroom" + i);
                System.out.println("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(true, false);
                bathrooms[i].setConnections(1);
                bathrooms[i].setDoors(1);
            } else {
                bathrooms[i].setRoomName("Male_Bathroom" + i);
                System.out.println("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(false, true);
                bathrooms[i].setConnections(1);
                bathrooms[i].setDoors(1);
            }
        }
    }

    @Override
    public void setBreakrooms(int number) {
        Breakroom[] breakrooms = new Breakroom[number];
        System.out.println("   Generating " + number + " breakroom(s)...");
        for (int i = 0; i < number; i++) {
            breakrooms[i] = new Breakroom();
            breakrooms[i].setRoomName("Breakroom" + i);
            System.out.println("      Generating " + breakrooms[i].getRoomName());
            breakrooms[i].setStudentRestriction(true);
        }
    }

    @Override
    public void setClassrooms(int number) {
        Classroom[] classrooms = new Classroom[number];
        System.out.println("   Generating " + number + " classrooms...");
        for (int i = 0; i < number; i++) {
            classrooms[i] = new Classroom();
            classrooms[i].setRoomName("Classroom" + i);
            System.out.println("      Generating " + classrooms[i].getRoomName());
        }
    }

    @Override
    public void setComputerLabs(int number) {
        ComputerLab[] computerLabs = new ComputerLab[number];
        System.out.println("   Generating " + number + " Computer lab(s)...");
        for (int i = 0; i < number; i++) {
            computerLabs[i] = new ComputerLab();
            computerLabs[i].setRoomName("ComputerLab" + i);
            System.out.println("      Generating " + computerLabs[i].getRoomName());
            computerLabs[i].setWindowCount(0);
        }
    }

    @Override
    public void setCourtyards(int number) {
        Courtyard[] courtyards = new Courtyard[number];
        System.out.println("   Generating " + number + " courtyard(s)...");
        for (int i = 0; i < number; i++) {
            courtyards[i] = new Courtyard();
            courtyards[i].setRoomName("Courtyard" + i);
            System.out.println("      Generating " + courtyards[i].getRoomName());
            courtyards[i].setWindowCount(0);
            courtyards[i].setConnections(4);
        }
    }

    @Override
    public void setGyms(int number) {
        Gym[] gyms = new Gym[number];
        System.out.println("   Generating " + number + " gym(s)...");
        for (int i = 0; i < number; i++) {
            gyms[i] = new Gym();
            gyms[i].setRoomName("Gym" + i);
            System.out.println("      Generating " + gyms[i].getRoomName());
        }
    }

    @Override
    public void setHallways(int number) {
        Hallway[] hallways = new Hallway[number];
        System.out.println("   Generating " + number + " hallways...");
        for (int i = 0; i < number; i++) {
            hallways[i] = new Hallway();
            hallways[i].setRoomName("Hallway" + i);
            System.out.println("      Generating " + hallways[i].getRoomName());
        }
    }

    @Override
    public void setLibraries(int number) {
        LibraryR[] libraries = new LibraryR[number];
        System.out.println("   Generating " + number + " libraries...");
        for (int i = 0; i < number; i++) {
            libraries[i] = new LibraryR();
            libraries[i].setRoomName("Library" + i);
            System.out.println("      Generating " + libraries[i].getRoomName());
        }
    }

    @Override
    public void setLunchrooms(int number) {
        Lunchroom[] lunchrooms = new Lunchroom[number];
        System.out.println("   Generating " + number + " lunchroom(s)...");
        for (int i = 0; i < number; i++) {
            lunchrooms[i] = new Lunchroom();
            lunchrooms[i].setRoomName("Lunchroom" + i);
            System.out.println("      Generating " + lunchrooms[i].getRoomName());
        }
    }

    @Override
    public void setOffices(int number) {
        Office[] offices = new Office[number];
        System.out.println("   Generating " + number + " offices...");
        for (int i = 0; i < number; i++) {
            offices[i] = new Office();
            if (i == 0) {
                offices[i].setRoomName("Principal's Office");
                System.out.println("      Generating Principal's office");
                offices[i].setDoors(1);
                offices[i].setWindowCount(3);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(6);
                offices[i].setConnections(1);
                offices[i].setRoomNumber(100);
            } else if (i == 1) {
                offices[i].setRoomName("Vice Principal's Office");
                System.out.println("      Generating Vice Principal's office");
                offices[i].setDoors(1);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(4);
                offices[i].setConnections(1);
                offices[i].setRoomNumber(101);
            } else if (i == 2) {
                offices[i].setRoomName("Guidance Councilor's Office");
                System.out.println("      Generating Councilor's Office");
                offices[i].setDoors(1);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(6);
                offices[i].setConnections(1);
                offices[i].setRoomNumber(102);
            } else if (i == 3) {
                offices[i].setRoomName("Front Office");
                System.out.println("      Generating Front Office");
                offices[i].setDoors(5);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(2);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(15);
                offices[i].setConnections(4);
                offices[i].setRoomNumber(103);
            } else {
                offices[i].setRoomName("Office" + i);
                System.out.println("      Generating " + offices[i].getRoomName());
            }
            offices[i].setStudentRestriction(true);

        }
    }

    @Override
    public void setUtilityRooms(int number) {
        UtilityRoom[] utilityrooms = new UtilityRoom[number];
        System.out.println("   Generating " + number + " utility rooms...");
        for (int i = 0; i < number; i++) {
            utilityrooms[i] = new UtilityRoom();
            utilityrooms[i].setRoomName("UtilityRoom" + i);
            System.out.println("       Generating " + utilityrooms[i].getRoomName());
            utilityrooms[i].setStudentRestriction(true);
        }
    }

    public String getSchoolName() {
        return this.schoolName;
    }

    public void setSchoolName(String name) {
        this.schoolName = name;
    }

}
