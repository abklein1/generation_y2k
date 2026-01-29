package utility;

import java.io.Serializable;

public interface PName extends Serializable {
    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);
}
