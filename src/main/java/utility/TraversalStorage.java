package utility;

import entity.Student;
import entity.StudentBlock;
import entity.StudentSchedule;
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
 * <p>
 * There are exactly {@value #NUM_TRANSITIONS} transition boundaries per day
 * (Period 1→2, 2→3, 3→4).  Each student's path list is a fixed-size array
 * where index {@code i} holds the path for the transition after period
 * {@code i + 1}.  Slots where the student has no class on one or both sides
 * of the boundary contain an empty list.
 */
public class TraversalStorage {

    static final int NUM_PERIODS = 4;
    static final int NUM_TRANSITIONS = NUM_PERIODS - 1;

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
     * Gets the transition path for a student for a specific transition index
     * within a semester.
     *
     * @param student          the student
     * @param transitionIndex  0-based transition number (0 = P1→P2, 1 = P2→P3, 2 = P3→P4)
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
            StudentSchedule schedule = student.studentStatistics.getStudentSchedule();

            List<List<Room>> fallPaths = buildSemesterPaths(
                    student, schedule, "Fall", schoolConnect);
            List<List<Room>> springPaths = buildSemesterPaths(
                    student, schedule, "Spring", schoolConnect);

            studentPathsFall.put(student, fallPaths);
            studentPathsSpring.put(student, springPaths);
        }
    }

    /**
     * Builds a fixed-size list of {@value #NUM_TRANSITIONS} transition paths
     * for one student in one semester.  Slot {@code i} is the path from the
     * student's period {@code i+1} room to their period {@code i+2} room.
     * If either period is unscheduled the slot is an empty list.
     */
    private List<List<Room>> buildSemesterPaths(Student student,
                                                StudentSchedule schedule,
                                                String semester,
                                                Graph<Room, DefaultEdge> schoolConnect) {
        List<List<Room>> paths = new ArrayList<>(NUM_TRANSITIONS);
        String studentLabel = student.studentName.getFirstName() + " "
                + student.studentName.getLastName();

        for (int t = 0; t < NUM_TRANSITIONS; t++) {
            int fromPeriod = t + 1;
            int toPeriod = t + 2;

            StudentBlock fromBlock = schedule.getByBlockNumber(fromPeriod, semester);
            StudentBlock toBlock = schedule.getByBlockNumber(toPeriod, semester);

            if (fromBlock == null || toBlock == null) {
                paths.add(Collections.emptyList());
                continue;
            }

            Room source = fromBlock.getRoom();
            Room dest = toBlock.getRoom();

            if (source == null || dest == null) {
                paths.add(Collections.emptyList());
                continue;
            }
            if (!schoolConnect.containsVertex(source)
                    || !schoolConnect.containsVertex(dest)) {
                GameLogger.logDebug("Skipping path for student " + studentLabel
                        + " because a scheduled room is not in the school graph. source="
                        + describeRoom(source) + " (inGraph="
                        + schoolConnect.containsVertex(source) + "), sink="
                        + describeRoom(dest) + " (inGraph="
                        + schoolConnect.containsVertex(dest) + ")");
                paths.add(Collections.emptyList());
                continue;
            }

            GraphPath<Room, DefaultEdge> path =
                    DijkstraShortestPath.findPathBetween(schoolConnect, source, dest);
            if (path != null) {
                ArrayList<Room> roomList = new ArrayList<>(path.getVertexList());
                GameLogger.logDebug(semester + " transition " + fromPeriod + "→" + toPeriod
                        + " path: " + roomList + " for student "
                        + student.studentName.getFirstName());
                paths.add(roomList);
            } else {
                GameLogger.logDebug("No path found between " + describeRoom(source)
                        + " and " + describeRoom(dest) + " for student "
                        + studentLabel);
                paths.add(Collections.emptyList());
            }
        }
        return paths;
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
