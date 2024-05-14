package game;
/*
    File: game.Main.java
    Author: Alex Klein
    Date: 04/13/2022
    Description: Here is the driver for the program
 */

//TODO: Optimize Imports

import entity.*;
import utility.*;
import view.GameView;

import java.util.HashMap;

import static utility.BossUtility.bossDecision;
import static utility.BossUtility.dungeonFight;
import static utility.Randomizer.setRandom;

public class Main {
    //TODO: Create seed ingestion for recreating specific schools or scenarios
    //TODO: Just a main run to see that the sim works. Revise this to make actual game loop
    public static void main(String[] args) {

        GameView view = new GameView();
        SchoolController controller = new SchoolController(view);
    }
}