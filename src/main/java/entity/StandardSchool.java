package entity;//*******************************************************************
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class StandardSchool implements SchoolPlan {

    String schoolName;
    String schoolMascot;
    String[] schoolColors;
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

    HashMap<Integer, Student> freshmanClass = new HashMap<>();
    HashMap<Integer, Student> sophomoreClass = new HashMap<>();
    HashMap<Integer, Student> juniorClass = new HashMap<>();
    HashMap<Integer, Student> seniorClass = new HashMap<>();

    @Override
    public void setUtilityRooms(int number) {
        utilityrooms = new UtilityRoom[number];
        System.out.println("   Generating " + number + " utility rooms...");
        for (int i = 0; i < number; i++) {
            utilityrooms[i] = new UtilityRoom();
            utilityrooms[i].setRoomName("UtilityRoom" + i);
            System.out.println("       Generating " + utilityrooms[i].getRoomName());
            utilityrooms[i].setStudentRestriction(true);
            utilityrooms[i].setDoors(2);
            utilityrooms[i].setConnections(2);
            utilityrooms[i].setWindowCount(0);
            utilityrooms[i].setInitialStaff(setRandom(1, 3));
            utilityrooms[i].setRoomCapacity(setRandom(3, 8));
            utilityrooms[i].setRoomNumber("U" + i + setRandom(0, 9));
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

    //TODO: Centralize this
    private int setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public int getTotalStudentCapacity() {
        int class_total = 0;
        int drama_total = 0;
        int music_total = 0;
        int art_total = 0;

        for (Classroom classroom : classrooms) {
            class_total = class_total + classroom.getStudentCapacity();
        }

        for (MusicRoom musicRoom : musicRooms) {
            music_total = music_total + musicRoom.getStudentCapacity();
        }

        for (DramaRoom dramaRoom : dramaRooms) {
            drama_total = drama_total + dramaRoom.getStudentCapacity();
        }

        for (ArtStudio artStudio : artStudios) {
            art_total = art_total + artStudio.getStudentCapacity();
        }

        return class_total + music_total + drama_total + art_total;
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

        class_count = classrooms.length;
        office_count = offices.length / 2;
        maint_count = utilityrooms.length + 2;
        library_count = libraries.length * 2;
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

        total = class_count + office_count + maint_count +
                lunch_count + library_count + gym_count +
                art_count + field_count + drama_count + music_count;
        return total;
    }

    private String schoolNameLoader() {
        String schoolName;
        int selection = setRandom(1, 20);
        Object object;
        try {
            object = new JSONParser().parse(new FileReader("src/main/java/Resources/highschool_gen.json"));
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        JSONObject choices = (JSONObject) object;

        if (selection <= 14) {
            Object names1 = choices.get("Place1");
            Object names2 = choices.get("Place2");
            JSONArray names_1 = (JSONArray) names1;
            JSONArray names_2 = (JSONArray) names2;

            selection = setRandom(0, names_1.size() - 1);
            schoolName = names_1.get(selection).toString();

            selection = setRandom(0, names_2.size() - 1);
            schoolName = schoolName + " " + names_2.get(selection).toString();
        } else if (selection <= 17) {
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
                bathrooms[i].setConnections(2);
                bathrooms[i].setDoors(2);
                bathrooms[i].setWindowCount(setRandom(1, 3));
                bathrooms[i].setRoomCapacity(setRandom(2, 16));
                bathrooms[i].setStallNumber(setRandom(3, 7));
            } else {
                bathrooms[i].setRoomName("Male_Bathroom" + i);
                System.out.println("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(false, true);
                bathrooms[i].setConnections(2);
                bathrooms[i].setDoors(2);
                bathrooms[i].setWindowCount(setRandom(1, 3));
                bathrooms[i].setRoomCapacity(setRandom(2, 18));
                bathrooms[i].setStallNumber(setRandom(2, 5));
                bathrooms[i].setRoomNumber("WC" + i + setRandom(0, 9));
            }
        }
    }

    public Breakroom[] getBreakrooms() {
        return breakrooms;
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
            breakrooms[i].setRoomNumber("B" + i + setRandom(0, 9));
        }
    }

    public Classroom[] getClassrooms() {
        return classrooms;
    }

    @Override
    public void setClassrooms(int number) {
        int WEIGHT = 7;
        int decision;
        classrooms = new Classroom[number];
        System.out.println("   Generating " + number + " classrooms...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(3, 5);
            decision = i % WEIGHT;
            classrooms[i] = new Classroom();
            classrooms[i].setRoomName("Classroom" + i);
            System.out.println("      Generating " + classrooms[i].getRoomName());
            classrooms[i].setConnections(connectN);
            classrooms[i].setDoors(connectN);
            classrooms[i].setClassroomType(decision);
            classrooms[i].setInitialStaff(1);
            classrooms[i].setRoomCapacity(setRandom(20, 45));
            classrooms[i].setRoomNumber(classrooms[i].getClassRoomType() + i + setRandom(0, 99));
        }
    }

    public ComputerLab[] getComputerLabs() {
        return computerLabs;
    }

    @Override
    public void setComputerLabs(int number) {
        computerLabs = new ComputerLab[number];
        System.out.println("   Generating " + number + " Computer lab(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(2, 6);
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

    public Courtyard[] getCourtyards() {
        return courtyards;
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
            courtyards[i].setConnections(16);
            courtyards[i].setDoors(16);
            courtyards[i].setRoomCapacity(setRandom(35, 150));
            courtyards[i].setRoomNumber("C" + i);
        }
    }

    public Gym[] getGyms() {
        return gyms;
    }

    @Override
    public void setGyms(int number) {
        gyms = new Gym[number];
        System.out.println("   Generating " + number + " gym(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(6, 9);
            gyms[i] = new Gym();
            gyms[i].setRoomName("Gym" + i);
            System.out.println("      Generating " + gyms[i].getRoomName());
            gyms[i].setConnections(connectN);
            gyms[i].setDoors(connectN);
            gyms[i].setWindowCount(setRandom(4, 16));
            gyms[i].setRoomCapacity(setRandom(100, 450));
            gyms[i].setInitialStaff(setRandom(2, 4));
            gyms[i].setRoomNumber("G" + i + setRandom(0, 9));
        }
    }

    public void renameGym(Gym gym, String name) {
        gym.setRoomName(name);
    }

    public Hallway[] getHallways() {
        return hallways;
    }

    @Override
    public void setHallways(int number) {
        hallways = new Hallway[number];
        System.out.println("   Generating " + number + " hallways...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(8, 16);
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

    public LibraryR[] getLibraries() {
        return libraries;
    }

    @Override
    public void setLibraries(int number) {
        libraries = new LibraryR[number];
        System.out.println("   Generating " + number + " libraries...");
        for (int i = 0; i < number; i++) {
            int connectorN = setRandom(3, 8);
            libraries[i] = new LibraryR();
            libraries[i].setRoomName("Library" + i);
            System.out.println("      Generating " + libraries[i].getRoomName());
            libraries[i].setWindowCount(setRandom(4, 20));
            libraries[i].setConnections(connectorN);
            libraries[i].setDoors(connectorN);
            libraries[i].setInitialStaff(2);
            libraries[i].setRoomCapacity(setRandom(30, 200));
            libraries[i].setRoomNumber("L" + i + setRandom(0, 9));
        }
    }

    public void renameLibrary(LibraryR libraryR, String name) {
        libraryR.setRoomName(name);
    }

    public Lunchroom[] getLunchrooms() {
        return lunchrooms;
    }

    @Override
    public void setLunchrooms(int number) {
        lunchrooms = new Lunchroom[number];
        System.out.println("   Generating " + number + " lunchroom(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(6, 8);
            lunchrooms[i] = new Lunchroom();
            lunchrooms[i].setRoomName("Lunchroom" + i);
            System.out.println("      Generating " + lunchrooms[i].getRoomName());
            lunchrooms[i].setWindowCount(setRandom(5, 24));
            lunchrooms[i].setConnections(connectN);
            lunchrooms[i].setDoors(connectN);
            lunchrooms[i].setInitialStaff(setRandom(3, 10));
            lunchrooms[i].setRoomCapacity(setRandom(50, 250));
            lunchrooms[i].setRoomNumber("L" + i + setRandom(0, 9));
        }
    }

    public Office[] getOffices() {
        return offices;
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
                offices[i].setDoors(2);
                offices[i].setWindowCount(3);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(6);
                offices[i].setConnections(2);
                offices[i].setRoomNumber("O-100");
            } else if (i == 1) {
                offices[i].setRoomName("Vice Principal's Office");
                System.out.println("      Generating Vice Principal's office");
                offices[i].setDoors(2);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(4);
                offices[i].setConnections(2);
                offices[i].setRoomNumber("O-101");
            } else if (i == 2) {
                offices[i].setRoomName("Guidance Councilor's Office");
                System.out.println("      Generating Councilor's Office");
                offices[i].setDoors(2);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(1);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(6);
                offices[i].setConnections(2);
                offices[i].setRoomNumber("O-102");
            } else if (i == 3) {
                offices[i].setRoomName("Front Office");
                System.out.println("      Generating Front Office");
                offices[i].setDoors(12);
                offices[i].setWindowCount(2);
                offices[i].setInitialStaff(2);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(15);
                offices[i].setConnections(12);
                offices[i].setRoomNumber("O-103");
            } else if (i == 4) {
                offices[i].setRoomName("Nurse's Office");
                System.out.println("      Generating Nurse's Office");
                offices[i].setDoors(3);
                offices[i].setWindowCount(1);
                offices[i].setInitialStaff(2);
                offices[i].setInitialStudents(0);
                offices[i].setRoomCapacity(6);
                offices[i].setConnections(3);
                offices[i].setRoomNumber("O-104");
            } else {
                offices[i].setRoomName("Office" + i);
                System.out.println("      Generating " + offices[i].getRoomName());
                offices[i].setConnections(2);
                offices[i].setDoors(2);
                offices[i].setInitialStaff(1);
                offices[i].setRoomCapacity(setRandom(2, 6));
                offices[i].setRoomNumber("O" + "-1" + i);
            }
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
    public void setArtStudios(int number) {
        artStudios = new ArtStudio[number];
        System.out.println("   Generating " + number + " art studio(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(4, 6);
            artStudios[i] = new ArtStudio();
            artStudios[i].setRoomName("Art Room" + i);
            System.out.println("      Generating " + artStudios[i].getRoomName());
            artStudios[i].setWindowCount(setRandom(5, 10));
            artStudios[i].setConnections(connectN);
            artStudios[i].setDoors(connectN);
            artStudios[i].setInitialStaff(setRandom(1, 2));
            artStudios[i].setRoomCapacity(setRandom(15, 35));
            artStudios[i].setRoomNumber("AT" + i + setRandom(100, 999));
        }
    }

    public AthleticField[] getAthleticFields() {
        return athleticFields;
    }

    @Override
    public void setAthleticFields(int number) {
        athleticFields = new AthleticField[number];
        System.out.println("   Generating " + number + " athletic fields(s)...");
        for (int i = 0; i < number; i++) {
            athleticFields[i] = new AthleticField();
            athleticFields[i].setRoomName("Field" + i);
            System.out.println("      Generating " + athleticFields[i].getRoomName());
            athleticFields[i].setWindowCount(0);
            athleticFields[i].setConnections(16);
            athleticFields[i].setDoors(16);
            athleticFields[i].setInitialStaff(setRandom(0, 2));
            athleticFields[i].setRoomCapacity(setRandom(150, 500));
            athleticFields[i].setRoomNumber("F" + i + setRandom(100, 999));
        }
    }

    public Auditorium[] getAuditoriums() {
        return auditoriums;
    }

    @Override
    public void setAuditoriums(int number) {
        auditoriums = new Auditorium[number];
        System.out.println("   Generating " + number + " auditorium(s)...");
        for (int i = 0; i < number; i++) {
            auditoriums[i] = new Auditorium();
            auditoriums[i].setRoomName("Auditorium" + i);
            System.out.println("      Generating " + auditoriums[i].getRoomName());
            auditoriums[i].setWindowCount(0);
            auditoriums[i].setConnections(16);
            auditoriums[i].setDoors(16);
            auditoriums[i].setInitialStaff(0);
            auditoriums[i].setRoomCapacity(setRandom(150, 550));
            auditoriums[i].setRoomNumber("AD" + i + setRandom(100, 999));
        }
    }

    public DramaRoom[] getDramaRooms() {
        return dramaRooms;
    }

    @Override
    public void setDramaRooms(int number) {
        dramaRooms = new DramaRoom[number];
        System.out.println("   Generating " + number + " Drama Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(2, 6);
            dramaRooms[i] = new DramaRoom();
            dramaRooms[i].setRoomName("Drama" + i);
            System.out.println("      Generating " + dramaRooms[i].getRoomName());
            dramaRooms[i].setWindowCount(setRandom(0, 6));
            dramaRooms[i].setConnections(connectN);
            dramaRooms[i].setDoors(connectN);
            dramaRooms[i].setInitialStaff(1);
            dramaRooms[i].setRoomCapacity(setRandom(15, 50));
            dramaRooms[i].setRoomNumber("D" + i + setRandom(100, 999));
        }
    }

    public LockerRoom[] getLockerRooms() {
        return lockerRooms;
    }

    @Override
    public void setLockerRooms(int number) {
        lockerRooms = new LockerRoom[number];
        System.out.println("   Generating " + number + " Locker Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(2, 6);
            lockerRooms[i] = new LockerRoom();
            lockerRooms[i].setRoomName("Locker Room" + i);
            System.out.println("      Generating " + lockerRooms[i].getRoomName());
            lockerRooms[i].setWindowCount(0);
            lockerRooms[i].setConnections(connectN);
            lockerRooms[i].setDoors(connectN);
            lockerRooms[i].setInitialStaff(0);
            lockerRooms[i].setRoomCapacity(setRandom(30, 90));
            lockerRooms[i].setRoomNumber("LK" + i + setRandom(100, 999));
        }
    }

    public MusicRoom[] getMusicRooms() {
        return musicRooms;
    }

    @Override
    public void setMusicRooms(int number) {
        musicRooms = new MusicRoom[number];
        System.out.println("   Generating " + number + " Music Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(3, 7);
            musicRooms[i] = new MusicRoom();
            musicRooms[i].setRoomName("Music Room" + i);
            System.out.println("      Generating " + musicRooms[i].getRoomName());
            musicRooms[i].setWindowCount(setRandom(0, 4));
            musicRooms[i].setConnections(connectN);
            musicRooms[i].setDoors(connectN);
            musicRooms[i].setInitialStaff(1);
            musicRooms[i].setRoomCapacity(setRandom(30, 90));
            musicRooms[i].setRoomNumber("MR" + i + setRandom(100, 999));
        }
    }

    public ScienceLab[] getScienceLabs() {
        return scienceLabs;
    }

    @Override
    public void setScienceLabs(int number) {
        scienceLabs = new ScienceLab[number];
        System.out.println("   Generating " + number + " Science Lab(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(2, 4);
            scienceLabs[i] = new ScienceLab();
            scienceLabs[i].setRoomName("Science Lab" + i);
            System.out.println("      Generating " + scienceLabs[i].getRoomName());
            scienceLabs[i].setWindowCount(0);
            scienceLabs[i].setConnections(connectN);
            scienceLabs[i].setDoors(connectN);
            scienceLabs[i].setInitialStaff(0);
            scienceLabs[i].setRoomCapacity(setRandom(15, 30));
            scienceLabs[i].setRoomNumber("Lab" + i + setRandom(100, 999));
        }
    }

    public void setStudentGradeClass(HashMap<Integer, Student> studentHashMap) {
        Integer f_count = 0;
        Integer s_count = 0;
        Integer j_count = 0;
        Integer sr_count = 0;
        // TODO: investigate placing og key vs new counter for performance
        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            String grade = entry.getValue().studentStatistics.getGradeLevel();
            switch (grade) {
                case "Freshman":
                    freshmanClass.put(f_count, entry.getValue());
                    f_count++;
                    break;
                case "Sophomore":
                    sophomoreClass.put(s_count, entry.getValue());
                    s_count++;
                    break;
                case "Junior":
                    juniorClass.put(j_count, entry.getValue());
                    j_count++;
                    break;
                case "Senior":
                    seniorClass.put(sr_count, entry.getValue());
                    sr_count++;
                    break;
                default:
                    System.out.println("Can't find student class");
                    break;
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
                System.out.println("Can't find student class");
                break;
        }
        return null;
    }

    public ConferenceRoom[] getConferenceRooms() {
        return conferenceRooms;
    }

    public void setConferenceRooms(int number) {
        conferenceRooms = new ConferenceRoom[number];
        System.out.println("   Generating " + number + " Conference Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(3, 4);
            conferenceRooms[i] = new ConferenceRoom();
            conferenceRooms[i].setRoomName("Conference Room" + i);
            System.out.println("      Generating " + conferenceRooms[i].getRoomName());
            conferenceRooms[i].setWindowCount(setRandom(1, 5));
            conferenceRooms[i].setConnections(connectN);
            conferenceRooms[i].setDoors(connectN);
            conferenceRooms[i].setInitialStaff(0);
            conferenceRooms[i].setRoomCapacity(setRandom(15, 30));
            conferenceRooms[i].setRoomNumber("Conference" + i + setRandom(100, 999));
        }
    }

    public ParkingLot[] getParkingLots() {
        return parkingLots;
    }

    public void setParkingLots(int number) {
        parkingLots = new ParkingLot[number];
        System.out.println("   Generating " + number + " Parking Lot(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = 16;
            parkingLots[i] = new ParkingLot();
            parkingLots[i].setRoomName("Parking Lot" + i);
            System.out.println("      Generating " + parkingLots[i].getRoomName());
            parkingLots[i].setWindowCount(0);
            parkingLots[i].setConnections(connectN);
            parkingLots[i].setDoors(0);
            parkingLots[i].setInitialStaff(0);
            parkingLots[i].setRoomCapacity(setRandom(100, 300));
            parkingLots[i].setRoomNumber("ParkingLot" + i + setRandom(100, 999));
        }
    }

    public void schoolColorsLoader() {
        String pathColors = "src/main/java/Resources/colors.txt";
        List<String> colors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(pathColors))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                colors.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        int firstColor = setRandom(0, colors.size() - 1);
        int secondColor;

        do {
            secondColor = setRandom(0, colors.size() - 1);
        } while (firstColor == secondColor);

        this.schoolColors = new String[]{colors.get(firstColor), colors.get(secondColor)};
    }

    public String[] getSchoolColors() {
        return Arrays.copyOf(schoolColors, schoolColors.length);
    }

    public void setSchoolColors(String[] colors) {
        this.schoolColors = colors;
    }
}

