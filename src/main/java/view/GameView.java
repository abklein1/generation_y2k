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
    private final JButton socialGraphButton;
    private final JButton createCharacterButton;
    private final JTextField seedInputField;
    private final JCheckBox useCustomSeedCheckbox;
    private final JLabel currentSeedLabel;
    private final JButton copySeedButton;

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
        socialGraphButton = new JButton("Show social graph");
        visualizeButton.setEnabled(false);
        socialGraphButton.setEnabled(false);
        createCharacterButton = new JButton("Create Player Character");
        createCharacterButton.setEnabled(true);

        // Seed controls
        seedInputField = new JTextField(15);
        seedInputField.setToolTipText("Enter a seed number to recreate a specific world");
        seedInputField.setEnabled(false);
        useCustomSeedCheckbox = new JCheckBox("Use custom seed");
        useCustomSeedCheckbox.setToolTipText("Check to use a specific seed instead of random");
        useCustomSeedCheckbox.addActionListener(e -> seedInputField.setEnabled(useCustomSeedCheckbox.isSelected()));
        currentSeedLabel = new JLabel("Current Seed: (none)");
        currentSeedLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        copySeedButton = new JButton("Copy");
        copySeedButton.setToolTipText("Copy seed to clipboard");
        copySeedButton.setMargin(new Insets(2, 6, 2, 6));
        copySeedButton.setEnabled(false);
        copySeedButton.addActionListener(e -> {
            String seedText = currentSeedLabel.getText().replace("Current Seed: ", "");
            if (!seedText.equals("(none)")) {
                java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(seedText);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            }
        });

        statusOutput = new JTextArea(20, 40);
        statusOutput.setEditable(false);

        DefaultCaret caret = (DefaultCaret) statusOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(statusOutput);

        // Seed panel
        JPanel seedPanel = new JPanel();
        seedPanel.setLayout(new BoxLayout(seedPanel, BoxLayout.Y_AXIS));
        seedPanel.setBorder(BorderFactory.createTitledBorder("World Seed"));
        
        JPanel seedInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        seedInputPanel.add(useCustomSeedCheckbox);
        
        JPanel seedFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        seedFieldPanel.add(new JLabel("Seed:"));
        seedFieldPanel.add(seedInputField);
        
        JPanel currentSeedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        currentSeedPanel.add(currentSeedLabel);
        currentSeedPanel.add(copySeedButton);
        
        seedPanel.add(seedInputPanel);
        seedPanel.add(seedFieldPanel);
        seedPanel.add(currentSeedPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(generateButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(seedPanel);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(visualizeButton);
        buttonPanel.add(socialGraphButton);
        buttonPanel.add(createCharacterButton);
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

    public void setSocialGraphButtonEnabled(boolean enabled) {
        socialGraphButton.setEnabled(enabled);
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

    public void addSocialGraphButtonListener(ActionListener listener) {
        socialGraphButton.addActionListener(listener);
    }

    public void addCreateCharacterButtonListener(ActionListener listener) {
        createCharacterButton.addActionListener(listener);
    }

    /**
     * Check if the user wants to use a custom seed.
     * @return true if custom seed checkbox is selected
     */
    public boolean isCustomSeedEnabled() {
        return useCustomSeedCheckbox.isSelected();
    }

    /**
     * Get the custom seed value from the input field.
     * @return The parsed seed value, or null if invalid/empty
     */
    public Long getCustomSeed() {
        String text = seedInputField.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Update the displayed current seed after generation.
     * @param seed The seed that was used
     */
    public void updateCurrentSeed(long seed) {
        currentSeedLabel.setText("Current Seed: " + seed);
        copySeedButton.setEnabled(true);
        // Also populate the input field if it was a random seed
        if (!useCustomSeedCheckbox.isSelected()) {
            seedInputField.setText(String.valueOf(seed));
        }
    }

    /**
     * Show an error message for invalid seed input.
     */
    public void showSeedError() {
        JOptionPane.showMessageDialog(frame, 
            "Invalid seed format. Please enter a valid number.\nExample: 1737570000000", 
            "Invalid Seed", 
            JOptionPane.ERROR_MESSAGE);
    }

}