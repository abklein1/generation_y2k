package utility;

import entity.Backpack;
import entity.StudentArms;
import entity.StudentLegs;
import entity.StudentUpperT;

public class StudentFactory implements PersonFactory {
    @Override
    public StudentName createName() {
        return new StudentName();
    }

    @Override
    public StudentUpperT createUpperTorso() {
        return new StudentUpperT();
    }

    @Override
    public StudentLegs createLegs() {
        return new StudentLegs();
    }

    @Override
    public StudentArms createArms() {
        return new StudentArms();
    }

    @Override
    public Backpack createCarry() {
        return new Backpack();
    }

    @Override
    public StudentStatistics setStats() {
        return new StudentStatistics();
    }
}
