package entity;

import utility.StudentName;
import utility.StudentStatistics;
import utility.StudentFactory;
public class Student {

    public StudentName studentName;
    private final StudentUpperT studentUpperT;
    private final StudentLegs studentLegs;
    private final StudentArms studentArms;
    private final Backpack backpack;
    public StudentStatistics studentStatistics;

    StudentFactory studentFactory = new StudentFactory();
    
    public Student(){
        studentName = studentFactory.createName();
        studentUpperT = studentFactory.createUpperTorso();
        studentLegs = studentFactory.createLegs();
        studentArms = studentFactory.createArms();
        backpack = studentFactory.createCarry();
        studentStatistics = studentFactory.setStats();
    }
}
