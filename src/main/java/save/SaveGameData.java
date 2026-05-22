package save;

import entity.Radio.Radio;
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
    private static final long serialVersionUID = 2L;
    public static final int FORMAT_VERSION = 2;

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
    private final Radio radio;

    public SaveGameData(long worldSeed, GameRandom.RandomState randomState,
            Time time, Town town, StandardSchool standardSchool,
            HashMap<Integer, Student> students, HashMap<Integer, Staff> staff,
            RoomConnector roomConnector, SocialLinkSnapshot socialLinks,
            SimulationRuntimeSnapshot runtime, Radio radio) {
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
        this.radio = radio;
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

    /**
     * @return the radio broadcast roster, or {@code null} for save files
     *         created before format version 2.
     */
    public Radio getRadio() {
        return radio;
    }
}
