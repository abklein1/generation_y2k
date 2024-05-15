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
    private final JLabel timeLabel;
    private final JMenu inspectionMenu;

    public GameView() {
        frame = new JFrame("Generation '04");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);

        JMenuBar menuBar = new JMenuBar();

        // Inspection
        inspectionMenu = new JMenu("Inspection");
        JMenuItem freshmanItem = new JMenuItem("Freshman");
        JMenuItem sophomoresItem = new JMenuItem("Sophomores");
        JMenuItem juniorsItem = new JMenuItem("Juniors");
        JMenuItem seniorsItem = new JMenuItem("Seniors");
        JMenuItem staffItem = new JMenuItem("Staff");
        inspectionMenu.add(freshmanItem);
        inspectionMenu.add(sophomoresItem);
        inspectionMenu.add(juniorsItem);
        inspectionMenu.add(seniorsItem);
        inspectionMenu.add(staffItem);
        menuBar.add(inspectionMenu);
        frame.setJMenuBar(menuBar);
        inspectionMenu.setEnabled(false);

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
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // time label
        timeLabel = new JLabel();
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.add(timeLabel, BorderLayout.EAST);
        timePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(timePanel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

    public void setInspectionMenuEnabled(boolean enabled) {
        inspectionMenu.setEnabled(enabled);
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

    public void updateTime(String time) {
        timeLabel.setText(time);
    }

    public void addInspectionMenuListener(ActionListener listener) {
        for (Component component : frame.getJMenuBar().getMenu(0).getMenuComponents()) {
            if (component instanceof JMenuItem) {
                ((JMenuItem) component).addActionListener(listener);
            }
        }
    }
}