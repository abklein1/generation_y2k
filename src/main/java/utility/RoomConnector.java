package utility;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import entity.Rooms.Auditorium;
import entity.Rooms.Bathroom;
import entity.Rooms.Room;
import entity.StandardSchool;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import javax.swing.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


// Procedural generation that builds the school by connecting rooms. Room connection starts
// by connecting hallways and courtyards at random, and then allows other connections to build
// off that backbone. jgrapht connectivity inspector ensures no dangling vertexes
// vertex is door
// edge is room
public class RoomConnector {
    private final Room[][] roomPool = new Room[18][];
    private int locker_count = 0;
    Graph<Room, DefaultEdge> schoolConnect = new Multigraph<>(DefaultEdge.class);

    public RoomConnector(StandardSchool standardSchool) {
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

        connectRooms();
    }
    //TODO: More procedural generation
    private void connectRooms() {
        populateVertex();
        constructBackbone();
        connectivityInspectionBackbone();
        populateAthleticFields();
        populateAuditoriums();
        populateGyms();
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

    private void connectivityInspection() {
        // Ensure that the school can be traversed
        ConnectivityInspector<Room, DefaultEdge> inspector = new ConnectivityInspector<>(schoolConnect);
        List<Set<Room>> connectedSets = inspector.connectedSets();
        Room connectorRoom = findCentralRoom();

        if(connectedSets.size() > 1) {
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
                if(connectorRoom.getConnections() == 0) {
                    connectorRoom = findCentralRoom();
                }
            }
        }
    }

    private Room findCentralRoom() {
        Room[] hallways = roomPool[10];
        Room[] courtyards = roomPool[7];
        int choice = setRandom(0,2);
        int count = 0;

        if(choice == 0) {
            do {
                choice = setRandom(0, hallways.length - 1);
                System.out.println("Connecting halls...");
                count++;
            } while (hallways[choice].getConnections() == 0 && count < calculateExpectedCycles(hallways.length));
            // Add a connection to a random hallway if no connections are left
            hallways[choice].setConnections(hallways[choice].getConnections() + 1);
            return hallways[choice];
        } else {
            do {
                choice = setRandom(0,courtyards.length - 1);
                System.out.println("Connecting courtyards...");
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

    private void populateAthleticFields() {
        Room[] athleticFields = roomPool[1];
        Room[] lockerRooms = roomPool[12];

        for (Room field: athleticFields) {
            Room connectRoom = findCentralRoom();
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

    private void populateAuditoriums() {
        Room[] auditoriums = roomPool[2];

        for (Room auditorium: auditoriums) {
            Room connectRoom = findCentralRoom();
            schoolConnect.addEdge(auditorium, connectRoom);
            connectRoom.setConnections(connectRoom.getConnections() - 1);
            auditorium.setConnections(auditorium.getConnections() - 1);
        }
    }

    private void populateGyms() {
        Room[] gyms = roomPool[9];
        Room[] lockerRooms = roomPool[12];

        for (Room gym: gyms) {
            Room connectRoom = findCentralRoom();
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

    private void constructBackbone() {
        Room[] hallways = roomPool[10];
        Room[] courtyards = roomPool[7];

        for (Room hallway : hallways) {
            int choice = setRandom(0, 2);

            if (choice == 0 && hallways.length > 1) {
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
                .filter(set -> set.stream().anyMatch(room -> isHallwayOrCourtyard(room)))
                .collect(Collectors.toList());

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
        return Arrays.asList(roomPool[10], roomPool[7]).stream().flatMap(Arrays::stream).anyMatch(r -> r.equals(room));
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

    private int setRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
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
    public void visualizer() {
        JGraphXAdapter<Room, DefaultEdge> graphAdapter = new JGraphXAdapter<>(schoolConnect);
        JFrame frame = new JFrame("School Rooms Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
