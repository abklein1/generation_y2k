package utility;

import entity.Rooms.Lunchroom;
import entity.*;
import entity.Rooms.Room;
import view.GameView;

import java.util.*;

public class StaffAssignment {

    //TODO: clean up these functions. Some can be combined
    public static void initialAssignments(HashMap<Integer, Staff> staffHashMap, int studentCap, GameView view, StandardSchool standardSchool) {
        HashMap<Integer, Staff> localStaffMap = new HashMap<>(staffHashMap);
        assignPrincipal(localStaffMap, view);
        assignVicePrincipal(localStaffMap, view);
        assignGuidanceCouncilors(localStaffMap, view);
        assignCoreTeachers(localStaffMap, studentCap, StaffType.ENGLISH, view);
        assignCoreTeachers(localStaffMap, studentCap, StaffType.HISTORY, view);
        assignCoreTeachers(localStaffMap, studentCap, StaffType.MATH, view);
        assignCoreTeachers(localStaffMap, studentCap, StaffType.SCIENCE, view);
        assignLanguageTeachers(localStaffMap, studentCap, view);
        StaffAssignment.assignElectiveByRooms(localStaffMap, standardSchool.getArtStudios().length, StaffType.VISUAL_ARTS, view);
        StaffAssignment.assignElectiveByRooms(localStaffMap, standardSchool.getAthleticFields().length + standardSchool.getGyms().length, StaffType.PHYSICAL_ED, view);
        StaffAssignment.assignElectiveByRooms(localStaffMap, standardSchool.getMusicRooms().length + standardSchool.getDramaRooms().length + standardSchool.getAuditoriums().length, StaffType.PERFORMING_ARTS, view);
        StaffAssignment.assignElectiveByRooms(localStaffMap, standardSchool.getVocationalRooms().length, StaffType.VOCATIONAL, view);
        StaffAssignment.assignElectiveByRooms(localStaffMap, standardSchool.getComputerLabs().length, StaffType.COMP_SCI, view);
        assignUtilityPersonnel(localStaffMap, view, standardSchool);
        assignLibraryPersonnel(localStaffMap, view, standardSchool);
        assignFrontOfficePersonnel(localStaffMap, view);
        assignNurse(localStaffMap, view);
        assignLunch(localStaffMap, view, standardSchool);
        assignBusiness(localStaffMap, view);
        assignSubs(localStaffMap, view);
    }

    public static void assignPrincipal(HashMap<Integer, Staff> staffHashMap, GameView view) {
        Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
        if (optionalStaff.isPresent()) {
            Staff teacher = optionalStaff.get();
            teacher.teacherStatistics.setStaffType(StaffType.PRINCIPAL);
            view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as principal!");
            staffHashMap.values().remove(teacher);
        } else {
            view.appendOutput("Failed to assign principal. No available staff.");
        }
    }

    public static void assignStaff(Staff staff, StaffType type) {
        staff.teacherStatistics.setStaffType(type);
    }

    private static void assignVicePrincipal(HashMap<Integer, Staff> staffHashMap, GameView view) {
        Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
        if (optionalStaff.isPresent()) {
            Staff teacher = optionalStaff.get();
            teacher.teacherStatistics.setStaffType(StaffType.VICE_PRINCIPAL);
            view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as vice principal!");
            staffHashMap.values().remove(teacher);
        } else {
            view.appendOutput("Failed to assign vice principal. No available staff.");
        }
    }

    private static void assignGuidanceCouncilors(HashMap<Integer, Staff> staffHashMap, GameView view) {
        int councilMax = 4;

        for (int councilCount = 0; councilCount < councilMax; councilCount++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(StaffType.GUIDANCE);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as guidance councilor!");
            } else {
                view.appendOutput("Failed to assign guidance councilor. No available staff.");
                break;
            }
        }
    }

    private static void assignCoreTeachers(HashMap<Integer, Staff> staffHashMap, int studentCap, StaffType type, GameView view) {
        // Core reqs (I- IV) have to be taught to half the student body per semester. Each teacher can handle 35 students and has 3 periods to teach a subject
        // Plus 4 extra teachers to teach ancillary courses/ AP
        int coreMax = (int) Math.round((((studentCap / 2.0) / 30.0) / 4.00) + 4);

        if(type.equals(StaffType.ENGLISH)) {
            coreMax = (int) Math.round(((studentCap / 30.0) / 4.00) + 4);
        }

        for (int count = 0; count < coreMax; count++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(type);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + type.toString().toLowerCase() + " teacher!");
            } else {
                view.appendOutput("Failed to assign core teacher. No available staff.");
                break;
            }
        }
    }

    private static void assignLanguageTeachers(HashMap<Integer, Staff> staffHashMap, int studentCap, GameView view) {
        // For now hardcode 2 teachers per language unless student cap below 800
        int staffMax;
        if (studentCap < 800) {
            staffMax = 4;
        }
        staffMax = 6;

        for (int count = 0; count < staffMax; count++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(StaffType.LANGUAGES);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.LANGUAGES.toString().toLowerCase() + " teacher!");
            } else {
                view.appendOutput("Failed to assign language teacher. No available staff.");
                break;
            }
        }
    }

    public static void assignElectiveByRooms(HashMap<Integer, Staff> staffHashMap, int roomCount, StaffType type, GameView view) {
        for (int count = 0; count < roomCount; count++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(type);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + type.toString().toLowerCase() + " teacher!");
            } else {
                view.appendOutput("Failed to assign elective teacher. No available staff.");
                break;
            }
        }
    }

    public static void assignFrontOfficePersonnel(HashMap<Integer, Staff> staffHashMap, GameView view) {
        int maxOffice = 2;

        for (int count = 0; count < maxOffice; count++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(StaffType.OFFICE);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as " + StaffType.OFFICE.toString().toLowerCase() + " staff!");
            } else {
                view.appendOutput("Failed to assign front office. No available staff.");
                break;
            }
        }
    }

    public static void assignUtilityPersonnel(HashMap<Integer, Staff> staffHashMap, GameView view, StandardSchool standardSchool) {
        int maxUtility = standardSchool.getUtilityrooms().length + 2;

        for (int count = 0; count < maxUtility; count++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(StaffType.MAINTENANCE);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.MAINTENANCE.toString().toLowerCase());
            } else {
                view.appendOutput("Failed to assign utility personnel. No available staff.");
                break;
            }
        }
    }

    //TODO: fix this since there can be multiple libraries
    public static void assignLibraryPersonnel(HashMap<Integer, Staff> staffHashMap, GameView view, StandardSchool standardSchool) {
        int maxLibrarian = standardSchool.getLibraries().length * 2;

        for (int count = 0; count < maxLibrarian; count++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(StaffType.LIBRARY);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.LIBRARY.toString().toLowerCase());
            } else {
                view.appendOutput("Failed to assign library staff. No available staff.");
                break;
            }
        }
    }

    public static void assignNurse(HashMap<Integer, Staff> staffHashMap, GameView view) {
        int maxNurse = 2;

        for (int count = 0; count < maxNurse; count++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(StaffType.NURSE);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as a " + StaffType.NURSE.toString().toLowerCase());
            } else {
                view.appendOutput("Failed to assign nurse. No available staff.");
                break;
            }
        }
    }

    public static void assignLunch(HashMap<Integer, Staff> staffHashMap, GameView view, StandardSchool standardSchool) {
        Lunchroom[] lunchrooms = standardSchool.getLunchrooms();
        int lunchCount = 0;

        for (Lunchroom lunchroom : lunchrooms) {
            lunchCount = lunchCount + lunchroom.getStaffCapacity();
        }

        for (int count = 0; count < lunchCount; count++) {
            Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
            if (optionalStaff.isPresent()) {
                Staff teacher = optionalStaff.get();
                teacher.teacherStatistics.setStaffType(StaffType.LUNCH);
                view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.LUNCH.toString().toLowerCase());
            } else {
                view.appendOutput("Failed to assign lunch staff. No available staff.");
                break;
            }
        }
    }

    public static void assignBusiness(HashMap<Integer, Staff> staffHashMap, GameView view) {
        Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
        if (optionalStaff.isPresent()) {
            Staff teacher = optionalStaff.get();
            teacher.teacherStatistics.setStaffType(StaffType.BUSINESS);
            view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.BUSINESS.toString().toLowerCase());
        } else {
            view.appendOutput("Failed to assign business teachers. No available staff.");
        }
    }

    // If anyone is left assign them to sub
    public static void assignSubs(HashMap<Integer, Staff> staffHashMap, GameView view) {
        for (Map.Entry<Integer, Staff> staff : staffHashMap.entrySet()) {
            if (staff.getValue().teacherStatistics.getStaffType() == null) {
                staff.getValue().teacherStatistics.setStaffType(StaffType.SUB);
                view.appendOutput("Staff " + staff.getValue().teacherName.getFirstName() + " " + staff.getValue().teacherName.getLastName() + " assigned to " + StaffType.SUB.toString().toLowerCase());
            }
        }
    }

    public static void reassignSubToRoom(HashMap<Integer, Staff> staffHashMap, GameView view, Room room) {
        List<Staff> subs = getTeachersOfType(staffHashMap, StaffType.SUB);
        if (subs.isEmpty()) {
            view.appendOutput("List of subs is empty!");
        }
        int choice = Randomizer.setRandom(0, subs.size() - 1);
        Staff sub = subs.get(choice);

        StaffType type = selectRandomCoreType();

        sub.teacherStatistics.setStaffType(type);

        room.setAssignedStaff(sub);
        view.appendOutput(sub.teacherName.getFirstName() + " " + sub.teacherName.getLastName() + " reassigned to " + sub.teacherStatistics.getStaffType() + " in " + room.getRoomName());
    }

    //TODO: Lazy way of adjusting English but will re-evaluate later. Right now English is over-capacity
    private static StaffType selectRandomCoreType() {
        List<StaffType> types = new ArrayList<>();
        int choice;
        types.add(StaffType.ENGLISH);
        types.add(StaffType.ENGLISH);
        types.add(StaffType.ENGLISH);
        types.add(StaffType.MATH);
        types.add(StaffType.SCIENCE);
        types.add(StaffType.HISTORY);
        choice = Randomizer.setRandom(0, types.size() - 1);
        return types.get(choice);
    }

    //TODO: explore efficiency of optionality
    private static Optional<Staff> selectRandomTeacher(HashMap<Integer, Staff> staffHashMap, GameView view) {
        Random random = new Random();
        int counter = 0;
        List<Integer> keys = new ArrayList<>(staffHashMap.keySet());
        int randomIndex;
        int key;
        do {
            randomIndex = random.nextInt(keys.size());
            key = keys.get(randomIndex);
            counter++;
        } while (staffHashMap.get(key).teacherStatistics.getStaffType() != null && counter < staffHashMap.size());
        if (counter >= staffHashMap.size()) {
            view.appendOutput("Staff cannot be assigned! Ran out of room.");
            return Optional.empty();
        }

        return Optional.of(staffHashMap.get(key));
    }

    public static List<Staff> getTeachersOfType(HashMap<Integer, Staff> staffHashMap, StaffType type) {
        List<Staff> staffList = new ArrayList<>();
        for (Map.Entry<Integer, Staff> staff : staffHashMap.entrySet()) {
            if (staff.getValue().teacherStatistics.getStaffType().equals(type)) {
                staffList.add(staff.getValue());
            }
        }

        return staffList;
    }

    public static void assignClassesToStaff(HashMap<Integer, Staff> staffHashMap, StandardSchool standardSchool, GameView view) {
        List<Staff> englishTeachers = getTeachersOfType(staffHashMap, StaffType.ENGLISH);
        List<Staff> mathTeachers = getTeachersOfType(staffHashMap, StaffType.MATH);
        int studentsInGrade;
        int classesNeeded;
        Staff selectedTeacher;
        TeacherSchedule schedule;
        TeacherBlock block;
        boolean classSwitch = false;

        if (englishTeachers.isEmpty()) {
            view.appendOutput("No English teachers available for assignment.");
            return;
        }

        String[] englishClasses = {"English I", "English II", "English III", "English IV"};
        String[] gradeLevels = {"Freshman", "Sophomore", "Junior", "Senior"};

        int[] studentsPerGrade = new int[gradeLevels.length];
        for (int i = 0; i < gradeLevels.length; i++) {
            HashMap<Integer, Student> gradeClass = standardSchool.getStudentGradeClass(gradeLevels[i]);
            studentsPerGrade[i] = (gradeClass != null) ? gradeClass.size() : 0;
        }

        // Step 1: Assign teachers to regular English classes
        for (int gradeIndex = 0; gradeIndex < englishClasses.length; gradeIndex++) {
            studentsInGrade = studentsPerGrade[gradeIndex];
            classesNeeded = (int) Math.ceil((double) studentsInGrade / 35);

            for (int classCount = 0; classCount < classesNeeded; classCount++) {
                selectedTeacher = null;
                for (Staff teacher : englishTeachers) {
                    schedule = teacher.teacherStatistics.getTeacherSchedule();
                    if (schedule.size() < 4) {
                        selectedTeacher = teacher;
                        break;
                    }
                }

                if (selectedTeacher == null) {
                    view.appendOutput("Not enough teachers available to cover all classes.");
                } else {
                    Room selectedRoom = standardSchool.getClassroomByStaff(selectedTeacher);
                    block = new TeacherBlock();
                    block.setBlockNumber(selectedTeacher.teacherStatistics.getTeacherSchedule().size() + 1);
                    block.setClassName(englishClasses[gradeIndex]);
                    block.setRoom(selectedRoom);
                    block.setSemester("Both");
                    block.addClassPopulationBlock(selectedRoom.getStudentCapacity());

                    selectedTeacher.teacherStatistics.addTeacherSchedule(block);
                    view.appendOutput("Assigned " + englishClasses[gradeIndex] + " to " + selectedTeacher.teacherName.getFirstName() + " " + selectedTeacher.teacherName.getLastName());
                }
            }
        }

        // Step 2: Collect remaining teachers
        List<Staff> remainingTeachers = new ArrayList<>();
        for (Staff teacher : englishTeachers) {
            if (teacher.teacherStatistics.getTeacherSchedule().size() < 4) {
                remainingTeachers.add(teacher);
            }
        }

        // Step 3: Assign only two teachers to AP classes
        if (remainingTeachers.size() >= 2) {
            Staff apLitTeacher = remainingTeachers.remove(remainingTeachers.size() - 1);
            Staff apLangTeacher = remainingTeachers.remove(remainingTeachers.size() - 1);

            // Assign to AP Literature
            while (apLitTeacher.teacherStatistics.getTeacherSchedule().size() < 4) {
                Room selectedRoom = standardSchool.getClassroomByStaff(apLitTeacher);
                block = new TeacherBlock();
                block.setBlockNumber(apLitTeacher.teacherStatistics.getTeacherSchedule().size() + 1);
                block.setClassName("AP English Literature & Composition");
                block.setRoom(selectedRoom);
                block.setSemester("Both");
                block.addClassPopulationBlock(selectedRoom.getStudentCapacity());

                apLitTeacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned AP English Literature & Composition to " + apLitTeacher.teacherName.getFirstName() + " " + apLitTeacher.teacherName.getLastName());
            }

            // Assign to AP Language
            while (apLangTeacher.teacherStatistics.getTeacherSchedule().size() < 4) {
                Room selectedRoom = standardSchool.getClassroomByStaff(apLangTeacher);
                block = new TeacherBlock();
                block.setBlockNumber(apLangTeacher.teacherStatistics.getTeacherSchedule().size() + 1);
                block.setClassName("AP English Language & Composition");
                block.setRoom(selectedRoom);
                block.setSemester("Both");
                block.addClassPopulationBlock(selectedRoom.getStudentCapacity());

                apLangTeacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned AP English Language & Composition to " + apLangTeacher.teacherName.getFirstName() + " " + apLangTeacher.teacherName.getLastName());
            }
        }

        // Step 4: Assign any additional remaining teachers to regular English classes
        for (Staff remainingTeacher : remainingTeachers) {
            while (remainingTeacher.teacherStatistics.getTeacherSchedule().size() < 4) {
                for (String englishClass : englishClasses) {
                    if (remainingTeacher.teacherStatistics.getTeacherSchedule().size() < 4) {
                        Room selectedRoom = standardSchool.getClassroomByStaff(remainingTeacher);
                        block = new TeacherBlock();
                        block.setBlockNumber(remainingTeacher.teacherStatistics.getTeacherSchedule().size() + 1);
                        block.setClassName(englishClass);
                        block.setRoom(selectedRoom);
                        block.setSemester("Both");
                        block.addClassPopulationBlock(selectedRoom.getStudentCapacity());

                        remainingTeacher.teacherStatistics.addTeacherSchedule(block);
                        view.appendOutput("Assigned " + englishClass + " to " + remainingTeacher.teacherName.getFirstName() + " " + remainingTeacher.teacherName.getLastName());
                    } else {
                        break;
                    }
                }
            }
        }
        //Math assignment
        for (int gradeIndex = 0; gradeIndex < 4; gradeIndex++) {
            studentsInGrade = studentsPerGrade[gradeIndex];
            classesNeeded = (int) Math.ceil((double) studentsInGrade / 35);
            for (int classCount = 0; classCount < classesNeeded; classCount++) {
                selectedTeacher = null;
                for (Staff teacher : mathTeachers) {
                    schedule = teacher.teacherStatistics.getTeacherSchedule();
                    if (schedule.size() < 8) {
                        selectedTeacher = teacher;
                        break;
                    }
                }

                if (selectedTeacher == null) {
                    view.appendOutput("Not enough teachers available to cover all classes.");
                } else {
                    //fall math
                    block = new TeacherBlock();
                    String f_name = mathHelper(block, selectedTeacher, "Fall", gradeIndex, standardSchool, mathTeachers.indexOf(selectedTeacher));
                    selectedTeacher.teacherStatistics.addTeacherSchedule(block);
                    //spring math
                    block = new TeacherBlock();
                    String s_name = mathHelper(block, selectedTeacher, "Spring", gradeIndex, standardSchool, mathTeachers.indexOf(selectedTeacher));
                    selectedTeacher.teacherStatistics.addTeacherSchedule(block);

                    view.appendOutput("Assigned " + f_name + " and " + s_name + " to " + selectedTeacher.teacherName.getFirstName() + " " + selectedTeacher.teacherName.getLastName());
                }
            }
        }
    }

    private static String mathHelper(TeacherBlock block, Staff teacher, String semester, int gradeIndex, StandardSchool standardSchool, int count) {

        String classname;
        int roomCapacity = standardSchool.getClassroomByStaff(teacher).getStudentCapacity();

        block.setBlockNumber(teacher.teacherStatistics.getTeacherSchedule().size() + 1);
        block.setRoom(standardSchool.getClassroomByStaff(teacher));
        block.setSemester(semester);
        block.addClassPopulationBlock(roomCapacity);

        if (semester.equals("Fall")) {
            if (gradeIndex == 0) {
                if (count % 2 == 0) {
                    classname = "Geometry";
                } else {
                    classname = "Fundamentals of Math";
                }
                block.setClassName(classname);
                return classname;
            } else if (gradeIndex == 1) {
                if (count % 2 == 0) {
                    classname = "Algebra II";
                } else {
                    classname = "Algebra I";
                }
                block.setClassName(classname);
                return classname;
            } else if (gradeIndex == 2) {
                if (count % 2 == 0) {
                    classname = "Precalculus";
                } else {
                    classname = "Trigonometry";
                }
                block.setClassName(classname);
                return classname;
            } else {
                if (count % 2 == 0) {
                    classname = "AP Calculus AB";
                } else {
                    classname = "Precalculus";
                }
                block.setClassName(classname);
                return classname;
            }
        } else if (semester.equals("Spring")) {
            if (gradeIndex == 0) {
                if (count % 2 == 0) {
                    classname = "Algebra I";
                } else {
                    classname = "Geometry";
                }
                block.setClassName(classname);
                return classname;
            } else if (gradeIndex == 1) {
                if (count % 2 == 0) {
                    classname = "Trigonometry";
                } else {
                    classname = "Algebra II";
                }
                block.setClassName(classname);
                return classname;
            } else if (gradeIndex == 2) {
                if (count % 2 == 0) {
                    classname = "AP Statistics";
                } else {
                    classname = "Math for Data and Financial Literacy";
                }
                block.setClassName(classname);
                return classname;
            } else {
                if (count % 2 == 0) {
                    classname = "AP Calculus BC";
                } else {
                    classname = "Algebra II";
                }
                block.setClassName(classname);
                return classname;
            }
        } else {
            System.err.println("Can't find semester");
            return "";
        }
    }
}
