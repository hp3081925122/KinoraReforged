package com.kinorareforged;

import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

public final class KinoraTrades {
    public static void registerWanderingTrades(WandererTradesEvent event) {
        event.getGenericTrades().add(new BasicItemListing(new ItemStack(ModItems.PLANTATION_RUNE.get()), new ItemStack(ModItems.MANTYD_FLUTE.get()), 1, 25, 0.0F));
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide || !(event.getTarget() instanceof WanderingTrader trader)) {
            return;
        }
        if (!event.getItemStack().is(ModItems.PLANTATION_RUNE.get()) || !trader.isAlive() || trader.isTrading()) {
            return;
        }
        MerchantOffers offers = trader.getOffers();
        for (MerchantOffer offer : offers) {
            if (offer.getCostA().is(ModItems.PLANTATION_RUNE.get()) && offer.getResult().is(ModItems.MANTYD_FLUTE.get())) {
                return;
            }
        }
        offers.add(new MerchantOffer(new ItemCost(ModItems.PLANTATION_RUNE.get()), new ItemStack(ModItems.MANTYD_FLUTE.get()), 1, 25, 0.0F));
    }

    private KinoraTrades() {
    }
}
