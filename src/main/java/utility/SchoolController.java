package utility;

import entity.*;
import view.GameView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class SchoolController {
    private final GameView view;
    private RoomConnector roomConnector;
    private StandardSchool standardSchool;

    public SchoolController(GameView view) {
        this.view = view;
        this.view.addGenerateButtonListener(new GenerateButtonListener());
        this.view.addVisualizeButtonListener(new VisualizeButtonListener());
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

    private class SchoolGenerationWorker extends SwingWorker<Void, String> {

        @Override
        protected Void doInBackground(){
            //Create hash maps for storage
            HashMap<Integer, Student> studentHashMap = new HashMap<Integer, Student>();
            HashMap<Integer, Staff> staffHashMap = new HashMap<Integer, Staff>();
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
            //This is the first Monday of school
            publish("Alright time to get on with the time then...");
            Time time = new Time();
            publish("Today is " + time.getFormattedDate());

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
        }


    }
}
