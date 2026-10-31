package com.kinorareforged;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.EnumSet;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BossMantydEntity extends KinoraGeoMonster {
    private final ServerBossEvent bossInfo = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_6);
    private int attackAge = -1;
    private String attackType = "";
    private int bloomTimer = -1;
    private BlockPos bloomPos = BlockPos.ZERO;
    private boolean deathReplacementSpawned;

    public BossMantydEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.42D)
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected float getDamageAfterMagicAbsorb(DamageSource source, float amount) {
        if (attackAge >= 0 && attackAge < 60 && attackType.equals("drohend") && !source.is(DamageTypeTags.BYPASSES_EFFECTS) && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            amount *= 0.2F;
        }
        return super.getDamageAfterMagicAbsorb(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean canChangeDimensions(Level oldLevel, Level newLevel) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(new net.minecraft.world.item.ItemStack(ModItems.ANCESTRAL_BAG.get()));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new ActionLockGoal());
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, ServerPlayer.class, true, true));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, true));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        targetSelector.addGoal(4, new HurtByTargetGoal(this));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        goalSelector.addGoal(6, new FloatGoal(this));
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (!level().isClientSide) {
            applyConfiguredAttributesOnce();
            tickAmbientParticles();
            tickPendingBloom();
            LivingEntity target = getTarget();
            if (target != null) {
                getPersistentData().putDouble("IA", getPersistentData().getDouble("IA") + 1.0D);
                tickCombat(target);
            } else if (attackAge >= 0) {
                attackAge++;
                tickAttack(null);
            }
            if (getPersistentData().getDouble("IA") >= 740.0D) {
                getPersistentData().putDouble("IA", 0.0D);
            }
            bossInfo.setProgress(getHealth() / getMaxHealth());
        }
        refreshDimensions();
    }

    private void tickCombat(LivingEntity target) {
        if (attackAge >= 0) {
            attackAge++;
            tickAttack(target);
            return;
        }
        int ia = (int) getPersistentData().getDouble("IA");
        if (ia == 40 || ia == 360) {
            stopMusic();
            level().playSound(null, blockPosition(), ModSounds.KINORA_BOSS_THEME.get(), SoundSource.MUSIC, 2.0F, 1.0F);
            beginAttack(random.nextBoolean() ? "fourth" : "wind");
        } else if (ia == 120 || ia == 460) {
            beginAttack(random.nextBoolean() ? "drohend" : "slither");
        } else if (ia == 290 || ia == 630) {
            beginAttack(random.nextBoolean() ? "slash" : "jump");
            if (ia == 630) {
                bloomTimer = 239;
                bloomPos = blockPosition();
            }
        } else if (ia >= 740) {
            getPersistentData().putDouble("IA", 0.0D);
        }
    }

    private void beginAttack(String type) {
        attackType = type;
        attackAge = 0;
        setAnimation("attack" + type);
        getNavigation().stop();
    }

    private boolean isActionLocked() {
        if (attackAge < 0) return false;
        return switch (attackType) {
            case "fourth", "wind" -> attackAge < 50;
            case "drohend" -> attackAge < 60 || attackAge >= 80 && attackAge < 115;
            case "slither" -> attackAge % 60 < 50;
            case "slash", "jump" -> attackAge < 65;
            default -> false;
        };
    }

    private final class ActionLockGoal extends Goal {
        private ActionLockGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return isActionLocked();
        }

        @Override
        public void start() {
            getNavigation().stop();
        }

        @Override
        public void tick() {
            getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    private void tickAttack(LivingEntity target) {
        switch (attackType) {
            case "fourth" -> {
                if (attackAge >= 20 && attackAge <= 30 && (attackAge - 20) % 2 == 0) {
                    areaDamage(4.0D, 12.0D, 100, 2.0D, 0.2D, 2.0D, false, 1.6F);
                }
                if (attackAge >= 45) finishAttack();
            }
            case "wind" -> {
                if (attackAge == 20) areaDamage(5.5D, 20.0D, 200, 3.0D, 0.2D, 3.0D, true, 1.4F);
                if (attackAge == 33) areaDamage(8.0D, 20.0D, 300, 5.0D, 0.2D, 5.0D, true, 1.4F);
                if (attackAge >= 45) finishAttack();
            }
            case "drohend" -> {
                if (attackAge == 60) {
                    setAnimation("empty");
                    setDeltaMovement(0.0D, 0.6D, 0.0D);
                }
                if (attackAge == 70) {
                    spawnParticles(ParticleTypes.POOF, 70, 0.1D, 0.3D, 0.1D, 0.4D);
                    if (target != null && target.isAlive()) dashToward(target);
                }
                if (attackAge == 80) setAnimation("attackdash");
                if (attackAge == 102) areaDamage(4.0D, 14.0D, 100, 2.0D, 0.2D, 2.0D, false, 1.6F);
                if (attackAge >= 115) finishAttack();
            }
            case "slither" -> {
                if (attackAge == 27 || attackAge == 87 || attackAge == 147) {
                    areaDamage(5.5D, 16.0D, 200, 3.0D, 0.2D, 3.0D, true, 1.6F);
                    spawnArrows(6);
                }
                if (attackAge == 45 || attackAge == 105 || attackAge == 165) setAnimation("empty");
                if (attackAge >= 170) finishAttack();
            }
            case "slash" -> {
                if (attackAge == 18 || attackAge == 38) {
                    areaDamage(3.5D, 16.0D, 200, 2.0D, 0.2D, 2.0D, true, 1.6F);
                    spawnArrows(3);
                }
                if (attackAge >= 65) finishAttack(false);
            }
            case "jump" -> {
                if (attackAge == 45) areaDamage(8.0D, 14.0D, 300, 5.0D, 0.2D, 5.0D, false, 0.6F);
                if (attackAge >= 65) finishAttack();
            }
            default -> finishAttack();
        }
    }

    private void finishAttack() {
        finishAttack(true);
    }

    private void finishAttack(boolean clearAnimation) {
        attackAge = -1;
        attackType = "";
        if (clearAnimation) setAnimation("empty");
    }

    private void cancelAttack() {
        if (attackAge >= 0) finishAttack();
    }

    private float skillDamage(double originalDamage) {
        double base = getAttributeValue(Attributes.ATTACK_DAMAGE);
        return (float) Math.max(0.0D, base * originalDamage / 40.0D);
    }

    private void areaDamage(double radius, double originalDamage, int particles, double dx, double dy, double dz, boolean sweep, float pitch) {
        Vec3 center = position();
        net.minecraft.sounds.SoundEvent sound = sweep ? net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP : net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value();
        level().playSound(null, blockPosition(), sound, SoundSource.NEUTRAL, 1.5F, pitch);
        spawnParticles(ParticleTypes.SWEEP_ATTACK, particles, dx, dy, dz, 0.01D);
        for (Player target : level().getEntitiesOfClass(Player.class, new AABB(center, center).inflate(radius))) {
            target.hurt(damageSources().mobAttack(this), skillDamage(originalDamage));
        }
    }

    private void dashToward(LivingEntity target) {
        Vec3 delta = target.position().subtract(position());
        setDeltaMovement(delta.x * 0.25D, delta.y * 0.25D + 0.05D, delta.z * 0.25D);
        hasImpulse = true;
    }

    private void spawnArrows(int count) {
        for (int i = 0; i < count; i++) {
            PetalProjectileEntity projectile = new PetalProjectileEntity(ModEntities.PETAL_PROJECTILE.get(), this, level());
            projectile.setPos(getX(), getEyeY() - 0.1D, getZ());
            projectile.setBaseDamage(skillDamage(5.0D));
            Vec3 look = getLookAngle();
            projectile.shoot(look.x, look.y, look.z, 2.0F, 30.0F);
            level().addFreshEntity(projectile);
        }
    }

    private void tickAmbientParticles() {
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(ParticleTypes.CHERRY_LEAVES, getX(), getY() + 6.0D, getZ(), 1, 20.0D, 3.0D, 20.0D, 1.0D);
        }
    }

    private void tickPendingBloom() {
        if (bloomTimer < 0) return;
        if (bloomTimer-- == 0 && level() instanceof net.minecraft.server.level.ServerLevel server) {
            MantydBloomEntity bloom = ModEntities.MANTYD_BLOOM.get().spawn(server, bloomPos, MobSpawnType.MOB_SUMMONED);
            if (bloom != null) {
                bloom.setDeltaMovement(0.0D, 0.0D, 0.0D);
            }
            bloomTimer = -1;
        }
    }

    private void spawnParticles(net.minecraft.core.particles.ParticleOptions type, int count, double dx, double dy, double dz, double speed) {
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(type, getX(), getY(), getZ(), count, dx, dy, dz, speed);
        }
    }

    private void stopMusic() {
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            for (ServerPlayer player : server.getPlayers(player -> player.distanceToSqr(this) <= 4096.0D)) {
                player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.MUSIC));
            }
        }
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if (deathTime < 100 || level().isClientSide || deathReplacementSpawned) return;
        deathReplacementSpawned = true;
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            MantydbossdeadEntity replacement = ModEntities.MANTYDBOSSDEAD.get().create(server);
            if (replacement != null) {
                replacement.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
                server.addFreshEntity(replacement);
            }
            stopMusic();
        }
        remove(Entity.RemovalReason.KILLED);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KinoraAttackAge", attackAge);
        tag.putString("KinoraAttackType", attackType);
        tag.putInt("KinoraBloomTimer", bloomTimer);
        tag.putLong("KinoraBloomPos", bloomPos.asLong());
        tag.putBoolean("KinoraDeathReplacementSpawned", deathReplacementSpawned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        attackAge = tag.getInt("KinoraAttackAge");
        attackType = tag.getString("KinoraAttackType").toLowerCase(Locale.ROOT);
        bloomTimer = tag.getInt("KinoraBloomTimer");
        bloomPos = tag.contains("KinoraBloomPos") ? BlockPos.of(tag.getLong("KinoraBloomPos")) : blockPosition();
        deathReplacementSpawned = tag.getBoolean("KinoraDeathReplacementSpawned");
        if (attackAge < -1) attackAge = -1;
    }

    private void applyConfiguredAttributesOnce() {
        if (getPersistentData().getBoolean("KinoraReforgedAttributesApplied")) {
            return;
        }
        float healthRatio = getMaxHealth() > 0.0F ? getHealth() / getMaxHealth() : 1.0F;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(KinoraConfig.BOSS_MAX_HEALTH.get());
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(KinoraConfig.BOSS_ATTACK_DAMAGE.get());
        getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(KinoraConfig.BOSS_KNOCKBACK_RESISTANCE.get());
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(KinoraConfig.BOSS_MOVEMENT_SPEED.get());
        setHealth(Math.min(getMaxHealth(), Math.max(1.0F, healthRatio * getMaxHealth())));
        getPersistentData().putBoolean("KinoraReforgedAttributesApplied", true);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossInfo.removePlayer(player);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        bossInfo.setName(getDisplayName());
    }

    @Override
    protected String defaultTexture() {
        return "mantydbosstexture";
    }

    @Override
    protected RawAnimation loopAnimation(AnimationState<?> state) {
        if (state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) {
            return RawAnimation.begin().thenLoop("walk");
        }
        if (isDeadOrDying()) {
            return RawAnimation.begin().thenPlay("death");
        }
        return RawAnimation.begin().thenLoop("idle");
    }
}
