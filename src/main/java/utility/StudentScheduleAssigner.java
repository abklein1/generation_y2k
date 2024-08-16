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
        int intelligence = student.studentStatistics.getIntelligence();
        int determination = student.studentStatistics.getDetermination();
        String income = student.studentStatistics.getIncomeLevel();
        int langChoice = Randomizer.setRandom(0, 4);

        // Determine paths for each subject
        String englishPath = classProbabilityLoader(intelligence, income, determination);
        String mathPath = classProbabilityLoader(intelligence, income, determination);
        String sciencePath = classProbabilityLoader(intelligence, income, determination);
        String historyPath = classProbabilityLoader(intelligence, income, determination);

        List<String> englishClasses = new ArrayList<>();
        List<String> mathClasses = new ArrayList<>();
        List<String> scienceClasses = new ArrayList<>();
        List<String> historyClasses = new ArrayList<>();
        List<String> languageClasses = new ArrayList<>();
        List<String> physEdClasses = new ArrayList<>();
        List<String> vocationalClassesFall = new ArrayList<>();
        List<String> vocationalClassesSpring = new ArrayList<>();

        // Determine English/Math/Science/History/Language/Vocational classes based on path and grade level
        switch (year) {
            case "Freshman" -> {
                mathClasses.add(mathPath.equals("AP") ? "Geometry" : mathPath.equals("Honors") ? "Geometry" : "Fundamentals of Math");
                if (mathPath.equals("AP") || mathPath.equals("Honors")) {
                    mathClasses.add("Algebra I");
                } else {
                    mathClasses.add("Geometry");
                }
                englishClasses.add("English I");
                // No science options freshman
                scienceClasses.add("Biology");
                historyClasses.add(historyPath.equals("AP") ? "AP Human Geography" : "World Geography");
                // Freshman have to take Health
                physEdClasses.add("Health");
            }
            case "Sophomore" -> {
                mathClasses.add(mathPath.equals("AP") ? "Algebra II" : mathPath.equals("Honors") ? "Algebra II" : "Algebra I");
                mathClasses.add(mathPath.equals("AP") ? "Trigonometry" : mathPath.equals("Honors") ? "Trigonometry" : "Algebra II");
                englishClasses.add("English II");
                //Science options
                if (sciencePath.equals("AP") || sciencePath.equals("Honors")) {
                    scienceClasses.add("Chemistry");
                } else {
                    String[] onLevelScienceOptions = {"Earth and Space Science", "Physical Science"};
                    scienceClasses.add(onLevelScienceOptions[Randomizer.setRandom(0, onLevelScienceOptions.length - 1)]);
                }
                historyClasses.add(historyPath.equals("AP") ? "AP World History" : "World History");
                // Sophomores have to take one phys ed class
                physEdClasses = List.of(physicalEdDecision(student));
            }
            case "Junior" -> {
                mathClasses.add(mathPath.equals("AP") ? "Precalculus" : mathPath.equals("Honors") ? "Precalculus" : "Trigonometry");
                if (mathPath.equals("AP")) {
                    mathClasses.add("AP Statistics");
                } else if (!mathPath.equals("Honors")) {
                    mathClasses.add("Math for Data and Financial Literacy");
                }
                englishClasses.add(englishPath.equals("AP") ? "AP English Language & Composition" : "English III");
                if (sciencePath.equals("AP")) {
                    String[] apScienceOptions = {"AP Biology", "AP Chemistry"};
                    scienceClasses.add(apScienceOptions[Randomizer.setRandom(0, apScienceOptions.length - 1)]);
                } else {
                    scienceClasses.add("Anatomy and Physiology");
                }
                historyClasses.add(historyPath.equals("AP") ? "AP US History" : "US History");
            }
            default -> {
                if (mathPath.equals("AP")) {
                    mathClasses.add("AP Calculus AB");
                    mathClasses.add("AP Calculus BC");
                } else if (mathPath.equals("Honors")) {
                    // No required math this year for Honors path
                } else {
                    mathClasses.add("Precalculus");
                }
                englishClasses.add(englishPath.equals("AP") ? "AP English Literature & Composition" : "English IV");
                if (sciencePath.equals("AP")) {
                    scienceClasses.add("AP Physics B");
                    scienceClasses.add("AP Physics C");
                } else if (sciencePath.equals("Honors")) {
                    scienceClasses.add("Physics");
                } else {
                    scienceClasses.add("Environmental Science");
                }
                if (historyPath.equals("AP")) {
                    historyClasses.add("AP US Government");
                    historyClasses.add("AP Economics Macro");
                } else {
                    historyClasses.add("US Government");
                }
            }
        }

        vocationalClassesFall = List.of(vocationalDecision(student, "Fall"));
        vocationalClassesSpring = List.of(vocationalDecision(student, "Spring"));

        // Schedule English classes
        for (String className : englishClasses) {
            if (classDetailsMap.containsKey(className)) {
                assignClassToStudent(student, className, staffHashMap, StaffType.ENGLISH);
            }
        }

        // Language assignment
        if (year.equals("Freshman")) {
            switch (langChoice) {
                case 0 -> {
                    languageClasses.add("Spanish I");
                    languageClasses.add("Spanish II");
                }
                case 1 -> {
                    languageClasses.add("French I");
                    languageClasses.add("French II");
                }
                case 2 -> {
                    languageClasses.add("German I");
                    languageClasses.add("German II");
                }
                case 3 -> {
                    languageClasses.add("American Sign Language I");
                    languageClasses.add("American Sign Language II");
                }
                case 4 -> {
                    languageClasses.add("Latin I");
                    languageClasses.add("Latin II");
                }
            }
        }

        for (String className : languageClasses) {
            if (classDetailsMap.containsKey(className)) {
                assignClassToStudent(student, className, staffHashMap, StaffType.LANGUAGES);
            }
        }

        // Schedule math classes
        scheduleMathClasses(student, mathClasses, staffHashMap);

        // Schedule science classes
        for (String className : scienceClasses) {
            if (classDetailsMap.containsKey(className)) {
                assignClassToStudent(student, className, staffHashMap, StaffType.SCIENCE);
            }
        }

        // Schedule history classes
        for (String className : historyClasses) {
            if (classDetailsMap.containsKey(className)) {
                assignClassToStudent(student, className, staffHashMap, StaffType.HISTORY);
            }
        }

        // Schedule vocational classes
        if (!physEdClasses.isEmpty()) {
            if (student.studentStatistics.getGradeLevel().equals("Sophomore")) {
                int classLength = student.studentStatistics.getStudentSchedule().getClassSchedule().size();
                for (String className : physEdClasses) {
                    assignClassToStudent(student, className, staffHashMap, StaffType.PHYSICAL_ED);
                    if (student.studentStatistics.getStudentSchedule().getClassSchedule().size() > classLength) {
                        break;
                    }
                }
            } else {
                for (String className : physEdClasses) {
                    assignClassToStudent(student, className, staffHashMap, StaffType.PHYSICAL_ED);
                }
            }
        }

        // Schedule additional vocational classes
        if (!student.studentStatistics.getGradeLevel().equals("Freshman")) {
            int classLength = student.studentStatistics.getStudentSchedule().getClassSchedule().size();
            for (String className : vocationalClassesFall) {
                assignClassToStudent(student, className, staffHashMap);
                if (classLength >= 8) {
                    break;
                }
            }
            for(String className : vocationalClassesSpring) {
                if(classLength >= 8) {
                    break;
                } else {
                    assignClassToStudent(student, className, staffHashMap);
                }
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

        for (int i = 0; i < mathClasses.size(); i++) {
            String className = mathClasses.get(i);

            // Ensure the class exists in the class details map
            if (classDetailsMap.containsKey(className)) {
                List<Staff> availableTeachers = getAvailableTeachersForClass(className, staffHashMap, StaffType.MATH);

                // Determine if this class is for fall or spring based on its position in the list
                boolean isFallClass = (i == 0);
                boolean isSpringClass = (i == 1);

                // Assign to the appropriate semester
                boolean classAssigned = false;
                for (Staff teacher : availableTeachers) {
                    List<TeacherBlock> teacherBlocks = teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className);

                    for (TeacherBlock availableBlock : teacherBlocks) {
                        boolean isCorrectSemesterBlock = isFallClass ? isFallBlock(availableBlock) : isSpringBlock(availableBlock);

                        if (availableBlock != null && isCorrectSemesterBlock &&
                                !hasBlockConflict(student, availableBlock.getBlockNumber(), availableBlock.getSemester())) {

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
                            availableBlock.addStudentToBlock(student);

                            System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " " + student.studentName.getLastName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in room " + availableBlock.getRoom().getRoomName());

                            mathClassCount++;
                            classAssigned = true;
                            break; // Move to the next class
                        }
                    }
                    if (classAssigned) break;
                }

                // If no specific semester is assigned and the student can take it in any semester
                if (!isFallClass && !isSpringClass) {
                    for (Staff teacher : availableTeachers) {
                        List<TeacherBlock> teacherBlocks = teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className);

                        for (TeacherBlock availableBlock : teacherBlocks) {
                            if (availableBlock != null &&
                                    !hasBlockConflict(student, availableBlock.getBlockNumber(), availableBlock.getSemester())) {

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
                                availableBlock.addStudentToBlock(student);

                                System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " " + student.studentName.getLastName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in room " + availableBlock.getRoom().getRoomName());

                                mathClassCount++;
                                break; // Move to the next class
                            }
                        }
                        if (mathClassCount > 0) break; // Move to the next class
                    }
                }
            }
        }
    }

    // Helper methods to determine if a block is in the Fall or Spring semester
    private static boolean isFallBlock(TeacherBlock block) {
        return block.getBlockNumber() % 2 != 0;
    }

    private static boolean isSpringBlock(TeacherBlock block) {
        return block.getBlockNumber() % 2 == 0;
    }

    private static void assignClassToStudent(Student student, String className, HashMap<Integer, Staff> staffHashMap, StaffType staffType) {
        List<Staff> availableTeachers = getAvailableTeachersForClass(className, staffHashMap, staffType);

        for (Staff teacher : availableTeachers) {
            List<TeacherBlock> teacherBlocks = teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className);
            int i = 0;

            // Use a while loop to iterate through available blocks
            while (i < teacherBlocks.size()) {
                TeacherBlock availableBlock = teacherBlocks.get(i);
                if (availableBlock != null && !hasBlockConflict(student, availableBlock.getBlockNumber(), availableBlock.getSemester()) && availableBlock.getClassPopulationBlock() < availableBlock.getBlockPopulation()) {
                    // TODO: hardcode but prob change later
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
                    availableBlock.addStudentToBlock(student);
                    System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " " + student.studentName.getLastName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in room " + availableBlock.getRoom().getRoomName());
                    return;
                }
                i++;
            }
        }

        // If no available teacher has room capacity left, assign to the first teacher regardless of capacity
        for (Staff teacher : availableTeachers) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if (!hasBlockConflict(student, block.getBlockNumber(), block.getSemester())) {
                    StudentBlock studentBlock = new StudentBlock();
                    studentBlock.setBlockNumber(block.getBlockNumber());
                    studentBlock.setClassName(className);
                    studentBlock.setTeacher(teacher);
                    studentBlock.setSemester(block.getSemester());
                    studentBlock.setRoom(block.getRoom());

                    // Assign the student block to the student's schedule
                    student.studentStatistics.getStudentSchedule().add(studentBlock);
                    // Decrease the room capacity
                    block.addStudentToBlock(student);

                    // Even though the room is over assigned, we add the student
                    System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " " + student.studentName.getLastName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in overcapacity room " + block.getRoom().getRoomName());
                    return;
                }
            }
        }
    }

    private static void assignClassToStudent(Student student, String className, HashMap<Integer, Staff> staffHashMap) {
        List<Staff> availableTeachers = getAvailableTeachersForClass(className, staffHashMap);

        for (Staff teacher : availableTeachers) {
            List<TeacherBlock> teacherBlocks = teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className);
            int i = 0;

            // Use a while loop to iterate through available blocks
            while (i < teacherBlocks.size()) {
                TeacherBlock availableBlock = teacherBlocks.get(i);
                if (availableBlock != null && !hasBlockConflict(student, availableBlock.getBlockNumber(), availableBlock.getSemester()) && availableBlock.getClassPopulationBlock() < availableBlock.getBlockPopulation()) {
                    // TODO: hardcode but prob change later
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
                    availableBlock.addStudentToBlock(student);
                    System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " " + student.studentName.getLastName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in room " + availableBlock.getRoom().getRoomName());
                    return;
                }
                i++;
            }
        }

        // If no available teacher has room capacity left, assign to the first teacher regardless of capacity
        for (Staff teacher : availableTeachers) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if (!hasBlockConflict(student, block.getBlockNumber(), block.getSemester())) {
                    StudentBlock studentBlock = new StudentBlock();
                    studentBlock.setBlockNumber(block.getBlockNumber());
                    studentBlock.setClassName(className);
                    studentBlock.setTeacher(teacher);
                    studentBlock.setSemester(block.getSemester());
                    studentBlock.setRoom(block.getRoom());

                    // Assign the student block to the student's schedule
                    student.studentStatistics.getStudentSchedule().add(studentBlock);
                    // Decrease the room capacity
                    block.addStudentToBlock(student);

                    // Even though the room is over assigned, we add the student
                    System.out.println("Assigned " + className + " to " + student.studentName.getFirstName() + " " + student.studentName.getLastName() + " with " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " in overcapacity room " + block.getRoom().getRoomName());
                    return;
                }
            }
        }
    }

    // Helper method to check if a block is already assigned in the student's schedule
    private static boolean hasBlockConflict(Student student, int blockNumber, String semester) {
        //System.out.println("Checking blocks for student " + student.studentName.getFirstName() + " " + student.studentName.getLastName());
        for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
            if (block.getBlockNumber() == blockNumber && block.getSemester().equals(semester)) {
                //System.out.println("Semester student " + block.getSemester() + " and teacher semester " + semester + " are equal. Student block " + block.getBlockNumber() + " and teacher block " + blockNumber + " are equal.");
                return true;
            }
            //System.out.println("Semester student " + block.getSemester() + " and teacher semester " + semester + " are NOT equal. Student block " + block.getBlockNumber() + " and teacher block " + blockNumber + " are NOT equal.");
        }
        return false;
    }

    private static List<Staff> getAvailableTeachersForClass(String className, HashMap<Integer, Staff> staffHashMap, StaffType staffType) {
        List<Staff> staffTypeTeachers = StaffAssignment.getTeachersOfType(staffHashMap, staffType);
        List<Staff> availableTeachers = new ArrayList<>();

        for (Staff teacher : staffTypeTeachers) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if (block.getClassName().equals(className)) {
                    availableTeachers.add(teacher);
                    break;
                }
            }
        }
        return availableTeachers;
    }

    private static List<Staff> getAvailableTeachersForClass(String className, HashMap<Integer, Staff> staffHashMap) {
        List<Staff> staffTypeTeachersV = StaffAssignment.getTeachersOfType(staffHashMap, StaffType.VOCATIONAL);
        List<Staff> staffTypeTeachersP = StaffAssignment.getTeachersOfType(staffHashMap, StaffType.PERFORMING_ARTS);
        List<Staff> staffTypeTeachersA = StaffAssignment.getTeachersOfType(staffHashMap, StaffType.VISUAL_ARTS);
        List<Staff> staffTypeTeachersB = StaffAssignment.getTeachersOfType(staffHashMap, StaffType.BUSINESS);
        List<Staff> staffTypeTeachersE = StaffAssignment.getTeachersOfType(staffHashMap, StaffType.PHYSICAL_ED);
        List<Staff> availableTeachers = new ArrayList<>();

        for (Staff teacher : staffTypeTeachersV) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if (block.getClassName().equals(className)) {
                    availableTeachers.add(teacher);
                    break;
                }
            }
        }
        for (Staff teacher : staffTypeTeachersP) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if (block.getClassName().equals(className)) {
                    availableTeachers.add(teacher);
                    break;
                }
            }
        }
        for (Staff teacher : staffTypeTeachersA) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if (block.getClassName().equals(className)) {
                    availableTeachers.add(teacher);
                    break;
                }
            }
        }
        for (Staff teacher : staffTypeTeachersB) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if (block.getClassName().equals(className)) {
                    availableTeachers.add(teacher);
                    break;
                }
            }
        }
        for (Staff teacher : staffTypeTeachersE) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if (block.getClassName().equals(className)) {
                    availableTeachers.add(teacher);
                    break;
                }
            }
        }
        return availableTeachers;
    }

    private static List<Staff> getAvailableTeachersForClass(String className, HashMap<Integer, Staff> staffHashMap, int semester, StaffType staffType) {
        List<Staff> staffTypeTeachers = StaffAssignment.getTeachersOfType(staffHashMap, staffType);
        List<Staff> availableTeachers = new ArrayList<>();

        for (Staff teacher : staffTypeTeachers) {
            for (TeacherBlock block : teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)) {
                if ((block.getSemester().equals("Fall") && semester == 0) || (block.getSemester().equals("Spring") && semester == 1)) {
                    availableTeachers.add(teacher);
                    break;
                }
            }
        }
        return availableTeachers;
    }

    private static String classProbabilityLoader(int intelligence, String income, int determination) {
        int random = Randomizer.setRandom(0, 100);
        double apProbability;
        double honorsProbability;
        double onLevelProbability;

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

        // Adjust probabilities based on determination
        double determinationFactor = (determination - 50) / 50.0; // Scaled so that 50 determination gives no modification
        apProbability += apProbability * determinationFactor;
        honorsProbability += honorsProbability * determinationFactor / 2;
        onLevelProbability -= onLevelProbability * determinationFactor / 2;

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

    private static String[] physicalEdDecision(Student student) {
        String[] choiceRank = new String[5];
        String gender = student.studentStatistics.getGender();
        int strength = student.studentStatistics.getStrength();
        int determination = student.studentStatistics.getDetermination();

        if (gender.equals("Male")) {
            // If a student has high strength or low strength but high determination they choose weightlifting
            if (strength > 60 || (strength < 29 && determination > 60)) {
                choiceRank[0] = "Weightlifting";
                choiceRank[1] = "Team Sports";
                choiceRank[2] = "Specialized Sports";
                choiceRank[3] = "Lifetime Recreation";
                choiceRank[4] = "Dance";
                // Student is just average and wants to do more social classes
            } else if (strength < 60 && strength > 30) {
                choiceRank[0] = "Team Sports";
                choiceRank[1] = "Specialized Sports";
                choiceRank[2] = "Weightlifting";
                choiceRank[3] = "Lifetime Recreation";
                choiceRank[4] = "Dance";
                // Student is lazy and wants easy classes
            } else if (determination < 30) {
                choiceRank[0] = "Lifetime Recreation";
                choiceRank[1] = "Specialized Sports";
                choiceRank[2] = "Team Sports";
                choiceRank[3] = "Dance";
                choiceRank[4] = "Weightlifting";
            } else {
                choiceRank[0] = "Specialized Sports";
                choiceRank[1] = "Team Sports";
                choiceRank[2] = "Weightlifting";
                choiceRank[3] = "Dance";
                choiceRank[4] = "Lifetime Recreation";
            }
        } else {
            if (strength > 60 || (strength < 29 && determination > 60)) {
                choiceRank[0] = "Dance";
                choiceRank[1] = "Team Sports";
                choiceRank[2] = "Specialized Sports";
                choiceRank[3] = "Weightlifting";
                choiceRank[4] = "Lifetime Recreation";
                // Student is just average and wants to do more social classes
            } else if (strength < 60 && strength > 30) {
                choiceRank[0] = "Specialized Sports";
                choiceRank[1] = "Lifetime Recreation";
                choiceRank[2] = "Dance";
                choiceRank[3] = "Weightlifting";
                choiceRank[4] = "Team Sports";
                // Student is lazy and wants easy classes
            } else if (determination < 30) {
                choiceRank[0] = "Lifetime Recreation";
                choiceRank[1] = "Specialized Sports";
                choiceRank[2] = "Dance";
                choiceRank[3] = "Team Sports";
                choiceRank[4] = "Weightlifting";
            } else {
                choiceRank[0] = "Specialized Sports";
                choiceRank[1] = "Team Sports";
                choiceRank[2] = "Weightlifting";
                choiceRank[3] = "Dance";
                choiceRank[4] = "Lifetime Recreation";
            }
        }
        return choiceRank;
    }

    private static String[] vocationalDecision(Student student, String semester) {
        String[] choiceRank = new String[8];
        int determination = student.studentStatistics.getDetermination();
        int charisma = student.studentStatistics.getCharisma();
        int creativity = student.studentStatistics.getCreativity();
        int perception = student.studentStatistics.getPerception();
        int intelligence = student.studentStatistics.getIntelligence();
        int curiosity = student.studentStatistics.getCuriosity();
        String year = student.studentStatistics.getGradeLevel();

        if (semester.equals("Fall")) {
            // If someone has high charisma, better than average determination and understands themselves better than the average person
            if (charisma > 70 && determination > 50 && perception > 50) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Theater I";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Musical Theater I";
                        choiceRank[3] = "Dance Techniques I";
                        choiceRank[4] = "Choir";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Introduction to Business";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Business Management";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Debate";
                        choiceRank[1] = "Choir";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Concert Band";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "ROTC";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "Business Management";
                    }
                }
                // If someone has high creativity and better than average perception
            } else if (creativity > 130 && perception > 50) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "2D Studio Art I";
                        choiceRank[1] = "Photography I";
                        choiceRank[2] = "3D Studio Art I";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "3D Studio Art I";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Photography I";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Theater Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Photography I";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If determination and intelligence are high and perception is better than average
            } else if (determination > 130 && intelligence > 105 && perception > 50) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Computer Aided Drafting I";
                        choiceRank[4] = "Intro to Programming";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "ROTC";
                        choiceRank[7] = "Auto Body Repair";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "AP Music Theory";
                        choiceRank[4] = "AP Philosophy";
                        choiceRank[5] = "Intro to Programming";
                        choiceRank[6] = "ROTC";
                        choiceRank[7] = "Debate";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If curiosity is high and intelligence are above average
            } else if (curiosity > 70 && intelligence > 105 && perception > 50) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting I";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting I";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "AP Music Theory";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Philosophy";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If someone is lacking determination
            } else if (determination < 30) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Home Economics";
                        choiceRank[2] = "Woodworking";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art I";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art I";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            } else {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Team Sports";
                        choiceRank[2] = "Specialized Sports5k ";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art I";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art I";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            }
        } else {
            // If someone has high charisma, better than average determination and understands themselves better than the average person
            if (charisma > 70 && determination > 50 && perception > 50) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Theater II";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Musical Theater II";
                        choiceRank[3] = "Dance Techniques II";
                        choiceRank[4] = "Choir";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Theater IV";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Digital Production Technology";
                        choiceRank[5] = "Entrepreneurial Skills";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Digital Production Technology";
                        choiceRank[5] = "Marketing";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Debate";
                        choiceRank[1] = "Choir";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Concert Band";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "ROTC";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "Marketing";
                    }
                }
                // If someone has high creativity and better than average perception
            } else if (creativity > 130 && perception > 50) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "2D Studio Art II";
                        choiceRank[1] = "Photography II";
                        choiceRank[2] = "3D Studio Art II";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "3D Studio Art II";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Photography II";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Theater Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Photography II";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Studio History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If determination and intelligence are high and perception is better than average
            } else if (determination > 130 && intelligence > 105 && perception > 50) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Computer Aided Drafting II";
                        choiceRank[4] = "Intro to Programming";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "ROTC";
                        choiceRank[7] = "Auto Body Repair";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "AP Music Theory";
                        choiceRank[4] = "AP Philosophy";
                        choiceRank[5] = "Intro to Programming";
                        choiceRank[6] = "ROTC";
                        choiceRank[7] = "Debate";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If curiosity is high and intelligence are above average
            } else if (curiosity > 70 && intelligence > 105 && perception > 50) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting II";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting II";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "AP Music Theory";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Philosophy";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If someone is lacking determination
            } else if (determination < 30) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Home Economics";
                        choiceRank[2] = "Woodworking";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art II";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art II";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            } else {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Team Sports";
                        choiceRank[2] = "Specialized Sports";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art II";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art II";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            }
        }
        return choiceRank;
    }
}
