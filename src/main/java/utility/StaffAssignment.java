package utility;

import entity.Staff;
import entity.StaffType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class StaffAssignment {

    public static void initialAssignments(HashMap<Integer, Staff> staffHashMap) {
        assignPrincipal(staffHashMap);
        assignVicePrincipal(staffHashMap);
        assignGuidanceCouncilors(staffHashMap);
    }

    public static void assignPrincipal(HashMap<Integer, Staff> staffHashMap) {
        Staff teacher = selectRandomTeacher(staffHashMap);
        teacher.teacherStatistics.setStaffType(StaffType.PRINCIPAL);
        System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as principal!");
    }

    public static void assignStaff(Staff staff, StaffType type) {
        staff.teacherStatistics.setStaffType(type);
    }

    public static void assignVicePrincipal(HashMap<Integer, Staff> staffHashMap) {
        Staff teacher = selectRandomTeacher(staffHashMap);
        teacher.teacherStatistics.setStaffType(StaffType.VICE_PRINCIPAL);
        System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as vice principal!");
    }

    public static void assignGuidanceCouncilors(HashMap<Integer, Staff> staffHashMap) {
        int councilMax = 4;

        for(int councilCount = 0; councilCount < councilMax; councilCount++) {
            Staff teacher = selectRandomTeacher(staffHashMap);
            teacher.teacherStatistics.setStaffType(StaffType.GUIDANCE);
            System.out.println("Staff " + teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() + " assigned as guidance councilor!");
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

        return staffHashMap.get(key);
    }
}
