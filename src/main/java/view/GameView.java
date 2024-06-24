package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class GameView {

    private final JFrame frame;
    private final JButton generateButton;
    private final JButton visualizeButton;
    private final JTextArea statusOutput;
    private final JLabel timeLabel;
    private final JLabel weatherAMIconLabel;
    private final JLabel weatherPMIconLabel;
    private final JLabel weatherAMTempLabel;
    private final JLabel weatherPMTempLabel;
    private final JLabel dayLabel;
    private final JMenu inspectionMenu;
    private final JPanel amPanel;
    private final JPanel pmPanel;

    public GameView() {
        frame = new JFrame("generation_y2k");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);

        JMenuBar menuBar = new JMenuBar();

        // Inspection
        inspectionMenu = new JMenu("Inspection");
        JMenuItem freshmanItem = new JMenuItem("Freshman");
        JMenuItem sophomoresItem = new JMenuItem("Sophomore");
        JMenuItem juniorsItem = new JMenuItem("Junior");
        JMenuItem seniorsItem = new JMenuItem("Senior");
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

        // weather icons and labels
        weatherAMIconLabel = new JLabel();
        weatherAMIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherAMIconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        weatherAMIconLabel.setVisible(false);

        weatherPMIconLabel = new JLabel();
        weatherPMIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherPMIconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        weatherPMIconLabel.setVisible(false);

        weatherAMTempLabel = new JLabel();
        weatherAMTempLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherAMTempLabel.setVisible(false);

        weatherPMTempLabel = new JLabel();
        weatherPMTempLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherPMTempLabel.setVisible(false);

        // day label
        dayLabel = new JLabel("Mon", SwingConstants.CENTER);
        dayLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dayLabel.setVisible(false);

        // weather panel
        JPanel weatherPanel = new JPanel(new BorderLayout());
        weatherPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        weatherPanel.setPreferredSize(new Dimension(300, 100));

        JPanel weatherIconsPanel = new JPanel(new GridLayout(1, 2));
        amPanel = new JPanel(new BorderLayout());
        JLabel amLabel = new JLabel("AM", SwingConstants.CENTER);
        amPanel.add(amLabel, BorderLayout.NORTH);
        amPanel.add(weatherAMIconLabel, BorderLayout.CENTER);
        amPanel.add(weatherAMTempLabel, BorderLayout.SOUTH);
        amPanel.setVisible(false);
        weatherIconsPanel.add(amPanel);

        pmPanel = new JPanel(new BorderLayout());
        JLabel pmLabel = new JLabel("PM", SwingConstants.CENTER);
        pmPanel.add(pmLabel, BorderLayout.NORTH);
        pmPanel.add(weatherPMIconLabel, BorderLayout.CENTER);
        pmPanel.add(weatherPMTempLabel, BorderLayout.SOUTH);
        pmPanel.setVisible(false);
        weatherIconsPanel.add(pmPanel);

        weatherPanel.add(dayLabel, BorderLayout.NORTH);
        weatherPanel.add(weatherIconsPanel, BorderLayout.CENTER);

        // main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.add(weatherPanel, BorderLayout.WEST);
        lowerPanel.add(timeLabel, BorderLayout.EAST);

        mainPanel.add(lowerPanel, BorderLayout.SOUTH);
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

    public void updateWeatherIcons(String amIconPath, String pmIconPath, String amName, String pmName) {
        try {
            // Load and scale AM icon
            System.out.println(amIconPath);
            BufferedImage amImage = ImageIO.read(Objects.requireNonNull(getClass().getResource(amIconPath)));
            Image scaledAmImage = amImage.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            ImageIcon weatherAMIcon = new ImageIcon(scaledAmImage);
            weatherAMIconLabel.setIcon(weatherAMIcon);
            weatherAMIconLabel.setToolTipText(amName);
            weatherAMIconLabel.setVisible(true);
            weatherAMTempLabel.setVisible(true);
            amPanel.setVisible(true);
            System.out.println("AM icon loaded and scaled successfully.");

            // Load and scale PM icon
            System.out.println(pmIconPath);
            BufferedImage pmImage = ImageIO.read(Objects.requireNonNull(getClass().getResource(pmIconPath)));
            Image scaledPmImage = pmImage.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            ImageIcon weatherPMIcon = new ImageIcon(scaledPmImage);
            weatherPMIconLabel.setIcon(weatherPMIcon);
            weatherPMIconLabel.setToolTipText(pmName);
            weatherPMIconLabel.setVisible(true);
            weatherPMTempLabel.setVisible(true);
            pmPanel.setVisible(true);
            System.out.println("PM icon loaded and scaled successfully.");

            dayLabel.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWeatherTemps(String amTemp, String pmTemp) {
        weatherAMTempLabel.setText(amTemp + "\u00B0" + " F");
        weatherPMTempLabel.setText(pmTemp + "\u00B0" + " F");
    }

    public void updateDayLabel(String day) {
        dayLabel.setText(day);
    }

    public void addInspectionMenuListener(ActionListener listener) {
        for (Component component : frame.getJMenuBar().getMenu(0).getMenuComponents()) {
            if (component instanceof JMenuItem) {
                ((JMenuItem) component).addActionListener(listener);
            }
        }
    }
}