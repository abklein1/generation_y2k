package utility;

import entity.ShoulderBag;
import entity.Body.TeacherArms;
import entity.Body.TeacherHead;
import entity.Body.TeacherLegs;
import entity.Body.TeacherUpperT;

public class TeacherFactory implements PersonFactory {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public TeacherName createName() {
        return new TeacherName();
    }

    @Override
    public TeacherHead createHead() {
        return new TeacherHead();
    }

    @Override
    public TeacherUpperT createUpperTorso() {
        return new TeacherUpperT();
    }

    @Override
    public TeacherLegs createLegs() {
        return new TeacherLegs();
    }

    @Override
    public TeacherArms createArms() {
        return new TeacherArms();
    }

    @Override
    public ShoulderBag createCarry() {
        return new ShoulderBag();
    }

    @Override
    public TeacherStatistics setStats() {
        return new TeacherStatistics();
    }
}
