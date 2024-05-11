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
        switch (type) {
            case COMP_SCI:
                ComputerLab[] computerLabs = school.getComputerLabs();
                for (ComputerLab computerLab : computerLabs) {
                    if (computerLab.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, computerLab);
                        break;
                    }
                }
                break;
            case VOCATIONAL:
                VocationalRoom[] vocationalRooms = school.getVocationalRooms();
                for (VocationalRoom vocationalRoom : vocationalRooms) {
                    if (vocationalRoom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, vocationalRoom);
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
                        break;
                    }
                }
                for (MusicRoom musicRoom : musicRooms) {
                    if (musicRoom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, musicRoom);
                        break;
                    }
                }
                break;
            case VISUAL_ARTS:
                ArtStudio[] artStudios = school.getArtStudios();
                for (ArtStudio artStudio : artStudios) {
                    if (artStudio.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, artStudio);
                        break;
                    }
                }
                break;
            case LIBRARY:
                LibraryR[] libraryRS = school.getLibraries();
                if (libraryRS.length == 1) {
                    assignTeacherToRoom(staff, libraryRS[0]);
                } else {
                    for (LibraryR libraryR : libraryRS) {
                        if (libraryR.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, libraryR);
                            break;
                        }
                    }
                }
                break;
            case NURSE:
                for (Office office : offices) {
                    if (office.getRoomName().contains("Nurse")) {
                        assignTeacherToRoom(staff, office);
                        break;
                    }
                }
                break;
            case GUIDANCE:
                for (Office office : offices) {
                    if (office.getRoomName().contains("Guidance") && office.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, office);
                        break;
                    }
                }
                break;
            case VICE_PRINCIPAL:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Vice Principal's Office")) {
                        assignTeacherToRoom(staff, office);
                        break;
                    }
                }
                break;
            case PRINCIPAL:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Principal's Office")) {
                        assignTeacherToRoom(staff, office);
                        break;
                    }
                }
                break;
            case LUNCH:
                Lunchroom[] lunchrooms = school.getLunchrooms();
                if (lunchrooms.length == 1) {
                    assignTeacherToRoom(staff, lunchrooms[0]);
                } else {
                    for (Lunchroom lunchroom : lunchrooms) {
                        if (lunchroom.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, lunchroom);
                            break;
                        }
                    }
                }
                break;
            case OFFICE:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Front Office")) {
                        assignTeacherToRoom(staff, office);
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
                        break;
                    }
                }
                for (AthleticField athleticField : athleticFields) {
                    if (athleticField.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, athleticField);
                        break;
                    }
                }
                break;
            default:
                Classroom[] classrooms = school.getClassrooms();
                for (Classroom classroom : classrooms) {
                    if (classroom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, classroom);
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

    public void assignClassToRoom() {

    }
}
