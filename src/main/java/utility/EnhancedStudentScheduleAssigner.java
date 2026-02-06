package utility;

import config.SchoolFundingModel;
import entity.*;
import entity.Rooms.*;
import view.GameView;

import java.util.*;
import java.util.stream.Collectors;

import static constants.SimConstants.*;
import static constants.SchedulingConstants.*;

/**
 * Enhanced StudentScheduleAssigner that incorporates minimum enrollment
 * requirements
 * and load balancing while preserving all existing student trait-based logic.
 * 
 * Now supports:
 * - Graduation requirements verification
 * - Overcrowding handling based on school funding level
 * - Dynamic class size limits based on funding model
 */
public class EnhancedStudentScheduleAssigner {

    // Optimization tracking
    private static final Map<String, List<ClassSection>> classSections = new HashMap<>();
    private static final Map<String, StudentDemand> demandTracker = new HashMap<>();
    private static final Map<String, Set<Student>> classWaitlists = new HashMap<>();

    // Funding-aware class size limits (can be adjusted per school)
    private static int currentMaxClassSize = MAX_CLASS_SIZE_RATIO;
    private static int currentOptimalClassSize = OPTIMAL_CLASS_SIZE_RATIO;
    private static boolean allowOvercrowding = false; // Disabled - sibling generation causes unpredictable enrollment

    // Reference to StudentPool for proper unassignment when students are returned to pool
    private static entity.StudentPool currentStudentPool = null;

    /**
     * Enhanced entry point that performs demand analysis before assignment.
     * This overload maintains backward compatibility with existing code.
     */
    public static void scheduleAllStudentsEnhanced(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool,
            GameView view) {
        scheduleAllStudentsEnhanced(studentHashMap, staffHashMap, standardSchool, view, null);
    }

    /**
     * Enhanced entry point that performs demand analysis before assignment.
     * This overload accepts a StudentPool to properly unassign students who are
     * returned to the pool due to missing requirements.
     * 
     * @param studentHashMap the students to schedule
     * @param staffHashMap the staff
     * @param standardSchool the school
     * @param view the game view
     * @param studentPool the student pool for proper unassignment (can be null for legacy mode)
     */
    public static void scheduleAllStudentsEnhanced(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool,
            GameView view,
            entity.StudentPool studentPool) {
        currentStudentPool = studentPool;
        GameLogger.logScheduling("Starting enhanced scheduling for " + studentHashMap.size() + " students");

        // Debug: Show all staff by type
        GameLogger.logScheduling("=== STAFF BY TYPE ===");
        Map<String, Integer> staffByType = new HashMap<>();
        for (Staff staff : staffHashMap.values()) {
            Enum<?> type = staff.teacherStatistics.getStaffType();
            String typeName = type != null ? type.toString() : "NULL";
            staffByType.merge(typeName, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : staffByType.entrySet()) {
            GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue() + " staff");
        }

        // Configure class sizes based on school funding model
        if (standardSchool != null) {
            configureClassSizesFromFunding(standardSchool.getFundingModel());
        }

        // *** CRITICAL FIX: Clear all existing student schedules to prevent duplicates
        // ***
        GameLogger.logScheduling("Clearing all existing student schedules...");
        for (Student student : studentHashMap.values()) {
            student.studentStatistics.getStudentSchedule().getClassSchedule().clear();
        }
        GameLogger.logScheduling("All schedules cleared - starting fresh assignment");

        // *** NEW DEMAND-FIRST APPROACH ***
        // Phase 0: Analyze student demand FIRST (before creating teacher blocks)
        GameLogger.logScheduling("=== PHASE 0: DEMAND-FIRST ANALYSIS ===");
        analyzeDemandWithTraits(studentHashMap, staffHashMap);

        // Phase 0.5: Create teacher blocks based on actual demand
        GameLogger.logScheduling("=== PHASE 0.5: DEMAND-DRIVEN TEACHER BLOCK CREATION ===");
        createDemandDrivenTeacherBlocks(studentHashMap, staffHashMap, standardSchool, view);

        // Phase 1: Re-analyze demands (demand already analyzed, just refresh sections)
        GameLogger.logScheduling("=== PHASE 1: REFRESHING DEMAND ANALYSIS ===");

        // Phase 2: Create optimal sections based on the new demand-driven blocks
        createOptimalSections(staffHashMap);

        // NEW Phase 2.5: Analyze resource shortages and reallocate substitutes
        analyzeAndReallocateResources(studentHashMap, staffHashMap);

        // Phase 3: Assign students using enhanced algorithm
        assignStudentsWithOptimization(studentHashMap, staffHashMap);

        // Phase 3.5: Optimize block assignments within subject areas (AFTER student
        // assignment)
        // This phase identifies empty/under-enrolled sections and reassigns teachers
        // to high-demand classes within their discipline
        GameLogger.logScheduling("=== PHASE 3.5: POST-ASSIGNMENT BLOCK OPTIMIZATION ===");
        optimizeBlockAssignmentsWithinSubjects(studentHashMap, staffHashMap);

        // Phase 4: Balance and optimize
        balanceClassSizes();

        // Phase 5: Handle waitlisted students
        processWaitlists(studentHashMap, staffHashMap);

        // *** NEW: Final duplicate detection check ***
        detectAndReportDuplicates(studentHashMap);

        // Phase 6: Verify graduation requirements
        verifyGraduationRequirements(studentHashMap, staffHashMap);

        printEnhancedStatistics();
    }

    /**
     * Creates teacher blocks based on actual student demand.
     * This is the core of the demand-first approach - we analyze what students need
     * and create teacher schedules to match that demand.
     * 
     * Key improvements:
     * - Ensures all teachers have room assignments before creating schedules
     * - Tracks per-class section distribution across time slots
     * - Ensures each class has sections spread across multiple periods
     * - Creates both Fall AND Spring sections for year-long availability
     */
    private static void createDemandDrivenTeacherBlocks(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool,
            GameView view) {
        GameLogger.logScheduling("Creating teacher blocks based on student demand...");

        // Step 0: Ensure all teaching staff have room assignments
        // Skip room assignment if standardSchool is null (backward compatibility mode)
        if (standardSchool != null) {
            ensureTeachersHaveRooms(staffHashMap, standardSchool, view);
        } else {
            GameLogger.logScheduling("  WARNING: StandardSchool is null - skipping room assignments");
        }

        // Step 1: Calculate sections needed per class
        Map<String, Integer> sectionsNeeded = new HashMap<>();
        for (Map.Entry<String, StudentDemand> entry : demandTracker.entrySet()) {
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
                // When standardSchool is null (backward compatibility), include all teachers
                // Otherwise, only include teachers who have a room assignment
                if (standardSchool == null) {
                    teachersByType.computeIfAbsent(type, k -> new ArrayList<>()).add(staff);
                } else {
                    Room room = getTeacherRoom(staff, standardSchool);
                    if (room != null) {
                        teachersByType.computeIfAbsent(type, k -> new ArrayList<>()).add(staff);
                    } else {
                        teachersWithoutRooms++;
                        GameLogger.logScheduling("  WARNING: " + type + " teacher " + staff.teacherName.getFirstName() +
                                " " + staff.teacherName.getLastName() + " has no room - skipping");
                    }
                }
            }
        }
        GameLogger.logScheduling("  Teachers without rooms (skipped): " + teachersWithoutRooms);

        // Report teacher counts by type
        GameLogger.logScheduling("=== TEACHERS WITH ROOM ASSIGNMENTS ===");
        for (Map.Entry<StaffType, List<Staff>> entry : teachersByType.entrySet()) {
            GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue().size() + " teachers");
        }

        // Step 4: For each class, create teacher blocks distributed across ALL time
        // slots
        // Track per-class section distribution to ensure coverage
        Map<String, int[]> classSlotsUsed = new HashMap<>(); // className -> slots used per period (0-7)

        // Sort classes by demand (highest first) to prioritize high-demand classes
        List<Map.Entry<String, Integer>> sortedClasses = new ArrayList<>(sectionsNeeded.entrySet());
        sortedClasses.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Integer> entry : sortedClasses) {
            String className = entry.getKey();
            int sectionsRequired = entry.getValue();

            if (sectionsRequired == 0)
                continue;

            // Initialize per-class slot tracking
            int[] classSlots = new int[8]; // Track how many sections of THIS class in each period
            classSlotsUsed.put(className, classSlots);

            // Determine which staff type teaches this class
            StaffType staffType = CurriculumRequirementsCalculator.mapClassToStaffType(className);
            List<Staff> qualifiedTeachers = teachersByType.getOrDefault(staffType, new ArrayList<>());

            if (qualifiedTeachers.isEmpty()) {
                GameLogger.logScheduling("  WARNING: No " + staffType + " teachers for " + className +
                        " (total " + staffType + " teachers in school: " +
                        teachersByType.getOrDefault(staffType, Collections.emptyList()).size() + ")");
                continue;
            }

            // Debug: For language classes, show detailed info
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

            // Create sections distributed across time slots
            // Goal: spread sections evenly so students have options in different periods
            int sectionsCreated = 0;
            int maxAttempts = qualifiedTeachers.size() * 32; // More attempts to find slots
            int attempts = 0;

            while (sectionsCreated < sectionsRequired && attempts < maxAttempts) {
                attempts++;

                // Find the time slot where THIS CLASS has the fewest sections
                int bestSlot = findLeastUsedSlotForClass(classSlots);

                // Alternate semesters: create Fall and Spring versions
                String[] semesters = { "Fall", "Spring" };

                for (String semester : semesters) {
                    if (sectionsCreated >= sectionsRequired)
                        break;

                    // Find a teacher who can teach at this slot/semester
                    Staff availableTeacher = findAvailableTeacher(qualifiedTeachers, bestSlot + 1, semester,
                            standardSchool);

                    if (availableTeacher != null) {
                        // Use our comprehensive room lookup instead of just getClassroomByStaff
                        Room teacherRoom = getTeacherRoom(availableTeacher, standardSchool);

                        if (teacherRoom != null) {
                            TeacherBlock block = new TeacherBlock();
                            block.setClassName(className);
                            block.setBlockNumber(bestSlot + 1);
                            block.setSemester(semester);
                            block.setRoom(teacherRoom);
                            block.addClassPopulationBlock(teacherRoom.getStudentCapacity());
                            availableTeacher.teacherStatistics.addTeacherSchedule(block);

                            classSlots[bestSlot]++;
                            sectionsCreated++;
                        }
                    }
                }
            }

            if (sectionsCreated < sectionsRequired) {
                GameLogger.logScheduling("  WARNING: Only created " + sectionsCreated + "/" + sectionsRequired +
                        " sections for " + className + " (not enough teachers/slots)");
            } else {
                // Show distribution across periods
                StringBuilder dist = new StringBuilder();
                for (int i = 0; i < 8; i++) {
                    if (classSlots[i] > 0) {
                        dist.append("P").append(i + 1).append(":").append(classSlots[i]).append(" ");
                    }
                }
                GameLogger.logScheduling("  ✓ Created " + sectionsCreated + " sections for " + className + " ["
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

    /**
     * Finds a teacher who is available to teach at the given slot and semester.
     */
    private static Staff findAvailableTeacher(List<Staff> teachers, int blockNumber, String semester,
            StandardSchool school) {
        for (Staff teacher : teachers) {
            // Check if teacher has a room (using comprehensive lookup)
            Room room = getTeacherRoom(teacher, school);
            if (room == null)
                continue;

            // Check if teacher is already teaching at this block/semester
            boolean hasConflict = false;
            for (TeacherBlock existing : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                if (existing.getBlockNumber() == blockNumber && existing.getSemester().equals(semester)) {
                    hasConflict = true;
                    break;
                }
            }

            // Also check if teacher is at capacity (max 8 blocks per semester)
            int semesterBlocks = 0;
            for (TeacherBlock existing : teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                if (existing.getSemester().equals(semester)) {
                    semesterBlocks++;
                }
            }

            if (!hasConflict && semesterBlocks < 8) {
                return teacher;
            }
        }
        return null;
    }

    /**
     * Finds the time slot with the fewest sections for a specific class.
     */
    private static int findLeastUsedSlotForClass(int[] classSlots) {
        int minSlot = 0;
        int minCount = classSlots[0];

        for (int i = 1; i < classSlots.length; i++) {
            if (classSlots[i] < minCount) {
                minCount = classSlots[i];
                minSlot = i;
            }
        }

        return minSlot;
    }

    /**
     * Checks if a staff type is a teaching position (not support staff).
     */
    private static boolean isTeachingStaffType(StaffType type) {
        return switch (type) {
            case ENGLISH, MATH, SCIENCE, HISTORY, LANGUAGES, PHYSICAL_ED,
                    VISUAL_ARTS, PERFORMING_ARTS, COMP_SCI, VOCATIONAL,
                    BUSINESS, CONSUMER_SCI, SUB ->
                true;
            default -> false;
        };
    }

    /**
     * Ensures all teaching staff have room assignments.
     * This is critical for demand-driven scheduling - teachers without rooms can't
     * teach.
     */
    private static void ensureTeachersHaveRooms(HashMap<Integer, Staff> staffHashMap,
            StandardSchool standardSchool,
            GameView view) {
        GameLogger.logScheduling("=== ENSURING TEACHERS HAVE ROOM ASSIGNMENTS ===");

        int teachersWithoutRooms = 0;
        int roomsAssigned = 0;

        // Get all available rooms by type
        List<Room> availableClassrooms = new ArrayList<>();
        for (Classroom classroom : standardSchool.getClassrooms()) {
            if (classroom.getAssignedStaff().isEmpty()) {
                availableClassrooms.add(classroom);
            }
        }

        List<Room> availableScienceLabs = new ArrayList<>();
        for (ScienceLab lab : standardSchool.getScienceLabs()) {
            if (lab.getAssignedStaff().isEmpty()) {
                availableScienceLabs.add(lab);
            }
        }

        List<Room> availableArtStudios = new ArrayList<>();
        for (ArtStudio studio : standardSchool.getArtStudios()) {
            if (studio.getAssignedStaff().isEmpty()) {
                availableArtStudios.add(studio);
            }
        }

        List<Room> availableMusicRooms = new ArrayList<>();
        for (MusicRoom room : standardSchool.getMusicRooms()) {
            if (room.getAssignedStaff().isEmpty()) {
                availableMusicRooms.add(room);
            }
        }

        List<Room> availableDramaRooms = new ArrayList<>();
        for (DramaRoom room : standardSchool.getDramaRooms()) {
            if (room.getAssignedStaff().isEmpty()) {
                availableDramaRooms.add(room);
            }
        }

        List<Room> availableGyms = new ArrayList<>();
        for (Gym gym : standardSchool.getGyms()) {
            if (gym.getAssignedStaff().isEmpty()) {
                availableGyms.add(gym);
            }
        }

        List<Room> availableVocationalRooms = new ArrayList<>();
        for (VocationalRoom room : standardSchool.getVocationalRooms()) {
            if (room.getAssignedStaff().isEmpty()) {
                availableVocationalRooms.add(room);
            }
        }

        List<Room> availableComputerLabs = new ArrayList<>();
        for (ComputerLab lab : standardSchool.getComputerLabs()) {
            if (lab.getAssignedStaff().isEmpty()) {
                availableComputerLabs.add(lab);
            }
        }

        // Portables serve as overflow classrooms for any subject type
        List<Room> availablePortables = new ArrayList<>();
        for (Portable portable : standardSchool.getPortables()) {
            if (portable.getAssignedStaff().isEmpty()) {
                availablePortables.add(portable);
            }
        }
        GameLogger.logScheduling("  Available portables for teacher assignment: " + availablePortables.size());

        // Check each teaching staff member
        for (Staff staff : staffHashMap.values()) {
            StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
            if (type == null || !isTeachingStaffType(type)) {
                continue;
            }

            // Check if this teacher already has a room
            Room existingRoom = getTeacherRoom(staff, standardSchool);
            if (existingRoom != null) {
                continue; // Already has a room
            }

            teachersWithoutRooms++;

            // Try to assign a room based on staff type
            // Portables serve as universal overflow for any type when primary rooms are exhausted
            Room assignedRoom = null;
            switch (type) {
                case SCIENCE:
                    if (!availableScienceLabs.isEmpty()) {
                        assignedRoom = availableScienceLabs.remove(0);
                    } else if (!availableClassrooms.isEmpty()) {
                        assignedRoom = availableClassrooms.remove(0);
                    } else if (!availablePortables.isEmpty()) {
                        assignedRoom = availablePortables.remove(0);
                    }
                    break;
                case VISUAL_ARTS:
                    if (!availableArtStudios.isEmpty()) {
                        assignedRoom = availableArtStudios.remove(0);
                    } else if (!availableClassrooms.isEmpty()) {
                        assignedRoom = availableClassrooms.remove(0);
                    } else if (!availablePortables.isEmpty()) {
                        assignedRoom = availablePortables.remove(0);
                    }
                    break;
                case PERFORMING_ARTS:
                    if (!availableMusicRooms.isEmpty()) {
                        assignedRoom = availableMusicRooms.remove(0);
                    } else if (!availableDramaRooms.isEmpty()) {
                        assignedRoom = availableDramaRooms.remove(0);
                    } else if (!availableClassrooms.isEmpty()) {
                        assignedRoom = availableClassrooms.remove(0);
                    } else if (!availablePortables.isEmpty()) {
                        assignedRoom = availablePortables.remove(0);
                    }
                    break;
                case PHYSICAL_ED:
                    if (!availableGyms.isEmpty()) {
                        assignedRoom = availableGyms.remove(0);
                    }
                    // PE teachers can't teach in portables (no gym equipment)
                    break;
                case VOCATIONAL:
                    if (!availableVocationalRooms.isEmpty()) {
                        assignedRoom = availableVocationalRooms.remove(0);
                    } else if (!availableClassrooms.isEmpty()) {
                        assignedRoom = availableClassrooms.remove(0);
                    } else if (!availablePortables.isEmpty()) {
                        assignedRoom = availablePortables.remove(0);
                    }
                    break;
                case COMP_SCI:
                    if (!availableComputerLabs.isEmpty()) {
                        assignedRoom = availableComputerLabs.remove(0);
                    } else if (!availableClassrooms.isEmpty()) {
                        assignedRoom = availableClassrooms.remove(0);
                    } else if (!availablePortables.isEmpty()) {
                        assignedRoom = availablePortables.remove(0);
                    }
                    break;
                default:
                    // ENGLISH, MATH, HISTORY, LANGUAGES, BUSINESS, etc.
                    if (!availableClassrooms.isEmpty()) {
                        assignedRoom = availableClassrooms.remove(0);
                    } else if (!availablePortables.isEmpty()) {
                        assignedRoom = availablePortables.remove(0);
                    }
                    break;
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

        // Debug: Count teachers by type and room status
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

    /**
     * Gets the room assigned to a teacher, checking all room types.
     * This is a more comprehensive version of StandardSchool.getClassroomByStaff
     */
    private static Room getTeacherRoom(Staff staff, StandardSchool standardSchool) {
        // Return null if school is not available (backward compatibility mode)
        if (standardSchool == null) {
            return null;
        }

        String staffName = staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName();

        // Check classrooms - use name matching as fallback for object identity issues
        for (Classroom classroom : standardSchool.getClassrooms()) {
            for (Staff assignedStaff : classroom.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName) || classroom.getAssignedStaff().contains(staff)) {
                    return classroom;
                }
            }
        }

        // Check science labs
        for (ScienceLab lab : standardSchool.getScienceLabs()) {
            for (Staff assignedStaff : lab.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return lab;
                }
            }
        }

        // Check gyms
        for (Gym gym : standardSchool.getGyms()) {
            for (Staff assignedStaff : gym.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return gym;
                }
            }
        }

        // Check art studios
        for (ArtStudio studio : standardSchool.getArtStudios()) {
            for (Staff assignedStaff : studio.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return studio;
                }
            }
        }

        // Check music rooms
        for (MusicRoom room : standardSchool.getMusicRooms()) {
            for (Staff assignedStaff : room.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return room;
                }
            }
        }

        // Check drama rooms
        for (DramaRoom room : standardSchool.getDramaRooms()) {
            for (Staff assignedStaff : room.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return room;
                }
            }
        }

        // Check vocational rooms
        for (VocationalRoom room : standardSchool.getVocationalRooms()) {
            for (Staff assignedStaff : room.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return room;
                }
            }
        }

        // Check computer labs
        for (ComputerLab lab : standardSchool.getComputerLabs()) {
            for (Staff assignedStaff : lab.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return lab;
                }
            }
        }

        // Check athletic fields
        for (AthleticField field : standardSchool.getAthleticFields()) {
            for (Staff assignedStaff : field.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return field;
                }
            }
        }

        // Check portable classrooms
        for (Portable portable : standardSchool.getPortables()) {
            for (Staff assignedStaff : portable.getAssignedStaff()) {
                String assignedName = assignedStaff.teacherName.getFirstName() + " "
                        + assignedStaff.teacherName.getLastName();
                if (staffName.equals(assignedName)) {
                    return portable;
                }
            }
        }

        return null; // No room found
    }

    /**
     * Configures class size limits based on the school's funding model.
     */
    private static void configureClassSizesFromFunding(SchoolFundingModel fundingModel) {
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

    /**
     * Verifies that students have all required classes for graduation.
     * Reports students with missing requirements and attempts to fix schedules.
     * 
     * Students are retained in school even with partial schedules when possible.
     * Only students missing a critical proportion of their required classes
     * (more than half) are returned to the pool. Students with minor gaps
     * (1-2 missing classes) are kept enrolled and flagged for future resolution.
     */
    private static void verifyGraduationRequirements(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("\n=== Verifying Graduation Requirements ===");

        Map<String, List<String>> missingByGrade = new HashMap<>();
        missingByGrade.put("Freshman", new ArrayList<>());
        missingByGrade.put("Sophomore", new ArrayList<>());
        missingByGrade.put("Junior", new ArrayList<>());
        missingByGrade.put("Senior", new ArrayList<>());

        // Track students who still have missing requirements after all attempts
        List<Student> studentsToRemove = new ArrayList<>();
        Map<Student, List<String>> studentMissingClasses = new HashMap<>();
        int studentsRetainedWithGaps = 0;

        int studentsWithMissingReqs = 0;
        int totalMissingClasses = 0;

        for (Student student : studentHashMap.values()) {
            String grade = student.studentStatistics.getGradeLevel();
            List<String> required = getRequiredClassesForGrade(grade);
            List<String> scheduled = getScheduledClassNames(student);

            List<String> missing = new ArrayList<>();
            for (String req : required) {
                if (!hasRequiredClass(scheduled, req)) {
                    missing.add(req);
                }
            }

            if (!missing.isEmpty()) {
                studentsWithMissingReqs++;
                totalMissingClasses += missing.size();

                // Try to schedule missing classes
                List<String> stillMissing = new ArrayList<>();
                for (String missingClass : missing) {
                    boolean scheduled2 = attemptToScheduleMissingClass(student, missingClass, staffHashMap);
                    if (!scheduled2) {
                        stillMissing.add(missingClass);
                        missingByGrade.get(grade).add(
                                student.studentName.getFirstName() + " " +
                                        student.studentName.getLastName() + " missing " + missingClass);
                    }
                }

                // Only remove students missing MORE than half their required classes.
                // Students with minor schedule gaps (1-2 missing) are retained.
                if (!stillMissing.isEmpty()) {
                    double missingRatio = (double) stillMissing.size() / required.size();
                    if (missingRatio > 0.5) {
                        studentsToRemove.add(student);
                        studentMissingClasses.put(student, stillMissing);
                    } else {
                        studentsRetainedWithGaps++;
                        GameLogger.logScheduling("  Retaining " + student.studentName.getFirstName() + " " +
                                student.studentName.getLastName() + " (" + grade + ") with " +
                                stillMissing.size() + "/" + required.size() + " missing: " +
                                String.join(", ", stillMissing));
                    }
                }
            }
        }

        GameLogger.logScheduling("Students with missing requirements: " + studentsWithMissingReqs);
        GameLogger.logScheduling("Total missing class assignments: " + totalMissingClasses);
        GameLogger.logScheduling("Students retained with partial schedules: " + studentsRetainedWithGaps);

        for (Map.Entry<String, List<String>> entry : missingByGrade.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                GameLogger.logScheduling(entry.getKey() + " issues (" + entry.getValue().size() + "):");
                // Only print first 5 to avoid flooding output
                int count = 0;
                for (String issue : entry.getValue()) {
                    if (count < 5) {
                        GameLogger.logScheduling("  - " + issue);
                    }
                    count++;
                }
                if (count > 5) {
                    GameLogger.logScheduling("  ... and " + (count - 5) + " more");
                }
            }
        }

        // Only return students with critically incomplete schedules to the pool
        if (!studentsToRemove.isEmpty()) {
            GameLogger.logScheduling("Returning " + studentsToRemove.size() +
                    " students with critically incomplete schedules (>50% missing)");
            returnStudentsToPool(studentsToRemove, studentMissingClasses, studentHashMap);
        } else {
            GameLogger.logScheduling("No students need to be removed - all retained with current schedules");
        }
    }

    /**
     * Returns students with missing requirements to the pool.
     * Updates sibling relationships so removed students are marked as "not in school".
     * These students can potentially be used later when more class sections become available.
     *
     * @param studentsToRemove     List of students to remove from enrollment
     * @param studentMissingClasses Map of students to their missing class requirements
     * @param studentHashMap       The student enrollment map
     */
    private static void returnStudentsToPool(List<Student> studentsToRemove,
            Map<Student, List<String>> studentMissingClasses,
            HashMap<Integer, Student> studentHashMap) {

        GameLogger.logScheduling("\n=== Returning Students with Unfulfilled Requirements to Pool ===");
        GameLogger.logScheduling("Students being returned to pool: " + studentsToRemove.size());

        Map<String, Integer> removedByGrade = new HashMap<>();
        removedByGrade.put("Freshman", 0);
        removedByGrade.put("Sophomore", 0);
        removedByGrade.put("Junior", 0);
        removedByGrade.put("Senior", 0);

        for (Student student : studentsToRemove) {
            String grade = student.studentStatistics.getGradeLevel();
            List<String> missing = studentMissingClasses.get(student);

            // Update sibling relationships
            updateSiblingRelationshipsForRemovedStudent(student);

            // Mark student as not in high school
            student.setInHighSchool(false);

            // Clear the student's schedule since they're being removed
            student.studentStatistics.getStudentSchedule().getClassSchedule().clear();

            // Remove from sections they were enrolled in
            removeStudentFromAllSections(student);

            // Remove from the studentHashMap
            Integer keyToRemove = null;
            for (Map.Entry<Integer, Student> entry : studentHashMap.entrySet()) {
                if (entry.getValue().equals(student)) {
                    keyToRemove = entry.getKey();
                    break;
                }
            }
            if (keyToRemove != null) {
                studentHashMap.remove(keyToRemove);
            }

            // Unassign from StudentPool if available (critical for accurate population counts)
            if (currentStudentPool != null) {
                currentStudentPool.unassignFromSchool(student);
            }

            // Track counts by grade
            removedByGrade.merge(grade, 1, Integer::sum);

            // Log first few removals with details
            if (removedByGrade.values().stream().mapToInt(Integer::intValue).sum() <= 10) {
                GameLogger.logScheduling("  Returned to pool: " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " (" + grade + ") - missing: " +
                        String.join(", ", missing));
            }
        }

        // Summary by grade
        GameLogger.logScheduling("\nStudents returned to pool by grade:");
        for (Map.Entry<String, Integer> entry : removedByGrade.entrySet()) {
            if (entry.getValue() > 0) {
                GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue());
            }
        }
        GameLogger.logScheduling("Total students returned to pool: " + studentsToRemove.size());
        GameLogger.logScheduling("These students are marked as 'not in high school' and can be re-enrolled later");
        GameLogger.logScheduling("Remaining enrolled students: " + studentHashMap.size());
    }

    /**
     * Updates sibling relationships when a student is removed from school.
     * Moves the student from siblingsInSchool to siblingsNotInSchool for all their siblings.
     * Note: StudentStatistics.addSiblingsNotInSchool() automatically prevents duplicates.
     */
    private static void updateSiblingRelationshipsForRemovedStudent(Student removedStudent) {
        // Get all siblings who are currently in school
        ArrayList<Student> siblingsInSchool = removedStudent.studentStatistics.getSiblingsInSchool();

        // For each sibling in school, update their sibling lists
        for (Student sibling : siblingsInSchool) {
            // Remove the student from their siblingsInSchool list
            sibling.studentStatistics.removeSiblingsInSchool(removedStudent);

            // Add the student to their siblingsNotInSchool list (duplicates prevented at source)
            sibling.studentStatistics.addSiblingsNotInSchool(removedStudent);
        }

        // Also update siblings not in school (they should know this student is now also not in school)
        ArrayList<Student> siblingsNotInSchool = removedStudent.studentStatistics.getSiblingsNotInSchool();
        for (Student sibling : siblingsNotInSchool) {
            // If the removed student was in their siblingsInSchool list, move them
            if (sibling.studentStatistics.getSiblingsInSchool().contains(removedStudent)) {
                sibling.studentStatistics.removeSiblingsInSchool(removedStudent);
                sibling.studentStatistics.addSiblingsNotInSchool(removedStudent);
            }
        }
    }

    /**
     * Removes a student from all class sections they were enrolled in.
     */
    private static void removeStudentFromAllSections(Student student) {
        for (Map.Entry<String, List<ClassSection>> entry : classSections.entrySet()) {
            for (ClassSection section : entry.getValue()) {
                if (section.getEnrolledStudents().contains(student)) {
                    section.getEnrolledStudents().remove(student);

                    // Also remove from the TeacherBlock's class population
                    TeacherBlock block = section.getTeacherBlock();
                    if (block.getClassPopulation() != null) {
                        block.getClassPopulation().remove(student);
                    }
                }
            }
        }
    }

    /**
     * Gets the required classes for a specific grade level.
     */
    private static List<String> getRequiredClassesForGrade(String grade) {
        List<String> required = new ArrayList<>();

        switch (grade) {
            case "Freshman":
                required.add("English I");
                required.add("Math"); // Algebra I, Geometry, or Fundamentals
                required.add("Biology");
                required.add("History"); // World Geography or AP Human Geography
                required.add("Health");
                break;
            case "Sophomore":
                required.add("English II");
                required.add("Math"); // Algebra, Geometry, etc.
                required.add("Science"); // Chemistry, Physical Science, etc.
                required.add("History"); // World History or AP
                break;
            case "Junior":
                required.add("English"); // English III or AP
                required.add("Math"); // Algebra II, Precalc, etc.
                required.add("Science");
                required.add("US History"); // US History or AP
                break;
            case "Senior":
                required.add("English"); // English IV or AP
                required.add("Government"); // US Government or AP
                break;
        }

        return required;
    }

    /**
     * Checks if the scheduled classes include the required class (or equivalent).
     */
    private static boolean hasRequiredClass(List<String> scheduled, String required) {
        String reqLower = required.toLowerCase();

        for (String className : scheduled) {
            String classLower = className.toLowerCase();

            // Direct match
            if (classLower.contains(reqLower)) {
                return true;
            }

            // Handle equivalents
            if (reqLower.equals("math")) {
                if (classLower.contains("algebra") || classLower.contains("geometry") ||
                        classLower.contains("calculus") || classLower.contains("precalculus") ||
                        classLower.contains("trigonometry") || classLower.contains("statistics") ||
                        classLower.contains("fundamentals of math")) {
                    return true;
                }
            }

            if (reqLower.equals("science")) {
                if (classLower.contains("biology") || classLower.contains("chemistry") ||
                        classLower.contains("physics") || classLower.contains("anatomy") ||
                        classLower.contains("environmental") || classLower.contains("earth")) {
                    return true;
                }
            }

            if (reqLower.equals("history")) {
                if (classLower.contains("history") || classLower.contains("geography") ||
                        classLower.contains("government") || classLower.contains("civics")) {
                    return true;
                }
            }

            if (reqLower.equals("english")) {
                if (classLower.contains("english") || classLower.contains("literature") ||
                        classLower.contains("composition")) {
                    return true;
                }
            }

            if (reqLower.equals("us history")) {
                if (classLower.contains("us history") || classLower.contains("u.s. history") ||
                        classLower.contains("ap united states history")) {
                    return true;
                }
            }

            if (reqLower.equals("government")) {
                if (classLower.contains("government") || classLower.contains("civics")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the class names from a student's schedule.
     */
    private static List<String> getScheduledClassNames(Student student) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .map(StudentBlock::getClassName)
                .collect(Collectors.toList());
    }

    /**
     * Attempts to schedule a missing required class for a student.
     */
    private static boolean attemptToScheduleMissingClass(Student student, String className,
            HashMap<Integer, Staff> staffHashMap) {
        // Find an available section for this class
        List<ClassSection> sections = classSections.get(className);
        if (sections == null || sections.isEmpty()) {
            // Try to find an equivalent class
            String equivalent = findEquivalentClass(className, student.studentStatistics.getGradeLevel());
            if (equivalent != null) {
                sections = classSections.get(equivalent);
            }
        }

        if (sections == null || sections.isEmpty()) {
            return false;
        }

        // Try to add student to a section that doesn't conflict
        for (ClassSection section : sections) {
            TeacherBlock block = section.getTeacherBlock();
            if (block == null)
                continue;

            // Check for conflicts
            if (!hasBlockConflict(student, block)) {
                // Check capacity (allow overcrowding if enabled)
                int maxCapacity = allowOvercrowding ? currentMaxClassSize : currentOptimalClassSize;
                if (section.getEnrolledStudents().size() < maxCapacity) {
                    // Add student to section
                    section.addStudent(student);

                    // Create student block
                    StudentBlock studentBlock = new StudentBlock();
                    studentBlock.setBlockNumber(block.getBlockNumber());
                    studentBlock.setClassName(block.getClassName());
                    studentBlock.setSemester(block.getSemester());
                    studentBlock.setRoom(block.getRoom());
                    student.studentStatistics.addStudentSchedule(studentBlock);

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Finds an equivalent class for a graduation requirement.
     */
    private static String findEquivalentClass(String required, String grade) {
        String reqLower = required.toLowerCase();

        // Check what classes we have available
        for (String className : classSections.keySet()) {
            String classLower = className.toLowerCase();

            if (reqLower.contains("math") &&
                    (classLower.contains("algebra") || classLower.contains("geometry"))) {
                return className;
            }

            if (reqLower.contains("science") &&
                    (classLower.contains("biology") || classLower.contains("chemistry"))) {
                return className;
            }

            if (reqLower.contains("history") &&
                    (classLower.contains("history") || classLower.contains("geography"))) {
                return className;
            }
        }

        return null;
    }

    /**
     * Backward compatibility method - calls enhanced version with null parameters
     */
    public static void scheduleAllStudentsEnhanced(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        scheduleAllStudentsEnhanced(studentHashMap, staffHashMap, null, null);
    }

    /**
     * Analyzes student demand using the existing trait-based logic
     */
    private static void analyzeDemandWithTraits(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("Analyzing student demand based on traits and requirements...");

        classSections.clear();
        demandTracker.clear();
        classWaitlists.clear();

        Map<String, Set<Student>> classDemand = new HashMap<>();

        for (Student student : studentHashMap.values()) {
            // Use existing logic to determine what classes this student needs/wants
            List<String> studentClasses = determineStudentClasses(student);

            for (String className : studentClasses) {
                classDemand.computeIfAbsent(className, k -> new HashSet<>()).add(student);
            }
        }

        // Create demand tracking objects
        for (Map.Entry<String, Set<Student>> entry : classDemand.entrySet()) {
            String className = entry.getKey();
            Set<Student> interestedStudents = entry.getValue();

            StudentDemand demand = new StudentDemand(className, interestedStudents.size(), interestedStudents);
            demandTracker.put(className, demand);

            GameLogger.logScheduling("Demand for " + className + ": " + interestedStudents.size() + " students");
        }

        // Debug: Show language class demand specifically
        GameLogger.logScheduling("=== LANGUAGE CLASS DEMAND ===");
        String[] languageClasses = { "Spanish I", "Spanish II", "French I", "French II",
                "German I", "German II", "Latin I", "Latin II",
                "American Sign Language I", "American Sign Language II" };
        for (String langClass : languageClasses) {
            int demand = demandTracker.containsKey(langClass) ? demandTracker.get(langClass).totalDemand() : 0;
            GameLogger.logScheduling("  " + langClass + ": " + demand + " students");
        }

        // Debug: Show science class demand specifically
        GameLogger.logScheduling("=== SCIENCE CLASS DEMAND ===");
        String[] scienceClasses = { "Biology", "Chemistry", "Physics", "AP Biology", "AP Chemistry",
                "AP Physics B", "Environmental Science", "Anatomy and Physiology" };
        for (String sciClass : scienceClasses) {
            int demand = demandTracker.containsKey(sciClass) ? demandTracker.get(sciClass).totalDemand() : 0;
            GameLogger.logScheduling("  " + sciClass + ": " + demand + " students");
        }
    }

    /**
     * Uses existing trait logic to determine what classes a student should take
     */
    private static List<String> determineStudentClasses(Student student) {
        List<String> allClasses = new ArrayList<>();

        String year = student.studentStatistics.getGradeLevel();
        int intelligence = student.studentStatistics.getIntelligence();
        int determination = student.studentStatistics.getDetermination();
        String income = student.studentStatistics.getIncomeLevel();

        // Use existing logic for determining academic paths
        String englishPath = classProbabilityLoader(intelligence, income, determination);
        String mathPath = classProbabilityLoader(intelligence, income, determination);
        String sciencePath = classProbabilityLoader(intelligence, income, determination);
        String historyPath = classProbabilityLoader(intelligence, income, determination);

        // Add core classes based on existing logic
        allClasses.addAll(determineEnglishClasses(year, englishPath));
        allClasses.addAll(determineMathClasses(year, mathPath));
        allClasses.addAll(determineScienceClasses(year, sciencePath));
        allClasses.addAll(determineHistoryClasses(year, historyPath));
        allClasses.addAll(determineLanguageClasses(year, student));
        allClasses.addAll(determinePhysEdClasses(year, student));
        allClasses.addAll(determineVocationalClasses(year, student));

        return allClasses;
    }

    /**
     * Creates optimal sections with minimum enrollment constraints
     */
    private static void createOptimalSections(HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("Creating optimal sections with minimum enrollment constraints...");

        // Debug: Show what classes are actually in teacher blocks
        GameLogger.logScheduling("=== TEACHER BLOCK CLASS NAMES ===");
        Map<String, Integer> blocksByClass = new HashMap<>();
        for (Staff staff : staffHashMap.values()) {
            for (TeacherBlock block : staff.teacherStatistics.getTeacherSchedule().getTeacherSchedule()) {
                String className = block.getClassName();
                blocksByClass.merge(className, 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> entry : blocksByClass.entrySet()) {
            GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue() + " blocks");
        }

        for (Map.Entry<String, StudentDemand> entry : demandTracker.entrySet()) {
            String className = entry.getKey();
            StudentDemand demand = entry.getValue();

            // Debug output for specific classes
            if (className.equals("World Geography") || className.equals("Health")
                    || className.equals("AP Human Geography")) {
                GameLogger.logScheduling("DEBUG: Processing " + className + " with demand: " + demand.totalDemand());
                List<Staff> qualifiedTeachers = getQualifiedTeachers(className, staffHashMap);
                GameLogger.logScheduling("DEBUG: Found " + qualifiedTeachers.size() + " qualified teachers for " + className);
                for (Staff teacher : qualifiedTeachers) {
                    GameLogger.logScheduling("DEBUG: Teacher " + teacher.teacherName.getFirstName() + " " +
                            teacher.teacherName.getLastName() + " can teach " + className);
                }
            }

            if (isCoreSubject(className)) {
                // Core subjects: ensure minimum enrollment
                if (demand.totalDemand() >= MIN_CLASS_SIZE) {
                    createSectionsForClass(className, demand, staffHashMap);
                } else {
                    GameLogger.logScheduling("WARNING: Core class " + className +
                            " has insufficient demand: " + demand.totalDemand());
                    // Still create section but flag for monitoring
                    createSectionsForClass(className, demand, staffHashMap);
                }
            } else {
                // Electives: apply stricter minimum
                int minRequired = className.contains("AP") ? MIN_AP_CLASS_SIZE : MIN_ELECTIVE_SIZE;
                if (demand.totalDemand() >= minRequired) {
                    createSectionsForClass(className, demand, staffHashMap);
                } else {
                    GameLogger.logScheduling("Canceling elective " + className +
                            " due to insufficient enrollment: " + demand.totalDemand());
                    // Add students to waitlist for alternative assignment
                    classWaitlists.put(className, demand.interestedStudents());
                }
            }
        }

        // Debug: Compare demand vs sections created
        GameLogger.logScheduling("=== DEMAND VS SECTIONS COMPARISON ===");
        for (Map.Entry<String, StudentDemand> entry : demandTracker.entrySet()) {
            String className = entry.getKey();
            int demand = entry.getValue().totalDemand();
            List<ClassSection> sections = classSections.get(className);
            int sectionCount = sections != null ? sections.size() : 0;
            int capacity = sections != null ? sections.stream().mapToInt(s -> s.capacity).sum() : 0;

            // Only show classes with demand but no sections, or significant gaps
            if (demand > 0 && (sectionCount == 0 || capacity < demand * 0.5)) {
                GameLogger.logScheduling("  GAP: " + className + " - demand: " + demand +
                        ", sections: " + sectionCount + ", capacity: " + capacity);
            }
        }
    }

    /**
     * Analyzes resource shortages and reallocates substitutes to address demand
     */
    private static void analyzeAndReallocateResources(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("=== RESOURCE ANALYSIS AND SUBSTITUTE REALLOCATION ===");

        // Step 1: Analyze available resources
        List<Staff> availableSubstitutes = StaffAssignmentService.getTeachersOfType(staffHashMap, StaffType.SUB);
        int totalSubstitutes = availableSubstitutes.size();

        GameLogger.logScheduling("Available substitutes: " + totalSubstitutes);

        // Step 2: Analyze current capacity vs demand for each class
        List<ResourceShortage> shortages = identifyResourceShortages();

        // Step 3: Sort shortages by priority (core subjects first, then by severity)
        shortages.sort((s1, s2) -> {
            // Core subjects get priority
            boolean s1Core = isCoreSubject(s1.className);
            boolean s2Core = isCoreSubject(s2.className);

            if (s1Core && !s2Core)
                return -1;
            if (s2Core && !s1Core)
                return 1;

            // Then by shortage severity
            return Integer.compare(s2.shortageAmount, s1.shortageAmount);
        });

        // Step 4: Display analysis
        GameLogger.logScheduling("=== DEMAND vs CAPACITY ANALYSIS ===");
        for (ResourceShortage shortage : shortages) {
            GameLogger.logScheduling(shortage.className + ": Need " + shortage.demandAmount +
                    ", Have capacity for " + shortage.currentCapacity +
                    ", Shortage: " + shortage.shortageAmount);
        }

        // Step 5: Reallocate substitutes to address critical shortages
        int substitutesUsed = 0;
        for (ResourceShortage shortage : shortages) {
            if (substitutesUsed >= totalSubstitutes) {
                GameLogger.logScheduling("No more substitutes available for reallocation");
                break;
            }

            if (shortage.shortageAmount > 0 && isCoreSubject(shortage.className)) {
                int teachersNeeded = calculateTeachersNeeded(shortage);
                int teachersToAllocate = Math.min(teachersNeeded, totalSubstitutes - substitutesUsed);

                if (teachersToAllocate > 0) {
                    boolean success = reallocateSubstitutesToClass(shortage.className, teachersToAllocate,
                            availableSubstitutes, substitutesUsed, staffHashMap);
                    if (success) {
                        substitutesUsed += teachersToAllocate;
                        GameLogger.logScheduling(
                                "✓ Reallocated " + teachersToAllocate + " substitutes to " + shortage.className);

                        // Recreate sections for this class with new teachers
                        StudentDemand demand = demandTracker.get(shortage.className);
                        if (demand != null) {
                            // Clear old sections and recreate with new teachers
                            classSections.remove(shortage.className);
                            createSectionsForClass(shortage.className, demand, staffHashMap);
                        }
                    } else {
                        GameLogger.logScheduling("✗ Failed to reallocate substitutes to " + shortage.className
                                + " (no available rooms)");
                    }
                }
            }
        }

        GameLogger.logScheduling("=== REALLOCATION SUMMARY ===");
        GameLogger.logScheduling("Total substitutes used: " + substitutesUsed + "/" + totalSubstitutes);
        GameLogger.logScheduling("Remaining substitutes: " + (totalSubstitutes - substitutesUsed));
        GameLogger.logScheduling("=== END RESOURCE ANALYSIS ===");
    }

    /**
     * Identifies classes with demand shortages
     */
    private static List<ResourceShortage> identifyResourceShortages() {
        List<ResourceShortage> shortages = new ArrayList<>();

        for (Map.Entry<String, StudentDemand> entry : demandTracker.entrySet()) {
            String className = entry.getKey();
            StudentDemand demand = entry.getValue();

            // Calculate current capacity
            List<ClassSection> sections = classSections.get(className);
            int currentCapacity = 0;
            if (sections != null) {
                currentCapacity = sections.stream()
                        .mapToInt(s -> s.capacity)
                        .sum();
            }

            int shortage = demand.totalDemand() - currentCapacity;

            ResourceShortage resourceShortage = new ResourceShortage(
                    className,
                    demand.totalDemand(),
                    currentCapacity,
                    shortage);

            shortages.add(resourceShortage);
        }

        return shortages;
    }

    /**
     * Calculates how many additional teachers are needed for a shortage
     */
    private static int calculateTeachersNeeded(ResourceShortage shortage) {
        if (shortage.shortageAmount <= 0)
            return 0;

        // Assume each teacher can handle ~25 students across all their blocks
        // Each teacher teaches 8 blocks, so roughly 200 students total capacity per
        // teacher
        // But for a specific class, they might teach it 2-4 times, so ~50-100 students
        // per class per teacher
        int studentsPerTeacherPerClass = 50; // Conservative estimate

        return (int) Math.ceil((double) shortage.shortageAmount / studentsPerTeacherPerClass);
    }

    /**
     * Reallocates substitutes to a specific class
     */
    private static boolean reallocateSubstitutesToClass(String className, int teachersNeeded,
            List<Staff> availableSubstitutes, int startIndex,
            HashMap<Integer, Staff> staffHashMap) {
        // Find appropriate subject type for the class
        StaffType targetType = determineStaffTypeForClass(className);

        if (targetType == null) {
            GameLogger.logScheduling("Cannot determine staff type for " + className);
            return false;
        }

        // Reallocate substitutes
        for (int i = 0; i < teachersNeeded && (startIndex + i) < availableSubstitutes.size(); i++) {
            Staff substitute = availableSubstitutes.get(startIndex + i);

            // Change their staff type
            substitute.teacherStatistics.setStaffType(targetType);

            GameLogger.logScheduling("Reallocated substitute " + substitute.teacherName.getFirstName() + " " +
                    substitute.teacherName.getLastName() + " to " + targetType + " for " + className);
        }

        return true;
    }

    /**
     * Determines the appropriate staff type for a class
     */
    private static StaffType determineStaffTypeForClass(String className) {
        if (belongsToSubjectArea(className, "english"))
            return StaffType.ENGLISH;
        if (belongsToSubjectArea(className, "math"))
            return StaffType.MATH;
        if (belongsToSubjectArea(className, "science"))
            return StaffType.SCIENCE;
        if (belongsToSubjectArea(className, "history"))
            return StaffType.HISTORY;
        if (belongsToSubjectArea(className, "language"))
            return StaffType.LANGUAGES;
        if (belongsToSubjectArea(className, "physical education"))
            return StaffType.PHYSICAL_ED;

        // Default for electives
        if (className.toLowerCase().contains("art"))
            return StaffType.VISUAL_ARTS;
        if (className.toLowerCase().contains("music") || className.toLowerCase().contains("band") ||
                className.toLowerCase().contains("theater") || className.toLowerCase().contains("choir"))
            return StaffType.PERFORMING_ARTS;
        if (className.toLowerCase().contains("business"))
            return StaffType.BUSINESS;

        return StaffType.VOCATIONAL; // Default for other electives
    }

    /**
     * Helper class to track resource shortages
     */
    private static class ResourceShortage {
        final String className;
        final int demandAmount;
        final int currentCapacity;
        final int shortageAmount;

        ResourceShortage(String className, int demandAmount, int currentCapacity, int shortageAmount) {
            this.className = className;
            this.demandAmount = demandAmount;
            this.currentCapacity = currentCapacity;
            this.shortageAmount = shortageAmount;
        }
    }

    /**
     * Enhanced assignment that prioritizes core classes and can rearrange schedules
     * NOW WITH DUPLICATE DETECTION
     */
    private static void assignStudentsWithOptimization(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("Assigning students with priority-based optimization (WITH DUPLICATE PREVENTION)...");

        // Sort students by priority (seniors first, then by intelligence for
        // tie-breaking)
        List<Student> sortedStudents = studentHashMap.values().stream()
                .sorted((s1, s2) -> {
                    String grade1 = s1.studentStatistics.getGradeLevel();
                    String grade2 = s2.studentStatistics.getGradeLevel();

                    // Senior -> Junior -> Sophomore -> Freshman priority order
                    int priority1 = getGradePriority(grade1);
                    int priority2 = getGradePriority(grade2);

                    if (priority1 != priority2) {
                        return Integer.compare(priority1, priority2); // Lower number = higher priority
                    }
                    // Tie-break by intelligence (for AP class priority)
                    return Integer.compare(s2.studentStatistics.getIntelligence(),
                            s1.studentStatistics.getIntelligence());
                })
                .collect(Collectors.toList());

        // PRIORITY PHASE 0: Language Assignment FIRST (most constrained - requires both
        // semesters)
        GameLogger.logScheduling("=== PRIORITY PHASE 0: Language Sequences (HIGHEST PRIORITY) ===");
        List<Student> freshmen = sortedStudents.stream()
                .filter(s -> s.studentStatistics.getGradeLevel().equals("Freshman"))
                .collect(Collectors.toList());
        if (!freshmen.isEmpty()) {
            assignSimpleLanguageSequences(freshmen, staffHashMap);
        }

        // PRIORITY PHASE 1: Core Academic Classes (absolutely required)
        GameLogger.logScheduling("=== PRIORITY PHASE 1: Core Academic Classes ===");
        String[] coreAcademics = { "English", "Math", "Science", "History" };
        for (String subjectArea : coreAcademics) {
            GameLogger.logScheduling("Assigning " + subjectArea + " classes (CORE PRIORITY)...");
            assignSubjectWithPriorityAndRearrangement(subjectArea, sortedStudents, staffHashMap, true);
        }

        // PRIORITY PHASE 2: Required PE
        GameLogger.logScheduling("=== PRIORITY PHASE 2: Required Physical Education ===");
        GameLogger.logScheduling("Assigning Physical Education classes (HIGH PRIORITY)...");
        assignSubjectWithPriorityAndRearrangement("Physical Education", sortedStudents, staffHashMap, true);

        // Standard language assignment for non-freshmen (if any)
        List<Student> nonFreshmen = sortedStudents.stream()
                .filter(s -> !s.studentStatistics.getGradeLevel().equals("Freshman"))
                .collect(Collectors.toList());
        if (!nonFreshmen.isEmpty()) {
            GameLogger.logScheduling("Assigning Language classes for non-freshmen (HIGH PRIORITY)...");
            assignSubjectWithPriorityAndRearrangement("Language", nonFreshmen, staffHashMap, true);
        }

        // PRIORITY PHASE 3: Electives and Vocational (fill remaining slots)
        GameLogger.logScheduling("=== PRIORITY PHASE 3: Electives and Vocational ===");
        GameLogger.logScheduling("Assigning Electives/Vocational classes (NORMAL PRIORITY)...");
        assignElectivesWithBalancing(sortedStudents, staffHashMap);

        GameLogger.logScheduling("=== Assignment Complete - Checking for Incomplete Schedules ===");
        checkForIncompleteSchedules(sortedStudents);
    }

    /**
     * Simple, correct language assignment that ensures Level I in Fall and Level II
     * in Spring
     */
    private static void assignSimpleLanguageSequences(List<Student> freshmen, HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("=== SIMPLIFIED LANGUAGE ASSIGNMENT: Ensuring Fall I -> Spring II ===");

        // Group students by language choice
        Map<String, List<Student>> languageGroups = new HashMap<>();

        for (Student student : freshmen) {
            List<String> languageClasses = determineLanguageClasses("Freshman", student);
            if (languageClasses.size() >= 2) {
                String languageBase = getLanguageBase(languageClasses.get(0));
                languageGroups.computeIfAbsent(languageBase, k -> new ArrayList<>()).add(student);
            }
        }

        // Process each language group
        for (Map.Entry<String, List<Student>> entry : languageGroups.entrySet()) {
            String languageBase = entry.getKey();
            List<Student> students = entry.getValue();

            GameLogger.logScheduling("Processing " + languageBase + " for " + students.size() + " students");

            String level1Class = languageBase + " I"; // Must be Fall
            String level2Class = languageBase + " II"; // Must be Spring

            // Create sections with strict semester requirements
            createSectionsForLanguageSequence(level1Class, level2Class, students.size(), staffHashMap);

            // Assign students to both classes with semester validation
            assignStudentsToStrictLanguageSequence(students, level1Class, level2Class, languageBase, staffHashMap);
        }
    }

    /**
     * Creates language sections with strict semester requirements: Level I in Fall,
     * Level II in Spring
     */
    private static void createSectionsForLanguageSequence(String level1Class, String level2Class,
            int totalStudents, HashMap<Integer, Staff> staffHashMap) {
        List<Staff> level1Teachers = getQualifiedTeachers(level1Class, staffHashMap);
        List<Staff> level2Teachers = getQualifiedTeachers(level2Class, staffHashMap);

        if (level1Teachers.isEmpty() || level2Teachers.isEmpty()) {
            GameLogger.logScheduling("WARNING: No qualified teachers found for " + level1Class + " or " + level2Class);
            return;
        }

        // Calculate needed sections
        int averageCapacity = calculateAverageRoomCapacity(level1Teachers);
        int neededSections = Math.max(1, (totalStudents + averageCapacity - 1) / averageCapacity);

        GameLogger.logScheduling("Creating " + neededSections + " sections each for " + level1Class +
                " (Fall) and " + level2Class + " (Spring)");

        List<ClassSection> level1Sections = new ArrayList<>();
        List<ClassSection> level2Sections = new ArrayList<>();

        // Create Level I sections (FALL ONLY)
        for (int i = 0; i < neededSections && i < level1Teachers.size(); i++) {
            Staff teacher = level1Teachers.get(i);
            TeacherBlock fallBlock = findBlockBySemester(teacher, level1Class, "Fall");

            if (fallBlock != null) {
                ClassSection section = new ClassSection(level1Class, teacher, fallBlock,
                        fallBlock.getRoom().getStudentCapacity());
                level1Sections.add(section);
                GameLogger.logScheduling("Created " + level1Class + " section: Fall Block " +
                        fallBlock.getBlockNumber() + " with " +
                        teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
            }
        }

        // Create Level II sections (SPRING ONLY)
        for (int i = 0; i < neededSections && i < level2Teachers.size(); i++) {
            Staff teacher = level2Teachers.get(i);
            TeacherBlock springBlock = findBlockBySemester(teacher, level2Class, "Spring");

            if (springBlock != null) {
                ClassSection section = new ClassSection(level2Class, teacher, springBlock,
                        springBlock.getRoom().getStudentCapacity());
                level2Sections.add(section);
                GameLogger.logScheduling("Created " + level2Class + " section: Spring Block " +
                        springBlock.getBlockNumber() + " with " +
                        teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName());
            }
        }

        // Store sections
        classSections.put(level1Class, level1Sections);
        classSections.put(level2Class, level2Sections);

        GameLogger.logScheduling("Language sections created: " + level1Sections.size() + " Fall sections, " +
                level2Sections.size() + " Spring sections");
    }

    /**
     * Finds a teacher block in the specified semester
     */
    private static TeacherBlock findBlockBySemester(Staff teacher, String className, String targetSemester) {
        List<TeacherBlock> availableBlocks = teacher.teacherStatistics.getTeacherSchedule()
                .getBlocksByClassName(className);

        for (TeacherBlock block : availableBlocks) {
            if (block.getSemester().equals(targetSemester)) {
                return block;
            }
        }

        return null; // No block found in target semester
    }

    /**
     * Assigns students to language sequence with strict semester validation
     */
    private static void assignStudentsToStrictLanguageSequence(List<Student> students, String level1Class,
            String level2Class, String languageBase, HashMap<Integer, Staff> staffHashMap) {
        List<ClassSection> level1Sections = classSections.get(level1Class);
        List<ClassSection> level2Sections = classSections.get(level2Class);

        if (level1Sections == null || level2Sections == null ||
                level1Sections.isEmpty() || level2Sections.isEmpty()) {
            GameLogger.logScheduling("ERROR: Insufficient sections for " + languageBase + " sequence");
            GameLogger.logScheduling("  Level I sections: " + (level1Sections != null ? level1Sections.size() : 0));
            GameLogger.logScheduling("  Level II sections: " + (level2Sections != null ? level2Sections.size() : 0));
            return;
        }

        int successCount = 0;
        int level1Index = 0;
        int level2Index = 0;

        for (Student student : students) {
            String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();
            boolean assigned = false;

            // Try to find available sections for both levels
            for (int attempt = 0; attempt < Math.max(level1Sections.size(), level2Sections.size())
                    && !assigned; attempt++) {
                ClassSection level1Section = level1Sections.get(level1Index % level1Sections.size());
                ClassSection level2Section = level2Sections.get(level2Index % level2Sections.size());

                // Validate: Level I must be Fall, Level II must be Spring
                boolean validSequence = level1Section.getTeacherBlock().getSemester().equals("Fall") &&
                        level2Section.getTeacherBlock().getSemester().equals("Spring");

                if (validSequence && !level1Section.isFull() && !level2Section.isFull() &&
                        !hasBlockConflict(student, level1Section.getTeacherBlock()) &&
                        !hasBlockConflict(student, level2Section.getTeacherBlock())) {

                    // Assign both classes
                    assignStudentToSection(student, level1Section, true);
                    assignStudentToSection(student, level2Section, true);

                    GameLogger.logScheduling("✓ SUCCESS: " + studentName + " assigned " + languageBase +
                            " sequence [Fall " + level1Section.getTeacherBlock().getBlockNumber() +
                            " -> Spring " + level2Section.getTeacherBlock().getBlockNumber() + "]");

                    assigned = true;
                    successCount++;

                    // Move to next sections for load balancing
                    level1Index = (level1Index + 1) % level1Sections.size();
                    level2Index = (level2Index + 1) % level2Sections.size();
                } else {
                    // Try next section combination
                    level1Index = (level1Index + 1) % level1Sections.size();
                    level2Index = (level2Index + 1) % level2Sections.size();
                }
            }

            if (!assigned) {
                GameLogger.logScheduling("✗ FAILED: Could not assign " + languageBase + " sequence to " + studentName);
                // Try alternative language as fallback
                trySimpleAlternativeLanguage(student, languageBase, staffHashMap);
            }
        }

        GameLogger.logScheduling("Language assignment results: " + successCount + "/" + students.size() +
                " students successfully assigned " + languageBase);
    }

    /**
     * Simple fallback to try alternative languages
     */
    private static void trySimpleAlternativeLanguage(Student student, String failedLanguage,
            HashMap<Integer, Staff> staffHashMap) {
        String[] alternatives = { "Spanish", "French", "German", "Latin", "American Sign Language" };
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();

        for (String alt : alternatives) {
            if (alt.equals(failedLanguage))
                continue;

            String level1 = alt + " I";
            String level2 = alt + " II";

            List<ClassSection> alt1Sections = classSections.get(level1);
            List<ClassSection> alt2Sections = classSections.get(level2);

            if (alt1Sections != null && alt2Sections != null && !alt1Sections.isEmpty() && !alt2Sections.isEmpty()) {
                for (ClassSection s1 : alt1Sections) {
                    for (ClassSection s2 : alt2Sections) {
                        if (s1.getTeacherBlock().getSemester().equals("Fall") &&
                                s2.getTeacherBlock().getSemester().equals("Spring") &&
                                !s1.isFull() && !s2.isFull() &&
                                !hasBlockConflict(student, s1.getTeacherBlock()) &&
                                !hasBlockConflict(student, s2.getTeacherBlock())) {

                            assignStudentToSection(student, s1, true);
                            assignStudentToSection(student, s2, true);

                            GameLogger.logScheduling("✓ ALTERNATIVE: " + studentName + " assigned " + alt +
                                    " sequence (fallback from " + failedLanguage + ")");
                            return;
                        }
                    }
                }
            }
        }

        GameLogger.logScheduling("✗ CRITICAL: No language sequence available for " + studentName);
    }

    /**
     * Enhanced assignment with schedule rearrangement capability
     * NOW WITH DUPLICATE PREVENTION
     */
    private static void assignSubjectWithPriorityAndRearrangement(String subjectArea, List<Student> students,
            HashMap<Integer, Staff> staffHashMap, boolean allowRearrangement) {
        GameLogger.logScheduling("Processing " + subjectArea + " for " + students.size() + " students (rearrangement: "
                + allowRearrangement + ")");

        for (Student student : students) {
            List<String> subjectClasses = getStudentClassesForSubject(student, subjectArea);

            if (!subjectClasses.isEmpty() && student.studentStatistics.getGradeLevel().equals("Freshman")) {
                GameLogger.logScheduling("Student " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " (" +
                        student.studentStatistics.getGradeLevel() + ") needs " +
                        subjectArea + " classes: " + subjectClasses);
            }

            for (String className : subjectClasses) {
                // *** CRITICAL FIX: Check for duplicates before assignment ***
                if (studentAlreadyHasClass(student, className)) {
                    if (student.studentStatistics.getGradeLevel().equals("Senior")) {
                        GameLogger.logScheduling("DUPLICATE PREVENTION: " + student.studentName.getFirstName() + " " +
                                student.studentName.getLastName() + " already has " + className +
                                " - skipping duplicate assignment");
                    }
                    continue; // Skip this class - student already has it
                }

                boolean assigned = false;

                if (classSections.containsKey(className)) {
                    // Enhanced debugging for History assignment issues
                    if (className.equals("World Geography") &&
                            student.studentName.getFirstName().equals("Steven") &&
                            student.studentName.getLastName().equals("Adler")) {
                        debugHistoryAssignment(student, className);
                    }

                    // Try normal assignment first
                    ClassSection bestSection = findOptimalSection(student, className);
                    if (bestSection != null) {
                        assignStudentToSection(student, bestSection, true);
                        assigned = true;
                    } else if (allowRearrangement) {
                        // Try with schedule rearrangement
                        assigned = tryAssignWithRearrangement(student, className, subjectArea);
                    }

                    if (!assigned && student.studentStatistics.getGradeLevel().equals("Freshman")) {
                        GameLogger.logScheduling("WARNING: Could not assign " + className + " to " +
                                student.studentName.getFirstName() + " " +
                                student.studentName.getLastName() + " even with rearrangement");
                    }
                } else if (student.studentStatistics.getGradeLevel().equals("Freshman")) {
                    GameLogger.logScheduling("WARNING: No sections created for class " + className +
                            " needed by " + student.studentName.getFirstName() + " " +
                            student.studentName.getLastName());
                }
            }
        }
    }

    /**
     * Debug method to analyze why History assignment fails
     */
    private static void debugHistoryAssignment(Student student, String className) {
        GameLogger.logScheduling("=== DEBUGGING HISTORY ASSIGNMENT FOR " + student.studentName.getFirstName() + " " +
                student.studentName.getLastName() + " (" + className + ") ===");

        // Show student's current schedule
        GameLogger.logScheduling("Student's current schedule:");
        for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
            GameLogger.logScheduling("  " + block.getSemester() + " " + block.getBlockNumber() +
                    ": " + block.getClassName());
        }

        // Show available History sections
        List<ClassSection> historySections = classSections.get(className);
        if (historySections == null) {
            GameLogger.logScheduling("ERROR: No sections found for " + className);
            return;
        }

        GameLogger.logScheduling("Available " + className + " sections (" + historySections.size() + " total):");
        int availableCount = 0;

        for (ClassSection section : historySections) {
            TeacherBlock teacherBlock = section.getTeacherBlock();
            boolean hasConflict = hasBlockConflict(student, teacherBlock);
            boolean isFull = section.isFull();
            int enrollment = section.getEnrolledStudents().size();

            String status = "";
            if (hasConflict)
                status += "[CONFLICT] ";
            if (isFull)
                status += "[FULL] ";
            if (!hasConflict && !isFull) {
                status += "[AVAILABLE] ";
                availableCount++;
            }

            GameLogger.logScheduling("  " + status + teacherBlock.getSemester() + " Block " +
                    teacherBlock.getBlockNumber() + " with " +
                    section.getTeacher().teacherName.getFirstName() + " " +
                    section.getTeacher().teacherName.getLastName() +
                    " (enrollment: " + enrollment + "/" + section.capacity +
                    ", room: " + teacherBlock.getRoom().getRoomName() + ")");
        }

        GameLogger.logScheduling("Summary: " + availableCount + " sections available without conflicts");

        if (availableCount == 0) {
            GameLogger.logScheduling("DIAGNOSIS: All World Geography sections conflict with student's schedule or are full");
            // Show specifically which blocks would work
            GameLogger.logScheduling("Student needs World Geography in one of these blocks:");
            boolean[] fallBlocks = new boolean[9]; // blocks 1-8
            boolean[] springBlocks = new boolean[9]; // blocks 1-8

            // Mark occupied blocks
            for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
                int blockNum = block.getBlockNumber();
                if (block.getSemester().equals("Fall")) {
                    fallBlocks[blockNum] = true;
                } else {
                    springBlocks[blockNum] = true;
                }
            }

            GameLogger.logScheduling("Available blocks for student:");
            for (int i = 1; i <= 8; i++) {
                if (!fallBlocks[i])
                    GameLogger.logScheduling("  Fall " + i + " - AVAILABLE");
                if (!springBlocks[i])
                    GameLogger.logScheduling("  Spring " + i + " - AVAILABLE");
            }
        }

        GameLogger.logScheduling("=== END DEBUGGING ===");
    }

    /**
     * Attempts to assign a class by rearranging the student's existing schedule
     */
    private static boolean tryAssignWithRearrangement(Student student, String className, String subjectArea) {
        String studentName = student.studentName.getFirstName() + " " + student.studentName.getLastName();
        GameLogger.logScheduling("Attempting schedule rearrangement for " + studentName + " to fit " + className);

        List<ClassSection> availableSections = classSections.get(className);
        if (availableSections == null || availableSections.isEmpty())
            return false;

        // For now, just return false - we can implement full rearrangement logic later
        // This ensures core classes get priority during initial assignment
        return false;
    }

    /**
     * Checks for incomplete schedules and reports them
     */
    private static void checkForIncompleteSchedules(List<Student> students) {
        int incompleteCount = 0;

        for (Student student : students) {
            int scheduleSize = student.studentStatistics.getStudentSchedule().getClassSchedule().size();
            String grade = student.studentStatistics.getGradeLevel();

            // Determine expected schedule size based on grade
            int expectedSize = getExpectedScheduleSize(grade);

            if (scheduleSize < expectedSize) {
                incompleteCount++;
                if (grade.equals("Freshman")) {
                    GameLogger.logScheduling("INCOMPLETE SCHEDULE: " + student.studentName.getFirstName() + " " +
                            student.studentName.getLastName() + " (" + grade + ") has " +
                            scheduleSize + "/" + expectedSize + " classes");

                    // Show what they have
                    GameLogger.logScheduling("  Current classes:");
                    for (StudentBlock block : student.studentStatistics.getStudentSchedule().getClassSchedule()) {
                        GameLogger.logScheduling("    " + block.getSemester() + " " + block.getBlockNumber() +
                                " " + block.getClassName());
                    }
                }
            }
        }

        GameLogger.logScheduling("Students with incomplete schedules: " + incompleteCount + "/" + students.size());
    }

    /**
     * Returns expected schedule size based on grade level
     */
    private static int getExpectedScheduleSize(String grade) {
        switch (grade) {
            case "Freshman":
                return 8; // English, Math(2), Science, History, Language(2), PE
            case "Sophomore":
                return 6; // English, Math(2), Science, History, PE
            case "Junior":
                return 6; // English, Math, Science, History, Electives(2)
            case "Senior":
                return 6; // English, Math, Science, History, Electives(2) - Increased from 4
            default:
                return 6;
        }
    }

    // === HELPER METHODS ===

    private static List<String> determineEnglishClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add("English I");
            case "Sophomore" -> classes.add("English II");
            case "Junior" -> classes.add(path.equals("AP") ? "AP English Language & Composition" : "English III");
            case "Senior" -> classes.add(path.equals("AP") ? "AP English Literature & Composition" : "English IV");
        }
        return classes;
    }

    private static List<String> determineMathClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> {
                classes.add(path.equals("AP") || path.equals("Honors") ? "Geometry" : "Fundamentals of Math");
                if (path.equals("AP") || path.equals("Honors")) {
                    classes.add("Algebra I");
                } else {
                    classes.add("Geometry");
                }
            }
            case "Sophomore" -> {
                classes.add(path.equals("AP") || path.equals("Honors") ? "Algebra II" : "Algebra I");
                classes.add(path.equals("AP") || path.equals("Honors") ? "Trigonometry" : "Algebra II");
            }
            case "Junior" -> {
                classes.add(path.equals("AP") ? "Precalculus" : path.equals("Honors") ? "Precalculus" : "Trigonometry");
                if (path.equals("AP")) {
                    classes.add("AP Statistics");
                } else if (!path.equals("Honors")) {
                    classes.add("Math for Data and Financial Literacy");
                }
            }
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP Calculus AB");
                    classes.add("AP Calculus BC");
                } else if (path.equals("Honors")) {
                    // Honors students should still take some math in senior year
                    classes.add("Precalculus");
                } else {
                    classes.add("Precalculus");
                }
            }
        }
        return classes;
    }

    private static List<String> determineScienceClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add("Biology");
            case "Sophomore" -> {
                if (path.equals("AP") || path.equals("Honors")) {
                    classes.add("Chemistry");
                } else {
                    String[] options = { "Earth and Space Science", "Physical Science" };
                    classes.add(options[Randomizer.setRandom(0, options.length - 1)]);
                }
            }
            case "Junior" -> {
                if (path.equals("AP")) {
                    String[] apScienceOptions = { "AP Biology", "AP Chemistry" };
                    classes.add(apScienceOptions[Randomizer.setRandom(0, apScienceOptions.length - 1)]);
                } else {
                    classes.add("Anatomy and Physiology");
                }
            }
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP Physics B");
                    classes.add("AP Physics C");
                } else if (path.equals("Honors")) {
                    classes.add("Physics");
                } else {
                    classes.add("Environmental Science");
                }
            }
        }
        return classes;
    }

    private static List<String> determineHistoryClasses(String year, String path) {
        List<String> classes = new ArrayList<>();
        switch (year) {
            case "Freshman" -> classes.add(path.equals("AP") ? "AP Human Geography" : "World Geography");
            case "Sophomore" -> classes.add(path.equals("AP") ? "AP World History" : "World History");
            case "Junior" -> classes.add(path.equals("AP") ? "AP US History" : "US History");
            case "Senior" -> {
                if (path.equals("AP")) {
                    classes.add("AP US Government");
                    classes.add("AP Economics Macro");
                } else {
                    classes.add("US Government");
                }
            }
        }
        return classes;
    }

    private static List<String> determineLanguageClasses(String year, Student student) {
        List<String> classes = new ArrayList<>();
        if (year.equals("Freshman")) {
            int langChoice = Randomizer.setRandom(0, LANGUAGE_CHOICE_SAMPLE_SIZE);
            switch (langChoice) {
                case 0 -> {
                    classes.add("Spanish I");
                    classes.add("Spanish II");
                }
                case 1 -> {
                    classes.add("French I");
                    classes.add("French II");
                }
                case 2 -> {
                    classes.add("German I");
                    classes.add("German II");
                }
                case 3 -> {
                    classes.add("American Sign Language I");
                    classes.add("American Sign Language II");
                }
                case 4 -> {
                    classes.add("Latin I");
                    classes.add("Latin II");
                }
            }
        }
        return classes;
    }

    private static List<String> determinePhysEdClasses(String year, Student student) {
        List<String> classes = new ArrayList<>();
        if (year.equals("Freshman")) {
            classes.add("Health");
        } else if (year.equals("Sophomore")) {
            String[] choices = physicalEdDecision(student);
            classes.add(choices[0]); // Add first choice
        }
        return classes;
    }

    private static List<String> determineVocationalClasses(String year, Student student) {
        List<String> classes = new ArrayList<>();
        if (!year.equals("Freshman")) {
            // Modified logic: Seniors always get electives, other grades depend on
            // determination
            if (year.equals("Senior") || student.studentStatistics
                    .getDetermination() >= SENIOR_VOCATIONAL_CLASS_DETERMINATION_THRESHOLD) {
                String[] fallChoices = vocationalDecision(student, "Fall");
                String[] springChoices = vocationalDecision(student, "Spring");
                classes.add(fallChoices[0]); // Add top choice for each semester
                classes.add(springChoices[0]);

                // Seniors get additional electives to fill their schedule
                if (year.equals("Senior")) {
                    if (fallChoices.length > 1)
                        classes.add(fallChoices[1]);
                    if (springChoices.length > 1)
                        classes.add(springChoices[1]);
                }
            }
        }
        return classes;
    }

    // Preserved physical education decision logic
    private static String[] physicalEdDecision(Student student) {
        String gender = student.studentStatistics.getGender();
        int strength = student.studentStatistics.getStrength();
        int determination = student.studentStatistics.getDetermination();

        if (gender.equals("Male")) {
            return getMalePhysicalEdDecision(strength, determination);
        } else {
            return getFemalePhysicalEdDecision(strength, determination);
        }
    }

    private static String[] getMalePhysicalEdDecision(int strength, int determination) {
        // EXACT SAME LOGIC as original
        if (strength > PHYSICAL_ED_MALE_STRENGTH_THRESHOLD || (strength < PHYSICAL_ED_MALE_LOW_STRENGTH_THRESHOLD
                && determination > PHYSICAL_ED_MALE_DETERMINATION_THRESHOLD)) {
            return new String[] { "Weightlifting", "Team Sports", "Specialized Sports", "Lifetime Recreation",
                    "Dance" };
        } else if (strength < PHYSICAL_ED_MALE_STRENGTH_THRESHOLD
                && strength > PHYSICAL_ED_MALE_LOW_STRENGTH_THRESHOLD) {
            return new String[] { "Team Sports", "Specialized Sports", "Weightlifting", "Lifetime Recreation",
                    "Dance" };
        } else if (determination < PHYSICAL_ED_MALE_LOW_DETERMINATION_THRESHOLD) {
            return new String[] { "Lifetime Recreation", "Specialized Sports", "Team Sports", "Dance",
                    "Weightlifting" };
        } else {
            return new String[] { "Specialized Sports", "Team Sports", "Weightlifting", "Dance",
                    "Lifetime Recreation" };
        }
    }

    private static String[] getFemalePhysicalEdDecision(int strength, int determination) {
        // EXACT SAME LOGIC as original
        if (strength > PHYSICAL_ED_FEMALE_STRENGTH_THRESHOLD || (strength < PHYSICAL_ED_FEMALE_LOW_STRENGTH_THRESHOLD
                && determination > PHYSICAL_ED_FEMALE_DETERMINATION_THRESHOLD)) {
            return new String[] { "Dance", "Team Sports", "Specialized Sports", "Weightlifting",
                    "Lifetime Recreation" };
        } else if (strength < PHYSICAL_ED_FEMALE_STRENGTH_THRESHOLD
                && strength > PHYSICAL_ED_FEMALE_LOW_STRENGTH_THRESHOLD) {
            return new String[] { "Specialized Sports", "Lifetime Recreation", "Dance", "Weightlifting",
                    "Team Sports" };
        } else if (determination < PHYSICAL_ED_FEMALE_LOW_DETERMINATION_THRESHOLD) {
            return new String[] { "Lifetime Recreation", "Specialized Sports", "Dance", "Team Sports",
                    "Weightlifting" };
        } else {
            return new String[] { "Specialized Sports", "Team Sports", "Weightlifting", "Dance",
                    "Lifetime Recreation" };
        }
    }

    // === OPTIMIZATION HELPER METHODS ===

    private static void createSectionsForClass(String className, StudentDemand demand,
            HashMap<Integer, Staff> staffHashMap) {
        List<Staff> availableTeachers = getQualifiedTeachers(className, staffHashMap);

        // Calculate demand-based requirements
        int studentDemand = demand.totalDemand();
        int sectionsNeeded = (int) Math.ceil((double) studentDemand / currentOptimalClassSize);

        if (availableTeachers.isEmpty()) {
            // Log detailed warning about the shortage
            StaffType neededType = CurriculumRequirementsCalculator.mapClassToStaffType(className);
            GameLogger.logScheduling("CRITICAL SHORTAGE: No qualified teachers found for " + className);
            GameLogger.logScheduling("  - Student demand: " + studentDemand + " students");
            GameLogger.logScheduling("  - Sections needed: " + sectionsNeeded);
            GameLogger.logScheduling("  - Staff type required: " + neededType);
            GameLogger.logScheduling("  - This is a " + (isCoreSubject(className) ? "CORE" : "ELECTIVE") + " subject");

            // Track this shortage for later reporting
            trackShortage(className, studentDemand, sectionsNeeded, neededType);
            return;
        }

        // Enhanced debugging for high-demand classes
        boolean isHighDemand = studentDemand > 500 || className.equals("World Geography");
        if (isHighDemand) {
            GameLogger.logScheduling("=== SECTION CREATION: " + className + " ===");
            GameLogger.logScheduling("Demand: " + studentDemand + " students, Sections needed: " + sectionsNeeded);
            GameLogger.logScheduling("Available teachers: " + availableTeachers.size());

            for (Staff teacher : availableTeachers) {
                GameLogger.logScheduling("Teacher: " + teacher.teacherName.getFirstName() + " " +
                        teacher.teacherName.getLastName());
                List<TeacherBlock> blocks = teacher.teacherStatistics.getTeacherSchedule()
                        .getBlocksByClassName(className);
                GameLogger.logScheduling("  Available blocks for " + className + ": " + blocks.size());
                for (TeacherBlock block : blocks) {
                    GameLogger.logScheduling("    " + block.getSemester() + " Block " + block.getBlockNumber() +
                            " in " + block.getRoom().getRoomName() +
                            " (capacity: " + block.getRoom().getStudentCapacity() + ")");
                }
            }
        }

        List<ClassSection> sections = new ArrayList<>();
        int totalBlocksCreated = 0;

        // Create one section for each teacher block (correct capacity calculation)
        for (Staff teacher : availableTeachers) {
            List<TeacherBlock> availableBlocks = teacher.teacherStatistics.getTeacherSchedule()
                    .getBlocksByClassName(className);

            for (TeacherBlock block : availableBlocks) {
                // Each teacher block is a separate section with room capacity
                int sectionCapacity = block.getRoom().getStudentCapacity();
                ClassSection section = new ClassSection(className, teacher, block, sectionCapacity);
                sections.add(section);
                totalBlocksCreated++;

                if (isHighDemand) {
                    GameLogger.logScheduling("Created section: " + className + " with " +
                            teacher.teacherName.getFirstName() + " " + teacher.teacherName.getLastName() +
                            " in " + block.getRoom().getRoomName() +
                            " (Block " + block.getBlockNumber() + ", " + block.getSemester() +
                            ", Capacity: " + sectionCapacity + ")");
                }
            }
        }

        classSections.put(className, sections);

        int totalCapacity = sections.stream()
                .mapToInt(s -> s.capacity)
                .sum();

        // Log section creation summary
        GameLogger.logScheduling("Created " + sections.size() + " sections for " + className +
                " (demand: " + studentDemand + ", needed: " + sectionsNeeded +
                ", capacity: " + totalCapacity + ")");

        // Check for capacity shortfall and log detailed warning
        if (totalCapacity < studentDemand) {
            int shortfall = studentDemand - totalCapacity;
            int additionalSectionsNeeded = (int) Math.ceil((double) shortfall / currentOptimalClassSize);

            GameLogger.logScheduling("CAPACITY SHORTFALL for " + className + ":");
            GameLogger.logScheduling("  - Student demand: " + studentDemand);
            GameLogger.logScheduling("  - Total capacity: " + totalCapacity);
            GameLogger.logScheduling("  - Shortfall: " + shortfall + " students");
            GameLogger.logScheduling("  - Additional sections needed: " + additionalSectionsNeeded);
            GameLogger.logScheduling("  - Sections created: " + sections.size() + "/" + sectionsNeeded + " needed");

            if (isCoreSubject(className)) {
                GameLogger.logScheduling("  - CRITICAL: This is a CORE subject - students may not graduate!");
            }
        } else if (totalBlocksCreated < sectionsNeeded) {
            // We have capacity but fewer sections than optimal - classes will be larger
            GameLogger.logScheduling("NOTE: " + className + " has fewer sections than optimal (" +
                    totalBlocksCreated + "/" + sectionsNeeded +
                    ") - class sizes will exceed optimal of " + currentOptimalClassSize);
        }

        // Additional debugging for high-demand classes to show block distribution
        if (isHighDemand) {
            GameLogger.logScheduling("=== " + className + " Section Distribution ===");
            Map<String, Integer> blockDistribution = new HashMap<>();
            for (ClassSection section : sections) {
                String key = section.getTeacherBlock().getSemester() + " Block "
                        + section.getTeacherBlock().getBlockNumber();
                blockDistribution.put(key, blockDistribution.getOrDefault(key, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : blockDistribution.entrySet()) {
                GameLogger.logScheduling("  " + entry.getKey() + ": " + entry.getValue() + " section(s)");
            }
            GameLogger.logScheduling("=== End Distribution ===");
        }
    }

    // Track critical shortages for reporting
    private static final Map<String, ShortageInfo> criticalShortages = new HashMap<>();

    private static void trackShortage(String className, int demand, int sectionsNeeded, StaffType staffType) {
        criticalShortages.put(className, new ShortageInfo(className, demand, sectionsNeeded, staffType));
    }

    /**
     * Gets a summary of critical shortages for reporting.
     */
    public static Map<String, ShortageInfo> getCriticalShortages() {
        return new HashMap<>(criticalShortages);
    }

    /**
     * Clears tracked shortages (call before new scheduling run).
     */
    public static void clearShortages() {
        criticalShortages.clear();
    }

    /**
     * Information about a scheduling shortage.
     */
    public static class ShortageInfo {
        public final String className;
        public final int studentDemand;
        public final int sectionsNeeded;
        public final StaffType staffTypeRequired;

        public ShortageInfo(String className, int demand, int sections, StaffType type) {
            this.className = className;
            this.studentDemand = demand;
            this.sectionsNeeded = sections;
            this.staffTypeRequired = type;
        }

        @Override
        public String toString() {
            return className + ": " + studentDemand + " students need " + sectionsNeeded +
                    " sections (requires " + staffTypeRequired + ")";
        }
    }

    private static List<Staff> getQualifiedTeachers(String className, HashMap<Integer, Staff> staffHashMap) {
        return staffHashMap.values().stream()
                .filter(teacher -> teacher.teacherStatistics.getTeacherSchedule().getBlocksByClassName(className)
                        .size() > 0)
                .collect(Collectors.toList());
    }

    private static int calculateAverageRoomCapacity(List<Staff> teachers) {
        return teachers.stream()
                .mapToInt(teacher -> {
                    return teacher.teacherStatistics.getTeacherSchedule().getTeacherSchedule()
                            .stream().findFirst()
                            .map(block -> block.getRoom().getStudentCapacity())
                            .orElse(25);
                })
                .sum() / Math.max(1, teachers.size());
    }

    private static boolean hasBlockConflict(Student student, TeacherBlock block) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .anyMatch(studentBlock -> studentBlock.getBlockNumber() == block.getBlockNumber() &&
                        studentBlock.getSemester().equals(block.getSemester()));
    }

    private static Student findMovableStudent(ClassSection fromSection, ClassSection toSection) {
        for (Student student : fromSection.getEnrolledStudents()) {
            // Check for schedule conflicts
            if (!hasBlockConflict(student, toSection.getTeacherBlock())) {
                // *** NEW: Check for potential duplicates ***
                String className = toSection.getClassName();
                long currentCount = student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                        .mapToLong(block -> block.getClassName().equals(className) ? 1 : 0)
                        .sum();

                // Only consider students who don't already have duplicates of this class
                if (currentCount <= 1) {
                    return student;
                }
            }
        }
        return null;
    }

    private static void moveStudentBetweenSections(Student student, ClassSection fromSection, ClassSection toSection) {
        // *** CRITICAL FIX: Prevent moves that would create duplicates ***
        String className = toSection.getClassName();

        // Count how many times the student already has this class
        long currentCount = student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .mapToLong(block -> block.getClassName().equals(className) ? 1 : 0)
                .sum();

        if (currentCount > 1) {
            GameLogger.logScheduling("DUPLICATE PREVENTION: Blocking move of " + student.studentName.getFirstName() + " " +
                    student.studentName.getLastName() + " for " + className +
                    " - already has " + currentCount + " instances");
            return; // Don't move - would create/worsen duplicates
        }

        // Remove from old section
        fromSection.removeStudent(student);

        // Update student schedule
        List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();
        schedule.removeIf(block -> block.getClassName().equals(fromSection.getClassName()) &&
                block.getBlockNumber() == fromSection.getTeacherBlock().getBlockNumber() &&
                block.getSemester().equals(fromSection.getTeacherBlock().getSemester()));

        // Add to new section
        assignStudentToSection(student, toSection, false);

        GameLogger.logScheduling("Moved " + student.studentName.getFirstName() + " " +
                student.studentName.getLastName() + " from section " +
                fromSection.getTeacherBlock().getBlockNumber() + " to " +
                toSection.getTeacherBlock().getBlockNumber() + " for " +
                fromSection.getClassName());
    }

    private static List<String> getStudentClassesForSubject(Student student, String subjectArea) {
        List<String> studentClasses = determineStudentClasses(student);
        return studentClasses.stream()
                .filter(className -> belongsToSubjectArea(className, subjectArea))
                .collect(Collectors.toList());
    }

    /**
     * Determines if a class belongs to a specific subject area.
     * Keywords are aligned with
     * CurriculumRequirementsCalculator.mapClassToStaffType()
     * to ensure consistent classification across the scheduling system.
     */
    private static boolean belongsToSubjectArea(String className, String subjectArea) {
        String lowerName = className.toLowerCase();
        return switch (subjectArea.toLowerCase()) {
            case "english" ->
                lowerName.contains("english") || lowerName.contains("literature") ||
                        lowerName.contains("composition") || lowerName.contains("journalism");
            case "math" ->
                lowerName.contains("math") || lowerName.contains("algebra") ||
                        lowerName.contains("geometry") || lowerName.contains("calculus") ||
                        lowerName.contains("trigonometry") || lowerName.contains("precalculus") ||
                        lowerName.contains("statistics") || lowerName.contains("financial");
            case "science" ->
                lowerName.contains("biology") || lowerName.contains("chemistry") ||
                        lowerName.contains("physics") || lowerName.contains("science") ||
                        lowerName.contains("anatomy") || lowerName.contains("environmental") ||
                        lowerName.contains("genetics");
            case "history" ->
                lowerName.contains("history") || lowerName.contains("government") ||
                        lowerName.contains("geography") || lowerName.contains("economics") ||
                        lowerName.contains("civics");
            case "physical education" ->
                lowerName.contains("health") || lowerName.contains("sports") ||
                        lowerName.contains("weightlifting") || lowerName.contains("dance") ||
                        lowerName.contains("recreation") || lowerName.contains("physical education") ||
                        lowerName.contains("pe");
            case "language" ->
                lowerName.contains("spanish") || lowerName.contains("french") ||
                        lowerName.contains("german") || lowerName.contains("latin") ||
                        lowerName.contains("sign language") || lowerName.contains("asl");
            case "visual arts" ->
                lowerName.contains("art") || lowerName.contains("drawing") ||
                        lowerName.contains("painting") || lowerName.contains("sculpture") ||
                        lowerName.contains("ceramics") || lowerName.contains("photography");
            case "performing arts" ->
                lowerName.contains("band") || lowerName.contains("choir") ||
                        lowerName.contains("orchestra") || lowerName.contains("music") ||
                        lowerName.contains("drama") || lowerName.contains("theater") ||
                        lowerName.contains("theatre");
            case "computer science" ->
                lowerName.contains("computer") || lowerName.contains("programming") ||
                        lowerName.contains("coding") || lowerName.contains("technology") ||
                        lowerName.contains("keyboarding");
            case "vocational" ->
                lowerName.contains("woodworking") || lowerName.contains("auto") ||
                        lowerName.contains("shop") || lowerName.contains("culinary") ||
                        lowerName.contains("welding") || lowerName.contains("construction") ||
                        lowerName.contains("hvac") || lowerName.contains("electrical");
            case "business" ->
                lowerName.contains("business") || lowerName.contains("accounting") ||
                        lowerName.contains("marketing") || lowerName.contains("entrepreneurship");
            case "consumer science" ->
                lowerName.contains("home economics") || lowerName.contains("consumer") ||
                        lowerName.contains("family") || lowerName.contains("child development");
            default -> false;
        };
    }

    private static boolean isCoreSubject(String className) {
        String[] coreKeywords = { "English", "Math", "Science", "History", "Biology", "Chemistry",
                "Physics", "Algebra", "Geometry", "Calculus", "Government", "Geography" };
        return Arrays.stream(coreKeywords)
                .anyMatch(keyword -> className.toLowerCase().contains(keyword.toLowerCase()));
    }

    /**
     * Returns priority order for grade levels (lower number = higher priority)
     */
    private static int getGradePriority(String gradeLevel) {
        switch (gradeLevel) {
            case "Senior":
                return 1;
            case "Junior":
                return 2;
            case "Sophomore":
                return 3;
            case "Freshman":
                return 4;
            default:
                return 5;
        }
    }

    private static void assignElectivesWithBalancing(List<Student> students, HashMap<Integer, Staff> staffHashMap) {
        for (Student student : students) {
            List<String> vocationalClasses = determineVocationalClasses(student.studentStatistics.getGradeLevel(),
                    student);

            for (String className : vocationalClasses) {
                // *** DUPLICATE PREVENTION for electives ***
                if (studentAlreadyHasClass(student, className)) {
                    GameLogger.logScheduling("DUPLICATE PREVENTION: " + student.studentName.getFirstName() + " " +
                            student.studentName.getLastName() + " already has elective " + className +
                            " - skipping duplicate assignment");
                    continue;
                }

                if (classSections.containsKey(className)) {
                    ClassSection bestSection = findOptimalSection(student, className);
                    if (bestSection != null) {
                        assignStudentToSection(student, bestSection, true);
                    }
                }
            }
        }
    }

    private static void processWaitlists(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("Processing waitlists for cancelled classes...");

        for (Map.Entry<String, Set<Student>> entry : classWaitlists.entrySet()) {
            String cancelledClass = entry.getKey();
            Set<Student> waitlistedStudents = entry.getValue();

            GameLogger.logScheduling("Finding alternatives for " + waitlistedStudents.size() +
                    " students waitlisted for " + cancelledClass);

            // Try to find similar classes
            for (Student student : waitlistedStudents) {
                List<String> alternatives = findAlternativeClasses(cancelledClass, student);
                boolean assigned = false;

                for (String alternative : alternatives) {
                    if (classSections.containsKey(alternative)) {
                        ClassSection bestSection = findOptimalSection(student, alternative);
                        if (bestSection != null) {
                            assignStudentToSection(student, bestSection, true);
                            assigned = true;
                            break;
                        }
                    }
                }

                if (!assigned) {
                    GameLogger.logScheduling("Could not find alternative for " +
                            student.studentName.getFirstName() + " " +
                            student.studentName.getLastName() +
                            " (waitlisted for " + cancelledClass + ")");
                }
            }
        }
    }

    private static List<String> findAlternativeClasses(String cancelledClass, Student student) {
        List<String> alternatives = new ArrayList<>();

        // Basic subject mapping for alternatives
        if (cancelledClass.toLowerCase().contains("art")) {
            alternatives.addAll(Arrays.asList("2D Studio Art I", "Photography I", "Digital Production Technology"));
        } else if (cancelledClass.toLowerCase().contains("theater")) {
            alternatives.addAll(Arrays.asList("Debate", "Choir", "Film Production"));
        } else if (cancelledClass.toLowerCase().contains("music")) {
            alternatives.addAll(Arrays.asList("Choir", "Concert Band", "Jazz Band"));
        } else if (cancelledClass.toLowerCase().contains("programming")) {
            alternatives.addAll(Arrays.asList("Digital Production Technology", "Computer Aided Drafting I"));
        }

        return alternatives;
    }

    private static void printEnhancedStatistics() {
        GameLogger.logScheduling("\n=== Enhanced Scheduling Statistics ===");

        int totalSections = 0;
        int totalStudents = 0;
        int underEnrolledSections = 0;
        int cancelledClasses = classWaitlists.size();

        for (Map.Entry<String, List<ClassSection>> entry : classSections.entrySet()) {
            String className = entry.getKey();
            List<ClassSection> sections = entry.getValue();

            totalSections += sections.size();

            int classTotal = sections.stream()
                    .mapToInt(s -> s.getEnrolledStudents().size())
                    .sum();
            totalStudents += classTotal;

            double avgEnrollment = sections.isEmpty() ? 0 : (double) classTotal / sections.size();

            long underEnrolled = sections.stream()
                    .mapToInt(s -> s.getEnrolledStudents().size())
                    .filter(count -> count < MIN_CLASS_SIZE)
                    .count();

            underEnrolledSections += underEnrolled;

            GameLogger.logScheduling(className + ": " + sections.size() + " sections, " +
                    classTotal + " students, avg " + String.format("%.1f", avgEnrollment) + "/section" +
                    (underEnrolled > 0 ? " [" + underEnrolled + " under-enrolled]" : ""));
        }

        GameLogger.logScheduling("\nSummary:");
        GameLogger.logScheduling("Total sections created: " + totalSections);
        GameLogger.logScheduling("Total student assignments: " + totalStudents);
        GameLogger.logScheduling("Under-enrolled sections: " + underEnrolledSections);
        GameLogger.logScheduling("Cancelled classes: " + cancelledClasses);
        GameLogger.logScheduling("Success rate: " + String.format("%.1f",
                100.0 * (totalSections - underEnrolledSections) / totalSections)
                + "% sections meet minimum enrollment");
    }

    // Inner classes for tracking
    private static class ClassSection {
        private final String className;
        private final Staff teacher;
        private final TeacherBlock teacherBlock;
        private final int capacity;
        private final Set<Student> enrolledStudents;

        public ClassSection(String className, Staff teacher, TeacherBlock teacherBlock, int capacity) {
            this.className = className;
            this.teacher = teacher;
            this.teacherBlock = teacherBlock;
            this.capacity = capacity;
            this.enrolledStudents = new HashSet<>();
        }

        public void addStudent(Student student) {
            enrolledStudents.add(student);
        }

        public void removeStudent(Student student) {
            enrolledStudents.remove(student);
        }

        public boolean isFull() {
            return enrolledStudents.size() >= capacity;
        }

        public String getClassName() {
            return className;
        }

        public Staff getTeacher() {
            return teacher;
        }

        public TeacherBlock getTeacherBlock() {
            return teacherBlock;
        }

        public Set<Student> getEnrolledStudents() {
            return enrolledStudents;
        }
    }

    private record StudentDemand(String className, int totalDemand, Set<Student> interestedStudents) {
    }

    private static String classProbabilityLoader(int intelligence, String income, int determination) {
        int random = Randomizer.setRandom(0, CLASS_PROBABILITY_LOADER_SAMPLE_SIZE);
        double apProbability;
        double honorsProbability;
        double onLevelProbability;

        // Base probabilities based on intelligence (EXACT SAME LOGIC)
        if (intelligence >= CLASS_PROBABILITY_LOADER_GIFTED_INTELLIGENCE_THRESHOLD) {
            apProbability = CLASS_PROBABILITY_LOADER_GIFTED_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_GIFTED_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_GIFTED_ON_LEVEL_PROBABILITY;
        } else if (intelligence >= CLASS_PROBABILITY_LOADER_HIGH_INTELLIGENCE_THRESHOLD) {
            apProbability = CLASS_PROBABILITY_LOADER_HIGH_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_HIGH_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_HIGH_ON_LEVEL_PROBABILITY;
        } else if (intelligence >= CLASS_PROBABILITY_LOADER_AVERAGE_INTELLIGENCE_THRESHOLD) {
            apProbability = CLASS_PROBABILITY_LOADER_AVERAGE_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_AVERAGE_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_AVERAGE_ON_LEVEL_PROBABILITY;
        } else if (intelligence <= CLASS_PROBABILITY_LOADER_LOW_INTELLIGENCE_THRESHOLD) {
            apProbability = CLASS_PROBABILITY_LOADER_LOW_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_LOW_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_LOW_ON_LEVEL_PROBABILITY;
        } else {
            apProbability = CLASS_PROBABILITY_LOADER_OTHER_AP_PROBABILITY;
            honorsProbability = CLASS_PROBABILITY_LOADER_OTHER_HONORS_PROBABILITY;
            onLevelProbability = CLASS_PROBABILITY_LOADER_OTHER_ON_LEVEL_PROBABILITY;
        }

        // Income adjustments (EXACT SAME LOGIC)
        switch (income) {
            case "high" -> {
                apProbability *= CLASS_PROBABILITY_LOADER_INCOME_HIGH_AP_ADJUSTMENT;
                honorsProbability *= CLASS_PROBABILITY_LOADER_INCOME_HIGH_HONORS_ADJUSTMENT;
                onLevelProbability *= CLASS_PROBABILITY_LOADER_INCOME_HIGH_ON_LEVEL_ADJUSTMENT;
            }
            case "low" -> {
                apProbability *= CLASS_PROBABILITY_LOADER_INCOME_LOW_AP_ADJUSTMENT;
                honorsProbability *= CLASS_PROBABILITY_LOADER_INCOME_LOW_HONORS_ADJUSTMENT;
                onLevelProbability *= CLASS_PROBABILITY_LOADER_INCOME_LOW_ON_LEVEL_ADJUSTMENT;
            }
        }

        // Determination adjustments (EXACT SAME LOGIC)
        double determinationFactor = (determination - CLASS_PROBABILITY_LOADER_DETERMINATION_THRESHOLD)
                / CLASS_PROBABILITY_LOADER_DETERMINATION_FACTOR_DIVISOR;
        apProbability += apProbability * determinationFactor;
        honorsProbability += honorsProbability * determinationFactor
                / CLASS_PROBABILITY_LOADER_DETERMINATION_HONORS_ADJUSTMENT;
        onLevelProbability -= onLevelProbability * determinationFactor
                / CLASS_PROBABILITY_LOADER_DETERMINATION_ON_LEVEL_ADJUSTMENT;

        // Normalize and determine (EXACT SAME LOGIC)
        double totalProbability = apProbability + honorsProbability + onLevelProbability;
        apProbability = (apProbability / totalProbability) * 100;
        honorsProbability = (honorsProbability / totalProbability) * 100;

        if (random < apProbability) {
            return "AP";
        } else if (random < apProbability + honorsProbability) {
            return "Honors";
        } else {
            return "On-Level";
        }
    }

    /**
     * Finds the optimal section for a student (least filled, no conflicts)
     */
    private static ClassSection findOptimalSection(Student student, String className) {
        List<ClassSection> sections = classSections.get(className);
        if (sections == null || sections.isEmpty())
            return null;

        ClassSection bestSection = null;
        int minEnrollment = Integer.MAX_VALUE;

        for (ClassSection section : sections) {
            // Check for schedule conflicts
            if (hasBlockConflict(student, section.getTeacherBlock())) {
                continue;
            }

            // Check capacity
            if (section.isFull()) {
                continue;
            }

            // Prefer sections with fewer students (load balancing)
            int currentEnrollment = section.getEnrolledStudents().size();
            if (currentEnrollment < minEnrollment) {
                minEnrollment = currentEnrollment;
                bestSection = section;
            }
        }

        return bestSection;
    }

    /**
     * Enhanced assignment that preserves existing logic structure
     * NOW WITH DUPLICATE PREVENTION AT THE CORE
     */
    private static void assignStudentToSection(Student student, ClassSection section, boolean logAssignment) {
        String className = section.getClassName();

        // *** CRITICAL FIX: Prevent duplicate assignments at the source ***
        if (studentAlreadyHasClass(student, className)) {
            if (logAssignment) {
                GameLogger.logScheduling("DUPLICATE PREVENTION: " + student.studentName.getFirstName() + " " +
                        student.studentName.getLastName() + " already has " + className +
                        " - blocking assignment");
            }
            return; // Don't assign - student already has this class
        }

        // Create student block (same as existing logic)
        StudentBlock studentBlock = new StudentBlock();
        studentBlock.setBlockNumber(section.getTeacherBlock().getBlockNumber());
        studentBlock.setClassName(section.getClassName());
        studentBlock.setTeacher(section.getTeacher());
        studentBlock.setSemester(section.getTeacherBlock().getSemester());
        studentBlock.setRoom(section.getTeacherBlock().getRoom());

        // Add to student schedule
        student.studentStatistics.getStudentSchedule().add(studentBlock);

        // Update section tracking
        section.addStudent(student);
        section.getTeacherBlock().addStudentToBlock(student);

        if (logAssignment) {
            GameLogger.logScheduling("Assigned " + section.getClassName() + " to " +
                    student.studentName.getFirstName() + " " +
                    student.studentName.getLastName() + " with " +
                    section.getTeacher().teacherName.getFirstName() + " " +
                    section.getTeacher().teacherName.getLastName() +
                    " in room " + section.getTeacherBlock().getRoom().getRoomName() +
                    " (section enrollment: " + section.getEnrolledStudents().size() + ")");
        }
    }

    /**
     * Load balancing after initial assignment
     */
    private static void balanceClassSizes() {
        GameLogger.logScheduling("Balancing class sizes...");

        for (Map.Entry<String, List<ClassSection>> entry : classSections.entrySet()) {
            String className = entry.getKey();
            List<ClassSection> sections = entry.getValue();

            if (sections.size() <= 1)
                continue;

            balanceSectionsForClass(className, sections);
        }
    }

    private static void balanceSectionsForClass(String className, List<ClassSection> sections) {
        // Calculate statistics
        int totalEnrolled = sections.stream()
                .mapToInt(s -> s.getEnrolledStudents().size())
                .sum();
        double averageEnrollment = (double) totalEnrolled / sections.size();

        // Move students from over-enrolled to under-enrolled sections
        for (int attempt = 0; attempt < MAX_OPTIMIZATION_ATTEMPTS; attempt++) {
            boolean madeChanges = false;

            sections.sort((s1, s2) -> Integer.compare(
                    s2.getEnrolledStudents().size(),
                    s1.getEnrolledStudents().size()));

            for (int i = 0; i < sections.size() - 1; i++) {
                ClassSection overSection = sections.get(i);
                ClassSection underSection = sections.get(sections.size() - 1 - i);

                if (overSection.getEnrolledStudents().size() <= averageEnrollment)
                    break;
                if (underSection.getEnrolledStudents().size() >= averageEnrollment)
                    break;

                // Try to move a student
                Student movableStudent = findMovableStudent(overSection, underSection);
                if (movableStudent != null) {
                    moveStudentBetweenSections(movableStudent, overSection, underSection);
                    madeChanges = true;
                }
            }

            if (!madeChanges)
                break;
        }
    }

    private static String[] vocationalDecision(Student student, String semester) {
        String[] choiceRank = new String[8];
        int determination = student.studentStatistics.getDetermination();
        int charisma = student.studentStatistics.getCharisma();
        int creativity = student.studentStatistics.getCreativity();
        int perception = student.studentStatistics.getPerception();
        int intelligence = student.studentStatistics.getIntelligence();
        int curiosity = student.studentStatistics.getCuriosity();
        String year = student.studentStatistics.getGradeLevel();

        if (semester.equals("Fall")) {
            // If someone has high charisma, better than average determination and
            // understands themselves better than the average person
            if (charisma > CHARISMA_VOCATIONAL_LOWER_BOUND && determination > DETERMINATION_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Theater I";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Musical Theater I";
                        choiceRank[3] = "Dance Techniques I";
                        choiceRank[4] = "Choir";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Introduction to Business";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Business Management";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Debate";
                        choiceRank[1] = "Choir";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Concert Band";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "ROTC";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "Business Management";
                    }
                }
                // If someone has high creativity and better than average perception
            } else if (creativity > CREATIVITY_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "2D Studio Art I";
                        choiceRank[1] = "Photography I";
                        choiceRank[2] = "3D Studio Art I";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "3D Studio Art I";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Photography I";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Theater Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Photography I";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If determination and intelligence are high and perception is better than
                // average
            } else if (determination > DETERMINATION_VOCATIONAL_LOWER_BOUND_BAND
                    && intelligence > INTELLIGENCE_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Computer Aided Drafting I";
                        choiceRank[4] = "Intro to Programming";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "ROTC";
                        choiceRank[7] = "Auto Body Repair";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "AP Music Theory";
                        choiceRank[4] = "AP Philosophy";
                        choiceRank[5] = "Intro to Programming";
                        choiceRank[6] = "Spanish III";
                        choiceRank[7] = "Debate";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "AP Spanish Language";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If curiosity is high and intelligence are above average
            } else if (curiosity > CURIOSITY_VOCATIONAL_LOWER_BOUND
                    && intelligence > INTELLIGENCE_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting I";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting I";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "AP Music Theory";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Spanish III";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Philosophy";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "AP Spanish Language";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting I";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art I";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If someone is lacking determination
            } else if (determination < LOW_DETERMINATION_VOCATIONAL_UPPER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Home Economics";
                        choiceRank[2] = "Woodworking";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art I";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art I";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            } else {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Team Sports";
                        choiceRank[2] = "Specialized Sports";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art I";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art I";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art I";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art I";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography I";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            }
        } else {
            // If someone has high charisma, better than average determination and
            // understands themselves better than the average person
            if (charisma > CHARISMA_VOCATIONAL_LOWER_BOUND && determination > DETERMINATION_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Theater II";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Musical Theater II";
                        choiceRank[3] = "Dance Techniques II";
                        choiceRank[4] = "Choir";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Theater IV";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Digital Production Technology";
                        choiceRank[5] = "Entrepreneurial Skills";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Theater III";
                        choiceRank[1] = "Debate";
                        choiceRank[2] = "Choir";
                        choiceRank[3] = "ROTC";
                        choiceRank[4] = "Digital Production Technology";
                        choiceRank[5] = "Marketing";
                        choiceRank[6] = "Marching Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Debate";
                        choiceRank[1] = "Choir";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Concert Band";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "ROTC";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "Marketing";
                    }
                }
                // If someone has high creativity and better than average perception
            } else if (creativity > CREATIVITY_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "2D Studio Art II";
                        choiceRank[1] = "Photography II";
                        choiceRank[2] = "3D Studio Art II";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "3D Studio Art II";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Photography II";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Film Production";
                        choiceRank[5] = "Theater Technology";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Photography II";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "AP Studio History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If determination and intelligence are high and perception is better than
                // average
            } else if (determination > DETERMINATION_VOCATIONAL_LOWER_BOUND_BAND
                    && intelligence > INTELLIGENCE_VOCATIONAL_LOWER_BOUND
                    && perception > INTELLIGENCE_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "Computer Aided Drafting II";
                        choiceRank[4] = "Intro to Programming";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "ROTC";
                        choiceRank[7] = "Auto Body Repair";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Jazz Band";
                        choiceRank[1] = "Concert Band";
                        choiceRank[2] = "Marching Band";
                        choiceRank[3] = "AP Music Theory";
                        choiceRank[4] = "AP Philosophy";
                        choiceRank[5] = "Intro to Programming";
                        choiceRank[6] = "AP Spanish Literature";
                        choiceRank[7] = "Debate";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "AP Spanish Language";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If curiosity is high and intelligence are above average
            } else if (curiosity > CURIOSITY_VOCATIONAL_LOWER_BOUND
                    && intelligence > INTELLIGENCE_VOCATIONAL_LOWER_BOUND
                    && perception > PERCEPTION_VOCATIONAL_LOWER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting II";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Debate";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Philosophy";
                        choiceRank[1] = "Digital Production Technology";
                        choiceRank[2] = "Computer Aided Drafting II";
                        choiceRank[3] = "Intro to Programming";
                        choiceRank[4] = "AP Music Theory";
                        choiceRank[5] = "AP Spanish Literature";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "AP Music Theory";
                        choiceRank[1] = "Philosophy";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "AP Spanish Language";
                        choiceRank[4] = "AP Art History";
                        choiceRank[5] = "Computer Aided Drafting II";
                        choiceRank[6] = "Jazz Band";
                        choiceRank[7] = "Concert Band";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "Film Production";
                        choiceRank[2] = "2D Studio Art II";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Printmaking";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "AP Art History";
                        choiceRank[7] = "Theater Technology";
                    }
                }
                // If someone is lacking determination
            } else if (determination < LOW_DETERMINATION_VOCATIONAL_UPPER_BOUND) {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Home Economics";
                        choiceRank[2] = "Woodworking";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art II";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art II";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            } else {
                switch (year) {
                    case "Sophomore" -> {
                        choiceRank[0] = "Keyboarding";
                        choiceRank[1] = "Team Sports";
                        choiceRank[2] = "Specialized Sports";
                        choiceRank[3] = "Auto Body Repair";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Culinary Arts";
                        choiceRank[6] = "Digital Production Technology";
                        choiceRank[7] = "2D Studio Art II";
                    }
                    case "Junior" -> {
                        choiceRank[0] = "Home Economics";
                        choiceRank[1] = "Woodworking";
                        choiceRank[2] = "Auto Body Repair";
                        choiceRank[3] = "Keyboarding";
                        choiceRank[4] = "Culinary Arts";
                        choiceRank[5] = "Digital Production Technology";
                        choiceRank[6] = "2D Studio Art II";
                        choiceRank[7] = "Theater Technology";
                    }
                    case "Senior" -> {
                        choiceRank[0] = "Woodworking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Culinary Arts";
                        choiceRank[3] = "Printmaking";
                        choiceRank[4] = "Theater Technology";
                        choiceRank[5] = "Auto Body Repair";
                        choiceRank[6] = "Printmaking";
                        choiceRank[7] = "Keyboarding";
                    }
                    default -> {
                        choiceRank[0] = "Printmaking";
                        choiceRank[1] = "2D Studio Art II";
                        choiceRank[2] = "Theater Technology";
                        choiceRank[3] = "3D Studio Art II";
                        choiceRank[4] = "Keyboarding";
                        choiceRank[5] = "Photography II";
                        choiceRank[6] = "Culinary Arts";
                        choiceRank[7] = "Woodworking";
                    }
                }
            }
        }
        return choiceRank;
    }

    /**
     * Extracts language base from class name (e.g., "French I" -> "French")
     */
    private static String getLanguageBase(String className) {
        if (className.contains(" I")) {
            return className.substring(0, className.indexOf(" I"));
        } else if (className.contains(" II")) {
            return className.substring(0, className.indexOf(" II"));
        }
        return className;
    }

    /**
     * Optimizes block assignments within subject areas by reassigning underutilized
     * blocks to high-demand classes where the same teacher can teach both.
     * This method runs AFTER student assignment to identify truly empty sections.
     */
    private static void optimizeBlockAssignmentsWithinSubjects(HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("=== BLOCK ASSIGNMENT OPTIMIZATION WITHIN SUBJECT AREAS ===");

        // Define all subject areas to optimize (aligned with belongsToSubjectArea)
        String[] subjectAreas = {
                "English", "Math", "Science", "History", "Language",
                "Physical Education", "Visual Arts", "Performing Arts",
                "Computer Science", "Vocational", "Business", "Consumer Science"
        };

        for (String subjectArea : subjectAreas) {
            GameLogger.logScheduling("Optimizing " + subjectArea + " block assignments...");
            optimizeSubjectArea(subjectArea, studentHashMap, staffHashMap);
        }

        GameLogger.logScheduling("=== END BLOCK OPTIMIZATION ===");
    }

    /**
     * Optimizes block assignments for a specific subject area
     */
    private static void optimizeSubjectArea(String subjectArea, HashMap<Integer, Student> studentHashMap,
            HashMap<Integer, Staff> staffHashMap) {

        // Step 1: Get all classes in this subject area
        List<String> subjectClasses = getClassesInSubjectArea(subjectArea);

        if (subjectClasses.isEmpty()) {
            GameLogger.logScheduling("No classes found for " + subjectArea);
            return;
        }

        // Step 2: Analyze utilization for each class
        List<ClassUtilization> utilizations = new ArrayList<>();

        for (String className : subjectClasses) {
            StudentDemand demand = demandTracker.get(className);
            List<ClassSection> sections = classSections.get(className);

            if (demand != null) {
                int totalCapacity = 0;
                int currentEnrollment = 0;
                int emptyBlocks = 0;

                if (sections != null) {
                    totalCapacity = sections.stream().mapToInt(s -> s.capacity).sum();
                    currentEnrollment = sections.stream().mapToInt(s -> s.getEnrolledStudents().size()).sum();

                    // Count ONLY completely empty blocks (0 students) for reassignment
                    // Respect academic tracks - don't disrupt students who chose their level
                    for (ClassSection section : sections) {
                        if (section.getEnrolledStudents().size() == 0) { // Completely empty only
                            emptyBlocks++;
                        }
                    }
                }

                ClassUtilization util = new ClassUtilization(
                        className,
                        demand.totalDemand(),
                        totalCapacity,
                        currentEnrollment,
                        emptyBlocks);

                utilizations.add(util);
            }
        }

        // Step 3: Display analysis
        GameLogger.logScheduling("=== " + subjectArea.toUpperCase() + " UTILIZATION ANALYSIS ===");
        for (ClassUtilization util : utilizations) {
            double utilizationPercent = util.totalCapacity > 0
                    ? (double) util.currentEnrollment / util.totalCapacity * 100
                    : 0;

            GameLogger.logScheduling(String.format(
                    "%s: Demand=%d, Capacity=%d, Enrolled=%d (%.1f%%), Empty blocks=%d",
                    util.className, util.demand, util.totalCapacity, util.currentEnrollment,
                    utilizationPercent, util.emptyBlocks));
        }

        // Step 4: Find optimization opportunities
        List<BlockReassignmentOpportunity> opportunities = findReassignmentOpportunities(utilizations, subjectArea,
                staffHashMap);

        // Step 5: Execute reassignments
        for (BlockReassignmentOpportunity opportunity : opportunities) {
            executeBlockReassignment(opportunity, staffHashMap);
        }

        // Step 6: Additional rebalancing using StaffType-based flexibility
        rebalanceEmptySectionsWithStaffType(subjectArea, utilizations, staffHashMap);
    }

    /**
     * Rebalances empty sections by finding ANY teacher in the discipline who can
     * teach the high-demand class, not just those who already have blocks in both.
     * This provides greater flexibility for teacher assignments.
     */
    private static void rebalanceEmptySectionsWithStaffType(String subjectArea,
            List<ClassUtilization> utilizations,
            HashMap<Integer, Staff> staffHashMap) {

        // Find sections that are still empty after initial optimization
        List<ClassUtilization> stillEmpty = utilizations.stream()
                .filter(u -> u.emptyBlocks > 0)
                .sorted((u1, u2) -> Integer.compare(u2.emptyBlocks, u1.emptyBlocks))
                .collect(Collectors.toList());

        // Find classes that still have unmet demand
        List<ClassUtilization> stillOverdemanded = utilizations.stream()
                .filter(u -> u.demand > u.currentEnrollment)
                .sorted((u1, u2) -> Integer.compare(
                        (u2.demand - u2.currentEnrollment),
                        (u1.demand - u1.currentEnrollment)))
                .collect(Collectors.toList());

        if (stillEmpty.isEmpty() || stillOverdemanded.isEmpty()) {
            return;
        }

        GameLogger.logScheduling("=== STAFFTYPE-BASED REBALANCING FOR " + subjectArea.toUpperCase() + " ===");
        GameLogger.logScheduling("Still empty sections: " + stillEmpty.stream()
                .mapToInt(u -> u.emptyBlocks).sum());
        GameLogger.logScheduling("Classes with unmet demand: " + stillOverdemanded.size());

        int totalReassigned = 0;

        for (ClassUtilization overdemand : stillOverdemanded) {
            if (overdemand.demand <= overdemand.currentEnrollment) {
                continue; // Demand is now met
            }

            // Find the StaffType that can teach this class
            StaffType targetType = CurriculumRequirementsCalculator.mapClassToStaffType(overdemand.className);

            for (ClassUtilization underutil : stillEmpty) {
                if (underutil.emptyBlocks <= 0) {
                    continue;
                }

                // Find teachers in this discipline who have empty blocks in underutil class
                List<Staff> teachersWithEmptyBlocks = findTeachersWithEmptyBlocksInDiscipline(
                        underutil.className, targetType, staffHashMap);

                for (Staff teacher : teachersWithEmptyBlocks) {
                    if (underutil.emptyBlocks <= 0 ||
                            overdemand.demand <= overdemand.currentEnrollment) {
                        break;
                    }

                    // Find an empty block from this teacher
                    TeacherBlock emptyBlock = findEmptyBlockForClass(teacher, underutil.className);

                    if (emptyBlock != null) {
                        // Reassign this block to the high-demand class
                        String oldClassName = emptyBlock.getClassName();
                        emptyBlock.setClassName(overdemand.className);

                        GameLogger.logScheduling("StaffType-Reassigned: " + teacher.teacherName.getFirstName() + " " +
                                teacher.teacherName.getLastName() + "'s " + emptyBlock.getSemester() +
                                " Block " + emptyBlock.getBlockNumber() + " from " + oldClassName +
                                " to " + overdemand.className);

                        underutil.emptyBlocks--;
                        totalReassigned++;

                        // Create a new section for the reassigned block
                        ClassSection newSection = new ClassSection(
                                overdemand.className,
                                teacher,
                                emptyBlock,
                                emptyBlock.getBlockPopulation());

                        classSections.computeIfAbsent(overdemand.className, k -> new ArrayList<>())
                                .add(newSection);
                    }
                }
            }
        }

        if (totalReassigned > 0) {
            GameLogger.logScheduling("Total blocks reassigned via StaffType flexibility: " + totalReassigned);
        }
    }

    /**
     * Finds teachers in a specific discipline who have empty blocks for a given
     * class.
     */
    private static List<Staff> findTeachersWithEmptyBlocksInDiscipline(String className, StaffType targetType,
            HashMap<Integer, Staff> staffHashMap) {
        List<Staff> result = new ArrayList<>();

        for (Staff staff : staffHashMap.values()) {
            StaffType staffType = (StaffType) staff.teacherStatistics.getStaffType();

            // Teacher must be in the target discipline
            if (staffType != targetType) {
                continue;
            }

            // Check if teacher has empty blocks for this class
            List<TeacherBlock> blocks = staff.teacherStatistics.getTeacherSchedule()
                    .getBlocksByClassName(className);

            for (TeacherBlock block : blocks) {
                List<ClassSection> sections = classSections.get(className);
                if (sections != null) {
                    for (ClassSection section : sections) {
                        if (section.getTeacherBlock().equals(block) &&
                                section.getEnrolledStudents().size() == 0) {
                            result.add(staff);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Finds an empty block for a specific class taught by the given teacher.
     */
    private static TeacherBlock findEmptyBlockForClass(Staff teacher, String className) {
        List<TeacherBlock> blocks = teacher.teacherStatistics.getTeacherSchedule()
                .getBlocksByClassName(className);

        for (TeacherBlock block : blocks) {
            List<ClassSection> sections = classSections.get(className);
            if (sections != null) {
                for (ClassSection section : sections) {
                    if (section.getTeacherBlock().equals(block) &&
                            section.getEnrolledStudents().size() == 0) {
                        // Remove this section from the old class
                        sections.remove(section);
                        return block;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets all classes that belong to a subject area from current demand tracker
     */
    private static List<String> getClassesInSubjectArea(String subjectArea) {
        return demandTracker.keySet().stream()
                .filter(className -> belongsToSubjectArea(className, subjectArea))
                .collect(Collectors.toList());
    }

    /**
     * Finds opportunities to reassign blocks from low-utilization to high-demand
     * classes
     */
    private static List<BlockReassignmentOpportunity> findReassignmentOpportunities(
            List<ClassUtilization> utilizations, String subjectArea, HashMap<Integer, Staff> staffHashMap) {

        List<BlockReassignmentOpportunity> opportunities = new ArrayList<>();

        // Sort by utilization - find classes with empty blocks and classes with unmet
        // demand
        List<ClassUtilization> underutilized = utilizations.stream()
                .filter(u -> u.emptyBlocks > 0)
                .sorted((u1, u2) -> Integer.compare(u2.emptyBlocks, u1.emptyBlocks)).toList();

        List<ClassUtilization> overdemanded = utilizations.stream()
                .filter(u -> u.demand > u.totalCapacity)
                .sorted((u1, u2) -> Integer.compare((u2.demand - u2.totalCapacity), (u1.demand - u1.totalCapacity)))
                .toList();

        GameLogger.logScheduling("Classes with empty blocks: " + underutilized.size());
        GameLogger.logScheduling("Classes with unmet demand: " + overdemanded.size());

        // Try to match underutilized blocks with overdemanded classes
        for (ClassUtilization overdemand : overdemanded) {
            for (ClassUtilization underutil : underutilized) {
                if (underutil.emptyBlocks > 0) {
                    // Check if teachers who teach underutil.className can also teach
                    // overdemand.className
                    List<Staff> sharedTeachers = findTeachersWhoCanTeachBoth(underutil.className, overdemand.className,
                            staffHashMap);

                    if (!sharedTeachers.isEmpty()) {
                        int blocksToReassign = Math.min(underutil.emptyBlocks,
                                Math.min(sharedTeachers.size(),
                                        (overdemand.demand - overdemand.totalCapacity + 24) / 25)); // Assume 25
                                                                                                    // students per
                                                                                                    // block

                        if (blocksToReassign > 0) {
                            BlockReassignmentOpportunity opportunity = new BlockReassignmentOpportunity(
                                    underutil.className,
                                    overdemand.className,
                                    blocksToReassign,
                                    sharedTeachers.subList(0, Math.min(blocksToReassign, sharedTeachers.size())));

                            opportunities.add(opportunity);
                            underutil.emptyBlocks -= blocksToReassign; // Update for next iteration

                            GameLogger.logScheduling("Found opportunity: Reassign " + blocksToReassign +
                                    " blocks from " + underutil.className + " to " + overdemand.className);
                        }
                    }
                }
            }
        }

        return opportunities;
    }

    /**
     * Finds teachers who can teach both classes based on discipline flexibility.
     * A teacher is considered qualified if:
     * 1. They have empty blocks for the fromClass, AND
     * 2. They are in the same discipline (StaffType) as required by the toClass
     *
     * This allows ANY teacher in the discipline to be reassigned, not just those
     * who were originally assigned to both classes.
     */
    private static List<Staff> findTeachersWhoCanTeachBoth(String fromClass, String toClass,
            HashMap<Integer, Staff> staffHashMap) {
        List<Staff> qualifiedTeachers = new ArrayList<>();

        // Determine the StaffType required for the target class
        StaffType toClassType = CurriculumRequirementsCalculator.mapClassToStaffType(toClass);
        StaffType fromClassType = CurriculumRequirementsCalculator.mapClassToStaffType(fromClass);

        for (Staff teacher : staffHashMap.values()) {
            StaffType teacherType = (StaffType) teacher.teacherStatistics.getStaffType();

            // Skip if teacher is not in a compatible discipline
            // Teacher must be able to teach both classes (same discipline or substitute)
            boolean canTeachFromClass = (teacherType == fromClassType || teacherType == StaffType.SUB);
            boolean canTeachToClass = (teacherType == toClassType || teacherType == StaffType.SUB);

            if (!canTeachFromClass || !canTeachToClass) {
                continue;
            }

            // Check if teacher has empty blocks for the fromClass
            List<TeacherBlock> fromBlocks = teacher.teacherStatistics.getTeacherSchedule()
                    .getBlocksByClassName(fromClass);

            boolean hasEmptyBlocks = false;
            for (TeacherBlock block : fromBlocks) {
                List<ClassSection> sections = classSections.get(fromClass);
                if (sections != null) {
                    for (ClassSection section : sections) {
                        if (section.getTeacherBlock().equals(block) &&
                                section.getEnrolledStudents().size() == 0) {
                            hasEmptyBlocks = true;
                            break;
                        }
                    }
                }
                if (hasEmptyBlocks)
                    break;
            }

            if (hasEmptyBlocks) {
                qualifiedTeachers.add(teacher);
            }
        }

        return qualifiedTeachers;
    }

    /**
     * Executes a block reassignment by modifying teacher schedules
     */
    private static void executeBlockReassignment(BlockReassignmentOpportunity opportunity,
            HashMap<Integer, Staff> staffHashMap) {
        GameLogger.logScheduling("Executing reassignment: " + opportunity.blocksToReassign +
                " blocks from " + opportunity.fromClass + " to " + opportunity.toClass);

        int blocksReassigned = 0;

        for (Staff teacher : opportunity.availableTeachers) {
            if (blocksReassigned >= opportunity.blocksToReassign)
                break;

            // Find underutilized blocks for the fromClass
            List<TeacherBlock> fromBlocks = teacher.teacherStatistics.getTeacherSchedule()
                    .getBlocksByClassName(opportunity.fromClass);

            for (TeacherBlock block : fromBlocks) {
                if (blocksReassigned >= opportunity.blocksToReassign)
                    break;

                // Check if this block has low utilization
                List<ClassSection> fromSections = classSections.get(opportunity.fromClass);
                if (fromSections != null) {
                    ClassSection correspondingSection = fromSections.stream()
                            .filter(s -> s.getTeacherBlock().equals(block))
                            .findFirst()
                            .orElse(null);

                    if (correspondingSection != null &&
                            correspondingSection.getEnrolledStudents().size() == 0) {

                        // Reassign this completely empty block only
                        // Respects academic tracks - students who chose AP Human Geography keep their
                        // class
                        reassignTeacherBlock(teacher, block, opportunity.fromClass, opportunity.toClass);
                        blocksReassigned++;

                        GameLogger.logScheduling("Reassigned " + teacher.teacherName.getFirstName() + " " +
                                teacher.teacherName.getLastName() + "'s " + block.getSemester() +
                                " Block " + block.getBlockNumber() + " from " + opportunity.fromClass +
                                " to " + opportunity.toClass + " (was completely empty)");
                    }
                }
            }
        }

        // Recreate sections for both classes
        if (blocksReassigned > 0) {
            GameLogger.logScheduling("Successfully reassigned " + blocksReassigned + " blocks");

            // Update sections for both classes
            StudentDemand fromDemand = demandTracker.get(opportunity.fromClass);
            StudentDemand toDemand = demandTracker.get(opportunity.toClass);

            if (fromDemand != null) {
                classSections.remove(opportunity.fromClass);
                createSectionsForClass(opportunity.fromClass, fromDemand, staffHashMap);
            }

            if (toDemand != null) {
                classSections.remove(opportunity.toClass);
                createSectionsForClass(opportunity.toClass, toDemand, staffHashMap);
            }
        }
    }

    /**
     * Reassigns a specific teacher block from one class to another
     */
    private static void reassignTeacherBlock(Staff teacher, TeacherBlock block, String fromClass, String toClass) {
        // This would typically involve modifying the teacher's schedule
        // For now, we'll change the class name associated with the block
        block.setClassName(toClass);
    }

    /**
     * Helper classes for block optimization
     */
    private static class ClassUtilization {
        final String className;
        final int demand;
        final int totalCapacity;
        final int currentEnrollment;
        int emptyBlocks; // Mutable for optimization calculations
        ClassUtilization(String className, int demand, int totalCapacity,
                int currentEnrollment, int emptyBlocks) {
            this.className = className;
            this.demand = demand;
            this.totalCapacity = totalCapacity;
            this.currentEnrollment = currentEnrollment;
            this.emptyBlocks = emptyBlocks;
        }
    }

    private record BlockReassignmentOpportunity(String fromClass, String toClass, int blocksToReassign,
            List<Staff> availableTeachers) {
    }

    /**
     * Checks if a student already has a specific class in their schedule
     */
    private static boolean studentAlreadyHasClass(Student student, String className) {
        return student.studentStatistics.getStudentSchedule().getClassSchedule().stream()
                .anyMatch(block -> block.getClassName().equals(className));
    }

    /**
     * Comprehensive duplicate detection and reporting
     */
    private static void detectAndReportDuplicates(HashMap<Integer, Student> studentHashMap) {
        GameLogger.logScheduling("=== FINAL DUPLICATE DETECTION CHECK ===");

        int studentsWithDuplicates = 0;
        int totalDuplicates = 0;

        for (Student student : studentHashMap.values()) {
            Map<String, Integer> classCount = new HashMap<>();
            List<StudentBlock> schedule = student.studentStatistics.getStudentSchedule().getClassSchedule();

            // Count occurrences of each class
            for (StudentBlock block : schedule) {
                String className = block.getClassName();
                classCount.put(className, classCount.getOrDefault(className, 0) + 1);
            }

            // Check for duplicates
            boolean hasDuplicates = false;
            for (Map.Entry<String, Integer> entry : classCount.entrySet()) {
                if (entry.getValue() > 1) {
                    if (!hasDuplicates) {
                        studentsWithDuplicates++;
                        hasDuplicates = true;
                        GameLogger.logScheduling("DUPLICATE DETECTED: " + student.studentName.getFirstName() + " " +
                                student.studentName.getLastName() + " (" +
                                student.studentStatistics.getGradeLevel() + ")");
                    }
                    GameLogger.logScheduling("  - " + entry.getKey() + ": " + entry.getValue() + " instances");
                    totalDuplicates += (entry.getValue() - 1); // Count extra instances
                }
            }
        }

        GameLogger.logScheduling("DUPLICATE SUMMARY:");
        GameLogger.logScheduling("Students with duplicates: " + studentsWithDuplicates + "/" + studentHashMap.size());
        GameLogger.logScheduling("Total duplicate assignments: " + totalDuplicates);

        if (studentsWithDuplicates == 0) {
            GameLogger.logScheduling("✓ NO DUPLICATES FOUND - All students have unique class assignments!");
        } else {
            GameLogger.logScheduling("✗ DUPLICATES DETECTED - Investigation needed");
        }

        GameLogger.logScheduling("=== END DUPLICATE DETECTION ===");
    }
}