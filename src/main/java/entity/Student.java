package entity;

import utility.StudentName;
import utility.StudentStatistics;
import utility.StudentFactory;

import java.io.Serializable;

public class Student implements Serializable {

    public StudentName studentName;
    public StudentStatistics studentStatistics;
    private final StudentUpperT studentUpperT;
    private final StudentLegs studentLegs;
    private final StudentArms studentArms;
    private final Backpack backpack;

    StudentFactory studentFactory = new StudentFactory();
    
    public Student(){
        studentName = studentFactory.createName();
        studentUpperT = studentFactory.createUpperTorso();
        studentLegs = studentFactory.createLegs();
        studentArms = studentFactory.createArms();
        backpack = studentFactory.createCarry();
        studentStatistics = studentFactory.setStats();
    }

    @Override
    public String toString() {
        return studentName.getFirstName() + " " + studentName.getLastName();
    }
}
