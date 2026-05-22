package utility;

import entity.Body.StudentHead;
import entity.Items.ClothingItem;
import entity.Items.Decoration;
import entity.Items.EquipmentSlot;
import entity.Items.Outfit;
import entity.Items.WearableItem;
import entity.Radio.Radio;
import entity.Radio.RadioStation;
import entity.Radio.Song;
import entity.Rooms.Classroom;
import entity.*;
import entity.Rooms.Room;
import entity.academic.AcademicSkill;
import entity.academic.CourseProgress;
import entity.academic.HomeworkAssignment;
import entity.academic.StudentAcademicRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Inspector {

    private static final DecimalFormat NEED_FORMAT = new DecimalFormat("#.#");

    /**
     * Formats a physiological need value as a readable string with its
     * numeric value and a short status label.
     */
    private static String formatNeed(double value) {
        String label;
        if (value >= 80) {
            label = "Good";
        } else if (value >= 50) {
            label = "Okay";
        } else if (value >= 30) {
            label = "Low";
        } else if (value >= 10) {
            label = "Critical";
        } else {
            label = "Desperate";
        }
        return NEED_FORMAT.format(value) + " (" + label + ")";
    }

    /**
     * Formats a height given in inches as a human-friendly "X feet and Y inches"
     * string. The underlying numeric value is preserved elsewhere; this is only
     * used for display in descriptions. The inches portion is rounded to the
     * nearest whole inch and overflow to the next foot is handled (e.g. a value
     * that rounds to 12 inches becomes an extra foot).
     */
    private static String formatHeightFeetInches(double heightInInches) {
        int totalInches = (int) Math.round(heightInInches);
        if (totalInches < 0) {
            totalInches = 0;
        }
        int feet = totalInches / 12;
        int inches = totalInches % 12;
        StringBuilder sb = new StringBuilder();
        sb.append(feet).append(feet == 1 ? " foot" : " feet");
        if (inches > 0) {
            sb.append(" and ").append(inches).append(inches == 1 ? " inch" : " inches");
        }
        return sb.toString();
    }

    /**
     * Builds the physical description text for a student.
     * Includes appearance, grade, birthday, family info, and braces/piercing history.
     *
     * @param student the student to describe
     * @return the formatted description text
     */
    private static String buildStudentDescriptionText(Student student) {
        StringBuilder sb = new StringBuilder();
        String firstName = student.studentName.getFirstName();
        String gender = student.studentStatistics.getGender();
        String hairColor = student.studentStatistics.getHairColor();
        String eyeColor = student.studentStatistics.getEyeColor();
        String skinColor = student.studentStatistics.getSkinColor();
        String hairLength = student.studentStatistics.getHairLength();
        String hairType = student.studentStatistics.getHairType();
        double height = student.studentStatistics.getHeight();
        boolean hasBraces = student.studentStatistics.getHasBraces();
        String bracesBandColor = student.studentStatistics.getBracesBandColor();
        String bracesSecondBandColor = student.studentStatistics.getBracesSecondBandColor();
        boolean hasAlternatingBands = student.studentStatistics.hasAlternatingBandColors();
        String bracesBracketType = student.studentStatistics.getBracesBracketType();
        boolean bracesHasElastics = student.studentStatistics.getBracesHasElastics();
        String bracesElasticColor = student.studentStatistics.getBracesElasticColor();
        String bracesElasticType = student.studentStatistics.getBracesElasticType();
        LocalDate bracesStartDate = student.studentStatistics.getBracesStartDate();
        LocalDate bracesEndDate = student.studentStatistics.getBracesEndDate();
        boolean hadBracesRemoved = student.studentStatistics.getHadBracesRemoved();
        String grade = student.studentStatistics.getGradeLevel();
        String income = student.studentStatistics.getIncomeLevel();
        LocalDate birth = student.studentStatistics.getBirthday();
        List<Student> siblingsNotInSchool = student.studentStatistics.getSiblingsNotInSchool();
        List<Student> siblingsInSchool = student.studentStatistics.getSiblingsInSchool();

        sb.append(student.studentName.getFullName()).append("\n=====================================\n");

        sb.append(firstName).append(" is a ").append(gender.toLowerCase()).append(" with ");
        sb.append(skinColor).append(" colored skin and ");
        sb.append(hairLength.toLowerCase()).append(", ").append(hairType.toLowerCase()).append(", ")
                .append(hairColor.toLowerCase());
        sb.append(" hair");
        String hairDye = student.studentStatistics.getHairDye();
        String hairHighlights = student.studentStatistics.getHairHighlights();
        String hairStyle = student.studentStatistics.getHairStyle();
        if (hairDye != null && !hairDye.isBlank()
                && !hairDye.equalsIgnoreCase(hairColor)) {
            sb.append(" dyed ").append(hairDye.toLowerCase());
        }
        if (hairHighlights != null) {
            sb.append(" with ").append(hairHighlights.toLowerCase()).append(" highlights");
        }
        if (hairStyle != null) {
            String styleLower = hairStyle.toLowerCase();
            sb.append(styleLower.endsWith("s") ? " in " : " in a ").append(styleLower);
        }
        sb.append(" and ").append(eyeColor.toLowerCase()).append(" eyes. ");
        List<String> uniqueTraits = student.studentStatistics.getUniqueTraits();
        if (uniqueTraits != null && !uniqueTraits.isEmpty()) {
            for (String trait : uniqueTraits) {
                sb.append(trait).append(" ");
            }
        }
        sb.append("They stand ").append(formatHeightFeetInches(height)).append(" tall.");
        if (hasBraces) {
            sb.append(" They have braces with ");
            if (hasAlternatingBands) {
                sb.append("alternating ").append(bracesBandColor).append(" and ")
                        .append(bracesSecondBandColor).append(" bands, ");
            } else {
                sb.append(bracesBandColor).append(" bands, ");
            }
            sb.append(bracesBracketType).append(" brackets");
            if (bracesHasElastics) {
                sb.append(", and a pair of ").append(bracesElasticColor).append(" ").append(bracesElasticType);
            }
            sb.append(".");
        }
        if (student.studentStatistics.getHasGlasses() && !student.studentStatistics.getHasContacts()) {
            sb.append(" They wear glasses.");
        }
        String piercingDesc = buildHeadPiercingDescription(student);
        if (piercingDesc != null) {
            sb.append(" ").append(piercingDesc);
        }
        String outfitDesc = buildOutfitDescription(
                student.studentStatistics.getCurrentOutfit());
        if (outfitDesc != null) {
            sb.append(" ").append(outfitDesc);
        }
        sb.append("\n");

        sb.append(firstName).append(" is a ").append(grade).append(".\n");
        String cliqueLabel = student.studentStatistics.getCliqueLabel();
        if (cliqueLabel != null) {
            sb.append(firstName).append(" is a ").append(cliqueLabel).append(".");
            String secondary = student.studentStatistics.getSecondaryClique();
            if (secondary != null) {
                sb.append(" Secondary: ").append(secondary).append(".");
            }
            sb.append("\n");
        }
        sb.append(firstName).append(" was born on ").append(birth).append(".\n");

        // Family info
        sb.append("\nTheir family has the following income: ").append(income).append("\n");
        if (!siblingsInSchool.isEmpty()) {
            sb.append("They have the following siblings in school:\n");
            for (Student sibling : siblingsInSchool) {
                sb.append("   ").append(sibling.studentName.getFullName()).append("\n");
            }
        }
        if (!siblingsNotInSchool.isEmpty()) {
            sb.append("They have the following siblings not in school:\n");
            for (Student sibling : siblingsNotInSchool) {
                sb.append("   ").append(sibling.studentName.getFullName()).append("\n");
            }
        }

        // Braces history
        if (hasBraces) {
            if (bracesStartDate != null && bracesEndDate != null) {
                sb.append("\n(Got braces: ").append(bracesStartDate)
                        .append(", Expected removal: ").append(bracesEndDate).append(")\n");
            }
        } else if (hadBracesRemoved) {
            sb.append("\nThey previously had braces");
            if (bracesStartDate != null && bracesEndDate != null) {
                sb.append(" (").append(bracesStartDate).append(" to ").append(bracesEndDate).append(")");
            }
            sb.append(".\n");
        }

        return sb.toString();
    }

    /**
     * Builds a natural-language description of piercings equipped on a
     * student's head. Handles ear piercings (both/single ear, counts,
     * mixed types) and other facial/body piercings.
     *
     * @param student the student whose head equipment to describe
     * @return description string, or null if no piercings equipped
     */
    private static String buildHeadPiercingDescription(Student student) {
        StudentHead head = student.getStudentHead();
        if (head == null || !head.hasAnyEquipped()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        List<WearableItem> leftEar = head.getEquippedList(EquipmentSlot.LEFT_EAR);
        List<WearableItem> rightEar = head.getEquippedList(EquipmentSlot.RIGHT_EAR);

        if (!leftEar.isEmpty() || !rightEar.isEmpty()) {
            appendEarDescription(sb, leftEar, rightEar);
        }

        appendSlotDescription(sb, head, EquipmentSlot.NOSE, "nose");
        appendSlotDescription(sb, head, EquipmentSlot.LIPS, "lip");
        appendSlotDescription(sb, head, EquipmentSlot.EYEBROW, "eyebrow");

        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Builds a natural-language description of the outfit a person is
     * currently wearing. Items are listed bottom-to-top
     * (one_piece, tops, bottoms, outerwear, shoes, accessories) so the
     * prose reads in a stable, intuitive order regardless of the order
     * generation added them.
     *
     * @param outfit the outfit to describe; may be {@code null}
     * @return description sentence (e.g. {@code "They are wearing a
     *         black band t-shirt, denim jeans, and white sneakers."}),
     *         or {@code null} when there is nothing to describe
     */
    private static String buildOutfitDescription(Outfit outfit) {
        if (outfit == null || outfit.isEmpty()) {
            return null;
        }

        List<ClothingItem> ordered = new java.util.ArrayList<>();
        for (String layer : OUTFIT_LAYER_ORDER) {
            ordered.addAll(outfit.getItemsByLayer(layer));
        }
        // Append any items in layers we don't know about, preserving
        // insertion order so unexpected layers still appear in prose.
        for (ClothingItem item : outfit.getItems()) {
            if (!ordered.contains(item)) {
                ordered.add(item);
            }
        }

        StringBuilder sb = new StringBuilder("They are wearing ");
        for (int i = 0; i < ordered.size(); i++) {
            String name = ordered.get(i).getDisplayName();
            if (i == 0) {
                sb.append(article(name).toLowerCase()).append(' ').append(name);
            } else if (i == ordered.size() - 1) {
                sb.append(ordered.size() > 2 ? ", and " : " and ").append(name);
            } else {
                sb.append(", ").append(name);
            }
        }
        sb.append('.');
        return sb.toString();
    }

    /**
     * Canonical layer order used when serializing outfit prose. Mirrors
     * the layer keys used in {@code outfit_types.json} and
     * {@code clique_clothing.json}.
     */
    private static final String[] OUTFIT_LAYER_ORDER = {
            "one_piece", "tops", "bottoms", "outerwear",
            "shoes", "accessories"
    };

    private static void appendEarDescription(StringBuilder sb,
                                             List<WearableItem> left,
                                             List<WearableItem> right) {
        boolean bothEars = !left.isEmpty() && !right.isEmpty();
        if (bothEars) {
            boolean allUniform = allSameType(left) && allSameType(right)
                    && left.get(0).getDisplayName().equals(
                            right.get(0).getDisplayName());

            if (allUniform) {
                sb.append("They have both ears pierced with ");
                appendItemName(sb, left.get(0), true);

                int leftCount = left.size();
                int rightCount = right.size();
                if (leftCount == rightCount && leftCount > 1) {
                    sb.append(" (").append(leftCount).append(" per ear)");
                } else if (leftCount != rightCount) {
                    sb.append(", with ").append(leftCount)
                            .append(" on the left and ").append(rightCount)
                            .append(" on the right");
                }
                sb.append(".");
            } else {
                sb.append("They have both ears pierced.");
                sb.append(" Their left ear has ");
                appendItemList(sb, left);
                sb.append(".");
                sb.append(" Their right ear has ");
                appendItemList(sb, right);
                sb.append(".");
            }
        } else {
            String ear = !left.isEmpty() ? "left" : "right";
            List<WearableItem> items = !left.isEmpty() ? left : right;

            if (items.size() == 1) {
                sb.append("Their ").append(ear)
                        .append(" ear is pierced with ");
                appendItemName(sb, items.get(0), false);
            } else if (allSameType(items)) {
                sb.append("Their ").append(ear).append(" ear has ")
                        .append(items.size()).append(" ");
                appendItemName(sb, items.get(0), true);
            } else {
                sb.append("Their ").append(ear).append(" ear has ");
                appendItemList(sb, items);
            }
            sb.append(".");
        }
    }

    private static boolean allSameType(List<WearableItem> items) {
        if (items.size() <= 1) {
            return true;
        }
        String first = items.get(0).getDisplayName();
        for (int i = 1; i < items.size(); i++) {
            if (!items.get(i).getDisplayName().equals(first)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Appends a natural-language list of piercings, grouping duplicates
     * by display name (e.g. "2 silver studs, a gold hoop, and a black
     * titanium small hoop").
     */
    private static void appendItemList(StringBuilder sb,
                                       List<WearableItem> items) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (WearableItem item : items) {
            counts.merge(item.getDisplayName(), 1, Integer::sum);
        }

        int idx = 0;
        int total = counts.size();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (idx > 0 && idx == total - 1) {
                sb.append(total > 2 ? ", and " : " and ");
            } else if (idx > 0) {
                sb.append(", ");
            }

            int count = entry.getValue();
            String name = entry.getKey();
            if (count > 1) {
                sb.append(count).append(" ")
                        .append(pluralizeDisplayName(name));
            } else {
                sb.append("a ").append(singularizeDisplayName(name));
            }
            idx++;
        }
    }

    private static void appendSlotDescription(StringBuilder sb,
                                              StudentHead head,
                                              EquipmentSlot slot,
                                              String areaName) {
        WearableItem item = head.getEquipped(slot);
        if (item == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" ");
        }
        String typeName = item.getName();
        boolean isPaired = typeName != null && typeName.endsWith("s");
        if (isPaired) {
            sb.append("They have ").append(singularizeDisplayName(item))
                    .append(" ").append(areaName).append(" piercings.");
        } else {
            sb.append("They have a ").append(item.getDisplayName())
                    .append(" ").append(areaName).append(" piercing.");
        }
    }

    /**
     * Returns the display name with the piercing type name singularized
     * (e.g. "gunmetal surgical steel snakebites" becomes
     * "gunmetal surgical steel snakebite").
     */
    private static String singularizeDisplayName(WearableItem item) {
        String display = item.getDisplayName();
        String name = item.getName();
        if (name != null && name.endsWith("s") && display.endsWith(name)) {
            return display.substring(0, display.length() - name.length())
                    + name.substring(0, name.length() - 1);
        }
        return display;
    }

    private static String singularizeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return displayName;
        }
        String lower = displayName.toLowerCase();
        if (lower.endsWith("gauges")) {
            return displayName.substring(0, displayName.length() - "gauges".length())
                    + "gauge";
        }
        if (lower.endsWith("hoops")) {
            return displayName.substring(0, displayName.length() - "hoops".length())
                    + "hoop";
        }
        if (lower.endsWith("dangling earrings")) {
            return displayName.substring(0, displayName.length() - "earrings".length())
                    + "earring";
        }
        if (lower.endsWith("studs")) {
            return displayName.substring(0, displayName.length() - "studs".length())
                    + "stud";
        }
        return displayName;
    }

    private static void appendItemName(StringBuilder sb, WearableItem item,
                                       boolean plural) {
        String displayName = item.getDisplayName();
        if (!plural) {
            sb.append("a ");
        }
        if (plural) {
            sb.append(pluralizeDisplayName(displayName));
        } else {
            sb.append(displayName);
        }
    }

    private static String pluralizeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return displayName;
        }
        String lower = displayName.toLowerCase();
        if (lower.endsWith("ch")
                || lower.endsWith("sh")
                || lower.endsWith("x")
                || lower.endsWith("z")
                || lower.endsWith("ss")) {
            return displayName + "es";
        }
        if (lower.endsWith("s")) {
            return displayName;
        }
        if (lower.endsWith("y") && displayName.length() > 1) {
            char beforeY = lower.charAt(lower.length() - 2);
            if ("aeiou".indexOf(beforeY) == -1) {
                return displayName.substring(0, displayName.length() - 1) + "ies";
            }
        }
        return displayName + "s";
    }

    /**
     * Builds the stats and status effects text for a student.
     * Includes base stats, secondary stats, and active status effects.
     *
     * @param student the student to build stats for
     * @return the formatted stats text
     */
    private static String buildStudentStatsText(Student student) {
        StringBuilder sb = new StringBuilder();
        String firstName = student.studentName.getFirstName();
        boolean hasBraces = student.studentStatistics.getHasBraces();
        boolean hadBracesRemoved = student.studentStatistics.getHadBracesRemoved();

        sb.append(student.studentName.getFullName()).append("\n=====================================\n");

        // Base stats
        sb.append("Base Stats:\n   INTELLIGENCE: ")
                .append(student.studentStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(student.studentStatistics.getEffectiveCharisma());
        if (hasBraces) {
            sb.append(" (reduced by braces)");
        } else if (hadBracesRemoved) {
            sb.append(" (boosted by past braces)");
        }
        if (student.studentStatistics.getHasEarPiercing()) {
            sb.append(" (boosted by earrings)");
        }
        sb.append("\n   AGILITY: ");
        sb.append(student.studentStatistics.getEffectiveAgility());
        if (student.studentStatistics.hasUncorrectedVision()) {
            sb.append(" (reduced by uncorrected vision)");
        }
        sb.append("\n   DETERMINATION: ")
                .append(student.studentStatistics.getDetermination());
        sb.append("\n   PERCEPTION: ").append(student.studentStatistics.getEffectivePerception());
        if (student.studentStatistics.hasUncorrectedVision()) {
            sb.append(" (reduced by uncorrected vision)");
        }
        sb.append("\n   STRENGTH: ");
        sb.append(student.studentStatistics.getStrength()).append("\n   LUCK: ")
                .append(student.studentStatistics.getLuck()).append("\n");
        sb.append("   EXP: ").append(student.studentStatistics.getExperience()).append("\n");

        // Secondary stats
        sb.append("\nSecondary Stats:\n   Creativity: ")
                .append(student.studentStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(student.studentStatistics.getEmpathy());
        sb.append("\n   Adaptability: ").append(student.studentStatistics.getAdaptability());
        sb.append("\n   Initiative: ").append(student.studentStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(student.studentStatistics.getResilience());
        sb.append("\n   Curiosity: ").append(student.studentStatistics.getCuriosity());
        sb.append("\n   Responsibility: ").append(student.studentStatistics.getResponsibility());
        sb.append("\n   Open-Mindedness: ").append(student.studentStatistics.getOpenMindedness()).append("\n");

        // Physiological needs
        EntityState entityState = student.getEntityState();
        if (entityState != null) {
            sb.append("\nNeeds:\n");
            sb.append("   Hunger:         ").append(formatNeed(entityState.getHunger())).append("\n");
            sb.append("   Thirst:         ").append(formatNeed(entityState.getThirst())).append("\n");
            sb.append("   Bladder:        ").append(formatNeed(entityState.getBladder())).append("\n");
            sb.append("   Temperature:    ").append(formatNeed(entityState.getTemperature())).append("\n");
            sb.append("   Entertainment:  ").append(formatNeed(entityState.getEntertainment())).append("\n");
            sb.append("   Energy:         ").append(formatNeed(entityState.getEnergy())).append("\n");
            if (entityState.isAsleep()) {
                sb.append("   ** ").append(firstName).append(" is asleep! **\n");
            }
        }

        // Status effects
        sb.append("\nStatus Effects:\n");
        if (student.studentStatistics.hasVisionIssue()) {
            String visionDescription = student.studentStatistics.getVisionIssueDescription();
            sb.append("   ").append(firstName).append(" has ").append(visionDescription);
            if (student.studentStatistics.hasVisionCorrection()) {
                String correctionDesc = student.studentStatistics.getVisionCorrectionDescription();
                sb.append(", corrected with ").append(correctionDesc).append(".\n");
            } else {
                sb.append(" (uncorrected - Perception -")
                        .append(student.studentStatistics.getVisionPerceptionPenalty())
                        .append(", Agility -")
                        .append(student.studentStatistics.getVisionAgilityPenalty())
                        .append(").\n");
            }
        } else {
            sb.append("   ").append(firstName).append(" has normal vision.\n");
        }

        return sb.toString();
    }

    /**
     * Builds the combined inspection text for legacy single-text-area views.
     * Combines description and stats into one block.
     *
     * @param student the student to build inspection text for
     * @return the formatted inspection text as a String
     */
    private static String buildStudentInspectionText(Student student) {
        return buildStudentDescriptionText(student) + "\n" + buildStudentStatsText(student);
    }

    /**
     * Builds a readable academic progress report for the student inspector.
     */
    private static String buildAcademicProgressText(Student student) {
        StringBuilder sb = new StringBuilder();
        StudentAcademicRecord record = student.studentStatistics.getAcademicRecord();

        sb.append(student.studentName.getFullName()).append("\n=====================================\n");

        sb.append("Course Understanding\n-------------------------------------\n");
        if (record.getCourseProgressByKey().isEmpty()) {
            sb.append("No course understanding recorded yet.\n");
        } else {
            for (CourseProgress course : record.getCourseProgressByKey().values()) {
                sb.append(String.format("%-28s %6s  attention: %d%n",
                        course.getClassName(),
                        formatPercent(course.getUnderstanding()),
                        course.getAttentionPoints()));
                sb.append(String.format("   homework assigned: %d, completed: %d, missing: %d%n",
                        course.getAssignedHomework(),
                        course.getCompletedHomework(),
                        course.getMissingHomework()));
            }
        }

        sb.append("\nSkill Mastery\n-------------------------------------\n");
        for (Map.Entry<AcademicSkill, Double> entry : record.getSkillMastery().entrySet()) {
            sb.append(String.format("%-18s %6s%n",
                    formatSkillName(entry.getKey()),
                    formatPercent(entry.getValue())));
        }

        sb.append("\nAssignments\n-------------------------------------\n");
        if (record.getHomeworkAssignments().isEmpty()) {
            sb.append("No homework assigned yet.\n");
        } else {
            for (HomeworkAssignment homework : record.getHomeworkAssignments()) {
                sb.append(String.format("%-28s %s%n",
                        homework.getClassName(),
                        formatHomeworkStatus(homework)));
                sb.append(String.format("   effort: %d, problems: %d, progress: %s, assigned day: %d, due day: %d%n",
                        homework.getEffort(),
                        homework.getProblemCount(),
                        formatPercent(homework.getProgress()),
                        homework.getAssignedDay(),
                        homework.getDueDay()));
            }
        }

        return sb.toString();
    }

    private static String formatPercent(double value) {
        return NEED_FORMAT.format(value) + "%";
    }

    private static String formatSkillName(AcademicSkill skill) {
        String name = skill.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static String formatHomeworkStatus(HomeworkAssignment homework) {
        if (homework.isCompleted()) {
            return "Completed";
        }
        if (homework.isMissing()) {
            return "Missing";
        }
        return "Pending";
    }

    /**
     * Builds the schedule text organized by semester with periods in order.
     * Fall semester periods 1-4 are listed first, then Spring semester periods 1-4.
     *
     * @param student the student whose schedule to format
     * @return the formatted schedule string
     */
    private static String buildScheduleText(Student student) {
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        StringBuilder sb = new StringBuilder();

        if (schedule.isEmpty()) {
            sb.append("No classes scheduled.\n");
            return sb.toString();
        }

        // Separate blocks by semester and sort by period
        List<StudentBlock> fallBlocks = new java.util.ArrayList<>();
        List<StudentBlock> springBlocks = new java.util.ArrayList<>();

        for (StudentBlock block : schedule) {
            if ("Fall".equalsIgnoreCase(block.getSemester())) {
                fallBlocks.add(block);
            } else if ("Spring".equalsIgnoreCase(block.getSemester())) {
                springBlocks.add(block);
            }
        }

        // Sort each semester by block number
        fallBlocks.sort(java.util.Comparator.comparingInt(StudentBlock::getBlockNumber));
        springBlocks.sort(java.util.Comparator.comparingInt(StudentBlock::getBlockNumber));

        // Fall semester
        sb.append("===============================\n");
        sb.append("        FALL SEMESTER\n");
        sb.append("===============================\n");
        if (fallBlocks.isEmpty()) {
            sb.append("  (No fall classes)\n");
        } else {
            for (StudentBlock block : fallBlocks) {
                int displayPeriod = mapBlockToPeriod(block.getBlockNumber());
                sb.append("  Period ").append(displayPeriod).append(": ");
                sb.append(block.getClassName());
                if (block.getTeacher() != null) {
                    sb.append("\n           ").append(block.getTeacher().teacherName.getFirstName())
                            .append(" ").append(block.getTeacher().teacherName.getLastName());
                }
                if (block.getRoom() != null) {
                    sb.append("  [").append(block.getRoom().getRoomName()).append("]");
                }
                sb.append("\n");
            }
        }

        sb.append("\n");

        // Spring semester
        sb.append("===============================\n");
        sb.append("       SPRING SEMESTER\n");
        sb.append("===============================\n");
        if (springBlocks.isEmpty()) {
            sb.append("  (No spring classes)\n");
        } else {
            for (StudentBlock block : springBlocks) {
                int displayPeriod = mapBlockToPeriod(block.getBlockNumber());
                sb.append("  Period ").append(displayPeriod).append(": ");
                sb.append(block.getClassName());
                if (block.getTeacher() != null) {
                    sb.append("\n           ").append(block.getTeacher().teacherName.getFirstName())
                            .append(" ").append(block.getTeacher().teacherName.getLastName());
                }
                if (block.getRoom() != null) {
                    sb.append("  [").append(block.getRoom().getRoomName()).append("]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Maps block numbers to display periods.
     * In a 4x4 block schedule, block numbers 1-4 correspond directly to periods
     * 1-4.
     * Each period exists in both Fall and Spring semesters.
     */
    private static int mapBlockToPeriod(int blockNumber) {
        return blockNumber;
    }

    /**
     * Public accessor for the schedule panel, used by SchoolController's inspection
     * window.
     *
     * @param student the student whose schedule to display
     * @return a JPanel containing the schedule table
     */
    public static JPanel buildStudentSchedulePanel(Student student) {
        return buildSchedulePanel(student);
    }

    public static JScrollPane buildStudentAcademicProgressPanel(Student student) {
        JTextArea academicArea = new JTextArea(buildAcademicProgressText(student));
        academicArea.setEditable(false);
        academicArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        academicArea.setCaretPosition(0);
        return new JScrollPane(academicArea);
    }

    /**
     * Builds a schedule panel as a JTable organized by semester.
     * Columns: Period | Fall Class | Fall Teacher | Fall Room | Spring Class |
     * Spring Teacher | Spring Room
     *
     * @param student the student whose schedule to display
     * @return a JPanel containing the schedule table
     */
    private static JPanel buildSchedulePanel(Student student) {
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        JPanel panel = new JPanel(new BorderLayout(0, 8));

        // Index blocks by semester and period for table layout
        // Key: "Fall-1" or "Spring-3", Value: StudentBlock
        Map<String, StudentBlock> blockIndex = new HashMap<>();
        for (StudentBlock block : schedule) {
            int period = mapBlockToPeriod(block.getBlockNumber());
            String key = block.getSemester() + "-" + period;
            blockIndex.put(key, block);
        }

        // Build table data: 4 periods x 7 columns
        String[] columns = { "Period", "Fall Class", "Fall Teacher", "Fall Room",
                "Spring Class", "Spring Teacher", "Spring Room" };
        Object[][] data = new Object[4][7];

        for (int period = 1; period <= 4; period++) {
            data[period - 1][0] = "Period " + period;

            // Fall semester
            StudentBlock fallBlock = blockIndex.get("Fall-" + period);
            if (fallBlock != null) {
                data[period - 1][1] = fallBlock.getClassName();
                data[period - 1][2] = fallBlock.getTeacher() != null
                        ? fallBlock.getTeacher().teacherName.getFirstName() + " "
                                + fallBlock.getTeacher().teacherName.getLastName()
                        : "";
                data[period - 1][3] = fallBlock.getRoom() != null
                        ? fallBlock.getRoom().getRoomName()
                        : "";
            } else {
                data[period - 1][1] = "--";
                data[period - 1][2] = "";
                data[period - 1][3] = "";
            }

            // Spring semester
            StudentBlock springBlock = blockIndex.get("Spring-" + period);
            if (springBlock != null) {
                data[period - 1][4] = springBlock.getClassName();
                data[period - 1][5] = springBlock.getTeacher() != null
                        ? springBlock.getTeacher().teacherName.getFirstName() + " "
                                + springBlock.getTeacher().teacherName.getLastName()
                        : "";
                data[period - 1][6] = springBlock.getRoom() != null
                        ? springBlock.getRoom().getRoomName()
                        : "";
            } else {
                data[period - 1][4] = "--";
                data[period - 1][5] = "";
                data[period - 1][6] = "";
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60); // Period
        table.getColumnModel().getColumn(1).setPreferredWidth(160); // Fall Class
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Fall Teacher
        table.getColumnModel().getColumn(3).setPreferredWidth(80); // Fall Room
        table.getColumnModel().getColumn(4).setPreferredWidth(160); // Spring Class
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Spring Teacher
        table.getColumnModel().getColumn(6).setPreferredWidth(80); // Spring Room

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Also include the formatted text view below the table
        JTextArea scheduleText = new JTextArea(buildScheduleText(student));
        scheduleText.setEditable(false);
        scheduleText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane textScroll = new JScrollPane(scheduleText);
        textScroll.setPreferredSize(new Dimension(600, 180));
        panel.add(textScroll, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Displays student inspection information in a text area (legacy method).
     * For the full tabbed view with schedule, use {@link #inspectStudent(Student)}.
     *
     * @param student        the student to inspect
     * @param inspectionArea the text area to display the information in
     */
    public static void studentInspection(Student student, JTextArea inspectionArea) {
        inspectionArea.setText(buildStudentInspectionText(student));
    }

    /**
     * Updates a JTextArea with the student's physical description.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStudentDescriptionArea(Student student, JTextArea area) {
        area.setText(buildStudentDescriptionText(student));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the student's stats and status effects.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStudentStatsArea(Student student, JTextArea area) {
        area.setText(buildStudentStatsText(student));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the student's academic progress.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStudentAcademicArea(Student student, JTextArea area) {
        area.setText(buildAcademicProgressText(student));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the staff member's physical description.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStaffDescriptionArea(Staff staff, JTextArea area) {
        area.setText(buildStaffDescriptionText(staff));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the staff member's stats and status effects.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStaffStatsArea(Staff staff, JTextArea area) {
        area.setText(buildStaffStatsText(staff));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with the staff member's teaching schedule.
     * Used by SchoolController's tabbed inspection window.
     */
    public static void updateStaffScheduleArea(Staff staff, JTextArea area) {
        area.setText(buildStaffScheduleText(staff));
        area.setCaretPosition(0);
    }

    /**
     * Updates a JTextArea with cell phone information.
     * Shows phone details if the person owns one, or a message if they don't.
     * Used by SchoolController's tabbed inspection window.
     *
     * @param phone     the CellPhone object, or null if the person has no phone
     * @param ownerName the display name of the owner
     * @param area      the JTextArea to update
     */
    public static void updateCellPhoneArea(CellPhone phone, String ownerName, JTextArea area) {
        if (phone == null) {
            area.setText(ownerName + " does not own a cell phone.");
        } else {
            StringBuilder sb = new StringBuilder();
            String makeModel = joinMakeModel(phone.getMake(), phone.getModel());

            if (!makeModel.isEmpty()) {
                sb.append(makeModel).append("\n");
            } else {
                sb.append("Cell Phone\n");
            }
            sb.append("=====================================\n");

            appendPhoneDescription(sb, phone, makeModel);

            String condition = phone.getCondition();
            if (condition != null && !condition.isEmpty()) {
                sb.append("Overall condition: ").append(capitalize(condition)).append("\n");
            }

            sb.append("\nOwner:        ").append(phone.getOwnerName()).append("\n");
            sb.append("Number:       ").append(phone.getPhoneNumber()).append("\n");

            sb.append("\nData Plan\n-------------------------------------\n");
            sb.append("Minutes:      ").append(phone.getMinutePlan()).append("/month\n");
            sb.append("Texts:        ").append(phone.getTextsRemaining())
              .append(" / ").append(phone.getTextLimit()).append("\n");

            appendDecorationsSection(sb, phone);
            appendContactsSection(sb, phone);

            area.setText(sb.toString());
        }
        area.setCaretPosition(0);
    }

    /**
     * Builds the natural-language opener for a phone, mirroring the
     * prose-paragraph style used for student appearance descriptions.
     * Combines color + make/model into a single subject sentence
     * (e.g. {@code "A Dark Roast Black Motoroid G330."}) and then
     * appends each condition trait line as its own sentence so the
     * paragraph reads like an in-universe description rather than a
     * field dump.  Skipped entirely when there's nothing to say.
     *
     * @param sb        the target builder
     * @param phone     the phone being described
     * @param makeModel the pre-joined "make model" string (possibly empty)
     */
    private static void appendPhoneDescription(StringBuilder sb, CellPhone phone,
                                               String makeModel) {
        String color = phone.getColor();
        boolean hasColor = color != null && !color.isEmpty();
        boolean hasMakeModel = !makeModel.isEmpty();

        StringBuilder paragraph = new StringBuilder();
        if (hasColor || hasMakeModel) {
            String subject;
            if (hasColor && hasMakeModel) {
                subject = color + " " + makeModel;
            } else if (hasColor) {
                subject = color + " phone";
            } else {
                subject = makeModel;
            }
            paragraph.append(article(subject)).append(' ')
                    .append(subject).append('.');
        }

        java.util.List<String> traits = phone.getConditionTraits();
        if (traits != null && !traits.isEmpty()) {
            for (String trait : traits) {
                if (trait == null || trait.isEmpty()) {
                    continue;
                }
                if (paragraph.length() > 0) {
                    paragraph.append(' ');
                }
                paragraph.append(trait);
                // Source JSON ends each trait with a period, but guard
                // against future entries that omit it so the prose stays
                // well-punctuated.
                if (!endsWithSentenceTerminator(trait)) {
                    paragraph.append('.');
                }
            }
        }

        if (paragraph.length() > 0) {
            sb.append(paragraph).append('\n');
        }
    }

    private static String joinMakeModel(String make, String model) {
        boolean hasMake = make != null && !make.isEmpty();
        boolean hasModel = model != null && !model.isEmpty();
        if (hasMake && hasModel) {
            return make + " " + model;
        }
        if (hasMake) {
            return make;
        }
        if (hasModel) {
            return model;
        }
        return "";
    }

    /**
     * Picks {@code "A"} or {@code "An"} based on the first letter of the
     * given subject so the prose reads correctly for vowel-starting
     * colors / brands (e.g. "An Antique White ...").
     */
    private static String article(String subject) {
        if (subject == null || subject.isEmpty()) {
            return "A";
        }
        char c = Character.toLowerCase(subject.charAt(0));
        if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
            return "An";
        }
        return "A";
    }

    private static boolean endsWithSentenceTerminator(String s) {
        char last = s.charAt(s.length() - 1);
        return last == '.' || last == '!' || last == '?';
    }

    /**
     * Appends a "Decorations" block listing every slot that carries at
     * least one decoration on the phone, ordered by slot insertion
     * order.  When the phone is undecorated the section is omitted
     * entirely so plain phones don't gain visual clutter in the
     * inspector.  Decoration data is a separate concern from the
     * phone's intrinsic specs (price, battery, etc.) and is sourced
     * from the clique decoration system rather than the phone's own
     * fields.
     *
     * @param sb    the target builder
     * @param phone the phone whose decorations should be displayed
     */
    private static void appendDecorationsSection(StringBuilder sb, CellPhone phone) {
        if (!phone.hasDecorations()) {
            return;
        }
        sb.append("\nDecorations\n-------------------------------------\n");
        Map<String, java.util.List<Decoration>> grouped = phone.getDecorations();
        for (Map.Entry<String, java.util.List<Decoration>> entry : grouped.entrySet()) {
            java.util.List<Decoration> list = entry.getValue();
            if (list == null || list.isEmpty()) {
                continue;
            }
            String slotLabel = capitalize(entry.getKey());
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    names.append(", ");
                }
                names.append(list.get(i).getDisplayName());
            }
            sb.append(String.format("%-13s %s%n", slotLabel + ":", names.toString()));
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Appends a "Contacts" block to the phone display area listing every
     * saved contact, sorted alphabetically by display name. Each row is
     * formatted in two monospaced columns: name (left, padded) and phone
     * number (right). When the phone has no saved contacts a placeholder
     * line is shown instead.
     *
     * @param sb    the target StringBuilder receiving the formatted text
     * @param phone the phone whose contact list should be displayed
     */
    private static void appendContactsSection(StringBuilder sb, CellPhone phone) {
        sb.append("\nContacts (")
          .append(phone.getContactCount())
          .append(")\n-------------------------------------\n");

        java.util.List<CellPhone.Contact> contacts = phone.getContacts();
        if (contacts.isEmpty()) {
            sb.append("(no saved contacts)\n");
            return;
        }

        java.util.List<CellPhone.Contact> sorted = new java.util.ArrayList<>(contacts);
        sorted.sort((a, b) -> {
            String an = a.getName() == null ? "" : a.getName();
            String bn = b.getName() == null ? "" : b.getName();
            return an.compareToIgnoreCase(bn);
        });

        for (CellPhone.Contact contact : sorted) {
            String name = (contact.getName() == null || contact.getName().isEmpty())
                    ? "(unknown)" : contact.getName();
            String number = contact.getPhoneNumber() == null ? "" : contact.getPhoneNumber();
            sb.append(String.format("%-28s%s%n", name, number));
        }
    }

    /**
     * Updates a JTextArea with the student's recent action log.
     * Auto-scrolls to the bottom so the latest actions are visible.
     *
     * @param student the student whose log to display
     * @param area    the JTextArea to update
     */
    public static void updateActivityArea(Student student, JTextArea area) {
        if (student == null || student.getEntityState() == null) {
            area.setText("No activity data available.");
            return;
        }
        java.util.List<String> log = student.getEntityState().getActionLog();
        if (log.isEmpty()) {
            area.setText("No activity recorded yet.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String entry : log) {
            sb.append(entry).append("\n");
        }
        area.setText(sb.toString());
        area.setCaretPosition(area.getDocument().getLength());
    }

    /**
     * Opens a full student inspection dialog with tabbed panes.
     * Tab 1 (Description): Physical appearance, grade, birthday, family, history.
     * Tab 2 (Stats): Base stats, secondary stats, and status effects.
     * Tab 3 (Schedule): Class schedule organized by Fall/Spring semesters.
     *
     * @param student the student to inspect
     */
    public static void inspectStudent(Student student) {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Description tab
        JTextArea descArea = new JTextArea(buildStudentDescriptionText(student));
        descArea.setEditable(false);
        descArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        descArea.setCaretPosition(0);
        JScrollPane descScroll = new JScrollPane(descArea);
        tabbedPane.addTab("Description", descScroll);

        // Stats tab
        JTextArea statsArea = new JTextArea(buildStudentStatsText(student));
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statsArea.setCaretPosition(0);
        JScrollPane statsScroll = new JScrollPane(statsArea);
        tabbedPane.addTab("Stats", statsScroll);

        // Schedule tab
        JPanel schedulePanel = buildSchedulePanel(student);
        tabbedPane.addTab("Schedule", schedulePanel);

        // Academic tab
        JTextArea academicArea = new JTextArea(buildAcademicProgressText(student));
        academicArea.setEditable(false);
        academicArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        academicArea.setCaretPosition(0);
        JScrollPane academicScroll = new JScrollPane(academicArea);
        tabbedPane.addTab("Academic", academicScroll);

        // Create the dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Student: " + student.studentName.getFullName());
        dialog.setContentPane(tabbedPane);
        dialog.setModal(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * Builds the physical description text for a staff member.
     * Includes appearance, age, birthday, assignment, and experience.
     *
     * @param staff the staff member to describe
     * @return the formatted description text
     */
    private static String buildStaffDescriptionText(Staff staff) {
        StringBuilder sb = new StringBuilder();
        String firstName = staff.teacherName.getFirstName();
        String lastName = staff.teacherName.getLastName();
        String gender = staff.teacherStatistics.getGender().toLowerCase();
        String age = Integer.toString(staff.teacherStatistics.getAge());
        String hairColor = staff.teacherStatistics.getHairColor().toLowerCase();
        String hairLength = staff.teacherStatistics.getHairLength().toLowerCase();
        String hairType = staff.teacherStatistics.getHairType().toLowerCase();
        String eyeColor = staff.teacherStatistics.getEyeColor().toLowerCase();
        double height = staff.teacherStatistics.getHeight();
        LocalDate birth = staff.teacherStatistics.getBirthday();
        String assignment = staff.teacherStatistics.getStaffType().toString().toLowerCase();
        String yearsOfExperience = Integer.toString(staff.teacherStatistics.getYearsOfExperience());

        sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");
        sb.append(firstName).append(" is a ").append(age).append(" year-old ").append(gender).append(". ");

        if (hairLength.equalsIgnoreCase("bald")) {
            sb.append("They are bald and have ").append(eyeColor).append(" eyes. ");
        } else {
            sb.append("They have ").append(hairLength).append(", ").append(hairType).append(", ").append(hairColor)
                    .append(" hair and ").append(eyeColor).append(" eyes. ");
        }

        sb.append("They stand ").append(formatHeightFeetInches(height)).append(" tall.");
        if (staff.teacherStatistics.getHasGlasses() && !staff.teacherStatistics.getHasContacts()) {
            sb.append(" They wear glasses.");
        }
        String staffOutfitDesc = buildOutfitDescription(
                staff.teacherStatistics.getCurrentOutfit());
        if (staffOutfitDesc != null) {
            sb.append(" ").append(staffOutfitDesc);
        }
        sb.append("\n");

        sb.append(firstName).append(" was born on ").append(birth).append(".\n");
        sb.append("\nThey are assigned as: ").append(assignment).append("\n");
        sb.append("They have ").append(yearsOfExperience).append(" year(s) of teaching experience.\n");

        return sb.toString();
    }

    /**
     * Builds the stats and status effects text for a staff member.
     * Includes base stats, secondary stats, and active status effects.
     *
     * @param staff the staff member to build stats for
     * @return the formatted stats text
     */
    private static String buildStaffStatsText(Staff staff) {
        StringBuilder sb = new StringBuilder();
        String firstName = staff.teacherName.getFirstName();
        String lastName = staff.teacherName.getLastName();

        sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");

        // Base stats
        sb.append("Base Stats:\n   INTELLIGENCE: ")
                .append(staff.teacherStatistics.getIntelligence());
        sb.append("\n   CHARISMA: ").append(staff.teacherStatistics.getCharisma());
        sb.append("\n   AGILITY: ").append(staff.teacherStatistics.getAgility());
        sb.append("\n   DETERMINATION: ").append(staff.teacherStatistics.getDetermination());
        sb.append("\n   PERCEPTION: ").append(staff.teacherStatistics.getPerception());
        sb.append("\n   STRENGTH: ").append(staff.teacherStatistics.getStrength());
        sb.append("\n   LUCK: ").append(staff.teacherStatistics.getLuck()).append("\n");

        // Secondary stats
        sb.append("\nSecondary Stats:\n   Creativity: ")
                .append(staff.teacherStatistics.getCreativity());
        sb.append("\n   Empathy: ").append(staff.teacherStatistics.getEmpathy());
        sb.append("\n   Adaptability: ").append(staff.teacherStatistics.getAdaptability());
        sb.append("\n   Initiative: ").append(staff.teacherStatistics.getInitiative());
        sb.append("\n   Resilience: ").append(staff.teacherStatistics.getResilience());
        sb.append("\n   Curiosity: ").append(staff.teacherStatistics.getCuriosity());
        sb.append("\n   Responsibility: ").append(staff.teacherStatistics.getResponsibility());
        sb.append("\n   Open-Mindedness: ").append(staff.teacherStatistics.getOpenMindedness()).append("\n");

        // Physiological needs
        EntityState entityState = staff.getEntityState();
        if (entityState != null) {
            sb.append("\nNeeds:\n");
            sb.append("   Hunger:         ").append(formatNeed(entityState.getHunger())).append("\n");
            sb.append("   Thirst:         ").append(formatNeed(entityState.getThirst())).append("\n");
            sb.append("   Bladder:        ").append(formatNeed(entityState.getBladder())).append("\n");
            sb.append("   Temperature:    ").append(formatNeed(entityState.getTemperature())).append("\n");
            sb.append("   Entertainment:  ").append(formatNeed(entityState.getEntertainment())).append("\n");
            sb.append("   Energy:         ").append(formatNeed(entityState.getEnergy())).append("\n");
            if (entityState.isAsleep()) {
                sb.append("   ** ").append(firstName).append(" is asleep! **\n");
            }
        }

        // Status effects
        sb.append("\nStatus Effects:\n");
        if (staff.teacherStatistics.hasVisionIssue()) {
            String visionDescription = staff.teacherStatistics.getVisionIssueDescription();
            sb.append("   ").append(firstName).append(" has ").append(visionDescription);
            if (staff.teacherStatistics.hasVisionCorrection()) {
                String correctionDesc = staff.teacherStatistics.getVisionCorrectionDescription();
                sb.append(", corrected with ").append(correctionDesc).append(".\n");
            } else {
                sb.append(" (uncorrected).\n");
            }
        } else {
            sb.append("   ").append(firstName).append(" has normal vision.\n");
        }

        return sb.toString();
    }

    /**
     * Builds the schedule text for a staff member.
     * Lists each teaching block with semester, period, and class name.
     *
     * @param staff the staff member whose schedule to format
     * @return the formatted schedule text
     */
    private static String buildStaffScheduleText(Staff staff) {
        StringBuilder sb = new StringBuilder();
        String firstName = staff.teacherName.getFirstName();
        String lastName = staff.teacherName.getLastName();
        List<TeacherBlock> blocks = staff.teacherStatistics.getTeacherSchedule().getTeacherSchedule();

        sb.append(firstName).append(" ").append(lastName).append("\n=====================================\n");

        if (blocks.isEmpty()) {
            sb.append("No classes assigned.\n");
            return sb.toString();
        }

        List<TeacherBlock> fallBlocks = new java.util.ArrayList<>();
        List<TeacherBlock> springBlocks = new java.util.ArrayList<>();

        for (TeacherBlock block : blocks) {
            if ("Fall".equalsIgnoreCase(block.getSemester())) {
                fallBlocks.add(block);
            } else if ("Spring".equalsIgnoreCase(block.getSemester())) {
                springBlocks.add(block);
            }
        }

        fallBlocks.sort(java.util.Comparator.comparingInt(TeacherBlock::getBlockNumber));
        springBlocks.sort(java.util.Comparator.comparingInt(TeacherBlock::getBlockNumber));

        sb.append("===============================\n");
        sb.append("        FALL SEMESTER\n");
        sb.append("===============================\n");
        if (fallBlocks.isEmpty()) {
            sb.append("  (No fall classes)\n");
        } else {
            for (TeacherBlock block : fallBlocks) {
                sb.append("  Period ").append(block.getBlockNumber()).append(": ");
                sb.append(block.getClassName());
                List<Student> students = block.getClassPopulation();
                if (students != null) {
                    sb.append("  [").append(students.size()).append(" students]");
                }
                sb.append("\n");
            }
        }

        sb.append("\n");

        sb.append("===============================\n");
        sb.append("       SPRING SEMESTER\n");
        sb.append("===============================\n");
        if (springBlocks.isEmpty()) {
            sb.append("  (No spring classes)\n");
        } else {
            for (TeacherBlock block : springBlocks) {
                sb.append("  Period ").append(block.getBlockNumber()).append(": ");
                sb.append(block.getClassName());
                List<Student> students = block.getClassPopulation();
                if (students != null) {
                    sb.append("  [").append(students.size()).append(" students]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Displays staff inspection information in a text area (legacy method).
     * Combines description and stats into one view.
     * For the full tabbed view, use {@link #inspectStaff(Staff)}.
     *
     * @param staff          the staff member to inspect
     * @param inspectionArea the text area to display the information in
     */
    public static void staffInspection(Staff staff, JTextArea inspectionArea) {
        inspectionArea.setText(buildStaffDescriptionText(staff) + "\n" + buildStaffStatsText(staff));
    }

    /**
     * Opens a full staff inspection dialog with tabbed panes.
     * Tab 1 (Description): Physical appearance, birthday, assignment, experience.
     * Tab 2 (Stats): Base stats, secondary stats, and status effects.
     * Tab 3 (Schedule): Teaching schedule organized by Fall/Spring semesters.
     *
     * @param staff the staff member to inspect
     */
    public static void inspectStaff(Staff staff) {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Description tab
        JTextArea descArea = new JTextArea(buildStaffDescriptionText(staff));
        descArea.setEditable(false);
        descArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        descArea.setCaretPosition(0);
        JScrollPane descScroll = new JScrollPane(descArea);
        tabbedPane.addTab("Description", descScroll);

        // Stats tab
        JTextArea statsArea = new JTextArea(buildStaffStatsText(staff));
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statsArea.setCaretPosition(0);
        JScrollPane statsScroll = new JScrollPane(statsArea);
        tabbedPane.addTab("Stats", statsScroll);

        // Schedule tab
        JTextArea schedArea = new JTextArea(buildStaffScheduleText(staff));
        schedArea.setEditable(false);
        schedArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        schedArea.setCaretPosition(0);
        JScrollPane schedScroll = new JScrollPane(schedArea);
        tabbedPane.addTab("Schedule", schedScroll);

        // Create the dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Staff: " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName());
        dialog.setContentPane(tabbedPane);
        dialog.setModal(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static String gradeClassInspection(HashMap<Integer, Student> studentGradeClass) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Student> entry : studentGradeClass.entrySet()) {
            Student student = entry.getValue();
            sb.append(student.studentName.getFullName()).append("\n");
        }
        return sb.toString();
    }

    public static String staffListInspection(HashMap<Integer, Staff> staffHashMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Staff> entry : staffHashMap.entrySet()) {
            Staff staff = entry.getValue();
            sb.append(staff.teacherName.getFirstName()).append(" ").append(staff.teacherName.getLastName())
                    .append("\n");
        }
        return sb.toString();
    }

    public static void inspectRoom(Room room) {
        String roomName = room.getRoomName();
        StringBuilder roomDetails = new StringBuilder();
        List<Staff> staff = room.getAssignedStaff();
        int studentCap = room.getStudentCapacity();
        List<TeacherBlock> teacherBlocks = null;
        HashMap<Integer, Student[][]> seatingArrangements = room.getPeriodSeatingArrangement();
        JPanel panel = new JPanel();

        if (staff.isEmpty()) {
            roomDetails.append("There are no staff assigned to this room.\n");
        } else {
            TeacherSchedule teacherSchedule = staff.get(0).teacherStatistics.getTeacherSchedule();
            teacherBlocks = teacherSchedule.getTeacherSchedule();
        }

        roomDetails.append("Welcome to ").append(roomName).append("\n");
        roomDetails.append("The room contains the following staff:\n");
        for (Staff value : staff) {
            roomDetails.append(value.teacherName.getFirstName()).append(" ").append(value.teacherName.getLastName())
                    .append("\n");
        }
        roomDetails.append("It has a student capacity of ").append(studentCap).append("\n");
        if (room instanceof Classroom) {
            String abbrev = ((Classroom) room).getClassRoomType();
            roomDetails.append("It is a classroom of type: ").append(abbrev).append("\n");
        } else {
            roomDetails.append("It is a ").append(room.getRoomName()).append("\n");
        }

        JTextArea roomInfoArea = new JTextArea(roomDetails.toString());
        roomInfoArea.setEditable(false);

        // Create a panel for block buttons
        JPanel blockButtonPanel = new JPanel();
        blockButtonPanel.setLayout(new GridLayout(1, 8));
        JButton[] blockButtons = new JButton[8];

        Student[][] firstArrangement = seatingArrangements.values().iterator().next();
        String[] columnNames = new String[firstArrangement[0].length];
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = "Col " + (i + 1);
        }
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, firstArrangement.length);
        JTable studentTable = new JTable(tableModel);
        studentTable.setFillsViewportHeight(true);

        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                int col = studentTable.columnAtPoint(e.getPoint());
                if (!"Empty".equals(tableModel.getValueAt(row, col))) {
                    // Determine which block is currently displayed to find the right student
                    Student[][] currentSeats = seatingArrangements.get(1);
                    if (currentSeats != null && row < currentSeats.length
                            && col < currentSeats[0].length && currentSeats[row][col] != null) {
                        inspectStudent(currentSeats[row][col]);
                    }
                }
            }
        });

        // ActionListener for the buttons to update the seating arrangement
        ActionListener blockButtonListener = e -> {
            int blockNumber = Integer.parseInt(e.getActionCommand());
            Student[][] seats = seatingArrangements.get(blockNumber);
            if (seats != null) {
                for (int row = 0; row < seats.length; row++) {
                    for (int col = 0; col < seats[0].length; col++) {
                        if (seats[row][col] != null) {
                            tableModel.setValueAt(seats[row][col].studentName.getFullName(), row, col);
                        } else {
                            tableModel.setValueAt("Empty", row, col);
                        }
                    }
                }
            } else {
                // If no seating arrangement for this block, set all cells to "Empty"
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        tableModel.setValueAt("Empty", row, col);
                    }
                }
            }
            tableModel.fireTableDataChanged();
        };

        // Create and add buttons for each block
        for (int i = 0; i < 8; i++) {
            blockButtons[i] = new JButton("Block " + (i + 1));
            blockButtons[i].setActionCommand(String.valueOf(i + 1));
            blockButtons[i].addActionListener(blockButtonListener);
            blockButtonPanel.add(blockButtons[i]);
        }

        // Initialize with the first block
        blockButtons[0].doClick();

        JScrollPane studentScrollPane = new JScrollPane(studentTable);
        studentScrollPane.setPreferredSize(new Dimension(400, 200));

        JTextArea studentListArea = new JTextArea();
        studentListArea.setEditable(false);
        if (teacherBlocks != null && !teacherBlocks.isEmpty()) {
            for (TeacherBlock block : teacherBlocks) {
                studentListArea.append("Block: ");
                studentListArea.append(String.valueOf(block.getBlockNumber()));
                studentListArea.append("\n");
                studentListArea.append(block.getClassName());
                studentListArea.append("\n");
                studentListArea.append(block.getSemester());
                studentListArea.append("\n");
                List<Student> students = block.getClassPopulation();
                if (students != null) {
                    for (Student student : students) {
                        studentListArea.append(student.studentName.getFullName());
                        studentListArea.append("\n");
                    }
                } else {
                    studentListArea.append("Students are null!\n");
                }
            }
        } else {
            studentListArea.append("No teacher blocks or students assigned to this room.\n");
        }
        JScrollPane studentListScrollPane = new JScrollPane(studentListArea);
        studentListScrollPane.setPreferredSize(new Dimension(200, 200));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, studentListScrollPane, studentScrollPane);
        splitPane.setResizeWeight(0.3);

        panel.setLayout(new BorderLayout());
        panel.add(roomInfoArea, BorderLayout.NORTH);
        panel.add(blockButtonPanel, BorderLayout.SOUTH); // Buttons at the bottom
        panel.add(splitPane, BorderLayout.CENTER);

        // Create a resizable JDialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Room Details");
        dialog.setContentPane(panel);
        dialog.setModal(true);
        dialog.pack();
        dialog.setSize(800, 600); // Initial size
        dialog.setLocationRelativeTo(null); // Center on screen
        dialog.setVisible(true);
    }

    /**
     * Build a multi-line summary of the FM radio dial. Each station gets one
     * line with its frequency, nickname, call sign, format, and the song it
     * is currently broadcasting.
     *
     * @param radio the radio container; {@code null} or empty produces a
     *              short fallback string
     * @return inspection-friendly text
     */
    public static String radioInspection(Radio radio) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FM Radio Dial ===\n");
        if (radio == null) {
            sb.append("No radio stations are broadcasting.\n");
            return sb.toString();
        }
        List<RadioStation> stations = radio.getStations();
        if (stations.isEmpty()) {
            sb.append("No radio stations are broadcasting.\n");
            return sb.toString();
        }
        for (RadioStation station : stations) {
            sb.append(station.displayName());
            sb.append(" [").append(station.getFormat().displayLabel()).append("]");
            Song song = station.getCurrentSong();
            if (song != null) {
                sb.append("\n    Now playing: ").append(song);
            } else {
                sb.append("\n    Off air.");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
