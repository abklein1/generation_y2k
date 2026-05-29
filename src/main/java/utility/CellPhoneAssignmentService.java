package utility;

import entity.CellPhone;
import entity.Staff;
import entity.Student;
import entity.Town;
import utility.traits.PhoneConditionContext;
import utility.traits.PhoneConditionWeightFunction;
import utility.traits.TraitDataset;
import utility.traits.TraitDatasetLoader;
import utility.traits.TraitSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
     * Classpath path to the phone condition / trait dataset.  Loaded
     * lazily by {@link TraitDatasetLoader} on first use; subsequent
     * loads are cached so the per-phone selection cost is negligible.
     */
    private static final String CELLPHONE_TRAITS_PATH =
            "/Resources/Flavor/cellphone_traits.json";

    /** Inclusive lower bound on the number of flavor descriptors per phone. */
    private static final int CELLPHONE_TRAIT_MIN_COUNT = 2;

    /** Inclusive upper bound on the number of flavor descriptors per phone. */
    private static final int CELLPHONE_TRAIT_MAX_COUNT = 3;

    /**
     * Single weight-function instance reused across every condition roll.
     * The function is stateless, so sharing one instance avoids a fresh
     * allocation per phone.
     */
    private static final PhoneConditionWeightFunction CONDITION_WEIGHTS =
            new PhoneConditionWeightFunction();

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
                ItemDecorationService.decoratePhone(phone, student);
                applyConditionTraits(phone,
                        PhoneConditionContext.forStudent(student, phone, simulationYear));
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
                applyConditionTraits(phone,
                        PhoneConditionContext.forStaff(staff, phone, simulationYear));
                staffCount++;
            }
        }

        GameLogger.logGeneration("Cell phone assignment complete (year " + simulationYear + "): "
                + studentCount + " students, " + staffCount + " staff");
    }

    /**
     * Populates each student phone's contact list based on existing social
     * relationships.  This must run <i>after</i> phones have been assigned
     * (see {@link #assignPhonesForTown}) <i>and</i> after friend/sibling
     * relationships have been initialized (see
     * {@link SocialLinkConnector#initializeSocialLinks}).
     *
     * <p>Contact population rules, evaluated per ordered (owner, peer) pair
     * where both own a phone:</p>
     * <ul>
     *   <li><b>Sibling (in or out of school):</b> the owner <i>always</i>
     *       saves the sibling's number whenever both own a phone — phones
     *       are family devices and parents make sure these are saved.
     *       This rule is applied <i>unconditionally</i> regardless of how
     *       the siblings feel about each other (close, neutral, or
     *       outright rivals).</li>
     *   <li><b>Friend (peer is in owner's friends list):</b> the owner saves
     *       the peer's number with probability
     *       {@link constants.SimConstants#PHONE_CONTACT_FRIEND_PROBABILITY}.
     *       This means a typical student carries most of their friends'
     *       numbers but not every single one.  Friends who already came in
     *       through the sibling rule are left untouched.</li>
     *   <li><b>Acquaintance / stranger:</b> not saved.</li>
     * </ul>
     *
     * <p>Population is asymmetric: A may have B's number while B does not
     * have A's, mirroring the directed nature of the social link graph.
     * The sibling guarantee is intentionally <i>symmetric</i> though — if
     * both siblings own phones, both directions are added.  Existing
     * contact entries are preserved across re-runs.</p>
     *
     * @param town the town whose phones should have contacts populated
     */
    public static void populatePhoneContacts(Town town) {
        if (town == null) {
            return;
        }

        Map<Student, CellPhone> studentPhones = town.getAllStudentPhones();
        if (studentPhones == null || studentPhones.isEmpty()) {
            return;
        }

        // Build a quick lookup so we can resolve each peer's CellPhone
        // without a linear scan per pair.
        Map<Student, CellPhone> phoneByStudent = new HashMap<>(studentPhones);

        int siblingContacts = 0;
        int friendContacts = 0;
        int phonesPopulated = 0;

        for (Map.Entry<Student, CellPhone> entry : studentPhones.entrySet()) {
            Student owner = entry.getKey();
            CellPhone ownerPhone = entry.getValue();
            if (owner == null || ownerPhone == null) {
                continue;
            }

            int before = ownerPhone.getContactCount();

            // Rule 1 (HARD GUARANTEE): siblings are ALWAYS in the contact
            // list whenever both parties own a phone.  No relationship
            // check, no probability roll — siblings come first so a later
            // friend roll can never displace or skip them.
            siblingContacts += addSiblingContactsAlways(owner, ownerPhone, phoneByStudent);

            // Rule 2: friends with a probability roll.  Siblings already
            // saved above will be a no-op here because hasContactNumber
            // short-circuits the add.
            friendContacts += addFriendContactsProbabilistic(owner, ownerPhone, phoneByStudent);

            if (ownerPhone.getContactCount() > before) {
                phonesPopulated++;
            }
        }

        GameLogger.logGeneration("Phone contacts populated: "
                + (siblingContacts + friendContacts) + " entries ("
                + siblingContacts + " sibling, " + friendContacts + " friend) across "
                + phonesPopulated + " student phones");
    }

    /**
     * Unconditionally saves every sibling's number into the owner's phone
     * (when the sibling also owns a phone).  Both in-school and
     * out-of-school siblings are considered so that any sibling who has
     * been issued a phone — for any reason — ends up in the owner's
     * contacts.  This implements the "siblings always, regardless of
     * relationship" guarantee documented on {@link #populatePhoneContacts}.
     *
     * @return the number of new sibling contacts added
     */
    private static int addSiblingContactsAlways(Student owner,
                                                CellPhone ownerPhone,
                                                Map<Student, CellPhone> phoneByStudent) {
        int added = 0;

        List<Student> inSchool = owner.studentStatistics.getSiblingsInSchool();
        if (inSchool != null) {
            for (Student sibling : inSchool) {
                if (addContactIfPossible(ownerPhone, sibling, phoneByStudent)) {
                    added++;
                }
            }
        }

        // Also include out-of-school siblings so that if they ever own a
        // phone (e.g. a younger sibling with a basic phone), we still
        // honor the family-contacts rule.  In the common case they have
        // no phone and addContactIfPossible no-ops.
        List<Student> outOfSchool = owner.studentStatistics.getSiblingsNotInSchool();
        if (outOfSchool != null) {
            for (Student sibling : outOfSchool) {
                if (addContactIfPossible(ownerPhone, sibling, phoneByStudent)) {
                    added++;
                }
            }
        }

        return added;
    }

    /**
     * Saves friends' numbers into the owner's phone with probability
     * {@link constants.SimConstants#PHONE_CONTACT_FRIEND_PROBABILITY}.
     * Friends already saved as siblings are no-ops here.
     *
     * @return the number of new friend contacts added
     */
    private static int addFriendContactsProbabilistic(Student owner,
                                                      CellPhone ownerPhone,
                                                      Map<Student, CellPhone> phoneByStudent) {
        int added = 0;
        List<Student> friends = owner.studentStatistics.getFriendsInSchool();
        if (friends == null) {
            return 0;
        }
        for (Student friend : friends) {
            if (GameRandom.nextDouble() <= PHONE_CONTACT_FRIEND_PROBABILITY) {
                if (addContactIfPossible(ownerPhone, friend, phoneByStudent)) {
                    added++;
                }
            }
        }
        return added;
    }

    /**
     * Adds the given peer to the owner's contact list, but only if the peer
     * is real, distinct, and owns a phone of their own.  Without a phone of
     * their own there's no number to save and no way to receive a text.
     *
     * @param ownerPhone     the phone whose contact list to mutate
     * @param peer           the peer to potentially add as a contact
     * @param phoneByStudent map from student to their cell phone
     * @return true if a new contact was added, false if it was skipped or
     *         already saved
     */
    private static boolean addContactIfPossible(CellPhone ownerPhone, Student peer,
                                                Map<Student, CellPhone> phoneByStudent) {
        if (peer == null || ownerPhone == null) {
            return false;
        }
        CellPhone peerPhone = phoneByStudent.get(peer);
        if (peerPhone == null) {
            return false;
        }
        String peerNumber = peerPhone.getPhoneNumber();
        if (peerNumber == null || peerNumber.isEmpty()) {
            return false;
        }
        if (ownerPhone.hasContactNumber(peerNumber)) {
            return false;
        }
        ownerPhone.addContact(peer.toString(), peerNumber);
        return true;
    }

    /**
     * Returns the list of co-located peers from {@code candidates} that the
     * given student can plausibly text right now: each candidate must own a
     * phone with SMS capability, and that number must be saved on the
     * student's own phone as a contact.  This is the canonical "who can I
     * text?" filter used by the texting behavior.
     *
     * @param student         the would-be sender
     * @param studentPhone    the sender's phone (must be non-null and
     *                        SMS-capable; callers usually verify this earlier)
     * @param town            the town used to resolve each candidate's phone
     * @param candidates      the pool of co-located peers (room mates or
     *                        transit-group members)
     * @return a freshly-allocated mutable list of textable candidates
     */
    public static List<Student> filterTextableCandidates(Student student,
                                                         CellPhone studentPhone,
                                                         Town town,
                                                         List<Student> candidates) {
        List<Student> textable = new ArrayList<>();
        if (student == null || studentPhone == null || town == null
                || candidates == null || candidates.isEmpty()) {
            return textable;
        }
        for (Student candidate : candidates) {
            if (candidate == null || candidate == student) {
                continue;
            }
            CellPhone peerPhone = town.getStudentPhone(candidate);
            if (peerPhone == null || !peerPhone.hasSms()) {
                // Peer either has no phone or no SMS capability
                continue;
            }
            if (!studentPhone.hasContactNumber(peerPhone.getPhoneNumber())) {
                // Sender doesn't have this peer's number saved
                continue;
            }
            textable.add(candidate);
        }
        return textable;
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
        phone.setReleaseYear(spec.getReleaseYear());
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
     * Loads the cellphone trait dataset, draws 2-3 condition descriptors
     * biased by the given context, and stores both the descriptors and
     * the dominant condition bucket on the phone.  The dominant bucket
     * is computed independently from the trait selector's draws so the
     * displayed condition is always consistent with the underlying
     * stats, even when the selector mixes in lines from neighbouring
     * buckets.  No-ops when the dataset fails to load (the phone simply
     * carries no condition information).
     *
     * @param phone the phone to annotate
     * @param ctx   the inputs that drive the condition roll
     */
    private static void applyConditionTraits(CellPhone phone, PhoneConditionContext ctx) {
        if (phone == null || ctx == null) {
            return;
        }
        TraitDataset dataset = TraitDatasetLoader.load(CELLPHONE_TRAITS_PATH);
        if (dataset == null) {
            return;
        }
        List<String> traits = TraitSelector.selectTraits(dataset, ctx,
                CONDITION_WEIGHTS,
                CELLPHONE_TRAIT_MIN_COUNT, CELLPHONE_TRAIT_MAX_COUNT);
        phone.setCondition(PhoneConditionWeightFunction.dominantCategory(ctx));
        phone.setConditionTraits(traits);
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
