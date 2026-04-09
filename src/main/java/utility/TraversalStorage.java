package utility;

import entity.Student;
import entity.StudentBlock;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import view.GameView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import entity.Rooms.Room;

/**
 * Pre-computes and stores per-transition shortest paths for every student.
 * Each student can have multiple transition paths (one per pair of consecutive
 * classes within the same semester).
 * <p>
 * Storage: {@code Map<Student, List<List<Room>>>} -- a list of paths per
 * student, indexed by transition number (0-based). Each path is an ordered
 * list of rooms from source to destination (inclusive of both endpoints).
 */
public class TraversalStorage {

    private final HashMap<Student, List<List<Room>>> studentPathsFall;
    private final HashMap<Student, List<List<Room>>> studentPathsSpring;

    public TraversalStorage(HashMap<Integer, Student> students, GameView view, RoomConnector roomConnector) {
        studentPathsFall = new HashMap<>();
        studentPathsSpring = new HashMap<>();
        initializeStudentPaths(students, view, roomConnector);
    }

    public HashMap<Student, List<List<Room>>> getStudentPathsFall() {
        return studentPathsFall;
    }

    public HashMap<Student, List<List<Room>>> getStudentPathsSpring() {
        return studentPathsSpring;
    }

    /**
     * Gets the transition path for a student for a specific transition number
     * within a semester.
     *
     * @param student          the student
     * @param transitionIndex  0-based transition number
     * @param semester         "Fall" or "Spring"
     * @return the path as a list of rooms, or an empty list if not found
     */
    public List<Room> getPath(Student student, int transitionIndex, String semester) {
        HashMap<Student, List<List<Room>>> map =
                "Fall".equals(semester) ? studentPathsFall : studentPathsSpring;
        List<List<Room>> paths = map.get(student);
        if (paths == null || transitionIndex < 0 || transitionIndex >= paths.size()) {
            return Collections.emptyList();
        }
        return paths.get(transitionIndex);
    }

    private void initializeStudentPaths(HashMap<Integer, Student> students,
                                        GameView view, RoomConnector roomConnector) {
        Graph<Room, DefaultEdge> schoolConnect = roomConnector.getSchoolConnect();

        for (Student student : students.values()) {
            List<StudentBlock> blocks = student.studentStatistics.getStudentSchedule().getClassSchedule();

            List<List<Room>> fallPaths = new ArrayList<>();
            List<List<Room>> springPaths = new ArrayList<>();

            for (int i = 0; i < blocks.size(); i++) {
                StudentBlock block = blocks.get(i);
                Room room = block.getRoom();
                if (i + 2 >= blocks.size()) {
                    continue;
                }
                Room nextRoom = blocks.get(i + 2).getRoom();
                if (room == null || nextRoom == null) {
                    continue;
                }
                if (!schoolConnect.containsVertex(room) || !schoolConnect.containsVertex(nextRoom)) {
                    GameLogger.logDebug("Skipping path for student " + student.studentName.getFirstName() +
                            " " + student.studentName.getLastName() + " because a scheduled room is not in " +
                            "the school graph. source=" + describeRoom(room) + " (inGraph=" +
                            schoolConnect.containsVertex(room) + "), sink=" + describeRoom(nextRoom) +
                            " (inGraph=" + schoolConnect.containsVertex(nextRoom) + ")");
                    addEmptyPath(block.getSemester(), fallPaths, springPaths);
                    continue;
                }

                GraphPath<Room, DefaultEdge> path = DijkstraShortestPath.findPathBetween(
                        schoolConnect, room, nextRoom);
                if (path != null) {
                    ArrayList<Room> roomList = new ArrayList<>(path.getVertexList());
                    GameLogger.logDebug(block.getSemester() + " path: " + roomList
                            + " for student " + student.studentName.getFirstName());
                    if ("Fall".equals(block.getSemester())) {
                        fallPaths.add(roomList);
                    } else {
                        springPaths.add(roomList);
                    }
                } else {
                    GameLogger.logDebug("No path found between " + describeRoom(room) + " and " +
                            describeRoom(nextRoom) + " for student " +
                            student.studentName.getFirstName() + " " +
                            student.studentName.getLastName());
                    addEmptyPath(block.getSemester(), fallPaths, springPaths);
                }
            }

            if (!fallPaths.isEmpty()) {
                studentPathsFall.put(student, fallPaths);
            }
            if (!springPaths.isEmpty()) {
                studentPathsSpring.put(student, springPaths);
            }
        }
    }

    private void addEmptyPath(String semester, List<List<Room>> fallPaths,
                              List<List<Room>> springPaths) {
        if ("Fall".equals(semester)) {
            fallPaths.add(Collections.emptyList());
        } else {
            springPaths.add(Collections.emptyList());
        }
    }

    private String describeRoom(Room room) {
        if (room == null) {
            return "null";
        }
        String roomName = room.getRoomName();
        if (roomName == null || roomName.isBlank()) {
            return room.getClass().getSimpleName();
        }
        return roomName;
    }
}
