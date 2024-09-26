package utility;

import entity.Student;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.ext.JGraphXAdapter;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.layout.mxFastOrganicLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static constants.SimConstants.SOCIAL_LINK_MEAN;
import static constants.SimConstants.SOCIAL_LINK_STANDARD_DEVIATION;

import javax.swing.JFrame;

public class SocialLinkConnector {
    
    Graph<Student, DefaultWeightedEdge> socialGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    public SocialLinkConnector(HashMap<Integer, Student> studentHashMap) {
        initializeSocialLinks(studentHashMap);
    }

    public void initializeSocialLinks(HashMap<Integer, Student> studentHashMap) {
        for (Student student : studentHashMap.values()) {
            ArrayList<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();
            for (Student sibling : siblingsInSchool) {
                socialGraph.addVertex(student);
                socialGraph.addVertex(sibling);
                DefaultWeightedEdge edge = socialGraph.addEdge(student, sibling);
                if (edge != null) {
                    socialGraph.setEdgeWeight(edge, assignInitialWeight());
                } else {
                    System.out.println("Edge is null");
                }
            }
        }
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
