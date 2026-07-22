package com.fender8891.targetdummy.inventory;

import com.fender8891.targetdummy.entity.DummyMobCatalog;
import com.fender8891.targetdummy.entity.TargetDummy;
import com.fender8891.targetdummy.registry.ModRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class TargetDummyMenu extends AbstractContainerMenu {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };
    private final TargetDummy dummy;

    public static TargetDummyMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        var entity = inventory.player.level().getEntity(buffer.readVarInt());
        return new TargetDummyMenu(id, inventory, entity instanceof TargetDummy target ? target : null);
    }

    public TargetDummyMenu(int id, Inventory inventory, TargetDummy dummy) {
        super(ModRegistries.TARGET_DUMMY_MENU.get(), id);
        this.dummy = dummy;
        for (int i = 0; i < SLOTS.length; i++) {
            final EquipmentSlot equipmentSlot = SLOTS[i];
            int x = i < 4 ? 28 : 112;
            int y = i < 4 ? 48 + i * 24 : 59 + (i - 4) * 42;
            addSlot(new Slot(new net.minecraft.world.SimpleContainer(1), 0, x, y) {
                private boolean presetArmorLocked() {
                    return dummy != null && dummy.getModelMode() == DummyMobCatalog.IMMORTAL_STEVE
                            && dummy.hasPresetLoadout() && equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
                }
                @Override public ItemStack getItem() { return dummy == null ? ItemStack.EMPTY : dummy.getItemBySlot(equipmentSlot); }
                @Override public void set(ItemStack stack) {
                    if (dummy != null && !presetArmorLocked()) dummy.setItemSlot(equipmentSlot, stack);
                    setChanged();
                }
                @Override public ItemStack remove(int amount) {
                    if (presetArmorLocked()) return ItemStack.EMPTY;
                    ItemStack current = getItem();
                    if (current.isEmpty()) return ItemStack.EMPTY;
                    set(ItemStack.EMPTY);
                    return current;
                }
                @Override public boolean mayPickup(Player player) { return !presetArmorLocked(); }
                @Override public boolean mayPlace(ItemStack stack) {
                    if (presetArmorLocked()) return false;
                    if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                        return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == equipmentSlot;
                    }
                    return true;
                }
                @Override public boolean isActive() {
                    return dummy != null && dummy.getModelMode() == DummyMobCatalog.IMMORTAL_STEVE;
                }
                @Override public int getMaxStackSize() { return 1; }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col + row * 9 + 9, 69 + col * 18, 194 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 69 + col * 18, 252));
    }

    @Override public boolean stillValid(Player player) {
        return dummy != null && dummy.isAlive() && player.distanceToSqr(dummy) < 64.0D;
    }
    public boolean isInfoCardEnabled() { return dummy != null && dummy.isInfoCardEnabled(); }
    public int getModelMode() { return dummy == null ? DummyMobCatalog.IMMORTAL_STEVE : dummy.getModelMode(); }
    public String getModelModeName() { return dummy == null ? "IMMORTAL STEVE" : dummy.getModelModeName(); }
    public String getSelectedMobId() { return dummy == null ? "" : dummy.getSelectedMobId(); }
    public int getLoadoutIndex() { return dummy == null ? TargetDummy.CUSTOM_LOADOUT : dummy.getLoadoutIndex(); }
    public String getLoadoutName() { return dummy == null ? "CUSTOM" : dummy.getLoadoutName(); }
    public int getDummyEntityId() { return dummy == null ? -1 : dummy.getId(); }
    public void setInfoCardEnabled(boolean enabled) { if (dummy != null) dummy.setInfoCardEnabled(enabled); }
    public boolean controls(TargetDummy target) { return dummy == target; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}