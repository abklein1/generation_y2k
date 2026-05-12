package save;

import entity.Staff;
import entity.StandardSchool;
import entity.Student;
import entity.Time;
import entity.Town;
import utility.GameRandom;
import utility.RoomConnector;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;

public class SaveGameData implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int FORMAT_VERSION = 1;

    private final int formatVersion;
    private final LocalDateTime savedAt;
    private final long worldSeed;
    private final GameRandom.RandomState randomState;
    private final Time time;
    private final Town town;
    private final StandardSchool standardSchool;
    private final HashMap<Integer, Student> students;
    private final HashMap<Integer, Staff> staff;
    private final RoomConnector roomConnector;
    private final SocialLinkSnapshot socialLinks;
    private final SimulationRuntimeSnapshot runtime;

    public SaveGameData(long worldSeed, GameRandom.RandomState randomState,
            Time time, Town town, StandardSchool standardSchool,
            HashMap<Integer, Student> students, HashMap<Integer, Staff> staff,
            RoomConnector roomConnector, SocialLinkSnapshot socialLinks,
            SimulationRuntimeSnapshot runtime) {
        this.formatVersion = FORMAT_VERSION;
        this.savedAt = LocalDateTime.now();
        this.worldSeed = worldSeed;
        this.randomState = randomState;
        this.time = time;
        this.town = town;
        this.standardSchool = standardSchool;
        this.students = students;
        this.staff = staff;
        this.roomConnector = roomConnector;
        this.socialLinks = socialLinks;
        this.runtime = runtime;
    }

    public int getFormatVersion() {
        return formatVersion;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public long getWorldSeed() {
        return worldSeed;
    }

    public GameRandom.RandomState getRandomState() {
        return randomState;
    }

    public Time getTime() {
        return time;
    }

    public Town getTown() {
        return town;
    }

    public StandardSchool getStandardSchool() {
        return standardSchool;
    }

    public HashMap<Integer, Student> getStudents() {
        return students;
    }

    public HashMap<Integer, Staff> getStaff() {
        return staff;
    }

    public RoomConnector getRoomConnector() {
        return roomConnector;
    }

    public SocialLinkSnapshot getSocialLinks() {
        return socialLinks;
    }

    public SimulationRuntimeSnapshot getRuntime() {
        return runtime;
    }
}
