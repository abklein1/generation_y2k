package entity.Body;

import entity.Items.EquipmentSlot;
import entity.Items.WearableItem;

import java.util.List;

/**
 * Represents a head body part that can hold equipped items
 * (piercings, glasses, hats, etc.) in defined slots.
 */
public interface Head {

    void setAttire();

    void setAttireName();

    void getAttire();

    void getAttireName();

    /**
     * Equips an item in the slot declared by the item itself.
     *
     * @param item the item to equip
     * @return true if equipped successfully, false if the slot is occupied
     */
    boolean equip(WearableItem item);

    /**
     * Removes and returns the item in the given slot.
     *
     * @param slot the slot to clear
     * @return the removed item, or null if the slot was empty
     */
    WearableItem unequip(EquipmentSlot slot);

    /**
     * Returns the item currently equipped in the given slot.
     *
     * @param slot the slot to inspect
     * @return the equipped item, or null if empty
     */
    WearableItem getEquipped(EquipmentSlot slot);

    /**
     * Returns all items currently equipped on the head,
     * across all slots, in a stable order.
     */
    List<WearableItem> getAllEquipped();

    /**
     * Returns only the items in visible slots
     * (those that contribute to appearance descriptions).
     */
    List<WearableItem> getVisibleEquipped();
}
