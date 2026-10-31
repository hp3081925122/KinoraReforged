package com.kinorareforged;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;

public final class MantydBloomEntity extends KinoraGeoMonster {
    public MantydBloomEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_DROWNING)) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected String movementAnimation() {
        return "idle";
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (!level().isClientSide) {
            addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 1, false, false));
            int ia = getPersistentData().getInt("IA") + 1;
            getPersistentData().putInt("IA", ia);
            if (ia == 20 && level() instanceof ServerLevel server) {
                server.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("message.kinora_reforged.bloom_appeared"), false);
            }
            if (ia == 100) {
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 600, 8.0D, 6.0D, 8.0D, 0.5D);
                }
                for (BossMantydEntity boss : level().getEntitiesOfClass(BossMantydEntity.class, new AABB(blockPosition()).inflate(24.0D))) {
                    boss.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 4, false, false));
                }
            }
            if (ia == 105) {
                discard();
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 80, 0.5D, 1.0D, 0.5D, 0.1D);
                }
            }
            if (ia == 140) {
                getPersistentData().putInt("IA", 0);
            }
        }
    }
}
