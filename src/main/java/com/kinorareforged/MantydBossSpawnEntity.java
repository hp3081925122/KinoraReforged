package com.kinorareforged;

import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class MantydBossSpawnEntity extends KinoraGeoPathfinder {
    private int introAge;

    public MantydBossSpawnEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.FOLLOW_RANGE, 11.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.1D, true));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, ServerPlayer.class, true, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true, true));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, ServerPlayer.class, 16.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        if (level().isClientSide()) {
            return;
        }
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            for (ServerPlayer player : server.getPlayers(player -> player.distanceToSqr(this) <= 4096.0D)) {
                player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.MUSIC));
            }
        }
        if (getTarget() == null) return;
        introAge++;
        if (introAge == 20) {
            setAnimation("spawning");
            for (net.minecraft.world.entity.LivingEntity target : level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, getBoundingBox().inflate(64.0D), entity -> !(entity instanceof Player))) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 255, 5, false, false));
            }
            level().playSound(null, blockPosition(), ModSounds.KINORA_INTRO_BETTER.get(), SoundSource.NEUTRAL, 2.0F, 1.0F);
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("message.kinora_reforged.intro.0"), false);
                server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 300, 0.7D, 1.6D, 0.7D, 0.1D);
            }
        } else if (introAge == 60 || introAge == 85 || introAge == 145 || introAge == 182) {
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                int line = introAge == 60 ? 1 : introAge == 85 ? 2 : introAge == 145 ? 3 : 4;
                server.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("message.kinora_reforged.intro." + line), false);
            }
        } else if (introAge == 275) {
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                BossMantydEntity boss = ModEntities.BOSS_MANTYD.get().spawn(server, blockPosition(), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
                if (boss != null) {
                    boss.setYRot(getYRot());
                    boss.setYHeadRot(getYRot());
                    boss.setYBodyRot(getYRot());
                    boss.setDeltaMovement(0.0D, 0.0D, 0.0D);
                }
            }
            discard();
        } else if (introAge >= 500) {
            introAge = 0;
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KinoraIntroAge", introAge);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput tag) {
        super.readAdditionalSaveData(tag);
        introAge = Math.max(0, Math.min(500, tag.getIntOr("KinoraIntroAge", 0)));
    }

    @Override
    public boolean canBeCollidedWith(net.minecraft.world.entity.Entity other) {
        return true;
    }

    @Override
    public boolean canCollideWith(net.minecraft.world.entity.Entity entity) {
        return true;
    }

    @Override
    protected String defaultTexture() {
        return "mantydbosstexture";
    }

    @Override
    protected RawAnimation loopAnimation(AnimationTest<?> state) {
        return RawAnimation.begin().thenLoop("idlespawn");
    }
}
