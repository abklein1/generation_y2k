package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Main game view with redesigned UI for simulation and game modes.
 */
public class GameView {

    private final JFrame frame;
    private final JButton generateButton;
    private final JButton startSimulationButton;
    private final JButton startGameButton;
    private final JTextArea statusOutput;
    private final JLabel timeLabel;
    private final JLabel weatherAMIconLabel;
    private final JLabel weatherPMIconLabel;
    private final JLabel weatherAMTempLabel;
    private final JLabel weatherPMTempLabel;
    private final JLabel dayLabel;
    private final JPanel amPanel;
    private final JPanel pmPanel;
    
    // Menu items
    private final JMenu inspectionMenu;
    private final JMenu optionsMenu;
    private final JMenu simulationMenu;
    private final JMenuItem visualizeItem;
    private final JMenuItem socialGraphItem;
    private final JMenuItem seedOptionsItem;
    
    // Simulation controls
    private final JPanel simulationControlPanel;
    private final JButton playPauseButton;
    private final JButton stepButton;
    private final JComboBox<String> speedComboBox;
    private final JLabel periodLabel;
    private final JLabel simulationStatusLabel;
    
    // Seed options (moved to dialog)
    private JTextField seedInputField;
    private JCheckBox useCustomSeedCheckbox;
    private long currentSeed;
    
    // Game mode tracking
    private boolean isGameMode = false;
    private boolean isSimulationRunning = false;
    
    // Legacy compatibility fields
    private final JButton visualizeButton;
    private final JButton socialGraphButton;
    private final JButton createCharacterButton;
    private final JLabel currentSeedLabel;
    private final JButton copySeedButton;

    public GameView() {
        frame = new JFrame("generation_y2k");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        // ===== MENU BAR =====
        JMenuBar menuBar = new JMenuBar();
        
        // Game Menu
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newSimMenuItem = new JMenuItem("New Simulation...");
        JMenuItem newGameMenuItem = new JMenuItem("New Game...");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        newSimMenuItem.addActionListener(e -> showStartDialog(false));
        newGameMenuItem.addActionListener(e -> showStartDialog(true));
        exitMenuItem.addActionListener(e -> System.exit(0));
        gameMenu.add(newSimMenuItem);
        gameMenu.add(newGameMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(exitMenuItem);
        menuBar.add(gameMenu);

        // Inspection Menu
        inspectionMenu = new JMenu("Inspection");
        JMenuItem freshmanItem = new JMenuItem("Freshman");
        JMenuItem sophomoresItem = new JMenuItem("Sophomore");
        JMenuItem juniorsItem = new JMenuItem("Junior");
        JMenuItem seniorsItem = new JMenuItem("Senior");
        JMenuItem staffItem = new JMenuItem("Staff");
        inspectionMenu.addSeparator();
        visualizeItem = new JMenuItem("School Layout...");
        socialGraphItem = new JMenuItem("Social Graph...");
        inspectionMenu.add(freshmanItem);
        inspectionMenu.add(sophomoresItem);
        inspectionMenu.add(juniorsItem);
        inspectionMenu.add(seniorsItem);
        inspectionMenu.add(staffItem);
        inspectionMenu.addSeparator();
        inspectionMenu.add(visualizeItem);
        inspectionMenu.add(socialGraphItem);
        menuBar.add(inspectionMenu);
        inspectionMenu.setEnabled(false);
        
        // Simulation Menu
        simulationMenu = new JMenu("Simulation");
        JMenuItem playPauseMenuItem = new JMenuItem("Play/Pause");
        JMenuItem stepMenuItem = new JMenuItem("Step Forward");
        JMenu speedMenu = new JMenu("Speed");
        JMenuItem slowSpeed = new JMenuItem("Slow (10 min/tick)");
        JMenuItem normalSpeed = new JMenuItem("Normal (5 min/tick)");
        JMenuItem fastSpeed = new JMenuItem("Fast (2 min/tick)");
        JMenuItem veryFastSpeed = new JMenuItem("Very Fast (1 min/tick)");
        speedMenu.add(slowSpeed);
        speedMenu.add(normalSpeed);
        speedMenu.add(fastSpeed);
        speedMenu.add(veryFastSpeed);
        simulationMenu.add(playPauseMenuItem);
        simulationMenu.add(stepMenuItem);
        simulationMenu.add(speedMenu);
        menuBar.add(simulationMenu);
        simulationMenu.setEnabled(false);
        
        // Options Menu
        optionsMenu = new JMenu("Options");
        seedOptionsItem = new JMenuItem("Seed Settings...");
        seedOptionsItem.addActionListener(e -> showSeedOptionsDialog());
        optionsMenu.add(seedOptionsItem);
        menuBar.add(optionsMenu);

        frame.setJMenuBar(menuBar);

        // ===== MAIN CONTENT AREA =====
        
        // Start Panel (shown initially)
        JPanel startPanel = createStartPanel();
        
        // Status Output
        statusOutput = new JTextArea(20, 50);
        statusOutput.setEditable(false);
        statusOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret) statusOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(statusOutput);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output Log"));

        // Simulation Control Panel (shown when simulation is running)
        simulationControlPanel = createSimulationControlPanel();
        simulationControlPanel.setVisible(false);

        // Time and weather panel
        timeLabel = new JLabel("Today is --");
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        timeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Weather components
        weatherAMIconLabel = new JLabel();
        weatherAMIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherAMIconLabel.setVisible(false);

        weatherPMIconLabel = new JLabel();
        weatherPMIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherPMIconLabel.setVisible(false);

        weatherAMTempLabel = new JLabel();
        weatherAMTempLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherAMTempLabel.setVisible(false);

        weatherPMTempLabel = new JLabel();
        weatherPMTempLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherPMTempLabel.setVisible(false);

        dayLabel = new JLabel("", SwingConstants.CENTER);
        dayLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        dayLabel.setVisible(false);

        // Weather panel layout
        JPanel weatherPanel = new JPanel(new BorderLayout());
        weatherPanel.setBorder(BorderFactory.createTitledBorder("Weather"));
        weatherPanel.setPreferredSize(new Dimension(200, 120));

        JPanel weatherIconsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        amPanel = new JPanel(new BorderLayout());
        JLabel amLabel = new JLabel("AM", SwingConstants.CENTER);
        amLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        amPanel.add(amLabel, BorderLayout.NORTH);
        amPanel.add(weatherAMIconLabel, BorderLayout.CENTER);
        amPanel.add(weatherAMTempLabel, BorderLayout.SOUTH);
        amPanel.setVisible(false);
        weatherIconsPanel.add(amPanel);

        pmPanel = new JPanel(new BorderLayout());
        JLabel pmLabel = new JLabel("PM", SwingConstants.CENTER);
        pmLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        pmPanel.add(pmLabel, BorderLayout.NORTH);
        pmPanel.add(weatherPMIconLabel, BorderLayout.CENTER);
        pmPanel.add(weatherPMTempLabel, BorderLayout.SOUTH);
        pmPanel.setVisible(false);
        weatherIconsPanel.add(pmPanel);

        weatherPanel.add(dayLabel, BorderLayout.NORTH);
        weatherPanel.add(weatherIconsPanel, BorderLayout.CENTER);

        // Period/Status labels
        periodLabel = new JLabel("Period: --");
        periodLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        simulationStatusLabel = new JLabel("Status: Ready");
        simulationStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        statusPanel.add(periodLabel);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(simulationStatusLabel);

        // Bottom info panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel bottomLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomLeftPanel.add(weatherPanel);
        bottomLeftPanel.add(statusPanel);
        
        bottomPanel.add(bottomLeftPanel, BorderLayout.WEST);
        bottomPanel.add(timeLabel, BorderLayout.EAST);

        // Left panel with start options and controls
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        leftPanel.add(startPanel, BorderLayout.NORTH);
        leftPanel.add(simulationControlPanel, BorderLayout.CENTER);

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        
        // Legacy buttons (hidden, for backward compatibility)
        generateButton = new JButton("Generate new school");
        visualizeButton = new JButton();
        socialGraphButton = new JButton();
        createCharacterButton = new JButton();
        currentSeedLabel = new JLabel();
        copySeedButton = new JButton();
        startSimulationButton = new JButton();
        startGameButton = new JButton();
        playPauseButton = new JButton();
        stepButton = new JButton();
        speedComboBox = new JComboBox<>();

        frame.setVisible(true);
    }
    
    /**
     * Creates the initial start panel with game mode options.
     */
    private JPanel createStartPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Start"));
        
        JButton simButton = new JButton("New Simulation");
        simButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        simButton.setMaximumSize(new Dimension(180, 35));
        simButton.setToolTipText("Start a simulation without a player character");
        simButton.addActionListener(e -> showStartDialog(false));
        
        JButton gameButton = new JButton("New Game");
        gameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameButton.setMaximumSize(new Dimension(180, 35));
        gameButton.setToolTipText("Create a character and play in the simulation");
        gameButton.addActionListener(e -> showStartDialog(true));
        
        JLabel orLabel = new JLabel("Select mode to begin:");
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        orLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        
        panel.add(Box.createVerticalStrut(10));
        panel.add(orLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(simButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(gameButton);
        panel.add(Box.createVerticalStrut(10));
        
        return panel;
    }
    
    /**
     * Creates the simulation control panel.
     */
    private JPanel createSimulationControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Simulation Controls"));
        
        // Play/Pause and Step buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        JButton playBtn = new JButton("\u25B6"); // Play symbol
        playBtn.setToolTipText("Play/Pause simulation");
        playBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        playBtn.setPreferredSize(new Dimension(50, 30));
        
        JButton stepBtn = new JButton("\u23E9"); // Step symbol
        stepBtn.setToolTipText("Step forward one tick");
        stepBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        stepBtn.setPreferredSize(new Dimension(50, 30));
        
        buttonRow.add(playBtn);
        buttonRow.add(stepBtn);
        
        // Speed control
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        speedPanel.add(new JLabel("Speed:"));
        String[] speeds = {"Slow", "Normal", "Fast", "Very Fast"};
        JComboBox<String> speedBox = new JComboBox<>(speeds);
        speedBox.setSelectedIndex(1); // Normal
        speedPanel.add(speedBox);
        
        panel.add(Box.createVerticalStrut(10));
        panel.add(buttonRow);
        panel.add(Box.createVerticalStrut(10));
        panel.add(speedPanel);
        panel.add(Box.createVerticalStrut(10));
        
        return panel;
    }
    
    /**
     * Shows the start dialog for new simulation or game.
     *
     * @param gameMode true for game mode, false for simulation only
     */
    private void showStartDialog(boolean gameMode) {
        JDialog dialog = new JDialog(frame, gameMode ? "New Game" : "New Simulation", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(frame);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Mode description
        JLabel modeLabel = new JLabel(gameMode ? 
                "<html><b>Game Mode</b><br>Create a character and experience high school life.</html>" :
                "<html><b>Simulation Mode</b><br>Watch the simulation run without direct participation.</html>");
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Seed options
        JPanel seedPanel = new JPanel();
        seedPanel.setLayout(new BoxLayout(seedPanel, BoxLayout.Y_AXIS));
        seedPanel.setBorder(BorderFactory.createTitledBorder("World Seed"));
        seedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        useCustomSeedCheckbox = new JCheckBox("Use custom seed");
        seedInputField = new JTextField(20);
        seedInputField.setEnabled(false);
        seedInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        useCustomSeedCheckbox.addActionListener(e -> seedInputField.setEnabled(useCustomSeedCheckbox.isSelected()));
        
        JPanel seedInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        seedInputRow.add(new JLabel("Seed: "));
        seedInputRow.add(seedInputField);
        seedInputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        seedPanel.add(useCustomSeedCheckbox);
        seedPanel.add(Box.createVerticalStrut(5));
        seedPanel.add(seedInputRow);
        
        contentPanel.add(modeLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(seedPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startButton = new JButton(gameMode ? "Create Character..." : "Generate World");
        JButton cancelButton = new JButton("Cancel");
        
        startButton.addActionListener(e -> {
            isGameMode = gameMode;
            dialog.dispose();
            
            // Trigger generation
            if (gameMode) {
                // First generate, then show character creation
                generateButton.doClick();
            } else {
                generateButton.doClick();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    /**
     * Shows the seed options dialog.
     */
    private void showSeedOptionsDialog() {
        JDialog dialog = new JDialog(frame, "Seed Settings", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(frame);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel currentLabel = new JLabel("Current Seed: " + (currentSeed != 0 ? currentSeed : "(none)"));
        currentLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        currentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        copyButton.addActionListener(e -> {
            if (currentSeed != 0) {
                java.awt.datatransfer.StringSelection selection = 
                        new java.awt.datatransfer.StringSelection(String.valueOf(currentSeed));
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                JOptionPane.showMessageDialog(dialog, "Seed copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        contentPanel.add(currentLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(copyButton);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    /**
     * Shows simulation controls after world is generated.
     */
    public void showSimulationControls() {
        simulationControlPanel.setVisible(true);
        simulationMenu.setEnabled(true);
    }
    
    /**
     * Updates the period display.
     *
     * @param period the current period (1-4) or 0 for transition
     */
    public void updatePeriod(int period) {
        if (period > 0) {
            periodLabel.setText("Period: " + period);
        } else {
            periodLabel.setText("Period: Transition");
        }
    }
    
    /**
     * Updates the simulation status.
     *
     * @param status the status text
     */
    public void updateSimulationStatus(String status) {
        simulationStatusLabel.setText("Status: " + status);
    }

    // ===== LEGACY METHODS FOR BACKWARD COMPATIBILITY =====

    public void addGenerateButtonListener(ActionListener listener) {
        generateButton.addActionListener(listener);
    }

    public void addVisualizeButtonListener(ActionListener listener) {
        visualizeItem.addActionListener(listener);
        visualizeButton.addActionListener(listener);
    }

    public void setVisualizeButtonEnabled(boolean enabled) {
        visualizeItem.setEnabled(enabled);
        visualizeButton.setEnabled(enabled);
    }

    public void setSocialGraphButtonEnabled(boolean enabled) {
        socialGraphItem.setEnabled(enabled);
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
            BufferedImage amImage = ImageIO.read(Objects.requireNonNull(getClass().getResource(amIconPath)));
            Image scaledAmImage = amImage.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            ImageIcon weatherAMIcon = new ImageIcon(scaledAmImage);
            weatherAMIconLabel.setIcon(weatherAMIcon);
            weatherAMIconLabel.setToolTipText(amName);
            weatherAMIconLabel.setVisible(true);
            weatherAMTempLabel.setVisible(true);
            amPanel.setVisible(true);

            BufferedImage pmImage = ImageIO.read(Objects.requireNonNull(getClass().getResource(pmIconPath)));
            Image scaledPmImage = pmImage.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            ImageIcon weatherPMIcon = new ImageIcon(scaledPmImage);
            weatherPMIconLabel.setIcon(weatherPMIcon);
            weatherPMIconLabel.setToolTipText(pmName);
            weatherPMIconLabel.setVisible(true);
            weatherPMTempLabel.setVisible(true);
            pmPanel.setVisible(true);

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
        for (int i = 0; i < 5; i++) { // First 5 items are grade levels and staff
            Component comp = inspectionMenu.getMenuComponent(i);
            if (comp instanceof JMenuItem) {
                ((JMenuItem) comp).addActionListener(listener);
            }
        }
    }

    public void addSocialGraphButtonListener(ActionListener listener) {
        socialGraphItem.addActionListener(listener);
        socialGraphButton.addActionListener(listener);
    }

    public void addCreateCharacterButtonListener(ActionListener listener) {
        createCharacterButton.addActionListener(listener);
    }

    public boolean isCustomSeedEnabled() {
        return useCustomSeedCheckbox != null && useCustomSeedCheckbox.isSelected();
    }

    public Long getCustomSeed() {
        if (seedInputField == null) {
            return null;
        }
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

    public void updateCurrentSeed(long seed) {
        this.currentSeed = seed;
        if (currentSeedLabel != null) {
            currentSeedLabel.setText("Current Seed: " + seed);
        }
        if (copySeedButton != null) {
            copySeedButton.setEnabled(true);
        }
        if (seedInputField != null && (useCustomSeedCheckbox == null || !useCustomSeedCheckbox.isSelected())) {
            seedInputField.setText(String.valueOf(seed));
        }
    }

    public void showSeedError() {
        JOptionPane.showMessageDialog(frame, 
            "Invalid seed format. Please enter a valid number.\nExample: 1737570000000", 
            "Invalid Seed", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Checks if we're in game mode (with player character).
     *
     * @return true if game mode
     */
    public boolean isGameMode() {
        return isGameMode;
    }
    
    /**
     * Gets the main frame.
     *
     * @return the JFrame
     */
    public JFrame getFrame() {
        return frame;
    }
}
