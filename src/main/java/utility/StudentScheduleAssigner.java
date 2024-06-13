package utility;

import entity.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
        for (Map.Entry<Integer, Student> student : studentHashMap.entrySet()) {
            scheduleStudent(student.getValue(), staffHashMap);
        }
    }

    public static void scheduleStudent(Student student, HashMap<Integer, Staff> staffHashMap) {
        String year = student.studentStatistics.getGradeLevel();
        List<String> requiredClasses = loadGradeRequirements(year);
        int intelligence = student.studentStatistics.getIntelligence();
        String income = student.studentStatistics.getIncomeLevel();
        String pathSelection = classProbabilityLoader(intelligence, income);

        List<String> mathClasses = new ArrayList<>();
        List<String> otherClasses = new ArrayList<>();

        if (pathSelection.equals("AP")) {
            switch (year) {
                case "Freshman" -> {
                    otherClasses.add("English I");
                    mathClasses.add("Geometry");
                    mathClasses.add("Algebra I");
                }
                case "Sophomore" -> {
                    otherClasses.add("English II");
                    mathClasses.add("Algebra II");
                    mathClasses.add("Trigonometry");
                }
                case "Junior" -> {
                    otherClasses.add("AP Language and Composition");
                    mathClasses.add("Precalculus");
                    mathClasses.add("AP Statistics");
                }
                default -> {
                    otherClasses.add("AP Literature and Composition");
                    mathClasses.add("AP Calculus AB");
                    mathClasses.add("AP Calculus BC");
                }
            }
        } else if (pathSelection.equals("Honors")) {
            switch (year) {
                case "Freshman" -> {
                    otherClasses.add("English I");
                    mathClasses.add("Geometry");
                    mathClasses.add("Algebra I");
                }
                case "Sophomore" -> {
                    otherClasses.add("English II");
                    mathClasses.add("Algebra II");
                    mathClasses.add("Trigonometry");
                }
                case "Junior" -> {
                    otherClasses.add("English III");
                    mathClasses.add("Precalculus");
                }
                default -> {
                    otherClasses.add("English IV");
                    System.out.println("No required math this year");
                }
            }
        } else {
            switch (year) {
                case "Freshman" -> {
                    otherClasses.add("English I");
                    mathClasses.add("Fundamentals of Math");
                    mathClasses.add("Geometry");
                }
                case "Sophomore" -> {
                    otherClasses.add("English II");
                    mathClasses.add("Algebra I");
                    mathClasses.add("Algebra II");
                }
                case "Junior" -> {
                    otherClasses.add("English III");
                    mathClasses.add("Trigonometry");
                    mathClasses.add("Math for Data and Financial Literacy");
                }
                default -> {
                    otherClasses.add("English IV");
                    mathClasses.add("Precalculus");
                }
            }
        }

        //schedule math
        scheduleMathClasses(student, mathClasses, staffHashMap);

        for (String className : otherClasses) {
            if (classDetailsMap.containsKey(className)) {
                ClassDetail classDetail = classDetailsMap.get(className);
                assignClassToStudent(student, className, staffHashMap, classDetail);
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

    private static void scheduleMathClasses(Student student, List<String> mathClasses, HashMap<Integer, Staff> staffHashMap) {
        int mathClassCount = 0;

        for (String className : mathClasses) {
            if (mathClassCount >= 2) {
                break; // Only schedule two math classes per year
            }

            if (classDetailsMap.containsKey(className)) {
                ClassDetail classDetail = classDetailsMap.get(className);
                int semester = mathClasses.indexOf(className);
                List<Staff> availableTeachers = getAvailableTeachersForClass(className, staffHashMap, semester);

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
                        availableBlock.getRoom().setStudentCap(availableBlock.getRoom().getStudentCapacity() - 1);

                        System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " " + student.studentName.getLastName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in room " + availableBlock.getRoom().getStudentCapacity());

                        mathClassCount++;
                        if (mathClassCount >= 2) {
                            break; // Stop scheduling after two math classes
                        }
                    }
                }
            }
        }
    }

    private static void assignClassToStudent(Student student, String className, HashMap<Integer, Staff> staffHashMap, ClassDetail classDetail) {
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
                availableBlock.getRoom().setStudentCap(availableBlock.getRoom().getStudentCapacity() - 1);

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

        for (Staff teacher : staffHashMap.values()) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                availableTeachers.add(teacher);
                break;
            }
        }
        return availableTeachers;
    }

    private static List<Staff> getAvailableTeachersForClass(String className, HashMap<Integer, Staff> staffHashMap, int semester) {
        List<Staff> availableTeachers = new ArrayList<>();

        for (Staff teacher : staffHashMap.values()) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if((block.getSemester().equals("Fall") && semester == 0) || (block.getSemester().equals("Spring") && semester == 1)) {
                    availableTeachers.add(teacher);
                    break;
                }
            }
        }
        return availableTeachers;
    }


    private static String classProbabilityLoader(int intelligence, String income) {
        int random = Randomizer.setRandom(0, 100);
        double apProbability = 0.0;
        double honorsProbability = 0.0;
        double onLevelProbability = 0.0;

        // Base probabilities based on intelligence
        if (intelligence >= 135) {
            apProbability = 80.0;
            honorsProbability = 10.0;
            onLevelProbability = 10.0;
        } else if (intelligence >= 120) {
            apProbability = 50.0;
            honorsProbability = 30.0;
            onLevelProbability = 20.0;
        } else if (intelligence >= 100) {
            apProbability = 20.0;
            honorsProbability = 30.0;
            onLevelProbability = 50.0;
        } else {
            apProbability = 5.0;
            honorsProbability = 20.0;
            onLevelProbability = 75.0;
        }

        // Adjust probabilities based on income level
        switch (income) {
            case "high":
                apProbability *= 1.2;
                honorsProbability *= 1.1;
                onLevelProbability *= 0.9;
                break;
            case "middle":
                // No adjustment for middle income
                break;
            case "low":
                apProbability *= 0.7;
                honorsProbability *= 0.9;
                onLevelProbability *= 1.2;
                break;
        }

        // Normalize probabilities to ensure they add up to 100
        double totalProbability = apProbability + honorsProbability + onLevelProbability;
        apProbability = (apProbability / totalProbability) * 100;
        honorsProbability = (honorsProbability / totalProbability) * 100;
        onLevelProbability = (onLevelProbability / totalProbability) * 100;

        // Determine class based on random value
        if (random < apProbability) {
            return "AP";
        } else if (random < apProbability + honorsProbability) {
            return "Honors";
        } else {
            return "On-Level";
        }
    }

}
