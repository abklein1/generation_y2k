package game;
/*
    File: game.Main.java
    Author: Alex Klein
    Date: 04/13/2022
    Description: Here is the driver for the program
 */

//TODO: Optimize Imports

import utility.SchoolController;
import view.GameView;

import javax.swing.*;

public class Main {
    //TODO: Create seed ingestion for recreating specific schools or scenarios
    public static void main(String[] args) {

        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Windows".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
        }

        SwingUtilities.invokeLater(() -> {
            GameView view = new GameView();
            SchoolController controller = new SchoolController(view);
        });
    }
}