package utility;

import entity.Staff;
import entity.StaffType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class StaffAssignment {
    public static void assignPrincipal(HashMap<Integer, Staff> staffHashMap) {
        Staff teacher = selectRandomTeacher(staffHashMap);
        teacher.teacherStatistics.setStaffType(StaffType.PRINCIPAL);
        System.out.println("Staff " + teacher.teacherName.getFirstName() + " assigned as principal!");
    }

    public static void assignPrincipal(Staff staff) {
        staff.teacherStatistics.setStaffType(StaffType.PRINCIPAL);
    }

    public static void assignVicePrincipal(HashMap<Integer, Staff> staffHashMap) {
        Staff teacher = selectRandomTeacher(staffHashMap);
        teacher.teacherStatistics.setStaffType(StaffType.VICE_PRINCIPAL);
        System.out.println("Staff " + teacher.teacherName.getFirstName() + " assigned as vice principal!");
    }

    public static void assignVicePrincipal(Staff staff) {
        staff.teacherStatistics.setStaffType(StaffType.VICE_PRINCIPAL);
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
