package com.kinorareforged;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class KinoraPlayerEvents {
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !player.getPersistentData().contains("KinoraFluteTicks")) {
            return;
        }
        int ticks = player.getPersistentData().getInt("KinoraFluteTicks") - 1;
        if (!player.getPersistentData().getString("KinoraFluteDimension").equals(player.level().dimension().location().toString())) {
            player.getPersistentData().remove("KinoraFluteTicks");
            player.getPersistentData().remove("KinoraFluteDimension");
            return;
        }
        if (ticks > 0) {
            player.getPersistentData().putInt("KinoraFluteTicks", ticks);
            return;
        }
        player.getPersistentData().remove("KinoraFluteTicks");
        player.getPersistentData().remove("KinoraFluteDimension");
        if (!(player.level() instanceof ServerLevel server)) {
            return;
        }
        if (!player.isAlive()) {
            return;
        }
        MantydbossEchoEntity echo = ModEntities.MANTYD_BOSS_ECHO.get().spawn(server, BlockPos.containing(player.getX(), player.getY(), player.getZ()), net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED);
        if (echo != null) {
            float yaw = player.getYRot() + 5.0F;
            echo.setYRot(yaw);
            echo.setYBodyRot(yaw);
            echo.setYHeadRot(yaw);
            echo.setDeltaMovement(0.0D, 0.0D, 0.0D);
        }
    }

    private KinoraPlayerEvents() {
    }
}
