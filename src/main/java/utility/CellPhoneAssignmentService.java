package utility;

import entity.CellPhone;
import entity.Staff;
import entity.Student;
import entity.Town;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static constants.SimConstants.*;

/**
 * Assigns cell phones to students and staff based on grade-level and role
 * ownership rates. Ownership rates grow year-over-year from the 2004 baseline
 * using a compound growth model. Re-calling this service in a later simulation
 * year will only assign new phones to people who do not already have one,
 * using the higher year-adjusted rates.
 */
public class CellPhoneAssignmentService {

    private static final String[] PHONE_COLORS = {
        "silver", "black", "blue", "red", "navy", "gray",
        "white", "pink", "dark blue", "charcoal"
    };

    /**
     * Assigns cell phones to all students and staff in the town's pools.
     * People who already own a phone are skipped.
     *
     * @param town           the town whose population should be processed
     * @param simulationYear the current simulation year (2004+)
     */
    public static void assignPhonesForTown(Town town, int simulationYear) {
        Set<String> usedNumbers = new HashSet<>(town.getUsedPhoneNumbers());
        int studentCount = 0;
        int staffCount = 0;

        HashMap<Integer, Student> allStudents = town.getStudentPool().getAllStudents();
        for (Student student : allStudents.values()) {
            if (town.hasPhone(student)) {
                continue;
            }
            String grade = student.studentStatistics.getGradeLevel();
            if (grade == null) {
                continue;
            }
            double rate = getStudentOwnershipRate(grade, simulationYear);
            if (GameRandom.nextDouble() < rate) {
                CellPhone phone = createPhone(student.toString(), usedNumbers, true);
                town.assignStudentPhone(student, phone);
                studentCount++;
            }
        }

        HashMap<Integer, Staff> allStaff = town.getStaffPool().getAllStaff();
        for (Staff staff : allStaff.values()) {
            if (town.hasPhone(staff)) {
                continue;
            }
            double rate = getStaffOwnershipRate(simulationYear);
            if (GameRandom.nextDouble() < rate) {
                CellPhone phone = createPhone(staff.toString(), usedNumbers, false);
                town.assignStaffPhone(staff, phone);
                staffCount++;
            }
        }

        GameLogger.logGeneration("Cell phone assignment complete (year " + simulationYear + "): "
                + studentCount + " students, " + staffCount + " staff");
    }

    /**
     * Returns the year-adjusted ownership rate for a student grade level.
     * Applies compound growth from the 2004 baseline, capped at the max rate.
     *
     * @param gradeLevel     the student's grade level
     * @param simulationYear the current simulation year
     * @return the effective ownership probability
     */
    static double getStudentOwnershipRate(String gradeLevel, int simulationYear) {
        double baseRate = switch (gradeLevel) {
            case "Freshman"  -> CELLPHONE_OWNERSHIP_FRESHMAN;
            case "Sophomore" -> CELLPHONE_OWNERSHIP_SOPHOMORE;
            case "Junior"    -> CELLPHONE_OWNERSHIP_JUNIOR;
            case "Senior"    -> CELLPHONE_OWNERSHIP_SENIOR;
            default -> 0.0;
        };
        return applyYearlyGrowth(baseRate, simulationYear);
    }

    /**
     * Returns the year-adjusted ownership rate for staff members.
     *
     * @param simulationYear the current simulation year
     * @return the effective ownership probability
     */
    static double getStaffOwnershipRate(int simulationYear) {
        return applyYearlyGrowth(CELLPHONE_OWNERSHIP_STAFF, simulationYear);
    }

    /**
     * Applies compound yearly growth to a base rate, capping at the maximum.
     *
     * @param baseRate       the 2004 baseline rate
     * @param simulationYear the current simulation year
     * @return the adjusted rate, capped at CELLPHONE_OWNERSHIP_MAX_RATE
     */
    private static double applyYearlyGrowth(double baseRate, int simulationYear) {
        int yearsElapsed = simulationYear - STARTING_YEAR;
        if (yearsElapsed <= 0) {
            return baseRate;
        }
        double adjusted = baseRate * Math.pow(1.0 + CELLPHONE_YEARLY_GROWTH_RATE, yearsElapsed);
        return Math.min(adjusted, CELLPHONE_OWNERSHIP_MAX_RATE);
    }

    /**
     * Creates a CellPhone with a unique number, random color, and a random plan tier.
     *
     * @param ownerName   display name of the owner
     * @param usedNumbers set of numbers already assigned (mutated to include the new number)
     * @param isStudent   true for student plan distribution, false for staff
     * @return a new CellPhone instance
     */
    private static CellPhone createPhone(String ownerName, Set<String> usedNumbers,
                                         boolean isStudent) {
        String number = generateUniquePhoneNumber(usedNumbers);
        String color = PHONE_COLORS[GameRandom.nextInt(0, PHONE_COLORS.length - 1)];
        Map.Entry<Integer, Integer> plan = rollPlanTier(isStudent);
        return new CellPhone(number, ownerName, color, plan.getKey(), plan.getValue());
    }

    /**
     * Generates a unique 7-digit phone number in XXX-XXXX format.
     * Retries until a number not already in the used set is found.
     *
     * @param usedNumbers set of numbers already assigned (mutated on success)
     * @return a unique phone number string
     */
    static String generateUniquePhoneNumber(Set<String> usedNumbers) {
        String number;
        do {
            int prefix = GameRandom.nextInt(100, 999);
            int suffix = GameRandom.nextInt(0, 9999);
            number = String.format("%03d-%04d", prefix, suffix);
        } while (usedNumbers.contains(number));
        usedNumbers.add(number);
        return number;
    }

    /**
     * Rolls a random plan tier and returns the minute/text limits.
     * Students lean toward basic plans; staff lean toward standard/premium.
     *
     * @param isStudent true for student distribution, false for staff
     * @return a Map.Entry with minutes as key and text limit as value
     */
    private static Map.Entry<Integer, Integer> rollPlanTier(boolean isStudent) {
        int roll = GameRandom.nextInt(0, 99);
        int basicThreshold;
        int standardThreshold;

        if (isStudent) {
            basicThreshold = CELLPHONE_PLAN_BASIC_THRESHOLD;
            standardThreshold = CELLPHONE_PLAN_STANDARD_THRESHOLD;
        } else {
            basicThreshold = CELLPHONE_PLAN_BASIC_THRESHOLD - 25;
            standardThreshold = CELLPHONE_PLAN_STANDARD_THRESHOLD - 20;
        }

        if (roll < basicThreshold) {
            return Map.entry(CELLPHONE_PLAN_BASIC_MINUTES, CELLPHONE_PLAN_BASIC_TEXTS);
        } else if (roll < standardThreshold) {
            return Map.entry(CELLPHONE_PLAN_STANDARD_MINUTES, CELLPHONE_PLAN_STANDARD_TEXTS);
        } else {
            return Map.entry(CELLPHONE_PLAN_PREMIUM_MINUTES, CELLPHONE_PLAN_PREMIUM_TEXTS);
        }
    }
}
