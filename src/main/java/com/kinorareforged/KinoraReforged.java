package com.kinorareforged;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(KinoraReforged.MOD_ID)
public final class KinoraReforged {
    public static final String MOD_ID = "kinora_reforged";
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    public KinoraReforged(IEventBus modBus, ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, KinoraConfig.SPEC, "kinora_reforged-boss.toml");
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, KinoraClientConfig.SPEC, "kinora_reforged-client.toml");
        KinoraPlayerData.init();
        ATTACHMENTS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModParticles.register(modBus);
        modBus.addListener(KinoraNetwork::register);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModMenus.register(modBus);
        ModEntities.register(modBus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraPlayerEvents::playerTick);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraCombatEvents::onLivingDeath);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraTrades::onEntityInteract);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraScythe::playerTick);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(KinoraScythe::onAttackEntity);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            modBus.addListener(com.kinorareforged.client.KinoraRenderers::register);
            modBus.addListener(com.kinorareforged.client.KinoraReforgedClient::registerScreens);
            modBus.addListener(com.kinorareforged.client.MantydHelmetModel::registerLayers);
            modBus.addListener(com.kinorareforged.client.MantydHelmetModel::registerClientExtensions);
            modBus.addListener(com.kinorareforged.client.KinoraParticles::register);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.kinorareforged.client.KinoraCameraShake::clientTick);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.kinorareforged.client.KinoraCameraShake::computeAngles);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.kinorareforged.client.KinoraScytheClient::onAttackKey);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
