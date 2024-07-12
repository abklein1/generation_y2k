package utility;

import entity.Rooms.Lunchroom;
import entity.Rooms.Room;
import entity.*;
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

        if (type.equals(StaffType.ENGLISH)) {
            coreMax = (int) Math.round(((studentCap / 30.0) / 4.00) + 2);
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

    //TODO: Change the dumb way to do things. Break up this method
    public static void assignClassesToStaff(HashMap<Integer, Staff> staffHashMap, StandardSchool standardSchool, GameView view) {
        List<Staff> englishTeachers = getTeachersOfType(staffHashMap, StaffType.ENGLISH);
        List<Staff> mathTeachers = getTeachersOfType(staffHashMap, StaffType.MATH);
        List<Staff> scienceTeachers = getTeachersOfType(staffHashMap, StaffType.SCIENCE);
        List<Staff> historyTeachers = getTeachersOfType(staffHashMap, StaffType.HISTORY);
        List<Staff> languageTeachers = getTeachersOfType(staffHashMap, StaffType.LANGUAGES);
        List<Staff> gymTeachers = getTeachersOfType(staffHashMap, StaffType.PHYSICAL_ED);
        List<Staff> visualArtsTeachers = getTeachersOfType(staffHashMap, StaffType.VISUAL_ARTS);
        List<Staff> performingArtsTeachers = getTeachersOfType(staffHashMap, StaffType.PERFORMING_ARTS);
        List<Staff> vocationalTeachers = getTeachersOfType(staffHashMap, StaffType.VOCATIONAL);
        List<Staff> businessTeachers = getTeachersOfType(staffHashMap, StaffType.BUSINESS);
        int studentsInGrade;
        int classesNeeded;
        Staff selectedTeacher;
        TeacherSchedule schedule;
        TeacherBlock block;
        int langCount = 0;
        int gymCount = 0;
        int perfCount = 0;

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
                if (selectedRoom != null) {
                    block = new TeacherBlock();
                    block.setBlockNumber(apLitTeacher.teacherStatistics.getTeacherSchedule().size() + 1);
                    block.setClassName("AP English Literature & Composition");
                    block.setRoom(selectedRoom);
                    block.setSemester("Both");
                    block.addClassPopulationBlock(selectedRoom.getStudentCapacity());

                    apLitTeacher.teacherStatistics.addTeacherSchedule(block);
                    view.appendOutput("Assigned AP English Literature & Composition to " + apLitTeacher.teacherName.getFirstName() + " " + apLitTeacher.teacherName.getLastName());
                } else {
                    System.out.println("AP Lit teachers could not be assigned!");
                    break;
                }
            }

            // Assign to AP Language
            while (apLangTeacher.teacherStatistics.getTeacherSchedule().size() < 4) {
                Room selectedRoom = standardSchool.getClassroomByStaff(apLangTeacher);
                if (selectedRoom != null) {
                    block = new TeacherBlock();
                    block.setBlockNumber(apLangTeacher.teacherStatistics.getTeacherSchedule().size() + 1);
                    block.setClassName("AP English Language & Composition");
                    block.setRoom(selectedRoom);
                    block.setSemester("Both");
                    block.addClassPopulationBlock(selectedRoom.getStudentCapacity());

                    apLangTeacher.teacherStatistics.addTeacherSchedule(block);
                    view.appendOutput("Assigned AP English Language & Composition to " + apLangTeacher.teacherName.getFirstName() + " " + apLangTeacher.teacherName.getLastName());
                } else {
                    System.out.println("AP English Language teacher could not be assigned!");
                    break;
                }
            }
        }

        // Step 4: Assign any additional remaining teachers to regular English classes
        for (Staff remainingTeacher : remainingTeachers) {
            while (remainingTeacher.teacherStatistics.getTeacherSchedule().size() < 4) {
                for (String englishClass : englishClasses) {
                    if (remainingTeacher.teacherStatistics.getTeacherSchedule().size() < 4) {
                        Room selectedRoom = standardSchool.getClassroomByStaff(remainingTeacher);
                        if (selectedRoom != null) {
                            block = new TeacherBlock();
                            block.setBlockNumber(remainingTeacher.teacherStatistics.getTeacherSchedule().size() + 1);
                            block.setClassName(englishClass);
                            block.setRoom(selectedRoom);
                            block.setSemester("Both");
                            block.addClassPopulationBlock(selectedRoom.getStudentCapacity());

                            remainingTeacher.teacherStatistics.addTeacherSchedule(block);
                            view.appendOutput("Assigned " + englishClass + " to " + remainingTeacher.teacherName.getFirstName() + " " + remainingTeacher.teacherName.getLastName());
                        } else {
                            System.out.println(remainingTeacher.teacherName.getFirstName() + " " + remainingTeacher.teacherName.getLastName() + " is misisng a room!");
                        }
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
                    view.appendOutput("Not enough teachers available to cover all math classes.");
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
        //Science assignment
        for (int gradeIndex = 0; gradeIndex < 4; gradeIndex++) {
            studentsInGrade = studentsPerGrade[gradeIndex];
            classesNeeded = (int) Math.ceil((double) studentsInGrade / 35);
            for (int classCount = 0; classCount < classesNeeded; classCount++) {
                selectedTeacher = null;
                for (Staff teacher : scienceTeachers) {
                    schedule = teacher.teacherStatistics.getTeacherSchedule();
                    if (schedule.size() < 8) {
                        selectedTeacher = teacher;
                        break;
                    }
                }

                if (selectedTeacher == null) {
                    view.appendOutput("Not enough teachers available to cover all science classes");
                } else {
                    //fall science
                    block = new TeacherBlock();
                    String f_name = scienceHelper(block, selectedTeacher, "Fall", gradeIndex, standardSchool, scienceTeachers.indexOf(selectedTeacher));
                    selectedTeacher.teacherStatistics.addTeacherSchedule(block);
                    //spring science
                    block = new TeacherBlock();
                    String s_name = scienceHelper(block, selectedTeacher, "Spring", gradeIndex, standardSchool, scienceTeachers.indexOf(selectedTeacher));
                    selectedTeacher.teacherStatistics.addTeacherSchedule(block);

                    view.appendOutput("Assigned " + f_name + " and " + s_name + " to " + selectedTeacher.teacherName.getFirstName() + " " + selectedTeacher.teacherName.getLastName());
                }
            }
        }
        //History assignment
        for (int gradeIndex = 0; gradeIndex < 4; gradeIndex++) {
            studentsInGrade = studentsPerGrade[gradeIndex];
            classesNeeded = (int) Math.ceil((double) studentsInGrade / 35);
            for (int classCount = 0; classCount < classesNeeded; classCount++) {
                selectedTeacher = null;
                for (Staff teacher : historyTeachers) {
                    schedule = teacher.teacherStatistics.getTeacherSchedule();
                    if (schedule.size() < 8) {
                        selectedTeacher = teacher;
                        break;
                    }
                }

                if (selectedTeacher == null) {
                    view.appendOutput("Not enough teachers available to cover all history classes");
                } else {
                    //fall history
                    block = new TeacherBlock();
                    String f_name = historyHelper(block, selectedTeacher, "Fall", gradeIndex, standardSchool, scienceTeachers.indexOf(selectedTeacher));
                    selectedTeacher.teacherStatistics.addTeacherSchedule(block);
                    //spring history
                    block = new TeacherBlock();
                    String s_name = historyHelper(block, selectedTeacher, "Spring", gradeIndex, standardSchool, scienceTeachers.indexOf(selectedTeacher));
                    selectedTeacher.teacherStatistics.addTeacherSchedule(block);

                    view.appendOutput("Assigned " + f_name + " and " + s_name + " to " + selectedTeacher.teacherName.getFirstName() + " " + selectedTeacher.teacherName.getLastName());
                }
            }
        }
        //Language Assignment
        for (Staff teacher : languageTeachers) {
            languageHelper(teacher, standardSchool, langCount, view);
            if (langCount >= 6) {
                langCount = 0;
            } else {
                langCount++;
            }
        }
        //Gym Assignment
        for (Staff teacher : gymTeachers) {
            gymHelper(teacher, standardSchool, gymCount, view);
            if (gymCount >= 3) {
                gymCount = 0;
            } else {
                gymCount++;
            }
        }
        //Visual Arts Assignment
        for (Staff teacher : visualArtsTeachers) {
            visualArtsHelper(teacher, standardSchool, view);
        }
        //Performing Arts Assignment
        for (Staff teacher : performingArtsTeachers) {
            performingArtsHelper(teacher, standardSchool, perfCount, view);
            if(perfCount >= 4) {
                perfCount = 0;
            } else {
                perfCount++;
            }
        }
        //Vocational Assignment
        for(Staff teacher : vocationalTeachers) {
            vocationalHelper(teacher, standardSchool, view);
        }
        //Business Assignment
        for(Staff teacher : businessTeachers) {
            businessHelper(teacher, standardSchool, view);
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

    private static String scienceHelper(TeacherBlock block, Staff teacher, String semester, int gradeIndex, StandardSchool standardSchool, int count) {

        String classname;
        Room potentialRoom = standardSchool.getClassroomByStaff(teacher);
        if (potentialRoom != null) {
            int roomCapacity = potentialRoom.getStudentCapacity();

            block.setBlockNumber(teacher.teacherStatistics.getTeacherSchedule().size() + 1);
            block.setRoom(standardSchool.getClassroomByStaff(teacher));
            block.setSemester(semester);
            block.addClassPopulationBlock(roomCapacity);

            if (semester.equals("Fall")) {
                if (gradeIndex == 0) {
                    classname = "Biology";
                    block.setClassName(classname);
                    return classname;
                } else if (gradeIndex == 1) {
                    classname = "Chemistry";
                    block.setClassName(classname);
                    return classname;
                } else if (gradeIndex == 2) {
                    if (count % 2 == 0) {
                        classname = "AP Biology";
                    } else {
                        classname = "Anatomy and Physiology";
                    }
                    block.setClassName(classname);
                    return classname;
                } else {
                    if (count % 2 == 0) {
                        classname = "AP Physics B";
                    } else {
                        classname = "Physics";
                    }
                    block.setClassName(classname);
                    return classname;
                }
            } else if (semester.equals("Spring")) {
                if (gradeIndex == 0) {
                    classname = "Biology";
                    block.setClassName(classname);
                    return classname;
                } else if (gradeIndex == 1) {
                    if (count % 2 == 0) {
                        classname = "Earth and Space Science";
                    } else {
                        classname = "Physical Science";
                    }
                    block.setClassName(classname);
                    return classname;
                } else if (gradeIndex == 2) {
                    if (count % 2 == 0) {
                        classname = "AP Chemistry";
                    } else {
                        classname = "Anatomy and Physiology";
                    }
                    block.setClassName(classname);
                    return classname;
                } else {
                    if (count % 2 == 0) {
                        classname = "AP Physics C";
                    } else {
                        classname = "Environmental Science";
                    }
                    block.setClassName(classname);
                    return classname;
                }
            } else {
                System.err.println("Can't find semester");
                return "";
            }
        } else {
            System.err.println("Room is null " + " for teacher " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
            return "";
        }
    }

    private static String historyHelper(TeacherBlock block, Staff teacher, String semester, int gradeIndex, StandardSchool standardSchool, int count) {
        String classname;
        Room potentialRoom = standardSchool.getClassroomByStaff(teacher);
        if (potentialRoom != null) {
            int roomCapacity = potentialRoom.getStudentCapacity();

            block.setBlockNumber(teacher.teacherStatistics.getTeacherSchedule().size() + 1);
            block.setRoom(standardSchool.getClassroomByStaff(teacher));
            block.setSemester(semester);
            block.addClassPopulationBlock(roomCapacity);

            if (semester.equals("Fall")) {
                if (gradeIndex == 0) {
                    classname = "World Geography";
                    block.setClassName(classname);
                    return classname;
                } else if (gradeIndex == 1) {
                    if (count % 2 == 0) {
                        classname = "World History";
                    } else {
                        classname = "AP World History";
                    }
                    block.setClassName(classname);
                    return classname;
                } else if (gradeIndex == 2) {
                    if (count % 2 == 0) {
                        classname = "US History";
                    } else {
                        classname = "AP US History";
                    }
                    block.setClassName(classname);
                    return classname;
                } else {
                    if (count % 2 == 0) {
                        classname = "US Government";
                    } else {
                        classname = "AP US Government";
                    }
                    block.setClassName(classname);
                    return classname;
                }
            } else if (semester.equals("Spring")) {
                if (gradeIndex == 0) {
                    classname = "AP Human Geography";
                    block.setClassName(classname);
                    return classname;
                } else if (gradeIndex == 1) {
                    if (count % 2 == 0) {
                        classname = "World History";
                    } else {
                        classname = "AP World History";
                    }
                    block.setClassName(classname);
                    return classname;
                } else if (gradeIndex == 2) {
                    if (count % 2 == 0) {
                        classname = "US History";
                    } else {
                        classname = "AP US History";
                    }
                    block.setClassName(classname);
                    return classname;
                } else {
                    if (count % 2 == 0) {
                        classname = "US Government";
                    } else {
                        classname = "AP Economics Macro";
                    }
                    block.setClassName(classname);
                    return classname;
                }
            } else {
                System.err.println("Can't find semester");
                return "";
            }
        } else {
            System.err.println("Room is null " + " for teacher " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
            return "";
        }
    }

    private static void languageHelper(Staff teacher, StandardSchool standardSchool, int count, GameView view) {
        String[] classes = new String[8];
        String f_name = teacher.teacherName.getFirstName();
        String l_name = teacher.teacherName.getLastName();
        Room room = standardSchool.getClassroomByStaff(teacher);
        if (room != null) {
            int studentPop = room.getStudentCapacity();
            switch (count) {
                case 0 -> {
                    classes[0] = "Spanish I";
                    classes[1] = "Spanish II";
                    classes[2] = "Spanish I";
                    classes[3] = "Spanish II";
                    classes[4] = "Spanish I";
                    classes[5] = "Spanish II";
                    classes[6] = "Spanish I";
                    classes[7] = "Spanish II";
                }
                case 1 -> {
                    classes[0] = "Spanish III";
                    classes[1] = "Spanish IV";
                    classes[2] = "Spanish III";
                    classes[3] = "Spanish IV";
                    classes[4] = "Spanish III";
                    classes[5] = "Spanish IV";
                    classes[6] = "AP Spanish Literature";
                    classes[7] = "AP Spanish Language";
                }
                case 2 -> {
                    classes[0] = "French I";
                    classes[1] = "French II";
                    classes[2] = "French I";
                    classes[3] = "French II";
                    classes[4] = "French I";
                    classes[5] = "French II";
                    classes[6] = "French I";
                    classes[7] = "French II";
                }
                case 3 -> {
                    classes[0] = "German I";
                    classes[1] = "German II";
                    classes[2] = "German I";
                    classes[3] = "German II";
                    classes[4] = "German I";
                    classes[5] = "German II";
                    classes[6] = "German I";
                    classes[7] = "German II";
                }
                case 4 -> {
                    classes[0] = "American Sign Language I";
                    classes[1] = "American Sign Language II";
                    classes[2] = "American Sign Language I";
                    classes[3] = "American Sign Language II";
                    classes[4] = "American Sign Language I";
                    classes[5] = "American Sign Language II";
                    classes[6] = "American Sign Language I";
                    classes[7] = "American Sign Language II";
                }
                case 5 -> {
                    classes[0] = "Latin I";
                    classes[1] = "Latin II";
                    classes[2] = "Latin I";
                    classes[3] = "Latin II";
                    classes[4] = "Latin I";
                    classes[5] = "Latin II";
                    classes[6] = "Latin I";
                    classes[7] = "Latin II";
                }
            }
            int index = 0;
            for (String classN : classes) {
                TeacherBlock block = new TeacherBlock();
                block.setClassName(classN);
                if (index % 2 == 0) {
                    block.setSemester("Fall");
                } else {
                    block.setSemester("Spring");
                }
                switch (index) {
                    case 0, 4 -> block.setBlockNumber(1);
                    case 1, 5 -> block.setBlockNumber(2);
                    case 2, 6 -> block.setBlockNumber(3);
                    case 3, 7 -> block.setBlockNumber(4);
                }
                block.setBlockPopulation(studentPop);
                block.setRoom(room);
                teacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned " + block.getClassName() + " to " + f_name + " " + l_name);
                index++;
            }
        } else {
            System.err.println("Room is null " + " for teacher " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
        }
    }

    private static void gymHelper(Staff teacher, StandardSchool standardSchool, int count, GameView view) {
        String[] classes = new String[8];
        String f_name = teacher.teacherName.getFirstName();
        String l_name = teacher.teacherName.getLastName();
        Room room = standardSchool.getRoomByStaff(teacher, "Physical Education");
        if (room != null) {
            int studentPop = room.getStudentCapacity();
            switch (count) {
                case 0 -> {
                    classes[0] = "Health";
                    classes[1] = "Health";
                    classes[2] = "Health";
                    classes[3] = "Health";
                    classes[4] = "Team Sports";
                    classes[5] = "Weightlifting";
                    classes[6] = "Specialized Sports";
                    classes[7] = "Lifetime Recreation";
                }
                case 1 -> {
                    classes[0] = "Weightlifting";
                    classes[1] = "Dance";
                    classes[2] = "Team Sports";
                    classes[3] = "Specialized Sports";
                    classes[4] = "Health";
                    classes[5] = "Health";
                    classes[6] = "Health";
                    classes[7] = "Health";
                }
                case 2 -> {
                    classes[0] = "Specialized Sports";
                    classes[1] = "Weightlifting";
                    classes[2] = "Health";
                    classes[3] = "Health";
                    classes[4] = "Health";
                    classes[5] = "Health";
                    classes[6] = "Lifetime Recreation";
                    classes[7] = "Dance";
                }
                default -> {
                    classes[0] = "Health";
                    classes[1] = "Health";
                    classes[2] = "Health";
                    classes[3] = "Health";
                    classes[4] = "Health";
                    classes[5] = "Health";
                    classes[6] = "Health";
                    classes[7] = "Health";
                }
            }
            int index = 0;
            for (String classN : classes) {
                TeacherBlock block = new TeacherBlock();
                block.setClassName(classN);
                if (index % 2 == 0) {
                    block.setSemester("Fall");
                } else {
                    block.setSemester("Spring");
                }
                switch (index) {
                    case 0, 4 -> block.setBlockNumber(1);
                    case 1, 5 -> block.setBlockNumber(2);
                    case 2, 6 -> block.setBlockNumber(3);
                    case 3, 7 -> block.setBlockNumber(4);
                }
                block.setBlockPopulation(studentPop);
                block.setRoom(room);
                teacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned " + block.getClassName() + " to " + f_name + " " + l_name);
                index++;
            }
        } else {
            System.err.println("Room is null " + " for teacher " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
        }
    }

    private static void visualArtsHelper(Staff teacher, StandardSchool standardSchool, GameView view) {
        String[] classes = new String[8];
        String f_name = teacher.teacherName.getFirstName();
        String l_name = teacher.teacherName.getLastName();
        Room room = standardSchool.getRoomByStaff(teacher, "Visual Arts");
        int choice = Randomizer.setRandom(0,4);
        if (room != null) {
            int studentPop = room.getStudentCapacity();
            switch (choice) {
                case 0 -> {
                    classes[0] = "2D Studio Art I";
                    classes[1] = "2D Studio Art II";
                    classes[2] = "2D Studio Art I";
                    classes[3] = "2D Studio Art II";
                    classes[4] = "3D Studio Art I";
                    classes[5] = "3D Studio Art II";
                    classes[6] = "3D Studio Art I";
                    classes[7] = "3D Studio Art II";
                }
                case 1 -> {
                    classes[0] = "Photography I";
                    classes[1] = "Photography II";
                    classes[2] = "Photography I";
                    classes[3] = "Photography II";
                    classes[4] = "Printmaking";
                    classes[5] = "Printmaking";
                    classes[6] = "Printmaking";
                    classes[7] = "Printmaking";
                }
                case 2 -> {
                    classes[0] = "3D Studio Art I";
                    classes[1] = "3D Studio Art II";
                    classes[2] = "3D Studio Art I";
                    classes[3] = "3D Studio Art II";
                    classes[4] = "2D Studio Art I";
                    classes[5] = "2D Studio Art II";
                    classes[6] = "2D Studio Art I";
                    classes[7] = "2D Studio Art II";
                }
                case 3 -> {
                    classes[0] = "Printmaking";
                    classes[1] = "Printmaking";
                    classes[2] = "Printmaking";
                    classes[3] = "Printmaking";
                    classes[4] = "Photography I";
                    classes[5] = "Photography II";
                    classes[6] = "Photography I";
                    classes[7] = "Photography II";
                }
                default -> {
                    classes[0] = "2D Studio Art II";
                    classes[1] = "AP Art History";
                    classes[2] = "2D Studio Art II";
                    classes[3] = "AP Art History";
                    classes[4] = "AP Studio Art";
                    classes[5] = "AP Studio Art";
                    classes[6] = "AP Studio Art";
                    classes[7] = "AP Studio Art";
                }
            }
            int index = 0;
            for (String classN : classes) {
                TeacherBlock block = new TeacherBlock();
                block.setClassName(classN);
                if (index % 2 == 0) {
                    block.setSemester("Fall");
                } else {
                    block.setSemester("Spring");
                }
                switch (index) {
                    case 0, 4 -> block.setBlockNumber(1);
                    case 1, 5 -> block.setBlockNumber(2);
                    case 2, 6 -> block.setBlockNumber(3);
                    case 3, 7 -> block.setBlockNumber(4);
                }
                block.setBlockPopulation(studentPop);
                block.setRoom(room);
                teacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned " + block.getClassName() + " to " + f_name + " " + l_name);
                index++;
            }
        } else {
            System.err.println("Room is null " + " for teacher " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
        }
    }

    private static void performingArtsHelper(Staff teacher, StandardSchool standardSchool, int count,  GameView view) {
        String[] classes = new String[8];
        String f_name = teacher.teacherName.getFirstName();
        String l_name = teacher.teacherName.getLastName();
        Room room = standardSchool.getRoomByStaff(teacher, "Performing Arts");
        int choice = Randomizer.setRandom(0,4);
        if (room != null) {
            int studentPop = room.getStudentCapacity();
            switch (count) {
                case 0 -> {
                    classes[0] = "Concert Band";
                    classes[1] = "Concert Band";
                    classes[2] = "Concert Band";
                    classes[3] = "Concert Band";
                    classes[4] = "Marching Band";
                    classes[5] = "Marching Band";
                    classes[6] = "Marching Band";
                    classes[7] = "Marching Band";
                }
                case 1 -> {
                    classes[0] = "Theater I";
                    classes[1] = "Theater II";
                    classes[2] = "Theater I";
                    classes[3] = "Theater II";
                    classes[4] = "Musical Theater I";
                    classes[5] = "Musical Theater II";
                    classes[6] = "Musical Theater I";
                    classes[7] = "Musical Theater II";
                }
                case 2 -> {
                    classes[0] = "Choir";
                    classes[1] = "Choir";
                    classes[2] = "Choir";
                    classes[3] = "Choir";
                    classes[4] = "Choir";
                    classes[5] = "Choir";
                    classes[6] = "Choir";
                    classes[7] = "Choir";
                }
                case 3 -> {
                    classes[0] = "Theater Technology";
                    classes[1] = "Theater Technology";
                    classes[2] = "Theater Technology";
                    classes[3] = "Theater Technology";
                    classes[4] = "Theater Technology";
                    classes[5] = "Theater Technology";
                    classes[6] = "Theater Technology";
                    classes[7] = "Theater Technology";
                }
                default -> {
                    switch (choice) {
                        case 0 -> {
                            classes[0] = "Dance Techniques I";
                            classes[1] = "Dance Techniques II";
                            classes[2] = "Dance Techniques I";
                            classes[3] = "Dance Techniques II";
                            classes[4] = "Dance Techniques I";
                            classes[5] = "Dance Techniques II";
                            classes[6] = "Dance Techniques I";
                            classes[7] = "Dance Techniques II";
                        }
                        case 1 -> {
                            classes[0] = "Debate";
                            classes[1] = "Debate";
                            classes[2] = "Debate";
                            classes[3] = "Debate";
                            classes[4] = "Debate";
                            classes[5] = "Debate";
                            classes[6] = "Debate";
                            classes[7] = "Debate";
                        }
                        case 2 -> {
                            classes[0] = "AP Music Theory";
                            classes[1] = "AP Music Theory";
                            classes[2] = "AP Music Theory";
                            classes[3] = "AP Music Theory";
                            classes[4] = "Jazz Band";
                            classes[5] = "Jazz Band";
                            classes[6] = "Jazz Band";
                            classes[7] = "Jazz Band";
                        }
                        default -> {
                            classes[0] = "Musical Theater I";
                            classes[1] = "Musical Theater II";
                            classes[2] = "Musical Theater I";
                            classes[3] = "Musical Theater II";
                            classes[4] = "Theater I";
                            classes[5] = "Theater II";
                            classes[6] = "Theater I";
                            classes[7] = "Theater II";
                        }
                    }
                }
            }
            int index = 0;
            for (String classN : classes) {
                TeacherBlock block = new TeacherBlock();
                block.setClassName(classN);
                if (index % 2 == 0) {
                    block.setSemester("Fall");
                } else {
                    block.setSemester("Spring");
                }
                switch (index) {
                    case 0, 4 -> block.setBlockNumber(1);
                    case 1, 5 -> block.setBlockNumber(2);
                    case 2, 6 -> block.setBlockNumber(3);
                    case 3, 7 -> block.setBlockNumber(4);
                }
                block.setBlockPopulation(studentPop);
                block.setRoom(room);
                teacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned " + block.getClassName() + " to " + f_name + " " + l_name);
                index++;
            }
        } else {
            System.err.println("Room is null " + " for teacher " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
        }
    }

    private static void vocationalHelper(Staff teacher, StandardSchool standardSchool, GameView view) {
        String[] classes = new String[8];
        String f_name = teacher.teacherName.getFirstName();
        String l_name = teacher.teacherName.getLastName();
        Room room = standardSchool.getRoomByStaff(teacher, "Vocational");
        int choice = Randomizer.setRandom(0,9);
        if(room != null) {
            int studentPop = room.getStudentCapacity();
            switch (choice) {
                case 0 -> {
                    classes[0] = "Computer Aided Drafting I";
                    classes[1] = "Computer Aided Drafting II";
                    classes[2] = "Computer Aided Drafting I";
                    classes[3] = "Computer Aided Drafting II";
                    classes[4] = "Computer Aided Drafting I";
                    classes[5] = "Computer Aided Drafting II";
                    classes[6] = "Computer Aided Drafting I";
                    classes[7] = "Computer Aided Drafting II";
                }
                case 1 -> {
                    classes[0] = "Auto Body Repair";
                    classes[1] = "Auto Mechanics";
                    classes[2] = "Auto Body Repair";
                    classes[3] = "Auto Mechanics";
                    classes[4] = "Auto Body Repair";
                    classes[5] = "Auto Mechanics";
                    classes[6] = "Auto Body Repair";
                    classes[7] = "Auto Mechanics";
                }
                case 2 -> {
                    classes[0] = "ROTC";
                    classes[1] = "ROTC";
                    classes[2] = "ROTC";
                    classes[3] = "ROTC";
                    classes[4] = "ROTC";
                    classes[5] = "ROTC";
                    classes[6] = "ROTC";
                    classes[7] = "ROTC";
                }
                case 3 -> {
                    classes[0] = "Culinary Arts";
                    classes[1] = "Culinary Arts";
                    classes[2] = "Culinary Arts";
                    classes[3] = "Culinary Arts";
                    classes[4] = "Culinary Arts";
                    classes[5] = "Culinary Arts";
                    classes[6] = "Culinary Arts";
                    classes[7] = "Culinary Arts";
                }
                case 4 -> {
                    classes[0] = "Keyboarding";
                    classes[1] = "Word Processing";
                    classes[2] = "Keyboarding";
                    classes[3] = "Word Processing";
                    classes[4] = "Keyboarding";
                    classes[5] = "Word Processing";
                    classes[6] = "Keyboarding";
                    classes[7] = "Word Processing";
                }
                case 5 -> {
                    classes[0] = "Digital Production Technology";
                    classes[1] = "Film Production";
                    classes[2] = "Digital Production Technology";
                    classes[3] = "Film Production";
                    classes[4] = "Digital Production Technology";
                    classes[5] = "Film Production";
                    classes[6] = "Digital Production Technology";
                    classes[7] = "Film Production";
                }
                case 6 -> {
                    classes[0] = "Woodworking";
                    classes[1] = "Building Construction and Technology";
                    classes[2] = "Woodworking";
                    classes[3] = "Building Construction and Technology";
                    classes[4] = "Woodworking";
                    classes[5] = "Building Construction and Technology";
                    classes[6] = "Woodworking";
                    classes[7] = "Building Construction and Technology";
                }
                case 7 -> {
                    classes[0] = "Philosophy";
                    classes[1] = "AP Psychology";
                    classes[2] = "Philosophy";
                    classes[3] = "AP Psychology";
                    classes[4] = "Philosophy";
                    classes[5] = "AP Psychology";
                    classes[6] = "Philosophy";
                    classes[7] = "AP Psychology";
                }
                case 8 -> {
                    classes[0] = "Home Economics";
                    classes[1] = "Family Studies";
                    classes[2] = "Home Economics";
                    classes[3] = "Family Studies";
                    classes[4] = "Home Economics";
                    classes[5] = "Family Studies";
                    classes[6] = "Home Economics";
                    classes[7] = "Family Studies";
                }
                case 9 -> {
                    classes[0] = "Intro to Programming";
                    classes[1] = "Intro to Programming";
                    classes[2] = "Intro to Programming";
                    classes[3] = "Intro to Programming";
                    classes[4] = "Intro to Programming";
                    classes[5] = "Intro to Programming";
                    classes[6] = "Intro to Programming";
                    classes[7] = "Intro to Programming";
                }
            }
            int index = 0;
            for (String classN : classes) {
                TeacherBlock block = new TeacherBlock();
                block.setClassName(classN);
                if (index % 2 == 0) {
                    block.setSemester("Fall");
                } else {
                    block.setSemester("Spring");
                }
                switch (index) {
                    case 0, 4 -> block.setBlockNumber(1);
                    case 1, 5 -> block.setBlockNumber(2);
                    case 2, 6 -> block.setBlockNumber(3);
                    case 3, 7 -> block.setBlockNumber(4);
                }
                block.setBlockPopulation(studentPop);
                block.setRoom(room);
                teacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned " + block.getClassName() + " to " + f_name + " " + l_name);
                index++;
            }
        } else {
            System.err.println("Room is null " + " for teacher " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
        }
    }

    private static void businessHelper(Staff teacher, StandardSchool standardSchool, GameView view) {
        String[] classes = new String[8];
        String f_name = teacher.teacherName.getFirstName();
        String l_name = teacher.teacherName.getLastName();
        Room room = standardSchool.getRoomByStaff(teacher, "Business");
        int choice = Randomizer.setRandom(0,2);
        if (room != null) {
            int studentPop = room.getStudentCapacity();
            switch (choice) {
                case 0 -> {
                    classes[0] = "Introduction to Business";
                    classes[1] = "Entrepreneurial Skills";
                    classes[2] = "Introduction to Business";
                    classes[3] = "Entrepreneurial Skills";
                    classes[4] = "Introduction to Business";
                    classes[5] = "Entrepreneurial Skills";
                    classes[6] = "Introduction to Business";
                    classes[7] = "Entrepreneurial Skills";
                }
                case 1 -> {
                    classes[0] = "Business Management";
                    classes[1] = "Marketing";
                    classes[2] = "Business Management";
                    classes[3] = "Marketing";
                    classes[4] = "Business Management";
                    classes[5] = "Marketing";
                    classes[6] = "Business Management";
                    classes[7] = "Marketing";
                }
                case 2 -> {
                    classes[0] = "Personal Finance";
                    classes[1] = "Accounting";
                    classes[2] = "Personal Finance";
                    classes[3] = "Accounting";
                    classes[4] = "Personal Finance";
                    classes[5] = "Accounting";
                    classes[6] = "Personal Finance";
                    classes[7] = "Accounting";
                }
            }
            int index = 0;
            for (String classN : classes) {
                TeacherBlock block = new TeacherBlock();
                block.setClassName(classN);
                if (index % 2 == 0) {
                    block.setSemester("Fall");
                } else {
                    block.setSemester("Spring");
                }
                switch (index) {
                    case 0, 4 -> block.setBlockNumber(1);
                    case 1, 5 -> block.setBlockNumber(2);
                    case 2, 6 -> block.setBlockNumber(3);
                    case 3, 7 -> block.setBlockNumber(4);
                }
                block.setBlockPopulation(studentPop);
                block.setRoom(room);
                teacher.teacherStatistics.addTeacherSchedule(block);
                view.appendOutput("Assigned " + block.getClassName() + " to " + f_name + " " + l_name);
                index++;
            }
        } else {
            System.err.println("Room is null " + " for teacher " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
        }
    }
}
