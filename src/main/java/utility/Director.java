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
import static constants.SchoolConstants.*;

import java.util.concurrent.ThreadLocalRandom;

import static utility.Randomizer.setRandom;

public class Director {
    //TODO: Change this to be dependent on staff/student caps
    //TODO: Create staff bathrooms

    public Director(StandardSchool standardSchool, GameView view) {
        setStandardSchool(standardSchool, view);
    }
    //TODO: Seed these values
    public void setStandardSchool(StandardSchool standardSchool, GameView view) {
        view.appendOutput("Building art studios...");
        standardSchool.setArtStudios(setRandom(ART_AMOUNT_LOWER_LIMIT, ART_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building athletic fields...");
        standardSchool.setAthleticFields(setRandom(ATHLETIC_AMOUNT_LOWER_LIMIT, ATHLETIC_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building auditoriums...");
        standardSchool.setAuditoriums(setRandom(AUDITORIUM_AMOUNT_LOWER_LIMIT, AUDITORIUM_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building breakrooms...");
        standardSchool.setBreakrooms(setRandom(BREAKROOM_AMOUNT_LOWER_LIMIT, BREAKROOM_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building classrooms...");
        standardSchool.setClassrooms(setRandom(CLASSROOM_AMOUNT_LOWER_LIMIT, CLASSROOM_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building vocational rooms...");
        standardSchool.setVocationalRooms(setRandom(VOCATIONAL_AMOUNT_LOWER_LIMIT, VOCATIONAL_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building computer labs...");
        standardSchool.setComputerLabs(setRandom(COMPUTER_LAB_AMOUNT_LOWER_LIMIT, COMPUTER_LAB_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building courtyards...");
        standardSchool.setCourtyards(setRandom(COURTYARD_AMOUNT_LOWER_LIMIT, COURTYARD_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building drama rooms...");
        standardSchool.setDramaRooms(setRandom(DRAMA_AMOUNT_LOWER_LIMIT, DRAMA_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building gyms...");
        standardSchool.setGyms(setRandom(GYM_AMOUNT_LOWER_LIMIT, GYM_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building hallways...");
        standardSchool.setHallways(setRandom(HALLWAY_AMOUNT_LOWER_LIMIT, HALLWAY_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building libraries...");
        standardSchool.setLibraries(setRandom(LIBRARY_AMOUNT_LOWER_LIMIT, LIBRARY_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building locker rooms...");
        standardSchool.setLockerRooms((standardSchool.getGyms().length + standardSchool.getAthleticFields().length) * LOCKER_ROOM_MODIFIER, view);
        view.appendOutput("Building lunchrooms...");
        standardSchool.setLunchrooms(setRandom(LUNCHROOM_AMOUNT_LOWER_LIMIT, LUNCHROOM_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building music rooms...");
        standardSchool.setMusicRooms(setRandom(MUSIC_AMOUNT_LOWER_LIMIT, MUSIC_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building offices...");
        standardSchool.setOffices(setRandom(OFFICE_AMOUNT_LOWER_LIMIT, standardSchool.getClassrooms().length), view);
        view.appendOutput("Building science labs...");
        standardSchool.setScienceLabs(setRandom(SCIENCE_LAB_AMOUNT_LOWER_LIMIT, SCIENCE_LAB_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building utility rooms...");
        standardSchool.setUtilityRooms(setRandom(UTILITY_AMOUNT_LOWER_LIMIT, UTILITY_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building conference rooms...");
        standardSchool.setConferenceRooms(setRandom(CONFERENCE_AMOUNT_LOWER_LIMIT, CONFERENCE_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building parking lots...");
        standardSchool.setParkingLots(setRandom(PARKING_AMOUNT_LOWER_LIMIT, PARKING_AMOUNT_UPPER_LIMIT), view);
        view.appendOutput("Building bathrooms...");
        standardSchool.setBathrooms(BATHROOM_AMOUNT, view);
        view.appendOutput("Setting school name...");
        standardSchool.setSchoolName();
        view.appendOutput("Setting school mascot...");
        standardSchool.setSchoolMascot();
        view.appendOutput("Setting school colors...");
        standardSchool.schoolColorsLoader();
        view.appendOutput("Setting school founded year...");
        standardSchool.setSchoolFoundedYear();
    }
}
