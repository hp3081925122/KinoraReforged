package com.kinorareforged;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

public final class KinoraTrades {
    public static void registerWanderingTrades(WandererTradesEvent event) {
        event.getGenericTrades().add(new BasicItemListing(new ItemStack(ModItems.PLANTATION_RUNE.get()), new ItemStack(ModItems.MANTYD_FLUTE.get()), 1, 25, 0.0F));
    }

    private KinoraTrades() {
    }
}
