package simulation;

import behavior.BehaviorContext;
import entity.ActivityType;
import entity.EntityState;
import entity.Rooms.Room;
import entity.Staff;
import entity.Student;
import utility.GameRandom;
import utility.TeacherStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static constants.SimConstants.*;

/**
 * Teacher-driven classroom discipline.
 *
 * <p>
 * Students no longer roll their own catch chances. Instead, misbehaving
 * student action nodes {@linkplain #reportMisbehavior report} their visible
 * misbehavior (with a concealment score derived from their own stats), and
 * the supervising teacher's behavior tree perceives and reacts:
 * </p>
 *
 * <ul>
 *   <li><b>Noise assessment</b> — when enough students are talking the room
 *       is deemed LOUD and the teacher settles the whole class at once, a
 *       lighter penalty for everyone involved.</li>
 *   <li><b>Repeat offenders</b> — a student the teacher keeps noticing
 *       talking while the rest of the class is quiet gets individually
 *       reprimanded (full penalty).</li>
 *   <li><b>Detection contest</b> — each noticed/caught roll pits the
 *       teacher's perception and experience against the student's
 *       concealment, modified by room noise and how busy the teacher is.
 *       Covert acts (notes, texting, whispering) are easier to pull off
 *       while the class is loud or the teacher is grading.</li>
 * </ul>
 *
 * <p>
 * Teacher actions always override student actions: a caught or settled
 * student is snapped back to {@link ActivityType#ATTENDING_CLASS} and their
 * decision cooldown is reset, ending whatever they were doing this tick.
 * </p>
 *
 * <p>
 * Usage per tick (orchestrated by {@link SimulationEngine}):
 * <ol>
 *   <li>{@link #clearTick()} at the start of each tick</li>
 *   <li>Student behavior trees tick; misbehavior nodes call
 *       {@link #reportMisbehavior}</li>
 *   <li>Social interactions resolve</li>
 *   <li>Teacher behavior trees tick; teacher action nodes call
 *       {@link #scanRoom}, {@link #settleClass}, {@link #applyReprimand}</li>
 *   <li>The engine logs every student in {@link #getDisciplinedStudents()}
 *       so [CAUGHT]/[SETTLED] tags land on this tick's log lines</li>
 * </ol>
 * </p>
 */
public class ClassroomDisciplineService {

    /** Cooldown applied to interrupted students (matches the action window). */
    private static final int INTERRUPT_COOLDOWN_TICKS = 5;

    /** In-class activities the teacher considers misbehavior. */
    private static final Set<ActivityType> MISBEHAVIOR_ACTIVITIES = Collections.unmodifiableSet(
            new HashSet<>(List.of(
                    ActivityType.TALKING,
                    ActivityType.WHISPERING,
                    ActivityType.PASSING_NOTE,
                    ActivityType.TEXTING,
                    ActivityType.DAYDREAMING)));

    private final List<MisbehaviorReport> reports = new ArrayList<>();
    private final Map<Student, Integer> talkingNotices = new HashMap<>();
    private final Map<Room, Integer> calmTicksByRoom = new HashMap<>();
    private final Set<Student> disciplinedThisTick = new HashSet<>();

    // ==================== Tick / period lifecycle ====================

    /**
     * Clears the per-tick state (misbehavior reports and the set of students
     * disciplined this tick) and counts down each room's calm window.
     * Must be called at the start of every simulation tick.
     */
    public void clearTick() {
        reports.clear();
        disciplinedThisTick.clear();
        calmTicksByRoom.entrySet().removeIf(entry -> {
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                return true;
            }
            entry.setValue(remaining);
            return false;
        });
    }

    /**
     * Resets per-period tracking: talking-notice counters and calm windows.
     * Call on every period change (a new class of students files in).
     */
    public void onPeriodChange() {
        talkingNotices.clear();
        calmTicksByRoom.clear();
    }

    // ==================== Student-side reporting ====================

    /**
     * Records a visible misbehavior attempt for the supervising teacher to
     * (potentially) notice this tick. Called by student action nodes instead
     * of rolling their own catch chance.
     *
     * @param student     the misbehaving student
     * @param room        the classroom the misbehavior happens in
     * @param type        the misbehavior activity (TALKING, PASSING_NOTE, ...)
     * @param concealment the student's concealment score — how many
     *                    percentage points their stats (and gear) shave off
     *                    the teacher's notice chance
     */
    public void reportMisbehavior(Student student, Room room,
            ActivityType type, int concealment) {
        if (student == null || room == null || type == null) {
            return;
        }
        reports.add(new MisbehaviorReport(student, room, type, concealment));
    }

    /**
     * Whether the room is inside a calm window after the teacher settled the
     * class. Students do not dare start new misbehavior while it lasts.
     *
     * @param room the room to check
     * @return true if the room was recently settled
     */
    public boolean isRoomCalmed(Room room) {
        return room != null && calmTicksByRoom.getOrDefault(room, 0) > 0;
    }

    // ==================== Noise assessment ====================

    /**
     * Counts the students in the room that are currently talking out loud.
     *
     * @param room the room to assess
     * @return the number of TALKING students
     */
    public int countTalkingStudents(Room room) {
        if (room == null || room.getStudents() == null) {
            return 0;
        }
        int talking = 0;
        for (Student student : room.getStudents()) {
            if (student == null) {
                continue;
            }
            EntityState state = student.getEntityState();
            if (state != null && state.getCurrentActivity() == ActivityType.TALKING) {
                talking++;
            }
        }
        return talking;
    }

    /**
     * Whether the room noise has crossed the LOUD threshold: at least
     * {@code max(CLASS_NOISE_LOUD_MIN_TALKERS, ceil(occupants * FRACTION))}
     * students talking at once. A LOUD room prompts a class-wide settle
     * instead of individual reprimands.
     *
     * @param room the room to assess
     * @return true if the room is loud
     */
    public boolean isRoomLoud(Room room) {
        if (room == null || room.getStudents() == null) {
            return false;
        }
        int occupants = room.getStudents().size();
        if (occupants == 0) {
            return false;
        }
        int threshold = Math.max(CLASS_NOISE_LOUD_MIN_TALKERS,
                (int) Math.ceil(occupants * CLASS_NOISE_LOUD_FRACTION));
        return countTalkingStudents(room) >= threshold;
    }

    // ==================== Teacher-side reactions ====================

    /**
     * The teacher sweeps the room for this tick's reported misbehavior.
     * Each report is resolved as a detection contest (teacher perception and
     * experience vs. student concealment, modified by room noise and the
     * teacher's current busyness):
     *
     * <ul>
     *   <li><b>Talking</b> — a win increments the student's notice counter.
     *       Punishment is deferred to {@link #applyReprimand} once the
     *       counter crosses the repeat threshold (first notice is a
     *       warning).</li>
     *   <li><b>Covert acts</b> (notes, texting, whispering, daydreaming) — a
     *       win catches the student immediately with the full individual
     *       penalty; a confirmed interaction partner is caught too.</li>
     * </ul>
     *
     * @param teacher the supervising teacher
     * @param room    the teacher's classroom
     * @return human-readable descriptions of this tick's catches, for the
     *         teacher's action log (empty if nothing was caught)
     */
    public List<String> scanRoom(Staff teacher, Room room) {
        List<String> catches = new ArrayList<>();
        if (teacher == null || room == null) {
            return catches;
        }
        boolean loud = isRoomLoud(room);

        for (MisbehaviorReport report : reports) {
            if (report.room != room || disciplinedThisTick.contains(report.student)) {
                continue;
            }
            // Skip reports whose action fizzled after reporting (e.g. the
            // interaction was denied and the student reverted to IDLE).
            EntityState state = report.student.getEntityState();
            if (state == null || state.getCurrentActivity() != report.type) {
                continue;
            }

            int noticeChance = computeNoticeChance(teacher, report.type,
                    report.concealment, loud);
            if (GameRandom.nextDouble(100) >= noticeChance) {
                continue;
            }

            if (report.type == ActivityType.TALKING) {
                talkingNotices.merge(report.student, 1, Integer::sum);
            } else {
                applyIndividualCatch(report.student, report.type,
                        INTERRUPT_COOLDOWN_TICKS);
                catches.add("caught " + report.student
                        + " " + describeMisbehavior(report.type));
            }
        }
        return catches;
    }

    /**
     * Finds a student the teacher should individually reprimand: still
     * talking right now, and already noticed talking at least
     * {@code REPRIMAND_REPEAT_THRESHOLD} times this period while the room as
     * a whole stayed quiet.
     *
     * @param room the teacher's classroom
     * @return the repeat offender, or null if nobody qualifies
     */
    public Student findReprimandTarget(Room room) {
        if (room == null || room.getStudents() == null) {
            return null;
        }
        for (Student student : room.getStudents()) {
            if (student == null || disciplinedThisTick.contains(student)) {
                continue;
            }
            EntityState state = student.getEntityState();
            if (state == null || state.getCurrentActivity() != ActivityType.TALKING) {
                continue;
            }
            if (talkingNotices.getOrDefault(student, 0) >= REPRIMAND_REPEAT_THRESHOLD) {
                return student;
            }
        }
        return null;
    }

    /**
     * Individually reprimands a repeat talker: full caught penalty, and a
     * decision cooldown extended by the teacher's experience multiplier so a
     * veteran's reprimand keeps the student on-task longer. Resets the
     * student's notice counter.
     *
     * @param teacher the reprimanding teacher
     * @param student the repeat offender
     */
    public void applyReprimand(Staff teacher, Student student) {
        if (teacher == null || student == null) {
            return;
        }
        double expMultiplier = teacher.teacherStatistics.getExperienceMultiplier();
        int cooldown = (int) Math.round(REPRIMAND_COOLDOWN_BASE_TICKS * expMultiplier);
        applyIndividualCatch(student, ActivityType.TALKING, cooldown);
        talkingNotices.remove(student);
    }

    /**
     * The teacher calls the whole class down. Every currently-misbehaving
     * student in the room is interrupted with the reduced "settled" penalty,
     * and the room enters a calm window (no new misbehavior) whose length
     * scales with the teacher's charisma, determination, and experience.
     *
     * @param teacher the teacher settling the class
     * @param room    the teacher's classroom
     * @return how many students were interrupted
     */
    public int settleClass(Staff teacher, Room room) {
        if (teacher == null || room == null || room.getStudents() == null) {
            return 0;
        }

        int interrupted = 0;
        for (Student student : new ArrayList<>(room.getStudents())) {
            if (student == null || disciplinedThisTick.contains(student)) {
                continue;
            }
            EntityState state = student.getEntityState();
            if (state == null
                    || !MISBEHAVIOR_ACTIVITIES.contains(state.getCurrentActivity())) {
                continue;
            }
            applySettled(student, state.getCurrentActivity());
            interrupted++;
        }

        TeacherStatistics stats = teacher.teacherStatistics;
        // Authority in [0..1]: average of charisma and determination over a
        // 100-point scale, boosted by experience.
        double authority = (stats.getCharisma() + stats.getDetermination()) / 200.0;
        authority = Math.min(1.0, authority * stats.getExperienceMultiplier());
        int calmTicks = SETTLE_CALM_BASE_TICKS
                + (int) Math.round(authority * SETTLE_CALM_MAX_BONUS_TICKS);
        calmTicksByRoom.put(room, calmTicks);

        return interrupted;
    }

    /**
     * Students that were caught, reprimanded, or settled this tick. The
     * engine must write action-log entries for these students even if their
     * own behavior tree did not tick, so the [CAUGHT]/[SETTLED] tag lands on
     * the correct minute.
     *
     * @return an unmodifiable snapshot of this tick's disciplined students
     */
    public Set<Student> getDisciplinedStudents() {
        return Collections.unmodifiableSet(new HashSet<>(disciplinedThisTick));
    }

    // ==================== Detection contest ====================

    /**
     * Computes the percent chance the teacher notices a misbehaving student:
     * a contest of teacher stats vs. student stats.
     *
     * <pre>
     * chance = base[type]
     *        + (teacher perception / divisor) * experienceMultiplier
     *        - student concealment
     *        - noise cover (covert acts only, while the room is LOUD)
     *        - busy-teacher penalty (grading &gt; settling/reprimanding)
     * clamped to [DETECTION_FLOOR, DETECTION_CEILING]
     * </pre>
     *
     * @param teacher     the supervising teacher
     * @param type        the misbehavior activity
     * @param concealment the student's concealment score
     * @param roomLoud    whether the room is currently LOUD
     * @return the clamped notice chance in percent
     */
    private int computeNoticeChance(Staff teacher, ActivityType type,
            int concealment, boolean roomLoud) {
        TeacherStatistics stats = teacher.teacherStatistics;
        double expMultiplier = stats.getExperienceMultiplier();

        int teacherAwareness = (int) Math.round(
                stats.getPerception() / (double) DISCIPLINE_TEACHER_PERCEPTION_DIVISOR
                        * expMultiplier);

        int chance = baseNoticeChance(type) + teacherAwareness - concealment;

        boolean covert = type != ActivityType.TALKING;
        if (covert && roomLoud) {
            chance -= DISCIPLINE_NOISE_COVER_MODIFIER;
        }

        chance -= busyPenalty(teacher, expMultiplier);

        return Math.max(DISCIPLINE_DETECTION_FLOOR,
                Math.min(DISCIPLINE_DETECTION_CEILING, chance));
    }

    private int baseNoticeChance(ActivityType type) {
        return switch (type) {
            case TALKING -> DISCIPLINE_BASE_NOTICE_TALKING;
            case PASSING_NOTE -> DISCIPLINE_BASE_NOTICE_PASSING_NOTE;
            case TEXTING -> DISCIPLINE_BASE_NOTICE_TEXTING;
            case WHISPERING -> DISCIPLINE_BASE_NOTICE_WHISPERING;
            case DAYDREAMING -> DISCIPLINE_BASE_NOTICE_DAYDREAMING;
            default -> DISCIPLINE_BASE_NOTICE_TALKING;
        };
    }

    /**
     * How distracted the teacher currently is, based on the activity chosen
     * on a previous tick. Misbehavior works better while the teacher is
     * busy: grading is the biggest opening (reduced for veterans, who grade
     * while still watching the room), settling or reprimanding a moderate
     * one.
     */
    private int busyPenalty(Staff teacher, double expMultiplier) {
        EntityState state = teacher.getEntityState();
        if (state == null) {
            return 0;
        }
        return switch (state.getCurrentActivity()) {
            case GRADING -> (int) Math.round(DISCIPLINE_BUSY_PENALTY_GRADING / expMultiplier);
            case SETTLING_CLASS, REPRIMANDING -> DISCIPLINE_BUSY_PENALTY_SETTLING;
            default -> 0;
        };
    }

    // ==================== Outcome application ====================

    /**
     * Full individual catch: caught flags on the behavior context, activity
     * override, decision cooldown reset, and the full caught stat drain.
     * A confirmed interaction partner still engaged in the same activity is
     * pulled into the incident (flags and interruption, no stat drain —
     * they did not initiate).
     */
    private void applyIndividualCatch(Student student, ActivityType type, int cooldownTicks) {
        flagAndInterrupt(student, type, "individual", cooldownTicks);

        student.studentStatistics.drainSecondaryStat("resilience",
                STAT_DRAIN_CAUGHT_RESILIENCE,
                ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
        student.studentStatistics.drainSecondaryStat("adaptability",
                STAT_DRAIN_CAUGHT_ADAPTABILITY,
                ALLOSTATIC_STRESS_FACTOR_ADAPTABILITY);

        Student partner = findEngagedPartner(student, type);
        if (partner != null) {
            flagAndInterrupt(partner, type, "individual", cooldownTicks);
        }
    }

    /**
     * Reduced class-wide penalty: settled flags, activity override, and a
     * small resilience drain. No partner propagation needed — the settle
     * sweep already covers everyone misbehaving in the room.
     */
    private void applySettled(Student student, ActivityType type) {
        flagAndInterrupt(student, type, "settled", INTERRUPT_COOLDOWN_TICKS);

        student.studentStatistics.drainSecondaryStat("resilience",
                STAT_DRAIN_SETTLED_RESILIENCE,
                ALLOSTATIC_STRESS_FACTOR_RESILIENCE);
    }

    /**
     * The shared override: sets the caught flags on the student's behavior
     * context, snaps them back to ATTENDING_CLASS (the teacher's action
     * always overrides the student's), and resets their decision cooldown.
     */
    private void flagAndInterrupt(Student student, ActivityType type,
            String severity, int cooldownTicks) {
        BehaviorContext context = student.getBehaviorContext();
        if (context != null) {
            context.setVariable("was_caught", true);
            context.setVariable("catch_type", describeMisbehavior(type));
            context.setVariable("caught_severity", severity);
        }
        EntityState state = student.getEntityState();
        if (state != null) {
            state.setCurrentActivity(ActivityType.ATTENDING_CLASS);
            state.resetDecisionCooldown(Math.max(state.getDecisionCooldown(), cooldownTicks));
        }
        disciplinedThisTick.add(student);
    }

    /**
     * Returns the student's confirmed interaction partner if that partner is
     * still engaged in the same misbehavior activity, otherwise null.
     */
    private Student findEngagedPartner(Student student, ActivityType type) {
        BehaviorContext context = student.getBehaviorContext();
        if (context == null) {
            return null;
        }
        Object partnerObj = context.getVariable("interaction_target");
        if (!(partnerObj instanceof Student partner) || partner == student
                || disciplinedThisTick.contains(partner)) {
            return null;
        }
        EntityState partnerState = partner.getEntityState();
        if (partnerState == null || partnerState.getCurrentActivity() != type) {
            return null;
        }
        return partner;
    }

    private static String describeMisbehavior(ActivityType type) {
        return switch (type) {
            case TALKING -> "talking";
            case PASSING_NOTE -> "passing_note";
            case TEXTING -> "texting";
            case WHISPERING -> "whispering";
            case DAYDREAMING -> "daydreaming";
            default -> type.name().toLowerCase();
        };
    }

    /**
     * A single misbehavior attempt reported by a student action node this
     * tick, waiting for the supervising teacher's detection contest.
     */
    private static class MisbehaviorReport {
        private final Student student;
        private final Room room;
        private final ActivityType type;
        private final int concealment;

        MisbehaviorReport(Student student, Room room, ActivityType type, int concealment) {
            this.student = student;
            this.room = room;
            this.type = type;
            this.concealment = concealment;
        }
    }
}
