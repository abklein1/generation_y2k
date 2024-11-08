package utility;

import entity.ShoulderBag;
import entity.Body.TeacherArms;
import entity.Body.TeacherLegs;
import entity.Body.TeacherUpperT;

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
