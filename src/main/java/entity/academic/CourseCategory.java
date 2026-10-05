package entity.academic;

import utility.SectionManager;

/**
 * Broad class-structure buckets used to pick a lesson-plan template. Core
 * classes get a quiz/test/exam structure; PE and electives are graded mostly
 * on performance (participation, projects, skills evaluations).
 */
public enum CourseCategory {

    CORE_ACADEMIC,
    WORLD_LANGUAGE,
    PHYSICAL_EDUCATION,
    VISUAL_ARTS,
    PERFORMING_ARTS,
    COMPUTER_SCIENCE,
    VOCATIONAL,
    BUSINESS,
    CONSUMER_SCIENCE,
    GENERAL;

    /**
     * Infers the category from a scheduled class name using the same keyword
     * matching as section/staff assignment.
     *
     * @param className the scheduled class name (e.g. "Algebra I", "Band")
     * @return the matching category, or GENERAL when nothing matches
     */
    public static CourseCategory fromClassName(String className) {
        if (className == null || className.isBlank()) {
            return GENERAL;
        }
        if (SectionManager.belongsToSubjectArea(className, "physical education")) {
            return PHYSICAL_EDUCATION;
        }
        if (SectionManager.belongsToSubjectArea(className, "language")) {
            return WORLD_LANGUAGE;
        }
        if (SectionManager.belongsToSubjectArea(className, "visual arts")) {
            return VISUAL_ARTS;
        }
        if (SectionManager.belongsToSubjectArea(className, "performing arts")) {
            return PERFORMING_ARTS;
        }
        if (SectionManager.belongsToSubjectArea(className, "computer science")) {
            return COMPUTER_SCIENCE;
        }
        if (SectionManager.belongsToSubjectArea(className, "vocational")) {
            return VOCATIONAL;
        }
        if (SectionManager.belongsToSubjectArea(className, "business")) {
            return BUSINESS;
        }
        if (SectionManager.belongsToSubjectArea(className, "consumer science")) {
            return CONSUMER_SCIENCE;
        }
        if (SectionManager.isCoreSubject(className)
                || SectionManager.belongsToSubjectArea(className, "english")
                || SectionManager.belongsToSubjectArea(className, "math")
                || SectionManager.belongsToSubjectArea(className, "science")
                || SectionManager.belongsToSubjectArea(className, "history")) {
            return CORE_ACADEMIC;
        }
        return GENERAL;
    }
}
