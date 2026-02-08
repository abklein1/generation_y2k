package utility;

import entity.FamilyInfo;
import entity.LifeHistory;
import entity.PlayerCharacter;
import entity.SiblingInfo;

import javax.swing.JTextArea;
import java.time.LocalDate;

/**
 * Generates and displays a player character's life history.
 *
 * The history is built from the character's birth through to the start
 * of the game (summer 2004). Events are displayed chronologically,
 * grouped by year:
 *
 * 1989
 * September 15 - You are born to parents Sarah and Michael.
 *
 * 1992
 * May 15 - Your brother, James, is born.
 *
 * Events include fixed milestones (birth, sibling births) and
 * random life events that occur with configurable probability.
 * Designers can add new event types by extending generateRandomEvents().
 */
public class PlayerStoryGenerator {

    /**
     * Builds the full life history for a player character and appends
     * it to the story output text area, formatted year by year.
     */
    public static void generateStory(PlayerCharacter playerCharacter, JTextArea storyOutput) {
        LifeHistory history = buildLifeHistory(playerCharacter);
        history.appendToTextArea(storyOutput);
    }

    /**
     * Builds a LifeHistory from a player character's data.
     * This is the main entry point for history generation.
     *
     * @param player the player character with family info populated
     * @return a LifeHistory containing all events from birth to game start
     */
    public static LifeHistory buildLifeHistory(PlayerCharacter player) {
        LifeHistory history = new LifeHistory();

        addBirthEvent(history, player);
        addSiblingBirthEvents(history, player);

        // ---------------------------------------------------------------
        // Random life events hook: add calls here as you write them.
        // Each method should check its own probability before adding
        // events. Events do not need to happen on any regular schedule.
        //
        // Example (future):
        // generateDivorceEvent(history, player);
        // generateMovingEvent(history, player);
        // generateFamilyEvent(history, player);
        // ---------------------------------------------------------------

        history.sort();
        return history;
    }

    // ------------------------------------------------------------------
    // Fixed events (always generated)
    // ------------------------------------------------------------------

    /**
     * Adds the player's birth event. Always occurs.
     */
    private static void addBirthEvent(LifeHistory history, PlayerCharacter player) {
        LocalDate birthday = player.studentStatistics.getBirthday();
        FamilyInfo family = player.getFamilyInfo();

        String motherName = family.getMother().getFirstName();
        String fatherName = family.getFather().getFirstName();

        history.addEvent(birthday,
                "You are born to parents " + motherName + " and " + fatherName + ".");
    }

    /**
     * Adds birth events for each sibling. These use the sibling's
     * actual generated birthday and gender to produce lines like:
     * "Your brother, James, is born."
     * "Your sister, Emily, is born."
     */
    private static void addSiblingBirthEvents(LifeHistory history, PlayerCharacter player) {
        FamilyInfo family = player.getFamilyInfo();
        if (family.getSiblings() == null) {
            return;
        }

        for (SiblingInfo sibling : family.getSiblings()) {
            String relation = sibling.getRelationLabel();
            String name = sibling.getFirstName();

            history.addEvent(sibling.getBirthday(),
                    "Your " + relation + ", " + name + ", is born.");
        }
    }

    // ------------------------------------------------------------------
    // Base stats display (unchanged, separate from history)
    // ------------------------------------------------------------------

    /**
     * Appends the player's base stats to the text area.
     * This is displayed separately from the life history.
     */
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
