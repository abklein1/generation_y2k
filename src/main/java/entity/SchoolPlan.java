package entity;//*******************************************************************
//  entity.SchoolPlan.java
//  Description: This is a school plan interface that defines what a school
//  should be built from
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import view.GameView;

public interface SchoolPlan {
    void setArtStudios(int number, GameView view);
    void setAthleticFields(int number, GameView view);
    void setAuditoriums(int number, GameView view);
    void setBathrooms(int number, GameView view);

    void setBreakrooms(int number, GameView view);

    void setClassrooms(int number, GameView view);

    void setCourtyards(int number, GameView view);

    void setComputerLabs(int number, GameView view);
    void setDramaRooms(int number, GameView view);

    void setGyms(int number, GameView view);

    void setHallways(int number, GameView view);

    void setLibraries(int number, GameView view);
    void setLockerRooms(int number, GameView view);

    void setLunchrooms(int number, GameView view);
    void setMusicRooms(int number, GameView view);

    void setOffices(int number, GameView view);
    void setScienceLabs(int number, GameView view);

    void setUtilityRooms(int number, GameView view);
}
