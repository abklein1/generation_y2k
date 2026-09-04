package utility;

import config.SchoolFundingModel;
import entity.*;
import entity.Rooms.*;
import view.GameView;

import java.util.*;

import static constants.SchedulingConstants.*;

/**
 * Handles teacher block creation and room assignment.
 * Owns the class-size configuration (max, optimal, overcrowding flag).
 *
 * Extracted from EnhancedStudentScheduleAssigner (Phase 1b).
 */
public class TeacherBlockBuilder {

    // Funding-aware class size limits
    private static int currentMaxClassSize = MAX_CLASS_SIZE_RATIO;
    private static int currentOptimalClassSize = OPTIMAL_CLASS_SIZE_RATIO;
    private static boolean allowOvercrowding = false;

    // -------------------------------------------------- class size accessors

    public static int getCurrentMaxClassSize() {
        return currentMaxClassSize;
    }

    public static int getCurrentOptimalClassSize() {
        return currentOptimalClassSize;
    }

    public static boolean isOvercrowdingAllowed() {
        return allowOvercrowding;
    }

    /** Configures class size limits based on the school's funding model. */
    public static void configureClassSizesFromFunding(SchoolFundingModel fundingModel) {
        if (fundingModel == null) {
            fundingModel = new SchoolFundingModel();
        }
        currentMaxClassSize = fundingModel.getMaxClassSize();
        currentOptimalClassSize = fundingModel.getOptimalClassSize();
        allowOvercrowding = fundingModel.isAllowOvercrowding();

        GameLogger.logScheduling("Class size limits configured: optimal=" + currentOptimalClassSize +
                ", max=" + currentMaxClassSize +
                ", overcrowding=" + (allowOvercrowding ? "allowed" : "not allowed"));
    }

    // -------------------------------------------------- teaching staff check

    /** Checks if a staff type is a teaching position (not support staff). */
    public static boolean isTeachingStaffType(StaffType type) {
        return switch (type) {
            case ENGLISH, MATH, SCIENCE, HISTORY, LANGUAGES, PHYSICAL_ED,
                    VISUAL_ARTS, PERFORMING_ARTS, COMP_SCI, VOCATIONAL,
                    BUSINESS, CONSUMER_SCI, SUB ->
                true;
            default -> false;
        };
    }

    // -------------------------------------------------- room lookup

    /**
     * Gets the room assigned to a teacher, checking all room types.
     * More comprehensive than StandardSchool.getClassroomByStaff.
     */
    public static Room getTeacherRoom(Staff staff, StandardSchool standardSchool) {
        if (standardSchool == null) {
            return null;
        }

        String staffName = staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName();

        for (Classroom classroom : standardSchool.getClassrooms()) {
            for (Staff assignedStaff : classroom.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName) || classroom.getAssignedStaff().contains(staff)) {
                    return classroom;
                }
            }
        }

        for (ScienceLab lab : standardSchool.getScienceLabs()) {
            for (Staff assignedStaff : lab.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return lab;
            }
        }

        for (Gym gym : standardSchool.getGyms()) {
            for (Staff assignedStaff : gym.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return gym;
            }
        }

        for (ArtStudio studio : standardSchool.getArtStudios()) {
            for (Staff assignedStaff : studio.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return studio;
            }
        }

        for (MusicRoom room : standardSchool.getMusicRooms()) {
            for (Staff assignedStaff : room.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return room;
            }
        }

        for (DramaRoom room : standardSchool.getDramaRooms()) {
            for (Staff assignedStaff : room.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName) || room.getAssignedStaff().contains(staff))
                    return room;
            }
        }

        for (Auditorium room : standardSchool.getAuditoriums()) {
            for (Staff assignedStaff : room.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName) || room.getAssignedStaff().contains(staff))
                    return room;
            }
        }

        for (VocationalRoom room : standardSchool.getVocationalRooms()) {
            for (Staff assignedStaff : room.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return room;
            }
        }

        for (ComputerLab lab : standardSchool.getComputerLabs()) {
            for (Staff assignedStaff : lab.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return lab;
            }
        }

        for (AthleticField field : standardSchool.getAthleticFields()) {
            for (Staff assignedStaff : field.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return field;
            }
        }

        for (Portable portable : standardSchool.getPortables()) {
            for (Staff assignedStaff : portable.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return portable;
            }
        }

        for (LibraryR library : standardSchool.getLibraries()) {
            for (Staff assignedStaff : library.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return library;
            }
        }

        for (ConferenceRoom conferenceRoom : standardSchool.getConferenceRooms()) {
            for (Staff assignedStaff : conferenceRoom.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return conferenceRoom;
            }
        }

        for (Lunchroom lunchroom : standardSchool.getLunchrooms()) {
            for (Staff assignedStaff : lunchroom.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName))
                    return lunchroom;
            }
        }

        return null;
    }

    // -------------------------------------------------- room assignment

    /**
     * Ensures all teaching staff have room assignments.
     * Critical for demand-driven scheduling -- teachers without rooms can't teach.
     */
    public static void ensureTeachersHaveRooms(HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool, GameView view) {
        GameLogger.logScheduling("=== ENSURING TEACHERS HAVE ROOM ASSIGNMENTS ===");

        int teachersWithoutRooms = 0;
        int roomsAssigned = 0;

        List<Room> availableClassrooms = new ArrayList<>();
        for (Classroom classroom : standardSchool.getClassrooms()) {
            if (classroom.getAssignedStaff().isEmpty())
                availableClassrooms.add(classroom);
        }
        List<Room> availableScienceLabs = new ArrayList<>();
        for (ScienceLab lab : standardSchool.getScienceLabs()) {
            if (lab.getAssignedStaff().isEmpty())
                availableScienceLabs.add(lab);
        }
        List<Room> availableArtStudios = new ArrayList<>();
        for (ArtStudio studio : standardSchool.getArtStudios()) {
            if (studio.getAssignedStaff().isEmpty())
                availableArtStudios.add(studio);
        }
        List<Room> availableMusicRooms = new ArrayList<>();
        for (MusicRoom room : standardSchool.getMusicRooms()) {
            if (room.getAssignedStaff().isEmpty())
                availableMusicRooms.add(room);
        }
        List<Room> availableDramaRooms = new ArrayList<>();
        for (DramaRoom room : standardSchool.getDramaRooms()) {
            if (room.getAssignedStaff().isEmpty())
                availableDramaRooms.add(room);
        }
        List<Room> availableAuditoriums = new ArrayList<>();
        for (Auditorium room : standardSchool.getAuditoriums()) {
            if (room.getAssignedStaff().isEmpty())
                availableAuditoriums.add(room);
        }
        List<Room> availableGyms = new ArrayList<>();
        for (Gym gym : standardSchool.getGyms()) {
            if (gym.getAssignedStaff().isEmpty())
                availableGyms.add(gym);
        }
        List<Room> availableAthleticFields = new ArrayList<>();
        for (AthleticField field : standardSchool.getAthleticFields()) {
            if (field.getAssignedStaff().isEmpty())
                availableAthleticFields.add(field);
        }
        List<Room> availableVocationalRooms = new ArrayList<>();
        for (VocationalRoom room : standardSchool.getVocationalRooms()) {
            if (room.getAssignedStaff().isEmpty())
                availableVocationalRooms.add(room);
        }
        List<Room> availableComputerLabs = new ArrayList<>();
        for (ComputerLab lab : standardSchool.getComputerLabs()) {
            if (lab.getAssignedStaff().isEmpty())
                availableComputerLabs.add(lab);
        }
        List<Room> availablePortables = new ArrayList<>();
        for (Portable portable : standardSchool.getPortables()) {
            if (portable.getAssignedStaff().isEmpty())
                availablePortables.add(portable);
        }
        List<Room> availableLibraries = new ArrayList<>();
        for (LibraryR library : standardSchool.getLibraries()) {
            if (library.getAssignedStaff().isEmpty())
                availableLibraries.add(library);
        }
        List<Room> availableConferenceRooms = new ArrayList<>();
        for (ConferenceRoom conferenceRoom : standardSchool.getConferenceRooms()) {
            if (conferenceRoom.getAssignedStaff().isEmpty())
                availableConferenceRooms.add(conferenceRoom);
        }
        List<Room> availableLunchrooms = new ArrayList<>();
        for (Lunchroom lunchroom : standardSchool.getLunchrooms()) {
            if (lunchroom.getAssignedStaff().isEmpty())
                availableLunchrooms.add(lunchroom);
        }
        GameLogger.logScheduling("  Available portables for teacher assignment: " + availablePortables.size());

        List<Staff> orderedStaff = new ArrayList<>(staffHashMap.values());
        orderedStaff.sort(Comparator.comparingInt(staff -> getRoomAssignmentPriority(
                (StaffType) staff.teacherStatistics.getStaffType())));

        for (Staff staff : orderedStaff) {
            StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
            if (type == null || !isTeachingStaffType(type))
                continue;

            // Substitutes are a floating reserve: they get no dedicated classroom.
            // They receive a room only when covering (reassignSubToRoom) or when
            // repurposed to a real teaching type by recovery/reallocation.
            if (type == StaffType.SUB)
                continue;

            Room existingRoom = getTeacherRoom(staff, standardSchool);
            if (existingRoom != null)
                continue;

            teachersWithoutRooms++;

            Room assignedRoom = null;
            switch (type) {
                case SCIENCE:
                    if (!availableScienceLabs.isEmpty())
                        assignedRoom = availableScienceLabs.remove(0);
                    else if (!availableClassrooms.isEmpty())
                        assignedRoom = availableClassrooms.remove(0);
                    else if (!availablePortables.isEmpty())
                        assignedRoom = availablePortables.remove(0);
                    else if (RoomAssignment.canUseOverflowTeachingRoom(type))
                        assignedRoom = takeOverflowRoom(availableLibraries, availableConferenceRooms,
                                availableAuditoriums, availableLunchrooms);
                    break;
                case VISUAL_ARTS:
                    if (!availableArtStudios.isEmpty())
                        assignedRoom = availableArtStudios.remove(0);
                    else if (!availableClassrooms.isEmpty())
                        assignedRoom = availableClassrooms.remove(0);
                    else if (!availablePortables.isEmpty())
                        assignedRoom = availablePortables.remove(0);
                    break;
                case PERFORMING_ARTS:
                    if (!availableMusicRooms.isEmpty())
                        assignedRoom = availableMusicRooms.remove(0);
                    else if (!availableDramaRooms.isEmpty())
                        assignedRoom = availableDramaRooms.remove(0);
                    else if (!availableAuditoriums.isEmpty())
                        assignedRoom = availableAuditoriums.remove(0);
                    else if (!availableClassrooms.isEmpty())
                        assignedRoom = availableClassrooms.remove(0);
                    else if (!availablePortables.isEmpty())
                        assignedRoom = availablePortables.remove(0);
                    break;
                case PHYSICAL_ED:
                    if (!availableGyms.isEmpty())
                        assignedRoom = availableGyms.remove(0);
                    else if (!availableAthleticFields.isEmpty())
                        assignedRoom = availableAthleticFields.remove(0);
                    else if (!availableClassrooms.isEmpty())
                        assignedRoom = availableClassrooms.remove(0);
                    else if (!availablePortables.isEmpty())
                        assignedRoom = availablePortables.remove(0);
                    break;
                case VOCATIONAL:
                    if (!availableVocationalRooms.isEmpty())
                        assignedRoom = availableVocationalRooms.remove(0);
                    else if (!availableClassrooms.isEmpty())
                        assignedRoom = availableClassrooms.remove(0);
                    else if (!availablePortables.isEmpty())
                        assignedRoom = availablePortables.remove(0);
                    break;
                case COMP_SCI:
                    if (!availableComputerLabs.isEmpty())
                        assignedRoom = availableComputerLabs.remove(0);
                    else if (!availableClassrooms.isEmpty())
                        assignedRoom = availableClassrooms.remove(0);
                    else if (!availablePortables.isEmpty())
                        assignedRoom = availablePortables.remove(0);
                    break;
                default:
                    if (!availableClassrooms.isEmpty())
                        assignedRoom = availableClassrooms.remove(0);
                    else if (!availablePortables.isEmpty())
                        assignedRoom = availablePortables.remove(0);
                    else if (RoomAssignment.canUseOverflowTeachingRoom(type))
                        assignedRoom = takeOverflowRoom(availableLibraries, availableConferenceRooms,
                                availableAuditoriums, availableLunchrooms);
                    break;
            }

            // All room pools exhausted: expand the school with a new classroom so
            // the teacher is not left roomless (roomless teachers get no schedule
            // and are stuck without a location at simulation start).
            if (assignedRoom == null) {
                int before = standardSchool.getClassrooms().length;
                standardSchool.addClassrooms(1, view);
                Classroom[] expanded = standardSchool.getClassrooms();
                if (expanded.length > before) {
                    assignedRoom = expanded[before];
                    GameLogger.logScheduling("  EXPANSION: Added " + assignedRoom.getRoomName() +
                            " for roomless teacher " + staff.teacherName.getFirstName() + " " +
                            staff.teacherName.getLastName() + " (" + type + ")");
                }
            }

            if (assignedRoom != null) {
                RoomAssignment.assignTeacherToRoom(staff, assignedRoom);
                roomsAssigned++;
                GameLogger.logScheduling("  Assigned " + staff.teacherName.getFirstName() + " " +
                        staff.teacherName.getLastName() + " (" + type + ") to " + assignedRoom.getRoomName());
            } else {
                GameLogger.logScheduling("  WARNING: No room available for " + staff.teacherName.getFirstName() + " " +
                        staff.teacherName.getLastName() + " (" + type + ")");
            }
        }

        GameLogger.logScheduling("  Teachers needing rooms: " + teachersWithoutRooms);
        GameLogger.logScheduling("  Rooms assigned: " + roomsAssigned);
        GameLogger.logScheduling("  Remaining available classrooms: " + availableClassrooms.size());

        // Debug summary
        Map<StaffType, Integer> teachersByTypeTotal = new HashMap<>();
        Map<StaffType, Integer> teachersByTypeWithRooms = new HashMap<>();
        for (Staff staff : staffHashMap.values()) {
            StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
            if (type != null && isTeachingStaffType(type)) {
                teachersByTypeTotal.merge(type, 1, Integer::sum);
                if (getTeacherRoom(staff, standardSchool) != null) {
                    teachersByTypeWithRooms.merge(type, 1, Integer::sum);
                }
            }
        }
        GameLogger.logScheduling("=== TEACHER ROOM ASSIGNMENT SUMMARY ===");
        for (StaffType type : teachersByTypeTotal.keySet()) {
            int total = teachersByTypeTotal.getOrDefault(type, 0);
            int withRooms = teachersByTypeWithRooms.getOrDefault(type, 0);
            GameLogger.logScheduling("  " + type + ": " + withRooms + "/" + total + " have rooms");
        }
    }

    // -------------------------------------------------- block creation

    /**
     * Creates teacher blocks based on actual student demand.
     * Core of the demand-first approach.
     */
    public static void createDemandDrivenTeacherBlocks(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool,
            GameView view) {
        GameLogger.logScheduling("Creating teacher blocks based on student demand...");

        Map<String, SectionManager.StudentDemand> demandTracker = SectionManager.getDemandTracker();

        // Step 0: Ensure all teaching staff have room assignments
        if (standardSchool != null) {
            ensureTeachersHaveRooms(staffHashMap, standardSchool, view);
        } else {
            GameLogger.logScheduling("  WARNING: StandardSchool is null - skipping room assignments");
        }

        // Step 1: Calculate sections needed per class
        Map<String, Integer> sectionsNeeded = new HashMap<>();
        for (Map.Entry<String, SectionManager.StudentDemand> entry : demandTracker.entrySet()) {
            String className = entry.getKey();
            int demand = entry.getValue().totalDemand();
            int sections = (int) Math.ceil((double) demand / currentOptimalClassSize);
            sectionsNeeded.put(className, sections);
            if (sections > 0) {
                GameLogger.logScheduling("  " + className + ": " + demand + " students need " + sections + " sections");
            }
        }

        // Step 2: Clear existing teacher schedules for teaching staff
        for (Staff staff : staffHashMap.values()) {
            StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
            if (type != null && isTeachingStaffType(type)) {
                staff.teacherStatistics.getTeacherSchedule().getTeacherSchedule().clear();
            }
        }

        // Step 3: Group teachers by type (only those WITH room assignments when school
        // is available)
        Map<StaffType, List<Staff>> teachersByType = new HashMap<>();
        int teachersWithoutRooms = 0;
        for (Staff staff : staffHashMap.values()) {
            StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
            if (type != null && isTeachingStaffType(type)) {
                if (standardSchool == null) {
                    teachersByType.computeIfAbsent(type, k -> new ArrayList<>()).add(staff);
                } else {
                    Room room = getTeacherRoom(staff, standardSchool);
                    if (room != null) {
                        teachersByType.computeIfAbsent(type, k -> new ArrayList<>()).add(staff);
                    } else if (type == StaffType.SUB) {
                        // Substitutes intentionally have no dedicated room; they are
                        // not scheduled for demand-driven blocks.
                        continue;
                    } else {
                        teachersWithoutRooms++;
                        GameLogger.logScheduling("  WARNING: " + type + " teacher " + staff.teacherName.getFirstName() +
                                " " + staff.teacherName.getLastName() + " has no room - skipping");
                    }
                }
            }
        }
        GameLogger.logScheduling("  Teachers without rooms (skipped): " + teachersWithoutRooms);

        GameLogger.logScheduling("=== TEACHERS WITH ROOM ASSIGNMENTS ===");
        for (Map.Entry<StaffType, List<Staff>> entry : teachersByType.entrySet()) {
            GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue().size() + " teachers");
        }

        // Step 4: For each class, create teacher blocks distributed across all 4
        // periods
        Map<String, int[]> classSlotsUsed = new HashMap<>();

        // Global usage of each (period, semester) slot across ALL classes. Indexed
        // [period 0-3][semester 0=Fall,1=Spring]. Spreading sections by global usage
        // prevents every class from piling into the lowest-numbered periods, which at
        // low populations (few sections per class) would otherwise leave periods 3-4
        // empty and force student block conflicts.
        int[][] globalSlotUsage = new int[4][2];

        List<Map.Entry<String, Integer>> sortedClasses = new ArrayList<>(sectionsNeeded.entrySet());
        sortedClasses.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Integer> entry : sortedClasses) {
            String className = entry.getKey();
            int sectionsRequired = entry.getValue();
            if (sectionsRequired == 0)
                continue;

            int[] classSlots = new int[4];
            classSlotsUsed.put(className, classSlots);

            StaffType staffType = CurriculumRequirementsCalculator.mapClassToStaffType(className);
            List<Staff> qualifiedTeachers = teachersByType.getOrDefault(staffType, new ArrayList<>());

            if (qualifiedTeachers.isEmpty()) {
                GameLogger.logScheduling("  WARNING: No " + staffType + " teachers for " + className +
                        " (total " + staffType + " teachers in school: " +
                        teachersByType.getOrDefault(staffType, Collections.emptyList()).size() + ")");
                continue;
            }

            if (staffType == StaffType.LANGUAGES) {
                GameLogger.logScheduling("  DEBUG LANGUAGES: " + className + " needs " + sectionsRequired +
                        " sections, " + qualifiedTeachers.size() + " teachers available");
                for (Staff teacher : qualifiedTeachers) {
                    Room room = getTeacherRoom(teacher, standardSchool);
                    GameLogger.logScheduling("    - " + teacher.teacherName.getFirstName() + " " +
                            teacher.teacherName.getLastName() +
                            " room: " + (room != null ? room.getRoomName() : "NONE"));
                }
            }

            // Language sequence classes carry an implicit semester: level I is taught
            // in Fall and level II in Spring, so a freshman can take "X I" (Fall) then
            // "X II" (Spring) in the same year. Pin their sections to the correct
            // semester so the language-sequence assigner can find usable Fall/Spring
            // blocks (otherwise the generic spreader places them in either semester and
            // the strict Fall->Spring pairing fails).
            String requiredSemester = null;
            if (staffType == StaffType.LANGUAGES) {
                if (className.endsWith(" II")) {
                    requiredSemester = "Spring";
                } else if (className.endsWith(" I")) {
                    requiredSemester = "Fall";
                }
            }

            int sectionsCreated = 0;
            // Per-class usage of each (period, semester) slot, so a single class also
            // spreads its own sections across distinct slots before doubling up.
            int[][] classSlotUsage = new int[4][2];
            String[] semesters = { "Fall", "Spring" };

            // sectionsRequired is derived from the optimal class size, but actual
            // rooms are often smaller than optimal. Track the real capacity placed
            // and keep adding sections until it covers the student demand, so we
            // don't stop short while teachers still have free block slots.
            int demandForClass = demandTracker.containsKey(className)
                    ? demandTracker.get(className).totalDemand()
                    : 0;
            int capacityPlaced = 0;

            while (sectionsCreated < sectionsRequired || capacityPlaced < demandForClass) {
                // Order all 8 (period, semester) slots so we fill the slot this class
                // uses least first, breaking ties by the globally least-used slot. This
                // distributes every class across all 4 periods and both semesters
                // instead of concentrating them in periods 1-2.
                List<int[]> slotOrder = new ArrayList<>();
                for (int p = 0; p < 4; p++) {
                    for (int s = 0; s < 2; s++) {
                        slotOrder.add(new int[] { p, s });
                    }
                }
                final int[][] cls = classSlotUsage;
                final int[][] glb = globalSlotUsage;
                slotOrder.sort((a, b) -> {
                    int byClass = Integer.compare(cls[a[0]][a[1]], cls[b[0]][b[1]]);
                    if (byClass != 0) {
                        return byClass;
                    }
                    return Integer.compare(glb[a[0]][a[1]], glb[b[0]][b[1]]);
                });

                boolean placed = false;
                for (int[] slot : slotOrder) {
                    int slotIdx = slot[0];
                    int semIdx = slot[1];
                    String semester = semesters[semIdx];
                    if (requiredSemester != null && !semester.equals(requiredSemester)) {
                        continue;
                    }
                    Staff availableTeacher = findAvailableTeacher(qualifiedTeachers, slotIdx + 1, semester,
                            standardSchool);
                    if (availableTeacher == null) {
                        continue;
                    }
                    Room teacherRoom = getTeacherRoom(availableTeacher, standardSchool);
                    if (teacherRoom == null) {
                        continue;
                    }
                    TeacherBlock block = new TeacherBlock();
                    block.setClassName(className);
                    block.setBlockNumber(slotIdx + 1);
                    block.setSemester(semester);
                    block.setRoom(teacherRoom);
                    block.addClassPopulationBlock(teacherRoom.getStudentCapacity());
                    availableTeacher.teacherStatistics.addTeacherSchedule(block);
                    capacityPlaced += teacherRoom.getStudentCapacity();
                    classSlots[slotIdx]++;
                    classSlotUsage[slotIdx][semIdx]++;
                    globalSlotUsage[slotIdx][semIdx]++;
                    sectionsCreated++;
                    placed = true;
                    break;
                }

                // No teacher/room available in any slot; stop to avoid infinite loop.
                if (!placed) {
                    break;
                }
            }

            if (sectionsCreated < sectionsRequired) {
                GameLogger.logScheduling("  WARNING: Only created " + sectionsCreated + "/" + sectionsRequired +
                        " sections for " + className + " (not enough teachers/slots)");
            } else {
                StringBuilder dist = new StringBuilder();
                for (int i = 0; i < classSlots.length; i++) {
                    if (classSlots[i] > 0) {
                        dist.append("P").append(i + 1).append(":").append(classSlots[i]).append(" ");
                    }
                }
                GameLogger.logScheduling("  Created " + sectionsCreated + " sections for " + className + " ["
                        + dist.toString().trim() + "]");
            }
        }

        // Step 5: Print teacher utilization summary
        GameLogger.logScheduling("=== DEMAND-DRIVEN TEACHER UTILIZATION ===");
        for (Map.Entry<StaffType, List<Staff>> entry : teachersByType.entrySet()) {
            StaffType type = entry.getKey();
            List<Staff> teachers = entry.getValue();
            int totalBlocks = 0;
            for (Staff teacher : teachers) {
                totalBlocks += teacher.teacherStatistics.getTeacherSchedule().size();
            }
            double avgBlocks = teachers.isEmpty() ? 0 : (double) totalBlocks / teachers.size();
            GameLogger.logScheduling("  " + type + ": " + teachers.size() + " teachers, " +
                    totalBlocks + " blocks total, avg " + String.format("%.1f", avgBlocks) + " blocks/teacher");
        }
    }

    // -------------------------------------------------- private helpers

    /** Finds a teacher who is available to teach at the given slot and semester. */
    private static Staff findAvailableTeacher(List<Staff> teachers, int blockNumber, String semester,
            StandardSchool school) {
        for (Staff teacher : teachers) {
            Room room = getTeacherRoom(teacher, school);
            if (room == null)
                continue;

            boolean hasConflict = false;
            for (TeacherBlock existing : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                if (existing.getBlockNumber() == blockNumber && existing.getSemester().equals(semester)) {
                    hasConflict = true;
                    break;
                }
            }

            int semesterBlocks = 0;
            for (TeacherBlock existing : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                if (existing.getSemester().equals(semester)) {
                    semesterBlocks++;
                }
            }

            if (!hasConflict && semesterBlocks < 4) {
                return teacher;
            }
        }
        return null;
    }

    private static Room takeOverflowRoom(List<Room> availableLibraries,
            List<Room> availableConferenceRooms,
            List<Room> availableAuditoriums,
            List<Room> availableLunchrooms) {
        if (!availableLibraries.isEmpty())
            return availableLibraries.remove(0);
        if (!availableConferenceRooms.isEmpty())
            return availableConferenceRooms.remove(0);
        if (!availableAuditoriums.isEmpty())
            return availableAuditoriums.remove(0);
        if (!availableLunchrooms.isEmpty())
            return availableLunchrooms.remove(0);
        return null;
    }

    private static int getRoomAssignmentPriority(StaffType type) {
        if (type == null) {
            return Integer.MAX_VALUE;
        }
        return switch (type) {
            case PERFORMING_ARTS -> 1;
            case PHYSICAL_ED -> 2;
            case SCIENCE -> 3;
            case VISUAL_ARTS -> 4;
            case VOCATIONAL -> 5;
            case COMP_SCI -> 6;
            default -> 10;
        };
    }
}
