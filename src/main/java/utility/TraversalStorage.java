package utility;

import entity.Rooms.Room;
import entity.Student;
import entity.StudentBlock;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import view.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

public class TraversalStorage {
    
    HashMap<Student, ArrayList<Room>> studentPathsFall;
    HashMap<Student, ArrayList<Room>> studentPathsSpring;

    // Block mappings Fall: 1->3->5->7 Spring: 2->4->6->8
    public TraversalStorage(HashMap<Integer, Student> students, GameView view, RoomConnector roomConnector) {
        studentPathsFall = new HashMap<>();
        studentPathsSpring = new HashMap<>();
        initializeStudentPaths(students, view, roomConnector);
    }

    public HashMap<Student, ArrayList<Room>> getStudentPathsFall() {
        return studentPathsFall;
    }

    public HashMap<Student, ArrayList<Room>> getStudentPathsSpring() {
        return studentPathsSpring;
    }

    public void setStudentPathsFall(HashMap<Student, ArrayList<Room>> studentPathsFall) {
        this.studentPathsFall = studentPathsFall;
    }

    public void setStudentPathsSpring(HashMap<Student, ArrayList<Room>> studentPathsSpring) {
        this.studentPathsSpring = studentPathsSpring;
    }

    // TODO: Turn off view.appendText when done debugging
    public void initializeStudentPaths(HashMap<Integer, Student> students, GameView view, RoomConnector roomConnector) {
        Graph<Room, DefaultEdge> schoolConnect = roomConnector.getSchoolConnect();

        for (Student student : students.values()) {
            List<StudentBlock> blocks = student.studentStatistics.getStudentSchedule().getClassSchedule();
            for (int i = 0; i < blocks.size(); i++) {
                StudentBlock block = blocks.get(i);
                Room room = block.getRoom();
                // Ensure we do not go out of bounds
                if (i + 2 < blocks.size()) {
                    Room nextRoom = blocks.get(i + 2).getRoom();
                    if (room != null && nextRoom != null) {
                        GraphPath<Room, DefaultEdge> path = DijkstraShortestPath.findPathBetween(schoolConnect, room, nextRoom);
                        if (path != null) {
                            ArrayList<Room> roomList = new ArrayList<>(path.getVertexList());
                            if (block.getSemester().equals("Fall")) {
                                studentPathsFall.put(student, roomList);
                            } else {
                                studentPathsSpring.put(student, roomList);
                            }
                        }
                    }
                }
            }
        }
    }
}
