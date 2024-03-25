package utility;

import entity.Bathroom;
import entity.Room;
import entity.StandardSchool;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

// For now will build a graph that connects rooms at random. Later we can add some
// procedural generation to make a proper floor plan
// vertex is door
// edge is room
public class RoomConnector {
    Graph<Room, DefaultEdge> schoolConnect = new Multigraph<>(DefaultEdge.class);
    private Room[][] roomPool = new Room[11][];

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
        populateEdges();
    }

    private void populateVertex() {
        /* If we traverse 2D array and add vertexes and edges at the same time it
        will connect all rooms of the same type together first even if using a randomizer.
        Better to use separate
        */
        for (Room[] rooms : roomPool) {
            if (rooms != null) {
                for (Room room : rooms) {
                    schoolConnect.addVertex(room);
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
                            targetRoom = getRandomRoom(1, targetRoom);
                        }
                        schoolConnect.addEdge(roomPool[i][j], targetRoom);
                        roomPool[i][j].setConnections(roomPool[i][j].getConnections() - 1);
                        targetRoom.setConnections(targetRoom.getConnections() - 1);
                    }
                }
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

    private Room getRandomRoom(int startIdx, Room roomToAvoid) {
        int x = setRandom(startIdx, roomPool.length - 1);
        int y = setRandom(0, roomPool[x].length - 1);
        while (roomPool[x][y].equals(roomToAvoid)) {
            x = setRandom(startIdx, roomPool.length - 1);
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
}
