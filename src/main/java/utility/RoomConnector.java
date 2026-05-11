package utility;

import constants.SchoolConstants;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import entity.Rooms.*;
import entity.StandardSchool;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;
import view.GameView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static utility.Randomizer.setRandom;

// Procedural generation that builds the school by connecting rooms. Room connection starts
// by connecting hallways and courtyards at random, and then allows other connections to build
// off that backbone. jgrapht connectivity inspector ensures no dangling vertexes
// vertex is door
// edge is room
public class RoomConnector {
    private final Room[][] roomPool = new Room[23][];
    private final StandardSchool standardSchool;
    Graph<Room, DefaultEdge> schoolConnect = new Multigraph<>(DefaultEdge.class);
    private int locker_count = 0;
    private int labs_count = 0;

    public RoomConnector(StandardSchool standardSchool, GameView view) {
        this.standardSchool = standardSchool;
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
        roomPool[21] = standardSchool.getPortables();
        roomPool[22] = standardSchool.getStairwells();

        connectRooms(view);
    }

    private void connectRooms(GameView view) {
        populateVertex();
        assignFloors();
        constructBackbone();
        connectivityInspectionBackbone();
        populateAthleticFields(view);
        populateParkingLots(view);
        populatePortables(view); // Portables connect to outdoor spaces only
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

    private void assignFloors() {
        int numberOfFloors = standardSchool.getNumberOfFloors();
        if (numberOfFloors <= 1) {
            return;
        }

        Room[] hallways = roomPool[10];
        Room[] stairwells = roomPool[22];

        int upperHallwayCount = (int) Math.ceil(hallways.length * SchoolConstants.UPPER_FLOOR_HALLWAY_RATIO);
        upperHallwayCount = Math.max(upperHallwayCount, numberOfFloors - 1);

        List<Room> shuffledHallways = new ArrayList<>(Arrays.asList(hallways));
        GameRandom.shuffle(shuffledHallways);

        int assigned = 0;
        for (int floor = 2; floor <= numberOfFloors && assigned < upperHallwayCount; floor++) {
            int hallwaysForThisFloor = upperHallwayCount / (numberOfFloors - 1);
            if (floor == 2) {
                hallwaysForThisFloor += upperHallwayCount % (numberOfFloors - 1);
            }
            for (int j = 0; j < hallwaysForThisFloor && assigned < upperHallwayCount; j++) {
                shuffledHallways.get(assigned).setFloorNumber(floor);
                assigned++;
            }
        }

        // Upper-floor-eligible room type indices:
        // 0=ArtStudios, 3=Bathrooms, 4=Breakrooms, 5=Classrooms, 6=ComputerLabs,
        // 8=DramaRooms, 11=Libraries, 14=MusicRooms, 15=Offices, 16=ScienceLabs,
        // 17=UtilityRooms, 18=ConferenceRooms, 20=VocationalRooms
        int[] eligibleIndices = {0, 3, 4, 5, 6, 8, 11, 14, 15, 16, 17, 18, 20};
        double ratio = SchoolConstants.UPPER_FLOOR_HALLWAY_RATIO;

        for (int idx : eligibleIndices) {
            Room[] rooms = roomPool[idx];
            if (rooms == null || rooms.length == 0) {
                continue;
            }
            int upperCount = (int) Math.ceil(rooms.length * ratio);
            List<Room> shuffled = new ArrayList<>(Arrays.asList(rooms));
            GameRandom.shuffle(shuffled);

            int roomAssigned = 0;
            for (int floor = 2; floor <= numberOfFloors && roomAssigned < upperCount; floor++) {
                int roomsForFloor = upperCount / (numberOfFloors - 1);
                if (floor == 2) {
                    roomsForFloor += upperCount % (numberOfFloors - 1);
                }
                for (int j = 0; j < roomsForFloor && roomAssigned < upperCount; j++) {
                    shuffled.get(roomAssigned).setFloorNumber(floor);
                    roomAssigned++;
                }
            }
        }

        if (stairwells != null) {
            List<Room> upperHallways = new ArrayList<>();
            for (Room h : hallways) {
                if (h.getFloorNumber() > 1) {
                    upperHallways.add(h);
                }
            }

            for (int i = 0; i < stairwells.length; i++) {
                Stairwell sw = (Stairwell) stairwells[i];
                sw.setConnectsFloorA(1);
                if (!upperHallways.isEmpty()) {
                    int targetFloor = upperHallways.get(i % upperHallways.size()).getFloorNumber();
                    sw.setConnectsFloorB(targetFloor);
                } else {
                    sw.setConnectsFloorB(2);
                }
            }
        }
    }

    private void connectivityInspection(GameView view) {
        ConnectivityInspector<Room, DefaultEdge> inspector = new ConnectivityInspector<>(schoolConnect);
        List<Set<Room>> connectedSets = inspector.connectedSets();

        if (connectedSets.size() <= 1) {
            return;
        }

        for (Set<Room> roomSet : connectedSets) {
            Room representative = roomSet.stream()
                    .filter(r -> r.getConnections() > 0)
                    .findAny()
                    .orElse(null);
            if (representative == null) {
                continue;
            }

            int floor = representative.getFloorNumber();

            if (floor > 1) {
                Room[] stairwells = roomPool[22];
                if (stairwells != null) {
                    Stairwell bridge = null;
                    for (Room sw : stairwells) {
                        Stairwell s = (Stairwell) sw;
                        if (s.getConnectsFloorA() == floor || s.getConnectsFloorB() == floor) {
                            if (!roomSet.contains(s)) {
                                bridge = s;
                                break;
                            }
                        }
                    }
                    if (bridge != null && !schoolConnect.containsEdge(representative, bridge)) {
                        schoolConnect.addEdge(representative, bridge);
                        representative.setConnections(representative.getConnections() - 1);
                        bridge.setConnections(bridge.getConnections() - 1);
                        continue;
                    }
                }
            }

            Room connectorRoom = findCentralRoomOnFloor(floor, view);
            if (!schoolConnect.containsEdge(representative, connectorRoom)) {
                schoolConnect.addEdge(representative, connectorRoom);
                representative.setConnections(representative.getConnections() - 1);
                connectorRoom.setConnections(connectorRoom.getConnections() - 1);
            }
        }
    }

    // TODO: Allow for injected weight distribution on hallways and courtyards
    // Adjust random weight of hallway or courtyard selection as needed. Might need
    // to re-balance (i.e. 60/40)
    private Room findCentralRoom(GameView view) {
        Room[] hallways = roomPool[10];
        Room[] courtyards = roomPool[7];
        int choice = setRandom(0, 3);
        int count = 0;

        if (choice < 3) {
            do {
                choice = setRandom(0, hallways.length - 1);
                GameLogger.logGeneration("Connecting halls...");
                count++;
            } while (hallways[choice].getConnections() == 0 && count < calculateExpectedCycles(hallways.length));
            // Add a connection to a random hallway if no connections are left
            hallways[choice].setConnections(hallways[choice].getConnections() + 1);
            return hallways[choice];
        } else {
            do {
                choice = setRandom(0, courtyards.length - 1);
                GameLogger.logGeneration("Connecting courtyards...");
                count++;
            } while (courtyards[choice].getConnections() == 0 && count < calculateExpectedCycles(courtyards.length));
            // Add a connection to a random courtyard if no connections are left
            courtyards[choice].setConnections(courtyards[choice].getConnections() + 1);
            return courtyards[choice];
        }
    }

    private Room findCentralRoomOnFloor(int floor, GameView view) {
        if (floor == 1) {
            return findCentralRoom(view);
        }

        Room[] hallways = roomPool[10];
        List<Room> floorHallways = new ArrayList<>();
        for (Room h : hallways) {
            if (h.getFloorNumber() == floor) {
                floorHallways.add(h);
            }
        }

        if (floorHallways.isEmpty()) {
            return findCentralRoom(view);
        }

        int count = 0;
        int maxTries = (int) calculateExpectedCycles(floorHallways.size());
        Room selected;
        do {
            selected = floorHallways.get(setRandom(0, floorHallways.size() - 1));
            count++;
        } while (selected.getConnections() == 0 && count < maxTries);

        selected.setConnections(selected.getConnections() + 1);
        return selected;
    }

    // Coupon Collector problem for finding minimum random selections for a set
    private double calculateExpectedCycles(int N) {
        double harmonic_N = 0;

        for (int i = 1; i <= N; i++) {
            harmonic_N += 1.0 / i;
        }

        return N * harmonic_N;
    }

    private void populateAthleticFields(GameView view) {
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

    /**
     * Connects portable classrooms to outdoor spaces only.
     * Portables are temporary modular buildings that can ONLY connect to:
     * - Athletic fields
     * - Courtyards
     * - Parking lots
     * They CANNOT connect to hallways (as per real-world placement).
     *
     * @param view the game view for output
     */
    private void populatePortables(GameView view) {
        Room[] portables = roomPool[21];

        // Skip if no portables
        if (portables == null || portables.length == 0) {
            return;
        }

        // Outdoor spaces that portables can connect to
        Room[] athleticFields = roomPool[1];
        Room[] courtyards = roomPool[7];
        Room[] parkingLots = roomPool[19];

        GameLogger.logGeneration("Connecting portable classrooms to outdoor spaces...");

        for (Room portable : portables) {
            Room outdoorSpace = findOutdoorSpace(athleticFields, courtyards, parkingLots);

            if (outdoorSpace != null) {
                schoolConnect.addEdge(portable, outdoorSpace);
                portable.setConnections(portable.getConnections() - 1);
                outdoorSpace.setConnections(outdoorSpace.getConnections() - 1);
                GameLogger
                        .logGeneration("   Connected " + portable.getRoomName() + " to " + outdoorSpace.getRoomName());
            } else {
                // Fallback: connect to courtyard (most common real-world scenario)
                // Add a connection if needed
                if (courtyards.length > 0) {
                    int idx = setRandom(0, courtyards.length - 1);
                    courtyards[idx].setConnections(courtyards[idx].getConnections() + 1);
                    schoolConnect.addEdge(portable, courtyards[idx]);
                    portable.setConnections(portable.getConnections() - 1);
                    courtyards[idx].setConnections(courtyards[idx].getConnections() - 1);
                    GameLogger.logGeneration("   Connected " + portable.getRoomName() + " to "
                            + courtyards[idx].getRoomName() + " (fallback)");
                }
            }
        }
    }

    /**
     * Finds an outdoor space (athletic field, courtyard, or parking lot) with
     * available connections.
     * Weighted distribution: 40% parking lots, 35% courtyards, 25% athletic fields
     *
     * @param athleticFields the athletic fields array
     * @param courtyards     the courtyards array
     * @param parkingLots    the parking lots array
     * @return an outdoor space with available connections, or null if none
     *         available
     */
    private Room findOutdoorSpace(Room[] athleticFields, Room[] courtyards, Room[] parkingLots) {
        int choice = setRandom(0, 100);
        Room selected = null;

        // Weighted selection: 40% parking, 35% courtyard, 25% field
        if (choice < 40 && parkingLots.length > 0) {
            // Try parking lots first
            selected = findRoomWithConnections(parkingLots);
        } else if (choice < 75 && courtyards.length > 0) {
            // Try courtyards
            selected = findRoomWithConnections(courtyards);
        } else if (athleticFields.length > 0) {
            // Try athletic fields
            selected = findRoomWithConnections(athleticFields);
        }

        // Fallback: try other options if primary choice failed
        if (selected == null && parkingLots.length > 0) {
            selected = findRoomWithConnections(parkingLots);
        }
        if (selected == null && courtyards.length > 0) {
            selected = findRoomWithConnections(courtyards);
        }
        if (selected == null && athleticFields.length > 0) {
            selected = findRoomWithConnections(athleticFields);
        }

        return selected;
    }

    /**
     * Finds a room in the given array that has available connections.
     *
     * @param rooms the array of rooms to search
     * @return a room with available connections, or null if none found
     */
    private Room findRoomWithConnections(Room[] rooms) {
        if (rooms == null || rooms.length == 0) {
            return null;
        }

        // Try random selection first
        int startIdx = setRandom(0, rooms.length - 1);
        for (int i = 0; i < rooms.length; i++) {
            int idx = (startIdx + i) % rooms.length;
            if (rooms[idx].getConnections() > 0) {
                return rooms[idx];
            }
        }

        return null;
    }

    private void populateConferenceRooms(GameView view) {
        Room[] conferenceRooms = roomPool[18];
        Room[] offices = roomPool[15];

        Room frontOffice = frontOfficeLocator(offices);
        int choice;

        for (Room conferenceRoom : conferenceRooms) {
            choice = setRandom(0, 5);
            if (choice >= 3 && frontOffice != null
                    && conferenceRoom.getFloorNumber() == frontOffice.getFloorNumber()) {
                schoolConnect.addEdge(conferenceRoom, frontOffice);
                conferenceRoom.setConnections(conferenceRoom.getConnections() - 1);
                frontOffice.setConnections(frontOffice.getConnections() - 1);
            } else {
                Room connectRoom = findCentralRoomOnFloor(conferenceRoom.getFloorNumber(), view);
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
            Room connectRoom = findCentralRoomOnFloor(library.getFloorNumber(), view);
            schoolConnect.addEdge(library, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            library.setConnections(library.getConnections() - 1);
        }
    }

    private void populateMusicRooms(GameView view) {
        Room[] musicRooms = roomPool[14];

        for (Room musicRoom : musicRooms) {
            Room connectRoom = findCentralRoomOnFloor(musicRoom.getFloorNumber(), view);
            schoolConnect.addEdge(musicRoom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            musicRoom.setConnections(musicRoom.getConnections() - 1);
        }
    }

    private void populateArtRooms(GameView view) {
        Room[] artRooms = roomPool[0];

        for (Room artRoom : artRooms) {
            Room connectRoom = findCentralRoomOnFloor(artRoom.getFloorNumber(), view);
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
            if (chance < 2 && dramaRoom.getFloorNumber() == 1) {
                Room selectedAuditorium = auditoriums[setRandom(0, auditoriums.length - 1)];
                schoolConnect.addEdge(dramaRoom, selectedAuditorium);
                dramaRoom.setRoomNumber("A" + setRandom(101, 901));
                selectedAuditorium.setConnections(selectedAuditorium.getConnections() - 1);
                dramaRoom.setConnections(dramaRoom.getConnections() - 1);
            } else {
                Room connectRoom = findCentralRoomOnFloor(dramaRoom.getFloorNumber(), view);
                schoolConnect.addEdge(dramaRoom, connectRoom);
                connectRoom.setConnections(connectRoom.getConnections() - 1);
                dramaRoom.setConnections(dramaRoom.getConnections() - 1);
            }
        }
    }

    // TODO: Tweak office gen so that multiple don't end up on classrooms. Possibly
    // add more offices to front office
    // TODO: Add meeting room to front office
    // TODO: Change to improved switch statement for performance
    private void populateOffices(GameView view) {
        Room[] offices = roomPool[15];
        Room[] coreOffices = new Room[4];
        Room frontOffice = null;

        for (Room office : offices) {
            if (office.getRoomName().equals("Front Office")) {
                Room connectRoom = findCentralRoomOnFloor(office.getFloorNumber(), view);
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

    private void officeHelper(Room office) {
        Room[] classrooms = roomPool[5];
        Room[] gyms = roomPool[9];
        Room[] musicRooms = roomPool[14];
        Room[] artRooms = roomPool[0];
        Room[] hallways = roomPool[10];
        boolean connected = false;
        int floor = office.getFloorNumber();

        int choice = setRandom(0, 100);
        if (choice < 30) {
            Room target = findSameFloorRoom(office, classrooms);
            if (target != null && target.getConnections() > 0) {
                schoolConnect.addEdge(target, office);
                target.setConnections(target.getConnections() - 1);
                office.setConnections(office.getConnections() - 1);
                connected = true;
            }
        } else if (choice < 45 && floor == 1) {
            choice = setRandom(0, gyms.length - 1);
            if (gyms[choice].getConnections() > 0) {
                schoolConnect.addEdge(gyms[choice], office);
                gyms[choice].setConnections(gyms[choice].getConnections() - 1);
                office.setConnections(office.getConnections() - 1);
                connected = true;
            }
        } else if (choice < 60) {
            Room target = findSameFloorRoom(office, musicRooms);
            if (target != null && target.getConnections() > 0) {
                schoolConnect.addEdge(target, office);
                target.setConnections(target.getConnections() - 1);
                office.setConnections(office.getConnections() - 1);
                connected = true;
            }
        } else if (choice < 75) {
            Room target = findSameFloorRoom(office, artRooms);
            if (target != null && target.getConnections() > 0) {
                schoolConnect.addEdge(target, office);
                target.setConnections(target.getConnections() - 1);
                office.setConnections(office.getConnections() - 1);
                connected = true;
            }
        }

        if (!connected) {
            Room hallway = findHallwayOnFloor(hallways, floor);
            if (hallway == null) {
                hallway = hallways[setRandom(0, hallways.length - 1)];
            }
            if (hallway.getConnections() == 0) {
                hallway.setConnections(hallway.getConnections() + 1);
            }
            schoolConnect.addEdge(hallway, office);
            hallway.setConnections(hallway.getConnections() - 1);
            office.setConnections(office.getConnections() - 1);
        }
    }

    private void populateStudentBathrooms(GameView view) {
        Room[] bathrooms = roomPool[3];

        for (Room bathroom : bathrooms) {
            Room connectRoom = findCentralRoomOnFloor(bathroom.getFloorNumber(), view);
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
            Room connectRoom = findCentralRoomOnFloor(classroom.getFloorNumber(), view);
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
            Room connectRoom = findCentralRoomOnFloor(labs[i].getFloorNumber(), view);
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
                Room sameFloorLib = findSameFloorRoom(computerLab, libraries);
                if (sameFloorLib != null) {
                    schoolConnect.addEdge(computerLab, sameFloorLib);
                    computerLab.setConnections(computerLab.getConnections() - 1);
                    sameFloorLib.setConnections(sameFloorLib.getConnections() - 1);
                } else {
                    Room connectRoom = findCentralRoomOnFloor(computerLab.getFloorNumber(), view);
                    schoolConnect.addEdge(computerLab, connectRoom);
                    computerLab.setConnections(computerLab.getConnections() - 1);
                    connectRoom.setConnections(connectRoom.getConnections() - 1);
                }
            } else {
                Room connectRoom = findCentralRoomOnFloor(computerLab.getFloorNumber(), view);
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
            boolean connected = false;
            switch (choice) {
                case 0 -> {
                    Room target = findSameFloorRoom(utilityRoom, libraries);
                    if (target != null) {
                        schoolConnect.addEdge(utilityRoom, target);
                        utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                        target.setConnections(target.getConnections() - 1);
                        connected = true;
                    }
                }
                case 1 -> {
                    Room target = findSameFloorRoom(utilityRoom, computerLabs);
                    if (target != null) {
                        schoolConnect.addEdge(utilityRoom, target);
                        utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                        target.setConnections(target.getConnections() - 1);
                        connected = true;
                    }
                }
                case 2 -> {
                    if (utilityRoom.getFloorNumber() == 1) {
                        choice = setRandom(0, auditoriums.length - 1);
                        schoolConnect.addEdge(utilityRoom, auditoriums[choice]);
                        utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                        auditoriums[choice].setConnections(auditoriums[choice].getConnections() - 1);
                        connected = true;
                    }
                }
                case 3 -> {
                    if (utilityRoom.getFloorNumber() == 1) {
                        choice = setRandom(0, lunchRooms.length - 1);
                        schoolConnect.addEdge(utilityRoom, lunchRooms[choice]);
                        utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                        lunchRooms[choice].setConnections(lunchRooms[choice].getConnections() - 1);
                        connected = true;
                    }
                }
                default -> { }
            }
            if (!connected) {
                Room connectRoom = findCentralRoomOnFloor(utilityRoom.getFloorNumber(), view);
                schoolConnect.addEdge(utilityRoom, connectRoom);
                utilityRoom.setConnections(utilityRoom.getConnections() - 1);
                connectRoom.setConnections(connectRoom.getConnections() - 1);
            }
        }
    }

    private void populateBreakrooms(GameView view) {
        Room[] breakrooms = roomPool[4];

        for (Room breakroom : breakrooms) {
            Room connectRoom = findCentralRoomOnFloor(breakroom.getFloorNumber(), view);
            schoolConnect.addEdge(breakroom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            breakroom.setConnections(breakroom.getConnections() - 1);
        }
    }

    private void populateVocationalRooms(GameView view) {
        Room[] vocationalRooms = roomPool[20];

        for (Room vocationalRoom : vocationalRooms) {
            Room connectRoom = findCentralRoomOnFloor(vocationalRoom.getFloorNumber(), view);
            schoolConnect.addEdge(vocationalRoom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            vocationalRoom.setConnections(vocationalRoom.getConnections() - 1);
        }
    }

    private void constructBackbone() {
        Room[] hallways = roomPool[10];
        Room[] courtyards = roomPool[7];
        Room[] stairwells = roomPool[22];

        for (Room hallway : hallways) {
            int choice = setRandom(0, 10);

            if (choice > 3 && hallways.length > 1) {
                Room targetHallway = findSameFloorHallway(hallway, hallways);
                if (targetHallway != null) {
                    schoolConnect.addEdge(hallway, targetHallway);
                    hallway.setConnections(hallway.getConnections() - 1);
                    targetHallway.setConnections(targetHallway.getConnections() - 1);
                }
            } else if (hallway.getFloorNumber() == 1 && courtyards.length > 0) {
                int randomIndex = setRandom(0, courtyards.length - 1);
                Room targetCourtyard = courtyards[randomIndex];
                schoolConnect.addEdge(hallway, targetCourtyard);
                hallway.setConnections(hallway.getConnections() - 1);
                targetCourtyard.setConnections(targetCourtyard.getConnections() - 1);
            } else {
                Room targetHallway = findSameFloorHallway(hallway, hallways);
                if (targetHallway != null) {
                    schoolConnect.addEdge(hallway, targetHallway);
                    hallway.setConnections(hallway.getConnections() - 1);
                    targetHallway.setConnections(targetHallway.getConnections() - 1);
                }
            }
        }

        if (stairwells != null && stairwells.length > 0) {
            for (Room stairwell : stairwells) {
                Stairwell sw = (Stairwell) stairwell;
                int floorA = sw.getConnectsFloorA();
                int floorB = sw.getConnectsFloorB();

                Room hallwayA = findHallwayOnFloor(hallways, floorA);
                Room hallwayB = findHallwayOnFloor(hallways, floorB);

                if (hallwayA != null) {
                    schoolConnect.addEdge(stairwell, hallwayA);
                    stairwell.setConnections(stairwell.getConnections() - 1);
                    hallwayA.setConnections(hallwayA.getConnections() - 1);
                }
                if (hallwayB != null) {
                    schoolConnect.addEdge(stairwell, hallwayB);
                    stairwell.setConnections(stairwell.getConnections() - 1);
                    hallwayB.setConnections(hallwayB.getConnections() - 1);
                }
            }
        }
    }

    private Room findSameFloorRoom(Room source, Room[] candidates) {
        int floor = source.getFloorNumber();
        List<Room> matching = new ArrayList<>();
        for (Room r : candidates) {
            if (r.getFloorNumber() == floor) {
                matching.add(r);
            }
        }
        if (matching.isEmpty()) {
            return null;
        }
        return matching.get(setRandom(0, matching.size() - 1));
    }

    private Room findSameFloorHallway(Room source, Room[] hallways) {
        int floor = source.getFloorNumber();
        List<Room> candidates = new ArrayList<>();
        for (Room h : hallways) {
            if (h != source && h.getFloorNumber() == floor) {
                candidates.add(h);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(setRandom(0, candidates.size() - 1));
    }

    private Room findHallwayOnFloor(Room[] hallways, int floor) {
        List<Room> candidates = new ArrayList<>();
        for (Room h : hallways) {
            if (h.getFloorNumber() == floor) {
                candidates.add(h);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(setRandom(0, candidates.size() - 1));
    }

    private void connectivityInspectionBackbone() {
        ConnectivityInspector<Room, DefaultEdge> inspector = new ConnectivityInspector<>(schoolConnect);
        List<Set<Room>> connectedSets = inspector.connectedSets();

        // Filter sets to include only those that contain backbone rooms
        List<Set<Room>> filteredSets = connectedSets.stream()
                .filter(set -> set.stream().anyMatch(this::isBackboneRoom)).toList();

        if (filteredSets.size() > 1) {
            for (int i = 0; i < filteredSets.size() - 1; i++) {
                Set<Room> currentSet = filteredSets.get(i);
                Set<Room> nextSet = filteredSets.get(i + 1);

                Room roomFromCurrentSet = findBackboneRoom(currentSet);
                Room roomFromNextSet = findBackboneRoom(nextSet);

                if (roomFromCurrentSet != null && roomFromNextSet != null) {
                    schoolConnect.addEdge(roomFromCurrentSet, roomFromNextSet);
                    roomFromCurrentSet.setConnections(roomFromCurrentSet.getConnections() - 1);
                    roomFromNextSet.setConnections(roomFromNextSet.getConnections() - 1);
                }
            }
        }
    }

    private boolean isBackboneRoom(Room room) {
        Stream<Room[]> backbonePools = Stream.of(roomPool[10], roomPool[7]);
        if (roomPool[22] != null && roomPool[22].length > 0) {
            backbonePools = Stream.of(roomPool[10], roomPool[7], roomPool[22]);
        }
        return backbonePools.flatMap(Arrays::stream).anyMatch(r -> r.equals(room));
    }

    private Room findBackboneRoom(Set<Room> roomSet) {
        return roomSet.stream().filter(this::isBackboneRoom).findAny().orElse(null);
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
                    GameLogger.logDebug(
                            "Room " + sourceRoom.getRoomName() + " is connected to " + targetRoom.getRoomName());
                }
            }
        }
    }

    public Graph<Room, DefaultEdge> getSchoolConnect() {
        return this.schoolConnect;
    }

    /**
     * Integrates dynamically added rooms (portables and classrooms) into the
     * existing
     * school graph. Called after expansion adds new rooms to the school.
     * 
     * New portables are connected to outdoor spaces using the same logic as initial
     * generation.
     * New classrooms are connected to hallways/courtyards via the backbone.
     *
     * @param school the school with potentially new rooms
     * @param view   the game view for output
     */
    public void integrateNewRooms(StandardSchool school, GameView view) {
        GameLogger.logGeneration("Integrating newly added rooms into school graph...");

        // Update roomPool references to pick up new arrays from the school
        roomPool[5] = school.getClassrooms();
        roomPool[21] = school.getPortables();

        // Find new portables (not yet in the graph)
        Room[] currentPortables = school.getPortables();
        int newPortableCount = 0;
        if (currentPortables != null) {
            for (Room portable : currentPortables) {
                if (!schoolConnect.containsVertex(portable)) {
                    schoolConnect.addVertex(portable);
                    newPortableCount++;
                }
            }
        }

        // Find new classrooms (not yet in the graph)
        Room[] currentClassrooms = school.getClassrooms();
        int newClassroomCount = 0;
        if (currentClassrooms != null) {
            for (Room classroom : currentClassrooms) {
                if (!schoolConnect.containsVertex(classroom)) {
                    schoolConnect.addVertex(classroom);
                    newClassroomCount++;
                }
            }
        }

        GameLogger.logGeneration("  New portables to connect: " + newPortableCount);
        GameLogger.logGeneration("  New classrooms to connect: " + newClassroomCount);

        // Connect new portables to outdoor spaces
        if (newPortableCount > 0) {
            connectNewPortables(currentPortables, view);
        }

        // Connect new classrooms to hallways/courtyards
        if (newClassroomCount > 0) {
            connectNewClassrooms(currentClassrooms, view);
        }

        // Run connectivity inspection to ensure no dangling vertices
        if (newPortableCount > 0 || newClassroomCount > 0) {
            connectivityInspection(view);
            GameLogger.logGeneration("  Graph updated: " + schoolConnect.vertexSet().size() +
                    " rooms, " + schoolConnect.edgeSet().size() + " connections");
        }
    }

    /**
     * Connects newly added portable classrooms to outdoor spaces.
     * Only connects portables that aren't already in the graph's edge set.
     */
    private void connectNewPortables(Room[] portables, GameView view) {
        Room[] athleticFields = roomPool[1];
        Room[] courtyards = roomPool[7];
        Room[] parkingLots = roomPool[19];

        for (Room portable : portables) {
            // Skip portables that already have connections in the graph
            if (schoolConnect.edgesOf(portable).size() > 0) {
                continue;
            }

            Room outdoorSpace = findOutdoorSpace(athleticFields, courtyards, parkingLots);
            if (outdoorSpace != null) {
                schoolConnect.addEdge(portable, outdoorSpace);
                portable.setConnections(portable.getConnections() - 1);
                outdoorSpace.setConnections(outdoorSpace.getConnections() - 1);
                GameLogger.logGeneration("   Connected new " + portable.getRoomName() +
                        " to " + outdoorSpace.getRoomName());
            } else if (courtyards.length > 0) {
                int idx = setRandom(0, courtyards.length - 1);
                courtyards[idx].setConnections(courtyards[idx].getConnections() + 1);
                schoolConnect.addEdge(portable, courtyards[idx]);
                portable.setConnections(portable.getConnections() - 1);
                courtyards[idx].setConnections(courtyards[idx].getConnections() - 1);
                GameLogger.logGeneration("   Connected new " + portable.getRoomName() +
                        " to " + courtyards[idx].getRoomName() + " (fallback)");
            }
        }
    }

    /**
     * Connects newly added classrooms to hallways/courtyards via the backbone.
     * Only connects classrooms that aren't already in the graph's edge set.
     */
    private void connectNewClassrooms(Room[] classrooms, GameView view) {
        for (Room classroom : classrooms) {
            // Skip classrooms that already have connections
            if (schoolConnect.edgesOf(classroom).size() > 0) {
                continue;
            }

            Room connectRoom = findCentralRoom(view);
            schoolConnect.addEdge(classroom, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            classroom.setConnections(classroom.getConnections() - 1);
            GameLogger.logGeneration("   Connected new " + classroom.getRoomName() +
                    " to " + connectRoom.getRoomName());
        }
    }

    public void visualizer(StandardSchool school) {
        String schoolName = school.getSchoolName();
        JGraphXAdapter<Room, DefaultEdge> graphAdapter = new JGraphXAdapter<>(schoolConnect);

        // Prevent users from moving edges or creating new connections
        graphAdapter.setCellsDisconnectable(false);
        graphAdapter.setConnectableEdges(false);
        graphAdapter.setEdgeLabelsMovable(false);

        // Apply color-coding by room type
        applyRoomStyles(graphAdapter);

        mxFastOrganicLayout layout = new mxFastOrganicLayout(graphAdapter);
        layout.setForceConstant(50);
        layout.setMinDistanceLimit(2.5);
        layout.setInitialTemp(200);
        layout.setMaxIterations(1000);
        layout.execute(graphAdapter.getDefaultParent());

        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        graphComponent.setPreferredSize(new Dimension(1200, 800));

        JFrame frame = new JFrame(schoolName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.add(graphComponent);

        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
                if (cell != null && cell.getValue() instanceof Room room) {
                    Inspector.inspectRoom(room);
                }
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

    private void applyRoomStyles(JGraphXAdapter<Room, DefaultEdge> graphAdapter) {
        Map<Room, mxICell> vertexMap = graphAdapter.getVertexToCellMap();
        graphAdapter.getModel().beginUpdate();
        try {
            for (Map.Entry<Room, mxICell> entry : vertexMap.entrySet()) {
                Room room = entry.getKey();
                mxICell cell = entry.getValue();
                String style = getStyleForRoom(room);
                graphAdapter.getModel().setStyle(cell, style);
            }
        } finally {
            graphAdapter.getModel().endUpdate();
        }
    }

    private String getStyleForRoom(Room room) {
        String base = mxConstants.STYLE_FONTSIZE + "=10;";

        if (room instanceof Hallway) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#D3D3D3;"
                    + mxConstants.STYLE_ROUNDED + "=1;"
                    + mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_RECTANGLE + ";";
        } else if (room instanceof Courtyard) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#90EE90;"
                    + mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_RECTANGLE + ";";
        } else if (room instanceof Stairwell) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#ADD8E6;"
                    + mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_RHOMBUS + ";";
        } else if (room instanceof Classroom || room instanceof Portable) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#FFFACD;";
        } else if (room instanceof Gym || room instanceof AthleticField || room instanceof LockerRoom) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#FFD699;";
        } else if (room instanceof Office || room instanceof ConferenceRoom) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#FFB6C1;";
        } else if (room instanceof Lunchroom) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#FFDAB9;";
        } else if (room instanceof LibraryR || room instanceof ComputerLab) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#E6E6FA;";
        } else if (room instanceof Auditorium || room instanceof DramaRoom || room instanceof MusicRoom
                || room instanceof ArtStudio) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#DDA0DD;";
        } else if (room instanceof Bathroom) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#E0FFFF;";
        } else if (room instanceof ParkingLot) {
            return base + mxConstants.STYLE_FILLCOLOR + "=#C0C0C0;";
        } else {
            return base + mxConstants.STYLE_FILLCOLOR + "=#FFFFFF;";
        }
    }
}
