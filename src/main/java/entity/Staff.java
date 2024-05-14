package entity;

import utility.TeacherName;
import utility.TeacherStatistics;
import utility.TeacherFactory;

import java.io.Serializable;

public class Staff implements Serializable {

    public TeacherName teacherName;
    private final TeacherUpperT teacherUpperT;
    private final TeacherLegs teacherLegs;
    private final TeacherArms teacherArms;
    private final ShoulderBag shoulderBag;
    public TeacherStatistics teacherStatistics;

    TeacherFactory teacherFactory = new TeacherFactory();

    public Staff(){
        teacherName = teacherFactory.createName();
        teacherUpperT = teacherFactory.createUpperTorso();
        teacherLegs = teacherFactory.createLegs();
        teacherArms = teacherFactory.createArms();
        shoulderBag = teacherFactory.createCarry();
        teacherStatistics = teacherFactory.setStats();
    }

}
