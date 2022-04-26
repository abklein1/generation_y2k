/*
    File: Main.java
    Author: Alex Klein
    Date: 04/13/2022
    Description: Here is the driver for the program

 */

public class Main {
    public static void main(String[] args) {

        System.out.println("Starting by generating the school");
        StandardSchool standardSchool = new StandardSchool();
        Director director = new Director(standardSchool);

        int student_cap = standardSchool.getTotalStudentCapacity();
        int staff_cap = standardSchool.getMinimumStaffRequirements();

        System.out.println("Student capacity is " + student_cap);
        System.out.println("Staff capacity is " + staff_cap);

        NameLoader.readCSVFirst(true);
        NameLoader.readCSVLast(true);
    }
}