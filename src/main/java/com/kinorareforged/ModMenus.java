package com.kinorareforged;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, KinoraReforged.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<TalkMenu>> TALK_1 = MENUS.register("talk_1", () -> new MenuType<>(TalkMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

    private ModMenus() {
    }
}
