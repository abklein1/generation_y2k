package utility;

public class TeacherName implements PName {

    private String firstName;
    private String lastName;

    public TeacherName(){
        this.firstName = null;
        this.lastName = null;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = capitalizeName(lastName);
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getLastName() {
        return this.lastName;
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
