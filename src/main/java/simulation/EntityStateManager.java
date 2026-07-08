package simulation;

import behavior.BehaviorTree;
import behavior.StudentBehaviorTreeBuilder;
import behavior.TeacherBehaviorTreeBuilder;
import entity.*;
import entity.Rooms.Room;
import utility.GameRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Manages entity states and behavior trees for the simulation.
 * Handles initialization and updates of all entity tracking data.
 */
public class EntityStateManager {

    private final HashMap<Integer, Student> students;
    private final HashMap<Integer, Staff> staff;
    private final StandardSchool school;
    private final Time time;

    /**
     * Creates an entity state manager.
     *
     * @param students the student population
     * @param staff    the staff population
     * @param school   the school
     * @param time     the game time
     */
    public EntityStateManager(HashMap<Integer, Student> students,
            HashMap<Integer, Staff> staff,
            StandardSchool school,
            Time time) {
        this.students = students;
        this.staff = staff;
        this.school = school;
        this.time = time;
    }

    /**
     * Initializes all entity states and behavior trees.
     */
    public void initializeAll() {
        initializeStudentStates();
        initializeStaffStates();
    }

    /**
     * Rebuilds transient runtime helpers after loading a save without changing
     * persisted entity state such as locations, needs, lunch assignment, or transit.
     */
    public void rebuildTransientState() {
        if (students != null) {
            for (Student student : students.values()) {
                if (student.getEntityState() == null) {
                    student.setEntityState(new EntityState());
                }
                BehaviorTree tree = StudentBehaviorTreeBuilder.buildTree(student);
                student.setBehaviorTree(tree);
            }
        }
        if (staff != null) {
            for (Staff staffMember : staff.values()) {
                if (staffMember.getEntityState() == null) {
                    staffMember.setEntityState(new EntityState());
                }
                BehaviorTree tree = TeacherBehaviorTreeBuilder.buildTree(staffMember);
                staffMember.setBehaviorTree(tree);
            }
        }
    }

    /**
     * Initializes entity states for all students.
     */
    public void initializeStudentStates() {
        if (students == null) {
            return;
        }

        for (Student student : students.values()) {
            // Ensure entity state exists
            if (student.getEntityState() == null) {
                student.setEntityState(new EntityState());
            }

            // Assign lunch period (alternating A/B based on grade)
            String grade = student.studentStatistics.getGradeLevel();
            String lunchPeriod = assignLunchPeriod(grade);
            student.getEntityState().setLunchPeriod(lunchPeriod);

            // Determine off-campus lunch eligibility for upperclassmen
            student.getEntityState().setCanAffordOffCampus(
                    determineOffCampusEligibility(grade,
                            student.studentStatistics.getIncomeLevel()));

            // Build and assign behavior tree
            BehaviorTree tree = StudentBehaviorTreeBuilder.buildTree(student);
            student.setBehaviorTree(tree);
        }
    }

    /**
     * Initializes entity states for all staff.
     */
    public void initializeStaffStates() {
        if (staff == null) {
            return;
        }

        for (Staff staffMember : staff.values()) {
            if (staffMember.getEntityState() == null) {
                staffMember.setEntityState(new EntityState());
            }

            // Set initial location to assigned room
            Room assignedRoom = school.getClassroomByStaff(staffMember);
            if (assignedRoom != null) {
                staffMember.getEntityState().setCurrentRoom(assignedRoom);
                staffMember.getEntityState().setExpectedRoom(assignedRoom);
            }

            // Build and assign the teacher decision tree
            BehaviorTree tree = TeacherBehaviorTreeBuilder.buildTree(staffMember);
            staffMember.setBehaviorTree(tree);
        }
    }

    /**
     * Assigns a lunch period based on grade level.
     * Freshman and Sophomores typically get A lunch,
     * Juniors and Seniors get B lunch.
     *
     * @param gradeLevel the student's grade level
     * @return "A" or "B"
     */
    private String assignLunchPeriod(String gradeLevel) {
        if (gradeLevel == null) {
            return "A";
        }

        switch (gradeLevel) {
            case "Freshman":
            case "Sophomore":
                return "A";
            case "Junior":
            case "Senior":
                return "B";
            default:
                return "A";
        }
    }

    /**
     * Determines whether an upperclassman can afford to leave campus for
     * lunch.  Freshmen and Sophomores are never eligible.  For Juniors and
     * Seniors, high-income students always qualify, middle-income students
     * qualify with roughly 50% probability (rolled once and cached), and
     * low-income students never qualify.
     */
    private boolean determineOffCampusEligibility(String gradeLevel,
                                                  String incomeLevel) {
        if (gradeLevel == null || incomeLevel == null) {
            return false;
        }
        boolean isUpperclassman = gradeLevel.equals("Junior")
                || gradeLevel.equals("Senior");
        if (!isUpperclassman) {
            return false;
        }
        return switch (incomeLevel) {
            case "high" -> true;
            case "middle" -> GameRandom.nextDouble() < 0.5;
            default -> false;
        };
    }

    /**
     * Updates all student expected locations based on the current period.
     *
     * @param currentPeriod the current period number (1-4)
     */
    public void updateStudentLocations(int currentPeriod) {
        if (students == null || currentPeriod <= 0) {
            return;
        }

        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }

            // Get scheduled room for this period
            StudentSchedule schedule = student.studentStatistics.getStudentSchedule();
            if (schedule == null) {
                continue;
            }

            String semester = time.getCurrentSemester();
            StudentBlock block = schedule.getByBlockNumber(currentPeriod, semester);
            if (block != null && block.getRoom() != null) {
                state.setExpectedRoom(block.getRoom());
            } else {
                Room freeRoom = getFreePeriodRoom();
                if (freeRoom != null) {
                    state.setExpectedRoom(freeRoom);
                }
            }
        }
    }

    /**
     * Places all students in their first period classrooms.
     * Call this at the start of the school day.
     */
    public void placeStudentsAtStartOfDay() {
        if (students == null) {
            return;
        }

        String semester = time.getCurrentSemester();

        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }

            // Reset state for new day
            state.resetForNewDay();

            // Get first period room for the current semester
            StudentSchedule schedule = student.studentStatistics.getStudentSchedule();
            Room room = null;
            if (schedule != null) {
                StudentBlock block = schedule.getByBlockNumber(1, semester);
                if (block != null) {
                    room = block.getRoom();
                }
            }

            if (room == null) {
                room = getFreePeriodRoom();
            }

            if (room != null) {
                state.setCurrentRoom(room);
                state.setExpectedRoom(room);
                state.setCurrentActivity(ActivityType.IDLE);
            }
        }
    }

    /**
     * Places all staff in their assigned rooms.
     * Call this at the start of the school day.
     */
    public void placeStaffAtStartOfDay() {
        if (staff == null) {
            return;
        }

        for (Staff staffMember : staff.values()) {
            EntityState state = staffMember.getEntityState();
            if (state == null) {
                continue;
            }

            state.resetForNewDay();

            Room assignedRoom = school.getClassroomByStaff(staffMember);
            if (assignedRoom != null) {
                state.setCurrentRoom(assignedRoom);
                state.setExpectedRoom(assignedRoom);
                state.setCurrentActivity(ActivityType.TEACHING);
            }
        }
    }

    /**
     * Returns a random common-area room (library, courtyard, lunchroom, or
     * hallway) for students with a free period.
     */
    private Room getFreePeriodRoom() {
        if (school == null) {
            return null;
        }
        List<Room> candidates = new ArrayList<>();
        addIfPresent(candidates, school.getLibraries());
        addIfPresent(candidates, school.getCourtyards());
        addIfPresent(candidates, school.getLunchrooms());
        addIfPresent(candidates, school.getHallways());
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(GameRandom.nextInt(candidates.size()));
    }

    private static void addIfPresent(List<Room> list, Room[] rooms) {
        if (rooms != null) {
            Collections.addAll(list, rooms);
        }
    }

    /**
     * Gets statistics about current entity states.
     *
     * @return a string with state statistics
     */
    public String getStateStatistics() {
        if (students == null) {
            return "No students loaded";
        }

        int attending = 0;
        int daydreaming = 0;
        int socializing = 0;
        int transitioning = 0;
        int bathroom = 0;
        int skipping = 0;
        int other = 0;

        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }

            ActivityType activity = state.getCurrentActivity();
            if (activity == null) {
                other++;
                continue;
            }

            switch (activity) {
                case ATTENDING_CLASS:
                case TAKING_NOTES:
                case ASKING_QUESTION:
                    attending++;
                    break;
                case DAYDREAMING:
                    daydreaming++;
                    break;
                case SOCIALIZING:
                case PASSING_NOTE:
                case WHISPERING:
                    socializing++;
                    break;
                case TRANSITIONING:
                    transitioning++;
                    break;
                case IN_BATHROOM:
                    bathroom++;
                    break;
                case SKIPPING:
                    skipping++;
                    break;
                default:
                    other++;
                    break;
            }
        }

        return String.format(
                "Students - Attending: %d, Daydreaming: %d, Socializing: %d, " +
                        "Transitioning: %d, Bathroom: %d, Skipping: %d, Other: %d",
                attending, daydreaming, socializing, transitioning, bathroom, skipping, other);
    }
}
