package entity;

import entity.Body.TeacherArms;
import entity.Body.TeacherLegs;
import entity.Body.TeacherUpperT;
import utility.TeacherFactory;
import utility.TeacherName;
import utility.TeacherStatistics;

import java.io.Serializable;

public class Staff implements Serializable {

    private final TeacherUpperT teacherUpperT;
    private final TeacherLegs teacherLegs;
    private final TeacherArms teacherArms;
    private final ShoulderBag shoulderBag;
    public TeacherName teacherName;
    public TeacherStatistics teacherStatistics;

    TeacherFactory teacherFactory = new TeacherFactory();

    public Staff() {
        teacherName = teacherFactory.createName();
        teacherUpperT = teacherFactory.createUpperTorso();
        teacherLegs = teacherFactory.createLegs();
        teacherArms = teacherFactory.createArms();
        shoulderBag = teacherFactory.createCarry();
        teacherStatistics = teacherFactory.setStats();
    }

    @Override
    public String toString() {
        return teacherName.getFirstName() + " " + teacherName.getLastName();
    }

}
