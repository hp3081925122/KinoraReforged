package com.kinorareforged.client;

import com.kinorareforged.TalkMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class TalkScreen extends AbstractContainerScreen<TalkMenu> {
    public TalkScreen(TalkMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 0;
        imageHeight = 0;
        titleLabelX = 0;
        titleLabelY = 0;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new PlainTextButton(leftPos - 19, topPos + 41, 46, 20, Component.translatable("gui.kinora_reforged.talk_1.button_text1"), button -> {}, font));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    }
}
