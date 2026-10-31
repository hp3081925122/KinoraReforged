package com.kinorareforged;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class KinoraGeoMonster extends Monster implements GeoEntity {
    protected static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(KinoraGeoMonster.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected String animationProcedure = "empty";

    protected KinoraGeoMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
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

    private software.bernie.geckolib.animation.PlayState movementPredicate(AnimationState<KinoraGeoMonster> state) {
        if (!animationProcedure.equals("empty")) {
            return software.bernie.geckolib.animation.PlayState.STOP;
        }
        if (isDeadOrDying()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("death"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop(state.isMoving() ? movementAnimation() : idleAnimation()));
    }

    private software.bernie.geckolib.animation.PlayState procedurePredicate(AnimationState<KinoraGeoMonster> state) {
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
