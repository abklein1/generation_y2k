package behavior.leaf.student;

import behavior.BehaviorContext;
import behavior.leaf.ConditionNode;
import entity.Student;
import utility.AssessmentService;

/**
 * Succeeds when the student's current class has an assessment in progress.
 * Placed at the top of the in-class selector so quizzes, tests, exams, and
 * project days suppress every social option.
 */
public class HasAssessmentNowCondition extends ConditionNode {

    public HasAssessmentNowCondition() {
        super("HasAssessmentNow");
    }

    @Override
    public boolean check(BehaviorContext context) {
        Student student = context.getStudent();
        if (student == null || student.getEntityState() == null
                || !student.getEntityState().isInClass()) {
            return false;
        }
        return AssessmentService.getActiveAssessmentFor(student, context.getTime()) != null;
    }
}
