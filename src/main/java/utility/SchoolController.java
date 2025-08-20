package utility;

import entity.Rooms.*;
import entity.*;
import view.GameView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import static utility.Inspector.staffInspection;
import static utility.Inspector.studentInspection;
import org.jdatepicker.impl.UtilDateModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import javax.swing.JFormattedTextField.AbstractFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class SchoolController {
    private final GameView view;
    private final Time time;
    HashMap<Integer, Staff> staffHashMap;
    HashMap<Integer, Student> studentHashMap;
    private RoomConnector roomConnector;
    private SocialLinkConnector socialLinkConnector;
    private StandardSchool standardSchool;


    public SchoolController(GameView view) {
        this.view = view;
        this.view.addGenerateButtonListener(new GenerateButtonListener());
        this.view.addVisualizeButtonListener(new VisualizeButtonListener());
        this.view.addSocialGraphButtonListener(new SocialGraphButtonListener());
        this.view.addInspectionMenuListener(new InspectionMenuListener());
        this.view.addCreateCharacterButtonListener(new CreateCharacterButtonListener());
        this.time = new Time();
    }

    private void updateTimeLabel() {
        String formattedDate = time.getFormattedDate();
        view.updateTime("Today is " + formattedDate);
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
        inspectionFrame.setSize(400, 300);

        JTextArea inspectionArea = new JTextArea();
        inspectionArea.setEditable(false);
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
                studentListComponent.addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Student selectedStudent = studentListComponent.getSelectedValue();
                        if (selectedStudent != null) {
                            if (selectedStudent.studentStatistics.getSiblingsInSchool().isEmpty()) {
                                studentInspection(selectedStudent, inspectionArea);
                            } else {
                                studentInspection(selectedStudent, inspectionArea, socialLinkConnector);
                            }
                        }
                    }
                });

                inspectionFrame.setLayout(new BorderLayout());
                inspectionFrame.add(new JScrollPane(studentListComponent), BorderLayout.WEST);
                inspectionFrame.add(scrollPane, BorderLayout.CENTER);
            }
        }


        inspectionFrame.setVisible(true);
    }

    private void showCharacterCreationMenu() {
        JDialog dialog = new JDialog((Frame) null, "Create Player Character", true);
        dialog.setLayout(new GridLayout(0, 2));

        // First Name
        dialog.add(new JLabel("First Name:"));
        JTextField firstNameField = new JTextField();
        dialog.add(firstNameField);

        // Last Name
        dialog.add(new JLabel("Last Name:"));
        JTextField lastNameField = new JTextField();
        dialog.add(lastNameField);

        // Suffix
        dialog.add(new JLabel("Suffix:"));
        JComboBox<String> suffixDropdown = new JComboBox<>(new String[]{"Jr.", "Sr.", "III", "II", "IV", "V"});
        dialog.add(suffixDropdown);

        // Gender
        dialog.add(new JLabel("Gender:"));
        JComboBox<String> genderDropdown = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        dialog.add(genderDropdown);

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
        dialog.add(new JLabel("Eye Color:"));
        JComboBox<String> eyeColorDropdown = new JComboBox<>(TraitLoader.getOptionsFromJson("/Resources.People/eye_color.json")); // Example colors
        dialog.add(eyeColorDropdown);

        // Hair Color
        dialog.add(new JLabel("Hair Color:"));
        JComboBox<String> hairColorDropdown = new JComboBox<>(TraitLoader.getOptionsFromJson("/Resources.People/hair_color.json")); // Example colors
        dialog.add(hairColorDropdown);

        // Hair Length
        dialog.add(new JLabel("Hair Length:"));
        JComboBox<String> hairLengthDropdown = new JComboBox<>(new String[]{"Short", "Medium", "Long"});
        dialog.add(hairLengthDropdown);

        // Hair Type
        dialog.add(new JLabel("Hair Type:"));
        JComboBox<String> hairTypeDropdown = new JComboBox<>(TraitLoader.getOptionsFromJson("/Resources.People/hair_type.json"));
        dialog.add(hairTypeDropdown);

        // Height
        dialog.add(new JLabel("Height:"));
        JComboBox<String> heightDropdown = new JComboBox<>(new String[]{"Short", "Average", "Tall"}); // Example heights
        dialog.add(heightDropdown);

        // Birthdate
        dialog.add(new JLabel("Birthdate:"));
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

        dialog.add(datePicker);

        // Family Income
        dialog.add(new JLabel("Family Income:"));
        JComboBox<String> incomeDropdown = new JComboBox<>(new String[]{"Low", "Middle", "High"});
        dialog.add(incomeDropdown);

        // Number of Siblings
        dialog.add(new JLabel("Number of Siblings:"));
        JComboBox<Integer> siblingsDropdown = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5});
        dialog.add(siblingsDropdown);

        // OK Button
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            PlayerCharacter playerCharacter = new PlayerCharacter();
            playerCharacter.studentName.setFirstName(firstNameField.getText());
            playerCharacter.studentName.setLastName(lastNameField.getText());
            playerCharacter.studentName.setSuffix(suffixDropdown.getSelectedItem().toString());
            playerCharacter.studentStatistics.setGender(genderDropdown.getSelectedItem().toString());
            playerCharacter.studentStatistics.setEyeColor(eyeColorDropdown.getSelectedItem().toString());
            playerCharacter.studentStatistics.setHairColor(hairColorDropdown.getSelectedItem().toString());
            playerCharacter.studentStatistics.setHairLength(hairLengthDropdown.getSelectedItem().toString());
            playerCharacter.studentStatistics.setHairType(hairTypeDropdown.getSelectedItem().toString());
            playerCharacter.studentStatistics.setGradeLevel(0);
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            if (selectedDate != null) {
                java.time.LocalDate localDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                playerCharacter.studentStatistics.setBirthday(localDate);
            }
            playerCharacter.studentStatistics.setIncomeLevel(incomeDropdown.getSelectedItem().toString());
            playerCharacter.setSiblings((Integer) siblingsDropdown.getSelectedItem());
            dialog.dispose();
        });
        dialog.add(okButton);

        dialog.pack();
        dialog.setVisible(true);
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
                publish("+++++++++++++++++++++++++++++++++++++++++");
                publish("Welcome to " + standardSchool.getSchoolName() + " founded in " + standardSchool.getSchoolFoundedYear() + "!");
                publish("Home of the " + standardSchool.getSchoolMascot() + "!");
                colors = standardSchool.getSchoolColors();
                publish("The school colors are " + colors[0] + " and " + colors[1]);
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
        }

    }
}
