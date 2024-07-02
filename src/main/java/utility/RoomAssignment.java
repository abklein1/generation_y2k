package utility;

import entity.Rooms.*;
import entity.Staff;
import entity.StaffType;
import entity.StandardSchool;

import java.util.HashMap;
import java.util.Map;

public class RoomAssignment {
    public static void assignTeacherToRoom(Staff staff, Room room) {
        room.setAssignedStaff(staff);
    }

    private static void initialRoomAssignmentHelper(Staff staff, StandardSchool school) {
        StaffType type = (StaffType) staff.teacherStatistics.getStaffType();
        Office[] offices = school.getOffices();
        boolean teacherAssigned = false;
        switch (type) {
            case COMP_SCI:
                ComputerLab[] computerLabs = school.getComputerLabs();
                for (ComputerLab computerLab : computerLabs) {
                    if (computerLab.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, computerLab);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + computerLab.getRoomName());
                        break;
                    }
                }
                break;
            case VOCATIONAL:
                VocationalRoom[] vocationalRooms = school.getVocationalRooms();
                for (VocationalRoom vocationalRoom : vocationalRooms) {
                    if (vocationalRoom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, vocationalRoom);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + vocationalRoom.getRoomName());
                        break;
                    }
                }
                break;
            case PERFORMING_ARTS:
                DramaRoom[] dramaRooms = school.getDramaRooms();
                MusicRoom[] musicRooms = school.getMusicRooms();
                for (DramaRoom dramaRoom : dramaRooms) {
                    if (dramaRoom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, dramaRoom);
                        teacherAssigned = true;
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + dramaRoom.getRoomName());
                        break;
                    }
                }
                if (!teacherAssigned) {
                    for (MusicRoom musicRoom : musicRooms) {
                        if (musicRoom.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, musicRoom);
                            System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + musicRoom.getRoomName());
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
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + artStudio.getRoomName());
                        break;
                    }
                }
                break;
            case LIBRARY:
                LibraryR[] libraryRS = school.getLibraries();
                if (libraryRS.length == 1) {
                    assignTeacherToRoom(staff, libraryRS[0]);
                    System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + libraryRS[0].getRoomName());
                    break;
                } else {
                    for (LibraryR libraryR : libraryRS) {
                        if (libraryR.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, libraryR);
                            System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + libraryR.getRoomName());
                            break;
                        }
                    }
                }
                break;
            case NURSE:
                for (Office office : offices) {
                    if (office.getRoomName().contains("Nurse")) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
                        break;
                    }
                }
                break;
            case GUIDANCE:
                for (Office office : offices) {
                    if (office.getRoomName().contains("Guidance") && office.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
                        break;
                    }
                }
                break;
            case VICE_PRINCIPAL:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Vice Principal's Office")) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
                        break;
                    }
                }
                break;
            case PRINCIPAL:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Principal's Office")) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
                        break;
                    }
                }
                break;
            case LUNCH:
                Lunchroom[] lunchrooms = school.getLunchrooms();
                if (lunchrooms.length == 1) {
                    assignTeacherToRoom(staff, lunchrooms[0]);
                    System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + lunchrooms[0].getRoomName());
                    break;
                } else {
                    for (Lunchroom lunchroom : lunchrooms) {
                        if (lunchroom.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, lunchroom);
                            System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + lunchroom.getRoomName());
                            break;
                        }
                    }
                }
                break;
            case OFFICE:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Front Office")) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
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
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + gym.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                if (!teacherAssigned) {
                    for (AthleticField athleticField : athleticFields) {
                        if (athleticField.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, athleticField);
                            System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + athleticField.getRoomName());
                            break;
                        }
                    }
                }
                break;
            case SUB:
                break;
            case MAINTENANCE:
                UtilityRoom[] utilityRooms = school.getUtilityrooms();
                for (UtilityRoom utilityRoom : utilityRooms) {
                    if (utilityRoom.getAssignedStaff().size() < utilityRoom.getStaffCapacity()) {
                        assignTeacherToRoom(staff, utilityRoom);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + utilityRoom.getRoomName());
                        break;
                    }
                }
                break;
            case SCIENCE:
                Classroom[] classrooms = school.getClassrooms();
                for (Classroom classroom : classrooms) {
                    if (classroom.getClassRoomType().equals("Science") && classroom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, classroom);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + classroom.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                if (!teacherAssigned) {
                    for (Classroom classroom : classrooms) {
                        if (classroom.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, classroom);
                            System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + classroom.getRoomName() + " of other type!");
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
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + classroom.getRoomName());
                        break;
                    }
                }
                break;
        }
    }

    public static void initialClassroomAssignments(StandardSchool school, HashMap<Integer, Staff> staffHashMap) {
        //Assign teacher to each classroom
        for (Map.Entry<Integer, Staff> entry : staffHashMap.entrySet()) {
            Enum<StaffType> type = entry.getValue().teacherStatistics.getStaffType();
            initialRoomAssignmentHelper(entry.getValue(), school);
        }
    }
}
