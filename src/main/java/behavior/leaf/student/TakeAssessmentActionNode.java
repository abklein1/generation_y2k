package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;
import behavior.leaf.ActionNode;
import constants.SimConstants;
import entity.ActivityType;
import entity.EntityState;
import entity.Student;
import entity.academic.ScheduledAssessment;
import utility.AssessmentService;

/**
 * The student sits the in-progress assessment: one minute of seat time is
 * recorded per tick (used at grading time to detect absentees), focus pools
 * drain slowly, and entertainment drops — tests are no fun.
 */
public class TakeAssessmentActionNode extends ActionNode {

    public TakeAssessmentActionNode() {
        super("TakeAssessment", 1);
    }

    @Override
    public boolean canExecute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null) {
            return false;
        }
        EntityState state = student.getEntityState();
        return state != null && state.isInClass();
    }

    @Override
    public BehaviorStatus execute(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null || student.studentStatistics == null) {
            return BehaviorStatus.FAILURE;
        }
        EntityState state = student.getEntityState();
        if (state == null) {
            return BehaviorStatus.FAILURE;
        }

        ScheduledAssessment assessment =
                AssessmentService.getActiveAssessmentFor(student, context.getTime());
        if (assessment == null) {
            return BehaviorStatus.FAILURE;
        }

        state.setCurrentActivity(ActivityType.TAKING_ASSESSMENT);
        student.studentStatistics.getAcademicRecord()
                .getOrCreateAttempt(assessment.getId())
                .recordMinute();

        student.studentStatistics.drainSecondaryStat("initiative",
                SimConstants.ASSESSMENT_INITIATIVE_DRAIN,
                SimConstants.ALLOSTATIC_STRESS_FACTOR_INITIATIVE);
        student.studentStatistics.drainSecondaryStat("responsibility",
                SimConstants.ASSESSMENT_RESPONSIBILITY_DRAIN,
                SimConstants.ALLOSTATIC_STRESS_FACTOR_RESPONSIBILITY);
        state.setEntertainment(state.getEntertainment()
                - SimConstants.ASSESSMENT_ENTERTAINMENT_DRAIN);

        return BehaviorStatus.SUCCESS;
    }
}
