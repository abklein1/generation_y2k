package utility;

import entity.Body.Arms;
import entity.Body.Carry;
import entity.Body.Legs;
import entity.Body.UpperTorso;

public interface PersonFactory {
    PName createName();

    UpperTorso createUpperTorso();

    Legs createLegs();

    Arms createArms();

    Carry createCarry();

    PStatistics setStats();
}
