package utility;//*******************************************************************
//  utility.Director.java
//  Description: This directs the construction of a school from rooms
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import entity.StandardSchool;

import java.util.concurrent.ThreadLocalRandom;

public class Director {
    final int BATHNUM = 10;
    private final StandardSchool standardSchool;

    public Director(StandardSchool standardSchool) {
        this.standardSchool = standardSchool;
        setStandardSchool(this.standardSchool);
    }

    public void setStandardSchool(StandardSchool standardSchool) {
        System.out.println("Building bathrooms...");
        standardSchool.setBathrooms(BATHNUM);
        System.out.println("Building breakrooms...");
        standardSchool.setBreakrooms(setRandom(1, 4));
        System.out.println("Building classrooms...");
        standardSchool.setClassrooms(setRandom(12, 35));
        System.out.println("Building computer labs...");
        standardSchool.setComputerLabs(setRandom(1, 3));
        System.out.println("Building courtyards...");
        standardSchool.setCourtyards(setRandom(1, 5));
        System.out.println("Building gyms...");
        standardSchool.setGyms(setRandom(1, 3));
        System.out.println("Building hallways...");
        standardSchool.setHallways(setRandom(6, 10));
        System.out.println("Building libraries...");
        standardSchool.setLibraries(setRandom(1, 2));
        System.out.println("Building lunchrooms...");
        standardSchool.setLunchrooms(setRandom(1, 2));
        System.out.println("Building offices...");
        standardSchool.setOffices(setRandom(5, 20));
        System.out.println("Building utility rooms...");
        standardSchool.setUtilityRooms(setRandom(5, 10));
        System.out.println("Setting school name...");
        standardSchool.setSchoolName();
    }

    private int setRandom(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max + 1);
        return random;
    }

}
