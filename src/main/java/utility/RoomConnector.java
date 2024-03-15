package utility;

import entity.Room;
import entity.StandardSchool;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

// For now will build a graph that connects rooms at random. Later we can add some
// procedural generation to make a proper floor plan
// vertex is door
// edge is room
public class RoomConnector {
    Graph<Room, DefaultEdge> schoolConnect = new Multigraph<>(DefaultEdge.class);
    Room[][] roomPool = new Room[11][];

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
        for (int i = 0; i < roomPool.length; i++) {
            for (int j = 0; j < roomPool[i].length; j++) {
                schoolConnect.addVertex(roomPool[i][j]);
            }
        }
    }

    private void populateEdges() {
        for (int i = 0; i < roomPool.length; i++) {
            for (int j = 0; j < roomPool[i].length; j++) {
                // If vertex1 has available connections
                if (roomPool[i][j].getConnections() > 0) {
                    int x = i;
                    int y = j;
                    // avoid loops in graph generation
                    while(x == i && y == j) {
                        x = setRandom(0, roomPool.length - 1);
                        y = setRandom(0, roomPool[x].length - 1);
                    }
                    // If vertex2 has available connections
                    if (roomPool[x][y].getConnections() > 0) {
                        schoolConnect.addEdge(roomPool[i][j], roomPool[x][y]);
                        roomPool[i][j].setConnections(roomPool[i][j].getConnections() - 1);
                        roomPool[x][y].setConnections(roomPool[x][y].getConnections() - 1);
                    }
                }
            }
        }
    }

    private int setRandom(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max + 1);
        return random;
    }

    // Perform simple print for now
    public void getConnections() {
        Iterator<Room> iterator = new DepthFirstIterator<>(schoolConnect);

        while (iterator.hasNext()) {
            Room room = iterator.next();
            System.out.println("Room " + room.getRoomName() + " is connected to " + schoolConnect.edgesOf(room));
        }
    }
}
