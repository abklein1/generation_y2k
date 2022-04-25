//*******************************************************************
//  StandardSchool.java
//  Description: This is the implementation of a standard school based
//  on the implementation of the school plan interface
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import java.util.concurrent.ThreadLocalRandom;

public class StandardSchool implements SchoolPlan {

    String schoolName;
    Bathroom[] bathrooms;
    Breakroom[] breakrooms;
    Classroom[] classrooms;
    ComputerLab[] computerLabs;
    Courtyard[] courtyards;
    Gym[] gyms;
    Hallway[] hallways;
    LibraryR[] libraries;
    Lunchroom[] lunchrooms;
    Office[] offices;
    UtilityRoom[] utilityrooms;


    @Override
    public void setBathrooms(int number) {
        bathrooms = new Bathroom[number];
        System.out.println("   Generating " + number + " bathrooms...");
        for (int i = 0; i < number; i++) {
            bathrooms[i] = new Bathroom();
            if (i % 2 == 0) {
                bathrooms[i].setRoomName("Female_Bathroom" + i);
                System.out.println("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(true, false);
                bathrooms[i].setConnections(1);
                bathrooms[i].setDoors(1);
                bathrooms[i].setWindowCount(setRandom(1, 3));
                bathrooms[i].setRoomCapacity(setRandom(2, 16));
                bathrooms[i].setStallNumber(setRandom(3, 7));
            } else {
                bathrooms[i].setRoomName("Male_Bathroom" + i);
                System.out.println("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(false, true);
                bathrooms[i].setConnections(1);
                bathrooms[i].setDoors(1);
                bathrooms[i].setWindowCount(setRandom(1, 3));
                bathrooms[i].setRoomCapacity(setRandom(2, 18));
                bathrooms[i].setStallNumber(setRandom(2, 5));
                bathrooms[i].setRoomNumber("WC" + i + setRandom(0,9));
            }
        }
    }

    @Override
    public void setBreakrooms(int number) {
        breakrooms = new Breakroom[number];
        System.out.println("   Generating " + number + " breakroom(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(1, 2);
            breakrooms[i] = new Breakroom();
            breakrooms[i].setRoomName("Breakroom" + i);
            System.out.println("      Generating " + breakrooms[i].getRoomName());
            breakrooms[i].setStudentRestriction(true);
            breakrooms[i].setConnections(connectN);
            breakrooms[i].setDoors(connectN);
            breakrooms[i].setWindowCount(setRandom(2, 6));
            breakrooms[i].setRoomCapacity(setRandom(10, 25));
            breakrooms[i].setRoomNumber("B" + i + setRandom(0,9));
        }
    }

    @Override
    public void setClassrooms(int number) {
        int WEIGHT = 7;
        int decision = 0;
        classrooms = new Classroom[number];
        System.out.println("   Generating " + number + " classrooms...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(1, 2);
            decision = i % WEIGHT;
            classrooms[i] = new Classroom();
            classrooms[i].setRoomName("Classroom" + i);
            System.out.println("      Generating " + classrooms[i].getRoomName());
            classrooms[i].setConnections(connectN);
            classrooms[i].setDoors(connectN);
            classrooms[i].setClassroomType(decision);
            classrooms[i].setInitialStaff(1);
            classrooms[i].setRoomCapacity(setRandom(15, 40));
            classrooms[i].setRoomNumber(classrooms[i].getClassRoomType() + i + setRandom(0,99));
        }
    }

    @Override
    public void setComputerLabs(int number) {
        computerLabs = new ComputerLab[number];
        System.out.println("   Generating " + number + " Computer lab(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(1, 3);
            computerLabs[i] = new ComputerLab();
            computerLabs[i].setRoomName("ComputerLab" + i);
            System.out.println("      Generating " + computerLabs[i].getRoomName());
            computerLabs[i].setWindowCount(0);
            computerLabs[i].setConnections(connectN);
            computerLabs[i].setDoors(connectN);
            computerLabs[i].setRoomCapacity(setRandom(15, 45));
            computerLabs[i].setInitialStaff(setRandom(0, 1));
            computerLabs[i].setRoomNumber("COM" + i);
        }
    }

    @Override
    public void setCourtyards(int number) {
        courtyards = new Courtyard[number];
        System.out.println("   Generating " + number + " courtyard(s)...");
        for (int i = 0; i < number; i++) {
            courtyards[i] = new Courtyard();
            courtyards[i].setRoomName("Courtyard" + i);
            System.out.println("      Generating " + courtyards[i].getRoomName());
            courtyards[i].setWindowCount(0);
            courtyards[i].setConnections(4);
            courtyards[i].setDoors(4);
            courtyards[i].setRoomCapacity(setRandom(35, 150));
            courtyards[i].setRoomNumber("C" + i);
        }
    }

    @Override
    public void setGyms(int number) {
        gyms = new Gym[number];
        System.out.println("   Generating " + number + " gym(s)...");
        for (int i = 0; i < number; i++) {
            gyms[i] = new Gym();
            gyms[i].setRoomName("Gym" + i);
            System.out.println("      Generating " + gyms[i].getRoomName());
            gyms[i].setConnections(4);
            gyms[i].setDoors(4);
            gyms[i].setWindowCount(setRandom(4, 16));
            gyms[i].setRoomCapacity(setRandom(100, 450));
            gyms[i].setInitialStaff(setRandom(2, 4));
            gyms[i].setRoomNumber("G" + i + setRandom(0,9));
        }
    }

    @Override
    public void setHallways(int number) {
        hallways = new Hallway[number];
        System.out.println("   Generating " + number + " hallways...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(2, 12);
            hallways[i] = new Hallway();
            hallways[i].setRoomName("Hallway" + i);
            System.out.println("      Generating " + hallways[i].getRoomName());
            hallways[i].setConnections(connectN);
            hallways[i].setDoors(connectN);
            hallways[i].setWindowCount(setRandom(0, 6));
            hallways[i].setRoomCapacity(setRandom(35, 75));
            hallways[i].setRoomNumber("H" + i);
        }
    }

    @Override
    public void setLibraries(int number) {
        libraries = new LibraryR[number];
        System.out.println("   Generating " + number + " libraries...");
        for (int i = 0; i < number; i++) {
            int connectorN = setRandom(1, 4);
            libraries[i] = new LibraryR();
            libraries[i].setRoomName("Library" + i);
            System.out.println("      Generating " + libraries[i].getRoomName());
            libraries[i].setWindowCount(setRandom(4, 20));
            libraries[i].setConnections(connectorN);
            libraries[i].setDoors(connectorN);
            libraries[i].setInitialStaff(2);
            libraries[i].setRoomCapacity(setRandom(30, 200));
            libraries[i].setRoomNumber("L" + i + setRandom(0,9));
        }
    }

    @Override
    public void setLunchrooms(int number) {
        lunchrooms = new Lunchroom[number];
        System.out.println("   Generating " + number + " lunchroom(s)...");
        for (int i = 0; i < number; i++) {
            lunchrooms[i] = new Lunchroom();
            lunchrooms[i].setRoomName("Lunchroom" + i);
            System.out.println("      Generating " + lunchrooms[i].getRoomName());
            lunchrooms[i].setWindowCount(setRandom(5, 24));
            lunchrooms[i].setConnections(4);
            lunchrooms[i].setDoors(4);
            lunchrooms[i].setInitialStaff(setRandom(3, 10));
            lunchrooms[i].setRoomCapacity(setRandom(50, 250));
            lunchrooms[i].setRoomNumber("L" + i + setRandom(0,9));
        }
    }

    @Override
    public void setOffices(int number) {
        offices = new Office[number];
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
                offices[i].setRoomNumber("100");
            } else if (i == 1) {
                offices[i].setRoomName("Vice Principal's Office");
                System.out.println("      Generating Vice Principal's office");
                offices[i].setDoors(1);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(4);
                offices[i].setConnections(1);
                offices[i].setRoomNumber("101");
            } else if (i == 2) {
                offices[i].setRoomName("Guidance Councilor's Office");
                System.out.println("      Generating Councilor's Office");
                offices[i].setDoors(1);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(6);
                offices[i].setConnections(1);
                offices[i].setRoomNumber("102");
            } else if (i == 3) {
                offices[i].setRoomName("Front Office");
                System.out.println("      Generating Front Office");
                offices[i].setDoors(5);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(2);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(15);
                offices[i].setConnections(4);
                offices[i].setRoomNumber("103");
            } else {
                offices[i].setRoomName("Office" + i);
                System.out.println("      Generating " + offices[i].getRoomName());
                offices[i].setConnections(1);
                offices[i].setDoors(1);
                offices[i].setInitialStaff(1);
                offices[i].setRoomCapacity(setRandom(2, 6));
                offices[i].setRoomNumber("O"+ "1" + i);
            }
            offices[i].setStudentRestriction(true);

        }
    }

    @Override
    public void setUtilityRooms(int number) {
        utilityrooms = new UtilityRoom[number];
        System.out.println("   Generating " + number + " utility rooms...");
        for (int i = 0; i < number; i++) {
            utilityrooms[i] = new UtilityRoom();
            utilityrooms[i].setRoomName("UtilityRoom" + i);
            System.out.println("       Generating " + utilityrooms[i].getRoomName());
            utilityrooms[i].setStudentRestriction(true);
            utilityrooms[i].setDoors(1);
            utilityrooms[i].setConnections(1);
            utilityrooms[i].setWindowCount(0);
            utilityrooms[i].setInitialStaff(setRandom(1, 3));
            utilityrooms[i].setRoomCapacity(setRandom(3, 8));
            utilityrooms[i].setRoomNumber("U" + i + setRandom(0,9));
        }
    }

    public String getSchoolName() {
        return this.schoolName;
    }

    public void setSchoolName(String name) {
        this.schoolName = name;
    }

    private int setRandom(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max + 1);
        return random;
    }

    public int getTotalStudentCapacity() {
        int total = 0;
        for (Classroom classroom : classrooms) {
            total = total + classroom.getStudentCapacity();
        }

        return total;
    }

    public int getMinimumStaffRequirements() {
        int total = 0;
        int class_count = 0;
        int office_count = 0;
        int maint_count = 0;
        int lunch_count = 0;
        int library_count = 0;
        int gym_count = 0;
        int computer_count = 0;

        class_count = classrooms.length;
        office_count = offices.length / 2;
        maint_count = utilityrooms.length + 2;
        library_count = libraries.length * 2;

        for (Lunchroom lunchroom : lunchrooms) {
            lunch_count = lunch_count + lunchroom.getStaffCapacity();
        }

        for (Gym gym : gyms) {
            gym_count = gym_count + gym.getStaffCapacity();
        }

        for (ComputerLab computerLab : computerLabs) {
            computer_count = computer_count + computerLab.getStaffCapacity();
        }

        total = class_count + office_count + maint_count + lunch_count + library_count + gym_count;
        return total;
    }
}

