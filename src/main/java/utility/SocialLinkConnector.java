package utility;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import entity.Student;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import entity.StandardSchool;
import static constants.SimConstants.*;

public class SocialLinkConnector {

    Graph<Student, DefaultWeightedEdge> socialGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    public SocialLinkConnector(HashMap<Integer, Student> studentHashMap, StandardSchool standardSchool) {
        initializeSocialLinks(studentHashMap, standardSchool);
    }

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
                    if (!student.studentStatistics.getFriendsInSchool().contains(potentialFriend) && !socialGraph.containsEdge(student, potentialFriend) && potentialFriend != student) {
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
            // If not a grade classmate students are more likely to befriend students from nearby grades
            if (random.nextInt(SOCIAL_LINK_ADJACENT_GRADE_SAMPLE_SIZE) < SOCIAL_LINK_ADJACENT_GRADE_THRESHOLD) {
                String[] adjacentGrades = gradeLevel.equals("Freshman") ? new String[] {"Sophomore"} : gradeLevel.equals("Senior") ? new String[] {"Junior"} : new String[] {"Freshman", "Sophomore"};
                for (String grade : adjacentGrades) {
                    HashMap<Integer, Student> adjacentGradeClassmates = standardSchool.getStudentGradeClass(grade);
                    potentialFriends.addAll(adjacentGradeClassmates.values());
                }
            } else {
                String[] otherGrades = gradeLevel.equals("Freshman") ? new String[] {"Junior", "Senior"} : gradeLevel.equals("Sophomore") ? new String[] {"Freshman", "Junior"} : new String[] {"Sophomore", "Senior"};
                for (String grade : otherGrades) {
                    HashMap<Integer, Student> otherGradeClassmates = standardSchool.getStudentGradeClass(grade);
                    potentialFriends.addAll(otherGradeClassmates.values());
                }
            }
        }
        return potentialFriends.get(random.nextInt(potentialFriends.size()));
    }

    private void setMaxBestFriends(Student student) {
        int charisma = student.studentStatistics.getCharisma();
        int luck = student.studentStatistics.getLuck();
        int empathy = student.studentStatistics.getEmpathy();

        Random random = new Random();
        double variability = random.nextDouble() * SOCIAL_LINK_FRIEND_VARIABILITY;
        int maxBestFriends = (int) Math.min(SOCIAL_LINK_FRIEND_MAXIMUM, ((charisma * SOCIAL_LINK_FRIEND_CHARISMA_MODIFIER) + (empathy * SOCIAL_LINK_FRIEND_EMPATHY_MODIFIER) + (luck * SOCIAL_LINK_FRIEND_LUCK_MODIFIER)) * (1 + variability));
        student.studentStatistics.setMaxBestFriends(maxBestFriends);
    }

    private double assignInitialWeight() {
        Random distribution = new Random();
        return (distribution.nextGaussian() * SOCIAL_LINK_STANDARD_DEVIATION + SOCIAL_LINK_MEAN);
    }

    public Graph<Student, DefaultWeightedEdge> getSocialGraph() {
        return socialGraph;
    }

    public void setEdgeWeight(Student student1, Student student2, double weight) {
        DefaultWeightedEdge edge = socialGraph.getEdge(student1, student2);
        if (edge != null) {
            socialGraph.setEdgeWeight(edge, weight);
        }
    }

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

        // Adjust layout settings
        mxFastOrganicLayout layout = new mxFastOrganicLayout(graphAdapter);
        layout.setForceConstant(150); // Increase the force constant for better spacing
        layout.setMinDistanceLimit(100); // Set minimum distance between nodes
        layout.setMaxDistanceLimit(200); // Set maximum distance between nodes
        layout.execute(graphAdapter.getDefaultParent());

        frame.pack();
        frame.setVisible(true);
    }
}
