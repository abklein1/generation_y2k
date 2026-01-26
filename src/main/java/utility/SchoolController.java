package utility;

import behavior.StudentBehaviorTreeBuilder;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
            String[] speedNames = {"Slow (1x)", "Normal (2x)", "Fast (4x)", "Very Fast (8x)"};
            view.appendOutput("Simulation speed set to: " + speedNames[speedIndex]);
        }
    }
    
    /**
     * Initializes the simulation engine after world generation.
     */
    private void initializeSimulation() {
        // Create simulation engine
        simulationEngine = new SimulationEngine(time, standardSchool, studentHashMap, staffHashMap);
        
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
        view.appendOutput("Behavior trees assigned to " + studentHashMap.size() + " students");
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
     * @return "Before School", "Transition", "After School", or null if in a regular period
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
        view.updateWeatherIcons(rootPath + weatherArray[0].getIconName(), rootPath + weatherArray[1].getIconName(), weatherArray[0].toString(), weatherArray[1].toString());
        view.updateWeatherTemps(weather.getTemp("TMAX"), weather.getTemp("TMIN"));
        view.updateDayLabel(time.getDayName());
    }

    private void showInspectionWindow(String type) {
        JFrame inspectionFrame = new JFrame(type + " Inspection");
        inspectionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inspectionFrame.setSize(550, 500);

        JTextArea inspectionArea = new JTextArea();
        inspectionArea.setEditable(false);
        inspectionArea.setLineWrap(true);
        inspectionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inspectionArea);

        if (type.equals("Staff")) {
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
                
                // Track the currently selected student for the button action
                final Student[] currentlySelectedStudent = {null};
                
                studentListComponent.addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Student selectedStudent = studentListComponent.getSelectedValue();
                        if (selectedStudent != null) {
                            currentlySelectedStudent[0] = selectedStudent;
                            studentInspection(selectedStudent, inspectionArea);
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
                inspectionFrame.add(scrollPane, BorderLayout.CENTER);
                inspectionFrame.add(buttonPanel, BorderLayout.SOUTH);
            }
        }


        inspectionFrame.setVisible(true);
    }

    /**
     * Shows the character creation menu.
     *
     * @return true if character was created, false if cancelled
     */
    private boolean showCharacterCreationMenu() {
        final boolean[] characterCreated = {false};
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
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        JTextField firstNameField = new JTextField();
        firstNameField.setColumns(12);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(firstNameField, gbc);
        row++;

        // Last Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Last Name:"), gbc);
        JTextField lastNameField = new JTextField();
        lastNameField.setColumns(12);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(lastNameField, gbc);
        row++;

        // Suffix
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Suffix:"), gbc);
        JComboBox<String> suffixDropdown = new JComboBox<>(new String[]{"Jr.", "Sr.", "III", "II", "IV", "V", "None"});
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(suffixDropdown, gbc);
        row++;

        // Gender
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Gender:"), gbc);
        JComboBox<String> genderDropdown = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(genderDropdown, gbc);
        row++;

        // Race (checkboxes - multiple selections map to multiracial)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
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
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(racePanel, gbc);
        row++;
        
        // Helper to get race code from checkboxes
        java.util.function.Supplier<String> getRaceCode = () -> {
            int count = 0;
            String singleRace = null;
            if (whiteCheckbox.isSelected()) { count++; singleRace = "white"; }
            if (blackCheckbox.isSelected()) { count++; singleRace = "black"; }
            if (asianCheckbox.isSelected()) { count++; singleRace = "api"; }
            if (latinoCheckbox.isSelected()) { count++; singleRace = "hispanic"; }
            
            if (count == 0) return null; // No race selected
            if (count > 1) return "2prace"; // Multiracial
            return singleRace; // Single race
        };

        // Add an action listener to enable/disable the suffix dropdown based on gender selection
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
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Eye Color:"), gbc);
        JComboBox<String> eyeColorDropdown = new JComboBox<>(TraitLoader.getOptionsFromJson("/Resources.People/eye_color.json"));
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(eyeColorDropdown, gbc);
        row++;

        // Hair Color
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Hair Color:"), gbc);
        JComboBox<String> hairColorDropdown = new JComboBox<>(TraitLoader.getOptionsFromJson("/Resources.People/hair_color.json"));
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(hairColorDropdown, gbc);
        row++;

        // Hair Length
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Hair Length:"), gbc);
        JComboBox<String> hairLengthDropdown = new JComboBox<>(new String[]{"Short", "Medium", "Long"});
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(hairLengthDropdown, gbc);
        row++;

        // Hair Type
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Hair Type:"), gbc);
        JComboBox<String> hairTypeDropdown = new JComboBox<>(TraitLoader.getOptionsFromJson("/Resources.People/hair_type.json"));
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(hairTypeDropdown, gbc);
        row++;

        // Birthdate
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
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

        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(datePicker, gbc);
        row++;

        // Family Income
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Family Income:"), gbc);
        JComboBox<String> incomeDropdown = new JComboBox<>(new String[]{"Low", "Middle", "High"});
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(incomeDropdown, gbc);
        row++;

        // Number of Siblings
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Number of Siblings:"), gbc);
        JComboBox<Integer> siblingsDropdown = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5});
        gbc.gridx = 1; gbc.weightx = 1.0;
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
            if (suffixDropdown.getSelectedItem().toString() != "None" || suffixDropdown.getSelectedItem().toString() != null) {
                playerCharacter.studentName.setSuffix(suffixDropdown.getSelectedItem().toString());
            } else {
                playerCharacter.studentName.setSuffix("");
            }
            playerCharacter.studentStatistics.setGender(genderDropdown.getSelectedItem().toString());
            playerCharacter.studentStatistics.setEyeColor(eyeColorDropdown.getSelectedItem().toString());
            if (hairColorDropdown.getSelectedItem().toString() != "None" || hairColorDropdown.getSelectedItem().toString() != null) {
                playerCharacter.studentStatistics.setHairColor(hairColorDropdown.getSelectedItem().toString());
            } else {
                playerCharacter.studentStatistics.setHairColor("");
            }
            if (hairLengthDropdown.getSelectedItem().toString() != "None" || hairLengthDropdown.getSelectedItem().toString() != null) {
                playerCharacter.studentStatistics.setHairLength(hairLengthDropdown.getSelectedItem().toString());
            } else {
                playerCharacter.studentStatistics.setHairLength("");
            }
            if (hairTypeDropdown.getSelectedItem().toString() != "None" || hairTypeDropdown.getSelectedItem().toString() != null) {
                playerCharacter.studentStatistics.setHairType(hairTypeDropdown.getSelectedItem().toString());
            } else {
                playerCharacter.studentStatistics.setHairType("");
            }
            playerCharacter.studentStatistics.setGradeLevel(0);
            playerCharacter.studentStatistics.setInitHeight();
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            if (selectedDate != null) {
                java.time.LocalDate localDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                playerCharacter.studentStatistics.setBirthday(localDate);
            }
            // Align income casing with NPC generation (low|middle|high)
            playerCharacter.studentStatistics.setIncomeLevel(incomeDropdown.getSelectedItem().toString().toLowerCase());
            playerCharacter.setSiblings((Integer) siblingsDropdown.getSelectedItem());

            // Initialize fields similar to NPC generation
            playerCharacter.studentStatistics.setLevel(1);
            playerCharacter.studentStatistics.setExperience(0);

            // Compute skin color from race + eye color
            String eyesForSkin = playerCharacter.studentStatistics.getEyeColor();
            if (eyesForSkin != null) {
                playerCharacter.studentStatistics.setSkinColor(TraitSelection.studentSkinColorSelection(raceCode, eyesForSkin));
            }

            // Append simple story points to the output window
            storyOutput.append("Generating base stats...\n");
            playerCharacter.studentStatistics.setInitStrength();
            playerCharacter.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(STUDENT_POP_INTELLIGENCE_MEAN, STUDENT_POP_INTELLIGENCE_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setCharisma((int) GameRandom.nextGaussian(STUDENT_POP_CHARISMA_MEAN, STUDENT_POP_CHARISMA_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setAgility((int) GameRandom.nextGaussian(STUDENT_POP_AGILITY_MEAN, STUDENT_POP_AGILITY_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setDetermination((int) GameRandom.nextGaussian(STUDENT_POP_DETERMINATION_MEAN, STUDENT_POP_DETERMINATION_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setPerception((int) GameRandom.nextGaussian(STUDENT_POP_PERCEPTION_MEAN, STUDENT_POP_PERCEPTION_STANDARD_DEVIATION));
            playerCharacter.studentStatistics.setLuck((int) GameRandom.nextGaussian(STUDENT_POP_LUCK_MEAN, STUDENT_POP_LUCK_STANDARD_DEVIATION));
            // Derived attributes mirroring NPC generation
            playerCharacter.studentStatistics.setInitCreativity();
            playerCharacter.studentStatistics.setInitEmpathy();
            playerCharacter.studentStatistics.setInitAdaptability();
            playerCharacter.studentStatistics.setInitInitiative();
            playerCharacter.studentStatistics.setInitResilience();
            playerCharacter.studentStatistics.setInitCuriosity();
            playerCharacter.studentStatistics.setInitResponsibility();
            playerCharacter.studentStatistics.setInitOpenMind();

            // Apply braces attributes (timing, cosmetics, charisma effects)
            SiblingGenerator.applyBracesAttributes(playerCharacter);

            PlayerStoryGenerator.reportBaseStats(playerCharacter, storyOutput);

            // Generate and attach family info (parents and siblings) BEFORE story generation
            FamilyInfo family = new FamilyInfo();
            // Parents: use staff name generation approach (adult birth years)
            java.time.LocalDate fatherBirth = BirthdayGenerator.generateRandomBirthdayStaff();
            String fatherFirst;
            String playerSuffixVal = playerCharacter.studentName.getSuffix();
            if (playerSuffixVal != null && (
                    playerSuffixVal.equals("Jr.") ||
                    playerSuffixVal.equals("II") ||
                    playerSuffixVal.equals("III") ||
                    playerSuffixVal.equals("IV") ||
                    playerSuffixVal.equals("V")
            )) {
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
            java.util.List<SiblingInfo> sibInfos = SiblingGenerator.generateSiblingInfosForPlayer(playerCharacter, sibCount, view);
            for (SiblingInfo info : sibInfos) {
                family.addSibling(info);
            }
            playerCharacter.setFamilyInfo(family);

            storyOutput.append("Generating your story...\n");
            PlayerStoryGenerator.generateStory(playerCharacter, storyOutput);
            
            // Display flavor text after story generation
            FlavorTextLoader.appendToTextArea(storyOutput);
            
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
            PlayerCharacter temp = new PlayerCharacter();
            lastName = temp.studentName.capitalizeName(lastName);
            if (setRandom(0, STUDENT_HYPHEN_GENERATION_SAMPLE_SIZE) < STUDENT_HYPHEN_GENERATION_RATE) {
                String hyphenName = NameLoader.selectWeightedRandom()[0];
                hyphenName = temp.studentName.capitalizeName(hyphenName);
                lastName = lastName + "-" + hyphenName;
            }

            // Suffix (male-biased)
            String suffixValue = "None";
            if (gender != null && gender.equalsIgnoreCase("Male") && setRandom(0, SUFFIX_GENERATION_SAMPLE_SIZE) < SUFFIX_GENERATION_RATE) {
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
                    do { second = setRandom(0, 3); } while (second == first);
                    JCheckBox[] boxes = {whiteCheckbox, blackCheckbox, asianCheckbox, latinoCheckbox};
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

            // Income distribution: Low (25%), Middle (60%), High (15%)
            int incomeRoll = setRandom(0, 100);
            String incomeUi = (incomeRoll <= 25) ? "Low" : (incomeRoll <= 85) ? "Middle" : "High";

            // Siblings: 0-5
            int siblings = setRandom(0, 5);

            // Populate UI controls
            firstNameField.setText(firstName);
            lastNameField.setText(lastName);
            String genderUi = gender == null || gender.isBlank() ? "Other" : (Character.toUpperCase(gender.charAt(0)) + gender.substring(1).toLowerCase());
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
            public void insertUpdate(DocumentEvent e) { validateForm(); }

            @Override
            public void removeUpdate(DocumentEvent e) { validateForm(); }

            @Override
            public void changedUpdate(DocumentEvent e) { validateForm(); }
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
                
                // Reset time to starting values
                time.reset();
                
                //Create hash maps for storage
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
                //String[] colorsHex;

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

                //Generate a new standard school with rooms
                publish("Generating the school...");
                standardSchool = new StandardSchool();
                Director director = new Director(standardSchool, view);
                student_cap = standardSchool.getTotalStudentCapacity();
                staff_cap = standardSchool.getMinimumStaffRequirements();
                publish("Connecting rooms...");
                roomConnector = new RoomConnector(standardSchool, view);
                publish("Populating school...");
                // Set for student population generation
                StudentPopGenerator.generateStudents(student_cap, studentHashMap, view);
                SiblingGenerator.siblingGenerator(studentHashMap, student_cap, view);
                standardSchool.setStudentGradeClass(studentHashMap, view);
                // Set for staff population generation
                TeacherPopGenerator.generateTeachers(staff_cap, staffHashMap, view);
                publish("Assigning initial staff...");
                StaffAssignment.initialAssignments(staffHashMap, student_cap, view, standardSchool);
                RoomAssignment.initialClassroomAssignments(standardSchool, staffHashMap);
                publish("Done creating school and students");
                colors = standardSchool.getSchoolColors();
                // Update the school info panel
                view.updateSchoolInfo(
                    standardSchool.getSchoolName(),
                    standardSchool.getSchoolFoundedYear(),
                    standardSchool.getSchoolMascot(),
                    colors[0],
                    colors[1]
                );
                updateTimeLabel();
                updateWeatherLabels();
                classrooms = standardSchool.getClassrooms();
                for (Classroom classroom : classrooms) {
                    classroom.reassignClassroomByTeacher(staffHashMap, view);
                }
                StaffAssignment.assignClassesToStaff(staffHashMap, standardSchool, view);
                try {
                    EnhancedStudentScheduleAssigner.scheduleAllStudentsEnhanced(studentHashMap, staffHashMap, standardSchool, view);
                    StudentSeatingAssigner.seatInitialStudents(standardSchool);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("some exception");
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

                TraversalStorage traversalStorage = new TraversalStorage(studentHashMap, view, roomConnector);

            } catch (Throwable t) {
                t.printStackTrace();
                publish("Caught an exception: " + t.getMessage());
            }
            return null;
        }

        @Override
        protected void process(java.util.List<String> chunks) {
            for (String message : chunks) {
                view.appendOutput(message);
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
