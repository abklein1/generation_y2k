package entity.Rooms;

import java.util.ArrayList;
import entity.Furniture;

public class Bedroom extends Room {
    
    private boolean playerBoolean;
    private int[][] dimensions;
    private String wallColor;
    private String floorColor;
    private String floorType;
    private ArrayList<Furniture> furniture;
    private int itemCapacity;

    public Bedroom() {
        this.numOfConnections = 0;
        this.windowCount = 0;
        this.roomName = null;
        this.numOfDoors = 0;
        this.playerBoolean = false;
        this.dimensions = new int[0][0];
        this.wallColor = null;
        this.floorColor = null;
        this.floorType = null;
        this.furniture = new ArrayList<>();
        this.itemCapacity = 0;
    }

    public void setPlayerBoolean(boolean playerBoolean) {
        this.playerBoolean = playerBoolean;
    }

    public void setDimensions(int[][] dimensions) {
        this.dimensions = dimensions;
    }

    public void setWallColor(String wallColor) {
        this.wallColor = wallColor;
    }

    public void setFloorColor(String floorColor) {
        this.floorColor = floorColor;
    }

    public void setFloorType(String floorType) {
        this.floorType = floorType;
    }

    public void setItemCapacity(int itemCapacity) {
        this.itemCapacity = itemCapacity;
    }

    public void addFurniture(Furniture furniture) {
        this.furniture.add(furniture);
    }

    public void removeFurniture(Furniture furniture) {
        this.furniture.remove(furniture);
    }

    public boolean getPlayerBoolean() {
        return this.playerBoolean;
    }

    public int[][] getDimensions() {
        return this.dimensions;
    }

    public String getWallColor() {
        return this.wallColor;
    }

    public String getFloorColor() {
        return this.floorColor;
    }
    
    public String getFloorType() {
        return this.floorType;
    }

    public ArrayList<Furniture> getFurniture() {
        return this.furniture;
    }

    public int getItemCapacity() {
        return this.itemCapacity;
    }
    
}
