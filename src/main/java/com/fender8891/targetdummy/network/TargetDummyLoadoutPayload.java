package com.fender8891.targetdummy.network;

import com.fender8891.targetdummy.TargetDummyMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TargetDummyLoadoutPayload(int entityId, int loadoutIndex) implements CustomPacketPayload {
    public static final Type<TargetDummyLoadoutPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TargetDummyMod.MOD_ID, "target_dummy_loadout"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TargetDummyLoadoutPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.entityId());
                buffer.writeVarInt(payload.loadoutIndex());
            },
            buffer -> new TargetDummyLoadoutPayload(buffer.readVarInt(), buffer.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
