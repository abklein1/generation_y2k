package simulation;

import entity.*;
import entity.Rooms.Room;
import utility.GameLogger;

import java.util.HashMap;

/**
 * Manages the lifecycle of students on room OccupancyGrids.
 * Handles placing students when they arrive at a room, removing them
 * when they leave, and transferring them room-to-room during transitions.
 */
public class RoomOccupancyManager {

    private final StandardSchool school;

    public RoomOccupancyManager(StandardSchool school) {
        this.school = school;
    }

    /**
     * Initializes OccupancyGrids for every room in the school.
     */
    public void initializeAllGrids() {
        for (Room room : getAllRooms()) {
            if (room.getFloorGrid() == null) {
                room.initializeFloorGrid();
            }
        }
    }

    /**
     * Places all students into their first-period classroom grids.
     * Called at the start of the school day after EntityStateManager places
     * students logically (sets currentRoom/expectedRoom).
     */
    public void placeStudentsForStartOfDay(HashMap<Integer, Student> students) {
        if (students == null) {
            return;
        }
        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null || state.getCurrentRoom() == null) {
                continue;
            }
            enterRoom(student, state.getCurrentRoom());
        }
    }

    /**
     * Places all students on their scheduled room's grid for the given period
     * and semester. Removes them from whatever room they were in previously.
     */
    public void placeStudentsForPeriod(HashMap<Integer, Student> students,
                                       int period, String semester) {
        if (students == null || period <= 0) {
            return;
        }
        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null) {
                continue;
            }

            Room scheduled = getStudentRoomForPeriod(student, period, semester);
            if (scheduled == null) {
                continue;
            }

            removeStudentFromCurrentRoom(student);

            state.setCurrentRoom(scheduled);
            state.setExpectedRoom(scheduled);
            enterRoom(student, scheduled);
        }
    }

    /**
     * Removes a student from their current room's OccupancyGrid.
     */
    public void removeStudentFromCurrentRoom(Student student) {
        if (student == null) {
            return;
        }
        EntityState state = student.getEntityState();
        if (state == null) {
            return;
        }
        Room room = state.getCurrentRoom();
        if (room != null) {
            OccupancyGrid grid = room.getFloorGrid();
            if (grid != null) {
                grid.remove(student);
            }
        }
        state.setFloorPosition(null);
    }

    /**
     * Places a student on a room's OccupancyGrid at a free cell.
     * Updates the student's floorPosition on their EntityState.
     *
     * @return true if placement succeeded
     */
    public boolean enterRoom(Student student, Room room) {
        if (student == null || room == null) {
            return false;
        }
        OccupancyGrid grid = room.getFloorGrid();
        if (grid == null) {
            room.initializeFloorGrid();
            grid = room.getFloorGrid();
        }
        if (grid == null) {
            return false;
        }

        int[] cell = grid.findEmpty();
        if (cell == null) {
            GameLogger.logDebug("OccupancyGrid full in " + room.getRoomName()
                    + " (" + grid.getOccupantCount() + "/" + grid.capacity() + ")");
            return false;
        }

        grid.place(student, cell[0], cell[1]);
        EntityState state = student.getEntityState();
        if (state != null) {
            state.setFloorPosition(cell);
            state.setCurrentRoom(room);
        }
        return true;
    }

    /**
     * Atomically transfers a student from one room's grid to another.
     *
     * @return true if the transfer succeeded
     */
    public boolean transferStudent(Student student, Room fromRoom, Room toRoom) {
        if (fromRoom != null) {
            OccupancyGrid fromGrid = fromRoom.getFloorGrid();
            if (fromGrid != null) {
                fromGrid.remove(student);
            }
        }
        return enterRoom(student, toRoom);
    }

    /**
     * Clears all OccupancyGrids (for day reset).
     */
    public void clearAllGrids() {
        for (Room room : getAllRooms()) {
            OccupancyGrid grid = room.getFloorGrid();
            if (grid != null) {
                grid.clear();
            }
        }
    }

    /**
     * Rebuilds transient room grids from persisted entity locations.
     */
    public void restoreCurrentOccupancy(HashMap<Integer, Student> students) {
        initializeAllGrids();
        clearAllGrids();
        if (students == null) {
            return;
        }
        for (Student student : students.values()) {
            EntityState state = student.getEntityState();
            if (state == null || state.getCurrentRoom() == null) {
                continue;
            }
            Room room = state.getCurrentRoom();
            OccupancyGrid grid = room.getFloorGrid();
            int[] savedPosition = state.getFloorPosition();
            if (grid != null && savedPosition != null
                    && grid.place(student, savedPosition[0], savedPosition[1])) {
                continue;
            }
            enterRoom(student, room);
        }
    }

    private Room getStudentRoomForPeriod(Student student, int period,
                                        String semester) {
        StudentSchedule schedule = student.studentStatistics.getStudentSchedule();
        if (schedule == null) {
            return null;
        }
        StudentBlock block = schedule.getByBlockNumber(period, semester);
        return block != null ? block.getRoom() : null;
    }

    private Room[] getAllRooms() {
        if (school == null) {
            return new Room[0];
        }
        java.util.List<Room> all = new java.util.ArrayList<>();
        addIfNotNull(all, school.getClassrooms());
        addIfNotNull(all, school.getPortables());
        addIfNotNull(all, school.getArtStudios());
        addIfNotNull(all, school.getDramaRooms());
        addIfNotNull(all, school.getMusicRooms());
        addIfNotNull(all, school.getAthleticFields());
        addIfNotNull(all, school.getAuditoriums());
        addIfNotNull(all, school.getBreakrooms());
        addIfNotNull(all, school.getBathrooms());
        addIfNotNull(all, school.getComputerLabs());
        addIfNotNull(all, school.getConferenceRooms());
        addIfNotNull(all, school.getCourtyards());
        addIfNotNull(all, school.getScienceLabs());
        addIfNotNull(all, school.getGyms());
        addIfNotNull(all, school.getLibraries());
        addIfNotNull(all, school.getVocationalRooms());
        addIfNotNull(all, school.getHallways());
        addIfNotNull(all, school.getUtilityrooms());
        addIfNotNull(all, school.getOffices());
        addIfNotNull(all, school.getParkingLots());
        addIfNotNull(all, school.getLunchrooms());
        addIfNotNull(all, school.getLockerRooms());
        return all.toArray(new Room[0]);
    }

    private void addIfNotNull(java.util.List<Room> list, Room[] rooms) {
        if (rooms != null) {
            java.util.Collections.addAll(list, rooms);
        }
    }
}
