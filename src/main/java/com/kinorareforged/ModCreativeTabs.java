package com.kinorareforged;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KinoraReforged.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KINORA = TABS.register("mokels_boss_kinora", () -> CreativeModeTab.builder()
            .title(Component.translatable("item_group.kinora_reforged.mokels_boss_kinora"))
            .icon(() -> ModItems.ANCESTRAL_BAG.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.BOSS_MANTYD_SPAWN_EGG.get());
                output.accept(ModItems.MANTYD_BLOOM_SPAWN_EGG.get());
                output.accept(ModItems.MANTYD_SCYTHE.get());
                output.accept(ModItems.MANTYD_FLUTE.get());
                output.accept(ModItems.ANCESTRAL_BAG.get());
                output.accept(ModItems.MANTYD_HELMET.get());
                output.accept(ModItems.MANTYD_MEDICINE.get());
                output.accept(ModItems.PLANTATION_RUNE.get());
                output.accept(ModItems.MANTYD_BOSS_SPAWN_SPAWN_EGG.get());
                output.accept(ModItems.MANTYDBOSSDEAD_SPAWN_EGG.get());
                output.accept(ModItems.MANTYDBOSSSITING_SPAWN_EGG.get());
                output.accept(ModItems.MANTYD_BOSS_ECHO_SPAWN_EGG.get());
                output.accept(ModItems.MANTYDBOSSSITINGTWO_SPAWN_EGG.get());
            })
            .build());

    private ModCreativeTabs() {
    }
}
