package com.direwolf20.buildinggadgets.client.screen.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class DireButton extends Button {

    public DireButton(int x, int y, int widthIn, int heightIn, Component buttonText, OnPress action) {
        super(x, y, widthIn, heightIn, buttonText, action);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            Font fontrenderer = Minecraft.getInstance().font;
            Minecraft.getInstance().getTextureManager().bindForSetup(WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.isHovered = isMouseOver(mouseX, mouseY);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.blit(matrices, this.x, this.y, 0, 46, this.width / 2, this.height);
            this.blit(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46, this.width / 2, this.height);


            int bottomToDraw = 2;
            this.blit(matrices, this.x, this.y + this.height - bottomToDraw, 0, 66 - bottomToDraw, this.width / 2, bottomToDraw);
            this.blit(matrices, this.x + this.width / 2, this.y + this.height - bottomToDraw, 200 - this.width / 2, 66 - bottomToDraw, this.width / 2, bottomToDraw);

            int j = 14737632;

            if (this.packedFGColor != 0) {
                j = this.packedFGColor;
            } else if (! this.active) {
                j = 10526880;
            } else if (this.isHovered) {
                j = 16777120;
            }

            this.drawCenteredString(matrices, fontrenderer, this.getMessage().getString(), this.x + this.width / 2, this.y + (this.height - 7) / 2, j);
        }
    }
}
