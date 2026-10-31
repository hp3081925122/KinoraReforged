package com.kinorareforged;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class KinoraFx {
    public static void slash(ServerLevel level, Vec3 pos, float yaw, float tilt, float size, int life) {
        slash(level, pos, yaw, 0.0F, tilt, size, life);
    }

    public static void slash(ServerLevel level, Vec3 pos, float yaw, float pitch, float tilt, float size, int life) {
        send(level, new ModParticles.FxOptions(ModParticles.SLASH.get(), yaw, pitch, tilt, size, life), pos);
    }

    public static void echoSlash(ServerLevel level, Vec3 pos, float yaw, float tilt, float size, int life) {
        send(level, new ModParticles.FxOptions(ModParticles.ECHO_SLASH.get(), yaw, 0.0F, tilt, size, life), pos);
    }

    public static void ring(ServerLevel level, Vec3 pos, float size, int life) {
        send(level, new ModParticles.FxOptions(ModParticles.RING.get(), 0.0F, 0.0F, 0.0F, size, life), pos);
    }

    public static void echoRing(ServerLevel level, Vec3 pos, float size, int life) {
        send(level, new ModParticles.FxOptions(ModParticles.ECHO_RING.get(), 0.0F, 0.0F, 0.0F, size, life), pos);
    }

    public static void warn(ServerLevel level, Vec3 pos, float size, int life) {
        send(level, new ModParticles.FxOptions(ModParticles.WARN.get(), 0.0F, 0.0F, 0.0F, size, life), pos);
    }

    public static void petals(ServerLevel level, Vec3 pos, int count, double spread, double height, double speed) {
        level.sendParticles(ModParticles.PETAL.get(), pos.x, pos.y, pos.z, count, spread, height, spread, speed);
    }

    public static void sparks(ServerLevel level, Vec3 pos, int count, double spread) {
        level.sendParticles(ModParticles.SPARK.get(), pos.x, pos.y, pos.z, count, spread, spread * 0.6D, spread, 0.0D);
    }

    public static void shake(ServerLevel level, Entity source, double radius, float strength, int duration) {
        PacketDistributor.sendToPlayersNear(level, null, source.getX(), source.getY(), source.getZ(), radius, new KinoraNetwork.ShakePayload(strength, duration));
    }

    public static boolean isFriendly(Entity owner, Entity entity) {
        if (owner == null || entity == owner || entity.isPassengerOfSameVehicle(owner) || entity.hasPassenger(owner) || owner.hasPassenger(entity) || entity.isAlliedTo(owner)) {
            return true;
        }
        return entity instanceof net.minecraft.world.entity.OwnableEntity ownable && ownable.getOwner() == owner;
    }

    public static Vec3 front(Entity entity, double distance, double height) {
        float yaw = entity.getYRot() * ((float) Math.PI / 180.0F);
        return entity.position().add(-Math.sin(yaw) * distance, height, Math.cos(yaw) * distance);
    }

    private static void send(ServerLevel level, ParticleOptions options, Vec3 pos) {
        level.sendParticles(options, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private KinoraFx() {
    }
}
