package entity.Body;

import entity.Items.EquipmentSlot;
import entity.Items.WearableItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Student head implementation with slot-based equipment storage.
 * Supports ear, nose, lip, eyebrow, and tongue piercing slots,
 * with the ability to hold multiple piercings per ear.
 */
public class StudentHead implements Head, Serializable {

    private static final long serialVersionUID = 1L;

    private static final EquipmentSlot[] HEAD_SLOTS = {
            EquipmentSlot.LEFT_EAR, EquipmentSlot.RIGHT_EAR,
            EquipmentSlot.NOSE, EquipmentSlot.LIPS,
            EquipmentSlot.EYEBROW, EquipmentSlot.TONGUE
    };

    private static final int MAX_PER_EAR = 3;

    private final Map<EquipmentSlot, List<WearableItem>> equipment;

    public StudentHead() {
        equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : HEAD_SLOTS) {
            equipment.put(slot, new ArrayList<>());
        }
    }

    @Override
    public boolean equip(WearableItem item) {
        EquipmentSlot slot = item.getSlot();
        List<WearableItem> items = equipment.get(slot);
        if (items == null) {
            return false;
        }

        int max = isEarSlot(slot) ? MAX_PER_EAR : 1;
        if (items.size() >= max) {
            return false;
        }
        items.add(item);
        return true;
    }

    @Override
    public WearableItem unequip(EquipmentSlot slot) {
        List<WearableItem> items = equipment.get(slot);
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.remove(items.size() - 1);
    }

    /**
     * Removes all items from the given slot.
     *
     * @param slot the slot to clear
     * @return the list of removed items (may be empty)
     */
    public List<WearableItem> unequipAll(EquipmentSlot slot) {
        List<WearableItem> items = equipment.get(slot);
        if (items == null) {
            return List.of();
        }
        List<WearableItem> removed = new ArrayList<>(items);
        items.clear();
        return removed;
    }

    @Override
    public WearableItem getEquipped(EquipmentSlot slot) {
        List<WearableItem> items = equipment.get(slot);
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.get(0);
    }

    /**
     * Returns all items equipped in the given slot.
     * Ear slots may contain multiple piercings.
     *
     * @param slot the slot to query
     * @return unmodifiable list of items in the slot
     */
    public List<WearableItem> getEquippedList(EquipmentSlot slot) {
        List<WearableItem> items = equipment.get(slot);
        if (items == null) {
            return List.of();
        }
        return List.copyOf(items);
    }

    public int getEquippedCount(EquipmentSlot slot) {
        List<WearableItem> items = equipment.get(slot);
        return items == null ? 0 : items.size();
    }

    @Override
    public List<WearableItem> getAllEquipped() {
        List<WearableItem> all = new ArrayList<>();
        for (EquipmentSlot slot : HEAD_SLOTS) {
            all.addAll(equipment.get(slot));
        }
        return all;
    }

    @Override
    public List<WearableItem> getVisibleEquipped() {
        List<WearableItem> visible = new ArrayList<>();
        for (EquipmentSlot slot : HEAD_SLOTS) {
            if (slot.isVisible()) {
                visible.addAll(equipment.get(slot));
            }
        }
        return visible;
    }

    public boolean hasAnyEquipped() {
        for (EquipmentSlot slot : HEAD_SLOTS) {
            if (!equipment.get(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEarSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEFT_EAR || slot == EquipmentSlot.RIGHT_EAR;
    }

    // Attire interface stubs (retained for interface convention)

    @Override
    public void setAttire() { }

    @Override
    public void setAttireName() { }

    @Override
    public void getAttire() { }

    @Override
    public void getAttireName() { }
}
