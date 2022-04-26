/*
    File: Main.java
    Author: Alex Klein
    Date: 04/13/2022
    Description: Here is the driver for the program

 */


import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        //Create hash maps for storage
        HashMap<Integer, Student> studentHashMap = new HashMap<Integer, Student>();
        HashMap<Integer, Staff> staffHashMap = new HashMap<Integer, Staff>();
        HashMap<Integer, String> fNameReference = new HashMap<Integer, String>();
        HashMap<Integer, String> lNameReference = new HashMap<Integer, String>();
        int student_cap;
        int staff_cap;
        //Generate a new standard school with rooms
        System.out.println("Starting by generating the school");
        StandardSchool standardSchool = new StandardSchool();
        Director director = new Director(standardSchool);
        //Pull capacities
        student_cap = standardSchool.getTotalStudentCapacity();
        staff_cap = standardSchool.getMinimumStaffRequirements();

        System.out.println("Student capacity is " + student_cap);
        System.out.println("Staff capacity is " + staff_cap);

        System.out.println("Populating school...");
        //Store student objects in hashmap
        for (Integer i = 0; i < student_cap; i++) {
            studentHashMap.put(i, new Student());
        }
        //Store staff objects in another hashmap
        for (Integer j = 0; j < staff_cap; j++) {
            staffHashMap.put(j, new Staff());
        }
        //Set for student randomization
        System.out.println("Randomizing " + student_cap + " students...");
        fNameReference.putAll(NameLoader.readCSVFirst(true));
        lNameReference.putAll(NameLoader.readCSVLast(true));
        for (Integer k = 0; k < student_cap; k++) {
            String f_name = fNameReference.get(setRandom(0, fNameReference.size() - 1));
            String l_name = lNameReference.get(setRandom(0, lNameReference.size() - 1));
            System.out.println("   Generating student " + f_name + " " + l_name);
            Student student = studentHashMap.get(k);
            student.studentName.setFirstName(f_name);
            student.studentName.setLastName(l_name);
            student.studentStatistics.setHairColor(hairSelection(setRandom(0, 102)));
            student.studentStatistics.setEyeColor(eyeSelection(setRandom(0, 109)));
            student.studentStatistics.setHeight(setRandom(48, 78));
            student.studentStatistics.setIntelligence(setRandom(0, 20));
            student.studentStatistics.setCharisma(setRandom(0, 20));
            student.studentStatistics.setAgility(setRandom(0, 20));
            student.studentStatistics.setDetermination(setRandom(0, 20));
            student.studentStatistics.setStrength(setRandom(0, 20));
            student.studentStatistics.setLevel(1);
            student.studentStatistics.setExperience(0);
        }
        //Clear map for new values
        fNameReference.clear();
        lNameReference.clear();
        //Set for staff randomization
        System.out.println("Randomizing " + staff_cap + " staff");
        fNameReference.putAll(NameLoader.readCSVFirst(false));
        lNameReference.putAll(NameLoader.readCSVLast(false));
        for (Integer l = 0; l < staff_cap; l++) {
            String f_name = fNameReference.get(setRandom(0, fNameReference.size() - 1));
            String l_name = lNameReference.get(setRandom(0, lNameReference.size() - 1));
            System.out.println("   Generating staff " + f_name + " " + l_name);
            Staff staff = staffHashMap.get(l);
            staff.teacherName.setFirstName(f_name);
            staff.teacherName.setLastName(l_name);
            staff.teacherStatistics.setHairColor(hairSelection(setRandom(0, 102)));
            staff.teacherStatistics.setEyeColor(eyeSelection(setRandom(0, 109)));
            staff.teacherStatistics.setHeight(setRandom(50, 84));
            staff.teacherStatistics.setIntelligence(setRandom(2, 22));
            staff.teacherStatistics.setCharisma(setRandom(1, 22));
            staff.teacherStatistics.setAgility(setRandom(1, 19));
            staff.teacherStatistics.setDetermination(setRandom(1, 20));
            staff.teacherStatistics.setStrength(setRandom(1, 20));
        }
        System.out.println("Done creating school and students");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        //Introduce me to random student
        System.out.println("Introduce me to a random student, please.");
        studentInspection(studentHashMap.get(setRandom(0, studentHashMap.size() - 1)));
        //Introduce me to a random teacher
        System.out.println("Ok now introduce me to a random teacher, please.");
        staffInspection(staffHashMap.get(setRandom(0, staffHashMap.size() - 1)));

    }

    //Not ideal design but need to add a few helpers here for simulation
    private static Integer setRandom(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max + 1);
        return random;
    }

    private static String hairSelection(int selection) {
        if (selection >= 0 && selection <= 21) {
            return "Black";
        } else if (selection >= 22 && selection <= 37) {
            return "Dark Brown";
        } else if (selection >= 38 && selection <= 48) {
            return "Medium Brown";
        } else if (selection >= 49 && selection <= 56) {
            return "Light Brown";
        } else if (selection >= 57 && selection <= 64) {
            return "Blond";
        } else if (selection >= 65 && selection <= 71) {
            return "Chestnut";
        } else if (selection >= 72 && selection <= 78) {
            return "Mahogany";
        } else if (selection >= 79 && selection <= 84) {
            return "Dirty Blond";
        } else if (selection >= 85 && selection <= 89) {
            return "Golden Blond";
        } else if (selection >= 90 && selection <= 93) {
            return "Light Blond";
        } else if (selection >= 94 && selection < 96) {
            return "Golden Brown";
        } else if (selection == 97) {
            return "Caramel";
        } else if (selection == 98) {
            return "Strawberry Blond";
        } else if (selection == 99) {
            return "Copper";
        } else if (selection == 100) {
            return "Red";
        } else if (selection == 101) {
            return "Platinum Blond";
        } else {
            int random = setRandom(0, 6);
            if (random == 0) {
                return "Auburn";
            } else if (random == 1) {
                return "Amber";
            } else if (random == 2) {
                return "Titian";
            } else if (random == 3) {
                return "White";
            } else if (random == 4) {
                return "No";
            } else if (random == 5) {
                return "Gray";
            } else {
                return "Champagne";
            }
        }
    }

    private static String eyeSelection(int selection) {
        if (selection >= 0 && selection <= 52) {
            return "Dark Brown";
        } else if (selection >= 53 && selection <= 75) {
            return "Light Brown";
        } else if (selection >= 76 && selection <= 83) {
            return "Blue";
        } else if (selection >= 84 && selection <= 90) {
            return "Light Blue";
        } else if (selection >= 91 && selection <= 96) {
            return "Hazel";
        } else if (selection >= 97 && selection <= 102) {
            return "Amber";
        } else if (selection >= 103 && selection <= 105) {
            return "Green";
        } else if (selection == 106) {
            return "Gray";
        } else if (selection == 107) {
            return "Violet";
        } else if (selection == 108) {
            return "Black";
        } else {
            return "Heterochromia";
        }
    }

    private static void studentInspection(Student student) {
        String fir = student.studentName.getFirstName();
        String las = student.studentName.getLastName();
        String h = student.studentStatistics.getHairColor();
        String e = student.studentStatistics.getEyeColor();
        int hei = student.studentStatistics.getHeight();
        int in = student.studentStatistics.getIntelligence();
        int chr = student.studentStatistics.getCharisma();
        int agl = student.studentStatistics.getAgility();
        int det = student.studentStatistics.getDetermination();
        int str = student.studentStatistics.getStrength();
        int bor = student.studentStatistics.getBoredom();
        boolean slp = student.studentStatistics.getSleepState();
        int exp = student.studentStatistics.getExperience();

        System.out.println(fir + " " + las);
        System.out.println("=====================================");
        System.out.println(fir + " has " + h + " hair and " + e + " eyes. They are " + hei + " inches tall.");
        System.out.println("They have the following stats: ");
        System.out.println("   INT: " + in);
        System.out.println("   CHR: " + chr);
        System.out.println("   AGL: " + agl);
        System.out.println("   DET: " + det);
        System.out.println("   STR: " + str);
        System.out.println("   EXP: " + exp);
        System.out.println(fir + " has the following status effects: ");
        if (bor == 0) {
            System.out.println(fir + " is not bored");
        } else {
            System.out.println(fir + " is slightly bored");
        }
        if (slp) {
            System.out.println(fir + " is asleep!");
        } else {
            System.out.println(fir + " is not asleep");
        }
        System.out.println("Nice to meet you " + fir + "!");
    }


    private static void staffInspection(Staff staff) {
        String fir = staff.teacherName.getFirstName();
        String las = staff.teacherName.getLastName();
        String h = staff.teacherStatistics.getHairColor();
        String e = staff.teacherStatistics.getEyeColor();
        int hei = staff.teacherStatistics.getHeight();
        int in = staff.teacherStatistics.getIntelligence();
        int chr = staff.teacherStatistics.getCharisma();
        int agl = staff.teacherStatistics.getAgility();
        int det = staff.teacherStatistics.getDetermination();
        int str = staff.teacherStatistics.getStrength();
        int bor = staff.teacherStatistics.getBoredom();
        boolean slp = staff.teacherStatistics.getSleepState();

        System.out.println(fir + " " + las);
        System.out.println("=====================================");
        System.out.println(fir + " has " + h + " hair and " + e + " eyes. They are " + hei + " inches tall.");
        System.out.println("They have the following stats: ");
        System.out.println("   INT: " + in);
        System.out.println("   CHR: " + chr);
        System.out.println("   AGL: " + agl);
        System.out.println("   DET: " + det);
        System.out.println("   STR: " + str);
        System.out.println(fir + " has the following status effects: ");
        if (bor == 0) {
            System.out.println(fir + " is not bored");
        } else {
            System.out.println(fir + " is slightly bored");
        }
        if (slp) {
            System.out.println(fir + " is asleep!");
        } else {
            System.out.println(fir + " is not asleep");
        }
        System.out.println("Nice to meet you " + fir + "!");
    }

}