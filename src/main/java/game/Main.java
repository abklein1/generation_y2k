package game;
/*
    File: game.Main.java
    Author: Alex Klein
    Date: 04/13/2022
    Description: Here is the driver for the program
 */

//TODO: Optimize Imports

import entity.*;
import utility.*;

import java.util.HashMap;

import static utility.BossUtility.bossDecision;
import static utility.BossUtility.dungeonFight;
import static utility.Randomizer.setRandom;

public class Main {
    //TODO: Create seed ingestion for recreating specific schools or scenarios
    //TODO: Just a main run to see that the sim works. Revise this to make actual game loop
    public static void main(String[] args) {
        //Create hash maps for storage
        HashMap<Integer, Student> studentHashMap = new HashMap<Integer, Student>();
        HashMap<Integer, Staff> staffHashMap = new HashMap<Integer, Staff>();
        int student_cap;
        int staff_cap;
        int dungeon;
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
        System.out.println("Each time might present a new challenge that every student must face");
        Homework first_homework = new Homework();
        //Each student must face the first boss
        for (int m = 0; m < student_cap; m++) {
            dungeonFight(studentHashMap.get(m), first_homework);
        }
        System.out.println("Alright let's see how a random student did...");
        Student day1 = studentHashMap.get(setRandom(0, studentHashMap.size() - 1));
        System.out.println("Hey " + day1.studentName.getFirstName());
        System.out.println("Ok so " + day1.studentName.getFirstName() + " got a " + day1.studentStatistics.getGradeAverage());
        System.out.println("Well tomorrow's a new time. Let's simulate another 10 of them!");
        time.incrementDayCounter();
        //Simulate the whole school for 10 days
        for (int n = 0; n < 9; n++) {
            System.out.println("Today is " + time.getDayName());
            dungeon = bossDecision(time);
            if (dungeon == 1) {
                Homework homework = new Homework();
                for (int o = 0; o < student_cap; o++) {
                    dungeonFight(studentHashMap.get(o), homework);
                }
                time.incrementDayCounter();
            } else if (dungeon == 2) {
                Quiz quiz = new Quiz();
                for (int p = 0; p < student_cap; p++) {
                    dungeonFight(studentHashMap.get(p), quiz);
                }
                time.incrementDayCounter();
            } else if (dungeon == 3) {
                Exam exam = new Exam();
                for (int q = 0; q < student_cap; q++) {
                    dungeonFight(studentHashMap.get(q), exam);
                }
                time.incrementDayCounter();
            } else {
                time.incrementDayCounter();
            }
        }
        System.out.println("Now let's check on a random student...");
        Student lastStudent = studentHashMap.get(setRandom(0, studentHashMap.size() - 1));
        Inspector.studentInspection(lastStudent);
        System.out.println("So " + lastStudent.studentName.getFirstName() + " you have the following grades:");
        lastStudent.studentStatistics.getAllGrades();
        System.out.println(lastStudent.studentName.getFirstName() + "'s average was " + lastStudent.studentStatistics.getGradeAverage());
        System.out.println("Let's check on an entire year of students...");
        Inspector.gradeClassInspection(standardSchool.getStudentGradeClass("Freshman"));
        System.out.println("Find the top student by primary stats...");
        Inspector.findHighestStudent(studentHashMap);
        System.out.println("Find the lowest student by primary stats...");
        Inspector.findLowestStudent(studentHashMap);
    }
}