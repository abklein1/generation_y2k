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
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + vocationalRoom.getRoomName());
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
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + dramaRoom.getRoomName());
                        break;
                    }
                }
                if (!teacherAssigned) {
                    for (MusicRoom musicRoom : musicRooms) {
                        if (musicRoom.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, musicRoom);
                            teacherAssigned = true;
                            System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + musicRoom.getRoomName());
                            break;
                        }
                    }
                }
                if (!teacherAssigned) {
                    for (Auditorium auditorium : auditoriums) {
                        if (auditorium.getAssignedStaff().isEmpty()) {
                            assignTeacherToRoom(staff, auditorium);
                            teacherAssigned = true;
                            System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + auditorium.getRoomName());
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
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case LIBRARY:
                LibraryR[] libraryRS = school.getLibraries();
                for (LibraryR libraryR : libraryRS) {
                    if (libraryR.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, libraryR);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + libraryR.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case NURSE:
                for (Office office : offices) {
                    if (office.getRoomName().contains("Nurse") && office.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case GUIDANCE:
                for (Office office : offices) {
                    if (office.getRoomName().contains("Guidance") && office.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case VICE_PRINCIPAL:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Vice Principal's Office") && office.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case PRINCIPAL:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Principal's Office") && office.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case LUNCH:
                Lunchroom[] lunchrooms = school.getLunchrooms();
                for (Lunchroom lunchroom : lunchrooms) {
                    if (lunchroom.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, lunchroom);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + lunchroom.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
            case OFFICE:
                for (Office office : offices) {
                    if (office.getRoomName().equals("Front Office") && office.getAssignedStaff().isEmpty()) {
                        assignTeacherToRoom(staff, office);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + office.getRoomName());
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
                            teacherAssigned = true;
                            break;
                        }
                    }
                }
                break;
            case SUB:
                // Subs might not need room assignments
                break;
            case MAINTENANCE:
                UtilityRoom[] utilityRooms = school.getUtilityrooms();
                for (UtilityRoom utilityRoom : utilityRooms) {
                    if (utilityRoom.getAssignedStaff().size() < utilityRoom.getStaffCapacity()) {
                        assignTeacherToRoom(staff, utilityRoom);
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + utilityRoom.getRoomName());
                        teacherAssigned = true;
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
                        System.out.println("Assigned " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName() + " to " + classroom.getRoomName());
                        teacherAssigned = true;
                        break;
                    }
                }
                break;
        }

        if (!teacherAssigned) {
            System.out.println("No available room found for " + staff.teacherName.getFirstName() + " " + staff.teacherName.getLastName());
        }
    }

    public static void initialClassroomAssignments(StandardSchool school, HashMap<Integer, Staff> staffHashMap) {
        //Assign teacher to each classroom
        for (Map.Entry<Integer, Staff> entry : staffHashMap.entrySet()) {
            initialRoomAssignmentHelper(entry.getValue(), school);
        }
    }
}
