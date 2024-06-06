package utility;

import entity.*;
import view.GameView;

import java.util.*;

public class StaffAssignment {
    //TODO: clean up these functions. Some can be combined
    public static void initialAssignmentsCore(HashMap<Integer, Staff> staffHashMap, int studentCap, GameView view) {
        assignPrincipal(staffHashMap, view);
        assignVicePrincipal(staffHashMap, view);
        assignGuidanceCouncilors(staffHashMap, view);
        assignCoreTeachers(staffHashMap, studentCap, StaffType.ENGLISH, view);
        assignCoreTeachers(staffHashMap, studentCap, StaffType.HISTORY, view);
        assignCoreTeachers(staffHashMap, studentCap, StaffType.MATH, view);
        assignCoreTeachers(staffHashMap, studentCap, StaffType.SCIENCE, view);
        assignLanguageTeachers(staffHashMap, studentCap, view);
    }

    public static void assignPrincipal(HashMap<Integer, Staff> staffHashMap, GameView view) {
        Optional<Staff> optionalStaff = selectRandomTeacher(staffHashMap, view);
        if (optionalStaff.isPresent()) {
            Staff teacher = optionalStaff.get();
            teacher.teacherStatistics.setStaffType(StaffType.PRINCIPAL);
            view.appendOutput("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as principal!");
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
        int coreMax = (((studentCap / 2) / 35) / 3) + 4;

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
        int maxOffice = 4;

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

    public static void assignUtilityPersonnel(HashMap<Integer, Staff> staffHashMap, GameView view) {
        int maxUtility = 4;

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
    public static void assignLibraryPersonnel(HashMap<Integer, Staff> staffHashMap, GameView view) {
        int maxLibrarian = 2;

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

    //TODO: fix this since there can be multiple lunchrooms
    public static void assignLunch(HashMap<Integer, Staff> staffHashMap, GameView view) {
        int maxLunchroom = 3;

        for (int count = 0; count < maxLunchroom; count++) {
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
            if(staff.getValue().teacherStatistics.getStaffType().equals(type)) {
                staffList.add(staff.getValue());
            }
        }

        return staffList;
    }

    public static void assignClassesToStaff(HashMap<Integer, Staff> staffHashMap, StandardSchool standardSchool, GameView view) {
        List<Staff> englishTeachers = getTeachersOfType(staffHashMap, StaffType.ENGLISH);

        if (englishTeachers.isEmpty()) {
            view.appendOutput("No English teachers available for assignment.");
        }

        String[] englishClasses = {"English I", "English II", "English III", "English IV"};
        String[] gradeLevels = {"Freshman", "Sophomore", "Junior", "Senior"};

        int[] studentsPerGrade = new int[gradeLevels.length];
        for (int i = 0; i < gradeLevels.length; i++) {
            HashMap<Integer, Student> gradeClass = standardSchool.getStudentGradeClass(gradeLevels[i]);
            studentsPerGrade[i] = (gradeClass != null) ? gradeClass.size() : 0;
        }

        for (int gradeIndex = 0; gradeIndex < englishClasses.length; gradeIndex++) {
            int studentsInGrade = studentsPerGrade[gradeIndex];
            int classesNeeded = (int) Math.ceil((double) studentsInGrade/35);

            for(int classCount = 0; classCount < classesNeeded; classCount++) {
                Staff selectedTeacher = null;
                for(Staff teacher : englishTeachers) {
                    TeacherSchedule schedule = teacher.teacherStatistics.getTeacherSchedule();
                    if(schedule.size() < 4) {
                        selectedTeacher = teacher;
                        break;
                    }
                }

                if (selectedTeacher == null) {
                    view.appendOutput("Not enough teachers available to cover all classes.");
                    return;
                }

                TeacherBlock block = new TeacherBlock();
                block.setBlockNumber(selectedTeacher.teacherStatistics.getTeacherSchedule().size() + 1);
                block.setClassName(englishClasses[gradeIndex]);
                block.setSemester("Both");

                selectedTeacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned " + englishClasses[gradeIndex] + " to " + selectedTeacher.teacherName.getFirstName() + " " + selectedTeacher.teacherName.getLastName());
            }
        }
    }
}
