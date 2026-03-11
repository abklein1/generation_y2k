package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a town neighborhood and the residents assigned to it.
 */
public class Neighborhood implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String wealthLevel;
    private int populationCapacity;
    private final List<Student> studentsInSchool;
    private final List<Student> siblingsNotInSchool;
    private final List<Staff> staff;

    public Neighborhood(String name, String wealthLevel, int populationCapacity) {
        this.name = name;
        this.wealthLevel = wealthLevel;
        this.populationCapacity = populationCapacity;
        this.studentsInSchool = new ArrayList<>();
        this.siblingsNotInSchool = new ArrayList<>();
        this.staff = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWealthLevel() {
        return wealthLevel;
    }

    public void setWealthLevel(String wealthLevel) {
        this.wealthLevel = wealthLevel;
    }

    public int getPopulationCapacity() {
        return populationCapacity;
    }

    public void setPopulationCapacity(int populationCapacity) {
        this.populationCapacity = populationCapacity;
    }

    public List<Student> getStudentsInSchool() {
        return new ArrayList<>(studentsInSchool);
    }

    public List<Student> getSiblingsNotInSchool() {
        return new ArrayList<>(siblingsNotInSchool);
    }

    public List<Staff> getStaff() {
        return new ArrayList<>(staff);
    }

    public int getCurrentPopulation() {
        return studentsInSchool.size() + siblingsNotInSchool.size() + staff.size();
    }

    public int getRemainingCapacity() {
        return Math.max(0, populationCapacity - getCurrentPopulation());
    }

    public boolean hasCapacityFor(int additionalResidents) {
        return getCurrentPopulation() + additionalResidents <= populationCapacity;
    }

    public void addResident(Student student) {
        if (student == null) {
            return;
        }
        if (student.isInHighSchool()) {
            if (!studentsInSchool.contains(student)) {
                studentsInSchool.add(student);
            }
        } else if (!siblingsNotInSchool.contains(student)) {
            siblingsNotInSchool.add(student);
        }
    }

    public void addResident(Staff staffMember) {
        if (staffMember != null && !staff.contains(staffMember)) {
            staff.add(staffMember);
        }
    }

    public void clearResidents() {
        studentsInSchool.clear();
        siblingsNotInSchool.clear();
        staff.clear();
    }

    @Override
    public String toString() {
        return "Neighborhood{" +
                "name='" + name + '\'' +
                ", wealthLevel='" + wealthLevel + '\'' +
                ", populationCapacity=" + populationCapacity +
                ", currentPopulation=" + getCurrentPopulation() +
                '}';
    }
}
