package utility;

import config.TownDemographics;
import entity.Items.ClothingItem;
import entity.Items.EquipmentSlot;
import entity.Items.Outfit;
import entity.Items.Piercing;
import entity.Items.Wardrobe;
import entity.Items.WearableItem;
import entity.Student;
import utility.music.FavoriteBandAssigner;
import utility.traits.StudentCharismaWeightFunction;
import utility.traits.TraitDataset;
import utility.traits.TraitDatasetLoader;
import utility.traits.TraitSelector;
import view.GameView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static constants.SimConstants.*;
import static utility.Randomizer.setRandom;

// TODO: improve performance. It is horrible
public class StudentPopGenerator {

    // Trait-selection knobs for the unique-traits pipeline.  The paths
    // and count window are local because they are student-specific;
    // other domains (cell phones, etc.) will declare their own.
    private static final String UNIQUE_TRAITS_DEFAULT_PATH =
            "/Resources/Flavor/unique_traits.json";
    private static final String UNIQUE_TRAITS_FEMALE_PATH =
            "/Resources/Flavor/unique_traits_female.json";
    private static final String UNIQUE_TRAITS_MALE_PATH =
            "/Resources/Flavor/unique_traits_male.json";
    private static final int UNIQUE_TRAIT_MIN_COUNT = 3;
    private static final int UNIQUE_TRAIT_MAX_COUNT = 5;

    // School colors for braces band color selection (set before generation)
    private static String[] schoolColors = null;

    static boolean hasSecondaryAppearanceClique(String mainClique,
                                                String secondaryClique) {
        return secondaryClique != null && !secondaryClique.equals(mainClique);
    }

    static String pickSecondaryAppearanceClique(String mainClique,
                                                String secondaryClique,
                                                boolean secondaryHasData) {
        if (hasSecondaryAppearanceClique(mainClique, secondaryClique)
                && secondaryHasData
                && GameRandom.nextDouble() < CLIQUE_SECONDARY_APPEARANCE_CHANCE) {
            return secondaryClique;
        }
        return mainClique;
    }

    /**
     * Sets the school colors for use in braces band color selection.
     * Should be called before generateStudents if school colors are available.
     *
     * @param colors the school colors array
     */
    public static void setSchoolColors(String[] colors) {
        schoolColors = colors;
    }

    /**
     * Gets the current school colors for use by other generators (e.g.,
     * SiblingGenerator).
     *
     * @return the school colors array, or null if not set
     */
    public static String[] getSchoolColors() {
        return schoolColors;
    }

    /**
     * Applies base attributes to a student including stats, physical traits,
     * braces, and vision.
     * This method is used by both StudentPopGenerator and SiblingGenerator to
     * ensure consistency.
     * 
     * Note: This method does NOT set level, experience, grade level, birthday,
     * gender, name, or race.
     * Those should be set by the caller before calling this method.
     *
     * @param student the student to apply attributes to
     */
    public static void applyBaseAttributes(Student student) {
        String race = student.studentStatistics.getRace();

        // Physical traits based on race
        student.studentStatistics.setEyeColor(TraitSelection.studentEyeColorSelection(race));
        String eyes = student.studentStatistics.getEyeColor();
        student.studentStatistics.setHairColor(TraitSelection.studentHairSelection(race, eyes));
        String hairColor = student.studentStatistics.getHairColor();
        student.studentStatistics.setInitHeight();

        // Base stats using Gaussian distributions
        student.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(STUDENT_POP_INTELLIGENCE_MEAN,
                STUDENT_POP_INTELLIGENCE_STANDARD_DEVIATION));
        student.studentStatistics.setCharisma(
                (int) GameRandom.nextGaussian(STUDENT_POP_CHARISMA_MEAN, STUDENT_POP_CHARISMA_STANDARD_DEVIATION));
        student.studentStatistics.setAgility(
                (int) GameRandom.nextGaussian(STUDENT_POP_AGILITY_MEAN, STUDENT_POP_AGILITY_STANDARD_DEVIATION));
        student.studentStatistics.setDetermination((int) GameRandom.nextGaussian(STUDENT_POP_DETERMINATION_MEAN,
                STUDENT_POP_DETERMINATION_STANDARD_DEVIATION));
        student.studentStatistics.setPerception(
                (int) GameRandom.nextGaussian(STUDENT_POP_PERCEPTION_MEAN, STUDENT_POP_PERCEPTION_STANDARD_DEVIATION));
        student.studentStatistics
                .setLuck((int) GameRandom.nextGaussian(STUDENT_POP_LUCK_MEAN, STUDENT_POP_LUCK_STANDARD_DEVIATION));

        // Derived stats
        student.studentStatistics.setInitStrength();
        student.studentStatistics.setInitCreativity();
        student.studentStatistics.setInitEmpathy();
        student.studentStatistics.setInitAdaptability();
        student.studentStatistics.setInitInitiative();
        student.studentStatistics.setInitResilience();
        student.studentStatistics.setInitCuriosity();
        student.studentStatistics.setInitResponsibility();
        student.studentStatistics.setInitOpenMind();

        // Initialize allostatic load tolerance (depends on resilience and determination)
        student.studentStatistics.initAllostaticLoad();

        // Hair and skin
        student.studentStatistics.setInitHairLength(setRandom(0, STUDENT_HAIR_LENGTH_SAMPLE_SIZE));
        student.studentStatistics.setHairType(TraitSelection.studentHairType(race, hairColor));
        student.studentStatistics.setSkinColor(TraitSelection.studentSkinColorSelection(race, eyes));

        // Apply braces attributes
        applyBracesAttributes(student);

        // Apply vision attributes
        applyVisionAttributes(student);

        // Apply charisma-driven unique traits
        applyUniqueTraits(student);

        // Piercings are applied separately after clique assignment
        // via applyPiercingAttributes so clique preferences can be used.
    }

    /**
     * Applies braces-related attributes to a student.
     * This includes determining if they have/had braces, timing, cosmetics, and
     * charisma effects.
     *
     * @param student the student to apply braces attributes to
     */
    public static void applyBracesAttributes(Student student) {
        LocalDate gameStartDate = LocalDate.of(STARTING_YEAR, STARTING_MONTH + 1, STARTING_DATE);
        String race = student.studentStatistics.getRace();
        String incomeLevel = student.studentStatistics.getIncomeLevel();
        String gradeLevel = student.studentStatistics.getGradeLevel();

        boolean hasBraces = TraitSelection.determineBraces(race, incomeLevel, gradeLevel);
        student.studentStatistics.setHasBraces(hasBraces);

        if (hasBraces) {
            // Determine if student has alternating band colors (relatively rare)
            boolean hasAlternating = TraitSelection.determineAlternatingBandColors();

            // Set braces band colors (with potential for alternating/school colors)
            String firstBandColor = TraitSelection.selectFirstBandColor(hasAlternating, schoolColors);
            student.studentStatistics.setBracesBandColor(firstBandColor);

            if (hasAlternating) {
                // Select second color, with higher chance of school colors
                String secondBandColor = TraitSelection.selectBracesBandColorWithSchoolOption(
                        true, schoolColors, firstBandColor);
                student.studentStatistics.setBracesSecondBandColor(secondBandColor);
            }

            // Set bracket type
            student.studentStatistics.setBracesBracketType(TraitSelection.selectBracesBracketType());

            // Determine if student has orthodontic elastics FIRST
            // (elastic type affects treatment duration)
            boolean hasElastics = TraitSelection.determineHasElastics();
            String elasticType = null;
            student.studentStatistics.setBracesHasElastics(hasElastics);
            if (hasElastics) {
                student.studentStatistics.setBracesElasticColor(TraitSelection.selectBracesElasticColor());
                elasticType = TraitSelection.selectBracesElasticType();
                student.studentStatistics.setBracesElasticType(elasticType);
            }

            // Generate braces timing
            // Certain modifiers like ligature ties extend treatment duration
            LocalDate[] bracesTiming = TraitSelection.generateBracesTiming(
                    gradeLevel, gameStartDate, hasElastics, elasticType);
            student.studentStatistics.setBracesStartDate(bracesTiming[0]);
            student.studentStatistics.setBracesEndDate(bracesTiming[1]);

            // Recalculate charisma-dependent stats with braces penalty
            student.studentStatistics.recalculateCharismaDependentStats();
        } else {
            // Check if student had braces in the past
            boolean hadPastBraces = TraitSelection.determinePastBraces(race, incomeLevel, gradeLevel, false);

            if (hadPastBraces) {
                student.studentStatistics.setHadBracesRemoved(true);

                // Generate past braces timing
                LocalDate birthday = student.studentStatistics.getBirthday();
                LocalDate[] pastTiming = TraitSelection.generatePastBracesTiming(birthday, gradeLevel, gameStartDate);
                student.studentStatistics.setBracesStartDate(pastTiming[0]);
                student.studentStatistics.setBracesEndDate(pastTiming[1]);

                // Apply charisma boost
                int currentCharisma = student.studentStatistics.getCharisma();
                student.studentStatistics.setCharisma(currentCharisma + BRACES_CHARISMA_BOOST);
                student.studentStatistics.setBracesCharismaBoost(BRACES_CHARISMA_BOOST);

                // Recalculate secondary stats
                student.studentStatistics.recalculateCharismaDependentStats();
            }
        }
    }

    /**
     * Applies vision-related attributes to a student.
     * This includes myopia, hyperopia, astigmatism, and corrective lenses.
     *
     * @param student the student to apply vision attributes to
     */
    public static void applyVisionAttributes(Student student) {
        String race = student.studentStatistics.getRace();
        String gender = student.studentStatistics.getGender();
        String incomeLevel = student.studentStatistics.getIncomeLevel();
        String gradeLevel = student.studentStatistics.getGradeLevel();

        // Determine vision issues based on NHANES data
        boolean[] visionIssues = TraitSelection.determineVisionIssues(race, gender);
        student.studentStatistics.setHasMyopia(visionIssues[0]);
        student.studentStatistics.setHasHyperopia(visionIssues[1]);
        student.studentStatistics.setHasAstigmatism(visionIssues[2]);

        // Determine corrective lenses if student has vision issues
        if (student.studentStatistics.hasVisionIssue()) {
            boolean[] correctiveLenses = TraitSelection.determineCorrectiveLenses(
                    race, gender, incomeLevel, gradeLevel);
            student.studentStatistics.setHasGlasses(correctiveLenses[0]);
            student.studentStatistics.setHasContacts(correctiveLenses[1]);
        }
    }

    /**
     * Selects 3-5 unique physical/behavioral traits for a student based on
     * their charisma. Charisma is N(50,15), so the z-score drives the
     * probability of drawing from positive, neutral, or negative trait pools.
     * Each trait is drawn from a distinct subcategory to avoid duplicate
     * descriptions for the same body part.
     *
     * @param student the student to assign unique traits to
     */
    public static void applyUniqueTraits(Student student) {
        TraitDataset dataset = TraitDatasetLoader.load(
                uniqueTraitsPathForGender(student.studentStatistics.getGender()));
        List<String> selectedTraits = TraitSelector.selectTraits(
                dataset,
                student,
                new StudentCharismaWeightFunction(),
                UNIQUE_TRAIT_MIN_COUNT,
                UNIQUE_TRAIT_MAX_COUNT);
        student.studentStatistics.setUniqueTraits(selectedTraits);
    }

    static String uniqueTraitsPathForGender(String gender) {
        if (gender == null) {
            return UNIQUE_TRAITS_DEFAULT_PATH;
        }
        return switch (gender.toLowerCase(Locale.ROOT)) {
            case "female" -> UNIQUE_TRAITS_FEMALE_PATH;
            case "male" -> UNIQUE_TRAITS_MALE_PATH;
            default -> UNIQUE_TRAITS_DEFAULT_PATH;
        };
    }

    /**
     * Applies all piercing attributes to a student.
     * Uses clique-specific piercing data when the student's clique has
     * defined preferences, otherwise falls back to generic TraitSelection logic.
     * Creates Piercing items and equips them on the student's head.
     *
     * @param student the student to apply piercing attributes to
     */
    public static void applyPiercingAttributes(Student student) {
        String gender = student.studentStatistics.getGender();
        String gradeLevel = student.studentStatistics.getGradeLevel();
        String clique = student.studentStatistics.getMainClique();
        String secondaryClique = student.studentStatistics.getSecondaryClique();

        boolean useCliqueData = clique != null
                && CliquePiercingLoader.hasPiercingData(clique, gender);
        boolean useSecondaryCliqueData = hasSecondaryAppearanceClique(
                clique, secondaryClique)
                && CliquePiercingLoader.hasPiercingData(secondaryClique, gender);

        // --- Ear piercings (use existing rate system) ---
        boolean hasEarPiercing = TraitSelection.determineEarPiercing(gender, gradeLevel);
        student.studentStatistics.setHasEarPiercing(hasEarPiercing);

        if (hasEarPiercing) {
            applyEarPiercings(student, gender, gradeLevel, clique, secondaryClique,
                    useCliqueData, useSecondaryCliqueData);
        }

        // --- Non-ear piercings (only when clique defines options for the slot) ---
        if (useCliqueData || useSecondaryCliqueData) {
            applyNonEarPiercings(student, gender, clique, secondaryClique,
                    useCliqueData, useSecondaryCliqueData);
        }
    }

    /**
     * Generates and equips ear piercings using the existing rate system.
     */
    private static void applyEarPiercings(Student student, String gender,
                                          String gradeLevel, String clique,
                                          String secondaryClique,
                                          boolean useCliqueData,
                                          boolean useSecondaryCliqueData) {
        boolean bothEars = TraitSelection.determineBothEarsPierced(gender);

        if (bothEars) {
            int leftCount = TraitSelection.determineEarPiercingCount(gradeLevel);
            int rightCount = TraitSelection.determineEarPiercingCount(gradeLevel);
            student.studentStatistics.setEarPiercingLeftCount(leftCount);
            student.studentStatistics.setEarPiercingRightCount(rightCount);
            equipEarPiercings(student, EquipmentSlot.LEFT_EAR, leftCount,
                    gender, clique, secondaryClique, useCliqueData,
                    useSecondaryCliqueData);
            List<WearableItem> leftPiercings = student.getStudentHead()
                    .getEquippedList(EquipmentSlot.LEFT_EAR);
            // A simple matched pair (one piercing per ear) almost always
            // wears identical earrings on each side; bump the match rate.
            boolean simplePair = (leftCount == 1 && rightCount == 1);
            equipMirroredEarPiercings(student, EquipmentSlot.RIGHT_EAR,
                    rightCount, leftPiercings, gender, clique, secondaryClique,
                    useCliqueData, useSecondaryCliqueData, simplePair);
        } else {
            boolean leftEar = TraitSelection.determineSingleEarIsLeft();
            int count = TraitSelection.determineEarPiercingCount(gradeLevel);
            student.studentStatistics.setEarPiercingLeftCount(leftEar ? count : 0);
            student.studentStatistics.setEarPiercingRightCount(leftEar ? 0 : count);
            EquipmentSlot slot = leftEar ? EquipmentSlot.LEFT_EAR : EquipmentSlot.RIGHT_EAR;
            equipEarPiercings(student, slot, count, gender, clique, secondaryClique,
                    useCliqueData, useSecondaryCliqueData);
        }
        ensureGaugePairs(student);

        // Backward compat: mirror first piercing's details into stats fields
        Piercing first = (Piercing) student.getStudentHead().getEquipped(
                student.studentStatistics.getEarPiercingLeftCount() > 0
                        ? EquipmentSlot.LEFT_EAR : EquipmentSlot.RIGHT_EAR);
        if (first != null) {
            student.studentStatistics.setEarPiercingType(first.getName());
            String displayMaterial = first.getColor() != null
                    ? first.getColor() + " " + first.getMaterial()
                    : first.getMaterial();
            student.studentStatistics.setEarPiercingMaterial(displayMaterial);
            student.studentStatistics.setEarPiercingSize(first.getSize());
        }

        student.studentStatistics.setEarPiercingCharismaBoost(PIERCING_EARRING_CHARISMA_BOOST);
        student.studentStatistics.recalculateCharismaDependentStats();
    }

    private static final EquipmentSlot[] NON_EAR_SLOTS = {
            EquipmentSlot.NOSE, EquipmentSlot.LIPS,
            EquipmentSlot.EYEBROW, EquipmentSlot.TONGUE,
            EquipmentSlot.NAVEL
    };

    /**
     * Generates non-ear piercings (nose, lips, eyebrow, tongue, navel)
     * based on clique-specific data. Each slot is rolled independently
     * against its own rate constant. Only slots where the clique defines
     * at least one option are considered.
     */
    private static void applyNonEarPiercings(Student student, String gender,
                                             String clique,
                                             String secondaryClique,
                                             boolean useCliqueData,
                                             boolean useSecondaryCliqueData) {
        boolean isFemale = gender.equalsIgnoreCase("Female");

        for (EquipmentSlot slot : NON_EAR_SLOTS) {
            String sourceClique = pickPiercingCliqueForSlot(clique, secondaryClique,
                    gender, slot, useCliqueData, useSecondaryCliqueData);
            if (sourceClique == null) {
                continue;
            }

            double rate = getNonEarPiercingRate(slot, isFemale);
            if (GameRandom.nextDouble() >= rate) {
                continue;
            }

            Piercing p = createCliquePiercing(sourceClique, gender, slot);
            if (p != null) {
                student.getStudentHead().equip(p);
            }
        }
    }

    private static double getNonEarPiercingRate(EquipmentSlot slot,
                                                boolean isFemale) {
        return switch (slot) {
            case NOSE -> isFemale
                    ? PIERCING_NOSE_FEMALE_RATE : PIERCING_NOSE_MALE_RATE;
            case LIPS -> isFemale
                    ? PIERCING_LIP_FEMALE_RATE : PIERCING_LIP_MALE_RATE;
            case EYEBROW -> isFemale
                    ? PIERCING_EYEBROW_FEMALE_RATE : PIERCING_EYEBROW_MALE_RATE;
            case TONGUE -> isFemale
                    ? PIERCING_TONGUE_FEMALE_RATE : PIERCING_TONGUE_MALE_RATE;
            case NAVEL -> isFemale
                    ? PIERCING_NAVEL_FEMALE_RATE : PIERCING_NAVEL_MALE_RATE;
            default -> 0.0;
        };
    }

    /**
     * Creates and equips ear piercings for a single ear slot.
     */
    private static void equipEarPiercings(Student student, EquipmentSlot slot,
                                          int count, String gender,
                                          String clique,
                                          String secondaryClique,
                                          boolean useCliqueData,
                                          boolean useSecondaryCliqueData) {
        for (int i = 0; i < count; i++) {
            Piercing p = createPiercingWithCliqueInfluence(clique, secondaryClique,
                    gender, slot, useCliqueData, useSecondaryCliqueData);
            if (p != null) {
                p.setStatModifier("charisma", PIERCING_EARRING_CHARISMA_BOOST);
                student.getStudentHead().equip(p);
            }
        }
    }

    /**
     * Equips ear piercings that tend to mirror the opposite ear. For each
     * position that has a corresponding template piercing, the same type/
     * material/color/jewel is reused with probability PIERCING_EAR_MATCH_RATE
     * (or the elevated PIERCING_EAR_PAIR_MATCH_*_RATE when this is a simple
     * one-piercing-per-ear matched pair); otherwise a fresh random piercing
     * is rolled. Positions beyond the template list are always rolled
     * independently.
     *
     * Gauges are treated as a special case: when the corresponding template
     * piercing is a gauge, this ear is paired as a matching gauge with
     * probability PIERCING_GAUGE_MATCH_RATE (rather than the normal match
     * rate), and the gauge size is always forced to match the template so
     * paired gauges share the same size on both ears.
     */
    private static void equipMirroredEarPiercings(Student student,
                                                  EquipmentSlot slot,
                                                  int count,
                                                  List<WearableItem> templates,
                                                  String gender,
                                                  String clique,
                                                  String secondaryClique,
                                                  boolean useCliqueData,
                                                  boolean useSecondaryCliqueData,
                                                  boolean simplePair) {
        double matchRate = simplePair
                ? earPairMatchRate(gender)
                : PIERCING_EAR_MATCH_RATE;

        for (int i = 0; i < count; i++) {
            Piercing source = (i < templates.size())
                    ? (Piercing) templates.get(i)
                    : null;
            boolean templateIsGauge = isGaugePiercing(source);

            Piercing p;
            if (source != null && templateIsGauge) {
                if (GameRandom.nextDouble() < PIERCING_GAUGE_MATCH_RATE) {
                    p = mirrorPiercing(source, slot);
                } else {
                    p = createPiercingWithCliqueInfluence(clique, secondaryClique,
                            gender, slot, useCliqueData, useSecondaryCliqueData);
                    if (isGaugePiercing(p)) {
                        p = new Piercing(source.getName(), p.getMaterial(),
                                p.getColor(), slot, source.getSize(),
                                p.getJewel());
                    }
                }
            } else if (source != null
                    && GameRandom.nextDouble() < matchRate) {
                p = mirrorPiercing(source, slot);
            } else {
                p = createPiercingWithCliqueInfluence(clique, secondaryClique,
                        gender, slot, useCliqueData, useSecondaryCliqueData);
                if (isGaugePiercing(p) && source != null
                        && GameRandom.nextDouble() < PIERCING_GAUGE_MATCH_RATE) {
                    p = mirrorPiercing(source, slot);
                }
            }

            if (p != null) {
                p.setStatModifier("charisma", PIERCING_EARRING_CHARISMA_BOOST);
                student.getStudentHead().equip(p);
            }
        }
    }

    /**
     * Returns the elevated match rate for a one-piercing-per-ear pair,
     * gendered to honor the convention that females wear matched pairs
     * slightly more consistently than males.
     */
    private static double earPairMatchRate(String gender) {
        return "Female".equalsIgnoreCase(gender)
                ? PIERCING_EAR_PAIR_MATCH_FEMALE_RATE
                : PIERCING_EAR_PAIR_MATCH_MALE_RATE;
    }

    /**
     * Builds a Piercing for {@code slot} that mirrors the source piercing's
     * type, material, color, size, and jewel. Centralizes the copy so the
     * jewel field is preserved consistently across all matching branches.
     */
    private static Piercing mirrorPiercing(Piercing source, EquipmentSlot slot) {
        return new Piercing(source.getName(), source.getMaterial(),
                source.getColor(), slot, source.getSize(), source.getJewel());
    }

    static void ensureGaugePairs(Student student) {
        List<WearableItem> left = student.getStudentHead()
                .getEquippedList(EquipmentSlot.LEFT_EAR);
        List<WearableItem> right = student.getStudentHead()
                .getEquippedList(EquipmentSlot.RIGHT_EAR);

        List<WearableItem> newLeft = new ArrayList<>(left);
        List<WearableItem> newRight = new ArrayList<>(right);
        int max = Math.max(newLeft.size(), newRight.size());

        for (int i = 0; i < max; i++) {
            Piercing leftPiercing = getPiercingAt(newLeft, i);
            Piercing rightPiercing = getPiercingAt(newRight, i);
            boolean leftGauge = isGaugePiercing(leftPiercing);
            boolean rightGauge = isGaugePiercing(rightPiercing);
            if (leftGauge == rightGauge || GameRandom.nextDouble() >= PIERCING_GAUGE_MATCH_RATE) {
                continue;
            }
            if (leftGauge) {
                putMirroredGauge(newRight, i, leftPiercing, EquipmentSlot.RIGHT_EAR);
            } else {
                putMirroredGauge(newLeft, i, rightPiercing, EquipmentSlot.LEFT_EAR);
            }
        }

        replaceEarPiercings(student, EquipmentSlot.LEFT_EAR, newLeft);
        replaceEarPiercings(student, EquipmentSlot.RIGHT_EAR, newRight);
        student.studentStatistics.setEarPiercingLeftCount(newLeft.size());
        student.studentStatistics.setEarPiercingRightCount(newRight.size());
    }

    private static Piercing getPiercingAt(List<WearableItem> items, int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index) instanceof Piercing piercing ? piercing : null;
    }

    private static void putMirroredGauge(List<WearableItem> items, int index,
                                         Piercing source, EquipmentSlot slot) {
        Piercing mirrored = mirrorPiercing(source, slot);
        mirrored.setStatModifier("charisma", PIERCING_EARRING_CHARISMA_BOOST);
        if (index < items.size() && isGaugePiercing(getPiercingAt(items, index))) {
            items.set(index, mirrored);
        } else if (items.size() < 3) {
            items.add(Math.min(index, items.size()), mirrored);
        } else if (index < items.size()) {
            // Ear slots are capped at three piercings; replacing is the only
            // way to avoid leaving an unpaired gauge when the slot is full.
            items.set(index, mirrored);
        } else {
            items.add(mirrored);
        }
    }

    private static void replaceEarPiercings(Student student, EquipmentSlot slot,
                                            List<WearableItem> items) {
        student.getStudentHead().unequipAll(slot);
        for (WearableItem item : items) {
            student.getStudentHead().equip(item);
        }
    }

    /**
     * Returns true if the given piercing is any flavor of gauge. Generic
     * gauges use the type name "gauges" (with a separate size descriptor),
     * while clique-defined gauges encode the size in the type name itself
     * (e.g. "00g gauge", "14mm gauge"); both are matched here.
     */
    private static boolean isGaugePiercing(Piercing piercing) {
        if (piercing == null || piercing.getName() == null) {
            return false;
        }
        return piercing.getName().toLowerCase().contains("gauge");
    }

    private static Piercing createPiercingWithCliqueInfluence(String clique,
                                                             String secondaryClique,
                                                             String gender,
                                                             EquipmentSlot slot,
                                                             boolean useCliqueData,
                                                             boolean useSecondaryCliqueData) {
        String sourceClique = pickPiercingCliqueForSlot(clique, secondaryClique,
                gender, slot, useCliqueData, useSecondaryCliqueData);
        return sourceClique == null
                ? createGenericPiercing(gender, slot)
                : createCliquePiercing(sourceClique, gender, slot);
    }

    private static String pickPiercingCliqueForSlot(String clique,
                                                   String secondaryClique,
                                                   String gender,
                                                   EquipmentSlot slot,
                                                   boolean useCliqueData,
                                                   boolean useSecondaryCliqueData) {
        boolean secondaryHasSlot = useSecondaryCliqueData
                && !CliquePiercingLoader.getPiercingTypes(
                        secondaryClique, gender, slot.getDisplayName()).isEmpty();
        String selected = pickSecondaryAppearanceClique(clique, secondaryClique,
                secondaryHasSlot);
        if (secondaryClique != null && secondaryClique.equals(selected)) {
            return secondaryClique;
        }
        if (!useCliqueData) {
            return null;
        }
        return CliquePiercingLoader.getPiercingTypes(
                clique, gender, slot.getDisplayName()).isEmpty()
                ? null
                : clique;
    }

    /**
     * Creates a piercing using clique-specific data from CliquePiercingLoader.
     */
    private static Piercing createCliquePiercing(String clique, String gender,
                                                 EquipmentSlot slot) {
        List<String> types = CliquePiercingLoader.getPiercingTypes(
                clique, gender, slot.getDisplayName());
        if (types.isEmpty()) {
            return createGenericPiercing(gender, slot);
        }

        String type = types.get((int) (GameRandom.nextDouble() * types.size()));

        List<String> materials = CliquePiercingLoader.getMaterials(clique, gender);
        String material = materials.isEmpty()
                ? TraitSelection.selectEarringMaterial(type)
                : materials.get((int) (GameRandom.nextDouble() * materials.size()));

        List<String> colors = CliquePiercingLoader.getColors(clique, gender);
        String color = colors.isEmpty()
                ? null
                : colors.get((int) (GameRandom.nextDouble() * colors.size()));

        List<String> jewels = CliquePiercingLoader.getJewels(clique, gender);
        String jewel;
        if (jewels.isEmpty()) {
            jewel = null;
        } else if ("hoops".equals(type)
                && GameRandom.nextDouble() >= PIERCING_HOOP_JEWEL_RATE) {
            // Hoops were mostly plain metal in the early 2000s; only a
            // small fraction had a set jewel, so most hoops skip the
            // clique's jewel palette entirely.
            jewel = null;
        } else {
            jewel = jewels.get((int) (GameRandom.nextDouble() * jewels.size()));
        }

        String size = TraitSelection.selectEarringSize(type);

        return new Piercing(type, material, color, slot, size, jewel);
    }

    /**
     * Creates a piercing using the existing generic TraitSelection logic.
     */
    private static Piercing createGenericPiercing(String gender,
                                                  EquipmentSlot slot) {
        String type = TraitSelection.selectEarringType(gender);
        String material = TraitSelection.selectEarringMaterial(type);
        String size = TraitSelection.selectEarringSize(type);
        return new Piercing(type, material, null, slot, size);
    }

    /**
     * Applies piercing attributes to all students in a HashMap.
     * Intended to be called after clique assignment so clique
     * preferences can influence piercing selection.
     *
     * @param studentHashMap the student population
     */
    public static void applyAllPiercingAttributes(
            HashMap<Integer, Student> studentHashMap) {
        for (Student student : studentHashMap.values()) {
            applyPiercingAttributes(student);
        }
    }

    /**
     * Applies clique-driven haircut attributes (style, dye, highlights)
     * to a single student based on their clique, gender, race, and hair length.
     */
    public static void applyHaircutAttributes(Student student) {
        String clique = student.studentStatistics.getMainClique();
        String secondaryClique = student.studentStatistics.getSecondaryClique();
        String gender = student.studentStatistics.getGender();
        String race = student.studentStatistics.getRace();
        String hairLength = student.studentStatistics.getHairLength();
        String hairColor = student.studentStatistics.getHairColor();

        if (clique == null || gender == null || race == null || hairLength == null) {
            return;
        }
        if (!CliqueHaircutLoader.hasHaircutData(clique, gender, race)) {
            return;
        }

        String styleClique = pickHairStyleClique(clique, secondaryClique, gender,
                race, hairLength);
        List<String> styles = CliqueHaircutLoader.getStyles(
                styleClique, gender, race, hairLength);
        if (!styles.isEmpty()) {
            student.studentStatistics.setHairStyle(
                    styles.get(setRandom(0, styles.size() - 1)));
        }

        String dyeClique = pickHairPaletteClique(clique, secondaryClique, gender,
                race, true);
        String highlightClique = pickHairPaletteClique(clique, secondaryClique,
                gender, race, false);
        List<String> dyes = CliqueHaircutLoader.getDyes(dyeClique, gender, race);
        List<String> highlights = CliqueHaircutLoader.getHighlights(
                highlightClique, gender, race);

        boolean highlightOnly = !highlights.isEmpty()
                && setRandom(0, 99) < CLIQUE_HAIR_HIGHLIGHT_ONLY_CHANCE;

        if (highlightOnly) {
            String chosenHighlight = pickAvoidingColor(highlights, hairColor);
            if (chosenHighlight != null) {
                student.studentStatistics.setHairHighlights(chosenHighlight);
            }
            return;
        }

        String chosenDye = null;
        if (!dyes.isEmpty() && setRandom(0, 99) < CLIQUE_HAIR_DYE_CHANCE) {
            chosenDye = pickAvoidingColor(dyes, hairColor);
            if (chosenDye != null) {
                student.studentStatistics.setHairDye(chosenDye);
            }
        }

        if (!highlights.isEmpty() && setRandom(0, 99) < CLIQUE_HAIR_HIGHLIGHT_CHANCE) {
            String avoidColor = chosenDye != null ? chosenDye : hairColor;
            String chosenHighlight = pickAvoidingColor(highlights, avoidColor);
            if (chosenHighlight != null) {
                student.studentStatistics.setHairHighlights(chosenHighlight);
            }
        }
    }

    private static String pickHairStyleClique(String clique,
                                              String secondaryClique,
                                              String gender,
                                              String race,
                                              String hairLength) {
        boolean secondaryHasStyles = hasSecondaryAppearanceClique(
                clique, secondaryClique)
                && !CliqueHaircutLoader.getStyles(
                        secondaryClique, gender, race, hairLength).isEmpty();
        return pickSecondaryAppearanceClique(clique, secondaryClique,
                secondaryHasStyles);
    }

    private static String pickHairPaletteClique(String clique,
                                                String secondaryClique,
                                                String gender,
                                                String race,
                                                boolean dyes) {
        boolean secondaryHasPalette = hasSecondaryAppearanceClique(
                clique, secondaryClique)
                && !(dyes
                        ? CliqueHaircutLoader.getDyes(secondaryClique, gender, race)
                        : CliqueHaircutLoader.getHighlights(
                                secondaryClique, gender, race)).isEmpty();
        return pickSecondaryAppearanceClique(clique, secondaryClique,
                secondaryHasPalette);
    }

    /**
     * Picks a random entry from the list, avoiding the given color when
     * possible. Returns null when every candidate collapses to the avoided
     * color after normalization.
     */
    private static String pickAvoidingColor(List<String> options, String avoid) {
        List<String> filtered = new ArrayList<>();
        String normalizedAvoid = normalizeColor(avoid);
        for (String opt : options) {
            if (opt != null && !normalizeColor(opt).equals(normalizedAvoid)) {
                filtered.add(opt);
            }
        }
        if (filtered.isEmpty()) {
            return null;
        }
        return filtered.get(setRandom(0, filtered.size() - 1));
    }

    private static String normalizeColor(String color) {
        if (color == null) {
            return "";
        }
        return color.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Applies clique-driven haircut attributes to all students.
     * Intended to be called after clique assignment.
     *
     * @param studentHashMap the student population
     */
    public static void applyAllHaircutAttributes(
            HashMap<Integer, Student> studentHashMap) {
        for (Student student : studentHashMap.values()) {
            applyHaircutAttributes(student);
        }
    }

    /**
     * Body slot hints used when materializing clothing items. The keys
     * match the inventory categories used in {@code clique_clothing.json}
     * (and the layer keys in {@code outfit_types.json}) so a category
     * lookup yields a plausible body region without forcing a coupling
     * to {@link EquipmentSlot}.
     */
    private static String bodySlotFor(String layer) {
        return switch (layer) {
            case "outerwear", "tops" -> "upper torso";
            case "bottoms" -> "lower torso";
            case "one_piece" -> "full body";
            case "shoes" -> "feet";
            case "accessories" -> "accessory";
            default -> layer;
        };
    }

    /**
     * Applies clique-driven clothing to a single student based on their
     * clique and gender. Pre-generates a full wardrobe of
     * {@code WARDROBE_INITIAL_OUTFITS} outfits (one per day of the first
     * school week), stores it on the student, and dresses them in the
     * first one for day 1. Each outfit picks a recipe from the clique's
     * {@code outfit_types} list and fills required (and probabilistically
     * optional) layers from the clique's inventory in
     * {@code clique_clothing.json}.
     *
     * <p>If the clique has no populated clothing data for the student's
     * gender, or if no outfit recipes are loaded, the student is left
     * with the default empty outfit and no wardrobe. There is no generic
     * fallback wardrobe in this first pass.</p>
     */
    public static void applyClothingAttributes(Student student) {
        if (student == null || student.studentStatistics == null) {
            return;
        }
        String clique = student.studentStatistics.getMainClique();
        String gender = student.studentStatistics.getGender();
        if (clique == null || gender == null) {
            return;
        }
        if (!CliqueClothingLoader.hasClothingData(clique, gender)) {
            return;
        }
        if (!OutfitTypeLoader.hasOutfitTypes()) {
            return;
        }

        Wardrobe wardrobe = new Wardrobe();
        for (int i = 0; i < WARDROBE_INITIAL_OUTFITS; i++) {
            Outfit outfit = generateOutfit(student);
            if (outfit != null && !outfit.isEmpty()) {
                wardrobe.addOutfit(outfit);
            }
        }
        if (wardrobe.isEmpty()) {
            return;
        }

        student.studentStatistics.setWardrobe(wardrobe);
        // Day 1 wears the first pre-generated outfit; DailyOutfitAssigner
        // consumes the rest of the wardrobe on subsequent mornings.
        student.studentStatistics.setCurrentOutfit(wardrobe.wearNext());
    }

    /**
     * Generates a single outfit for the student from their clique's
     * recipes and inventory. Returns {@code null} when no recipe's
     * required layers can be satisfied.
     */
    private static Outfit generateOutfit(Student student) {
        String clique = student.studentStatistics.getMainClique();
        String secondaryClique = student.studentStatistics.getSecondaryClique();
        String gender = student.studentStatistics.getGender();

        OutfitTypeLoader.OutfitTypeData recipe = pickOutfitRecipe(clique, gender);
        if (recipe == null) {
            return null;
        }

        Outfit outfit = new Outfit(recipe.getName());
        List<String> favoriteBands =
                student.studentStatistics.getFavoriteBands();
        // Pick a single coordinated color scheme for the whole outfit so
        // every garment draws from one cohesive palette rather than each
        // item getting an independent (potentially garish) random color.
        List<String> schemeColors = pickSchemeColors(clique);

        for (String layer : recipe.getRequiredLayers()) {
            addItemsForLayer(outfit, layer, recipe,
                    pickClothingLayerClique(clique, secondaryClique, gender, layer),
                    gender, favoriteBands, schemeColors);
        }

        for (String layer : recipe.getOptionalLayers()) {
            // Optional layers roll independently per layer. Accessories
            // are gated more permissively so a clique's accessory
            // palette has a real chance to show up.
            int chance = "accessories".equals(layer)
                    ? CLIQUE_CLOTHING_OPTIONAL_ACCESSORY_CHANCE
                    : CLIQUE_CLOTHING_OPTIONAL_LAYER_CHANCE;
            if (setRandom(0, 99) < chance) {
                addItemsForLayer(outfit, layer, recipe,
                        pickClothingLayerClique(clique, secondaryClique, gender, layer),
                        gender, favoriteBands, schemeColors);
            }
        }

        return outfit;
    }

    /**
     * Picks one coordinated color scheme for an outfit and returns its
     * color list. The scheme is chosen from the clique's allowed schemes
     * (falling back to the default scheme set) so e.g. Emo/Goth outfits
     * stay in the dark, mostly-black palette. Returns an empty list when
     * no schemes are loaded so callers leave colors unset.
     */
    private static List<String> pickSchemeColors(String clique) {
        if (!ColorSchemeLoader.hasSchemes()) {
            return List.of();
        }
        List<String> schemeNames = ColorSchemeLoader.getSchemesForClique(clique);
        if (schemeNames.isEmpty()) {
            return List.of();
        }
        String scheme = schemeNames.get(setRandom(0, schemeNames.size() - 1));
        return ColorSchemeLoader.getSchemeColors(scheme);
    }

    private static String pickClothingLayerClique(String clique,
                                                  String secondaryClique,
                                                  String gender,
                                                  String layer) {
        boolean secondaryHasLayer = hasSecondaryAppearanceClique(
                clique, secondaryClique)
                && !CliqueClothingLoader.getItems(
                        secondaryClique, gender, layer).isEmpty();
        return pickSecondaryAppearanceClique(clique, secondaryClique,
                secondaryHasLayer);
    }

    /**
     * Picks an outfit recipe whose required layers can be fully
     * satisfied by the clique/gender inventory. When the clique defines
     * an {@code outfit_types} list in {@code clique_clothing.json}, the
     * pick is weighted among those referenced recipes (so e.g. Emo
     * favors layered looks while Prep leans on shirt-and-pants); cliques
     * without a list fall back to a uniform pick over every loaded
     * recipe. Recipes that demand layers the clique doesn't stock (e.g.
     * a {@code dress} recipe for a clique with no {@code one_piece}
     * entries) are skipped so generation never produces an outfit with
     * missing required slots.
     *
     * @return a usable recipe, or {@code null} when none match
     */
    private static OutfitTypeLoader.OutfitTypeData pickOutfitRecipe(
            String clique, String gender) {
        List<CliqueClothingLoader.OutfitTypeRef> refs =
                CliqueClothingLoader.getOutfitTypeRefs(clique, gender);
        if (!refs.isEmpty()) {
            List<OutfitTypeLoader.OutfitTypeData> viable = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            int totalWeight = 0;
            for (CliqueClothingLoader.OutfitTypeRef ref : refs) {
                OutfitTypeLoader.OutfitTypeData recipe =
                        OutfitTypeLoader.getOutfitType(ref.getName());
                if (recipe != null
                        && canSatisfyRequiredLayers(recipe, clique, gender)) {
                    viable.add(recipe);
                    weights.add(ref.getWeight());
                    totalWeight += ref.getWeight();
                }
            }
            if (!viable.isEmpty()) {
                int roll = setRandom(0, totalWeight - 1);
                for (int i = 0; i < viable.size(); i++) {
                    roll -= weights.get(i);
                    if (roll < 0) {
                        return viable.get(i);
                    }
                }
                return viable.get(viable.size() - 1);
            }
        }

        List<OutfitTypeLoader.OutfitTypeData> viable = new ArrayList<>();
        for (OutfitTypeLoader.OutfitTypeData recipe
                : OutfitTypeLoader.getAllOutfitTypes()) {
            if (canSatisfyRequiredLayers(recipe, clique, gender)) {
                viable.add(recipe);
            }
        }
        if (viable.isEmpty()) {
            return null;
        }
        return viable.get(setRandom(0, viable.size() - 1));
    }

    /**
     * Returns true when the clique/gender inventory stocks at least one
     * item for every layer the recipe requires.
     */
    private static boolean canSatisfyRequiredLayers(
            OutfitTypeLoader.OutfitTypeData recipe,
            String clique, String gender) {
        for (String layer : recipe.getRequiredLayers()) {
            if (CliqueClothingLoader.getItems(clique, gender, layer).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Materializes up to {@code maxLayers} clothing items for the given
     * layer and appends them to {@code outfit}. Each item draws a name
     * from the clique's category list, its brand/material/pattern
     * qualifiers from that specific garment's own descriptor lists (so a
     * denim material never lands on a t-shirt), and its color from the
     * outfit's chosen color scheme.
     */
    private static void addItemsForLayer(Outfit outfit, String layer,
                                         OutfitTypeLoader.OutfitTypeData recipe,
                                         String clique, String gender,
                                         List<String> favoriteBands,
                                         List<String> schemeColors) {
        List<CliqueClothingLoader.ClothingOption> options =
                CliqueClothingLoader.getOptions(clique, gender, layer);
        if (options.isEmpty()) {
            return;
        }
        int max = Math.max(1, recipe.getMaxForLayer(layer));
        int count = "accessories".equals(layer)
                ? setRandom(1, max)
                : 1;

        String slot = bodySlotFor(layer);

        for (int i = 0; i < count; i++) {
            CliqueClothingLoader.ClothingOption option =
                    options.get(setRandom(0, options.size() - 1));
            String name = substituteBand(option.getName(), favoriteBands);

            String color = schemeColors.isEmpty()
                    ? null
                    : schemeColors.get(setRandom(0, schemeColors.size() - 1));

            List<String> patterns = option.getPatterns();
            String pattern = (patterns.isEmpty()
                    || setRandom(0, 99) >= CLIQUE_CLOTHING_PATTERN_CHANCE)
                    ? null
                    : patterns.get(setRandom(0, patterns.size() - 1));

            List<String> materials = option.getMaterials();
            String material = materials.isEmpty()
                    ? null
                    : materials.get(setRandom(0, materials.size() - 1));

            List<String> brands = option.getBrands();
            String brand = (brands.isEmpty()
                    || setRandom(0, 99) >= CLIQUE_CLOTHING_BRAND_CHANCE)
                    ? null
                    : brands.get(setRandom(0, brands.size() - 1));

            int warmth = option.getWarmth() != null
                    ? option.getWarmth()
                    : defaultWarmthFor(layer);

            outfit.addItem(new ClothingItem(name, layer, layer, slot,
                    material, color, pattern, brand, warmth));
        }
    }

    /**
     * Returns the default warmth contribution for a clothing category,
     * used when a garment defines no explicit {@code "warmth"} in
     * {@code clique_clothing.json}.
     */
    private static int defaultWarmthFor(String layer) {
        return switch (layer) {
            case "outerwear" -> CLOTHING_WARMTH_OUTERWEAR;
            case "tops" -> CLOTHING_WARMTH_TOPS;
            case "bottoms" -> CLOTHING_WARMTH_BOTTOMS;
            case "one_piece" -> CLOTHING_WARMTH_ONE_PIECE;
            case "shoes" -> CLOTHING_WARMTH_SHOES;
            default -> CLOTHING_WARMTH_ACCESSORIES;
        };
    }

    /**
     * Replaces the {@code {band}} placeholder in a clothing name with one of
     * the student's favorite bands (e.g. {@code "{band} hoodie"} ->
     * {@code "Green Day hoodie"}). Names without the token are returned
     * unchanged; if the student has no favorite bands a generic
     * {@code "local band"} stand-in is used so merch never reads literally
     * as {@code "{band} hoodie"}.
     */
    private static String substituteBand(String name,
                                         List<String> favoriteBands) {
        if (name == null || !name.contains("{band}")) {
            return name;
        }
        String band = FavoriteBandAssigner.FALLBACK_BAND;
        if (favoriteBands != null && !favoriteBands.isEmpty()) {
            band = favoriteBands.get(setRandom(0, favoriteBands.size() - 1));
        }
        return name.replace("{band}", band);
    }

    /**
     * Applies clothing to all students. Intended to be called after
     * clique assignment so each student can draw from their clique's
     * inventory.
     *
     * @param studentHashMap the student population
     */
    public static void applyAllClothingAttributes(
            HashMap<Integer, Student> studentHashMap) {
        for (Student student : studentHashMap.values()) {
            applyClothingAttributes(student);
        }
    }

    /**
     * Assigns favorite bands to a single student based on their clique's
     * music taste. Must run after clique assignment and before clothing
     * generation, since band merch is built from these bands.
     *
     * @param student the student to assign bands to
     */
    public static void applyFavoriteBands(Student student) {
        FavoriteBandAssigner.assign(student);
    }

    /**
     * Assigns favorite bands to every student. Intended to be called after
     * clique assignment and before {@link #applyAllClothingAttributes}.
     *
     * @param studentHashMap the student population
     */
    public static void applyAllFavoriteBands(
            HashMap<Integer, Student> studentHashMap) {
        for (Student student : studentHashMap.values()) {
            applyFavoriteBands(student);
        }
    }

    /**
     * Assigns favorite bands to every student relative to a reference date,
     * which lets tastemaker students pull picks from the sim's near future.
     *
     * @param studentHashMap the student population
     * @param referenceDate  the current sim date ("now")
     */
    public static void applyAllFavoriteBands(
            HashMap<Integer, Student> studentHashMap,
            java.time.LocalDate referenceDate) {
        for (Student student : studentHashMap.values()) {
            FavoriteBandAssigner.assign(student, referenceDate);
        }
    }

    /**
     * Generates students and returns them as a list.
     * This is the preferred method for the new Town-based architecture.
     *
     * @param count the number of students to generate
     * @param view  the game view for output
     * @return list of generated students
     */
    public static List<Student> generateStudentList(int count, GameView view) {
        HashMap<Integer, Student> tempMap = new HashMap<>();
        generateStudents(count, tempMap, view);
        return new ArrayList<>(tempMap.values());
    }

    /**
     * Generates students using demographics configuration.
     * Uses the gender and income distributions from the demographics.
     *
     * @param demographics the demographics configuration
     * @param view         the game view for output
     * @return list of generated students
     */
    public static List<Student> generateStudentsFromDemographics(TownDemographics demographics, GameView view) {
        int count = demographics.getTotalStudentsToGenerate();
        HashMap<Integer, Student> tempMap = new HashMap<>();
        generateStudentsWithDemographics(count, tempMap, view, demographics);
        return new ArrayList<>(tempMap.values());
    }

    /**
     * Generates students and populates a HashMap.
     * This method maintains backward compatibility with existing code.
     *
     * @param studentCap     the number of students to generate
     * @param studentHashMap the HashMap to populate
     * @param view           the game view for output
     */
    public static void generateStudents(int studentCap, HashMap<Integer, Student> studentHashMap, GameView view) {

        String f_name;
        String[] l_name;

        for (int i = 0; i < studentCap; i++) {
            studentHashMap.put(i, new Student());
        }

        GameLogger.logGeneration("Randomizing " + studentCap + " students...");
        loadCSVData();

        for (int k = 0; k < studentCap; k++) {
            Student student = studentHashMap.get(k);

            // Set identity attributes (grade, birthday, gender)
            student.studentStatistics.setExperience(0);
            student.studentStatistics.setGradeLevel(setRandom(0, 3));
            student.studentStatistics
                    .setBirthday(BirthdayGenerator.generateDateFromClass(student.studentStatistics.getGradeLevel()));
            student.studentStatistics.setGender(GenderLoader.genderSelection());

            // Set name attributes
            f_name = NameLoader.nameGenerator(String.valueOf(student.studentStatistics.getBirthday().getYear()),
                    student.studentStatistics.getGender());
            l_name = NameLoader.selectWeightedRandom();
            String lastName = l_name[0];
            String race = l_name[1];
            student.studentName.setFirstName(f_name);
            lastName = StudentName.capitalizeName(lastName);
            student.studentName.setLastName(lastName);
            if (setRandom(0, SUFFIX_GENERATION_SAMPLE_SIZE) < SUFFIX_GENERATION_RATE) {
                student.studentName.setSuffix(NameLoader.suffixNameGenerator(student.studentStatistics.getGender()));
            }
            if (setRandom(0, STUDENT_HYPHEN_GENERATION_SAMPLE_SIZE) < STUDENT_HYPHEN_GENERATION_RATE) {
                String hyphenName = NameLoader.selectWeightedRandom()[0];
                hyphenName = StudentName.capitalizeName(hyphenName);
                student.studentName.setLastName(lastName + "-" + hyphenName);
            }
            String suffix = student.studentName.getSuffix();

            // Set race and income (required before applyBaseAttributes)
            student.studentStatistics.setRace(race);
            student.studentStatistics.setInitIncomeLevel(setRandom(0, STUDENT_INCOME_LEVEL_SAMPLE_SIZE));

            // Apply all base attributes (stats, physical traits, braces, vision)
            applyBaseAttributes(student);

            if (suffix != null) {
                GameLogger.logGeneration(
                        "   Generated student " + f_name + " " + student.studentName.getLastName() + " " + suffix);
            } else {
                GameLogger.logGeneration("   Generated student " + f_name + " " + student.studentName.getLastName());
            }
        }
    }

    /**
     * Generates students using demographics configuration for distributions.
     * Uses custom gender and income distributions from the demographics object.
     *
     * @param studentCap     the number of students to generate
     * @param studentHashMap the HashMap to populate
     * @param view           the game view for output
     * @param demographics   the demographics configuration with distributions
     */
    public static void generateStudentsWithDemographics(int studentCap, HashMap<Integer, Student> studentHashMap,
            GameView view, TownDemographics demographics) {

        String f_name;
        String[] l_name;

        // Get distribution values from demographics
        java.util.Map<String, Double> genderDist = demographics.getGenderDistribution();
        java.util.Map<String, Double> incomeDist = demographics.getIncomeDistribution();
        double malePercent = genderDist.getOrDefault("Male", 0.51);
        double lowIncomePercent = incomeDist.getOrDefault("Low", 0.25);
        double middleIncomePercent = incomeDist.getOrDefault("Middle", 0.60);

        for (int i = 0; i < studentCap; i++) {
            studentHashMap.put(i, new Student());
        }

        GameLogger.logGeneration("Randomizing " + studentCap + " students...");
        loadCSVData();

        for (int k = 0; k < studentCap; k++) {
            Student student = studentHashMap.get(k);

            // Set identity attributes (grade, birthday, gender)
            student.studentStatistics.setExperience(0);
            student.studentStatistics.setGradeLevel(setRandom(0, 3));
            student.studentStatistics
                    .setBirthday(BirthdayGenerator.generateDateFromClass(student.studentStatistics.getGradeLevel()));

            // Use demographics gender distribution
            student.studentStatistics.setGender(GenderLoader.genderSelection(malePercent));

            // Set name attributes
            f_name = NameLoader.nameGenerator(String.valueOf(student.studentStatistics.getBirthday().getYear()),
                    student.studentStatistics.getGender());
            l_name = NameLoader.selectWeightedRandom();
            String lastName = l_name[0];
            String race = l_name[1];
            student.studentName.setFirstName(f_name);
            lastName = StudentName.capitalizeName(lastName);
            student.studentName.setLastName(lastName);
            if (setRandom(0, SUFFIX_GENERATION_SAMPLE_SIZE) < SUFFIX_GENERATION_RATE) {
                student.studentName.setSuffix(NameLoader.suffixNameGenerator(student.studentStatistics.getGender()));
            }
            if (setRandom(0, STUDENT_HYPHEN_GENERATION_SAMPLE_SIZE) < STUDENT_HYPHEN_GENERATION_RATE) {
                String hyphenName = NameLoader.selectWeightedRandom()[0];
                hyphenName = StudentName.capitalizeName(hyphenName);
                student.studentName.setLastName(lastName + "-" + hyphenName);
            }
            String suffix = student.studentName.getSuffix();

            // Set race and income (required before applyBaseAttributes)
            student.studentStatistics.setRace(race);
            student.studentStatistics.setIncomeFromDistribution(lowIncomePercent, middleIncomePercent);

            // Apply all base attributes (stats, physical traits, braces, vision)
            applyBaseAttributes(student);

            if (suffix != null) {
                GameLogger.logGeneration(
                        "   Generated student " + f_name + " " + student.studentName.getLastName() + " " + suffix);
            } else {
                GameLogger.logGeneration("   Generated student " + f_name + " " + student.studentName.getLastName());
            }
        }
    }

    private static void loadCSVData() {
        // Read large file into memory first
        NameLoader.readCSVFirst("1986");
        NameLoader.readCSVFirst("1987");
        NameLoader.readCSVFirst("1988");
        NameLoader.readCSVFirst("1989");
        NameLoader.readCSVFirst("1990");
        NameLoader.readCSVLastStudent();
    }
}
