package constants;

public final class SchoolConstants {
    // OVERALL MODIFIERS
    public static final int TOTAL_SCHOOL_PERIODS = 8;
    public static final int SCHOOL_FOUNDED_YEAR_2000s_CHANCE = 21;
    public static final int SCHOOL_FOUNDED_YEAR_1960s_CHANCE = 34;
    public static final int SCHOOL_FOUNDED_YEAR_1950s_CHANCE = 46;
    public static final int SCHOOL_FOUNDED_2000s_LOWER_LIMIT = 2001;
    public static final int SCHOOL_FOUNDED_1960s_LOWER_LIMIT = 1960;
    public static final int SCHOOL_FOUNDED_1960s_UPPER_LIMIT = 1969;
    public static final int SCHOOL_FOUNDED_1950s_LOWER_LIMIT = 1900;
    public static final int SCHOOL_FOUNDED_1950s_UPPER_LIMIT = 1949;
    public static final int SCHOOL_FOUNDED_OTHER_LOWER_LIMIT = 1950;
    public static final int SCHOOL_FOUNDED_OTHER_UPPER_LIMIT = 2000;

    // STUDENT CAP MODIFIERS
    public static final double TOTAL_STUDENT_CAP_MODIFIER = 0.55;
    // STAFF REQUIREMENT MODIFIERS
    public static final int OFFICE_NUMBER_MODIFIER = 2;
    public static final int UTILITY_ROOM_NUMBER_MODIFIER = 2;
    public static final int LIBRARY_ROOM_NUMBER_MODIFIER = 2;
    public static final double TOTAL_STAFF_CAP_MODIFIER = 0.025;
    // SCHOOL NAME LOADER
    public static final int SCHOOL_NAME_SELECTION_LOWER_LIMIT = 1;
    public static final int SCHOOL_NAME_SELECTION_UPPER_LIMIT = 20;
    public static final int SCHOOL_NAME_PLACES_WEIGHT = 14;
    public static final int SCHOOL_NAME_SINGLE_WEIGHT = 17;
    // UTILITY ROOMS
    public static final int UTILITY_CONNECTION_COUNT = 2;
    public static final int UTILITY_WINDOW_COUNT = 0;
    public static final int UTILITY_STAFF_LOWER_LIMIT = 1;
    public static final int UTILITY_STAFF_UPPER_LIMIT = 3;
    public static final int UTILITY_STUDENT_CAP_LOWER_LIMIT = 1;
    public static final int UTILITY_STUDENT_CAP_UPPER_LIMIT = 4;
    public static final int UTILITY_ROOM_NUMBER_LOWER_LIMIT = 0;
    public static final int UTILITY_ROOM_NUMBER_UPPER_LIMIT = 9;
    // BATHROOMS
    public static final int BATHROOM_CAPACITY_LOWER_LIMIT = 4;
    public static final int BATHROOM_CAPACITY_UPPER_LIMIT = 20;
    public static final int BATHROOM_CONNECTION_COUNT = 2;
    public static final int BATHROOM_WINDOW_LOWER_COUNT = 1;
    public static final int BATHROOM_WINDOW_UPPER_COUNT = 3;
    public static final int BATHROOM_INITIAL_STAFF = 0;
    public static final int STALL_NUMBER_LOWER_LIMIT = 3;
    public static final int STALL_NUMBER_UPPER_LIMIT = 7;
    public static final int BATHROOM_NUMBER_LOWER_LIMIT = 0;
    public static final int BATHROOM_NUMBER_UPPER_LIMIT = 9;
    // BREAKROOMS
    public static final int BREAKROOM_STUDENT_CAPACITY = 0;
    public static final int BREAKROOM_CONNECTION_LOWER_LIMIT = 1;
    public static final int BREAKROOM_CONNECTION_UPPER_LIMIT = 2;
    public static final int BREAKROOM_WINDOW_LOWER_COUNT = 2;
    public static final int BREAKROOM_WINDOW_UPPER_COUNT = 6;
    public static final int BREAKROOM_INITIAL_STAFF_LOWER_LIMIT = 10;
    public static final int BREAKROOM_INITIAL_STAFF_UPPER_LIMIT = 25;
    public static final int BREAKROOM_NUMBER_LOWER_LIMIT = 0;
    public static final int BREAKROOM_NUMBER_UPPER_LIMIT = 9;
    // CLASSROOMS
    public static final int CLASSROOM_WEIGHT = 7;
    public static final int CLASSROOM_STUDENT_CAPACITY_LOWER_LIMIT = 20;
    public static final int CLASSROOM_STUDENT_CAPACITY_UPPER_LIMIT = 40;  // Increased from 30 for larger rooms
    public static final int CLASSROOM_CONNECTION_LOWER_LIMIT = 3;
    public static final int CLASSROOM_CONNECTION_UPPER_LIMIT = 5;
    public static final int CLASSROOM_INITIAL_STAFF = 1;
    public static final int CLASSROOM_NUMBER_LOWER_LIMIT = 0;
    public static final int CLASSROOM_NUMBER_UPPER_LIMIT = 99;
    // Classroom capacity distribution thresholds for weighted generation
    // ~40% small (20-25), ~40% medium (26-34), ~20% large (35-40)
    public static final int CLASSROOM_SMALL_CAP_LOWER = 20;
    public static final int CLASSROOM_SMALL_CAP_UPPER = 25;
    public static final int CLASSROOM_MEDIUM_CAP_LOWER = 26;
    public static final int CLASSROOM_MEDIUM_CAP_UPPER = 34;
    public static final int CLASSROOM_LARGE_CAP_LOWER = 35;
    public static final int CLASSROOM_LARGE_CAP_UPPER = 40;
    public static final int CLASSROOM_SIZE_SMALL_WEIGHT = 40;   // 40% chance
    public static final int CLASSROOM_SIZE_MEDIUM_WEIGHT = 80;  // 40% chance (40-80)
    // Remaining 20% is large (80-100)
    // ROOM DIVIDERS
    // Dividers allow a room to be split into two teaching spaces with two teachers
    // Only rooms with capacity >= minimum can have dividers
    public static final int DIVIDER_ELIGIBLE_MIN_CAPACITY = 30;  // Only rooms >= 30 can have dividers
    public static final int DIVIDER_PROBABILITY = 15;  // 15% of eligible classrooms get dividers
    // COMPUTER LABS
    public static final int COMPUTER_STUDENT_CAPACITY_LOWER_LIMIT = 15;
    public static final int COMPUTER_STUDENT_CAPACITY_UPPER_LIMIT = 45;
    public static final int COMPUTER_CONNECTION_LOWER_LIMIT = 2;
    public static final int COMPUTER_CONNECTION_UPPER_LIMIT = 6;
    public static final int COMPUTER_WINDOW_COUNT = 0;
    public static final int COMPUTER_INITIAL_STAFF_LOWER_LIMIT = 0;
    public static final int COMPUTER_INITIAL_STAFF_UPPER_LIMIT = 1;
    // COURTYARDS
    public static final int COURTYARD_STUDENT_CAPACITY_LOWER_LIMIT = 35;
    public static final int COURTYARD_STUDENT_CAPACITY_UPPER_LIMIT = 150;
    public static final int COURTYARD_CONNECTION_COUNT = 16;
    public static final int COURTYARD_WINDOW_COUNT = 0;
    public static final int COURTYARD_INITIAL_STAFF = 0;
    // GYMS
    public static final int GYM_STUDENT_CAPACITY_LOWER_LIMIT = 100;
    public static final int GYM_STUDENT_CAPACITY_UPPER_LIMIT = 450;
    public static final int GYM_CONNECTION_LOWER_LIMIT = 6;
    public static final int GYM_CONNECTION_UPPER_LIMIT = 9;
    public static final int GYM_WINDOW_LOWER_LIMIT = 4;
    public static final int GYM_WINDOW_UPPER_LIMIT = 16;
    public static final int GYM_INITIAL_STAFF = 0;
    public static final int GYM_ROOM_NUMBER_LOWER_LIMIT = 0;
    public static final int GYM_ROOM_NUMBER_UPPER_LIMIT = 9;
    // HALLWAYS
    public static final int HALLWAY_CONNECTION_LOWER_LIMIT = 8;
    public static final int HALLWAY_CONNECTION_UPPER_LIMIT = 16;
    public static final int HALLWAY_WINDOW_LOWER_LIMIT = 0;
    public static final int HALLWAY_WINDOW_UPPER_LIMIT = 6;
    public static final int HALLWAY_INITIAL_STAFF = 0;
    public static final int HALLWAY_STUDENT_CAPACITY_LOWER_LIMIT = 40;
    public static final int HALLWAY_STUDENT_CAPACITY_UPPER_LIMIT = 80;
    //LIBRARIES
    public static final int LIBRARY_CONNECTION_LOWER_LIMIT = 3;
    public static final int LIBRARY_CONNECTION_UPPER_LIMIT = 8;
    public static final int LIBRARY_WINDOW_LOWER_LIMIT = 4;
    public static final int LIBRARY_WINDOW_UPPER_LIMIT = 20;
    public static final int LIBRARY_INITIAL_STAFF = 2;
    public static final int LIBRARY_STUDENT_CAPACITY_LOWER_LIMIT = 30;
    public static final int LIBRARY_STUDENT_CAPACITY_UPPER_LIMIT = 200;
    public static final int LIBRARY_NUMBER_LOWER_LIMIT = 0;
    public static final int LIBRARY_NUMBER_UPPER_LIMIT = 99;
    // LUNCHROOMS
    public static final int LUNCH_CONNECTION_LOWER_LIMIT = 6;
    public static final int LUNCH_CONNECTION_UPPER_LIMIT = 8;
    public static final int LUNCH_WINDOW_LOWER_LIMIT = 5;
    public static final int LUNCH_WINDOW_UPPER_LIMIT = 24;
    public static final int LUNCH_INITIAL_STAFF_LOWER_LIMIT = 3;
    public static final int LUNCH_INITIAL_STAFF_UPPER_LIMIT = 10;
    public static final int LUNCH_STUDENT_CAPACITY_LOWER_LIMIT = 50;
    public static final int LUNCH_STUDENT_CAPACITY_UPPER_LIMIT = 250;
    public static final int LUNCH_NUMBER_LOWER_LIMIT = 0;
    public static final int LUNCH_NUMBER_UPPER_LIMIT = 9;
    // OFFICES
    public static final int PRINCIPAL_CONNECTION_COUNT = 2;
    public static final int PRINCIPAL_WINDOW_COUNT = 3;
    public static final int PRINCIPAL_INITIAL_STAFF = 1;
    public static final int PRINCIPAL_INITIAL_STUDENTS = 0;
    public static final int PRINCIPAL_STUDENT_CAP = 4;
    public static final int VICE_PRINCIPAL_CONNECTION_COUNT = 2;
    public static final int VICE_PRINCIPAL_WINDOW_COUNT = 2;
    public static final int VICE_PRINCIPAL_INITIAL_STAFF = 1;
    public static final int VICE_PRINCIPAL_INITIAL_STUDENTS = 0;
    public static final int VICE_PRINCIPAL_STUDENT_CAP = 4;
    public static final int GUIDANCE_CONNECTION_COUNT = 2;
    public static final int GUIDANCE_WINDOW_COUNT = 2;
    public static final int GUIDANCE_INITIAL_STAFF = 1;
    public static final int GUIDANCE_INITIAL_STUDENTS = 0;
    public static final int GUIDANCE_STUDENT_CAP = 4;
    public static final int FRONT_OFFICE_CONNECTION_COUNT = 12;
    public static final int FRONT_OFFICE_WINDOW_COUNT = 2;
    public static final int FRONT_OFFICE_INITIAL_STAFF = 2;
    public static final int FRONT_OFFICE_INITIAL_STUDENTS = 0;
    public static final int FRONT_OFFICE_STUDENT_CAP = 15;
    public static final int NURSE_CONNECTION_COUNT = 3;
    public static final int NURSE_WINDOW_COUNT = 1;
    public static final int NURSE_INITIAL_STAFF = 2;
    public static final int NURSE_INITIAL_STUDENTS = 0;
    public static final int NURSE_STUDENT_CAP = 6;
    public static final int DEFAULT_OFFICE_CONNECTION_COUNT = 2;
    public static final int DEFAULT_OFFICE_WINDOW_COUNT = 0;
    public static final int DEFAULT_OFFICE_INITIAL_STAFF = 1;
    public static final int DEFAULT_OFFICE_INITIAL_STUDENTS = 0;
    public static final int DEFAULT_OFFICE_STUDENT_CAP_LOWER_LIMIT = 2;
    public static final int DEFAULT_OFFICE_STUDENT_CAP_UPPER_LIMIT = 6;
    // ART STUDIOS
    public static final int ART_CONNECTION_LOWER_LIMIT = 4;
    public static final int ART_CONNECTION_UPPER_LIMIT = 6;
    public static final int ART_WINDOW_LOWER_LIMIT = 5;
    public static final int ART_WINDOW_UPPER_LIMIT = 10;
    public static final int ART_INITIAL_STAFF_LOWER_LIMIT = 1;
    public static final int ART_INITIAL_STAFF_UPPER_LIMIT = 2;
    public static final int ART_STUDENT_CAPACITY_LOWER_LIMIT = 15;
    public static final int ART_STUDENT_CAPACITY_UPPER_LIMIT = 40;
    public static final int ART_NUMBER_LOWER_LIMIT = 100;
    public static final int ART_NUMBER_UPPER_LIMIT = 999;
    // ATHLETIC FIELDS
    public static final int ATHLETIC_CONNECTION_COUNT = 16;
    public static final int ATHLETIC_WINDOW_COUNT = 0;
    public static final int ATHLETIC_INITIAL_STAFF_LOWER_LIMIT = 0;
    public static final int ATHLETIC_INITIAL_STAFF_UPPER_LIMIT = 2;
    public static final int ATHLETIC_STUDENT_CAPACITY_LOWER_LIMIT = 150;
    public static final int ATHLETIC_STUDENT_CAPACITY_UPPER_LIMIT = 500;
    public static final int ATHLETIC_NUMBER_LOWER_LIMIT = 100;
    public static final int ATHLETIC_NUMBER_UPPER_LIMIT = 999;
    // AUDITORIUMS
    public static final int AUDITORIUM_CONNECTION_COUNT = 16;
    public static final int AUDITORIUM_WINDOW_COUNT = 0;
    public static final int AUDITORIUM_INITIAL_STAFF = 0;
    public static final int AUDITORIUM_STUDENT_CAPACITY_LOWER_LIMIT = 150;
    public static final int AUDITORIUM_STUDENT_CAPACITY_UPPER_LIMIT = 500;
    public static final int AUDITORIUM_NUMBER_LOWER_LIMIT = 100;
    public static final int AUDITORIUM_NUMBER_UPPER_LIMIT = 999;
    // DRAMA ROOMS
    public static final int DRAMA_CONNECTION_LOWER_LIMIT = 2;
    public static final int DRAMA_CONNECTION_UPPER_LIMIT = 6;
    public static final int DRAMA_WINDOW_LOWER_LIMIT = 0;
    public static final int DRAMA_WINDOW_UPPER_LIMIT = 6;
    public static final int DRAMA_INITIAL_STAFF = 1;
    public static final int DRAMA_STUDENT_CAPACITY_LOWER_LIMIT = 15;
    public static final int DRAMA_STUDENT_CAPACITY_UPPER_LIMIT = 35;
    public static final int DRAMA_NUMBER_LOWER_LIMIT = 100;
    public static final int DRAMA_NUMBER_UPPER_LIMIT = 999;
    // LOCKER ROOMS
    public static final int LOCKER_CONNECTION_LOWER_LIMIT = 2;
    public static final int LOCKER_CONNECTION_UPPER_LIMIT = 6;
    public static final int LOCKER_WINDOW_COUNT = 0;
    public static final int LOCKER_INITIAL_STAFF = 0;
    public static final int LOCKER_STUDENT_CAPACITY_LOWER_LIMIT = 30;
    public static final int LOCKER_STUDENT_CAPACITY_UPPER_LIMIT = 90;
    public static final int LOCKER_NUMBER_LOWER_LIMIT = 100;
    public static final int LOCKER_NUMBER_UPPER_LIMIT = 999;
    // MUSIC ROOMS
    public static final int MUSIC_CONNECTION_LOWER_LIMIT = 3;
    public static final int MUSIC_CONNECTION_UPPER_LIMIT = 7;
    public static final int MUSIC_WINDOW_LOWER_LIMIT = 0;
    public static final int MUSIC_WINDOW_UPPER_LIMIT = 4;
    public static final int MUSIC_INITIAL_STAFF = 1;
    public static final int MUSIC_STUDENT_CAPACITY_LOWER_LIMIT = 30;
    public static final int MUSIC_STUDENT_CAPACITY_UPPER_LIMIT = 150;
    public static final int MUSIC_NUMBER_LOWER_LIMIT = 100;
    public static final int MUSIC_NUMBER_UPPER_LIMIT = 999;
    // SCIENCE LABS
    public static final int SCIENCE_LAB_CONNECTION_LOWER_LIMIT = 2;
    public static final int SCIENCE_LAB_CONNECTION_UPPER_LIMIT = 4;
    public static final int SCIENCE_LAB_WINDOW_COUNT = 0;
    public static final int SCIENCE_LAB_INITIAL_STAFF = 0;
    public static final int SCIENCE_LAB_STUDENT_CAPACITY_LOWER_LIMIT = 15;
    public static final int SCIENCE_LAB_STUDENT_CAPACITY_UPPER_LIMIT = 40;
    public static final int SCIENCE_LAB_NUMBER_LOWER_LIMIT = 100;
    public static final int SCIENCE_LAB_NUMBER_UPPER_LIMIT = 999;
    // CONFERENCE ROOMS
    public static final int CONFERENCE_CONNECTION_LOWER_LIMIT = 3;
    public static final int CONFERENCE_CONNECTION_UPPER_LIMIT = 4;
    public static final int CONFERENCE_WINDOW_LOWER_LIMIT = 1;
    public static final int CONFERENCE_WINDOW_UPPER_LIMIT = 5;
    public static final int CONFERENCE_INITIAL_STAFF = 0;
    public static final int CONFERENCE_STUDENT_CAPACITY_LOWER_LIMIT = 15;
    public static final int CONFERENCE_STUDENT_CAPACITY_UPPER_LIMIT = 35;
    public static final int CONFERENCE_NUMBER_LOWER_LIMIT = 100;
    public static final int CONFERENCE_NUMBER_UPPER_LIMIT = 999;
    // VOCATIONAL ROOMS
    public static final int VOCATIONAL_CONNECTION_LOWER_LIMIT = 3;
    public static final int VOCATIONAL_CONNECTION_UPPER_LIMIT = 5;
    public static final int VOCATIONAL_WINDOW_LOWER_LIMIT = 1;
    public static final int VOCATIONAL_WINDOW_UPPER_LIMIT = 6;
    public static final int VOCATIONAL_INITIAL_STAFF = 1;
    public static final int VOCATIONAL_STUDENT_CAPACITY_LOWER_LIMIT = 25;
    public static final int VOCATIONAL_STUDENT_CAPACITY_UPPER_LIMIT = 45;
    public static final int VOCATIONAL_NUMBER_LOWER_LIMIT = 100;
    public static final int VOCATIONAL_NUMBER_UPPER_LIMIT = 999;
    // PARKING LOTS
    public static final int PARKING_CONNECTION_COUNT = 16;
    public static final int PARKING_WINDOW_COUNT = 0;
    public static final int PARKING_INITIAL_STAFF = 0;
    public static final int PARKING_STUDENT_CAPACITY_LOWER_LIMIT = 30;
    public static final int PARKING_STUDENT_CAPACITY_UPPER_LIMIT = 150;
    public static final int PARKING_NUMBER_LOWER_LIMIT = 100;
    public static final int PARKING_NUMBER_UPPER_LIMIT = 999;
    // PORTABLE CLASSROOMS
    // Portables are temporary modular buildings used as additional classroom space.
    // They are more common at underfunded schools (present in ~1/3 of American schools)
    // and can only connect to outdoor spaces (fields, courtyards, parking lots).
    public static final int PORTABLE_CONNECTION_COUNT = 2;
    public static final int PORTABLE_WINDOW_LOWER_LIMIT = 0;
    public static final int PORTABLE_WINDOW_UPPER_LIMIT = 2;
    public static final int PORTABLE_INITIAL_STAFF = 1;
    public static final int PORTABLE_STUDENT_CAPACITY_LOWER_LIMIT = 20;
    public static final int PORTABLE_STUDENT_CAPACITY_UPPER_LIMIT = 35;
    public static final int PORTABLE_NUMBER_LOWER_LIMIT = 0;
    public static final int PORTABLE_NUMBER_UPPER_LIMIT = 99;
    // Portable generation probabilities by funding level (percentage chance school has portables)
    public static final int PORTABLE_CHANCE_SEVERELY_UNDERFUNDED = 30;
    public static final int PORTABLE_CHANCE_UNDERFUNDED = 25;
    public static final int PORTABLE_CHANCE_ADEQUATE = 15;
    public static final int PORTABLE_CHANCE_WELL_FUNDED = 5;
    public static final int PORTABLE_CHANCE_EXCELLENTLY_FUNDED = 5;
    // AMOUNT OF EACH ROOM
    public static final int ART_AMOUNT_LOWER_LIMIT = 1;
    public static final int ART_AMOUNT_UPPER_LIMIT = 4;
    public static final int ATHLETIC_AMOUNT_LOWER_LIMIT = 1;
    public static final int ATHLETIC_AMOUNT_UPPER_LIMIT = 3;
    public static final int AUDITORIUM_AMOUNT_LOWER_LIMIT = 1;
    public static final int AUDITORIUM_AMOUNT_UPPER_LIMIT = 2;
    public static final int BREAKROOM_AMOUNT_LOWER_LIMIT = 1;
    public static final int BREAKROOM_AMOUNT_UPPER_LIMIT = 4;
    public static final int CLASSROOM_AMOUNT_LOWER_LIMIT = 29;
    public static final int CLASSROOM_AMOUNT_UPPER_LIMIT = 78;
    public static final int CONFERENCE_AMOUNT_LOWER_LIMIT = 1;
    public static final int CONFERENCE_AMOUNT_UPPER_LIMIT = 4;
    public static final int COMPUTER_LAB_AMOUNT_LOWER_LIMIT = 1;
    public static final int COMPUTER_LAB_AMOUNT_UPPER_LIMIT = 3;
    public static final int COURTYARD_AMOUNT_LOWER_LIMIT = 2;
    public static final int COURTYARD_AMOUNT_UPPER_LIMIT = 5;
    public static final int DRAMA_AMOUNT_LOWER_LIMIT = 1;
    public static final int DRAMA_AMOUNT_UPPER_LIMIT = 2;
    public static final int GYM_AMOUNT_LOWER_LIMIT = 1;
    public static final int GYM_AMOUNT_UPPER_LIMIT = 3;
    public static final int HALLWAY_AMOUNT_LOWER_LIMIT = 9;
    public static final int HALLWAY_AMOUNT_UPPER_LIMIT = 12;
    public static final int LIBRARY_AMOUNT_LOWER_LIMIT = 1;
    public static final int LIBRARY_AMOUNT_UPPER_LIMIT = 2;
    public static final int LUNCHROOM_AMOUNT_LOWER_LIMIT = 1;
    public static final int LUNCHROOM_AMOUNT_UPPER_LIMIT = 2;
    public static final int MUSIC_AMOUNT_LOWER_LIMIT = 1;
    public static final int MUSIC_AMOUNT_UPPER_LIMIT = 2;
    public static final int OFFICE_AMOUNT_LOWER_LIMIT = 5;
    public static final int SCIENCE_LAB_AMOUNT_LOWER_LIMIT = 2;
    public static final int SCIENCE_LAB_AMOUNT_UPPER_LIMIT = 6;
    public static final int UTILITY_AMOUNT_LOWER_LIMIT = 5;
    public static final int UTILITY_AMOUNT_UPPER_LIMIT = 10;
    public static final int VOCATIONAL_AMOUNT_LOWER_LIMIT = 4;
    public static final int VOCATIONAL_AMOUNT_UPPER_LIMIT = 14;
    public static final int PARKING_AMOUNT_LOWER_LIMIT = 2;
    public static final int PARKING_AMOUNT_UPPER_LIMIT = 6;
    public static final int BATHROOM_AMOUNT = 15;
    // Portable amounts vary inversely with funding - more portables at underfunded schools
    public static final int PORTABLE_AMOUNT_SEVERELY_UNDERFUNDED_LOWER = 3;
    public static final int PORTABLE_AMOUNT_SEVERELY_UNDERFUNDED_UPPER = 8;
    public static final int PORTABLE_AMOUNT_UNDERFUNDED_LOWER = 2;
    public static final int PORTABLE_AMOUNT_UNDERFUNDED_UPPER = 5;
    public static final int PORTABLE_AMOUNT_ADEQUATE_LOWER = 1;
    public static final int PORTABLE_AMOUNT_ADEQUATE_UPPER = 3;
    public static final int PORTABLE_AMOUNT_WELL_FUNDED_LOWER = 0;
    public static final int PORTABLE_AMOUNT_WELL_FUNDED_UPPER = 1;
    public static final int PORTABLE_AMOUNT_EXCELLENTLY_FUNDED_LOWER = 0;
    public static final int PORTABLE_AMOUNT_EXCELLENTLY_FUNDED_UPPER = 1;

    public static final int LOCKER_ROOM_MODIFIER = 2;

    private SchoolConstants() {
    }

}
