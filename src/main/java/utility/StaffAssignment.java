package utility;

import entity.Staff;
import entity.StaffType;

import java.util.*;

public class StaffAssignment {
    //TODO: clean up these functions. Some can be combined
    public static void initialAssignmentsCore(HashMap<Integer, Staff> staffHashMap, int studentCap) {
        assignPrincipal(staffHashMap);
        assignVicePrincipal(staffHashMap);
        assignGuidanceCouncilors(staffHashMap);
        assignCoreTeachers(staffHashMap, studentCap, StaffType.ENGLISH);
        assignCoreTeachers(staffHashMap, studentCap, StaffType.HISTORY);
        assignCoreTeachers(staffHashMap, studentCap, StaffType.MATH);
        assignCoreTeachers(staffHashMap, studentCap, StaffType.SCIENCE);
        assignLanguageTeachers(staffHashMap, studentCap);
    }

    public static void assignPrincipal(HashMap<Integer, Staff> staffHashMap) {
        Staff teacher = selectRandomTeacher(staffHashMap);
        teacher.teacherStatistics.setStaffType(StaffType.PRINCIPAL);
        System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as principal!");
    }

    public static void assignStaff(Staff staff, StaffType type) {
        staff.teacherStatistics.setStaffType(type);
    }

    private static void assignVicePrincipal(HashMap<Integer, Staff> staffHashMap) {
        Staff teacher = selectRandomTeacher(staffHashMap);
        teacher.teacherStatistics.setStaffType(StaffType.VICE_PRINCIPAL);
        System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as vice principal!");
    }

    private static void assignGuidanceCouncilors(HashMap<Integer, Staff> staffHashMap) {
        int councilMax = 4;

        for (int councilCount = 0; councilCount < councilMax; councilCount++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(StaffType.GUIDANCE);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as guidance councilor!");
        }
    }

    private static void assignCoreTeachers(HashMap<Integer, Staff> staffHashMap, int studentCap, StaffType type) {
        // Core reqs (I- IV) have to be taught to half the student body per semester. Each teacher can handle 35 students and has 3 periods to teach a subject
        // Plus 4 extra teachers to teach ancillary courses/ AP
        int coreMax = (((studentCap / 2) / 35) / 3) + 4;

        for (int count = 0; count < coreMax; count++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(type);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + type.toString().toLowerCase() + " teacher!");
        }
    }

    private static void assignLanguageTeachers(HashMap<Integer, Staff> staffHashMap, int studentCap) {
        // For now hardcode 2 teachers per language unless student cap below 800
        int staffMax;
        if (studentCap < 800) {
            staffMax = 4;
        }
        staffMax = 6;

        for (int count = 0; count < staffMax; count++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(StaffType.LANGUAGES);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.LANGUAGES.toString().toLowerCase() + " teacher!");
        }
    }

    public static void assignElectiveByRooms(HashMap<Integer, Staff> staffHashMap, int roomCount, StaffType type) {
        for (int count = 0; count < roomCount; count++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(type);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + type.toString().toLowerCase() + " teacher!");
        }
    }

    public static void assignFrontOfficePersonnel(HashMap<Integer, Staff> staffHashMap) {
        int maxOffice = 4;

        for (int count = 0; count < maxOffice; count++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(StaffType.OFFICE);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as " + StaffType.OFFICE.toString().toLowerCase() + " staff!");
        }
    }

    public static void assignUtilityPersonnel(HashMap<Integer, Staff> staffHashMap) {
        int maxUtility = 4;

        for (int count = 0; count < maxUtility; count++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(StaffType.MAINTENANCE);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.MAINTENANCE.toString().toLowerCase());
        }
    }

    public static void assignLibraryPersonnel(HashMap<Integer, Staff> staffHashMap) {
        int maxLibrarian = 2;

        for (int count = 0; count < maxLibrarian; count++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(StaffType.LIBRARY);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.LIBRARY.toString().toLowerCase());
        }
    }

    public static void assignNurse(HashMap<Integer, Staff> staffHashMap) {
        int maxNurse = 2;

        for (int count = 0; count < maxNurse; count++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(StaffType.NURSE);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as a " + StaffType.NURSE.toString().toLowerCase());
        }
    }

    public static void assignLunch(HashMap<Integer, Staff> staffHashMap) {
        int maxLunchroom = 3;

        for (int count = 0; count < maxLunchroom; count++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(StaffType.LUNCH);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.LUNCH.toString().toLowerCase());
        }
    }

    public static void assignBusiness(HashMap<Integer, Staff> staffHashMap) {
        Staff teacher = selectRandomTeacher(staffHashMap);
        teacher.teacherStatistics.setStaffType(StaffType.BUSINESS);
        System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned to " + StaffType.BUSINESS.toString().toLowerCase());
    }

    // If anyone is left assign them to sub
    public static void assignSubs(HashMap<Integer,Staff> staffHashMap) {
        for(Map.Entry<Integer, Staff> staff : staffHashMap.entrySet()) {
            if(staff.getValue().teacherStatistics.getStaffType() == null) {
                staff.getValue().teacherStatistics.setStaffType(StaffType.SUB);
                System.out.println("Staff " + staff.getValue().teacherName.getFirstName() + " " + staff.getValue().teacherName.getLastName() + " assigned to " + StaffType.SUB.toString().toLowerCase());
            }
        }
    }

    private static Staff selectRandomTeacher(HashMap<Integer, Staff> staffHashMap) {
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
        // TODO: error handling etc. prob throw exception
        if (counter >= staffHashMap.size()) {
            System.out.println("Staff cannot be assigned! Ran out of room.");
            return null;
        }

        return staffHashMap.get(key);
    }
}
