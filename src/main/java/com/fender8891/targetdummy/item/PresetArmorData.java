package com.fender8891.targetdummy.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class PresetArmorData {
    private static final String PRESET_MARKER = "Fender8891TargetDummyPreset";

    private PresetArmorData() {
    }

    public static ItemStack mark(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(PRESET_MARKER, true));
        return stack;
    }

    public static boolean isPreset(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBoolean(PRESET_MARKER);
    }
}
