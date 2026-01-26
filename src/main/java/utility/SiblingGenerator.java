package utility;

import entity.Student;
import view.GameView;

import java.time.LocalDate;
import java.util.*;

import static constants.SimConstants.*;
import static utility.Randomizer.setRandom;

public class SiblingGenerator {

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

    private static String generateNotInSchoolSiblingFirstName(Student student) {
        boolean older = setRandom(0, 1) == 0;
        int year;
        if (older) {
            year = setRandom(1982, 1985);
        } else {
            year = setRandom(1992, 2000);
        }
        String yearStr = String.valueOf(year);
        NameLoader.readCSVFirst(yearStr);
        String gender = GenderLoader.genderSelection();
        return NameLoader.nameGenerator(yearStr, gender);
    }

    // Helper for player family generation: create sibling infos without touching global maps
    public static java.util.List<entity.SiblingInfo> generateSiblingInfosForPlayer(entity.Student player, int count, view.GameView view) {
        java.util.List<entity.SiblingInfo> infos = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Decide if sibling is in school with the player
            boolean inSchool = setRandom(0, 12) <= 3;
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
                    sib = generateSibling(player, view);
                }
                infos.add(new entity.SiblingInfo(sib.studentName.getFirstName(), sib.studentStatistics.getBirthday(), true));
            } else {
                // Not in school: pick older or younger and synthesize birthday
                boolean older = setRandom(0, 1) == 0;
                java.time.LocalDate birthday;
                if (older) {
                    // Older sibling: 1982–1985
                    int year = setRandom(1982, 1985);
                    int month = setRandom(1, 12);
                    int day = setRandom(1, java.time.Month.of(month).length(false));
                    birthday = java.time.LocalDate.of(year, month, day);
                } else {
                    // Younger sibling: after 1990
                    int year = setRandom(1992, 2000);
                    int month = setRandom(1, 12);
                    int day = setRandom(1, java.time.Month.of(month).length(false));
                    birthday = java.time.LocalDate.of(year, month, day);
                }
                String gen = GenderLoader.genderSelection();
                // Ensure name data for the selected year is loaded
                NameLoader.readCSVFirst(String.valueOf(birthday.getYear()));
                String first = NameLoader.nameGenerator(String.valueOf(birthday.getYear()), gen);
                infos.add(new entity.SiblingInfo(first, birthday, false));
            }
        }

        return infos;
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
                        // Otherwise, student is not in school and younger or older
                        String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                        student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                    }
                } else if (hasAdoptedSibling) {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateAdoptedSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        generatedSiblings.add(sibling);
                    } else {
                        String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                        student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                    }
                } else if (hasHalfSibling) {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateHalfSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        generatedSiblings.add(sibling);
                    } else {
                        String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                        student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                    }
                } else {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateSibling(student.getValue(), view);
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        generatedSiblings.add(sibling);
                    } else {
                        String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                        student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
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
                                //TODO: think about this one for reverse add
                                addedStudents.put(studentCap, sibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                                generatedSiblings.add(sibling);
                            } else {
                                // add two twins not in school
                                for (int i = 0; i < siblings; i++) {
                                    String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                                    student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
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
                            String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                            student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
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
                                String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                                student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
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
                                    String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                                    student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
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
                            String siblingName = generateNotInSchoolSiblingFirstName(student.getValue());
                            student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                        }
                    }
                }
            } else {
                System.out.println("No siblings to generate");
            }

            // Update sibling lists for all generated siblings
            for (Student generatedSibling : generatedSiblings) {
                for (Student otherSibling : generatedSiblings) {
                    if (!generatedSibling.equals(otherSibling)) {
                        generatedSibling.studentStatistics.addSiblingsInSchool(otherSibling);
                    }
                }
                // Copy siblingsNotInSchool from the original student to each generated sibling
                for (String siblingNotInSchool : student.getValue().studentStatistics.getSiblingsNotInSchool()) {
                    generatedSibling.studentStatistics.addSiblingsNotInSchool(siblingNotInSchool);
                }
            }
        }

        studentHashMap.putAll(addedStudents);
    }

    //TODO: Possibly centralize this under StudentPop in future
    private static Student generateStepSibling(Student student, GameView view) {
        Student studentCopy = new Student();
        String f_name;
        String race;
        String[] l_name = new String[2];
        String lastName;
        int int_stdDev = 15;
        int int_mean = 100;
        int chr_stdDev = 15;
        int chr_mean = 50;
        int agl_stdDev = 15;
        int agl_mean = 50;
        int det_stdDev = 15;
        int det_mean = 50;
        int per_stdDev = 15;
        int per_mean = 50;
        int lck_stdDev = 10;
        int lck_mean = 0;

        studentCopy.studentStatistics.setLevel(1);
        studentCopy.studentStatistics.setExperience(0);
        studentCopy.studentStatistics.setGradeLevel(setRandom(0, 3));
        studentCopy.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(studentCopy.studentStatistics.getGradeLevel()));
        studentCopy.studentStatistics.setGender(GenderLoader.genderSelection());
        f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        while (f_name.equals(student.studentName.getFirstName())) {
            f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        }
        // chance of having different last name than sibling
        if (setRandom(0, 3) == 2) {
            l_name = NameLoader.selectWeightedRandom();
            lastName = l_name[0];
        } else {
            lastName = student.studentName.getLastName();
        }
        studentCopy.studentName.setFirstName(f_name);
        studentCopy.studentName.setLastName(lastName);
        // General trends in intermarriage dictate chance of step-sibling having different race 
        if (setRandom(0, 10) < 2) {
            if (l_name[1] != null) {
                race = l_name[1];
                studentCopy.studentStatistics.setRace(race);
            } else {
                race = student.studentStatistics.getRace();
                studentCopy.studentStatistics.setRace(race);
            }

        } else {
            race = student.studentStatistics.getRace();
            studentCopy.studentStatistics.setRace(race);
        }
        studentCopy.studentStatistics.setEyeColor(TraitSelection.studentEyeColorSelection(race));
        String eyes = studentCopy.studentStatistics.getEyeColor();
        studentCopy.studentStatistics.setHairColor(TraitSelection.studentHairSelection(race, eyes));
        String hairColor = studentCopy.studentStatistics.getHairColor();
        studentCopy.studentStatistics.setInitHeight();
        studentCopy.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(int_mean, int_stdDev));
        studentCopy.studentStatistics.setCharisma((int) GameRandom.nextGaussian(chr_mean, chr_stdDev));
        studentCopy.studentStatistics.setAgility((int) GameRandom.nextGaussian(agl_mean, agl_stdDev));
        studentCopy.studentStatistics.setDetermination((int) GameRandom.nextGaussian(det_mean, det_stdDev));
        studentCopy.studentStatistics.setPerception((int) GameRandom.nextGaussian(per_mean, per_stdDev));
        studentCopy.studentStatistics.setLuck((int) GameRandom.nextGaussian(lck_mean, lck_stdDev));
        studentCopy.studentStatistics.setInitStrength();
        studentCopy.studentStatistics.setInitCreativity();
        studentCopy.studentStatistics.setInitEmpathy();
        studentCopy.studentStatistics.setInitAdaptability();
        studentCopy.studentStatistics.setInitInitiative();
        studentCopy.studentStatistics.setInitResilience();
        studentCopy.studentStatistics.setInitCuriosity();
        studentCopy.studentStatistics.setInitResponsibility();
        studentCopy.studentStatistics.setInitOpenMind();
        studentCopy.studentStatistics.setInitHairLength(setRandom(0, 10000));
        studentCopy.studentStatistics.setHairType(TraitSelection.studentHairType(race, hairColor));
        studentCopy.studentStatistics.setSkinColor(TraitSelection.studentSkinColorSelection(race, eyes));
        studentCopy.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());
        studentCopy.studentStatistics.addSiblingsInSchool(student);

        // Apply braces attributes (timing, cosmetics, charisma effects)
        applyBracesAttributes(studentCopy);

        view.appendOutput("Generated step-sibling " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
    }

    private static Student generateHalfSibling(Student student, GameView view) {
        Student studentCopy = new Student();
        String f_name;
        String race;
        String lastName;
        String studentGrade = student.studentStatistics.getGradeLevel();
        String siblingGrade;
        int int_stdDev = 15;
        int int_mean = 100;
        int chr_stdDev = 15;
        int chr_mean = 50;
        int agl_stdDev = 15;
        int agl_mean = 50;
        int det_stdDev = 15;
        int det_mean = 50;
        int per_stdDev = 15;
        int per_mean = 50;
        int lck_stdDev = 10;
        int lck_mean = 0;

        studentCopy.studentStatistics.setLevel(1);
        studentCopy.studentStatistics.setExperience(0);
        // Half sibling can either come from mother or father. if father the age gap can be closer
        if (setRandom(0, 10) <= 5) {
            studentCopy.studentStatistics.setGradeLevel(setRandom(0, 3));
        } else {
            // Ensure min age gap between siblings from same mother
            do {
                studentCopy.studentStatistics.setGradeLevel(setRandom(0, 3));
                siblingGrade = studentCopy.studentStatistics.getGradeLevel();
            } while (studentGrade.equals(siblingGrade));
        }
        studentCopy.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(studentCopy.studentStatistics.getGradeLevel()));
        studentCopy.studentStatistics.setGender(GenderLoader.genderSelection());
        // Make sure sibling names don't equal each other
        f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        while (f_name.equals(student.studentName.getFirstName())) {
            f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        }
        lastName = student.studentName.getLastName();
        studentCopy.studentName.setFirstName(f_name);
        studentCopy.studentName.setLastName(lastName);
        // General trends in intermarriage dictate chance of half-sibling having different race
        if (setRandom(0, 10) < 1) {
            race = NameLoader.selectWeightedRandom()[1];
            studentCopy.studentStatistics.setRace(race);
        } else {
            race = student.studentStatistics.getRace();
            studentCopy.studentStatistics.setRace(race);
        }
        studentCopy.studentStatistics.setEyeColor(TraitSelection.studentEyeColorSelection(race));
        String eyes = studentCopy.studentStatistics.getEyeColor();
        studentCopy.studentStatistics.setHairColor(TraitSelection.studentHairSelection(race, eyes));
        String hairColor = studentCopy.studentStatistics.getHairColor();
        studentCopy.studentStatistics.setInitHeight();
        studentCopy.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(int_mean, int_stdDev));
        studentCopy.studentStatistics.setCharisma((int) GameRandom.nextGaussian(chr_mean, chr_stdDev));
        studentCopy.studentStatistics.setAgility((int) GameRandom.nextGaussian(agl_mean, agl_stdDev));
        studentCopy.studentStatistics.setDetermination((int) GameRandom.nextGaussian(det_mean, det_stdDev));
        studentCopy.studentStatistics.setPerception((int) GameRandom.nextGaussian(per_mean, per_stdDev));
        studentCopy.studentStatistics.setLuck((int) GameRandom.nextGaussian(lck_mean, lck_stdDev));
        studentCopy.studentStatistics.setInitStrength();
        studentCopy.studentStatistics.setInitCreativity();
        studentCopy.studentStatistics.setInitEmpathy();
        studentCopy.studentStatistics.setInitAdaptability();
        studentCopy.studentStatistics.setInitInitiative();
        studentCopy.studentStatistics.setInitResilience();
        studentCopy.studentStatistics.setInitCuriosity();
        studentCopy.studentStatistics.setInitResponsibility();
        studentCopy.studentStatistics.setInitOpenMind();
        studentCopy.studentStatistics.setInitHairLength(setRandom(0, 10000));
        studentCopy.studentStatistics.setHairType(TraitSelection.studentHairType(race, hairColor));
        studentCopy.studentStatistics.setSkinColor(TraitSelection.studentSkinColorSelection(race, eyes));
        studentCopy.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());
        studentCopy.studentStatistics.addSiblingsInSchool(student);

        // Apply braces attributes (timing, cosmetics, charisma effects)
        applyBracesAttributes(studentCopy);

        view.appendOutput("Generated half-sibling " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
    }

    private static Student generateAdoptedSibling(Student student, GameView view) {
        Student studentCopy = new Student();
        String f_name;
        String l_name;
        String race;
        int int_stdDev = 15;
        int int_mean = 100;
        int chr_stdDev = 15;
        int chr_mean = 50;
        int agl_stdDev = 15;
        int agl_mean = 50;
        int det_stdDev = 15;
        int det_mean = 50;
        int per_stdDev = 15;
        int per_mean = 50;
        int lck_stdDev = 10;
        int lck_mean = 0;

        studentCopy.studentStatistics.setLevel(1);
        studentCopy.studentStatistics.setExperience(0);
        studentCopy.studentStatistics.setGradeLevel(setRandom(0, 3));
        studentCopy.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(studentCopy.studentStatistics.getGradeLevel()));
        studentCopy.studentStatistics.setGender(GenderLoader.genderSelection());
        f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        while (f_name.equals(student.studentName.getFirstName())) {
            f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        }
        l_name = student.studentName.getLastName();
        studentCopy.studentName.setFirstName(f_name);
        studentCopy.studentName.setLastName(l_name);
        race = NameLoader.selectWeightedRandom()[1];
        studentCopy.studentStatistics.setRace(race);
        studentCopy.studentStatistics.setEyeColor(TraitSelection.studentEyeColorSelection(race));
        String eyes = studentCopy.studentStatistics.getEyeColor();
        studentCopy.studentStatistics.setHairColor(TraitSelection.studentHairSelection(race, eyes));
        String hairColor = studentCopy.studentStatistics.getHairColor();
        studentCopy.studentStatistics.setInitHeight();
        studentCopy.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(int_mean, int_stdDev));
        studentCopy.studentStatistics.setCharisma((int) GameRandom.nextGaussian(chr_mean, chr_stdDev));
        studentCopy.studentStatistics.setAgility((int) GameRandom.nextGaussian(agl_mean, agl_stdDev));
        studentCopy.studentStatistics.setDetermination((int) GameRandom.nextGaussian(det_mean, det_stdDev));
        studentCopy.studentStatistics.setPerception((int) GameRandom.nextGaussian(per_mean, per_stdDev));
        studentCopy.studentStatistics.setLuck((int) GameRandom.nextGaussian(lck_mean, lck_stdDev));
        studentCopy.studentStatistics.setInitStrength();
        studentCopy.studentStatistics.setInitCreativity();
        studentCopy.studentStatistics.setInitEmpathy();
        studentCopy.studentStatistics.setInitAdaptability();
        studentCopy.studentStatistics.setInitInitiative();
        studentCopy.studentStatistics.setInitResilience();
        studentCopy.studentStatistics.setInitCuriosity();
        studentCopy.studentStatistics.setInitResponsibility();
        studentCopy.studentStatistics.setInitOpenMind();
        studentCopy.studentStatistics.setInitHairLength(setRandom(0, 10000));
        studentCopy.studentStatistics.setHairType(TraitSelection.studentHairType(race, hairColor));
        studentCopy.studentStatistics.setSkinColor(TraitSelection.studentSkinColorSelection(race, eyes));
        studentCopy.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());
        studentCopy.studentStatistics.addSiblingsInSchool(student);

        // Apply braces attributes (timing, cosmetics, charisma effects)
        applyBracesAttributes(studentCopy);

        view.appendOutput("Generated adopted sibling " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
    }

    private static Student generateTwinOrTriplet(Student student, GameView view) {
        Student studentCopy = new Student();
        String f_name;
        String l_name;
        int int_stdDev = 15;
        int int_mean = 100;
        int chr_stdDev = 15;
        int chr_mean = 50;
        int agl_stdDev = 15;
        int agl_mean = 50;
        int det_stdDev = 15;
        int det_mean = 50;
        int per_stdDev = 15;
        int per_mean = 50;
        int lck_stdDev = 10;
        int lck_mean = 0;

        studentCopy.studentStatistics.setLevel(1);
        studentCopy.studentStatistics.setExperience(0);
        studentCopy.studentStatistics.setGradeLevel(student.studentStatistics.getGradeLevel());
        studentCopy.studentStatistics.setBirthday(student.studentStatistics.getBirthday());
        studentCopy.studentStatistics.setGender(GenderLoader.genderSelection());
        f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        while (f_name.equals(student.studentName.getFirstName())) {
            f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        }
        l_name = student.studentName.getLastName();
        studentCopy.studentName.setFirstName(f_name);
        studentCopy.studentName.setLastName(l_name);
        studentCopy.studentStatistics.setRace(student.studentStatistics.getRace());
        studentCopy.studentStatistics.setEyeColor(student.studentStatistics.getEyeColor());
        studentCopy.studentStatistics.setHairColor(student.studentStatistics.getHairColor());
        studentCopy.studentStatistics.setHeight(student.studentStatistics.getHeight());
        studentCopy.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(int_mean, int_stdDev));
        studentCopy.studentStatistics.setCharisma((int) GameRandom.nextGaussian(chr_mean, chr_stdDev));
        studentCopy.studentStatistics.setAgility((int) GameRandom.nextGaussian(agl_mean, agl_stdDev));
        studentCopy.studentStatistics.setDetermination((int) GameRandom.nextGaussian(det_mean, det_stdDev));
        studentCopy.studentStatistics.setPerception((int) GameRandom.nextGaussian(per_mean, per_stdDev));
        studentCopy.studentStatistics.setLuck((int) GameRandom.nextGaussian(lck_mean, lck_stdDev));
        studentCopy.studentStatistics.setInitStrength();
        studentCopy.studentStatistics.setInitCreativity();
        studentCopy.studentStatistics.setInitEmpathy();
        studentCopy.studentStatistics.setInitAdaptability();
        studentCopy.studentStatistics.setInitInitiative();
        studentCopy.studentStatistics.setInitResilience();
        studentCopy.studentStatistics.setInitCuriosity();
        studentCopy.studentStatistics.setInitResponsibility();
        studentCopy.studentStatistics.setInitOpenMind();
        studentCopy.studentStatistics.setInitHairLength(setRandom(0, 10000));
        studentCopy.studentStatistics.setHairType(student.studentStatistics.getHairType());
        studentCopy.studentStatistics.setSkinColor(student.studentStatistics.getSkinColor());
        studentCopy.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());
        studentCopy.studentStatistics.addSiblingsInSchool(student);

        // Apply braces attributes (timing, cosmetics, charisma effects)
        applyBracesAttributes(studentCopy);

        view.appendOutput("Generated twin or triplet " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
    }

    private static Student generateSibling(Student student, GameView view) {
        Student studentCopy = new Student();
        String f_name;
        String l_name;
        String studentGrade = student.studentStatistics.getGradeLevel();
        String siblingGrade;
        int int_stdDev = 15;
        int int_mean = 100;
        int chr_stdDev = 15;
        int chr_mean = 50;
        int agl_stdDev = 15;
        int agl_mean = 50;
        int det_stdDev = 15;
        int det_mean = 50;
        int per_stdDev = 15;
        int per_mean = 50;
        int lck_stdDev = 10;
        int lck_mean = 0;

        studentCopy.studentStatistics.setLevel(1);
        studentCopy.studentStatistics.setExperience(0);
        studentCopy.studentStatistics.setGradeLevel(setRandom(0, 3));
        // Ensure min age gap between true siblings
        do {
            studentCopy.studentStatistics.setGradeLevel(setRandom(0, 3));
            siblingGrade = studentCopy.studentStatistics.getGradeLevel();
        } while (studentGrade.equals(siblingGrade));
        studentCopy.studentStatistics.setBirthday(BirthdayGenerator.generateDateFromClass(studentCopy.studentStatistics.getGradeLevel()));
        studentCopy.studentStatistics.setGender(GenderLoader.genderSelection());
        f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        while (f_name.equals(student.studentName.getFirstName())) {
            f_name = NameLoader.nameGenerator(String.valueOf(studentCopy.studentStatistics.getBirthday().getYear()), studentCopy.studentStatistics.getGender());
        }
        l_name = student.studentName.getLastName();
        studentCopy.studentName.setFirstName(f_name);
        studentCopy.studentName.setLastName(l_name);
        studentCopy.studentStatistics.setRace(student.studentStatistics.getRace());
        studentCopy.studentStatistics.setEyeColor(student.studentStatistics.getEyeColor());
        studentCopy.studentStatistics.setHairColor(student.studentStatistics.getHairColor());
        studentCopy.studentStatistics.setHeight(student.studentStatistics.getHeight());
        studentCopy.studentStatistics.setIntelligence((int) GameRandom.nextGaussian(int_mean, int_stdDev));
        studentCopy.studentStatistics.setCharisma((int) GameRandom.nextGaussian(chr_mean, chr_stdDev));
        studentCopy.studentStatistics.setAgility((int) GameRandom.nextGaussian(agl_mean, agl_stdDev));
        studentCopy.studentStatistics.setDetermination((int) GameRandom.nextGaussian(det_mean, det_stdDev));
        studentCopy.studentStatistics.setPerception((int) GameRandom.nextGaussian(per_mean, per_stdDev));
        studentCopy.studentStatistics.setLuck((int) GameRandom.nextGaussian(lck_mean, lck_stdDev));
        studentCopy.studentStatistics.setInitStrength();
        studentCopy.studentStatistics.setInitCreativity();
        studentCopy.studentStatistics.setInitEmpathy();
        studentCopy.studentStatistics.setInitAdaptability();
        studentCopy.studentStatistics.setInitInitiative();
        studentCopy.studentStatistics.setInitResilience();
        studentCopy.studentStatistics.setInitCuriosity();
        studentCopy.studentStatistics.setInitResponsibility();
        studentCopy.studentStatistics.setInitOpenMind();
        studentCopy.studentStatistics.setInitHairLength(setRandom(0, 10000));
        studentCopy.studentStatistics.setHairType(student.studentStatistics.getHairType());
        studentCopy.studentStatistics.setSkinColor(student.studentStatistics.getSkinColor());
        studentCopy.studentStatistics.setIncomeLevel(student.studentStatistics.getIncomeLevel());
        studentCopy.studentStatistics.addSiblingsInSchool(student);

        // Apply braces attributes (timing, cosmetics, charisma effects)
        applyBracesAttributes(studentCopy);

        view.appendOutput("Generated sibling " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
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
     * This includes determining if they have/had braces, timing, cosmetics, and charisma effects.
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
}
