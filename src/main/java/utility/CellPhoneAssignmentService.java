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
 *
 * Phone make/model/color are drawn from phones.json via PhoneDataLoader,
 * with working-class students weighted toward cheaper and older models.
 */
public class CellPhoneAssignmentService {

    /**
     * Assigns cell phones to all students and staff in the town's pools.
     * People who already own a phone are skipped.
     *
     * @param town           the town whose population should be processed
     * @param simulationYear the current simulation year (2004+)
     */
    public static void assignPhonesForTown(Town town, int simulationYear) {
        PhoneDataLoader.ensureLoaded();
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
                String income = student.studentStatistics.getIncomeLevel();
                CellPhone phone = createStudentPhone(student.toString(), income, usedNumbers);
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
                CellPhone phone = createStaffPhone(staff.toString(), usedNumbers);
                town.assignStaffPhone(staff, phone);
                staffCount++;
            }
        }

        GameLogger.logGeneration("Cell phone assignment complete (year " + simulationYear + "): "
                + studentCount + " students, " + staffCount + " staff");
    }

    /**
     * Returns the year-adjusted ownership rate for a student grade level.
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
     */
    static double getStaffOwnershipRate(int simulationYear) {
        return applyYearlyGrowth(CELLPHONE_OWNERSHIP_STAFF, simulationYear);
    }

    private static double applyYearlyGrowth(double baseRate, int simulationYear) {
        int yearsElapsed = simulationYear - STARTING_YEAR;
        if (yearsElapsed <= 0) {
            return baseRate;
        }
        double adjusted = baseRate * Math.pow(1.0 + CELLPHONE_YEARLY_GROWTH_RATE, yearsElapsed);
        return Math.min(adjusted, CELLPHONE_OWNERSHIP_MAX_RATE);
    }

    /**
     * Creates a phone for a student, selecting a model weighted by income level.
     */
    private static CellPhone createStudentPhone(String ownerName, String incomeLevel,
                                                Set<String> usedNumbers) {
        PhoneDataLoader.PhoneSpec spec = PhoneDataLoader.selectByIncome(
                incomeLevel != null ? incomeLevel : "Middle");
        return buildPhone(ownerName, spec, usedNumbers, true);
    }

    /**
     * Creates a phone for a staff member with a general distribution.
     */
    private static CellPhone createStaffPhone(String ownerName, Set<String> usedNumbers) {
        PhoneDataLoader.PhoneSpec spec = PhoneDataLoader.selectForStaff();
        return buildPhone(ownerName, spec, usedNumbers, false);
    }

    /**
     * Assembles a CellPhone from a loaded PhoneSpec, a unique number, and a plan tier.
     */
    private static CellPhone buildPhone(String ownerName, PhoneDataLoader.PhoneSpec spec,
                                        Set<String> usedNumbers, boolean isStudent) {
        String number = generateUniquePhoneNumber(usedNumbers);
        String color = spec.randomColor();
        Map.Entry<Integer, Integer> plan = rollPlanTier(isStudent);

        CellPhone phone = new CellPhone(number, ownerName, spec.getMake(),
                spec.getModel(), color, plan.getKey(), plan.getValue());

        phone.setPrice(spec.getPrice());
        phone.setSize(spec.getSize());
        phone.setBattery(spec.getBattery());
        phone.setKeyboard(spec.hasKeyboard());
        phone.setCamera(spec.hasCamera());
        phone.setVideo(spec.hasVideo());
        phone.setWifi(spec.hasWifi());
        phone.setBluetooth(spec.hasBluetooth());
        phone.setSms(spec.hasSms());
        phone.setIm(spec.hasIm());
        phone.setPda(spec.hasPda());
        phone.setMp3(spec.hasMp3());

        return phone;
    }

    /**
     * Generates a unique 7-digit phone number in XXX-XXXX format.
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
