package entity;//*******************************************************************
//  entity.SchoolPlan.java
//  Description: This is a school plan interface that defines what a school
//  should be built from
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

public interface SchoolPlan {
    void setArtStudios(int number);
    void setAthleticFields(int number);
    void setAuditoriums(int number);
    void setBathrooms(int number);

    void setBreakrooms(int number);

    void setClassrooms(int number);

    void setCourtyards(int number);

    void setComputerLabs(int number);
    void setDramaRooms(int number);

    void setGyms(int number);

    void setHallways(int number);

    void setLibraries(int number);
    void setLockerRooms(int number);

    void setLunchrooms(int number);
    void setMusicRooms(int number);

    void setOffices(int number);
    void setScienceLabs(int number);

    void setUtilityRooms(int number);
}
