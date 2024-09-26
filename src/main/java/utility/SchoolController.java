package utility;

import entity.*;
import entity.Rooms.*;
import view.GameView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import static utility.Inspector.*;

public class SchoolController {
    private final GameView view;
    private RoomConnector roomConnector;
    private SocialLinkConnector socialLinkConnector;
    private StandardSchool standardSchool;
    private final Time time;
    HashMap<Integer, Staff> staffHashMap;

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
            try {
                //Create hash maps for storage
                HashMap<Integer, Student> studentHashMap = new HashMap<Integer, Student>();
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
                //Set for staff population generation
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
                for(Classroom classroom : classrooms) {
                    classroom.reassignClassroomByTeacher(staffHashMap, view);
                }
                StaffAssignment.assignClassesToStaff(staffHashMap, standardSchool, view);
                try {
                    StudentScheduleAssigner.scheduleAllStudents(studentHashMap, staffHashMap);
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
                socialLinkConnector = new SocialLinkConnector(studentHashMap);

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
        }

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
                            if(selectedStudent.studentStatistics.getSiblingsInSchool().isEmpty()) {
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
}
