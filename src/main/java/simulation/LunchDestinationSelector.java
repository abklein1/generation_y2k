package simulation;

import entity.Rooms.Courtyard;
import entity.Rooms.Lunchroom;
import entity.Rooms.OffCampus;
import entity.Rooms.Room;
import entity.EntityState;
import entity.StandardSchool;
import entity.Student;
import utility.GameRandom;
import utility.SocialLinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Selects where a student eats lunch based on off-campus eligibility,
 * social links, and room availability.
 */
public class LunchDestinationSelector {

    private final StandardSchool school;
    private final SocialLinkConnector socialLinkConnector;

    public LunchDestinationSelector(StandardSchool school,
                                    SocialLinkConnector socialLinkConnector) {
        this.school = school;
        this.socialLinkConnector = socialLinkConnector;
    }

    /**
     * Picks a lunch destination for the given student.
     *
     * @param student the student to place
     * @return the room the student should eat lunch in
     */
    public Room selectDestination(Student student) {
        if (isEligibleForOffCampus(student)) {
            return school.getOffCampus();
        }
        return selectOnCampusDestination(student);
    }

    private boolean isEligibleForOffCampus(Student student) {
        String grade = student.studentStatistics.getGradeLevel();
        if (grade == null) {
            return false;
        }
        boolean isUpperclassman = grade.equals("Junior") || grade.equals("Senior");
        if (!isUpperclassman) {
            return false;
        }

        EntityState state = student.getEntityState();
        if (state == null) {
            return false;
        }
        return state.canAffordOffCampus();
    }

    /**
     * Chooses among lunchrooms and courtyards, weighted by where the
     * student's friends have already been placed this lunch wave.
     */
    private Room selectOnCampusDestination(Student student) {
        List<Room> candidates = buildCandidateList();
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1 || socialLinkConnector == null) {
            return candidates.get(GameRandom.nextInt(candidates.size()));
        }

        Map<Room, Double> weights = new HashMap<>();
        for (Room room : candidates) {
            weights.put(room, 1.0);
        }

        List<Student> friends = student.studentStatistics.getFriendsInSchool();
        String myLunch = student.getEntityState() != null
                ? student.getEntityState().getLunchPeriod() : "A";

        for (Student friend : friends) {
            EntityState friendState = friend.getEntityState();
            if (friendState == null || !friendState.isAtLunch()) {
                continue;
            }
            String friendLunch = friendState.getLunchPeriod();
            if (!myLunch.equalsIgnoreCase(friendLunch)) {
                continue;
            }
            Room friendRoom = friendState.getCurrentRoom();
            if (friendRoom == null || friendRoom instanceof OffCampus) {
                continue;
            }
            if (weights.containsKey(friendRoom)) {
                double score = socialLinkConnector.getSocialScore(student, friend);
                if (score > 0) {
                    weights.merge(friendRoom, score, Double::sum);
                }
            }
        }

        return weightedRandomPick(candidates, weights);
    }

    private List<Room> buildCandidateList() {
        List<Room> candidates = new ArrayList<>();
        Lunchroom[] lunchrooms = school.getLunchrooms();
        if (lunchrooms != null) {
            for (Lunchroom lr : lunchrooms) {
                if (lr != null) {
                    candidates.add(lr);
                }
            }
        }
        Courtyard[] courtyards = school.getCourtyards();
        if (courtyards != null) {
            for (Courtyard cy : courtyards) {
                if (cy != null) {
                    candidates.add(cy);
                }
            }
        }
        return candidates;
    }

    private Room weightedRandomPick(List<Room> candidates,
                                    Map<Room, Double> weights) {
        double totalWeight = 0;
        for (Room room : candidates) {
            totalWeight += weights.getOrDefault(room, 1.0);
        }
        double roll = GameRandom.nextDouble() * totalWeight;
        double cumulative = 0;
        for (Room room : candidates) {
            cumulative += weights.getOrDefault(room, 1.0);
            if (roll < cumulative) {
                return room;
            }
        }
        return candidates.get(candidates.size() - 1);
    }
}
