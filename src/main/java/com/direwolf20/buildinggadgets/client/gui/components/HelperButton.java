package com.direwolf20.buildinggadgets.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;

public class HelperButton extends Button {
    private String helpText;

    public HelperButton(int widthIn, int heightIn, int width, int height, String text, String helpText, IPressable onPress) {
        super(widthIn, heightIn, width, height, text, onPress);
        this.helpText = helpText;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        super.render(mouseX, mouseY, partialTick);

        if( this.isHovered() )
            this.renderToolTip(mouseX, mouseY);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        drawString(Minecraft.getInstance().fontRenderer, this.helpText, mouseX, mouseY, Color.WHITE.getRGB());
    }
}
