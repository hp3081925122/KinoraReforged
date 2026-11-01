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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class KinoraGeoPathfinder extends PathfinderMob implements KinoraGeoEntity {
    protected static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(KinoraGeoPathfinder.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(KinoraGeoPathfinder.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected String animationProcedure = "empty";
    private int animationRequest;
    private int handledAnimationRequest;

    protected KinoraGeoPathfinder(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_FIRE) || source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof net.minecraft.world.entity.player.Player || source.getDirectEntity() instanceof AbstractThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.WITHER) || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.WITHER_SKULL) || source.is(DamageTypes.TRIDENT)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
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

    private PlayState movementPredicate(AnimationTest<KinoraGeoPathfinder> state) {
        if (animationProcedure.equals("empty")) {
            return state.setAndContinue(loopAnimation(state));
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationTest<KinoraGeoPathfinder> state) {
        AnimationController<KinoraGeoPathfinder> controller = state.controller();
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
        controllers.add(new AnimationController<KinoraGeoPathfinder>("movement", animationTransitionTicks(), this::movementPredicate));
        controllers.add(new AnimationController<KinoraGeoPathfinder>("procedure", animationTransitionTicks(), this::procedurePredicate));
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
