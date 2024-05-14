package view;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameView {

    private final JFrame frame;
    private final JButton generateButton;
    private final JTextArea statusOutput;

    public GameView() {
        frame = new JFrame("High School Sim");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        generateButton = new JButton("Generate new school");

        statusOutput = new JTextArea(20, 40);
        statusOutput.setEditable(false);

        DefaultCaret caret= (DefaultCaret) statusOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(statusOutput);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(generateButton, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);
    }

    public void addGenerateButtonListener(ActionListener listener) {
        generateButton.addActionListener(listener);
    }

    public void appendOutput(String message) {
        statusOutput.append(message + "\n");
    }

    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }
}