package view;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameView {

    private JFrame frame;
    private JButton generateButton;
    public GameView() {
        frame = new JFrame("High School Sim");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250,250);

        generateButton = new JButton("Generate new school");

        JPanel panel = new JPanel();
        panel.add(generateButton);
        frame.add(panel);

        frame.setVisible(true);
    }

    public void addGenerateButtonListener(ActionListener listener) {
        generateButton.addActionListener(listener);
    }

    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }
}