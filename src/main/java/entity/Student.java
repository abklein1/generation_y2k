package entity;

import utility.StudentName;
import utility.StudentStatistics;
import utility.StudentFactory;
public class Student {

    public StudentName studentName;
    public StudentStatistics studentStatistics;

    StudentFactory studentFactory = new StudentFactory();
    
    public Student(){
        studentName = studentFactory.createName();
        StudentUpperT studentUpperT = studentFactory.createUpperTorso();
        StudentLegs studentLegs = studentFactory.createLegs();
        StudentArms studentArms = studentFactory.createArms();
        Backpack backpack = studentFactory.createCarry();
        studentStatistics = studentFactory.setStats();
    }
}
