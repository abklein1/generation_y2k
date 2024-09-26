package entity;
//*******************************************************************
//  entity.StandardSchool.java
//  Description: This is the implementation of a standard school based
//  on the implementation of the school plan interface
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import entity.Rooms.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import view.GameView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static constants.SchoolConstants.*;
import static constants.SimConstants.STARTING_YEAR;
import static utility.Randomizer.setRandom;

public class StandardSchool implements SchoolPlan {

    String schoolName;
    String schoolMascot;
    String schoolFoundedYear;
    String[] schoolColors;
    String[] schoolColorsHex;
    ArtStudio[] artStudios;
    AthleticField[] athleticFields;
    Auditorium[] auditoriums;
    Bathroom[] bathrooms;
    Breakroom[] breakrooms;
    Classroom[] classrooms;
    ComputerLab[] computerLabs;
    Courtyard[] courtyards;
    DramaRoom[] dramaRooms;
    Gym[] gyms;
    Hallway[] hallways;
    LibraryR[] libraries;
    LockerRoom[] lockerRooms;
    Lunchroom[] lunchrooms;
    MusicRoom[] musicRooms;
    Office[] offices;
    ScienceLab[] scienceLabs;
    UtilityRoom[] utilityrooms;
    ConferenceRoom[] conferenceRooms;
    ParkingLot[] parkingLots;
    VocationalRoom[] vocationalRooms;
    HashMap<Integer, Student> freshmanClass = new HashMap<>();
    HashMap<Integer, Student> sophomoreClass = new HashMap<>();
    HashMap<Integer, Student> juniorClass = new HashMap<>();
    HashMap<Integer, Student> seniorClass = new HashMap<>();

    @Override
    public void setUtilityRooms(int number, GameView view) {
        utilityrooms = new UtilityRoom[number];
        view.appendOutput("   Generating " + number + " utility rooms...");
        for (int i = 0; i < number; i++) {
            utilityrooms[i] = new UtilityRoom();
            utilityrooms[i].setRoomName("UtilityRoom" + i);
            view.appendOutput("       Generating " + utilityrooms[i].getRoomName());
            utilityrooms[i].setStudentRestriction(true);
            utilityrooms[i].setDoors(UTILITY_CONNECTION_COUNT);
            utilityrooms[i].setConnections(UTILITY_CONNECTION_COUNT);
            utilityrooms[i].setWindowCount(UTILITY_WINDOW_COUNT);
            utilityrooms[i].setInitialStaff(setRandom(UTILITY_STAFF_LOWER_LIMIT, UTILITY_STAFF_UPPER_LIMIT));
            utilityrooms[i].setStudentCap(setRandom(UTILITY_STUDENT_CAP_LOWER_LIMIT, UTILITY_STUDENT_CAP_UPPER_LIMIT));
            utilityrooms[i].setSeatArrangement();
            utilityrooms[i].setRoomNumber("U" + i + setRandom(UTILITY_ROOM_NUMBER_LOWER_LIMIT, UTILITY_ROOM_NUMBER_UPPER_LIMIT));
        }
    }

    public String getSchoolName() {
        return this.schoolName;
    }

    public void setSchoolName() {
        this.schoolName = schoolNameLoader();
    }

    public void setSchoolMascot() {
        this.schoolMascot = schoolMascotLoader();
    }

    public String getMascot() {
        return this.schoolMascot;
    }

    public int getTotalStudentCapacity() {

        int class_total = 0;

        for (Classroom classroom : classrooms) {
            class_total = class_total + classroom.getStudentCapacity();
        }

        // We don't want school to be at total capacity to begin with
        return (int) ((class_total) * TOTAL_STUDENT_CAP_MODIFIER);
    }

    public int getMinimumStaffRequirements() {
        int total;
        int class_count;
        int office_count;
        int maint_count;
        int lunch_count = 0;
        int library_count;
        int gym_count = 0;
        int computer_count = 0;
        int art_count = 0;
        int field_count = 0;
        int drama_count;
        int music_count;
        int vocation_count = 0;

        class_count = classrooms.length;
        office_count = offices.length / OFFICE_NUMBER_MODIFIER;
        maint_count = utilityrooms.length + UTILITY_ROOM_NUMBER_MODIFIER;
        library_count = libraries.length * LIBRARY_ROOM_NUMBER_MODIFIER;
        drama_count = dramaRooms.length;
        music_count = musicRooms.length;

        for (Lunchroom lunchroom : lunchrooms) {
            lunch_count = lunch_count + lunchroom.getStaffCapacity();
        }

        for (Gym gym : gyms) {
            gym_count = gym_count + gym.getStaffCapacity();
        }

        for (ComputerLab computerLab : computerLabs) {
            computer_count = computer_count + computerLab.getStaffCapacity();
        }

        for (ArtStudio artStudio : artStudios) {
            art_count = art_count + artStudio.getStaffCapacity();
        }

        for (AthleticField field : athleticFields) {
            field_count = field_count + field.getStaffCapacity();
        }

        for (VocationalRoom room : vocationalRooms) {
            vocation_count = vocation_count + room.getStaffCapacity();
        }
        // Going to over-assign staff for now
        total = class_count + office_count + maint_count +
                lunch_count + library_count + gym_count +
                art_count + field_count + drama_count + music_count + vocation_count + ((int) Math.round(getTotalStudentCapacity() * TOTAL_STAFF_CAP_MODIFIER));

        return total;
    }

    private String schoolNameLoader() {
        String schoolName;
        int selection = setRandom(SCHOOL_NAME_SELECTION_LOWER_LIMIT, SCHOOL_NAME_SELECTION_UPPER_LIMIT);
        Object object;
        try {
            object = new JSONParser().parse(new FileReader("src/main/java/Resources/highschool_gen.json"));
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        JSONObject choices = (JSONObject) object;

        if (selection <= SCHOOL_NAME_PLACES_WEIGHT) {
            Object names1 = choices.get("Place1");
            Object names2 = choices.get("Place2");
            JSONArray names_1 = (JSONArray) names1;
            JSONArray names_2 = (JSONArray) names2;

            selection = setRandom(0, names_1.size() - 1);
            schoolName = names_1.get(selection).toString();

            selection = setRandom(0, names_2.size() - 1);
            schoolName = schoolName + " " + names_2.get(selection).toString();
        } else if (selection <= SCHOOL_NAME_SINGLE_WEIGHT) {
            Object names1 = choices.get("Single");
            JSONArray names_1 = (JSONArray) names1;
            selection = setRandom(0, names_1.size() - 1);
            schoolName = names_1.get(selection).toString();
        } else {
            Object names3 = choices.get("Name");
            Object names4 = choices.get("MiddleInitial");
            Object names5 = choices.get("LastName");
            JSONArray names_3 = (JSONArray) names3;
            JSONArray names_4 = (JSONArray) names4;
            JSONArray names_5 = (JSONArray) names5;

            selection = setRandom(0, names_3.size() - 1);
            schoolName = names_3.get(selection).toString();

            selection = setRandom(0, names_4.size() - 1);
            schoolName = schoolName + " " + names_4.get(selection).toString();

            selection = setRandom(0, names_5.size() - 1);
            schoolName = schoolName + " " + names_5.get(selection).toString();
        }

        return schoolName + " High School";
    }

    // Weighted chance of random mascot
    private String schoolMascotLoader() {
        String pathCSVMascots = "src/main/java/Resources/mascots.csv";
        List<String> mascots = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        List<Integer> cumulativeCounts = new ArrayList<>();
        int totalSum = 0;
        int randomIdx;
        int insertPoint;

        try (BufferedReader br = new BufferedReader(new FileReader(pathCSVMascots))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                mascots.add(values[0]);
                counts.add(Integer.parseInt(values[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        for (int count : counts) {
            totalSum += count;
            cumulativeCounts.add(totalSum);
        }

        randomIdx = setRandom(0, totalSum + 1);
        insertPoint = Collections.binarySearch(cumulativeCounts, randomIdx);
        if (insertPoint < 0) {
            insertPoint = -insertPoint - 1;
        }
        return mascots.get(insertPoint);
    }

    public String getSchoolMascot() {
        return this.schoolMascot;
    }

    public Bathroom[] getBathrooms() {
        return bathrooms;
    }

    public String getSchoolFoundedYear() {
        return this.schoolFoundedYear;
    }

    public void setSchoolFoundedYear() {
        this.schoolFoundedYear = schoolFoundedYearLoader();
    }

    private String schoolFoundedYearLoader() {
        int year;
        double random = Math.random() * 100;

        if (random < SCHOOL_FOUNDED_YEAR_2000s_CHANCE) {
            // 21% chance: Built after 2000
            year = setRandom(SCHOOL_FOUNDED_2000s_LOWER_LIMIT, STARTING_YEAR);
        } else if (random < SCHOOL_FOUNDED_YEAR_1960s_CHANCE) {
            // 13% chance: Built in the 1960s
            year = setRandom(SCHOOL_FOUNDED_1960s_LOWER_LIMIT, SCHOOL_FOUNDED_1960s_UPPER_LIMIT);
        } else if (random < SCHOOL_FOUNDED_YEAR_1950s_CHANCE) {
            // 12% chance: Built before the 1950s
            year = setRandom(SCHOOL_FOUNDED_1950s_LOWER_LIMIT, SCHOOL_FOUNDED_1950s_UPPER_LIMIT);
        } else {
            // Remaining 54% chance: Built between 1950 and 2000
            year = setRandom(SCHOOL_FOUNDED_OTHER_LOWER_LIMIT, SCHOOL_FOUNDED_OTHER_UPPER_LIMIT);
        }

        return String.valueOf(year);
    }

    @Override
    public void setBathrooms(int number, GameView view) {
        bathrooms = new Bathroom[number];
        view.appendOutput("   Generating " + number + " bathrooms...");
        for (int i = 0; i < number; i++) {
            bathrooms[i] = new Bathroom();
            int cap = setRandom(BATHROOM_CAPACITY_LOWER_LIMIT, BATHROOM_CAPACITY_UPPER_LIMIT);
            if (i % 2 == 0) {
                bathrooms[i].setRoomName("Female_Bathroom" + i);
                view.appendOutput("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(true, false);
                bathrooms[i].setConnections(BATHROOM_CONNECTION_COUNT);
                bathrooms[i].setDoors(BATHROOM_CONNECTION_COUNT);
                bathrooms[i].setWindowCount(setRandom(BATHROOM_WINDOW_LOWER_COUNT, BATHROOM_WINDOW_UPPER_COUNT));
                bathrooms[i].setStudentCap(cap);
                bathrooms[i].setInitialStaff(BATHROOM_INITIAL_STAFF);
                bathrooms[i].setStallNumber(setRandom(STALL_NUMBER_LOWER_LIMIT, STALL_NUMBER_UPPER_LIMIT));
                bathrooms[i].setSeatArrangement();
                bathrooms[i].setRoomNumber("WC" + i + setRandom(BATHROOM_NUMBER_LOWER_LIMIT, BATHROOM_NUMBER_UPPER_LIMIT));
            } else {
                bathrooms[i].setRoomName("Male_Bathroom" + i);
                view.appendOutput("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(false, true);
                bathrooms[i].setConnections(BATHROOM_CONNECTION_COUNT);
                bathrooms[i].setDoors(BATHROOM_CONNECTION_COUNT);
                bathrooms[i].setWindowCount(setRandom(BATHROOM_WINDOW_LOWER_COUNT, BATHROOM_WINDOW_UPPER_COUNT));
                bathrooms[i].setStudentCap(cap);
                bathrooms[i].setStallNumber(setRandom(STALL_NUMBER_LOWER_LIMIT, STALL_NUMBER_UPPER_LIMIT));
                bathrooms[i].setInitialStaff(BATHROOM_INITIAL_STAFF);
                bathrooms[i].setSeatArrangement();
                bathrooms[i].setRoomNumber("WC" + i + setRandom(BATHROOM_NUMBER_LOWER_LIMIT, BATHROOM_NUMBER_UPPER_LIMIT));
            }
            bathrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
        }
    }

    public Breakroom[] getBreakrooms() {
        return breakrooms;
    }

    @Override
    public void setBreakrooms(int number, GameView view) {
        breakrooms = new Breakroom[number];
        view.appendOutput("   Generating " + number + " breakroom(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(BREAKROOM_CONNECTION_LOWER_LIMIT, BREAKROOM_CONNECTION_UPPER_LIMIT);
            breakrooms[i] = new Breakroom();
            breakrooms[i].setRoomName("Breakroom" + i);
            view.appendOutput("      Generating " + breakrooms[i].getRoomName());
            breakrooms[i].setStudentRestriction(true);
            breakrooms[i].setConnections(connectN);
            breakrooms[i].setDoors(connectN);
            breakrooms[i].setWindowCount(setRandom(BREAKROOM_WINDOW_LOWER_COUNT, BREAKROOM_WINDOW_UPPER_COUNT));
            breakrooms[i].setInitialStaff(setRandom(BREAKROOM_INITIAL_STAFF_LOWER_LIMIT, BREAKROOM_INITIAL_STAFF_UPPER_LIMIT));
            breakrooms[i].setStudentCap(BREAKROOM_STUDENT_CAPACITY);
            breakrooms[i].setSeatArrangement();
            breakrooms[i].setRoomNumber("B" + i + setRandom(BREAKROOM_NUMBER_LOWER_LIMIT, BREAKROOM_NUMBER_UPPER_LIMIT));
            breakrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
        }
    }

    public Classroom[] getClassrooms() {
        return classrooms;
    }

    @Override
    public void setClassrooms(int number, GameView view) {
        int decision;
        classrooms = new Classroom[number];
        view.appendOutput("   Generating " + number + " classrooms...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(CLASSROOM_CONNECTION_LOWER_LIMIT, CLASSROOM_CONNECTION_UPPER_LIMIT);
            decision = i % CLASSROOM_WEIGHT;
            classrooms[i] = new Classroom();
            classrooms[i].setRoomName("Classroom" + i);
            view.appendOutput("      Generating " + classrooms[i].getRoomName());
            classrooms[i].setConnections(connectN);
            classrooms[i].setDoors(connectN);
            classrooms[i].setClassroomType(decision);
            classrooms[i].setInitialStaff(CLASSROOM_INITIAL_STAFF);
            classrooms[i].setStudentCap(setRandom(CLASSROOM_STUDENT_CAPACITY_LOWER_LIMIT, CLASSROOM_STUDENT_CAPACITY_UPPER_LIMIT));
            classrooms[i].setSeatArrangement();
            classrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            classrooms[i].setRoomNumber(classrooms[i].getClassRoomType() + i + setRandom(CLASSROOM_NUMBER_LOWER_LIMIT, CLASSROOM_NUMBER_UPPER_LIMIT));
        }
    }

    public ComputerLab[] getComputerLabs() {
        return computerLabs;
    }

    @Override
    public void setComputerLabs(int number, GameView view) {
        computerLabs = new ComputerLab[number];
        view.appendOutput("   Generating " + number + " Computer lab(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(COMPUTER_CONNECTION_LOWER_LIMIT, COMPUTER_CONNECTION_UPPER_LIMIT);
            computerLabs[i] = new ComputerLab();
            computerLabs[i].setRoomName("ComputerLab" + i);
            view.appendOutput("      Generating " + computerLabs[i].getRoomName());
            computerLabs[i].setWindowCount(COMPUTER_WINDOW_COUNT);
            computerLabs[i].setConnections(connectN);
            computerLabs[i].setDoors(connectN);
            computerLabs[i].setInitialStaff(setRandom(COMPUTER_INITIAL_STAFF_LOWER_LIMIT, COMPUTER_INITIAL_STAFF_UPPER_LIMIT));
            computerLabs[i].setStudentCap(setRandom(COMPUTER_STUDENT_CAPACITY_LOWER_LIMIT, COMPUTER_STUDENT_CAPACITY_UPPER_LIMIT));
            computerLabs[i].setSeatArrangement();
            computerLabs[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            computerLabs[i].setRoomNumber("COM" + i);
        }
    }

    public Courtyard[] getCourtyards() {
        return courtyards;
    }

    @Override
    public void setCourtyards(int number, GameView view) {
        courtyards = new Courtyard[number];
        view.appendOutput("   Generating " + number + " courtyard(s)...");
        for (int i = 0; i < number; i++) {
            courtyards[i] = new Courtyard();
            courtyards[i].setRoomName("Courtyard" + i);
            view.appendOutput("      Generating " + courtyards[i].getRoomName());
            courtyards[i].setWindowCount(COURTYARD_WINDOW_COUNT);
            courtyards[i].setConnections(COURTYARD_CONNECTION_COUNT);
            courtyards[i].setDoors(COURTYARD_CONNECTION_COUNT);
            courtyards[i].setInitialStaff(COURTYARD_INITIAL_STAFF);
            courtyards[i].setStudentCap(setRandom(COURTYARD_STUDENT_CAPACITY_LOWER_LIMIT, COURTYARD_STUDENT_CAPACITY_UPPER_LIMIT));
            courtyards[i].setSeatArrangement();
            courtyards[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            courtyards[i].setRoomNumber("C" + i);
        }
    }

    public Gym[] getGyms() {
        return gyms;
    }

    @Override
    public void setGyms(int number, GameView view) {
        gyms = new Gym[number];
        view.appendOutput("   Generating " + number + " gym(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(GYM_CONNECTION_LOWER_LIMIT, GYM_CONNECTION_UPPER_LIMIT);
            gyms[i] = new Gym();
            gyms[i].setRoomName("Gym" + i);
            view.appendOutput("      Generating " + gyms[i].getRoomName());
            gyms[i].setConnections(connectN);
            gyms[i].setDoors(connectN);
            gyms[i].setWindowCount(setRandom(GYM_WINDOW_LOWER_LIMIT, GYM_WINDOW_UPPER_LIMIT));
            gyms[i].setInitialStaff(GYM_INITIAL_STAFF);
            gyms[i].setStudentCap(setRandom(GYM_STUDENT_CAPACITY_LOWER_LIMIT, GYM_STUDENT_CAPACITY_UPPER_LIMIT));
            gyms[i].setSeatArrangement();
            gyms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            gyms[i].setRoomNumber("G" + i + setRandom(GYM_ROOM_NUMBER_LOWER_LIMIT, GYM_ROOM_NUMBER_UPPER_LIMIT));
        }
    }

    public void renameGym(Gym gym, String name) {
        gym.setRoomName(name);
    }

    public Hallway[] getHallways() {
        return hallways;
    }

    @Override
    public void setHallways(int number, GameView view) {
        hallways = new Hallway[number];
        view.appendOutput("   Generating " + number + " hallways...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(HALLWAY_CONNECTION_LOWER_LIMIT, HALLWAY_CONNECTION_UPPER_LIMIT);
            hallways[i] = new Hallway();
            hallways[i].setRoomName("Hallway" + i);
            view.appendOutput("      Generating " + hallways[i].getRoomName());
            hallways[i].setConnections(connectN);
            hallways[i].setDoors(connectN);
            hallways[i].setWindowCount(setRandom(HALLWAY_WINDOW_LOWER_LIMIT, HALLWAY_WINDOW_UPPER_LIMIT));
            hallways[i].setInitialStaff(HALLWAY_INITIAL_STAFF);
            hallways[i].setStudentCap(setRandom(HALLWAY_STUDENT_CAPACITY_LOWER_LIMIT, HALLWAY_STUDENT_CAPACITY_UPPER_LIMIT));
            hallways[i].setSeatArrangement();
            hallways[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            hallways[i].setRoomNumber("H" + i);
        }
    }

    public LibraryR[] getLibraries() {
        return libraries;
    }

    @Override
    public void setLibraries(int number, GameView view) {
        libraries = new LibraryR[number];
        view.appendOutput("   Generating " + number + " libraries...");
        for (int i = 0; i < number; i++) {
            int connectorN = setRandom(LIBRARY_CONNECTION_LOWER_LIMIT, LIBRARY_CONNECTION_UPPER_LIMIT);
            libraries[i] = new LibraryR();
            libraries[i].setRoomName("Library" + i);
            view.appendOutput("      Generating " + libraries[i].getRoomName());
            libraries[i].setWindowCount(setRandom(LIBRARY_WINDOW_LOWER_LIMIT, LIBRARY_WINDOW_UPPER_LIMIT));
            libraries[i].setConnections(connectorN);
            libraries[i].setDoors(connectorN);
            libraries[i].setInitialStaff(LIBRARY_INITIAL_STAFF);
            libraries[i].setStudentCap(setRandom(LIBRARY_STUDENT_CAPACITY_LOWER_LIMIT, LIBRARY_STUDENT_CAPACITY_UPPER_LIMIT));
            libraries[i].setSeatArrangement();
            libraries[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            libraries[i].setRoomNumber("L" + i + setRandom(LIBRARY_NUMBER_LOWER_LIMIT, LIBRARY_NUMBER_UPPER_LIMIT));
        }
    }

    public void renameLibrary(LibraryR libraryR, String name) {
        libraryR.setRoomName(name);
    }

    public Lunchroom[] getLunchrooms() {
        return lunchrooms;
    }

    @Override
    public void setLunchrooms(int number, GameView view) {
        lunchrooms = new Lunchroom[number];
        view.appendOutput("   Generating " + number + " lunchroom(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(LUNCH_CONNECTION_LOWER_LIMIT, LUNCH_CONNECTION_UPPER_LIMIT);
            lunchrooms[i] = new Lunchroom();
            lunchrooms[i].setRoomName("Lunchroom" + i);
            view.appendOutput("      Generating " + lunchrooms[i].getRoomName());
            lunchrooms[i].setWindowCount(setRandom(LUNCH_WINDOW_LOWER_LIMIT, LUNCH_WINDOW_UPPER_LIMIT));
            lunchrooms[i].setConnections(connectN);
            lunchrooms[i].setDoors(connectN);
            lunchrooms[i].setInitialStaff(setRandom(LUNCH_INITIAL_STAFF_LOWER_LIMIT, LUNCH_INITIAL_STAFF_UPPER_LIMIT));
            lunchrooms[i].setStudentCap(setRandom(LUNCH_STUDENT_CAPACITY_LOWER_LIMIT, LUNCH_STUDENT_CAPACITY_UPPER_LIMIT));
            lunchrooms[i].setSeatArrangement();
            lunchrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            lunchrooms[i].setRoomNumber("L" + i + setRandom(LUNCH_NUMBER_LOWER_LIMIT, LUNCH_NUMBER_UPPER_LIMIT));
        }
    }

    public Office[] getOffices() {
        return offices;
    }

    @Override
    public void setOffices(int number, GameView view) {
        offices = new Office[number];
        view.appendOutput("   Generating " + number + " offices...");
        for (int i = 0; i < number; i++) {
            offices[i] = new Office();
            switch (i) {
                case 0 -> {
                    offices[i].setRoomName("Principal's Office");
                    view.appendOutput("      Generating Principal's office");
                    offices[i].setDoors(PRINCIPAL_CONNECTION_COUNT);
                    offices[i].setWindowCount(PRINCIPAL_WINDOW_COUNT);
                    offices[i].setInitialStaff(PRINCIPAL_INITIAL_STAFF);
                    offices[i].setInitialStudents(PRINCIPAL_INITIAL_STUDENTS);
                    offices[i].setStudentCap(PRINCIPAL_STUDENT_CAP);
                    offices[i].setConnections(PRINCIPAL_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-100");
                }
                case 1 -> {
                    offices[i].setRoomName("Vice Principal's Office");
                    view.appendOutput("      Generating Vice Principal's office");
                    offices[i].setDoors(VICE_PRINCIPAL_CONNECTION_COUNT);
                    offices[i].setWindowCount(VICE_PRINCIPAL_WINDOW_COUNT);
                    offices[i].setInitialStaff(VICE_PRINCIPAL_INITIAL_STAFF);
                    offices[i].setInitialStudents(VICE_PRINCIPAL_INITIAL_STUDENTS);
                    offices[i].setStudentCap(VICE_PRINCIPAL_STUDENT_CAP);
                    offices[i].setConnections(VICE_PRINCIPAL_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-101");
                }
                case 2 -> {
                    offices[i].setRoomName("Guidance Councilor's Office");
                    view.appendOutput("      Generating Councilor's Office");
                    offices[i].setDoors(GUIDANCE_CONNECTION_COUNT);
                    offices[i].setWindowCount(GUIDANCE_WINDOW_COUNT);
                    offices[i].setInitialStaff(GUIDANCE_INITIAL_STAFF);
                    offices[i].setInitialStudents(GUIDANCE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(GUIDANCE_STUDENT_CAP);
                    offices[i].setConnections(GUIDANCE_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-102");
                }
                case 3 -> {
                    offices[i].setRoomName("Front Office");
                    view.appendOutput("      Generating Front Office");
                    offices[i].setDoors(FRONT_OFFICE_CONNECTION_COUNT);
                    offices[i].setWindowCount(FRONT_OFFICE_WINDOW_COUNT);
                    offices[i].setInitialStaff(FRONT_OFFICE_INITIAL_STAFF);
                    offices[i].setInitialStudents(FRONT_OFFICE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(FRONT_OFFICE_STUDENT_CAP);
                    offices[i].setConnections(FRONT_OFFICE_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-103");
                }
                case 4 -> {
                    offices[i].setRoomName("Nurse's Office");
                    view.appendOutput("      Generating Nurse's Office");
                    offices[i].setDoors(NURSE_CONNECTION_COUNT);
                    offices[i].setWindowCount(NURSE_WINDOW_COUNT);
                    offices[i].setInitialStaff(NURSE_INITIAL_STAFF);
                    offices[i].setInitialStudents(NURSE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(NURSE_STUDENT_CAP);
                    offices[i].setConnections(NURSE_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-104");
                }
                case 5 -> {
                    offices[i].setRoomName("Guidance Councilor's Office");
                    view.appendOutput("      Generating Councilor's Office");
                    offices[i].setDoors(GUIDANCE_CONNECTION_COUNT);
                    offices[i].setWindowCount(GUIDANCE_WINDOW_COUNT);
                    offices[i].setInitialStaff(GUIDANCE_INITIAL_STAFF);
                    offices[i].setInitialStudents(GUIDANCE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(GUIDANCE_STUDENT_CAP);
                    offices[i].setConnections(GUIDANCE_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-103");
                }
                default -> {
                    offices[i].setRoomName("Office" + i);
                    view.appendOutput("      Generating " + offices[i].getRoomName());
                    offices[i].setConnections(DEFAULT_OFFICE_CONNECTION_COUNT);
                    offices[i].setDoors(DEFAULT_OFFICE_CONNECTION_COUNT);
                    offices[i].setWindowCount(DEFAULT_OFFICE_WINDOW_COUNT);
                    offices[i].setInitialStaff(DEFAULT_OFFICE_INITIAL_STAFF);
                    offices[i].setInitialStudents(DEFAULT_OFFICE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(setRandom(DEFAULT_OFFICE_STUDENT_CAP_LOWER_LIMIT, DEFAULT_OFFICE_STUDENT_CAP_UPPER_LIMIT));
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O" + "-1" + i);
                }
            }
            offices[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            offices[i].setStudentRestriction(true);

        }
    }

    public UtilityRoom[] getUtilityrooms() {
        return utilityrooms;
    }

    public ArtStudio[] getArtStudios() {
        return artStudios;
    }

    @Override
    public void setArtStudios(int number, GameView view) {
        artStudios = new ArtStudio[number];
        view.appendOutput("   Generating " + number + " art studio(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(ART_CONNECTION_LOWER_LIMIT, ART_CONNECTION_UPPER_LIMIT);
            artStudios[i] = new ArtStudio();
            artStudios[i].setRoomName("Art Room" + i);
            view.appendOutput("      Generating " + artStudios[i].getRoomName());
            artStudios[i].setWindowCount(setRandom(ART_WINDOW_LOWER_LIMIT, ART_WINDOW_UPPER_LIMIT));
            artStudios[i].setConnections(connectN);
            artStudios[i].setDoors(connectN);
            artStudios[i].setInitialStaff(setRandom(ART_INITIAL_STAFF_LOWER_LIMIT, ART_INITIAL_STAFF_UPPER_LIMIT));
            artStudios[i].setStudentCap(setRandom(ART_STUDENT_CAPACITY_LOWER_LIMIT, ART_STUDENT_CAPACITY_UPPER_LIMIT));
            artStudios[i].setSeatArrangement();
            artStudios[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            artStudios[i].setRoomNumber("AT" + i + setRandom(ART_NUMBER_LOWER_LIMIT, ART_NUMBER_UPPER_LIMIT));
        }
    }

    public AthleticField[] getAthleticFields() {
        return athleticFields;
    }

    @Override
    public void setAthleticFields(int number, GameView view) {
        athleticFields = new AthleticField[number];
        view.appendOutput("   Generating " + number + " athletic fields(s)...");
        for (int i = 0; i < number; i++) {
            athleticFields[i] = new AthleticField();
            athleticFields[i].setRoomName("Field" + i);
            view.appendOutput("      Generating " + athleticFields[i].getRoomName());
            athleticFields[i].setWindowCount(ATHLETIC_WINDOW_COUNT);
            athleticFields[i].setConnections(ATHLETIC_CONNECTION_COUNT);
            athleticFields[i].setDoors(ATHLETIC_CONNECTION_COUNT);
            athleticFields[i].setInitialStaff(setRandom(ATHLETIC_INITIAL_STAFF_LOWER_LIMIT, ATHLETIC_INITIAL_STAFF_UPPER_LIMIT));
            athleticFields[i].setStudentCap(setRandom(ATHLETIC_STUDENT_CAPACITY_LOWER_LIMIT, ATHLETIC_STUDENT_CAPACITY_UPPER_LIMIT));
            athleticFields[i].setSeatArrangement();
            athleticFields[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            athleticFields[i].setRoomNumber("F" + i + setRandom(ATHLETIC_NUMBER_LOWER_LIMIT, ATHLETIC_NUMBER_UPPER_LIMIT));
        }
    }

    public Auditorium[] getAuditoriums() {
        return auditoriums;
    }

    @Override
    public void setAuditoriums(int number, GameView view) {
        auditoriums = new Auditorium[number];
        view.appendOutput("   Generating " + number + " auditorium(s)...");
        for (int i = 0; i < number; i++) {
            auditoriums[i] = new Auditorium();
            auditoriums[i].setRoomName("Auditorium" + i);
            view.appendOutput("      Generating " + auditoriums[i].getRoomName());
            auditoriums[i].setWindowCount(AUDITORIUM_WINDOW_COUNT);
            auditoriums[i].setConnections(AUDITORIUM_CONNECTION_COUNT);
            auditoriums[i].setDoors(AUDITORIUM_CONNECTION_COUNT);
            auditoriums[i].setInitialStaff(AUDITORIUM_INITIAL_STAFF);
            auditoriums[i].setStudentCap(setRandom(AUDITORIUM_STUDENT_CAPACITY_LOWER_LIMIT, AUDITORIUM_STUDENT_CAPACITY_UPPER_LIMIT));
            auditoriums[i].setSeatArrangement();
            auditoriums[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            auditoriums[i].setRoomNumber("AD" + i + setRandom(AUDITORIUM_NUMBER_LOWER_LIMIT, AUDITORIUM_NUMBER_UPPER_LIMIT));
        }
    }

    public DramaRoom[] getDramaRooms() {
        return dramaRooms;
    }

    @Override
    public void setDramaRooms(int number, GameView view) {
        dramaRooms = new DramaRoom[number];
        view.appendOutput("   Generating " + number + " Drama Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(DRAMA_CONNECTION_LOWER_LIMIT, DRAMA_CONNECTION_UPPER_LIMIT);
            dramaRooms[i] = new DramaRoom();
            dramaRooms[i].setRoomName("Drama" + i);
            view.appendOutput("      Generating " + dramaRooms[i].getRoomName());
            dramaRooms[i].setWindowCount(setRandom(DRAMA_WINDOW_LOWER_LIMIT, DRAMA_WINDOW_UPPER_LIMIT));
            dramaRooms[i].setConnections(connectN);
            dramaRooms[i].setDoors(connectN);
            dramaRooms[i].setInitialStaff(DRAMA_INITIAL_STAFF);
            dramaRooms[i].setStudentCap(setRandom(DRAMA_STUDENT_CAPACITY_LOWER_LIMIT, DRAMA_STUDENT_CAPACITY_UPPER_LIMIT));
            dramaRooms[i].setSeatArrangement();
            dramaRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            dramaRooms[i].setRoomNumber("D" + i + setRandom(DRAMA_NUMBER_LOWER_LIMIT, DRAMA_NUMBER_UPPER_LIMIT));
        }
    }

    public LockerRoom[] getLockerRooms() {
        return lockerRooms;
    }

    @Override
    public void setLockerRooms(int number, GameView view) {
        lockerRooms = new LockerRoom[number];
        view.appendOutput("   Generating " + number + " Locker Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(LOCKER_CONNECTION_LOWER_LIMIT, LOCKER_CONNECTION_UPPER_LIMIT);
            lockerRooms[i] = new LockerRoom();
            lockerRooms[i].setRoomName("Locker Room" + i);
            view.appendOutput("      Generating " + lockerRooms[i].getRoomName());
            lockerRooms[i].setWindowCount(LOCKER_WINDOW_COUNT);
            lockerRooms[i].setConnections(connectN);
            lockerRooms[i].setDoors(connectN);
            lockerRooms[i].setInitialStaff(LOCKER_INITIAL_STAFF);
            lockerRooms[i].setStudentCap(setRandom(LOCKER_STUDENT_CAPACITY_LOWER_LIMIT, LOCKER_STUDENT_CAPACITY_UPPER_LIMIT));
            lockerRooms[i].setSeatArrangement();
            lockerRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            lockerRooms[i].setRoomNumber("LK" + i + setRandom(LOCKER_NUMBER_LOWER_LIMIT, LOCKER_NUMBER_UPPER_LIMIT));
        }
    }

    public MusicRoom[] getMusicRooms() {
        return musicRooms;
    }

    @Override
    public void setMusicRooms(int number, GameView view) {
        musicRooms = new MusicRoom[number];
        view.appendOutput("   Generating " + number + " Music Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(MUSIC_CONNECTION_LOWER_LIMIT, MUSIC_CONNECTION_UPPER_LIMIT);
            musicRooms[i] = new MusicRoom();
            musicRooms[i].setRoomName("Music Room" + i);
            view.appendOutput("      Generating " + musicRooms[i].getRoomName());
            musicRooms[i].setWindowCount(setRandom(MUSIC_WINDOW_LOWER_LIMIT, MUSIC_WINDOW_UPPER_LIMIT));
            musicRooms[i].setConnections(connectN);
            musicRooms[i].setDoors(connectN);
            musicRooms[i].setInitialStaff(MUSIC_INITIAL_STAFF);
            musicRooms[i].setStudentCap(setRandom(MUSIC_STUDENT_CAPACITY_LOWER_LIMIT, MUSIC_STUDENT_CAPACITY_UPPER_LIMIT));
            musicRooms[i].setSeatArrangement();
            musicRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            musicRooms[i].setRoomNumber("MR" + i + setRandom(MUSIC_NUMBER_LOWER_LIMIT, MUSIC_NUMBER_UPPER_LIMIT));
        }
    }

    public ScienceLab[] getScienceLabs() {
        return scienceLabs;
    }

    @Override
    public void setScienceLabs(int number, GameView view) {
        scienceLabs = new ScienceLab[number];
        view.appendOutput("   Generating " + number + " Science Lab(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(SCIENCE_LAB_CONNECTION_LOWER_LIMIT, SCIENCE_LAB_CONNECTION_UPPER_LIMIT);
            scienceLabs[i] = new ScienceLab();
            scienceLabs[i].setRoomName("Science Lab" + i);
            view.appendOutput("      Generating " + scienceLabs[i].getRoomName());
            scienceLabs[i].setWindowCount(SCIENCE_LAB_WINDOW_COUNT);
            scienceLabs[i].setConnections(connectN);
            scienceLabs[i].setDoors(connectN);
            scienceLabs[i].setInitialStaff(SCIENCE_LAB_INITIAL_STAFF);
            scienceLabs[i].setStudentCap(setRandom(SCIENCE_LAB_STUDENT_CAPACITY_LOWER_LIMIT, SCIENCE_LAB_STUDENT_CAPACITY_UPPER_LIMIT));
            scienceLabs[i].setSeatArrangement();
            scienceLabs[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            scienceLabs[i].setRoomNumber("Lab" + i + setRandom(SCIENCE_LAB_NUMBER_LOWER_LIMIT, SCIENCE_LAB_NUMBER_UPPER_LIMIT));
        }
    }

    public void setStudentGradeClass(HashMap<Integer, Student> studentHashMap, GameView view) {
        Integer f_count = 0;
        Integer s_count = 0;
        Integer j_count = 0;
        Integer sr_count = 0;
        // TODO: investigate placing og key vs new counter for performance
        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            String grade = entry.getValue().studentStatistics.getGradeLevel();
            switch (grade) {
                case "Freshman" -> {
                    freshmanClass.put(f_count, entry.getValue());
                    f_count++;
                }
                case "Sophomore" -> {
                    sophomoreClass.put(s_count, entry.getValue());
                    s_count++;
                }
                case "Junior" -> {
                    juniorClass.put(j_count, entry.getValue());
                    j_count++;
                }
                case "Senior" -> {
                    seniorClass.put(sr_count, entry.getValue());
                    sr_count++;
                }
                default -> view.appendOutput("Can't find student class");
            }
        }
    }

    public HashMap<Integer, Student> getStudentGradeClass(String gradClass) {
        switch (gradClass) {
            case "Freshman":
                return freshmanClass;
            case "Sophomore":
                return sophomoreClass;
            case "Junior":
                return juniorClass;
            case "Senior":
                return seniorClass;
            default:
                break;
        }
        return null;
    }

    public ConferenceRoom[] getConferenceRooms() {
        return conferenceRooms;
    }

    public void setConferenceRooms(int number, GameView view) {
        conferenceRooms = new ConferenceRoom[number];
        view.appendOutput("   Generating " + number + " Conference Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(CONFERENCE_CONNECTION_LOWER_LIMIT, CONFERENCE_CONNECTION_UPPER_LIMIT);
            conferenceRooms[i] = new ConferenceRoom();
            conferenceRooms[i].setRoomName("Conference Room" + i);
            view.appendOutput("      Generating " + conferenceRooms[i].getRoomName());
            conferenceRooms[i].setWindowCount(setRandom(CONFERENCE_WINDOW_LOWER_LIMIT, CONFERENCE_WINDOW_UPPER_LIMIT));
            conferenceRooms[i].setConnections(connectN);
            conferenceRooms[i].setDoors(connectN);
            conferenceRooms[i].setInitialStaff(CONFERENCE_INITIAL_STAFF);
            conferenceRooms[i].setStudentCap(setRandom(CONFERENCE_STUDENT_CAPACITY_LOWER_LIMIT, CONFERENCE_STUDENT_CAPACITY_UPPER_LIMIT));
            conferenceRooms[i].setSeatArrangement();
            conferenceRooms[i].setRoomNumber("Conference" + i + setRandom(CONFERENCE_NUMBER_LOWER_LIMIT, CONFERENCE_NUMBER_UPPER_LIMIT));
        }
    }

    public VocationalRoom[] getVocationalRooms() {
        return vocationalRooms;
    }

    public void setVocationalRooms(int number, GameView view) {
        vocationalRooms = new VocationalRoom[number];
        view.appendOutput("   Generating " + number + " Vocational room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(VOCATIONAL_CONNECTION_LOWER_LIMIT, VOCATIONAL_CONNECTION_UPPER_LIMIT);
            vocationalRooms[i] = new VocationalRoom();
            vocationalRooms[i].setRoomName("Vocational Room" + i);
            view.appendOutput("      Generating " + vocationalRooms[i].getRoomName());
            vocationalRooms[i].setWindowCount(setRandom(VOCATIONAL_WINDOW_LOWER_LIMIT, VOCATIONAL_WINDOW_UPPER_LIMIT));
            vocationalRooms[i].setConnections(connectN);
            vocationalRooms[i].setDoors(connectN);
            vocationalRooms[i].setInitialStaff(VOCATIONAL_INITIAL_STAFF);
            vocationalRooms[i].setStudentCap(setRandom(VOCATIONAL_STUDENT_CAPACITY_LOWER_LIMIT, VOCATIONAL_STUDENT_CAPACITY_UPPER_LIMIT));
            vocationalRooms[i].setSeatArrangement();
            vocationalRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            vocationalRooms[i].setRoomNumber("Vocational" + i + setRandom(VOCATIONAL_NUMBER_LOWER_LIMIT, VOCATIONAL_NUMBER_UPPER_LIMIT));
        }
    }

    public ParkingLot[] getParkingLots() {
        return parkingLots;
    }

    public void setParkingLots(int number, GameView view) {
        parkingLots = new ParkingLot[number];
        view.appendOutput("   Generating " + number + " Parking Lot(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = PARKING_CONNECTION_COUNT;
            parkingLots[i] = new ParkingLot();
            parkingLots[i].setRoomName("Parking Lot" + i);
            view.appendOutput("      Generating " + parkingLots[i].getRoomName());
            parkingLots[i].setWindowCount(PARKING_WINDOW_COUNT);
            parkingLots[i].setConnections(connectN);
            parkingLots[i].setDoors(connectN);
            parkingLots[i].setInitialStaff(PARKING_INITIAL_STAFF);
            parkingLots[i].setStudentCap(setRandom(PARKING_STUDENT_CAPACITY_LOWER_LIMIT, PARKING_STUDENT_CAPACITY_UPPER_LIMIT));
            parkingLots[i].setSeatArrangement();
            parkingLots[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            parkingLots[i].setRoomNumber("ParkingLot" + i + setRandom(PARKING_NUMBER_LOWER_LIMIT, PARKING_NUMBER_UPPER_LIMIT));
        }
    }

    public void schoolColorsLoader() {
        String pathColors = "src/main/java/Resources/colors.txt";
        Map<String, String> colorMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(pathColors))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    colorMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        List<String> colorNames = new ArrayList<>(colorMap.keySet());

        int firstColorIndex = setRandom(0, colorNames.size() - 1);
        int secondColorIndex;

        do {
            secondColorIndex = setRandom(0, colorNames.size() - 1);
        } while (firstColorIndex == secondColorIndex);

        String firstColorName = colorNames.get(firstColorIndex);
        String secondColorName = colorNames.get(secondColorIndex);

        this.schoolColors = new String[]{firstColorName, secondColorName};
        this.schoolColorsHex = new String[]{colorMap.get(firstColorName), colorMap.get(secondColorName)};
    }

    public String[] getSchoolColors() {
        return Arrays.copyOf(schoolColors, schoolColors.length);
    }

    public void setSchoolColors(String[] colors) {
        this.schoolColors = colors;
    }

    public String[] getSchoolColorsHex() {
        return Arrays.copyOf(schoolColorsHex, schoolColorsHex.length);
    }

    public Room getClassroomByStaff(Staff staff) {
        for (Classroom classroom : classrooms) {
            List<Staff> staffList = classroom.getAssignedStaff();
            for (Staff staff1 : staffList) {
                if (staff1.equals(staff)) {
                    return classroom;
                }
            }
        }
        System.out.println("Cant find classroom of " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName());
        return null;
    }

    public Room getRoomByStaff(Staff staff, String roomType) {
        switch (roomType) {
            case "Physical Education" -> {
                for (Gym gym : gyms) {
                    List<Staff> staffList = gym.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return gym;
                        }
                    }
                }
                for (AthleticField field : athleticFields) {
                    List<Staff> staffList = field.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return field;
                        }
                    }
                }
            }
            case "Office" -> {
                for (Office office : offices) {
                    List<Staff> staffList = office.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return office;
                        }
                    }
                }
            }
            case "Visual Arts" -> {
                for (ArtStudio artStudio : artStudios) {
                    List<Staff> staffList = artStudio.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return artStudio;
                        }
                    }
                }
            }
            case "Performing Arts" -> {
                for (DramaRoom dramaRoom : dramaRooms) {
                    List<Staff> staffList = dramaRoom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return dramaRoom;
                        }
                    }
                }
                for (MusicRoom musicRoom : musicRooms) {
                    List<Staff> staffList = musicRoom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return musicRoom;
                        }
                    }
                }
                for (Auditorium auditorium : auditoriums) {
                    List<Staff> staffList = auditorium.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return auditorium;
                        }
                    }
                }
            }
            case "Vocational" -> {
                for (VocationalRoom vocationalRoom : vocationalRooms) {
                    List<Staff> staffList = vocationalRoom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return vocationalRoom;
                        }
                    }
                }
                for (Classroom classroom : classrooms) {
                    List<Staff> staffList = classroom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return classroom;
                        }
                    }
                }
            }
            case "Business" -> {
                for (Classroom classroom : classrooms) {
                    List<Staff> staffList = classroom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return classroom;
                        }
                    }
                }
            }
        }
        return null;
    }

}

