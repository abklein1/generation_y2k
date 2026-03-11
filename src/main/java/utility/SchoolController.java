package utility;

import config.DemographicsLoader;
import config.SchoolFundingModel;
import config.TownDemographics;
import entity.Rooms.*;
import entity.*;
import simulation.EntityStateManager;
import simulation.SimulationEngine;
import view.GameView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.text.DefaultCaret;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static utility.Inspector.staffInspection;
import static utility.Inspector.studentInspection;
import org.jdatepicker.impl.UtilDateModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.table.DefaultTableModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import static utility.Randomizer.setRandom;
import static constants.SimConstants.*;

public class SchoolController {
    private final GameView view;
    private final Time time;
    HashMap<Integer, Staff> staffHashMap;
    HashMap<Integer, Student> studentHashMap;
    private RoomConnector roomConnector;
    private SocialLinkConnector socialLinkConnector;
    private StandardSchool standardSchool;

    // Town-based population management (new architecture)
    private Town town;
    private boolean useTownBasedGeneration = true; // Toggle for new vs legacy generation

    // Simulation components
    private SimulationEngine simulationEngine;
    private EntityStateManager entityStateManager;
    private Timer simulationTimer;
    private boolean simulationRunning = false;

    public SchoolController(GameView view) {
        this.view = view;
        this.view.addGenerateButtonListener(new GenerateButtonListener());
        this.view.addVisualizeButtonListener(new VisualizeButtonListener());
        this.view.addSocialGraphButtonListener(new SocialGraphButtonListener());
        this.view.addInspectionMenuListener(new InspectionMenuListener());
        this.view.addCreateCharacterButtonListener(new CreateCharacterButtonListener());

        // Wire up simulation controls
        this.view.addPlayPauseListener(e -> toggleSimulation());
        this.view.addStepListener(e -> stepSimulation());
        this.view.addSpeedChangeListener(e -> updateSimulationSpeed());

        this.time = new Time();
    }

    // ==================== Town-based Population Management ====================

    /**
     * Gets the Town entity containing population pools.
     * Only available when using Town-based generation.
     *
     * @return the Town, or null if using legacy generation
     */
    public Town getTown() {
        return town;
    }

    /**
     * Checks if Town-based generation is enabled.
     *
     * @return true if using the new Town-based architecture
     */
    public boolean isUsingTownBasedGeneration() {
        return useTownBasedGeneration;
    }

    /**
     * Enables or disables Town-based generation.
     * Must be called before generating a world.
     *
     * @param enabled true to use Town-based generation
     */
    public void setUseTownBasedGeneration(boolean enabled) {
        this.useTownBasedGeneration = enabled;
    }

    /**
     * Determines the funding level for school generation.
     * This can be set by UI, derived from demographics, or randomized.
     *
     * @return the funding model to use for school generation
     */
    private SchoolFundingModel determineFundingLevel() {
        // Check if custom demographics are set and derive funding from income
        // distribution
        if (view.isCustomDemographicsEnabled()) {
            double lowIncome = view.getDemographicsIncomeLowPercent() / 100.0;
            double highIncome = view.getDemographicsIncomeHighPercent() / 100.0;

            // Higher low-income percentage tends toward underfunded schools
            // Higher high-income percentage tends toward well-funded schools
            if (lowIncome > 0.4) {
                return new SchoolFundingModel(SchoolFundingModel.FundingLevel.UNDERFUNDED);
            } else if (lowIncome > 0.3) {
                return new SchoolFundingModel(SchoolFundingModel.FundingLevel.ADEQUATE);
            } else if (highIncome > 0.3) {
                return new SchoolFundingModel(SchoolFundingModel.FundingLevel.WELL_FUNDED);
            } else if (highIncome > 0.4) {
                return new SchoolFundingModel(SchoolFundingModel.FundingLevel.EXCELLENTLY_FUNDED);
            }
        }

        // Default: randomize funding level with realistic distribution
        // Most schools are adequately funded
        int random = Randomizer.setRandom(1, 100);
        if (random <= 10) {
            return new SchoolFundingModel(SchoolFundingModel.FundingLevel.SEVERELY_UNDERFUNDED);
        } else if (random <= 25) {
            return new SchoolFundingModel(SchoolFundingModel.FundingLevel.UNDERFUNDED);
        } else if (random <= 70) {
            return new SchoolFundingModel(SchoolFundingModel.FundingLevel.ADEQUATE);
        } else if (random <= 90) {
            return new SchoolFundingModel(SchoolFundingModel.FundingLevel.WELL_FUNDED);
        } else {
            return new SchoolFundingModel(SchoolFundingModel.FundingLevel.EXCELLENTLY_FUNDED);
        }
    }

    /**
     * Gets the current school's funding model.
     *
     * @return the funding model, or null if no school has been generated
     */
    public SchoolFundingModel getSchoolFundingModel() {
        return standardSchool != null ? standardSchool.getFundingModel() : null;
    }

    /**
     * Gets the number of available (unassigned) students in the town.
     *
     * @return the count of available students, or 0 if not using Town-based
     *         generation
     */
    public int getAvailableStudentCount() {
        return town != null ? town.getAvailableStudentCount() : 0;
    }

    /**
     * Gets the number of available (unassigned) staff in the town.
     *
     * @return the count of available staff, or 0 if not using Town-based generation
     */
    public int getAvailableStaffCount() {
        return town != null ? town.getAvailableStaffCount() : 0;
    }

    /**
     * Toggles the simulation between playing and paused states.
     */
    private void toggleSimulation() {
        if (simulationRunning) {
            pauseSimulation();
        } else {
            startSimulation();
        }
        view.updatePlayPauseButton(simulationRunning);
    }

    /**
     * Updates the simulation speed based on UI selection.
     */
    private void updateSimulationSpeed() {
        if (simulationEngine != null) {
            int speedIndex = view.getSelectedSpeedIndex();
            simulationEngine.setSpeedByIndex(speedIndex);
            String[] speedNames = { "Slow (1x)", "Normal (2x)", "Fast (4x)", "Very Fast (8x)" };
            view.appendOutput("Simulation speed set to: " + speedNames[speedIndex]);
        }
    }

    /**
     * Initializes the simulation engine after world generation.
     */
    private void initializeSimulation() {
        // Create simulation engine
        simulationEngine = new SimulationEngine(time, standardSchool, studentHashMap, staffHashMap);

        // Connect social link system so interactions update relationship scores
        if (socialLinkConnector != null) {
            simulationEngine.setSocialLinkConnector(socialLinkConnector);
        }

        // Create entity state manager and initialize all entities
        entityStateManager = new EntityStateManager(studentHashMap, staffHashMap, standardSchool, time);
        entityStateManager.initializeAll();
        entityStateManager.placeStudentsAtStartOfDay();
        entityStateManager.placeStaffAtStartOfDay();

        // Add simulation listener to update UI
        simulationEngine.addListener(new SimulationEngine.SimulationListener() {
            @Override
            public void onTick(int tickNumber, Time time) {
                SwingUtilities.invokeLater(() -> {
                    updateTimeLabel();
                    updatePeriodDisplay();
                    view.updateSimulationStatus(simulationRunning ? "Running" : "Paused");
                });
            }

            @Override
            public void onPeriodChange(int oldPeriod, int newPeriod) {
                SwingUtilities.invokeLater(() -> {
                    view.appendOutput("Period changed: " + oldPeriod + " -> " + newPeriod);
                    updatePeriodDisplay();
                });
            }

            @Override
            public void onTransitionStart() {
                SwingUtilities.invokeLater(() -> {
                    view.appendOutput("Transition period started - students moving to next class");
                    updatePeriodDisplay();
                });
            }

            @Override
            public void onTransitionEnd() {
                SwingUtilities.invokeLater(() -> {
                    view.appendOutput("Transition ended - classes resuming");
                });
            }

            @Override
            public void onLunchStart(String lunchPeriod) {
                SwingUtilities.invokeLater(() -> {
                    view.appendOutput("Lunch " + lunchPeriod + " has started");
                });
            }

            @Override
            public void onLunchEnd(String lunchPeriod) {
                SwingUtilities.invokeLater(() -> {
                    view.appendOutput("Lunch " + lunchPeriod + " has ended");
                });
            }

            @Override
            public void onDayEnd() {
                SwingUtilities.invokeLater(() -> {
                    view.appendOutput("School day has ended!");
                    stopSimulation();
                });
            }
        });

        // Create timer for automatic simulation updates
        // Timer fires once per second, engine processes multiple ticks based on speed
        simulationTimer = new Timer(1000, e -> {
            if (simulationRunning && simulationEngine != null) {
                simulationEngine.update();
            }
        });

        // Set initial speed based on UI
        updateSimulationSpeed();

        view.appendOutput("Simulation engine initialized!");
        if (studentHashMap != null) {
            view.appendOutput("Behavior trees assigned to " + studentHashMap.size() + " students");
        } else {
            view.appendOutput("WARNING: No students loaded - simulation may not work correctly");
        }
        view.appendOutput("Use Play/Pause button or Simulation menu to control the simulation.");
    }

    /**
     * Starts the simulation.
     */
    public void startSimulation() {
        if (simulationEngine != null) {
            simulationRunning = true;
            simulationEngine.start();
            simulationTimer.start();
            view.updateSimulationStatus("Running");
            view.updatePlayPauseButton(true);
        }
    }

    /**
     * Pauses the simulation.
     */
    public void pauseSimulation() {
        simulationRunning = false;
        if (simulationEngine != null) {
            simulationEngine.pause();
        }
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        view.updateSimulationStatus("Paused");
        view.updatePlayPauseButton(false);
    }

    /**
     * Stops the simulation.
     */
    public void stopSimulation() {
        simulationRunning = false;
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        view.updateSimulationStatus("Stopped");
        view.updatePlayPauseButton(false);
    }

    /**
     * Steps the simulation forward by one tick (regardless of speed).
     */
    public void stepSimulation() {
        if (simulationEngine != null) {
            // Pause if running, then step
            if (simulationRunning) {
                pauseSimulation();
            }
            simulationEngine.tick();
        }
    }

    /**
     * Sets the simulation speed.
     *
     * @param speed speed in minutes per tick
     */
    public void setSimulationSpeed(int speed) {
        if (simulationEngine != null) {
            simulationEngine.setSpeed(speed);
        }
    }

    private void updateTimeLabel() {
        String formattedDate = time.getFormattedDate();
        view.updateTime("Today is " + formattedDate);
    }

    /**
     * Gets the appropriate period status string based on current time.
     *
     * @return "Before School", "Transition", "After School", or null if in a
     *         regular period
     */
    private String getPeriodStatus() {
        if (simulationEngine == null) {
            return null;
        }

        var bellSchedule = simulationEngine.getBellSchedule();
        if (bellSchedule == null) {
            return null;
        }

        if (bellSchedule.isBeforeSchool(time)) {
            return "Before School";
        } else if (bellSchedule.isAfterSchool(time)) {
            return "After School";
        } else if (bellSchedule.isTransitionTime(time)) {
            return "Transition";
        }

        return null; // In a regular period
    }

    /**
     * Updates the period display with the current period and status.
     */
    private void updatePeriodDisplay() {
        int period = time.getCurrentPeriod();
        String status = getPeriodStatus();
        view.updatePeriod(period, status);
    }

    private void updateWeatherLabels() {
        String rootPath = "/Resources/Weather/Icons/";
        Weather weather = new Weather(standardSchool.getSchoolName());
        WeatherPatterns[] weatherArray = weather.determineWeatherAMPM(time.getCurrentDate());
        view.updateWeatherIcons(rootPath + weatherArray[0].getIconName(), rootPath + weatherArray[1].getIconName(),
                weatherArray[0].toString(), weatherArray[1].toString());
        view.updateWeatherTemps(weather.getTemp("TMAX"), weather.getTemp("TMIN"));
        view.updateDayLabel(time.getDayName());
    }

    private void showInspectionWindow(String type) {
        if (type.equals("Neighborhoods")) {
            showNeighborhoodWindow();
            return;
        }

        JFrame inspectionFrame = new JFrame(type + " Inspection");
        inspectionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea inspectionArea = new JTextArea();
        inspectionArea.setEditable(false);
        inspectionArea.setLineWrap(true);
        inspectionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inspectionArea);

        if (type.equals("Staff")) {
            inspectionFrame.setSize(550, 500);
            ArrayList<Staff> staffList = new ArrayList<>(staffHashMap.values());
            staffList.sort(Comparator.comparing(staff -> staff.teacherName.getLastName()));

            DefaultListModel<Staff> listModel = new DefaultListModel<>();
            for (Staff staff : staffList) {
                listModel.addElement(staff);
            }

            JList<Staff> staffJList = new JList<>(listModel);
            staffJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            staffJList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    Staff selectedStaff = staffJList.getSelectedValue();
                    if (selectedStaff != null) {
                        staffInspection(selectedStaff, inspectionArea);
                    }
                }
            });

            inspectionFrame.setLayout(new BorderLayout());
            inspectionFrame.add(new JScrollPane(staffJList), BorderLayout.WEST);
            inspectionFrame.add(scrollPane, BorderLayout.CENTER);
        } else {
            // Student inspection uses a tabbed pane (Info + Schedule tabs)
            inspectionFrame.setSize(850, 550);
            HashMap<Integer, Student> studentGradeClass = standardSchool.getStudentGradeClass(type);

            if (studentGradeClass != null) {
                ArrayList<Student> studentList = new ArrayList<>(studentGradeClass.values());
                studentList.sort(Comparator.comparing(student -> student.studentName.getLastName()));

                DefaultListModel<Student> listModel = new DefaultListModel<>();
                for (Student student : studentList) {
                    listModel.addElement(student);
                }

                JList<Student> studentListComponent = new JList<>(listModel);
                studentListComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                // Create button panel with Show Social Links button
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JButton showSocialLinksButton = new JButton("Show Social Links");
                showSocialLinksButton.setEnabled(false);
                showSocialLinksButton.setToolTipText("Select a student to view their social links");
                buttonPanel.add(showSocialLinksButton);

                // Tabbed pane for Info + Schedule
                JTabbedPane studentTabs = new JTabbedPane();
                studentTabs.addTab("Info", scrollPane);
                // Placeholder schedule panel until a student is selected
                JPanel emptySchedule = new JPanel(new BorderLayout());
                emptySchedule.add(new JLabel("Select a student to view their schedule",
                        SwingConstants.CENTER), BorderLayout.CENTER);
                studentTabs.addTab("Schedule", emptySchedule);

                // Track the currently selected student for the button action
                final Student[] currentlySelectedStudent = { null };

                studentListComponent.addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Student selectedStudent = studentListComponent.getSelectedValue();
                        if (selectedStudent != null) {
                            currentlySelectedStudent[0] = selectedStudent;
                            studentInspection(selectedStudent, inspectionArea);

                            // Replace the Schedule tab content with this student's schedule
                            JPanel schedulePanel = Inspector.buildStudentSchedulePanel(selectedStudent);
                            studentTabs.setComponentAt(1, schedulePanel);

                            showSocialLinksButton.setEnabled(true);
                            showSocialLinksButton.setToolTipText("View social links for " +
                                    selectedStudent.studentName.getFirstName() + " " +
                                    selectedStudent.studentName.getLastName());
                        }
                    }
                });

                // Add action listener for the Show Social Links button
                showSocialLinksButton.addActionListener(e -> {
                    if (currentlySelectedStudent[0] != null) {
                        socialLinkConnector.studentVisualizer(currentlySelectedStudent[0]);
                    }
                });

                inspectionFrame.setLayout(new BorderLayout());
                inspectionFrame.add(new JScrollPane(studentListComponent), BorderLayout.WEST);
                inspectionFrame.add(studentTabs, BorderLayout.CENTER);
                inspectionFrame.add(buttonPanel, BorderLayout.SOUTH);
            }
        }

        inspectionFrame.setVisible(true);
    }

    private void showNeighborhoodWindow() {
        JFrame neighborhoodFrame = new JFrame("Neighborhood Inspection");
        neighborhoodFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        neighborhoodFrame.setSize(900, 600);

        if (town == null || town.getNeighborhoods().isEmpty()) {
            JTextArea emptyState = new JTextArea("No neighborhoods have been generated yet.");
            emptyState.setEditable(false);
            emptyState.setLineWrap(true);
            emptyState.setWrapStyleWord(true);
            neighborhoodFrame.add(new JScrollPane(emptyState), BorderLayout.CENTER);
            neighborhoodFrame.setVisible(true);
            return;
        }

        JTabbedPane neighborhoodTabs = new JTabbedPane();
        List<Neighborhood> neighborhoods = new ArrayList<>(town.getNeighborhoods());
        neighborhoods.sort(Comparator.comparing(Neighborhood::getWealthLevel).thenComparing(Neighborhood::getName));

        for (Neighborhood neighborhood : neighborhoods) {
            neighborhoodTabs.addTab(neighborhood.getName(), buildNeighborhoodPanel(neighborhood));
        }

        neighborhoodFrame.setLayout(new BorderLayout());
        neighborhoodFrame.add(neighborhoodTabs, BorderLayout.CENTER);
        neighborhoodFrame.setVisible(true);
    }

    private JPanel buildNeighborhoodPanel(Neighborhood neighborhood) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setRows(5);
        summaryArea.setText(
                "Neighborhood: " + neighborhood.getName() + "\n" +
                        "Wealth Level: " + capitalizeLabel(neighborhood.getWealthLevel()) + "\n" +
                        "Distance From School: " + neighborhood.getDistanceFromSchoolMiles() + " miles\n" +
                        "Population: " + neighborhood.getCurrentPopulation() + " / " +
                        neighborhood.getPopulationCapacity() + "\n" +
                        "Residents: " + neighborhood.getStudentsInSchool().size() + " students in school, " +
                        neighborhood.getSiblingsNotInSchool().size() + " siblings not in school, " +
                        neighborhood.getStaff().size() + " staff");

        JTable residentTable = buildNeighborhoodResidentTable(neighborhood);
        JScrollPane tableScrollPane = new JScrollPane(residentTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Residents"));

        panel.add(summaryArea, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JTable buildNeighborhoodResidentTable(Neighborhood neighborhood) {
        String[] columns = { "Name", "Resident Type", "School Status", "Grade / Role", "Income" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Student> inSchoolStudents = new ArrayList<>(neighborhood.getStudentsInSchool());
        inSchoolStudents.sort(Comparator.comparing(student -> student.studentName.getLastName()));
        for (Student student : inSchoolStudents) {
            model.addRow(new Object[] {
                    student.studentName.getFullName(),
                    "Student",
                    "In School",
                    student.studentStatistics.getGradeLevel(),
                    capitalizeLabel(student.studentStatistics.getIncomeLevel())
            });
        }

        List<Student> outOfSchoolSiblings = new ArrayList<>(neighborhood.getSiblingsNotInSchool());
        outOfSchoolSiblings.sort(Comparator.comparing(student -> student.studentName.getLastName()));
        for (Student sibling : outOfSchoolSiblings) {
            model.addRow(new Object[] {
                    sibling.studentName.getFullName(),
                    "Sibling",
                    "Not In School",
                    sibling.studentStatistics.getGradeLevel(),
                    capitalizeLabel(sibling.studentStatistics.getIncomeLevel())
            });
        }

        List<Staff> staffMembers = new ArrayList<>(neighborhood.getStaff());
        staffMembers.sort(Comparator.comparing(staff -> staff.teacherName.getLastName()));
        for (Staff staff : staffMembers) {
            Object staffType = staff.teacherStatistics.getStaffType();
            model.addRow(new Object[] {
                    staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName(),
                    "Staff",
                    "Assigned Staff",
                    staffType != null ? staffType.toString() : "Unassigned",
                    "-"
            });
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        return table;
    }

    private String capitalizeLabel(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * Shows the character creation menu.
     *
     * @return true if character was created, false if cancelled
     */
    private boolean showCharacterCreationMenu() {
        final boolean[] characterCreated = { false };
        JDialog dialog = new JDialog((Frame) null, "Create Player Character", true);
        dialog.getContentPane().setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        int row = 0;

        // First Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        JTextField firstNameField = new JTextField();
        firstNameField.setColumns(12);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(firstNameField, gbc);
        row++;

        // Last Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Last Name:"), gbc);
        JTextField lastNameField = new JTextField();
        lastNameField.setColumns(12);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(lastNameField, gbc);
        row++;

        // Suffix
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Suffix:"), gbc);
        JComboBox<String> suffixDropdown = new JComboBox<>(
                new String[] { "Jr.", "Sr.", "III", "II", "IV", "V", "None" });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(suffixDropdown, gbc);
        row++;

        // Gender
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Gender:"), gbc);
        JComboBox<String> genderDropdown = new JComboBox<>(new String[] { "Male", "Female", "Other" });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(genderDropdown, gbc);
        row++;

        // Race (checkboxes - multiple selections map to multiracial)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Race:"), gbc);
        JPanel racePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JCheckBox whiteCheckbox = new JCheckBox("White");
        JCheckBox blackCheckbox = new JCheckBox("Black");
        JCheckBox asianCheckbox = new JCheckBox("Asian");
        JCheckBox latinoCheckbox = new JCheckBox("Latino");
        racePanel.add(whiteCheckbox);
        racePanel.add(blackCheckbox);
        racePanel.add(asianCheckbox);
        racePanel.add(latinoCheckbox);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(racePanel, gbc);
        row++;

        // Helper to get race code from checkboxes
        java.util.function.Supplier<String> getRaceCode = () -> {
            int count = 0;
            String singleRace = null;
            if (whiteCheckbox.isSelected()) {
                count++;
                singleRace = "white";
            }
            if (blackCheckbox.isSelected()) {
                count++;
                singleRace = "black";
            }
            if (asianCheckbox.isSelected()) {
                count++;
                singleRace = "api";
            }
            if (latinoCheckbox.isSelected()) {
                count++;
                singleRace = "hispanic";
            }

            if (count == 0)
                return null; // No race selected
            if (count > 1)
                return "2prace"; // Multiracial
            return singleRace; // Single race
        };

        // Add an action listener to enable/disable the suffix dropdown based on gender
        // selection
        genderDropdown.addActionListener(e -> {
            String selectedGender = (String) genderDropdown.getSelectedItem();
            if ("Male".equals(selectedGender) || "Other".equals(selectedGender)) {
                suffixDropdown.setEnabled(true);
            } else {
                suffixDropdown.setEnabled(false);
            }
        });

        // Set initial state of suffix dropdown
        suffixDropdown.setEnabled(false);

        // Eye Color
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Eye Color:"), gbc);
        JComboBox<String> eyeColorDropdown = new JComboBox<>(
                TraitLoader.getOptionsFromJson("/Resources.People/eye_color.json"));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(eyeColorDropdown, gbc);
        row++;

        // Hair Color
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Hair Color:"), gbc);
        JComboBox<String> hairColorDropdown = new JComboBox<>(
                TraitLoader.getOptionsFromJson("/Resources.People/hair_color.json"));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(hairColorDropdown, gbc);
        row++;

        // Hair Length
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Hair Length:"), gbc);
        JComboBox<String> hairLengthDropdown = new JComboBox<>(new String[] { "Short", "Medium", "Long" });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(hairLengthDropdown, gbc);
        row++;

        // Hair Type
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Hair Type:"), gbc);
        JComboBox<String> hairTypeDropdown = new JComboBox<>(
                TraitLoader.getOptionsFromJson("/Resources.People/hair_type.json"));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(hairTypeDropdown, gbc);
        row++;

        // Birthdate
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Birthdate:"), gbc);
        UtilDateModel model = new UtilDateModel();
        model.setDate(1989, 8, 1);
        model.setSelected(true);
        // Set the default date to today
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        datePicker.addActionListener(e -> {
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            if (selectedDate != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(selectedDate);

                java.util.Calendar minDate = java.util.Calendar.getInstance();
                minDate.set(1989, java.util.Calendar.AUGUST, 1);

                java.util.Calendar maxDate = java.util.Calendar.getInstance();
                maxDate.set(1990, java.util.Calendar.SEPTEMBER, 31);

                if (cal.before(minDate) || cal.after(maxDate)) {
                    // Reset to initial date if out of range
                    model.setDate(1989, 8, 1);
                    model.setSelected(true);
                }
            }
        });

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(datePicker, gbc);
        row++;

        // Family Income
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Family Income:"), gbc);
        JComboBox<String> incomeDropdown = new JComboBox<>(new String[] { "Low", "Middle", "High" });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(incomeDropdown, gbc);
        row++;

        // Number of Siblings
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Number of Siblings:"), gbc);
        JComboBox<Integer> siblingsDropdown = new JComboBox<>(new Integer[] { 0, 1, 2, 3, 4, 5 });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(siblingsDropdown, gbc);
        row++;

        // Story Output Area (scrollable)
        JTextArea storyOutput = new JTextArea(8, 30);
        storyOutput.setEditable(false);
        DefaultCaret storyCaret = (DefaultCaret) storyOutput.getCaret();
        storyCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane storyScrollPane = new JScrollPane(storyOutput);
        JPanel storyPanel = new JPanel(new BorderLayout());
        storyPanel.add(new JLabel("Story Output:"), BorderLayout.NORTH);
        storyPanel.add(storyScrollPane, BorderLayout.CENTER);
        dialog.add(formPanel, BorderLayout.NORTH);
        dialog.add(storyPanel, BorderLayout.CENTER);

        // Character Creation Buttons
        JButton previewButton = new JButton("Preview Character");
        JButton startGameButton = new JButton("Start Game");
        JButton randomizeButton = new JButton("Randomize");
        JButton cancelButton = new JButton("Cancel");

        // Start Game is disabled until character is previewed
        startGameButton.setEnabled(false);
        startGameButton.setToolTipText("Preview your character first");

        // Start Game closes the dialog and proceeds with game
        startGameButton.addActionListener(e -> {
            characterCreated[0] = true;
            dialog.dispose();
        });

        previewButton.addActionListener(e -> {
            PlayerCharacter playerCharacter = new PlayerCharacter();
            playerCharacter.studentName.setFirstName(firstNameField.getText());
            playerCharacter.studentName.setLastName(lastNameField.getText());

            // Get race code from checkboxes
            String raceCode = getRaceCode.get();
            if (raceCode == null) {
                storyOutput.append("Error: Please select at least one race.\n");
                return;
            }
            playerCharacter.studentStatistics.setRace(raceCode);
            if (suffixDropdown.getSelectedItem().toString() != "None"
                    || suffixDropdown.getSelectedItem().toString() != null) {
                playerCharacter.studentName.setSuffix(suffixDropdown.getSelectedItem().toString());
            } else {
                playerCharacter.studentName.setSuffix("");
            }
            playerCharacter.studentStatistics.setGender(genderDropdown.getSelectedItem().toString());
            playerCharacter.studentStatistics.setEyeColor(eyeColorDropdown.getSelectedItem().toString());
            if (hairColorDropdown.getSelectedItem().toString() != "None"
                    || hairColorDropdown.getSelectedItem().toString() != null) {
                playerCharacter.studentStatistics.setHairColor(hairColorDropdown.getSelectedItem().toString());
            } else {
                playerCharacter.studentStatistics.setHairColor("");
            }
            if (hairLengthDropdown.getSelectedItem().toString() != "None"
                    || hairLengthDropdown.getSelectedItem().toString() != null) {
                playerCharacter.studentStatistics.setHairLength(hairLengthDropdown.getSelectedItem().toString());
            } else {
                playerCharacter.studentStatistics.setHairLength("");
            }
            if (hairTypeDropdown.getSelectedItem().toString() != "None"
                    || hairTypeDropdown.getSelectedItem().toString() != null) {
                playerCharacter.studentStatistics.setHairType(hairTypeDropdown.getSelectedItem().toString());
            } else {
                playerCharacter.studentStatistics.setHairType("");
            }
            playerCharacter.studentStatistics.setGradeLevel(0);
            playerCharacter.studentStatistics.setInitHeight();
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            if (selectedDate != null) {
                java.time.LocalDate localDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                playerCharacter.studentStatistics.setBirthday(localDate);
            }
            // Align income casing with NPC generation (low|middle|high)
            playerCharacter.studentStatistics.setIncomeLevel(incomeDropdown.getSelectedItem().toString().toLowerCase());
            playerCharacter.setSiblings((Integer) siblingsDropdown.getSelectedItem());

            // Initialize fields similar to NPC generation
            playerCharacter.studentStatistics.setExperience(0);

            // Compute skin color from race + eye color
            String eyesForSkin = playerCharacter.studentStatistics.getEyeColor();
            if (eyesForSkin != null) {
                playerCharacter.studentStatistics
                        .setSkinColor(TraitSelection.studentSkinColorSelection(raceCode, eyesForSkin));
            }

            // Append simple story points to the output window
            storyOutput.append("Generating base stats...\n");
            playerCharacter.studentStatistics.setInitStrength();
            playerCharacter.studentStatistics.setIntelligence((int) GameRandom
                    .nextGaussian(STUDENT_POP_INTELLIGENCE_MEAN, STUDENT_POP_INTELLIGENCE_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setCharisma(
                    (int) GameRandom.nextGaussian(STUDENT_POP_CHARISMA_MEAN, STUDENT_POP_CHARISMA_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setAgility(
                    (int) GameRandom.nextGaussian(STUDENT_POP_AGILITY_MEAN, STUDENT_POP_AGILITY_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setDetermination((int) GameRandom
                    .nextGaussian(STUDENT_POP_DETERMINATION_MEAN, STUDENT_POP_DETERMINATION_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setPerception((int) GameRandom.nextGaussian(STUDENT_POP_PERCEPTION_MEAN,
                    STUDENT_POP_PERCEPTION_STANDARD_DEVIATION));
            playerCharacter.studentStatistics
                    .setLuck((int) GameRandom.nextGaussian(STUDENT_POP_LUCK_MEAN, STUDENT_POP_LUCK_STANDARD_DEVIATION));
            // Derived attributes mirroring NPC generation
            playerCharacter.studentStatistics.setInitCreativity();
            playerCharacter.studentStatistics.setInitEmpathy();
            playerCharacter.studentStatistics.setInitAdaptability();
            playerCharacter.studentStatistics.setInitInitiative();
            playerCharacter.studentStatistics.setInitResilience();
            playerCharacter.studentStatistics.setInitCuriosity();
            playerCharacter.studentStatistics.setInitResponsibility();
            playerCharacter.studentStatistics.setInitOpenMind();

            // Initialize allostatic load tolerance (depends on resilience and
            // determination)
            playerCharacter.studentStatistics.initAllostaticLoad();

            // Apply braces attributes (timing, cosmetics, charisma effects)
            SiblingGenerator.applyBracesAttributes(playerCharacter);

            PlayerStoryGenerator.reportBaseStats(playerCharacter, storyOutput);

            // Generate and attach family info (parents and siblings) BEFORE story
            // generation
            FamilyInfo family = new FamilyInfo();
            // Parents: use staff name generation approach (adult birth years)
            java.time.LocalDate fatherBirth = BirthdayGenerator.generateRandomBirthdayStaff();
            String fatherFirst;
            String playerSuffixVal = playerCharacter.studentName.getSuffix();
            if (playerSuffixVal != null && (playerSuffixVal.equals("Jr.") ||
                    playerSuffixVal.equals("II") ||
                    playerSuffixVal.equals("III") ||
                    playerSuffixVal.equals("IV") ||
                    playerSuffixVal.equals("V"))) {
                fatherFirst = playerCharacter.studentName.getFirstName();
            } else {
                fatherFirst = NameLoader.nameGenerator(String.valueOf(fatherBirth.getYear()), "Male");
            }
            java.time.LocalDate motherBirth = BirthdayGenerator.generateRandomBirthdayStaff();
            String motherFirst = NameLoader.nameGenerator(String.valueOf(motherBirth.getYear()), "Female");
            family.setFather(new ParentInfo("Father", fatherFirst));
            family.setMother(new ParentInfo("Mother", motherFirst));

            // Siblings: produce names and birthdays using sibling generator logic
            int sibCount = playerCharacter.getSiblings();
            java.util.List<SiblingInfo> sibInfos = SiblingGenerator.generateSiblingInfosForPlayer(playerCharacter,
                    sibCount, view);
            for (SiblingInfo info : sibInfos) {
                family.addSibling(info);
            }
            playerCharacter.setFamilyInfo(family);

            // Build the life history once, display it, and store it
            storyOutput.append("\n═══════════ Life History ═══════════\n\n");
            entity.LifeHistory lifeHistory = PlayerStoryGenerator.buildLifeHistory(playerCharacter);
            lifeHistory.appendToTextArea(storyOutput);
            playerCharacter.setLifeHistory(lifeHistory);
            storyOutput.append("═══════════════════════════════════\n");

            // Enable Start Game button after preview is complete
            startGameButton.setEnabled(true);
            startGameButton.setToolTipText("Begin your adventure!");
            storyOutput.append("\n--- Character ready! Click 'Start Game' to begin. ---\n");
        });
        cancelButton.addActionListener(e -> {
            dialog.dispose();
        });
        randomizeButton.addActionListener(e -> {
            // Ensure datasets are loaded for name and race distributions
            NameLoader.readCSVFirst("1986");
            NameLoader.readCSVFirst("1987");
            NameLoader.readCSVFirst("1988");
            NameLoader.readCSVFirst("1989");
            NameLoader.readCSVFirst("1990");
            NameLoader.readCSVLastStudent();

            // Core generated attributes
            String gender = GenderLoader.genderSelection();
            String[] lastNameAndRace = NameLoader.selectWeightedRandom();
            String lastName = lastNameAndRace[0];
            String raceCode = lastNameAndRace[1];

            // Birthday within freshman range
            String gradeLevel = "Freshman";
            java.time.LocalDate birthday = BirthdayGenerator.generateDateFromClass(gradeLevel);

            // First name by birth year and gender
            String firstName = NameLoader.nameGenerator(String.valueOf(birthday.getYear()), gender);

            // Capitalize last name and optionally hyphenate
            lastName = StudentName.capitalizeName(lastName);
            if (setRandom(0, STUDENT_HYPHEN_GENERATION_SAMPLE_SIZE) < STUDENT_HYPHEN_GENERATION_RATE) {
                String hyphenName = NameLoader.selectWeightedRandom()[0];
                hyphenName = StudentName.capitalizeName(hyphenName);
                lastName = lastName + "-" + hyphenName;
            }

            // Suffix (male-biased)
            String suffixValue = "None";
            if (gender != null && gender.equalsIgnoreCase("Male")
                    && setRandom(0, SUFFIX_GENERATION_SAMPLE_SIZE) < SUFFIX_GENERATION_RATE) {
                suffixValue = NameLoader.suffixNameGenerator(gender);
            }

            // Set race checkboxes based on internal code
            whiteCheckbox.setSelected(false);
            blackCheckbox.setSelected(false);
            asianCheckbox.setSelected(false);
            latinoCheckbox.setSelected(false);
            switch (raceCode) {
                case "white" -> whiteCheckbox.setSelected(true);
                case "black" -> blackCheckbox.setSelected(true);
                case "api" -> asianCheckbox.setSelected(true);
                case "hispanic" -> latinoCheckbox.setSelected(true);
                case "2prace" -> {
                    // For multiracial, randomly select 2 races
                    int first = setRandom(0, 3);
                    int second;
                    do {
                        second = setRandom(0, 3);
                    } while (second == first);
                    JCheckBox[] boxes = { whiteCheckbox, blackCheckbox, asianCheckbox, latinoCheckbox };
                    boxes[first].setSelected(true);
                    boxes[second].setSelected(true);
                }
            }

            // Eye and hair traits driven by race
            String eyeColor = TraitSelection.studentEyeColorSelection(raceCode);
            String hairColor = TraitSelection.studentHairSelection(raceCode, eyeColor);
            String hairType = TraitSelection.studentHairType(raceCode, hairColor);

            // Hair length from available UI options
            int hlCount = hairLengthDropdown.getItemCount();
            String hairLength = hairLengthDropdown.getItemAt(setRandom(0, hlCount - 1));

            // Income distribution: Low (25%), Middle (60%), High (15%) - from SimConstants
            int incomeRoll = setRandom(0, STUDENT_INCOME_LEVEL_SAMPLE_SIZE);
            String incomeUi = (incomeRoll <= INCOME_THRESHOLD_LOW) ? "Low"
                    : (incomeRoll <= INCOME_THRESHOLD_MIDDLE) ? "Middle" : "High";

            // Siblings: 0-5
            int siblings = setRandom(0, 5);

            // Populate UI controls
            firstNameField.setText(firstName);
            lastNameField.setText(lastName);
            String genderUi = gender == null || gender.isBlank() ? "Other"
                    : (Character.toUpperCase(gender.charAt(0)) + gender.substring(1).toLowerCase());
            genderDropdown.setSelectedItem(genderUi);
            suffixDropdown.setSelectedItem(suffixValue);
            // Race checkboxes are already set above
            eyeColorDropdown.setSelectedItem(eyeColor);
            hairColorDropdown.setSelectedItem(hairColor);
            hairTypeDropdown.setSelectedItem(hairType);
            hairLengthDropdown.setSelectedItem(hairLength);
            incomeDropdown.setSelectedItem(incomeUi);
            siblingsDropdown.setSelectedItem(siblings);

            // Set birthdate
            model.setDate(birthday.getYear(), birthday.getMonthValue() - 1, birthday.getDayOfMonth());
            model.setSelected(true);
        });
        // Disable preview until required fields are filled
        previewButton.setEnabled(false);
        DocumentListener docListener = new DocumentListener() {
            private void validateForm() {
                boolean hasFirst = firstNameField.getText() != null && !firstNameField.getText().trim().isEmpty();
                boolean hasLast = lastNameField.getText() != null && !lastNameField.getText().trim().isEmpty();
                boolean hasGender = genderDropdown.getSelectedItem() != null;
                boolean hasBirthday = datePicker.getModel().getValue() != null;
                previewButton.setEnabled(hasFirst && hasLast && hasGender && hasBirthday);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateForm();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateForm();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateForm();
            }
        };
        firstNameField.getDocument().addDocumentListener(docListener);
        lastNameField.getDocument().addDocumentListener(docListener);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Insets small = new Insets(2, 8, 2, 8);
        previewButton.setMargin(small);
        startGameButton.setMargin(small);
        randomizeButton.setMargin(small);
        cancelButton.setMargin(small);
        buttonPanel.add(randomizeButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(startGameButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setVisible(true);

        return characterCreated[0];
    }

    private class DateLabelFormatter extends AbstractFormatter {
        private String datePattern = "yyyy-MM-dd";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                java.util.Calendar cal = (java.util.Calendar) value;
                return dateFormatter.format(cal.getTime());
            }

            return "";
        }
    }

    class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // For game mode, show character creation BEFORE generating the world
            if (view.isGameMode()) {
                boolean characterCreated = showCharacterCreationMenu();
                if (!characterCreated) {
                    // User cancelled character creation, don't generate world
                    return;
                }
            }
            // Generate the world (only if not game mode, or if character was created)
            new SchoolGenerationWorker().execute();
        }
    }

    class VisualizeButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            roomConnector.visualizer(standardSchool);
        }
    }

    class InspectionMenuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            showInspectionWindow(command);
        }
    }

    class SocialGraphButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            socialLinkConnector.schoolSocialLinkVisualizer();
        }
    }

    class CreateCharacterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showCharacterCreationMenu();
        }
    }

    private class SchoolGenerationWorker extends SwingWorker<Void, String> {

        @Override
        protected Void doInBackground() {
            try {
                // Stop any running simulation first
                if (simulationRunning) {
                    stopSimulation();
                }

                // Initialize or update the centralized logger
                // Always apply the user's current debug menu settings (preserves any changes
                // made before clicking New Simulation/New Game)
                GameLogger.setView(view);
                javax.swing.SwingUtilities.invokeLater(() -> view.applyDebugMenuToLogger());

                // Reset time to starting values
                time.reset();

                // Initialize the seeded random generator
                long seed;
                if (view.isCustomSeedEnabled()) {
                    Long customSeed = view.getCustomSeed();
                    if (customSeed != null) {
                        GameRandom.initialize(customSeed);
                        seed = customSeed;
                        publish("Using custom seed: " + seed);
                    } else {
                        // Invalid seed input, show error and use random
                        javax.swing.SwingUtilities.invokeLater(() -> view.showSeedError());
                        seed = GameRandom.initialize();
                        publish("Invalid seed input - using random seed: " + seed);
                    }
                } else {
                    seed = GameRandom.initialize();
                    publish("World Seed: " + seed);
                }
                final long finalSeed = seed;
                javax.swing.SwingUtilities.invokeLater(() -> view.updateCurrentSeed(finalSeed));
                publish("(Save this seed to recreate the same world!)");

                if (useTownBasedGeneration) {
                    // New Town-based generation flow
                    generateWithTown();
                } else {
                    // Legacy generation flow (kept for backward compatibility)
                    generateLegacy();
                }

            } catch (Throwable t) {
                t.printStackTrace();
                publish("Caught an exception: " + t.getMessage());
            }
            return null;
        }

        /**
         * New Town-based generation flow.
         * Generates population independently from school, then assigns to school.
         * Now supports funding levels and demand-driven staffing.
         */
        private void generateWithTown() {
            String[] colors;
            Gym[] gyms;
            AthleticField[] athleticFields;
            LibraryR[] libraries;
            Auditorium[] auditoriums;

            // Step 1: Determine funding level for school
            config.SchoolFundingModel fundingModel = determineFundingLevel();
            publish("School funding level: " + fundingModel.getFundingLevel().getDisplayName());

            // Step 2: Create Town with demographics configuration FIRST
            // This is now independent of school - the town population exists regardless
            publish("Creating town population...");
            TownDemographics demographics = DemographicsLoader.loadOrDefault();

            // Check for custom demographics from UI
            if (view.isCustomDemographicsEnabled()) {
                publish("Using custom demographics settings...");
                demographics.setTotalStudentPopulation(view.getDemographicsStudentPopulation());
                demographics.setTotalStaffPopulation(view.getDemographicsStaffPopulation());
                demographics.setExtraStudentPoolPercent(view.getDemographicsExtraStudentPercent() / 100.0);
                demographics.setExtraStaffPoolPercent(view.getDemographicsExtraStaffPercent() / 100.0);

                // Set custom gender distribution
                java.util.Map<String, Double> genderDist = new java.util.HashMap<>();
                genderDist.put("Male", view.getDemographicsMalePercent() / 100.0);
                genderDist.put("Female", view.getDemographicsFemalePercent() / 100.0);
                demographics.setGenderDistribution(genderDist);

                // Set custom income distribution
                java.util.Map<String, Double> incomeDist = new java.util.HashMap<>();
                incomeDist.put("Low", view.getDemographicsIncomeLowPercent() / 100.0);
                incomeDist.put("Middle", view.getDemographicsIncomeMiddlePercent() / 100.0);
                incomeDist.put("High", view.getDemographicsIncomeHighPercent() / 100.0);
                demographics.setIncomeDistribution(incomeDist);

                publish("  Students: " + demographics.getTotalStudentPopulation() +
                        " (+" + (int) (demographics.getExtraStudentPoolPercent() * 100) + "% pool)");
                publish("  Staff: " + demographics.getTotalStaffPopulation() +
                        " (+" + (int) (demographics.getExtraStaffPoolPercent() * 100) + "% pool)");
                publish("  Gender: " + view.getDemographicsMalePercent() + "% Male / " +
                        view.getDemographicsFemalePercent() + "% Female");
                publish("  Income: " + view.getDemographicsIncomeLowPercent() + "% Low / " +
                        view.getDemographicsIncomeMiddlePercent() + "% Middle / " +
                        view.getDemographicsIncomeHighPercent() + "% High");
            }
            // Note: If not using custom demographics, we use defaults from
            // DemographicsLoader
            // The population is no longer dependent on school capacity

            // Step 3: Generate the school structure based on funding level and target
            // population
            publish("Generating the school...");
            standardSchool = new StandardSchool();
            int targetPopulation = demographics.getTotalStudentPopulation();
            new Director(standardSchool, fundingModel, targetPopulation, view);

            publish("Connecting rooms...");
            roomConnector = new RoomConnector(standardSchool, view);

            publish("School capacity - Optimal: " + standardSchool.getOptimalCapacity() +
                    ", Physical: " + standardSchool.getPhysicalCapacity());

            // Step 4: Generate the town population (completely independent of school)
            colors = standardSchool.getSchoolColors();
            StudentPopGenerator.setSchoolColors(colors);
            SiblingGenerator.setSchoolColors(colors);

            town = TownPopulationGenerator.generateTown("Town", demographics, view);
            town.setTownColors(colors);

            publish("Town population generated: " + town.getStudentPool().getTotalCount() +
                    " students, " + town.getStaffPool().getTotalCount() + " staff");

            // Step 5: Assign population to school using DEMAND-DRIVEN services
            // This analyzes curriculum requirements first, then assigns staff by type
            publish("Assigning population to school using demand-driven staffing...");
            SchoolAssignmentService.populateSchoolDemandDriven(town, standardSchool, view);

            // Get HashMaps for compatibility with existing code
            studentHashMap = SchoolAssignmentService.getStudentHashMap(town, standardSchool);
            staffHashMap = SchoolAssignmentService.getStaffHashMap(town, standardSchool);

            // Step 6: Attempt expansion if scheduling has gaps
            // This adds portables/classrooms/teachers and integrates them into the school
            // map
            publish("Checking if school expansion is needed...");
            SchoolAssignmentService.ExpansionReport expansionReport = SchoolAssignmentService
                    .expandSchoolToMeetDemand(town, standardSchool, roomConnector, view);
            if (expansionReport.expansionOccurred) {
                publish("Expansion complete: +" + expansionReport.classroomsAdded + " classrooms, +"
                        + expansionReport.portablesAdded + " portables, +"
                        + expansionReport.teachersHired + " teachers");
                // Refresh HashMaps after expansion may have changed enrollment
                studentHashMap = SchoolAssignmentService.getStudentHashMap(town, standardSchool);
                staffHashMap = SchoolAssignmentService.getStaffHashMap(town, standardSchool);
            }

            // Report on school status
            if (standardSchool.isOvercrowded()) {
                publish("NOTE: School is overcrowded at " +
                        String.format("%.1f%%", standardSchool.getOvercrowdingLevel() * 100) +
                        " of optimal capacity");
            }

            publish("Done creating school and population");

            // Update the school info panel
            view.updateSchoolInfo(
                    standardSchool.getSchoolName(),
                    standardSchool.getSchoolFoundedYear(),
                    standardSchool.getSchoolMascot(),
                    colors[0],
                    colors[1]);
            updateTimeLabel();
            updateWeatherLabels();

            // Add names to rooms
            gyms = standardSchool.getGyms();
            for (Gym gym : gyms) {
                RoomNameGenerator.generateRoomName(gym, standardSchool);
            }
            athleticFields = standardSchool.getAthleticFields();
            for (AthleticField athleticField : athleticFields) {
                RoomNameGenerator.generateRoomName(athleticField, standardSchool);
            }
            libraries = standardSchool.getLibraries();
            for (LibraryR library : libraries) {
                RoomNameGenerator.generateRoomName(library, standardSchool);
            }
            auditoriums = standardSchool.getAuditoriums();
            for (Auditorium auditorium : auditoriums) {
                RoomNameGenerator.generateRoomName(auditorium, standardSchool);
            }

            publish("Initializing social links...");
            socialLinkConnector = new SocialLinkConnector(studentHashMap, standardSchool);

            new TraversalStorage(studentHashMap, view, roomConnector);

            // Log population summary
            publish(SchoolAssignmentService.getPopulationSummary(town, standardSchool));
        }

        /**
         * Legacy generation flow (pre-Town architecture).
         * Kept for backward compatibility.
         */
        private void generateLegacy() {
            // Create hash maps for storage
            studentHashMap = new HashMap<Integer, Student>();
            staffHashMap = new HashMap<Integer, Staff>();
            int student_cap;
            int staff_cap;
            String[] colors;
            Classroom[] classrooms;
            Gym[] gyms;
            AthleticField[] athleticFields;
            LibraryR[] libraries;
            Auditorium[] auditoriums;

            // Generate a new standard school with rooms
            publish("Generating the school...");
            standardSchool = new StandardSchool();
            new Director(standardSchool, view);
            student_cap = standardSchool.getOptimalCapacity(); // Use optimal instead of deprecated method
            staff_cap = standardSchool.getMinimumStaffRequirements();
            publish("Connecting rooms...");
            roomConnector = new RoomConnector(standardSchool, view);
            publish("Populating school...");
            // Set school colors for braces band color selection before generating students
            StudentPopGenerator.setSchoolColors(standardSchool.getSchoolColors());
            SiblingGenerator.setSchoolColors(standardSchool.getSchoolColors());
            // Set for student population generation
            StudentPopGenerator.generateStudents(student_cap, studentHashMap, view);
            SiblingGenerator.siblingGenerator(studentHashMap, student_cap, view);
            standardSchool.setStudentGradeClass(studentHashMap, view);
            // Set for staff population generation
            TeacherPopGenerator.generateTeachers(staff_cap, staffHashMap, view);
            publish("Assigning initial staff...");
            StaffAssignmentService.assignInitialStaffRoles(staffHashMap, student_cap, view, standardSchool);
            RoomAssignment.initialClassroomAssignments(standardSchool, staffHashMap);
            publish("Done creating school and students");
            colors = standardSchool.getSchoolColors();
            // Update the school info panel
            view.updateSchoolInfo(
                    standardSchool.getSchoolName(),
                    standardSchool.getSchoolFoundedYear(),
                    standardSchool.getSchoolMascot(),
                    colors[0],
                    colors[1]);
            updateTimeLabel();
            updateWeatherLabels();
            classrooms = standardSchool.getClassrooms();
            for (Classroom classroom : classrooms) {
                classroom.reassignClassroomByTeacher(staffHashMap, view);
            }
            // Note: Class scheduling is handled by
            // EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced() below
            try {
                EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(studentHashMap, staffHashMap,
                        standardSchool, view);
                StudentSeatingAssigner.seatInitialStudents(standardSchool);
            } catch (Exception e) {
                e.printStackTrace();
                GameLogger.logDebug("some exception");
            }
            // Add names to rooms
            gyms = standardSchool.getGyms();
            for (Gym gym : gyms) {
                RoomNameGenerator.generateRoomName(gym, standardSchool);
            }
            athleticFields = standardSchool.getAthleticFields();
            for (AthleticField athleticField : athleticFields) {
                RoomNameGenerator.generateRoomName(athleticField, standardSchool);
            }
            libraries = standardSchool.getLibraries();
            for (LibraryR library : libraries) {
                RoomNameGenerator.generateRoomName(library, standardSchool);
            }
            auditoriums = standardSchool.getAuditoriums();
            for (Auditorium auditorium : auditoriums) {
                RoomNameGenerator.generateRoomName(auditorium, standardSchool);
            }
            publish("Initializing social links...");
            socialLinkConnector = new SocialLinkConnector(studentHashMap, standardSchool);

            new TraversalStorage(studentHashMap, view, roomConnector);
        }

        @Override
        protected void process(java.util.List<String> chunks) {
            for (String message : chunks) {
                GameLogger.logGeneration(message);
            }
        }

        @Override
        protected void done() {
            view.displayMessage("School generated successfully!");
            view.setVisualizeButtonEnabled(true);
            view.setInspectionMenuEnabled(true);
            view.setSocialGraphButtonEnabled(true);

            // Initialize simulation engine
            initializeSimulation();

            // Show simulation controls
            view.showSimulationControls();
            updatePeriodDisplay();

            // Character creation is now shown BEFORE generation in GenerateButtonListener
        }

    }
}
