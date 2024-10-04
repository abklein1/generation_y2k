package constants;

public final class SimConstants {
    // TIME
    public static final int STARTING_YEAR = 2004;
    public static final int STARTING_MONTH = 7;
    public static final int STARTING_DATE = 23;
    public static final int STARTING_HOUR = -4;
    public static final int STARTING_MINUTE = 0;

    // GENDER RATES
    public static final int GENDER_SAMPLE_SIZE = 20313;
    public static final int GENDER_MALE_RATE = 10339;

    // NAME SUFFIX RATES
    public static final int SUFFIX_GENERATION_SAMPLE_SIZE = 170;
    public static final int SUFFIX_GENERATION_RATE = 2;

    // STUDENT HYPHEN NAME RATES
    public static final int STUDENT_HYPHEN_GENERATION_SAMPLE_SIZE = 100;
    public static final int STUDENT_HYPHEN_GENERATION_RATE = 3;

    // TEACHER HYPHEN NAME RATES
    public static final int TEACHER_HYPHEN_GENERATION_SAMPLE_SIZE = 100;
    public static final int TEACHER_HYPHEN_GENERATION_RATE = 4;

    // STUDENT HAIR LENGTH
    public static final int STUDENT_HAIR_LENGTH_SAMPLE_SIZE = 10000;

    // TEACHER HAIR LENGTH
    public static final int TEACHER_HAIR_LENGTH_SAMPLE_SIZE = 10000;

    // TEACHER HAIR TYPE
    public static final int TEACHER_HAIR_TYPE_SAMPLE_SIZE = 975;
    public static final int TEACHER_HAIR_SELECTION_SAMPLE_SIZE = 102;

    // TEACHER YEARS OF EXPERIENCE MODIFIER
    public static final int TEACHER_YEARS_OF_EXPERIENCE_MODIFIER = 23;

    // STUDENT INCOME LEVEL
    public static final int STUDENT_INCOME_LEVEL_SAMPLE_SIZE = 100;

    // STUDENT POP STAT DISTRIBUTION
    public static final int STUDENT_POP_INTELLIGENCE_MEAN = 100;
    public static final int STUDENT_POP_INTELLIGENCE_STANDARD_DEVIATION = 15;
    public static final int STUDENT_POP_CHARISMA_MEAN = 50;
    public static final int STUDENT_POP_CHARISMA_STANDARD_DEVIATION = 15;
    public static final int STUDENT_POP_AGILITY_MEAN = 50;
    public static final int STUDENT_POP_AGILITY_STANDARD_DEVIATION = 15;
    public static final int STUDENT_POP_DETERMINATION_MEAN = 50;
    public static final int STUDENT_POP_DETERMINATION_STANDARD_DEVIATION = 15;
    public static final int STUDENT_POP_PERCEPTION_MEAN = 50;
    public static final int STUDENT_POP_PERCEPTION_STANDARD_DEVIATION = 15;
    public static final int STUDENT_POP_LUCK_MEAN = 0;
    public static final int STUDENT_POP_LUCK_STANDARD_DEVIATION = 15;

    // TEACHER POP STAT DISTRIBUTION
    public static final int TEACHER_POP_INTELLIGENCE_MEAN = 100;
    public static final int TEACHER_POP_INTELLIGENCE_STANDARD_DEVIATION = 15;
    public static final int TEACHER_POP_CHARISMA_MEAN = 50;
    public static final int TEACHER_POP_CHARISMA_STANDARD_DEVIATION = 15;
    public static final int TEACHER_POP_AGILITY_MEAN = 50;
    public static final int TEACHER_POP_AGILITY_STANDARD_DEVIATION = 15;
    public static final int TEACHER_POP_DETERMINATION_MEAN = 50;
    public static final int TEACHER_POP_DETERMINATION_STANDARD_DEVIATION = 15;
    public static final int TEACHER_POP_PERCEPTION_MEAN = 50;
    public static final int TEACHER_POP_PERCEPTION_STANDARD_DEVIATION = 15;
    public static final int TEACHER_POP_LUCK_MEAN = 0;
    public static final int TEACHER_POP_LUCK_STANDARD_DEVIATION = 10;

    // TEACHER HAIR COLOR DISTRIBUTION
    public static final int TEACHER_BLACK_HAIR_LOWER_BOUND = 0;
    public static final int TEACHER_BLACK_HAIR_UPPER_BOUND = 21;
    public static final int TEACHER_DARK_BROWN_HAIR_LOWER_BOUND = 22;
    public static final int TEACHER_DARK_BROWN_HAIR_UPPER_BOUND = 37;
    public static final int TEACHER_MEDIUM_BROWN_HAIR_LOWER_BOUND = 38;
    public static final int TEACHER_MEDIUM_BROWN_HAIR_UPPER_BOUND = 48;
    public static final int TEACHER_LIGHT_BROWN_HAIR_LOWER_BOUND = 49;
    public static final int TEACHER_LIGHT_BROWN_HAIR_UPPER_BOUND = 56;
    public static final int TEACHER_BLONDE_HAIR_LOWER_BOUND = 57;
    public static final int TEACHER_BLONDE_HAIR_UPPER_BOUND = 64;
    public static final int TEACHER_CHESTNUT_HAIR_LOWER_BOUND = 65;
    public static final int TEACHER_CHESTNUT_HAIR_UPPER_BOUND = 71;
    public static final int TEACHER_MAHOGANY_HAIR_LOWER_BOUND = 72;
    public static final int TEACHER_MAHOGANY_HAIR_UPPER_BOUND = 78;
    public static final int TEACHER_DIRTY_BLOND_HAIR_LOWER_BOUND = 79;
    public static final int TEACHER_DIRTY_BLOND_HAIR_UPPER_BOUND = 84;
    public static final int TEACHER_GOLDEN_BLOND_HAIR_LOWER_BOUND = 85;
    public static final int TEACHER_GOLDEN_BLOND_HAIR_UPPER_BOUND = 89;
    public static final int TEACHER_LIGHT_BLOND_HAIR_LOWER_BOUND = 90;
    public static final int TEACHER_LIGHT_BLOND_HAIR_UPPER_BOUND = 93;
    public static final int TEACHER_GOLDEN_BROWN_HAIR_LOWER_BOUND = 94;
    public static final int TEACHER_GOLDEN_BROWN_HAIR_UPPER_BOUND = 96;
    public static final int TEACHER_CARAMEL_HAIR_LOWER_BOUND = 97;
    public static final int TEACHER_CARAMEL_HAIR_UPPER_BOUND = 97;
    public static final int TEACHER_STRAWBERRY_BLOND_HAIR_LOWER_BOUND = 98;
    public static final int TEACHER_STRAWBERRY_BLOND_HAIR_UPPER_BOUND = 98;
    public static final int TEACHER_COPPER_HAIR_LOWER_BOUND = 99;
    public static final int TEACHER_COPPER_HAIR_UPPER_BOUND = 99;
    public static final int TEACHER_RED_HAIR_LOWER_BOUND = 100;
    public static final int TEACHER_RED_HAIR_UPPER_BOUND = 100;
    public static final int TEACHER_PLATINUM_BLOND_HAIR_LOWER_BOUND = 101;
    public static final int TEACHER_PLATINUM_BLOND_HAIR_UPPER_BOUND = 101;
    public static final int TEACHER_OTHER_HAIR_SAMPLE_SIZE = 5;
    public static final int TEACHER_YOUNGER_AGE_HAIR_COLOR_THRESHOLD = 37;
    public static final int TEACHER_MIDDLE_AGE_HAIR_COLOR_THRESHOLD = 47;
    public static final int TEACHER_MIDDLE_AGE_GRAY_HAIR_THRESHOLD = 90;
    public static final int TEACHER_MIDDLE_AGE_BLACK_HAIR_LOWER_BOUND = 0;
    public static final int TEACHER_MIDDLE_AGE_BLACK_HAIR_UPPER_BOUND = 21;
    public static final int TEACHER_MIDDLE_AGE_DARK_BROWN_HAIR_LOWER_BOUND = 22;
    public static final int TEACHER_MIDDLE_AGE_DARK_BROWN_HAIR_UPPER_BOUND = 37;
    public static final int TEACHER_MIDDLE_AGE_MEDIUM_BROWN_HAIR_LOWER_BOUND = 38;
    public static final int TEACHER_MIDDLE_AGE_MEDIUM_BROWN_HAIR_UPPER_BOUND = 48;
    public static final int TEACHER_MIDDLE_AGE_LIGHT_BROWN_HAIR_LOWER_BOUND = 49;
    public static final int TEACHER_MIDDLE_AGE_LIGHT_BROWN_HAIR_UPPER_BOUND = 56;
    public static final int TEACHER_MIDDLE_AGE_BLONDE_HAIR_LOWER_BOUND = 57;
    public static final int TEACHER_MIDDLE_AGE_BLONDE_HAIR_UPPER_BOUND = 64;
    public static final int TEACHER_MIDDLE_AGE_CHESTNUT_HAIR_LOWER_BOUND = 65;
    public static final int TEACHER_MIDDLE_AGE_CHESTNUT_HAIR_UPPER_BOUND = 71;
    public static final int TEACHER_MIDDLE_AGE_MAHOGANY_HAIR_LOWER_BOUND = 72;
    public static final int TEACHER_MIDDLE_AGE_MAHOGANY_HAIR_UPPER_BOUND = 78;
    public static final int TEACHER_MIDDLE_AGE_DIRTY_BLOND_HAIR_LOWER_BOUND = 79;
    public static final int TEACHER_MIDDLE_AGE_DIRTY_BLOND_HAIR_UPPER_BOUND = 84;
    public static final int TEACHER_MIDDLE_AGE_GOLDEN_BLOND_HAIR_LOWER_BOUND = 85;
    public static final int TEACHER_MIDDLE_AGE_GOLDEN_BLOND_HAIR_UPPER_BOUND = 89;
    public static final int TEACHER_MIDDLE_AGE_LIGHT_BLOND_HAIR_LOWER_BOUND = 90;
    public static final int TEACHER_MIDDLE_AGE_LIGHT_BLOND_HAIR_UPPER_BOUND = 93;
    public static final int TEACHER_MIDDLE_AGE_GOLDEN_BROWN_HAIR_LOWER_BOUND = 94;
    public static final int TEACHER_MIDDLE_AGE_GOLDEN_BROWN_HAIR_UPPER_BOUND = 96;
    public static final int TEACHER_MIDDLE_AGE_CARAMEL_HAIR_LOWER_BOUND = 97;
    public static final int TEACHER_MIDDLE_AGE_CARAMEL_HAIR_UPPER_BOUND = 97;
    public static final int TEACHER_MIDDLE_AGE_STRAWBERRY_BLOND_HAIR_LOWER_BOUND = 98;
    public static final int TEACHER_MIDDLE_AGE_STRAWBERRY_BLOND_HAIR_UPPER_BOUND = 98;
    public static final int TEACHER_MIDDLE_AGE_COPPER_HAIR_LOWER_BOUND = 99;
    public static final int TEACHER_MIDDLE_AGE_COPPER_HAIR_UPPER_BOUND = 99;
    public static final int TEACHER_MIDDLE_AGE_RED_HAIR_LOWER_BOUND = 100;
    public static final int TEACHER_MIDDLE_AGE_RED_HAIR_UPPER_BOUND = 100;
    public static final int TEACHER_MIDDLE_AGE_PLATINUM_BLOND_HAIR_LOWER_BOUND = 101;
    public static final int TEACHER_MIDDLE_AGE_PLATINUM_BLOND_HAIR_UPPER_BOUND = 101;
    public static final int TEACHER_OLD_AGE_GRAY_HAIR_THRESHOLD = 28;
    public static final int TEACHER_OLD_AGE_BLACK_HAIR_LOWER_BOUND = 0;
    public static final int TEACHER_OLD_AGE_BLACK_HAIR_UPPER_BOUND = 15;
    public static final int TEACHER_OLD_AGE_DARK_BROWN_HAIR_LOWER_BOUND = 16;
    public static final int TEACHER_OLD_AGE_DARK_BROWN_HAIR_UPPER_BOUND = 22;
    public static final int TEACHER_OLD_AGE_MEDIUM_BROWN_HAIR_LOWER_BOUND = 23;
    public static final int TEACHER_OLD_AGE_MEDIUM_BROWN_HAIR_UPPER_BOUND = 29;
    public static final int TEACHER_OLD_AGE_LIGHT_BROWN_HAIR_LOWER_BOUND = 30;
    public static final int TEACHER_OLD_AGE_LIGHT_BROWN_HAIR_UPPER_BOUND = 35;
    public static final int TEACHER_OLD_AGE_BLONDE_HAIR_LOWER_BOUND = 36;
    public static final int TEACHER_OLD_AGE_BLONDE_HAIR_UPPER_BOUND = 40;
    public static final int TEACHER_OLD_AGE_CHESTNUT_HAIR_LOWER_BOUND = 41;
    public static final int TEACHER_OLD_AGE_CHESTNUT_HAIR_UPPER_BOUND = 43;
    public static final int TEACHER_OLD_AGE_MAHOGANY_HAIR_LOWER_BOUND = 44;
    public static final int TEACHER_OLD_AGE_MAHOGANY_HAIR_UPPER_BOUND = 46;
    public static final int TEACHER_OLD_AGE_DIRTY_BLOND_HAIR_LOWER_BOUND = 47;
    public static final int TEACHER_OLD_AGE_DIRTY_BLOND_HAIR_UPPER_BOUND = 50;
    public static final int TEACHER_OLD_AGE_GOLDEN_BLOND_HAIR_LOWER_BOUND = 51;
    public static final int TEACHER_OLD_AGE_GOLDEN_BLOND_HAIR_UPPER_BOUND = 53;
    public static final int TEACHER_OLD_AGE_LIGHT_BLOND_HAIR_LOWER_BOUND = 54;
    public static final int TEACHER_OLD_AGE_LIGHT_BLOND_HAIR_UPPER_BOUND = 56;
    public static final int TEACHER_OLD_AGE_GOLDEN_BROWN_HAIR_LOWER_BOUND = 57;
    public static final int TEACHER_OLD_AGE_GOLDEN_BROWN_HAIR_UPPER_BOUND = 59;
    public static final int TEACHER_OLD_AGE_CARAMEL_HAIR_LOWER_BOUND = 60;
    public static final int TEACHER_OLD_AGE_CARAMEL_HAIR_UPPER_BOUND = 60;
    public static final int TEACHER_OLD_AGE_STRAWBERRY_BLOND_HAIR_LOWER_BOUND = 61;
    public static final int TEACHER_OLD_AGE_STRAWBERRY_BLOND_HAIR_UPPER_BOUND = 61;
    public static final int TEACHER_OLD_AGE_COPPER_HAIR_LOWER_BOUND = 62;
    public static final int TEACHER_OLD_AGE_COPPER_HAIR_UPPER_BOUND = 62;
    public static final int TEACHER_OLD_AGE_RED_HAIR_LOWER_BOUND = 63;
    public static final int TEACHER_OLD_AGE_RED_HAIR_UPPER_BOUND = 63;
    public static final int TEACHER_OLD_AGE_PLATINUM_BLOND_HAIR_LOWER_BOUND = 64;
    public static final int TEACHER_OLD_AGE_PLATINUM_BLOND_HAIR_UPPER_BOUND = 64;

    // TEACHER EYE COLOR DISTRIBUTION
    public static final int TEACHER_DARK_BROWN_EYE_LOWER_BOUND = 0;
    public static final int TEACHER_DARK_BROWN_EYE_UPPER_BOUND = 52;
    public static final int TEACHER_LIGHT_BROWN_EYE_LOWER_BOUND = 53;
    public static final int TEACHER_LIGHT_BROWN_EYE_UPPER_BOUND = 75;
    public static final int TEACHER_BLUE_EYE_LOWER_BOUND = 76;
    public static final int TEACHER_BLUE_EYE_UPPER_BOUND = 83;
    public static final int TEACHER_LIGHT_BLUE_EYE_LOWER_BOUND = 84;
    public static final int TEACHER_LIGHT_BLUE_EYE_UPPER_BOUND = 90;
    public static final int TEACHER_HAZEL_EYE_LOWER_BOUND = 91;
    public static final int TEACHER_HAZEL_EYE_UPPER_BOUND = 96;
    public static final int TEACHER_AMBER_EYE_LOWER_BOUND = 97;
    public static final int TEACHER_AMBER_EYE_UPPER_BOUND = 102;
    public static final int TEACHER_GREEN_EYE_LOWER_BOUND = 103;
    public static final int TEACHER_GREEN_EYE_UPPER_BOUND = 105;
    public static final int TEACHER_GRAY_EYE_LOWER_BOUND = 106;
    public static final int TEACHER_GRAY_EYE_UPPER_BOUND = 106;
    public static final int TEACHER_VIOLET_EYE_LOWER_BOUND = 107;
    public static final int TEACHER_VIOLET_EYE_UPPER_BOUND = 107;
    public static final int TEACHER_BLACK_EYE_LOWER_BOUND = 108;
    public static final int TEACHER_BLACK_EYE_UPPER_BOUND = 108;

    // TEACHER HAIR TYPE DISTRIBUTION
    public static final int TEACHER_FINE_STRAIGHT_HAIR_LOWER_BOUND = 0;
    public static final int TEACHER_FINE_STRAIGHT_HAIR_UPPER_BOUND = 50;
    public static final int TEACHER_STRAIGHT_HAIR_LOWER_BOUND = 51;
    public static final int TEACHER_STRAIGHT_HAIR_UPPER_BOUND = 250;
    public static final int TEACHER_COARSE_STRAIGHT_HAIR_LOWER_BOUND = 251;
    public static final int TEACHER_COARSE_STRAIGHT_HAIR_UPPER_BOUND = 350;
    public static final int TEACHER_THIN_WAVEY_HAIR_LOWER_BOUND = 351;
    public static final int TEACHER_THIN_WAVEY_HAIR_UPPER_BOUND = 400;
    public static final int TEACHER_WAVEY_HAIR_LOWER_BOUND = 401;
    public static final int TEACHER_WAVEY_HAIR_UPPER_BOUND = 550;
    public static final int TEACHER_THICK_WAVEY_HAIR_LOWER_BOUND = 551;
    public static final int TEACHER_THICK_WAVEY_HAIR_UPPER_BOUND = 650;
    public static final int TEACHER_LOOSE_CURLY_HAIR_LOWER_BOUND = 651;
    public static final int TEACHER_LOOSE_CURLY_HAIR_UPPER_BOUND = 700;
    public static final int TEACHER_CURLY_HAIR_LOWER_BOUND = 701;
    public static final int TEACHER_CURLY_HAIR_UPPER_BOUND = 750;
    public static final int TEACHER_DENSE_CURLY_HAIR_LOWER_BOUND = 751;
    public static final int TEACHER_DENSE_CURLY_HAIR_UPPER_BOUND = 800;
    public static final int TEACHER_TIGHT_COILY_HAIR_LOWER_BOUND = 801;
    public static final int TEACHER_TIGHT_COILY_HAIR_UPPER_BOUND = 850;
    public static final int TEACHER_COILY_HAIR_LOWER_BOUND = 851;
    public static final int TEACHER_COILY_HAIR_UPPER_BOUND = 900;
    public static final int TEACHER_DENSE_COILY_HAIR_LOWER_BOUND = 901;
    public static final int TEACHER_DENSE_COILY_HAIR_UPPER_BOUND = 950;

    // SOCIAL LINK ADJUSTMENT
    public static final int SOCIAL_LINK_MEAN = 0;
    public static final int SOCIAL_LINK_STANDARD_DEVIATION = 25;

    // STUDENT SCHEDULE ASSIGNMENT
    public static final int LANGUAGE_CHOICE_SAMPLE_SIZE = 4;
    public static final int SENIOR_VOCATIONAL_CLASS_DETERMINATION_THRESHOLD = 35;
    public static final int CLASS_PROBABILITY_LOADER_SAMPLE_SIZE = 100;
    public static final int CLASS_PROBABILITY_LOADER_GIFTED_INTELLIGENCE_THRESHOLD = 135;
    public static final int CLASS_PROBABILITY_LOADER_HIGH_INTELLIGENCE_THRESHOLD = 120;
    public static final int CLASS_PROBABILITY_LOADER_AVERAGE_INTELLIGENCE_THRESHOLD = 100;
    public static final int CLASS_PROBABILITY_LOADER_LOW_INTELLIGENCE_THRESHOLD = 80;
    public static final int CLASS_PROBABILITY_LOADER_GIFTED_AP_PROBABILITY = 80;
    public static final int CLASS_PROBABILITY_LOADER_GIFTED_HONORS_PROBABILITY = 10;
    public static final int CLASS_PROBABILITY_LOADER_GIFTED_ON_LEVEL_PROBABILITY = 10;
    public static final int CLASS_PROBABILITY_LOADER_HIGH_AP_PROBABILITY = 50;
    public static final int CLASS_PROBABILITY_LOADER_HIGH_HONORS_PROBABILITY = 30;
    public static final int CLASS_PROBABILITY_LOADER_HIGH_ON_LEVEL_PROBABILITY = 20;
    public static final int CLASS_PROBABILITY_LOADER_AVERAGE_AP_PROBABILITY = 20;
    public static final int CLASS_PROBABILITY_LOADER_AVERAGE_HONORS_PROBABILITY = 30;
    public static final int CLASS_PROBABILITY_LOADER_AVERAGE_ON_LEVEL_PROBABILITY = 50;
    public static final int CLASS_PROBABILITY_LOADER_LOW_AP_PROBABILITY = 0;
    public static final int CLASS_PROBABILITY_LOADER_LOW_HONORS_PROBABILITY = 2;
    public static final int CLASS_PROBABILITY_LOADER_LOW_ON_LEVEL_PROBABILITY = 98;
    public static final int CLASS_PROBABILITY_LOADER_OTHER_AP_PROBABILITY = 5;
    public static final int CLASS_PROBABILITY_LOADER_OTHER_HONORS_PROBABILITY = 20;
    public static final int CLASS_PROBABILITY_LOADER_OTHER_ON_LEVEL_PROBABILITY = 75;
    public static final double CLASS_PROBABILITY_LOADER_INCOME_HIGH_AP_ADJUSTMENT = 1.2;
    public static final double CLASS_PROBABILITY_LOADER_INCOME_HIGH_HONORS_ADJUSTMENT = 1.1;
    public static final double CLASS_PROBABILITY_LOADER_INCOME_HIGH_ON_LEVEL_ADJUSTMENT = 0.9;
    public static final double CLASS_PROBABILITY_LOADER_INCOME_LOW_AP_ADJUSTMENT = 0.7;
    public static final double CLASS_PROBABILITY_LOADER_INCOME_LOW_HONORS_ADJUSTMENT = 0.9;
    public static final double CLASS_PROBABILITY_LOADER_INCOME_LOW_ON_LEVEL_ADJUSTMENT = 1.2;
    public static final int CLASS_PROBABILITY_LOADER_DETERMINATION_THRESHOLD = 50;
    public static final double CLASS_PROBABILITY_LOADER_DETERMINATION_FACTOR_DIVISOR = 50.0;
    public static final int CLASS_PROBABILITY_LOADER_DETERMINATION_HONORS_ADJUSTMENT = 2;
    public static final int CLASS_PROBABILITY_LOADER_DETERMINATION_ON_LEVEL_ADJUSTMENT = 2;
    public static final int PHYSICAL_ED_MALE_STRENGTH_THRESHOLD = 60;
    public static final int PHYSICAL_ED_MALE_LOW_STRENGTH_THRESHOLD = 29;
    public static final int PHYSICAL_ED_MALE_DETERMINATION_THRESHOLD = 60;
    public static final int PHYSICAL_ED_MALE_LOW_DETERMINATION_THRESHOLD = 30;
    public static final int PHYSICAL_ED_FEMALE_STRENGTH_THRESHOLD = 50;
    public static final int PHYSICAL_ED_FEMALE_LOW_STRENGTH_THRESHOLD = 29;
    public static final int PHYSICAL_ED_FEMALE_DETERMINATION_THRESHOLD = 50;
    public static final int PHYSICAL_ED_FEMALE_LOW_DETERMINATION_THRESHOLD = 29;

    // Student Vocational Decision Values
    public static final int CHARISMA_VOCATIONAL_LOWER_BOUND = 68;
    public static final int DETERMINATION_VOCATIONAL_LOWER_BOUND = 50;
    public static final int PERCEPTION_VOCATIONAL_LOWER_BOUND = 50;
    public static final int CREATIVITY_VOCATIONAL_LOWER_BOUND = 120;
    public static final int DETERMINATION_VOCATIONAL_LOWER_BOUND_BAND = 68;
    public static final int INTELLIGENCE_VOCATIONAL_LOWER_BOUND = 105;
    public static final int CURIOSITY_VOCATIONAL_LOWER_BOUND = 68;
    public static final int LOW_DETERMINATION_VOCATIONAL_UPPER_BOUND = 30;

    private SimConstants() {
    }
}
