package utility;

import entity.*;
import view.GameView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import static utility.Randomizer.setRandom;

public class SchoolController {
    private GameView view;

    public SchoolController(GameView view) {
        this.view = view;
        this.view.addGenerateButtonListener(new GenerateButtonListener());
    }

    class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            generateSchool();
        }
    }

    private void generateSchool() {
        //Create hash maps for storage
        HashMap<Integer, Student> studentHashMap = new HashMap<Integer, Student>();
        HashMap<Integer, Staff> staffHashMap = new HashMap<Integer, Staff>();
        int student_cap;
        int staff_cap;
        String[] colors;

        //Generate a new standard school with rooms
        System.out.println("Starting by generating the school");
        StandardSchool standardSchool = new StandardSchool();
        Director director = new Director(standardSchool);
        //Pull capacities
        student_cap = standardSchool.getTotalStudentCapacity();
        staff_cap = standardSchool.getMinimumStaffRequirements();
        // TODO: move this all into an inspector
        System.out.println("Student capacity is " + student_cap);
        System.out.println("Staff capacity is " + staff_cap);

        System.out.println("Connecting rooms");
        RoomConnector roomConnector = new RoomConnector(standardSchool);
        System.out.println("Show connections");
        roomConnector.visualizer();
        System.out.println("Populating school...");
        // Set for student population generation
        StudentPopGenerator.generateStudents(student_cap, studentHashMap);
        standardSchool.setStudentGradeClass(studentHashMap);
        //Set for staff population generation
        TeacherPopGenerator.generateTeachers(staff_cap, staffHashMap);
        System.out.println("Assign initial staff");
        StaffAssignment.initialAssignmentsCore(staffHashMap, student_cap);
        StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getArtStudios().length, StaffType.VISUAL_ARTS);
        StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getAthleticFields().length + standardSchool.getGyms().length, StaffType.PHYSICAL_ED);
        StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getMusicRooms().length + standardSchool.getDramaRooms().length + standardSchool.getAuditoriums().length, StaffType.PERFORMING_ARTS);
        StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getVocationalRooms().length, StaffType.VOCATIONAL);
        StaffAssignment.assignElectiveByRooms(staffHashMap, standardSchool.getComputerLabs().length, StaffType.COMP_SCI);
        StaffAssignment.assignFrontOfficePersonnel(staffHashMap);
        StaffAssignment.assignUtilityPersonnel(staffHashMap);
        StaffAssignment.assignLibraryPersonnel(staffHashMap);
        StaffAssignment.assignNurse(staffHashMap);
        StaffAssignment.assignLunch(staffHashMap);
        StaffAssignment.assignBusiness(staffHashMap);
        StaffAssignment.assignSubs(staffHashMap);
        RoomAssignment.initialClassroomAssignments(standardSchool, staffHashMap);
        System.out.println("Inspect a random classroom");
        Inspector.inspectRoom(standardSchool.getClassrooms()[2]);
        System.out.println("Done creating school and students");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        //Welcome
        System.out.println("Welcome to " + standardSchool.getSchoolName());
        System.out.println("Home of the " + standardSchool.getSchoolMascot() + "!");
        colors = standardSchool.getSchoolColors();
        System.out.println("The school colors are " + colors[0] + " and " + colors[1]);
        //Introduce me to random student
        System.out.println("Introduce me to a random student, please.");
        Inspector.studentInspection(studentHashMap.get(setRandom(0, studentHashMap.size() - 1)));
        //Introduce me to a random teacher
        System.out.println("Ok now introduce me to a random teacher, please.");
        Inspector.staffInspection(staffHashMap.get(setRandom(0, staffHashMap.size() - 1)));
        //This is the first Monday of school
        System.out.println("Alright time to get on with the time then...");
        Time time = new Time();
        System.out.println("Today is " + time.getFormattedDate());
    }
}
