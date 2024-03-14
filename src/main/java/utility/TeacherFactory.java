package utility;

import entity.ShoulderBag;
import entity.TeacherArms;
import entity.TeacherLegs;
import entity.TeacherUpperT;

public class TeacherFactory implements PersonFactory {
    @Override
    public TeacherName createName() {
        return new TeacherName();
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
