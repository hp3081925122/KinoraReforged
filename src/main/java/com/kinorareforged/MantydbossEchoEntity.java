package com.kinorareforged;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class MantydbossEchoEntity extends KinoraGeoPathfinder {
    private int echoAge = -1;

    public MantydbossEchoEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new RandomLookAroundGoal(this));
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (level().isClientSide) {
            return;
        }
        if (echoAge < 0) {
            echoAge = 0;
            setAnimation("attacksummon");
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 300, 0.7D, 1.6D, 0.7D, 0.1D);
            }
        }
        echoAge++;
        if (echoAge == 81) {
            float damage = (float) Math.max(0.0D, getAttributeValue(Attributes.ATTACK_DAMAGE));
            for (Mob target : level().getEntitiesOfClass(Mob.class, new AABB(position(), position()).inflate(10.0D), mob -> mob != this)) {
                target.hurt(damageSources().mobAttack(this), damage);
            }
            level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 2.0F, 0.6F);
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                net.minecraft.world.phys.Vec3 center = position().add(0.0D, 1.2D, 0.0D);
                for (int i = 0; i < 4; i++) {
                    KinoraFx.echoSlash(server, center, getYRot() + i * 90.0F + 45.0F, (i % 2 == 0 ? 10.0F : -10.0F), 7.0F, 9);
                }
                KinoraFx.echoRing(server, position().add(0.0D, 0.08D, 0.0D), 10.0F, 16);
                KinoraFx.echoRing(server, position().add(0.0D, 0.1D, 0.0D), 6.0F, 11);
                KinoraFx.sparks(server, center, 8, 2.0D);
                KinoraFx.shake(server, this, 24.0D, 1.5F, 10);
            }
        }
        if (echoAge >= 95) {
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 300, 0.7D, 1.6D, 0.7D, 0.1D);
            }
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KinoraEchoAge", echoAge);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        echoAge = tag.getInt("KinoraEchoAge");
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    protected String defaultTexture() {
        return "mantydbosstextureecho";
    }

    @Override
    protected RawAnimation loopAnimation(AnimationState<?> state) {
        return RawAnimation.begin().thenLoop("idle");
    }
}
