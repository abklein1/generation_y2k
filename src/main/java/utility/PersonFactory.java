package utility;

import entity.Arms;
import entity.Carry;
import entity.Legs;
import entity.UpperTorso;

public interface PersonFactory {
    PName createName();

    UpperTorso createUpperTorso();

    Legs createLegs();

    Arms createArms();

    Carry createCarry();

    PStatistics setStats();
}
