package com.kinorareforged.client;

import com.kinorareforged.KinoraClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class KinoraCameraShake {
    private static final RandomSource RANDOM = RandomSource.create();
    private static float strength;
    private static int duration;
    private static int remaining;

    public static void start(float newStrength, int newDuration) {
        float scaled = newStrength * KinoraClientConfig.SCREEN_SHAKE.get().floatValue();
        if (scaled <= 0.0F || newDuration <= 0) {
            return;
        }
        if (remaining <= 0 || scaled >= currentStrength(0.0F)) {
            strength = scaled;
            duration = newDuration;
            remaining = newDuration;
        }
    }

    public static void clientTick(ClientTickEvent.Post event) {
        if (remaining > 0 && !Minecraft.getInstance().isPaused()) {
            remaining--;
        }
    }

    public static void computeAngles(ViewportEvent.ComputeCameraAngles event) {
        if (remaining <= 0) {
            return;
        }
        float amount = currentStrength((float) event.getPartialTick());
        event.setYaw(event.getYaw() + (RANDOM.nextFloat() - 0.5F) * 2.0F * amount);
        event.setPitch(event.getPitch() + (RANDOM.nextFloat() - 0.5F) * 2.0F * amount);
        event.setRoll(event.getRoll() + (RANDOM.nextFloat() - 0.5F) * 1.5F * amount);
    }

    private static float currentStrength(float partialTick) {
        if (duration <= 0) {
            return 0.0F;
        }
        float t = Mth.clamp((remaining - partialTick) / duration, 0.0F, 1.0F);
        return strength * t * t;
    }

    private KinoraCameraShake() {
    }
}
