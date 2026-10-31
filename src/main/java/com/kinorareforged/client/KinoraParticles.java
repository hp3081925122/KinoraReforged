package com.kinorareforged.client;

import com.kinorareforged.ModParticles;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class KinoraParticles {
    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SLASH.get(), sprites -> (options, level, x, y, z, vx, vy, vz) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.SLASH));
        event.registerSpriteSet(ModParticles.ECHO_SLASH.get(), sprites -> (options, level, x, y, z, vx, vy, vz) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.SLASH));
        event.registerSpriteSet(ModParticles.RING.get(), sprites -> (options, level, x, y, z, vx, vy, vz) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.RING));
        event.registerSpriteSet(ModParticles.ECHO_RING.get(), sprites -> (options, level, x, y, z, vx, vy, vz) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.RING));
        event.registerSpriteSet(ModParticles.WARN.get(), sprites -> (options, level, x, y, z, vx, vy, vz) -> new FlatFxParticle(level, x, y, z, options, sprites, Mode.WARN));
        event.registerSpriteSet(ModParticles.PETAL.get(), sprites -> (SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) -> new PetalParticle(level, x, y, z, vx, vy, vz, sprites));
        event.registerSpriteSet(ModParticles.SPARK.get(), sprites -> (SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) -> new SparkParticle(level, x, y, z, sprites));
    }

    private enum Mode {
        SLASH,
        RING,
        WARN
    }

    private static final class FlatFxParticle extends TextureSheetParticle {
        private final SpriteSet sprites;
        private final Mode mode;
        private final float yaw;
        private final float pitch;
        private final float tilt;
        private final float size;

        private FlatFxParticle(ClientLevel level, double x, double y, double z, ModParticles.FxOptions options, SpriteSet sprites, Mode mode) {
            super(level, x, y, z);
            this.sprites = sprites;
            this.mode = mode;
            this.yaw = options.yaw() * Mth.DEG_TO_RAD;
            this.pitch = options.pitch() * Mth.DEG_TO_RAD;
            this.tilt = options.tilt() * Mth.DEG_TO_RAD;
            this.size = Math.max(0.1F, options.size());
            this.lifetime = Math.max(2, options.life());
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.xd = 0.0D;
            this.yd = 0.0D;
            this.zd = 0.0D;
            if (mode == Mode.WARN) {
                setSprite(sprites.get(0, 1));
            } else {
                setSpriteFromAge(sprites);
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed && mode != Mode.WARN) {
                setSpriteFromAge(sprites);
            }
        }

        @Override
        public void render(VertexConsumer buffer, Camera camera, float partialTick) {
            Vec3 cam = camera.getPosition();
            float px = (float) (Mth.lerp(partialTick, xo, x) - cam.x());
            float py = (float) (Mth.lerp(partialTick, yo, y) - cam.y());
            float pz = (float) (Mth.lerp(partialTick, zo, z) - cam.z());
            float t = Mth.clamp((age + partialTick) / lifetime, 0.0F, 1.0F);
            switch (mode) {
                case SLASH -> {
                    Quaternionf rotation = new Quaternionf().rotateY(Mth.PI - yaw).rotateX(-pitch).rotateZ(tilt).rotateX(-Mth.HALF_PI);
                    float alpha = t < 0.7F ? 1.0F : 1.0F - (t - 0.7F) / 0.3F;
                    quad(buffer, rotation, px, py, pz, size * (0.88F + 0.12F * t), alpha);
                }
                case RING -> {
                    Quaternionf rotation = new Quaternionf().rotateY(yaw + (age + partialTick) * 0.04F).rotateX(-Mth.HALF_PI);
                    float grow = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
                    quad(buffer, rotation, px, py, pz, size * (0.2F + 0.8F * grow), 1.0F - t * t * 0.6F);
                }
                case WARN -> {
                    Quaternionf rotation = new Quaternionf().rotateY((age + partialTick) * 0.02F).rotateX(-Mth.HALF_PI);
                    float pulse = 0.7F + 0.3F * Mth.sin((age + partialTick) * 0.6F);
                    setSprite(sprites.get(0, 1));
                    quad(buffer, rotation, px, py, pz, size, 0.9F * pulse);
                    setSprite(sprites.get(1, 1));
                    quad(buffer, rotation, px, py + 0.01F, pz, size * t * t, 0.45F + 0.4F * t);
                    setSprite(sprites.get(0, 1));
                }
            }
        }

        private void quad(VertexConsumer buffer, Quaternionf rotation, float px, float py, float pz, float half, float alpha) {
            Vector3f a = corner(rotation, -1.0F, -1.0F, half, px, py, pz);
            Vector3f b = corner(rotation, -1.0F, 1.0F, half, px, py, pz);
            Vector3f c = corner(rotation, 1.0F, 1.0F, half, px, py, pz);
            Vector3f d = corner(rotation, 1.0F, -1.0F, half, px, py, pz);
            float u0 = getU0();
            float u1 = getU1();
            float v0 = getV0();
            float v1 = getV1();
            int light = LightTexture.FULL_BRIGHT;
            float al = Mth.clamp(alpha, 0.0F, 1.0F);
            vertex(buffer, d, u1, v1, al, light);
            vertex(buffer, c, u1, v0, al, light);
            vertex(buffer, b, u0, v0, al, light);
            vertex(buffer, a, u0, v1, al, light);
            vertex(buffer, a, u0, v1, al, light);
            vertex(buffer, b, u0, v0, al, light);
            vertex(buffer, c, u1, v0, al, light);
            vertex(buffer, d, u1, v1, al, light);
        }

        private static Vector3f corner(Quaternionf rotation, float lx, float ly, float half, float px, float py, float pz) {
            return new Vector3f(lx, ly, 0.0F).rotate(rotation).mul(half).add(px, py, pz);
        }

        private void vertex(VertexConsumer buffer, Vector3f pos, float u, float v, float alpha, int light) {
            buffer.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(rCol, gCol, bCol, alpha).setLight(light);
        }

        @Override
        protected int getLightColor(float partialTick) {
            return LightTexture.FULL_BRIGHT;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        public AABB getRenderBoundingBox(float partialTicks) {
            return new AABB(x, y, z, x, y, z).inflate(size + 1.0D);
        }
    }

    private static final class PetalParticle extends TextureSheetParticle {
        private final float spin;

        private PetalParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z, vx, vy, vz);
            pickSprite(sprites);
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

        @Override
        public void tick() {
            oRoll = roll;
            super.tick();
            if (onGround) {
                return;
            }
            roll += spin;
            xd += Mth.sin(age * 0.25F + spin * 10.0F) * 0.004D;
            zd += Mth.cos(age * 0.21F + spin * 10.0F) * 0.004D;
            float t = (float) age / lifetime;
            alpha = t < 0.7F ? 1.0F : 1.0F - (t - 0.7F) / 0.3F;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    private static final class SparkParticle extends TextureSheetParticle {
        private final SpriteSet sprites;

        private SparkParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z);
            this.sprites = sprites;
            this.lifetime = 6 + random.nextInt(4);
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.xd = 0.0D;
            this.yd = 0.01D;
            this.zd = 0.0D;
            this.quadSize = 0.22F + random.nextFloat() * 0.2F;
            setSpriteFromAge(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed) {
                setSpriteFromAge(sprites);
            }
        }

        @Override
        protected int getLightColor(float partialTick) {
            return LightTexture.FULL_BRIGHT;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    private KinoraParticles() {
    }
}
