package utility;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import entity.Bathroom;
import entity.Room;
import entity.StandardSchool;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


// Procedural generation that builds the school by connecting rooms. Room connection starts
// by connecting hallways and courtyards at random, and then allows other connections to build
// off that backbone. jgrapht connectivity inspector ensures no dangling vertexes
// vertex is door
// edge is room
public class RoomConnector {
    private final Room[][] roomPool = new Room[11][];
    Graph<Room, DefaultEdge> schoolConnect = new Multigraph<>(DefaultEdge.class);

    public RoomConnector(StandardSchool standardSchool) {
        roomPool[0] = standardSchool.getBathrooms();
        roomPool[1] = standardSchool.getBreakrooms();
        roomPool[2] = standardSchool.getClassrooms();
        roomPool[3] = standardSchool.getComputerLabs();
        roomPool[4] = standardSchool.getCourtyards();
        roomPool[5] = standardSchool.getGyms();
        roomPool[6] = standardSchool.getHallways();
        roomPool[7] = standardSchool.getLibraries();
        roomPool[8] = standardSchool.getLunchrooms();
        roomPool[9] = standardSchool.getOffices();
        roomPool[10] = standardSchool.getUtilityrooms();

        connectRooms();
    }

    private void connectRooms() {
        populateVertex();
        constructBackbone();
        populateEdges();
        connectivityInspection();
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
        if (!inspector.isConnected()) {
            List<Set<Room>> connectedSets = inspector.connectedSets();
            Iterator<Set<Room>> iterator = connectedSets.iterator();

            while (iterator.hasNext()) {
                Set<Room> firstSet = iterator.next();
                Set<Room> nextSet = iterator.hasNext() ? iterator.next() : null;

                Room roomFromFirstSet = firstSet.iterator().next();
                Room roomFromNextSet = nextSet != null ? nextSet.iterator().next() : null;

                if (roomFromNextSet != null) {
                    while (roomFromFirstSet.getConnections() > 0 && roomFromNextSet.getConnections() > 0) {
                        schoolConnect.addEdge(roomFromFirstSet, roomFromNextSet);
                        roomFromFirstSet.setConnections(roomFromFirstSet.getConnections() - 1);
                        roomFromNextSet.setConnections(roomFromNextSet.getConnections() - 1);
                    }
                }
            }
        }
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

    private void constructBackbone() {
        Room[] hallways = roomPool[6];
        Room[] courtyards = roomPool[4];

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
