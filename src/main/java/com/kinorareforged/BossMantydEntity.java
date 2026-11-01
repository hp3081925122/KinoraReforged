package com.kinorareforged;

import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;

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
import net.minecraft.world.entity.EntitySpawnReason;
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
    private final ServerBossEvent bossInfo = new ServerBossEvent(getUUID(), getDisplayName(), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_6);
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
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) return false;
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected float getDamageAfterMagicAbsorb(DamageSource source, float amount) {
        if (attackAge >= 0 && attackAge < 60 && attackType.equals("drohend") && !source.is(DamageTypeTags.BYPASSES_EFFECTS) && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            amount *= 0.2F;
        }
        return super.getDamageAfterMagicAbsorb(source, amount);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level, Entity target) {
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
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(level, new net.minecraft.world.item.ItemStack(ModItems.ANCESTRAL_BAG.get()));
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
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        {
            applyConfiguredAttributesOnce();
            tickAmbientParticles();
            tickPendingBloom();
            LivingEntity target = getTarget();
            if (target != null) {
                getPersistentData().putDouble("IA", getPersistentData().getDouble("IA").orElse(0.0D) + 1.0D);
                tickCombat(target);
            } else if (attackAge >= 0) {
                attackAge++;
                tickAttack(null);
            }
            if (getPersistentData().getDouble("IA").orElse(0.0D) >= 740.0D) {
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
        int ia = getPersistentData().getDouble("IA").orElse(0.0D).intValue();
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
        net.minecraft.server.level.ServerLevel server = level() instanceof net.minecraft.server.level.ServerLevel serverLevel ? serverLevel : null;
        switch (attackType) {
            case "fourth" -> {
                if (attackAge == 1 && server != null) KinoraFx.warn(server, ground(), 4.0F, 19);
                if (attackAge >= 20 && attackAge <= 30 && (attackAge - 20) % 2 == 0) {
                    areaDamage(4.0D, 12.0D, false, 1.6F);
                    if (server != null) {
                        int hit = (attackAge - 20) / 2;
                        float side = hit % 2 == 0 ? 1.0F : -1.0F;
                        KinoraFx.slash(server, KinoraFx.front(this, 1.2D, 1.4D + hit * 0.12D), getYRot() + side * (15.0F + random.nextFloat() * 25.0F), side * (25.0F + random.nextFloat() * 30.0F), 3.4F, 7);
                        KinoraFx.sparks(server, KinoraFx.front(this, 2.2D, 1.5D), 2, 0.6D);
                        if (hit == 5) {
                            KinoraFx.ring(server, ground(), 4.0F, 10);
                            KinoraFx.petals(server, KinoraFx.front(this, 1.5D, 1.2D), 14, 0.8D, 0.4D, 0.18D);
                        }
                    }
                }
                if (attackAge >= 45) finishAttack();
            }
            case "wind" -> {
                if (attackAge == 1 && server != null) KinoraFx.warn(server, ground(), 5.5F, 19);
                if (attackAge == 20) {
                    areaDamage(5.5D, 20.0D, true, 1.4F);
                    windBurst(server, 5.5F, false);
                    if (server != null) KinoraFx.warn(server, ground(), 8.0F, 11);
                }
                if (attackAge == 31) {
                    areaDamage(8.0D, 20.0D, true, 1.4F);
                    windBurst(server, 8.0F, true);
                }
                if (attackAge >= 45) finishAttack();
            }
            case "drohend" -> {
                if (server != null && attackAge < 60 && attackAge % 10 == 0) {
                    KinoraFx.ring(server, ground(), 2.6F, 12);
                    KinoraFx.petals(server, position().add(0.0D, 1.6D, 0.0D), 5, 1.2D, 0.8D, 0.04D);
                    KinoraFx.sparks(server, position().add(0.0D, 2.0D, 0.0D), 2, 1.0D);
                }
                if (attackAge == 60) {
                    setAnimation("empty");
                    setDeltaMovement(0.0D, 0.6D, 0.0D);
                    if (server != null) KinoraFx.ring(server, ground(), 3.0F, 10);
                }
                if (attackAge == 70) {
                    spawnParticles(ParticleTypes.POOF, 70, 0.1D, 0.3D, 0.1D, 0.4D);
                    if (server != null) KinoraFx.petals(server, position().add(0.0D, 1.0D, 0.0D), 20, 0.6D, 0.6D, 0.25D);
                    if (target != null && target.isAlive()) dashToward(target);
                }
                if (attackAge == 80) {
                    setAnimation("attackdash");
                    if (server != null) KinoraFx.warn(server, ground(), 4.0F, 19);
                }
                if (attackAge == 99) {
                    areaDamage(4.0D, 14.0D, false, 1.6F);
                    if (server != null) {
                        Vec3 pos = KinoraFx.front(this, 1.4D, 1.3D);
                        KinoraFx.slash(server, pos, getYRot() - 20.0F, 40.0F, 4.0F, 8);
                        KinoraFx.slash(server, pos, getYRot() + 20.0F, -40.0F, 4.0F, 8);
                        KinoraFx.ring(server, ground(), 4.5F, 12);
                        KinoraFx.sparks(server, pos, 6, 1.0D);
                        KinoraFx.petals(server, pos, 18, 0.8D, 0.4D, 0.2D);
                        KinoraFx.shake(server, this, 24.0D, 1.6F, 8);
                    }
                }
                if (attackAge >= 115) finishAttack();
            }
            case "slither" -> {
                int cycle = attackAge % 60;
                if (attackAge > 0 && cycle == 0 && attackAge < 180) setAnimation("attackslither");
                if (cycle == 1 && server != null) KinoraFx.warn(server, ground(), 5.5F, 24);
                if (cycle == 25) {
                    areaDamage(5.5D, 16.0D, true, 1.6F);
                    spawnArrows(6);
                    if (server != null) {
                        KinoraFx.slash(server, KinoraFx.front(this, 0.8D, 0.5D), getYRot(), 0.0F, 5.5F, 8);
                        KinoraFx.ring(server, ground(), 5.5F, 12);
                        KinoraFx.petals(server, KinoraFx.front(this, 2.0D, 0.5D), 22, 1.6D, 0.2D, 0.22D);
                        KinoraFx.sparks(server, KinoraFx.front(this, 2.5D, 0.6D), 4, 1.2D);
                        KinoraFx.shake(server, this, 20.0D, 0.8F, 6);
                    }
                }
                if (cycle == 45) setAnimation("empty");
                if (attackAge >= 170) finishAttack();
            }
            case "slash" -> {
                if (attackAge == 1 && server != null) KinoraFx.warn(server, ground(), 3.5F, 13);
                if (attackAge == 14 || attackAge == 34) {
                    areaDamage(3.5D, 16.0D, true, 1.6F);
                    spawnArrows(3);
                    if (server != null) {
                        float side = attackAge == 14 ? 1.0F : -1.0F;
                        KinoraFx.slash(server, KinoraFx.front(this, 1.0D, 1.5D), getYRot() + side * 25.0F, side * 55.0F, 3.8F, 7);
                        KinoraFx.sparks(server, KinoraFx.front(this, 2.0D, 1.5D), 3, 0.8D);
                        KinoraFx.petals(server, KinoraFx.front(this, 1.6D, 1.4D), 10, 0.8D, 0.5D, 0.16D);
                        if (attackAge == 14) KinoraFx.warn(server, ground(), 3.5F, 20);
                    }
                }
                if (attackAge >= 65) finishAttack(false);
            }
            case "jump" -> {
                if (attackAge == 6 && server != null) {
                    spawnParticles(ParticleTypes.POOF, 30, 1.0D, 0.1D, 1.0D, 0.08D);
                    KinoraFx.ring(server, ground(), 2.8F, 10);
                }
                if (attackAge == 10 && server != null) KinoraFx.warn(server, ground(), 8.0F, 35);
                if (attackAge == 45) {
                    areaDamage(8.0D, 14.0D, false, 0.6F);
                    if (server != null) {
                        KinoraFx.ring(server, ground(), 8.5F, 14);
                        KinoraFx.ring(server, ground().add(0.0D, 0.02D, 0.0D), 5.0F, 10);
                        KinoraFx.petals(server, position().add(0.0D, 0.6D, 0.0D), 50, 2.5D, 0.3D, 0.3D);
                        KinoraFx.sparks(server, position().add(0.0D, 0.5D, 0.0D), 10, 2.5D);
                        spawnParticles(ParticleTypes.POOF, 40, 2.0D, 0.1D, 2.0D, 0.12D);
                        KinoraFx.shake(server, this, 32.0D, 3.0F, 14);
                    }
                }
                if (attackAge >= 65) finishAttack();
            }
            default -> finishAttack();
        }
    }

    private void windBurst(net.minecraft.server.level.ServerLevel server, float size, boolean heavy) {
        if (server == null) return;
        Vec3 pos = position().add(0.0D, 1.2D, 0.0D);
        KinoraFx.slash(server, pos, getYRot() + 90.0F, heavy ? 8.0F : -8.0F, size, 8);
        KinoraFx.slash(server, pos, getYRot() - 90.0F, heavy ? -8.0F : 8.0F, size, 8);
        KinoraFx.ring(server, ground(), size, 12);
        KinoraFx.petals(server, pos, heavy ? 36 : 22, size * 0.35D, 0.3D, 0.28D);
        KinoraFx.sparks(server, pos, heavy ? 6 : 3, size * 0.3D);
        KinoraFx.shake(server, this, 24.0D, heavy ? 1.4F : 0.7F, heavy ? 9 : 6);
    }

    private Vec3 ground() {
        return position().add(0.0D, 0.08D, 0.0D);
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

    private void areaDamage(double radius, double originalDamage, boolean sweep, float pitch) {
        Vec3 center = position();
        net.minecraft.sounds.SoundEvent sound = sweep ? net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP : net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value();
        level().playSound(null, blockPosition(), sound, SoundSource.NEUTRAL, 1.5F, pitch);
        for (Player target : level().getEntitiesOfClass(Player.class, new AABB(center, center).inflate(radius))) {
            if (level() instanceof net.minecraft.server.level.ServerLevel server) target.hurtServer(server, damageSources().mobAttack(this), skillDamage(originalDamage));
        }
    }

    private void dashToward(LivingEntity target) {
        Vec3 delta = target.position().subtract(position());
        setDeltaMovement(delta.x * 0.25D, delta.y * 0.25D + 0.05D, delta.z * 0.25D);
        needsSync = true;
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
            MantydBloomEntity bloom = ModEntities.MANTYD_BLOOM.get().spawn(server, bloomPos, EntitySpawnReason.MOB_SUMMONED);
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
        if (deathTime < 100 || level().isClientSide() || deathReplacementSpawned) return;
        deathReplacementSpawned = true;
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            MantydbossdeadEntity replacement = ModEntities.MANTYDBOSSDEAD.get().create(server, EntitySpawnReason.MOB_SUMMONED);
            if (replacement != null) {
                replacement.setPos(getX(), getY(), getZ());
                replacement.setYRot(getYRot());
                replacement.setXRot(getXRot());
                server.addFreshEntity(replacement);
            }
            stopMusic();
        }
        remove(Entity.RemovalReason.KILLED);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KinoraAttackAge", attackAge);
        tag.putString("KinoraAttackType", attackType);
        tag.putInt("KinoraBloomTimer", bloomTimer);
        tag.putLong("KinoraBloomPos", bloomPos.asLong());
        tag.putBoolean("KinoraDeathReplacementSpawned", deathReplacementSpawned);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput tag) {
        super.readAdditionalSaveData(tag);
        attackAge = tag.getIntOr("KinoraAttackAge", -1);
        attackType = tag.getStringOr("KinoraAttackType", "").toLowerCase(Locale.ROOT);
        bloomTimer = tag.getIntOr("KinoraBloomTimer", -1);
        bloomPos = tag.getLong("KinoraBloomPos").map(BlockPos::of).orElseGet(this::blockPosition);
        deathReplacementSpawned = tag.getBooleanOr("KinoraDeathReplacementSpawned", false);
        if (attackAge < -1) attackAge = -1;
    }

    private void applyConfiguredAttributesOnce() {
        if (getPersistentData().getBoolean("KinoraReforgedAttributesApplied").orElse(false)) {
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
    protected RawAnimation loopAnimation(AnimationTest<?> state) {
        if (state.isMoving() || Math.abs(state.getDataOrDefault(KinoraAnimationData.WALK_SPEED, 0.0F)) >= 0.15F) {
            return RawAnimation.begin().thenLoop("walk");
        }
        if (isDeadOrDying()) {
            return RawAnimation.begin().thenPlay("death");
        }
        return RawAnimation.begin().thenLoop("idle");
    }
}
