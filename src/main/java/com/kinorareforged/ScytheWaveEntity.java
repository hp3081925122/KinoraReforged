package com.kinorareforged;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ScytheWaveEntity extends Projectile {
    public static final int LIFETIME = 14;
    private final IntOpenHashSet hitEntities = new IntOpenHashSet();
    private float damage;
    private int ignoredEntity = -1;

    public ScytheWaveEntity(EntityType<? extends ScytheWaveEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static void launch(Player player, Entity ignored) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        ScytheWaveEntity wave = new ScytheWaveEntity(ModEntities.SCYTHE_WAVE.get(), level);
        wave.setOwner(player);
        wave.damage = (float) Math.max(0.0D, player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * KinoraConfig.SCYTHE_WAVE_DAMAGE_RATIO.get());
        wave.ignoredEntity = ignored == null ? -1 : ignored.getId();
        Vec3 look = player.getLookAngle();
        Vec3 start = player.getEyePosition().subtract(0.0D, 0.35D, 0.0D).add(look.scale(0.8D));
        wave.moveTo(start.x, start.y, start.z, player.getYRot(), player.getXRot());
        wave.setDeltaMovement(look.scale(1.15D));
        level.addFreshEntity(wave);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.3F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.6F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 start = position();
        Vec3 motion = getDeltaMovement();
        Vec3 end = start.add(motion);
        if (!level().isClientSide) {
            HitResult block = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (block.getType() != HitResult.Type.MISS) {
                end = block.getLocation();
                hitAlong(start, end);
                burst(end);
                discard();
                return;
            }
            hitAlong(start, end);
        } else if (tickCount % 2 == 0) {
            level().addParticle(ModParticles.PETAL.get(), getX() + (random.nextDouble() - 0.5D) * 1.6D, getY(), getZ() + (random.nextDouble() - 0.5D) * 1.6D, motion.x * 0.1D, 0.02D, motion.z * 0.1D);
        }
        setPos(end);
        if (!level().isClientSide && tickCount >= LIFETIME) {
            burst(end);
            discard();
        }
    }

    private void hitAlong(Vec3 start, Vec3 end) {
        Entity owner = getOwner();
        AABB area = new AABB(start, end).inflate(1.4D, 0.7D, 1.4D);
        for (Entity entity : level().getEntities(this, area, this::canHitEntity)) {
            if (entity.getId() == ignoredEntity || !hitEntities.add(entity.getId())) {
                continue;
            }
            if (entity.hurt(damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null), damage) && entity instanceof LivingEntity living) {
                Vec3 push = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize();
                living.knockback(0.35D, -push.x, -push.z);
            }
            if (level() instanceof ServerLevel server) {
                KinoraFx.sparks(server, entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D), 2, 0.3D);
            }
        }
    }

    private void burst(Vec3 pos) {
        if (level() instanceof ServerLevel server) {
            KinoraFx.petals(server, pos, 6, 0.6D, 0.2D, 0.08D);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator() && !KinoraFx.isFriendly(getOwner(), entity);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public float fade(float partialTick) {
        return Mth.clamp((LIFETIME - tickCount - partialTick) / 4.0F, 0.0F, 1.0F);
    }
}
