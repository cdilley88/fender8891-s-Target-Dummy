package com.fender8891.targetdummy.world;

import com.fender8891.targetdummy.item.PresetArmorData;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class PresetArmorDropGuard {
    private PresetArmorDropGuard() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity && PresetArmorData.isPreset(itemEntity.getItem())) {
            event.setCanceled(true);
        }
    }
}
