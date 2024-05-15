package view;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameView {

    private final JFrame frame;
    private final JButton generateButton;
    private final JButton visualizeButton;
    private final JTextArea statusOutput;

    public GameView() {
        frame = new JFrame("High School Sim");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        generateButton = new JButton("Generate new school");
        visualizeButton = new JButton("Show school layout");
        visualizeButton.setEnabled(false);

        statusOutput = new JTextArea(20, 40);
        statusOutput.setEditable(false);

        DefaultCaret caret = (DefaultCaret) statusOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(statusOutput);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(generateButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(visualizeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public void addGenerateButtonListener(ActionListener listener) {
        generateButton.addActionListener(listener);
    }

    public void addVisualizeButtonListener(ActionListener listener) {
        visualizeButton.addActionListener(listener);
    }

    public void setVisualizeButtonEnabled(boolean enabled) {
        visualizeButton.setEnabled(enabled);
    }

    public void setVisualizeButtonVisible(boolean visible) {
        visualizeButton.setVisible(visible);
    }

    public void appendOutput(String message) {
        statusOutput.append(message + "\n");
    }

    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }
}