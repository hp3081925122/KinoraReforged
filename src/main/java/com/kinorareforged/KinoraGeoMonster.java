package com.kinorareforged;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class KinoraGeoMonster extends Monster implements KinoraGeoEntity {
    protected static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(KinoraGeoMonster.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(KinoraGeoMonster.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected String animationProcedure = "empty";
    private int animationRequest;
    private int handledAnimationRequest;

    protected KinoraGeoMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, "undefined");
        builder.define(TEXTURE, defaultTexture());
    }

    protected abstract String defaultTexture();

    protected abstract RawAnimation loopAnimation(AnimationTest<?> state);

    protected int animationTransitionTicks() {
        return 2;
    }

    @Override
    public String getTexture() {
        return entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    public String getSyncedAnimation() {
        return entityData.get(ANIMATION);
    }

    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    public void tick() {
        String synced = getSyncedAnimation();
        if (!synced.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            animationProcedure = synced;
            animationRequest++;
        }
        super.tick();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Texture", getTexture());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getString("Texture").ifPresent(this::setTexture);
    }

    private PlayState movementPredicate(AnimationTest<KinoraGeoMonster> state) {
        if (animationProcedure.equals("empty")) {
            return state.setAndContinue(loopAnimation(state));
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationTest<KinoraGeoMonster> state) {
        AnimationController<KinoraGeoMonster> controller = state.controller();
        if (animationProcedure.equals("empty")) {
            handledAnimationRequest = animationRequest;
            if (controller.getCurrentRawAnimation() != null) controller.reset();
            return PlayState.STOP;
        }
        AnimationPoint point = controller.getCurrentAnimationPoint();
        boolean holding = point != null && point.loopType() == LoopType.HOLD_ON_LAST_FRAME;
        boolean idle = controller.getCurrentRawAnimation() == null || controller.getTimeline() == null || (controller.hasAnimationFinished() && !holding);
        if (!idle) return PlayState.CONTINUE;
        if (handledAnimationRequest != animationRequest) {
            handledAnimationRequest = animationRequest;
            controller.reset();
            controller.setAnimation(RawAnimation.begin().thenPlay(animationProcedure));
            return PlayState.CONTINUE;
        }
        animationProcedure = "empty";
        handledAnimationRequest = animationRequest;
        controller.reset();
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<KinoraGeoMonster>("movement", animationTransitionTicks(), this::movementPredicate));
        controllers.add(new AnimationController<KinoraGeoMonster>("procedure", animationTransitionTicks(), this::procedurePredicate));
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return net.minecraft.sounds.SoundEvents.GENERIC_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return net.minecraft.sounds.SoundEvents.GENERIC_DEATH;
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
