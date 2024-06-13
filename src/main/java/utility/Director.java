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
import view.GameView;

import java.util.concurrent.ThreadLocalRandom;

import static utility.Randomizer.setRandom;

public class Director {
    //TODO: Change this to be dependent on staff/student caps
    //TODO: Create staff bathrooms
    final int BATHNUM = 15;

    public Director(StandardSchool standardSchool, GameView view) {
        setStandardSchool(standardSchool, view);
    }
    //TODO: Seed these values
    public void setStandardSchool(StandardSchool standardSchool, GameView view) {
        view.appendOutput("Building art studios...");
        standardSchool.setArtStudios(setRandom(1, 4), view);
        view.appendOutput("Building athletic fields...");
        standardSchool.setAthleticFields(setRandom(1, 3), view);
        view.appendOutput("Building auditoriums...");
        standardSchool.setAuditoriums(setRandom(1, 2), view);
        view.appendOutput("Building breakrooms...");
        standardSchool.setBreakrooms(setRandom(1, 4), view);
        view.appendOutput("Building classrooms...");
        standardSchool.setClassrooms(setRandom(18, 75), view);
        view.appendOutput("Building vocational rooms...");
        standardSchool.setVocationalRooms(setRandom(4,14), view);
        view.appendOutput("Building computer labs...");
        standardSchool.setComputerLabs(setRandom(1, 3), view);
        view.appendOutput("Building courtyards...");
        standardSchool.setCourtyards(setRandom(2, 5), view);
        view.appendOutput("Building drama rooms...");
        standardSchool.setDramaRooms(setRandom(1, 2), view);
        view.appendOutput("Building gyms...");
        standardSchool.setGyms(setRandom(1, 3), view);
        view.appendOutput("Building hallways...");
        standardSchool.setHallways(setRandom(9, 12), view);
        view.appendOutput("Building libraries...");
        standardSchool.setLibraries(setRandom(1, 2), view);
        view.appendOutput("Building locker rooms...");
        standardSchool.setLockerRooms((standardSchool.getGyms().length + standardSchool.getAthleticFields().length) * 2, view);
        view.appendOutput("Building lunchrooms...");
        standardSchool.setLunchrooms(setRandom(1, 2), view);
        view.appendOutput("Building music rooms...");
        standardSchool.setMusicRooms(setRandom(1, 2), view);
        view.appendOutput("Building offices...");
        standardSchool.setOffices(setRandom(5, standardSchool.getClassrooms().length), view);
        view.appendOutput("Building science labs...");
        standardSchool.setScienceLabs(setRandom(2, 6), view);
        view.appendOutput("Building utility rooms...");
        standardSchool.setUtilityRooms(setRandom(5, 10), view);
        view.appendOutput("Building conference rooms...");
        standardSchool.setConferenceRooms(setRandom(1,4), view);
        view.appendOutput("Building parking lots...");
        standardSchool.setParkingLots(setRandom(2,6), view);
        view.appendOutput("Building bathrooms...");
        standardSchool.setBathrooms(BATHNUM, view);
        view.appendOutput("Setting school name...");
        standardSchool.setSchoolName();
        view.appendOutput("Setting school mascot...");
        standardSchool.setSchoolMascot();
        view.appendOutput("Setting school colors...");
        standardSchool.schoolColorsLoader();
    }
}
