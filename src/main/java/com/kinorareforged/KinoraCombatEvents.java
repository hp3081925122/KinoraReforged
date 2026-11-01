package com.kinorareforged;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class KinoraCombatEvents {
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (victim instanceof BossMantydEntity boss && attacker instanceof Player player) {
            boss.addEffect(new MobEffectInstance(MobEffects.INSTANT_HEALTH, 20, 100, false, false));
            if (player instanceof ServerPlayer serverPlayer) {
                AdvancementHolder advancement = serverPlayer.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(KinoraReforged.MOD_ID, "achievement_kill_one"));
                if (advancement != null) {
                    AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                    for (String criterion : progress.getRemainingCriteria()) {
                        serverPlayer.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }

    private KinoraCombatEvents() {
    }
}
