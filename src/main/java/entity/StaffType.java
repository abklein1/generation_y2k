package entity;

public enum StaffType {
    ENGLISH,
    HISTORY,
    MATH,
    SCIENCE,
    LANGUAGES,
    VISUAL_ARTS,
    VOCATIONAL,
    BUSINESS,
    COMP_SCI,
    PHYSICAL_ED,
    CONSUMER_SCI,
    PERFORMING_ARTS,
    PRINCIPAL,
    VICE_PRINCIPAL,
    GUIDANCE,
    MAINTENANCE,
    LUNCH,
    OFFICE,
    LIBRARY,
    NURSE,
    SUB;

    @Override
    public String toString() {
        return switch (this) {
            case ENGLISH -> "English";
            case HISTORY -> "History";
            case MATH -> "Math";
            case SCIENCE -> "Science";
            case LANGUAGES -> "Language";
            case VISUAL_ARTS -> "Visual Arts";
            case VOCATIONAL -> "Vocational";
            case BUSINESS -> "Business";
            case COMP_SCI -> "Computer Science";
            case PHYSICAL_ED -> "Physical Education";
            case CONSUMER_SCI -> "Consumer Science";
            case PERFORMING_ARTS -> "Performing Arts";
            case PRINCIPAL -> "Principal";
            case VICE_PRINCIPAL -> "Vice Principal";
            case GUIDANCE -> "Guidance Councilor";
            case MAINTENANCE -> "Maintenance";
            case LUNCH -> "Lunch staff";
            case OFFICE -> "Front Office staff";
            case LIBRARY -> "Librarian";
            case NURSE -> "Nurse";
            case SUB -> "Substitute";
        };
    }
}
