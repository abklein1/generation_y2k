package utility;


import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.*;
import com.mxgraph.view.mxGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import entity.Student;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import entity.StandardSchool;
import static constants.SimConstants.*;

public class SocialLinkConnector {

    Graph<Student, DefaultWeightedEdge> socialGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    private JSlider zoomSlider;
    private mxGraph graph;
    private mxGraphComponent graphComponent;

    /**
     * Constructor to initialize social links.
     *
     * @param studentHashMap  HashMap of students.
     * @param standardSchool  The standard school entity.
     */
    public SocialLinkConnector(HashMap<Integer, Student> studentHashMap, StandardSchool standardSchool) {
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
        
        Random random = new Random();
        
        for (Student student : studentHashMap.values()) {
            ArrayList<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();
            
            for (Student sibling : siblingsInSchool) {
                // Add vertices
                socialGraph.addVertex(student);
                socialGraph.addVertex(sibling);
                
                // Add edge from Student to Sibling
                DefaultWeightedEdge edgeToSibling = socialGraph.addEdge(student, sibling);
                if (edgeToSibling != null) {
                    socialGraph.setEdgeWeight(edgeToSibling, assignInitialWeight());
                } else {
                    System.out.println("Edge from " + student + " to " + sibling + " is null");
                }
                
                // Add edge from Sibling to Student
                DefaultWeightedEdge edgeFromSibling = socialGraph.addEdge(sibling, student);
                if (edgeFromSibling != null) {
                    socialGraph.setEdgeWeight(edgeFromSibling, assignInitialWeight());
                } else {
                    System.out.println("Edge from " + sibling + " to " + student + " is null");
                }
            }

            setMaxBestFriends(student);
            
            // Initialize the simulation with some friends for each student
            for (int i = 0; i < student.studentStatistics.getMaxBestFriends(); i++) {
                // Students are most likely to befriend other students from same grade
                Student potentialFriend;
                if (random.nextInt(SOCIAL_LINK_FRIEND_INITIAL_SAMPLE_SIZE) < SOCIAL_LINK_FRIEND_INITIAL_THRESHOLD) {
                    potentialFriend = findPotentialFriend(student, standardSchool);
                    if (!student.studentStatistics.getFriendsInSchool().contains(potentialFriend) && 
                        !socialGraph.containsEdge(student, potentialFriend) && 
                        potentialFriend != student) {
                        
                        student.studentStatistics.addFriendInSchool(potentialFriend);
                        
                        // Add vertices
                        socialGraph.addVertex(student);
                        socialGraph.addVertex(potentialFriend);
                        
                        // Add edge from Student to Potential Friend
                        DefaultWeightedEdge edgeToFriend = socialGraph.addEdge(student, potentialFriend);
                        if (edgeToFriend != null) {
                            socialGraph.setEdgeWeight(edgeToFriend, assignInitialWeight());
                        } else {
                            System.out.println("Edge from " + student + " to " + potentialFriend + " is null");
                        }
                        
                        // Add edge from Potential Friend to Student
                        DefaultWeightedEdge edgeFromFriend = socialGraph.addEdge(potentialFriend, student);
                        if (edgeFromFriend != null) {
                            socialGraph.setEdgeWeight(edgeFromFriend, assignInitialWeight());
                        } else {
                            System.out.println("Edge from " + potentialFriend + " to " + student + " is null");
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds a potential friend for a student based on grade level.
     *
     * @param student         The student seeking friends.
     * @param standardSchool  The standard school entity.
     * @return                A potential friend.
     */
    private Student findPotentialFriend(Student student, StandardSchool standardSchool) {
        String gradeLevel = student.studentStatistics.getGradeLevel();
        Random random = new Random();
        HashMap<Integer, Student> gradeClassmates = standardSchool.getStudentGradeClass(gradeLevel);
        ArrayList<Student> potentialFriends = new ArrayList<>();
        if (random.nextInt(SOCIAL_LINK_GRADE_CLASSMATE_SAMPLE_SIZE) < SOCIAL_LINK_GRADE_CLASSMATE_THRESHOLD) {
            for (Student otherStudent : gradeClassmates.values()) {
                if (otherStudent.studentStatistics.getGradeLevel().equals(gradeLevel)) {    
                    potentialFriends.add(otherStudent);
                }
            }
        } else {
            // If not a grade classmate, students are more likely to befriend students from nearby grades
            if (random.nextInt(SOCIAL_LINK_ADJACENT_GRADE_SAMPLE_SIZE) < SOCIAL_LINK_ADJACENT_GRADE_THRESHOLD) {
                String[] adjacentGrades = gradeLevel.equals("Freshman") ? new String[] {"Sophomore"} 
                                      : gradeLevel.equals("Senior") ? new String[] {"Junior"} 
                                      : new String[] {"Freshman", "Sophomore"};
                for (String grade : adjacentGrades) {
                    HashMap<Integer, Student> adjacentGradeClassmates = standardSchool.getStudentGradeClass(grade);
                    potentialFriends.addAll(adjacentGradeClassmates.values());
                }
            } else {
                String[] otherGrades = gradeLevel.equals("Freshman") ? new String[] {"Junior", "Senior"} 
                                      : gradeLevel.equals("Sophomore") ? new String[] {"Freshman", "Junior"} 
                                      : new String[] {"Sophomore", "Senior"};
                for (String grade : otherGrades) {
                    HashMap<Integer, Student> otherGradeClassmates = standardSchool.getStudentGradeClass(grade);
                    potentialFriends.addAll(otherGradeClassmates.values());
                }
            }
        }
        return potentialFriends.get(random.nextInt(potentialFriends.size()));
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

        Random random = new Random();

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
        int maxFriends = SOCIAL_LINK_FRIEND_MAXIMUM;

        // Calculate maxBestFriends based on the normalized score
        int maxBestFriends = (int) Math.round(maxFriends * normalizedScore);

        student.studentStatistics.setMaxBestFriends(maxBestFriends);
    }


    /**
     * Assigns an initial weight to an edge using a Gaussian distribution.
     *
     * @return The assigned weight.
     */
    private double assignInitialWeight() {
        Random distribution = new Random();
        return (distribution.nextGaussian() * SOCIAL_LINK_STANDARD_DEVIATION + SOCIAL_LINK_MEAN);
    }

    /**
     * Retrieves the social graph.
     *
     * @return The social graph.
     */
    public Graph<Student, DefaultWeightedEdge> getSocialGraph() {
        return socialGraph;
    }

    /**
     * Sets the weight of an edge between two students.
     *
     * @param student1 The first student.
     * @param student2 The second student.
     * @param weight   The weight to set.
     */
    public void setEdgeWeight(Student student1, Student student2, double weight) {
        DefaultWeightedEdge edge = socialGraph.getEdge(student1, student2);
        if (edge != null) {
            socialGraph.setEdgeWeight(edge, weight);
        }
    }

    /**
     * Visualizes a specific student's social connections.
     *
     * @param student The student to visualize.
     */
    public void studentVisualizer(Student student) {
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();

        // Create a subgraph for the specific student and their connections
        Graph<Student, DefaultWeightedEdge> subGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        subGraph.addVertex(student);

        for (DefaultWeightedEdge edge : socialGraph.edgesOf(student)) {
            Student source = socialGraph.getEdgeSource(edge);
            Student target = socialGraph.getEdgeTarget(edge);
            subGraph.addVertex(source);
            subGraph.addVertex(target);
            DefaultWeightedEdge subEdge = subGraph.addEdge(source, target);
            subGraph.setEdgeWeight(subEdge, socialGraph.getEdgeWeight(edge));
        }

        JGraphXAdapter<Student, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(subGraph);
        JFrame frame = new JFrame(studentName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        frame.add(graphComponent);

        // Adjust layout settings using mxFastOrganicLayout with increased spacing
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graphAdapter);
        layout.setForceConstant(150); // Increase the force constant for better spacing
        layout.setMinDistanceLimit(100); // Set minimum distance between nodes
        layout.setMaxDistanceLimit(200); // Set maximum distance between nodes
        layout.execute(graphAdapter.getDefaultParent());

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Visualizes the entire social graph of the school with zoom functionality.
     *
     * @param standardSchool The standard school entity.
     */
    public void schoolSocialLinkVisualizer(StandardSchool standardSchool) {
        // Create the adapter and graph component
        String schoolName = standardSchool.getSchoolName();
        JGraphXAdapter<Student, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(socialGraph);
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        graphComponent.setConnectable(false);
        graphComponent.getGraph().setAllowDanglingEdges(false);

        // Initialize frame
        JFrame frame = new JFrame(schoolName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Apply mxCircleLayout instead of mxFastOrganicLayout for increased spacing
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.setRadius(5); // Adjust radius to increase distance between nodes
        layout.execute(graphAdapter.getDefaultParent());

        // Create toolbar with zoom controls
        JToolBar toolBar = new JToolBar();
        JButton zoomInButton = new JButton("Zoom In");
        JButton zoomOutButton = new JButton("Zoom Out");
        JButton resetZoomButton = new JButton("Reset Zoom");

        // Add zoom in action
        zoomInButton.addActionListener(e -> {
            graphComponent.zoomIn();
            updateZoomSlider(zoomSlider, graphComponent);
        });

        // Add zoom out action
        zoomOutButton.addActionListener(e -> {
            graphComponent.zoomOut();
            updateZoomSlider(zoomSlider, graphComponent);
        });

        // Add reset zoom action
        resetZoomButton.addActionListener(e -> {
            graphComponent.zoomActual();
            zoomSlider.setValue(100);
        });

        toolBar.add(zoomInButton);
        toolBar.add(zoomOutButton);
        toolBar.add(resetZoomButton);

        // Initialize the class member zoomSlider instead of creating a new local variable
        zoomSlider = new JSlider(10, 400, 100); // Represents 10% to 400%
        zoomSlider.setPreferredSize(new Dimension(150, 20));
        zoomSlider.setToolTipText("Zoom");
        zoomSlider.addChangeListener(e -> {
            if (!zoomSlider.getValueIsAdjusting()) {
                double scale = zoomSlider.getValue() / 100.0;
                graphComponent.zoomTo(scale, true);
            }
        });
        toolBar.add(new JLabel("Zoom: "));
        toolBar.add(zoomSlider);

        // Add the toolbar to the north
        frame.add(toolBar, BorderLayout.NORTH);

        // Implement Mouse Wheel Zooming
        graphComponent.addMouseWheelListener(e -> {
            if (e.isControlDown()) { // Zoom only when Ctrl is pressed
                if (e.getWheelRotation() < 0) {
                    graphComponent.zoomIn();
                    zoomSlider.setValue(Math.min(zoomSlider.getValue() + 10, zoomSlider.getMaximum()));
                } else {
                    graphComponent.zoomOut();
                    zoomSlider.setValue(Math.max(zoomSlider.getValue() - 10, zoomSlider.getMinimum()));
                }
                e.consume();
            }
        });

        // Listen for SCALE events to update the slider
        graphComponent.getGraph().getView().addListener(mxEvent.SCALE, new mxEventSource.mxIEventListener() {
            @Override
            public void invoke(Object sender, mxEventObject evt) {
                double currentScale = graphComponent.getGraph().getView().getScale();
                int sliderValue = (int) (currentScale * 100);
                zoomSlider.setValue(sliderValue);
            }
        });

        // Add Keyboard Shortcuts
        addKeyboardShortcuts(graphComponent, zoomSlider);

        // Add Context Menu for Zooming
        addContextMenu(graphComponent, zoomSlider);

        // Finalize and display the frame
        frame.add(graphComponent, BorderLayout.CENTER);
        frame.setVisible(true);

        // Removed scaleGraph(2) as it's no longer necessary
    }

    /**
     * Adds keyboard shortcuts for zooming.
     *
     * @param graphComponent The graph component.
     * @param zoomSlider     The zoom slider.
     */
    private void addKeyboardShortcuts(mxGraphComponent graphComponent, JSlider zoomSlider) {
        // Key Bindings for Zoom In (Ctrl + +)
        graphComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke('+', InputEvent.CTRL_DOWN_MASK), "zoomIn");
        graphComponent.getActionMap().put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphComponent.zoomIn();
                zoomSlider.setValue(Math.min(zoomSlider.getValue() + 10, zoomSlider.getMaximum()));
            }
        });


        // Key Bindings for Zoom Out (Ctrl + -)
        graphComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke('-', InputEvent.CTRL_DOWN_MASK), "zoomOut");
        graphComponent.getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphComponent.zoomOut();
                zoomSlider.setValue(Math.max(zoomSlider.getValue() - 10, zoomSlider.getMinimum()));
            }
        });

        // Key Bindings for Reset Zoom (Ctrl + 0)
        graphComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke('0', InputEvent.CTRL_DOWN_MASK), "resetZoom");
        graphComponent.getActionMap().put("resetZoom", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphComponent.zoomActual();
                zoomSlider.setValue(100);
            }
        });
    }

    /**
     * Adds a context menu with zoom options.
     *
     * @param graphComponent The graph component.
     * @param zoomSlider     The zoom slider.
     */
    private void addContextMenu(mxGraphComponent graphComponent, JSlider zoomSlider) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem zoomInMenuItem = new JMenuItem("Zoom In");
        zoomInMenuItem.addActionListener(e -> {
            graphComponent.zoomIn();
            zoomSlider.setValue(Math.min(zoomSlider.getValue() + 10, zoomSlider.getMaximum()));
        });
        contextMenu.add(zoomInMenuItem);

        JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out");
        zoomOutMenuItem.addActionListener(e -> {
            graphComponent.zoomOut();
            zoomSlider.setValue(Math.max(zoomSlider.getValue() - 10, zoomSlider.getMinimum()));
        });
        contextMenu.add(zoomOutMenuItem);

        JMenuItem resetZoomMenuItem = new JMenuItem("Reset Zoom");
        resetZoomMenuItem.addActionListener(e -> {
            graphComponent.zoomActual();
            zoomSlider.setValue(100);
        });
        contextMenu.add(resetZoomMenuItem);

        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Updates the zoom slider based on the current scale.
     *
     * @param zoomSlider     The zoom slider.
     * @param graphComponent The graph component.
     */
    private void updateZoomSlider(JSlider zoomSlider, mxGraphComponent graphComponent) {
        double currentScale = graphComponent.getGraph().getView().getScale();
        int sliderValue = (int) (currentScale * 100);
        zoomSlider.setValue(sliderValue);
    }

    // Removed applyEnhancedLayout() and scaleGraph() methods as they are no longer needed
}