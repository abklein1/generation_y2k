package utility.traits;

import entity.CellPhone;
import entity.Staff;
import entity.Student;

/**
 * Immutable bundle of inputs that drive a phone's condition roll: how
 * old the phone is, the owner's household income tier, and the owner's
 * agility / luck stats.  Built once per phone at assignment time and
 * fed to {@link PhoneConditionWeightFunction} via
 * {@link TraitSelector#selectTraits}.
 *
 * <p>The income string is normalized to lowercase
 * ({@code "low" / "middle" / "high"}) so the weight function can switch
 * on it without worrying about the casing inconsistencies that exist
 * elsewhere in the codebase (see {@code PhoneDataLoader.selectByIncome}).
 * Staff don't carry a household income tier, so the
 * {@link #forStaff(Staff, CellPhone, int) staff factory} pins it to
 * {@code "middle"}.</p>
 */
public final class PhoneConditionContext {

    /**
     * Income tier used for staff, who don't have a household income
     * level on their statistics object.  A flat middle bias keeps staff
     * conditions plausible without inventing a separate income system.
     */
    private static final String STAFF_INCOME_TIER = "middle";

    private final int phoneAgeYears;
    private final String incomeLevel;
    private final int agility;
    private final int luck;

    /**
     * Direct constructor used by the static factories and by tests that
     * want full control over the inputs.
     *
     * @param phoneAgeYears the phone's age in years (clamped to >= 0)
     * @param incomeLevel   the owner's income tier (lowercased; null
     *                      becomes the empty string)
     * @param agility       the owner's agility primary stat
     * @param luck          the owner's luck primary stat
     */
    public PhoneConditionContext(int phoneAgeYears, String incomeLevel,
                                 int agility, int luck) {
        this.phoneAgeYears = Math.max(0, phoneAgeYears);
        this.incomeLevel = incomeLevel == null ? "" : incomeLevel.toLowerCase();
        this.agility = agility;
        this.luck = luck;
    }

    /**
     * Builds a context for a student-owned phone.  Pulls agility, luck,
     * and household income tier directly off the student's statistics,
     * and computes the phone's age as
     * {@code max(0, simulationYear - phone.releaseYear)}.  When the phone
     * has no recorded release year (0), the age is treated as 0 rather
     * than producing a wildly negative penalty.
     *
     * @param student        the phone's owner (must be non-null)
     * @param phone          the phone whose condition is being rolled
     * @param simulationYear the current simulation year
     * @return a fully-populated context
     */
    public static PhoneConditionContext forStudent(Student student, CellPhone phone,
                                                   int simulationYear) {
        int agility = student.studentStatistics.getAgility();
        int luck = student.studentStatistics.getLuck();
        String income = student.studentStatistics.getIncomeLevel();
        int age = computeAge(phone, simulationYear);
        return new PhoneConditionContext(age, income, agility, luck);
    }

    /**
     * Builds a context for a staff-owned phone.  Staff don't have a
     * household income tier, so a flat {@code "middle"} bias is used.
     * Agility, luck, and the age computation otherwise mirror the
     * student factory.
     *
     * @param staff          the phone's owner (must be non-null)
     * @param phone          the phone whose condition is being rolled
     * @param simulationYear the current simulation year
     * @return a fully-populated context
     */
    public static PhoneConditionContext forStaff(Staff staff, CellPhone phone,
                                                 int simulationYear) {
        int agility = staff.teacherStatistics.getAgility();
        int luck = staff.teacherStatistics.getLuck();
        int age = computeAge(phone, simulationYear);
        return new PhoneConditionContext(age, STAFF_INCOME_TIER, agility, luck);
    }

    private static int computeAge(CellPhone phone, int simulationYear) {
        if (phone == null || phone.getReleaseYear() <= 0) {
            return 0;
        }
        return Math.max(0, simulationYear - phone.getReleaseYear());
    }

    public int getPhoneAgeYears() {
        return phoneAgeYears;
    }

    public String getIncomeLevel() {
        return incomeLevel;
    }

    public int getAgility() {
        return agility;
    }

    public int getLuck() {
        return luck;
    }
}
