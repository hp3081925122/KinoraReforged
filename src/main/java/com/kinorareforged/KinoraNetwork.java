package com.kinorareforged;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class KinoraNetwork {
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(ShakePayload.TYPE, ShakePayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> com.kinorareforged.client.KinoraCameraShake.start(payload.strength(), payload.duration())));
    }

    public record ShakePayload(float strength, int duration) implements CustomPacketPayload {
        public static final Type<ShakePayload> TYPE = new Type<>(KinoraReforged.id("shake"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShakePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, ShakePayload::strength,
                ByteBufCodecs.VAR_INT, ShakePayload::duration,
                ShakePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private KinoraNetwork() {
    }
}
