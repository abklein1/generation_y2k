package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.Student;

import static constants.SimConstants.JEALOUSY_SABOTAGE_EMPATHY_MAX;
import static constants.SimConstants.JEALOUSY_SABOTAGE_RESPONSIBILITY_MAX;

/**
 * Condition that decides how a jealous student responds to a rival:
 * students whose empathy and responsibility are both low would rather tear
 * the rival down (badmouthing) than win their crush over fairly.
 *
 * <p>Both stats are drainable secondary stats checked at tick time, so a
 * student who has been socially worn down over the school day can cross the
 * sabotage line by last period even if they started the morning above it.</p>
 */
public class IsSabotageInclinedCondition extends ConditionNode {

    public IsSabotageInclinedCondition() {
        super("IsSabotageInclined");
    }

    @Override
    public boolean check(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        return student.studentStatistics.getEmpathy() < JEALOUSY_SABOTAGE_EMPATHY_MAX
                && student.studentStatistics.getResponsibility() < JEALOUSY_SABOTAGE_RESPONSIBILITY_MAX;
    }
}
