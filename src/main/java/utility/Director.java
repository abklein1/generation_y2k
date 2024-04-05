package utility;

//*******************************************************************
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
    //TODO: Why is bathnum specified always as 10?
    final int BATHNUM = 10;

    public Director(StandardSchool standardSchool) {
        setStandardSchool(standardSchool);
    }
    //TODO: Seed these values
    public void setStandardSchool(StandardSchool standardSchool) {
        System.out.println("Building art studios...");
        standardSchool.setArtStudios(setRandom(1, 3));
        System.out.println("Building athletic fields...");
        standardSchool.setAthleticFields(setRandom(1, 3));
        System.out.println("Building auditoriums...");
        standardSchool.setAuditoriums(setRandom(1, 2));
        System.out.println("Building bathrooms...");
        standardSchool.setBathrooms(BATHNUM);
        System.out.println("Building breakrooms...");
        standardSchool.setBreakrooms(setRandom(1, 4));
        System.out.println("Building classrooms...");
        standardSchool.setClassrooms(setRandom(18, 65));
        System.out.println("Building computer labs...");
        standardSchool.setComputerLabs(setRandom(1, 3));
        System.out.println("Building courtyards...");
        standardSchool.setCourtyards(setRandom(2, 5));
        System.out.println("Building drama rooms...");
        standardSchool.setDramaRooms(setRandom(1, 2));
        System.out.println("Building gyms...");
        standardSchool.setGyms(setRandom(1, 3));
        System.out.println("Building hallways...");
        standardSchool.setHallways(setRandom(9, 12));
        System.out.println("Building libraries...");
        standardSchool.setLibraries(setRandom(1, 2));
        System.out.println("Building locker rooms...");
        standardSchool.setLockerRooms((standardSchool.getGyms().length + standardSchool.getAthleticFields().length) * 2);
        System.out.println("Building lunchrooms...");
        standardSchool.setLunchrooms(setRandom(1, 2));
        System.out.println("Building music rooms...");
        standardSchool.setMusicRooms(setRandom(1, 2));
        System.out.println("Building offices...");
        standardSchool.setOffices(setRandom(5, standardSchool.getClassrooms().length));
        System.out.println("Building science labs...");
        standardSchool.setScienceLabs(setRandom(2, 6));
        System.out.println("Building utility rooms...");
        standardSchool.setUtilityRooms(setRandom(5, 10));
        System.out.println("Setting school name...");
        standardSchool.setSchoolName();
        System.out.println("Setting school mascot...");
        standardSchool.setSchoolMascot();
    }

    //TODO: move this to central utility for use among multiple classes
    private int setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

}
