package com.fender8891.targetdummy.network;

import com.fender8891.targetdummy.TargetDummyMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TargetDummyModelPayload(int entityId, int modelMode, String mobId) implements CustomPacketPayload {
    public static final Type<TargetDummyModelPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TargetDummyMod.MOD_ID, "target_dummy_model"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TargetDummyModelPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.entityId());
                buffer.writeVarInt(payload.modelMode());
                buffer.writeUtf(payload.mobId(), 128);
            },
            buffer -> new TargetDummyModelPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(128))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
