package com.kinorareforged;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;

public abstract class KinoraGeoPathfinder extends PathfinderMob implements GeoEntity {
    protected static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(KinoraGeoPathfinder.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected String animationProcedure = "empty";

    protected KinoraGeoPathfinder(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_FIRE) || source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof net.minecraft.world.entity.player.Player || source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.WITHER) || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.WITHER_SKULL) || source.is(DamageTypes.TRIDENT)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, "undefined");
    }

    public String getSyncedAnimation() {
        return entityData.get(ANIMATION);
    }

    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
        animationProcedure = animation.equals("undefined") ? "empty" : animation;
    }

    protected String movementAnimation() {
        return "walk";
    }

    protected String idleAnimation() {
        return "idle";
    }

    private software.bernie.geckolib.animation.PlayState movementPredicate(AnimationState<KinoraGeoPathfinder> state) {
        if (!animationProcedure.equals("empty")) {
            return software.bernie.geckolib.animation.PlayState.STOP;
        }
        if (isDeadOrDying()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("death"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop(state.isMoving() ? movementAnimation() : idleAnimation()));
    }

    private software.bernie.geckolib.animation.PlayState procedurePredicate(AnimationState<KinoraGeoPathfinder> state) {
        if (!animationProcedure.equals("empty") && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
            state.getController().setAnimation(RawAnimation.begin().thenPlay(animationProcedure));
            animationProcedure = "empty";
            state.getController().forceAnimationReset();
        }
        return animationProcedure.equals("empty") ? software.bernie.geckolib.animation.PlayState.STOP : software.bernie.geckolib.animation.PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "procedure", 2, this::procedurePredicate));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
