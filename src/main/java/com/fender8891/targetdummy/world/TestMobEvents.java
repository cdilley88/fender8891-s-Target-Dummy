package com.fender8891.targetdummy.world;

import com.fender8891.targetdummy.entity.TargetDummy;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class TestMobEvents {
    public static final String CONTROLLER_KEY = "Fender8891TargetDummyController";
    public static final String CARD_ON_MARKER = "f8891_target_dummy_card_on";
    public static final String CARD_OFF_MARKER = "f8891_target_dummy_card_off";

    private TestMobEvents() {
    }

    public static boolean isTestMob(Entity entity) {
        return entity.getPersistentData().hasUUID(CONTROLLER_KEY);
    }

    public static boolean isClientVisibleTestMob(Entity entity) {
        if (entity.getCustomName() == null) return false;
        String marker = entity.getCustomName().getStyle().getInsertion();
        return CARD_ON_MARKER.equals(marker) || CARD_OFF_MARKER.equals(marker);
    }

    public static boolean isCardEnabled(Entity entity) {
        return entity.getCustomName() != null
                && CARD_ON_MARKER.equals(entity.getCustomName().getStyle().getInsertion());
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!isClientVisibleTestMob(event.getTarget()) && !isTestMob(event.getTarget())) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) return;
        UUID controllerId = event.getTarget().getPersistentData().getUUID(CONTROLLER_KEY);
        if (level.getEntity(controllerId) instanceof TargetDummy controller) {
            controller.interactThroughProxy(player);
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (isTestMob(event.getEntity())) event.setCanceled(true);
    }

    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        if (isTestMob(event.getEntity())) event.setDroppedExperience(0);
    }
}
