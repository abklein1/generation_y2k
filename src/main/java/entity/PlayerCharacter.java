package entity;

import entity.Body.StudentArms;
import entity.Body.StudentLegs;
import entity.Body.StudentUpperT;
import utility.StudentFactory;
import utility.StudentName;
import utility.StudentStatistics;

public class PlayerCharacter extends Student {

    private int siblingsNumber;

    public PlayerCharacter() {
        super();
    }

    @Override
    public String toString() {
        this.siblingsNumber = 0;
        return super.toString();
    }

    public void setSiblings(int siblings) {
        this.siblingsNumber = siblings;
    }

    public int getSiblings() {
        return this.siblingsNumber;
    }
}
