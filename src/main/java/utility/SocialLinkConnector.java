package utility;

import entity.Student;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static constants.SimConstants.SOCIAL_LINK_MEAN;
import static constants.SimConstants.SOCIAL_LINK_STANDARD_DEVIATION;

import javax.swing.JFrame;
import org.jgrapht.ext.JGraphXAdapter;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.layout.mxFastOrganicLayout;

public class SocialLinkConnector {
    
    Graph<Student, DefaultEdge> socialGraph = new DirectedWeightedMultigraph<>(DefaultEdge.class);

    public SocialLinkConnector(HashMap<Integer, Student> studentHashMap) {
        initializeSocialLinks(studentHashMap);
    }

    public void initializeSocialLinks(HashMap<Integer, Student> studentHashMap) {
        for (Student student : studentHashMap.values()) {
            ArrayList<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();
            for (Student sibling : siblingsInSchool) {
                socialGraph.addVertex(student);
                socialGraph.addVertex(sibling);
                socialGraph.addEdge(student, sibling);
                socialGraph.setEdgeWeight(student, sibling, assignInitialWeight(student, sibling));
            }
        }
    }

    private double assignInitialWeight(Student student, Student sibling) {
        Random distribution = new Random();
        return (distribution.nextGaussian() * SOCIAL_LINK_STANDARD_DEVIATION + SOCIAL_LINK_MEAN);
    }

    public Graph<Student, DefaultEdge> getSocialGraph() {
        return socialGraph;
    }

    public void setEdgeWeight(Student student1, Student student2, double weight) {
        socialGraph.setEdgeWeight(student1, student2, weight);
    }

    public void studentVisualizer(Student student) {
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();
        
        // Create a subgraph for the specific student and their connections
        Graph<Student, DefaultEdge> subGraph = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        subGraph.addVertex(student);
        
        for (DefaultEdge edge : socialGraph.edgesOf(student)) {
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
