public interface PersonFactory {
    PName createName();

    UpperTorso createUpperTorso();

    Legs createLegs();

    Arms createArms();

    Carry createCarry();

    PStatistics setStats();
}
