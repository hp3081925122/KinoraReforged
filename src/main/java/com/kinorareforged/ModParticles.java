package com.kinorareforged;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, KinoraReforged.MOD_ID);
    public static final DeferredHolder<ParticleType<?>, FxType> SLASH = PARTICLES.register("slash", FxType::new);
    public static final DeferredHolder<ParticleType<?>, FxType> ECHO_SLASH = PARTICLES.register("echo_slash", FxType::new);
    public static final DeferredHolder<ParticleType<?>, FxType> RING = PARTICLES.register("ring", FxType::new);
    public static final DeferredHolder<ParticleType<?>, FxType> ECHO_RING = PARTICLES.register("echo_ring", FxType::new);
    public static final DeferredHolder<ParticleType<?>, FxType> WARN = PARTICLES.register("warn", FxType::new);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PETAL = PARTICLES.register("petal", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARK = PARTICLES.register("spark", () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }

    public record FxOptions(FxType type, float yaw, float pitch, float tilt, float size, int life) implements ParticleOptions {
        @Override
        public ParticleType<?> getType() {
            return type;
        }
    }

    public static final class FxType extends ParticleType<FxOptions> {
        private final MapCodec<FxOptions> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, FxOptions> streamCodec;

        public FxType() {
            super(true);
            codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("yaw").forGetter(FxOptions::yaw),
                    Codec.FLOAT.optionalFieldOf("pitch", 0.0F).forGetter(FxOptions::pitch),
                    Codec.FLOAT.fieldOf("tilt").forGetter(FxOptions::tilt),
                    Codec.FLOAT.fieldOf("size").forGetter(FxOptions::size),
                    Codec.INT.fieldOf("life").forGetter(FxOptions::life)
            ).apply(instance, (yaw, pitch, tilt, size, life) -> new FxOptions(this, yaw, pitch, tilt, size, life)));
            streamCodec = StreamCodec.composite(
                    ByteBufCodecs.FLOAT, FxOptions::yaw,
                    ByteBufCodecs.FLOAT, FxOptions::pitch,
                    ByteBufCodecs.FLOAT, FxOptions::tilt,
                    ByteBufCodecs.FLOAT, FxOptions::size,
                    ByteBufCodecs.VAR_INT, FxOptions::life,
                    (yaw, pitch, tilt, size, life) -> new FxOptions(this, yaw, pitch, tilt, size, life));
        }

        @Override
        public MapCodec<FxOptions> codec() {
            return codec;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, FxOptions> streamCodec() {
            return streamCodec;
        }
    }

    private ModParticles() {
    }
}
