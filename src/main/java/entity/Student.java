package entity;

import entity.Body.StudentArms;
import entity.Body.StudentLegs;
import entity.Body.StudentUpperT;
import utility.StudentFactory;
import utility.StudentName;
import utility.StudentStatistics;

import java.io.Serializable;

public class Student implements Serializable {

    private final StudentUpperT studentUpperT;
    private final StudentLegs studentLegs;
    private final StudentArms studentArms;
    private final Backpack backpack;
    public StudentName studentName;
    public StudentStatistics studentStatistics;
    StudentFactory studentFactory = new StudentFactory();

    public Student() {
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
