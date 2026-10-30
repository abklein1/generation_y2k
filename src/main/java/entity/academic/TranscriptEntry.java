package entity.academic;

import java.io.Serializable;

/**
 * One finalized course result on a student's permanent transcript: the class,
 * the school year and semester it was taken, the final percent and letter
 * grade, and the grade points it contributes to GPA (both unweighted and with
 * the AP/Honors bonus applied).
 */
public class TranscriptEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Start year of the school year, e.g. 2001 means the 2001-02 year. */
    private final int schoolYear;
    private final String gradeLevel;
    private final String semester;
    private final String className;
    private final String courseKey;
    private final double finalPercent;
    private final String letterGrade;
    private final double gradePoints;
    private final double weightedGradePoints;
    private final double credit;
    private final String teacherName;

    /**
     * Creates a finalized course result.
     *
     * @param schoolYear  start year of the school year (2001 = 2001-02)
     * @param gradeLevel  the student's grade level that year (e.g. "Freshman")
     * @param semester    "Fall" or "Spring"
     * @param className   the class name as scheduled
     * @param finalPercent the final 0-100 course percent
     * @param credit      credits earned for the semester course
     * @param teacherName display name of the teacher, or null when unknown
     */
    public TranscriptEntry(int schoolYear, String gradeLevel, String semester,
                           String className, double finalPercent, double credit,
                           String teacherName) {
        this.schoolYear = schoolYear;
        this.gradeLevel = gradeLevel;
        this.semester = semester;
        this.className = className;
        this.courseKey = StudentAcademicRecord.normalizeCourseKey(className);
        this.finalPercent = Math.max(0.0, Math.min(100.0, finalPercent));
        this.letterGrade = GradeBook.letterGradeFor(this.finalPercent);
        this.gradePoints = GradeBook.gradePointsFor(this.finalPercent);
        this.weightedGradePoints = this.gradePoints + GradeBook.honorsBonusFor(className);
        this.credit = credit;
        this.teacherName = teacherName;
    }

    public int getSchoolYear() {
        return schoolYear;
    }

    /** Display form of the school year, e.g. "2001-02". */
    public String getSchoolYearLabel() {
        return schoolYear + "-" + String.format("%02d", (schoolYear + 1) % 100);
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public String getSemester() {
        return semester;
    }

    public String getClassName() {
        return className;
    }

    public String getCourseKey() {
        return courseKey;
    }

    public double getFinalPercent() {
        return finalPercent;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public double getGradePoints() {
        return gradePoints;
    }

    public double getWeightedGradePoints() {
        return weightedGradePoints;
    }

    public double getCredit() {
        return credit;
    }

    public String getTeacherName() {
        return teacherName;
    }
}
