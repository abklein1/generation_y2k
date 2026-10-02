package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.StudentBehaviorTreeBuilder;
import behavior.leaf.ConditionNode;
import entity.EntityState;
import entity.RomanticStatus;
import entity.Student;
import utility.GameRandom;
import utility.RomanceUpdater;
import utility.SocialLinkConnector;

import java.util.List;

import static constants.SimConstants.ROMANCE_JEALOUSY_ACT_CHANCE;

/**
 * Condition gating the jealous-drama branch: succeeds when the student holds
 * a (non-hidden) crush on a co-located peer and knows that crush is in a
 * still-observable couple with someone else.
 *
 * <p>Even then the student only acts some of the time: an act-frequency gate
 * scaled by initiative ({@code ROMANCE_JEALOUSY_ACT_CHANCE * (0.5 +
 * initiative / 100)}) keeps jealous students from monomaniacally obsessing.
 * Driven students scheme most decisions; passive students mostly stew and
 * leave the resentment to the passive jealousy drip in
 * {@link RomanceUpdater}.</p>
 *
 * <p>On success the chosen crush and rival are published to the behavior
 * context as {@code jealousy_crush} / {@code jealousy_rival} for the action
 * nodes downstream in the same sequence to consume.</p>
 */
public class HasJealousRivalCondition extends ConditionNode {

    public HasJealousRivalCondition() {
        super("HasJealousRival");
    }

    @Override
    public boolean check(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null || student.getEntityState() == null) {
            return false;
        }
        SocialLinkConnector connector = context.getSocialLinkConnector();
        if (connector == null) {
            return false;
        }

        EntityState state = student.getEntityState();
        List<Student> coLocated = StudentBehaviorTreeBuilder.collectCoLocatedPeers(student, state);
        if (coLocated.isEmpty()) {
            return false;
        }

        for (Student crush : connector.getRomanticInterests(student)) {
            if (connector.getRomanticStatus(student, crush) != RomanticStatus.CRUSH) {
                continue;
            }
            // Acting on a hidden same-gender crush would out a closeted student
            if (RomanceUpdater.isSecretCrush(student, crush)) {
                continue;
            }
            if (!coLocated.contains(crush)) {
                continue;
            }
            List<Student> rivals = connector.getKnownPartnersOf(student, crush);
            if (rivals.isEmpty()) {
                continue;
            }
            // A rival exists and the crush is in reach: roll whether the
            // student feels like doing something about it this decision.
            double actChance = ROMANCE_JEALOUSY_ACT_CHANCE
                    * (0.5 + student.studentStatistics.getInitiative() / 100.0);
            if (GameRandom.nextDouble() >= actChance) {
                return false;
            }
            context.setVariable("jealousy_crush", crush);
            context.setVariable("jealousy_rival", rivals.get(0));
            return true;
        }
        return false;
    }
}
