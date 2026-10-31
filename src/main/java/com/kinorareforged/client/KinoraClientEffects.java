package com.kinorareforged.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public final class KinoraClientEffects {
    public static void displayItemActivation(ItemStack stack) {
        Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
    }

    private KinoraClientEffects() {
    }
}
