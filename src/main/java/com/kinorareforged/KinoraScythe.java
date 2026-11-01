package com.kinorareforged;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class KinoraScythe {
    private static final String COMBO = "KinoraScytheCombo";
    private static final String WAVE = "KinoraScytheWaveTick";
    private static final int HITS = 6;
    private static final int INTERVAL = 2;

    public static boolean holdsScythe(Player player) {
        return player.getMainHandItem().is(ModItems.MANTYD_SCYTHE.get());
    }

    public static void startCombo(Player player) {
        KinoraPlayerData.get(player).comboAge(0);
        KinoraPlayerData.get(player).comboActive(true);
        player.getCooldowns().addCooldown(player.getMainHandItem(), KinoraConfig.SCYTHE_COMBO_COOLDOWN.get());
    }

    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !KinoraPlayerData.get(player).comboActive()) {
            return;
        }
        int age = KinoraPlayerData.get(player).comboAge();
        if (!player.isAlive() || !holdsScythe(player) || age > (HITS - 1) * INTERVAL) {
            KinoraPlayerData.get(player).comboAge(0);
            KinoraPlayerData.get(player).comboActive(false);
            return;
        }
        if (age % INTERVAL == 0) {
            comboHit(player, age / INTERVAL);
        }
        KinoraPlayerData.get(player).comboAge(age + 1);
    }

    private static void comboHit(Player player, int hit) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        float damage = (float) Math.max(0.0D, KinoraConfig.SCYTHE_COMBO_BASE_DAMAGE.get() + player.getAttributeValue(Attributes.ATTACK_DAMAGE) * KinoraConfig.SCYTHE_COMBO_ATTACK_RATIO.get());
        Vec3 look = player.getLookAngle();
        Vec3 flat = Vec3.directionFromRotation(0.0F, player.getYRot());
        Vec3 origin = player.getEyePosition().subtract(0.0D, 0.3D, 0.0D);
        double range = 3.5D;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(origin, origin).inflate(range + 1.0D), entity -> entity.isAlive() && !KinoraFx.isFriendly(player, entity))) {
            Vec3 to = target.getBoundingBox().getCenter().subtract(origin);
            double distance = to.length();
            double reach = range + target.getBbWidth() * 0.5D;
            if (distance > reach || distance > 1.2D && to.normalize().dot(look) < 0.35D) {
                continue;
            }
            target.invulnerableTime = 0;
            if (target.hurtServer(level, level.damageSources().playerAttack(player), damage)) {
                target.knockback(0.15D, -flat.x, -flat.z);
            }
        }
        player.swing(InteractionHand.MAIN_HAND, true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 1.4F + hit * 0.06F);
        float side = hit % 2 == 0 ? 1.0F : -1.0F;
        Vec3 front = origin.add(look.scale(1.3D)).add(0.0D, hit * 0.05D, 0.0D);
        KinoraFx.slash(level, front, player.getYRot() + side * (15.0F + level.getRandom().nextFloat() * 25.0F), player.getXRot(), side * (25.0F + level.getRandom().nextFloat() * 30.0F), 2.8F, 7);
        KinoraFx.sparks(level, front.add(look.scale(0.8D)), 2, 0.5D);
        if (hit == HITS - 1) {
            KinoraFx.ring(level, player.position().add(0.0D, 0.08D, 0.0D), 3.5F, 10);
            KinoraFx.petals(level, front, 12, 0.7D, 0.4D, 0.16D);
        }
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !holdsScythe(player) || player.getAttackStrengthScale(0.5F) < 0.9F) {
            return;
        }
        tryWave(player, event.getTarget());
    }

    public static void onWaveRequest(ServerPlayer player) {
        if (!holdsScythe(player) || player.isSpectator() || !player.isAlive()) {
            return;
        }
        if (tryWave(player, null)) {
            player.resetAttackStrengthTicker();
        }
    }

    private static boolean tryWave(Player player, net.minecraft.world.entity.Entity ignored) {
        long now = player.level().getGameTime();
        long last = KinoraPlayerData.get(player).waveTick();
        long delay = Math.max(1L, (long) (player.getCurrentItemAttackStrengthDelay() * 0.9F));
        if (now - last < delay && now >= last) {
            return false;
        }
        KinoraPlayerData.get(player).waveTick(now);
        ScytheWaveEntity.launch(player, ignored);
        return true;
    }

    private KinoraScythe() {
    }
}
