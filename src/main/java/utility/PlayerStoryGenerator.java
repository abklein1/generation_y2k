package utility;

import entity.PlayerCharacter;
import javax.swing.JTextArea;

public class PlayerStoryGenerator {
    public static void generateStory(PlayerCharacter playerCharacter, JTextArea storyOutput) {
        initialBirthMessage(playerCharacter, storyOutput);
    }

    public static void initialBirthMessage(PlayerCharacter playerCharacter, JTextArea storyOutput) {
        storyOutput.append("You are born on " + playerCharacter.studentStatistics.getBirthday().toString() + ".\n");
    }
}
