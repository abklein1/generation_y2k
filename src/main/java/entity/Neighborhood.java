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
    private int distanceFromSchoolMiles;
    private final List<Student> studentsInSchool;
    private final List<Student> siblingsNotInSchool;
    private final List<Staff> staff;

    public Neighborhood(String name, String wealthLevel, int populationCapacity) {
        this(name, wealthLevel, populationCapacity, 0);
    }

    public Neighborhood(String name, String wealthLevel, int populationCapacity, int distanceFromSchoolMiles) {
        this.name = name;
        this.wealthLevel = wealthLevel;
        this.populationCapacity = populationCapacity;
        this.distanceFromSchoolMiles = distanceFromSchoolMiles;
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

    public int getDistanceFromSchoolMiles() {
        return distanceFromSchoolMiles;
    }

    public void setDistanceFromSchoolMiles(int distanceFromSchoolMiles) {
        this.distanceFromSchoolMiles = distanceFromSchoolMiles;
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
                ", distanceFromSchoolMiles=" + distanceFromSchoolMiles +
                ", currentPopulation=" + getCurrentPopulation() +
                '}';
    }
}
