package utility;

//*******************************************************************
//  utility.Director.java
//  Description: This directs the construction of a school from rooms.
//  Now supports funding-based room generation for realistic school sizes.
//  Bugs:
//
//  @author     Alex Klein
//  @version    04242022
//*******************************************************************

import config.SchoolFundingModel;
import entity.StandardSchool;
import view.GameView;

import java.util.Map;

import static constants.SchoolConstants.*;
import static utility.Randomizer.setRandom;

/**
 * Directs the construction of a school by creating rooms based on
 * either random ranges (legacy) or funding-based calculations (new).
 */
public class Director {

    private SchoolFundingModel fundingModel;

    /**
     * Creates a Director with default funding and builds the school.
     * This constructor maintains backward compatibility.
     *
     * @param standardSchool the school to build
     * @param view           the game view for output
     */
    public Director(StandardSchool standardSchool, GameView view) {
        this(standardSchool, new SchoolFundingModel(), view);
    }

    /**
     * Creates a Director with specified funding level and builds the school.
     *
     * @param standardSchool the school to build
     * @param fundingModel   the funding model to use for room calculations
     * @param view           the game view for output
     */
    public Director(StandardSchool standardSchool, SchoolFundingModel fundingModel, GameView view) {
        this.fundingModel = fundingModel != null ? fundingModel : new SchoolFundingModel();
        standardSchool.setFundingModel(this.fundingModel);
        setStandardSchool(standardSchool, view);
    }

    /**
     * Creates a Director with specified funding level for a target population.
     *
     * @param standardSchool   the school to build
     * @param fundingModel     the funding model
     * @param targetPopulation the expected student population
     * @param view             the game view for output
     */
    public Director(StandardSchool standardSchool, SchoolFundingModel fundingModel,
            int targetPopulation, GameView view) {
        this.fundingModel = fundingModel != null ? fundingModel : new SchoolFundingModel();
        standardSchool.setFundingModel(this.fundingModel);
        standardSchool.setTargetEnrollment(targetPopulation);
        setStandardSchoolForPopulation(standardSchool, targetPopulation, view);
    }

    /**
     * Builds a school using random room counts within the standard ranges.
     * This is the legacy behavior.
     */
    public void setStandardSchool(StandardSchool standardSchool, GameView view) {
        double roomModifier = fundingModel.getRoomCountModifier();
        double specializedModifier = fundingModel.getSpecializedRoomModifier();

        view.appendOutput("Building school with " + fundingModel.getFundingLevel().getDisplayName() + " funding...");

        // Core classrooms - scaled by room modifier
        int classroomCount = applyModifier(
                setRandom(CLASSROOM_AMOUNT_LOWER_LIMIT, CLASSROOM_AMOUNT_UPPER_LIMIT),
                roomModifier);
        view.appendOutput("Building classrooms...");
        standardSchool.setClassrooms(classroomCount, view);

        // Specialized rooms - scaled by specialized room modifier
        view.appendOutput("Building art studios...");
        standardSchool.setArtStudios(
                applyModifier(setRandom(ART_AMOUNT_LOWER_LIMIT, ART_AMOUNT_UPPER_LIMIT), specializedModifier),
                view);

        view.appendOutput("Building athletic fields...");
        standardSchool.setAthleticFields(
                applyModifier(setRandom(ATHLETIC_AMOUNT_LOWER_LIMIT, ATHLETIC_AMOUNT_UPPER_LIMIT), specializedModifier),
                view);

        view.appendOutput("Building auditoriums...");
        standardSchool.setAuditoriums(
                applyModifier(setRandom(AUDITORIUM_AMOUNT_LOWER_LIMIT, AUDITORIUM_AMOUNT_UPPER_LIMIT),
                        specializedModifier),
                view);

        view.appendOutput("Building breakrooms...");
        standardSchool.setBreakrooms(setRandom(BREAKROOM_AMOUNT_LOWER_LIMIT, BREAKROOM_AMOUNT_UPPER_LIMIT), view);

        view.appendOutput("Building vocational rooms...");
        standardSchool.setVocationalRooms(
                applyModifier(setRandom(VOCATIONAL_AMOUNT_LOWER_LIMIT, VOCATIONAL_AMOUNT_UPPER_LIMIT),
                        specializedModifier),
                view);

        view.appendOutput("Building computer labs...");
        standardSchool.setComputerLabs(
                applyModifier(setRandom(COMPUTER_LAB_AMOUNT_LOWER_LIMIT, COMPUTER_LAB_AMOUNT_UPPER_LIMIT),
                        specializedModifier),
                view);

        view.appendOutput("Building courtyards...");
        standardSchool.setCourtyards(setRandom(COURTYARD_AMOUNT_LOWER_LIMIT, COURTYARD_AMOUNT_UPPER_LIMIT), view);

        view.appendOutput("Building drama rooms...");
        standardSchool.setDramaRooms(
                applyModifier(setRandom(DRAMA_AMOUNT_LOWER_LIMIT, DRAMA_AMOUNT_UPPER_LIMIT), specializedModifier),
                view);

        view.appendOutput("Building gyms...");
        standardSchool.setGyms(setRandom(GYM_AMOUNT_LOWER_LIMIT, GYM_AMOUNT_UPPER_LIMIT), view);

        view.appendOutput("Building hallways...");
        standardSchool.setHallways(
                applyModifier(setRandom(HALLWAY_AMOUNT_LOWER_LIMIT, HALLWAY_AMOUNT_UPPER_LIMIT), roomModifier),
                view);

        view.appendOutput("Building libraries...");
        standardSchool.setLibraries(setRandom(LIBRARY_AMOUNT_LOWER_LIMIT, LIBRARY_AMOUNT_UPPER_LIMIT), view);

        view.appendOutput("Building locker rooms...");
        standardSchool.setLockerRooms(
                (standardSchool.getGyms().length + standardSchool.getAthleticFields().length) * LOCKER_ROOM_MODIFIER,
                view);

        view.appendOutput("Building lunchrooms...");
        standardSchool.setLunchrooms(setRandom(LUNCHROOM_AMOUNT_LOWER_LIMIT, LUNCHROOM_AMOUNT_UPPER_LIMIT), view);

        view.appendOutput("Building music rooms...");
        standardSchool.setMusicRooms(
                applyModifier(setRandom(MUSIC_AMOUNT_LOWER_LIMIT, MUSIC_AMOUNT_UPPER_LIMIT), specializedModifier),
                view);

        view.appendOutput("Building offices...");
        standardSchool.setOffices(
                applyModifier(setRandom(OFFICE_AMOUNT_LOWER_LIMIT, standardSchool.getClassrooms().length),
                        roomModifier),
                view);

        view.appendOutput("Building science labs...");
        standardSchool.setScienceLabs(
                applyModifier(setRandom(SCIENCE_LAB_AMOUNT_LOWER_LIMIT, SCIENCE_LAB_AMOUNT_UPPER_LIMIT),
                        specializedModifier),
                view);

        view.appendOutput("Building utility rooms...");
        standardSchool.setUtilityRooms(setRandom(UTILITY_AMOUNT_LOWER_LIMIT, UTILITY_AMOUNT_UPPER_LIMIT), view);

        view.appendOutput("Building conference rooms...");
        standardSchool.setConferenceRooms(setRandom(CONFERENCE_AMOUNT_LOWER_LIMIT, CONFERENCE_AMOUNT_UPPER_LIMIT),
                view);

        view.appendOutput("Building parking lots...");
        standardSchool.setParkingLots(setRandom(PARKING_AMOUNT_LOWER_LIMIT, PARKING_AMOUNT_UPPER_LIMIT), view);

        // Portable classrooms - more common at underfunded schools
        int portableCount = calculatePortableCount();
        if (portableCount > 0) {
            view.appendOutput("Building portable classrooms...");
            standardSchool.setPortables(portableCount, view);
        } else {
            standardSchool.setPortables(0, view); // Initialize empty array
        }

        view.appendOutput("Building bathrooms...");
        standardSchool.setBathrooms(BATHROOM_AMOUNT, view);

        // School identity
        view.appendOutput("Setting school name...");
        standardSchool.setSchoolName();
        view.appendOutput("Setting school mascot...");
        standardSchool.setSchoolMascot();
        view.appendOutput("Setting school colors...");
        standardSchool.schoolColorsLoader();
        view.appendOutput("Setting school founded year...");
        standardSchool.setSchoolFoundedYear();

        view.appendOutput("School built: " + standardSchool.getClassrooms().length + " classrooms, " +
                "optimal capacity: " + standardSchool.getOptimalCapacity() + " students");
    }

    /**
     * Builds a school sized for a specific target population.
     * Room counts are calculated based on the expected student population
     * and funding level.
     *
     * @param standardSchool   the school to build
     * @param targetPopulation the expected number of students
     * @param view             the game view for output
     */
    public void setStandardSchoolForPopulation(StandardSchool standardSchool, int targetPopulation, GameView view) {
        double specializedModifier = fundingModel.getSpecializedRoomModifier();

        view.appendOutput("Building school for " + targetPopulation + " students with " +
                fundingModel.getFundingLevel().getDisplayName() + " funding...");

        // Calculate classrooms needed for the target population
        int classroomsNeeded = fundingModel.calculateClassroomsNeeded(targetPopulation, TOTAL_SCHOOL_PERIODS);
        // Ensure we're within reasonable bounds
        classroomsNeeded = Math.max(CLASSROOM_AMOUNT_LOWER_LIMIT,
                Math.min(classroomsNeeded, CLASSROOM_AMOUNT_UPPER_LIMIT * 2));

        view.appendOutput("Building classrooms...");
        standardSchool.setClassrooms(classroomsNeeded, view);

        // Scale other rooms based on classroom count and specialization modifier
        int baseSpecialized = Math.max(1, classroomsNeeded / 10);

        view.appendOutput("Building art studios...");
        standardSchool.setArtStudios(
                Math.max(1, (int) (baseSpecialized * specializedModifier)),
                view);

        view.appendOutput("Building athletic fields...");
        standardSchool.setAthleticFields(
                Math.max(1, (int) (Math.ceil(classroomsNeeded / 20.0) * specializedModifier)),
                view);

        view.appendOutput("Building auditoriums...");
        standardSchool.setAuditoriums(
                Math.max(1, (int) (Math.ceil(classroomsNeeded / 30.0) * specializedModifier)),
                view);

        view.appendOutput("Building breakrooms...");
        standardSchool.setBreakrooms(Math.max(1, classroomsNeeded / 20), view);

        view.appendOutput("Building vocational rooms...");
        standardSchool.setVocationalRooms(
                Math.max(4, (int) (baseSpecialized * 1.5 * specializedModifier)),
                view);

        view.appendOutput("Building computer labs...");
        standardSchool.setComputerLabs(
                Math.max(1, (int) (baseSpecialized * specializedModifier)),
                view);

        view.appendOutput("Building courtyards...");
        standardSchool.setCourtyards(Math.max(1, classroomsNeeded / 25), view);

        view.appendOutput("Building drama rooms...");
        standardSchool.setDramaRooms(
                Math.max(1, (int) (Math.ceil(baseSpecialized / 2.0) * specializedModifier)),
                view);

        view.appendOutput("Building gyms...");
        standardSchool.setGyms(Math.max(1, classroomsNeeded / 25), view);

        view.appendOutput("Building hallways...");
        standardSchool.setHallways(Math.max(9, classroomsNeeded / 5), view);

        view.appendOutput("Building libraries...");
        standardSchool.setLibraries(Math.max(1, classroomsNeeded / 40), view);

        view.appendOutput("Building locker rooms...");
        standardSchool.setLockerRooms(
                (standardSchool.getGyms().length + standardSchool.getAthleticFields().length) * LOCKER_ROOM_MODIFIER,
                view);

        view.appendOutput("Building lunchrooms...");
        standardSchool.setLunchrooms(Math.max(1, classroomsNeeded / 30), view);

        view.appendOutput("Building music rooms...");
        standardSchool.setMusicRooms(
                Math.max(1, (int) (Math.ceil(baseSpecialized / 2.0) * specializedModifier)),
                view);

        view.appendOutput("Building offices...");
        standardSchool.setOffices(Math.max(OFFICE_AMOUNT_LOWER_LIMIT, classroomsNeeded / 3), view);

        view.appendOutput("Building science labs...");
        standardSchool.setScienceLabs(
                Math.max(2, (int) (baseSpecialized * specializedModifier)),
                view);

        view.appendOutput("Building utility rooms...");
        standardSchool.setUtilityRooms(Math.max(UTILITY_AMOUNT_LOWER_LIMIT, classroomsNeeded / 15), view);

        view.appendOutput("Building conference rooms...");
        standardSchool.setConferenceRooms(Math.max(1, classroomsNeeded / 20), view);

        view.appendOutput("Building parking lots...");
        standardSchool.setParkingLots(Math.max(1, classroomsNeeded / 15), view);

        // Portable classrooms - more common at underfunded schools
        int portableCount = calculatePortableCount();
        if (portableCount > 0) {
            view.appendOutput("Building portable classrooms...");
            standardSchool.setPortables(portableCount, view);
        } else {
            standardSchool.setPortables(0, view); // Initialize empty array
        }

        view.appendOutput("Building bathrooms...");
        // Scale bathrooms to population
        int bathroomCount = Math.max(BATHROOM_AMOUNT, targetPopulation / 100);
        standardSchool.setBathrooms(bathroomCount, view);

        // School identity
        view.appendOutput("Setting school name...");
        standardSchool.setSchoolName();
        view.appendOutput("Setting school mascot...");
        standardSchool.setSchoolMascot();
        view.appendOutput("Setting school colors...");
        standardSchool.schoolColorsLoader();
        view.appendOutput("Setting school founded year...");
        standardSchool.setSchoolFoundedYear();

        view.appendOutput("School built: " + standardSchool.getClassrooms().length + " classrooms, " +
                "optimal capacity: " + standardSchool.getOptimalCapacity() + " students, " +
                "physical capacity: " + standardSchool.getPhysicalCapacity() + " students");
    }

    /**
     * Applies a modifier to a room count, ensuring minimum of 1.
     */
    private int applyModifier(int base, double modifier) {
        return Math.max(1, (int) Math.round(base * modifier));
    }

    /**
     * Calculates the number of portable classrooms based on funding level.
     * Portables are more common at underfunded schools (present in ~1/3 of American
     * schools).
     * The chance of having portables and the count are inversely related to
     * funding.
     *
     * @return the number of portables to create (0 if none)
     */
    private int calculatePortableCount() {
        int chanceOfPortables;
        int lowerLimit;
        int upperLimit;

        switch (fundingModel.getFundingLevel()) {
            case SEVERELY_UNDERFUNDED -> {
                chanceOfPortables = PORTABLE_CHANCE_SEVERELY_UNDERFUNDED;
                lowerLimit = PORTABLE_AMOUNT_SEVERELY_UNDERFUNDED_LOWER;
                upperLimit = PORTABLE_AMOUNT_SEVERELY_UNDERFUNDED_UPPER;
            }
            case UNDERFUNDED -> {
                chanceOfPortables = PORTABLE_CHANCE_UNDERFUNDED;
                lowerLimit = PORTABLE_AMOUNT_UNDERFUNDED_LOWER;
                upperLimit = PORTABLE_AMOUNT_UNDERFUNDED_UPPER;
            }
            case ADEQUATE -> {
                chanceOfPortables = PORTABLE_CHANCE_ADEQUATE;
                lowerLimit = PORTABLE_AMOUNT_ADEQUATE_LOWER;
                upperLimit = PORTABLE_AMOUNT_ADEQUATE_UPPER;
            }
            case WELL_FUNDED -> {
                chanceOfPortables = PORTABLE_CHANCE_WELL_FUNDED;
                lowerLimit = PORTABLE_AMOUNT_WELL_FUNDED_LOWER;
                upperLimit = PORTABLE_AMOUNT_WELL_FUNDED_UPPER;
            }
            case EXCELLENTLY_FUNDED -> {
                chanceOfPortables = PORTABLE_CHANCE_EXCELLENTLY_FUNDED;
                lowerLimit = PORTABLE_AMOUNT_EXCELLENTLY_FUNDED_LOWER;
                upperLimit = PORTABLE_AMOUNT_EXCELLENTLY_FUNDED_UPPER;
            }
            default -> {
                chanceOfPortables = PORTABLE_CHANCE_ADEQUATE;
                lowerLimit = PORTABLE_AMOUNT_ADEQUATE_LOWER;
                upperLimit = PORTABLE_AMOUNT_ADEQUATE_UPPER;
            }
        }

        // Roll to see if this school has portables
        int roll = setRandom(0, 100);
        if (roll < chanceOfPortables) {
            return setRandom(lowerLimit, upperLimit);
        }

        return 0;
    }

    // ==================== Demand-Based Construction (Phase 2b)
    // ====================

    /**
     * Builds (or adapts) a school based on pre-computed room requirements from
     * {@link DemandAnalyzer}. Instead of guessing from funding model + target
     * population, this method uses actual student demand to size the school.
     * <p>
     * Room types in the demand map are strings matching the names used elsewhere
     * (e.g. "Classroom", "ScienceLab", "Gym", "ArtStudio", etc.).
     *
     * @param standardSchool the school to build / adapt
     * @param roomNeeds      room type -&gt; count needed (from
     *                       {@link DemandAnalyzer.DemandResult#roomNeeds()})
     * @param view           the game view for output
     */
    public void buildSchoolFromDemand(StandardSchool standardSchool, Map<String, Integer> roomNeeds, GameView view) {
        double specializedModifier = fundingModel.getSpecializedRoomModifier();

        view.appendOutput("Building school from demand analysis with " +
                fundingModel.getFundingLevel().getDisplayName() + " funding...");

        // Core classrooms
        int classrooms = roomNeeds.getOrDefault("Classroom", 10);
        classrooms = Math.max(CLASSROOM_AMOUNT_LOWER_LIMIT, classrooms);
        view.appendOutput("Building classrooms...");
        standardSchool.setClassrooms(classrooms, view);

        // Specialized teaching rooms
        view.appendOutput("Building science labs...");
        standardSchool.setScienceLabs(
                Math.max(2, roomNeeds.getOrDefault("ScienceLab", 2)), view);

        view.appendOutput("Building art studios...");
        standardSchool.setArtStudios(
                Math.max(1, roomNeeds.getOrDefault("ArtStudio", 1)), view);

        view.appendOutput("Building gyms...");
        standardSchool.setGyms(
                Math.max(1, roomNeeds.getOrDefault("Gym", 1)), view);

        view.appendOutput("Building vocational rooms...");
        standardSchool.setVocationalRooms(
                Math.max(1, roomNeeds.getOrDefault("VocationalRoom", 1)), view);

        view.appendOutput("Building computer labs...");
        standardSchool.setComputerLabs(
                Math.max(1, roomNeeds.getOrDefault("ComputerLab", 1)), view);

        view.appendOutput("Building music rooms...");
        standardSchool.setMusicRooms(
                Math.max(1, roomNeeds.getOrDefault("MusicRoom", 1)), view);

        view.appendOutput("Building drama rooms...");
        standardSchool.setDramaRooms(
                Math.max(1, roomNeeds.getOrDefault("DramaRoom", 1)), view);

        // Non-teaching rooms - from demand or scaled from classrooms
        int baseFromClassrooms = Math.max(1, classrooms / 10);

        view.appendOutput("Building athletic fields...");
        standardSchool.setAthleticFields(
                Math.max(1, (int) (Math.ceil(classrooms / 20.0) * specializedModifier)), view);

        view.appendOutput("Building auditoriums...");
        standardSchool.setAuditoriums(
                Math.max(1, (int) (Math.ceil(classrooms / 30.0) * specializedModifier)), view);

        view.appendOutput("Building breakrooms...");
        standardSchool.setBreakrooms(Math.max(1, classrooms / 20), view);

        view.appendOutput("Building courtyards...");
        standardSchool.setCourtyards(Math.max(1, classrooms / 25), view);

        view.appendOutput("Building hallways...");
        standardSchool.setHallways(
                roomNeeds.getOrDefault("Hallway", Math.max(9, classrooms / 5)), view);

        view.appendOutput("Building libraries...");
        standardSchool.setLibraries(
                roomNeeds.getOrDefault("Library", Math.max(1, classrooms / 40)), view);

        view.appendOutput("Building locker rooms...");
        standardSchool.setLockerRooms(
                (standardSchool.getGyms().length + standardSchool.getAthleticFields().length)
                        * LOCKER_ROOM_MODIFIER,
                view);

        view.appendOutput("Building lunchrooms...");
        standardSchool.setLunchrooms(
                roomNeeds.getOrDefault("Lunchroom", Math.max(1, classrooms / 30)), view);

        view.appendOutput("Building offices...");
        standardSchool.setOffices(
                roomNeeds.getOrDefault("Office", Math.max(OFFICE_AMOUNT_LOWER_LIMIT, classrooms / 3)), view);

        view.appendOutput("Building utility rooms...");
        standardSchool.setUtilityRooms(
                roomNeeds.getOrDefault("UtilityRoom", Math.max(UTILITY_AMOUNT_LOWER_LIMIT, classrooms / 15)), view);

        view.appendOutput("Building conference rooms...");
        standardSchool.setConferenceRooms(Math.max(1, classrooms / 20), view);

        view.appendOutput("Building parking lots...");
        standardSchool.setParkingLots(Math.max(1, classrooms / 15), view);

        // Portables
        int portableCount = calculatePortableCount();
        if (portableCount > 0) {
            view.appendOutput("Building portable classrooms...");
            standardSchool.setPortables(portableCount, view);
        } else {
            standardSchool.setPortables(0, view);
        }

        view.appendOutput("Building bathrooms...");
        standardSchool.setBathrooms(
                roomNeeds.getOrDefault("Bathroom", BATHROOM_AMOUNT), view);

        // School identity
        view.appendOutput("Setting school name...");
        standardSchool.setSchoolName();
        view.appendOutput("Setting school mascot...");
        standardSchool.setSchoolMascot();
        view.appendOutput("Setting school colors...");
        standardSchool.schoolColorsLoader();
        view.appendOutput("Setting school founded year...");
        standardSchool.setSchoolFoundedYear();

        view.appendOutput("School built from demand: " + standardSchool.getClassrooms().length +
                " classrooms, optimal capacity: " + standardSchool.getOptimalCapacity() +
                " students, physical capacity: " + standardSchool.getPhysicalCapacity() + " students");
    }

    /**
     * Static convenience method that adapts an already-built school to additional
     * demand-based room requirements without recreating identity/non-teaching
     * rooms.
     * Primarily adds or expands teaching rooms.
     *
     * @param school    the school to adapt
     * @param roomNeeds room type -&gt; count needed
     * @param view      the game view for output
     */
    public static void adaptSchoolToDemand(StandardSchool school, Map<String, Integer> roomNeeds, GameView view) {
        view.appendOutput("Adapting school rooms to meet demand...");

        adaptIfNeeded("classrooms", roomNeeds.getOrDefault("Classroom", 0), school.getClassrooms().length,
                count -> school.addClassrooms(count, view), view);
        adaptIfNeeded("science labs", roomNeeds.getOrDefault("ScienceLab", 0), school.getScienceLabs().length,
                count -> school.addScienceLabs(count, view), view);
        adaptIfNeeded("art studios", roomNeeds.getOrDefault("ArtStudio", 0), school.getArtStudios().length,
                count -> school.addArtStudios(count, view), view);
        adaptIfNeeded("drama rooms", roomNeeds.getOrDefault("DramaRoom", 0), school.getDramaRooms().length,
                count -> school.addDramaRooms(count, view), view);
        adaptIfNeeded("music rooms", roomNeeds.getOrDefault("MusicRoom", 0), school.getMusicRooms().length,
                count -> school.addMusicRooms(count, view), view);
        adaptIfNeeded("gyms", roomNeeds.getOrDefault("Gym", 0), school.getGyms().length,
                count -> school.addGyms(count, view), view);
        adaptIfNeeded("vocational rooms", roomNeeds.getOrDefault("VocationalRoom", 0),
                school.getVocationalRooms().length, count -> school.addVocationalRooms(count, view), view);
        adaptIfNeeded("computer labs", roomNeeds.getOrDefault("ComputerLab", 0), school.getComputerLabs().length,
                count -> school.addComputerLabs(count, view), view);

        view.appendOutput("Adaptation complete. Classrooms: " + school.getClassrooms().length +
                ", Optimal capacity: " + school.getOptimalCapacity());
    }

    private static void adaptIfNeeded(String label, int needed, int current,
            java.util.function.IntUnaryOperator roomAdder, GameView view) {
        if (needed <= current) {
            return;
        }

        int toAdd = needed - current;
        view.appendOutput("  Adding " + toAdd + " " + label + " (have " + current + ", need " + needed + ")");
        roomAdder.applyAsInt(toAdd);
    }

    /**
     * Gets the funding model being used.
     *
     * @return the funding model
     */
    public SchoolFundingModel getFundingModel() {
        return fundingModel;
    }
}
