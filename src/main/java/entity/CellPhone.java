package entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a cell phone owned by a student or staff member.
 * Phone numbers use a local 7-digit format (XXX-XXXX) with no area code.
 * Hardware fields (keyboard, camera, pda, etc.) are stored for future use.
 */
public class CellPhone implements Serializable {

    private static final long serialVersionUID = 2L;

    private String phoneNumber;
    private String ownerName;
    private String make;
    private String model;
    private String color;
    private int minutePlan;
    private int textLimit;

    private int price;
    private String size;
    private int battery;
    private boolean keyboard;
    private boolean camera;
    private boolean video;
    private boolean wifi;
    private boolean bluetooth;
    private boolean sms;
    private boolean im;
    private boolean pda;
    private boolean mp3;

    /**
     * Creates a cell phone with core fields specified.
     * Hardware fields default to false/zero and can be set via setters.
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
        this.size = "";
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public boolean hasKeyboard() {
        return keyboard;
    }

    public void setKeyboard(boolean keyboard) {
        this.keyboard = keyboard;
    }

    public boolean hasCamera() {
        return camera;
    }

    public void setCamera(boolean camera) {
        this.camera = camera;
    }

    public boolean hasVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public boolean hasWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean hasBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public boolean hasSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    public boolean hasIm() {
        return im;
    }

    public void setIm(boolean im) {
        this.im = im;
    }

    public boolean hasPda() {
        return pda;
    }

    public void setPda(boolean pda) {
        this.pda = pda;
    }

    public boolean hasMp3() {
        return mp3;
    }

    public void setMp3(boolean mp3) {
        this.mp3 = mp3;
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
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
