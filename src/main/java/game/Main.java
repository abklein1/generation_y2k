package game;
/*
    File: game.Main.java
    Author: Alex Klein
    Date: 04/13/2022
    Description: Here is the driver for the program
 */

import utility.SchoolController;
import view.GameView;

import javax.swing.*;

public class Main {
    // Seed-based random generation is now supported via GameRandom class
    // The seed is displayed in the console when generating a school
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