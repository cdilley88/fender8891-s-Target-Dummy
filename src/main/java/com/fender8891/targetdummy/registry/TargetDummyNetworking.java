package com.fender8891.targetdummy.registry;

import com.fender8891.targetdummy.entity.DummyMobCatalog;
import com.fender8891.targetdummy.entity.TargetDummy;
import com.fender8891.targetdummy.inventory.TargetDummyMenu;
import com.fender8891.targetdummy.network.TargetDummyInfoCardPayload;
import com.fender8891.targetdummy.network.TargetDummyLoadoutPayload;
import com.fender8891.targetdummy.network.TargetDummyModelPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class TargetDummyNetworking {
    private TargetDummyNetworking() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(TargetDummyInfoCardPayload.TYPE, TargetDummyInfoCardPayload.STREAM_CODEC, TargetDummyNetworking::handleInfoCardToggle)
                .playToServer(TargetDummyLoadoutPayload.TYPE, TargetDummyLoadoutPayload.STREAM_CODEC, TargetDummyNetworking::handleLoadoutChange)
                .playToServer(TargetDummyModelPayload.TYPE, TargetDummyModelPayload.STREAM_CODEC, TargetDummyNetworking::handleModelChange);
    }

    private static TargetDummy controlledDummy(int entityId, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)
                || !(serverPlayer.level().getEntity(entityId) instanceof TargetDummy dummy)
                || !(serverPlayer.containerMenu instanceof TargetDummyMenu menu)
                || !menu.controls(dummy) || serverPlayer.distanceToSqr(dummy) >= 64.0D) return null;
        return dummy;
    }

    private static void handleInfoCardToggle(TargetDummyInfoCardPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            TargetDummy dummy = controlledDummy(payload.entityId(), context);
            if (dummy == null) return;
            dummy.setInfoCardEnabled(payload.enabled());
            context.player().displayClientMessage(Component.literal("Target Dummy info card: " + (payload.enabled() ? "ON" : "OFF")), true);
        });
    }

    private static void handleLoadoutChange(TargetDummyLoadoutPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            TargetDummy dummy = controlledDummy(payload.entityId(), context);
            if (dummy == null || dummy.getModelMode() != DummyMobCatalog.IMMORTAL_STEVE
                    || payload.loadoutIndex() < TargetDummy.CUSTOM_LOADOUT || payload.loadoutIndex() > TargetDummy.LAST_PRESET) return;
            dummy.setLoadoutIndex(payload.loadoutIndex());
            context.player().displayClientMessage(Component.literal("Target Dummy loadout: " + dummy.getLoadoutName()), true);
        });
    }

    private static void handleModelChange(TargetDummyModelPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            TargetDummy dummy = controlledDummy(payload.entityId(), context);
            if (dummy == null || payload.modelMode() < DummyMobCatalog.IMMORTAL_STEVE
                    || payload.modelMode() > DummyMobCatalog.HOSTILE_MOBS) return;
            if (payload.modelMode() != dummy.getModelMode()) dummy.setModelMode(payload.modelMode());
            if (!payload.mobId().isBlank()) dummy.setSelectedMob(payload.mobId());
            context.player().displayClientMessage(Component.literal("Dummy model: " + dummy.getModelModeName()), true);
        });
    }
}