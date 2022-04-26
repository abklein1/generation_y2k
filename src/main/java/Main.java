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
        HashMap<Integer, Staff> staffHashMap = new HashMap<Integer,Staff>();
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
        for(Integer i = 0; i < student_cap; i++){
            studentHashMap.put(i, new Student());
        }
        //Store staff objects in another hashmap
        for(Integer j = 0; j < staff_cap; j++){
            staffHashMap.put(j, new Staff());
        }
        //Set for student randomization
        System.out.println("Randomizing " + student_cap + " students...");
        fNameReference.putAll(NameLoader.readCSVFirst(true));
        lNameReference.putAll(NameLoader.readCSVLast(true));
        for(Integer k = 0; k < student_cap; k++){
            String f_name = fNameReference.get(setRandom(0,fNameReference.size() - 1));
            String l_name = lNameReference.get(setRandom(0, lNameReference.size() -1));
            System.out.println("   Generating student " + f_name + " " + l_name);
            Student student = studentHashMap.get(k);
            student.studentName.setFirstName(f_name);
            student.studentName.setLastName(l_name);
            student.studentStatistics.setHeight(setRandom(48,78));
            student.studentStatistics.setIntelligence(setRandom(0,20));
            student.studentStatistics.setCharisma(setRandom(0,20));
            student.studentStatistics.setAgility(setRandom(0,20));
            student.studentStatistics.setDetermination(setRandom(0,20));
            student.studentStatistics.setStrength(setRandom(0,20));
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
        for(Integer l = 0; l < staff_cap; l++){
            String f_name = fNameReference.get(setRandom(0, fNameReference.size() - 1));
            String l_name = lNameReference.get(setRandom(0, lNameReference.size() -1));
            System.out.println("   Generating staff " + f_name + " " + l_name);
            Staff staff = staffHashMap.get(l);
            staff.teacherName.setFirstName(f_name);
            staff.teacherName.setLastName(l_name);
            staff.teacherStatistics.setHeight(setRandom(50,84));
            staff.teacherStatistics.setIntelligence(setRandom(2,22));
            staff.teacherStatistics.setCharisma(setRandom(1,22));
            staff.teacherStatistics.setAgility(setRandom(1,19));
            staff.teacherStatistics.setDetermination(setRandom(1,20));
            staff.teacherStatistics.setStrength(setRandom(1,20));
        }

    }

    private static Integer setRandom(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max + 1);
        return random;
    }

}