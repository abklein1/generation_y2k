package utility;

import entity.*;
import view.GameView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import static utility.Inspector.*;

public class SchoolController {
    private final GameView view;
    private RoomConnector roomConnector;
    private StandardSchool standardSchool;
    private final Time time;
    HashMap<Integer, Staff> staffHashMap;
    private WeatherPatterns[] weatherArray;

    public SchoolController(GameView view) {
        this.view = view;
        this.view.addGenerateButtonListener(new GenerateButtonListener());
        this.view.addVisualizeButtonListener(new VisualizeButtonListener());
        this.view.addInspectionMenuListener(new InspectionMenuListener());
        this.time = new Time();
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

    private class SchoolGenerationWorker extends SwingWorker<Void, String> {

        @Override
        protected Void doInBackground(){
            //Create hash maps for storage
            HashMap<Integer, Student> studentHashMap = new HashMap<Integer, Student>();
            staffHashMap = new HashMap<Integer, Staff>();
            int student_cap;
            int staff_cap;
            String[] colors;

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
            standardSchool.setStudentGradeClass(studentHashMap, view);
            //Set for staff population generation
            TeacherPopGenerator.generateTeachers(staff_cap, staffHashMap, view);
            publish("Assigning initial staff...");
            StaffAssignment.initialAssignmentsCore(staffHashMap, student_cap, view);
            StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getArtStudios().length, StaffType.VISUAL_ARTS, view);
            StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getAthleticFields().length + standardSchool.getGyms().length, StaffType.PHYSICAL_ED, view);
            StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getMusicRooms().length + standardSchool.getDramaRooms().length + standardSchool.getAuditoriums().length, StaffType.PERFORMING_ARTS, view);
            StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getVocationalRooms().length, StaffType.VOCATIONAL, view);
            StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getComputerLabs().length, StaffType.COMP_SCI, view);
            StaffAssignment.assignFrontOfficePersonnel(staffHashMap, view);
            StaffAssignment.assignUtilityPersonnel(staffHashMap, view);
            StaffAssignment.assignLibraryPersonnel(staffHashMap, view);
            StaffAssignment.assignNurse(staffHashMap, view);
            StaffAssignment.assignLunch(staffHashMap, view);
            StaffAssignment.assignBusiness(staffHashMap, view);
            StaffAssignment.assignSubs(staffHashMap, view);
            RoomAssignment.initialClassroomAssignments(standardSchool, staffHashMap);
            publish("Done creating school and students");
            publish("+++++++++++++++++++++++++++++++++++++++++");
            publish("Welcome to " + standardSchool.getSchoolName());
            publish("Home of the " + standardSchool.getSchoolMascot() + "!");
            colors = standardSchool.getSchoolColors();
            publish("The school colors are " + colors[0] + " and " + colors[1]);
            updateTimeLabel();
            updateWeatherLabels();
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
        }

    }

    private void updateTimeLabel() {
        String formattedDate = time.getFormattedDate();
        view.updateTime("Today is " + formattedDate);
    }

    private void updateWeatherLabels() {
        String rootPath = "/Resources/Weather/Icons/";
        Weather weather = new Weather(standardSchool.getSchoolName());
        weatherArray = weather.determineWeatherAMPM(time.getCurrentDate());
        view.updateWeatherIcons(rootPath + weatherArray[0].getIconName(), rootPath + weatherArray[1].getIconName());
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
            DefaultListModel<Staff> listModel = new DefaultListModel<>();
            for (Staff staff : staffHashMap.values()) {
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
                DefaultListModel<Student> listModel = new DefaultListModel<>();
                for (Student student : studentGradeClass.values()) {
                    listModel.addElement(student);
                }

                JList<Student> studentList = new JList<>(listModel);
                studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                studentList.addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Student selectedStudent = studentList.getSelectedValue();
                        if (selectedStudent != null) {
                            studentInspection(selectedStudent, inspectionArea);
                        }
                    }
                });

                inspectionFrame.setLayout(new BorderLayout());
                inspectionFrame.add(new JScrollPane(studentList), BorderLayout.WEST);
                inspectionFrame.add(scrollPane, BorderLayout.CENTER);
            }
        }


        inspectionFrame.setVisible(true);
    }
}
