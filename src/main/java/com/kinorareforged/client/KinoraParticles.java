package com.kinorareforged.client;

import com.kinorareforged.ModParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.joml.Quaternionf;

public final class KinoraParticles {
    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SLASH.get(), sprites -> (options, level, x, y, z, vx, vy, vz, random) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.SLASH));
        event.registerSpriteSet(ModParticles.ECHO_SLASH.get(), sprites -> (options, level, x, y, z, vx, vy, vz, random) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.SLASH));
        event.registerSpriteSet(ModParticles.RING.get(), sprites -> (options, level, x, y, z, vx, vy, vz, random) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.RING));
        event.registerSpriteSet(ModParticles.ECHO_RING.get(), sprites -> (options, level, x, y, z, vx, vy, vz, random) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.RING));
        event.registerSpriteSet(ModParticles.WARN.get(), sprites -> (options, level, x, y, z, vx, vy, vz, random) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.WARN));
        event.registerSpriteSet(ModParticles.PETAL.get(), sprites -> (SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, net.minecraft.util.RandomSource random) -> new PetalParticle(level, x, y, z, vx, vy, vz, sprites));
        event.registerSpriteSet(ModParticles.SPARK.get(), sprites -> (SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, net.minecraft.util.RandomSource random) -> new SparkParticle(level, x, y, z, sprites));
    }

    private enum Mode { SLASH, RING, WARN }

    private static final class FlatFxParticle extends SingleQuadParticle {
        private final SpriteSet sprites;
        private final Mode mode;
        private final float yaw;
        private final float pitch;
        private final float tilt;
        private final float size;

        private FlatFxParticle(ClientLevel level, double x, double y, double z, ModParticles.FxOptions options, SpriteSet sprites, Mode mode) {
            super(level, x, y, z, sprites.get(0, 1));
            this.sprites = sprites;
            this.mode = mode;
            this.yaw = options.yaw() * Mth.DEG_TO_RAD;
            this.pitch = options.pitch() * Mth.DEG_TO_RAD;
            this.tilt = options.tilt() * Mth.DEG_TO_RAD;
            this.size = Math.max(0.1F, options.size());
            this.lifetime = Math.max(2, options.life());
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.xd = this.yd = this.zd = 0.0D;
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed && mode != Mode.WARN) setSpriteFromAge(sprites);
        }

        @Override
        public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
            float t = Mth.clamp((age + partialTick) / lifetime, 0.0F, 1.0F);
            switch (mode) {
                case SLASH -> {
                    Quaternionf rotation = new Quaternionf().rotateY(Mth.PI - yaw).rotateX(-pitch).rotateZ(tilt).rotateX(-Mth.HALF_PI);
                    float alpha = t < 0.7F ? 1.0F : 1.0F - (t - 0.7F) / 0.3F;
                    quad(state, camera, rotation, partialTick, 0.0F, size * (0.88F + 0.12F * t), alpha);
                }
                case RING -> {
                    Quaternionf rotation = new Quaternionf().rotateY(yaw + (age + partialTick) * 0.04F).rotateX(-Mth.HALF_PI);
                    float grow = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
                    quad(state, camera, rotation, partialTick, 0.0F, size * (0.2F + 0.8F * grow), 1.0F - t * t * 0.6F);
                }
                case WARN -> {
                    Quaternionf rotation = new Quaternionf().rotateY((age + partialTick) * 0.02F).rotateX(-Mth.HALF_PI);
                    float pulse = 0.7F + 0.3F * Mth.sin((age + partialTick) * 0.6F);
                    setSprite(sprites.get(0, 1));
                    quad(state, camera, rotation, partialTick, 0.0F, size, 0.9F * pulse);
                    setSprite(sprites.get(1, 1));
                    quad(state, camera, rotation, partialTick, 0.01F, size * t * t, 0.45F + 0.4F * t);
                    setSprite(sprites.get(0, 1));
                }
            }
        }

        private void quad(QuadParticleRenderState state, Camera camera, Quaternionf rotation, float partialTick, float yOffset, float half, float alpha) {
            Vec3 cam = camera.position();
            float px = (float) (Mth.lerp(partialTick, xo, x) - cam.x());
            float py = (float) (Mth.lerp(partialTick, yo, y) - cam.y()) + yOffset;
            float pz = (float) (Mth.lerp(partialTick, zo, z) - cam.z());
            int color = ARGB.colorFromFloat(Mth.clamp(alpha, 0.0F, 1.0F), rCol, gCol, bCol);
            int light = getLightCoords(partialTick);
            Quaternionf back = new Quaternionf(rotation).rotateY(Mth.PI);
            state.add(getLayer(), px, py, pz, rotation.x, rotation.y, rotation.z, rotation.w, half, getU0(), getU1(), getV0(), getV1(), color, light);
            state.add(getLayer(), px, py, pz, back.x, back.y, back.z, back.w, half, getU1(), getU0(), getV0(), getV1(), color, light);
        }

        @Override
        protected SingleQuadParticle.Layer getLayer() {
            return SingleQuadParticle.Layer.TRANSLUCENT;
        }

        @Override
        protected int getLightCoords(float partialTick) {
            return 15728880;
        }
    }

    private static final class PetalParticle extends SingleQuadParticle {
        private final float spin;
        private PetalParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z, vx, vy, vz, sprites.get(0, 1));
            this.xd = vx;
            this.yd = vy + 0.05D;
            this.zd = vz;
            this.lifetime = 30 + random.nextInt(25);
            this.gravity = 0.035F;
            this.friction = 0.93F;
            this.quadSize = 0.09F + random.nextFloat() * 0.07F;
            this.spin = (random.nextFloat() - 0.5F) * 0.5F;
            this.roll = random.nextFloat() * Mth.TWO_PI;
            this.oRoll = roll;
        }
        @Override public void tick() {
            oRoll = roll;
            super.tick();
            if (onGround) return;
            roll += spin;
            xd += Mth.sin(age * 0.25F + spin * 10.0F) * 0.004D;
            zd += Mth.cos(age * 0.21F + spin * 10.0F) * 0.004D;
            float t = (float) age / lifetime;
            setAlpha(t < 0.7F ? 1.0F : 1.0F - (t - 0.7F) / 0.3F);
        }
        @Override protected SingleQuadParticle.Layer getLayer() { return SingleQuadParticle.Layer.TRANSLUCENT; }
    }

    private static final class SparkParticle extends SingleQuadParticle {
        private final SpriteSet sprites;
        private SparkParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z, sprites.get(0, 1));
            this.sprites = sprites;
            this.lifetime = 6 + random.nextInt(4);
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.xd = this.zd = 0.0D;
            this.yd = 0.01D;
            this.quadSize = 0.22F + random.nextFloat() * 0.2F;
        }
        @Override public void tick() { super.tick(); if (!removed) setSpriteFromAge(sprites); }
        @Override protected SingleQuadParticle.Layer getLayer() { return SingleQuadParticle.Layer.TRANSLUCENT; }
        @Override protected int getLightCoords(float partialTick) { return 15728880; }
    }

    private KinoraParticles() {}
}
