package view;

import utility.GameLogger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

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

    // Debug menu items
    private final JMenu debugMenu;
    private JCheckBoxMenuItem debugGenerationItem;
    private JCheckBoxMenuItem debugSocialLinksItem;
    private JCheckBoxMenuItem debugSchedulingItem;
    private JCheckBoxMenuItem debugStoryItem;
    private JCheckBoxMenuItem debugMessagesItem;

    // Simulation controls
    private final JPanel simulationControlPanel;
    private JButton playPauseButton;
    private JButton stepButton;
    private JComboBox<String> speedComboBox;
    private final JLabel periodLabel;
    private final JLabel simulationStatusLabel;

    // Menu items for simulation
    private JMenuItem playPauseMenuItem;
    private JMenuItem stepMenuItem;
    private JMenuItem slowSpeedItem;
    private JMenuItem normalSpeedItem;
    private JMenuItem fastSpeedItem;
    private JMenuItem veryFastSpeedItem;

    // School info labels
    private final JLabel schoolNameLabel;
    private final JLabel schoolFoundedLabel;
    private final JLabel schoolMascotLabel;
    private final JLabel schoolColorsLabel;

    // Seed options (moved to dialog)
    private JTextField seedInputField;
    private JCheckBox useCustomSeedCheckbox;
    private long currentSeed;

    // Demographics options
    private int demographicsStudentPopulation = 1200;
    private int demographicsStaffPopulation = 100;
    private int demographicsExtraStudentPercent = 15;
    private int demographicsExtraStaffPercent = 20;
    private boolean useCustomDemographics = false;

    // Gender distribution (Male %, Female is 100 - Male)
    private int demographicsMalePercent = 51; // Default from SimConstants

    // Income distribution (Low %, High %, Middle is 100 - Low - High)
    private int demographicsIncomeLowPercent = 25; // Default from SimConstants
    private int demographicsIncomeHighPercent = 15; // Default from SimConstants

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
        visualizeItem = new JMenuItem("School Layout...");
        socialGraphItem = new JMenuItem("Social Graph...");
        // Add items in correct order (grade levels first, then separator, then tools)
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
        playPauseMenuItem = new JMenuItem("Play/Pause");
        stepMenuItem = new JMenuItem("Step Forward");
        JMenu speedMenu = new JMenu("Speed");
        slowSpeedItem = new JMenuItem("Slow (1 tick/sec)");
        normalSpeedItem = new JMenuItem("Normal (2 ticks/sec)");
        fastSpeedItem = new JMenuItem("Fast (4 ticks/sec)");
        veryFastSpeedItem = new JMenuItem("Very Fast (8 ticks/sec)");
        speedMenu.add(slowSpeedItem);
        speedMenu.add(normalSpeedItem);
        speedMenu.add(fastSpeedItem);
        speedMenu.add(veryFastSpeedItem);
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

        // Debug Menu
        debugMenu = new JMenu("Debug");
        debugGenerationItem = new JCheckBoxMenuItem("Generation Messages", true);
        debugSocialLinksItem = new JCheckBoxMenuItem("Social Links Messages", true);
        debugSchedulingItem = new JCheckBoxMenuItem("Scheduling Messages", true);
        debugStoryItem = new JCheckBoxMenuItem("Story Messages", true);
        debugMessagesItem = new JCheckBoxMenuItem("Debug Messages", true);

        // Add action listeners to update GameLogger
        debugGenerationItem.addActionListener(e ->
                GameLogger.setEnabled(GameLogger.Category.GENERATION, debugGenerationItem.isSelected()));
        debugSocialLinksItem.addActionListener(e ->
                GameLogger.setEnabled(GameLogger.Category.SOCIAL_LINKS, debugSocialLinksItem.isSelected()));
        debugSchedulingItem.addActionListener(e ->
                GameLogger.setEnabled(GameLogger.Category.SCHEDULING, debugSchedulingItem.isSelected()));
        debugStoryItem.addActionListener(e ->
                GameLogger.setEnabled(GameLogger.Category.STORY, debugStoryItem.isSelected()));
        debugMessagesItem.addActionListener(e ->
                GameLogger.setEnabled(GameLogger.Category.DEBUG, debugMessagesItem.isSelected()));

        debugMenu.add(debugGenerationItem);
        debugMenu.add(debugSocialLinksItem);
        debugMenu.add(debugSchedulingItem);
        debugMenu.add(debugStoryItem);
        debugMenu.add(debugMessagesItem);
        debugMenu.addSeparator();

        JMenuItem enableAllItem = new JMenuItem("Enable All");
        JMenuItem disableAllItem = new JMenuItem("Disable All (except Story)");
        enableAllItem.addActionListener(e -> setAllDebugCategories(true));
        disableAllItem.addActionListener(e -> setAllDebugCategories(false));
        debugMenu.add(enableAllItem);
        debugMenu.add(disableAllItem);

        menuBar.add(debugMenu);

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
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

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
        weatherPanel.setPreferredSize(new Dimension(180, 110));
        weatherPanel.setMinimumSize(new Dimension(180, 110));

        JPanel weatherIconsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        amPanel = new JPanel(new BorderLayout());
        JLabel amLabel = new JLabel("AM", SwingConstants.CENTER);
        amLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        amPanel.add(amLabel, BorderLayout.NORTH);
        amPanel.add(weatherAMIconLabel, BorderLayout.CENTER);
        amPanel.add(weatherAMTempLabel, BorderLayout.SOUTH);
        weatherIconsPanel.add(amPanel);

        pmPanel = new JPanel(new BorderLayout());
        JLabel pmLabel = new JLabel("PM", SwingConstants.CENTER);
        pmLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        pmPanel.add(pmLabel, BorderLayout.NORTH);
        pmPanel.add(weatherPMIconLabel, BorderLayout.CENTER);
        pmPanel.add(weatherPMTempLabel, BorderLayout.SOUTH);
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

        // School Info panel
        schoolNameLabel = new JLabel("School: --");
        schoolNameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        schoolFoundedLabel = new JLabel("Founded: --");
        schoolFoundedLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        schoolMascotLabel = new JLabel("Mascot: --");
        schoolMascotLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        schoolColorsLabel = new JLabel("Colors: --");
        schoolColorsLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("School Info"));
        infoPanel.add(schoolNameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(schoolFoundedLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(schoolMascotLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(schoolColorsLabel);

        // Bottom info panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel bottomLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomLeftPanel.add(weatherPanel);
        bottomLeftPanel.add(statusPanel);
        bottomLeftPanel.add(infoPanel);

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

        frame.setVisible(true);
    }

    // ===== SIMULATION CONTROL LISTENER METHODS =====

    /**
     * Adds a listener for play/pause button clicks.
     *
     * @param listener the action listener
     */
    public void addPlayPauseListener(ActionListener listener) {
        if (playPauseButton != null) {
            playPauseButton.addActionListener(listener);
        }
        if (playPauseMenuItem != null) {
            playPauseMenuItem.addActionListener(listener);
        }
    }

    /**
     * Adds a listener for step button clicks.
     *
     * @param listener the action listener
     */
    public void addStepListener(ActionListener listener) {
        if (stepButton != null) {
            stepButton.addActionListener(listener);
        }
        if (stepMenuItem != null) {
            stepMenuItem.addActionListener(listener);
        }
    }

    /**
     * Adds a listener for speed changes.
     *
     * @param listener the action listener
     */
    public void addSpeedChangeListener(ActionListener listener) {
        if (speedComboBox != null) {
            speedComboBox.addActionListener(listener);
        }
        if (slowSpeedItem != null) {
            slowSpeedItem.addActionListener(e -> {
                speedComboBox.setSelectedIndex(0);
                listener.actionPerformed(e);
            });
        }
        if (normalSpeedItem != null) {
            normalSpeedItem.addActionListener(e -> {
                speedComboBox.setSelectedIndex(1);
                listener.actionPerformed(e);
            });
        }
        if (fastSpeedItem != null) {
            fastSpeedItem.addActionListener(e -> {
                speedComboBox.setSelectedIndex(2);
                listener.actionPerformed(e);
            });
        }
        if (veryFastSpeedItem != null) {
            veryFastSpeedItem.addActionListener(e -> {
                speedComboBox.setSelectedIndex(3);
                listener.actionPerformed(e);
            });
        }
    }

    /**
     * Gets the currently selected speed index.
     * 0=Slow (1x), 1=Normal (2x), 2=Fast (4x), 3=Very Fast (8x)
     *
     * @return the speed index
     */
    public int getSelectedSpeedIndex() {
        if (speedComboBox != null) {
            return speedComboBox.getSelectedIndex();
        }
        return 1; // Default to normal
    }

    /**
     * Updates the play/pause button to show play or pause state.
     *
     * @param isPlaying true if simulation is playing
     */
    public void updatePlayPauseButton(boolean isPlaying) {
        if (playPauseButton != null) {
            playPauseButton.setText(isPlaying ? "\u23F8" : "\u25B6"); // Pause or Play symbol
            playPauseButton.setToolTipText(isPlaying ? "Pause simulation" : "Play simulation");
        }
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
        playPauseButton = new JButton("\u25B6"); // Play symbol
        playPauseButton.setToolTipText("Play/Pause simulation");
        playPauseButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        playPauseButton.setPreferredSize(new Dimension(50, 30));

        stepButton = new JButton("\u23E9"); // Step symbol
        stepButton.setToolTipText("Step forward one tick");
        stepButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        stepButton.setPreferredSize(new Dimension(50, 30));

        buttonRow.add(playPauseButton);
        buttonRow.add(stepButton);

        // Speed control
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        speedPanel.add(new JLabel("Speed:"));
        String[] speeds = { "Slow (1x)", "Normal (2x)", "Fast (4x)", "Very Fast (8x)" };
        speedComboBox = new JComboBox<>(speeds);
        speedComboBox.setSelectedIndex(1); // Normal
        speedPanel.add(speedComboBox);

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
        dialog.setSize(500, 580);
        dialog.setLocationRelativeTo(frame);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Mode description
        JLabel modeLabel = new JLabel(gameMode
                ? "<html><b>Game Mode</b><br>Create a character and experience high school life.</html>"
                : "<html><b>Simulation Mode</b><br>Watch the simulation run without direct participation.</html>");
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Seed options
        JPanel seedPanel = new JPanel();
        seedPanel.setLayout(new BoxLayout(seedPanel, BoxLayout.Y_AXIS));
        seedPanel.setBorder(BorderFactory.createTitledBorder("World Seed"));
        seedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        seedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

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

        // Demographics options panel
        JPanel demographicsPanel = new JPanel();
        demographicsPanel.setLayout(new BoxLayout(demographicsPanel, BoxLayout.Y_AXIS));
        demographicsPanel.setBorder(BorderFactory.createTitledBorder("Town Demographics"));
        demographicsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox customDemographicsCheckbox = new JCheckBox("Customize demographics");
        customDemographicsCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Student population slider (200 - 2000)
        JLabel studentPopLabel = new JLabel("Student Population: " + demographicsStudentPopulation);
        studentPopLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider studentPopSlider = new JSlider(200, 2000, demographicsStudentPopulation);
        studentPopSlider.setMajorTickSpacing(400);
        studentPopSlider.setMinorTickSpacing(100);
        studentPopSlider.setPaintTicks(true);
        studentPopSlider.setPaintLabels(true);
        studentPopSlider.setEnabled(false);
        studentPopSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        studentPopSlider.addChangeListener(e -> {
            demographicsStudentPopulation = studentPopSlider.getValue();
            studentPopLabel.setText("Student Population: " + demographicsStudentPopulation);
        });

        // Staff population slider (30 - 200)
        JLabel staffPopLabel = new JLabel("Staff Population: " + demographicsStaffPopulation);
        staffPopLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider staffPopSlider = new JSlider(30, 200, demographicsStaffPopulation);
        staffPopSlider.setMajorTickSpacing(40);
        staffPopSlider.setMinorTickSpacing(10);
        staffPopSlider.setPaintTicks(true);
        staffPopSlider.setPaintLabels(true);
        staffPopSlider.setEnabled(false);
        staffPopSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        staffPopSlider.addChangeListener(e -> {
            demographicsStaffPopulation = staffPopSlider.getValue();
            staffPopLabel.setText("Staff Population: " + demographicsStaffPopulation);
        });

        // Extra student pool slider (0% - 50%)
        JLabel extraStudentLabel = new JLabel("Extra Student Pool: " + demographicsExtraStudentPercent + "%");
        extraStudentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider extraStudentSlider = new JSlider(0, 50, demographicsExtraStudentPercent);
        extraStudentSlider.setMajorTickSpacing(10);
        extraStudentSlider.setMinorTickSpacing(5);
        extraStudentSlider.setPaintTicks(true);
        extraStudentSlider.setPaintLabels(true);
        extraStudentSlider.setEnabled(false);
        extraStudentSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        extraStudentSlider.addChangeListener(e -> {
            demographicsExtraStudentPercent = extraStudentSlider.getValue();
            extraStudentLabel.setText("Extra Student Pool: " + demographicsExtraStudentPercent + "%");
        });

        // Extra staff pool slider (0% - 50%)
        JLabel extraStaffLabel = new JLabel("Extra Staff Pool (Substitutes): " + demographicsExtraStaffPercent + "%");
        extraStaffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider extraStaffSlider = new JSlider(0, 50, demographicsExtraStaffPercent);
        extraStaffSlider.setMajorTickSpacing(10);
        extraStaffSlider.setMinorTickSpacing(5);
        extraStaffSlider.setPaintTicks(true);
        extraStaffSlider.setPaintLabels(true);
        extraStaffSlider.setEnabled(false);
        extraStaffSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        extraStaffSlider.addChangeListener(e -> {
            demographicsExtraStaffPercent = extraStaffSlider.getValue();
            extraStaffLabel.setText("Extra Staff Pool (Substitutes): " + demographicsExtraStaffPercent + "%");
        });

        // Separator for distribution settings
        JSeparator distributionSeparator = new JSeparator();
        distributionSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
        distributionSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel distributionHeaderLabel = new JLabel("Population Distributions");
        distributionHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        distributionHeaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Gender distribution slider (0% Male to 100% Male, Female is complement)
        int femalePercent = 100 - demographicsMalePercent;
        JLabel genderLabel = new JLabel(
                "Gender: " + demographicsMalePercent + "% Male / " + femalePercent + "% Female");
        genderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider genderSlider = new JSlider(0, 100, demographicsMalePercent);
        genderSlider.setMajorTickSpacing(25);
        genderSlider.setMinorTickSpacing(5);
        genderSlider.setPaintTicks(true);
        genderSlider.setPaintLabels(true);
        genderSlider.setEnabled(false);
        genderSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        genderSlider.addChangeListener(e -> {
            demographicsMalePercent = genderSlider.getValue();
            int female = 100 - demographicsMalePercent;
            genderLabel.setText("Gender: " + demographicsMalePercent + "% Male / " + female + "% Female");
        });

        // Income distribution - Low % slider
        JLabel incomeLowLabel = new JLabel("Income - Low: " + demographicsIncomeLowPercent + "%");
        incomeLowLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider incomeLowSlider = new JSlider(0, 100, demographicsIncomeLowPercent);
        incomeLowSlider.setMajorTickSpacing(25);
        incomeLowSlider.setMinorTickSpacing(5);
        incomeLowSlider.setPaintTicks(true);
        incomeLowSlider.setPaintLabels(true);
        incomeLowSlider.setEnabled(false);
        incomeLowSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Income distribution - High % slider
        JLabel incomeHighLabel = new JLabel("Income - High: " + demographicsIncomeHighPercent + "%");
        incomeHighLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider incomeHighSlider = new JSlider(0, 100, demographicsIncomeHighPercent);
        incomeHighSlider.setMajorTickSpacing(25);
        incomeHighSlider.setMinorTickSpacing(5);
        incomeHighSlider.setPaintTicks(true);
        incomeHighSlider.setPaintLabels(true);
        incomeHighSlider.setEnabled(false);
        incomeHighSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Income Middle label (calculated from Low + High)
        int middlePercent = 100 - demographicsIncomeLowPercent - demographicsIncomeHighPercent;
        JLabel incomeMiddleLabel = new JLabel("Income - Middle: " + middlePercent + "% (calculated)");
        incomeMiddleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        incomeMiddleLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));

        // Update income labels and enforce constraints
        Runnable updateIncomeLabels = () -> {
            int low = incomeLowSlider.getValue();
            int high = incomeHighSlider.getValue();

            // Ensure Low + High doesn't exceed 100
            if (low + high > 100) {
                // Adjust the slider that was NOT just changed
                if (incomeLowSlider.getValueIsAdjusting()) {
                    high = 100 - low;
                    incomeHighSlider.setValue(high);
                } else {
                    low = 100 - high;
                    incomeLowSlider.setValue(low);
                }
            }

            demographicsIncomeLowPercent = low;
            demographicsIncomeHighPercent = high;
            int middle = 100 - low - high;

            incomeLowLabel.setText("Income - Low: " + low + "%");
            incomeHighLabel.setText("Income - High: " + high + "%");
            incomeMiddleLabel.setText("Income - Middle: " + middle + "% (calculated)");
        };

        incomeLowSlider.addChangeListener(e -> updateIncomeLabels.run());
        incomeHighSlider.addChangeListener(e -> updateIncomeLabels.run());

        // Enable/disable sliders based on checkbox
        customDemographicsCheckbox.addActionListener(e -> {
            boolean enabled = customDemographicsCheckbox.isSelected();
            useCustomDemographics = enabled;
            studentPopSlider.setEnabled(enabled);
            staffPopSlider.setEnabled(enabled);
            extraStudentSlider.setEnabled(enabled);
            extraStaffSlider.setEnabled(enabled);
            genderSlider.setEnabled(enabled);
            incomeLowSlider.setEnabled(enabled);
            incomeHighSlider.setEnabled(enabled);
        });

        // Tooltip explaining the extra pools
        JLabel poolInfoLabel = new JLabel(
                "<html><i>Extra pools provide unassigned people for mid-year transfers, substitutes, etc.</i></html>");
        poolInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        poolInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        demographicsPanel.add(customDemographicsCheckbox);
        demographicsPanel.add(Box.createVerticalStrut(10));
        demographicsPanel.add(studentPopLabel);
        demographicsPanel.add(studentPopSlider);
        demographicsPanel.add(Box.createVerticalStrut(5));
        demographicsPanel.add(staffPopLabel);
        demographicsPanel.add(staffPopSlider);
        demographicsPanel.add(Box.createVerticalStrut(5));
        demographicsPanel.add(extraStudentLabel);
        demographicsPanel.add(extraStudentSlider);
        demographicsPanel.add(Box.createVerticalStrut(5));
        demographicsPanel.add(extraStaffLabel);
        demographicsPanel.add(extraStaffSlider);
        demographicsPanel.add(Box.createVerticalStrut(5));
        demographicsPanel.add(poolInfoLabel);
        demographicsPanel.add(Box.createVerticalStrut(10));
        demographicsPanel.add(distributionSeparator);
        demographicsPanel.add(Box.createVerticalStrut(5));
        demographicsPanel.add(distributionHeaderLabel);
        demographicsPanel.add(Box.createVerticalStrut(5));
        demographicsPanel.add(genderLabel);
        demographicsPanel.add(genderSlider);
        demographicsPanel.add(Box.createVerticalStrut(10));
        demographicsPanel.add(incomeLowLabel);
        demographicsPanel.add(incomeLowSlider);
        demographicsPanel.add(Box.createVerticalStrut(5));
        demographicsPanel.add(incomeHighLabel);
        demographicsPanel.add(incomeHighSlider);
        demographicsPanel.add(Box.createVerticalStrut(3));
        demographicsPanel.add(incomeMiddleLabel);

        contentPanel.add(modeLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(seedPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(demographicsPanel);

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

        // Use scroll pane for content in case dialog is resized smaller
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        dialog.add(scrollPane, BorderLayout.CENTER);
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
                java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(
                        String.valueOf(currentSeed));
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                JOptionPane.showMessageDialog(dialog, "Seed copied to clipboard!", "Copied",
                        JOptionPane.INFORMATION_MESSAGE);
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
     * @param period       the current period (1-4) or 0 for non-class time
     * @param periodStatus descriptive status ("Before School", "Transition", "After
     *                     School", or null for default)
     */
    public void updatePeriod(int period, String periodStatus) {
        if (period > 0) {
            periodLabel.setText("Period: " + period);
        } else if (periodStatus != null) {
            periodLabel.setText("Period: " + periodStatus);
        } else {
            periodLabel.setText("Period: --");
        }
    }

    /**
     * Updates the period display (legacy method for backward compatibility).
     *
     * @param period the current period (1-4) or 0 for transition
     */
    public void updatePeriod(int period) {
        updatePeriod(period, period == 0 ? "Transition" : null);
    }

    /**
     * Updates the simulation status.
     *
     * @param status the status text
     */
    public void updateSimulationStatus(String status) {
        simulationStatusLabel.setText("Status: " + status);
    }

    /**
     * Updates the school info panel with school details.
     *
     * @param name        the school name
     * @param foundedYear the year the school was founded
     * @param mascot      the school mascot
     * @param color1      the first school color
     * @param color2      the second school color
     */
    public void updateSchoolInfo(String name, String foundedYear, String mascot, String color1, String color2) {
        schoolNameLabel.setText(name);
        schoolFoundedLabel.setText("Founded: " + foundedYear);
        schoolMascotLabel.setText("Mascot: " + mascot);
        schoolColorsLabel.setText("Colors: " + color1 + " & " + color2);
    }

    /**
     * Clears the school info panel (resets to default values).
     */
    public void clearSchoolInfo() {
        schoolNameLabel.setText("School: --");
        schoolFoundedLabel.setText("Founded: --");
        schoolMascotLabel.setText("Mascot: --");
        schoolColorsLabel.setText("Colors: --");
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

    /**
     * Sets all debug categories to enabled or disabled.
     * Story messages are always kept enabled.
     *
     * @param enabled true to enable all categories, false to disable (except Story)
     */
    private void setAllDebugCategories(boolean enabled) {
        debugGenerationItem.setSelected(enabled);
        debugSocialLinksItem.setSelected(enabled);
        debugSchedulingItem.setSelected(enabled);
        debugStoryItem.setSelected(true); // Story always on
        debugMessagesItem.setSelected(enabled);

        GameLogger.setEnabled(GameLogger.Category.GENERATION, enabled);
        GameLogger.setEnabled(GameLogger.Category.SOCIAL_LINKS, enabled);
        GameLogger.setEnabled(GameLogger.Category.SCHEDULING, enabled);
        GameLogger.setEnabled(GameLogger.Category.STORY, true); // Always enabled
        GameLogger.setEnabled(GameLogger.Category.DEBUG, enabled);
    }

    /**
     * Synchronizes the debug menu checkboxes with the current GameLogger state.
     * Call this after GameLogger.initialize() to reflect the correct state.
     */
    public void syncDebugMenuWithLogger() {
        debugGenerationItem.setSelected(GameLogger.isEnabled(GameLogger.Category.GENERATION));
        debugSocialLinksItem.setSelected(GameLogger.isEnabled(GameLogger.Category.SOCIAL_LINKS));
        debugSchedulingItem.setSelected(GameLogger.isEnabled(GameLogger.Category.SCHEDULING));
        debugStoryItem.setSelected(GameLogger.isEnabled(GameLogger.Category.STORY));
        debugMessagesItem.setSelected(GameLogger.isEnabled(GameLogger.Category.DEBUG));
    }

    /**
     * Applies the current debug menu checkbox states to GameLogger.
     * Use this to preserve user's menu selections when starting a simulation.
     */
    public void applyDebugMenuToLogger() {
        GameLogger.setEnabled(GameLogger.Category.GENERATION, debugGenerationItem.isSelected());
        GameLogger.setEnabled(GameLogger.Category.SOCIAL_LINKS, debugSocialLinksItem.isSelected());
        GameLogger.setEnabled(GameLogger.Category.SCHEDULING, debugSchedulingItem.isSelected());
        GameLogger.setEnabled(GameLogger.Category.STORY, debugStoryItem.isSelected());
        GameLogger.setEnabled(GameLogger.Category.DEBUG, debugMessagesItem.isSelected());
    }

    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public void updateTime(String time) {
        timeLabel.setText(time);
    }

    public void updateWeatherIcons(String amIconPath, String pmIconPath, String amName, String pmName) {
        try {
            // Convert resource path to file path (remove leading slash and prepend
            // src/main/java)
            String basePath = "src/main/java";
            String amFilePath = basePath + amIconPath;
            String pmFilePath = basePath + pmIconPath;

            // Load and set AM icon using file-based loading
            java.io.File amFile = new java.io.File(amFilePath);
            BufferedImage amImage = ImageIO.read(amFile);
            if (amImage == null) {
                GameLogger.logDebug("Failed to load AM weather icon: " + amFilePath);
                return;
            }
            Image scaledAmImage = amImage.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            ImageIcon weatherAMIcon = new ImageIcon(scaledAmImage);
            weatherAMIconLabel.setIcon(weatherAMIcon);
            weatherAMIconLabel.setToolTipText(amName);
            weatherAMIconLabel.setVisible(true);
            weatherAMTempLabel.setVisible(true);

            // Load and set PM icon using file-based loading
            java.io.File pmFile = new java.io.File(pmFilePath);
            BufferedImage pmImage = ImageIO.read(pmFile);
            if (pmImage == null) {
                GameLogger.logDebug("Failed to load PM weather icon: " + pmFilePath);
                return;
            }
            Image scaledPmImage = pmImage.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            ImageIcon weatherPMIcon = new ImageIcon(scaledPmImage);
            weatherPMIconLabel.setIcon(weatherPMIcon);
            weatherPMIconLabel.setToolTipText(pmName);
            weatherPMIconLabel.setVisible(true);
            weatherPMTempLabel.setVisible(true);

            dayLabel.setVisible(true);

            // Force layout update
            amPanel.revalidate();
            amPanel.repaint();
            pmPanel.revalidate();
            pmPanel.repaint();
            frame.revalidate();
            frame.repaint();
        } catch (Exception e) {
            GameLogger.logDebug("Error loading weather icons: " + e.getMessage());
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

    // ==================== Demographics Settings ====================

    /**
     * Checks if custom demographics are enabled.
     *
     * @return true if the user has enabled custom demographics
     */
    public boolean isCustomDemographicsEnabled() {
        return useCustomDemographics;
    }

    /**
     * Gets the custom student population setting.
     *
     * @return the student population
     */
    public int getDemographicsStudentPopulation() {
        return demographicsStudentPopulation;
    }

    /**
     * Gets the custom staff population setting.
     *
     * @return the staff population
     */
    public int getDemographicsStaffPopulation() {
        return demographicsStaffPopulation;
    }

    /**
     * Gets the extra student pool percentage.
     *
     * @return the extra student pool percentage (0-50)
     */
    public int getDemographicsExtraStudentPercent() {
        return demographicsExtraStudentPercent;
    }

    /**
     * Gets the extra staff pool percentage.
     *
     * @return the extra staff pool percentage (0-50)
     */
    public int getDemographicsExtraStaffPercent() {
        return demographicsExtraStaffPercent;
    }

    /**
     * Gets the male percentage for gender distribution.
     *
     * @return the male percentage (0-100)
     */
    public int getDemographicsMalePercent() {
        return demographicsMalePercent;
    }

    /**
     * Gets the female percentage for gender distribution.
     *
     * @return the female percentage (0-100)
     */
    public int getDemographicsFemalePercent() {
        return 100 - demographicsMalePercent;
    }

    /**
     * Gets the low income percentage.
     *
     * @return the low income percentage (0-100)
     */
    public int getDemographicsIncomeLowPercent() {
        return demographicsIncomeLowPercent;
    }

    /**
     * Gets the middle income percentage (calculated from low and high).
     *
     * @return the middle income percentage (0-100)
     */
    public int getDemographicsIncomeMiddlePercent() {
        return 100 - demographicsIncomeLowPercent - demographicsIncomeHighPercent;
    }

    /**
     * Gets the high income percentage.
     *
     * @return the high income percentage (0-100)
     */
    public int getDemographicsIncomeHighPercent() {
        return demographicsIncomeHighPercent;
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
