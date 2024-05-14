package utility;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import entity.Rooms.Bathroom;
import entity.Rooms.Classroom;
import entity.Rooms.Room;
import entity.StandardSchool;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;
import view.GameView;

import javax.swing.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utility.Randomizer.setRandom;


// Procedural generation that builds the school by connecting rooms. Room connection starts
// by connecting hallways and courtyards at random, and then allows other connections to build
// off that backbone. jgrapht connectivity inspector ensures no dangling vertexes
// vertex is door
// edge is room
public class RoomConnector {
    private final Room[][] roomPool = new Room[21][];
    Graph<Room, DefaultEdge> schoolConnect = new Multigraph<>(DefaultEdge.class);
    private int locker_count = 0;
    private int labs_count = 0;

    public RoomConnector(StandardSchool standardSchool, GameView view) {
        roomPool[0] = standardSchool.getArtStudios();
        roomPool[1] = standardSchool.getAthleticFields();
        roomPool[2] = standardSchool.getAuditoriums();
        roomPool[3] = standardSchool.getBathrooms();
        roomPool[4] = standardSchool.getBreakrooms();
        roomPool[5] = standardSchool.getClassrooms();
        roomPool[6] = standardSchool.getComputerLabs();
        roomPool[7] = standardSchool.getCourtyards();
        roomPool[8] = standardSchool.getDramaRooms();
        roomPool[9] = standardSchool.getGyms();
        roomPool[10] = standardSchool.getHallways();
        roomPool[11] = standardSchool.getLibraries();
        roomPool[12] = standardSchool.getLockerRooms();
        roomPool[13] = standardSchool.getLunchrooms();
        roomPool[14] = standardSchool.getMusicRooms();
        roomPool[15] = standardSchool.getOffices();
        roomPool[16] = standardSchool.getScienceLabs();
        roomPool[17] = standardSchool.getUtilityrooms();
        roomPool[18] = standardSchool.getConferenceRooms();
        roomPool[19] = standardSchool.getParkingLots();
        roomPool[20] = standardSchool.getVocationalRooms();

        connectRooms(view);
    }

    private void connectRooms(GameView view) {
        populateVertex();
        constructBackbone();
        connectivityInspectionBackbone();
        populateAthleticFields(view);
        populateParkingLots(view);
        populateAuditoriums(view);
        populateGyms(view);
        populateLunchrooms(view);
        populateLibraries(view);
        populateMusicRooms(view);
        populateArtRooms(view);
        populateDramaRooms(view);
        populateOffices(view);
        populateConferenceRooms(view);
        populateStudentBathrooms(view);
        populateClassrooms(view);
        populateRemainingLabs(view);
        populateComputerLabs(view);
        populateUtilityRooms(view);
        populateBreakrooms(view);
        populateVocationalRooms(view);
        connectivityInspection(view);
    }

    private void populateVertex() {
        for (Room[] rooms : roomPool) {
            if (rooms != null) {
                for (Room room : rooms) {
                    schoolConnect.addVertex(room);
                }
            }
        }
    }

    private void connectivityInspection(GameView view) {
        // Ensure that the school can be traversed
        ConnectivityInspector<Room, DefaultEdge> inspector = new ConnectivityInspector<>(schoolConnect);
        List<Set<Room>> connectedSets = inspector.connectedSets();
        Room connectorRoom = findCentralRoom(view);

        if (connectedSets.size() > 1) {
            for (Set<Room> roomSet : connectedSets) {
                Room roomToConnect = roomSet.stream()
                        .filter(r -> r.getConnections() > 0)
                        .findAny()
                        .orElse(null);
                if (roomToConnect != null && !schoolConnect.containsEdge(roomToConnect, connectorRoom)) {
                    schoolConnect.addEdge(roomToConnect, connectorRoom);
                    roomToConnect.setConnections(roomToConnect.getConnections() - 1);
                    connectorRoom.setConnections(connectorRoom.getConnections() - 1);
                }
                if (connectorRoom.getConnections() == 0) {
                    connectorRoom = findCentralRoom(view);
                }
            }
        }
    }

    // TODO: Allow for injected weight distribution on hallways and courtyards
    // Adjust random weight of hallway or courtyard selection as needed. Might need to re-balance (i.e. 60/40)
    private Room findCentralRoom(GameView view) {
        Room[] hallways = roomPool[10];
        Room[] courtyards = roomPool[7];
        int choice = setRandom(0, 3);
        int count = 0;

        if (choice < 3) {
            do {
                choice = setRandom(0, hallways.length - 1);
                view.appendOutput("Connecting halls...");
                count++;
            } while (hallways[choice].getConnections() == 0 && count < calculateExpectedCycles(hallways.length));
            // Add a connection to a random hallway if no connections are left
            hallways[choice].setConnections(hallways[choice].getConnections() + 1);
            return hallways[choice];
        } else {
            do {
                choice = setRandom(0, courtyards.length - 1);
                view.appendOutput("Connecting courtyards...");
                count++;
            } while (courtyards[choice].getConnections() == 0 && count < calculateExpectedCycles(courtyards.length));
            // Add a connection to a random courtyard if no connections are left
            courtyards[choice].setConnections(courtyards[choice].getConnections() + 1);
            return courtyards[choice];
        }
    }

    // Coupon Collector problem for finding minimum random selections for a set
    private double calculateExpectedCycles(int N) {
        double harmonic_N = 0;

        for (int i = 1; i <= N; i++) {
            harmonic_N += 1.0 / i;
        }

        return N * harmonic_N;
    }

    private void populateEdges() {
        for (int i = 0; i < roomPool.length; i++) {
            for (int j = 0; j < roomPool[i].length; j++) {
                if (roomPool[i][j].getConnections() > 0) {
                    Room targetRoom = getRandomRoom(i, j);
                    while (targetRoom != null && targetRoom.getConnections() == 0) {
                        targetRoom = getRandomRoom(i, j);
                    }
                    if (targetRoom != null) {
                        if (roomPool[i][j] instanceof Bathroom) {
                            targetRoom = getRandomRoom(targetRoom);
                        }
                        schoolConnect.addEdge(roomPool[i][j], targetRoom);
                        roomPool[i][j].setConnections(roomPool[i][j].getConnections() - 1);
                        targetRoom.setConnections(targetRoom.getConnections() - 1);
                    }
                }
            }
        }
    }

    private void populateAthleticFields(GameView view ) {
        Room[] athleticFields = roomPool[1];
        Room[] lockerRooms = roomPool[12];

        for (Room field : athleticFields) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(field, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            field.setConnections(field.getConnections() - 1);
            // Add two locker rooms per athletic field
            schoolConnect.addEdge(field, lockerRooms[locker_count]);
            field.setConnections(field.getConnections() - 1);
            lockerRooms[locker_count].setConnections(lockerRooms[locker_count].getConnections() - 1);
            locker_count++;
            schoolConnect.addEdge(field, lockerRooms[locker_count]);
            field.setConnections(field.getConnections() - 1);
            lockerRooms[locker_count].setConnections(lockerRooms[locker_count].getConnections() - 1);
            locker_count++;
        }
    }

    private void populateParkingLots(GameView view) {
        Room[] parkingLots = roomPool[19];
        Room[] fields = roomPool[1];
        int choice = 0;

        for (Room parkingLot : parkingLots) {
            choice = setRandom(0, 10);
            if (choice < 5) {
                choice = setRandom(0, fields.length - 1);
                schoolConnect.addEdge(fields[choice], parkingLot);
                fields[choice].setConnections(fields[choice].getConnections() - 1);
                parkingLot.setConnections(parkingLot.getConnections() - 1);
            } else {
                Room connectRoom = findCentralRoom(view);
                schoolConnect.addEdge(parkingLot, connectRoom);
                connectRoom.setConnections(connectRoom.getConnections() - 1);
                parkingLot.setConnections(parkingLot.getConnections() - 1);
            }
        }
    }

    private void populateConferenceRooms(GameView view) {
        Room[] conferenceRooms = roomPool[18];
        Room[] offices = roomPool[15];

        Room frontOffice = frontOfficeLocator(offices);
        int choice;

        for (Room conferenceRoom : conferenceRooms) {
            choice = setRandom(0, 5);
            if (choice >= 3 && frontOffice != null) {
                schoolConnect.addEdge(conferenceRoom, frontOffice);
                conferenceRoom.setConnections(conferenceRoom.getConnections() - 1);
                frontOffice.setConnections(frontOffice.getConnections() - 1);
            } else {
                Room connectRoom = findCentralRoom(view);
                schoolConnect.addEdge(connectRoom, conferenceRoom);
                connectRoom.setConnections(connectRoom.getConnections() - 1);
                conferenceRoom.setConnections(conferenceRoom.getConnections() - 1);
            }
        }
    }

    private Room frontOfficeLocator(Room[] offices) {

        Room front = null;

        for (Room office : offices) {
            if (office.getRoomName().equals("Front Office") && office.getConnections() != 0) {
                front = office;
                break;
            }
        }

        return front;
    }

    private void populateAuditoriums(GameView view) {
        Room[] auditoriums = roomPool[2];

        for (Room auditorium : auditoriums) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(auditorium, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            auditorium.setConnections(auditorium.getConnections() - 1);
        }
    }

    private void populateGyms(GameView view) {
        Room[] gyms = roomPool[9];
        Room[] lockerRooms = roomPool[12];

        for (Room gym : gyms) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(gym, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            gym.setConnections(gym.getConnections() - 1);
            // Add two locker rooms per Gym
            schoolConnect.addEdge(gym, lockerRooms[locker_count]);
            gym.setConnections(gym.getConnections() - 1);
            lockerRooms[locker_count].setConnections(lockerRooms[locker_count].getConnections() - 1);
            locker_count++;
            schoolConnect.addEdge(gym, lockerRooms[locker_count]);
            gym.setConnections(gym.getConnections() - 1);
            lockerRooms[locker_count].setConnections(lockerRooms[locker_count].getConnections() - 1);
            locker_count++;
        }
    }

    private void populateLunchrooms(GameView view) {
        Room[] lunchrooms = roomPool[13];

        for (Room lunchroom : lunchrooms) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(lunchroom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            lunchroom.setConnections(lunchroom.getConnections() - 1);
        }
    }

    private void populateLibraries(GameView view) {
        Room[] libraries = roomPool[11];

        for (Room library : libraries) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(library, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            library.setConnections(library.getConnections() - 1);
        }
    }

    private void populateMusicRooms(GameView view) {
        Room[] musicRooms = roomPool[14];

        for (Room musicRoom : musicRooms) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(musicRoom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            musicRoom.setConnections(musicRoom.getConnections() - 1);
        }
    }

    private void populateArtRooms(GameView view) {
        Room[] artRooms = roomPool[0];

        for (Room artRoom : artRooms) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(artRoom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            artRoom.setConnections(artRoom.getConnections() - 1);
        }
    }

    private void populateDramaRooms(GameView view) {
        Room[] dramaRooms = roomPool[8];
        Room[] auditoriums = roomPool[2];
        int chance;

        for (Room dramaRoom : dramaRooms) {
            chance = setRandom(0, 10);
            if (chance < 2) {
                Room selectedAuditorium = auditoriums[setRandom(0, auditoriums.length - 1)];
                schoolConnect.addEdge(dramaRoom, selectedAuditorium);
                dramaRoom.setRoomNumber("A" + setRandom(101, 901));
                selectedAuditorium.setConnections(selectedAuditorium.getConnections() - 1);
                dramaRoom.setConnections(dramaRoom.getConnections() - 1);
            } else {
                Room connectRoom = findCentralRoom(view);
                schoolConnect.addEdge(dramaRoom, connectRoom);
                connectRoom.setConnections(connectRoom.getConnections() - 1);
                dramaRoom.setConnections(dramaRoom.getConnections() - 1);
            }
        }
    }

    //TODO: Tweak office gen so that multiple don't end up on classrooms. Possibly add more offices to front office
    //TODO: Add meeting room to front office
    //TODO: Change to improved switch statement for performance
    private void populateOffices(GameView view) {
        Room[] offices = roomPool[15];
        Room[] coreOffices = new Room[4];
        Room frontOffice = null;

        for (Room office : offices) {
            if (office.getRoomName().equals("Front Office")) {
                Room connectRoom = findCentralRoom(view);
                frontOffice = office;
                schoolConnect.addEdge(frontOffice, connectRoom);
                connectRoom.setConnections(connectRoom.getConnections() - 1);
                frontOffice.setConnections(frontOffice.getConnections() - 1);
            } else if (office.getRoomName().equals("Principal's Office")) {
                if (frontOffice == null) {
                    coreOffices[0] = office;
                } else {
                    schoolConnect.addEdge(frontOffice, office);
                    office.setConnections(office.getConnections() - 1);
                    frontOffice.setConnections(frontOffice.getConnections() - 1);
                }
            } else if (office.getRoomName().equals("Vice Principal's Office")) {
                if (frontOffice == null) {
                    coreOffices[1] = office;
                } else {
                    schoolConnect.addEdge(frontOffice, office);
                    office.setConnections(office.getConnections() - 1);
                    frontOffice.setConnections(frontOffice.getConnections() - 1);
                }
            } else if (office.getRoomName().equals("Guidance Councilor's Office")) {
                if (frontOffice == null) {
                    coreOffices[2] = office;
                } else {
                    schoolConnect.addEdge(frontOffice, office);
                    office.setConnections(office.getConnections() - 1);
                    frontOffice.setConnections(frontOffice.getConnections() - 1);
                }
            } else if (office.getRoomName().equals("Nurse's Office")) {
                if (frontOffice == null) {
                    coreOffices[3] = office;
                } else {
                    schoolConnect.addEdge(frontOffice, office);
                    office.setConnections(office.getConnections() - 1);
                    frontOffice.setConnections(frontOffice.getConnections() - 1);
                }
            } else {
                officeHelper(office);
            }
        }

        for (Room office : coreOffices) {
            if (office != null) {
                schoolConnect.addEdge(frontOffice, office);
                office.setConnections(office.getConnections() - 1);
                assert frontOffice != null;
                frontOffice.setConnections(frontOffice.getConnections() - 1);
            }
        }
    }

    // TODO: switch statement here too
    private void officeHelper(Room office) {
        Room[] classrooms = roomPool[5];
        Room[] gyms = roomPool[9];
        Room[] musicRooms = roomPool[14];
        Room[] artRooms = roomPool[0];
        Room[] hallways = roomPool[10];
        boolean connected = false;

        int choice = setRandom(0, 80);
        if (choice < 50) {
            choice = setRandom(0, classrooms.length - 1);
            schoolConnect.addEdge(classrooms[choice], office);
            classrooms[choice].setConnections(classrooms[choice].getConnections() - 1);
            office.setConnections(office.getConnections() - 1);
            connected = true;
        } else if (50 < choice && choice < 60) {
            choice = setRandom(0, gyms.length - 1);
            if (gyms[choice].getConnections() > 0) {
                schoolConnect.addEdge(gyms[choice], office);
                gyms[choice].setConnections(gyms[choice].getConnections() - 1);
                office.setConnections(office.getConnections() - 1);
                connected = true;
            }
        } else if (60 < choice && choice < 70) {
            choice = setRandom(0, musicRooms.length - 1);
            if (musicRooms[choice].getConnections() > 0) {
                schoolConnect.addEdge(musicRooms[choice], office);
                musicRooms[choice].setConnections(musicRooms[choice].getConnections() - 1);
                office.setConnections(office.getConnections() - 1);
                connected = true;
            }
        } else {
            choice = setRandom(0, artRooms.length - 1);
            if (artRooms[choice].getConnections() > 0) {
                schoolConnect.addEdge(artRooms[choice], office);
                artRooms[choice].setConnections(artRooms[choice].getConnections() - 1);
                office.setConnections(office.getConnections() - 1);
                connected = true;
            }
        }

        if (!connected) {
            choice = setRandom(0, hallways.length - 1);
            if (hallways[choice].getConnections() == 0) {
                hallways[choice].setConnections(hallways[choice].getConnections() + 1);
            }
            schoolConnect.addEdge(hallways[choice], office);
            hallways[choice].setConnections(hallways[choice].getConnections() - 1);
            office.setConnections(office.getConnections() - 1);
        }
    }

    private void populateStudentBathrooms(GameView view) {
        Room[] bathrooms = roomPool[3];

        for (Room bathroom : bathrooms) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(bathroom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            bathroom.setConnections(bathroom.getConnections() - 1);
        }
    }

    private void populateClassrooms(GameView view) {
        Room[] classrooms = roomPool[5];
        Room[] labs = roomPool[16];
        Classroom classR = null;
        for (Room classroom : classrooms) {
            if (classroom instanceof Classroom) {
                classR = (Classroom) classroom;
            }
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(classroom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            classroom.setConnections(classroom.getConnections() - 1);
            assert classR != null;
            if (classR.getClassRoomType().equals("Science") && labs_count < labs.length) {
                schoolConnect.addEdge(classroom, labs[labs_count]);
                labs[labs_count].setConnections(labs[labs_count].getConnections() - 1);
                classroom.setConnections(classroom.getConnections() - 1);
                labs_count++;
            }
        }
    }

    private void populateRemainingLabs(GameView view) {
        Room[] labs = roomPool[16];

        for (int i = labs_count; i < labs.length; i++) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(labs[i], connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            labs[i].setConnections(labs[i].getConnections() - 1);
        }
    }

    private void populateComputerLabs(GameView view) {
        Room[] computerLabs = roomPool[6];
        Room[] libraries = roomPool[11];

        for (Room computerLab : computerLabs) {
            int choice = setRandom(0, 2);
            if (choice == 1) {
                choice = setRandom(0, libraries.length - 1);
                schoolConnect.addEdge(computerLab, libraries[choice]);
                computerLab.setConnections(computerLab.getConnections() - 1);
                libraries[choice].setConnections(libraries[choice].getConnections() - 1);
            } else {
                Room connectRoom = findCentralRoom(view);
                schoolConnect.addEdge(computerLab, connectRoom);
                computerLab.setConnections(computerLab.getConnections() - 1);
                connectRoom.setConnections(connectRoom.getConnections() - 1);
            }
        }
    }

    private void populateUtilityRooms(GameView view) {
        Room[] utilityRooms = roomPool[17];
        Room[] libraries = roomPool[11];
        Room[] computerLabs = roomPool[6];
        Room[] auditoriums = roomPool[2];
        Room[] lunchRooms = roomPool[13];

        for (Room utilityRoom : utilityRooms) {
            int choice = setRandom(0, 10);
            switch (choice) {
                case 0 -> {
                    choice = setRandom(0, libraries.length - 1);
                    schoolConnect.addEdge(utilityRoom, libraries[choice]);
                    utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                    libraries[choice].setConnections(libraries[choice].getConnections() - 1);
                }
                case 1 -> {
                    choice = setRandom(0, computerLabs.length - 1);
                    schoolConnect.addEdge(utilityRoom, computerLabs[choice]);
                    utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                    computerLabs[choice].setConnections(computerLabs[choice].getConnections() - 1);
                }
                case 2 -> {
                    choice = setRandom(0, auditoriums.length - 1);
                    schoolConnect.addEdge(utilityRoom, auditoriums[choice]);
                    utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                    auditoriums[choice].setConnections(auditoriums[choice].getConnections() - 1);
                }
                case 3 -> {
                    choice = setRandom(0, lunchRooms.length - 1);
                    schoolConnect.addEdge(utilityRoom, lunchRooms[choice]);
                    utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                    lunchRooms[choice].setConnections(lunchRooms[choice].getConnections() - 1);
                }
                default -> {
                    Room connectRoom = findCentralRoom(view);
                    schoolConnect.addEdge(utilityRoom, connectRoom);
                    utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                    connectRoom.setConnections(connectRoom.getConnections() - 1);
                }
            }
        }
    }

    private void populateBreakrooms(GameView view) {
        Room[] breakrooms = roomPool[4];

        for (Room breakroom : breakrooms) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(breakroom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            breakroom.setConnections(breakroom.getConnections() - 1);
        }
    }

    private void populateVocationalRooms(GameView view) {
        Room[] vocationalRooms = roomPool[20];

        for(Room vocationalRoom : vocationalRooms) {
            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(vocationalRoom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            vocationalRoom.setConnections(vocationalRoom.getConnections() - 1);
        }
    }

    private void constructBackbone() {
        Room[] hallways = roomPool[10];
        Room[] courtyards = roomPool[7];

        for (Room hallway : hallways) {
            int choice = setRandom(0, 10);

            if (choice > 3 && hallways.length > 1) {
                Room targetHallway;
                do {
                    int randomIndex = setRandom(0, hallways.length - 1);
                    targetHallway = hallways[randomIndex];
                } while (targetHallway == hallway);
                schoolConnect.addEdge(hallway, targetHallway);
                hallway.setConnections(hallway.getConnections() - 1);
                targetHallway.setConnections(targetHallway.getConnections() - 1);
            } else if (courtyards.length > 0) {
                int randomIndex = setRandom(0, courtyards.length - 1);
                Room targetCourtyard = courtyards[randomIndex];
                schoolConnect.addEdge(hallway, targetCourtyard);
                hallway.setConnections(hallway.getConnections() - 1);
                targetCourtyard.setConnections(targetCourtyard.getConnections() - 1);
            }
        }
    }

    private void connectivityInspectionBackbone() {
        ConnectivityInspector<Room, DefaultEdge> inspector = new ConnectivityInspector<>(schoolConnect);
        List<Set<Room>> connectedSets = inspector.connectedSets();

        // Filter sets to include only those that contain hallways or courtyards
        List<Set<Room>> filteredSets = connectedSets.stream()
                .filter(set -> set.stream().anyMatch(this::isHallwayOrCourtyard)).toList();

        if (filteredSets.size() > 1) {
            for (int i = 0; i < filteredSets.size() - 1; i++) {
                Set<Room> currentSet = filteredSets.get(i);
                Set<Room> nextSet = filteredSets.get(i + 1);

                // Find hallways or courtyards within the current and next sets to connect
                Room roomFromCurrentSet = findHallwayOrCourtyard(currentSet);
                Room roomFromNextSet = findHallwayOrCourtyard(nextSet);

                if (roomFromCurrentSet != null && roomFromNextSet != null) {
                    schoolConnect.addEdge(roomFromCurrentSet, roomFromNextSet);
                    roomFromCurrentSet.setConnections(roomFromCurrentSet.getConnections() - 1);
                    roomFromNextSet.setConnections(roomFromNextSet.getConnections() - 1);
                }
            }
        }
    }

    // Helper method to check if a room is a hallway or courtyard
    private boolean isHallwayOrCourtyard(Room room) {
        return Stream.of(roomPool[10], roomPool[7]).flatMap(Arrays::stream).anyMatch(r -> r.equals(room));
    }

    // Helper method to find a hallway or courtyard in a set
    private Room findHallwayOrCourtyard(Set<Room> roomSet) {
        return roomSet.stream().filter(this::isHallwayOrCourtyard).findAny().orElse(null);
    }

    private Room getRandomRoom(int i, int j) {
        int x = setRandom(0, roomPool.length - 1);
        int y = setRandom(0, roomPool[x].length - 1);
        while (x == i && y == j) {
            x = setRandom(0, roomPool.length - 1);
            y = setRandom(0, roomPool[x].length - 1);
        }
        return roomPool[x][y];
    }

    private Room getRandomRoom(Room roomToAvoid) {
        int x = setRandom(1, roomPool.length - 1);
        int y = setRandom(0, roomPool[x].length - 1);
        while (roomPool[x][y].equals(roomToAvoid)) {
            x = setRandom(1, roomPool.length - 1);
            y = setRandom(0, roomPool[x].length - 1);
        }
        return roomPool[x][y];
    }

    // Perform simple print for now
    public void getConnections() {
        Iterator<Room> iterator = new DepthFirstIterator<>(schoolConnect);

        while (iterator.hasNext()) {
            Room room = iterator.next();
            Set<DefaultEdge> edges = schoolConnect.edgesOf(room);

            for (DefaultEdge edge : edges) {
                Room sourceRoom = schoolConnect.getEdgeSource(edge);
                Room targetRoom = schoolConnect.getEdgeTarget(edge);

                if (sourceRoom.equals(room)) {
                    System.out.println("Room " + sourceRoom.getRoomName() + " is connected to " + targetRoom.getRoomName());
                }
            }
        }
    }

    //TODO: fix visibility on graphs
    public void visualizer(StandardSchool school) {
        String schoolName = school.getSchoolName();
        JGraphXAdapter<Room, DefaultEdge> graphAdapter = new JGraphXAdapter<>(schoolConnect);
        JFrame frame = new JFrame(schoolName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        frame.add(graphComponent);
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graphAdapter);
        layout.setForceConstant(50);
        layout.setMinDistanceLimit(2.0);
        layout.setInitialTemp(200);
        layout.setMaxIterations(1000);
        layout.execute(graphAdapter.getDefaultParent());
        frame.pack();
        frame.setVisible(true);
    }
}
