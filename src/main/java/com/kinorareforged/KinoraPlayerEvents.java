package com.kinorareforged;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class KinoraPlayerEvents {
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        KinoraPlayerData data = KinoraPlayerData.get(player);
        if (player.level().isClientSide() || data.fluteTicks() <= 0) return;
        int ticks = data.fluteTicks() - 1;
        if (!data.fluteDimension().equals(player.level().dimension().identifier().toString())) {
            data.fluteTicks(0);
            return;
        }
        if (ticks > 0) { data.fluteTicks(ticks); return; }
        data.fluteTicks(0);
        if (!(player.level() instanceof ServerLevel server) || !player.isAlive()) return;
        MantydbossEchoEntity echo = ModEntities.MANTYD_BOSS_ECHO.get().spawn(server, BlockPos.containing(player.getX(), player.getY(), player.getZ()), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (echo != null) {
            float yaw = player.getYRot() + 5.0F;
            echo.setYRot(yaw); echo.setYBodyRot(yaw); echo.setYHeadRot(yaw); echo.setDeltaMovement(0.0D, 0.0D, 0.0D);
        }
    }
    private KinoraPlayerEvents() {}
}
