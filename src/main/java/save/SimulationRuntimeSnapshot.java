package save;

import java.io.Serializable;

public class SimulationRuntimeSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean paused;
    private final int ticksPerUpdate;
    private final int minutesPerTick;
    private final int currentTick;
    private final int currentTransitionIndex;
    private final int lastProcessedMonth;
    private final int lastHomeworkAssignmentDay;
    private final boolean wasLunchA;
    private final boolean wasLunchB;

    public SimulationRuntimeSnapshot(boolean paused, int ticksPerUpdate,
            int minutesPerTick, int currentTick, int currentTransitionIndex,
            int lastProcessedMonth, int lastHomeworkAssignmentDay,
            boolean wasLunchA, boolean wasLunchB) {
        this.paused = paused;
        this.ticksPerUpdate = ticksPerUpdate;
        this.minutesPerTick = minutesPerTick;
        this.currentTick = currentTick;
        this.currentTransitionIndex = currentTransitionIndex;
        this.lastProcessedMonth = lastProcessedMonth;
        this.lastHomeworkAssignmentDay = lastHomeworkAssignmentDay;
        this.wasLunchA = wasLunchA;
        this.wasLunchB = wasLunchB;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getTicksPerUpdate() {
        return ticksPerUpdate;
    }

    public int getMinutesPerTick() {
        return minutesPerTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public int getCurrentTransitionIndex() {
        return currentTransitionIndex;
    }

    public int getLastProcessedMonth() {
        return lastProcessedMonth;
    }

    public int getLastHomeworkAssignmentDay() {
        return lastHomeworkAssignmentDay;
    }

    public boolean wasLunchA() {
        return wasLunchA;
    }

    public boolean wasLunchB() {
        return wasLunchB;
    }
}
