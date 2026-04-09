package entity;

import utility.GameRandom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A 2D spatial grid that tracks which students physically occupy a room.
 * Enforces strict one-student-per-cell collision. Provides the live occupant
 * roster that replaces the previously-unused {@code Room.getStudents()} list.
 */
public class OccupancyGrid implements Serializable {

    private final int rows;
    private final int cols;
    private final Student[][] cells;
    private final Map<Student, int[]> positions;

    public OccupancyGrid(int rows, int cols) {
        this.rows = Math.max(1, rows);
        this.cols = Math.max(1, cols);
        this.cells = new Student[this.rows][this.cols];
        this.positions = new HashMap<>();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    /**
     * Places a student at the given cell if it is empty.
     *
     * @return true if placement succeeded
     */
    public boolean place(Student student, int row, int col) {
        if (student == null || outOfBounds(row, col)) {
            return false;
        }
        if (cells[row][col] != null) {
            return false;
        }
        // Remove from old position if already on this grid
        if (positions.containsKey(student)) {
            int[] old = positions.get(student);
            cells[old[0]][old[1]] = null;
        }
        cells[row][col] = student;
        positions.put(student, new int[]{row, col});
        return true;
    }

    /**
     * Removes a student from the grid.
     *
     * @return true if the student was present and removed
     */
    public boolean remove(Student student) {
        if (student == null) {
            return false;
        }
        int[] pos = positions.remove(student);
        if (pos != null) {
            cells[pos[0]][pos[1]] = null;
            return true;
        }
        return false;
    }

    /**
     * Atomically moves a student to a new cell.
     *
     * @return true if the move succeeded (target was empty)
     */
    public boolean move(Student student, int toRow, int toCol) {
        if (student == null || outOfBounds(toRow, toCol)) {
            return false;
        }
        if (cells[toRow][toCol] != null && cells[toRow][toCol] != student) {
            return false;
        }
        int[] old = positions.get(student);
        if (old != null) {
            cells[old[0]][old[1]] = null;
        }
        cells[toRow][toCol] = student;
        positions.put(student, new int[]{toRow, toCol});
        return true;
    }

    /**
     * Finds a random unoccupied cell.
     *
     * @return [row, col] or null if the grid is full
     */
    public int[] findEmpty() {
        int total = rows * cols;
        if (positions.size() >= total) {
            return null;
        }
        int attempts = total * 2;
        for (int i = 0; i < attempts; i++) {
            int r = (int) GameRandom.nextDouble(rows);
            int c = (int) GameRandom.nextDouble(cols);
            if (cells[r][c] == null) {
                return new int[]{r, c};
            }
        }
        // Fallback: linear scan
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] == null) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    /**
     * Finds the nearest unoccupied cell to the given position using a
     * spiral BFS expansion.
     *
     * @return [row, col] or null if the grid is full
     */
    public int[] findEmptyNear(int row, int col) {
        if (!outOfBounds(row, col) && cells[row][col] == null) {
            return new int[]{row, col};
        }
        int maxRadius = Math.max(rows, cols);
        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int dr = -radius; dr <= radius; dr++) {
                for (int dc = -radius; dc <= radius; dc++) {
                    if (Math.abs(dr) != radius && Math.abs(dc) != radius) {
                        continue;
                    }
                    int r = row + dr;
                    int c = col + dc;
                    if (!outOfBounds(r, c) && cells[r][c] == null) {
                        return new int[]{r, c};
                    }
                }
            }
        }
        return findEmpty();
    }

    /**
     * Returns the position of a student on this grid.
     *
     * @return [row, col] or null if the student is not on this grid
     */
    public int[] getPosition(Student student) {
        if (student == null) {
            return null;
        }
        return positions.get(student);
    }

    public boolean isOccupied(int row, int col) {
        if (outOfBounds(row, col)) {
            return true;
        }
        return cells[row][col] != null;
    }

    public boolean contains(Student student) {
        return student != null && positions.containsKey(student);
    }

    /**
     * Returns a snapshot list of all students currently on this grid.
     * This is the live occupant roster that replaces Room.getStudents().
     */
    public List<Student> getOccupants() {
        return new ArrayList<>(positions.keySet());
    }

    public int getOccupantCount() {
        return positions.size();
    }

    /**
     * Removes all students from the grid.
     */
    public void clear() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = null;
            }
        }
        positions.clear();
    }

    /**
     * Returns the total number of cells in the grid.
     */
    public int capacity() {
        return rows * cols;
    }

    private boolean outOfBounds(int row, int col) {
        return row < 0 || row >= rows || col < 0 || col >= cols;
    }
}
