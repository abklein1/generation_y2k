package utility;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import entity.Student;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import javax.swing.*;

import entity.StandardSchool;
import static constants.SimConstants.*;

public class SocialLinkConnector {

    // Directed graph to maintain distinct edges for each direction
    Graph<Student, DefaultWeightedEdge> socialGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    private JSlider zoomSlider;
    private mxGraph graph;
    private mxGraphComponent graphComponent;
    private Random random = new Random(); // Single Random instance
    private HashMap<Student, Object> vertexToCellMap = new HashMap<>();


    /**
     * Constructor to initialize social links.
     *
     * @param studentHashMap  HashMap of students.
     * @param standardSchool  The standard school entity.
     */
    public SocialLinkConnector(HashMap<Integer, Student> studentHashMap, StandardSchool standardSchool) {
        this(); // Call the default constructor to initialize graphComponent
        initializeSocialLinks(studentHashMap, standardSchool);
    }


    /**
     * Default constructor initializing graph components.
     */
    public SocialLinkConnector() {
        graph = new mxGraph();
        graphComponent = new mxGraphComponent(graph);
        zoomSlider = new JSlider();

        // Existing initialization code
        graphComponent.zoomActual();
        zoomSlider.setValue(100);

        // Removed applyEnhancedLayout() and scaleGraph() as per request
    }


    /**
     * Initializes social links between students.
     *
     * @param studentHashMap  HashMap of students.
     * @param standardSchool  The standard school entity.
     */
    public void initializeSocialLinks(HashMap<Integer, Student> studentHashMap, StandardSchool standardSchool) {
        
        if (studentHashMap == null || standardSchool == null) {
            throw new IllegalArgumentException("Student hash map and standard school cannot be null.");
        }

        for (Student student : studentHashMap.values()) {
            ArrayList<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();
            
            // Add sibling relationships
            for (Student sibling : siblingsInSchool) {
                if (sibling == null) {
                    continue; // Skip if sibling is null
                }

                // Add vertices if they don't already exist
                socialGraph.addVertex(student);
                socialGraph.addVertex(sibling);
                
                // Add edge from Student to Sibling if it doesn't already exist
                addEdgeWithCheck(student, sibling);

                // Add edge from Sibling to Student if it doesn't already exist
                addEdgeWithCheck(sibling, student);
            }

            // Set maximum number of best friends for the student
            setMaxBestFriends(student);
            
            // Initialize the simulation with some best friends for each student
            for (int i = 0; i < student.studentStatistics.getMaxBestFriends(); i++) {
                // Students are most likely to befriend other students from the same grade
                if (random.nextInt(SOCIAL_LINK_FRIEND_INITIAL_SAMPLE_SIZE) < SOCIAL_LINK_FRIEND_INITIAL_THRESHOLD) {
                    Student potentialFriend = findPotentialFriend(student, standardSchool);
                    
                    if (potentialFriend == null) {
                        continue; // Skip to the next iteration
                    }

                    // Ensure valid potential friend and no existing edge in either direction
                    if (!student.studentStatistics.getFriendsInSchool().contains(potentialFriend) && 
                        !socialGraph.containsEdge(student, potentialFriend) && 
                        !socialGraph.containsEdge(potentialFriend, student) && 
                        !student.equals(potentialFriend)) {
                        
                        // Add best friend relationship
                        student.studentStatistics.addFriendInSchool(potentialFriend);
                        
                        // Add vertices if they don't already exist
                        socialGraph.addVertex(student);
                        socialGraph.addVertex(potentialFriend);
                        
                        // Add edge from Student to Potential Friend
                        addEdgeWithCheck(student, potentialFriend);
                        
                        // Optionally, add edge from Potential Friend to Student
                        // Uncomment the following line if mutual perception is needed
                        // addEdgeWithCheck(potentialFriend, student);
                    }
                }
            }
        }

        // After initializing all social links, visualize the graph
        schoolSocialLinkVisualizer();
    }

    /**
     * Adds an edge from source to target if it doesn't already exist.
     *
     * @param source The source student.
     * @param target The target student.
     */
    private void addEdgeWithCheck(Student source, Student target) {
        if (!socialGraph.containsEdge(source, target)) {
            DefaultWeightedEdge edge = socialGraph.addEdge(source, target);
            if (edge != null) {
                socialGraph.setEdgeWeight(edge, assignInitialWeight());
            } else {
                System.out.println("Failed to add edge from " + source + " to " + target);
            }
        } else {
            System.out.println("Edge from " + source + " to " + target + " already exists.");
        }
    }

    /**
     * Sets the maximum number of best friends for a student based on attributes.
     *
     * @param student The student.
     */
    private void setMaxBestFriends(Student student) {
        int charisma = student.studentStatistics.getCharisma();
        int luck = student.studentStatistics.getLuck();
        int empathy = student.studentStatistics.getEmpathy();

        // Calculate a composite score based on attributes and their modifiers
        double compositeScore = (charisma * SOCIAL_LINK_FRIEND_CHARISMA_MODIFIER) +
                                (empathy * SOCIAL_LINK_FRIEND_EMPATHY_MODIFIER) +
                                (luck * SOCIAL_LINK_FRIEND_LUCK_MODIFIER);

        // Introduce variability to avoid deterministic outcomes
        double variabilityFactor = 1 + (random.nextDouble() * SOCIAL_LINK_FRIEND_VARIABILITY_RANGE) - (SOCIAL_LINK_FRIEND_VARIABILITY_RANGE / 2);

        // Apply variability to the composite score
        double variedScore = compositeScore * variabilityFactor;

        // Normalize the varied score to a range between 0 and 1
        double normalizedScore = Math.max(0, Math.min(1, variedScore / SOCIAL_LINK_FRIEND_SCALING_FACTOR));

        // Map the normalized score to the desired range of max best friends

        // Calculate maxBestFriends based on the normalized score
        int maxBestFriends = (int) Math.round(SOCIAL_LINK_FRIEND_MAXIMUM * normalizedScore);

        student.studentStatistics.setMaxBestFriends(maxBestFriends);
    }

    /**
     * Finds a potential friend for a student based on grade level.
     *
     * @param student         The student seeking friends.
     * @param standardSchool  The standard school entity.
     * @return                A potential friend or null if none found.
     */
    private Student findPotentialFriend(Student student, StandardSchool standardSchool) {
        String gradeLevel = student.studentStatistics.getGradeLevel();
        ArrayList<Student> potentialFriends = new ArrayList<>();

        if (random.nextInt(SOCIAL_LINK_FRIEND_GRADE_CLASSMATE_SAMPLE_SIZE) < SOCIAL_LINK_FRIEND_GRADE_CLASSMATE_THRESHOLD) {
            // Prefer same grade friends
            HashMap<Integer, Student> gradeClassmates = standardSchool.getStudentGradeClass(gradeLevel);
            if (gradeClassmates != null) {
                for (Student otherStudent : gradeClassmates.values()) {
                    if (otherStudent.studentStatistics.getGradeLevel().equals(gradeLevel) && 
                        !otherStudent.equals(student)) {    
                        potentialFriends.add(otherStudent);
                    }
                }
            }
        } else {
            // Consider adjacent or other grades
            if (random.nextInt(SOCIAL_LINK_FRIEND_ADJACENT_GRADE_SAMPLE_SIZE) < SOCIAL_LINK_FRIEND_ADJACENT_GRADE_THRESHOLD) {
                String[] adjacentGrades = getAdjacentGrades(gradeLevel);
                for (String grade : adjacentGrades) {
                    HashMap<Integer, Student> adjacentGradeClassmates = standardSchool.getStudentGradeClass(grade);
                    if (adjacentGradeClassmates != null) {
                        potentialFriends.addAll(adjacentGradeClassmates.values());
                    }
                }
            } else {
                String[] otherGrades = getOtherGrades(gradeLevel);
                for (String grade : otherGrades) {
                    HashMap<Integer, Student> otherGradeClassmates = standardSchool.getStudentGradeClass(grade);
                    if (otherGradeClassmates != null) {
                        potentialFriends.addAll(otherGradeClassmates.values());
                    }
                }
            }
        }

        if (potentialFriends.isEmpty()) {
            return null;
        }

        return potentialFriends.get(random.nextInt(potentialFriends.size()));
    }

    // Helper methods to determine adjacent and other grades
    private String[] getAdjacentGrades(String gradeLevel) {
        return switch (gradeLevel) {
            case "Freshman" -> new String[]{"Sophomore"};
            case "Senior" -> new String[]{"Junior"};
            default -> new String[]{};
        };
    }

    private String[] getOtherGrades(String gradeLevel) {
        return switch (gradeLevel) {
            case "Freshman" -> new String[]{"Junior", "Senior"};
            case "Sophomore" -> new String[]{"Freshman", "Junior", "Senior"};
            case "Junior" -> new String[]{"Freshman", "Sophomore", "Senior"};
            case "Senior" -> new String[]{"Freshman", "Sophomore"};
            default -> new String[]{};
        };
    }

    /**
     * Assigns an initial weight to an edge.
     *
     * @return The initial weight.
     */
    private double assignInitialWeight() {
        // Example: Assign a random weight between 1.0 and 5.0
        return 1.0 + (4.0 * random.nextDouble());
    }


    /**
     * Visualizes the social graph using mxGraph.
     */
    public void schoolSocialLinkVisualizer() {
        // Use JGraphXAdapter to adapt JGraphT graph to JGraphX
        JGraphXAdapter<Student, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(socialGraph);
        graphAdapter.setAllowDanglingEdges(false);

        graph = new mxGraph();
        graph.getModel().beginUpdate();
        try {
            Object parent = graph.getDefaultParent();

            // Insert all vertices and map them to mxCells
            for (Student student : socialGraph.vertexSet()) {
                Object cell = graph.insertVertex(parent, null, student.toString(), 0, 0, 30, 30);
                vertexToCellMap.put(student, cell);
            }

            // Insert all edges using the mapped mxCells
            for (DefaultWeightedEdge edge : socialGraph.edgeSet()) {
                Student source = socialGraph.getEdgeSource(edge);
                Student target = socialGraph.getEdgeTarget(edge);
                Object sourceCell = vertexToCellMap.get(source);
                Object targetCell = vertexToCellMap.get(target);

                // Ensure that both source and target cells exist
                if (sourceCell != null && targetCell != null) {
                    graph.insertEdge(parent, null, "", sourceCell, targetCell);
                } else {
                    System.err.println("Source or Target cell is null for edge: " + edge);
                }
            }

        } finally {
            graph.getModel().endUpdate();
        }

        // Apply layout (mxCircleLayout for evenly spaced nodes)
        mxCircleLayout layout = new mxCircleLayout(graph);
        layout.setRadius(150); // Adjust radius as needed
        layout.execute(graph.getDefaultParent());

        // Update the graph in the component
        graphComponent.setGraph(graph);
        graphComponent.refresh();

        // Additional visualization settings can be applied here
    }


    public void studentVisualizer(Student student) {
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();

        // Create a subgraph for the specific student and their connections
        Graph<Student, DefaultEdge> subGraph = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        subGraph.addVertex(student);

        for (DefaultWeightedEdge edge : socialGraph.edgesOf(student)) {
            Student source = socialGraph.getEdgeSource(edge);
            Student target = socialGraph.getEdgeTarget(edge);
            subGraph.addVertex(source);
            subGraph.addVertex(target);
            subGraph.addEdge(source, target);
            subGraph.setEdgeWeight(source, target, socialGraph.getEdgeWeight(edge));
        }

        JGraphXAdapter<Student, DefaultEdge> graphAdapter = new JGraphXAdapter<>(subGraph);
        JFrame frame = new JFrame(studentName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        frame.add(graphComponent);
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        frame.pack();
        frame.setVisible(true);
    }
}