package entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a cell phone owned by a student or staff member.
 * Phone numbers use a local 7-digit format (XXX-XXXX) with no area code.
 */
public class CellPhone implements Serializable {

    private static final long serialVersionUID = 1L;

    private String phoneNumber;
    private String ownerName;
    private String make;
    private String model;
    private String color;
    private int minutePlan;
    private int textLimit;

    /**
     * Creates a cell phone with all fields specified.
     *
     * @param phoneNumber the 7-digit phone number in XXX-XXXX format
     * @param ownerName   the display name of the phone's owner
     * @param make        the manufacturer (may be empty)
     * @param model       the model name (may be empty)
     * @param color       the phone's color
     * @param minutePlan  monthly minute allowance
     * @param textLimit   monthly text message limit
     */
    public CellPhone(String phoneNumber, String ownerName, String make,
                     String model, String color, int minutePlan, int textLimit) {
        this.phoneNumber = phoneNumber;
        this.ownerName = ownerName;
        this.make = make;
        this.model = model;
        this.color = color;
        this.minutePlan = minutePlan;
        this.textLimit = textLimit;
    }

    /**
     * Creates a cell phone with make and model left blank.
     *
     * @param phoneNumber the 7-digit phone number in XXX-XXXX format
     * @param ownerName   the display name of the phone's owner
     * @param color       the phone's color
     * @param minutePlan  monthly minute allowance
     * @param textLimit   monthly text message limit
     */
    public CellPhone(String phoneNumber, String ownerName, String color,
                     int minutePlan, int textLimit) {
        this(phoneNumber, ownerName, "", "", color, minutePlan, textLimit);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getMinutePlan() {
        return minutePlan;
    }

    public void setMinutePlan(int minutePlan) {
        this.minutePlan = minutePlan;
    }

    public int getTextLimit() {
        return textLimit;
    }

    public void setTextLimit(int textLimit) {
        this.textLimit = textLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CellPhone cellPhone = (CellPhone) o;
        return Objects.equals(phoneNumber, cellPhone.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }

    @Override
    public String toString() {
        return "CellPhone{" +
                "number='" + phoneNumber + '\'' +
                ", owner='" + ownerName + '\'' +
                ", color='" + color + '\'' +
                ", minutes=" + minutePlan +
                ", texts=" + textLimit +
                '}';
    }
}
