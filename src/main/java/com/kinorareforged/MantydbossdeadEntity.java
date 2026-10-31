package com.kinorareforged;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public final class MantydbossdeadEntity extends KinoraGeoPathfinder {
    private int revivalTicks;

    public MantydbossdeadEntity(EntityType<? extends PathfinderMob> type, Level level) {
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
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.MANTYD_MEDICINE.get()) || revivalTicks > 0) {
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide) {
            stack.shrink(1);
            revivalTicks = 130;
            setAnimation("standingafterdeath");
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(), 300, 1.0D, 0.6D, 1.0D, 0.1D);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (revivalTicks <= 0) {
            return;
        }
        if (--revivalTicks == 0 && level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 600, 1.0D, 0.6D, 1.0D, 0.1D);
            MantydbosssitingEntity restored = ModEntities.MANTYDBOSSSITING.get().spawn(server, blockPosition(), net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED);
            if (restored != null) {
                restored.setYRot(getYRot());
                restored.setYHeadRot(getYRot());
                restored.setYBodyRot(getYRot());
                restored.setDeltaMovement(0.0D, 0.0D, 0.0D);
            }
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KinoraRevivalTicks", revivalTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        revivalTicks = Math.max(0, Math.min(130, tag.getInt("KinoraRevivalTicks")));
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
        return "mantydbosstexture";
    }

    @Override
    protected RawAnimation loopAnimation(AnimationState<?> state) {
        return RawAnimation.begin().thenLoop("deathloop");
    }

    @Override
    protected int animationTransitionTicks() {
        return 0;
    }
}
