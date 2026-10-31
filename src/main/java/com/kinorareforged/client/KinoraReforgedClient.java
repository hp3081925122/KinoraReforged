package com.kinorareforged.client;

import com.kinorareforged.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class KinoraReforgedClient {
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TALK_1.get(), TalkScreen::new);
    }

    private KinoraReforgedClient() {
    }
}
