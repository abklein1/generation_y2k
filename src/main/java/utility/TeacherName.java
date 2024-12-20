package utility;

public class TeacherName implements PName {

    private String firstName;
    private String lastName;
    private String suffix;

    public TeacherName() {
        this.firstName = null;
        this.lastName = null;
        this.suffix = null;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String capitalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Convert the entire name to lowercase first
        name = name.toLowerCase();

        if (name.equals("mack")) {
            return "Mack";
        }

        if (name.equals("macias")) {
            return "Macias";
        }

        if (name.equals("oneal")) {
            return "O'Neal";
        }

        // Handle multi-part names like "DELOSREYES"
        if (name.startsWith("delosreyes")) {
            return "de los Reyes";
        } else if (name.startsWith("delacruz")) {
            return "de la Cruz";
        } else if (name.startsWith("delapaz")) {
            return "de la Paz";
        } else if (name.startsWith("delatorre")) {
            return "de la Torre";
        }

        // Handle Italian names like "DiPierto" or "DeGiacomo" but not "Disantis"
        if (name.startsWith("di") && name.length() > 2 && Character.isUpperCase(name.charAt(2))) {
            return "Di" + name.substring(2);
        } else if (name.startsWith("de") && name.length() > 2 && Character.isUpperCase(name.charAt(2))) {
            return "De" + name.substring(2);
        }

        // Handle "O'" prefix for Irish-like names
        if (shouldApplyOConnorRule(name)) {
            name = "O'" + Character.toUpperCase(name.charAt(1)) + name.substring(2);
        }
        // Handle common prefixes like "Mc" and "Mac"
        else if (name.startsWith("mc") && name.length() > 2) {
            name = "Mc" + Character.toUpperCase(name.charAt(2)) + name.substring(3);
        } else if (name.startsWith("mac") && name.length() > 3) {
            name = "Mac" + Character.toUpperCase(name.charAt(3)) + name.substring(4);
        } else {
            // Capitalize the first letter of the name
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        return name;
    }

    private boolean shouldApplyOConnorRule(String name) {
        char secondChar = name.charAt(1);
        // Apply rule if the second character is a consonant and not one of these common non-Irish name prefixes
        return Character.isUpperCase(secondChar) &&
                (secondChar != 's' && secondChar != 't' && secondChar != 'w' &&
                        secondChar != 'c' && secondChar != 'd' && secondChar != 'l');
    }
}
