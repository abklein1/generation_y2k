package entity.Body;

import entity.Items.EquipmentSlot;
import entity.Items.WearableItem;

import java.io.Serializable;
import java.util.List;

public class TeacherHead implements Head, Serializable {
    @Override
    public void setAttire() { }

    @Override
    public void setAttireName() { }

    @Override
    public void getAttire() { }

    @Override
    public void getAttireName() { }

    @Override
    public boolean equip(WearableItem item) {
        return false;
    }

    @Override
    public WearableItem unequip(EquipmentSlot slot) {
        return null;
    }

    @Override
    public WearableItem getEquipped(EquipmentSlot slot) {
        return null;
    }

    @Override
    public List<WearableItem> getAllEquipped() {
        return List.of();
    }

    @Override
    public List<WearableItem> getVisibleEquipped() {
        return List.of();
    }
}
