package com.fender8891.targetdummy.world;

import com.fender8891.targetdummy.entity.DummyMobCatalog;
import com.fender8891.targetdummy.entity.TargetDummy;
import com.fender8891.targetdummy.registry.ModRegistries;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
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
            return;
        }
        recoverOrphanedProxy(level, player, event.getTarget());
    }

    private static void recoverOrphanedProxy(ServerLevel level, ServerPlayer player, Entity proxy) {
        ResourceLocation mobKey = BuiltInRegistries.ENTITY_TYPE.getKey(proxy.getType());
        String mobId = mobKey.toString();
        int mode = DummyMobCatalog.HOSTILE.contains(mobId) ? DummyMobCatalog.HOSTILE_MOBS
                : DummyMobCatalog.PASSIVE.contains(mobId) ? DummyMobCatalog.PASSIVE_MOBS : -1;
        if (mode < 0) {
            proxy.discard();
            return;
        }
        TargetDummy controller = ModRegistries.TARGET_DUMMY_ENTITY.get().create(level);
        if (controller == null) return;
        controller.moveTo(proxy.getX(), proxy.getY(), proxy.getZ(), proxy.getYRot(), 0.0F);
        controller.setModelMode(mode);
        controller.setSelectedMob(mobId);
        if (!level.addFreshEntity(controller)) return;
        proxy.discard();
        controller.interactThroughProxy(player);
    }

    public static void onMobDespawn(MobDespawnEvent event) {
        if (isTestMob(event.getEntity())) event.setResult(MobDespawnEvent.Result.DENY);
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (isTestMob(event.getEntity()) && event.getSource().is(DamageTypeTags.IS_FIRE)
                && event.getSource().getEntity() == null) {
            event.setCanceled(true);
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (isTestMob(event.getEntity())) event.setCanceled(true);
    }

    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        if (isTestMob(event.getEntity())) event.setDroppedExperience(0);
    }
}
