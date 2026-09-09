package utility;

import entity.Rooms.*;
import entity.Staff;
import entity.StaffType;
import entity.StandardSchool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.function.Predicate;

public class RoomAssignment {
    public static void assignTeacherToRoom(Staff staff, Room room) {
        room.setAssignedStaff(staff);

        // Rename classroom based on the assigned teacher
        if (room instanceof Classroom classroom) {
            String originalName = classroom.getRoomName();
            String newName = RoomNameGenerator.generateClassroomName(classroom, staff, originalName);
            classroom.setRoomName(newName);
        }
    }

    public static boolean canUseOverflowTeachingRoom(StaffType type) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case ENGLISH, MATH, SCIENCE, HISTORY, LANGUAGES, BUSINESS, CONSUMER_SCI -> true;
            default -> false;
        };
    }

    public static List<Room> getOverflowTeachingRooms(StandardSchool school) {
        List<Room> rooms = new ArrayList<>();
        for (LibraryR library : school.getLibraries()) {
            rooms.add(library);
        }
        for (ConferenceRoom conferenceRoom : school.getConferenceRooms()) {
            rooms.add(conferenceRoom);
        }
        for (Auditorium auditorium : school.getAuditoriums()) {
            rooms.add(auditorium);
        }
        for (Lunchroom lunchroom : school.getLunchrooms()) {
            rooms.add(lunchroom);
        }
        return rooms;
    }

    public static boolean tryAssignOverflowTeachingRoom(Staff staff, StandardSchool school) {
        StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
        if (!canUseOverflowTeachingRoom(type)) {
            return false;
        }

        for (Room room : getOverflowTeachingRooms(school)) {
            if (room.getAssignedStaff().isEmpty()) {
                assignTeacherToRoom(staff, room);
                GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                        + staff.teacherName.getLastName() + " to overflow space " + room.getRoomName());
                return true;
            }
        }
        return false;
    }

    /**
     * Support staff types that need a workspace rather than a teaching classroom.
     */
    private static boolean isSupportStaffType(StaffType type) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case PRINCIPAL, VICE_PRINCIPAL, GUIDANCE, NURSE, OFFICE, LIBRARY, LUNCH, MAINTENANCE -> true;
            default -> false;
        };
    }

    /**
     * Assigns every support staff member (office, admin, lunch, maintenance, etc.)
     * to an appropriate shared workspace. Substitutes are skipped. Safe to call
     * more than once: staff who already have a room are left in place.
     */
    public static void ensureSupportStaffHaveRooms(StandardSchool school, HashMap<Integer, Staff> staffHashMap) {
        if (school == null || staffHashMap == null) {
            return;
        }

        GameLogger.logScheduling("=== ENSURING SUPPORT STAFF HAVE ROOM ASSIGNMENTS ===");
        int alreadyHad = 0;
        int newlyAssigned = 0;
        int failed = 0;

        for (Staff staff : staffHashMap.values()) {
            if (staff == null || staff.teacherStatistics == null) {
                continue;
            }
            StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
            if (type == null || type == StaffType.SUB || !isSupportStaffType(type)) {
                continue;
            }

            Room existing = staff.getAssignedClassroom();
            if (existing == null) {
                existing = school.locateStaffRoom(staff);
            }
            if (existing != null) {
                staff.setAssignedClassroom(existing);
                alreadyHad++;
                continue;
            }

            if (assignSupportStaffWorkspace(staff, type, school)) {
                newlyAssigned++;
            } else {
                failed++;
                GameLogger.logScheduling("  WARNING: No workspace for " + staff.teacherName.getFirstName() + " "
                        + staff.teacherName.getLastName() + " (" + type + ")");
            }
        }

        GameLogger.logScheduling("  Support staff already housed: " + alreadyHad);
        GameLogger.logScheduling("  Support staff newly assigned: " + newlyAssigned);
        if (failed > 0) {
            GameLogger.logScheduling("  Support staff still without a room: " + failed);
        }
    }

    private static boolean assignSupportStaffWorkspace(Staff staff, StaffType type, StandardSchool school) {
        boolean assigned = switch (type) {
            case PRINCIPAL -> assignToNamedOffices(staff, school, "Principal's Office", true);
            case VICE_PRINCIPAL -> assignToNamedOffices(staff, school, "Vice Principal's Office", true);
            case GUIDANCE -> assignToNamedOffices(staff, school, "Guidance", false);
            case NURSE -> assignToNamedOffices(staff, school, "Nurse", false);
            case OFFICE -> assignToNamedOffices(staff, school, "Front Office", true);
            case LIBRARY -> assignToSharedRooms(staff, school.getLibraries(), "library");
            case LUNCH -> assignToSharedRooms(staff, school.getLunchrooms(), "lunchroom");
            case MAINTENANCE -> assignToSharedRooms(staff, school.getUtilityrooms(), "utility room")
                    || assignToSharedRooms(staff, school.getBreakrooms(), "breakroom");
            default -> false;
        };
        if (!assigned) {
            assigned = tryAssignSupportFallbackRoom(staff, type, school);
        }
        return assigned;
    }

    private static boolean assignToNamedOffices(Staff staff, StandardSchool school, String nameToken, boolean exact) {
        Room room = pickBestSharedRoom(school.getOffices(), office -> matchesOfficeName(office, nameToken, exact));
        if (room == null) {
            return false;
        }
        assignSupportStaffToRoom(staff, room, room.getRoomName());
        return true;
    }

    private static boolean matchesOfficeName(Room office, String nameToken, boolean exact) {
        String name = office.getRoomName();
        if (name == null) {
            return false;
        }
        return exact ? name.equals(nameToken) : name.contains(nameToken);
    }

    private static boolean assignToSharedRooms(Staff staff, Room[] rooms, String reason) {
        Room room = pickBestSharedRoom(rooms, ignored -> true);
        if (room == null) {
            return false;
        }
        assignSupportStaffToRoom(staff, room, reason);
        return true;
    }

    /**
     * Prefers a matching room that is still under staff capacity, otherwise the
     * least-occupied match so extra clerks/counselors still share their office.
     */
    private static Room pickBestSharedRoom(Room[] rooms, Predicate<Room> filter) {
        if (rooms == null) {
            return null;
        }
        Room bestUnderCapacity = null;
        Room leastOccupied = null;
        for (Room room : rooms) {
            if (room == null || !filter.test(room)) {
                continue;
            }
            int assigned = room.getAssignedStaff().size();
            int capacity = Math.max(1, room.getStaffCapacity());
            if (assigned < capacity) {
                if (bestUnderCapacity == null
                        || assigned < bestUnderCapacity.getAssignedStaff().size()) {
                    bestUnderCapacity = room;
                }
            }
            if (leastOccupied == null
                    || assigned < leastOccupied.getAssignedStaff().size()) {
                leastOccupied = room;
            }
        }
        return bestUnderCapacity != null ? bestUnderCapacity : leastOccupied;
    }

    /**
     * Fallback room assignment for support staff whose dedicated room is
     * unavailable. Tries remaining primary rooms, then any office.
     */
    private static boolean tryAssignSupportFallbackRoom(Staff staff, StaffType type, StandardSchool school) {
        if (type == StaffType.LIBRARY && assignToSharedRooms(staff, school.getLibraries(), "shared library")) {
            return true;
        }
        if (type == StaffType.LUNCH && assignToSharedRooms(staff, school.getLunchrooms(), "shared lunchroom")) {
            return true;
        }
        if (type == StaffType.MAINTENANCE
                && (assignToSharedRooms(staff, school.getUtilityrooms(), "utility room")
                        || assignToSharedRooms(staff, school.getBreakrooms(), "breakroom"))) {
            return true;
        }

        Room office = pickBestSharedRoom(school.getOffices(), ignored -> true);
        if (office != null) {
            assignSupportStaffToRoom(staff, office, "office fallback");
            return true;
        }
        return false;
    }

    private static void assignSupportStaffToRoom(Staff staff, Room room, String reason) {
        assignTeacherToRoom(staff, room);
        GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                + staff.teacherName.getLastName() + " to " + room.getRoomName() + " (" + reason + ")");
    }

    private static void initialRoomAssignmentHelper(Staff staff, StandardSchool school) {
        StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
        boolean teacherAssigned = false;

        if (type == null) {
            GameLogger.logScheduling("WARNING: Staff " + staff.teacherName.getFirstName() + " " +
                    staff.teacherName.getLastName() + " has no assigned type, skipping room assignment");
            return;
        }

        if (type == StaffType.SUB) {
            return;
        }

        if (isSupportStaffType(type)) {
            if (!assignSupportStaffWorkspace(staff, type, school)) {
                GameLogger.logScheduling("No available room found for " + staff.teacherName.getFirstName() + " "
                        + staff.teacherName.getLastName());
            }
            return;
        }

        switch (type) {
            case COMP_SCI:
                ComputerLab[] computerLabs = school.getComputerLabs();
                for (ComputerLab computerLab : computerLabs) {
                    if (computerLab.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, computerLab);
                        GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                + staff.teacherName.getLastName() + " to " + computerLab.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case VOCATIONAL:
                VocationalRoom[] vocationalRooms = school.getVocationalRooms();
                for (VocationalRoom vocationalRoom : vocationalRooms) {
                    if (vocationalRoom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, vocationalRoom);
                        GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                + staff.teacherName.getLastName() + " to " + vocationalRoom.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case PERFORMING_ARTS:
                DramaRoom[] dramaRooms = school.getDramaRooms();
                MusicRoom[] musicRooms = school.getMusicRooms();
                Auditorium[] auditoriums = school.getAuditoriums();
                for (DramaRoom dramaRoom : dramaRooms) {
                    if (dramaRoom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, dramaRoom);
                        teacherAssigned = true;
                        GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                + staff.teacherName.getLastName() + " to " + dramaRoom.getRoomName());
                        break;
                    }
                }
                if (!teacherAssigned) {
                    for (MusicRoom musicRoom : musicRooms) {
                        if (musicRoom.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, musicRoom);
                            teacherAssigned = true;
                            GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                    + staff.teacherName.getLastName() + " to " + musicRoom.getRoomName());
                            break;
                        }
                    }
                }
                if (!teacherAssigned) {
                    for (Auditorium auditorium : auditoriums) {
                        if (auditorium.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, auditorium);
                            teacherAssigned = true;
                            GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                    + staff.teacherName.getLastName() + " to " + auditorium.getRoomName());
                            break;
                        }
                    }
                }
                break;
            case VISUAL_ARTS:
                ArtStudio[] artStudios = school.getArtStudios();
                for (ArtStudio artStudio : artStudios) {
                    if (artStudio.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, artStudio);
                        GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                + staff.teacherName.getLastName() + " to " + artStudio.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case PHYSICAL_ED:
                Gym[] gyms = school.getGyms();
                AthleticField[] athleticFields = school.getAthleticFields();
                for (Gym gym : gyms) {
                    if (gym.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, gym);
                        GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                + staff.teacherName.getLastName() + " to " + gym.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                if (!teacherAssigned) {
                    for (AthleticField athleticField : athleticFields) {
                        if (athleticField.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, athleticField);
                            GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                    + staff.teacherName.getLastName() + " to " + athleticField.getRoomName());
                            teacherAssigned = true;
                            break;
                        }
                    }
                }
                break;
            case SCIENCE:
                Classroom[] classrooms = school.getClassrooms();
                for (Classroom classroom : classrooms) {
                    if (classroom.getClassRoomType().equals("Science") && classroom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, classroom);
                        GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                + staff.teacherName.getLastName() + " to " + classroom.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                if (!teacherAssigned) {
                    for (Classroom classroom : classrooms) {
                        if (classroom.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, classroom);
                            GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                    + staff.teacherName.getLastName() + " to " + classroom.getRoomName()
                                    + " of other type!");
                            teacherAssigned = true;
                            break;
                        }
                    }
                }
                break;
            default:
                classrooms = school.getClassrooms();
                for (Classroom classroom : classrooms) {
                    if (classroom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, classroom);
                        GameLogger.logScheduling("Assigned " + staff.teacherName.getFirstName() + " "
                                + staff.teacherName.getLastName() + " to " + classroom.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
        }

        if (!teacherAssigned && canUseOverflowTeachingRoom(type)) {
            teacherAssigned = tryAssignOverflowTeachingRoom(staff, school);
        }

        if (!teacherAssigned) {
            GameLogger.logScheduling("No available room found for " + staff.teacherName.getFirstName() + " "
                    + staff.teacherName.getLastName());
        }
    }

    public static void initialClassroomAssignments(StandardSchool school, HashMap<Integer, Staff> staffHashMap) {
        // Assign teacher to each classroom
        for (Map.Entry<Integer, Staff> entry : staffHashMap.entrySet()) {
            initialRoomAssignmentHelper(entry.getValue(), school);
        }
    }
}
