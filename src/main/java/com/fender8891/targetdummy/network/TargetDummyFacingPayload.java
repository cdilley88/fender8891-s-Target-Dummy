package com.fender8891.targetdummy.network;

import com.fender8891.targetdummy.TargetDummyMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TargetDummyFacingPayload(int entityId, int facing) implements CustomPacketPayload {
    public static final Type<TargetDummyFacingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TargetDummyMod.MOD_ID, "target_dummy_facing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TargetDummyFacingPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.entityId());
                buffer.writeVarInt(payload.facing());
            },
            buffer -> new TargetDummyFacingPayload(buffer.readVarInt(), buffer.readVarInt())
    );

    @Override
    public Type<? extends TargetDummyFacingPayload> type() {
        return TYPE;
    }
}