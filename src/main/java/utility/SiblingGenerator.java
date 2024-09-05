package utility;

import entity.Student;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static utility.Randomizer.setRandom;

public class SiblingGenerator {

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

    public static void siblingGenerator(HashMap<Integer, Student> studentHashMap, int studentCap) {
        HashMap<Integer, Student> addedStudents = new HashMap<>();
        HashMap<Integer, Student> combinedStudent = new HashMap<>();

        for (Map.Entry<Integer, Student> student : studentHashMap.entrySet()) {
            int siblings = siblingProbabilityLoader();
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
                    sibling = generateTwinOrTriplet(student.getValue());
                    addedStudents.put(studentCap, sibling);
                } else if (hasStepSibling) {
                    // Higher chance step-sibling is around same age and therefore in school
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateStepSibling(student.getValue());
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                    } else {
                        // Otherwise, student is not in school and younger or older
                        String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
                        student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                    }
                } else if (hasAdoptedSibling) {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateAdoptedSibling(student.getValue());
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                    } else {
                        String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
                        student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                    }
                } else if (hasHalfSibling) {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateHalfSibling(student.getValue());
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                    } else {
                        String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
                        student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                    }
                } else {
                    if (setRandom(0, 12) <= 3) {
                        studentCap++;
                        sibling = generateSibling(student.getValue());
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                    } else {
                        String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
                        student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                    }
                }
            } else if (siblings == 2) {
                if (hasTriplets) {
                    for (int x = 0; x < siblings; x++) {
                        studentCap++;
                        sibling = generateTwinOrTriplet(student.getValue());
                        addedStudents.put(studentCap, sibling);
                        student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                    }
                    finishedGeneration = true;
                } else {
                    if (hasTwins) {
                        // can either be a twin or have twin siblings
                        if (setRandom(0, 1) == 1) {
                            studentCap++;
                            sibling = generateTwinOrTriplet(student.getValue());
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            siblings--;
                        } else {
                            if (setRandom(0, 12) <= 3) {
                                // Generate twin sibling first and then copy sibling
                                studentCap++;
                                Student twinSibling = generateSibling(student.getValue());
                                addedStudents.put(studentCap, twinSibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(twinSibling);
                                // add second twin
                                studentCap++;
                                sibling = generateTwinOrTriplet(twinSibling);
                                addedStudents.put(studentCap, sibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            } else {
                                // add two twins not in school
                                for (int i = 0; i < siblings; i++) {
                                    String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
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
                                sibling = generateStepSibling(student.getValue());
                                addedStudents.put(studentCap, sibling);
                            } else if (hasAdoptedSibling) {
                                sibling = generateAdoptedSibling(student.getValue());
                                addedStudents.put(studentCap, sibling);
                            } else if (hasHalfSibling) {
                                sibling = generateHalfSibling(student.getValue());
                                addedStudents.put(studentCap, sibling);
                            } else {
                                sibling = generateSibling(student.getValue());
                                addedStudents.put(studentCap, sibling);
                            }
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        } else {
                            String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
                            student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                        }
                    }
                }
            } else if (siblings >= 3) {
                if (hasTriplets) {
                    if (setRandom(0, 1) == 1) {
                        for (int x = 0; x < siblings; x++) {
                            studentCap++;
                            sibling = generateTwinOrTriplet(student.getValue());
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            siblings--;
                        }
                    } else {
                        if (setRandom(0, 12) <= 3) {
                            // Generate triplet sibling first and then copy sibling
                            studentCap++;
                            Student tripletSibling = generateSibling(student.getValue());
                            addedStudents.put(studentCap, tripletSibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(tripletSibling);
                            siblings--;
                            // add second twin
                            studentCap++;
                            sibling = generateTwinOrTriplet(tripletSibling);
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            siblings--;
                        } else {
                            for (int i = 0; i < 3; i++) {
                                String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
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
                            sibling = generateTwinOrTriplet(student.getValue());
                            addedStudents.put(studentCap, sibling);
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                            siblings--;
                        } else {
                            if (setRandom(0, 12) <= 3) {
                                // Generate twin sibling first and then copy sibling
                                studentCap++;
                                Student twinSibling = generateSibling(student.getValue());
                                addedStudents.put(studentCap, twinSibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(twinSibling);
                                siblings--;
                                // add second twin
                                studentCap++;
                                sibling = generateTwinOrTriplet(twinSibling);
                                addedStudents.put(studentCap, sibling);
                                student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                                siblings--;
                            } else {
                                for (int i = 0; i < 2; i++) {
                                    String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
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
                                sibling = generateStepSibling(student.getValue());
                                addedStudents.put(studentCap, sibling);
                            } else if (hasAdoptedSibling) {
                                sibling = generateAdoptedSibling(student.getValue());
                                addedStudents.put(studentCap, sibling);
                            } else if (hasHalfSibling) {
                                sibling = generateHalfSibling(student.getValue());
                                addedStudents.put(studentCap, sibling);
                            } else {
                                sibling = generateSibling(student.getValue());
                                addedStudents.put(studentCap, sibling);
                            }
                            student.getValue().studentStatistics.addSiblingsInSchool(sibling);
                        } else {
                            String siblingName = NameLoader.nameGenerator(setRandom(1986, 1990).toString(), GenderLoader.genderSelection());
                            student.getValue().studentStatistics.addSiblingsNotInSchool(siblingName);
                        }
                    }
                }
            } else {
                System.out.println("No siblings to generate");
            }
        }

        studentHashMap.putAll(addedStudents);
    }

    //TODO: Possibly centralize this under StudentPop in future
    private static Student generateStepSibling(Student student) {
        Student studentCopy = new Student();
        Random distribution = new Random();
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
        studentCopy.studentStatistics.setIntelligence((int) (distribution.nextGaussian() * int_stdDev + int_mean));
        studentCopy.studentStatistics.setCharisma((int) (distribution.nextGaussian() * chr_stdDev + chr_mean));
        studentCopy.studentStatistics.setAgility((int) (distribution.nextGaussian() * agl_stdDev + agl_mean));
        studentCopy.studentStatistics.setDetermination((int) (distribution.nextGaussian() * det_stdDev + det_mean));
        studentCopy.studentStatistics.setPerception((int) (distribution.nextGaussian() * per_stdDev + per_mean));
        studentCopy.studentStatistics.setLuck((int) (distribution.nextGaussian() * lck_stdDev + lck_mean));
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

        System.out.println("   Generated step-sibling " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
    }

    private static Student generateHalfSibling(Student student) {
        Student studentCopy = new Student();
        Random distribution = new Random();
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
        if (setRandom(0,10) <= 5) {
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
        studentCopy.studentStatistics.setIntelligence((int) (distribution.nextGaussian() * int_stdDev + int_mean));
        studentCopy.studentStatistics.setCharisma((int) (distribution.nextGaussian() * chr_stdDev + chr_mean));
        studentCopy.studentStatistics.setAgility((int) (distribution.nextGaussian() * agl_stdDev + agl_mean));
        studentCopy.studentStatistics.setDetermination((int) (distribution.nextGaussian() * det_stdDev + det_mean));
        studentCopy.studentStatistics.setPerception((int) (distribution.nextGaussian() * per_stdDev + per_mean));
        studentCopy.studentStatistics.setLuck((int) (distribution.nextGaussian() * lck_stdDev + lck_mean));
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

        System.out.println("   Generated half-sibling " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
    }

    private static Student generateAdoptedSibling(Student student) {
        Student studentCopy = new Student();
        Random distribution = new Random();
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
        studentCopy.studentStatistics.setIntelligence((int) (distribution.nextGaussian() * int_stdDev + int_mean));
        studentCopy.studentStatistics.setCharisma((int) (distribution.nextGaussian() * chr_stdDev + chr_mean));
        studentCopy.studentStatistics.setAgility((int) (distribution.nextGaussian() * agl_stdDev + agl_mean));
        studentCopy.studentStatistics.setDetermination((int) (distribution.nextGaussian() * det_stdDev + det_mean));
        studentCopy.studentStatistics.setPerception((int) (distribution.nextGaussian() * per_stdDev + per_mean));
        studentCopy.studentStatistics.setLuck((int) (distribution.nextGaussian() * lck_stdDev + lck_mean));
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

        System.out.println("   Generated adopted sibling " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
    }

    private static Student generateTwinOrTriplet(Student student) {
        Student studentCopy = new Student();
        Random distribution = new Random();
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
        studentCopy.studentStatistics.setIntelligence((int) (distribution.nextGaussian() * int_stdDev + int_mean));
        studentCopy.studentStatistics.setCharisma((int) (distribution.nextGaussian() * chr_stdDev + chr_mean));
        studentCopy.studentStatistics.setAgility((int) (distribution.nextGaussian() * agl_stdDev + agl_mean));
        studentCopy.studentStatistics.setDetermination((int) (distribution.nextGaussian() * det_stdDev + det_mean));
        studentCopy.studentStatistics.setPerception((int) (distribution.nextGaussian() * per_stdDev + per_mean));
        studentCopy.studentStatistics.setLuck((int) (distribution.nextGaussian() * lck_stdDev + lck_mean));
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

        System.out.println("   Generated twin or triplet " + f_name + " " + studentCopy.studentName.getLastName());

        return studentCopy;
    }

    private static Student generateSibling(Student student) {
        Student studentCopy = new Student();
        Random distribution = new Random();
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
        studentCopy.studentStatistics.setIntelligence((int) (distribution.nextGaussian() * int_stdDev + int_mean));
        studentCopy.studentStatistics.setCharisma((int) (distribution.nextGaussian() * chr_stdDev + chr_mean));
        studentCopy.studentStatistics.setAgility((int) (distribution.nextGaussian() * agl_stdDev + agl_mean));
        studentCopy.studentStatistics.setDetermination((int) (distribution.nextGaussian() * det_stdDev + det_mean));
        studentCopy.studentStatistics.setPerception((int) (distribution.nextGaussian() * per_stdDev + per_mean));
        studentCopy.studentStatistics.setLuck((int) (distribution.nextGaussian() * lck_stdDev + lck_mean));
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

        System.out.println("   Generated sibling " + f_name + " " + studentCopy.studentName.getLastName());

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
}
