package utility;

import entity.PlayerCharacter;
import javax.swing.JTextArea;

public class PlayerStoryGenerator {
    public static void generateStory(PlayerCharacter playerCharacter, JTextArea storyOutput) {
        initialBirthMessage(playerCharacter, storyOutput);
    }

    public static void initialBirthMessage(PlayerCharacter playerCharacter, JTextArea storyOutput) {
        storyOutput.append("You are born on " + playerCharacter.studentStatistics.getBirthday().toString() + " to parents " + playerCharacter.getFamilyInfo().getMother().getFirstName() + " and " + playerCharacter.getFamilyInfo().getFather().getFirstName() + ".\n");
    }

    public static void reportBaseStats(PlayerCharacter playerCharacter, JTextArea storyOutput) {
        storyOutput.append("Strength: " + playerCharacter.studentStatistics.getStrength() + "\n");
        storyOutput.append("Intelligence: " + playerCharacter.studentStatistics.getIntelligence() + "\n");
        storyOutput.append("Charisma: " + playerCharacter.studentStatistics.getCharisma() + "\n");
        storyOutput.append("Agility: " + playerCharacter.studentStatistics.getAgility() + "\n");
        storyOutput.append("Determination: " + playerCharacter.studentStatistics.getDetermination() + "\n");
        storyOutput.append("Perception: " + playerCharacter.studentStatistics.getPerception() + "\n");
        storyOutput.append("Luck: " + playerCharacter.studentStatistics.getLuck() + "\n");
    }
}
