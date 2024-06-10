package utility;

import entity.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentScheduleAssigner {

    private static final HashMap<String, ClassDetail> classDetailsMap = ClassDetailsLoader.loadClassDetails("src/main/java/Resources/available_classes.json");

    //TODO: Not an amazing entry point but we will use it for now
    public static void scheduleAllStudents(HashMap<Integer, Student> studentHashMap, HashMap<Integer, Staff> staffHashMap) {
        System.out.println("In the scheduler");
        for (Map.Entry<Integer, Student> student : studentHashMap.entrySet()) {
            scheduleStudent(student.getValue(), staffHashMap);
        }
    }

    public static void scheduleStudent(Student student, HashMap<Integer, Staff> staffHashMap) {
        String year = student.studentStatistics.getGradeLevel();
        List<String> requiredClasses = loadGradeRequirements(year);

        for (String className:  requiredClasses) {
            if (classDetailsMap.containsKey(className)) {
                ClassDetail classDetail = classDetailsMap.get(className);
                assignClassToStudent(student, className, staffHashMap, classDetail);
            } else {
                System.out.println("Class details for " + className + " not found.");
            }
        }

    }

    private static List<String> loadGradeRequirements(String year) {
        Object object;
        List<String> availableClasses = new ArrayList<>();

        try {
            object = new JSONParser().parse(new FileReader("src/main/java/Resources/grad_requirements.json"));
            JSONObject jsonObject = (JSONObject) object;
            JSONObject gradeLevel = (JSONObject) jsonObject.get(year);

            if (gradeLevel != null) {
                for (Object key : gradeLevel.keySet()) {
                    String subject = (String) key;
                    Object classes = gradeLevel.get(subject);

                    if (classes instanceof JSONArray) {
                        // Add each class to the list
                        for (Object className : (JSONArray) classes) {
                            availableClasses.add((String) className);
                        }
                    } else if (classes instanceof Long) {
                        // Handle the "Elective" case
                        availableClasses.add(subject + ": " + classes + " elective(s)");
                    }
                }
            } else {
                throw new RuntimeException("Grade level " + year + " not found in the JSON document.");
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        return availableClasses;
    }

    private static void assignClassToStudent(Student student, String className, HashMap<Integer, Staff > staffHashMap, ClassDetail classDetail) {
        List<Staff> availableTeachers = getAvailableTeachersForClass(className, staffHashMap);

        for (Staff teacher : availableTeachers) {
            TeacherBlock availableBlock = teacher.teacherStatistics.getTeacherSchedule().getBlockByClassNameAndAvailability(className);
            if (availableBlock != null) {
                // Create a new StudentBlock and assign the class
                StudentBlock studentBlock = new StudentBlock();
                studentBlock.setBlockNumber(availableBlock.getBlockNumber());
                studentBlock.setClassName(className);
                studentBlock.setTeacher(teacher);
                studentBlock.setSemester(availableBlock.getSemester());
                studentBlock.setRoom(availableBlock.getRoom());

                // Assign the student block to the student's schedule
                student.studentStatistics.getStudentSchedule().add(studentBlock);

                // Decrease the room capacity
                availableBlock.getRoom().setRoomCapacity(availableBlock.getRoom().getStudentCapacity() - 1);

                System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " " + student.studentName.getLastName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in room " + availableBlock.getRoom().getStudentCapacity());
                return;
            }
        }

        // If no available teacher has room capacity left, assign to the first teacher regardless of capacity
        for (Staff teacher : availableTeachers) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                StudentBlock studentBlock = new StudentBlock();
                studentBlock.setBlockNumber(block.getBlockNumber());
                studentBlock.setClassName(className);
                studentBlock.setTeacher(teacher);
                studentBlock.setSemester(block.getSemester());
                studentBlock.setRoom(block.getRoom());

                student.studentStatistics.getStudentSchedule().add(studentBlock);

                // Even though the room is overassigned, we add the student
                System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in overcapacity room " + block.getRoom().getStudentCapacity());
                return;
            }
        }
    }

    private static List<Staff> getAvailableTeachersForClass(String className, HashMap<Integer, Staff> staffHashMap) {
        List<Staff> availableTeachers = new ArrayList<>();

        for(Staff teacher : staffHashMap.values()) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                availableTeachers.add(teacher);
                break;
            }
        }
        return availableTeachers;
    }



}
