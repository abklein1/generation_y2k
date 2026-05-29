package entity;
//*******************************************************************

//  entity.StandardSchool.java
//  Description: This is the implementation of a standard school based
//  on the implementation of the school plan interface
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import config.SchoolFundingModel;
import entity.Rooms.*;
import utility.GameLogger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utility.io.ResourceAccess;
import view.GameView;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static constants.SchoolConstants.*;
import static constants.SimConstants.STARTING_YEAR;
import static utility.Randomizer.setRandom;

public class StandardSchool implements SchoolPlan {

    private static final long serialVersionUID = 1L;

    String schoolName;
    String schoolMascot;
    Date schoolFoundedDate;
    String[] schoolColors;
    String[] schoolColorsHex;
    ArtStudio[] artStudios;
    AthleticField[] athleticFields;
    Auditorium[] auditoriums;
    Bathroom[] bathrooms;
    Breakroom[] breakrooms;
    Classroom[] classrooms;
    ComputerLab[] computerLabs;
    Courtyard[] courtyards;
    DramaRoom[] dramaRooms;
    Gym[] gyms;
    Hallway[] hallways;
    LibraryR[] libraries;
    LockerRoom[] lockerRooms;
    Lunchroom[] lunchrooms;
    MusicRoom[] musicRooms;
    Office[] offices;
    ScienceLab[] scienceLabs;
    UtilityRoom[] utilityrooms;
    ConferenceRoom[] conferenceRooms;
    ParkingLot[] parkingLots;
    VocationalRoom[] vocationalRooms;
    Portable[] portables;
    Stairwell[] stairwells;
    OffCampus offCampus;
    HashMap<Integer, Student> freshmanClass = new HashMap<>();
    HashMap<Integer, Student> sophomoreClass = new HashMap<>();
    HashMap<Integer, Student> juniorClass = new HashMap<>();
    HashMap<Integer, Student> seniorClass = new HashMap<>();

    private int numberOfFloors = 1;

    // Funding model for capacity calculations
    private SchoolFundingModel fundingModel = new SchoolFundingModel();

    // Current enrollment tracking
    private int currentEnrollment = 0;

    // Target enrollment from demographics (the intended population size).
    // This is separate from optimalCapacity because the school may have extra
    // classrooms for non-core teaching staff (language teachers, etc.) that
    // increase physical capacity beyond what enrollment should be.
    private int targetEnrollment = 0;

    @Override
    public void setUtilityRooms(int number, GameView view) {
        utilityrooms = new UtilityRoom[number];
        GameLogger.logGeneration("   Generating " + number + " utility rooms...");
        for (int i = 0; i < number; i++) {
            utilityrooms[i] = new UtilityRoom();
            utilityrooms[i].setRoomName("UtilityRoom" + i);
            GameLogger.logGeneration("       Generating " + utilityrooms[i].getRoomName());
            utilityrooms[i].setStudentRestriction(true);
            utilityrooms[i].setDoors(UTILITY_CONNECTION_COUNT);
            utilityrooms[i].setConnections(UTILITY_CONNECTION_COUNT);
            utilityrooms[i].setWindowCount(UTILITY_WINDOW_COUNT);
            utilityrooms[i].setInitialStaff(setRandom(UTILITY_STAFF_LOWER_LIMIT, UTILITY_STAFF_UPPER_LIMIT));
            utilityrooms[i].setStudentCap(setRandom(UTILITY_STUDENT_CAP_LOWER_LIMIT, UTILITY_STUDENT_CAP_UPPER_LIMIT));
            utilityrooms[i].setSeatArrangement();
            utilityrooms[i].setRoomNumber(
                    "U" + i + setRandom(UTILITY_ROOM_NUMBER_LOWER_LIMIT, UTILITY_ROOM_NUMBER_UPPER_LIMIT));
        }
    }

    public String getSchoolName() {
        return this.schoolName;
    }

    public void setSchoolName() {
        this.schoolName = schoolNameLoader();
    }

    public void setSchoolMascot() {
        this.schoolMascot = schoolMascotLoader();
    }

    public String getMascot() {
        return this.schoolMascot;
    }

    // ==================== Funding Model ====================

    /**
     * Gets the school's funding model.
     *
     * @return the funding model
     */
    public SchoolFundingModel getFundingModel() {
        return fundingModel;
    }

    /**
     * Sets the school's funding model.
     *
     * @param fundingModel the funding model to use
     */
    public void setFundingModel(SchoolFundingModel fundingModel) {
        this.fundingModel = fundingModel != null ? fundingModel : new SchoolFundingModel();
    }

    /**
     * Sets the funding level using the enum directly.
     *
     * @param level the funding level
     */
    public void setFundingLevel(SchoolFundingModel.FundingLevel level) {
        this.fundingModel = new SchoolFundingModel(level);
    }

    // ==================== Capacity Methods ====================

    /**
     * Gets the physical capacity of the school - the absolute maximum
     * number of students that could physically fit in classrooms.
     * This does not account for comfort or quality of education.
     *
     * @return the physical capacity
     */
    public int getPhysicalCapacity() {
        if (classrooms == null || classrooms.length == 0) {
            return 0;
        }

        // Each student takes ~7 classes, each classroom can be used 8 periods
        // Physical capacity = (classrooms * periods * maxClassSize) / classesPerStudent
        int classesPerStudent = 7;
        return (classrooms.length * TOTAL_SCHOOL_PERIODS * fundingModel.getMaxClassSize()) / classesPerStudent;
    }

    /**
     * Gets the optimal capacity of the school - the comfortable number
     * of students for quality education based on funding level.
     *
     * @return the optimal capacity
     */
    public int getOptimalCapacity() {
        if (classrooms == null || classrooms.length == 0) {
            return 0;
        }

        // Optimal capacity uses optimal class size from funding model
        int classesPerStudent = 7;
        return (classrooms.length * TOTAL_SCHOOL_PERIODS * fundingModel.getOptimalClassSize()) / classesPerStudent;
    }

    /**
     * Gets the total student capacity.
     * For backward compatibility, this returns the optimal capacity.
     * Use getOptimalCapacity() or getPhysicalCapacity() for specific needs.
     *
     * @return the student capacity
     * @deprecated Use getOptimalCapacity() or getPhysicalCapacity() instead
     */
    @Deprecated
    public int getTotalStudentCapacity() {
        // For backward compatibility, return optimal capacity
        // This removes the arbitrary 55% modifier
        return getOptimalCapacity();
    }

    /**
     * Gets the raw classroom capacity sum (useful for calculations).
     *
     * @return sum of all classroom capacities
     */
    public int getRawClassroomCapacity() {
        if (classrooms == null) {
            return 0;
        }
        int total = 0;
        for (Classroom classroom : classrooms) {
            total += classroom.getStudentCapacity();
        }
        return total;
    }

    /**
     * Gets the current enrollment count.
     *
     * @return number of students currently enrolled
     */
    public int getCurrentEnrollment() {
        return currentEnrollment;
    }

    /**
     * Sets the current enrollment count.
     *
     * @param count the new enrollment count
     */
    public void setCurrentEnrollment(int count) {
        this.currentEnrollment = count;
    }

    /**
     * Gets the target enrollment for this school.
     * This is the intended population size from demographics, which may be
     * lower than optimalCapacity when extra classrooms exist for non-core staff.
     *
     * @return the target enrollment, or optimalCapacity if not explicitly set
     */
    public int getTargetEnrollment() {
        return targetEnrollment > 0 ? targetEnrollment : getOptimalCapacity();
    }

    /**
     * Sets the target enrollment for this school.
     *
     * @param target the intended enrollment from demographics
     */
    public void setTargetEnrollment(int target) {
        this.targetEnrollment = target;
    }

    /**
     * Updates enrollment count based on grade class sizes.
     */
    public void updateEnrollmentFromGrades() {
        this.currentEnrollment = freshmanClass.size() + sophomoreClass.size() +
                juniorClass.size() + seniorClass.size();
    }

    /**
     * Gets the overcrowding level as a ratio.
     * A value of 1.0 means at optimal capacity.
     * Values > 1.0 indicate overcrowding.
     *
     * @return the overcrowding ratio
     */
    public double getOvercrowdingLevel() {
        int optimal = getOptimalCapacity();
        if (optimal <= 0) {
            return 0;
        }
        return (double) currentEnrollment / optimal;
    }

    /**
     * Checks if the school is overcrowded.
     *
     * @return true if enrollment exceeds optimal capacity by more than 10%
     */
    public boolean isOvercrowded() {
        return getOvercrowdingLevel() > 1.1;
    }

    /**
     * Checks if the school can accept more students.
     *
     * @return true if enrollment is below physical capacity
     */
    public boolean canAcceptMoreStudents() {
        if (!fundingModel.isAllowOvercrowding()) {
            return currentEnrollment < getOptimalCapacity();
        }
        return currentEnrollment < fundingModel.getMaxAllowedEnrollment(getPhysicalCapacity());
    }

    /**
     * Gets the number of available spots for new students.
     *
     * @return the number of available spots
     */
    public int getAvailableSpots() {
        int max = fundingModel.isAllowOvercrowding()
                ? fundingModel.getMaxAllowedEnrollment(getPhysicalCapacity())
                : getOptimalCapacity();
        return Math.max(0, max - currentEnrollment);
    }

    /**
     * Gets the minimum staff requirements based on curriculum needs.
     * This is now based on the funding model rather than a fixed modifier.
     *
     * @return minimum number of staff required
     */
    public int getMinimumStaffRequirements() {
        // Use funding model to calculate staff needs based on optimal student capacity
        return fundingModel.calculateMinimumStaff(getOptimalCapacity());
    }

    /**
     * Gets detailed staff requirements based on room types.
     * This provides a more granular breakdown for staffing decisions.
     *
     * @return total staff requirements based on rooms
     */
    public int getDetailedStaffRequirements() {
        int total;
        int class_count;
        int office_count;
        int maint_count;
        int lunch_count = 0;
        int library_count;
        int gym_count = 0;
        int computer_count = 0;
        int art_count = 0;
        int field_count = 0;
        int drama_count;
        int music_count;
        int vocation_count = 0;

        class_count = classrooms != null ? classrooms.length : 0;
        office_count = offices != null ? offices.length / OFFICE_NUMBER_MODIFIER : 0;
        maint_count = utilityrooms != null ? utilityrooms.length + UTILITY_ROOM_NUMBER_MODIFIER : 0;
        library_count = libraries != null ? libraries.length * LIBRARY_ROOM_NUMBER_MODIFIER : 0;
        drama_count = dramaRooms != null ? dramaRooms.length : 0;
        music_count = musicRooms != null ? musicRooms.length : 0;

        if (lunchrooms != null) {
            for (Lunchroom lunchroom : lunchrooms) {
                lunch_count = lunch_count + lunchroom.getStaffCapacity();
            }
        }

        if (gyms != null) {
            for (Gym gym : gyms) {
                gym_count = gym_count + gym.getStaffCapacity();
            }
        }

        if (computerLabs != null) {
            for (ComputerLab computerLab : computerLabs) {
                computer_count = computer_count + computerLab.getStaffCapacity();
            }
        }

        if (artStudios != null) {
            for (ArtStudio artStudio : artStudios) {
                art_count = art_count + artStudio.getStaffCapacity();
            }
        }

        if (athleticFields != null) {
            for (AthleticField field : athleticFields) {
                field_count = field_count + field.getStaffCapacity();
            }
        }

        if (vocationalRooms != null) {
            for (VocationalRoom room : vocationalRooms) {
                vocation_count = vocation_count + room.getStaffCapacity();
            }
        }

        // Base staff from room requirements
        total = class_count + office_count + maint_count +
                lunch_count + library_count + gym_count +
                art_count + field_count + drama_count + music_count + vocation_count;

        // Add percentage based on student capacity using funding model ratio
        total += (int) Math.round(getOptimalCapacity() * fundingModel.getStaffStudentRatio());

        return total;
    }

    private String schoolNameLoader() {
        String schoolName;
        int selection = setRandom(SCHOOL_NAME_SELECTION_LOWER_LIMIT, SCHOOL_NAME_SELECTION_UPPER_LIMIT);
        Object object;
        try {
            try (var reader = ResourceAccess.reader("/Resources/School/highschool_gen.json")) {
                object = new JSONParser().parse(reader);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        JSONObject choices = (JSONObject) object;

        if (selection <= SCHOOL_NAME_PLACES_WEIGHT) {
            Object names1 = choices.get("Place1");
            Object names2 = choices.get("Place2");
            JSONArray names_1 = (JSONArray) names1;
            JSONArray names_2 = (JSONArray) names2;

            selection = setRandom(0, names_1.size() - 1);
            schoolName = names_1.get(selection).toString();

            selection = setRandom(0, names_2.size() - 1);
            schoolName = schoolName + " " + names_2.get(selection).toString();
        } else if (selection <= SCHOOL_NAME_SINGLE_WEIGHT) {
            Object names1 = choices.get("Single");
            JSONArray names_1 = (JSONArray) names1;
            selection = setRandom(0, names_1.size() - 1);
            schoolName = names_1.get(selection).toString();
        } else {
            Object names3 = choices.get("Name");
            Object names4 = choices.get("MiddleInitial");
            Object names5 = choices.get("LastName");
            JSONArray names_3 = (JSONArray) names3;
            JSONArray names_4 = (JSONArray) names4;
            JSONArray names_5 = (JSONArray) names5;

            selection = setRandom(0, names_3.size() - 1);
            schoolName = names_3.get(selection).toString();

            selection = setRandom(0, names_4.size() - 1);
            schoolName = schoolName + " " + names_4.get(selection).toString();

            selection = setRandom(0, names_5.size() - 1);
            schoolName = schoolName + " " + names_5.get(selection).toString();
        }

        return schoolName + " High School";
    }

    // Weighted chance of random mascot
    private String schoolMascotLoader() {
        String pathCSVMascots = "/Resources/Town/mascots.csv";
        List<String> mascots = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        List<Integer> cumulativeCounts = new ArrayList<>();
        int totalSum = 0;
        int randomIdx;
        int insertPoint;

        try (BufferedReader br = new BufferedReader(ResourceAccess.reader(pathCSVMascots))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                mascots.add(values[0]);
                counts.add(Integer.parseInt(values[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        for (int count : counts) {
            totalSum += count;
            cumulativeCounts.add(totalSum);
        }

        randomIdx = setRandom(0, totalSum + 1);
        insertPoint = Collections.binarySearch(cumulativeCounts, randomIdx);
        if (insertPoint < 0) {
            insertPoint = -insertPoint - 1;
        }
        return mascots.get(insertPoint);
    }

    public String getSchoolMascot() {
        return this.schoolMascot;
    }

    public Bathroom[] getBathrooms() {
        return bathrooms;
    }

    /**
     * Gets the school founded date as a formatted string for display.
     *
     * @return the formatted date string (e.g., "September 15, 1975")
     */
    public String getSchoolFoundedYear() {
        if (this.schoolFoundedDate == null) {
            return "--";
        }
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM d, yyyy");
        return displayFormat.format(this.schoolFoundedDate);
    }

    /**
     * Gets the raw Date object for the school founding date.
     *
     * @return the Date object
     */
    public Date getSchoolFoundedDate() {
        return this.schoolFoundedDate;
    }

    public void setSchoolFoundedYear() {
        this.schoolFoundedDate = schoolFoundedDateLoader();
    }

    private Date schoolFoundedDateLoader() {
        int year;
        double random = utility.GameRandom.nextDouble(100);

        if (random < SCHOOL_FOUNDED_YEAR_2000s_CHANCE) {
            // 21% chance: Built after 2000
            year = setRandom(SCHOOL_FOUNDED_2000s_LOWER_LIMIT, STARTING_YEAR);
        } else if (random < SCHOOL_FOUNDED_YEAR_1960s_CHANCE) {
            // 13% chance: Built in the 1960s
            year = setRandom(SCHOOL_FOUNDED_1960s_LOWER_LIMIT, SCHOOL_FOUNDED_1960s_UPPER_LIMIT);
        } else if (random < SCHOOL_FOUNDED_YEAR_1950s_CHANCE) {
            // 12% chance: Built before the 1950s
            year = setRandom(SCHOOL_FOUNDED_1950s_LOWER_LIMIT, SCHOOL_FOUNDED_1950s_UPPER_LIMIT);
        } else {
            // Remaining 54% chance: Built between 1950 and 2000
            year = setRandom(SCHOOL_FOUNDED_OTHER_LOWER_LIMIT, SCHOOL_FOUNDED_OTHER_UPPER_LIMIT);
        }

        // Generate random month (0-11) and day
        int month = setRandom(0, 11);

        // Determine max days for the month
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int day = setRandom(1, maxDay);

        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    @Override
    public void setBathrooms(int number, GameView view) {
        bathrooms = new Bathroom[number];
        GameLogger.logGeneration("   Generating " + number + " bathrooms...");
        for (int i = 0; i < number; i++) {
            bathrooms[i] = new Bathroom();
            int cap = setRandom(BATHROOM_CAPACITY_LOWER_LIMIT, BATHROOM_CAPACITY_UPPER_LIMIT);
            if (i % 2 == 0) {
                bathrooms[i].setRoomName("Female_Bathroom" + i);
                GameLogger.logGeneration("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(true, false);
                bathrooms[i].setConnections(BATHROOM_CONNECTION_COUNT);
                bathrooms[i].setDoors(BATHROOM_CONNECTION_COUNT);
                bathrooms[i].setWindowCount(setRandom(BATHROOM_WINDOW_LOWER_COUNT, BATHROOM_WINDOW_UPPER_COUNT));
                bathrooms[i].setStudentCap(cap);
                bathrooms[i].setInitialStaff(BATHROOM_INITIAL_STAFF);
                bathrooms[i].setStallNumber(setRandom(STALL_NUMBER_LOWER_LIMIT, STALL_NUMBER_UPPER_LIMIT));
                bathrooms[i].setSeatArrangement();
                bathrooms[i]
                        .setRoomNumber("WC" + i + setRandom(BATHROOM_NUMBER_LOWER_LIMIT, BATHROOM_NUMBER_UPPER_LIMIT));
            } else {
                bathrooms[i].setRoomName("Male_Bathroom" + i);
                GameLogger.logGeneration("      Generating " + bathrooms[i].getRoomName());
                bathrooms[i].setRoomRestrictions(false, true);
                bathrooms[i].setConnections(BATHROOM_CONNECTION_COUNT);
                bathrooms[i].setDoors(BATHROOM_CONNECTION_COUNT);
                bathrooms[i].setWindowCount(setRandom(BATHROOM_WINDOW_LOWER_COUNT, BATHROOM_WINDOW_UPPER_COUNT));
                bathrooms[i].setStudentCap(cap);
                bathrooms[i].setStallNumber(setRandom(STALL_NUMBER_LOWER_LIMIT, STALL_NUMBER_UPPER_LIMIT));
                bathrooms[i].setInitialStaff(BATHROOM_INITIAL_STAFF);
                bathrooms[i].setSeatArrangement();
                bathrooms[i]
                        .setRoomNumber("WC" + i + setRandom(BATHROOM_NUMBER_LOWER_LIMIT, BATHROOM_NUMBER_UPPER_LIMIT));
            }
            bathrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
        }
    }

    public Breakroom[] getBreakrooms() {
        return breakrooms;
    }

    @Override
    public void setBreakrooms(int number, GameView view) {
        breakrooms = new Breakroom[number];
        GameLogger.logGeneration("   Generating " + number + " breakroom(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(BREAKROOM_CONNECTION_LOWER_LIMIT, BREAKROOM_CONNECTION_UPPER_LIMIT);
            breakrooms[i] = new Breakroom();
            breakrooms[i].setRoomName("Breakroom" + i);
            GameLogger.logGeneration("      Generating " + breakrooms[i].getRoomName());
            breakrooms[i].setStudentRestriction(true);
            breakrooms[i].setConnections(connectN);
            breakrooms[i].setDoors(connectN);
            breakrooms[i].setWindowCount(setRandom(BREAKROOM_WINDOW_LOWER_COUNT, BREAKROOM_WINDOW_UPPER_COUNT));
            breakrooms[i].setInitialStaff(
                    setRandom(BREAKROOM_INITIAL_STAFF_LOWER_LIMIT, BREAKROOM_INITIAL_STAFF_UPPER_LIMIT));
            breakrooms[i].setStudentCap(BREAKROOM_STUDENT_CAPACITY);
            breakrooms[i]
                    .setRoomNumber("B" + i + setRandom(BREAKROOM_NUMBER_LOWER_LIMIT, BREAKROOM_NUMBER_UPPER_LIMIT));
            breakrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
        }
    }

    public Classroom[] getClassrooms() {
        return classrooms;
    }

    @Override
    public void setClassrooms(int number, GameView view) {
        int decision;
        int dividersAssigned = 0;
        classrooms = new Classroom[number];
        GameLogger.logGeneration("   Generating " + number + " classrooms...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(CLASSROOM_CONNECTION_LOWER_LIMIT, CLASSROOM_CONNECTION_UPPER_LIMIT);
            decision = i % CLASSROOM_WEIGHT;
            classrooms[i] = new Classroom();
            classrooms[i].setRoomName("Classroom" + i);
            GameLogger.logGeneration("      Generating " + classrooms[i].getRoomName());
            classrooms[i].setConnections(connectN);
            classrooms[i].setDoors(connectN);
            classrooms[i].setClassroomType(decision);
            classrooms[i].setInitialStaff(CLASSROOM_INITIAL_STAFF);
            int capacity = getWeightedClassroomCapacity();
            classrooms[i].setStudentCap(capacity);

            // Assign dividers to eligible classrooms (capacity >= minimum and random
            // chance)
            if (capacity >= DIVIDER_ELIGIBLE_MIN_CAPACITY && setRandom(0, 100) < DIVIDER_PROBABILITY) {
                classrooms[i].setHasDivider(true);
                dividersAssigned++;
            }

            classrooms[i].setSeatArrangement();
            classrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            classrooms[i].setRoomNumber(classrooms[i].getClassRoomType() + i
                    + setRandom(CLASSROOM_NUMBER_LOWER_LIMIT, CLASSROOM_NUMBER_UPPER_LIMIT));
        }

        if (dividersAssigned > 0) {
            GameLogger.logGeneration("   " + dividersAssigned + " classrooms equipped with dividers");
        }
    }

    /**
     * Adds additional classrooms to the school dynamically.
     * Used for expansion when scheduling fails due to insufficient capacity.
     *
     * @param additionalCount the number of classrooms to add
     * @param view            the game view for output (can be null for silent
     *                        operation)
     * @return the number of classrooms added
     */
    public int addClassrooms(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = classrooms != null ? classrooms.length : 0;
        int newTotal = currentCount + additionalCount;

        // Create new array and copy existing classrooms
        Classroom[] newClassrooms = new Classroom[newTotal];
        if (classrooms != null) {
            System.arraycopy(classrooms, 0, newClassrooms, 0, currentCount);
        }

        if (view != null) {
            GameLogger.logGeneration("   Expanding school: Adding " + additionalCount + " classrooms...");
        }
        GameLogger.logGeneration("EXPANSION: Adding " + additionalCount + " classrooms (total: " + newTotal + ")");

        // Create new classrooms
        int dividersAssigned = 0;
        for (int i = currentCount; i < newTotal; i++) {
            int connectN = setRandom(CLASSROOM_CONNECTION_LOWER_LIMIT, CLASSROOM_CONNECTION_UPPER_LIMIT);
            int decision = i % CLASSROOM_WEIGHT;
            newClassrooms[i] = new Classroom();
            newClassrooms[i].setRoomName("Classroom" + i);
            if (view != null) {
                GameLogger.logGeneration("      Generating " + newClassrooms[i].getRoomName());
            }
            newClassrooms[i].setConnections(connectN);
            newClassrooms[i].setDoors(connectN);
            newClassrooms[i].setClassroomType(decision);
            newClassrooms[i].setInitialStaff(CLASSROOM_INITIAL_STAFF);
            int capacity = getWeightedClassroomCapacity();
            newClassrooms[i].setStudentCap(capacity);

            // Assign dividers to eligible classrooms
            if (capacity >= DIVIDER_ELIGIBLE_MIN_CAPACITY && setRandom(0, 100) < DIVIDER_PROBABILITY) {
                newClassrooms[i].setHasDivider(true);
                dividersAssigned++;
            }

            newClassrooms[i].setSeatArrangement();
            newClassrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newClassrooms[i].setRoomNumber(newClassrooms[i].getClassRoomType() + i
                    + setRandom(CLASSROOM_NUMBER_LOWER_LIMIT, CLASSROOM_NUMBER_UPPER_LIMIT));
        }

        classrooms = newClassrooms;

        if (view != null && dividersAssigned > 0) {
            GameLogger.logGeneration("   " + dividersAssigned + " new classrooms equipped with dividers");
        }

        // Note: Capacities are calculated on-the-fly via getOptimalCapacity() and
        // getPhysicalCapacity()
        // so they will automatically reflect the new classroom count

        return additionalCount;
    }

    /**
     * Returns a weighted classroom capacity.
     * Distribution: ~40% small (20-25), ~40% medium (26-34), ~20% large (35-40)
     *
     * @return the classroom capacity
     */
    private int getWeightedClassroomCapacity() {
        int roll = setRandom(0, 100);

        if (roll < CLASSROOM_SIZE_SMALL_WEIGHT) {
            // Small classroom (40% chance)
            return setRandom(CLASSROOM_SMALL_CAP_LOWER, CLASSROOM_SMALL_CAP_UPPER);
        } else if (roll < CLASSROOM_SIZE_MEDIUM_WEIGHT) {
            // Medium classroom (40% chance)
            return setRandom(CLASSROOM_MEDIUM_CAP_LOWER, CLASSROOM_MEDIUM_CAP_UPPER);
        } else {
            // Large classroom (20% chance)
            return setRandom(CLASSROOM_LARGE_CAP_LOWER, CLASSROOM_LARGE_CAP_UPPER);
        }
    }

    public ComputerLab[] getComputerLabs() {
        return computerLabs;
    }

    @Override
    public void setComputerLabs(int number, GameView view) {
        computerLabs = new ComputerLab[number];
        GameLogger.logGeneration("   Generating " + number + " Computer lab(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(COMPUTER_CONNECTION_LOWER_LIMIT, COMPUTER_CONNECTION_UPPER_LIMIT);
            computerLabs[i] = new ComputerLab();
            computerLabs[i].setRoomName("ComputerLab" + i);
            GameLogger.logGeneration("      Generating " + computerLabs[i].getRoomName());
            computerLabs[i].setWindowCount(COMPUTER_WINDOW_COUNT);
            computerLabs[i].setConnections(connectN);
            computerLabs[i].setDoors(connectN);
            computerLabs[i]
                    .setInitialStaff(setRandom(COMPUTER_INITIAL_STAFF_LOWER_LIMIT, COMPUTER_INITIAL_STAFF_UPPER_LIMIT));
            computerLabs[i].setStudentCap(
                    setRandom(COMPUTER_STUDENT_CAPACITY_LOWER_LIMIT, COMPUTER_STUDENT_CAPACITY_UPPER_LIMIT));
            computerLabs[i].setSeatArrangement();
            computerLabs[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            computerLabs[i].setRoomNumber("COM" + i);
        }
    }

    public int addComputerLabs(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = computerLabs != null ? computerLabs.length : 0;
        int newTotal = currentCount + additionalCount;
        ComputerLab[] newLabs = new ComputerLab[newTotal];
        if (computerLabs != null) {
            System.arraycopy(computerLabs, 0, newLabs, 0, currentCount);
        }

        for (int i = currentCount; i < newTotal; i++) {
            int connectN = setRandom(COMPUTER_CONNECTION_LOWER_LIMIT, COMPUTER_CONNECTION_UPPER_LIMIT);
            newLabs[i] = new ComputerLab();
            newLabs[i].setRoomName("ComputerLab" + i);
            GameLogger.logGeneration("      Generating " + newLabs[i].getRoomName());
            newLabs[i].setWindowCount(COMPUTER_WINDOW_COUNT);
            newLabs[i].setConnections(connectN);
            newLabs[i].setDoors(connectN);
            newLabs[i].setInitialStaff(setRandom(COMPUTER_INITIAL_STAFF_LOWER_LIMIT, COMPUTER_INITIAL_STAFF_UPPER_LIMIT));
            newLabs[i].setStudentCap(
                    setRandom(COMPUTER_STUDENT_CAPACITY_LOWER_LIMIT, COMPUTER_STUDENT_CAPACITY_UPPER_LIMIT));
            newLabs[i].setSeatArrangement();
            newLabs[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newLabs[i].setRoomNumber("COM" + i);
        }

        computerLabs = newLabs;
        if (view != null) {
            GameLogger.logGeneration("EXPANSION: Added " + additionalCount + " computer lab(s)");
        }
        return additionalCount;
    }

    public Courtyard[] getCourtyards() {
        return courtyards;
    }

    @Override
    public void setCourtyards(int number, GameView view) {
        courtyards = new Courtyard[number];
        GameLogger.logGeneration("   Generating " + number + " courtyard(s)...");
        for (int i = 0; i < number; i++) {
            courtyards[i] = new Courtyard();
            courtyards[i].setRoomName("Courtyard" + i);
            GameLogger.logGeneration("      Generating " + courtyards[i].getRoomName());
            courtyards[i].setWindowCount(COURTYARD_WINDOW_COUNT);
            courtyards[i].setConnections(COURTYARD_CONNECTION_COUNT);
            courtyards[i].setDoors(COURTYARD_CONNECTION_COUNT);
            courtyards[i].setInitialStaff(COURTYARD_INITIAL_STAFF);
            courtyards[i].setStudentCap(
                    setRandom(COURTYARD_STUDENT_CAPACITY_LOWER_LIMIT, COURTYARD_STUDENT_CAPACITY_UPPER_LIMIT));
            courtyards[i].setSeatArrangement();
            courtyards[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            courtyards[i].setRoomNumber("C" + i);
        }
    }

    public Gym[] getGyms() {
        return gyms;
    }

    @Override
    public void setGyms(int number, GameView view) {
        gyms = new Gym[number];
        GameLogger.logGeneration("   Generating " + number + " gym(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(GYM_CONNECTION_LOWER_LIMIT, GYM_CONNECTION_UPPER_LIMIT);
            gyms[i] = new Gym();
            gyms[i].setRoomName("Gym" + i);
            GameLogger.logGeneration("      Generating " + gyms[i].getRoomName());
            gyms[i].setConnections(connectN);
            gyms[i].setDoors(connectN);
            gyms[i].setWindowCount(setRandom(GYM_WINDOW_LOWER_LIMIT, GYM_WINDOW_UPPER_LIMIT));
            gyms[i].setInitialStaff(GYM_INITIAL_STAFF);
            gyms[i].setStudentCap(setRandom(GYM_STUDENT_CAPACITY_LOWER_LIMIT, GYM_STUDENT_CAPACITY_UPPER_LIMIT));
            gyms[i].setSeatArrangement();
            gyms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            gyms[i].setRoomNumber("G" + i + setRandom(GYM_ROOM_NUMBER_LOWER_LIMIT, GYM_ROOM_NUMBER_UPPER_LIMIT));
        }
    }

    public int addGyms(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = gyms != null ? gyms.length : 0;
        int newTotal = currentCount + additionalCount;
        Gym[] newGyms = new Gym[newTotal];
        if (gyms != null) {
            System.arraycopy(gyms, 0, newGyms, 0, currentCount);
        }

        for (int i = currentCount; i < newTotal; i++) {
            int connectN = setRandom(GYM_CONNECTION_LOWER_LIMIT, GYM_CONNECTION_UPPER_LIMIT);
            newGyms[i] = new Gym();
            newGyms[i].setRoomName("Gym" + i);
            GameLogger.logGeneration("      Generating " + newGyms[i].getRoomName());
            newGyms[i].setConnections(connectN);
            newGyms[i].setDoors(connectN);
            newGyms[i].setWindowCount(setRandom(GYM_WINDOW_LOWER_LIMIT, GYM_WINDOW_UPPER_LIMIT));
            newGyms[i].setInitialStaff(GYM_INITIAL_STAFF);
            newGyms[i].setStudentCap(setRandom(GYM_STUDENT_CAPACITY_LOWER_LIMIT, GYM_STUDENT_CAPACITY_UPPER_LIMIT));
            newGyms[i].setSeatArrangement();
            newGyms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newGyms[i].setRoomNumber("G" + i + setRandom(GYM_ROOM_NUMBER_LOWER_LIMIT, GYM_ROOM_NUMBER_UPPER_LIMIT));
        }

        gyms = newGyms;
        if (view != null) {
            GameLogger.logGeneration("EXPANSION: Added " + additionalCount + " gym(s)");
        }
        return additionalCount;
    }

    public void renameGym(Gym gym, String name) {
        gym.setRoomName(name);
    }

    public Hallway[] getHallways() {
        return hallways;
    }

    @Override
    public void setHallways(int number, GameView view) {
        hallways = new Hallway[number];
        GameLogger.logGeneration("   Generating " + number + " hallways...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(HALLWAY_CONNECTION_LOWER_LIMIT, HALLWAY_CONNECTION_UPPER_LIMIT);
            hallways[i] = new Hallway();
            hallways[i].setRoomName("Hallway" + i);
            GameLogger.logGeneration("      Generating " + hallways[i].getRoomName());
            hallways[i].setConnections(connectN);
            hallways[i].setDoors(connectN);
            hallways[i].setWindowCount(setRandom(HALLWAY_WINDOW_LOWER_LIMIT, HALLWAY_WINDOW_UPPER_LIMIT));
            hallways[i].setInitialStaff(HALLWAY_INITIAL_STAFF);
            hallways[i].setStudentCap(
                    setRandom(HALLWAY_STUDENT_CAPACITY_LOWER_LIMIT, HALLWAY_STUDENT_CAPACITY_UPPER_LIMIT));
            hallways[i].setSeatArrangement();
            hallways[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            hallways[i].setRoomNumber("H" + i);
        }
    }

    public LibraryR[] getLibraries() {
        return libraries;
    }

    @Override
    public void setLibraries(int number, GameView view) {
        libraries = new LibraryR[number];
        GameLogger.logGeneration("   Generating " + number + " libraries...");
        for (int i = 0; i < number; i++) {
            int connectorN = setRandom(LIBRARY_CONNECTION_LOWER_LIMIT, LIBRARY_CONNECTION_UPPER_LIMIT);
            libraries[i] = new LibraryR();
            libraries[i].setRoomName("Library" + i);
            GameLogger.logGeneration("      Generating " + libraries[i].getRoomName());
            libraries[i].setWindowCount(setRandom(LIBRARY_WINDOW_LOWER_LIMIT, LIBRARY_WINDOW_UPPER_LIMIT));
            libraries[i].setConnections(connectorN);
            libraries[i].setDoors(connectorN);
            libraries[i].setInitialStaff(LIBRARY_INITIAL_STAFF);
            libraries[i].setStudentCap(
                    setRandom(LIBRARY_STUDENT_CAPACITY_LOWER_LIMIT, LIBRARY_STUDENT_CAPACITY_UPPER_LIMIT));
            libraries[i].setSeatArrangement();
            libraries[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            libraries[i].setRoomNumber("L" + i + setRandom(LIBRARY_NUMBER_LOWER_LIMIT, LIBRARY_NUMBER_UPPER_LIMIT));
        }
    }

    public void renameLibrary(LibraryR libraryR, String name) {
        libraryR.setRoomName(name);
    }

    public Lunchroom[] getLunchrooms() {
        return lunchrooms;
    }

    public OffCampus getOffCampus() {
        if (offCampus == null) {
            offCampus = new OffCampus();
        }
        return offCampus;
    }

    @Override
    public void setLunchrooms(int number, GameView view) {
        lunchrooms = new Lunchroom[number];
        GameLogger.logGeneration("   Generating " + number + " lunchroom(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(LUNCH_CONNECTION_LOWER_LIMIT, LUNCH_CONNECTION_UPPER_LIMIT);
            lunchrooms[i] = new Lunchroom();
            lunchrooms[i].setRoomName("Lunchroom" + i);
            GameLogger.logGeneration("      Generating " + lunchrooms[i].getRoomName());
            lunchrooms[i].setWindowCount(setRandom(LUNCH_WINDOW_LOWER_LIMIT, LUNCH_WINDOW_UPPER_LIMIT));
            lunchrooms[i].setConnections(connectN);
            lunchrooms[i].setDoors(connectN);
            lunchrooms[i].setInitialStaff(setRandom(LUNCH_INITIAL_STAFF_LOWER_LIMIT, LUNCH_INITIAL_STAFF_UPPER_LIMIT));
            lunchrooms[i]
                    .setStudentCap(setRandom(LUNCH_STUDENT_CAPACITY_LOWER_LIMIT, LUNCH_STUDENT_CAPACITY_UPPER_LIMIT));
            lunchrooms[i].setSeatArrangement();
            lunchrooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            lunchrooms[i].setRoomNumber("L" + i + setRandom(LUNCH_NUMBER_LOWER_LIMIT, LUNCH_NUMBER_UPPER_LIMIT));
        }
    }

    public Office[] getOffices() {
        return offices;
    }

    @Override
    public void setOffices(int number, GameView view) {
        offices = new Office[number];
        GameLogger.logGeneration("   Generating " + number + " offices...");
        for (int i = 0; i < number; i++) {
            offices[i] = new Office();
            switch (i) {
                case 0 -> {
                    offices[i].setRoomName("Principal's Office");
                    GameLogger.logGeneration("      Generating Principal's office");
                    offices[i].setDoors(PRINCIPAL_CONNECTION_COUNT);
                    offices[i].setWindowCount(PRINCIPAL_WINDOW_COUNT);
                    offices[i].setInitialStaff(PRINCIPAL_INITIAL_STAFF);
                    offices[i].setInitialStudents(PRINCIPAL_INITIAL_STUDENTS);
                    offices[i].setStudentCap(PRINCIPAL_STUDENT_CAP);
                    offices[i].setConnections(PRINCIPAL_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-100");
                }
                case 1 -> {
                    offices[i].setRoomName("Vice Principal's Office");
                    GameLogger.logGeneration("      Generating Vice Principal's office");
                    offices[i].setDoors(VICE_PRINCIPAL_CONNECTION_COUNT);
                    offices[i].setWindowCount(VICE_PRINCIPAL_WINDOW_COUNT);
                    offices[i].setInitialStaff(VICE_PRINCIPAL_INITIAL_STAFF);
                    offices[i].setInitialStudents(VICE_PRINCIPAL_INITIAL_STUDENTS);
                    offices[i].setStudentCap(VICE_PRINCIPAL_STUDENT_CAP);
                    offices[i].setConnections(VICE_PRINCIPAL_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-101");
                }
                case 2 -> {
                    offices[i].setRoomName("Guidance Councilor's Office");
                    GameLogger.logGeneration("      Generating Councilor's Office");
                    offices[i].setDoors(GUIDANCE_CONNECTION_COUNT);
                    offices[i].setWindowCount(GUIDANCE_WINDOW_COUNT);
                    offices[i].setInitialStaff(GUIDANCE_INITIAL_STAFF);
                    offices[i].setInitialStudents(GUIDANCE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(GUIDANCE_STUDENT_CAP);
                    offices[i].setConnections(GUIDANCE_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-102");
                }
                case 3 -> {
                    offices[i].setRoomName("Front Office");
                    GameLogger.logGeneration("      Generating Front Office");
                    offices[i].setDoors(FRONT_OFFICE_CONNECTION_COUNT);
                    offices[i].setWindowCount(FRONT_OFFICE_WINDOW_COUNT);
                    offices[i].setInitialStaff(FRONT_OFFICE_INITIAL_STAFF);
                    offices[i].setInitialStudents(FRONT_OFFICE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(FRONT_OFFICE_STUDENT_CAP);
                    offices[i].setConnections(FRONT_OFFICE_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-103");
                }
                case 4 -> {
                    offices[i].setRoomName("Nurse's Office");
                    GameLogger.logGeneration("      Generating Nurse's Office");
                    offices[i].setDoors(NURSE_CONNECTION_COUNT);
                    offices[i].setWindowCount(NURSE_WINDOW_COUNT);
                    offices[i].setInitialStaff(NURSE_INITIAL_STAFF);
                    offices[i].setInitialStudents(NURSE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(NURSE_STUDENT_CAP);
                    offices[i].setConnections(NURSE_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-104");
                }
                case 5 -> {
                    offices[i].setRoomName("Guidance Councilor's Office");
                    GameLogger.logGeneration("      Generating Councilor's Office");
                    offices[i].setDoors(GUIDANCE_CONNECTION_COUNT);
                    offices[i].setWindowCount(GUIDANCE_WINDOW_COUNT);
                    offices[i].setInitialStaff(GUIDANCE_INITIAL_STAFF);
                    offices[i].setInitialStudents(GUIDANCE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(GUIDANCE_STUDENT_CAP);
                    offices[i].setConnections(GUIDANCE_CONNECTION_COUNT);
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O-103");
                }
                default -> {
                    offices[i].setRoomName("Office" + i);
                    GameLogger.logGeneration("      Generating " + offices[i].getRoomName());
                    offices[i].setConnections(DEFAULT_OFFICE_CONNECTION_COUNT);
                    offices[i].setDoors(DEFAULT_OFFICE_CONNECTION_COUNT);
                    offices[i].setWindowCount(DEFAULT_OFFICE_WINDOW_COUNT);
                    offices[i].setInitialStaff(DEFAULT_OFFICE_INITIAL_STAFF);
                    offices[i].setInitialStudents(DEFAULT_OFFICE_INITIAL_STUDENTS);
                    offices[i].setStudentCap(
                            setRandom(DEFAULT_OFFICE_STUDENT_CAP_LOWER_LIMIT, DEFAULT_OFFICE_STUDENT_CAP_UPPER_LIMIT));
                    offices[i].setSeatArrangement();
                    offices[i].setRoomNumber("O" + "-1" + i);
                }
            }
            offices[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            offices[i].setStudentRestriction(true);

        }
    }

    public UtilityRoom[] getUtilityrooms() {
        return utilityrooms;
    }

    public ArtStudio[] getArtStudios() {
        return artStudios;
    }

    @Override
    public void setArtStudios(int number, GameView view) {
        artStudios = new ArtStudio[number];
        GameLogger.logGeneration("   Generating " + number + " art studio(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(ART_CONNECTION_LOWER_LIMIT, ART_CONNECTION_UPPER_LIMIT);
            artStudios[i] = new ArtStudio();
            artStudios[i].setRoomName("Art Room" + i);
            GameLogger.logGeneration("      Generating " + artStudios[i].getRoomName());
            artStudios[i].setWindowCount(setRandom(ART_WINDOW_LOWER_LIMIT, ART_WINDOW_UPPER_LIMIT));
            artStudios[i].setConnections(connectN);
            artStudios[i].setDoors(connectN);
            artStudios[i].setInitialStaff(setRandom(ART_INITIAL_STAFF_LOWER_LIMIT, ART_INITIAL_STAFF_UPPER_LIMIT));
            artStudios[i].setStudentCap(setRandom(ART_STUDENT_CAPACITY_LOWER_LIMIT, ART_STUDENT_CAPACITY_UPPER_LIMIT));
            artStudios[i].setSeatArrangement();
            artStudios[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            artStudios[i].setRoomNumber("AT" + i + setRandom(ART_NUMBER_LOWER_LIMIT, ART_NUMBER_UPPER_LIMIT));
        }
    }

    public int addArtStudios(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = artStudios != null ? artStudios.length : 0;
        int newTotal = currentCount + additionalCount;
        ArtStudio[] newStudios = new ArtStudio[newTotal];
        if (artStudios != null) {
            System.arraycopy(artStudios, 0, newStudios, 0, currentCount);
        }

        for (int i = currentCount; i < newTotal; i++) {
            int connectN = setRandom(ART_CONNECTION_LOWER_LIMIT, ART_CONNECTION_UPPER_LIMIT);
            newStudios[i] = new ArtStudio();
            newStudios[i].setRoomName("Art Room" + i);
            GameLogger.logGeneration("      Generating " + newStudios[i].getRoomName());
            newStudios[i].setWindowCount(setRandom(ART_WINDOW_LOWER_LIMIT, ART_WINDOW_UPPER_LIMIT));
            newStudios[i].setConnections(connectN);
            newStudios[i].setDoors(connectN);
            newStudios[i].setInitialStaff(setRandom(ART_INITIAL_STAFF_LOWER_LIMIT, ART_INITIAL_STAFF_UPPER_LIMIT));
            newStudios[i].setStudentCap(setRandom(ART_STUDENT_CAPACITY_LOWER_LIMIT, ART_STUDENT_CAPACITY_UPPER_LIMIT));
            newStudios[i].setSeatArrangement();
            newStudios[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newStudios[i].setRoomNumber("AT" + i + setRandom(ART_NUMBER_LOWER_LIMIT, ART_NUMBER_UPPER_LIMIT));
        }

        artStudios = newStudios;
        if (view != null) {
            GameLogger.logGeneration("EXPANSION: Added " + additionalCount + " art studio(s)");
        }
        return additionalCount;
    }

    public AthleticField[] getAthleticFields() {
        return athleticFields;
    }

    @Override
    public void setAthleticFields(int number, GameView view) {
        athleticFields = new AthleticField[number];
        GameLogger.logGeneration("   Generating " + number + " athletic fields(s)...");
        for (int i = 0; i < number; i++) {
            athleticFields[i] = new AthleticField();
            athleticFields[i].setRoomName("Field" + i);
            GameLogger.logGeneration("      Generating " + athleticFields[i].getRoomName());
            athleticFields[i].setWindowCount(ATHLETIC_WINDOW_COUNT);
            athleticFields[i].setConnections(ATHLETIC_CONNECTION_COUNT);
            athleticFields[i].setDoors(ATHLETIC_CONNECTION_COUNT);
            athleticFields[i]
                    .setInitialStaff(setRandom(ATHLETIC_INITIAL_STAFF_LOWER_LIMIT, ATHLETIC_INITIAL_STAFF_UPPER_LIMIT));
            athleticFields[i].setStudentCap(
                    setRandom(ATHLETIC_STUDENT_CAPACITY_LOWER_LIMIT, ATHLETIC_STUDENT_CAPACITY_UPPER_LIMIT));
            athleticFields[i].setSeatArrangement();
            athleticFields[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            athleticFields[i]
                    .setRoomNumber("F" + i + setRandom(ATHLETIC_NUMBER_LOWER_LIMIT, ATHLETIC_NUMBER_UPPER_LIMIT));
        }
    }

    public Auditorium[] getAuditoriums() {
        return auditoriums;
    }

    @Override
    public void setAuditoriums(int number, GameView view) {
        auditoriums = new Auditorium[number];
        GameLogger.logGeneration("   Generating " + number + " auditorium(s)...");
        for (int i = 0; i < number; i++) {
            auditoriums[i] = new Auditorium();
            auditoriums[i].setRoomName("Auditorium" + i);
            GameLogger.logGeneration("      Generating " + auditoriums[i].getRoomName());
            auditoriums[i].setWindowCount(AUDITORIUM_WINDOW_COUNT);
            auditoriums[i].setConnections(AUDITORIUM_CONNECTION_COUNT);
            auditoriums[i].setDoors(AUDITORIUM_CONNECTION_COUNT);
            auditoriums[i].setInitialStaff(AUDITORIUM_INITIAL_STAFF);
            auditoriums[i].setStudentCap(
                    setRandom(AUDITORIUM_STUDENT_CAPACITY_LOWER_LIMIT, AUDITORIUM_STUDENT_CAPACITY_UPPER_LIMIT));
            auditoriums[i].setSeatArrangement();
            auditoriums[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            auditoriums[i]
                    .setRoomNumber("AD" + i + setRandom(AUDITORIUM_NUMBER_LOWER_LIMIT, AUDITORIUM_NUMBER_UPPER_LIMIT));
        }
    }

    public DramaRoom[] getDramaRooms() {
        return dramaRooms;
    }

    @Override
    public void setDramaRooms(int number, GameView view) {
        dramaRooms = new DramaRoom[number];
        GameLogger.logGeneration("   Generating " + number + " Drama Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(DRAMA_CONNECTION_LOWER_LIMIT, DRAMA_CONNECTION_UPPER_LIMIT);
            dramaRooms[i] = new DramaRoom();
            dramaRooms[i].setRoomName("Drama" + i);
            GameLogger.logGeneration("      Generating " + dramaRooms[i].getRoomName());
            dramaRooms[i].setWindowCount(setRandom(DRAMA_WINDOW_LOWER_LIMIT, DRAMA_WINDOW_UPPER_LIMIT));
            dramaRooms[i].setConnections(connectN);
            dramaRooms[i].setDoors(connectN);
            dramaRooms[i].setInitialStaff(DRAMA_INITIAL_STAFF);
            dramaRooms[i]
                    .setStudentCap(setRandom(DRAMA_STUDENT_CAPACITY_LOWER_LIMIT, DRAMA_STUDENT_CAPACITY_UPPER_LIMIT));
            dramaRooms[i].setSeatArrangement();
            dramaRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            dramaRooms[i].setRoomNumber("D" + i + setRandom(DRAMA_NUMBER_LOWER_LIMIT, DRAMA_NUMBER_UPPER_LIMIT));
        }
    }

    public int addDramaRooms(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = dramaRooms != null ? dramaRooms.length : 0;
        int newTotal = currentCount + additionalCount;
        DramaRoom[] newRooms = new DramaRoom[newTotal];
        if (dramaRooms != null) {
            System.arraycopy(dramaRooms, 0, newRooms, 0, currentCount);
        }

        for (int i = currentCount; i < newTotal; i++) {
            int connectN = setRandom(DRAMA_CONNECTION_LOWER_LIMIT, DRAMA_CONNECTION_UPPER_LIMIT);
            newRooms[i] = new DramaRoom();
            newRooms[i].setRoomName("Drama" + i);
            GameLogger.logGeneration("      Generating " + newRooms[i].getRoomName());
            newRooms[i].setWindowCount(setRandom(DRAMA_WINDOW_LOWER_LIMIT, DRAMA_WINDOW_UPPER_LIMIT));
            newRooms[i].setConnections(connectN);
            newRooms[i].setDoors(connectN);
            newRooms[i].setInitialStaff(DRAMA_INITIAL_STAFF);
            newRooms[i].setStudentCap(setRandom(DRAMA_STUDENT_CAPACITY_LOWER_LIMIT, DRAMA_STUDENT_CAPACITY_UPPER_LIMIT));
            newRooms[i].setSeatArrangement();
            newRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newRooms[i].setRoomNumber("D" + i + setRandom(DRAMA_NUMBER_LOWER_LIMIT, DRAMA_NUMBER_UPPER_LIMIT));
        }

        dramaRooms = newRooms;
        if (view != null) {
            GameLogger.logGeneration("EXPANSION: Added " + additionalCount + " drama room(s)");
        }
        return additionalCount;
    }

    public LockerRoom[] getLockerRooms() {
        return lockerRooms;
    }

    @Override
    public void setLockerRooms(int number, GameView view) {
        lockerRooms = new LockerRoom[number];
        GameLogger.logGeneration("   Generating " + number + " Locker Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(LOCKER_CONNECTION_LOWER_LIMIT, LOCKER_CONNECTION_UPPER_LIMIT);
            lockerRooms[i] = new LockerRoom();
            lockerRooms[i].setRoomName("Locker Room" + i);
            GameLogger.logGeneration("      Generating " + lockerRooms[i].getRoomName());
            lockerRooms[i].setWindowCount(LOCKER_WINDOW_COUNT);
            lockerRooms[i].setConnections(connectN);
            lockerRooms[i].setDoors(connectN);
            lockerRooms[i].setInitialStaff(LOCKER_INITIAL_STAFF);
            lockerRooms[i]
                    .setStudentCap(setRandom(LOCKER_STUDENT_CAPACITY_LOWER_LIMIT, LOCKER_STUDENT_CAPACITY_UPPER_LIMIT));
            lockerRooms[i].setSeatArrangement();
            lockerRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            lockerRooms[i].setRoomNumber("LK" + i + setRandom(LOCKER_NUMBER_LOWER_LIMIT, LOCKER_NUMBER_UPPER_LIMIT));
        }
    }

    public MusicRoom[] getMusicRooms() {
        return musicRooms;
    }

    @Override
    public void setMusicRooms(int number, GameView view) {
        musicRooms = new MusicRoom[number];
        GameLogger.logGeneration("   Generating " + number + " Music Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(MUSIC_CONNECTION_LOWER_LIMIT, MUSIC_CONNECTION_UPPER_LIMIT);
            musicRooms[i] = new MusicRoom();
            musicRooms[i].setRoomName("Music Room" + i);
            GameLogger.logGeneration("      Generating " + musicRooms[i].getRoomName());
            musicRooms[i].setWindowCount(setRandom(MUSIC_WINDOW_LOWER_LIMIT, MUSIC_WINDOW_UPPER_LIMIT));
            musicRooms[i].setConnections(connectN);
            musicRooms[i].setDoors(connectN);
            musicRooms[i].setInitialStaff(MUSIC_INITIAL_STAFF);
            musicRooms[i]
                    .setStudentCap(setRandom(MUSIC_STUDENT_CAPACITY_LOWER_LIMIT, MUSIC_STUDENT_CAPACITY_UPPER_LIMIT));
            musicRooms[i].setSeatArrangement();
            musicRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            musicRooms[i].setRoomNumber("MR" + i + setRandom(MUSIC_NUMBER_LOWER_LIMIT, MUSIC_NUMBER_UPPER_LIMIT));
        }
    }

    public int addMusicRooms(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = musicRooms != null ? musicRooms.length : 0;
        int newTotal = currentCount + additionalCount;
        MusicRoom[] newRooms = new MusicRoom[newTotal];
        if (musicRooms != null) {
            System.arraycopy(musicRooms, 0, newRooms, 0, currentCount);
        }

        for (int i = currentCount; i < newTotal; i++) {
            int connectN = setRandom(MUSIC_CONNECTION_LOWER_LIMIT, MUSIC_CONNECTION_UPPER_LIMIT);
            newRooms[i] = new MusicRoom();
            newRooms[i].setRoomName("Music Room" + i);
            GameLogger.logGeneration("      Generating " + newRooms[i].getRoomName());
            newRooms[i].setWindowCount(setRandom(MUSIC_WINDOW_LOWER_LIMIT, MUSIC_WINDOW_UPPER_LIMIT));
            newRooms[i].setConnections(connectN);
            newRooms[i].setDoors(connectN);
            newRooms[i].setInitialStaff(MUSIC_INITIAL_STAFF);
            newRooms[i].setStudentCap(setRandom(MUSIC_STUDENT_CAPACITY_LOWER_LIMIT, MUSIC_STUDENT_CAPACITY_UPPER_LIMIT));
            newRooms[i].setSeatArrangement();
            newRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newRooms[i].setRoomNumber("MR" + i + setRandom(MUSIC_NUMBER_LOWER_LIMIT, MUSIC_NUMBER_UPPER_LIMIT));
        }

        musicRooms = newRooms;
        if (view != null) {
            GameLogger.logGeneration("EXPANSION: Added " + additionalCount + " music room(s)");
        }
        return additionalCount;
    }

    public ScienceLab[] getScienceLabs() {
        return scienceLabs;
    }

    @Override
    public void setScienceLabs(int number, GameView view) {
        scienceLabs = new ScienceLab[number];
        GameLogger.logGeneration("   Generating " + number + " Science Lab(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(SCIENCE_LAB_CONNECTION_LOWER_LIMIT, SCIENCE_LAB_CONNECTION_UPPER_LIMIT);
            scienceLabs[i] = new ScienceLab();
            scienceLabs[i].setRoomName("Science Lab" + i);
            GameLogger.logGeneration("      Generating " + scienceLabs[i].getRoomName());
            scienceLabs[i].setWindowCount(SCIENCE_LAB_WINDOW_COUNT);
            scienceLabs[i].setConnections(connectN);
            scienceLabs[i].setDoors(connectN);
            scienceLabs[i].setInitialStaff(SCIENCE_LAB_INITIAL_STAFF);
            scienceLabs[i].setStudentCap(
                    setRandom(SCIENCE_LAB_STUDENT_CAPACITY_LOWER_LIMIT, SCIENCE_LAB_STUDENT_CAPACITY_UPPER_LIMIT));
            scienceLabs[i].setSeatArrangement();
            scienceLabs[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            scienceLabs[i].setRoomNumber(
                    "Lab" + i + setRandom(SCIENCE_LAB_NUMBER_LOWER_LIMIT, SCIENCE_LAB_NUMBER_UPPER_LIMIT));
        }
    }

    public int addScienceLabs(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = scienceLabs != null ? scienceLabs.length : 0;
        int newTotal = currentCount + additionalCount;
        ScienceLab[] newLabs = new ScienceLab[newTotal];
        if (scienceLabs != null) {
            System.arraycopy(scienceLabs, 0, newLabs, 0, currentCount);
        }

        for (int i = currentCount; i < newTotal; i++) {
            int connectN = setRandom(SCIENCE_LAB_CONNECTION_LOWER_LIMIT, SCIENCE_LAB_CONNECTION_UPPER_LIMIT);
            newLabs[i] = new ScienceLab();
            newLabs[i].setRoomName("Science Lab" + i);
            GameLogger.logGeneration("      Generating " + newLabs[i].getRoomName());
            newLabs[i].setWindowCount(SCIENCE_LAB_WINDOW_COUNT);
            newLabs[i].setConnections(connectN);
            newLabs[i].setDoors(connectN);
            newLabs[i].setInitialStaff(SCIENCE_LAB_INITIAL_STAFF);
            newLabs[i].setStudentCap(
                    setRandom(SCIENCE_LAB_STUDENT_CAPACITY_LOWER_LIMIT, SCIENCE_LAB_STUDENT_CAPACITY_UPPER_LIMIT));
            newLabs[i].setSeatArrangement();
            newLabs[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newLabs[i].setRoomNumber(
                    "Lab" + i + setRandom(SCIENCE_LAB_NUMBER_LOWER_LIMIT, SCIENCE_LAB_NUMBER_UPPER_LIMIT));
        }

        scienceLabs = newLabs;
        if (view != null) {
            GameLogger.logGeneration("EXPANSION: Added " + additionalCount + " science lab(s)");
        }
        return additionalCount;
    }

    public void setStudentGradeClass(HashMap<Integer, Student> studentHashMap, GameView view) {
        clearEnrolledStudents();
        Integer f_count = 0;
        Integer s_count = 0;
        Integer j_count = 0;
        Integer sr_count = 0;
        // TODO: investigate placing og key vs new counter for performance
        for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
            String grade = entry.getValue().studentStatistics.getGradeLevel();
            switch (grade) {
                case "Freshman" -> {
                    freshmanClass.put(f_count, entry.getValue());
                    f_count++;
                }
                case "Sophomore" -> {
                    sophomoreClass.put(s_count, entry.getValue());
                    s_count++;
                }
                case "Junior" -> {
                    juniorClass.put(j_count, entry.getValue());
                    j_count++;
                }
                case "Senior" -> {
                    seniorClass.put(sr_count, entry.getValue());
                    sr_count++;
                }
                default -> GameLogger.logGeneration("Can't find student class");
            }
        }
        updateEnrollmentFromGrades();
    }

    public HashMap<Integer, Student> getStudentGradeClass(String gradClass) {
        switch (gradClass) {
            case "Freshman":
                return freshmanClass;
            case "Sophomore":
                return sophomoreClass;
            case "Junior":
                return juniorClass;
            case "Senior":
                return seniorClass;
            default:
                break;
        }
        return null;
    }

    public ConferenceRoom[] getConferenceRooms() {
        return conferenceRooms;
    }

    public void setConferenceRooms(int number, GameView view) {
        conferenceRooms = new ConferenceRoom[number];
        GameLogger.logGeneration("   Generating " + number + " Conference Room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(CONFERENCE_CONNECTION_LOWER_LIMIT, CONFERENCE_CONNECTION_UPPER_LIMIT);
            conferenceRooms[i] = new ConferenceRoom();
            conferenceRooms[i].setRoomName("Conference Room" + i);
            GameLogger.logGeneration("      Generating " + conferenceRooms[i].getRoomName());
            conferenceRooms[i].setWindowCount(setRandom(CONFERENCE_WINDOW_LOWER_LIMIT, CONFERENCE_WINDOW_UPPER_LIMIT));
            conferenceRooms[i].setConnections(connectN);
            conferenceRooms[i].setDoors(connectN);
            conferenceRooms[i].setInitialStaff(CONFERENCE_INITIAL_STAFF);
            conferenceRooms[i].setStudentCap(
                    setRandom(CONFERENCE_STUDENT_CAPACITY_LOWER_LIMIT, CONFERENCE_STUDENT_CAPACITY_UPPER_LIMIT));
            conferenceRooms[i].setSeatArrangement();
            conferenceRooms[i].setRoomNumber(
                    "Conference" + i + setRandom(CONFERENCE_NUMBER_LOWER_LIMIT, CONFERENCE_NUMBER_UPPER_LIMIT));
        }
    }

    public VocationalRoom[] getVocationalRooms() {
        return vocationalRooms;
    }

    public void setVocationalRooms(int number, GameView view) {
        vocationalRooms = new VocationalRoom[number];
        GameLogger.logGeneration("   Generating " + number + " Vocational room(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = setRandom(VOCATIONAL_CONNECTION_LOWER_LIMIT, VOCATIONAL_CONNECTION_UPPER_LIMIT);
            vocationalRooms[i] = new VocationalRoom();
            vocationalRooms[i].setRoomName("Vocational Room" + i);
            GameLogger.logGeneration("      Generating " + vocationalRooms[i].getRoomName());
            vocationalRooms[i].setWindowCount(setRandom(VOCATIONAL_WINDOW_LOWER_LIMIT, VOCATIONAL_WINDOW_UPPER_LIMIT));
            vocationalRooms[i].setConnections(connectN);
            vocationalRooms[i].setDoors(connectN);
            vocationalRooms[i].setInitialStaff(VOCATIONAL_INITIAL_STAFF);
            vocationalRooms[i].setStudentCap(
                    setRandom(VOCATIONAL_STUDENT_CAPACITY_LOWER_LIMIT, VOCATIONAL_STUDENT_CAPACITY_UPPER_LIMIT));
            vocationalRooms[i].setSeatArrangement();
            vocationalRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            vocationalRooms[i].setRoomNumber(
                    "Vocational" + i + setRandom(VOCATIONAL_NUMBER_LOWER_LIMIT, VOCATIONAL_NUMBER_UPPER_LIMIT));
        }
    }

    public int addVocationalRooms(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = vocationalRooms != null ? vocationalRooms.length : 0;
        int newTotal = currentCount + additionalCount;
        VocationalRoom[] newRooms = new VocationalRoom[newTotal];
        if (vocationalRooms != null) {
            System.arraycopy(vocationalRooms, 0, newRooms, 0, currentCount);
        }

        for (int i = currentCount; i < newTotal; i++) {
            int connectN = setRandom(VOCATIONAL_CONNECTION_LOWER_LIMIT, VOCATIONAL_CONNECTION_UPPER_LIMIT);
            newRooms[i] = new VocationalRoom();
            newRooms[i].setRoomName("Vocational Room" + i);
            GameLogger.logGeneration("      Generating " + newRooms[i].getRoomName());
            newRooms[i].setWindowCount(setRandom(VOCATIONAL_WINDOW_LOWER_LIMIT, VOCATIONAL_WINDOW_UPPER_LIMIT));
            newRooms[i].setConnections(connectN);
            newRooms[i].setDoors(connectN);
            newRooms[i].setInitialStaff(VOCATIONAL_INITIAL_STAFF);
            newRooms[i].setStudentCap(
                    setRandom(VOCATIONAL_STUDENT_CAPACITY_LOWER_LIMIT, VOCATIONAL_STUDENT_CAPACITY_UPPER_LIMIT));
            newRooms[i].setSeatArrangement();
            newRooms[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newRooms[i].setRoomNumber(
                    "Vocational" + i + setRandom(VOCATIONAL_NUMBER_LOWER_LIMIT, VOCATIONAL_NUMBER_UPPER_LIMIT));
        }

        vocationalRooms = newRooms;
        if (view != null) {
            GameLogger.logGeneration("EXPANSION: Added " + additionalCount + " vocational room(s)");
        }
        return additionalCount;
    }

    public ParkingLot[] getParkingLots() {
        return parkingLots;
    }

    public void setParkingLots(int number, GameView view) {
        parkingLots = new ParkingLot[number];
        GameLogger.logGeneration("   Generating " + number + " Parking Lot(s)...");
        for (int i = 0; i < number; i++) {
            int connectN = PARKING_CONNECTION_COUNT;
            parkingLots[i] = new ParkingLot();
            parkingLots[i].setRoomName("Parking Lot" + i);
            GameLogger.logGeneration("      Generating " + parkingLots[i].getRoomName());
            parkingLots[i].setWindowCount(PARKING_WINDOW_COUNT);
            parkingLots[i].setConnections(connectN);
            parkingLots[i].setDoors(connectN);
            parkingLots[i].setInitialStaff(PARKING_INITIAL_STAFF);
            parkingLots[i].setStudentCap(
                    setRandom(PARKING_STUDENT_CAPACITY_LOWER_LIMIT, PARKING_STUDENT_CAPACITY_UPPER_LIMIT));
            parkingLots[i].setSeatArrangement();
            parkingLots[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            parkingLots[i].setRoomNumber(
                    "ParkingLot" + i + setRandom(PARKING_NUMBER_LOWER_LIMIT, PARKING_NUMBER_UPPER_LIMIT));
        }
    }

    // ==================== Portable Classrooms ====================

    /**
     * Gets the portable classrooms array.
     *
     * @return array of portable classrooms, or empty array if none
     */
    public Portable[] getPortables() {
        return portables != null ? portables : new Portable[0];
    }

    /**
     * Sets up portable classrooms for the school.
     * Portables are temporary modular buildings used as additional classroom space.
     * They are more common at underfunded schools and can only connect to outdoor
     * spaces.
     *
     * @param number the number of portables to create
     * @param view   the game view for output
     */
    public void setPortables(int number, GameView view) {
        if (number <= 0) {
            portables = new Portable[0];
            return;
        }

        portables = new Portable[number];
        GameLogger.logGeneration("   Generating " + number + " Portable Classroom(s)...");
        for (int i = 0; i < number; i++) {
            int decision = i % CLASSROOM_WEIGHT;
            portables[i] = new Portable();
            portables[i].setRoomName("Portable" + i);
            GameLogger.logGeneration("      Generating " + portables[i].getRoomName());
            portables[i].setWindowCount(setRandom(PORTABLE_WINDOW_LOWER_LIMIT, PORTABLE_WINDOW_UPPER_LIMIT));
            portables[i].setConnections(PORTABLE_CONNECTION_COUNT);
            portables[i].setDoors(PORTABLE_CONNECTION_COUNT);
            portables[i].setClassroomType(decision);
            portables[i].setInitialStaff(PORTABLE_INITIAL_STAFF);
            portables[i].setStudentCap(
                    setRandom(PORTABLE_STUDENT_CAPACITY_LOWER_LIMIT, PORTABLE_STUDENT_CAPACITY_UPPER_LIMIT));
            portables[i].setSeatArrangement();
            portables[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            portables[i].setRoomNumber("P" + i + setRandom(PORTABLE_NUMBER_LOWER_LIMIT, PORTABLE_NUMBER_UPPER_LIMIT));
        }
    }

    /**
     * Adds additional portable classrooms to the school dynamically.
     * Used for expansion when scheduling fails due to insufficient capacity.
     *
     * @param additionalCount the number of portables to add
     * @param view            the game view for output (can be null for silent
     *                        operation)
     * @return the number of portables added
     */
    public int addPortables(int additionalCount, GameView view) {
        if (additionalCount <= 0) {
            return 0;
        }

        int currentCount = portables != null ? portables.length : 0;
        int newTotal = currentCount + additionalCount;

        // Create new array and copy existing portables
        Portable[] newPortables = new Portable[newTotal];
        if (portables != null) {
            System.arraycopy(portables, 0, newPortables, 0, currentCount);
        }

        if (view != null) {
            GameLogger.logGeneration("   Expanding school: Adding " + additionalCount + " portable(s)...");
        }
        GameLogger.logGeneration("EXPANSION: Adding " + additionalCount + " portables (total: " + newTotal + ")");

        // Create new portables
        for (int i = currentCount; i < newTotal; i++) {
            int decision = i % CLASSROOM_WEIGHT;
            newPortables[i] = new Portable();
            newPortables[i].setRoomName("Portable" + i);
            if (view != null) {
                GameLogger.logGeneration("      Generating " + newPortables[i].getRoomName());
            }
            newPortables[i].setWindowCount(setRandom(PORTABLE_WINDOW_LOWER_LIMIT, PORTABLE_WINDOW_UPPER_LIMIT));
            newPortables[i].setConnections(PORTABLE_CONNECTION_COUNT);
            newPortables[i].setDoors(PORTABLE_CONNECTION_COUNT);
            newPortables[i].setClassroomType(decision);
            newPortables[i].setInitialStaff(PORTABLE_INITIAL_STAFF);
            newPortables[i].setStudentCap(
                    setRandom(PORTABLE_STUDENT_CAPACITY_LOWER_LIMIT, PORTABLE_STUDENT_CAPACITY_UPPER_LIMIT));
            newPortables[i].setSeatArrangement();
            newPortables[i].initializeSeatingArrangements(TOTAL_SCHOOL_PERIODS);
            newPortables[i]
                    .setRoomNumber("P" + i + setRandom(PORTABLE_NUMBER_LOWER_LIMIT, PORTABLE_NUMBER_UPPER_LIMIT));
        }

        portables = newPortables;
        return additionalCount;
    }

    /**
     * Checks if the school has any portable classrooms.
     *
     * @return true if the school has portables
     */
    public boolean hasPortables() {
        return portables != null && portables.length > 0;
    }

    /**
     * Gets the total capacity of all portable classrooms.
     *
     * @return total student capacity of portables
     */
    public int getPortableCapacity() {
        if (portables == null) {
            return 0;
        }
        int total = 0;
        for (Portable portable : portables) {
            total += portable.getStudentCapacity();
        }
        return total;
    }

    /**
     * Gets the stairwells array.
     *
     * @return array of stairwells, or empty array if none (single-story school)
     */
    public Stairwell[] getStairwells() {
        return stairwells != null ? stairwells : new Stairwell[0];
    }

    /**
     * Sets up stairwells for the school. Stairwells are part of the backbone
     * and connect hallways across different floors. They are limited to exactly
     * 2 connections. Single-story schools will have 0 stairwells.
     *
     * @param number the number of stairwells to create
     * @param view   the game view for output
     */
    public void setStairwells(int number, GameView view) {
        if (number <= 0) {
            stairwells = new Stairwell[0];
            return;
        }

        stairwells = new Stairwell[number];
        GameLogger.logGeneration("   Generating " + number + " stairwell(s)...");
        for (int i = 0; i < number; i++) {
            stairwells[i] = new Stairwell();
            stairwells[i].setRoomName("Stairwell" + i);
            GameLogger.logGeneration("      Generating " + stairwells[i].getRoomName());
            stairwells[i].setConnections(STAIRWELL_CONNECTION_COUNT);
            stairwells[i].setDoors(STAIRWELL_CONNECTION_COUNT);
            stairwells[i].setWindowCount(STAIRWELL_WINDOW_COUNT);
            stairwells[i].setInitialStaff(STAIRWELL_INITIAL_STAFF);
            stairwells[i].setStudentCap(
                    setRandom(STAIRWELL_STUDENT_CAPACITY_LOWER_LIMIT, STAIRWELL_STUDENT_CAPACITY_UPPER_LIMIT));
            stairwells[i].setSeatArrangement();
            stairwells[i].setStudentRestriction(false);
            stairwells[i].setRoomNumber("SW" + i);
        }
    }

    /**
     * Checks if the school is multi-story (has stairwells).
     *
     * @return true if the school has stairwells
     */
    public boolean isMultiStory() {
        return stairwells != null && stairwells.length > 0;
    }

    public int getNumberOfFloors() {
        return numberOfFloors;
    }

    public void setNumberOfFloors(int numberOfFloors) {
        this.numberOfFloors = numberOfFloors;
    }

    public void schoolColorsLoader() {
        String pathColors = "/Resources/Town/colors.txt";
        Map<String, String> colorMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(ResourceAccess.reader(pathColors))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    colorMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        List<String> colorNames = new ArrayList<>(colorMap.keySet());

        int firstColorIndex = setRandom(0, colorNames.size() - 1);
        int secondColorIndex;

        do {
            secondColorIndex = setRandom(0, colorNames.size() - 1);
        } while (firstColorIndex == secondColorIndex);

        String firstColorName = colorNames.get(firstColorIndex);
        String secondColorName = colorNames.get(secondColorIndex);

        this.schoolColors = new String[] { firstColorName, secondColorName };
        this.schoolColorsHex = new String[] { colorMap.get(firstColorName), colorMap.get(secondColorName) };
    }

    public String[] getSchoolColors() {
        return Arrays.copyOf(schoolColors, schoolColors.length);
    }

    public void setSchoolColors(String[] colors) {
        this.schoolColors = colors;
    }

    public String[] getSchoolColorsHex() {
        return Arrays.copyOf(schoolColorsHex, schoolColorsHex.length);
    }

    public Room getClassroomByStaff(Staff staff) {
        Room assignedRoom = findAssignedRoom(staff, classrooms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, scienceLabs);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, gyms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, athleticFields);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, artStudios);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, musicRooms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, dramaRooms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, auditoriums);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, vocationalRooms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, computerLabs);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, libraries);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, lunchrooms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, offices);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, utilityrooms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, breakrooms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, conferenceRooms);
        if (assignedRoom == null)
            assignedRoom = findAssignedRoom(staff, portables);

        if (assignedRoom != null) {
            return assignedRoom;
        }

        GameLogger.logScheduling(
                "Cant find room of " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName());
        return null;
    }

    private Room findAssignedRoom(Staff staff, Room[] rooms) {
        if (staff == null || rooms == null) {
            return null;
        }

        for (Room room : rooms) {
            if (room == null) {
                continue;
            }

            List<Staff> staffList = room.getAssignedStaff();
            for (Staff assignedStaff : staffList) {
                if (assignedStaff.equals(staff)) {
                    return room;
                }
            }

            if (room.getSecondTeacher() != null && room.getSecondTeacher().equals(staff)) {
                return room;
            }
        }

        return null;
    }

    public Room getRoomByStaff(Staff staff, String roomType) {
        switch (roomType) {
            case "Physical Education" -> {
                for (Gym gym : gyms) {
                    List<Staff> staffList = gym.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return gym;
                        }
                    }
                }
                for (AthleticField field : athleticFields) {
                    List<Staff> staffList = field.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return field;
                        }
                    }
                }
            }
            case "Office" -> {
                for (Office office : offices) {
                    List<Staff> staffList = office.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return office;
                        }
                    }
                }
            }
            case "Visual Arts" -> {
                for (ArtStudio artStudio : artStudios) {
                    List<Staff> staffList = artStudio.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return artStudio;
                        }
                    }
                }
            }
            case "Performing Arts" -> {
                for (DramaRoom dramaRoom : dramaRooms) {
                    List<Staff> staffList = dramaRoom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return dramaRoom;
                        }
                    }
                }
                for (MusicRoom musicRoom : musicRooms) {
                    List<Staff> staffList = musicRoom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return musicRoom;
                        }
                    }
                }
                for (Auditorium auditorium : auditoriums) {
                    List<Staff> staffList = auditorium.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return auditorium;
                        }
                    }
                }
            }
            case "Vocational" -> {
                for (VocationalRoom vocationalRoom : vocationalRooms) {
                    List<Staff> staffList = vocationalRoom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return vocationalRoom;
                        }
                    }
                }
                for (Classroom classroom : classrooms) {
                    List<Staff> staffList = classroom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return classroom;
                        }
                    }
                }
            }
            case "Business" -> {
                for (Classroom classroom : classrooms) {
                    List<Staff> staffList = classroom.getAssignedStaff();
                    for (Staff staff1 : staffList) {
                        if (staff1.equals(staff)) {
                            return classroom;
                        }
                    }
                }
            }
        }
        return null;
    }

    // ==================== Town-based Architecture Support ====================

    /**
     * Gets the total number of enrolled students across all grade levels.
     *
     * @return the total enrolled student count
     */
    public int getTotalEnrolledStudents() {
        return freshmanClass.size() + sophomoreClass.size() +
                juniorClass.size() + seniorClass.size();
    }

    /**
     * Gets a HashMap of all enrolled students (for compatibility).
     *
     * @return HashMap of all students keyed by sequential integers
     */
    public HashMap<Integer, Student> getAllEnrolledStudents() {
        HashMap<Integer, Student> allStudents = new HashMap<>();
        int index = 0;
        for (Student student : freshmanClass.values()) {
            allStudents.put(index++, student);
        }
        for (Student student : sophomoreClass.values()) {
            allStudents.put(index++, student);
        }
        for (Student student : juniorClass.values()) {
            allStudents.put(index++, student);
        }
        for (Student student : seniorClass.values()) {
            allStudents.put(index++, student);
        }
        return allStudents;
    }

    /**
     * Clears all enrolled students from grade-level classes.
     * Useful for resetting school enrollment.
     */
    public void clearEnrolledStudents() {
        freshmanClass.clear();
        sophomoreClass.clear();
        juniorClass.clear();
        seniorClass.clear();
    }

    /**
     * Gets enrollment counts by grade level.
     *
     * @return map of grade level to enrollment count
     */
    public Map<String, Integer> getEnrollmentByGrade() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Freshman", freshmanClass.size());
        counts.put("Sophomore", sophomoreClass.size());
        counts.put("Junior", juniorClass.size());
        counts.put("Senior", seniorClass.size());
        return counts;
    }

    /**
     * Checks if the school has capacity for more students.
     *
     * @return true if the school can accept more students
     */
    public boolean hasCapacity() {
        return getTotalEnrolledStudents() < getTotalStudentCapacity();
    }

    /**
     * Gets the number of available spots for new students.
     *
     * @return the number of available enrollment spots
     */
    public int getAvailableCapacity() {
        return Math.max(0, getTotalStudentCapacity() - getTotalEnrolledStudents());
    }

}
