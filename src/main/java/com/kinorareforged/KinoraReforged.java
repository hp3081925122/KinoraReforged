package com.kinorareforged;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(KinoraReforged.MOD_ID)
public final class KinoraReforged {
    public static final String MOD_ID = "kinora_reforged";

    public KinoraReforged(IEventBus modBus, ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, KinoraConfig.SPEC, "kinora_reforged-boss.toml");
        ModSounds.SOUNDS.register(modBus);
        ModItems.ARMOR_MATERIALS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModMenus.register(modBus);
        ModEntities.register(modBus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraPlayerEvents::playerTick);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraCombatEvents::onLivingDeath);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraTrades::registerWanderingTrades);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraTrades::onEntityInteract);
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            modBus.addListener(com.kinorareforged.client.KinoraRenderers::register);
            modBus.addListener(com.kinorareforged.client.KinoraReforgedClient::registerScreens);
            modBus.addListener(com.kinorareforged.client.MantydHelmetModel::registerLayers);
            modBus.addListener(com.kinorareforged.client.MantydHelmetModel::registerClientExtensions);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
