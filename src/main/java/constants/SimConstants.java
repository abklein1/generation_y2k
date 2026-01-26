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
    public static final int SOCIAL_LINK_FRIEND_INITIAL_SAMPLE_SIZE = 100;
    public static final int SOCIAL_LINK_FRIEND_INITIAL_THRESHOLD = 50;
    public static final int SOCIAL_LINK_FRIEND_MAXIMUM = 5;
    public static final double SOCIAL_LINK_FRIEND_CHARISMA_MODIFIER = 0.8;
    public static final double SOCIAL_LINK_FRIEND_EMPATHY_MODIFIER = 0.5;
    public static final double SOCIAL_LINK_FRIEND_LUCK_MODIFIER = 0.2;
    public static final double SOCIAL_LINK_FRIEND_VARIABILITY = 0.1;
    public static final int SOCIAL_LINK_GRADE_CLASSMATE_SAMPLE_SIZE = 100;
    public static final int SOCIAL_LINK_GRADE_CLASSMATE_THRESHOLD = 90;
    public static final int SOCIAL_LINK_ADJACENT_GRADE_SAMPLE_SIZE = 100;
    public static final int SOCIAL_LINK_ADJACENT_GRADE_THRESHOLD = 75;
    public static final double SOCIAL_LINK_FRIEND_VARIABILITY_RANGE = 0.2;
    public static final double SOCIAL_LINK_FRIEND_SCALING_FACTOR = 10.0;
    public static final int SOCIAL_LINK_FRIEND_GRADE_CLASSMATE_SAMPLE_SIZE = 100;
    public static final int SOCIAL_LINK_FRIEND_GRADE_CLASSMATE_THRESHOLD = 90;
    public static final int SOCIAL_LINK_FRIEND_ADJACENT_GRADE_SAMPLE_SIZE = 100;
    public static final int SOCIAL_LINK_FRIEND_ADJACENT_GRADE_THRESHOLD = 75;

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

    // Braces System Constants
    // Treatment duration in months (1-3 years typical)
    public static final int BRACES_MIN_DURATION_MONTHS = 12;
    public static final int BRACES_MAX_DURATION_MONTHS = 36;
    public static final int BRACES_MEAN_DURATION_MONTHS = 24;
    public static final int BRACES_DURATION_STANDARD_DEVIATION = 6;

    // Charisma effects (less than 1 standard deviation of 15)
    // Penalty while wearing braces
    public static final int BRACES_CHARISMA_PENALTY = 10;
    // Boost after removal (net positive benefit)
    public static final int BRACES_CHARISMA_BOOST = 13;

    // Probability of having elastics with braces (approximately 60% of patients)
    public static final int BRACES_ELASTIC_PROBABILITY = 60;
    public static final int BRACES_ELASTIC_SAMPLE_SIZE = 100;

    // Alternating band colors (relatively low chance)
    public static final int BRACES_ALTERNATING_BAND_PROBABILITY = 15;  // 15% chance
    public static final int BRACES_ALTERNATING_BAND_SAMPLE_SIZE = 100;
    // When alternating, higher chance of using school colors
    public static final int BRACES_SCHOOL_COLOR_PROBABILITY = 60;  // 60% chance when alternating
    public static final int BRACES_SCHOOL_COLOR_SAMPLE_SIZE = 100;

    // Past braces rates by race (from 2004 Ohio study - total orthodontic treatment rates)
    // These represent the total who have EVER had braces (current + past)
    // Current braces rates are already defined in TraitSelection
    public static final double BRACES_TOTAL_RATE_WHITE = 0.31;
    public static final double BRACES_TOTAL_RATE_HISPANIC = 0.11;
    public static final double BRACES_TOTAL_RATE_BLACK = 0.08;
    public static final double BRACES_TOTAL_RATE_API = 0.25;
    public static final double BRACES_TOTAL_RATE_AIAN = 0.10;
    public static final double BRACES_TOTAL_RATE_2PRACE = 0.18;
    public static final double BRACES_TOTAL_RATE_DEFAULT = 0.15;

    // Income multipliers for total braces rate
    // Suburban affluent: 50%+ utilization
    // Inner city/low income: less than 10% utilization
    public static final double BRACES_INCOME_MULTIPLIER_HIGH = 1.6;
    public static final double BRACES_INCOME_MULTIPLIER_MIDDLE = 1.0;
    public static final double BRACES_INCOME_MULTIPLIER_LOW = 0.3;

    // Grade-based multipliers for how likely someone is to have ALREADY had braces removed
    // Older students are more likely to have completed treatment in the past
    public static final double BRACES_PAST_RATE_FRESHMAN = 0.15;   // 15% of total eligible already done
    public static final double BRACES_PAST_RATE_SOPHOMORE = 0.30;  // 30% of total eligible already done
    public static final double BRACES_PAST_RATE_JUNIOR = 0.50;     // 50% of total eligible already done
    public static final double BRACES_PAST_RATE_SENIOR = 0.65;     // 65% of total eligible already done

    // Vision Issues Constants (based on 1999-2004 NHANES data)
    // Base prevalence rates for refractive errors in U.S. population aged 20+
    // Note: High school students (14-18) may have slightly different rates

    // Hyperopia (farsightedness) - 3.6% age-standardized prevalence
    public static final double VISION_HYPEROPIA_BASE_RATE = 0.036;

    // Myopia (nearsightedness) - 33.1% age-standardized prevalence
    // Higher in younger populations due to near-work activities
    public static final double VISION_MYOPIA_BASE_RATE = 0.331;

    // Myopia gender differences (among 20-39 year olds)
    public static final double VISION_MYOPIA_FEMALE_RATE = 0.40;  // 40% in females
    public static final double VISION_MYOPIA_MALE_RATE = 0.33;    // 33% in males

    // Myopia rates by race/ethnicity (from NHANES data)
    public static final double VISION_MYOPIA_WHITE_RATE = 0.352;           // 35.2% non-Hispanic whites
    public static final double VISION_MYOPIA_BLACK_RATE = 0.286;           // 28.6% non-Hispanic blacks
    public static final double VISION_MYOPIA_HISPANIC_RATE = 0.251;        // 25.1% Mexican Americans
    public static final double VISION_MYOPIA_API_RATE = 0.40;              // Estimated higher for Asian populations
    public static final double VISION_MYOPIA_AIAN_RATE = 0.28;             // Estimated similar to other minorities
    public static final double VISION_MYOPIA_2PRACE_RATE = 0.32;           // Weighted average

    // Astigmatism - 36.2% age-standardized prevalence
    // Can occur alongside myopia or hyperopia
    public static final double VISION_ASTIGMATISM_BASE_RATE = 0.362;

    // Multiplier for high school students (myopia tends to develop/worsen in teen years)
    public static final double VISION_YOUTH_MYOPIA_MULTIPLIER = 1.1;

    // Perception and Agility penalties for uncorrected vision issues
    public static final int VISION_MYOPIA_PERCEPTION_PENALTY = 8;
    public static final int VISION_HYPEROPIA_PERCEPTION_PENALTY = 5;
    public static final int VISION_ASTIGMATISM_PERCEPTION_PENALTY = 4;
    public static final int VISION_MYOPIA_AGILITY_PENALTY = 5;
    public static final int VISION_HYPEROPIA_AGILITY_PENALTY = 3;
    public static final int VISION_ASTIGMATISM_AGILITY_PENALTY = 2;

    // Corrective Lens Constants (based on 1988 Medical Expenditure Panel Survey)
    // 25.4% of children 6-18 had corrective lenses
    // Note: This represents those WITH vision issues who have correction

    // Base rate of having corrective lenses among those with vision issues
    // Majority of people with vision issues have glasses
    public static final double CORRECTIVE_LENS_BASE_RATE = 0.75;

    // Gender odds ratio - girls 1.41x more likely than boys
    public static final double CORRECTIVE_LENS_FEMALE_MULTIPLIER = 1.18;  // sqrt(1.41) to balance
    public static final double CORRECTIVE_LENS_MALE_MULTIPLIER = 0.84;    // 1/1.18

    // Income-based multipliers for corrective lens access
    // Based on survey: uninsured black/Hispanic baseline, others have higher odds
    public static final double CORRECTIVE_LENS_HIGH_INCOME_MULTIPLIER = 1.3;   // Better access
    public static final double CORRECTIVE_LENS_MIDDLE_INCOME_MULTIPLIER = 1.0; // Baseline
    public static final double CORRECTIVE_LENS_LOW_INCOME_MULTIPLIER = 0.6;    // Reduced access

    // Race/ethnicity multipliers (derived from odds ratios in study)
    // Uninsured black/Hispanic = baseline (1.0)
    // Uninsured non-black/non-Hispanic = 2.29x
    // These are applied additively with income effects
    public static final double CORRECTIVE_LENS_WHITE_MULTIPLIER = 1.15;
    public static final double CORRECTIVE_LENS_BLACK_MULTIPLIER = 0.85;
    public static final double CORRECTIVE_LENS_HISPANIC_MULTIPLIER = 0.85;
    public static final double CORRECTIVE_LENS_API_MULTIPLIER = 1.10;
    public static final double CORRECTIVE_LENS_AIAN_MULTIPLIER = 0.90;
    public static final double CORRECTIVE_LENS_2PRACE_MULTIPLIER = 1.0;

    // Contact lens rates (among those with corrective lenses)
    // Higher income = significantly higher chance of contacts
    public static final double CONTACTS_HIGH_INCOME_RATE = 0.35;    // 35% of lens wearers
    public static final double CONTACTS_MIDDLE_INCOME_RATE = 0.18;  // 18% of lens wearers
    public static final double CONTACTS_LOW_INCOME_RATE = 0.05;     // 5% of lens wearers

    // Age effect on corrective lens adoption (higher income families show age effect)
    // Per the study: odds increase with age for families >=200% poverty level
    public static final double CORRECTIVE_LENS_SENIOR_MULTIPLIER = 1.15;
    public static final double CORRECTIVE_LENS_JUNIOR_MULTIPLIER = 1.10;
    public static final double CORRECTIVE_LENS_SOPHOMORE_MULTIPLIER = 1.05;
    public static final double CORRECTIVE_LENS_FRESHMAN_MULTIPLIER = 1.0;

    // Adult/Teacher Vision Constants
    // Older adults have higher rates of vision issues, especially hyperopia and astigmatism
    // Adults are much more likely to have corrective lenses if needed

    // Age multipliers for vision issues in adults
    // Under 40: baseline rates similar to general population
    // 40-59: increased rates (presbyopia onset around 40)
    // 60+: significantly increased rates
    public static final double ADULT_VISION_UNDER_40_MULTIPLIER = 1.0;
    public static final double ADULT_VISION_40_TO_59_MULTIPLIER = 1.4;
    public static final double ADULT_VISION_60_PLUS_MULTIPLIER = 1.8;

    // Hyperopia increases significantly with age (presbyopia)
    public static final double ADULT_HYPEROPIA_UNDER_40_RATE = 0.05;
    public static final double ADULT_HYPEROPIA_40_TO_59_RATE = 0.25;
    public static final double ADULT_HYPEROPIA_60_PLUS_RATE = 0.50;

    // Astigmatism also increases with age
    public static final double ADULT_ASTIGMATISM_UNDER_40_MULTIPLIER = 0.9;
    public static final double ADULT_ASTIGMATISM_40_TO_59_MULTIPLIER = 1.1;
    public static final double ADULT_ASTIGMATISM_60_PLUS_MULTIPLIER = 1.3;

    // Adults with vision issues almost always have corrective lenses
    // Working professionals especially need functional vision
    public static final double ADULT_CORRECTIVE_LENS_RATE = 0.95;

    // Contact lens rates for adults (decreases with age due to dry eye issues)
    public static final double ADULT_CONTACTS_UNDER_40_RATE = 0.30;
    public static final double ADULT_CONTACTS_40_TO_59_RATE = 0.15;
    public static final double ADULT_CONTACTS_60_PLUS_RATE = 0.05;

    private SimConstants() {
    }
}
