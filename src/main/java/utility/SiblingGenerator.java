package utility;

import entity.Student;
import entity.StudentPool;
import view.GameView;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static constants.SimConstants.*;
import static utility.Randomizer.setRandom;

public class SiblingGenerator {
    private static final int MIN_BIOLOGICAL_SIBLING_GAP_DAYS = 270;
    private static final int MAX_SIBLING_BIRTHDAY_ATTEMPTS = 250;

    // School colors for braces band color selection
    private static String[] schoolColors = null;

    /**
     * Sets the school colors for use in braces band color selection.
     *
     * @param colors the school colors array
     */
    public static void setSchoolColors(String[] colors) {
        schoolColors = colors;
    }

    /**
     * Generates siblings for students in a StudentPool.
     * This is the preferred method for the new Town-based architecture.
     *
     * @param pool the student pool to generate siblings for
     * @param view the game view for output
     */
    public static void siblingGeneratorForPool(StudentPool pool, GameView view) {
        HashMap<Integer, Student> studentMap = pool.getAllStudents();
        int originalCount = studentMap.size();

        // Generate siblings using the existing method
        siblingGenerator(studentMap, originalCount, view);

        // The siblingGenerator method adds new students to the map
        // Clear and re-add all students to the pool to include siblings
        pool.clear();
        pool.addStudentsFromMap(studentMap);

        GameLogger.logSocialLinks("Sibling generation complete. Pool now has " + pool.getTotalCount() +
                " students (added " + (pool.getTotalCount() - originalCount) + " siblings)");
    }

    private static final int SAMPLE_SIZE = 73227;
    private static final int NO_SIBLING_RATE = 15524;
    private static final int ONE_SIBLING_RATE = 28368;
    private static final int TWO_SIBLING_RATE = 18145;
    private static final int THREE_SIBLING_RATE = 7493;
    private static final int MULTI_BIRTH_SAMPLE_SIZE = 1000;
    private static final int TWIN_RATE = 31;
    private static final int TRIPLET_RATE = 5;
    private static final int SIBLING_SAMPLE_SIZE = 57703;
    private static final int STEP_SIBLING_RATE = 1263;
    private static final int ADOPTED_SIBLING_RATE = 1128;
    private static final int HALF_SIBLING_RATE = 8567;

    /**
     * Generates a fully simulated sibling who is not of high school age.
     * This sibling will have inHighSchool=false and won't be assigned a schedule.
     * Like full siblings, they have a high probability (85%) of sharing physical
     * traits.
     *
     * @param student the original student to base sibling traits on
     * @param view    the game view for output
     * @return a fully simulated Student object with inHighSchool=false
     */
    private static Student generateNotInSchoolSibling(Student student, GameView view) {
        Student sibling = new Student();
        String f_name;

        // 85% probability of inheriting each physical trait from sibling
        final int TRAIT_INHERITANCE_PROBABILITY = 85;

        // Determine if older or younger sibling
        boolean older = setRandom(0, 1) == 0;
        int year;
        if (older) {
            // Older sibling: 1982-1985 (graduated or in college)
            year = setRandom(1982, 1985);
        } else {
            // Younger sibling: 1992-2000 (not yet in high school or elementary)
            year = setRandom(1992, 2000);
        }

        // Load name data for the year
        NameLoader.readCSVFirst(String.valueOf(year));

        // Set identity attributes
        sibling.studentStatistics.setExperience(0);
        // Grade level is N/A for non-high-school students, but we set it for
        // consistency
        sibling.studentStatistics.setGradeLevel(setRandom(0, 3));

        // Generate birthday based on the year
        int month = setRandom(1, 12);
        int day = setRandom(1, java.time.Month.of(month).length(false));
        sibling.studentStatistics.setBirthday(java.time.LocalDate.of(year, month, day));
        sibling.studentStatistics.setGender(GenderLoader.genderSelection());

        // Generate unique first name
        f_name = generateUniqueSiblingFirstName(student, sibling);
        sibling.studentName.setFirstName(f_name);
        sibling.studentName.setLastName(student.studentName.getLastName());

        // Share race and income level with original student (full sibling assumption)
        sibling.studentStatistics.setRace(student.studentStatistics.getRace());
        sibling.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());

        // Apply all base attributes (stats, physical traits, braces, vision)
        StudentPopGenerator.applyBaseAttributes(sibling);

        // Full siblings have a high probability of sharing physical traits, but not
        // guaranteed
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setEyeColor(student.studentStatistics.getEyeColor());
        }
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setHairColor(student.studentStatistics.getHairColor());
        }
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setHairType(student.studentStatistics.getHairType());
        }
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setSkinColor(student.studentStatistics.getSkinColor());
        }

        // Mark as NOT in high school (won't be assigned a schedule)
        sibling.setInHighSchool(false);

        // Link sibling relationships
        sibling.studentStatistics.addSiblingsInSchool(student);

        GameLogger.logSocialLinks(
                "Generated sibling (not in school) " + f_name + " " + sibling.studentName.getLastName());

        return sibling;
    }

    // Helper for player family generation: create sibling infos without touching
    // global maps
    public static java.util.List<entity.SiblingInfo> generateSiblingInfosForPlayer(entity.Student player, int count,
            view.GameView view) {
        java.util.List<entity.SiblingInfo> infos = new java.util.ArrayList<>();
        java.util.List<Boolean> spacingRestricted = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Decide if sibling is in school with the player
            boolean inSchool = setRandom(0, 12) <= 3;
            boolean sharedMotherPossible = false;
            if (inSchool) {
                int choice = setRandom(0, SIBLING_SAMPLE_SIZE);
                entity.Student sib;
                if (choice < STEP_SIBLING_RATE) {
                    sib = generateStepSibling(player, view);
                } else if (choice < STEP_SIBLING_RATE + ADOPTED_SIBLING_RATE) {
                    sib = generateAdoptedSibling(player, view);
                } else if (choice < STEP_SIBLING_RATE + ADOPTED_SIBLING_RATE + HALF_SIBLING_RATE) {
                    sib = generateHalfSibling(player, view);
                } else {
                    sharedMotherPossible = true;
                    int attempts = 0;
                    do {
                        sib = generateSibling(player, view);
                        attempts++;
                    } while (hasBirthdaySpacingConflict(sib.studentStatistics.getBirthday(),
                            player.studentStatistics.getBirthday(), infos, spacingRestricted)
                            && attempts < MAX_SIBLING_BIRTHDAY_ATTEMPTS);
                }
                String uniqueFirstName = generateUniquePlayerSiblingFirstName(player, infos,
                        sib.studentStatistics.getBirthday(), sib.studentStatistics.getGender());
                sib.studentName.setFirstName(uniqueFirstName);
                entity.SiblingInfo info = new entity.SiblingInfo(uniqueFirstName,
                        sib.studentStatistics.getBirthday(), true, sib.studentStatistics.getGender());
                infos.add(info);
                spacingRestricted.add(sharedMotherPossible);
            } else {
                // Not in school: pick older or younger and synthesize birthday
                boolean older = setRandom(0, 1) == 0;
                java.time.LocalDate birthday;
                if (older) {
                    // Older sibling: 1982–1985
                    birthday = generateNonSchoolSiblingBirthday(true, player.studentStatistics.getBirthday(), infos,
                            spacingRestricted);
                } else {
                    // Younger sibling: after 1990
                    birthday = generateNonSchoolSiblingBirthday(false, player.studentStatistics.getBirthday(), infos,
                            spacingRestricted);
                }
                String gen = GenderLoader.genderSelection();
                String first = generateUniquePlayerSiblingFirstName(player, infos, birthday, gen);
                entity.SiblingInfo info = new entity.SiblingInfo(first, birthday, false, gen);
                infos.add(info);
                spacingRestricted.add(true);
            }
        }

        return infos;
    }

    private static LocalDate generateNonSchoolSiblingBirthday(boolean older, LocalDate playerBirthday,
            java.util.List<entity.SiblingInfo> infos, java.util.List<Boolean> spacingRestricted) {
        LocalDate candidate = older ? LocalDate.of(1982, 1, 1) : LocalDate.of(1992, 1, 1);
        int attempts = 0;

        do {
            int year = older ? setRandom(1982, 1985) : setRandom(1992, 2000);
            int month = setRandom(1, 12);
            int day = setRandom(1, java.time.Month.of(month).length(false));
            candidate = java.time.LocalDate.of(year, month, day);
            attempts++;
        } while (hasBirthdaySpacingConflict(candidate, playerBirthday, infos, spacingRestricted)
                && attempts < MAX_SIBLING_BIRTHDAY_ATTEMPTS);

        if (!hasBirthdaySpacingConflict(candidate, playerBirthday, infos, spacingRestricted)) {
            return candidate;
        }

        LocalDate start = older ? LocalDate.of(1982, 1, 1) : LocalDate.of(1992, 1, 1);
        LocalDate end = older ? LocalDate.of(1985, 12, 31) : LocalDate.of(2000, 12, 31);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (!hasBirthdaySpacingConflict(date, playerBirthday, infos, spacingRestricted)) {
                return date;
            }
        }

        return candidate;
    }

    private static boolean hasBirthdaySpacingConflict(LocalDate candidate, LocalDate playerBirthday,
            java.util.List<entity.SiblingInfo> infos, java.util.List<Boolean> spacingRestricted) {
        if (candidate == null) {
            return true;
        }
        if (playerBirthday != null && daysBetween(candidate, playerBirthday) < MIN_BIOLOGICAL_SIBLING_GAP_DAYS) {
            return true;
        }
        for (int i = 0; i < infos.size(); i++) {
            if (!spacingRestricted.get(i)) {
                continue;
            }
            if (daysBetween(candidate, infos.get(i).getBirthday()) < MIN_BIOLOGICAL_SIBLING_GAP_DAYS) {
                return true;
            }
        }
        return false;
    }

    private static long daysBetween(LocalDate first, LocalDate second) {
        if (first == null || second == null) {
            return -1;
        }
        return Math.abs(ChronoUnit.DAYS.between(first, second));
    }

    private static String generateUniquePlayerSiblingFirstName(Student player,
            java.util.List<entity.SiblingInfo> existingSiblings, LocalDate birthday, String gender) {
        Set<String> forbiddenNames = new HashSet<>();
        addForbiddenName(forbiddenNames, player.studentName.getFirstName());
        for (entity.SiblingInfo sibling : existingSiblings) {
            addForbiddenName(forbiddenNames, sibling.getFirstName());
        }
        return generateUniqueFirstName(String.valueOf(birthday.getYear()), gender, forbiddenNames);
    }

    private static String generateUniqueSiblingFirstName(Student sourceStudent, Student sibling) {
        Set<String> forbiddenNames = new HashSet<>();
        addForbiddenName(forbiddenNames, sourceStudent.studentName.getFirstName());
        for (Student existingSibling : sourceStudent.studentStatistics.getSiblingsInSchool()) {
            addForbiddenName(forbiddenNames, existingSibling.studentName.getFirstName());
        }
        for (Student existingSibling : sourceStudent.studentStatistics.getSiblingsNotInSchool()) {
            addForbiddenName(forbiddenNames, existingSibling.studentName.getFirstName());
        }
        return generateUniqueFirstName(String.valueOf(sibling.studentStatistics.getBirthday().getYear()),
                sibling.studentStatistics.getGender(), forbiddenNames);
    }

    private static String generateUniqueFirstName(String year, String gender, Set<String> forbiddenNames) {
        String firstName = NameLoader.nameGenerator(year, gender);
        while (forbiddenNames.contains(normalizeName(firstName))) {
            firstName = NameLoader.nameGenerator(year, gender);
        }
        return firstName;
    }

    private static void addForbiddenName(Set<String> forbiddenNames, String name) {
        String normalizedName = normalizeName(name);
        if (normalizedName != null) {
            forbiddenNames.add(normalizedName);
        }
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            return null;
        }
        return trimmedName.toLowerCase(Locale.ROOT);
    }

    public static void siblingGenerator(HashMap<Integer, Student> studentHashMap, int studentCap, GameView view) {
        HashMap<Integer, Student> addedStudents = new HashMap<>();

        for (Map.Entry<Integer, Student> student : studentHashMap.entrySet()) {
            int siblings = siblingProbabilityLoader();
            List<Student> generatedSiblings = new ArrayList<>();
            Student sibling;
            boolean finishedGeneration = false;
            boolean hasTwins = setRandom(0, MULTI_BIRTH_SAMPLE_SIZE) < TWIN_RATE;
            boolean hasTriplets = setRandom(0, MULTI_BIRTH_SAMPLE_SIZE) < TRIPLET_RATE;
            boolean hasStepSibling = setRandom(0, SIBLING_SAMPLE_SIZE) < STEP_SIBLING_RATE;
            boolean hasAdoptedSibling = setRandom(0, SIBLING_SAMPLE_SIZE) < ADOPTED_SIBLING_RATE;
            boolean hasHalfSibling = setRandom(0, SIBLING_SAMPLE_SIZE) < HALF_SIBLING_RATE;

            if (siblings == 1) {
                if (hasTwins) {
                    studentCap++;
                    sibling = generateTwinOrTriplet(student.getValue(), view);
                    addedStudents.put(studentCap, sibling);
                } else if (hasStepSibling) {
                    // Higher chance step-sibling is around same age and therefore in school
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateStepSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        generatedSiblings.add(sibling);
                    } else {
                        // Otherwise, sibling is not in high school (younger or older)
                        studentCap++;
                        sibling = generateNotInSchoolSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                        generatedSiblings.add(sibling);
                    }
                } else if (hasAdoptedSibling) {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateAdoptedSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        generatedSiblings.add(sibling);
                    } else {
                        studentCap++;
                        sibling = generateNotInSchoolSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                        generatedSiblings.add(sibling);
                    }
                } else if (hasHalfSibling) {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateHalfSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        generatedSiblings.add(sibling);
                    } else {
                        studentCap++;
                        sibling = generateNotInSchoolSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                        generatedSiblings.add(sibling);
                    }
                } else {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        generatedSiblings.add(sibling);
                    } else {
                        studentCap++;
                        sibling = generateNotInSchoolSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                        generatedSiblings.add(sibling);
                    }
                }
            } else if (siblings == 2) {
                if (hasTriplets) {
                    for (int x = 0; x < siblings; x++) {
                        studentCap++;
                        sibling = generateTwinOrTriplet(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        generatedSiblings.add(sibling);
                    }
                    finishedGeneration = true;
                } else {
                    if (hasTwins) {
                        // can either be a twin or have twin siblings
                        if (setRandom(0, 1) == 1) {
                            studentCap++;
                            sibling = generateTwinOrTriplet(student.getValue(), view);
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            generatedSiblings.add(sibling);
                            siblings--;
                        } else {
                            if (setRandom(0, 12) <= 3) {
                                // Generate twin sibling first and then copy sibling
                                studentCap++;
                                Student twinSibling = generateSibling(student.getValue(), view);
                                addedStudents.put(studentCap, twinSibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(twinSibling);
                                generatedSiblings.add(twinSibling);
                                // add second twin
                                studentCap++;
                                sibling = generateTwinOrTriplet(twinSibling, view);
                                // TODO: think about this one for reverse add
                                addedStudents.put(studentCap, sibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                                generatedSiblings.add(sibling);
                            } else {
                                // add two twins not in school
                                for (int i = 0; i < siblings; i++) {
                                    studentCap++;
                                    sibling = generateNotInSchoolSibling(student.getValue(), view);
                                    addedStudents.put(studentCap, sibling);
                                    student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                                    generatedSiblings.add(sibling);
                                }
                            }
                            finishedGeneration = true;
                        }
                    }
                }
                // chance of other siblings being any other type
                if (!finishedGeneration) {
                    for (int i = 0; i < siblings; i++) {
                        if (setRandom(0, 12) <= 3) {
                            studentCap++;
                            if (hasStepSibling) {
                                sibling = generateStepSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                generatedSiblings.add(sibling);
                            } else if (hasAdoptedSibling) {
                                sibling = generateAdoptedSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                generatedSiblings.add(sibling);
                            } else if (hasHalfSibling) {
                                sibling = generateHalfSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                generatedSiblings.add(sibling);
                            } else {
                                sibling = generateSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                generatedSiblings.add(sibling);
                            }
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        } else {
                            studentCap++;
                            sibling = generateNotInSchoolSibling(student.getValue(), view);
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                            generatedSiblings.add(sibling);
                        }
                    }
                }
            } else if (siblings >= 3) {
                if (hasTriplets) {
                    if (setRandom(0, 1) == 1) {
                        for (int x = 0; x < siblings; x++) {
                            studentCap++;
                            sibling = generateTwinOrTriplet(student.getValue(), view);
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            generatedSiblings.add(sibling);
                            siblings--;
                        }
                    } else {
                        if (setRandom(0, 12) <= 3) {
                            // Generate triplet sibling first and then copy sibling
                            studentCap++;
                            Student tripletSibling = generateSibling(student.getValue(), view);
                            addedStudents.put(studentCap, tripletSibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(tripletSibling);
                            generatedSiblings.add(tripletSibling);
                            siblings--;
                            // add second twin
                            studentCap++;
                            sibling = generateTwinOrTriplet(tripletSibling, view);
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            generatedSiblings.add(sibling);
                            siblings--;
                        } else {
                            for (int i = 0; i < 3; i++) {
                                studentCap++;
                                sibling = generateNotInSchoolSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                                generatedSiblings.add(sibling);
                                siblings--;
                            }
                        }
                    }
                } else {
                    if (hasTwins) {
                        // can either be a twin or have twin siblings
                        if (setRandom(0, 1) == 1) {
                            studentCap++;
                            sibling = generateTwinOrTriplet(student.getValue(), view);
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            generatedSiblings.add(sibling);
                            siblings--;
                        } else {
                            if (setRandom(0, 12) <= 3) {
                                // Generate twin sibling first and then copy sibling
                                studentCap++;
                                Student twinSibling = generateSibling(student.getValue(), view);
                                addedStudents.put(studentCap, twinSibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(twinSibling);
                                generatedSiblings.add(twinSibling);
                                siblings--;
                                // add second twin
                                studentCap++;
                                sibling = generateTwinOrTriplet(twinSibling, view);
                                addedStudents.put(studentCap, sibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                                generatedSiblings.add(sibling);
                                siblings--;
                            } else {
                                for (int i = 0; i < 2; i++) {
                                    studentCap++;
                                    sibling = generateNotInSchoolSibling(student.getValue(), view);
                                    addedStudents.put(studentCap, sibling);
                                    student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                                    generatedSiblings.add(sibling);
                                    siblings--;
                                }
                            }
                        }
                    }
                }
                // chance of other siblings being any other type
                if (siblings > 0) {
                    for (int i = 0; i < siblings; i++) {
                        if (setRandom(0, 12) <= 3) {
                            studentCap++;
                            if (hasStepSibling) {
                                sibling = generateStepSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                generatedSiblings.add(sibling);
                            } else if (hasAdoptedSibling) {
                                sibling = generateAdoptedSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                generatedSiblings.add(sibling);
                            } else if (hasHalfSibling) {
                                sibling = generateHalfSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                generatedSiblings.add(sibling);
                            } else {
                                sibling = generateSibling(student.getValue(), view);
                                addedStudents.put(studentCap, sibling);
                                generatedSiblings.add(sibling);
                            }
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        } else {
                            studentCap++;
                            sibling = generateNotInSchoolSibling(student.getValue(), view);
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsNotInSchool(sibling);
                            generatedSiblings.add(sibling);
                        }
                    }
                }
            } else {
                GameLogger.logSocialLinks("No siblings to generate");
            }

            // Update sibling lists for all generated siblings
            for (Student generatedSibling : generatedSiblings) {
                for (Student otherSibling : generatedSiblings) {
                    if (!generatedSibling.equals(otherSibling)) {
                        // Link siblings in school to each other
                        if (otherSibling.isInHighSchool()) {
                            generatedSibling.studentStatistics.addSiblingsInSchool(otherSibling);
                        } else {
                            generatedSibling.studentStatistics.addSiblingsNotInSchool(otherSibling);
                        }
                    }
                }
                // Copy siblingsNotInSchool from the original student to each generated sibling
                for (Student siblingNotInSchool : student.getValue().studentStatistics.getSiblingsNotInSchool()) {
                    if (!generatedSibling.equals(siblingNotInSchool)) {
                        generatedSibling.studentStatistics.addSiblingsNotInSchool(siblingNotInSchool);
                    }
                }
            }
        }

        studentHashMap.putAll(addedStudents);
    }

    /**
     * Generates a step-sibling for the given student.
     * Step-siblings may have different last names (33% chance) and different race
     * (20% chance).
     */
    private static Student generateStepSibling(Student student, GameView view) {
        Student sibling = new Student();
        String f_name;
        String race;
        String[] l_name = new String[2];
        String lastName;

        // Set identity attributes
        sibling.studentStatistics.setExperience(0);
        sibling.studentStatistics.setGradeLevel(setRandom(0, 3));
        sibling.studentStatistics
                .setBirthday(BirthdayGenerator.generateDateFromClass(sibling.studentStatistics.getGradeLevel()));
        sibling.studentStatistics.setGender(GenderLoader.genderSelection());

        // Generate unique first name
        f_name = generateUniqueSiblingFirstName(student, sibling);

        // Chance of having different last name than sibling (33%)
        if (setRandom(0, 3) == 2) {
            l_name = NameLoader.selectWeightedRandom();
            lastName = l_name[0];
            lastName = StudentName.capitalizeName(lastName);
        } else {
            lastName = student.studentName.getLastName();
        }
        sibling.studentName.setFirstName(f_name);
        sibling.studentName.setLastName(lastName);

        // Chance of step-sibling having different race (20%)
        if (setRandom(0, 10) < 2) {
            if (l_name[1] != null) {
                race = l_name[1];
            } else {
                race = student.studentStatistics.getRace();
            }
        } else {
            race = student.studentStatistics.getRace();
        }
        sibling.studentStatistics.setRace(race);

        // Set income level (same as original student)
        sibling.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());

        // Apply all base attributes (stats, physical traits, braces, vision)
        StudentPopGenerator.applyBaseAttributes(sibling);

        // Link sibling relationships
        sibling.studentStatistics.addSiblingsInSchool(student);

        GameLogger.logSocialLinks("Generated step-sibling " + f_name + " " + sibling.studentName.getLastName());

        return sibling;
    }

    /**
     * Generates a half-sibling for the given student.
     * Half-siblings share last name and usually race (90% same).
     * If from same mother, there's an enforced age gap.
     */
    private static Student generateHalfSibling(Student student, GameView view) {
        Student sibling = new Student();
        String f_name;
        String race;
        String studentGrade = student.studentStatistics.getGradeLevel();
        String siblingGrade;

        // Set identity attributes
        sibling.studentStatistics.setExperience(0);

        // Half sibling can either come from mother or father. If father the age gap can
        // be closer
        if (setRandom(0, 10) <= 5) {
            sibling.studentStatistics.setGradeLevel(setRandom(0, 3));
        } else {
            // Ensure min age gap between siblings from same mother
            do {
                sibling.studentStatistics.setGradeLevel(setRandom(0, 3));
                siblingGrade = sibling.studentStatistics.getGradeLevel();
            } while (studentGrade.equals(siblingGrade));
        }
        sibling.studentStatistics
                .setBirthday(BirthdayGenerator.generateDateFromClass(sibling.studentStatistics.getGradeLevel()));
        sibling.studentStatistics.setGender(GenderLoader.genderSelection());

        // Generate unique first name
        f_name = generateUniqueSiblingFirstName(student, sibling);
        sibling.studentName.setFirstName(f_name);
        sibling.studentName.setLastName(student.studentName.getLastName());

        // Chance of half-sibling having different race (10%)
        if (setRandom(0, 10) < 1) {
            race = NameLoader.selectWeightedRandom()[1];
        } else {
            race = student.studentStatistics.getRace();
        }
        sibling.studentStatistics.setRace(race);

        // Set income level (same as original student)
        sibling.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());

        // Apply all base attributes (stats, physical traits, braces, vision)
        StudentPopGenerator.applyBaseAttributes(sibling);

        // Link sibling relationships
        sibling.studentStatistics.addSiblingsInSchool(student);

        GameLogger.logSocialLinks("Generated half-sibling " + f_name + " " + sibling.studentName.getLastName());

        return sibling;
    }

    /**
     * Generates an adopted sibling for the given student.
     * Adopted siblings share last name but always have a different race.
     */
    private static Student generateAdoptedSibling(Student student, GameView view) {
        Student sibling = new Student();
        String f_name;

        // Set identity attributes
        sibling.studentStatistics.setExperience(0);
        sibling.studentStatistics.setGradeLevel(setRandom(0, 3));
        sibling.studentStatistics
                .setBirthday(BirthdayGenerator.generateDateFromClass(sibling.studentStatistics.getGradeLevel()));
        sibling.studentStatistics.setGender(GenderLoader.genderSelection());

        // Generate unique first name
        f_name = generateUniqueSiblingFirstName(student, sibling);
        sibling.studentName.setFirstName(f_name);
        sibling.studentName.setLastName(student.studentName.getLastName());

        // Adopted siblings always have a different race
        sibling.studentStatistics.setRace(NameLoader.selectWeightedRandom()[1]);

        // Set income level (same as original student)
        sibling.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());

        // Apply all base attributes (stats, physical traits, braces, vision)
        StudentPopGenerator.applyBaseAttributes(sibling);

        // Link sibling relationships
        sibling.studentStatistics.addSiblingsInSchool(student);

        GameLogger.logSocialLinks("Generated adopted sibling " + f_name + " " + sibling.studentName.getLastName());

        return sibling;
    }

    /**
     * Generates a twin or triplet for the given student.
     * Twins/triplets share birthday, grade level, race, eye color, hair color,
     * height, hair type, skin color.
     */
    private static Student generateTwinOrTriplet(Student student, GameView view) {
        Student sibling = new Student();
        String f_name;

        // Set identity attributes - twins/triplets share grade level and birthday
        sibling.studentStatistics.setExperience(0);
        sibling.studentStatistics.setGradeLevel(student.studentStatistics.getGradeLevel());
        sibling.studentStatistics.setBirthday(student.studentStatistics.getBirthday());
        sibling.studentStatistics.setGender(GenderLoader.genderSelection());

        // Generate unique first name
        f_name = generateUniqueSiblingFirstName(student, sibling);
        sibling.studentName.setFirstName(f_name);
        sibling.studentName.setLastName(student.studentName.getLastName());

        // Twins share race and income level
        sibling.studentStatistics.setRace(student.studentStatistics.getRace());
        sibling.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());

        // Apply all base attributes (stats, physical traits, braces, vision)
        StudentPopGenerator.applyBaseAttributes(sibling);

        // Override physical traits to match twin (these should be inherited, not
        // random)
        sibling.studentStatistics.setEyeColor(student.studentStatistics.getEyeColor());
        sibling.studentStatistics.setHairColor(student.studentStatistics.getHairColor());
        sibling.studentStatistics.setHeight(student.studentStatistics.getHeight());
        sibling.studentStatistics.setHairType(student.studentStatistics.getHairType());
        sibling.studentStatistics.setSkinColor(student.studentStatistics.getSkinColor());

        // Link sibling relationships
        sibling.studentStatistics.addSiblingsInSchool(student);

        GameLogger.logSocialLinks("Generated twin or triplet " + f_name + " " + sibling.studentName.getLastName());

        return sibling;
    }

    /**
     * Generates a full sibling for the given student.
     * Full siblings share race and have a high probability (85%) of sharing
     * physical traits
     * (eye color, hair color, height, hair type, skin color) but it's not
     * guaranteed.
     * They have an enforced age gap (can't be same grade level).
     */
    private static Student generateSibling(Student student, GameView view) {
        Student sibling = new Student();
        String f_name;
        String studentGrade = student.studentStatistics.getGradeLevel();
        String siblingGrade;

        // 85% probability of inheriting each physical trait from sibling
        final int TRAIT_INHERITANCE_PROBABILITY = 85;

        // Set identity attributes with enforced age gap
        sibling.studentStatistics.setExperience(0);

        // Ensure min age gap between true siblings
        do {
            sibling.studentStatistics.setGradeLevel(setRandom(0, 3));
            siblingGrade = sibling.studentStatistics.getGradeLevel();
        } while (studentGrade.equals(siblingGrade));
        sibling.studentStatistics
                .setBirthday(BirthdayGenerator.generateDateFromClass(sibling.studentStatistics.getGradeLevel()));
        sibling.studentStatistics.setGender(GenderLoader.genderSelection());

        // Generate unique first name
        f_name = generateUniqueSiblingFirstName(student, sibling);
        sibling.studentName.setFirstName(f_name);
        sibling.studentName.setLastName(student.studentName.getLastName());

        // Full siblings share race and income level
        sibling.studentStatistics.setRace(student.studentStatistics.getRace());
        sibling.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());

        // Apply all base attributes (stats, physical traits, braces, vision)
        StudentPopGenerator.applyBaseAttributes(sibling);

        // Full siblings have a high probability of sharing physical traits, but not
        // guaranteed
        // Each trait is independently determined (genetics isn't all-or-nothing)
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setEyeColor(student.studentStatistics.getEyeColor());
        }
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setHairColor(student.studentStatistics.getHairColor());
        }
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setHeight(student.studentStatistics.getHeight());
        }
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setHairType(student.studentStatistics.getHairType());
        }
        if (setRandom(0, 100) < TRAIT_INHERITANCE_PROBABILITY) {
            sibling.studentStatistics.setSkinColor(student.studentStatistics.getSkinColor());
        }

        // Link sibling relationships
        sibling.studentStatistics.addSiblingsInSchool(student);

        GameLogger.logSocialLinks("Generated sibling " + f_name + " " + sibling.studentName.getLastName());

        return sibling;
    }

    private static int siblingProbabilityLoader() {
        int siblings;
        int choice = setRandom(0, SAMPLE_SIZE);

        if (choice <= NO_SIBLING_RATE) {
            siblings = 0;
        } else if (choice <= NO_SIBLING_RATE + ONE_SIBLING_RATE) {
            siblings = 1;
        } else if (choice <= NO_SIBLING_RATE + ONE_SIBLING_RATE + TWO_SIBLING_RATE) {
            siblings = 2;
        } else if (choice <= NO_SIBLING_RATE + ONE_SIBLING_RATE + TWO_SIBLING_RATE + THREE_SIBLING_RATE) {
            siblings = 3;
        } else {
            int moreSib = setRandom(0, 10);
            if (moreSib <= 7) {
                siblings = 4;
            } else if (moreSib <= 9) {
                siblings = 5;
            } else {
                siblings = setRandom(6, 10);
            }
        }

        return siblings;
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
     * Applies ear piercing attributes to a student.
     * Delegates to StudentPopGenerator for consistent behavior.
     *
     * @param student the student to apply piercing attributes to
     */
    public static void applyPiercingAttributes(Student student) {
        StudentPopGenerator.applyPiercingAttributes(student);
    }
}
