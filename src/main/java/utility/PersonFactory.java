package utility;

import entity.Body.Arms;
import entity.Body.Carry;
import entity.Body.Legs;
import entity.Body.UpperTorso;

import java.io.Serializable;

public interface PersonFactory extends Serializable {
    PName createName();

    UpperTorso createUpperTorso();

    Legs createLegs();

    Arms createArms();

    Carry createCarry();

    PStatistics setStats();
}
