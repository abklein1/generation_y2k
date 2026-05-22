package save;

import entity.ActivityType;
import entity.Staff;
import entity.StandardSchool;
import entity.Student;
import entity.Time;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utility.GameRandom;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("SaveGameService")
class SaveGameServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should round-trip save data and random state")
    void testSaveGameRoundTrip() throws Exception {
        GameRandom.reset();
        GameRandom.initialize(424242L);
        GameRandom.nextInt(1000);
        GameRandom.RandomState randomState = GameRandom.captureState();
        int expectedNextRoll = GameRandom.nextInt(1000);

        SaveGameData data = new SaveGameData(424242L, randomState,
                new Time(), null, new StandardSchool(),
                new HashMap<Integer, Student>(), new HashMap<Integer, Staff>(),
                null, new SocialLinkSnapshot(),
                new SimulationRuntimeSnapshot(true, 2, 1, 10, 1, 8, 3, false, true),
                null);

        Path savePath = tempDir.resolve("round-trip.dat");
        SaveGameService.save(data, savePath);
        SaveGameData loaded = SaveGameService.load(savePath);

        assertEquals(SaveGameData.FORMAT_VERSION, loaded.getFormatVersion());
        assertEquals(424242L, loaded.getWorldSeed());
        assertNotNull(loaded.getTime());
        assertEquals(10, loaded.getRuntime().getCurrentTick());

        GameRandom.restoreState(loaded.getRandomState());
        assertEquals(expectedNextRoll, GameRandom.nextInt(1000),
                "Loaded save should restore the RNG stream position");
    }

    @Test
    @DisplayName("Should preserve non-empty checkpoint state")
    void testSaveGamePreservesCheckpointState() throws Exception {
        GameRandom.reset();
        GameRandom.initialize(13579L);
        GameRandom.nextDouble();
        GameRandom.RandomState randomState = GameRandom.captureState();
        int expectedNextRoll = GameRandom.nextInt(10_000);

        Time time = new Time();
        time.stepForwardMinutes(95);
        time.incrementDayCounter();

        HashMap<Integer, Student> students = new HashMap<>();
        Student student = new Student();
        student.studentName.setFirstName("Casey");
        student.studentName.setLastName("Checkpoint");
        student.studentStatistics.setGradeLevel("Junior");
        student.getEntityState().setCurrentActivity(ActivityType.TEXTING);
        student.getEntityState().setHunger(42.5);
        student.getEntityState().setFloorPosition(3, 4);
        students.put(7, student);

        HashMap<Integer, Staff> staff = new HashMap<>();
        Staff teacher = new Staff();
        teacher.teacherName.setFirstName("Morgan");
        teacher.teacherName.setLastName("Teacher");
        teacher.getEntityState().setCurrentActivity(ActivityType.TEACHING);
        staff.put(2, teacher);

        SocialLinkSnapshot socialLinks = new SocialLinkSnapshot();
        socialLinks.addEdge(7, 8, 88.5);
        socialLinks.putCatalysts(Map.of("7:8", "Casey and a friend passed the save/load test."));

        SimulationRuntimeSnapshot runtime = new SimulationRuntimeSnapshot(
                true, 4, 1, 123, 2, 9, 44, true, false);
        SaveGameData data = new SaveGameData(13579L, randomState, time, null,
                new StandardSchool(), students, staff, null, socialLinks, runtime,
                null);

        Path savePath = tempDir.resolve("checkpoint-state.dat");
        SaveGameService.save(data, savePath);
        SaveGameData loaded = SaveGameService.load(savePath);

        assertEquals(13579L, loaded.getWorldSeed());
        assertEquals(time.getFormattedDate(), loaded.getTime().getFormattedDate());
        assertEquals(time.getDayCounter(), loaded.getTime().getDayCounter());
        assertEquals("Casey", loaded.getStudents().get(7).studentName.getFirstName());
        assertEquals(ActivityType.TEXTING,
                loaded.getStudents().get(7).getEntityState().getCurrentActivity());
        assertEquals(42.5, loaded.getStudents().get(7).getEntityState().getHunger());
        assertEquals(3, loaded.getStudents().get(7).getEntityState().getFloorPosition()[0]);
        assertEquals(4, loaded.getStudents().get(7).getEntityState().getFloorPosition()[1]);
        assertEquals("Morgan", loaded.getStaff().get(2).teacherName.getFirstName());
        assertEquals(ActivityType.TEACHING,
                loaded.getStaff().get(2).getEntityState().getCurrentActivity());
        assertEquals(1, loaded.getSocialLinks().getEdges().size());
        assertEquals(88.5, loaded.getSocialLinks().getEdges().get(0).getWeight());
        assertEquals("Casey and a friend passed the save/load test.",
                loaded.getSocialLinks().getCatalysts().get("7:8"));
        assertRuntimeSnapshotEquals(runtime, loaded.getRuntime());

        GameRandom.restoreState(loaded.getRandomState());
        assertEquals(expectedNextRoll, GameRandom.nextInt(10_000),
                "Loaded checkpoint should continue random events from the saved point");
    }

    @Test
    @DisplayName("Should reject unsupported save format versions")
    void testLoadRejectsUnsupportedSaveFormatVersion() throws Exception {
        SaveGameData data = new SaveGameData(1L, GameRandom.captureState(),
                new Time(), null, new StandardSchool(),
                new HashMap<Integer, Student>(), new HashMap<Integer, Staff>(),
                null, new SocialLinkSnapshot(),
                new SimulationRuntimeSnapshot(true, 2, 1, 0, 0, 8, -1, false, false),
                null);
        Path savePath = tempDir.resolve("unsupported-format.dat");
        SaveGameService.save(data, savePath);

        corruptFormatVersion(savePath);

        java.io.IOException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                java.io.IOException.class,
                () -> SaveGameService.load(savePath));
        org.junit.jupiter.api.Assertions.assertTrue(
                thrown.getMessage().contains("Unsupported save format version"));
    }

    private void assertRuntimeSnapshotEquals(SimulationRuntimeSnapshot expected,
            SimulationRuntimeSnapshot actual) {
        assertEquals(expected.isPaused(), actual.isPaused());
        assertEquals(expected.getTicksPerUpdate(), actual.getTicksPerUpdate());
        assertEquals(expected.getMinutesPerTick(), actual.getMinutesPerTick());
        assertEquals(expected.getCurrentTick(), actual.getCurrentTick());
        assertEquals(expected.getCurrentTransitionIndex(), actual.getCurrentTransitionIndex());
        assertEquals(expected.getLastProcessedMonth(), actual.getLastProcessedMonth());
        assertEquals(expected.getLastHomeworkAssignmentDay(), actual.getLastHomeworkAssignmentDay());
        assertEquals(expected.wasLunchA(), actual.wasLunchA());
        assertEquals(expected.wasLunchB(), actual.wasLunchB());
    }

    private void corruptFormatVersion(Path savePath) throws Exception {
        SaveGameData loaded = SaveGameService.load(savePath);
        java.lang.reflect.Field formatVersion = SaveGameData.class.getDeclaredField("formatVersion");
        formatVersion.setAccessible(true);
        formatVersion.setInt(loaded, SaveGameData.FORMAT_VERSION + 1);
        SaveGameService.save(loaded, savePath);
    }
}
